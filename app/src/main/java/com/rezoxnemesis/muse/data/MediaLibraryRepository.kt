package com.rezoxnemesis.muse.data

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaLibraryRepository(
    private val context: Context,
) {
    suspend fun loadTracks(): List<Track> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.DATE_ADDED)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.SIZE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.MediaColumns.ALBUM_ARTIST)
                add(MediaStore.Audio.AudioColumns.GENRE)
                add(MediaStore.MediaColumns.BITRATE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(MediaStore.Audio.AudioColumns.SAMPLERATE)
            }
        }.toTypedArray()
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        buildList {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder,
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val albumArtistColumn = cursor.getColumnIndex("album_artist")
                val genreColumn = cursor.getColumnIndex("genre")
                val bitrateColumn = cursor.getColumnIndex("bitrate")
                val sampleRateColumn = cursor.getColumnIndex("samplerate")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unknown title"
                    val artist = cursor.getString(artistColumn)
                        ?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                        ?: "Unknown artist"
                    val album = cursor.getString(albumColumn)
                        ?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                        ?: "Unknown album"
                    val albumId = cursor.getLong(albumIdColumn).takeIf { it > 0L }
                    val duration = cursor.getLong(durationColumn).coerceAtLeast(0L)
                    val dateAdded = cursor.getLong(dateAddedColumn).coerceAtLeast(0L)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val trackNumber = cursor.getInt(trackColumn).takeIf { it > 0 }
                    val year = cursor.getInt(yearColumn).takeIf { it > 0 }
                    val sizeBytes = cursor.getLong(sizeColumn).takeIf { it > 0L }
                    val albumArtist = albumArtistColumn
                        .takeIf { it >= 0 }
                        ?.let(cursor::getString)
                        ?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                    val genre = genreColumn
                        .takeIf { it >= 0 }
                        ?.let(cursor::getString)
                        ?.takeIf { it.isNotBlank() }
                    val bitrateBps = bitrateColumn
                        .takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let(cursor::getInt)
                        ?.takeIf { it > 0 }
                    val sampleRateHz = sampleRateColumn
                        .takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let(cursor::getInt)
                        ?.takeIf { it > 0 }

                    add(
                        Track(
                            id = id,
                            uri = ContentUris.withAppendedId(collection, id),
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
}
