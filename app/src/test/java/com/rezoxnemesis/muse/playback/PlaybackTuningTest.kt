package com.rezoxnemesis.muse.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTuningTest {
    @Test
    fun speedIsBoundedAndInvalidValuesReset() {
        assertEquals(PlaybackTuning.MinSpeed, PlaybackTuning.sanitizeSpeed(0.1f), 0.0001f)
        assertEquals(PlaybackTuning.MaxSpeed, PlaybackTuning.sanitizeSpeed(3.0f), 0.0001f)
        assertEquals(PlaybackTuning.DefaultSpeed, PlaybackTuning.sanitizeSpeed(Float.NaN), 0.0001f)
    }

    @Test
    fun pitchIsBoundedAndDefaultsToOriginalPitch() {
        assertEquals(PlaybackTuning.MinPitch, PlaybackTuning.sanitizePitch(0.2f), 0.0001f)
        assertEquals(PlaybackTuning.MaxPitch, PlaybackTuning.sanitizePitch(2.0f), 0.0001f)
        assertEquals(PlaybackTuning.DefaultPitch, PlaybackTuning.sanitizePitch(Float.POSITIVE_INFINITY), 0.0001f)
    }
}
