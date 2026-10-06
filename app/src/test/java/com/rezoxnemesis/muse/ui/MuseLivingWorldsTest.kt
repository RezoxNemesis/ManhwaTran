package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseLivingWorldsTest {
    @Test
    fun everyWorldHasAUniqueMaterialAndMotionIdentity() {
        val worlds = MuseVisualProfile.entries.associateWith { it.livingWorldStyle() }

        assertEquals(MuseVisualProfile.entries.size, worlds.values.map { it.materialKind }.distinct().size)
        assertEquals(MuseVisualProfile.entries.size, worlds.values.map { it.motionKind }.distinct().size)
        assertTrue(worlds.values.all { it.greyscaleSignature >= 0.72f })
        assertTrue(worlds.values.all { it.surfaceRoughness in 0.15f..1f })
        assertTrue(worlds.values.all { it.subsurfaceLight in 0.10f..1f })

        val verdant = worlds.getValue(MuseVisualProfile.VerdantRain)
        val ember = worlds.getValue(MuseVisualProfile.MidnightEmber)
        val moon = worlds.getValue(MuseVisualProfile.MoonlitViolet)
        val ocean = worlds.getValue(MuseVisualProfile.OceanPulse)
        val rose = worlds.getValue(MuseVisualProfile.RoseNoir)

        assertTrue(verdant.waterAdhesion > ember.waterAdhesion)
        assertTrue(ember.sparkDensity > moon.sparkDensity)
        assertTrue(moon.volumetricMist > verdant.volumetricMist)
        assertTrue(ocean.refractionWarp > rose.refractionWarp)
        assertTrue(rose.petalDepth > verdant.petalDepth)
        assertNotEquals(verdant.foliageHueBias, ember.foliageHueBias)
    }

    @Test
    fun worldsMapDifferentAudioBandsToDifferentEnvironmentalResponses() {
        val input = MuseAudioSpectrum(
            energy = 0.55f,
            bass = 0.90f,
            mid = 0.42f,
            high = 0.76f,
            transient = 0.84f,
        )

        val verdant = resolveWorldAudioResponse(MuseVisualProfile.VerdantRain, input)
        val aurora = resolveWorldAudioResponse(MuseVisualProfile.AuroraGlass, input)
        val ember = resolveWorldAudioResponse(MuseVisualProfile.MidnightEmber, input)
        val moon = resolveWorldAudioResponse(MuseVisualProfile.MoonlitViolet, input)
        val ocean = resolveWorldAudioResponse(MuseVisualProfile.OceanPulse, input)

        assertTrue(verdant.dropletImpulse > moon.dropletImpulse)
        assertTrue(aurora.shimmer > ember.shimmer)
        assertTrue(ember.particleImpulse > moon.particleImpulse)
        assertTrue(ocean.refractionImpulse > verdant.refractionImpulse)
        assertTrue(moon.calmFactor > ocean.calmFactor)
    }
}
