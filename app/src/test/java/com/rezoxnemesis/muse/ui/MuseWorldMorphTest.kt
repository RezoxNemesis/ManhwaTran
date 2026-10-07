package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MuseWorldMorphTest {
    @Test
    fun morphStartsFromCurrentWorldAndFinishesAtTargetWorld() {
        val settled = settledMuseWorldMorph(MuseVisualProfile.VerdantRain)
        val morph = retargetMuseWorldMorph(
            state = settled,
            target = MuseVisualProfile.AuroraGlass,
            currentProgress = 1f,
        )

        assertBlendIsProfile(
            sampleMuseWorldMorph(morph, 0f),
            MuseVisualProfile.VerdantRain,
        )
        assertBlendIsProfile(
            sampleMuseWorldMorph(morph, 1f),
            MuseVisualProfile.AuroraGlass,
        )
        assertEquals(
            MuseVisualProfile.AuroraGlass.transitionSignature().durationMillis,
            morph.durationMillis,
        )
    }

    @Test
    fun retargetingMidMorphStartsFromTheVisibleBlendInsteadOfQueuedTarget() {
        val first = retargetMuseWorldMorph(
            state = settledMuseWorldMorph(MuseVisualProfile.VerdantRain),
            target = MuseVisualProfile.AuroraGlass,
            currentProgress = 1f,
        )
        val visible = sampleMuseWorldMorph(first, 0.40f)

        val retargeted = retargetMuseWorldMorph(
            state = first,
            target = MuseVisualProfile.MidnightEmber,
            currentProgress = 0.40f,
        )

        assertEquals(visible, retargeted.source)
        assertEquals(MuseVisualProfile.MidnightEmber, retargeted.target)
        assertEquals(visible, sampleMuseWorldMorph(retargeted, 0f))
        assertBlendIsProfile(
            sampleMuseWorldMorph(retargeted, 1f),
            MuseVisualProfile.MidnightEmber,
        )
        assertTrue(retargeted.source.weightOf(MuseVisualProfile.VerdantRain) > 0f)
        assertTrue(retargeted.source.weightOf(MuseVisualProfile.AuroraGlass) > 0f)
    }

    @Test
    fun morphProgressIsClampedAndWeightsStayNormalized() {
        val morph = retargetMuseWorldMorph(
            state = settledMuseWorldMorph(MuseVisualProfile.MoonlitViolet),
            target = MuseVisualProfile.OceanPulse,
            currentProgress = 1f,
        )

        val before = sampleMuseWorldMorph(morph, -2f)
        val after = sampleMuseWorldMorph(morph, 3f)
        val middle = sampleMuseWorldMorph(morph, 0.37f)

        assertBlendIsProfile(before, MuseVisualProfile.MoonlitViolet)
        assertBlendIsProfile(after, MuseVisualProfile.OceanPulse)
        assertEquals(1f, middle.totalWeight(), 0.0001f)
        assertTrue(middle.weightOf(MuseVisualProfile.MoonlitViolet) > 0f)
        assertTrue(middle.weightOf(MuseVisualProfile.OceanPulse) > 0f)
    }

    private fun assertBlendIsProfile(
        blend: MuseWorldBlend,
        profile: MuseVisualProfile,
    ) {
        MuseVisualProfile.entries.forEach { candidate ->
            val expected = if (candidate == profile) 1f else 0f
            assertEquals(expected, blend.weightOf(candidate), 0.0001f)
        }
        assertEquals(1f, blend.totalWeight(), 0.0001f)
    }
}
