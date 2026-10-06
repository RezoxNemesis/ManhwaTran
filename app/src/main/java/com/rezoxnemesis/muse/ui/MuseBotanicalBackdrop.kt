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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseGlow
import com.rezoxnemesis.muse.ui.theme.MuseGreen

internal data class MuseTouchRipple(
    val id: Long,
    val xFraction: Float,
    val yFraction: Float,
)

internal enum class MuseVisualIntensity(
    val storedValue: String,
    val label: String,
) {
    Calm("calm", "Calm"),
    Balanced("balanced", "Balanced"),
    Vivid("vivid", "Vivid");

    fun next(): MuseVisualIntensity =
        when (this) {
            Calm -> Balanced
            Balanced -> Vivid
            Vivid -> Calm
        }

    companion object {
        fun fromStored(value: String?): MuseVisualIntensity =
            entries.firstOrNull { it.storedValue == value } ?: Balanced
    }
}


private data class MuseBackdropPalette(
    val background: List<Color>,
    val keyLight: Color,
    val secondaryLight: Color,
    val rainTint: Color,
)

private fun MuseVisualProfile.backdropPalette(): MuseBackdropPalette =
    when (this) {
        MuseVisualProfile.VerdantRain -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF010604),
                Color(0xFF03170B),
                Color(0xFF001108),
                Color(0xFF020805),
            ),
            keyLight = Color(0xFFDFFF80),
            secondaryLight = Color(0xFF72E666),
            rainTint = Color(0xFFE8FFD8),
        )
        MuseVisualProfile.AuroraGlass -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF01070A),
                Color(0xFF062127),
                Color(0xFF071724),
                Color(0xFF020609),
            ),
            keyLight = Color(0xFF9CFFE1),
            secondaryLight = Color(0xFF71B8FF),
            rainTint = Color(0xFFD8FFF6),
        )
        MuseVisualProfile.MidnightEmber -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF050302),
                Color(0xFF170806),
                Color(0xFF100704),
                Color(0xFF030201),
            ),
            keyLight = Color(0xFFFFB36B),
            secondaryLight = Color(0xFFDB5D3C),
            rainTint = Color(0xFFFFDEC2),
        )
        MuseVisualProfile.MoonlitViolet -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF030208),
                Color(0xFF130B23),
                Color(0xFF0B0718),
                Color(0xFF020105),
            ),
            keyLight = Color(0xFFD4B8FF),
            secondaryLight = Color(0xFF8D78FF),
            rainTint = Color(0xFFE7DCFF),
        )
        MuseVisualProfile.OceanPulse -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF001014),
                Color(0xFF04262D),
                Color(0xFF00202A),
                Color(0xFF00080B),
            ),
            keyLight = Color(0xFF79F4FF),
            secondaryLight = Color(0xFF31BFD2),
            rainTint = Color(0xFFCFFBFF),
        )
        MuseVisualProfile.RoseNoir -> MuseBackdropPalette(
            background = listOf(
                Color(0xFF070207),
                Color(0xFF1C0812),
                Color(0xFF150610),
                Color(0xFF030103),
            ),
            keyLight = Color(0xFFFFA3C5),
            secondaryLight = Color(0xFFCC5A89),
            rainTint = Color(0xFFFFDFEA),
        )
    }

private data class LeafSpec(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val angle: Float,
    val alpha: Float,
    val foreground: Boolean,
    val mirror: Boolean = false,
)

@Composable
internal fun MuseNativeBotanicalBackdrop(
    route: String?,
    active: Boolean,
    intensity: MuseVisualIntensity = MuseVisualIntensity.Balanced,
    profile: MuseVisualProfile = MuseVisualProfile.VerdantRain,
    rainLevel: MuseRainLevel = MuseRainLevel.Rain,
    audioSpectrum: MuseAudioSpectrum = MuseAudioSpectrum(),
    touchRipple: MuseTouchRipple? = null,
    modifier: Modifier = Modifier,
) {
    val palette = profile.backdropPalette()
    val atmosphere = profile.atmosphereBehavior()
    val world = profile.livingWorldStyle()
    val strength = when (intensity) {
        MuseVisualIntensity.Calm -> 0.76f
        MuseVisualIntensity.Balanced -> 1.0f
        MuseVisualIntensity.Vivid -> 1.16f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MuseBackground),
    ) {
        // Keep the expensive botanical geometry static. This makes tab changes much
        // lighter while preserving the rich foliage and glass contrast.
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = palette.background,
                ),
            )

            val sunCenter = Offset(
                x = size.width * 0.79f,
                y = size.height * 0.055f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.keyLight.copy(alpha = 0.24f * strength),
                        palette.secondaryLight.copy(alpha = 0.13f * strength),
                        palette.secondaryLight.copy(alpha = 0.038f * strength),
                        Color.Transparent,
                    ),
                    center = sunCenter,
                    radius = size.minDimension * 0.72f,
                ),
                center = sunCenter,
                radius = size.minDimension * 0.72f,
            )

            val bokeh = listOf(
                floatArrayOf(0.10f, 0.10f, 0.060f, 0.10f),
                floatArrayOf(0.22f, 0.17f, 0.032f, 0.15f),
                floatArrayOf(0.34f, 0.07f, 0.024f, 0.13f),
                floatArrayOf(0.53f, 0.12f, 0.046f, 0.12f),
                floatArrayOf(0.67f, 0.18f, 0.030f, 0.18f),
                floatArrayOf(0.88f, 0.16f, 0.054f, 0.12f),
                floatArrayOf(0.11f, 0.38f, 0.068f, 0.08f),
                floatArrayOf(0.35f, 0.31f, 0.032f, 0.10f),
                floatArrayOf(0.72f, 0.37f, 0.052f, 0.09f),
                floatArrayOf(0.92f, 0.48f, 0.036f, 0.12f),
                floatArrayOf(0.15f, 0.69f, 0.052f, 0.08f),
                floatArrayOf(0.44f, 0.75f, 0.037f, 0.10f),
                floatArrayOf(0.80f, 0.72f, 0.064f, 0.08f),
                floatArrayOf(0.61f, 0.91f, 0.044f, 0.08f),
            )
            bokeh.forEachIndexed { index, dot ->
                val center = Offset(
                    size.width * dot[0],
                    size.height * dot[1],
                )
                val radius = size.minDimension * dot[2]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (index % 3 == 0) {
                                palette.keyLight.copy(alpha = dot[3] * strength)
                            } else {
                                palette.secondaryLight.copy(
                                    alpha = dot[3] * 0.62f * strength,
                                )
                            },
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    center = center,
                    radius = radius,
                )
            }

            nativeLeafLayout(route)
                .filterNot(LeafSpec::foreground)
                .forEach { spec ->
                    drawBotanicalLeaf(
                        spec = spec,
                        strength = strength,
                        pulse = 0.96f,
                        style = world,
                    )
                }

            // A soft centre veil keeps the excellent text clarity from the current
            // build while the leaves stay brighter and more dimensional at the edge.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.04f),
                        Color.Black.copy(alpha = 0.10f),
                        Color.Black.copy(alpha = 0.50f),
                    ),
                    center = Offset(size.width * 0.52f, size.height * 0.47f),
                    radius = size.maxDimension * 0.80f,
                ),
            )
        }

        MuseBotanicalMotionOverlay(
            route = route,
            active = active,
            behavior = atmosphere,
            style = world,
            strength = strength,
            palette = palette,
            modifier = Modifier.fillMaxSize(),
        )

        if (rainLevel != MuseRainLevel.Off) {
            MuseLivingRainOverlay(
                level = rainLevel,
                profile = profile,
                behavior = atmosphere,
                intensity = intensity,
                modifier = Modifier.fillMaxSize(),
            )
        }

        MuseLivingWorldsV3Overlay(
            profile = profile,
            style = world,
            spectrum = audioSpectrum,
            active = active,
            intensity = intensity,
            rainLevel = rainLevel,
            modifier = Modifier.fillMaxSize(),
        )

        MuseAtmosphereV2Overlay(
            route = route,
            profile = profile,
            behavior = atmosphere,
            active = active,
            audioEnergy = audioSpectrum.energy,
            rainLevel = rainLevel,
            intensity = intensity,
            touchRipple = touchRipple,
            modifier = Modifier.fillMaxSize(),
        )

        MuseTouchRippleOverlay(
            ripple = touchRipple,
            palette = palette,
            modifier = Modifier.fillMaxSize(),
        )

        MuseReactiveDropletOverlay(
            ripple = touchRipple,
            profile = profile,
            behavior = atmosphere,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun MuseBotanicalMotionOverlay(
    route: String?,
    active: Boolean,
    behavior: MuseAtmosphereBehavior,
    style: MuseLivingWorldStyle,
    strength: Float,
    palette: MuseBackdropPalette,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "MuseAmbientLight")
    val drift by transition.animateFloat(
        initialValue = -0.018f,
        targetValue = 0.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 7_600 else 12_800,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseAmbientDrift",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 5_400 else 9_800,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseAmbientPulse",
    )

    Canvas(modifier) {
        val upper = Offset(
            x = size.width * (0.76f + drift),
            y = size.height * 0.10f,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.keyLight.copy(alpha = 0.070f * pulse * strength),
                    palette.secondaryLight.copy(alpha = 0.030f * pulse * strength),
                    Color.Transparent,
                ),
                center = upper,
                radius = size.minDimension * 0.56f,
            ),
            center = upper,
            radius = size.minDimension * 0.56f,
        )

        val lower = Offset(
            x = size.width * (0.18f - drift * 0.5f),
            y = size.height * 0.78f,
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.secondaryLight.copy(alpha = 0.030f * pulse * strength),
                    Color.Transparent,
                ),
                center = lower,
                radius = size.minDimension * 0.42f,
            ),
            center = lower,
            radius = size.minDimension * 0.42f,
        )

        // Foreground foliage lives on the animated layer. The back canopy stays
        // still, so this produces real depth/parallax without moving the UI.
        val swayAmplitude = size.width *
            (if (active) 0.012f else 0.0055f) *
            behavior.foliageMotion *
            behavior.depthParallax
        val liftAmplitude = size.height *
            (if (active) 0.0055f else 0.0025f) *
            behavior.foliageMotion *
            behavior.depthParallax

        nativeLeafLayout(route)
            .filter(LeafSpec::foreground)
            .forEachIndexed { index, spec ->
                val direction = if (index % 2 == 0) 1f else -1f
                val localPhase = drift * direction
                val worldMotion = when (style.motionKind) {
                    MuseWorldMotionKind.RainWeightedSway -> Triple(
                        localPhase * 0.30f,
                        localPhase * 0.08f + (1f - pulse) * 0.005f,
                        localPhase * 24f,
                    )
                    MuseWorldMotionKind.RibbonDrift -> Triple(
                        localPhase * 0.18f,
                        (pulse - 0.86f) * 0.010f,
                        localPhase * 11f,
                    )
                    MuseWorldMotionKind.ThermalLift -> Triple(
                        localPhase * 0.08f,
                        -(pulse - 0.72f) * 0.016f,
                        localPhase * 7f,
                    )
                    MuseWorldMotionKind.LunarFloat -> Triple(
                        localPhase * 0.10f,
                        (pulse - 0.84f) * 0.006f,
                        localPhase * 8f,
                    )
                    MuseWorldMotionKind.TidalPulse -> Triple(
                        localPhase * 0.22f,
                        (pulse - 0.82f) * 0.014f,
                        localPhase * 15f,
                    )
                    MuseWorldMotionKind.PetalOrbit -> Triple(
                        localPhase * 0.16f,
                        direction * (pulse - 0.84f) * 0.010f,
                        localPhase * 18f,
                    )
                }
                val shifted = spec.copy(
                    x = spec.x + worldMotion.first * behavior.foliageMotion,
                    y = spec.y + worldMotion.second * behavior.foliageMotion,
                    angle = spec.angle + worldMotion.third * behavior.foliageMotion,
                )
                drawBotanicalLeaf(
                    spec = shifted,
                    strength = strength,
                    pulse = 0.90f + pulse * 0.10f,
                    style = style,
                )

                // A tiny moving specular bead gives wet leaves a physical shimmer
                // without turning the foliage into a flashing visualizer.
                if (index % 2 == 0) {
                    val center = Offset(
                        x = size.width * shifted.x +
                            swayAmplitude * direction * pulse,
                        y = size.height * shifted.y -
                            liftAmplitude * pulse,
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.11f * pulse * strength),
                                palette.keyLight.copy(alpha = 0.045f * pulse * strength),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = 8.dp.toPx(),
                        ),
                        center = center,
                        radius = 8.dp.toPx(),
                    )
                }
            }
    }
}



@Composable
private fun MuseTouchRippleOverlay(
    ripple: MuseTouchRipple?,
    palette: MuseBackdropPalette,
    modifier: Modifier = Modifier,
) {
    if (ripple == null) return

    val progress = remember { Animatable(1f) }
    LaunchedEffect(ripple.id) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 820,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Canvas(modifier) {
        val p = progress.value.coerceIn(0f, 1f)
        val center = Offset(
            x = size.width * ripple.xFraction,
            y = size.height * ripple.yFraction,
        )
        val fade = (1f - p).coerceIn(0f, 1f)
        val baseRadius = size.minDimension * (0.035f + 0.24f * p)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    palette.keyLight.copy(alpha = 0.11f * fade),
                    palette.secondaryLight.copy(alpha = 0.055f * fade),
                    Color.Transparent,
                ),
                center = center,
                radius = baseRadius.coerceAtLeast(1f),
            ),
            center = center,
            radius = baseRadius.coerceAtLeast(1f),
        )

        repeat(3) { ring ->
            val delayed = (p - ring * 0.10f).coerceIn(0f, 1f)
            if (delayed <= 0f) return@repeat
            val ringFade = (1f - delayed) * (1f - ring * 0.18f)
            drawCircle(
                color = palette.rainTint.copy(
                    alpha = 0.30f * ringFade,
                ),
                radius = size.minDimension *
                    (0.025f + delayed * (0.16f + ring * 0.045f)),
                center = center,
                style = Stroke(
                    width = (1.25f - ring * 0.18f)
                        .coerceAtLeast(0.7f)
                        .dp
                        .toPx(),
                ),
            )
        }
    }
}

@Composable
private fun MuseRainOverlay(
    level: MuseRainLevel,
    palette: MuseBackdropPalette,
    intensity: MuseVisualIntensity,
    modifier: Modifier = Modifier,
) {
    if (level == MuseRainLevel.Off) return

    val transition = rememberInfiniteTransition(label = "MuseRain")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1700f / level.speedMultiplier)
                    .toInt()
                    .coerceAtLeast(620),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseRainPhase",
    )
    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.72f
        MuseVisualIntensity.Balanced -> 1f
        MuseVisualIntensity.Vivid -> 1.24f
    }

    Canvas(modifier) {
        repeat(level.dropCount) { index ->
            val xSeed = ((index * 37 + 11) % 101) / 100f
            val ySeed = ((index * 53 + 7) % 113) / 112f
            val speed = 0.58f + ((index * 19) % 37) / 100f
            val drift = (((index * 13) % 17) - 8) / 1000f
            val normalizedY = (ySeed + phase * speed) % 1.12f - 0.06f
            val x = size.width * (xSeed + normalizedY * (0.020f + drift))
            val y = size.height * normalizedY
            val longDrop = index % 5 == 0
            val length = size.height * if (longDrop) 0.034f else 0.020f
            val alpha = (
                if (longDrop) 0.22f else 0.13f
            ) * intensityScale

            drawLine(
                color = palette.rainTint.copy(alpha = alpha.coerceAtMost(0.34f)),
                start = Offset(x, y),
                end = Offset(
                    x + size.width * 0.006f,
                    y + length,
                ),
                strokeWidth = if (longDrop) 1.05.dp.toPx() else 0.72.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun nativeLeafLayout(route: String?): List<LeafSpec> =
    when (route) {
        "sleep" -> listOf(
            LeafSpec(0.02f, 0.19f, 0.39f, 0.21f, -38f, 0.90f, true),
            LeafSpec(0.96f, 0.16f, 0.34f, 0.19f, 40f, 0.86f, true, true),
            LeafSpec(0.09f, 0.55f, 0.27f, 0.16f, -52f, 0.78f, true),
            LeafSpec(0.92f, 0.58f, 0.28f, 0.15f, 45f, 0.74f, true, true),
            LeafSpec(0.20f, 0.92f, 0.37f, 0.20f, 28f, 0.76f, false),
        )
        "album", "artist" -> listOf(
            LeafSpec(0.04f, 0.12f, 0.33f, 0.18f, -48f, 0.86f, true),
            LeafSpec(0.94f, 0.10f, 0.32f, 0.18f, 44f, 0.88f, true, true),
            LeafSpec(0.05f, 0.39f, 0.23f, 0.14f, -61f, 0.72f, false),
            LeafSpec(0.97f, 0.46f, 0.25f, 0.14f, 52f, 0.74f, true, true),
            LeafSpec(0.17f, 0.90f, 0.37f, 0.20f, 34f, 0.76f, false),
            LeafSpec(0.88f, 0.91f, 0.33f, 0.18f, -35f, 0.78f, false, true),
        )
        "downloads", "more" -> listOf(
            LeafSpec(0.03f, 0.16f, 0.34f, 0.19f, -43f, 0.86f, true),
            LeafSpec(0.95f, 0.13f, 0.31f, 0.18f, 39f, 0.82f, true, true),
            LeafSpec(0.94f, 0.64f, 0.30f, 0.17f, 48f, 0.80f, true, true),
            LeafSpec(0.06f, 0.76f, 0.28f, 0.16f, -55f, 0.74f, false),
            LeafSpec(0.82f, 0.93f, 0.35f, 0.19f, -28f, 0.78f, true, true),
        )
        else -> listOf(
            LeafSpec(0.02f, 0.14f, 0.36f, 0.20f, -44f, 0.88f, true),
            LeafSpec(0.95f, 0.12f, 0.33f, 0.18f, 38f, 0.84f, true, true),
            LeafSpec(0.04f, 0.48f, 0.25f, 0.14f, -57f, 0.70f, false),
            LeafSpec(0.96f, 0.55f, 0.28f, 0.16f, 48f, 0.72f, true, true),
            LeafSpec(0.16f, 0.91f, 0.38f, 0.21f, 31f, 0.78f, true),
            LeafSpec(0.88f, 0.92f, 0.34f, 0.19f, -33f, 0.76f, false, true),
        )
    }

private fun DrawScope.drawBotanicalLeaf(
    spec: LeafSpec,
    strength: Float,
    pulse: Float,
    style: MuseLivingWorldStyle,
) {
    val center = Offset(size.width * spec.x, size.height * spec.y)
    val width = size.width * spec.width
    val height = size.height * spec.height
    val alpha = (spec.alpha * strength).coerceIn(0f, 1f)
    val mirror = if (spec.mirror) -1f else 1f
    val geometry = style.foliageKind.geometry()

    rotate(spec.angle, center) {
        val tip = Offset(center.x, center.y - height * 0.50f)
        val base = Offset(center.x, center.y + height * 0.50f)
        val half = width * 0.50f

        val shoulder = half * geometry.shoulderWidth
        val waist = half * geometry.waistWidth
        val wave = height * geometry.waveAmount
        val asymmetry = height * geometry.asymmetry

        val leaf = Path().apply {
            when (style.foliageKind) {
                MuseWorldFoliageKind.WetBroadleaf -> {
                    // Broad rain leaf with irregular, lightly serrated shoulders.
                    moveTo(tip.x, tip.y)
                    lineTo(center.x + shoulder * 0.48f * mirror, center.y - height * 0.41f)
                    lineTo(center.x + shoulder * 0.74f * mirror, center.y - height * 0.33f)
                    lineTo(center.x + shoulder * 0.66f * mirror, center.y - height * 0.27f)
                    lineTo(center.x + shoulder * 1.02f * mirror, center.y - height * 0.12f)
                    lineTo(center.x + shoulder * 0.82f * mirror, center.y - height * 0.03f)
                    lineTo(center.x + shoulder * 0.96f * mirror, center.y + height * 0.15f)
                    lineTo(center.x + waist * 0.66f * mirror, center.y + height * 0.31f)
                    lineTo(base.x, base.y)
                    lineTo(center.x - waist * 0.61f * mirror, center.y + height * 0.29f + asymmetry * 0.18f)
                    lineTo(center.x - shoulder * 0.92f * mirror, center.y + height * 0.12f)
                    lineTo(center.x - shoulder * 0.77f * mirror, center.y - height * 0.02f)
                    lineTo(center.x - shoulder * 0.96f * mirror, center.y - height * 0.17f)
                    lineTo(center.x - shoulder * 0.62f * mirror, center.y - height * 0.30f)
                    lineTo(center.x - shoulder * 0.46f * mirror, center.y - height * 0.41f)
                    close()
                }
                MuseWorldFoliageKind.PrismBlade -> {
                    // Aurora foliage reads as a translucent faceted blade.
                    moveTo(tip.x, tip.y)
                    lineTo(center.x + shoulder * 0.82f * mirror, center.y - height * 0.19f)
                    lineTo(center.x + waist * 0.72f * mirror, center.y + height * 0.17f)
                    lineTo(base.x, base.y)
                    lineTo(center.x - waist * 0.64f * mirror, center.y + height * 0.13f)
                    lineTo(center.x - shoulder * 0.72f * mirror, center.y - height * 0.23f)
                    close()
                }
                MuseWorldFoliageKind.CharredShard -> {
                    // Ember foliage is intentionally broken and asymmetric.
                    moveTo(tip.x, tip.y)
                    lineTo(center.x + shoulder * 0.42f * mirror, center.y - height * 0.39f)
                    lineTo(center.x + shoulder * 0.88f * mirror, center.y - height * 0.28f)
                    lineTo(center.x + shoulder * 0.62f * mirror, center.y - height * 0.16f)
                    lineTo(center.x + shoulder * 1.04f * mirror, center.y - height * 0.01f)
                    lineTo(center.x + waist * 0.58f * mirror, center.y + height * 0.13f)
                    lineTo(center.x + waist * 0.84f * mirror, center.y + height * 0.29f)
                    lineTo(base.x, base.y)
                    lineTo(center.x - waist * 0.44f * mirror, center.y + height * 0.33f + asymmetry * 0.24f)
                    lineTo(center.x - shoulder * 0.82f * mirror, center.y + height * 0.19f)
                    lineTo(center.x - shoulder * 0.52f * mirror, center.y + height * 0.04f)
                    lineTo(center.x - shoulder * 0.91f * mirror, center.y - height * 0.13f)
                    lineTo(center.x - shoulder * 0.53f * mirror, center.y - height * 0.29f - asymmetry * 0.20f)
                    close()
                }
                MuseWorldFoliageKind.MoonLance -> {
                    // Moonlit leaves are long, quiet silver lances.
                    moveTo(tip.x, tip.y)
                    cubicTo(
                        center.x + shoulder * 0.70f * mirror,
                        center.y - height * 0.34f,
                        center.x + waist * 0.78f * mirror,
                        center.y + height * 0.18f,
                        base.x,
                        base.y,
                    )
                    cubicTo(
                        center.x - waist * 0.70f * mirror,
                        center.y + height * 0.16f,
                        center.x - shoulder * 0.62f * mirror,
                        center.y - height * 0.36f,
                        tip.x,
                        tip.y,
                    )
                    close()
                }
                MuseWorldFoliageKind.TidalFrond -> {
                    // Ocean foliage bends in an S-curve instead of behaving like a leaf.
                    moveTo(tip.x, tip.y)
                    cubicTo(
                        center.x + shoulder * 0.42f * mirror,
                        center.y - height * 0.38f,
                        center.x + shoulder * 1.02f * mirror,
                        center.y - height * 0.15f + wave * 0.25f,
                        center.x + waist * 0.70f * mirror,
                        center.y + wave * 0.10f,
                    )
                    cubicTo(
                        center.x + waist * 0.32f * mirror,
                        center.y + height * 0.24f,
                        center.x + waist * 0.48f * mirror,
                        center.y + height * 0.38f,
                        base.x,
                        base.y,
                    )
                    cubicTo(
                        center.x - waist * 0.62f * mirror,
                        center.y + height * 0.34f,
                        center.x - shoulder * 0.92f * mirror,
                        center.y + height * 0.08f - wave * 0.20f,
                        center.x - waist * 0.58f * mirror,
                        center.y - height * 0.10f,
                    )
                    cubicTo(
                        center.x - shoulder * 0.28f * mirror,
                        center.y - height * 0.31f,
                        center.x - shoulder * 0.18f * mirror,
                        center.y - height * 0.43f,
                        tip.x,
                        tip.y,
                    )
                    close()
                }
                MuseWorldFoliageKind.VelvetPetal -> {
                    // Rose Noir uses full petal lobes rather than botanical blades.
                    moveTo(tip.x, tip.y)
                    cubicTo(
                        center.x + shoulder * 0.88f * mirror,
                        center.y - height * 0.39f,
                        center.x + shoulder * 1.08f * mirror,
                        center.y - height * 0.03f,
                        center.x + waist * 0.66f * mirror,
                        center.y + height * 0.19f,
                    )
                    cubicTo(
                        center.x + waist * 0.34f * mirror,
                        center.y + height * 0.33f,
                        center.x + waist * 0.14f * mirror,
                        center.y + height * 0.43f,
                        base.x,
                        base.y,
                    )
                    cubicTo(
                        center.x - waist * 0.22f * mirror,
                        center.y + height * 0.40f,
                        center.x - waist * 0.48f * mirror,
                        center.y + height * 0.28f,
                        center.x - waist * 0.72f * mirror,
                        center.y + height * 0.11f,
                    )
                    cubicTo(
                        center.x - shoulder * 1.02f * mirror,
                        center.y - height * 0.09f,
                        center.x - shoulder * 0.78f * mirror,
                        center.y - height * 0.40f,
                        tip.x,
                        tip.y,
                    )
                    close()
                }
            }
        }

        // A dark offset silhouette gives each leaf real separation from the forest.
        drawPath(
            path = leaf,
            color = Color.Black.copy(alpha = alpha * if (spec.foreground) 0.28f else 0.18f),
        )
        drawPath(
            path = leaf,
            brush = Brush.linearGradient(
                colors = if (spec.foreground) {
                    listOf(
                        style.leafHighlight.copy(alpha = alpha),
                        style.leafPrimary.copy(alpha = alpha),
                        style.leafSecondary.copy(alpha = alpha),
                        style.leafShadow.copy(alpha = alpha),
                        Color.Black.copy(alpha = alpha * 0.92f),
                    )
                } else {
                    listOf(
                        style.leafPrimary.copy(alpha = alpha * 0.72f),
                        style.leafSecondary.copy(alpha = alpha * 0.82f),
                        style.leafShadow.copy(alpha = alpha),
                    )
                },
                start = Offset(center.x - half * 0.78f, tip.y),
                end = Offset(center.x + half, base.y),
            ),
        )

        if (spec.foreground) {
            // Subtle chlorophyll mottling and wet-surface sparkle. Deterministic
            // positions keep the renderer cheap and stable across frames.
            val texture = listOf(
                floatArrayOf(-0.23f, -0.27f, 0.020f, 0.15f),
                floatArrayOf(0.18f, -0.31f, 0.014f, 0.18f),
                floatArrayOf(-0.31f, -0.08f, 0.018f, 0.12f),
                floatArrayOf(0.28f, 0.02f, 0.022f, 0.13f),
                floatArrayOf(-0.16f, 0.16f, 0.016f, 0.14f),
                floatArrayOf(0.17f, 0.28f, 0.013f, 0.16f),
            )
            texture.forEachIndexed { index, spot ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (index % 2 == 0) {
                                style.subsurfaceTint.copy(
                                    alpha = alpha * spot[3] * style.subsurfaceLight,
                                )
                            } else {
                                style.leafShadow.copy(
                                    alpha = alpha * spot[3] * style.surfaceRoughness,
                                )
                            },
                            Color.Transparent,
                        ),
                        center = Offset(
                            center.x + width * spot[0] * mirror,
                            center.y + height * spot[1],
                        ),
                        radius = width * spot[2] * 3.0f,
                    ),
                    center = Offset(
                        center.x + width * spot[0] * mirror,
                        center.y + height * spot[1],
                    ),
                    radius = width * spot[2] * 3.0f,
                )
            }
        }

        val gloss = Path().apply {
            moveTo(center.x - width * 0.12f * mirror, tip.y + height * 0.09f)
            cubicTo(
                center.x + width * 0.22f * mirror,
                center.y - height * 0.25f,
                center.x + width * 0.17f * mirror,
                center.y + height * 0.11f,
                center.x + width * 0.02f * mirror,
                base.y - height * 0.13f,
            )
        }
        drawPath(
            path = gloss,
            brush = Brush.verticalGradient(
                colors = listOf(
                    style.specularTint.copy(
                        alpha = alpha *
                            (0.22f + (1f - style.surfaceRoughness) * 0.34f) *
                            pulse,
                    ),
                    style.subsurfaceTint.copy(
                        alpha = alpha * 0.14f * style.subsurfaceLight,
                    ),
                    Color.Transparent,
                ),
                startY = tip.y,
                endY = base.y,
            ),
            style = Stroke(width = width * 0.10f, cap = StrokeCap.Round),
        )

        drawPath(
            path = leaf,
            color = style.leafHighlight.copy(
                alpha = alpha * (0.22f + style.subsurfaceLight * 0.28f),
            ),
            style = Stroke(width = 1.05.dp.toPx()),
        )

        drawLine(
            color = style.leafShadow.copy(alpha = alpha * 0.88f),
            start = base,
            end = Offset(
                center.x + width * 0.055f * mirror,
                base.y + height * 0.16f,
            ),
            strokeWidth = if (spec.foreground) 2.1.dp.toPx() else 1.2.dp.toPx(),
            cap = StrokeCap.Round,
        )

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    style.specularTint.copy(alpha = alpha * 0.72f),
                    style.vein.copy(alpha = alpha * 0.58f),
                    style.leafSecondary.copy(alpha = alpha * 0.30f),
                ),
                startY = tip.y,
                endY = base.y,
            ),
            start = Offset(center.x, tip.y + height * 0.04f),
            end = Offset(center.x, base.y - height * 0.04f),
            strokeWidth = 1.45.dp.toPx(),
            cap = StrokeCap.Round,
        )

        val veins = when (style.foliageKind) {
            MuseWorldFoliageKind.WetBroadleaf ->
                listOf(-0.33f, -0.23f, -0.12f, 0.00f, 0.12f, 0.24f, 0.35f)
            MuseWorldFoliageKind.PrismBlade ->
                listOf(-0.24f, 0.02f, 0.27f)
            MuseWorldFoliageKind.CharredShard ->
                listOf(-0.31f, -0.17f, -0.03f, 0.14f, 0.30f)
            MuseWorldFoliageKind.MoonLance ->
                listOf(-0.24f, 0.03f, 0.29f)
            MuseWorldFoliageKind.TidalFrond ->
                listOf(-0.28f, -0.08f, 0.13f, 0.31f)
            MuseWorldFoliageKind.VelvetPetal ->
                listOf(-0.24f, -0.06f, 0.13f, 0.28f)
        }
        veins.forEachIndexed { index, fraction ->
            val y = center.y + height * fraction
            val taper = 0.41f - index * 0.035f
            val span = half * taper
            val rise = height * (0.105f - index * 0.005f)
            drawLine(
                color = style.vein.copy(
                    alpha = alpha * (0.18f + style.subsurfaceLight * 0.18f),
                ),
                start = Offset(center.x, y),
                end = Offset(center.x + span * mirror, y - rise),
                strokeWidth = 0.72.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = style.subsurfaceTint.copy(
                    alpha = alpha * (0.10f + style.subsurfaceLight * 0.14f),
                ),
                start = Offset(center.x, y),
                end = Offset(center.x - span * 0.88f * mirror, y - rise * 0.86f),
                strokeWidth = 0.64.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        if (spec.foreground) {
            val dew = listOf(
                Triple(0.20f, -0.20f, 0.034f),
                Triple(-0.18f, -0.05f, 0.026f),
                Triple(0.28f, 0.11f, 0.021f),
                Triple(-0.11f, 0.24f, 0.017f),
            )
            dew.forEachIndexed { index, drop ->
                val point = Offset(
                    center.x + width * drop.first * mirror,
                    center.y + height * drop.second,
                )
                val radius = width * drop.third
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(
                                alpha = 0.72f * alpha * style.waterAdhesion,
                            ),
                            style.specularTint.copy(
                                alpha = 0.36f * alpha * style.waterAdhesion,
                            ),
                            style.leafShadow.copy(alpha = 0.16f * alpha),
                            Color.Transparent,
                        ),
                        center = Offset(point.x - radius * 0.24f, point.y - radius * 0.28f),
                        radius = radius * 1.45f,
                    ),
                    center = point,
                    radius = radius * 1.45f,
                )
                drawCircle(
                    color = Color.White.copy(
                        alpha = (0.72f - index * 0.07f) *
                            alpha *
                            style.waterAdhesion,
                    ),
                    center = Offset(point.x - radius * 0.34f, point.y - radius * 0.38f),
                    radius = radius * 0.22f,
                )
            }

            val dropCenter = Offset(
                center.x + width * 0.06f * mirror,
                base.y + height * 0.035f,
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = alpha * 0.76f * style.waterAdhesion,
                        ),
                        style.specularTint.copy(
                            alpha = alpha * 0.32f * style.waterAdhesion,
                        ),
                        Color.Transparent,
                    ),
                    center = Offset(dropCenter.x - width * 0.006f, dropCenter.y - height * 0.006f),
                    radius = width * 0.055f,
                ),
                topLeft = Offset(dropCenter.x - width * 0.032f, dropCenter.y - height * 0.028f),
                size = Size(width * 0.064f, height * 0.060f),
            )
        }
    }
}
