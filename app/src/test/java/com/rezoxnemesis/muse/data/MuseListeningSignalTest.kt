package com.rezoxnemesis.muse.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MuseListeningSignalTest {
    @Test
    fun record_tracksAverageCompletionAndClassifiesCompletedSessions() {
        val signal = MuseListeningSignal()
            .record(0.90)
            .record(0.75)

        assertEquals(2, signal.observedSessions)
        assertEquals(1, signal.completedSessions)
        assertEquals(0, signal.skippedSessions)
        assertEquals(0.825, signal.averageCompletionRatio, 0.0001)
        assertEquals(0.0, signal.skipRatio, 0.0001)
    }

    @Test
    fun record_countsEarlyExitAsSkipButKeepsMidTrackExitNeutral() {
        val signal = MuseListeningSignal()
            .record(0.20)
            .record(0.60)

        assertEquals(2, signal.observedSessions)
        assertEquals(0, signal.completedSessions)
        assertEquals(1, signal.skippedSessions)
        assertEquals(0.40, signal.averageCompletionRatio, 0.0001)
        assertEquals(0.50, signal.skipRatio, 0.0001)
    }

    @Test
    fun record_clampsOutOfRangeFractionsAndIgnoresNonFiniteSamples() {
        val signal = MuseListeningSignal()
            .record(-2.0)
            .record(4.0)
            .record(Double.NaN)
            .record(Double.POSITIVE_INFINITY)

        assertEquals(2, signal.observedSessions)
        assertEquals(1, signal.completedSessions)
        assertEquals(1, signal.skippedSessions)
        assertEquals(0.50, signal.averageCompletionRatio, 0.0001)
        assertEquals(0.50, signal.skipRatio, 0.0001)
    }
}
