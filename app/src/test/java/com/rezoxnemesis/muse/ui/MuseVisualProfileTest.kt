package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
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
