package com.rezoxnemesis.muse.ui

import androidx.compose.ui.graphics.Color

internal enum class MuseVisualProfile(
    val storedValue: String,
    val label: String,
    val description: String,
) {
    VerdantRain(
        storedValue = "verdant_rain",
        label = "Verdant Rain",
        description = "Wet botanical depth, dew and forest-green light",
    ),
    AuroraGlass(
        storedValue = "aurora_glass",
        label = "Aurora Glass",
        description = "Cool luminous glass with an aurora-like glow",
    ),
    MidnightEmber(
        storedValue = "midnight_ember",
        label = "Midnight Ember",
        description = "Near-black forest glass with restrained warm embers",
    ),
    MoonlitViolet(
        storedValue = "moonlit_violet",
        label = "Moonlit Violet",
        description = "Soft violet moonlight through the botanical canopy",
    ),
    OceanPulse(
        storedValue = "ocean_pulse",
        label = "Ocean Pulse",
        description = "Deep teal atmosphere with electric aqua energy",
    ),
    RoseNoir(
        storedValue = "rose_noir",
        label = "Rose Noir",
        description = "Dark botanical glass with muted rose illumination",
    ),
}

internal enum class MuseVisualMode(
    val storedValue: String,
    val label: String,
    val fixedProfile: MuseVisualProfile?,
) {
    Auto("auto", "Auto", null),
    VerdantRain(
        MuseVisualProfile.VerdantRain.storedValue,
        MuseVisualProfile.VerdantRain.label,
        MuseVisualProfile.VerdantRain,
    ),
    AuroraGlass(
        MuseVisualProfile.AuroraGlass.storedValue,
        MuseVisualProfile.AuroraGlass.label,
        MuseVisualProfile.AuroraGlass,
    ),
    MidnightEmber(
        MuseVisualProfile.MidnightEmber.storedValue,
        MuseVisualProfile.MidnightEmber.label,
        MuseVisualProfile.MidnightEmber,
    ),
    MoonlitViolet(
        MuseVisualProfile.MoonlitViolet.storedValue,
        MuseVisualProfile.MoonlitViolet.label,
        MuseVisualProfile.MoonlitViolet,
    ),
    OceanPulse(
        MuseVisualProfile.OceanPulse.storedValue,
        MuseVisualProfile.OceanPulse.label,
        MuseVisualProfile.OceanPulse,
    ),
    RoseNoir(
        MuseVisualProfile.RoseNoir.storedValue,
        MuseVisualProfile.RoseNoir.label,
        MuseVisualProfile.RoseNoir,
    );

    companion object {
        fun fromStored(value: String?): MuseVisualMode =
            entries.firstOrNull { it.storedValue == value } ?: VerdantRain
    }
}

internal enum class MuseRainLevel(
    val storedValue: String,
    val label: String,
    val dropCount: Int,
    val speedMultiplier: Float,
) {
    Off("off", "Off", 0, 0f),
    Mist("mist", "Mist", 24, 0.72f),
    Rain("rain", "Rain", 48, 1f),
    Downpour("downpour", "Downpour", 82, 1.34f);

    companion object {
        fun fromStored(value: String?): MuseRainLevel =
            entries.firstOrNull { it.storedValue == value } ?: Rain
    }
}

internal fun resolveMuseVisualProfile(
    mode: MuseVisualMode,
    descriptor: String,
): MuseVisualProfile {
    mode.fixedProfile?.let { return it }

    val text = descriptor.lowercase()
    fun containsAny(vararg words: String): Boolean = words.any(text::contains)

    return when {
        containsAny("love", "romance", "romantic", "heart", "soul", "r&b", "ballad") ->
            MuseVisualProfile.RoseNoir
        containsAny("sleep", "night", "moon", "piano", "classical", "ambient", "meditation") ->
            MuseVisualProfile.MoonlitViolet
        containsAny("edm", "electronic", "house", "trance", "techno", "synth", "club") ->
            MuseVisualProfile.OceanPulse
        containsAny("rock", "metal", "workout", "gym", "power", "fire", "punk") ->
            MuseVisualProfile.MidnightEmber
        containsAny("chill", "dream", "dreamy", "acoustic", "indie", "pop", "lofi", "lo-fi") ->
            MuseVisualProfile.AuroraGlass
        else -> MuseVisualProfile.VerdantRain
    }
}

internal fun MuseVisualProfile.previewColors(): List<Color> =
    when (this) {
        MuseVisualProfile.VerdantRain -> listOf(
            Color(0xFF020604),
            Color(0xFF0A3218),
            Color(0xFF8DFF62),
        )
        MuseVisualProfile.AuroraGlass -> listOf(
            Color(0xFF030912),
            Color(0xFF133B43),
            Color(0xFF8CF7D4),
        )
        MuseVisualProfile.MidnightEmber -> listOf(
            Color(0xFF080403),
            Color(0xFF3B1710),
            Color(0xFFFFA45D),
        )
        MuseVisualProfile.MoonlitViolet -> listOf(
            Color(0xFF05040C),
            Color(0xFF26153D),
            Color(0xFFC5A2FF),
        )
        MuseVisualProfile.OceanPulse -> listOf(
            Color(0xFF001014),
            Color(0xFF073B47),
            Color(0xFF58E9F5),
        )
        MuseVisualProfile.RoseNoir -> listOf(
            Color(0xFF090307),
            Color(0xFF421525),
            Color(0xFFFF91BA),
        )
    }
