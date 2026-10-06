package com.rezoxnemesis.muse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MuseBackground = Color(0xFF030805)
val MuseBackgroundRaised = Color(0xFF07120A)
val MuseSurface = Color(0xC70A1C10)
val MuseSurfaceStrong = Color(0xEA0C2414)
val MuseGlass = Color(0xC70A1C10)
val MuseGlassStrong = Color(0xE20B2112)
val MuseGlassElevated = Color(0xEA0E2917)
val MuseGreen = Color(0xFF9BFF8D)
val MuseGreenStrong = Color(0xFF59F064)
val MuseGlow = Color(0xFF8EFF73)
val MuseGlowSoft = Color(0x665CFF6B)
val MuseBorder = Color(0x6F65C96D)
val MuseBorderBright = Color(0xB784FF8B)
val MuseMuted = Color(0xFFB8C9BB)
val MuseError = Color(0xFFFF6B6B)
val MuseWarning = Color(0xFFFFC857)

private val MuseColorScheme = darkColorScheme(
    primary = MuseGreen,
    onPrimary = Color(0xFF071208),
    secondary = MuseGreenStrong,
    background = MuseBackground,
    onBackground = Color.White,
    surface = MuseSurface,
    onSurface = Color.White,
    surfaceVariant = MuseBackgroundRaised,
    onSurfaceVariant = MuseMuted,
    error = MuseError,
)

@Composable
fun MuseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MuseColorScheme,
        typography = Typography(),
        content = content,
    )
}
