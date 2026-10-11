package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseWorldScenePreviewPolicyTest {
    private fun policy(
        profile: MuseVisualProfile,
        rain: MuseRainLevel = MuseRainLevel.Rain,
        intensity: MuseVisualIntensity = MuseVisualIntensity.Balanced,
        playing: Boolean = false,
        energy: Float = 0f,
    ) = resolveMuseWorldPreviewPolicy(profile, rain, intensity, playing, energy)

    @Test
    fun eachWorldHasAUniquePreviewIdentityAndMatchesItsRealWaterPolicy() {
        val all = MuseVisualProfile.entries.map { policy(it) }
        assertEquals(6, all.map { it.motif }.distinct().size)
        MuseVisualProfile.entries.forEach { profile ->
            assertEquals(profile.worldExperience().waterLanguage, policy(profile).waterLanguage)
        }
    }

    @Test
    fun rainfallInThumbnailsIsOnlyPhysicalInVerdantRain() {
        assertEquals(0, policy(MuseVisualProfile.VerdantRain, MuseRainLevel.Off).rainStreaks)
        assertEquals(1, policy(MuseVisualProfile.VerdantRain, MuseRainLevel.Mist).rainStreaks)
        assertEquals(3, policy(MuseVisualProfile.VerdantRain, MuseRainLevel.Rain).rainStreaks)
        assertEquals(5, policy(MuseVisualProfile.VerdantRain, MuseRainLevel.Downpour).rainStreaks)
        MuseVisualProfile.entries.filterNot { it == MuseVisualProfile.VerdantRain }.forEach {
            assertEquals(0, policy(it, MuseRainLevel.Downpour).rainStreaks)
        }
    }

    @Test
    fun audioReactionRequiresPlayingAndSanitizesInput() {
        val world = MuseVisualProfile.OceanPulse
        assertEquals(0f, policy(world, playing = false, energy = 1f).reactiveGlow, 0.0001f)
        assertTrue(policy(world, playing = true, energy = 0.85f).reactiveGlow > 0f)
        assertEquals(0f, policy(world, playing = true, energy = Float.NaN).reactiveGlow, 0.0001f)
        assertEquals(0.30f, policy(world, playing = true, energy = 9f).reactiveGlow, 0.0001f)
        assertEquals(0f, policy(world, playing = true, energy = Float.NEGATIVE_INFINITY).reactiveGlow, 0.0001f)
    }

    @Test
    fun intensityPresetsAreMonotonicAndIdleMotionIsSubdued() {
        val world = MuseVisualProfile.AuroraGlass
        val calm = policy(world, intensity = MuseVisualIntensity.Calm, playing = true)
        val balanced = policy(world, intensity = MuseVisualIntensity.Balanced, playing = true)
        val vivid = policy(world, intensity = MuseVisualIntensity.Vivid, playing = true)
        val idle = policy(world, intensity = MuseVisualIntensity.Vivid, playing = false)
        assertTrue(calm.motionScale < balanced.motionScale)
        assertTrue(balanced.motionScale < vivid.motionScale)
        assertTrue(idle.motionScale < vivid.motionScale)
        assertTrue(idle.cycleMillis > vivid.cycleMillis)
        assertTrue(vivid.cycleMillis in 4_000..12_000)
        assertFalse(idle.reactiveGlow > 0f)
    }
}
