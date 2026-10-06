package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Atmosphere V3 is the visible "Living Worlds" layer.
 *
 * V2 established reliable native UI, rain depth and leaf-bound water. V3 makes
 * each profile recognisable by motion and material even when colour is ignored.
 */
@Composable
internal fun MuseLivingWorldsV3Overlay(
    profile: MuseVisualProfile,
    style: MuseLivingWorldStyle,
    spectrum: MuseAudioSpectrum,
    active: Boolean,
    intensity: MuseVisualIntensity,
    rainLevel: MuseRainLevel,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "MuseLivingWorldsV3")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (style.motionKind) {
                    MuseWorldMotionKind.RainWeightedSway -> if (active) 5_100 else 8_300
                    MuseWorldMotionKind.RibbonDrift -> if (active) 6_400 else 10_300
                    MuseWorldMotionKind.ThermalLift -> if (active) 4_400 else 7_400
                    MuseWorldMotionKind.LunarFloat -> if (active) 10_800 else 15_800
                    MuseWorldMotionKind.TidalPulse -> if (active) 5_000 else 8_200
                    MuseWorldMotionKind.PetalOrbit -> if (active) 7_500 else 11_800
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseLivingWorldsV3Phase",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.76f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (style.motionKind) {
                    MuseWorldMotionKind.ThermalLift -> 1_400
                    MuseWorldMotionKind.TidalPulse -> 1_850
                    MuseWorldMotionKind.RibbonDrift -> 2_300
                    MuseWorldMotionKind.RainWeightedSway -> 2_650
                    MuseWorldMotionKind.PetalOrbit -> 3_000
                    MuseWorldMotionKind.LunarFloat -> 4_400
                },
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseLivingWorldsV3Breathe",
    )

    val response = resolveWorldAudioResponse(profile, spectrum)
    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.72f
        MuseVisualIntensity.Balanced -> 1f
        MuseVisualIntensity.Vivid -> 1.25f
    }
    val activeScale = if (active) 1f else 0.72f

    Canvas(modifier) {
        val strength = intensityScale * activeScale
        when (style.motionKind) {
            MuseWorldMotionKind.RainWeightedSway -> drawVerdantWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                rainLevel = rainLevel,
                strength = strength,
            )
            MuseWorldMotionKind.RibbonDrift -> drawAuroraWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                strength = strength,
            )
            MuseWorldMotionKind.ThermalLift -> drawEmberWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                strength = strength,
            )
            MuseWorldMotionKind.LunarFloat -> drawMoonWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                strength = strength,
            )
            MuseWorldMotionKind.TidalPulse -> drawOceanWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                strength = strength,
            )
            MuseWorldMotionKind.PetalOrbit -> drawRoseWorld(
                phase = phase,
                breathe = breathe,
                response = response,
                style = style,
                strength = strength,
            )
        }
    }
}

private fun DrawScope.drawVerdantWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    rainLevel: MuseRainLevel,
    strength: Float,
) {
    val rainBoost = when (rainLevel) {
        MuseRainLevel.Off -> 0.30f
        MuseRainLevel.Mist -> 0.55f
        MuseRainLevel.Rain -> 0.82f
        MuseRainLevel.Downpour -> 1f
    }
    repeat(5) { index ->
        val drift = sin(phase * 2f * PI.toFloat() + index * 0.84f)
        val x = size.width * (0.10f + index * 0.205f) + drift * size.width * 0.025f
        val width = size.width * (0.055f + index % 2 * 0.020f)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    style.specularTint.copy(
                        alpha = (0.035f + response.dropletImpulse * 0.055f) *
                            rainBoost * strength,
                    ),
                    style.subsurfaceTint.copy(alpha = 0.018f * strength),
                    Color.Transparent,
                ),
                center = Offset(x, size.height * 0.42f),
                radius = width * 2.6f,
            ),
            topLeft = Offset(x - width, size.height * 0.12f),
            size = Size(width * 2f, size.height * 0.62f),
        )
    }

    repeat(9) { index ->
        val local = (phase + index * 0.117f) % 1f
        val center = Offset(
            size.width * (((index * 31 + 17) % 97) / 96f),
            size.height * (0.08f + local * 0.82f),
        )
        val pulse = (1f - local) * response.dropletImpulse * rainBoost
        if (pulse > 0.04f) {
            drawCircle(
                color = Color.White.copy(alpha = pulse * 0.13f * strength),
                center = center,
                radius = size.minDimension * (0.004f + pulse * 0.010f),
                style = Stroke(width = 0.8.dp.toPx()),
            )
        }
    }

    val ground = Offset(size.width * 0.50f, size.height * 0.90f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                style.subsurfaceTint.copy(alpha = 0.055f * breathe * strength),
                Color.Transparent,
            ),
            center = ground,
            radius = size.width * 0.48f,
        ),
        topLeft = Offset(ground.x - size.width * 0.46f, ground.y - size.height * 0.09f),
        size = Size(size.width * 0.92f, size.height * 0.18f),
    )
}

private fun DrawScope.drawAuroraWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    strength: Float,
) {
    repeat(5) { ribbon ->
        val depth = 0.55f + ribbon * 0.11f
        val yBase = size.height * (0.08f + ribbon * 0.16f)
        val wave = sin(phase * 2f * PI.toFloat() + ribbon * 1.13f)
        val path = Path().apply {
            moveTo(-size.width * 0.15f, yBase)
            cubicTo(
                size.width * 0.16f,
                yBase + size.height * (0.10f + wave * 0.025f),
                size.width * 0.38f,
                yBase - size.height * (0.12f - wave * 0.020f),
                size.width * 0.64f,
                yBase + size.height * 0.020f,
            )
            cubicTo(
                size.width * 0.82f,
                yBase + size.height * (0.11f + wave * 0.024f),
                size.width * 1.03f,
                yBase - size.height * 0.085f,
                size.width * 1.17f,
                yBase + size.height * 0.025f,
            )
        }
        val shimmer = (0.08f + response.shimmer * 0.18f) * depth * strength
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    style.leafSecondary.copy(alpha = shimmer * 0.55f),
                    Color.White.copy(alpha = shimmer),
                    style.specularTint.copy(alpha = shimmer * 0.72f),
                    Color.Transparent,
                )
            ),
            style = Stroke(
                width = (14f + ribbon * 7f + response.shimmer * 8f).dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )
    }

    repeat(18) { index ->
        val x = size.width * (((index * 43 + 9) % 101) / 100f)
        val ySeed = ((index * 29 + 5) % 103) / 102f
        val y = size.height * ((ySeed + phase * (0.025f + index % 3 * 0.008f)) % 1f)
        val radius = (0.7f + index % 3 * 0.45f + response.shimmer * 0.7f).dp.toPx()
        drawCircle(
            color = Color.White.copy(
                alpha = (0.05f + response.shimmer * 0.15f) * breathe * strength,
            ),
            center = Offset(x, y),
            radius = radius,
        )
    }
}

private fun DrawScope.drawEmberWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    strength: Float,
) {
    val floor = Offset(size.width * 0.52f, size.height * 0.94f)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                style.leafHighlight.copy(
                    alpha = (0.08f + response.particleImpulse * 0.10f) * breathe * strength,
                ),
                style.leafPrimary.copy(alpha = 0.035f * strength),
                Color.Transparent,
            ),
            center = floor,
            radius = size.width * 0.70f,
        ),
        topLeft = Offset(floor.x - size.width * 0.60f, floor.y - size.height * 0.12f),
        size = Size(size.width * 1.20f, size.height * 0.24f),
    )

    val count = (18 + style.sparkDensity * 20f).toInt()
    repeat(count) { index ->
        val seed = ((index * 47 + 13) % 103) / 102f
        val speed = 0.18f + (index % 5) * 0.035f + response.particleImpulse * 0.12f
        val local = (seed + phase * speed) % 1f
        val sway = sin(phase * 2f * PI.toFloat() + index * 0.71f) * size.width * 0.016f
        val center = Offset(
            size.width * (((index * 31 + 7) % 101) / 100f) + sway,
            size.height * (1.04f - local * 0.98f),
        )
        val radius = (0.7f + index % 4 * 0.35f + response.particleImpulse * 0.8f).dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.66f * strength),
                    style.leafHighlight.copy(
                        alpha = (0.36f + response.particleImpulse * 0.28f) * strength,
                    ),
                    style.leafPrimary.copy(alpha = 0.16f * strength),
                    Color.Transparent,
                ),
                center = center,
                radius = radius * 3.2f,
            ),
            center = center,
            radius = radius * 3.2f,
        )
    }

    repeat(4) { band ->
        val y = size.height * (0.55f + band * 0.09f)
        val offset = sin(phase * 2f * PI.toFloat() + band) * size.width * 0.025f
        drawLine(
            color = Color.White.copy(
                alpha = (0.015f + response.particleImpulse * 0.025f) * strength,
            ),
            start = Offset(size.width * 0.12f + offset, y),
            end = Offset(size.width * 0.88f + offset, y + size.height * 0.012f),
            strokeWidth = (4f + band * 2f).dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawMoonWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    strength: Float,
) {
    val moon = Offset(size.width * 0.82f, size.height * 0.11f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f * strength),
                style.specularTint.copy(alpha = 0.07f * strength),
                Color.Transparent,
            ),
            center = moon,
            radius = size.minDimension * 0.32f,
        ),
        center = moon,
        radius = size.minDimension * 0.32f,
    )

    repeat(7) { band ->
        val depth = 0.44f + band * 0.08f
        val drift = ((phase * (0.10f + band * 0.018f) + band * 0.13f) % 1f) - 0.5f
        val center = Offset(
            size.width * (0.50f + drift * 0.36f),
            size.height * (0.24f + band * 0.105f),
        )
        val calm = response.calmFactor
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(
                        alpha = (0.025f + style.volumetricMist * 0.040f) *
                            depth * calm * strength,
                    ),
                    style.leafPrimary.copy(alpha = 0.022f * breathe * strength),
                    Color.Transparent,
                ),
                center = center,
                radius = size.width * 0.60f,
            ),
            topLeft = Offset(
                center.x - size.width * 0.54f,
                center.y - size.height * (0.035f + depth * 0.012f),
            ),
            size = Size(
                size.width * 1.08f,
                size.height * (0.070f + depth * 0.024f),
            ),
        )
    }
}

private fun DrawScope.drawOceanWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    strength: Float,
) {
    repeat(9) { ring ->
        val local = (phase + ring * 0.11f) % 1f
        val warp = style.refractionWarp * (0.72f + response.refractionImpulse * 0.58f)
        val center = Offset(
            size.width * (
                0.50f + sin((phase * 2f + ring) * PI.toFloat()) * 0.08f * warp
            ),
            size.height * (0.18f + ring * 0.09f),
        )
        val radiusX = size.width * (0.045f + local * 0.39f * warp)
        val radiusY = size.height * (0.010f + local * 0.070f * warp)
        drawOval(
            color = Color.White.copy(
                alpha = (1f - local) *
                    (0.055f + response.refractionImpulse * 0.11f) *
                    strength,
            ),
            topLeft = Offset(center.x - radiusX, center.y - radiusY),
            size = Size(radiusX * 2f, radiusY * 2f),
            style = Stroke(width = (0.8f + ring * 0.11f).dp.toPx()),
        )
    }

    repeat(7) { beam ->
        val xSeed = 0.02f + beam * 0.17f
        val sway = sin(phase * 2f * PI.toFloat() + beam * 0.74f) *
            size.width * (0.02f + response.refractionImpulse * 0.025f)
        val path = Path().apply {
            moveTo(size.width * xSeed + sway, -size.height * 0.05f)
            cubicTo(
                size.width * (xSeed + 0.03f) + sway,
                size.height * 0.30f,
                size.width * (xSeed - 0.04f) - sway,
                size.height * 0.66f,
                size.width * (xSeed + 0.11f) + sway,
                size.height * 1.04f,
            )
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    style.specularTint.copy(
                        alpha = (0.035f + response.shimmer * 0.05f) * breathe * strength,
                    ),
                    Color.Transparent,
                )
            ),
            style = Stroke(
                width = (2.2f + beam % 2 * 1.6f).dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )
    }
}

private fun DrawScope.drawRoseWorld(
    phase: Float,
    breathe: Float,
    response: MuseWorldAudioResponse,
    style: MuseLivingWorldStyle,
    strength: Float,
) {
    val count = (18 + style.petalDepth * 12f).toInt()
    repeat(count) { index ->
        val depth = 0.35f + (index % 4) * 0.18f
        val orbit = phase * 2f * PI.toFloat() * (0.30f + depth * 0.28f) + index * 0.77f
        val xSeed = ((index * 37 + 11) % 101) / 100f
        val ySeed = ((index * 23 + 17) % 103) / 102f
        val center = Offset(
            x = size.width * (
                xSeed + cos(orbit) * (0.012f + depth * 0.020f)
            ),
            y = size.height * (
                (ySeed + phase * (0.018f + depth * 0.024f)) % 1f
            ),
        )
        val petalW = (1.7f + depth * 4.2f + response.particleImpulse * 1.2f).dp.toPx()
        val petalH = petalW * (1.35f + depth * 0.34f)
        rotate(
            degrees = (index * 29f + phase * 92f * depth) % 180f,
            pivot = center,
        ) {
            drawOval(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f * depth * strength),
                        style.leafHighlight.copy(
                            alpha = (0.08f + response.shimmer * 0.08f) * depth * strength,
                        ),
                        style.leafPrimary.copy(alpha = 0.08f * depth * strength),
                    )
                ),
                topLeft = Offset(center.x - petalW, center.y - petalH),
                size = Size(petalW * 2f, petalH * 2f),
            )
        }
    }

    val bloom = Offset(size.width * 0.74f, size.height * 0.30f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                style.specularTint.copy(
                    alpha = (0.035f + response.shimmer * 0.05f) * breathe * strength,
                ),
                style.leafPrimary.copy(alpha = 0.018f * strength),
                Color.Transparent,
            ),
            center = bloom,
            radius = size.minDimension * 0.28f,
        ),
        center = bloom,
        radius = size.minDimension * 0.28f,
    )
}
