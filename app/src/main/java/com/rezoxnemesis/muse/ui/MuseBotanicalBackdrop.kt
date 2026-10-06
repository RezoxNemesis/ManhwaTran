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
    touchRipple: MuseTouchRipple? = null,
    modifier: Modifier = Modifier,
) {
    val palette = profile.backdropPalette()
    val atmosphere = profile.atmosphereBehavior()
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

            nativeLeafLayout(route).forEach { spec ->
                drawBotanicalLeaf(
                    spec = spec,
                    strength = strength,
                    pulse = 0.96f,
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

        if (active) {
            MuseBotanicalMotionOverlay(
                strength = strength,
                palette = palette,
                modifier = Modifier.fillMaxSize(),
            )
        }

        MuseProfileAtmosphereOverlay(
            profile = profile,
            behavior = atmosphere,
            active = active,
            intensity = intensity,
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
                durationMillis = 9_500,
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
                durationMillis = 6_800,
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
) {
    val center = Offset(size.width * spec.x, size.height * spec.y)
    val width = size.width * spec.width
    val height = size.height * spec.height
    val alpha = (spec.alpha * strength).coerceIn(0f, 1f)
    val mirror = if (spec.mirror) -1f else 1f

    rotate(spec.angle, center) {
        val tip = Offset(center.x, center.y - height * 0.50f)
        val base = Offset(center.x, center.y + height * 0.50f)
        val half = width * 0.50f

        val leaf = Path().apply {
            moveTo(tip.x, tip.y)
            cubicTo(
                center.x + half * 0.95f * mirror,
                center.y - height * 0.37f,
                center.x + half * 1.06f * mirror,
                center.y + height * 0.18f,
                base.x,
                base.y,
            )
            cubicTo(
                center.x - half * 0.98f * mirror,
                center.y + height * 0.27f,
                center.x - half * 0.88f * mirror,
                center.y - height * 0.31f,
                tip.x,
                tip.y,
            )
            close()
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
                        Color(0xFFC8FF82).copy(alpha = alpha),
                        Color(0xFF72DF4F).copy(alpha = alpha),
                        Color(0xFF258238).copy(alpha = alpha),
                        Color(0xFF082813).copy(alpha = alpha),
                        Color(0xFF020E07).copy(alpha = alpha),
                    )
                } else {
                    listOf(
                        Color(0xFF7ED95A).copy(alpha = alpha * 0.80f),
                        Color(0xFF2E7C35).copy(alpha = alpha * 0.84f),
                        Color(0xFF07190D).copy(alpha = alpha),
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
                                Color(0xFFE9FFC4).copy(alpha = alpha * spot[3])
                            } else {
                                Color(0xFF174D26).copy(alpha = alpha * spot[3] * 0.90f)
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
                    Color.White.copy(alpha = alpha * 0.42f * pulse),
                    Color(0xFFCBFF8F).copy(alpha = alpha * 0.12f),
                    Color.Transparent,
                ),
                startY = tip.y,
                endY = base.y,
            ),
            style = Stroke(width = width * 0.10f, cap = StrokeCap.Round),
        )

        drawPath(
            path = leaf,
            color = Color(0xFFD7FF9A).copy(alpha = alpha * 0.50f),
            style = Stroke(width = 1.05.dp.toPx()),
        )

        drawLine(
            color = Color(0xFF173D20).copy(alpha = alpha * 0.80f),
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
                    Color(0xFFF2FFD3).copy(alpha = alpha * 0.86f),
                    Color(0xFF8DFF62).copy(alpha = alpha * 0.52f),
                    Color(0xFF1B7C36).copy(alpha = alpha * 0.26f),
                ),
                startY = tip.y,
                endY = base.y,
            ),
            start = Offset(center.x, tip.y + height * 0.04f),
            end = Offset(center.x, base.y - height * 0.04f),
            strokeWidth = 1.45.dp.toPx(),
            cap = StrokeCap.Round,
        )

        val veins = listOf(-0.30f, -0.18f, -0.05f, 0.09f, 0.23f, 0.35f)
        veins.forEachIndexed { index, fraction ->
            val y = center.y + height * fraction
            val taper = 0.41f - index * 0.035f
            val span = half * taper
            val rise = height * (0.105f - index * 0.005f)
            drawLine(
                color = Color(0xFFD8FFAB).copy(alpha = alpha * 0.28f),
                start = Offset(center.x, y),
                end = Offset(center.x + span * mirror, y - rise),
                strokeWidth = 0.72.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color(0xFF8EF067).copy(alpha = alpha * 0.20f),
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
                            Color.White.copy(alpha = 0.90f * alpha),
                            Color(0xFFD7FFCE).copy(alpha = 0.34f * alpha),
                            Color(0xFF285B34).copy(alpha = 0.18f * alpha),
                            Color.Transparent,
                        ),
                        center = Offset(point.x - radius * 0.24f, point.y - radius * 0.28f),
                        radius = radius * 1.45f,
                    ),
                    center = point,
                    radius = radius * 1.45f,
                )
                drawCircle(
                    color = Color.White.copy(alpha = (0.80f - index * 0.08f) * alpha),
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
                        Color.White.copy(alpha = alpha * 0.86f),
                        MuseGlow.copy(alpha = alpha * 0.28f),
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
