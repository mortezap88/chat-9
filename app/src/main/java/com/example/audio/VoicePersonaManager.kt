package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.network.GeminiPoolService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class VoicePersonaManager(
    private val context: Context,
    private val poolService: GeminiPoolService
) {
    var currentlySpeakingPersonaId by mutableStateOf<String?>(null)
        private set

    var isLoadingPersonaId by mutableStateOf<String?>(null)
        private set

    var currentlySpeakingMessageId by mutableStateOf<String?>(null)
        private set

    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var isTtsInitialized = false

    init {
        try {
            androidTts = TextToSpeech(context) { status ->
                isTtsInitialized = (status == TextToSpeech.SUCCESS)
                if (isTtsInitialized) {
                    androidTts?.language = Locale.US
                }
            }
        } catch (e: Exception) {
            Log.e("VoicePersonaManager", "Error init TTS: ${e.message}")
        }
    }

    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            if (isTtsInitialized) {
                androidTts?.stop()
            }
        } catch (_: Exception) {}

        currentlySpeakingPersonaId = null
        isLoadingPersonaId = null
        currentlySpeakingMessageId = null
    }

    fun previewPersona(
        persona: VoicePersona,
        ttsModel: String,
        scope: CoroutineScope
    ) {
        stop()
        isLoadingPersonaId = persona.id

        scope.launch {
            val audioResult = poolService.generateTtsAudio(
                text = persona.sampleText,
                voiceName = persona.geminiVoiceName,
                model = ttsModel
            )

            isLoadingPersonaId = null

            audioResult.onSuccess { audioBytes ->
                playAudioBytes(audioBytes, persona.id)
            }.onFailure {
                fallbackSpeak(persona.sampleText, persona.pitch, persona.speechRate, persona.id)
            }
        }
    }

    fun speakChatMessage(
        messageId: String,
        text: String,
        persona: VoicePersona,
        ttsModel: String,
        scope: CoroutineScope
    ) {
        stop()
        currentlySpeakingMessageId = messageId
        isLoadingPersonaId = persona.id

        scope.launch {
            val audioResult = poolService.generateTtsAudio(
                text = text.take(600),
                voiceName = persona.geminiVoiceName,
                model = ttsModel
            )

            isLoadingPersonaId = null

            audioResult.onSuccess { audioBytes ->
                playAudioBytes(audioBytes, persona.id, messageId)
            }.onFailure {
                fallbackSpeak(text.take(600), persona.pitch, persona.speechRate, persona.id, messageId)
            }
        }
    }

    private fun playAudioBytes(
        audioBytes: ByteArray,
        personaId: String,
        messageId: String? = null
    ) {
        try {
            val tempFile = File.createTempFile("tts_preview_", ".mp3", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            val player = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                setOnCompletionListener {
                    currentlySpeakingPersonaId = null
                    currentlySpeakingMessageId = null
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                }
                setOnErrorListener { _, _, _ ->
                    currentlySpeakingPersonaId = null
                    currentlySpeakingMessageId = null
                    release()
                    mediaPlayer = null
                    tempFile.delete()
                    true
                }
                prepare()
                start()
            }
            mediaPlayer = player
            currentlySpeakingPersonaId = personaId
            currentlySpeakingMessageId = messageId
        } catch (e: Exception) {
            Log.e("VoicePersonaManager", "Play audio error: ${e.message}")
            currentlySpeakingPersonaId = null
            currentlySpeakingMessageId = null
        }
    }

    private fun fallbackSpeak(
        text: String,
        pitch: Float,
        speechRate: Float,
        personaId: String,
        messageId: String? = null
    ) {
        if (!isTtsInitialized || androidTts == null) {
            currentlySpeakingPersonaId = null
            currentlySpeakingMessageId = null
            return
        }
        try {
            androidTts?.setPitch(pitch)
            androidTts?.setSpeechRate(speechRate)
            currentlySpeakingPersonaId = personaId
            currentlySpeakingMessageId = messageId

            val utteranceId = "tts_${System.currentTimeMillis()}"
            androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    currentlySpeakingPersonaId = null
                    currentlySpeakingMessageId = null
                }
                override fun onError(utteranceId: String?) {
                    currentlySpeakingPersonaId = null
                    currentlySpeakingMessageId = null
                }
            })
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e("VoicePersonaManager", "Fallback TTS error: ${e.message}")
            currentlySpeakingPersonaId = null
            currentlySpeakingMessageId = null
        }
    }
}
