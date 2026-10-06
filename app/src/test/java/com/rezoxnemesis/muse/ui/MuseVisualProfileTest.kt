package com.rezoxnemesis.muse.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseVisualProfileTest {
    @Test
    fun storedModesRoundTrip() {
        MuseVisualMode.entries.forEach { mode ->
            assertEquals(mode, MuseVisualMode.fromStored(mode.storedValue))
        }
        assertEquals(
            MuseVisualMode.VerdantRain,
            MuseVisualMode.fromStored("unknown"),
        )
    }

    @Test
    fun fixedModesAlwaysResolveToTheirOwnProfile() {
        MuseVisualMode.entries
            .filter { it.fixedProfile != null }
            .forEach { mode ->
                assertEquals(
                    mode.fixedProfile,
                    resolveMuseVisualProfile(mode, "unrelated song metadata"),
                )
            }
    }

    @Test
    fun everyProfileHasDistinctChromeAccent() {
        val accents = MuseVisualProfile.entries
            .map { it.chromePalette().primary.value }
        assertEquals(accents.size, accents.distinct().size)
        assertEquals(
            Color(0xFF9BFF8D).value,
            MuseVisualProfile.VerdantRain.chromePalette().primary.value,
        )
    }

    @Test
    fun everyProfileOwnsDistinctLivingAtmosphereBehavior() {
        val behaviors = MuseVisualProfile.entries
            .associateWith { it.atmosphereBehavior() }

        assertEquals(
            MuseVisualProfile.entries.size,
            behaviors.values.map { it.kind }.distinct().size,
        )
        assertTrue(behaviors.values.all { it.rainDepthLayers >= 3 })
        assertTrue(
            behaviors.getValue(MuseVisualProfile.VerdantRain).dropletMobility >
                behaviors.getValue(MuseVisualProfile.MidnightEmber).dropletMobility,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.AuroraGlass).auroraRibbonStrength > 0.5f,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.MidnightEmber).emberStrength > 0.5f,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.MoonlitViolet).mistStrength > 0.5f,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.OceanPulse).refractionStrength > 0.5f,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.RoseNoir).bloomStrength > 0.5f,
        )
        assertTrue(behaviors.values.all { it.musicResponse in 0.2f..1.0f })
        assertTrue(behaviors.values.all { it.depthParallax in 0.5f..1.0f })
        assertTrue(behaviors.values.all { it.leafWetness in 0.55f..1.0f })
        assertTrue(behaviors.values.all { it.profileSignature >= 0.72f })
        assertTrue(
            behaviors.getValue(MuseVisualProfile.VerdantRain).rainImpactResponse > 0.85f,
        )
        assertTrue(
            behaviors.getValue(MuseVisualProfile.OceanPulse).musicTransientResponse >
                behaviors.getValue(MuseVisualProfile.MoonlitViolet).musicTransientResponse,
        )
    }

    @Test
    fun autoModeUsesLocalMetadataHeuristics() {
        assertEquals(
            MuseVisualProfile.RoseNoir,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "romantic soul ballad"),
        )
        assertEquals(
            MuseVisualProfile.MoonlitViolet,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "night piano ambient"),
        )
        assertEquals(
            MuseVisualProfile.OceanPulse,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "electronic house synth"),
        )
        assertEquals(
            MuseVisualProfile.MidnightEmber,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "rock workout power"),
        )
        assertEquals(
            MuseVisualProfile.AuroraGlass,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "chill indie acoustic"),
        )
        assertEquals(
            MuseVisualProfile.VerdantRain,
            resolveMuseVisualProfile(MuseVisualMode.Auto, "instrumental track"),
        )
    }
}
