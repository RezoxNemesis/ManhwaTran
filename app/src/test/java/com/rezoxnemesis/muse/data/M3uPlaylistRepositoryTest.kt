package com.rezoxnemesis.muse.data

import org.junit.Assert.assertEquals
import org.junit.Test

class M3uPlaylistRepositoryTest {
    @Test
    fun parsesExtendedM3uEntriesOnly() {
        val entries = M3uPlaylistRepository.parseEntries(
            """
            #EXTM3U
            #EXTINF:123,Artist - Song
            content://media/external/audio/media/42

            # a comment
            content://example/document/track.mp3
            """.trimIndent()
        )

        assertEquals(
            listOf(
                "content://media/external/audio/media/42",
                "content://example/document/track.mp3",
            ),
            entries,
        )
    }

    @Test
    fun stripsUtf8BomAndWhitespace() {
        val entries = M3uPlaylistRepository.parseEntries(
            "\uFEFF  file:///Music/Track.mp3  \n"
        )

        assertEquals(
            listOf("file:///Music/Track.mp3"),
            entries,
        )
    }

    @Test
    fun preservesDuplicateSourceEntriesForResolutionPhase() {
        val entries = M3uPlaylistRepository.parseEntries(
            """
            one.mp3
            one.mp3
            two.mp3
            """.trimIndent()
        )

        assertEquals(
            listOf("one.mp3", "one.mp3", "two.mp3"),
            entries,
        )
    }
}
