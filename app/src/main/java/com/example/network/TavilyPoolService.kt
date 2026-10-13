package com.example.network

import android.util.Log
import com.example.data.ApiKeyItem
import com.example.data.KeyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TavilyPoolService(
    private var apiKeys: List<ApiKeyItem> = emptyList()
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private var activeKeyIndex = 0

    fun updateKeys(newKeys: List<ApiKeyItem>) {
        apiKeys = newKeys
        if (activeKeyIndex >= apiKeys.size) activeKeyIndex = 0
    }

    suspend fun searchWeb(
        query: String,
        thoroughness: String = "Balanced",
        maxResults: Int = 5
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKeys.isEmpty()) {
            return@withContext Result.failure(Exception("No Tavily API keys configured"))
        }

        val searchDepth = when (thoroughness.lowercase()) {
            "fast" -> "basic"
            "comprehensive" -> "advanced"
            else -> "basic"
        }

        val numResults = when (thoroughness.lowercase()) {
            "fast" -> 2
            "comprehensive" -> maxResults.coerceAtLeast(6)
            else -> maxResults.coerceIn(3, 5)
        }

        val jsonBody = JSONObject().apply {
            put("query", query)
            put("search_depth", searchDepth)
            put("include_answer", true)
            put("max_results", numResults)
        }

        var attempts = 0
        while (attempts < apiKeys.size) {
            val keyItem = apiKeys[activeKeyIndex]
            try {
                val request = Request.Builder()
                    .url("https://api.tavily.com/search")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer ${keyItem.key}")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val code = response.code
                val responseBody = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val root = JSONObject(responseBody)
                    val directAnswer = root.optString("answer", "")
                    val results = root.optJSONArray("results") ?: JSONArray()

                    val formatted = buildString {
                        if (directAnswer.isNotBlank()) {
                            appendLine("Summary: $directAnswer")
                            appendLine()
                        }
                        appendLine("Sources:")
                        for (i in 0 until results.length().coerceAtMost(numResults)) {
                            val r = results.getJSONObject(i)
                            val title = r.optString("title", "Source")
                            val content = r.optString("content", "")
                            val url = r.optString("url", "")
                            appendLine("- [$title]($url): ${content.take(240)}...")
                        }
                    }
                    return@withContext Result.success(formatted)
                } else {
                    Log.w("TavilyPool", "Key ${keyItem.label} failed with code $code: $responseBody")
                    activeKeyIndex = (activeKeyIndex + 1) % apiKeys.size
                    attempts++
                }
            } catch (e: Exception) {
                Log.e("TavilyPool", "Error querying Tavily: ${e.message}")
                activeKeyIndex = (activeKeyIndex + 1) % apiKeys.size
                attempts++
            }
        }
        Result.failure(Exception("All Tavily keys failed or exhausted"))
    }
}
