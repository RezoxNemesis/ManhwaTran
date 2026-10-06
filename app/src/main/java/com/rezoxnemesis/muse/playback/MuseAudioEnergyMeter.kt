package com.rezoxnemesis.muse.playback

import kotlin.math.sqrt

/**
 * Lightweight envelope follower for Muse's visual system.
 *
 * The PCM processor feeds mean absolute sample energy into this class. A faster
 * attack lets the atmosphere notice beats quickly, while a slower release keeps
 * leaves, mist and particles from flickering between audio buffers.
 */
internal class MuseAudioEnergyMeter(
    private val attack: Float = 0.34f,
    private val release: Float = 0.10f,
) {
    @Volatile
    private var smoothedEnergy = 0f

    fun observeBlock(meanAbsolute: Float): Float {
        val normalized = meanAbsolute.coerceIn(0f, 1f)
        val perceptual = sqrt(normalized)
        val target = ((perceptual - NoiseFloor) / UsableRange)
            .coerceIn(0f, 1f)
        val coefficient = if (target > smoothedEnergy) attack else release

        smoothedEnergy += (target - smoothedEnergy) * coefficient
        if (target == 0f && smoothedEnergy < SnapToSilence) {
            smoothedEnergy = 0f
        }
        return smoothedEnergy
    }

    fun value(): Float = smoothedEnergy.coerceIn(0f, 1f)

    fun reset() {
        smoothedEnergy = 0f
    }

    private companion object {
        const val NoiseFloor = 0.035f
        const val UsableRange = 0.78f
        const val SnapToSilence = 0.003f
    }
}
