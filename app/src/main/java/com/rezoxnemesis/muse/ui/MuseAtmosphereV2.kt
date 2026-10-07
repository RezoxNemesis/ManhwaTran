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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Atmosphere V2 keeps the UI native while giving the backdrop a physical model.
 * The deterministic helpers below are unit-testable and are also consumed by the
 * Canvas renderer, so the tests describe real motion rather than test-only math.
 */
internal data class MuseLeafWaterMotion(
    val surfaceProgress: Float,
    val surfaceTravel: Float,
    val lateral: Float,
    val edgeHang: Float,
    val edgeFall: Float,
    val impactPulse: Float,
)

internal data class MuseDropletCoalescence(
    val shouldMerge: Boolean,
    val progress: Float,
    val lateral: Float,
    val radiusScale: Float,
    val releaseBoost: Float,
)

internal fun resolveMuseDropletCoalescence(
    progressA: Float,
    lateralA: Float,
    radiusA: Float,
    progressB: Float,
    lateralB: Float,
    radiusB: Float,
    adhesion: Float,
): MuseDropletCoalescence {
    val aRadius = radiusA.coerceAtLeast(0.01f)
    val bRadius = radiusB.coerceAtLeast(0.01f)
    val wetAdhesion = adhesion.coerceIn(0f, 1f)
    val progressDistance = abs(progressA - progressB)
    val lateralDistance = abs(lateralA - lateralB)
    val progressThreshold = 0.052f + wetAdhesion * 0.052f
    val lateralThreshold = 0.070f + wetAdhesion * 0.090f
    val shouldMerge =
        progressDistance <= progressThreshold &&
            lateralDistance <= lateralThreshold

    if (!shouldMerge) {
        return MuseDropletCoalescence(
            shouldMerge = false,
            progress = progressA.coerceIn(0f, 1f),
            lateral = lateralA.coerceIn(-1f, 1f),
            radiusScale = aRadius,
            releaseBoost = 0f,
        )
    }

    // Use 2D projected area as the visual mass. The resulting radius grows
    // naturally instead of simply adding two radii together.
    val massA = aRadius * aRadius
    val massB = bRadius * bRadius
    val totalMass = (massA + massB).coerceAtLeast(0.001f)
    val mergedRadius = sqrt(totalMass)
    val progress = (
        progressA * massA +
            progressB * massB
        ) / totalMass
    val lateral = (
        lateralA * massA +
            lateralB * massB
        ) / totalMass
    val radiusGain = (
        mergedRadius / maxOf(aRadius, bRadius) - 1f
        ).coerceAtLeast(0f)

    return MuseDropletCoalescence(
        shouldMerge = true,
        progress = progress.coerceIn(0f, 1f),
        lateral = lateral.coerceIn(-1f, 1f),
        radiusScale = mergedRadius,
        releaseBoost = (
            radiusGain * (0.72f + wetAdhesion * 0.68f)
            ).coerceIn(0f, 0.75f),
    )
}

internal fun resolveMuseLeafWaterMotion(
    seed: Int,
    phase: Float,
    mobility: Float,
    rainLevel: MuseRainLevel,
): MuseLeafWaterMotion {
    val normalizedPhase = phase.coerceIn(0f, 1f)
    val normalizedMobility = mobility.coerceIn(0f, 1f)
    val rainDrive = when (rainLevel) {
        MuseRainLevel.Off -> 0.18f
        MuseRainLevel.Mist -> 0.42f
        MuseRainLevel.Rain -> 0.72f
        MuseRainLevel.Downpour -> 1.00f
    }

    val surfaceTravel = normalizedPhase *
        (0.34f + normalizedMobility * 0.74f) *
        (0.56f + rainDrive * 0.54f)
    val seededTravel = (surfaceTravel + (seed * 0.173f % 1f)) % 1f
    val surfaceProgress = (0.10f + seededTravel * 0.80f).coerceIn(0f, 1f)
    val lateral = sin(seed * 1.93 + normalizedPhase * PI * 2.0)
        .toFloat()
        .coerceIn(-1f, 1f)

    val edgeCycle = (
        normalizedPhase * (0.42f + normalizedMobility * 0.76f) * rainDrive +
            seed * 0.211f
        ) % 1f
    val edgeHang = ((edgeCycle - 0.54f) / 0.24f).coerceIn(0f, 1f)
    val edgeFall = ((edgeCycle - 0.76f) / 0.24f).coerceIn(0f, 1f)

    val impactCycle = (
        normalizedPhase * (1.45f + rainDrive * 1.15f) +
            seed * 0.287f
        ) % 1f
    val impactPulse = (1f - abs(impactCycle - 0.10f) / 0.10f)
        .coerceIn(0f, 1f) * rainDrive

    return MuseLeafWaterMotion(
        surfaceProgress = surfaceProgress,
        surfaceTravel = surfaceTravel,
        lateral = lateral,
        edgeHang = edgeHang,
        edgeFall = edgeFall,
        impactPulse = impactPulse,
    )
}

internal fun museAtmosphereDepthSpeeds(depthParallax: Float): List<Float> {
    val depth = depthParallax.coerceIn(0f, 1f)
    return listOf(
        0.18f + depth * 0.18f,
        0.44f + depth * 0.30f,
        0.78f + depth * 0.38f,
    )
}

private data class MuseWetLeafAnchor(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val angle: Float,
    val mirror: Boolean,
    val depth: Float,
)

@Composable
internal fun MuseAtmosphereV2Overlay(
    route: String?,
    profile: MuseVisualProfile,
    behavior: MuseAtmosphereBehavior,
    active: Boolean,
    audioEnergy: Float,
    rainLevel: MuseRainLevel,
    intensity: MuseVisualIntensity,
    touchRipple: MuseTouchRipple?,
    modifier: Modifier = Modifier,
) {
    val chrome = profile.chromePalette()
    val transition = rememberInfiniteTransition(label = "MuseAtmosphereV2")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (behavior.kind) {
                    MuseAtmosphereKind.BotanicalRain -> if (active) 5_900 else 9_400
                    MuseAtmosphereKind.AuroraRibbons -> if (active) 6_600 else 10_800
                    MuseAtmosphereKind.EmberDrift -> if (active) 5_100 else 8_700
                    MuseAtmosphereKind.MoonMist -> if (active) 9_500 else 14_200
                    MuseAtmosphereKind.OceanRefraction -> if (active) 5_000 else 8_400
                    MuseAtmosphereKind.RoseBloom -> if (active) 7_300 else 11_700
                },
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseAtmosphereV2Phase",
    )

    val microPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 2_400 else 4_200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseAtmosphereV2MicroPhase",
    )

    val touchProgress = remember { Animatable(1f) }
    LaunchedEffect(touchRipple?.id) {
        if (touchRipple != null) {
            touchProgress.snapTo(0f)
            touchProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1_180,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.72f
        MuseVisualIntensity.Balanced -> 1f
        MuseVisualIntensity.Vivid -> 1.24f
    }

    Canvas(modifier) {
        val energy = audioEnergy.coerceIn(0f, 1f)
        val signatureStrength = behavior.profileSignature *
            intensityScale *
            (0.88f + energy * behavior.musicTransientResponse * 0.34f)

        drawAtmosphereDepthBokeh(
            chrome = chrome,
            behavior = behavior,
            phase = phase,
            energy = energy,
            strength = intensityScale,
        )
        drawLeafBoundWater(
            route = route,
            chrome = chrome,
            behavior = behavior,
            phase = phase,
            rainLevel = rainLevel,
            strength = intensityScale,
        )

        if (touchRipple != null) {
            drawTouchWaterImpactV2(
                ripple = touchRipple,
                progress = touchProgress.value,
                chrome = chrome,
                behavior = behavior,
                strength = intensityScale,
            )
        }
    }
}

private fun DrawScope.drawAtmosphereDepthBokeh(
    chrome: MuseChromePalette,
    behavior: MuseAtmosphereBehavior,
    phase: Float,
    energy: Float,
    strength: Float,
) {
    val layerSpeeds = museAtmosphereDepthSpeeds(behavior.depthParallax)

    layerSpeeds.forEachIndexed { layer, speed ->
        val depth = (layer + 1f) / layerSpeeds.size
        repeat(7) { index ->
            val seed = index + layer * 17
            val xSeed = ((seed * 43 + 19) % 101) / 100f
            val ySeed = ((seed * 31 + 7) % 109) / 108f
            val travel = (phase * speed + ySeed) % 1f
            val horizontalDrift = sin(
                phase.toDouble() * 2.0 * PI * speed + seed * 0.71
            ).toFloat() *
                (0.008f + depth * 0.014f) *
                behavior.bokehDrift

            val center = Offset(
                x = size.width * (xSeed + horizontalDrift),
                y = size.height * travel,
            )
            val radius = size.minDimension *
                (0.006f + depth * 0.012f + (seed % 3) * 0.002f)
            val alpha = (
                0.024f +
                    depth * 0.038f +
                    energy * behavior.musicResponse * 0.018f
                ) * strength

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.highlight.copy(alpha = alpha),
                        chrome.glow.copy(alpha = alpha * 0.46f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 2.4f,
                ),
                center = center,
                radius = radius * 2.4f,
            )
        }
    }
}

private fun DrawScope.drawProfileSignatureV2(
    chrome: MuseChromePalette,
    behavior: MuseAtmosphereBehavior,
    phase: Float,
    microPhase: Float,
    strength: Float,
) {
    when (behavior.kind) {
        MuseAtmosphereKind.BotanicalRain -> {
            repeat(4) { shaft ->
                val sway = sin(phase.toDouble() * 2.0 * PI + shaft * 0.9)
                    .toFloat() * size.width * 0.018f
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            chrome.highlight.copy(alpha = 0.035f * strength),
                            chrome.primary.copy(alpha = 0.060f * strength),
                            Color.Transparent,
                        ),
                        start = Offset(size.width * 0.72f, 0f),
                        end = Offset(size.width * 0.18f, size.height),
                    ),
                    start = Offset(
                        x = size.width * (0.58f + shaft * 0.11f) + sway,
                        y = -size.height * 0.08f,
                    ),
                    end = Offset(
                        x = size.width * (0.18f + shaft * 0.12f) + sway,
                        y = size.height * 0.74f,
                    ),
                    strokeWidth = (10f + shaft * 4f).dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        MuseAtmosphereKind.AuroraRibbons -> {
            repeat(4) { ribbon ->
                val yBase = size.height * (0.10f + ribbon * 0.17f)
                val wave = sin(
                    phase.toDouble() * 2.0 * PI + ribbon * 1.27
                ).toFloat()
                val path = Path().apply {
                    moveTo(-size.width * 0.12f, yBase)
                    cubicTo(
                        size.width * 0.20f,
                        yBase + size.height * (0.10f + wave * 0.025f),
                        size.width * 0.42f,
                        yBase - size.height * (0.12f - wave * 0.018f),
                        size.width * 0.67f,
                        yBase + size.height * 0.035f,
                    )
                    cubicTo(
                        size.width * 0.84f,
                        yBase + size.height * (0.12f + wave * 0.020f),
                        size.width * 1.02f,
                        yBase - size.height * 0.08f,
                        size.width * 1.16f,
                        yBase + size.height * 0.025f,
                    )
                }
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            chrome.primary.copy(alpha = 0.10f * strength),
                            chrome.highlight.copy(alpha = 0.19f * strength),
                            chrome.glow.copy(alpha = 0.11f * strength),
                            Color.Transparent,
                        )
                    ),
                    style = Stroke(
                        width = (18f + ribbon * 9f).dp.toPx(),
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }

        MuseAtmosphereKind.EmberDrift -> {
            val floorCenter = Offset(size.width * 0.52f, size.height * 0.92f)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.primaryStrong.copy(alpha = 0.09f * strength),
                        chrome.glow.copy(alpha = 0.035f * strength),
                        Color.Transparent,
                    ),
                    center = floorCenter,
                    radius = size.width * 0.62f,
                ),
                topLeft = Offset(
                    floorCenter.x - size.width * 0.55f,
                    floorCenter.y - size.height * 0.12f,
                ),
                size = Size(size.width * 1.10f, size.height * 0.24f),
            )
            repeat(18) { index ->
                val xSeed = ((index * 47 + 13) % 101) / 100f
                val local = (phase * (0.22f + index % 4 * 0.035f) +
                    ((index * 29) % 97) / 96f) % 1f
                val sway = sin(phase.toDouble() * 2.0 * PI + index)
                    .toFloat() * size.width * 0.012f
                val center = Offset(
                    size.width * xSeed + sway,
                    size.height * (1.04f - local * 0.92f),
                )
                val radius = (0.8f + index % 4 * 0.45f).dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            chrome.highlight.copy(alpha = 0.68f * strength),
                            chrome.primaryStrong.copy(alpha = 0.30f * strength),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius * 3.3f,
                    ),
                    center = center,
                    radius = radius * 3.3f,
                )
            }
        }

        MuseAtmosphereKind.MoonMist -> {
            val moon = Offset(size.width * 0.82f, size.height * 0.105f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.085f * strength),
                        chrome.highlight.copy(alpha = 0.075f * strength),
                        chrome.glow.copy(alpha = 0.030f * strength),
                        Color.Transparent,
                    ),
                    center = moon,
                    radius = size.minDimension * 0.30f,
                ),
                center = moon,
                radius = size.minDimension * 0.30f,
            )
            repeat(5) { band ->
                val xDrift = (
                    ((phase * (0.24f + band * 0.035f) + band * 0.19f) % 1f) -
                        0.5f
                    ) * size.width * 0.24f
                val center = Offset(
                    size.width * 0.50f + xDrift,
                    size.height * (0.27f + band * 0.14f),
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            chrome.primary.copy(alpha = 0.054f * strength),
                            chrome.glassElevated.copy(alpha = 0.028f * strength),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = size.width * 0.55f,
                    ),
                    topLeft = Offset(
                        center.x - size.width * 0.48f,
                        center.y - size.height * 0.045f,
                    ),
                    size = Size(size.width * 0.96f, size.height * 0.09f),
                )
            }
        }

        MuseAtmosphereKind.OceanRefraction -> {
            repeat(7) { ring ->
                val local = (phase + ring * 0.13f) % 1f
                val center = Offset(
                    x = size.width * (
                        0.48f +
                            sin((phase * 2f + ring) * PI).toFloat() * 0.07f
                        ),
                    y = size.height * (0.24f + ring * 0.105f),
                )
                val radiusX = size.width * (0.06f + local * 0.36f)
                val radiusY = size.height * (0.012f + local * 0.065f)
                drawOval(
                    color = chrome.highlight.copy(
                        alpha = (1f - local) * 0.15f * strength,
                    ),
                    topLeft = Offset(center.x - radiusX, center.y - radiusY),
                    size = Size(radiusX * 2f, radiusY * 2f),
                    style = Stroke(width = (0.8f + ring * 0.10f).dp.toPx()),
                )
            }
            repeat(5) { beam ->
                val shift = sin(
                    phase.toDouble() * 2.0 * PI + beam * 0.8
                ).toFloat() * size.width * 0.03f
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            chrome.highlight.copy(alpha = 0.075f * strength),
                            chrome.primary.copy(alpha = 0.025f * strength),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * (0.08f + beam * 0.20f) + shift, 0f),
                    end = Offset(
                        size.width * (0.22f + beam * 0.17f) + shift,
                        size.height,
                    ),
                    strokeWidth = (1.2f + beam % 2 * 0.7f).dp.toPx(),
                )
            }
        }

        MuseAtmosphereKind.RoseBloom -> {
            repeat(16) { index ->
                val depth = 0.45f + (index % 3) * 0.22f
                val orbit = phase * 2f * PI + index * 0.73
                val xSeed = ((index * 37 + 11) % 101) / 100f
                val ySeed = ((index * 23 + 17) % 103) / 102f
                val center = Offset(
                    x = size.width * (
                        xSeed + cos(orbit).toFloat() * 0.018f * depth
                        ),
                    y = size.height * (
                        (ySeed + phase * (0.025f + depth * 0.025f)) % 1f
                        ),
                )
                val petalW = (1.8f + depth * 3.0f).dp.toPx()
                val petalH = petalW * (1.45f + microPhase * 0.18f)
                rotate(
                    degrees = (index * 31f + phase * 110f) % 180f,
                    pivot = center,
                ) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                chrome.highlight.copy(alpha = 0.16f * depth * strength),
                                chrome.primary.copy(alpha = 0.10f * depth * strength),
                                Color.Transparent,
                            ),
                            start = Offset(center.x, center.y - petalH),
                            end = Offset(center.x, center.y + petalH),
                        ),
                        topLeft = Offset(center.x - petalW, center.y - petalH),
                        size = Size(petalW * 2f, petalH * 2f),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawLeafBoundWater(
    route: String?,
    chrome: MuseChromePalette,
    behavior: MuseAtmosphereBehavior,
    phase: Float,
    rainLevel: MuseRainLevel,
    strength: Float,
) {
    val anchors = museWetLeafAnchors(route)
    val rainDrive = when (rainLevel) {
        MuseRainLevel.Off -> 0.18f
        MuseRainLevel.Mist -> 0.42f
        MuseRainLevel.Rain -> 0.72f
        MuseRainLevel.Downpour -> 1.00f
    }
    val wetness = behavior.leafWetness * (0.66f + rainDrive * 0.34f)

    anchors.forEachIndexed { anchorIndex, anchor ->
        // Surface sheen follows the leaf axis, making the water feel attached to
        // the foliage rather than floating in front of the whole screen.
        val sheenStart = leafPoint(anchor, 0.16f, -0.08f)
        val sheenEnd = leafPoint(anchor, 0.78f, 0.05f)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    chrome.highlight.copy(alpha = 0.075f * wetness * strength),
                    Color.White.copy(alpha = 0.045f * wetness * strength),
                    Color.Transparent,
                ),
                start = sheenStart,
                end = sheenEnd,
            ),
            start = sheenStart,
            end = sheenEnd,
            strokeWidth = (0.9f + anchor.depth * 0.9f).dp.toPx(),
            cap = StrokeCap.Round,
        )

        data class SurfaceBead(
            val motion: MuseLeafWaterMotion,
            val lateral: Float,
            val radiusDp: Float,
        )

        val beads = (0 until 4).map { dropletIndex ->
            val seed = anchorIndex * 11 + dropletIndex * 3 + 1
            val motion = resolveMuseLeafWaterMotion(
                seed = seed,
                phase = phase,
                mobility = behavior.dropletMobility,
                rainLevel = rainLevel,
            )
            SurfaceBead(
                motion = motion,
                lateral = (
                    motion.lateral * (0.18f + dropletIndex * 0.07f)
                    ).coerceIn(-0.62f, 0.62f),
                radiusDp = 1.15f +
                    dropletIndex * 0.24f +
                    anchor.depth * 0.75f,
            )
        }

        fun drawSurfaceBead(
            progress: Float,
            lateral: Float,
            radiusDp: Float,
            impactPulse: Float,
            releaseBoost: Float,
        ) {
            val releaseProgress = (
                progress + releaseBoost * 0.085f
                ).coerceIn(0.08f, 0.96f)
            val center = leafPoint(
                anchor = anchor,
                progress = releaseProgress,
                lateral = lateral,
            )
            val trailStart = leafPoint(
                anchor = anchor,
                progress = (
                    releaseProgress -
                        0.07f -
                        releaseBoost * 0.045f
                    ).coerceAtLeast(0.08f),
                lateral = lateral * 0.92f,
            )
            val radius = radiusDp.dp.toPx()
            val stretch = 1f + releaseBoost * 0.70f

            drawLine(
                color = chrome.highlight.copy(
                    alpha = (
                        0.055f +
                            releaseBoost * 0.045f
                        ) * wetness * strength,
                ),
                start = trailStart,
                end = center,
                strokeWidth = (
                    0.65f +
                        anchor.depth * 0.35f +
                        releaseBoost * 0.28f
                    ).dp.toPx(),
                cap = StrokeCap.Round,
            )

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = (
                                0.46f +
                                    releaseBoost * 0.10f
                                ).coerceAtMost(0.62f) *
                                wetness *
                                strength,
                        ),
                        chrome.highlight.copy(alpha = 0.20f * wetness * strength),
                        chrome.glow.copy(alpha = 0.065f * wetness * strength),
                        Color.Transparent,
                    ),
                    center = Offset(
                        center.x - radius * 0.28f,
                        center.y - radius * 0.35f,
                    ),
                    radius = radius * 2.2f,
                ),
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius * 1.25f * stretch,
                ),
                size = Size(
                    radius * 2f,
                    radius * 2.5f * stretch,
                ),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.42f * wetness * strength),
                center = Offset(
                    center.x - radius * 0.24f,
                    center.y - radius * 0.38f * stretch,
                ),
                radius = radius * 0.20f,
            )

            if (
                rainLevel == MuseRainLevel.Rain ||
                rainLevel == MuseRainLevel.Downpour
            ) {
                val impact = (
                    impactPulse * behavior.rainImpactResponse +
                        releaseBoost * 0.22f
                    ).coerceIn(0f, 1f)
                if (impact > 0.06f) {
                    drawCircle(
                        color = chrome.highlight.copy(
                            alpha = impact * 0.22f * strength,
                        ),
                        center = center,
                        radius = radius * (1.8f + impact * 3.2f),
                        style = Stroke(
                            width = (0.55f + impact * 0.45f).dp.toPx(),
                        ),
                    )
                }
            }
        }

        listOf(0 to 1, 2 to 3).forEach { (firstIndex, secondIndex) ->
            val first = beads[firstIndex]
            val second = beads[secondIndex]
            val merge = resolveMuseDropletCoalescence(
                progressA = first.motion.surfaceProgress,
                lateralA = first.lateral,
                radiusA = first.radiusDp,
                progressB = second.motion.surfaceProgress,
                lateralB = second.lateral,
                radiusB = second.radiusDp,
                adhesion = behavior.leafWetness,
            )

            if (merge.shouldMerge) {
                drawSurfaceBead(
                    progress = merge.progress,
                    lateral = merge.lateral,
                    radiusDp = merge.radiusScale,
                    impactPulse = maxOf(
                        first.motion.impactPulse,
                        second.motion.impactPulse,
                    ),
                    releaseBoost = merge.releaseBoost,
                )
            } else {
                drawSurfaceBead(
                    progress = first.motion.surfaceProgress,
                    lateral = first.lateral,
                    radiusDp = first.radiusDp,
                    impactPulse = first.motion.impactPulse,
                    releaseBoost = 0f,
                )
                drawSurfaceBead(
                    progress = second.motion.surfaceProgress,
                    lateral = second.lateral,
                    radiusDp = second.radiusDp,
                    impactPulse = second.motion.impactPulse,
                    releaseBoost = 0f,
                )
            }
        }

        val edgeMotion = resolveMuseLeafWaterMotion(
            seed = anchorIndex * 13 + 9,
            phase = phase,
            mobility = behavior.dropletMobility,
            rainLevel = rainLevel,
        )
        if (edgeMotion.edgeHang > 0f) {
            val edge = leafPoint(
                anchor = anchor,
                progress = 0.92f,
                lateral = if (anchorIndex % 2 == 0) 0.14f else -0.14f,
            )
            val fallDistance = size.height *
                (0.008f + 0.055f * edgeMotion.edgeFall) *
                behavior.dropletMobility
            val center = Offset(edge.x, edge.y + fallDistance)
            val radius = (
                1.5f +
                    edgeMotion.edgeHang * 1.15f +
                    anchor.depth * 0.7f
                ).dp.toPx()
            val edgeAlpha = (
                edgeMotion.edgeHang * (1f - edgeMotion.edgeFall * 0.72f)
                ).coerceIn(0f, 1f)

            if (edgeMotion.edgeFall > 0.08f) {
                drawLine(
                    color = chrome.highlight.copy(
                        alpha = 0.11f * edgeAlpha * wetness * strength,
                    ),
                    start = edge,
                    end = center,
                    strokeWidth = 0.7.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.48f * edgeAlpha * wetness * strength),
                        chrome.highlight.copy(
                            alpha = 0.20f * edgeAlpha * wetness * strength,
                        ),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = radius * 2.1f,
                ),
                topLeft = Offset(center.x - radius, center.y - radius * 1.35f),
                size = Size(radius * 2f, radius * 2.7f),
            )
        }
    }
}

private fun DrawScope.drawTouchWaterImpactV2(
    ripple: MuseTouchRipple,
    progress: Float,
    chrome: MuseChromePalette,
    behavior: MuseAtmosphereBehavior,
    strength: Float,
) {
    val p = progress.coerceIn(0f, 1f)
    val fade = (1f - p).coerceIn(0f, 1f)
    if (fade <= 0f) return

    val origin = Offset(
        size.width * ripple.xFraction,
        size.height * ripple.yFraction,
    )

    repeat(3) { ring ->
        val delayed = (p - ring * 0.07f).coerceIn(0f, 1f)
        if (delayed > 0f) {
            drawCircle(
                color = chrome.highlight.copy(
                    alpha = 0.22f * (1f - delayed) * strength,
                ),
                center = origin,
                radius = size.minDimension *
                    (0.018f + delayed * (0.085f + ring * 0.028f)),
                style = Stroke(
                    width = (1.0f - ring * 0.12f).coerceAtLeast(0.6f).dp.toPx(),
                ),
            )
        }
    }

    repeat(9) { index ->
        val angle = index / 9f * 2f * PI.toFloat()
        val launch = size.minDimension *
            p *
            (0.045f + behavior.rainImpactResponse * 0.045f)
        val gravity = size.height * p * p * 0.045f
        val center = Offset(
            x = origin.x + cos(angle.toDouble()).toFloat() * launch,
            y = origin.y + sin(angle.toDouble()).toFloat() * launch + gravity,
        )
        val radius = (1.0f + index % 3 * 0.45f).dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.38f * fade * strength),
                    chrome.highlight.copy(alpha = 0.15f * fade * strength),
                    Color.Transparent,
                ),
                center = center,
                radius = radius * 2.2f,
            ),
            center = center,
            radius = radius * 2.2f,
        )
    }
}

private fun DrawScope.leafPoint(
    anchor: MuseWetLeafAnchor,
    progress: Float,
    lateral: Float,
): Offset {
    val t = progress.coerceIn(0f, 1f)
    val centerX = size.width * anchor.x
    val centerY = size.height * anchor.y
    val widthPx = size.width * anchor.width
    val heightPx = size.height * anchor.height
    val leafWidth = sin(PI * t).toFloat().coerceAtLeast(0f) * widthPx * 0.48f
    val mirror = if (anchor.mirror) -1f else 1f
    val localX = leafWidth * lateral * mirror
    val localY = (t - 0.5f) * heightPx

    val angle = anchor.angle / 180f * PI
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()

    return Offset(
        x = centerX + localX * cosA - localY * sinA,
        y = centerY + localX * sinA + localY * cosA,
    )
}

private fun museWetLeafAnchors(route: String?): List<MuseWetLeafAnchor> =
    when (route) {
        "sleep" -> listOf(
            MuseWetLeafAnchor(0.02f, 0.19f, 0.39f, 0.21f, -38f, false, 1.00f),
            MuseWetLeafAnchor(0.96f, 0.16f, 0.34f, 0.19f, 40f, true, 0.92f),
            MuseWetLeafAnchor(0.09f, 0.55f, 0.27f, 0.16f, -52f, false, 0.78f),
            MuseWetLeafAnchor(0.92f, 0.58f, 0.28f, 0.15f, 45f, true, 0.76f),
        )

        "album", "artist" -> listOf(
            MuseWetLeafAnchor(0.04f, 0.12f, 0.33f, 0.18f, -48f, false, 0.96f),
            MuseWetLeafAnchor(0.94f, 0.10f, 0.32f, 0.18f, 44f, true, 1.00f),
            MuseWetLeafAnchor(0.97f, 0.46f, 0.25f, 0.14f, 52f, true, 0.82f),
        )

        "downloads", "more" -> listOf(
            MuseWetLeafAnchor(0.03f, 0.16f, 0.34f, 0.19f, -43f, false, 0.94f),
            MuseWetLeafAnchor(0.95f, 0.13f, 0.31f, 0.18f, 39f, true, 0.90f),
            MuseWetLeafAnchor(0.94f, 0.64f, 0.30f, 0.17f, 48f, true, 0.86f),
            MuseWetLeafAnchor(0.82f, 0.93f, 0.35f, 0.19f, -28f, true, 0.78f),
        )

        else -> listOf(
            MuseWetLeafAnchor(0.02f, 0.14f, 0.36f, 0.20f, -44f, false, 1.00f),
            MuseWetLeafAnchor(0.95f, 0.12f, 0.33f, 0.18f, 38f, true, 0.94f),
            MuseWetLeafAnchor(0.96f, 0.55f, 0.28f, 0.16f, 48f, true, 0.82f),
            MuseWetLeafAnchor(0.16f, 0.91f, 0.38f, 0.21f, 31f, false, 0.86f),
        )
    }
