package com.rezoxnemesis.muse.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.util.Base64
import android.util.Base64InputStream
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.SequenceInputStream
import java.util.Collections

internal enum class MuseVisualIntensity(
    val storedValue: String,
    val label: String,
) {
    Calm("calm", "Calm"),
    Balanced("balanced", "Balanced"),
    Vivid("vivid", "Vivid");

    fun next(): MuseVisualIntensity =
        when (this) {
            Calm -> Balanced
            Balanced -> Vivid
            Vivid -> Calm
        }

    companion object {
        fun fromStored(value: String?): MuseVisualIntensity =
            entries.firstOrNull { it.storedValue == value }
                ?: Balanced
    }
}

internal enum class MuseReferenceScreen(
    val atlasIndex: Int,
) {
    Splash(0),
    Home(1),
    NowPlaying(2),
    Lyrics(3),
    Queue(4),
    Explore(5),
    Library(6),
    Playlists(7),
    Equalizer(8),
    Settings(9),
    Artist(10),
    Album(11),
    Downloads(12),
    SleepTimer(13),
    MoreOptions(14);

    companion object {
        fun fromRoute(route: String?): MuseReferenceScreen =
            when (route) {
                "home" -> Home
                "explore" -> Explore
                "library", "liked", "recent" -> Library
                "equalizer" -> Equalizer
                "nowPlaying" -> NowPlaying
                "queue" -> Queue
                "lyrics" -> Lyrics
                "playlists", "playlistDetail" -> Playlists
                "artist" -> Artist
                "album" -> Album
                "downloads" -> Downloads
                "sleep" -> SleepTimer
                "settings", "diagnostics", "tools" -> Settings
                "more" -> MoreOptions
                else -> Home
            }
    }
}

/**
 * The approved 15 Muse screens are stored at their original 941 x 1672 pixel
 * dimensions inside one lossless 5 x 3 WebP atlas. The atlas is lossless, so
 * wet-leaf detail, bokeh, glass blur, glow and colour values survive packaging
 * without visual recompression.
 *
 * Native Compose content remains above this visual foundation so Muse keeps
 * real playback, library, navigation and accessibility behavior. Only narrow
 * Android system-chrome safety veils are applied over the source visuals.
 */
@Composable
internal fun MuseReferenceBackdrop(
    screen: MuseReferenceScreen,
    intensity: MuseVisualIntensity = MuseVisualIntensity.Balanced,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = MuseReferenceBackdropLoader.peek(screen),
        key1 = screen,
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                MuseReferenceBackdropLoader.load(
                    context = context.applicationContext,
                    screen = screen,
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MuseBackground),
    ) {
        val activeBitmap = bitmap
        if (activeBitmap == null) {
            MuseAtmosphere()
        } else {
            val centerVeil = when (intensity) {
                MuseVisualIntensity.Calm -> 0.18f
                MuseVisualIntensity.Balanced -> 0.10f
                MuseVisualIntensity.Vivid -> 0.04f
            }

            Image(
                bitmap = activeBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Black.copy(alpha = 0.72f),
                                0.07f to Color.Black.copy(alpha = 0.28f),
                                0.16f to Color.Transparent,
                                0.78f to Color.Transparent,
                                0.94f to Color.Black.copy(alpha = 0.16f),
                                1.00f to Color.Black.copy(alpha = 0.58f),
                            )
                        )
                    ),
            )
            if (centerVeil > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color(0xFF010503).copy(alpha = centerVeil)
                        ),
                )
            }
        }
    }
}

private object MuseReferenceBackdropLoader {
    private const val AssetDirectory = "muse_reference"
    private const val AtlasChunkPrefix = "muse_reference_atlas_lossless.b64."
    private const val AtlasChunkCount = 28
    private const val AtlasColumns = 5
    private const val AtlasRows = 3
    private const val CellWidth = 941
    private const val CellHeight = 1672
    private const val AtlasWidth = CellWidth * AtlasColumns
    private const val AtlasHeight = CellHeight * AtlasRows

    private val bitmapCache = object : LruCache<String, Bitmap>(32 * 1024) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int = value.byteCount / 1024
    }

    private var compressedAtlas: ByteArray? = null

    @Synchronized
    fun peek(screen: MuseReferenceScreen): Bitmap? =
        bitmapCache.get(screen.name)

    @Synchronized
    fun load(
        context: android.content.Context,
        screen: MuseReferenceScreen,
    ): Bitmap? {
        bitmapCache.get(screen.name)?.let { return it }

        val atlas = compressedAtlas ?: decodeAtlas(context)?.also {
            compressedAtlas = it
        } ?: return null

        val decoder = runCatching {
            BitmapRegionDecoder.newInstance(
                atlas,
                0,
                atlas.size,
                false,
            )
        }.getOrNull() ?: return null

        val bitmap = try {
            if (decoder.width != AtlasWidth || decoder.height != AtlasHeight) {
                return null
            }

            val column = screen.atlasIndex % AtlasColumns
            val row = screen.atlasIndex / AtlasColumns
            if (row !in 0 until AtlasRows) return null

            decoder.decodeRegion(
                Rect(
                    column * CellWidth,
                    row * CellHeight,
                    (column + 1) * CellWidth,
                    (row + 1) * CellHeight,
                ),
                BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                },
            )
        } finally {
            decoder.recycle()
        } ?: return null

        bitmapCache.put(screen.name, bitmap)
        return bitmap
    }

    private fun decodeAtlas(
        context: android.content.Context,
    ): ByteArray? {
        val chunkNames = runCatching {
            context.assets
                .list(AssetDirectory)
                ?.filter { it.startsWith(AtlasChunkPrefix) }
                ?.sorted()
                .orEmpty()
        }.getOrDefault(emptyList())

        if (chunkNames.size != AtlasChunkCount) return null

        return runCatching {
            val streams = chunkNames.map { name ->
                context.assets.open("$AssetDirectory/$name")
            }
            SequenceInputStream(Collections.enumeration(streams)).use { encoded ->
                Base64InputStream(encoded, Base64.DEFAULT).use { decoded ->
                    decoded.readBytes()
                }
            }
        }.getOrNull()
    }
}
