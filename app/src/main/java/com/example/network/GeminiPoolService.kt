package com.example.network

import android.util.Base64
import android.util.Log
import com.example.data.ApiKeyItem
import com.example.data.AppSettings
import com.example.data.ChatMessage
import com.example.data.CorrectionType
import com.example.data.ExtractedMemoryItem
import com.example.data.KeyStatus
import com.example.data.LinguisticCorrection
import com.example.data.ThreePartParsedResponse
import com.example.data.UnifiedTurnUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiPoolService(
    private var apiKeys: List<ApiKeyItem> = emptyList(),
    var onKeyStatusChanged: ((Int, KeyStatus, String?) -> Unit)? = null
) {
    // OkHttpClient configured with extended timeouts to accommodate reasoning thinking time
    // and network/VPN route handshakes without prematurely dropping connections.
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS) // Accommodates internal thinking before first token emission
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    var activeKeyIndex: Int = 0
        private set

    fun updateKeys(newKeys: List<ApiKeyItem>) {
        apiKeys = newKeys
        if (activeKeyIndex >= apiKeys.size) activeKeyIndex = 0
    }

    private fun markKeyFailed(index: Int, reason: String) {
        if (index in apiKeys.indices) {
            onKeyStatusChanged?.invoke(index, KeyStatus.FAILED, reason)
        }
        if (apiKeys.isNotEmpty()) {
            activeKeyIndex = (activeKeyIndex + 1) % apiKeys.size
        }
    }

    private fun markKeySuccess(index: Int) {
        if (index in apiKeys.indices) {
            onKeyStatusChanged?.invoke(index, KeyStatus.WORKING, null)
        }
    }

    // Preserves the official AI Studio alias tags (gemini-flash-latest, gemini-pro-latest, gemini-flash-lite-latest)
    // Non-limited dynamic model IDs ensure stability over time without hardcoded version expiration.
    private fun resolveModelName(raw: String): String {
        val clean = raw.trim().removePrefix("models/")
        return if (clean.isNotBlank()) clean else "gemini-flash-latest"
    }

    /**
     * UNIFIED STREAMING PIPELINE:
     * One single request to Gemini handling text OR multimodal audio.
     * Extracts Part 1 (Answer), Part 2 (User Sentence), and Part 3 (Corrections).
     */
    fun streamUnifiedTurn(
        session: List<ChatMessage>,
        systemInstruction: String,
        settings: AppSettings,
        userText: String? = null,
        audioBase64: String? = null
    ): Flow<UnifiedTurnUpdate> = callbackFlow {
        if (apiKeys.isEmpty()) {
            close(Exception("No Gemini API keys configured. Open Settings to add your key."))
            return@callbackFlow
        }

        val targetModel = resolveModelName(settings.primaryModel)
        var attempts = 0
        var success = false
        var lastErrorMessage = "All available Gemini API keys failed"

        while (attempts < apiKeys.size && !success) {
            val keyIndex = activeKeyIndex
            val key = apiKeys[keyIndex].key
            try {
                val jsonBody = buildUnifiedRequestBody(
                    session = session,
                    systemInstruction = systemInstruction,
                    settings = settings,
                    targetModel = targetModel,
                    userText = userText,
                    audioBase64 = audioBase64
                )
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:streamGenerateContent?alt=sse&key=$key"

                val request = Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val err = response.body?.string().orEmpty()
                    val parsedMsg = try {
                        JSONObject(err).optJSONObject("error")?.optString("message", "")
                    } catch (_: Exception) { null }

                    val detail = if (!parsedMsg.isNullOrBlank()) parsedMsg else "HTTP ${response.code}"
                    lastErrorMessage = detail
                    Log.w("GeminiPool", "Unified model $targetModel returned ${response.code}: $err")
                    markKeyFailed(keyIndex, detail)
                    attempts++
                    continue
                }

                markKeySuccess(keyIndex)
                success = true

                val source = response.body?.byteStream()
                if (source != null) {
                    val reader = BufferedReader(InputStreamReader(source))
                    var line: String?
                    val rawAccumulated = StringBuilder()
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        if (currentLine.startsWith("data: ")) {
                            val data = currentLine.removePrefix("data: ").trim()
                            if (data == "[DONE]") break
                            try {
                                val root = JSONObject(data)
                                val candidates = root.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val candidate = candidates.getJSONObject(0)
                                    val content = candidate.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    if (parts != null) {
                                        for (p in 0 until parts.length()) {
                                            val part = parts.getJSONObject(p)
                                            val chunk = part.optString("text", "")
                                            if (chunk.isNotEmpty()) {
                                                rawAccumulated.append(chunk)
                                                val update = parseUnifiedResponse(rawAccumulated.toString(), isFinished = false)
                                                trySend(update)
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                    val finalUpdate = parseUnifiedResponse(rawAccumulated.toString(), isFinished = true)
                    trySend(finalUpdate)
                }
                close()
            } catch (e: Exception) {
                val errDetail = when (e) {
                    is UnknownHostException -> "Unable to reach Google servers. Please check your internet connection or VPN route."
                    is SocketTimeoutException -> "Connection timed out during model thinking/streaming. Please retry."
                    else -> e.message ?: "Connection error"
                }
                lastErrorMessage = errDetail
                Log.e("GeminiPool", "Unified streaming exception: $errDetail", e)
                markKeyFailed(keyIndex, lastErrorMessage)
                attempts++
            }
        }

        if (!success) {
            close(Exception(lastErrorMessage))
        }

        awaitClose {}
    }.flowOn(Dispatchers.IO)

    private fun cleanStreamingAnswer(text: String): String {
        val idx = text.indexOf("<<<")
        return if (idx >= 0) text.substring(0, idx).trimEnd() else text
    }

    private fun parseCorrectionsJson(raw: String): List<LinguisticCorrection> {
        val start = raw.indexOf('[')
        val end = raw.lastIndexOf(']')
        if (start >= 0 && end > start) {
            val jsonSubstring = raw.substring(start, end + 1)
            try {
                val arr = JSONArray(jsonSubstring)
                val list = mutableListOf<LinguisticCorrection>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val typeStr = obj.optString("type", "GRAMMAR").uppercase()
                    val type = if (typeStr.contains("WORD")) CorrectionType.WORD_CHOICE else CorrectionType.GRAMMAR
                    list.add(
                        LinguisticCorrection(
                            id = UUID.randomUUID().toString(),
                            type = type,
                            original = obj.optString("original", ""),
                            suggested = obj.optString("suggested", ""),
                            reason = obj.optString("reason", "")
                        )
                    )
                }
                return list
            } catch (_: Exception) {}
        }
        return emptyList()
    }

    private fun parseUnifiedResponse(rawText: String, isFinished: Boolean): UnifiedTurnUpdate {
        var currentAnswer = ""
        var currentUserSentence: String? = null
        var currentCorrections = emptyList<LinguisticCorrection>()

        if (rawText.contains("<<<USER_TRANSCRIPT>>>")) {
            val afterAnswer = if (rawText.contains("<<<ANSWER>>>")) {
                rawText.substringAfter("<<<ANSWER>>>")
            } else {
                rawText
            }
            currentAnswer = afterAnswer.substringBefore("<<<USER_TRANSCRIPT>>>").trim()

            val transcriptSection = afterAnswer.substringAfter("<<<USER_TRANSCRIPT>>>")
            val transcriptText = if (transcriptSection.contains("<<<CORRECTIONS>>>")) {
                transcriptSection.substringBefore("<<<CORRECTIONS>>>").trim()
            } else {
                if (isFinished) transcriptSection.trim() else cleanStreamingAnswer(transcriptSection).trim()
            }
            if (transcriptText.isNotBlank()) {
                currentUserSentence = transcriptText
            }

            if (transcriptSection.contains("<<<CORRECTIONS>>>")) {
                val corrRaw = transcriptSection.substringAfter("<<<CORRECTIONS>>>").trim()
                val parsed = parseCorrectionsJson(corrRaw)
                if (parsed.isNotEmpty()) {
                    currentCorrections = parsed
                }
            }
        } else {
            val ans = if (rawText.contains("<<<ANSWER>>>")) {
                rawText.substringAfter("<<<ANSWER>>>")
            } else {
                rawText
            }
            currentAnswer = if (isFinished) ans.trim() else cleanStreamingAnswer(ans)
        }

        return UnifiedTurnUpdate(
            answer = currentAnswer,
            userSentence = currentUserSentence,
            corrections = currentCorrections,
            isComplete = isFinished
        )
    }

    private fun buildUnifiedRequestBody(
        session: List<ChatMessage>,
        systemInstruction: String,
        settings: AppSettings,
        targetModel: String,
        userText: String?,
        audioBase64: String?
    ): JSONObject {
        val root = JSONObject()

        val formattingGuidance = """
Respond strictly adhering to this 3-part layout:
<<<ANSWER>>>
[Your direct conversational answer to the user]
<<<USER_TRANSCRIPT>>>
[The exact user sentence uttered in the audio or typed by user]
<<<CORRECTIONS>>>
[{"type":"GRAMMAR"|"WORD_CHOICE","original":"faulty substring","suggested":"correction","reason":"explanation"}]
(Note: If there are no grammar or word choice issues, output [] for CORRECTIONS)
""".trimIndent()

        val fullSystemInstruction = if (systemInstruction.isNotBlank()) {
            "$systemInstruction\n\n$formattingGuidance"
        } else {
            formattingGuidance
        }

        val sysObj = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().apply { put("text", fullSystemInstruction) })
        sysObj.put("parts", sysParts)
        root.put("systemInstruction", sysObj)

        val contentsArr = JSONArray()

        // Include completed past turns
        val pastCompletedTurns = session.dropLast(1)
            .filter { it.userText.isNotBlank() && it.aiResponse.isNotBlank() }
            .takeLast(12)

        for (turn in pastCompletedTurns) {
            contentsArr.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", turn.userText)))
            })
            contentsArr.put(JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", turn.aiResponse)))
            })
        }

        // Current turn
        val currentParts = JSONArray()
        if (!audioBase64.isNullOrBlank()) {
            currentParts.put(JSONObject().apply {
                val inline = JSONObject().apply {
                    put("mimeType", "audio/wav")
                    put("data", audioBase64)
                }
                put("inlineData", inline)
            })
            val audioPrompt = if (!userText.isNullOrBlank()) {
                "User attached audio and notes: \"$userText\". Listen carefully to the audio, provide your answer in ANSWER, transcribe the user's speech verbatim in USER_TRANSCRIPT, and check grammar in CORRECTIONS."
            } else {
                "Listen carefully to the audio, converse naturally, transcribe the user's spoken utterance verbatim in USER_TRANSCRIPT, and inspect grammar in CORRECTIONS."
            }
            currentParts.put(JSONObject().apply { put("text", audioPrompt) })
        } else {
            val textPrompt = (userText ?: "").trim()
            currentParts.put(JSONObject().apply { put("text", textPrompt) })
        }

        contentsArr.put(JSONObject().apply {
            put("role", "user")
            put("parts", currentParts)
        })

        root.put("contents", contentsArr)

        val generationConfig = JSONObject()
        if (settings.thinkingEnabled && !targetModel.contains("lite", ignoreCase = true)) {
            val thinkingLevelValue = when (settings.thinkingLevel.lowercase().trim()) {
                "low" -> "low"
                "high" -> "high"
                else -> "medium"
            }
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", thinkingLevelValue)
            generationConfig.put("thinkingConfig", thinkingConfig)
        }
        root.put("generationConfig", generationConfig)

        return root
    }

    fun streamGenerateContent(
        session: List<ChatMessage>,
        systemInstruction: String,
        settings: AppSettings
    ): Flow<String> = callbackFlow {
        if (apiKeys.isEmpty()) {
            close(Exception("No Gemini API keys configured. Open Settings to add your key."))
            return@callbackFlow
        }

        val targetModel = resolveModelName(settings.primaryModel)
        var attempts = 0
        var success = false
        var lastErrorMessage = "All available Gemini API keys failed"

        while (attempts < apiKeys.size && !success) {
            val keyIndex = activeKeyIndex
            val key = apiKeys[keyIndex].key
            try {
                val jsonBody = buildRequestBody(session, systemInstruction, settings, targetModel)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:streamGenerateContent?alt=sse&key=$key"

                val request = Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response: Response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val err = response.body?.string().orEmpty()
                    val parsedMsg = try {
                        JSONObject(err).optJSONObject("error")?.optString("message", "")
                    } catch (_: Exception) { null }

                    val detail = if (!parsedMsg.isNullOrBlank()) parsedMsg else "HTTP ${response.code}"
                    lastErrorMessage = detail
                    Log.w("GeminiPool", "Model $targetModel returned ${response.code}: $err")
                    markKeyFailed(keyIndex, detail)
                    attempts++
                    continue
                }

                markKeySuccess(keyIndex)
                success = true

                val source = response.body?.byteStream()
                if (source != null) {
                    val reader = BufferedReader(InputStreamReader(source))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        if (currentLine.startsWith("data: ")) {
                            val data = currentLine.removePrefix("data: ").trim()
                            if (data == "[DONE]") break
                            try {
                                val root = JSONObject(data)
                                val candidates = root.optJSONArray("candidates")
                                if (candidates != null && candidates.length() > 0) {
                                    val candidate = candidates.getJSONObject(0)
                                    val content = candidate.optJSONObject("content")
                                    val parts = content?.optJSONArray("parts")
                                    if (parts != null) {
                                        for (p in 0 until parts.length()) {
                                            val part = parts.getJSONObject(p)
                                            val text = part.optString("text", "")
                                            if (text.isNotEmpty()) {
                                                trySend(text)
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
                close()
            } catch (e: Exception) {
                val errDetail = when (e) {
                    is UnknownHostException -> "Unable to reach Google servers. Please check your internet connection or VPN route."
                    is SocketTimeoutException -> "Connection timed out during model thinking/streaming. Please retry."
                    else -> e.message ?: "Connection error"
                }
                lastErrorMessage = errDetail
                Log.e("GeminiPool", "Streaming request exception: $errDetail", e)
                markKeyFailed(keyIndex, lastErrorMessage)
                attempts++
            }
        }

        if (!success) {
            close(Exception(lastErrorMessage))
        }

        awaitClose {}
    }.flowOn(Dispatchers.IO)

    private fun buildRequestBody(
        session: List<ChatMessage>,
        systemInstruction: String,
        settings: AppSettings,
        targetModel: String
    ): JSONObject {
        val root = JSONObject()

        if (systemInstruction.isNotBlank()) {
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().apply { put("text", systemInstruction) })
            sysObj.put("parts", sysParts)
            root.put("systemInstruction", sysObj)
        }

        val contentsArr = JSONArray()

        // Include completed turns so roles strictly alternate user -> model
        val pastCompletedTurns = session.dropLast(1)
            .filter { it.userText.isNotBlank() && it.aiResponse.isNotBlank() }
            .takeLast(12)

        for (turn in pastCompletedTurns) {
            val userContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", turn.userText)))
            }
            val modelContent = JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", turn.aiResponse)))
            }
            contentsArr.put(userContent)
            contentsArr.put(modelContent)
        }

        // Add current user prompt
        val currentTurn = session.lastOrNull()
        if (currentTurn != null && currentTurn.userText.isNotBlank()) {
            val userContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", currentTurn.userText)))
            }
            contentsArr.put(userContent)
        }

        root.put("contents", contentsArr)

        val generationConfig = JSONObject()

        // Standard Gemini 3 thinking config per official AI Studio documentation:
        // uses thinkingLevel ("low", "medium", "high").
        if (settings.thinkingEnabled && !targetModel.contains("lite", ignoreCase = true)) {
            val thinkingLevelValue = when (settings.thinkingLevel.lowercase().trim()) {
                "low" -> "low"
                "high" -> "high"
                else -> "medium"
            }
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", thinkingLevelValue)
            generationConfig.put("thinkingConfig", thinkingConfig)
        }

        root.put("generationConfig", generationConfig)
        return root
    }

    /**
     * Speech-to-Text Audio Transcription:
     * Powered by Gemini 3.5 Flash-Lite through Google's non-limited dynamic model alias
     * "gemini-flash-lite-latest". This ensures continuous updates without model ID deprecation.
     */
    suspend fun transcribeAudio(
        wavBase64: String,
        modelName: String = "gemini-flash-lite-latest",
        contextualSmoothing: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKeys.isEmpty()) return@withContext Result.failure(Exception("No API key available"))

        val targetModel = resolveModelName(modelName)
        val prompt = if (contextualSmoothing) {
            "Transcribe the spoken audio verbatim. Smooth subtle speech boundaries and punctuation for natural reading, but preserve colloquial words and any raw grammatical errors exactly as uttered. Return ONLY the transcription text."
        } else {
            "Transcribe the spoken audio verbatim. Return ONLY the exact spoken transcription."
        }

        var attempts = 0
        while (attempts < apiKeys.size) {
            val keyIndex = activeKeyIndex
            val key = apiKeys[keyIndex].key
            try {
                val root = JSONObject()
                val contents = JSONArray()
                val content = JSONObject().apply {
                    put("role", "user")
                    val parts = JSONArray()
                    parts.put(JSONObject().apply {
                        val inline = JSONObject().apply {
                            put("mimeType", "audio/wav")
                            put("data", wavBase64)
                        }
                        put("inlineData", inline)
                    })
                    parts.put(JSONObject().apply { put("text", prompt) })
                    put("parts", parts)
                }
                contents.put(content)
                root.put("contents", contents)

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$key"
                val request = Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(root.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val resp = client.newCall(request).execute()
                val body = resp.body?.string().orEmpty()
                if (resp.isSuccessful) {
                    markKeySuccess(keyIndex)
                    val json = JSONObject(body)
                    val text = json.optJSONArray("candidates")
                        ?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text", "")
                        .orEmpty()
                    return@withContext Result.success(text.trim())
                } else {
                    markKeyFailed(keyIndex, "HTTP ${resp.code}")
                    attempts++
                }
            } catch (e: Exception) {
                markKeyFailed(keyIndex, e.message ?: "Transcription error")
                attempts++
            }
        }
        Result.failure(Exception("Failed to transcribe audio across all keys"))
    }

    suspend fun inspectGrammarAndLinguistics(
        userText: String,
        modelName: String = "gemini-flash-lite-latest"
    ): Result<ThreePartParsedResponse> = withContext(Dispatchers.IO) {
        if (apiKeys.isEmpty() || userText.isBlank()) {
            return@withContext Result.success(ThreePartParsedResponse(mainAnswer = userText))
        }

        val targetModel = resolveModelName(modelName)
        val prompt = """
        Analyze this user sentence for grammar, awkward syntax, or vocabulary errors:
        "$userText"

        Return ONLY a JSON object with:
        {
          "polishedText": "A more natural version of the sentence",
          "corrections": [
            {
              "type": "GRAMMAR" or "WORD_CHOICE",
              "original": "the exact faulty substring",
              "suggested": "the correction",
              "reason": "1 short sentence explaining why"
            }
          ]
        }
        If the text is already completely natural with no mistakes, return {"polishedText": null, "corrections": []}.
        """.trimIndent()

        try {
            val key = apiKeys[activeKeyIndex].key
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().apply { put("text", prompt) })
                put("parts", parts)
            }
            contents.put(content)
            root.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$key"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val resp = client.newCall(request).execute()
            val body = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val json = JSONObject(body)
                val rawText = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "")
                    .orEmpty()

                val start = rawText.indexOf('{')
                val end = rawText.lastIndexOf('}')
                if (start >= 0 && end > start) {
                    val parsed = JSONObject(rawText.substring(start, end + 1))
                    val polished = if (parsed.has("polishedText") && !parsed.isNull("polishedText")) parsed.getString("polishedText") else null
                    val corrArr = parsed.optJSONArray("corrections") ?: JSONArray()
                    val corrList = mutableListOf<LinguisticCorrection>()
                    for (i in 0 until corrArr.length()) {
                        val c = corrArr.getJSONObject(i)
                        corrList.add(
                            LinguisticCorrection(
                                id = UUID.randomUUID().toString(),
                                type = try {
                                    CorrectionType.valueOf(c.optString("type", "GRAMMAR"))
                                } catch (_: Exception) { CorrectionType.GRAMMAR },
                                original = c.optString("original", ""),
                                suggested = c.optString("suggested", ""),
                                reason = c.optString("reason", "")
                            )
                        )
                    }
                    return@withContext Result.success(ThreePartParsedResponse(mainAnswer = userText, polishedText = polished, corrections = corrList))
                }
            }
        } catch (e: Exception) {
            Log.w("GeminiPool", "Grammar inspection exception: ${e.message}")
        }
        Result.success(ThreePartParsedResponse(mainAnswer = userText))
    }

    suspend fun consolidateSessionIntoMemories(
        session: List<ChatMessage>,
        model: String = "gemini-flash-lite-latest"
    ): Result<List<ExtractedMemoryItem>> = withContext(Dispatchers.IO) {
        if (apiKeys.isEmpty() || session.isEmpty()) return@withContext Result.success(emptyList())

        val targetModel = resolveModelName(model)
        val prompt = buildString {
            appendLine("Analyze the following conversation context and extract 2 to 8 atomic, permanent memories about the user (their facts, preferences, projects, style, or personal context).")
            appendLine("Format your output strictly as a JSON array of objects with the keys: content, category (FACT, PREFERENCE, PROJECT, LINGUISTIC, EPISODIC, GENERAL), importance (1-5), keywords (array of 2-4 strings).")
            appendLine("Conversation Transcript:")
            session.takeLast(25).forEach {
                appendLine("User: ${it.userText}")
                appendLine("AI: ${it.aiResponse.take(200)}")
            }
        }

        try {
            val key = apiKeys[activeKeyIndex].key
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().apply { put("text", prompt) })
                put("parts", parts)
            }
            contents.put(content)
            root.put("contents", contents)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$key"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val resp = client.newCall(request).execute()
            val raw = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val json = JSONObject(raw)
                val rawText = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "")
                    .orEmpty()

                val start = rawText.indexOf('[')
                val end = rawText.lastIndexOf(']')
                if (start >= 0 && end > start) {
                    val jsonArr = JSONArray(rawText.substring(start, end + 1))
                    val items = mutableListOf<ExtractedMemoryItem>()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        val kwArr = obj.optJSONArray("keywords")
                        val kws = mutableListOf<String>()
                        if (kwArr != null) {
                            for (k in 0 until kwArr.length()) kws.add(kwArr.getString(k))
                        }
                        items.add(
                            ExtractedMemoryItem(
                                content = obj.optString("content", ""),
                                category = obj.optString("category", "GENERAL"),
                                importance = obj.optInt("importance", 3),
                                keywords = kws
                            )
                        )
                    }
                    return@withContext Result.success(items)
                }
            }
        } catch (e: Exception) {
            Log.w("GeminiPool", "Consolidation extraction error: ${e.message}")
        }
        Result.success(emptyList())
    }

    suspend fun generateTtsAudio(
        text: String,
        voiceName: String,
        model: String = "gemini-flash-latest"
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        if (apiKeys.isEmpty()) return@withContext Result.failure(Exception("No API key available"))

        try {
            val key = apiKeys[activeKeyIndex].key
            val targetModel = resolveModelName(model)
            val root = JSONObject()
            val contents = JSONArray()
            val content = JSONObject().apply {
                put("role", "user")
                val parts = JSONArray()
                parts.put(JSONObject().apply { put("text", "Read aloud the following text exactly using voice $voiceName: $text") })
                put("parts", parts)
            }
            contents.put(content)
            root.put("contents", contents)

            val genConfig = JSONObject()
            genConfig.put("responseModalities", JSONArray().put("AUDIO"))
            val speechConfig = JSONObject().apply {
                val voiceConfig = JSONObject().apply {
                    val prebuilt = JSONObject().apply {
                        put("voiceName", voiceName)
                    }
                    put("prebuiltVoiceConfig", prebuilt)
                }
                put("voiceConfig", voiceConfig)
            }
            genConfig.put("speechConfig", speechConfig)
            root.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$key"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val resp = client.newCall(request).execute()
            val body = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val json = JSONObject(body)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val base64Data = inlineData.optString("data", "")
                            if (base64Data.isNotEmpty()) {
                                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                                return@withContext Result.success(bytes)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("GeminiPool", "Audio generation fallback: ${e.message}")
        }
        Result.failure(Exception("TTS audio generation not supported directly; falling back"))
    }
}
