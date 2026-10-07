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
    @Test
    fun nearbyLeafDropletsCoalesceIntoOneHeavierBead() {
        val merged = resolveMuseDropletCoalescence(
            progressA = 0.48f,
            lateralA = 0.10f,
            radiusA = 1.4f,
            progressB = 0.52f,
            lateralB = 0.13f,
            radiusB = 1.1f,
            adhesion = 0.92f,
        )
        val separate = resolveMuseDropletCoalescence(
            progressA = 0.18f,
            lateralA = -0.40f,
            radiusA = 1.4f,
            progressB = 0.78f,
            lateralB = 0.42f,
            radiusB = 1.1f,
            adhesion = 0.92f,
        )

        assertTrue(merged.shouldMerge)
        assertTrue(merged.radiusScale > 1.4f)
        assertTrue(merged.progress in 0.48f..0.52f)
        assertTrue(merged.lateral in 0.10f..0.13f)
        assertTrue(merged.releaseBoost > 0f)

        assertTrue(!separate.shouldMerge)
        assertEquals(1.4f, separate.radiusScale, 0.0001f)
        assertEquals(0f, separate.releaseBoost, 0.0001f)
    }

}
