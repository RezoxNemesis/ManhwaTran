package com.rezoxnemesis.muse.playback

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class MuseAudioSpectrumMeterTest {
    @Test
    fun lowToneProducesMoreBassThanHighEnergy() {
        val meter = MuseAudioSpectrumMeter(sampleRateHz = 48_000)
        val samples = sineWave(frequencyHz = 90f, sampleRate = 48_000, count = 2048)

        repeat(4) { meter.observeMono(samples) }
        val spectrum = meter.value()

        assertTrue(spectrum.bass > spectrum.mid)
        assertTrue(spectrum.bass > spectrum.high)
        assertTrue(spectrum.energy > 0f)
    }

    @Test
    fun highToneProducesMoreHighThanBassAndTransientSpikes() {
        val meter = MuseAudioSpectrumMeter(sampleRateHz = 48_000)
        val high = sineWave(frequencyHz = 6_000f, sampleRate = 48_000, count = 2048)

        repeat(4) { meter.observeMono(high) }
        val steady = meter.value()
        assertTrue(steady.high > steady.bass)

        val impulse = FloatArray(2048)
        impulse[0] = 1f
        meter.observeMono(impulse)
        val afterImpulse = meter.value()
        assertTrue(afterImpulse.transient > steady.transient)
    }

    private fun sineWave(
        frequencyHz: Float,
        sampleRate: Int,
        count: Int,
    ): FloatArray = FloatArray(count) { index ->
        (sin(2.0 * PI * frequencyHz * index / sampleRate) * 0.72).toFloat()
    }
}
