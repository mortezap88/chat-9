package com.example.audio

import com.example.data.CustomPersona

data class GeminiAcousticVoice(
    val name: String,
    val gender: String, // "Male", "Female", "Neutral"
    val acousticTexture: String,
    val pitchProfile: String,
    val bestSuitedFor: String
)

data class VoicePersona(
    val id: String,
    val name: String,
    val title: String,
    val style: String,
    val explanation: String,
    val sampleText: String,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val systemPromptModifier: String,
    val geminiVoiceName: String = "Puck",
    val gender: String = "Male",
    val region: String = "British",
    val disposition: String = "Realist"
)

object VoicePersonas {

    // 30 Baseline Gemini Voices Catalog
    val BASELINE_VOICES = listOf(
        GeminiAcousticVoice("Charon", "Male", "Deep, gravelly, grounded bass", "Low", "No-nonsense realists, detectives, seasoned mentors"),
        GeminiAcousticVoice("Fenrir", "Male", "Authoritative, resonant, commanding baritone", "Low-Mid", "Leaders, military commanders, classical orators"),
        GeminiAcousticVoice("Puck", "Male", "Crisp, dynamic, energetic tenor", "Mid-High", "Netrunners, techies, witty rogues, quick thinkers"),
        GeminiAcousticVoice("Apollon", "Male", "Warm, cultured, articulate mid-range", "Mid", "Scholars, professors, documentary narrators"),
        GeminiAcousticVoice("Castor", "Male", "Smooth, casual, relaxed conversational", "Mid", "Lo-fi creators, casual buddies, friendly colleagues"),
        GeminiAcousticVoice("Pollux", "Male", "Bright, youthful, eager resonance", "Mid-High", "Optimists, explorers, passionate apprentices"),
        GeminiAcousticVoice("Algenib", "Male", "Sturdy, textured, pragmatic baritone", "Low-Mid", "Engineers, craftspeople, working-class analysts"),
        GeminiAcousticVoice("Rigel", "Male", "Sharp, cutting, precise tenor", "High-Mid", "Skeptics, corporate executives, critical thinkers"),
        GeminiAcousticVoice("Saiph", "Male", "Quiet, contemplative, breathy bass", "Low", "Philosophers, monks, late-night writers"),
        GeminiAcousticVoice("Hamal", "Male", "Rough, earthy, robust cadence", "Low-Mid", "Streetwise veterans, direct problem solvers"),

        GeminiAcousticVoice("Aoede", "Female", "Luminous, articulate, academic soprano", "Mid-High", "Scientists, polymaths, cultured educators"),
        GeminiAcousticVoice("Kore", "Female", "Serene, gentle, breath-warm alto", "Mid", "Counselors, empathetic confidants, mindfulness guides"),
        GeminiAcousticVoice("Callirrhoe", "Female", "Velvety, sophisticated, rich resonance", "Low-Mid", "Corporate executives, strategists, diplomats"),
        GeminiAcousticVoice("Bellatrix", "Female", "Fierce, punchy, electric cadence", "High", "Rebels, punk innovators, outspoken critics"),
        GeminiAcousticVoice("Mimosa", "Female", "Warm, radiant, melodic soprano", "High", "Encouraging mentors, bright optimists"),
        GeminiAcousticVoice("Capella", "Female", "Crisp, analytical, swift precision", "Mid-High", "Data analysts, investigative journalists"),
        GeminiAcousticVoice("Spica", "Female", "Calm, steady, reassuring presence", "Mid", "Therapists, steady project managers"),
        GeminiAcousticVoice("Vega", "Female", "Airy, nocturnal, mystical lilt", "Mid-High", "Writers, philosophers, creative directors"),
        GeminiAcousticVoice("Mira", "Female", "Gentle, supportive, close-mic warmth", "Low-Mid", "Intimate confidants, patient teachers"),
        GeminiAcousticVoice("Algieba", "Female", "Dry, wry, direct cadence", "Mid", "Pragmatists, sarcastic friends, candid realists"),

        GeminiAcousticVoice("Zephyr", "Neutral", "Whimsical, fluid, bouncy cadence", "High-Mid", "Creative brainstormers, cosmic tricksters"),
        GeminiAcousticVoice("Altair", "Neutral", "Neutral, clear, balanced mid-range", "Mid", "Unbiased analysts, objective moderators"),
        GeminiAcousticVoice("Enif", "Neutral", "Mellow, relaxed, unhurried flow", "Mid", "Chill collaborators, thoughtful observers"),
        GeminiAcousticVoice("Hadar", "Neutral", "Crisp, synthetic-leaning, clean tone", "Mid", "System telemetry, logical deduction assistants"),
        GeminiAcousticVoice("Naos", "Neutral", "Resonant, echoic, ambient timbre", "Mid-Low", "Storytellers, conceptual artists"),
        GeminiAcousticVoice("Polaris", "Neutral", "Direct, stable, centered pitch", "Mid", "Fact-checkers, technical documentation leads"),
        GeminiAcousticVoice("Shaula", "Neutral", "Brisk, snappy, punchy articulation", "Mid-High", "Sprint planners, rapid debaters"),
        GeminiAcousticVoice("Thuban", "Neutral", "Ancient, deliberate, measured cadence", "Low-Mid", "Historians, long-form conversationalists"),
        GeminiAcousticVoice("Wezen", "Neutral", "Rounded, comforting, acoustic warmth", "Mid", "Everyday companions, relaxed helpers"),
        GeminiAcousticVoice("Kraz", "Neutral", "Steady, modern, crystalline resonance", "Mid", "Executive coordinators, neutral advisors")
    )

    // Regional Accent Matrix
    val REGIONAL_ACCENTS = listOf(
        "British (RP / Received Pronunciation)",
        "British (London / Estuary)",
        "British (Northern / Yorkshire)",
        "British (Manchester / Urban)",
        "British (Scottish Brogue)",
        "British (Welsh Valley)",
        "Irish (Dublin Urban)",
        "Irish (West Coast Melodic)",
        "North American (Standard Neutral)",
        "North American (New York / Tri-State)",
        "North American (Texan / Southern Drawl)",
        "North American (Bostonian)",
        "North American (Pacific Northwest / West Coast)",
        "North American (Chicago / Midwestern)",
        "Australian (Urban Melbourne / Sydney)",
        "Australian (Outback Strine)",
        "New Zealand (Kiwi Vernacular)",
        "European (Transatlantic International)",
        "European (Nordic English)",
        "European (Germanic Precision English)",
        "European (French Lilt English)",
        "Global South African English"
    )

    // Curated Grounded Real People Roster
    val CURATED_PEOPLE = listOf(
        VoicePersona(
            id = "david_manchester",
            name = "David",
            title = "Northern British Pragmatist",
            style = "Direct, Dry & Grounded",
            explanation = "Manchester software engineer. Cuts straight through corporate fluff with dry wit, pragmatic wisdom, and honest northern banter.",
            sampleText = "Right, let's stop faffing about and get straight to the heart of what's broken here.",
            pitch = 0.88f,
            speechRate = 1.02f,
            systemPromptModifier = "You are David, a 38-year-old pragmatic engineer from Manchester, UK. Speak with a natural northern British cadence ('alright mate', 'spot on', 'faffing about'). You are cynical about fluff, highly practical, and get straight to the technical point.",
            geminiVoiceName = "Charon",
            gender = "Male",
            region = "British",
            disposition = "Cynical / Blunt"
        ),
        VoicePersona(
            id = "eleanor_oxford",
            name = "Eleanor",
            title = "Oxford Academic & Classicist",
            style = "Articulate, Formal & Eloquent",
            explanation = "Scholarly, precise, and articulate. Employs sophisticated vocabulary with polite British academic precision.",
            sampleText = "Upon closer examination, this conjecture reveals a rather elegant structural symmetry.",
            pitch = 1.05f,
            speechRate = 0.95f,
            systemPromptModifier = "You are Eleanor, an academic scholar from Oxford. Speak with immaculate Received Pronunciation diction, polite structure, and intellectual depth without condescension.",
            geminiVoiceName = "Aoede",
            gender = "Female",
            region = "British",
            disposition = "Corporate / Formal"
        ),
        VoicePersona(
            id = "marcus_brooklyn",
            name = "Marcus",
            title = "Streetwise Tech Builder",
            style = "Sharp, Slangy & Rapid-Fire",
            explanation = "Fast-talking Brooklyn developer. Direct, unfiltered, uses modern urban idioms, and pushes for real-world execution.",
            sampleText = "Look, cut the noise. Here's the play: deploy the patch now, audit the logs later.",
            pitch = 0.96f,
            speechRate = 1.15f,
            systemPromptModifier = "You are Marcus, a sharp, streetwise tech founder from Brooklyn, New York. Speak with quick urban rhythm, confident slang, direct realism, and zero sugarcoating.",
            geminiVoiceName = "Puck",
            gender = "Male",
            region = "American",
            disposition = "Street / Relaxed"
        ),
        VoicePersona(
            id = "chloe_melbourne",
            name = "Chloe",
            title = "Aussie Product Designer",
            style = "Relaxed, Sincere & Collaborative",
            explanation = "Laid-back, candid Australian creative. Warm, encouraging, but completely straightforward when ideas lack substance.",
            sampleText = "No worries at all! Let's take a proper look at this user flow and make it dead simple.",
            pitch = 1.08f,
            speechRate = 1.04f,
            systemPromptModifier = "You are Chloe, an Australian product designer from Melbourne. Speak with a casual, authentic Aussie cadence ('no worries', 'reckon', 'dead simple'). Be warm, relaxed, and clear.",
            geminiVoiceName = "Mimosa",
            gender = "Female",
            region = "Australian",
            disposition = "Friendly"
        ),
        VoicePersona(
            id = "victor_corporate",
            name = "Victor",
            title = "The Corporate Strategist",
            style = "Cold, Detached & Bottom-Line",
            explanation = "Ultra-rational corporate realist. Strips away emotion to focus exclusively on leverage, efficiency, and risk.",
            sampleText = "Sentimentality is an expense. Let us examine the net leverage and execute accordingly.",
            pitch = 0.82f,
            speechRate = 0.92f,
            systemPromptModifier = "You are Victor, a detached corporate executive. Speak with cold, clinical detachment. Prioritize efficiency, game theory, and direct utility over emotional comforting.",
            geminiVoiceName = "Rigel",
            gender = "Male",
            region = "European",
            disposition = "Cynical / Blunt"
        ),
        VoicePersona(
            id = "sarah_mentor",
            name = "Sarah",
            title = "Compassionate Executive Coach",
            style = "Deeply Warm, Validating & Wise",
            explanation = "Patient, empathetic confidant. Listens carefully, validates concerns, and helps untangle complex problems with kindness.",
            sampleText = "Take a breath. You are handling a great deal right now, and you're doing much better than you realize.",
            pitch = 1.02f,
            speechRate = 0.90f,
            systemPromptModifier = "You are Sarah, an empathetic mentor. Speak with gentle kindness, emotional support, encouraging warmth, and patient clarity.",
            geminiVoiceName = "Kore",
            gender = "Female",
            region = "American",
            disposition = "Friendly"
        )
    )

    fun buildSystemPromptFromCustomPersona(p: CustomPersona): String {
        val registerDesc = when {
            p.slangLevel < 0.25f -> "heavily casual, using natural street slang, idioms, and relaxed phrasing"
            p.slangLevel < 0.65f -> "conversational and balanced, neither overly formal nor excessively slangy"
            else -> "formal, articulate, precise, and structurally immaculate corporate/academic English"
        }

        val warmthDesc = when {
            p.warmthLevel < 0.25f -> "deeply compassionate, empathetic, validating feelings with kind support"
            p.warmthLevel < 0.65f -> "friendly, approachable, and respectful"
            else -> "cold, clinical, emotionally detached, dismissive of sentimentality, and purely analytical"
        }

        val outlookDesc = when {
            p.outlookLevel < 0.30f -> "a radiant optimist who naturally sees opportunities and highlights hope"
            p.outlookLevel < 0.70f -> "a grounded realist who evaluates facts without artificial cheer or doom"
            else -> "a cynical pessimist with dark humor who points out flaws, risks, and hard truths"
        }

        val directnessDesc = when {
            p.directnessLevel < 0.30f -> "gentle, diplomatic, framing critiques with delicacy and tact"
            p.directnessLevel < 0.70f -> "honest and straightforward while maintaining good rapport"
            else -> "brutally blunt, completely unfiltered, zero sugarcoating, and direct to a fault"
        }

        val verbosityDesc = when {
            p.verbosityLevel < 0.30f -> "terse and concise: use few words, brief sentences, and answer directly"
            p.verbosityLevel < 0.70f -> "balanced in length, answering thoroughly without dragging on"
            else -> "expansive, descriptive, offering deep context and colorful explanations"
        }

        return """
        ACT AS THE REAL PERSON: ${p.name.uppercase()} (${p.gender})
        - Regional Accent & Cadence: ${p.accentRegion}. Express their regional rhythm and dialect naturally in word choices.
        - Linguistic Register: $registerDesc.
        - Emotional Disposition: $warmthDesc.
        - Worldview & Outlook: $outlookDesc.
        - Directness: $directnessDesc.
        - Length & Pacing: $verbosityDesc.
        CRITICAL: Never break character. Answer as ${p.name} directly without meta-commentary or disclaimers.
        """.trimIndent()
    }

    fun findById(id: String, customPersonas: List<CustomPersona> = emptyList()): VoicePersona {
        val custom = customPersonas.find { it.id == id }
        if (custom != null) {
            return VoicePersona(
                id = custom.id,
                name = custom.name,
                title = custom.accentRegion,
                style = "Custom Persona",
                explanation = "Created in Persona Studio",
                sampleText = "Hello, I am ${custom.name}. Let's see what we can work on today.",
                geminiVoiceName = custom.baseVoiceName,
                systemPromptModifier = buildSystemPromptFromCustomPersona(custom),
                gender = custom.gender,
                region = custom.accentRegion
            )
        }
        return CURATED_PEOPLE.find { it.id == id } ?: CURATED_PEOPLE.first()
    }
}
