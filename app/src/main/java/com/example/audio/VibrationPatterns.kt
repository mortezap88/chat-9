package com.example.audio

data class VibrationPattern(
    val id: String,
    val name: String,
    val persona: String,
    val timings: LongArray,
    val amplitudes: IntArray,
    val repeatIndex: Int = 0
)

object VibrationPatterns {
    val ALL = listOf(
        VibrationPattern(
            id = "ocean_surge",
            name = "Ocean Surge",
            persona = "Gradual rhythmic swells mimicking tidal breathing",
            timings = longArrayOf(0, 150, 100, 250, 100, 350, 200, 100),
            amplitudes = intArrayOf(0, 40, 0, 120, 0, 220, 0, 60),
            repeatIndex = 1
        ),
        VibrationPattern(
            id = "heartbeat",
            name = "Heartbeat",
            persona = "Dual-pulse cadence recreating calm biological pulse",
            timings = longArrayOf(0, 80, 80, 120, 500),
            amplitudes = intArrayOf(0, 180, 0, 240, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "gentle_whisper",
            name = "Gentle Whisper",
            persona = "Subtle, low-amplitude micro-buzzes",
            timings = longArrayOf(0, 60, 200, 60, 400),
            amplitudes = intArrayOf(0, 50, 0, 70, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "cyber_haptic",
            name = "Cyber Haptic",
            persona = "Crisp, snappy mechanical switch clicks",
            timings = longArrayOf(0, 35, 70, 35, 70, 45, 300),
            amplitudes = intArrayOf(0, 255, 0, 200, 0, 255, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "zen_bell",
            name = "Zen Bell",
            persona = "Sharp initial strike followed by long decaying resonance",
            timings = longArrayOf(0, 40, 100, 100, 150, 150, 400),
            amplitudes = intArrayOf(0, 255, 120, 70, 40, 15, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "rolling_thunder",
            name = "Rolling Thunder",
            persona = "Rumbling low-frequency growl with peak bursts",
            timings = longArrayOf(0, 200, 50, 300, 80, 200, 300),
            amplitudes = intArrayOf(0, 90, 0, 210, 0, 140, 0),
            repeatIndex = 1
        ),
        VibrationPattern(
            id = "staccato_tap",
            name = "Staccato Tap",
            persona = "Rapid, brisk Morse-code like micro pulsations",
            timings = longArrayOf(0, 40, 60, 40, 60, 40, 400),
            amplitudes = intArrayOf(0, 170, 0, 170, 0, 220, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "cosmic_drift",
            name = "Cosmic Drift",
            persona = "Slow sine-like wave undulating smoothly",
            timings = longArrayOf(0, 300, 100, 300, 100),
            amplitudes = intArrayOf(0, 80, 0, 160, 0),
            repeatIndex = 1
        ),
        VibrationPattern(
            id = "metronome",
            name = "Metronome",
            persona = "Steady, unhurried tempo marks every second",
            timings = longArrayOf(0, 50, 450, 50, 450),
            amplitudes = intArrayOf(0, 200, 0, 200, 0),
            repeatIndex = 0
        ),
        VibrationPattern(
            id = "purring_kitten",
            name = "Purr",
            persona = "Continuous ultra-fine vibration harmonic vibration",
            timings = longArrayOf(0, 80, 40, 80, 40, 80, 150),
            amplitudes = intArrayOf(0, 70, 20, 90, 20, 70, 0),
            repeatIndex = 1
        )
    )

    fun findById(id: String): VibrationPattern {
        return ALL.find { it.id == id } ?: ALL.first()
    }
}
