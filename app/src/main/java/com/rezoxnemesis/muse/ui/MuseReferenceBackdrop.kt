package com.rezoxnemesis.muse.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal enum class MuseReferenceScreen(
    val assetName: String,
) {
    Splash("splash"),
    Home("home"),
    NowPlaying("now_playing"),
    Lyrics("lyrics"),
    Queue("queue"),
    Explore("explore"),
    Library("library"),
    Playlists("playlists"),
    Equalizer("equalizer"),
    Settings("settings"),
    Artist("artist"),
    Album("album"),
    Downloads("downloads"),
    SleepTimer("sleep_timer"),
    MoreOptions("more_options");

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
 * Approved Muse artwork is the sole visible surface for each route.
 * The build materializes a repaired atlas into independent PNGs so the
 * large-WebP region-decoder corruption seen on device cannot recur.
 */
@Composable
internal fun MuseExactReferenceSurface(
    screen: MuseReferenceScreen,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(
        initialValue = MuseReferenceScreenLoader.peek(screen),
        key1 = screen,
    ) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                MuseReferenceScreenLoader.load(
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
        bitmap?.let { activeBitmap ->
            Image(
                bitmap = activeBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private object MuseReferenceScreenLoader {
    private const val AssetDirectory = "muse_reference"

    private val bitmapCache = object : LruCache<String, Bitmap>(48 * 1024) {
        override fun sizeOf(
            key: String,
            value: Bitmap,
        ): Int = value.byteCount / 1024
    }

    @Synchronized
    fun peek(screen: MuseReferenceScreen): Bitmap? =
        bitmapCache.get(screen.name)

    @Synchronized
    fun load(
        context: android.content.Context,
        screen: MuseReferenceScreen,
    ): Bitmap? {
        bitmapCache.get(screen.name)?.let { return it }

        val bitmap = runCatching {
            context.assets
                .open("$AssetDirectory/screen_${screen.assetName}.png")
                .use { stream ->
                    BitmapFactory.decodeStream(
                        stream,
                        null,
                        BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        },
                    )
                }
        }.getOrNull() ?: return null

        bitmapCache.put(screen.name, bitmap)
        return bitmap
    }
}
