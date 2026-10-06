package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseAtmospherePhysicsTest {
    @Test
    fun leafWaterMovesFasterAndFallsFurtherInHeavyRain() {
        val mist = resolveMuseLeafWaterMotion(
            seed = 2,
            phase = 0.62f,
            mobility = 0.36f,
            rainLevel = MuseRainLevel.Mist,
        )
        val downpour = resolveMuseLeafWaterMotion(
            seed = 2,
            phase = 0.62f,
            mobility = 1.00f,
            rainLevel = MuseRainLevel.Downpour,
        )

        assertTrue(downpour.surfaceTravel > mist.surfaceTravel)
        assertTrue(downpour.edgeFall >= mist.edgeFall)
        assertTrue(downpour.impactPulse in 0f..1f)
        assertTrue(downpour.surfaceProgress in 0f..1f)
    }

    @Test
    fun atmosphereDepthLayersHaveThreeDistinctParallaxSpeeds() {
        val speeds = museAtmosphereDepthSpeeds(depthParallax = 0.92f)

        assertEquals(3, speeds.size)
        assertEquals(3, speeds.distinct().size)
        assertTrue(speeds[0] < speeds[1])
        assertTrue(speeds[1] < speeds[2])
        assertTrue(speeds.all { it > 0f })
    }
}
