package com.rezoxnemesis.muse.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.ui.theme.MuseBorder
import com.rezoxnemesis.muse.ui.theme.MuseGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object ArtworkMemoryCache : LruCache<Long, Bitmap>(12 * 1024) {
    override fun sizeOf(key: Long, value: Bitmap): Int =
        value.allocationByteCount / 1024
}

@Composable
fun TrackArtwork(
    track: Track?,
    modifier: Modifier,
    contentDescription: String? = track?.let { "${it.title} album artwork" },
    shape: Shape = RoundedCornerShape(18.dp),
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = track?.id) {
        value = track?.let { loadArtwork(context, it) }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFF173B20),
                            androidx.compose.ui.graphics.Color(0xFF07150B),
                            androidx.compose.ui.graphics.Color(0xFF285B2C),
                        )
                    )
                )
                .border(1.dp, MuseBorder, shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MuseGreen,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

private suspend fun loadArtwork(
    context: Context,
    track: Track,
): Bitmap? = withContext(Dispatchers.IO) {
    ArtworkMemoryCache.get(track.id)?.let { return@withContext it }

    val bitmap = loadMediaStoreThumbnail(context, track)
        ?: loadEmbeddedArtwork(context, track)

    if (bitmap != null) {
        ArtworkMemoryCache.put(track.id, bitmap)
    }
    bitmap
}

private fun loadMediaStoreThumbnail(
    context: Context,
    track: Track,
): Bitmap? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    return runCatching {
        context.contentResolver.loadThumbnail(
            track.uri,
            Size(ArtworkSizePx, ArtworkSizePx),
            null,
        )
    }.getOrNull()
}

private fun loadEmbeddedArtwork(
    context: Context,
    track: Track,
): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, track.uri)
        val bytes = retriever.embeddedPicture ?: return null
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: RuntimeException) {
        null
    } finally {
        runCatching { retriever.release() }
    }
}

private const val ArtworkSizePx = 768
