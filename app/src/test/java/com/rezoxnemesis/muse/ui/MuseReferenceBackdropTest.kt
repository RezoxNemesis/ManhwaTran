package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MuseReferenceBackdropTest {
    @Test
    fun everyReferenceScreenUsesTheApprovedHighResolutionAsset() {
        val expected = mapOf(
            MuseReferenceScreen.Splash to "muse_ref_splash.webp",
            MuseReferenceScreen.Home to "muse_ref_home.webp",
            MuseReferenceScreen.NowPlaying to "muse_ref_now_playing.webp",
            MuseReferenceScreen.Lyrics to "muse_ref_lyrics.webp",
            MuseReferenceScreen.Queue to "muse_ref_queue.webp",
            MuseReferenceScreen.Explore to "muse_ref_explore.webp",
            MuseReferenceScreen.Library to "muse_ref_library.webp",
            MuseReferenceScreen.Playlists to "muse_ref_playlists.webp",
            MuseReferenceScreen.Equalizer to "muse_ref_equalizer.webp",
            MuseReferenceScreen.Settings to "muse_ref_settings.webp",
            MuseReferenceScreen.Artist to "muse_ref_artist.webp",
            MuseReferenceScreen.Album to "muse_ref_album.webp",
            MuseReferenceScreen.Downloads to "muse_ref_downloads.webp",
            MuseReferenceScreen.SleepTimer to "muse_ref_sleep.webp",
            MuseReferenceScreen.MoreOptions to "muse_ref_more.webp",
        )

        assertEquals(
            expected,
            MuseReferenceScreen.entries.associateWith { it.assetEntryName },
        )
    }
}
