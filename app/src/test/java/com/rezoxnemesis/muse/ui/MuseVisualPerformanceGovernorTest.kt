package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseVisualPerformanceGovernorTest {
    @Test
    fun sustainedFramePressureDegradesSecondaryWorkGraduallyButProtectsHeroExperience() {
        var state = initialMuseVisualPerformanceGovernor()

        repeat(18) {
            state = updateMuseVisualPerformanceGovernor(
                state = state,
                frameDurationMillis = 31f,
                isForeground = true,
                expensiveLayersVisible = true,
            )
        }

        val budget = state.qualityBudget()
        assertTrue(budget.secondaryParticleScale < 1f)
        assertTrue(budget.bokehScale < 1f)
        assertTrue(budget.microDetailScale < 1f)
        assertTrue(budget.secondaryShimmerScale < 1f)
        assertTrue(budget.secondaryParticleScale >= 0.30f)
        assertTrue(budget.bokehScale >= 0.30f)

        assertTrue(budget.heroLightingEnabled)
        assertTrue(budget.heroGeometryEnabled)
        assertTrue(budget.interactionEnabled)
        assertTrue(budget.morphEnabled)
    }

    @Test
    fun governorUsesHysteresisSoOneGoodFrameCannotInstantlyRestoreQuality() {
        var state = initialMuseVisualPerformanceGovernor()

        repeat(24) {
            state = updateMuseVisualPerformanceGovernor(
                state = state,
                frameDurationMillis = 29f,
                isForeground = true,
                expensiveLayersVisible = true,
            )
        }
        val degraded = state.qualityBudget()

        state = updateMuseVisualPerformanceGovernor(
            state = state,
            frameDurationMillis = 11f,
            isForeground = true,
            expensiveLayersVisible = true,
        )
        val afterOneGoodFrame = state.qualityBudget()

        assertTrue(afterOneGoodFrame.secondaryParticleScale <= degraded.secondaryParticleScale + 0.05f)

        repeat(40) {
            state = updateMuseVisualPerformanceGovernor(
                state = state,
                frameDurationMillis = 13f,
                isForeground = true,
                expensiveLayersVisible = true,
            )
        }
        val recovered = state.qualityBudget()

        assertTrue(recovered.secondaryParticleScale > degraded.secondaryParticleScale)
        assertTrue(recovered.bokehScale > degraded.bokehScale)
        assertTrue(recovered.secondaryParticleScale <= 1f)
        assertTrue(recovered.bokehScale <= 1f)
    }

    @Test
    fun nearThresholdFramesDoNotCauseQualityFlapping() {
        var state = initialMuseVisualPerformanceGovernor()

        repeat(60) { index ->
            state = updateMuseVisualPerformanceGovernor(
                state = state,
                frameDurationMillis = if (index % 2 == 0) 18.5f else 20.5f,
                isForeground = true,
                expensiveLayersVisible = true,
            )
        }

        val budget = state.qualityBudget()
        assertTrue(budget.secondaryParticleScale >= 0.85f)
        assertTrue(budget.bokehScale >= 0.85f)
    }

    @Test
    fun inactiveOrInvisibleVisualsReduceExpendableWorkWithoutDisablingHeroContracts() {
        var state = initialMuseVisualPerformanceGovernor()

        repeat(4) {
            state = updateMuseVisualPerformanceGovernor(
                state = state,
                frameDurationMillis = 16f,
                isForeground = false,
                expensiveLayersVisible = false,
            )
        }

        val budget = state.qualityBudget()
        assertTrue(budget.secondaryParticleScale <= 0.40f)
        assertTrue(budget.bokehScale <= 0.40f)
        assertTrue(budget.microDetailScale <= 0.50f)
        assertEquals(true, budget.heroLightingEnabled)
        assertEquals(true, budget.heroGeometryEnabled)
        assertEquals(true, budget.interactionEnabled)
        assertEquals(true, budget.morphEnabled)
    }
}
