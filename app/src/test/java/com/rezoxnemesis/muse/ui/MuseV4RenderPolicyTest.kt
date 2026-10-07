package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseV4RenderPolicyTest {
    @Test
    fun waterRenderingMatchesEachWorldInsteadOfLeakingRainforestDropletsEverywhere() {
        val fullBudget = initialMuseVisualPerformanceGovernor().qualityBudget()

        val verdant = resolveMuseV4RenderPolicy(
            MuseVisualProfile.VerdantRain.worldExperience(),
            fullBudget,
            isForeground = true,
        )
        val aurora = resolveMuseV4RenderPolicy(
            MuseVisualProfile.AuroraGlass.worldExperience(),
            fullBudget,
            isForeground = true,
        )
        val ember = resolveMuseV4RenderPolicy(
            MuseVisualProfile.MidnightEmber.worldExperience(),
            fullBudget,
            isForeground = true,
        )
        val moon = resolveMuseV4RenderPolicy(
            MuseVisualProfile.MoonlitViolet.worldExperience(),
            fullBudget,
            isForeground = true,
        )
        val ocean = resolveMuseV4RenderPolicy(
            MuseVisualProfile.OceanPulse.worldExperience(),
            fullBudget,
            isForeground = true,
        )
        val rose = resolveMuseV4RenderPolicy(
            MuseVisualProfile.RoseNoir.worldExperience(),
            fullBudget,
            isForeground = true,
        )

        assertTrue(verdant.physicalDropletScale >= 0.90f)
        assertTrue(verdant.waterRefractionScale > 0f)

        assertEquals(0f, ember.physicalDropletScale, 0.0001f)
        assertEquals(0f, ember.waterRefractionScale, 0.0001f)

        assertEquals(0f, moon.physicalDropletScale, 0.0001f)
        assertTrue(moon.condensationHazeScale > 0f)

        assertEquals(0f, ocean.physicalDropletScale, 0.0001f)
        assertTrue(ocean.waterRefractionScale > aurora.waterRefractionScale)

        assertTrue(rose.physicalDropletScale in 0.05f..0.30f)
        assertTrue(aurora.waterRefractionScale > rose.waterRefractionScale)
    }

    @Test
    fun lowHeadroomReducesOnlySecondaryDensityAndNeverDisablesHeroWorldContracts() {
        val full = initialMuseVisualPerformanceGovernor().qualityBudget()

        var reducedState = initialMuseVisualPerformanceGovernor()
        repeat(4) {
            reducedState = updateMuseVisualPerformanceGovernor(
                state = reducedState,
                frameDurationMillis = 16f,
                isForeground = false,
                expensiveLayersVisible = false,
            )
        }
        val reduced = reducedState.qualityBudget()

        val world = MuseVisualProfile.OceanPulse.worldExperience()
        val fullPolicy = resolveMuseV4RenderPolicy(world, full, isForeground = true)
        val reducedPolicy = resolveMuseV4RenderPolicy(world, reduced, isForeground = false)

        assertTrue(reducedPolicy.secondaryParticleScale < fullPolicy.secondaryParticleScale)
        assertTrue(reducedPolicy.bokehScale < fullPolicy.bokehScale)
        assertTrue(reducedPolicy.microDetailScale < fullPolicy.microDetailScale)

        assertTrue(reducedPolicy.heroLightingEnabled)
        assertTrue(reducedPolicy.heroGeometryEnabled)
        assertTrue(reducedPolicy.interactionEnabled)
        assertTrue(reducedPolicy.morphEnabled)
    }

    @Test
    fun everyWorldCarriesItsOwnInteractionSignatureIntoTheRenderer() {
        val budget = initialMuseVisualPerformanceGovernor().qualityBudget()
        val policies = MuseVisualProfile.entries.map { profile ->
            resolveMuseV4RenderPolicy(
                experience = profile.worldExperience(),
                budget = budget,
                isForeground = true,
            )
        }

        assertEquals(
            MuseVisualProfile.entries.size,
            policies.map {
                Triple(
                    it.waterLanguage,
                    it.echoKind,
                    it.memoryKind,
                )
            }.distinct().size,
        )
    }
}
