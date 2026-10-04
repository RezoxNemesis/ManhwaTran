package com.rezoxnemesis.muse.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
 * The approved 15 Muse mockups are packaged as a compact atlas and used as
 * screen-specific photographic atmosphere. The atlas cells are intentionally
 * softened/darkened derivatives of the exact approved images, so they preserve
 * the wet-leaf, forest-light, album-art and bokeh character without leaving
 * readable example text underneath the live interface.
 *
 * Every label, icon, control, song title and action above this layer remains a
 * native Compose element backed by real Muse state. The artwork never acts as a
 * fake button or a screenshot pretending to be interactive UI.
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
            val verticalScrim = when (intensity) {
                MuseVisualIntensity.Calm -> listOf(0.42f, 0.55f, 0.47f, 0.60f)
                MuseVisualIntensity.Balanced -> listOf(0.29f, 0.39f, 0.32f, 0.43f)
                MuseVisualIntensity.Vivid -> listOf(0.20f, 0.29f, 0.23f, 0.34f)
            }
            val centerScrim = when (intensity) {
                MuseVisualIntensity.Calm -> 0.34f
                MuseVisualIntensity.Balanced -> 0.26f
                MuseVisualIntensity.Vivid -> 0.18f
            }

            Image(
                bitmap = activeBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Layered text-safety veil: enough contrast for long/local metadata
            // while allowing bright botanical highlights to remain visible.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF010503).copy(alpha = verticalScrim[0]),
                                Color(0xFF010503).copy(alpha = verticalScrim[1]),
                                Color(0xFF010503).copy(alpha = verticalScrim[2]),
                                Color(0xFF010302).copy(alpha = verticalScrim[3]),
                            )
                        )
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = centerScrim * 0.34f),
                                Color(0xFF020A05).copy(alpha = centerScrim),
                                Color.Black.copy(alpha = centerScrim * 0.34f),
                            )
                        )
                    ),
            )
        }
    }
}

private object MuseReferenceBackdropLoader {
    private const val AssetPath =
        "muse_reference/muse_reference_atlas.webp.b64"
    private const val AtlasColumns = 5
    private const val AtlasRows = 3

    private val bitmapCache = object : LruCache<String, Bitmap>(20 * 1024) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int = value.byteCount / 1024
    }

    private var atlas: Bitmap? = null

    @Synchronized
    fun peek(screen: MuseReferenceScreen): Bitmap? =
        bitmapCache.get(screen.name)

    @Synchronized
    fun load(
        context: android.content.Context,
        screen: MuseReferenceScreen,
    ): Bitmap? {
        bitmapCache.get(screen.name)?.let { return it }

        val activeAtlas = atlas ?: decodeAtlas(context)?.also {
            atlas = it
        } ?: return null

        val cellWidth = activeAtlas.width / AtlasColumns
        val cellHeight = activeAtlas.height / AtlasRows
        if (cellWidth <= 0 || cellHeight <= 0) return null

        val column = screen.atlasIndex % AtlasColumns
        val row = screen.atlasIndex / AtlasColumns
        if (row !in 0 until AtlasRows) return null

        val bitmap = runCatching {
            Bitmap.createBitmap(
                activeAtlas,
                column * cellWidth,
                row * cellHeight,
                cellWidth,
                cellHeight,
            )
        }.getOrNull() ?: return null

        bitmapCache.put(screen.name, bitmap)
        return bitmap
    }

    private fun decodeAtlas(
        context: android.content.Context,
    ): Bitmap? {
        val encoded = runCatching {
            context.assets
                .open(AssetPath)
                .bufferedReader(Charsets.US_ASCII)
                .use { it.readText() }
        }.getOrNull() ?: return null

        val bytes = runCatching {
            Base64.decode(encoded, Base64.DEFAULT)
        }.getOrNull() ?: return null

        return BitmapFactory.decodeByteArray(
            bytes,
            0,
            bytes.size,
        )
    }
}
