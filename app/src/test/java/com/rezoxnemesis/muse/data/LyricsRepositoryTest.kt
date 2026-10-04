package com.rezoxnemesis.muse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsRepositoryTest {
    @Test
    fun parsesPlainLyricsWithoutInventingTimestamps() {
        val result = LyricsRepository.parseLyrics(
            """
            First line
            Second line
            """.trimIndent()
        )

        assertFalse(result.synced)
        assertEquals(2, result.lines.size)
        assertEquals("First line", result.lines[0].text)
        assertEquals(null, result.lines[0].timeMs)
    }

    @Test
    fun parsesAndSortsSyncedLrcLines() {
        val result = LyricsRepository.parseLyrics(
            """
            [00:12.50]Second
            [00:03.25]First
            """.trimIndent()
        )

        assertTrue(result.synced)
        assertEquals(listOf("First", "Second"), result.lines.map { it.text })
        assertEquals(listOf(3_250L, 12_500L), result.lines.map { it.timeMs })
    }

    @Test
    fun appliesGlobalOffsetAndMultipleTimestamps() {
        val result = LyricsRepository.parseLyrics(
            """
            [00:01.00][00:02.00]Echo
            [offset:250]
            """.trimIndent()
        )

        assertTrue(result.synced)
        assertEquals(2, result.lines.size)
        assertEquals(listOf(1_250L, 2_250L), result.lines.map { it.timeMs })
        assertEquals(listOf("Echo", "Echo"), result.lines.map { it.text })
    }

    @Test
    fun ignoresCommonMetadataTags() {
        val result = LyricsRepository.parseLyrics(
            """
            [ar:Artist]
            [ti:Title]
            [00:01.5]Lyric
            """.trimIndent()
        )

        assertTrue(result.synced)
        assertEquals(1, result.lines.size)
        assertEquals("Lyric", result.lines.single().text)
        assertEquals(1_500L, result.lines.single().timeMs)
    }
}
