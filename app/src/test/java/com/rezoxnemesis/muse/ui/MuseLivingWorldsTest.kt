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
    @Test
    fun worldsHaveDistinctFoliageSilhouettesAndUiMaterialLanguages() {
        val worlds = MuseVisualProfile.entries.associateWith { it.livingWorldStyle() }

        assertEquals(
            MuseVisualProfile.entries.size,
            worlds.values.map { it.foliageKind }.distinct().size,
        )
        assertEquals(
            MuseVisualProfile.entries.size,
            worlds.values.map { it.chromeKind }.distinct().size,
        )

        val verdant = worlds.getValue(MuseVisualProfile.VerdantRain)
        val aurora = worlds.getValue(MuseVisualProfile.AuroraGlass)
        val ember = worlds.getValue(MuseVisualProfile.MidnightEmber)
        val moon = worlds.getValue(MuseVisualProfile.MoonlitViolet)
        val ocean = worlds.getValue(MuseVisualProfile.OceanPulse)
        val rose = worlds.getValue(MuseVisualProfile.RoseNoir)

        assertTrue(verdant.edgeIrregularity > aurora.edgeIrregularity)
        assertTrue(ember.edgeIrregularity > moon.edgeIrregularity)
        assertNotEquals(verdant.foliageKind, rose.foliageKind)
        assertNotEquals(aurora.chromeKind, ocean.chromeKind)
    }

    @Test
    fun worldIdentityChangesGeometryAndChromeMotionNotOnlyColor() {
        val foliage = MuseVisualProfile.entries.map {
            it.livingWorldStyle().foliageKind.geometry()
        }
        val chrome = MuseVisualProfile.entries.map {
            it.livingWorldStyle().chromeKind.geometry()
        }

        assertEquals(
            MuseVisualProfile.entries.size,
            foliage.map {
                listOf(
                    it.shoulderWidth,
                    it.waistWidth,
                    it.waveAmount,
                    it.asymmetry,
                    it.serration,
                    it.roundness,
                )
            }.distinct().size,
        )
        assertEquals(
            MuseVisualProfile.entries.size,
            chrome.map {
                listOf(
                    it.topStartScale,
                    it.topEndScale,
                    it.bottomEndScale,
                    it.bottomStartScale,
                    it.sheenTravel,
                    it.rimPulse,
                )
            }.distinct().size,
        )
        assertEquals(
            MuseVisualProfile.entries.size,
            chrome.map { it.motionPeriodMillis }.distinct().size,
        )

        val verdant = MuseWorldFoliageKind.WetBroadleaf.geometry()
        val aurora = MuseWorldFoliageKind.PrismBlade.geometry()
        val ember = MuseWorldFoliageKind.CharredShard.geometry()
        val moon = MuseWorldFoliageKind.MoonLance.geometry()
        val ocean = MuseWorldFoliageKind.TidalFrond.geometry()
        val rose = MuseWorldFoliageKind.VelvetPetal.geometry()

        assertTrue(ember.serration > aurora.serration)
        assertTrue(ocean.waveAmount > moon.waveAmount)
        assertTrue(rose.roundness > aurora.roundness)
        assertTrue(verdant.shoulderWidth > moon.shoulderWidth)
    }

}
