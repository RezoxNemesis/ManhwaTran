package com.rezoxnemesis.muse.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class M3uImportResult(
    val name: String,
    val trackIds: List<Long>,
    val unresolvedEntries: List<String>,
)

class M3uPlaylistRepository(
    private val context: Context,
) {
    suspend fun importPlaylist(
        source: Uri,
        library: List<Track>,
    ): Result<M3uImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(source)
                ?.use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(8 * 1024)
                    var total = 0

                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        require(total <= MaxPlaylistBytes) {
                            "Playlist file is too large."
                        }
                        output.write(buffer, 0, read)
                    }
                    output.toByteArray()
                }
                ?: error("Could not open the selected playlist.")

            val entries = parseEntries(
                raw.toString(Charsets.UTF_8),
            )
            require(entries.isNotEmpty()) {
                "The selected playlist does not contain any media entries."
            }

            val byExactUri = library.associateBy {
                it.uri.toString()
            }
            val byDecodedUri = library.associateBy {
                Uri.decode(it.uri.toString())
            }
            val byTitle = library
                .groupBy { normalizeTitle(it.title) }

            val resolved = mutableListOf<Long>()
            val unresolved = mutableListOf<String>()

            entries.forEach { entry ->
                val decoded = Uri.decode(entry)
                val exact = byExactUri[entry]
                    ?: byDecodedUri[decoded]

                val fallbackTitle = entry
                    .substringAfterLast('/')
                    .substringAfterLast('\\')
                    .substringBeforeLast('.')
                    .takeIf { it.isNotBlank() }
                    ?.let(::normalizeTitle)
                    ?.let(byTitle::get)
                    ?.singleOrNull()

                val track = exact ?: fallbackTitle
                if (track == null) {
                    unresolved += entry
                } else {
                    resolved += track.id
                }
            }

            M3uImportResult(
                name = sourceDisplayName(source)
                    ?.substringBeforeLast('.')
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Imported Playlist",
                trackIds = resolved.distinct(),
                unresolvedEntries = unresolved,
            )
        }
    }

    suspend fun exportPlaylist(
        destination: Uri,
        tracks: List<Track>,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val text = buildString {
                appendLine("#EXTM3U")
                tracks.forEach { track ->
                    val seconds = if (track.durationMs > 0L) {
                        track.durationMs / 1_000L
                    } else {
                        -1L
                    }
                    append("#EXTINF:")
                    append(seconds)
                    append(',')
                    append(track.artist.replace("\n", " "))
                    append(" - ")
                    appendLine(track.title.replace("\n", " "))
                    appendLine(track.uri.toString())
                }
            }

            context.contentResolver.openOutputStream(
                destination,
                "wt",
            )?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                writer.write(text)
            } ?: error("Could not open the selected playlist destination.")
        }
    }

    private fun sourceDisplayName(
        source: Uri,
    ): String? =
        runCatching {
            context.contentResolver.query(
                source,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME,
                )
                if (index >= 0) cursor.getString(index) else null
            }
        }.getOrNull()

    companion object {
        private const val MaxPlaylistBytes = 2 * 1024 * 1024

        fun parseEntries(
            raw: String,
        ): List<String> =
            raw
                .removePrefix("\uFEFF")
                .lineSequence()
                .map(String::trim)
                .filter { line ->
                    line.isNotBlank() &&
                        !line.startsWith('#')
                }
                .toList()

        private fun normalizeTitle(
            value: String,
        ): String =
            value
                .trim()
                .lowercase()
                .replace(Regex("\\s+"), " ")
    }
}
