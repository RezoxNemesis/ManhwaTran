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
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class MuseReferenceScreen(
    val assetEntry: String,
) {
    Splash("01_splash_screen.webp"),
    Home("02_home_screen.webp"),
    NowPlaying("03_now_playing.webp"),
    Lyrics("04_lyrics.webp"),
    Queue("05_play_queue.webp"),
    Explore("06_explore.webp"),
    Library("07_library.webp"),
    Playlists("08_playlists.webp"),
    Equalizer("09_equalizer.webp"),
    Settings("10_settings.webp"),
    Artist("11_artist_page.webp"),
    Album("12_album_page.webp"),
    Downloads("13_downloads.webp"),
    SleepTimer("14_sleep_timer.webp"),
    MoreOptions("15_more_options.webp");

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
 * Uses the approved Muse screen art as a photographic atmosphere layer, never
 * as a fake UI. The packaged derivatives deliberately remove readable mock
 * content while retaining the wet-leaf, forest-light and bokeh composition.
 *
 * All labels, icons, data and touch targets above this layer remain native
 * Compose components backed by real Muse state.
 */
@Composable
internal fun MuseReferenceBackdrop(
    screen: MuseReferenceScreen,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = MuseReferenceBackdropLoader.peek(screen.assetEntry),
        key1 = screen,
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                MuseReferenceBackdropLoader.load(
                    context = context.applicationContext,
                    entryName = screen.assetEntry,
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
            Image(
                bitmap = activeBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Text-safety veil: preserves edge foliage and light depth while
            // guaranteeing native labels remain readable over every screen.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x3D010503),
                                Color(0x62010503),
                                Color(0x4A010503),
                                Color(0x68010302),
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
                                Color(0x14000000),
                                Color(0x3A020A05),
                                Color(0x14000000),
                            )
                        )
                    ),
            )
        }
    }
}

private object MuseReferenceBackdropLoader {
    private const val AssetDirectory = "muse_reference"
    private const val PartPrefix = "muse_backdrops.part"

    private val bitmapCache = object : LruCache<String, Bitmap>(20 * 1024) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int = value.byteCount / 1024
    }

    @Synchronized
    fun peek(entryName: String): Bitmap? =
        bitmapCache.get(entryName)

    @Synchronized
    fun load(
        context: android.content.Context,
        entryName: String,
    ): Bitmap? {
        bitmapCache.get(entryName)?.let { return it }

        val partNames = runCatching {
            context.assets
                .list(AssetDirectory)
                .orEmpty()
                .filter { it.startsWith(PartPrefix) }
                .sorted()
        }.getOrDefault(emptyList())

        if (partNames.isEmpty()) return null

        val encoded = buildString {
            partNames.forEach { name ->
                runCatching {
                    context.assets
                        .open("$AssetDirectory/$name")
                        .bufferedReader(Charsets.US_ASCII)
                        .use { reader -> append(reader.readText()) }
                }.getOrElse { return null }
            }
        }

        val archive = runCatching {
            Base64.decode(encoded, Base64.DEFAULT)
        }.getOrNull() ?: return null

        ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (!entry.isDirectory && entry.name.substringAfterLast('/') == entryName) {
                    val bytes = zip.readBytes()
                    val bitmap = BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size,
                    ) ?: return null
                    bitmapCache.put(entryName, bitmap)
                    return bitmap
                }
                zip.closeEntry()
            }
        }

        return null
    }
}
