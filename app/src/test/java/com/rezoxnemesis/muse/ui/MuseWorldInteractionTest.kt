package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseWorldInteractionTest {
    @Test
    fun everyWorldOwnsDistinctEchoAndMusicMemoryLanguages() {
        val languages = MuseVisualProfile.entries.map {
            it.worldExperience().interactionLanguage()
        }

        assertEquals(
            MuseVisualProfile.entries.size,
            languages.map { it.echoKind }.distinct().size,
        )
        assertEquals(
            MuseVisualProfile.entries.size,
            languages.map { it.memoryKind }.distinct().size,
        )

        assertEquals(
            MuseWorldEchoKind.WetFoliageTrail,
            MuseVisualProfile.VerdantRain.worldExperience().interactionLanguage().echoKind,
        )
        assertEquals(
            MuseWorldEchoKind.PrismRibbon,
            MuseVisualProfile.AuroraGlass.worldExperience().interactionLanguage().echoKind,
        )
        assertEquals(
            MuseWorldEchoKind.EmberTrace,
            MuseVisualProfile.MidnightEmber.worldExperience().interactionLanguage().echoKind,
        )
        assertEquals(
            MuseWorldEchoKind.MistDisplacement,
            MuseVisualProfile.MoonlitViolet.worldExperience().interactionLanguage().echoKind,
        )
        assertEquals(
            MuseWorldEchoKind.TidalRipple,
            MuseVisualProfile.OceanPulse.worldExperience().interactionLanguage().echoKind,
        )
        assertEquals(
            MuseWorldEchoKind.PetalDisplacement,
            MuseVisualProfile.RoseNoir.worldExperience().interactionLanguage().echoKind,
        )
    }

    @Test
    fun musicMemoryAccumulatesButRemainsBoundedAndDecaysAway() {
        val experience = MuseVisualProfile.MidnightEmber.worldExperience()
        val spectrum = MuseAudioSpectrum(
            energy = 1f,
            bass = 1f,
            mid = 1f,
            high = 1f,
            transient = 1f,
        )
        var state = emptyMuseMusicMemory(experience)

        repeat(100) {
            state = accumulateMuseMusicMemory(state, spectrum)
        }

        assertTrue(state.strength in 0f..1f)
        assertEquals(1f, state.strength, 0.0001f)

        val partlyDecayed = decayMuseMusicMemory(state, deltaMillis = 750)
        assertTrue(partlyDecayed.strength in 0f..<state.strength)

        val fullyDecayed = decayMuseMusicMemory(state, deltaMillis = 20_000)
        assertEquals(0f, fullyDecayed.strength, 0.0001f)
    }

    @Test
    fun quietWorldStillBreathesAndPlayingMusicRaisesItsEnergy() {
        val moon = MuseVisualProfile.MoonlitViolet.worldExperience()
        val quiet = resolveMuseWorldBreathing(
            experience = moon,
            isPlaying = false,
            audioEnergy = 0f,
        )
        val playing = resolveMuseWorldBreathing(
            experience = moon,
            isPlaying = true,
            audioEnergy = 0.85f,
        )

        assertTrue(quiet.amplitude > 0f)
        assertTrue(quiet.tempoScale > 0f)
        assertTrue(playing.amplitude > quiet.amplitude)
        assertTrue(playing.tempoScale > quiet.tempoScale)
    }


    @Test
    fun routeFocusMappingAndInteractionFrameCarryFocusAndGestureEnergyIntoV4() {
        assertEquals(MuseFocusRegion.Home, museFocusRegionForRoute("home"))
        assertEquals(MuseFocusRegion.Explore, museFocusRegionForRoute("explore"))
        assertEquals(MuseFocusRegion.Library, museFocusRegionForRoute("library"))
        assertEquals(MuseFocusRegion.Equalizer, museFocusRegionForRoute("equalizer"))
        assertEquals(MuseFocusRegion.MuseLab, museFocusRegionForRoute("tools"))
        assertEquals(MuseFocusRegion.NowPlaying, museFocusRegionForRoute("nowPlaying"))

        val frame = resolveMuseV4InteractionFrame(
            focus = focusMuseRegion(MuseFocusRegion.Equalizer),
            momentum = MuseGestureMomentum(
                x = 0.82f,
                y = -0.46f,
                magnitude = 0.82f,
            ),
        )

        assertEquals(MuseFocusRegion.Equalizer, frame.focusRegion)
        assertEquals(1f, frame.focusStrength, 0.0001f)
        assertEquals(0.82f, frame.momentumX, 0.0001f)
        assertEquals(-0.46f, frame.momentumY, 0.0001f)
        assertEquals(0.82f, frame.momentumStrength, 0.0001f)

        val clamped = resolveMuseV4InteractionFrame(
            focus = MuseLivingFocusState(MuseFocusRegion.Home, 4f),
            momentum = MuseGestureMomentum(3f, -4f, 7f),
        )
        assertEquals(1f, clamped.focusStrength, 0.0001f)
        assertEquals(1f, clamped.momentumX, 0.0001f)
        assertEquals(-1f, clamped.momentumY, 0.0001f)
        assertEquals(1f, clamped.momentumStrength, 0.0001f)
    }

    @Test
    fun focusAndGestureMomentumAreBoundedAndDecayDeterministically() {
        val focused = focusMuseRegion(MuseFocusRegion.Equalizer)
        val fading = decayMuseLivingFocus(focused, deltaMillis = 500)
        val gone = decayMuseLivingFocus(focused, deltaMillis = 20_000)

        assertEquals(1f, focused.strength, 0.0001f)
        assertTrue(fading.strength in 0f..<1f)
        assertEquals(0f, gone.strength, 0.0001f)

        val momentum = resolveMuseGestureMomentum(
            deltaX = 9_000f,
            deltaY = -9_000f,
            velocityX = 40_000f,
            velocityY = -40_000f,
        )
        assertTrue(momentum.x in -1f..1f)
        assertTrue(momentum.y in -1f..1f)
        assertEquals(1f, momentum.x, 0.0001f)
        assertEquals(-1f, momentum.y, 0.0001f)
    }
}
