package com.rezoxnemesis.muse.playback

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class SoundProfileBandMapperTest {
    @Test
    fun keepsMatchingBandsAndClampsLevels() {
        val mapped = SoundProfileBandMapper.mapLevels(
            savedCentersHz = intArrayOf(60, 1_000, 14_000),
            savedLevelsMb = intArrayOf(-2_000, 250, 2_000),
            targetCentersHz = intArrayOf(60, 1_000, 14_000),
            minimumLevelMb = -1_500,
            maximumLevelMb = 1_500,
        )

        assertArrayEquals(
            intArrayOf(-1_500, 250, 1_500),
            mapped,
        )
    }

    @Test
    fun mapsDifferentDeviceBandsByNearestLogFrequency() {
        val mapped = SoundProfileBandMapper.mapLevels(
            savedCentersHz = intArrayOf(100, 1_000, 10_000),
            savedLevelsMb = intArrayOf(-500, 100, 700),
            targetCentersHz = intArrayOf(80, 800, 8_000),
            minimumLevelMb = -1_500,
            maximumLevelMb = 1_500,
        )

        assertArrayEquals(
            intArrayOf(-500, 100, 700),
            mapped,
        )
    }

    @Test
    fun invalidSavedProfileFallsBackToNeutralLevels() {
        val mapped = SoundProfileBandMapper.mapLevels(
            savedCentersHz = intArrayOf(100, 1_000),
            savedLevelsMb = intArrayOf(300),
            targetCentersHz = intArrayOf(80, 800, 8_000),
            minimumLevelMb = -1_500,
            maximumLevelMb = 1_500,
        )

        assertArrayEquals(
            intArrayOf(0, 0, 0),
            mapped,
        )
    }
}
