package com.rezoxnemesis.muse.playback

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

internal data class MuseAudioSpectrum(
    val energy: Float = 0f,
    val bass: Float = 0f,
    val mid: Float = 0f,
    val high: Float = 0f,
    val transient: Float = 0f,
)

/**
 * Lightweight three-band analyser for visual reactions.
 *
 * This deliberately avoids an FFT on ExoPlayer's real-time audio thread.
 * Two one-pole low-pass states split each PCM sample into bass, mid and high
 * components, while a block peak delta produces a transient/beat impulse.
 */
internal class MuseAudioSpectrumMeter(
    sampleRateHz: Int = 48_000,
) {
    private var sampleRate = sampleRateHz.coerceAtLeast(8_000)
    private var bassAlpha = alphaFor(220f)
    private var midAlpha = alphaFor(2_800f)

    private var bassState = 0f
    private var bassAndMidState = 0f

    private var sampleCount = 0
    private var energySum = 0f
    private var bassSum = 0f
    private var midSum = 0f
    private var highSum = 0f
    private var peak = 0f

    private var lastRawEnergy = 0f
    private var lastPeak = 0f
    private var spectrum = MuseAudioSpectrum()

    fun updateSampleRate(sampleRateHz: Int) {
        sampleRate = sampleRateHz.coerceAtLeast(8_000)
        bassAlpha = alphaFor(220f)
        midAlpha = alphaFor(2_800f)
    }

    fun beginBlock() {
        sampleCount = 0
        energySum = 0f
        bassSum = 0f
        midSum = 0f
        highSum = 0f
        peak = 0f
    }

    fun observeSample(sample: Float) {
        val value = sample.coerceIn(-1f, 1f)
        bassState += bassAlpha * (value - bassState)
        bassAndMidState += midAlpha * (value - bassAndMidState)

        val bass = bassState
        val mid = bassAndMidState - bassState
        val high = value - bassAndMidState

        energySum += abs(value)
        bassSum += abs(bass)
        midSum += abs(mid)
        highSum += abs(high)
        peak = maxOf(peak, abs(value))
        sampleCount += 1
    }

    fun endBlock(): MuseAudioSpectrum {
        if (sampleCount == 0) return spectrum

        val divisor = sampleCount.toFloat()
        val rawEnergy = (energySum / divisor).coerceIn(0f, 1f)
        val energyTarget = normalize(rawEnergy, 1.75f)
        val bassTarget = normalize(bassSum / divisor, 2.20f)
        val midTarget = normalize(midSum / divisor, 2.35f)
        val highTarget = normalize(highSum / divisor, 2.05f)

        val peakJump = (peak - lastPeak).coerceAtLeast(0f)
        val energyFlux = (rawEnergy - lastRawEnergy).coerceAtLeast(0f)
        val transientTarget = (peakJump * 2.4f + energyFlux * 1.15f)
            .coerceIn(0f, 1f)

        spectrum = MuseAudioSpectrum(
            energy = smooth(spectrum.energy, energyTarget, attack = 0.34f, release = 0.10f),
            bass = smooth(spectrum.bass, bassTarget, attack = 0.42f, release = 0.12f),
            mid = smooth(spectrum.mid, midTarget, attack = 0.38f, release = 0.11f),
            high = smooth(spectrum.high, highTarget, attack = 0.46f, release = 0.14f),
            transient = smooth(
                spectrum.transient,
                transientTarget,
                attack = 0.62f,
                release = 0.22f,
            ),
        )
        lastRawEnergy = rawEnergy
        lastPeak = peak
        return spectrum
    }

    fun observeMono(samples: FloatArray): MuseAudioSpectrum {
        beginBlock()
        samples.forEach(::observeSample)
        return endBlock()
    }

    fun value(): MuseAudioSpectrum = spectrum

    fun reset() {
        bassState = 0f
        bassAndMidState = 0f
        lastRawEnergy = 0f
        lastPeak = 0f
        spectrum = MuseAudioSpectrum()
        beginBlock()
    }

    private fun alphaFor(cutoffHz: Float): Float =
        (1.0 - exp(-2.0 * PI * cutoffHz / sampleRate.toDouble()))
            .toFloat()
            .coerceIn(0.0001f, 1f)

    private fun normalize(value: Float, gain: Float): Float =
        sqrt((value * gain).coerceIn(0f, 1f))

    private fun smooth(
        current: Float,
        target: Float,
        attack: Float,
        release: Float,
    ): Float {
        val coefficient = if (target > current) attack else release
        return (current + (target - current) * coefficient).coerceIn(0f, 1f)
    }
}
