package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseBorder
import com.rezoxnemesis.muse.ui.theme.MuseBorderBright
import com.rezoxnemesis.muse.ui.theme.MuseGlass
import com.rezoxnemesis.muse.ui.theme.MuseGlassElevated
import com.rezoxnemesis.muse.ui.theme.MuseGlassStrong
import com.rezoxnemesis.muse.ui.theme.MuseGlow
import com.rezoxnemesis.muse.ui.theme.MuseGlowSoft
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
            entries.firstOrNull { it.storedValue == value }
                ?: Balanced
    }
}

internal enum class MuseGlassVariant {
    Standard,
    Strong,
    Elevated,
    Selected,
    Destructive,
}

@Composable
internal fun MuseAtmosphere(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020604),
                    MuseBackground,
                    Color(0xFF010302),
                ),
            ),
        )

        drawLightBloom(
            center = Offset(size.width * 0.67f, size.height * 0.05f),
            radius = size.minDimension * 0.56f,
            core = Color(0xFFD8FF72).copy(alpha = 0.21f),
        )
        drawLightBloom(
            center = Offset(size.width * 0.18f, size.height * 0.52f),
            radius = size.minDimension * 0.40f,
            core = MuseGlow.copy(alpha = 0.085f),
        )

        val bokeh = listOf(
            Bokeh(0.12f, 0.07f, 0.028f, 0.13f),
            Bokeh(0.29f, 0.13f, 0.050f, 0.11f),
            Bokeh(0.55f, 0.075f, 0.066f, 0.15f),
            Bokeh(0.73f, 0.18f, 0.040f, 0.11f),
            Bokeh(0.91f, 0.27f, 0.052f, 0.09f),
            Bokeh(0.84f, 0.50f, 0.036f, 0.08f),
            Bokeh(0.40f, 0.38f, 0.030f, 0.075f),
            Bokeh(0.19f, 0.58f, 0.038f, 0.065f),
            Bokeh(0.26f, 0.75f, 0.052f, 0.075f),
            Bokeh(0.66f, 0.82f, 0.043f, 0.07f),
            Bokeh(0.91f, 0.91f, 0.060f, 0.065f),
        )
        bokeh.forEach { dot ->
            val radius = size.minDimension * dot.radiusFraction
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFDFFF83).copy(alpha = dot.alpha),
                        MuseGlow.copy(alpha = dot.alpha * 0.34f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * dot.x, size.height * dot.y),
                    radius = radius,
                ),
                radius = radius,
                center = Offset(size.width * dot.x, size.height * dot.y),
            )
        }

        drawMuseLeaf(
            center = Offset(size.width * -0.04f, size.height * 0.14f),
            width = size.width * 0.34f,
            height = size.height * 0.20f,
            rotationDegrees = -30f,
            alpha = 0.98f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.18f, size.height * 0.035f),
            width = size.width * 0.20f,
            height = size.height * 0.11f,
            rotationDegrees = 24f,
            alpha = 0.84f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.98f, size.height * 0.13f),
            width = size.width * 0.30f,
            height = size.height * 0.17f,
            rotationDegrees = 31f,
            alpha = 0.94f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 1.015f, size.height * 0.39f),
            width = size.width * 0.32f,
            height = size.height * 0.19f,
            rotationDegrees = -24f,
            alpha = 0.82f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.015f, size.height * 0.48f),
            width = size.width * 0.25f,
            height = size.height * 0.15f,
            rotationDegrees = 18f,
            alpha = 0.68f,
            foreground = false,
        )
        drawMuseLeaf(
            center = Offset(size.width * -0.015f, size.height * 0.80f),
            width = size.width * 0.31f,
            height = size.height * 0.18f,
            rotationDegrees = 23f,
            alpha = 0.86f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.985f, size.height * 0.86f),
            width = size.width * 0.33f,
            height = size.height * 0.19f,
            rotationDegrees = -23f,
            alpha = 0.92f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.82f, size.height * 0.025f),
            width = size.width * 0.18f,
            height = size.height * 0.105f,
            rotationDegrees = 18f,
            alpha = 0.78f,
            foreground = false,
        )
    }
}

@Composable
internal fun MuseLivingLightOverlay(
    active: Boolean,
    intensity: MuseVisualIntensity = MuseVisualIntensity.Balanced,
    modifier: Modifier = Modifier,
) {
    if (!active) return

    val intensityScale = when (intensity) {
        MuseVisualIntensity.Calm -> 0.60f
        MuseVisualIntensity.Balanced -> 1.0f
        MuseVisualIntensity.Vivid -> 1.35f
    }

    val transition = rememberInfiniteTransition(
        label = "MuseLivingLight",
    )
    val drift by transition.animateFloat(
        initialValue = -0.08f,
        targetValue = 0.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseLivingLightDrift",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 7_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "MuseLivingLightPulse",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.minDimension * 0.72f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFB9FF72).copy(alpha = 0.070f * pulse * intensityScale),
                    MuseGlow.copy(alpha = 0.030f * pulse * intensityScale),
                    Color.Transparent,
                ),
                center = Offset(
                    x = size.width * (0.62f + drift),
                    y = size.height * 0.15f,
                ),
                radius = radius,
            ),
            center = Offset(
                x = size.width * (0.62f + drift),
                y = size.height * 0.15f,
            ),
            radius = radius,
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MuseGreen.copy(alpha = 0.035f * pulse * intensityScale),
                    Color.Transparent,
                ),
                center = Offset(
                    x = size.width * (0.20f - drift * 0.5f),
                    y = size.height * 0.72f,
                ),
                radius = radius * 0.75f,
            ),
            center = Offset(
                x = size.width * (0.20f - drift * 0.5f),
                y = size.height * 0.72f,
            ),
            radius = radius * 0.75f,
        )
    }
}

@Composable
fun MuseBrandMark(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val leaf = Path().apply {
                moveTo(w * 0.19f, h * 0.82f)
                cubicTo(
                    w * 0.32f, h * 0.34f,
                    w * 0.61f, h * 0.12f,
                    w * 0.82f, h * 0.08f,
                )
                cubicTo(
                    w * 0.83f, h * 0.43f,
                    w * 0.68f, h * 0.76f,
                    w * 0.24f, h * 0.88f,
                )
                cubicTo(
                    w * 0.21f, h * 0.87f,
                    w * 0.19f, h * 0.85f,
                    w * 0.19f, h * 0.82f,
                )
                close()
            }

            drawPath(
                path = leaf,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB8FF64),
                        Color(0xFF4CC53E),
                        Color(0xFF0B4B1A),
                        Color(0xFF06210E),
                    ),
                    start = Offset(w * 0.78f, h * 0.08f),
                    end = Offset(w * 0.20f, h * 0.88f),
                ),
            )
            drawPath(
                path = leaf,
                color = Color(0xFFD9FF9C).copy(alpha = 0.72f),
                style = Stroke(width = 1.3.dp.toPx()),
            )
            drawLine(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFFE7FFC5),
                        MuseGreen,
                        Color(0xFFFFB977),
                    ),
                ),
                start = Offset(w * 0.22f, h * 0.84f),
                end = Offset(w * 0.74f, h * 0.15f),
                strokeWidth = 1.7.dp.toPx(),
                cap = StrokeCap.Round,
            )

            val veins = listOf(
                0.34f to 0.55f,
                0.43f to 0.47f,
                0.52f to 0.39f,
                0.61f to 0.30f,
            )
            veins.forEach { (x, y) ->
                drawLine(
                    color = Color.White.copy(alpha = 0.24f),
                    start = Offset(w * x, h * y),
                    end = Offset(w * (x + 0.20f), h * (y - 0.08f)),
                    strokeWidth = 0.8.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            val barXs = listOf(0.67f, 0.73f, 0.79f, 0.85f)
            val barHeights = listOf(0.16f, 0.27f, 0.21f, 0.12f)
            val barColors = listOf(
                MuseGreen,
                Color(0xFFFFD26C),
                Color(0xFFFFA85C),
                MuseGreen,
            )
            barXs.indices.forEach { index ->
                val bh = h * barHeights[index]
                drawLine(
                    color = barColors[index],
                    start = Offset(w * barXs[index], h * 0.58f - bh / 2f),
                    end = Offset(w * barXs[index], h * 0.58f + bh / 2f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            listOf(
                Offset(w * 0.58f, h * 0.22f),
                Offset(w * 0.73f, h * 0.32f),
                Offset(w * 0.36f, h * 0.67f),
            ).forEachIndexed { index, point ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f - index * 0.05f),
                    center = point,
                    radius = (2.2.dp + index.dp * 0.35f).toPx(),
                )
            }
        }

        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(58.dp)
                .offset(x = (-13).dp, y = 2.dp),
        )
    }
}

@Composable
internal fun MuseGlassSurface(
    modifier: Modifier = Modifier,
    variant: MuseGlassVariant = MuseGlassVariant.Standard,
    cornerRadius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val fill = when (variant) {
        MuseGlassVariant.Standard -> listOf(
            Color(0xA60B2614),
            Color(0x7806110A),
            Color(0x9008190E),
        )
        MuseGlassVariant.Strong -> listOf(
            Color(0xC20D2A18),
            Color(0xAE07150B),
            Color(0xB70B2112),
        )
        MuseGlassVariant.Elevated -> listOf(
            Color(0xC511321C),
            Color(0x9E0B2112),
            Color(0xA6091A0D),
        )
        MuseGlassVariant.Selected -> listOf(
            Color(0xD021592E),
            Color(0xB50A2A15),
            Color(0xB70B1E10),
        )
        MuseGlassVariant.Destructive -> listOf(
            Color(0xCB49171A),
            Color(0xA9260B0D),
            Color(0xB818080A),
        )
    }
    val rim = when (variant) {
        MuseGlassVariant.Selected -> listOf(
            MuseGlow.copy(alpha = 0.96f),
            MuseBorderBright.copy(alpha = 0.72f),
            MuseGlowSoft.copy(alpha = 0.44f),
        )
        MuseGlassVariant.Destructive -> listOf(
            Color(0xFFFF7F7F).copy(alpha = 0.86f),
            Color(0xFFB94248).copy(alpha = 0.60f),
            Color(0xFFFF6B6B).copy(alpha = 0.30f),
        )
        MuseGlassVariant.Elevated -> listOf(
            MuseBorderBright.copy(alpha = 0.72f),
            MuseBorder.copy(alpha = 0.70f),
            MuseGlowSoft.copy(alpha = 0.28f),
        )
        else -> listOf(
            MuseBorderBright.copy(alpha = 0.52f),
            MuseBorder.copy(alpha = 0.64f),
            Color(0x3355E85E),
        )
    }
    val elevation = when (variant) {
        MuseGlassVariant.Standard -> 10.dp
        MuseGlassVariant.Strong -> 14.dp
        MuseGlassVariant.Elevated -> 18.dp
        MuseGlassVariant.Selected -> 18.dp
        MuseGlassVariant.Destructive -> 12.dp
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.72f),
                spotColor = when (variant) {
                    MuseGlassVariant.Selected -> MuseGlow.copy(alpha = 0.24f)
                    MuseGlassVariant.Destructive -> Color(0xFFFF5D64).copy(alpha = 0.18f)
                    else -> Color.Black.copy(alpha = 0.80f)
                },
                clip = false,
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = fill,
                    start = Offset.Zero,
                    end = Offset(900f, 1400f),
                ),
            )
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.025f),
                            Color.Black.copy(alpha = 0.08f),
                        ),
                        endY = size.height * 0.80f,
                    ),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        x = cornerRadius.toPx(),
                        y = cornerRadius.toPx(),
                    ),
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            MuseGlow.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * 0.08f, 1.2.dp.toPx()),
                    end = Offset(size.width * 0.92f, 1.2.dp.toPx()),
                    strokeWidth = 0.85.dp.toPx(),
                )
            }
            .border(
                width = if (variant == MuseGlassVariant.Selected) 1.35.dp else 0.9.dp,
                brush = Brush.linearGradient(rim),
                shape = shape,
            ),
    ) {
        content()
    }
}

@Composable
internal fun MuseGlassAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MuseGlassVariant = MuseGlassVariant.Standard,
    cornerRadius: Dp = 22.dp,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val activePress = pressed && enabled
    val scale by animateFloatAsState(
        targetValue = if (activePress) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "MuseGlassPressScale",
    )

    MuseGlassSurface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.48f
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        variant = if (activePress && variant == MuseGlassVariant.Standard) {
            MuseGlassVariant.Selected
        } else {
            variant
        },
        cornerRadius = cornerRadius,
        content = content,
    )
}

private data class Bokeh(
    val x: Float,
    val y: Float,
    val radiusFraction: Float,
    val alpha: Float,
)

private fun DrawScope.drawLightBloom(
    center: Offset,
    radius: Float,
    core: Color,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                core,
                core.copy(alpha = core.alpha * 0.36f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        center = center,
        radius = radius,
    )
}

private fun DrawScope.drawMuseLeaf(
    center: Offset,
    width: Float,
    height: Float,
    rotationDegrees: Float,
    alpha: Float,
    foreground: Boolean,
) {
    rotate(rotationDegrees, pivot = center) {
        val top = center.y - height / 2f
        val bottom = center.y + height / 2f
        val half = width / 2f

        val leaf = Path().apply {
            moveTo(center.x, top)
            cubicTo(
                center.x + half * 0.95f,
                center.y - height * 0.30f,
                center.x + half * 1.02f,
                center.y + height * 0.24f,
                center.x,
                bottom,
            )
            cubicTo(
                center.x - half * 1.02f,
                center.y + height * 0.24f,
                center.x - half * 0.95f,
                center.y - height * 0.30f,
                center.x,
                top,
            )
            close()
        }

        val leafColors = if (foreground) {
            listOf(
                Color(0xFF76ED58).copy(alpha = alpha),
                Color(0xFF2B8737).copy(alpha = alpha),
                Color(0xFF071D0D).copy(alpha = alpha * 0.98f),
            )
        } else {
            listOf(
                Color(0xFF4CAF45).copy(alpha = alpha),
                Color(0xFF195124).copy(alpha = alpha),
                Color(0xFF051309).copy(alpha = alpha * 0.98f),
            )
        }

        drawPath(
            path = leaf,
            brush = Brush.linearGradient(
                colors = leafColors,
                start = Offset(center.x - half, top),
                end = Offset(center.x + half, bottom),
            ),
        )
        drawPath(
            path = leaf,
            color = Color(0xFFB2FF86).copy(alpha = alpha * if (foreground) 0.30f else 0.14f),
            style = Stroke(width = if (foreground) 1.2.dp.toPx() else 0.7.dp.toPx()),
        )

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFE3FFAE).copy(alpha = alpha * 0.72f),
                    Color(0xFF72E666).copy(alpha = alpha * 0.38f),
                    Color.Transparent,
                ),
                startY = top,
                endY = bottom,
            ),
            start = Offset(center.x, top + height * 0.08f),
            end = Offset(center.x, bottom - height * 0.06f),
            strokeWidth = if (foreground) 1.25.dp.toPx() else 0.75.dp.toPx(),
        )

        val veinFractions = listOf(-0.28f, -0.08f, 0.12f, 0.30f)
        veinFractions.forEachIndexed { index, fraction ->
            val y = center.y + height * fraction
            val span = half * (0.62f - index * 0.07f)
            val stem = Offset(center.x, y)
            val veinAlpha = alpha * if (foreground) 0.18f else 0.09f

            drawLine(
                color = Color(0xFFB6F891).copy(alpha = veinAlpha),
                start = stem,
                end = Offset(center.x + span, y - height * 0.10f),
                strokeWidth = 0.65.dp.toPx(),
            )
            drawLine(
                color = Color(0xFFB6F891).copy(alpha = veinAlpha * 0.86f),
                start = stem,
                end = Offset(center.x - span, y - height * 0.10f),
                strokeWidth = 0.65.dp.toPx(),
            )
        }

        if (foreground) {
            val dew = listOf(
                Offset(center.x + width * 0.14f, center.y - height * 0.18f) to 4.2.dp.toPx(),
                Offset(center.x - width * 0.19f, center.y + height * 0.02f) to 2.8.dp.toPx(),
                Offset(center.x + width * 0.23f, center.y + height * 0.17f) to 2.2.dp.toPx(),
            )
            dew.forEach { (point, radius) ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha * 0.72f),
                            MuseGlow.copy(alpha = alpha * 0.24f),
                            Color.Transparent,
                        ),
                        center = point,
                        radius = radius * 1.7f,
                    ),
                    center = point,
                    radius = radius * 1.7f,
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.54f),
                    center = Offset(point.x - radius * 0.22f, point.y - radius * 0.28f),
                    radius = radius * 0.20f,
                )
            }
        }
    }
}
