package com.rezoxnemesis.muse.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rezoxnemesis.muse.ui.theme.MuseMuted

/**
 * Motion language distilled from the supplied interaction references:
 * compact elements morph and breathe instead of hard-cutting, but the material
 * remains Muse's emerald botanical glass rather than generic clear glass.
 */
@Composable
internal fun RowScope.MuseMotionNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val chrome = LocalMuseChromePalette.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.025f else 1f,
        animationSpec = tween(
            durationMillis = 180,
            easing = FastOutSlowInEasing,
        ),
        label = "MuseNavItemScale",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) chrome.primary else MuseMuted,
        animationSpec = tween(145),
        label = "MuseNavColor",
    )

    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(66.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(if (selected) 25.dp else 23.dp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun MusePlayingPulse(
    playing: Boolean,
    modifier: Modifier = Modifier,
) {
    val chrome = LocalMuseChromePalette.current
    val transition = rememberInfiniteTransition(label = "MusePlayingPulse")
    val a by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(470, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseA",
    )
    val b by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseB",
    )
    val c by transition.animateFloat(
        initialValue = 0.52f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(540, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseC",
    )

    Canvas(modifier) {
        val values = if (playing) listOf(a, b, c) else listOf(0.32f, 0.32f, 0.32f)
        val barWidth = size.width * 0.17f
        val gap = size.width * 0.105f
        val total = barWidth * 3f + gap * 2f
        val left = (size.width - total) / 2f

        values.forEachIndexed { index, value ->
            val h = size.height * (0.28f + value * 0.62f)
            val x = left + index * (barWidth + gap)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        chrome.highlight,
                        chrome.primary,
                        chrome.primaryStrong,
                    )
                ),
                topLeft = Offset(x, (size.height - h) / 2f),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth, barWidth),
            )
        }
    }
}

@Composable
internal fun MuseMiniProgress(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
) {
    val chrome = LocalMuseChromePalette.current
    val target = if (durationMs > 0L) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "MuseMiniProgress",
    )

    Canvas(modifier) {
        val y = size.height / 2f
        drawLine(
            color = Color.White.copy(alpha = 0.10f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = size.height,
        )
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    chrome.glow.copy(alpha = 0.55f),
                    chrome.primary,
                    chrome.highlight,
                )
            ),
            start = Offset(0f, y),
            end = Offset(size.width * progress, y),
            strokeWidth = size.height,
        )
    }
}


@Composable
internal fun MuseMotionToggle(
    checked: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    val chrome = LocalMuseChromePalette.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 27.dp else 3.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 640f,
        ),
        label = "MuseToggleThumb",
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            chrome.primaryStrong
        } else {
            chrome.toggleOff
        },
        animationSpec = tween(160),
        label = "MuseToggleTrack",
    )
    val halo by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(180),
        label = "MuseToggleHalo",
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(58.dp)
            .height(34.dp)
            .graphicsLayer {
                alpha = if (enabled) 1f else 0.44f
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
            )
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        trackColor.copy(alpha = 0.94f),
                        trackColor.copy(alpha = 0.66f),
                    )
                ),
                shape = RoundedCornerShape(18.dp),
            ),
    ) {
        if (halo > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.10f * halo),
                                chrome.glow.copy(alpha = 0.13f * halo),
                                Color.Transparent,
                            )
                        ),
                        shape = RoundedCornerShape(18.dp),
                    )
            )
        }

        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 3.dp)
                .size(28.dp)
                .shadow(
                    elevation = if (checked) 8.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.55f),
                    spotColor = if (checked) chrome.glow.copy(alpha = 0.42f) else Color.Black,
                )
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            chrome.highlight,
                            chrome.primary,
                            chrome.primaryStrong,
                        )
                    ),
                    shape = CircleShape,
                ),
        )
    }
}


@Composable
internal fun MuseSlidingTabRow(
    labels: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    icons: List<ImageVector?> = emptyList(),
    onSelected: (Int) -> Unit,
) {
    if (labels.isEmpty()) return
    val chrome = LocalMuseChromePalette.current
    val safeIndex = selectedIndex.coerceIn(labels.indices)

    MuseGlassSurface(
        modifier = modifier,
        variant = MuseGlassVariant.Strong,
        cornerRadius = 24.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(3.dp),
        ) {
            val itemWidth = maxWidth / labels.size
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * safeIndex,
                animationSpec = spring(
                    dampingRatio = 0.74f,
                    stiffness = 560f,
                ),
                label = "MuseSegmentIndicator",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                chrome.glow.copy(alpha = 0.12f),
                                chrome.primary.copy(alpha = 0.29f),
                                chrome.highlight.copy(alpha = 0.10f),
                            )
                        ),
                        shape = RoundedCornerShape(21.dp),
                    )
                    .border(
                        0.9.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                chrome.primary.copy(alpha = 0.72f),
                                Color.White.copy(alpha = 0.08f),
                            )
                        ),
                        shape = RoundedCornerShape(21.dp),
                    ),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                labels.forEachIndexed { index, label ->
                    val selected = index == safeIndex
                    val color by animateColorAsState(
                        targetValue = if (selected) chrome.primary else Color.White.copy(alpha = 0.82f),
                        animationSpec = tween(135),
                        label = "MuseSegmentText",
                    )
                    val lift by animateDpAsState(
                        targetValue = if (selected) (-1).dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = 0.80f,
                            stiffness = 620f,
                        ),
                        label = "MuseSegmentLift",
                    )
                    val interaction = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .offset(y = lift)
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = { onSelected(index) },
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        icons.getOrNull(index)?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(5.dp))
                        }
                        Text(
                            text = label,
                            color = color,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
