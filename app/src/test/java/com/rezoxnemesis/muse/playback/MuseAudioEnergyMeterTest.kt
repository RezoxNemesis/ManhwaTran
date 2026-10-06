package com.rezoxnemesis.muse.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseAudioEnergyMeterTest {
    @Test
    fun silenceStaysAtZeroAndMusicEnergyIsNormalized() {
        val meter = MuseAudioEnergyMeter()

        repeat(4) { meter.observeBlock(0f) }
        assertEquals(0f, meter.value(), 0.0001f)

        repeat(5) { meter.observeBlock(0.36f) }
        assertTrue(meter.value() in 0.35f..1f)

        repeat(8) { meter.observeBlock(1f) }
        assertTrue(meter.value() in 0.75f..1f)
    }

    @Test
    fun releaseIsSmootherThanAttackAndResetClearsState() {
        val meter = MuseAudioEnergyMeter()

        val firstAttack = meter.observeBlock(0.8f)
        val secondAttack = meter.observeBlock(0.8f)
        assertTrue(secondAttack > firstAttack)

        val firstRelease = meter.observeBlock(0f)
        assertTrue(firstRelease > 0f)
        assertTrue(firstRelease < secondAttack)

        meter.reset()
        assertEquals(0f, meter.value(), 0.0001f)
    }
}
