package com.rezoxnemesis.muse.data

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaLibraryRepository(
    private val context: Context,
) {
    suspend fun loadTracks(): List<Track> = withContext(Dispatchers.IO) {
        val baseProjection = baseProjection()
        val enhancedProjection = enhancedProjection(baseProjection)

        if (enhancedProjection.contentEquals(baseProjection)) {
            return@withContext queryTracks(baseProjection)
        }

        try {
            queryTracks(enhancedProjection)
        } catch (error: IllegalArgumentException) {
            // Some OEM MediaStore providers omit optional metadata columns even
            // when the platform API exposes them. Core library discovery must
            // still work, so retry with the portable projection.
            queryTracks(baseProjection)
        }
    }

    private fun queryTracks(
        projection: Array<String>,
    ): List<Track> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        return buildList {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder,
            )?.use { cursor ->
                val columns = Columns(cursor)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(columns.id)
                    val title = cursor.getString(columns.title)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unknown title"
                    val artist = cursor.getString(columns.artist)
                        ?.takeIf {
                            it.isNotBlank() &&
                                it != MediaStore.UNKNOWN_STRING
                        }
                        ?: "Unknown artist"
                    val album = cursor.getString(columns.album)
                        ?.takeIf {
                            it.isNotBlank() &&
                                it != MediaStore.UNKNOWN_STRING
                        }
                        ?: "Unknown album"
                    val albumId = cursor
                        .getLong(columns.albumId)
                        .takeIf { it > 0L }
                    val duration = cursor
                        .getLong(columns.duration)
                        .coerceAtLeast(0L)
                    val dateAdded = cursor
                        .getLong(columns.dateAdded)
                        .coerceAtLeast(0L)
                    val mimeType = cursor.getString(columns.mimeType)
                    val trackNumber = cursor
                        .getInt(columns.track)
                        .takeIf { it > 0 }
                    val year = cursor
                        .getInt(columns.year)
                        .takeIf { it > 0 }
                    val sizeBytes = cursor
                        .getLong(columns.size)
                        .takeIf { it > 0L }

                    val albumArtist = cursor.optionalString(
                        columns.albumArtist,
                    )?.takeIf {
                        it.isNotBlank() &&
                            it != MediaStore.UNKNOWN_STRING
                    }
                    val genre = cursor.optionalString(
                        columns.genre,
                    )?.takeIf { it.isNotBlank() }
                    val bitrateBps = cursor.optionalInt(
                        columns.bitrate,
                    )?.takeIf { it > 0 }
                    val sampleRateHz = cursor.optionalInt(
                        columns.sampleRate,
                    )?.takeIf { it > 0 }

                    add(
                        Track(
                            id = id,
                            uri = ContentUris.withAppendedId(
                                collection,
                                id,
                            ),
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            durationMs = duration,
                            dateAddedSeconds = dateAdded,
                            mimeType = mimeType,
                            trackNumber = trackNumber,
                            year = year,
                            sizeBytes = sizeBytes,
                            albumArtist = albumArtist,
                            genre = genre,
                            bitrateBps = bitrateBps,
                            sampleRateHz = sampleRateHz,
                        )
                    )
                }
            }
        }
    }

    private fun baseProjection(): Array<String> =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.SIZE,
        )

    private fun enhancedProjection(
        base: Array<String>,
    ): Array<String> =
        buildList {
            addAll(base)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.MediaColumns.ALBUM_ARTIST)
                add(MediaStore.Audio.AudioColumns.GENRE)
                add(MediaStore.MediaColumns.BITRATE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(MediaStore.Audio.AudioColumns.SAMPLERATE)
            }
        }
            .distinct()
            .toTypedArray()

    private class Columns(
        cursor: Cursor,
    ) {
        val id = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media._ID,
        )
        val title = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.TITLE,
        )
        val artist = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ARTIST,
        )
        val album = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ALBUM,
        )
        val albumId = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.ALBUM_ID,
        )
        val duration = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DURATION,
        )
        val dateAdded = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DATE_ADDED,
        )
        val mimeType = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.MIME_TYPE,
        )
        val track = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.TRACK,
        )
        val year = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.YEAR,
        )
        val size = cursor.getColumnIndexOrThrow(
            MediaStore.Audio.Media.SIZE,
        )

        val albumArtist = cursor.getColumnIndex("album_artist")
        val genre = cursor.getColumnIndex("genre")
        val bitrate = cursor.getColumnIndex("bitrate")
        val sampleRate = cursor.getColumnIndex("samplerate")
    }

    private fun Cursor.optionalString(
        index: Int,
    ): String? =
        index
            .takeIf { it >= 0 && !isNull(it) }
            ?.let(::getString)

    private fun Cursor.optionalInt(
        index: Int,
    ): Int? =
        index
            .takeIf { it >= 0 && !isNull(it) }
            ?.let(::getInt)
}
