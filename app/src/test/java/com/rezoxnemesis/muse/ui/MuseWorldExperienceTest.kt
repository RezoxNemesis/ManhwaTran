package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseWorldExperienceTest {
    @Test
    fun everyProfileOwnsOneCentralV4WorldExperience() {
        val experiences = MuseVisualProfile.entries.map { it.worldExperience() }

        assertEquals(MuseVisualProfile.entries.size, experiences.size)
        assertEquals(
            MuseVisualProfile.entries.size,
            experiences.map {
                listOf(
                    it.style.materialKind,
                    it.style.motionKind,
                    it.style.foliageKind,
                    it.style.chromeKind,
                    it.waterLanguage,
                )
            }.distinct().size,
        )

        MuseVisualProfile.entries.forEachIndexed { index, profile ->
            val experience = experiences[index]
            assertEquals(profile, experience.profile)
            assertEquals(profile.livingWorldStyle(), experience.style)
            assertEquals(profile.worldSceneGeometry(), experience.sceneGeometry)
            assertEquals(profile.worldLightField(), experience.lightField)
            assertEquals(profile.transitionSignature(), experience.transition)
            assertEquals(profile.atmosphereBehavior(), experience.atmosphere)
        }
    }

    @Test
    fun waterLanguageKeepsEachWorldPhysicallyDistinct() {
        assertEquals(
            MuseWaterLanguage.PhysicalDroplets,
            MuseVisualProfile.VerdantRain.worldExperience().waterLanguage,
        )
        assertEquals(
            MuseWaterLanguage.CondensationRefraction,
            MuseVisualProfile.AuroraGlass.worldExperience().waterLanguage,
        )
        assertEquals(
            MuseWaterLanguage.DryHeat,
            MuseVisualProfile.MidnightEmber.worldExperience().waterLanguage,
        )
        assertEquals(
            MuseWaterLanguage.LunarCondensation,
            MuseVisualProfile.MoonlitViolet.worldExperience().waterLanguage,
        )
        assertEquals(
            MuseWaterLanguage.TidalRefraction,
            MuseVisualProfile.OceanPulse.worldExperience().waterLanguage,
        )
        assertEquals(
            MuseWaterLanguage.RestrainedDew,
            MuseVisualProfile.RoseNoir.worldExperience().waterLanguage,
        )

        val verdant = MuseVisualProfile.VerdantRain.worldExperience()
        val ember = MuseVisualProfile.MidnightEmber.worldExperience()
        val ocean = MuseVisualProfile.OceanPulse.worldExperience()

        assertTrue(verdant.allowsPhysicalDroplets)
        assertTrue(ocean.allowsWaterRefraction)
        assertTrue(!ember.allowsPhysicalDroplets)
        assertTrue(!ember.allowsWaterRefraction)
        assertNotEquals(verdant.waterLanguage, ocean.waterLanguage)
    }
}
