package com.rezoxnemesis.muse.data

import android.content.Context
import android.net.Uri
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LyricLine(
    val timeMs: Long?,
    val text: String,
)

data class LyricsDocument(
    val lines: List<LyricLine>,
    val synced: Boolean,
)

class LyricsRepository(
    private val context: Context,
) {
    private val lyricsDirectory: File
        get() = File(context.filesDir, "lyrics").apply { mkdirs() }

    suspend fun importLyrics(
        trackId: Long,
        source: Uri,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(source)?.use { input ->
                input.readNBytes(MaxLyricsBytes + 1)
            } ?: error("Could not open the selected lyrics file.")

            require(bytes.size <= MaxLyricsBytes) {
                "Lyrics file is too large."
            }

            val text = bytes.toString(Charsets.UTF_8)
                .removePrefix("\uFEFF")
                .trim()

            require(text.isNotBlank()) {
                "The selected lyrics file is empty."
            }

            val target = fileFor(trackId)
            val temporary = File(target.parentFile, "${target.name}.tmp")
            temporary.writeText(text, Charsets.UTF_8)

            if (target.exists() && !target.delete()) {
                temporary.delete()
                error("Could not replace the existing lyrics file.")
            }
            if (!temporary.renameTo(target)) {
                temporary.delete()
                error("Could not save the lyrics file.")
            }
        }
    }

    suspend fun loadLyrics(trackId: Long): LyricsDocument? = withContext(Dispatchers.IO) {
        val file = fileFor(trackId)
        if (!file.isFile) return@withContext null
        val text = runCatching {
            file.readText(Charsets.UTF_8)
        }.getOrNull() ?: return@withContext null
        parseLyrics(text)
    }

    suspend fun removeLyrics(trackId: Long): Boolean = withContext(Dispatchers.IO) {
        val file = fileFor(trackId)
        !file.exists() || file.delete()
    }

    private fun fileFor(trackId: Long): File =
        File(lyricsDirectory, "$trackId.lrc")

    companion object {
        private const val MaxLyricsBytes = 2 * 1024 * 1024

        fun parseLyrics(raw: String): LyricsDocument {
            val sourceLines = raw
                .removePrefix("\uFEFF")
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .lines()

            var offsetMs = 0L
            val parsed = mutableListOf<LyricLine>()
            val plain = mutableListOf<LyricLine>()

            sourceLines.forEach { source ->
                val line = source.trimEnd()

                val offsetMatch = OffsetRegex.matchEntire(line.trim())
                if (offsetMatch != null) {
                    offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: offsetMs
                    return@forEach
                }

                if (MetadataRegex.matches(line.trim())) {
                    return@forEach
                }

                val timestamps = TimestampRegex.findAll(line).toList()
                if (timestamps.isEmpty()) {
                    if (line.isNotBlank()) {
                        plain += LyricLine(timeMs = null, text = line)
                    }
                    return@forEach
                }

                val text = line.replace(TimestampRegex, "").trim()
                timestamps.forEach { match ->
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val fraction = match.groupValues[3]
                    val fractionMs = when (fraction.length) {
                        0 -> 0L
                        1 -> fraction.toLongOrNull()?.times(100L) ?: 0L
                        2 -> fraction.toLongOrNull()?.times(10L) ?: 0L
                        else -> fraction.take(3).padEnd(3, '0').toLongOrNull() ?: 0L
                    }
                    val timeMs = (minutes * 60_000L + seconds * 1_000L + fractionMs + offsetMs)
                        .coerceAtLeast(0L)
                    parsed += LyricLine(timeMs = timeMs, text = text)
                }
            }

            if (parsed.isNotEmpty()) {
                return LyricsDocument(
                    lines = parsed.sortedBy { it.timeMs },
                    synced = true,
                )
            }

            return LyricsDocument(
                lines = plain.ifEmpty {
                    sourceLines
                        .filter { it.isNotBlank() }
                        .map { LyricLine(timeMs = null, text = it) }
                },
                synced = false,
            )
        }

        private val TimestampRegex =
            Regex("""\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]""")

        private val OffsetRegex =
            Regex("""\[offset:([+-]?\d+)]""", RegexOption.IGNORE_CASE)

        private val MetadataRegex =
            Regex("""\[(ar|al|ti|au|by|re|ve|length):.*]""", RegexOption.IGNORE_CASE)
    }
}
