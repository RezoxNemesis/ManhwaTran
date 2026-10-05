package com.rezoxnemesis.muse.ui

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
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseGlow
import com.rezoxnemesis.muse.ui.theme.MuseGreen

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
    modifier: Modifier = Modifier,
) {
    val strength = when (intensity) {
        MuseVisualIntensity.Calm -> 0.76f
        MuseVisualIntensity.Balanced -> 1.0f
        MuseVisualIntensity.Vivid -> 1.16f
    }
    val transition = rememberInfiniteTransition(label = "NativeBotanicalMotion")
    val drift by transition.animateFloat(
        initialValue = -0.018f,
        targetValue = 0.018f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 8_000 else 16_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "BotanicalDrift",
    )
    val pulse by transition.animateFloat(
        initialValue = if (active) 0.72f else 0.86f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (active) 5_500 else 11_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "BotanicalPulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MuseBackground),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF020806),
                        Color(0xFF03150A),
                        Color(0xFF001006),
                        Color(0xFF020805),
                    ),
                ),
            )

            val sunCenter = Offset(
                x = size.width * (0.78f + drift),
                y = size.height * 0.055f,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF7A0).copy(alpha = 0.22f * pulse * strength),
                        Color(0xFFB8FF63).copy(alpha = 0.12f * strength),
                        Color(0xFF5DDF5B).copy(alpha = 0.035f * strength),
                        Color.Transparent,
                    ),
                    center = sunCenter,
                    radius = size.minDimension * 0.72f,
                ),
                center = sunCenter,
                radius = size.minDimension * 0.72f,
            )

            val bokeh = listOf(
                floatArrayOf(0.16f, 0.13f, 0.055f, 0.12f),
                floatArrayOf(0.28f, 0.08f, 0.032f, 0.15f),
                floatArrayOf(0.52f, 0.12f, 0.044f, 0.13f),
                floatArrayOf(0.67f, 0.18f, 0.028f, 0.18f),
                floatArrayOf(0.88f, 0.16f, 0.052f, 0.12f),
                floatArrayOf(0.11f, 0.38f, 0.064f, 0.08f),
                floatArrayOf(0.35f, 0.31f, 0.030f, 0.10f),
                floatArrayOf(0.72f, 0.37f, 0.050f, 0.09f),
                floatArrayOf(0.92f, 0.48f, 0.034f, 0.12f),
                floatArrayOf(0.15f, 0.69f, 0.050f, 0.08f),
                floatArrayOf(0.44f, 0.75f, 0.035f, 0.10f),
                floatArrayOf(0.80f, 0.72f, 0.061f, 0.08f),
                floatArrayOf(0.61f, 0.91f, 0.042f, 0.08f),
            )
            bokeh.forEachIndexed { index, dot ->
                val center = Offset(
                    size.width * (dot[0] + drift * if (index % 2 == 0) 0.7f else -0.45f),
                    size.height * dot[1],
                )
                val radius = size.minDimension * dot[2]
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (index % 3 == 0) {
                                Color(0xFFFFF3A5).copy(alpha = dot[3] * pulse * strength)
                            } else {
                                MuseGreen.copy(alpha = dot[3] * 0.62f * strength)
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

            nativeLeafLayout(route).forEachIndexed { index, spec ->
                drawBotanicalLeaf(
                    spec = spec.copy(
                        x = spec.x + drift * if (index % 2 == 0) 0.75f else -0.55f,
                    ),
                    strength = strength,
                    pulse = pulse,
                )
            }

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.48f),
                    ),
                    center = Offset(size.width * 0.52f, size.height * 0.46f),
                    radius = size.maxDimension * 0.78f,
                ),
            )
        }
    }
}

private fun nativeLeafLayout(route: String?): List<LeafSpec> =
    when (route) {
        "equalizer" -> listOf(
            LeafSpec(-0.04f, 0.17f, 0.34f, 0.18f, -44f, 0.78f, true),
            LeafSpec(0.97f, 0.22f, 0.29f, 0.16f, 36f, 0.74f, true, true),
            LeafSpec(0.90f, 0.79f, 0.34f, 0.18f, -35f, 0.72f, false),
            LeafSpec(0.09f, 0.93f, 0.30f, 0.16f, 24f, 0.68f, false),
        )
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

        drawPath(
            path = leaf,
            brush = Brush.linearGradient(
                colors = if (spec.foreground) {
                    listOf(
                        Color(0xFFB4FF70).copy(alpha = alpha),
                        Color(0xFF4ECB42).copy(alpha = alpha),
                        Color(0xFF12612B).copy(alpha = alpha),
                        Color(0xFF03170B).copy(alpha = alpha),
                    )
                } else {
                    listOf(
                        Color(0xFF69CF4B).copy(alpha = alpha * 0.78f),
                        Color(0xFF1B6A2A).copy(alpha = alpha * 0.82f),
                        Color(0xFF03130A).copy(alpha = alpha),
                    )
                },
                start = Offset(center.x - half, tip.y),
                end = Offset(center.x + half, base.y),
            ),
        )

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
