package com.rezoxnemesis.muse.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rezoxnemesis.muse.MuseViewModel
import com.rezoxnemesis.muse.data.Track
import kotlinx.coroutines.delay

/**
 * Pixel-first visual shell for Muse.
 *
 * The approved botanical/glass reference art is the only visible layout surface.
 * All of the former Compose cards/headers/navigation chrome remain unreachable from
 * MainActivity, so there is no second UI hierarchy to overlap the design.
 *
 * Interaction is restored through transparent, accessibility-labelled hit regions
 * that call the real Muse playback/library/navigation operations.
 */
@Composable
fun MuseExactVisualApp(
    viewModel: MuseViewModel,
    requestedRoute: String? = null,
    onRouteHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // The Android 12+ system splash already covers process startup. Keep the
    // branded Muse artwork only for a short cold-launch handoff, and skip it
    // entirely for direct/deep-route launches used by widgets, notifications
    // and runtime verification.
    var showSplash by rememberSaveable {
        mutableStateOf(requestedRoute == null)
    }

    LaunchedEffect(showSplash) {
        if (showSplash) {
            delay(550)
            showSplash = false
        }
    }

    LaunchedEffect(requestedRoute) {
        val route = requestedRoute ?: return@LaunchedEffect
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }
        onRouteHandled()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("MuseRoot")
            .background(Color.Black),
    ) {
        if (showSplash) {
            MuseExactReferenceSurface(
                screen = MuseReferenceScreen.Splash,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            ExactNavHost(
                viewModel = viewModel,
                navController = navController,
            )
        }
    }
}

@Composable
private fun ExactNavHost(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("home") {
            ExactHome(viewModel, navController)
        }
        composable("explore") {
            ExactExplore(viewModel, navController)
        }
        composable("library") {
            ExactLibrary(viewModel, navController)
        }
        composable("equalizer") {
            ExactEqualizer(viewModel, navController)
        }
        composable("tools") {
            ExactTools(navController)
        }
        composable("nowPlaying") {
            ExactNowPlaying(viewModel, navController)
        }
        composable("queue") {
            ExactQueue(viewModel, navController)
        }
        composable("lyrics") {
            ExactStaticDetail(MuseReferenceScreen.Lyrics, navController)
        }
        composable("liked") {
            ExactLibrary(viewModel, navController)
        }
        composable("recent") {
            ExactLibrary(viewModel, navController)
        }
        composable("playlists") {
            ExactPlaylists(viewModel, navController)
        }
        composable("playlistDetail") {
            ExactPlaylists(viewModel, navController)
        }
        composable("artist") {
            ExactArtist(viewModel, navController)
        }
        composable("album") {
            ExactAlbum(viewModel, navController)
        }
        composable("downloads") {
            ExactDownloads(viewModel, navController)
        }
        composable("sleep") {
            ExactSleepTimer(viewModel, navController)
        }
        composable("settings") {
            ExactSettings(navController)
        }
        composable("diagnostics") {
            ExactSettings(navController)
        }
        composable("more") {
            ExactMoreOptions(viewModel, navController)
        }
    }
}

private data class ExactRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

@Composable
private fun InvisibleHotspot(
    rect: ExactRect,
    label: String,
    onClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * rect.x,
                    y = maxHeight * rect.y,
                )
                .size(
                    width = maxWidth * rect.width,
                    height = maxHeight * rect.height,
                )
                .semantics {
                    contentDescription = label
                    role = Role.Button
                }
                .clickable(onClick = onClick),
        )
    }
}

@Composable
private fun ExactSurface(
    screen: MuseReferenceScreen,
    hotspots: @Composable () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("MuseExactVisualRoot"),
    ) {
        MuseExactReferenceSurface(
            screen = screen,
            modifier = Modifier.fillMaxSize(),
        )
        hotspots()
    }
}

@Composable
private fun MainNavHotspots(
    navController: NavHostController,
) {
    val y = 0.915f
    val h = 0.085f
    val w = 0.20f
    InvisibleHotspot(ExactRect(0.00f, y, w, h), "Home") {
        navController.navigate("home") { launchSingleTop = true }
    }
    InvisibleHotspot(ExactRect(0.20f, y, w, h), "Explore") {
        navController.navigate("explore") { launchSingleTop = true }
    }
    InvisibleHotspot(ExactRect(0.40f, y, w, h), "Library") {
        navController.navigate("library") { launchSingleTop = true }
    }
    InvisibleHotspot(ExactRect(0.60f, y, w, h), "Equalizer") {
        navController.navigate("equalizer") { launchSingleTop = true }
    }
    InvisibleHotspot(ExactRect(0.80f, y, w, h), "Muse Lab") {
        navController.navigate("tools") { launchSingleTop = true }
    }
}

@Composable
private fun BackHotspot(
    navController: NavHostController,
) {
    InvisibleHotspot(
        ExactRect(0.00f, 0.00f, 0.18f, 0.11f),
        "Back",
    ) {
        navController.popBackStack()
    }
}

@Composable
private fun ExactHome(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    ExactSurface(MuseReferenceScreen.Home) {
        InvisibleHotspot(ExactRect(0.05f, 0.24f, 0.43f, 0.13f), "Liked songs") {
            navController.navigate("liked")
        }
        InvisibleHotspot(ExactRect(0.52f, 0.24f, 0.43f, 0.13f), "Playlists") {
            navController.navigate("playlists")
        }
        InvisibleHotspot(ExactRect(0.05f, 0.38f, 0.43f, 0.13f), "Downloads") {
            navController.navigate("downloads")
        }
        InvisibleHotspot(ExactRect(0.52f, 0.38f, 0.43f, 0.13f), "Recently played") {
            navController.navigate("recent")
        }
        library.tracks.take(4).forEachIndexed { index, track ->
            InvisibleHotspot(
                ExactRect(0.04f + index * 0.235f, 0.57f, 0.22f, 0.20f),
                "Play ${track.title}",
            ) {
                viewModel.playTrack(track)
                navController.navigate("nowPlaying")
            }
        }
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactExplore(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    ExactSurface(MuseReferenceScreen.Explore) {
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactLibrary(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    ExactSurface(MuseReferenceScreen.Library) {
        library.tracks.take(6).forEachIndexed { index, track ->
            InvisibleHotspot(
                ExactRect(0.04f, 0.31f + index * 0.082f, 0.92f, 0.075f),
                "Play ${track.title}",
            ) {
                viewModel.playTrack(track)
                navController.navigate("nowPlaying")
            }
        }
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactPlaylists(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    ExactSurface(MuseReferenceScreen.Playlists) {
        BackHotspot(navController)
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactNowPlaying(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    ExactSurface(MuseReferenceScreen.NowPlaying) {
        BackHotspot(navController)
        InvisibleHotspot(ExactRect(0.39f, 0.70f, 0.22f, 0.13f), "Play or pause") {
            viewModel.playback.playPause()
        }
        InvisibleHotspot(ExactRect(0.20f, 0.70f, 0.18f, 0.13f), "Previous") {
            viewModel.playback.previous()
        }
        InvisibleHotspot(ExactRect(0.62f, 0.70f, 0.18f, 0.13f), "Next") {
            viewModel.playback.next()
        }
        InvisibleHotspot(ExactRect(0.04f, 0.71f, 0.14f, 0.11f), "Shuffle") {
            viewModel.playback.toggleShuffle()
        }
        InvisibleHotspot(ExactRect(0.82f, 0.71f, 0.14f, 0.11f), "Repeat") {
            viewModel.playback.cycleRepeat()
        }
        InvisibleHotspot(ExactRect(0.04f, 0.84f, 0.29f, 0.10f), "Lyrics") {
            track?.let(viewModel::loadLyrics)
            navController.navigate("lyrics")
        }
        InvisibleHotspot(ExactRect(0.36f, 0.84f, 0.28f, 0.10f), "Queue") {
            navController.navigate("queue")
        }
        InvisibleHotspot(ExactRect(0.69f, 0.84f, 0.27f, 0.10f), "More options") {
            navController.navigate("more")
        }
        track?.let { current ->
            InvisibleHotspot(ExactRect(0.84f, 0.52f, 0.13f, 0.09f), "Favourite") {
                viewModel.toggleFavorite(current.id)
            }
        }
    }
}

@Composable
private fun ExactQueue(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    ExactSurface(MuseReferenceScreen.Queue) {
        BackHotspot(navController)
        InvisibleHotspot(ExactRect(0.67f, 0.12f, 0.28f, 0.09f), "Shuffle queue") {
            viewModel.playback.toggleShuffle()
        }
        playback.queue.take(7).forEachIndexed { index, item ->
            InvisibleHotspot(
                ExactRect(0.04f, 0.28f + index * 0.078f, 0.92f, 0.073f),
                "Queue item ${index + 1}",
            ) {
                val targetIndex = playback.queue.indexOfFirst { it.mediaId == item.mediaId }
                if (targetIndex >= 0) {
                    viewModel.playback.seekTo(0L)
                }
            }
        }
    }
}

@Composable
private fun ExactArtist(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val artist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val tracks = viewModel.artistTracks(artist)
    ExactSurface(MuseReferenceScreen.Artist) {
        BackHotspot(navController)
        InvisibleHotspot(ExactRect(0.11f, 0.30f, 0.58f, 0.09f), "Play artist") {
            if (tracks.isNotEmpty()) {
                viewModel.playTracks(tracks)
                navController.navigate("nowPlaying")
            }
        }
        tracks.take(4).forEachIndexed { index, track ->
            InvisibleHotspot(
                ExactRect(0.05f, 0.51f + index * 0.09f, 0.90f, 0.085f),
                "Play ${track.title}",
            ) {
                viewModel.playTrack(track)
                navController.navigate("nowPlaying")
            }
        }
    }
}

@Composable
private fun ExactAlbum(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val album by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val tracks = viewModel.albumTracks(album)
    ExactSurface(MuseReferenceScreen.Album) {
        BackHotspot(navController)
        InvisibleHotspot(ExactRect(0.05f, 0.42f, 0.42f, 0.09f), "Play album") {
            if (tracks.isNotEmpty()) {
                viewModel.playTracks(tracks)
                navController.navigate("nowPlaying")
            }
        }
        InvisibleHotspot(ExactRect(0.49f, 0.42f, 0.32f, 0.09f), "Shuffle album") {
            if (tracks.isNotEmpty()) {
                viewModel.playTracksShuffled(tracks)
                navController.navigate("nowPlaying")
            }
        }
        tracks.take(6).forEachIndexed { index, track ->
            InvisibleHotspot(
                ExactRect(0.04f, 0.54f + index * 0.065f, 0.92f, 0.062f),
                "Play ${track.title}",
            ) {
                viewModel.playTrack(track)
                navController.navigate("nowPlaying")
            }
        }
    }
}

@Composable
private fun ExactDownloads(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.managedMediaState.collectAsStateWithLifecycle()
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importManagedMedia)
    }

    ExactSurface(MuseReferenceScreen.Downloads) {
        BackHotspot(navController)
        state.tracks.take(5).forEachIndexed { index, track ->
            InvisibleHotspot(
                ExactRect(0.04f, 0.28f + index * 0.105f, 0.92f, 0.095f),
                "Play ${track.title}",
            ) {
                viewModel.playTrack(track)
                navController.navigate("nowPlaying")
            }
        }
        InvisibleHotspot(ExactRect(0.10f, 0.84f, 0.80f, 0.11f), "Import music") {
            importLauncher.launch(arrayOf("audio/*"))
        }
    }
}

@Composable
private fun ExactSleepTimer(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    var selectedMinutes by remember { mutableIntStateOf(30) }
    ExactSurface(MuseReferenceScreen.SleepTimer) {
        BackHotspot(navController)
        listOf(10, 30, 60, 90).forEachIndexed { index, minutes ->
            InvisibleHotspot(
                ExactRect(0.05f + index * 0.235f, 0.69f, 0.20f, 0.10f),
                "Set sleep timer to $minutes minutes",
            ) {
                selectedMinutes = minutes
            }
        }
        InvisibleHotspot(ExactRect(0.08f, 0.82f, 0.84f, 0.12f), "Start sleep timer") {
            viewModel.playback.startSleepTimer(selectedMinutes)
        }
    }
}

@Composable
private fun ExactEqualizer(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val effects by viewModel.playback.audioEffects.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.playback.refreshAudioEffects()
    }

    ExactSurface(MuseReferenceScreen.Equalizer) {
        InvisibleHotspot(ExactRect(0.79f, 0.04f, 0.17f, 0.09f), "Enable equalizer") {
            viewModel.playback.setAudioEffectsEnabled(!effects.masterEnabled)
        }
        InvisibleHotspot(ExactRect(0.08f, 0.57f, 0.25f, 0.14f), "Bass boost") {
            if (effects.bassAvailable) {
                viewModel.playback.setBassEnabled(!effects.bassEnabled)
            }
        }
        InvisibleHotspot(ExactRect(0.38f, 0.57f, 0.25f, 0.14f), "Virtualizer") {
            if (effects.virtualizerAvailable) {
                viewModel.playback.setVirtualizerEnabled(!effects.virtualizerEnabled)
            }
        }
        InvisibleHotspot(ExactRect(0.68f, 0.57f, 0.25f, 0.14f), "3D audio") {
            if (effects.spatialAvailable) {
                // Spatial capability is visualised by the exact design; the
                // platform-specific effect remains capability-gated.
            }
        }
        InvisibleHotspot(ExactRect(0.06f, 0.77f, 0.88f, 0.07f), "Loudness enhancer") {
            if (effects.loudnessAvailable) {
                viewModel.playback.setLoudnessEnabled(!effects.loudnessEnabled)
            }
        }
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactSettings(
    navController: NavHostController,
) {
    ExactSurface(MuseReferenceScreen.Settings) {
        BackHotspot(navController)
        InvisibleHotspot(ExactRect(0.04f, 0.30f, 0.92f, 0.085f), "Sleep timer") {
            navController.navigate("sleep")
        }
        InvisibleHotspot(ExactRect(0.04f, 0.39f, 0.92f, 0.085f), "Audio enhancement") {
            navController.navigate("equalizer")
        }
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactTools(
    navController: NavHostController,
) {
    ExactSurface(MuseReferenceScreen.Settings) {
        InvisibleHotspot(ExactRect(0.04f, 0.30f, 0.92f, 0.085f), "Sleep timer") {
            navController.navigate("sleep")
        }
        InvisibleHotspot(ExactRect(0.04f, 0.39f, 0.92f, 0.085f), "Equalizer") {
            navController.navigate("equalizer")
        }
        MainNavHotspots(navController)
    }
}

@Composable
private fun ExactMoreOptions(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()

    ExactSurface(MuseReferenceScreen.MoreOptions) {
        BackHotspot(navController)

        InvisibleHotspot(ExactRect(0.04f, 0.20f, 0.92f, 0.075f), "Add to playlist") {
            navController.navigate("playlists")
        }
        InvisibleHotspot(ExactRect(0.04f, 0.35f, 0.92f, 0.075f), "View album") {
            track?.let {
                viewModel.selectAlbum(it.album)
                navController.navigate("album")
            }
        }
        InvisibleHotspot(ExactRect(0.04f, 0.43f, 0.92f, 0.075f), "View artist") {
            track?.let {
                viewModel.selectArtist(it.artist)
                navController.navigate("artist")
            }
        }
        InvisibleHotspot(ExactRect(0.04f, 0.51f, 0.92f, 0.075f), "Share") {
            track?.let { current ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, current.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share with")
                )
            }
        }
        InvisibleHotspot(ExactRect(0.04f, 0.59f, 0.92f, 0.075f), "Song radio") {
            track?.let {
                viewModel.playLocalSongRadio(it)
                navController.navigate("nowPlaying")
            }
        }
        InvisibleHotspot(ExactRect(0.04f, 0.83f, 0.92f, 0.10f), "Remove from library") {
            track?.let(viewModel::hideFromLibrary)
            navController.popBackStack()
        }
    }
}

@Composable
private fun ExactStaticDetail(
    screen: MuseReferenceScreen,
    navController: NavHostController,
) {
    ExactSurface(screen) {
        BackHotspot(navController)
    }
}
