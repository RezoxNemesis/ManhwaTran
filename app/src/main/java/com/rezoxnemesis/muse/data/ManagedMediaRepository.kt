package com.rezoxnemesis.muse.data

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ManagedMediaRepository(
    private val context: Context,
) {
    suspend fun inspectAndPersist(uri: Uri): Track = withContext(Dispatchers.IO) {
        takeReadPermission(uri)

        val resolver = context.contentResolver
        val displayInfo = queryDisplayInfo(resolver, uri)
        val metadata = readMetadata(uri)

        val title = metadata.title
            ?.takeIf { it.isNotBlank() }
            ?: displayInfo.displayName
                ?.substringBeforeLast('.', missingDelimiterValue = displayInfo.displayName)
                ?.takeIf { it.isNotBlank() }
            ?: "Imported audio"

        Track(
            id = stableManagedId(uri),
            uri = uri,
            title = title,
            artist = metadata.artist?.takeIf { it.isNotBlank() } ?: "Unknown artist",
            album = metadata.album?.takeIf { it.isNotBlank() } ?: "Imported",
            albumId = null,
            durationMs = metadata.durationMs.coerceAtLeast(0L),
            dateAddedSeconds = System.currentTimeMillis() / 1_000L,
            mimeType = resolver.getType(uri),
            trackNumber = metadata.trackNumber,
            year = metadata.year,
            sizeBytes = displayInfo.sizeBytes,
            managedByMuse = true,
            albumArtist = metadata.albumArtist,
            genre = metadata.genre,
            bitrateBps = metadata.bitrateBps,
            sampleRateHz = metadata.sampleRateHz,
        )
    }

    suspend fun releasePersistedAccess(uri: Uri) = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    private fun takeReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    private fun queryDisplayInfo(
        resolver: ContentResolver,
        uri: Uri,
    ): DisplayInfo {
        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE,
        )
        return runCatching {
            resolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use DisplayInfo()

                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                DisplayInfo(
                    displayName = nameIndex
                        .takeIf { it >= 0 }
                        ?.let(cursor::getString),
                    sizeBytes = sizeIndex
                        .takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let(cursor::getLong)
                        ?.takeIf { it > 0L },
                )
            } ?: DisplayInfo()
        }.getOrDefault(DisplayInfo())
    }

    private fun readMetadata(uri: Uri): ImportedMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            ImportedMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                albumArtist = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
                ),
                genre = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_GENRE,
                ),
                bitrateBps = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toIntOrNull()
                    ?.takeIf { it > 0 },
                sampleRateHz = if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ) {
                    retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
                        ?.toIntOrNull()
                        ?.takeIf { it > 0 }
                } else {
                    null
                },
                durationMs = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull()
                    ?: 0L,
                trackNumber = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                    ?.substringBefore('/')
                    ?.toIntOrNull(),
                year = retriever
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                    ?.toIntOrNull(),
            )
        } catch (error: RuntimeException) {
            throw IllegalArgumentException(
                "The selected file could not be read as supported audio.",
                error,
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun stableManagedId(uri: Uri): Long {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(uri.toString().toByteArray(Charsets.UTF_8))
        val candidate = ByteBuffer.wrap(digest.copyOfRange(0, Long.SIZE_BYTES)).long
        return candidate or Long.MIN_VALUE
    }

    private data class DisplayInfo(
        val displayName: String? = null,
        val sizeBytes: Long? = null,
    )

    private data class ImportedMetadata(
        val title: String?,
        val artist: String?,
        val album: String?,
        val albumArtist: String?,
        val genre: String?,
        val bitrateBps: Int?,
        val sampleRateHz: Int?,
        val durationMs: Long,
        val trackNumber: Int?,
        val year: Int?,
    )
}
