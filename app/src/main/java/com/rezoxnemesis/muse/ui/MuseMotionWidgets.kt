package com.rezoxnemesis.muse.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rezoxnemesis.muse.ui.theme.MuseGlow
import com.rezoxnemesis.muse.ui.theme.MuseGreen
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
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.055f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 520f,
        ),
        label = "MuseNavItemScale",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(170, easing = FastOutSlowInEasing),
        label = "MuseNavGlow",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MuseGreen else MuseMuted,
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
        if (glowAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MuseGlow.copy(alpha = 0.08f * glowAlpha),
                                MuseGreen.copy(alpha = 0.25f * glowAlpha),
                                MuseGlow.copy(alpha = 0.08f * glowAlpha),
                            )
                        ),
                        shape = RoundedCornerShape(24.dp),
                    )
            )
        }

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
                        Color(0xFFD8FF9F),
                        MuseGreen,
                        Color(0xFF26B444),
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
                    MuseGlow.copy(alpha = 0.55f),
                    MuseGreen,
                    Color(0xFFD5FF8C),
                )
            ),
            start = Offset(0f, y),
            end = Offset(size.width * progress, y),
            strokeWidth = size.height,
        )
    }
}
