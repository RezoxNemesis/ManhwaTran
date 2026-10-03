package com.rezoxnemesis.muse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MuseBackground = Color(0xFF061108)
val MuseBackgroundRaised = Color(0xFF0B1B10)
val MuseSurface = Color(0xCC102817)
val MuseSurfaceStrong = Color(0xEE14331D)
val MuseGreen = Color(0xFF8BFF83)
val MuseGreenStrong = Color(0xFF55E85E)
val MuseBorder = Color(0x665CCB6A)
val MuseMuted = Color(0xFFAAC2AF)
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
