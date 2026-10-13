package com.example.data

import java.util.UUID

enum class KeyStatus {
    IDLE,
    WORKING,
    FAILED
}

data class ApiKeyItem(
    val id: String = UUID.randomUUID().toString(),
    val key: String,
    val label: String,
    val status: KeyStatus = KeyStatus.IDLE,
    val errorMessage: String? = null,
    val failCount: Int = 0
) {
    val maskedKey: String
        get() {
            if (key.length <= 10) return "••••"
            val start = key.take(6)
            val end = key.takeLast(4)
            return "$start••••$end"
        }
}

enum class CorrectionType {
    GRAMMAR,
    WORD_CHOICE
}

data class LinguisticCorrection(
    val id: String = UUID.randomUUID().toString(),
    val type: CorrectionType,
    val original: String,
    val suggested: String,
    val reason: String
)

data class ThreePartParsedResponse(
    val mainAnswer: String,
    val polishedText: String? = null,
    val corrections: List<LinguisticCorrection> = emptyList()
)

data class UnifiedTurnUpdate(
    val answer: String = "",
    val userSentence: String? = null,
    val corrections: List<LinguisticCorrection> = emptyList(),
    val isComplete: Boolean = false
)

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val userText: String,
    val isAudioInput: Boolean = false,
    val aiResponse: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-flash-latest",
    val corrections: List<LinguisticCorrection> = emptyList(),
    val isAnalyzingCorrections: Boolean = false,
    val isWebSearchEnhanced: Boolean = false,
    val polishedText: String? = null
)

data class CustomPersona(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val gender: String, // "Male", "Female", "Neutral"
    val baseVoiceName: String, // Gemini baseline voice
    val accentRegion: String,
    val slangLevel: Float = 0.5f,      // 0 = Street Slang, 1 = Polished Corporate
    val warmthLevel: Float = 0.5f,     // 0 = Deeply Compassionate, 1 = Cold / Sociopath
    val outlookLevel: Float = 0.5f,    // 0 = Radiant Optimist, 1 = Cynical Pessimist
    val directnessLevel: Float = 0.5f, // 0 = Diplomatic, 1 = Brutally Blunt
    val verbosityLevel: Float = 0.5f,  // 0 = Terse, 1 = Expansive
    val customSystemPrompt: String = ""
)

data class AppSettings(
    val primaryModel: String = "gemini-flash-latest",
    val thinkingEnabled: Boolean = false,
    val thinkingLevel: String = "Low", // "Low", "Medium", "High" (per AI Studio documentation)
    val tokenStreaming: Boolean = true,
    val transcriptionModel: String = "gemini-flash-lite-latest",
    val smoothingModel: String = "gemini-flash-lite-latest",
    val contextualSmoothing: Boolean = true,
    val grammarInspector: Boolean = true,
    val micSensitivity: String = "Low",
    val silencePauseDuration: Float = 2.0f,
    val maxFontSizeSp: Int = 24,
    val longTermMemoryEnabled: Boolean = true,
    val maxRetrievedMemories: Int = 4,
    val vibrationPattern: String = "ocean_surge",
    val minVoiceHoldDurationSeconds: Float = 1.0f,
    val ttsModel: String = "gemini-flash-latest",
    val selectedVoicePersona: String = "david_manchester",
    val isMicEnabled: Boolean = true,
    val ttsAutoSpeak: Boolean = false,
    val dedicatedTtsModelActive: Boolean = true,
    val tavilySearchEnabled: Boolean = false,
    val tavilyThoroughness: String = "Balanced",
    val tavilyMaxResults: Int = 5
)

data class ExtractedMemoryItem(
    val content: String,
    val category: String,
    val importance: Int,
    val keywords: List<String>
)

data class AudioRecordingResult(
    val fullAudioBase64: String,
    val finalChunkBase64: String? = null,
    val mimeType: String = "audio/wav",
    val durationMs: Long
)
