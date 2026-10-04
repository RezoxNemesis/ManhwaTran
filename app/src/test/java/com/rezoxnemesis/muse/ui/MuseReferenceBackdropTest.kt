package com.rezoxnemesis.muse.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MuseReferenceBackdropTest {
    @Test
    fun everyReferenceScreenUsesTheApprovedHighResolutionAsset() {
        val expected = mapOf(
            MuseReferenceScreen.Splash to "muse_ref_splash.png",
            MuseReferenceScreen.Home to "muse_ref_home.png",
            MuseReferenceScreen.NowPlaying to "muse_ref_now_playing.png",
            MuseReferenceScreen.Lyrics to "muse_ref_lyrics.png",
            MuseReferenceScreen.Queue to "muse_ref_queue.png",
            MuseReferenceScreen.Explore to "muse_ref_explore.png",
            MuseReferenceScreen.Library to "muse_ref_library.png",
            MuseReferenceScreen.Playlists to "muse_ref_playlists.png",
            MuseReferenceScreen.Equalizer to "muse_ref_equalizer.png",
            MuseReferenceScreen.Settings to "muse_ref_settings.png",
            MuseReferenceScreen.Artist to "muse_ref_artist.png",
            MuseReferenceScreen.Album to "muse_ref_album.png",
            MuseReferenceScreen.Downloads to "muse_ref_downloads.png",
            MuseReferenceScreen.SleepTimer to "muse_ref_sleep.png",
            MuseReferenceScreen.MoreOptions to "muse_ref_more.png",
        )

        assertEquals(
            expected,
            MuseReferenceScreen.entries.associateWith { it.assetEntryName },
        )
    }
}
