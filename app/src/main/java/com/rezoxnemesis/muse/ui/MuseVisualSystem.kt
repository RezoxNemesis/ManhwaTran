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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
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

internal enum class MuseGlassVariant {
    Standard,
    Strong,
    Elevated,
    Selected,
    Destructive,
}

internal val LocalMuseChromePalette = staticCompositionLocalOf {
    MuseVisualProfile.VerdantRain.chromePalette()
}

internal val LocalMuseLivingWorldStyle = staticCompositionLocalOf {
    MuseVisualProfile.VerdantRain.livingWorldStyle()
}

internal val LocalMuseChromeMotionPhase = staticCompositionLocalOf { 0f }

@Composable
internal fun MuseChromePaletteProvider(
    profile: MuseVisualProfile,
    content: @Composable () -> Unit,
) {
    val palette = remember(profile) {
        profile.chromePalette()
    }
    val world = remember(profile) {
        profile.livingWorldStyle()
    }
    val chromeGeometry = remember(world.chromeKind) {
        world.chromeKind.geometry()
    }
    val transition = rememberInfiniteTransition(label = "MuseWorldChrome")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = chromeGeometry.motionPeriodMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MuseWorldChromePhase",
    )

    CompositionLocalProvider(
        LocalMuseChromePalette provides palette,
        LocalMuseLivingWorldStyle provides world,
        LocalMuseChromeMotionPhase provides phase,
        content = content,
    )
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
    val chrome = LocalMuseChromePalette.current
    val world = LocalMuseLivingWorldStyle.current
    val phase = LocalMuseChromeMotionPhase.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val motif = Path().apply {
                when (world.foliageKind) {
                    MuseWorldFoliageKind.WetBroadleaf -> {
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
                    }
                    MuseWorldFoliageKind.PrismBlade -> {
                        moveTo(w * 0.20f, h * 0.84f)
                        lineTo(w * 0.35f, h * 0.42f)
                        lineTo(w * 0.78f, h * 0.08f)
                        lineTo(w * 0.70f, h * 0.52f)
                        lineTo(w * 0.28f, h * 0.90f)
                    }
                    MuseWorldFoliageKind.CharredShard -> {
                        moveTo(w * 0.18f, h * 0.86f)
                        lineTo(w * 0.31f, h * 0.58f)
                        lineTo(w * 0.27f, h * 0.43f)
                        lineTo(w * 0.48f, h * 0.33f)
                        lineTo(w * 0.54f, h * 0.13f)
                        lineTo(w * 0.70f, h * 0.24f)
                        lineTo(w * 0.82f, h * 0.08f)
                        lineTo(w * 0.75f, h * 0.46f)
                        lineTo(w * 0.59f, h * 0.60f)
                        lineTo(w * 0.63f, h * 0.77f)
                        lineTo(w * 0.25f, h * 0.91f)
                    }
                    MuseWorldFoliageKind.MoonLance -> {
                        moveTo(w * 0.20f, h * 0.87f)
                        cubicTo(
                            w * 0.31f, h * 0.38f,
                            w * 0.58f, h * 0.10f,
                            w * 0.82f, h * 0.10f,
                        )
                        cubicTo(
                            w * 0.61f, h * 0.31f,
                            w * 0.47f, h * 0.68f,
                            w * 0.25f, h * 0.91f,
                        )
                    }
                    MuseWorldFoliageKind.TidalFrond -> {
                        moveTo(w * 0.18f, h * 0.86f)
                        cubicTo(
                            w * 0.27f, h * 0.60f,
                            w * 0.62f, h * 0.54f,
                            w * 0.47f, h * 0.34f,
                        )
                        cubicTo(
                            w * 0.35f, h * 0.18f,
                            w * 0.66f, h * 0.11f,
                            w * 0.82f, h * 0.08f,
                        )
                        cubicTo(
                            w * 0.73f, h * 0.38f,
                            w * 0.47f, h * 0.53f,
                            w * 0.30f, h * 0.90f,
                        )
                    }
                    MuseWorldFoliageKind.VelvetPetal -> {
                        moveTo(w * 0.22f, h * 0.87f)
                        cubicTo(
                            w * 0.12f, h * 0.57f,
                            w * 0.27f, h * 0.21f,
                            w * 0.50f, h * 0.23f,
                        )
                        cubicTo(
                            w * 0.59f, h * 0.06f,
                            w * 0.88f, h * 0.14f,
                            w * 0.82f, h * 0.40f,
                        )
                        cubicTo(
                            w * 0.76f, h * 0.69f,
                            w * 0.51f, h * 0.85f,
                            w * 0.26f, h * 0.91f,
                        )
                    }
                }
                close()
            }

            drawPath(
                path = motif,
                brush = Brush.linearGradient(
                    colors = listOf(
                        world.leafHighlight,
                        world.leafPrimary,
                        world.leafSecondary,
                        world.leafShadow,
                    ),
                    start = Offset(w * 0.78f, h * 0.08f),
                    end = Offset(w * 0.20f, h * 0.90f),
                ),
            )
            drawPath(
                path = motif,
                color = chrome.highlight.copy(alpha = 0.70f),
                style = Stroke(width = 1.35.dp.toPx()),
            )
            drawLine(
                brush = Brush.linearGradient(
                    listOf(
                        chrome.highlight,
                        chrome.primaryStrong,
                        world.specularTint,
                    ),
                ),
                start = Offset(w * 0.22f, h * 0.84f),
                end = Offset(w * 0.74f, h * 0.15f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // Each world carries a tiny moving signature inside the brand mark.
            when (world.chromeKind) {
                MuseWorldChromeKind.DewGlass -> {
                    repeat(3) { index ->
                        val x = w * (0.38f + index * 0.14f)
                        val y = h * (0.30f + ((phase + index * 0.23f) % 1f) * 0.38f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.78f), Color.Transparent),
                                center = Offset(x, y),
                                radius = 4.2.dp.toPx(),
                            ),
                            center = Offset(x, y),
                            radius = 4.2.dp.toPx(),
                        )
                    }
                }
                MuseWorldChromeKind.PrismFacet -> {
                    val travel = w * (-0.05f + phase * 0.80f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.42f),
                        start = Offset(travel, h * 0.75f),
                        end = Offset(travel + w * 0.34f, h * 0.18f),
                        strokeWidth = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                MuseWorldChromeKind.ForgedEmber -> {
                    repeat(4) { index ->
                        val local = (phase + index * 0.21f) % 1f
                        drawCircle(
                            color = chrome.highlight.copy(alpha = (1f - local) * 0.66f),
                            center = Offset(
                                w * (0.40f + index * 0.10f),
                                h * (0.74f - local * 0.48f),
                            ),
                            radius = (1.4f + index * 0.25f).dp.toPx(),
                        )
                    }
                }
                MuseWorldChromeKind.LunarHalo -> {
                    drawCircle(
                        color = chrome.highlight.copy(alpha = 0.18f + phase * 0.10f),
                        center = Offset(w * 0.61f, h * 0.35f),
                        radius = w * (0.19f + phase * 0.035f),
                        style = Stroke(width = 1.2.dp.toPx()),
                    )
                }
                MuseWorldChromeKind.TidalLens -> {
                    repeat(2) { index ->
                        val local = (phase + index * 0.45f) % 1f
                        drawOval(
                            color = chrome.highlight.copy(alpha = (1f - local) * 0.30f),
                            topLeft = Offset(
                                w * (0.28f - local * 0.08f),
                                h * (0.32f + index * 0.13f),
                            ),
                            size = Size(
                                w * (0.44f + local * 0.18f),
                                h * (0.11f + local * 0.04f),
                            ),
                            style = Stroke(width = 1.2.dp.toPx()),
                        )
                    }
                }
                MuseWorldChromeKind.RoseVelvet -> {
                    repeat(3) { index ->
                        val local = (phase + index * 0.31f) % 1f
                        drawOval(
                            color = chrome.highlight.copy(alpha = 0.16f + (1f - local) * 0.18f),
                            topLeft = Offset(
                                w * (0.47f + index * 0.06f),
                                h * (0.26f + local * 0.22f),
                            ),
                            size = Size(w * 0.10f, h * 0.16f),
                        )
                    }
                }
            }

            val barXs = listOf(0.67f, 0.73f, 0.79f, 0.85f)
            val barHeights = listOf(0.16f, 0.27f, 0.21f, 0.12f)
            val barColors = listOf(
                chrome.primaryStrong,
                chrome.highlight,
                world.specularTint,
                chrome.primary,
            )
            barXs.indices.forEach { index ->
                val bh = h * barHeights[index] *
                    (0.88f + if (index % 2 == 0) phase * 0.20f else (1f - phase) * 0.20f)
                drawLine(
                    color = barColors[index],
                    start = Offset(w * barXs[index], h * 0.58f - bh / 2f),
                    end = Offset(w * barXs[index], h * 0.58f + bh / 2f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round,
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
    val chrome = LocalMuseChromePalette.current
    val world = LocalMuseLivingWorldStyle.current
    val chromeGeometry = world.chromeKind.geometry()
    val materialPhase = if (
        variant == MuseGlassVariant.Standard ||
        variant == MuseGlassVariant.Destructive
    ) {
        0.34f
    } else {
        LocalMuseChromeMotionPhase.current
    }
    fun scaledRadius(scale: Float): Dp =
        (cornerRadius.value * scale).dp
    val shape = RoundedCornerShape(
        topStart = scaledRadius(chromeGeometry.topStartScale),
        topEnd = scaledRadius(chromeGeometry.topEndScale),
        bottomEnd = scaledRadius(chromeGeometry.bottomEndScale),
        bottomStart = scaledRadius(chromeGeometry.bottomStartScale),
    )
    val materialAlpha = when (variant) {
        MuseGlassVariant.Standard -> 0.46f
        MuseGlassVariant.Strong -> 0.70f
        MuseGlassVariant.Elevated -> 0.82f
        MuseGlassVariant.Selected -> 1.00f
        MuseGlassVariant.Destructive -> 0f
    }
    val fill = when (variant) {
        MuseGlassVariant.Standard -> listOf(
            chrome.glassBase.copy(alpha = 0.65f),
            chrome.glassBase.copy(alpha = 0.47f),
            chrome.glassBase.copy(alpha = 0.56f),
        )
        MuseGlassVariant.Strong -> listOf(
            chrome.glassStrong.copy(alpha = 0.76f),
            chrome.glassBase.copy(alpha = 0.68f),
            chrome.glassStrong.copy(alpha = 0.72f),
        )
        MuseGlassVariant.Elevated -> listOf(
            chrome.glassElevated.copy(alpha = 0.77f),
            chrome.glassBase.copy(alpha = 0.62f),
            chrome.glassStrong.copy(alpha = 0.65f),
        )
        MuseGlassVariant.Selected -> listOf(
            chrome.glassSelected.copy(alpha = 0.82f),
            chrome.glassStrong.copy(alpha = 0.71f),
            chrome.glassBase.copy(alpha = 0.72f),
        )
        MuseGlassVariant.Destructive -> listOf(
            Color(0xCB49171A),
            Color(0xA9260B0D),
            Color(0xB818080A),
        )
    }
    val rim = when (variant) {
        MuseGlassVariant.Selected -> listOf(
            chrome.glow.copy(alpha = 0.96f),
            chrome.rim.copy(alpha = 0.72f),
            chrome.primaryStrong.copy(alpha = 0.44f),
        )
        MuseGlassVariant.Destructive -> listOf(
            Color(0xFFFF7F7F).copy(alpha = 0.86f),
            Color(0xFFB94248).copy(alpha = 0.60f),
            Color(0xFFFF6B6B).copy(alpha = 0.30f),
        )
        MuseGlassVariant.Elevated -> listOf(
            chrome.rim.copy(alpha = 0.72f),
            chrome.primary.copy(alpha = 0.52f),
            chrome.glow.copy(alpha = 0.28f),
        )
        else -> listOf(
            chrome.rim.copy(alpha = 0.52f),
            chrome.primary.copy(alpha = 0.46f),
            chrome.primaryStrong.copy(alpha = 0.20f),
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
                    MuseGlassVariant.Selected -> chrome.glow.copy(alpha = 0.24f)
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
                val radiusPx = cornerRadius.toPx()
                val glassCorner = androidx.compose.ui.geometry.CornerRadius(
                    x = radiusPx,
                    y = radiusPx,
                )

                // Broad translucent sheen gives the surface depth without reducing
                // text contrast.
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.17f),
                            Color.White.copy(alpha = 0.038f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.11f),
                        ),
                        endY = size.height,
                    ),
                    size = size,
                    cornerRadius = glassCorner,
                )

                // Directional reflection, similar to light catching curved glass.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.115f),
                            chrome.glow.copy(alpha = 0.040f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * 0.18f,
                            y = size.height * 0.02f,
                        ),
                        radius = size.maxDimension * 0.66f,
                    ),
                    center = Offset(
                        x = size.width * 0.18f,
                        y = size.height * 0.02f,
                    ),
                    radius = size.maxDimension * 0.66f,
                )

                // World-specific material behavior keeps the UI itself from becoming
                // the same glass card with six different tints.
                when (world.chromeKind) {
                    MuseWorldChromeKind.DewGlass -> {
                        repeat(3) { index ->
                            val x = size.width * (0.18f + index * 0.31f)
                            val y = size.height * (0.12f + ((materialPhase + index * 0.27f) % 1f) * 0.18f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.18f * materialAlpha),
                                        chrome.glow.copy(alpha = 0.055f * materialAlpha),
                                        Color.Transparent,
                                    ),
                                    center = Offset(x, y),
                                    radius = 5.5.dp.toPx(),
                                ),
                                center = Offset(x, y),
                                radius = 5.5.dp.toPx(),
                            )
                        }
                    }
                    MuseWorldChromeKind.PrismFacet -> {
                        val travelX = size.width * (-0.28f + materialPhase * 1.45f)
                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.24f * materialAlpha),
                                    chrome.glow.copy(alpha = 0.19f * materialAlpha),
                                    Color.Transparent,
                                ),
                            ),
                            start = Offset(travelX, size.height * 1.10f),
                            end = Offset(travelX + size.width * 0.46f, -size.height * 0.10f),
                            strokeWidth = 4.5.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    MuseWorldChromeKind.ForgedEmber -> {
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    chrome.primaryStrong.copy(
                                        alpha = (0.07f + materialPhase * 0.05f) * materialAlpha,
                                    ),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.52f, size.height * 1.02f),
                                radius = size.width * 0.70f,
                            ),
                            topLeft = Offset(-size.width * 0.08f, size.height * 0.72f),
                            size = Size(size.width * 1.16f, size.height * 0.42f),
                        )
                        repeat(3) { index ->
                            val local = (materialPhase + index * 0.29f) % 1f
                            drawCircle(
                                color = chrome.highlight.copy(
                                    alpha = (1f - local) * 0.16f * materialAlpha,
                                ),
                                center = Offset(
                                    size.width * (0.24f + index * 0.25f),
                                    size.height * (0.90f - local * 0.58f),
                                ),
                                radius = (1.0f + index * 0.35f).dp.toPx(),
                            )
                        }
                    }
                    MuseWorldChromeKind.LunarHalo -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.085f * materialAlpha),
                                    chrome.glow.copy(
                                        alpha = (0.04f + materialPhase * 0.025f) * materialAlpha,
                                    ),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.76f, size.height * 0.02f),
                                radius = size.maxDimension * 0.52f,
                            ),
                            center = Offset(size.width * 0.76f, size.height * 0.02f),
                            radius = size.maxDimension * 0.52f,
                        )
                    }
                    MuseWorldChromeKind.TidalLens -> {
                        repeat(2) { index ->
                            val local = (materialPhase + index * 0.48f) % 1f
                            val ringWidth = size.width * (0.28f + local * 0.26f)
                            val ringHeight = size.height * (0.18f + local * 0.10f)
                            drawOval(
                                color = chrome.highlight.copy(
                                    alpha = (1f - local) * 0.11f * materialAlpha,
                                ),
                                topLeft = Offset(
                                    size.width * 0.50f - ringWidth,
                                    size.height * (0.42f + index * 0.12f) - ringHeight,
                                ),
                                size = Size(ringWidth * 2f, ringHeight * 2f),
                                style = Stroke(width = 0.9.dp.toPx()),
                            )
                        }
                    }
                    MuseWorldChromeKind.RoseVelvet -> {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    chrome.highlight.copy(
                                        alpha = (0.075f + materialPhase * 0.035f) * materialAlpha,
                                    ),
                                    chrome.primary.copy(alpha = 0.028f * materialAlpha),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.86f, size.height * 0.12f),
                                radius = size.maxDimension * 0.52f,
                            ),
                            center = Offset(size.width * 0.86f, size.height * 0.12f),
                            radius = size.maxDimension * 0.52f,
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    chrome.glow.copy(alpha = 0.050f * materialAlpha),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.12f, size.height * 0.88f),
                                radius = size.maxDimension * 0.35f,
                            ),
                            center = Offset(size.width * 0.12f, size.height * 0.88f),
                            radius = size.maxDimension * 0.35f,
                        )
                    }
                }

                // Bright upper rim plus a darker lower internal rim create a
                // thicker pane instead of a flat translucent card.
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.38f),
                            chrome.glow.copy(alpha = 0.26f),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * 0.07f, 1.15.dp.toPx()),
                    end = Offset(size.width * 0.93f, 1.15.dp.toPx()),
                    strokeWidth = 1.05.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.34f),
                            chrome.glassBase.copy(alpha = 0.30f),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * 0.10f, size.height - 1.4.dp.toPx()),
                    end = Offset(size.width * 0.90f, size.height - 1.4.dp.toPx()),
                    strokeWidth = 1.0.dp.toPx(),
                    cap = StrokeCap.Round,
                )

                val inset = 1.6.dp.toPx()
                if (size.width > inset * 2f && size.height > inset * 2f) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent,
                                chrome.glow.copy(alpha = 0.08f),
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height),
                        ),
                        topLeft = Offset(inset, inset),
                        size = Size(
                            width = size.width - inset * 2f,
                            height = size.height - inset * 2f,
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            x = (radiusPx - inset).coerceAtLeast(0f),
                            y = (radiusPx - inset).coerceAtLeast(0f),
                        ),
                        style = Stroke(width = 0.7.dp.toPx()),
                    )
                }
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
    val chrome = LocalMuseChromePalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val activePress = pressed && enabled
    val scale by animateFloatAsState(
        targetValue = if (activePress) 0.952f else 1f,
        animationSpec = tween(durationMillis = if (activePress) 78 else 120),
        label = "MuseGlassPressScale",
    )
    val pressGlow by animateFloatAsState(
        targetValue = if (activePress) 1f else 0f,
        animationSpec = tween(durationMillis = if (activePress) 70 else 145),
        label = "MuseGlassPressGlow",
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
        variant = variant,
        cornerRadius = cornerRadius,
    ) {
        // IMPORTANT: do not fill the parent's unconstrained vertical space here.
        // MuseGlassAction is used inside Scaffold bottom bars. A fillMaxSize()
        // wrapper caused the newly-visible MiniPlayer to measure almost a full
        // screen tall, which pushed the current destination off-screen and
        // produced the "blank botanical screen with player strip at the top"
        // seen in the user's real-device recording.
        Box(
            modifier = Modifier
                .drawBehind {
                    if (pressGlow > 0.001f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f * pressGlow),
                                    chrome.glow.copy(alpha = 0.17f * pressGlow),
                                    Color.Transparent,
                                ),
                                center = Offset(
                                    x = size.width * 0.50f,
                                    y = size.height * 0.46f,
                                ),
                                radius = size.maxDimension * 0.66f,
                            ),
                            center = Offset(
                                x = size.width * 0.50f,
                                y = size.height * 0.46f,
                            ),
                            radius = size.maxDimension * 0.66f,
                        )
                    }
                },
        ) {
            content()
        }
    }
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
