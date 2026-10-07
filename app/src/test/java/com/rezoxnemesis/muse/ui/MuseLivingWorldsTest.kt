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

    @Test
    fun genericBotanicalRainIsReservedForVerdantSoOtherWorldsKeepTheirIdentity() {
        assertTrue(MuseVisualProfile.VerdantRain.usesBotanicalRainLayer())
        assertTrue(
            MuseVisualProfile.entries
                .filterNot { it == MuseVisualProfile.VerdantRain }
                .none { it.usesBotanicalRainLayer() }
        )
    }

    @Test
    fun worldsUseDifferentSceneCompositionsNotOnlyDifferentShapes() {
        val scenes = MuseVisualProfile.entries.map { it.worldSceneGeometry() }

        assertEquals(
            MuseVisualProfile.entries.size,
            scenes.map {
                listOf(
                    it.widthScale,
                    it.heightScale,
                    it.verticalBias,
                    it.rotationBias,
                    it.edgePull,
                    it.alphaScale,
                )
            }.distinct().size,
        )

        val verdant = MuseVisualProfile.VerdantRain.worldSceneGeometry()
        val aurora = MuseVisualProfile.AuroraGlass.worldSceneGeometry()
        val ember = MuseVisualProfile.MidnightEmber.worldSceneGeometry()
        val moon = MuseVisualProfile.MoonlitViolet.worldSceneGeometry()
        val ocean = MuseVisualProfile.OceanPulse.worldSceneGeometry()
        val rose = MuseVisualProfile.RoseNoir.worldSceneGeometry()

        assertTrue(verdant.widthScale > moon.widthScale)
        assertTrue(ember.verticalBias > aurora.verticalBias)
        assertTrue(ocean.heightScale > verdant.heightScale)
        assertTrue(rose.rotationBias != moon.rotationBias)
        assertTrue(aurora.alphaScale < verdant.alphaScale)
    }

    @Test
    fun eachWorldUsesItsOwnNavigationMotionLanguage() {
        val motions = MuseVisualProfile.entries.map {
            it.livingWorldStyle().chromeKind.navMotion()
        }

        assertEquals(
            MuseVisualProfile.entries.size,
            motions.map {
                listOf(
                    it.dampingRatio,
                    it.stiffness,
                    it.stretch,
                    it.squash,
                    it.leadingPull,
                    it.pivotBias,
                )
            }.distinct().size,
        )

        val dew = MuseWorldChromeKind.DewGlass.navMotion()
        val prism = MuseWorldChromeKind.PrismFacet.navMotion()
        val ember = MuseWorldChromeKind.ForgedEmber.navMotion()
        val lunar = MuseWorldChromeKind.LunarHalo.navMotion()
        val tidal = MuseWorldChromeKind.TidalLens.navMotion()
        val rose = MuseWorldChromeKind.RoseVelvet.navMotion()

        assertTrue(ember.stiffness > lunar.stiffness)
        assertTrue(tidal.stretch > lunar.stretch)
        assertTrue(prism.leadingPull > dew.leadingPull)
        assertTrue(rose.squash > prism.squash)
    }

    @Test
    fun everyMaterialHasItsOwnMicroDetailLanguage() {
        val details = MuseWorldMaterialKind.entries.map { it.microDetail() }

        assertEquals(
            MuseWorldMaterialKind.entries.size,
            details.map {
                listOf(
                    it.dewBeads,
                    it.facetLines,
                    it.crackLines,
                    it.hazeSpecks,
                    it.causticBands,
                    it.velvetGrain,
                )
            }.distinct().size,
        )

        val wet = MuseWorldMaterialKind.WetRainforest.microDetail()
        val prism = MuseWorldMaterialKind.IridescentGlass.microDetail()
        val ember = MuseWorldMaterialKind.CharredCopper.microDetail()
        val lunar = MuseWorldMaterialKind.LunarSilver.microDetail()
        val tidal = MuseWorldMaterialKind.TidalTeal.microDetail()
        val rose = MuseWorldMaterialKind.VelvetRose.microDetail()

        assertTrue(wet.dewBeads > ember.dewBeads)
        assertTrue(prism.facetLines > lunar.facetLines)
        assertTrue(ember.crackLines > rose.crackLines)
        assertTrue(lunar.hazeSpecks > ember.hazeSpecks)
        assertTrue(tidal.causticBands > wet.causticBands)
        assertTrue(rose.velvetGrain > prism.velvetGrain)
    }

    @Test
    fun worldsHaveDistinctLightingCompositions() {
        val fields = MuseVisualProfile.entries.map { it.worldLightField() }

        assertEquals(
            MuseVisualProfile.entries.size,
            fields.map {
                listOf(
                    it.keyX,
                    it.keyY,
                    it.keyRadius,
                    it.skyWeight,
                    it.floorWeight,
                    it.sideBias,
                    it.bokehDensity,
                    it.shaftStrength,
                    it.centerVeil,
                )
            }.distinct().size,
        )

        val verdant = MuseVisualProfile.VerdantRain.worldLightField()
        val aurora = MuseVisualProfile.AuroraGlass.worldLightField()
        val ember = MuseVisualProfile.MidnightEmber.worldLightField()
        val moon = MuseVisualProfile.MoonlitViolet.worldLightField()
        val ocean = MuseVisualProfile.OceanPulse.worldLightField()
        val rose = MuseVisualProfile.RoseNoir.worldLightField()

        assertTrue(ember.keyY > verdant.keyY)
        assertTrue(ember.floorWeight > moon.floorWeight)
        assertTrue(moon.keyRadius > ember.keyRadius)
        assertTrue(ocean.shaftStrength > verdant.shaftStrength)
        assertTrue(aurora.bokehDensity < verdant.bokehDensity)
        assertTrue(rose.sideBias != moon.sideBias)
    }

    @Test
    fun eachWorldHasAUniqueArrivalTransitionSignature() {
        val signatures = MuseVisualProfile.entries.map { it.transitionSignature() }

        assertEquals(
            MuseVisualProfile.entries.size,
            signatures.map {
                listOf(
                    it.durationMillis.toFloat(),
                    it.sweep,
                    it.ignition,
                    it.mistBloom,
                    it.refraction,
                    it.petalBurst,
                    it.dewPulse,
                )
            }.distinct().size,
        )

        val verdant = MuseVisualProfile.VerdantRain.transitionSignature()
        val aurora = MuseVisualProfile.AuroraGlass.transitionSignature()
        val ember = MuseVisualProfile.MidnightEmber.transitionSignature()
        val moon = MuseVisualProfile.MoonlitViolet.transitionSignature()
        val ocean = MuseVisualProfile.OceanPulse.transitionSignature()
        val rose = MuseVisualProfile.RoseNoir.transitionSignature()

        assertTrue(verdant.dewPulse > verdant.ignition)
        assertTrue(aurora.sweep > ember.sweep)
        assertTrue(ember.ignition > moon.ignition)
        assertTrue(moon.mistBloom > ocean.mistBloom)
        assertTrue(ocean.refraction > verdant.refraction)
        assertTrue(rose.petalBurst > aurora.petalBurst)
    }

}
