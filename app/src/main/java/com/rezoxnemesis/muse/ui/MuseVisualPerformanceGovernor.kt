package com.rezoxnemesis.muse.ui

internal data class MuseVisualQualityBudget(
    val secondaryParticleScale: Float,
    val bokehScale: Float,
    val microDetailScale: Float,
    val secondaryShimmerScale: Float,
    val heroLightingEnabled: Boolean = true,
    val heroGeometryEnabled: Boolean = true,
    val interactionEnabled: Boolean = true,
    val morphEnabled: Boolean = true,
)

internal data class MuseVisualPerformanceGovernorState(
    val pressure: Float,
    val quality: Float,
)

internal fun initialMuseVisualPerformanceGovernor(): MuseVisualPerformanceGovernorState =
    MuseVisualPerformanceGovernorState(
        pressure = 0f,
        quality = 1f,
    )

internal fun updateMuseVisualPerformanceGovernor(
    state: MuseVisualPerformanceGovernorState,
    frameDurationMillis: Float,
    isForeground: Boolean,
    expensiveLayersVisible: Boolean,
): MuseVisualPerformanceGovernorState {
    if (!isForeground || !expensiveLayersVisible) {
        return state.copy(
            pressure = state.pressure.coerceAtLeast(0.80f),
            quality = state.quality.coerceAtMost(0.10f),
        )
    }

    val frame = frameDurationMillis.coerceAtLeast(0f)
    return when {
        frame >= 24f -> {
            val nextPressure = (state.pressure + 0.10f).coerceIn(0f, 1f)
            val nextQuality = if (nextPressure >= 0.35f) {
                (state.quality - 0.045f).coerceAtLeast(0.38f)
            } else {
                state.quality
            }
            MuseVisualPerformanceGovernorState(
                pressure = nextPressure,
                quality = nextQuality,
            )
        }

        frame <= 16f -> {
            val nextPressure = (state.pressure - 0.065f).coerceIn(0f, 1f)
            val nextQuality = if (nextPressure <= 0.25f) {
                (state.quality + 0.025f).coerceAtMost(1f)
            } else {
                state.quality
            }
            MuseVisualPerformanceGovernorState(
                pressure = nextPressure,
                quality = nextQuality,
            )
        }

        else -> {
            MuseVisualPerformanceGovernorState(
                pressure = (state.pressure * 0.985f).coerceIn(0f, 1f),
                quality = state.quality.coerceIn(0.38f, 1f),
            )
        }
    }
}

internal fun MuseVisualPerformanceGovernorState.qualityBudget(): MuseVisualQualityBudget {
    val normalized = quality.coerceIn(0f, 1f)
    return MuseVisualQualityBudget(
        secondaryParticleScale = 0.30f + normalized * 0.70f,
        bokehScale = 0.30f + normalized * 0.70f,
        microDetailScale = 0.34f + normalized * 0.66f,
        secondaryShimmerScale = 0.32f + normalized * 0.68f,
        heroLightingEnabled = true,
        heroGeometryEnabled = true,
        interactionEnabled = true,
        morphEnabled = true,
    )
}
