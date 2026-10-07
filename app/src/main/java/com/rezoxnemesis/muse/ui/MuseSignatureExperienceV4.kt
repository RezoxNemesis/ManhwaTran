package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class MuseV4RenderPolicy(
    val waterLanguage: MuseWaterLanguage,
    val physicalDropletScale: Float,
    val waterRefractionScale: Float,
    val condensationHazeScale: Float,
    val secondaryParticleScale: Float,
    val bokehScale: Float,
    val microDetailScale: Float,
    val secondaryShimmerScale: Float,
    val heroLightingEnabled: Boolean,
    val heroGeometryEnabled: Boolean,
    val interactionEnabled: Boolean,
    val morphEnabled: Boolean,
    val echoKind: MuseWorldEchoKind,
    val memoryKind: MuseMusicMemoryKind,
)

internal fun resolveMuseV4RenderPolicy(
    experience: MuseWorldExperience,
    budget: MuseVisualQualityBudget,
    isForeground: Boolean,
): MuseV4RenderPolicy {
    val visibilityScale = if (isForeground) 1f else 0.72f
    val language = experience.interactionLanguage()

    val water = when (experience.waterLanguage) {
        MuseWaterLanguage.PhysicalDroplets -> Triple(1.00f, 0.34f, 0.12f)
        MuseWaterLanguage.CondensationRefraction -> Triple(0f, 0.60f, 0.28f)
        MuseWaterLanguage.DryHeat -> Triple(0f, 0f, 0f)
        MuseWaterLanguage.LunarCondensation -> Triple(0f, 0.10f, 0.82f)
        MuseWaterLanguage.TidalRefraction -> Triple(0f, 1.00f, 0.18f)
        MuseWaterLanguage.RestrainedDew -> Triple(0.16f, 0.12f, 0.10f)
    }

    return MuseV4RenderPolicy(
        waterLanguage = experience.waterLanguage,
        physicalDropletScale = water.first,
        waterRefractionScale = water.second,
        condensationHazeScale = water.third,
        secondaryParticleScale = (
            budget.secondaryParticleScale * visibilityScale
            ).coerceIn(0f, 1f),
        bokehScale = (budget.bokehScale * visibilityScale).coerceIn(0f, 1f),
        microDetailScale = (budget.microDetailScale * visibilityScale).coerceIn(0f, 1f),
        secondaryShimmerScale = (
            budget.secondaryShimmerScale * visibilityScale
            ).coerceIn(0f, 1f),
        heroLightingEnabled = budget.heroLightingEnabled,
        heroGeometryEnabled = budget.heroGeometryEnabled,
        interactionEnabled = budget.interactionEnabled,
        morphEnabled = budget.morphEnabled,
        echoKind = language.echoKind,
        memoryKind = language.memoryKind,
    )
}

@Composable
internal fun rememberMuseVisualQualityBudget(
    isForeground: Boolean,
    expensiveLayersVisible: Boolean = true,
): MuseVisualQualityBudget {
    var governor by remember {
        mutableStateOf(initialMuseVisualPerformanceGovernor())
    }

    LaunchedEffect(isForeground, expensiveLayersVisible) {
        if (!isForeground || !expensiveLayersVisible) {
            governor = updateMuseVisualPerformanceGovernor(
                state = governor,
                frameDurationMillis = 16f,
                isForeground = isForeground,
                expensiveLayersVisible = expensiveLayersVisible,
            )
            return@LaunchedEffect
        }

        var previousFrameNanos = 0L
        var accumulatedMillis = 0f
        var sampleCount = 0

        while (true) {
            withFrameNanos { frameNanos ->
                if (previousFrameNanos != 0L) {
                    accumulatedMillis +=
                        (frameNanos - previousFrameNanos) / 1_000_000f
                    sampleCount += 1
                }
                previousFrameNanos = frameNanos
            }

            if (sampleCount >= 6) {
                governor = updateMuseVisualPerformanceGovernor(
                    state = governor,
                    frameDurationMillis = accumulatedMillis / sampleCount,
                    isForeground = true,
                    expensiveLayersVisible = true,
                )
                accumulatedMillis = 0f
                sampleCount = 0
            }
        }
    }

    return governor.qualityBudget()
}

@Composable
internal fun rememberMuseWorldBlend(
    targetProfile: MuseVisualProfile,
): MuseWorldBlend {
    var morph by remember {
        mutableStateOf(settledMuseWorldMorph(targetProfile))
    }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(targetProfile) {
        if (targetProfile != morph.target) {
            val retargeted = retargetMuseWorldMorph(
                state = morph,
                target = targetProfile,
                currentProgress = progress.value,
            )
            morph = retargeted
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = retargeted.durationMillis,
                ),
            )
        }
    }

    return sampleMuseWorldMorph(morph, progress.value)
}

@Composable
internal fun MuseSignatureExperienceV4Overlay(
    profile: MuseVisualProfile,
    blend: MuseWorldBlend,
    spectrum: MuseAudioSpectrum,
    active: Boolean,
    touchRipple: MuseTouchRipple?,
    interactionFrame: MuseV4InteractionFrame,
    qualityBudget: MuseVisualQualityBudget,
    modifier: Modifier = Modifier,
) {
    val experience = profile.worldExperience()
    val policy = resolveMuseV4RenderPolicy(
        experience = experience,
        budget = qualityBudget,
        isForeground = true,
    )
    val interaction = experience.interactionLanguage()
    val breathing = resolveMuseWorldBreathing(
        experience = experience,
        isPlaying = active,
        audioEnergy = spectrum.energy,
    )

    val transition = rememberInfiniteTransition(label = "MuseV4SharedClock")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (
                    7_200f / breathing.tempoScale.coerceAtLeast(0.20f)
                    ).toInt().coerceIn(3_400, 16_000),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseV4SharedPhase",
    )

    val strongTransient = active && spectrum.transient > 0.46f
    val memoryTarget = if (strongTransient) {
        (0.48f + spectrum.transient * 0.34f + spectrum.energy * 0.18f)
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val memoryStrength by animateFloatAsState(
        targetValue = memoryTarget,
        animationSpec = if (strongTransient) {
            tween(durationMillis = 140)
        } else {
            tween(
                durationMillis = (
                    1_000f / interaction.memoryDecayPerSecond.coerceAtLeast(0.08f)
                    ).toInt().coerceIn(4_000, 10_000),
                easing = LinearEasing,
            )
        },
        label = "MuseV4MusicMemory",
    )

    val echoProgress = remember { Animatable(1f) }
    LaunchedEffect(touchRipple?.id) {
        if (touchRipple != null && policy.interactionEnabled) {
            echoProgress.snapTo(0f)
            echoProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = interaction.echoLifetimeMillis,
                ),
            )
        }
    }

    val focusStrength = remember { Animatable(0f) }
    LaunchedEffect(
        touchRipple?.id,
        interactionFrame.focusRegion,
        interactionFrame.focusStrength,
    ) {
        if (policy.interactionEnabled && interactionFrame.focusRegion != null) {
            focusStrength.snapTo(interactionFrame.focusStrength)
            focusStrength.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1_450),
            )
        }
    }

    val momentumStrength = remember { Animatable(0f) }
    LaunchedEffect(
        touchRipple?.id,
        interactionFrame.momentumX,
        interactionFrame.momentumY,
        interactionFrame.momentumStrength,
    ) {
        if (policy.interactionEnabled && interactionFrame.momentumStrength > 0.015f) {
            momentumStrength.snapTo(interactionFrame.momentumStrength)
            momentumStrength.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 920),
            )
        }
    }

    Canvas(modifier = modifier) {
        if (policy.morphEnabled) {
            drawMuseV4MorphField(
                blend = blend,
                phase = phase,
                secondaryScale = policy.secondaryShimmerScale,
            )
        }

        if (memoryStrength > 0.01f) {
            drawMuseV4MusicMemory(
                kind = policy.memoryKind,
                strength = memoryStrength,
                phase = phase,
                profile = profile,
                breathingAmplitude = breathing.amplitude,
                densityScale = policy.secondaryParticleScale,
            )
        }

        if (touchRipple != null && echoProgress.value < 1f) {
            drawMuseV4WorldEcho(
                kind = policy.echoKind,
                center = Offset(
                    x = size.width * touchRipple.xFraction,
                    y = size.height * touchRipple.yFraction,
                ),
                progress = echoProgress.value,
                profile = profile,
                densityScale = policy.secondaryParticleScale,
            )
        }

        if (focusStrength.value > 0.01f && interactionFrame.focusRegion != null) {
            drawMuseV4LivingFocus(
                region = interactionFrame.focusRegion,
                strength = focusStrength.value,
                profile = profile,
                chromeKind = experience.style.chromeKind,
            )
        }

        if (momentumStrength.value > 0.01f) {
            drawMuseV4GestureMomentum(
                x = interactionFrame.momentumX,
                y = interactionFrame.momentumY,
                strength = momentumStrength.value,
                profile = profile,
                echoKind = policy.echoKind,
            )
        }
    }
}


private fun DrawScope.drawMuseV4MorphField(
    blend: MuseWorldBlend,
    phase: Float,
    secondaryScale: Float,
) {
    MuseVisualProfile.entries.forEach { candidate ->
        val weight = blend.weightOf(candidate).coerceIn(0f, 1f)
        if (weight <= 0.01f) return@forEach

        val chrome = candidate.chromePalette()
        val light = candidate.worldLightField()
        val signature = candidate.transitionSignature()
        val center = Offset(
            x = size.width * light.keyX,
            y = size.height * light.keyY,
        )
        val radius = size.minDimension * light.keyRadius

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    chrome.highlight.copy(alpha = 0.045f * weight),
                    chrome.glow.copy(alpha = 0.024f * weight),
                    Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            center = center,
            radius = radius,
        )

        if (signature.sweep > 0.70f) {
            repeat(2) { lane ->
                val travel = (phase + lane * 0.38f) % 1f
                val x = size.width * (-0.15f + travel * 1.30f)
                drawLine(
                    color = chrome.highlight.copy(
                        alpha = 0.10f * weight * secondaryScale,
                    ),
                    start = Offset(x - size.width * 0.16f, 0f),
                    end = Offset(x + size.width * 0.18f, size.height),
                    strokeWidth = (5f + lane * 3f).dp.toPx(),
                )
            }
        }

        if (signature.ignition > 0.70f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        chrome.primary.copy(
                            alpha = 0.085f * weight * secondaryScale,
                        ),
                    ),
                    startY = size.height * 0.56f,
                    endY = size.height,
                ),
            )
        }

        if (signature.mistBloom > 0.70f) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.highlight.copy(
                            alpha = 0.060f * weight * secondaryScale,
                        ),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.58f, size.height * 0.34f),
                    radius = size.width * 0.48f,
                ),
                topLeft = Offset(size.width * 0.06f, size.height * 0.12f),
                size = Size(size.width * 0.88f, size.height * 0.44f),
            )
        }

        if (signature.refraction > 0.70f) {
            repeat(3) { ring ->
                val p = (phase + ring * 0.28f) % 1f
                drawCircle(
                    color = chrome.highlight.copy(
                        alpha = (1f - p) * 0.085f * weight * secondaryScale,
                    ),
                    center = Offset(size.width * 0.50f, size.height * 0.48f),
                    radius = size.minDimension * (0.12f + p * 0.45f),
                    style = Stroke(width = (1.0f + ring * 0.35f).dp.toPx()),
                )
            }
        }

        if (signature.petalBurst > 0.70f) {
            repeat(5) { petal ->
                val angle = phase * 2f * PI.toFloat() + petal * 1.256f
                val orbit = size.minDimension * (0.15f + phase * 0.12f)
                val c = Offset(
                    x = size.width * 0.50f + cos(angle) * orbit,
                    y = size.height * 0.46f + sin(angle) * orbit,
                )
                rotate(
                    degrees = petal * 72f + phase * 35f,
                    pivot = c,
                ) {
                    drawOval(
                        color = chrome.primary.copy(
                            alpha = 0.075f * weight * secondaryScale,
                        ),
                        topLeft = Offset(c.x - 7.dp.toPx(), c.y - 14.dp.toPx()),
                        size = Size(14.dp.toPx(), 28.dp.toPx()),
                    )
                }
            }
        }

        if (signature.dewPulse > 0.70f) {
            repeat(3) { drop ->
                val p = (phase + drop * 0.31f) % 1f
                drawCircle(
                    color = chrome.highlight.copy(
                        alpha = (1f - p) * 0.075f * weight * secondaryScale,
                    ),
                    center = Offset(
                        x = size.width * (0.20f + drop * 0.29f),
                        y = size.height * (0.22f + p * 0.46f),
                    ),
                    radius = size.minDimension * (0.008f + p * 0.018f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
    }
}

private fun DrawScope.drawMuseV4MusicMemory(
    kind: MuseMusicMemoryKind,
    strength: Float,
    phase: Float,
    profile: MuseVisualProfile,
    breathingAmplitude: Float,
    densityScale: Float,
) {
    val chrome = profile.chromePalette()
    val alpha = strength * densityScale
    val center = Offset(size.width * 0.50f, size.height * 0.48f)

    when (kind) {
        MuseMusicMemoryKind.WaterAccumulation -> {
            repeat(3) { index ->
                val p = (phase + index * 0.29f) % 1f
                drawCircle(
                    color = chrome.highlight.copy(alpha = alpha * 0.11f * (1f - p)),
                    center = Offset(
                        size.width * (0.28f + index * 0.22f),
                        size.height * (0.30f + p * 0.30f),
                    ),
                    radius = size.minDimension * (0.008f + strength * 0.012f),
                )
            }
        }

        MuseMusicMemoryKind.PrismAfterimage -> {
            val travel = phase * size.width
            repeat(3) { index ->
                drawLine(
                    color = chrome.highlight.copy(alpha = alpha * (0.07f + index * 0.018f)),
                    start = Offset(travel - size.width * 0.35f + index * 24f, 0f),
                    end = Offset(travel + index * 24f, size.height),
                    strokeWidth = (2f + index).dp.toPx(),
                )
            }
        }

        MuseMusicMemoryKind.HeatResidue -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        chrome.primary.copy(alpha = alpha * 0.13f),
                    ),
                    startY = size.height * 0.60f,
                    endY = size.height,
                ),
            )
        }

        MuseMusicMemoryKind.HazeBloom -> {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.highlight.copy(alpha = alpha * 0.075f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.width * (0.34f + breathingAmplitude * 0.18f),
                ),
                topLeft = Offset(size.width * 0.10f, size.height * 0.24f),
                size = Size(size.width * 0.80f, size.height * 0.42f),
            )
        }

        MuseMusicMemoryKind.InterferenceRing -> {
            repeat(4) { ring ->
                val p = ((phase + ring * 0.19f) % 1f)
                drawCircle(
                    color = chrome.highlight.copy(alpha = alpha * 0.10f * (1f - p)),
                    center = center,
                    radius = size.minDimension * (0.10f + p * 0.52f),
                    style = Stroke(width = (0.8f + ring * 0.24f).dp.toPx()),
                )
            }
        }

        MuseMusicMemoryKind.PetalBloom -> {
            repeat(6) { petal ->
                val angle = phase * 2f * PI.toFloat() + petal * (PI.toFloat() / 3f)
                val c = Offset(
                    center.x + cos(angle) * size.minDimension * 0.12f,
                    center.y + sin(angle) * size.minDimension * 0.08f,
                )
                rotate(petal * 60f + phase * 25f, c) {
                    drawOval(
                        color = chrome.primary.copy(alpha = alpha * 0.075f),
                        topLeft = Offset(c.x - 8.dp.toPx(), c.y - 15.dp.toPx()),
                        size = Size(16.dp.toPx(), 30.dp.toPx()),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMuseV4WorldEcho(
    kind: MuseWorldEchoKind,
    center: Offset,
    progress: Float,
    profile: MuseVisualProfile,
    densityScale: Float,
) {
    val chrome = profile.chromePalette()
    val fade = (1f - progress).coerceIn(0f, 1f)
    val alpha = fade * densityScale

    when (kind) {
        MuseWorldEchoKind.WetFoliageTrail -> {
            drawCircle(
                color = chrome.highlight.copy(alpha = alpha * 0.22f),
                center = center,
                radius = size.minDimension * (0.02f + progress * 0.11f),
                style = Stroke(width = 1.2.dp.toPx()),
            )
            drawLine(
                color = chrome.highlight.copy(alpha = alpha * 0.12f),
                start = center,
                end = Offset(center.x + size.width * 0.03f, center.y + size.height * 0.13f * progress),
                strokeWidth = 1.1.dp.toPx(),
            )
        }

        MuseWorldEchoKind.PrismRibbon -> {
            repeat(3) { lane ->
                drawLine(
                    color = chrome.highlight.copy(alpha = alpha * (0.14f - lane * 0.025f)),
                    start = Offset(center.x - size.width * 0.16f * progress, center.y - lane * 7f),
                    end = Offset(center.x + size.width * 0.20f * progress, center.y + size.height * 0.10f + lane * 8f),
                    strokeWidth = (2f + lane).dp.toPx(),
                )
            }
        }

        MuseWorldEchoKind.EmberTrace -> {
            repeat(6) { spark ->
                val angle = spark * 1.047f + progress * 2f
                val distance = size.minDimension * progress * (0.05f + spark * 0.012f)
                drawCircle(
                    color = chrome.primary.copy(alpha = alpha * 0.18f),
                    center = Offset(
                        center.x + cos(angle) * distance,
                        center.y - sin(angle) * distance,
                    ),
                    radius = (1.4f + spark * 0.22f).dp.toPx(),
                )
            }
        }

        MuseWorldEchoKind.MistDisplacement -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        chrome.highlight.copy(alpha = alpha * 0.075f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = size.minDimension * (0.08f + progress * 0.30f),
                ),
                center = center,
                radius = size.minDimension * (0.08f + progress * 0.30f),
            )
        }

        MuseWorldEchoKind.TidalRipple -> {
            repeat(3) { ring ->
                drawCircle(
                    color = chrome.highlight.copy(alpha = alpha * (0.16f - ring * 0.035f)),
                    center = center,
                    radius = size.minDimension * (
                        0.035f + progress * (0.18f + ring * 0.07f)
                        ),
                    style = Stroke(width = (1.0f + ring * 0.4f).dp.toPx()),
                )
            }
        }

        MuseWorldEchoKind.PetalDisplacement -> {
            repeat(5) { petal ->
                val angle = petal * 1.256f + progress * 0.8f
                val distance = size.minDimension * progress * 0.18f
                val c = Offset(
                    center.x + cos(angle) * distance,
                    center.y + sin(angle) * distance,
                )
                rotate(petal * 72f + progress * 50f, c) {
                    drawOval(
                        color = chrome.primary.copy(alpha = alpha * 0.13f),
                        topLeft = Offset(c.x - 6.dp.toPx(), c.y - 11.dp.toPx()),
                        size = Size(12.dp.toPx(), 22.dp.toPx()),
                    )
                }
            }
        }
    }
}


private fun DrawScope.drawMuseV4LivingFocus(
    region: MuseFocusRegion,
    strength: Float,
    profile: MuseVisualProfile,
    chromeKind: MuseWorldChromeKind,
) {
    val chrome = profile.chromePalette()
    val center = when (region) {
        MuseFocusRegion.Home -> Offset(size.width * 0.50f, size.height * 0.24f)
        MuseFocusRegion.Explore -> Offset(size.width * 0.50f, size.height * 0.30f)
        MuseFocusRegion.Library -> Offset(size.width * 0.50f, size.height * 0.36f)
        MuseFocusRegion.Equalizer -> Offset(size.width * 0.50f, size.height * 0.48f)
        MuseFocusRegion.MuseLab -> Offset(size.width * 0.50f, size.height * 0.42f)
        MuseFocusRegion.Navigation -> Offset(size.width * 0.50f, size.height * 0.90f)
        MuseFocusRegion.NowPlaying -> Offset(size.width * 0.50f, size.height * 0.34f)
    }
    val alpha = strength.coerceIn(0f, 1f)

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                chrome.highlight.copy(alpha = 0.10f * alpha),
                chrome.glow.copy(alpha = 0.050f * alpha),
                Color.Transparent,
            ),
            center = center,
            radius = size.minDimension * 0.34f,
        ),
        center = center,
        radius = size.minDimension * 0.34f,
    )

    when (chromeKind) {
        MuseWorldChromeKind.DewGlass -> {
            repeat(3) { index ->
                drawCircle(
                    color = chrome.highlight.copy(alpha = 0.13f * alpha),
                    center = Offset(
                        center.x + size.width * (index - 1) * 0.055f,
                        center.y + size.height * 0.018f * index,
                    ),
                    radius = size.minDimension * (0.006f + index * 0.002f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        MuseWorldChromeKind.PrismFacet -> {
            repeat(3) { lane ->
                drawLine(
                    color = chrome.highlight.copy(alpha = (0.12f - lane * 0.022f) * alpha),
                    start = Offset(center.x - size.width * 0.16f, center.y + lane * 11.dp.toPx()),
                    end = Offset(center.x + size.width * 0.16f, center.y - lane * 9.dp.toPx()),
                    strokeWidth = (1.2f + lane * 0.5f).dp.toPx(),
                )
            }
        }
        MuseWorldChromeKind.ForgedEmber -> {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        chrome.primary.copy(alpha = 0.16f * alpha),
                        Color.Transparent,
                    ),
                    center = Offset(center.x, center.y + size.height * 0.08f),
                    radius = size.width * 0.30f,
                ),
                topLeft = Offset(center.x - size.width * 0.30f, center.y),
                size = Size(size.width * 0.60f, size.height * 0.18f),
            )
        }
        MuseWorldChromeKind.LunarHalo -> {
            drawOval(
                color = chrome.highlight.copy(alpha = 0.10f * alpha),
                topLeft = Offset(center.x - size.width * 0.28f, center.y - size.height * 0.075f),
                size = Size(size.width * 0.56f, size.height * 0.15f),
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }
        MuseWorldChromeKind.TidalLens -> {
            repeat(2) { ring ->
                drawOval(
                    color = chrome.highlight.copy(alpha = (0.11f - ring * 0.035f) * alpha),
                    topLeft = Offset(
                        center.x - size.width * (0.17f + ring * 0.06f),
                        center.y - size.height * (0.055f + ring * 0.022f),
                    ),
                    size = Size(
                        size.width * (0.34f + ring * 0.12f),
                        size.height * (0.11f + ring * 0.044f),
                    ),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        MuseWorldChromeKind.RoseVelvet -> {
            repeat(3) { petal ->
                rotate(petal * 60f - 60f, center) {
                    drawOval(
                        color = chrome.primary.copy(alpha = 0.075f * alpha),
                        topLeft = Offset(center.x - 9.dp.toPx(), center.y - 25.dp.toPx()),
                        size = Size(18.dp.toPx(), 50.dp.toPx()),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMuseV4GestureMomentum(
    x: Float,
    y: Float,
    strength: Float,
    profile: MuseVisualProfile,
    echoKind: MuseWorldEchoKind,
) {
    val chrome = profile.chromePalette()
    val dx = x.coerceIn(-1f, 1f)
    val dy = y.coerceIn(-1f, 1f)
    val power = strength.coerceIn(0f, 1f)
    val center = Offset(size.width * 0.50f, size.height * 0.54f)
    val travel = Offset(
        x = dx * size.width * 0.26f,
        y = dy * size.height * 0.18f,
    )
    val start = Offset(center.x - travel.x * 0.55f, center.y - travel.y * 0.55f)
    val end = Offset(center.x + travel.x, center.y + travel.y)

    when (echoKind) {
        MuseWorldEchoKind.WetFoliageTrail -> {
            drawLine(
                color = chrome.highlight.copy(alpha = 0.10f * power),
                start = start,
                end = end,
                strokeWidth = 1.4.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            repeat(3) { index ->
                val t = (index + 1) / 4f
                drawCircle(
                    color = chrome.highlight.copy(alpha = 0.13f * power * (1f - t * 0.45f)),
                    center = Offset(
                        start.x + (end.x - start.x) * t,
                        start.y + (end.y - start.y) * t,
                    ),
                    radius = size.minDimension * (0.004f + index * 0.0015f),
                )
            }
        }
        MuseWorldEchoKind.PrismRibbon -> {
            repeat(3) { lane ->
                val offset = (lane - 1) * 8.dp.toPx()
                drawLine(
                    color = chrome.highlight.copy(alpha = (0.12f - lane * 0.02f) * power),
                    start = Offset(start.x, start.y + offset),
                    end = Offset(end.x, end.y + offset),
                    strokeWidth = (1.3f + lane * 0.4f).dp.toPx(),
                )
            }
        }
        MuseWorldEchoKind.EmberTrace -> {
            repeat(5) { spark ->
                val t = (spark + 1) / 6f
                val c = Offset(
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t - spark * 3.dp.toPx(),
                )
                drawCircle(
                    color = chrome.primary.copy(alpha = 0.15f * power * (1f - t * 0.35f)),
                    center = c,
                    radius = (1.5f + spark * 0.35f).dp.toPx(),
                )
            }
        }
        MuseWorldEchoKind.MistDisplacement -> {
            drawLine(
                color = chrome.highlight.copy(alpha = 0.055f * power),
                start = start,
                end = end,
                strokeWidth = 18.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        MuseWorldEchoKind.TidalRipple -> {
            repeat(2) { ring ->
                drawCircle(
                    color = chrome.highlight.copy(alpha = (0.11f - ring * 0.03f) * power),
                    center = end,
                    radius = size.minDimension * (0.035f + ring * 0.035f + power * 0.035f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
        MuseWorldEchoKind.PetalDisplacement -> {
            repeat(4) { petal ->
                val t = (petal + 1) / 5f
                val c = Offset(
                    start.x + (end.x - start.x) * t,
                    start.y + (end.y - start.y) * t,
                )
                rotate(petal * 31f + dx * 22f, c) {
                    drawOval(
                        color = chrome.primary.copy(alpha = 0.085f * power),
                        topLeft = Offset(c.x - 6.dp.toPx(), c.y - 11.dp.toPx()),
                        size = Size(12.dp.toPx(), 22.dp.toPx()),
                    )
                }
            }
        }
    }
}
