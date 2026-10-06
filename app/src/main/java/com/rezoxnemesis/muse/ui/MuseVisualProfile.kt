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


internal data class MuseChromePalette(
    val primary: Color,
    val primaryStrong: Color,
    val glow: Color,
    val rim: Color,
    val glassBase: Color,
    val glassStrong: Color,
    val glassElevated: Color,
    val glassSelected: Color,
    val toggleOff: Color,
    val highlight: Color,
)

internal fun MuseVisualProfile.chromePalette(): MuseChromePalette =
    when (this) {
        MuseVisualProfile.VerdantRain -> MuseChromePalette(
            primary = Color(0xFF9BFF8D),
            primaryStrong = Color(0xFF59F064),
            glow = Color(0xFF8EFF73),
            rim = Color(0xFF84FF8B),
            glassBase = Color(0xFF0B2614),
            glassStrong = Color(0xFF0D2A18),
            glassElevated = Color(0xFF11321C),
            glassSelected = Color(0xFF21592E),
            toggleOff = Color(0xFF173520),
            highlight = Color(0xFFD8FF9F),
        )
        MuseVisualProfile.AuroraGlass -> MuseChromePalette(
            primary = Color(0xFF8CF7D4),
            primaryStrong = Color(0xFF4EDCC7),
            glow = Color(0xFF79FFE5),
            rim = Color(0xFF82DCE4),
            glassBase = Color(0xFF09242A),
            glassStrong = Color(0xFF0B3036),
            glassElevated = Color(0xFF103943),
            glassSelected = Color(0xFF195A61),
            toggleOff = Color(0xFF16343A),
            highlight = Color(0xFFDCFFF5),
        )
        MuseVisualProfile.MidnightEmber -> MuseChromePalette(
            primary = Color(0xFFFFB072),
            primaryStrong = Color(0xFFFF7A48),
            glow = Color(0xFFFFA45D),
            rim = Color(0xFFFF8B5E),
            glassBase = Color(0xFF2A130D),
            glassStrong = Color(0xFF351711),
            glassElevated = Color(0xFF432015),
            glassSelected = Color(0xFF63301D),
            toggleOff = Color(0xFF34231C),
            highlight = Color(0xFFFFE4C8),
        )
        MuseVisualProfile.MoonlitViolet -> MuseChromePalette(
            primary = Color(0xFFC5A2FF),
            primaryStrong = Color(0xFFA572FF),
            glow = Color(0xFFD1B5FF),
            rim = Color(0xFFB893FF),
            glassBase = Color(0xFF1C102D),
            glassStrong = Color(0xFF25143A),
            glassElevated = Color(0xFF311B48),
            glassSelected = Color(0xFF4B2C69),
            toggleOff = Color(0xFF2A2036),
            highlight = Color(0xFFF0E8FF),
        )
        MuseVisualProfile.OceanPulse -> MuseChromePalette(
            primary = Color(0xFF58E9F5),
            primaryStrong = Color(0xFF20BED1),
            glow = Color(0xFF79F4FF),
            rim = Color(0xFF5BD8E7),
            glassBase = Color(0xFF08262D),
            glassStrong = Color(0xFF0A3139),
            glassElevated = Color(0xFF0C3C46),
            glassSelected = Color(0xFF145661),
            toggleOff = Color(0xFF17343A),
            highlight = Color(0xFFDCFCFF),
        )
        MuseVisualProfile.RoseNoir -> MuseChromePalette(
            primary = Color(0xFFFF91BA),
            primaryStrong = Color(0xFFE95791),
            glow = Color(0xFFFFA3C5),
            rim = Color(0xFFF17AA8),
            glassBase = Color(0xFF2A101C),
            glassStrong = Color(0xFF351421),
            glassElevated = Color(0xFF42192A),
            glassSelected = Color(0xFF60243D),
            toggleOff = Color(0xFF38202B),
            highlight = Color(0xFFFFE4EE),
        )
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
