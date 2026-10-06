package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The living-atmosphere layer deliberately sits behind Muse's real Compose UI.
 * It gives every visual profile its own motion grammar instead of turning one
 * layout into six recolours.
 */
@Composable
internal fun MuseProfileAtmosphereOverlay(
    profile: MuseVisualProfile,
    behavior: MuseAtmosphereBehavior,
    active: Boolean,
    intensity: MuseVisualIntensity,
    modifier: Modifier = Modifier,
) {
    val chrome = profile.chromePalette()
    val transition = rememberInfiniteTransition(label = "MuseProfileAtmosphere")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (behavior.kind) {
                    MuseAtmosphereKind.BotanicalRain -> if (active) 6_600 else 10_400
                    MuseAtmosphereKind.AuroraRibbons -> if (active) 7_200 else 11_800
                    MuseAtmosphereKind.EmberDrift -> if (active) 5_800 else 9_600
                    MuseAtmosphereKind.MoonMist -> if (active) 10_500 else 15_500
                    MuseAtmosphereKind.OceanRefraction -> if (active) 5_600 else 9_200
                    MuseAtmosphereKind.RoseBloom -> if (active) 8_200 else 12_800
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseProfilePhase",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.76f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 2_900 else 5_600,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseProfileBreathe",
    )

    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.72f
        MuseVisualIntensity.Balanced -> 1f
        MuseVisualIntensity.Vivid -> 1.22f
    }
    val musicLift = if (active) {
        1f + behavior.musicResponse * 0.16f * breathe
    } else {
        0.78f
    }

    Canvas(modifier) {
        val strength = intensityScale * musicLift
        when (behavior.kind) {
            MuseAtmosphereKind.BotanicalRain -> {
                repeat(10) { index ->
                    val xSeed = ((index * 37 + 13) % 101) / 100f
                    val ySeed = ((index * 29 + 17) % 97) / 96f
                    val drift = sin((phase * 2f * PI + index * 0.8).toFloat())
                    val center = Offset(
                        x = size.width * (xSeed + drift * 0.006f * behavior.foliageMotion),
                        y = size.height * ySeed,
                    )
                    val radius = size.minDimension * (0.006f + (index % 3) * 0.0018f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.16f * strength),
                                chrome.highlight.copy(alpha = 0.10f * strength),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = radius * 2.3f,
                        ),
                        center = center,
                        radius = radius * 2.3f,
                    )
                }
            }

            MuseAtmosphereKind.AuroraRibbons -> {
                repeat(3) { ribbon ->
                    val yBase = size.height * (0.16f + ribbon * 0.18f)
                    val wave = sin((phase * 2f * PI + ribbon * 1.7).toFloat())
                    val path = Path().apply {
                        moveTo(-size.width * 0.08f, yBase)
                        cubicTo(
                            size.width * 0.22f,
                            yBase + size.height * (0.08f + wave * 0.018f),
                            size.width * 0.48f,
                            yBase - size.height * (0.09f - wave * 0.014f),
                            size.width * 0.72f,
                            yBase + size.height * 0.025f,
                        )
                        cubicTo(
                            size.width * 0.88f,
                            yBase + size.height * (0.10f + wave * 0.018f),
                            size.width * 1.04f,
                            yBase - size.height * 0.06f,
                            size.width * 1.12f,
                            yBase + size.height * 0.03f,
                        )
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                chrome.primary.copy(
                                    alpha = 0.09f *
                                        behavior.auroraRibbonStrength *
                                        strength,
                                ),
                                chrome.highlight.copy(
                                    alpha = 0.16f *
                                        behavior.auroraRibbonStrength *
                                        strength,
                                ),
                                chrome.glow.copy(
                                    alpha = 0.08f *
                                        behavior.auroraRibbonStrength *
                                        strength,
                                ),
                                Color.Transparent,
                            )
                        ),
                        style = Stroke(
                            width = (22f + ribbon * 8f).dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            }

            MuseAtmosphereKind.EmberDrift -> {
                repeat(24) { index ->
                    val xSeed = ((index * 43 + 9) % 101) / 100f
                    val ySeed = ((index * 31 + 5) % 109) / 108f
                    val speed = 0.26f + ((index * 17) % 31) / 100f
                    val y = (ySeed - phase * speed + 1.08f) % 1.08f
                    val sway = sin((phase * 2f * PI + index).toFloat()) * 0.016f
                    val center = Offset(
                        size.width * (xSeed + sway),
                        size.height * y,
                    )
                    val radius = (0.85f + (index % 4) * 0.38f).dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                chrome.highlight.copy(
                                    alpha = 0.72f * behavior.emberStrength * strength,
                                ),
                                chrome.primaryStrong.copy(
                                    alpha = 0.36f * behavior.emberStrength * strength,
                                ),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = radius * 3.2f,
                        ),
                        center = center,
                        radius = radius * 3.2f,
                    )
                }
            }

            MuseAtmosphereKind.MoonMist -> {
                val moon = Offset(size.width * 0.80f, size.height * 0.10f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            chrome.highlight.copy(
                                alpha = 0.13f * behavior.mistStrength * strength,
                            ),
                            chrome.glow.copy(
                                alpha = 0.055f * behavior.mistStrength * strength,
                            ),
                            Color.Transparent,
                        ),
                        center = moon,
                        radius = size.minDimension * 0.34f,
                    ),
                    center = moon,
                    radius = size.minDimension * 0.34f,
                )
                repeat(4) { band ->
                    val drift = ((phase + band * 0.21f) % 1f) - 0.5f
                    val center = Offset(
                        size.width * (0.50f + drift * 0.24f),
                        size.height * (0.30f + band * 0.17f),
                    )
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                chrome.primary.copy(
                                    alpha = 0.055f *
                                        behavior.mistStrength *
                                        strength,
                                ),
                                chrome.glassElevated.copy(alpha = 0.025f * strength),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = size.width * 0.54f,
                        ),
                        topLeft = Offset(
                            center.x - size.width * 0.48f,
                            center.y - size.height * 0.055f,
                        ),
                        size = Size(size.width * 0.96f, size.height * 0.11f),
                    )
                }
            }

            MuseAtmosphereKind.OceanRefraction -> {
                repeat(5) { ring ->
                    val local = (phase + ring * 0.17f) % 1f
                    val center = Offset(
                        size.width * (0.52f + sin((ring + phase) * PI).toFloat() * 0.05f),
                        size.height * (0.34f + ring * 0.11f),
                    )
                    val radiusX = size.width * (0.08f + local * 0.38f)
                    val radiusY = size.height * (0.018f + local * 0.070f)
                    drawOval(
                        color = chrome.primary.copy(
                            alpha = (1f - local) *
                                0.15f *
                                behavior.refractionStrength *
                                strength,
                        ),
                        topLeft = Offset(center.x - radiusX, center.y - radiusY),
                        size = Size(radiusX * 2f, radiusY * 2f),
                        style = Stroke(
                            width = (0.8f + ring * 0.14f).dp.toPx(),
                        ),
                    )
                }

                repeat(6) { beam ->
                    val shift = sin((phase * 2f * PI + beam).toFloat()) * size.width * 0.025f
                    drawLine(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                chrome.highlight.copy(
                                    alpha = 0.065f *
                                        behavior.refractionStrength *
                                        strength,
                                ),
                                Color.Transparent,
                            )
                        ),
                        start = Offset(size.width * (0.05f + beam * 0.19f) + shift, 0f),
                        end = Offset(
                            size.width * (0.18f + beam * 0.16f) + shift,
                            size.height,
                        ),
                        strokeWidth = (1.1f + beam % 2).dp.toPx(),
                    )
                }
            }

            MuseAtmosphereKind.RoseBloom -> {
                repeat(18) { index ->
                    val orbit = phase * 2f * PI + index * 0.72
                    val xSeed = ((index * 41 + 7) % 97) / 96f
                    val ySeed = ((index * 23 + 11) % 101) / 100f
                    val center = Offset(
                        size.width * (xSeed + cos(orbit).toFloat() * 0.014f),
                        size.height * (
                            (ySeed - phase * (0.04f + (index % 4) * 0.012f) + 1f) % 1f
                        ),
                    )
                    val petalW = (1.8f + index % 3).dp.toPx()
                    val petalH = petalW * 1.8f
                    rotate((index * 29f + phase * 90f) % 180f, center) {
                        drawOval(
                            color = chrome.primary.copy(
                                alpha = 0.14f * behavior.bloomStrength * strength,
                            ),
                            topLeft = Offset(center.x - petalW, center.y - petalH),
                            size = Size(petalW * 2f, petalH * 2f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun MuseLivingRainOverlay(
    level: MuseRainLevel,
    profile: MuseVisualProfile,
    behavior: MuseAtmosphereBehavior,
    intensity: MuseVisualIntensity,
    modifier: Modifier = Modifier,
) {
    if (level == MuseRainLevel.Off) return

    val chrome = profile.chromePalette()
    val transition = rememberInfiniteTransition(label = "MuseLivingRain")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1_650f / level.speedMultiplier)
                    .toInt()
                    .coerceAtLeast(600),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseLivingRainPhase",
    )
    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.70f
        MuseVisualIntensity.Balanced -> 1f
        MuseVisualIntensity.Vivid -> 1.26f
    }

    Canvas(modifier) {
        val layers = behavior.rainDepthLayers.coerceIn(3, 4)
        repeat(layers) { layer ->
            val depth = (layer + 1f) / layers
            val layerCount = (
                level.dropCount.toFloat() *
                    (0.18f + depth * 0.16f)
                ).toInt().coerceAtLeast(4)
            val speedScale = 0.52f + depth * 0.68f
            val lengthScale = 0.42f + depth * 0.82f
            val alphaScale = 0.34f + depth * 0.76f

            repeat(layerCount) { index ->
                val seed = index + layer * 97
                val xSeed = ((seed * 37 + 11) % 103) / 102f
                val ySeed = ((seed * 53 + 7) % 127) / 126f
                val speed = (0.52f + ((seed * 19) % 41) / 100f) * speedScale
                val normalizedY = (ySeed + phase * speed) % 1.14f - 0.07f
                val lateral = (
                    ((seed * 13) % 21) - 10
                    ) / 900f * behavior.dropletMobility
                val x = size.width * (xSeed + normalizedY * (0.012f + lateral))
                val y = size.height * normalizedY
                val longDrop = seed % 6 == 0
                val length = size.height *
                    (if (longDrop) 0.036f else 0.019f) *
                    lengthScale
                val alpha = (
                    if (longDrop) 0.18f else 0.105f
                    ) * alphaScale * intensityScale

                drawLine(
                    color = chrome.highlight.copy(
                        alpha = alpha.coerceAtMost(0.32f),
                    ),
                    start = Offset(x, y),
                    end = Offset(
                        x + size.width * (0.0025f + depth * 0.0035f),
                        y + length,
                    ),
                    strokeWidth = (
                        0.42f + depth * if (longDrop) 0.84f else 0.46f
                        ).dp.toPx(),
                    cap = StrokeCap.Round,
                )

                if (layer == layers - 1 && seed % 11 == 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.11f * intensityScale),
                        center = Offset(x, y + length),
                        radius = (0.7f + depth).dp.toPx(),
                    )
                }
            }
        }
    }
}

@Composable
internal fun MuseReactiveDropletOverlay(
    ripple: MuseTouchRipple?,
    profile: MuseVisualProfile,
    behavior: MuseAtmosphereBehavior,
    modifier: Modifier = Modifier,
) {
    if (ripple == null) return

    val chrome = profile.chromePalette()
    val progress = remember { Animatable(1f) }
    LaunchedEffect(ripple.id) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1_050,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Canvas(modifier) {
        val p = progress.value.coerceIn(0f, 1f)
        val fade = (1f - p).coerceIn(0f, 1f)
        val origin = Offset(
            size.width * ripple.xFraction,
            size.height * ripple.yFraction,
        )

        repeat(7) { index ->
            val angle = (index / 7f) * 2f * PI.toFloat()
            val spread = size.minDimension *
                (0.025f + p * (0.045f + behavior.dropletMobility * 0.055f))
            val wobble = sin((p * 5.5f + index) * PI).toFloat() *
                size.minDimension *
                0.004f *
                behavior.dropletMobility
            val center = Offset(
                x = origin.x + cos(angle.toDouble()).toFloat() * spread + wobble,
                y = origin.y + sin(angle.toDouble()).toFloat() * spread +
                    size.height * p * 0.028f * behavior.dropletMobility,
            )
            val radius = (1.6f + (index % 3) * 0.55f).dp.toPx()

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f * fade),
                        chrome.highlight.copy(alpha = 0.25f * fade),
                        chrome.glow.copy(alpha = 0.08f * fade),
                        Color.Transparent,
                    ),
                    center = Offset(
                        center.x - radius * 0.22f,
                        center.y - radius * 0.34f,
                    ),
                    radius = radius * 2.2f,
                ),
                topLeft = Offset(center.x - radius, center.y - radius * 1.35f),
                size = Size(radius * 2f, radius * 2.7f),
            )

            if (p > 0.34f) {
                drawLine(
                    color = chrome.highlight.copy(alpha = 0.12f * fade),
                    start = Offset(center.x, center.y - radius * 0.8f),
                    end = Offset(
                        center.x,
                        center.y - radius * (2.2f + p * 2.0f),
                    ),
                    strokeWidth = 0.55.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        val fall = (p - 0.18f).coerceIn(0f, 1f)
        val mainCenter = Offset(
            origin.x + size.width * 0.006f * sin(p * PI).toFloat(),
            origin.y + size.height * (0.014f + fall * 0.11f) * behavior.dropletMobility,
        )
        val mainW = (2.5f + fall * 1.3f).dp.toPx()
        val mainH = mainW * (1.25f + fall * 0.95f)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.76f * fade),
                    chrome.highlight.copy(alpha = 0.26f * fade),
                    Color.Transparent,
                ),
                center = Offset(
                    mainCenter.x - mainW * 0.24f,
                    mainCenter.y - mainH * 0.24f,
                ),
                radius = mainH * 1.3f,
            ),
            topLeft = Offset(
                mainCenter.x - mainW,
                mainCenter.y - mainH,
            ),
            size = Size(mainW * 2f, mainH * 2f),
        )
    }
}
