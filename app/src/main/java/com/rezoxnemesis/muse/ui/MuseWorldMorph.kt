package com.rezoxnemesis.muse.ui

/**
 * A normalized six-world mixture used as the visual source of a V4 morph.
 *
 * A blend can become the source of another morph, which is what lets rapid
 * world switching retarget from what is actually visible instead of queuing
 * stale world destinations.
 */
internal data class MuseWorldBlend(
    val verdantRain: Float,
    val auroraGlass: Float,
    val midnightEmber: Float,
    val moonlitViolet: Float,
    val oceanPulse: Float,
    val roseNoir: Float,
) {
    fun weightOf(profile: MuseVisualProfile): Float =
        when (profile) {
            MuseVisualProfile.VerdantRain -> verdantRain
            MuseVisualProfile.AuroraGlass -> auroraGlass
            MuseVisualProfile.MidnightEmber -> midnightEmber
            MuseVisualProfile.MoonlitViolet -> moonlitViolet
            MuseVisualProfile.OceanPulse -> oceanPulse
            MuseVisualProfile.RoseNoir -> roseNoir
        }

    fun totalWeight(): Float =
        verdantRain +
            auroraGlass +
            midnightEmber +
            moonlitViolet +
            oceanPulse +
            roseNoir

    companion object {
        fun single(profile: MuseVisualProfile): MuseWorldBlend =
            MuseWorldBlend(
                verdantRain = if (profile == MuseVisualProfile.VerdantRain) 1f else 0f,
                auroraGlass = if (profile == MuseVisualProfile.AuroraGlass) 1f else 0f,
                midnightEmber = if (profile == MuseVisualProfile.MidnightEmber) 1f else 0f,
                moonlitViolet = if (profile == MuseVisualProfile.MoonlitViolet) 1f else 0f,
                oceanPulse = if (profile == MuseVisualProfile.OceanPulse) 1f else 0f,
                roseNoir = if (profile == MuseVisualProfile.RoseNoir) 1f else 0f,
            )
    }
}

internal data class MuseWorldMorphState(
    val source: MuseWorldBlend,
    val target: MuseVisualProfile,
    val durationMillis: Int,
)

internal fun settledMuseWorldMorph(
    profile: MuseVisualProfile,
): MuseWorldMorphState =
    MuseWorldMorphState(
        source = MuseWorldBlend.single(profile),
        target = profile,
        durationMillis = 0,
    )

internal fun retargetMuseWorldMorph(
    state: MuseWorldMorphState,
    target: MuseVisualProfile,
    currentProgress: Float,
): MuseWorldMorphState =
    MuseWorldMorphState(
        source = sampleMuseWorldMorph(state, currentProgress),
        target = target,
        durationMillis = target.transitionSignature().durationMillis,
    )

internal fun sampleMuseWorldMorph(
    state: MuseWorldMorphState,
    progress: Float,
): MuseWorldBlend {
    val amount = progress.coerceIn(0f, 1f)
    val retained = 1f - amount
    val target = MuseWorldBlend.single(state.target)

    return MuseWorldBlend(
        verdantRain = state.source.verdantRain * retained + target.verdantRain * amount,
        auroraGlass = state.source.auroraGlass * retained + target.auroraGlass * amount,
        midnightEmber = state.source.midnightEmber * retained + target.midnightEmber * amount,
        moonlitViolet = state.source.moonlitViolet * retained + target.moonlitViolet * amount,
        oceanPulse = state.source.oceanPulse * retained + target.oceanPulse * amount,
        roseNoir = state.source.roseNoir * retained + target.roseNoir * amount,
    )
}
