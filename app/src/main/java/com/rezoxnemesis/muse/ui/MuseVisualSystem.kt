package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
            core = Color(0xFFD8FF72).copy(alpha = 0.12f),
        )
        drawLightBloom(
            center = Offset(size.width * 0.18f, size.height * 0.52f),
            radius = size.minDimension * 0.40f,
            core = MuseGlow.copy(alpha = 0.055f),
        )

        val bokeh = listOf(
            Bokeh(0.14f, 0.08f, 0.026f, 0.08f),
            Bokeh(0.30f, 0.16f, 0.044f, 0.075f),
            Bokeh(0.57f, 0.10f, 0.060f, 0.095f),
            Bokeh(0.77f, 0.22f, 0.035f, 0.065f),
            Bokeh(0.88f, 0.46f, 0.052f, 0.055f),
            Bokeh(0.43f, 0.39f, 0.028f, 0.045f),
            Bokeh(0.24f, 0.73f, 0.050f, 0.050f),
            Bokeh(0.70f, 0.84f, 0.038f, 0.050f),
            Bokeh(0.90f, 0.91f, 0.060f, 0.040f),
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
            center = Offset(size.width * -0.015f, size.height * 0.16f),
            width = size.width * 0.50f,
            height = size.height * 0.24f,
            rotationDegrees = -28f,
            alpha = 0.94f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 1.03f, size.height * 0.30f),
            width = size.width * 0.53f,
            height = size.height * 0.27f,
            rotationDegrees = 31f,
            alpha = 0.80f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.03f, size.height * 0.79f),
            width = size.width * 0.43f,
            height = size.height * 0.22f,
            rotationDegrees = 20f,
            alpha = 0.70f,
            foreground = false,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.98f, size.height * 0.89f),
            width = size.width * 0.47f,
            height = size.height * 0.23f,
            rotationDegrees = -24f,
            alpha = 0.76f,
            foreground = true,
        )
        drawMuseLeaf(
            center = Offset(size.width * 0.83f, size.height * 0.04f),
            width = size.width * 0.30f,
            height = size.height * 0.15f,
            rotationDegrees = 20f,
            alpha = 0.66f,
            foreground = false,
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
            MuseGlass.copy(alpha = 0.94f),
            Color(0xA806120A),
            Color(0xC0081A0E),
        )
        MuseGlassVariant.Strong -> listOf(
            MuseGlassStrong.copy(alpha = 0.98f),
            Color(0xE607140A),
            Color(0xE30A1A0D),
        )
        MuseGlassVariant.Elevated -> listOf(
            MuseGlassElevated,
            Color(0xE00B2112),
            Color(0xD908160C),
        )
        MuseGlassVariant.Selected -> listOf(
            Color(0xDA173F21),
            Color(0xD90A2111),
            Color(0xE10A170C),
        )
        MuseGlassVariant.Destructive -> listOf(
            Color(0xD43A1214),
            Color(0xD320090B),
            Color(0xE1140708),
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
                            Color.White.copy(alpha = 0.075f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.10f),
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
                            Color.White.copy(alpha = 0.16f),
                            MuseGlow.copy(alpha = 0.16f),
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
                Color(0xFF4EBA3D).copy(alpha = alpha),
                Color(0xFF174F22).copy(alpha = alpha),
                Color(0xFF06170B).copy(alpha = alpha * 0.98f),
            )
        } else {
            listOf(
                Color(0xFF2F7B35).copy(alpha = alpha),
                Color(0xFF10371A).copy(alpha = alpha),
                Color(0xFF051108).copy(alpha = alpha * 0.98f),
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
            color = Color(0xFF8CFF7E).copy(alpha = alpha * if (foreground) 0.20f else 0.10f),
            style = Stroke(width = if (foreground) 1.2.dp.toPx() else 0.7.dp.toPx()),
        )

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFCBFF9B).copy(alpha = alpha * 0.58f),
                    Color(0xFF5DCB5A).copy(alpha = alpha * 0.28f),
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
