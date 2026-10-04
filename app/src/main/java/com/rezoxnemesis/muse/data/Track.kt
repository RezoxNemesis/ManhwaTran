package com.rezoxnemesis.muse.data

import android.net.Uri

data class Track(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long?,
    val durationMs: Long,
    val dateAddedSeconds: Long,
    val mimeType: String?,
    val trackNumber: Int?,
    val year: Int?,
    val sizeBytes: Long? = null,
    val managedByMuse: Boolean = false,
)
