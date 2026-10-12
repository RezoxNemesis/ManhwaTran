package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A small, genuinely live interpretation of each V4 environment, not a
 * screenshot or a static recoloured gradient. The real world foliage drawer
 * supplies the same geometry, leaf veins and attached droplets as the full UI.
 * Motion stays bounded for the horizontally scrolling Muse Lab carousel.
 */
internal enum class MuseWorldPreviewMotif {
    BotanicalDew,
    RefractedFacets,
    CharredSparks,
    LunarHalo,
    TidalRipples,
    VelvetPetals,
}

internal data class MuseWorldPreviewPolicy(
    val motif: MuseWorldPreviewMotif,
    val waterLanguage: MuseWaterLanguage,
    val rainStreaks: Int,
    val cycleMillis: Int,
    val motionScale: Float,
    val reactiveGlow: Float,
)

internal fun resolveMuseWorldPreviewPolicy(
    profile: MuseVisualProfile,
    rainLevel: MuseRainLevel,
    intensity: MuseVisualIntensity,
    isPlaying: Boolean,
    audioEnergy: Float,
): MuseWorldPreviewPolicy {
    val motif = when (profile) {
        MuseVisualProfile.VerdantRain -> MuseWorldPreviewMotif.BotanicalDew
        MuseVisualProfile.AuroraGlass -> MuseWorldPreviewMotif.RefractedFacets
        MuseVisualProfile.MidnightEmber -> MuseWorldPreviewMotif.CharredSparks
        MuseVisualProfile.MoonlitViolet -> MuseWorldPreviewMotif.LunarHalo
        MuseVisualProfile.OceanPulse -> MuseWorldPreviewMotif.TidalRipples
        MuseVisualProfile.RoseNoir -> MuseWorldPreviewMotif.VelvetPetals
    }
    val basePeriod = when (motif) {
        MuseWorldPreviewMotif.BotanicalDew -> 6_400
        MuseWorldPreviewMotif.RefractedFacets -> 7_600
        MuseWorldPreviewMotif.CharredSparks -> 5_000
        MuseWorldPreviewMotif.LunarHalo -> 11_500
        MuseWorldPreviewMotif.TidalRipples -> 6_200
        MuseWorldPreviewMotif.VelvetPetals -> 9_300
    }
    val rainStreaks = if (profile == MuseVisualProfile.VerdantRain) {
        when (rainLevel) {
            MuseRainLevel.Off -> 0
            MuseRainLevel.Mist -> 1
            MuseRainLevel.Rain -> 3
            MuseRainLevel.Downpour -> 5
        }
    } else {
        0
    }
    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.42f
        MuseVisualIntensity.Balanced -> 0.68f
        MuseVisualIntensity.Vivid -> 1f
    }
    val safeEnergy = if (audioEnergy.isFinite()) {
        audioEnergy.coerceIn(0f, 1f)
    } else {
        0f
    }
    return MuseWorldPreviewPolicy(
        motif = motif,
        waterLanguage = profile.worldExperience().waterLanguage,
        rainStreaks = rainStreaks,
        cycleMillis = if (isPlaying) basePeriod else basePeriod * 2,
        motionScale = intensityScale * if (isPlaying) 1f else 0.38f,
        reactiveGlow = if (isPlaying) safeEnergy * 0.30f else 0f,
    )
}

@Composable
internal fun MuseWorldScenePreview(
    profile: MuseVisualProfile,
    rainLevel: MuseRainLevel,
    intensity: MuseVisualIntensity,
    isPlaying: Boolean,
    audioEnergy: Float,
    modifier: Modifier = Modifier,
) {
    val style = remember(profile) { profile.livingWorldStyle() }
    val chrome = remember(profile) { profile.chromePalette() }
    val colors = remember(profile) { profile.previewColors() }
    val policy = resolveMuseWorldPreviewPolicy(
        profile = profile,
        rainLevel = rainLevel,
        intensity = intensity,
        isPlaying = isPlaying,
        audioEnergy = audioEnergy,
    )
    val clock = rememberInfiniteTransition(label = "MuseLabPreviewClock")
    val phase by clock.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = policy.cycleMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseLabPreviewPhase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )

        val light = Offset(size.width * 0.74f, size.height * 0.14f)
        val lightRadius = size.width * 0.60f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    chrome.highlight.copy(alpha = 0.21f + policy.reactiveGlow),
                    chrome.glow.copy(alpha = 0.080f + policy.reactiveGlow * 0.45f),
                    Color.Transparent,
                ),
                center = light,
                radius = lightRadius,
            ),
            center = light,
            radius = lightRadius,
        )

        // Three distant sources of bokeh move on their own shallow depth plane.
        repeat(3) { index ->
            val x = (0.17f + index * 0.31f) * size.width
            val y = (0.13f + index % 2 * 0.22f +
                sin(phase * 2f * PI.toFloat() + index) *
                0.045f * policy.motionScale) * size.height
            val r = size.minDimension * (0.12f + index * 0.025f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.highlight.copy(alpha = 0.11f - index * 0.02f),
                        Color.Transparent,
                    ),
                    center = Offset(x, y),
                    radius = r,
                ),
                center = Offset(x, y),
                radius = r,
            )
        }

        drawMusePreviewWorldSignature(policy, phase, chrome)
        drawMuseWorldPreviewFoliage(
            style = style,
            phase = phase,
            motionScale = policy.motionScale,
        )

        // This scrim protects small native labels without flattening the
        // independently animated environment beneath the glass card.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0x60010604),
                    Color(0xE8000704),
                ),
                startY = size.height * 0.33f,
                endY = size.height,
            ),
        )
    }
}

private fun DrawScope.drawMusePreviewWorldSignature(
    policy: MuseWorldPreviewPolicy,
    phase: Float,
    chrome: MuseChromePalette,
) {
    val tau = 2f * PI.toFloat()
    val strength = policy.motionScale
    when (policy.motif) {
        MuseWorldPreviewMotif.BotanicalDew -> {
            repeat(policy.rainStreaks) { index ->
                val x = size.width * (0.14f + index * 0.18f)
                val y = size.height * ((phase + index * 0.23f) % 1f)
                drawLine(
                    color = chrome.highlight.copy(alpha = 0.085f * strength),
                    start = Offset(x, y),
                    end = Offset(x - size.width * 0.018f, y + size.height * 0.14f),
                    strokeWidth = 0.7.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        MuseWorldPreviewMotif.RefractedFacets -> {
            repeat(3) { index ->
                val travel = sin(phase * tau + index * 0.8f) * size.width * 0.07f * strength
                drawLine(
                    color = chrome.highlight.copy(alpha = 0.24f - index * 0.055f),
                    start = Offset(size.width * (0.16f + index * 0.23f) + travel, 0f),
                    end = Offset(size.width * (0.50f + index * 0.17f) + travel, size.height * 0.70f),
                    strokeWidth = (1.2f + index * 0.3f).dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        MuseWorldPreviewMotif.CharredSparks -> {
            repeat(5) { index ->
                val p = (phase + index * 0.19f) % 1f
                val x = size.width * (0.17f + ((index * 19) % 5) * 0.15f)
                val y = size.height * (0.66f - p * 0.57f * strength)
                drawCircle(
                    color = chrome.highlight.copy(alpha = (1f - p) * 0.46f),
                    radius = size.minDimension * (0.010f + index % 2 * 0.007f),
                    center = Offset(x, y),
                )
            }
        }
        MuseWorldPreviewMotif.LunarHalo -> {
            val center = Offset(size.width * 0.69f, size.height * 0.22f)
            val radius = size.minDimension * (0.26f + 0.012f * sin(phase * tau) * strength)
            drawCircle(
                color = chrome.highlight.copy(alpha = 0.24f),
                center = center,
                radius = radius,
                style = Stroke(width = 1.5.dp.toPx()),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(chrome.highlight.copy(alpha = 0.12f), Color.Transparent),
                    center = center,
                    radius = radius * 1.45f,
                ),
                center = center,
                radius = radius * 1.45f,
            )
        }
        MuseWorldPreviewMotif.TidalRipples -> {
            repeat(3) { index ->
                val p = (phase + index * 0.32f) % 1f
                drawCircle(
                    color = chrome.highlight.copy(alpha = (1f - p) * 0.26f),
                    center = Offset(size.width * 0.62f, size.height * 0.24f),
                    radius = size.minDimension * (0.07f + p * 0.49f * strength),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        MuseWorldPreviewMotif.VelvetPetals -> {
            repeat(4) { index ->
                val angle = phase * tau * strength + index * (tau / 4f)
                val pivot = Offset(
                    size.width * (0.60f + cos(angle) * 0.22f),
                    size.height * (0.30f + sin(angle) * 0.17f),
                )
                rotate(angle * 180f / PI.toFloat(), pivot) {
                    drawOval(
                        color = chrome.highlight.copy(alpha = 0.16f),
                        topLeft = Offset(pivot.x - 5.dp.toPx(), pivot.y - 10.dp.toPx()),
                        size = Size(10.dp.toPx(), 20.dp.toPx()),
                    )
                }
            }
        }
    }
}
