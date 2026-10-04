package com.rezoxnemesis.muse.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmbeddedLyricsReaderTest {
    @Test
    fun readsUtf8UsltFromId3v23() {
        val lyric = "First line\nSecond line"
        val body = byteArrayOf(3) +
            "eng".toByteArray(Charsets.ISO_8859_1) +
            byteArrayOf(0) +
            lyric.toByteArray(Charsets.UTF_8)

        val tag = id3v23(
            frame(
                id = "USLT",
                body = body,
            )
        )

        assertEquals(
            lyric,
            EmbeddedLyricsReader.readUnsynchronisedLyrics(tag),
        )
    }

    @Test
    fun skipsNonLyricsFrames() {
        val titleBody = byteArrayOf(3) +
            "A title".toByteArray(Charsets.UTF_8)

        val tag = id3v23(
            frame(
                id = "TIT2",
                body = titleBody,
            )
        )

        assertNull(
            EmbeddedLyricsReader.readUnsynchronisedLyrics(tag),
        )
    }

    @Test
    fun ignoresNonId3Input() {
        assertNull(
            EmbeddedLyricsReader.readUnsynchronisedLyrics(
                "not an id3 tag".toByteArray(),
            )
        )
    }

    private fun id3v23(
        frameBytes: ByteArray,
    ): ByteArray {
        val size = synchsafe(frameBytes.size)
        return byteArrayOf(
            'I'.code.toByte(),
            'D'.code.toByte(),
            '3'.code.toByte(),
            3,
            0,
            0,
            size[0],
            size[1],
            size[2],
            size[3],
        ) + frameBytes
    }

    private fun frame(
        id: String,
        body: ByteArray,
    ): ByteArray {
        val size = body.size
        return id.toByteArray(Charsets.ISO_8859_1) +
            byteArrayOf(
                ((size ushr 24) and 0xFF).toByte(),
                ((size ushr 16) and 0xFF).toByte(),
                ((size ushr 8) and 0xFF).toByte(),
                (size and 0xFF).toByte(),
                0,
                0,
            ) +
            body
    }

    private fun synchsafe(
        value: Int,
    ): ByteArray =
        byteArrayOf(
            ((value ushr 21) and 0x7F).toByte(),
            ((value ushr 14) and 0x7F).toByte(),
            ((value ushr 7) and 0x7F).toByte(),
            (value and 0x7F).toByte(),
        )
}
