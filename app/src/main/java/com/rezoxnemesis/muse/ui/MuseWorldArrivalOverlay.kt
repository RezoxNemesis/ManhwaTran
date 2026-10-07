package com.rezoxnemesis.muse.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A short profile-arrival gesture. It deliberately stays translucent so the
 * native UI remains readable while the atmosphere itself visibly transforms.
 */
@Composable
internal fun MuseWorldArrivalOverlay(
    profile: MuseVisualProfile,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(1f) }
    val signature = profile.transitionSignature()
    val style = profile.livingWorldStyle()
    val chrome = profile.chromePalette()

    LaunchedEffect(profile) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = signature.durationMillis,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Canvas(modifier) {
        val p = progress.value.coerceIn(0f, 1f)
        if (p >= 0.999f) return@Canvas

        val envelope = sin(PI.toFloat() * p).coerceAtLeast(0f)
        val fade = (1f - p).coerceIn(0f, 1f)

        if (signature.sweep > 0.10f) {
            val travelX = size.width * (-0.34f + p * 1.68f)
            repeat(3) { lane ->
                val laneOffset = size.width * (lane - 1) * 0.13f
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            style.specularTint.copy(
                                alpha = 0.16f * signature.sweep * envelope,
                            ),
                            chrome.glow.copy(
                                alpha = 0.10f * signature.sweep * envelope,
                            ),
                            Color.Transparent,
                        ),
                        start = Offset(travelX + laneOffset, size.height),
                        end = Offset(
                            travelX + laneOffset + size.width * 0.34f,
                            0f,
                        ),
                    ),
                    start = Offset(
                        travelX + laneOffset,
                        size.height * 1.10f,
                    ),
                    end = Offset(
                        travelX + laneOffset + size.width * 0.34f,
                        -size.height * 0.10f,
                    ),
                    strokeWidth = (15f + lane * 8f).dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        if (signature.ignition > 0.10f) {
            val floor = Offset(size.width * 0.52f, size.height * 1.02f)
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        style.leafHighlight.copy(
                            alpha = 0.20f * signature.ignition * envelope,
                        ),
                        style.leafPrimary.copy(
                            alpha = 0.10f * signature.ignition * envelope,
                        ),
                        Color.Transparent,
                    ),
                    center = floor,
                    radius = size.width * (0.34f + p * 0.42f),
                ),
                topLeft = Offset(
                    -size.width * 0.10f,
                    size.height * (0.70f - p * 0.12f),
                ),
                size = Size(size.width * 1.20f, size.height * 0.42f),
            )
            repeat(10) { index ->
                val local = (p * (1.0f + index % 3 * 0.18f) + index * 0.073f) % 1f
                val x = size.width * (((index * 37 + 13) % 97) / 96f)
                val y = size.height * (1.02f - local * 0.62f)
                drawCircle(
                    color = style.specularTint.copy(
                        alpha = (1f - local) *
                            0.22f *
                            signature.ignition *
                            envelope,
                    ),
                    center = Offset(x, y),
                    radius = (1.0f + index % 3 * 0.55f).dp.toPx(),
                )
            }
        }

        if (signature.mistBloom > 0.10f) {
            val mistCenter = Offset(
                size.width * 0.52f,
                size.height * (0.40f + p * 0.06f),
            )
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(
                            alpha = 0.075f * signature.mistBloom * envelope,
                        ),
                        style.specularTint.copy(
                            alpha = 0.055f * signature.mistBloom * envelope,
                        ),
                        Color.Transparent,
                    ),
                    center = mistCenter,
                    radius = size.width * (0.36f + p * 0.48f),
                ),
                topLeft = Offset(
                    -size.width * (0.05f + p * 0.10f),
                    mistCenter.y - size.height * (0.12f + p * 0.05f),
                ),
                size = Size(
                    size.width * (1.10f + p * 0.20f),
                    size.height * (0.24f + p * 0.10f),
                ),
            )
        }

        if (signature.refraction > 0.10f) {
            repeat(4) { ring ->
                val local = (p + ring * 0.13f).coerceIn(0f, 1f)
                val width = size.width * (0.10f + local * 0.56f)
                val height = size.height * (0.025f + local * 0.10f)
                drawOval(
                    color = style.specularTint.copy(
                        alpha = (1f - local) *
                            0.18f *
                            signature.refraction *
                            envelope,
                    ),
                    topLeft = Offset(
                        size.width * 0.50f - width,
                        size.height * 0.48f - height,
                    ),
                    size = Size(width * 2f, height * 2f),
                    style = Stroke(
                        width = (0.9f + ring * 0.18f).dp.toPx(),
                    ),
                )
            }
        }

        if (signature.petalBurst > 0.10f) {
            val origin = Offset(size.width * 0.54f, size.height * 0.45f)
            repeat(14) { index ->
                val angle = index * (2f * PI.toFloat() / 14f) + p * 0.78f
                val radius = size.minDimension * (
                    0.04f + p * (0.18f + index % 4 * 0.022f)
                )
                val center = Offset(
                    origin.x + cos(angle) * radius,
                    origin.y + sin(angle) * radius,
                )
                val petalW = (2.0f + index % 4 * 0.70f).dp.toPx()
                val petalH = petalW * 1.65f
                rotate(
                    degrees = index * 31f + p * 82f,
                    pivot = center,
                ) {
                    drawOval(
                        brush = Brush.linearGradient(
                            listOf(
                                style.specularTint.copy(
                                    alpha = 0.18f *
                                        signature.petalBurst *
                                        fade,
                                ),
                                style.leafPrimary.copy(
                                    alpha = 0.14f *
                                        signature.petalBurst *
                                        fade,
                                ),
                            ),
                        ),
                        topLeft = Offset(
                            center.x - petalW,
                            center.y - petalH,
                        ),
                        size = Size(petalW * 2f, petalH * 2f),
                    )
                }
            }
        }

        if (signature.dewPulse > 0.10f) {
            repeat(8) { index ->
                val seedX = ((index * 31 + 17) % 89) / 88f
                val seedY = ((index * 47 + 11) % 83) / 82f
                val center = Offset(
                    size.width * seedX,
                    size.height * seedY,
                )
                val radius = size.minDimension * (
                    0.006f + p * 0.020f + index % 3 * 0.003f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(
                                alpha = 0.22f *
                                    signature.dewPulse *
                                    fade,
                            ),
                            style.specularTint.copy(
                                alpha = 0.09f *
                                    signature.dewPulse *
                                    fade,
                            ),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    center = center,
                    radius = radius,
                )
            }
        }
    }
}
