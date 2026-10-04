package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MuseReferenceBackdropTest {
    @Test
    fun everyReferenceScreenUsesItsApprovedLosslessAtlasCell() {
        val expected = mapOf(
            MuseReferenceScreen.Splash to 0,
            MuseReferenceScreen.Home to 1,
            MuseReferenceScreen.NowPlaying to 2,
            MuseReferenceScreen.Lyrics to 3,
            MuseReferenceScreen.Queue to 4,
            MuseReferenceScreen.Explore to 5,
            MuseReferenceScreen.Library to 6,
            MuseReferenceScreen.Playlists to 7,
            MuseReferenceScreen.Equalizer to 8,
            MuseReferenceScreen.Settings to 9,
            MuseReferenceScreen.Artist to 10,
            MuseReferenceScreen.Album to 11,
            MuseReferenceScreen.Downloads to 12,
            MuseReferenceScreen.SleepTimer to 13,
            MuseReferenceScreen.MoreOptions to 14,
        )

        assertEquals(
            expected,
            MuseReferenceScreen.entries.associateWith { it.atlasIndex },
        )
        assertEquals(
            (0 until 15).toList(),
            MuseReferenceScreen.entries.map { it.atlasIndex }.sorted(),
        )
    }
}
