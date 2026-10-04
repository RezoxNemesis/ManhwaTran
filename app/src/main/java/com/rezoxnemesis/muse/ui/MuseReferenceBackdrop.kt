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
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

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
    val assetEntryName: String,
) {
    Splash("muse_ref_splash.webp"),
    Home("muse_ref_home.webp"),
    NowPlaying("muse_ref_now_playing.webp"),
    Lyrics("muse_ref_lyrics.webp"),
    Queue("muse_ref_queue.webp"),
    Explore("muse_ref_explore.webp"),
    Library("muse_ref_library.webp"),
    Playlists("muse_ref_playlists.webp"),
    Equalizer("muse_ref_equalizer.webp"),
    Settings("muse_ref_settings.webp"),
    Artist("muse_ref_artist.webp"),
    Album("muse_ref_album.webp"),
    Downloads("muse_ref_downloads.webp"),
    SleepTimer("muse_ref_sleep.webp"),
    MoreOptions("muse_ref_more.webp");

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
 * The approved 15 Muse screens are packaged at high resolution and rendered as
 * the screen-specific visual foundation. They preserve the selected wet-leaf,
 * green-black glass, bokeh, glow and blur treatment instead of approximating it.
 *
 * Native Compose content remains above the reference layer so playback, library,
 * navigation and accessibility stay real. Only narrow top/bottom safety veils
 * protect Android system chrome and long dynamic metadata; the central artwork
 * remains intentionally vivid.
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

            // Keep the selected images visibly intact. Only protect the extreme
            // top/bottom where the source mockups contain device chrome and where
            // Android system/navigation UI may overlap.
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
    private const val ArchivePrefix = "muse_reference_assets.zip.b64."

    private val bitmapCache = object : LruCache<String, Bitmap>(28 * 1024) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int = value.byteCount / 1024
    }

    private var archiveBytes: ByteArray? = null

    @Synchronized
    fun peek(screen: MuseReferenceScreen): Bitmap? =
        bitmapCache.get(screen.assetEntryName)

    @Synchronized
    fun load(
        context: android.content.Context,
        screen: MuseReferenceScreen,
    ): Bitmap? {
        bitmapCache.get(screen.assetEntryName)?.let { return it }

        val archive = archiveBytes ?: decodeArchive(context)?.also {
            archiveBytes = it
        } ?: return null

        val encodedImage = findZipEntry(
            archive = archive,
            entryName = screen.assetEntryName,
        ) ?: return null

        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(
            encodedImage,
            0,
            encodedImage.size,
            options,
        ) ?: return null

        bitmapCache.put(screen.assetEntryName, bitmap)
        return bitmap
    }

    private fun decodeArchive(
        context: android.content.Context,
    ): ByteArray? {
        val chunkNames = runCatching {
            context.assets
                .list(AssetDirectory)
                ?.filter { it.startsWith(ArchivePrefix) }
                ?.sorted()
                .orEmpty()
        }.getOrDefault(emptyList())

        if (chunkNames.isEmpty()) return null

        val encoded = buildString {
            chunkNames.forEach { name ->
                val chunk = runCatching {
                    context.assets
                        .open("$AssetDirectory/$name")
                        .bufferedReader(Charsets.US_ASCII)
                        .use { it.readText() }
                }.getOrNull() ?: return null
                append(chunk)
            }
        }

        return runCatching {
            Base64.decode(encoded, Base64.DEFAULT)
        }.getOrNull()
    }

    private fun findZipEntry(
        archive: ByteArray,
        entryName: String,
    ): ByteArray? =
        runCatching {
            ZipInputStream(ByteArrayInputStream(archive)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && entry.name == entryName) {
                        return@runCatching zip.readBytes()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                null
            }
        }.getOrNull()
}
