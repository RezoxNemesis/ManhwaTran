package com.rezoxnemesis.muse.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rezoxnemesis.muse.MuseViewModel
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.playback.PlaybackUiState
import com.rezoxnemesis.muse.ui.theme.MuseBackground
import com.rezoxnemesis.muse.ui.theme.MuseBorder
import com.rezoxnemesis.muse.ui.theme.MuseGreen
import com.rezoxnemesis.muse.ui.theme.MuseMuted
import com.rezoxnemesis.muse.ui.theme.MuseSurface
import kotlin.math.roundToLong

private data class PrimaryDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val PrimaryDestinations = listOf(
    PrimaryDestination("home", "Home", Icons.Rounded.Home),
    PrimaryDestination("explore", "Explore", Icons.Rounded.Explore),
    PrimaryDestination("library", "Library", Icons.Rounded.LibraryMusic),
    PrimaryDestination("equalizer", "Equalizer", Icons.Rounded.Equalizer),
    PrimaryDestination("tools", "Muse Lab", Icons.Rounded.Tune),
)

@Composable
fun MuseApp(
    viewModel: MuseViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val showPrimaryNav = currentRoute in PrimaryDestinations.map { it.route }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF07170B),
                        MuseBackground,
                        Color(0xFF020704),
                    )
                )
            ),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (showPrimaryNav) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xE607120A)),
                    ) {
                        if (playback.currentMediaId != null) {
                            MiniPlayer(
                                state = playback,
                                onOpen = { navController.navigate("nowPlaying") },
                                onPlayPause = viewModel.playback::playPause,
                            )
                        }
                        MuseBottomNavigation(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding),
            ) {
                composable("home") {
                    HomeScreen(viewModel, navController)
                }
                composable("explore") {
                    ExploreScreen(viewModel, navController)
                }
                composable("library") {
                    LibraryScreen(viewModel, navController)
                }
                composable("equalizer") {
                    EqualizerFoundationScreen(playback)
                }
                composable("tools") {
                    MuseLabScreen(navController)
                }
                composable("nowPlaying") {
                    NowPlayingScreen(viewModel, navController)
                }
                composable("queue") {
                    QueueScreen(viewModel, navController)
                }
                composable("lyrics") {
                    LyricsScreen(navController)
                }
                composable("liked") {
                    LikedScreen(viewModel, navController)
                }
                composable("playlists") {
                    PlaylistsScreen(viewModel, navController)
                }
                composable("artist") {
                    ArtistScreen(viewModel, navController)
                }
                composable("album") {
                    AlbumScreen(viewModel, navController)
                }
                composable("downloads") {
                    DownloadsScreen(navController)
                }
                composable("sleep") {
                    SleepTimerScreen(viewModel, navController)
                }
                composable("settings") {
                    SettingsScreen(viewModel, navController)
                }
                composable("more") {
                    MoreOptionsScreen(viewModel, navController)
                }
            }
        }
    }
}

@Composable
private fun MuseBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = Color(0xF008170C),
        contentColor = Color.White,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        PrimaryDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                    )
                },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MuseGreen,
                    selectedTextColor = MuseGreen,
                    indicatorColor = Color(0x332DFF4C),
                    unselectedIconColor = MuseMuted,
                    unselectedTextColor = MuseMuted,
                ),
            )
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        action?.invoke()
    }
}

@Composable
private fun HomeScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.libraryState.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filtered by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val recent by viewModel.recentTracks.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 24.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ArtworkPlaceholder(
                    modifier = Modifier.size(92.dp),
                    icon = Icons.Rounded.MusicNote,
                )
                Spacer(Modifier.height(10.dp))
                Text("Good Evening", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "FEEL THE MUSIC",
                    color = MuseGreen,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search songs, artists, albums…") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { navController.navigate("library") }
                ),
                shape = RoundedCornerShape(22.dp),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickAction(
                    label = "Favorites",
                    icon = Icons.Rounded.Favorite,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("liked") },
                )
                QuickAction(
                    label = "Playlists",
                    icon = Icons.Rounded.PlaylistPlay,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("playlists") },
                )
                QuickAction(
                    label = "Downloads",
                    icon = Icons.Rounded.Download,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("downloads") },
                )
                QuickAction(
                    label = "Recent",
                    icon = Icons.Rounded.AccessTime,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("library") },
                )
            }
        }

        if (state.error != null) {
            item { ErrorCard(state.error.orEmpty(), onRetry = viewModel::refreshLibrary) }
        }

        if (state.tracks.isEmpty() && !state.loading) {
            item {
                EmptyCard(
                    title = "No music found",
                    body = "Add audio to this device or refresh the library from Muse Lab.",
                )
            }
        } else {
            val homeTracks = recent.ifEmpty { state.tracks }
            item {
                SectionTitle(
                    if (recent.isEmpty()) "Recently Added" else "Recently Played",
                    trailing = "${homeTracks.size} local tracks",
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(homeTracks.take(8), key = { it.id }) { track ->
                        TrackPoster(
                            track = track,
                            favorite = track.id in favourites,
                            onClick = { viewModel.playTrack(track) },
                        )
                    }
                }
            }
        }

        if (query.isNotBlank()) {
            item { SectionTitle("Search results", trailing = "${filtered.size}") }
            items(filtered.take(8), key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    favorite = track.id in favourites,
                    onPlay = { viewModel.playTrack(track) },
                    onFavorite = { viewModel.toggleFavorite(track.id) },
                    onArtist = {
                        viewModel.selectArtist(track.artist)
                        navController.navigate("artist")
                    },
                    onAlbum = {
                        viewModel.selectAlbum(track.album)
                        navController.navigate("album")
                    },
                )
            }
        }
    }
}

@Composable
private fun ExploreScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.libraryState.collectAsStateWithLifecycle()
    val liked by viewModel.likedTracks.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(title = "Explore")
        }
        item {
            SectionTitle("Trending in Your Library", trailing = "Local")
        }
        items(state.tracks.take(8), key = { it.id }) { track ->
            TrackRow(
                track = track,
                favorite = liked.any { it.id == track.id },
                onPlay = { viewModel.playTrack(track) },
                onFavorite = { viewModel.toggleFavorite(track.id) },
                onArtist = {
                    viewModel.selectArtist(track.artist)
                    navController.navigate("artist")
                },
                onAlbum = {
                    viewModel.selectAlbum(track.album)
                    navController.navigate("album")
                },
            )
        }
        item {
            EmptyCard(
                title = "Muse Moods",
                body = "Mood mixes will use local metadata and listening history. No cloud catalogue or fabricated recommendations are used.",
            )
        }
    }
}

@Composable
private fun LibraryScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val tracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = "Your Library",
                action = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search your library") },
                shape = RoundedCornerShape(20.dp),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text("Songs") })
                FilterChip(
                    selected = false,
                    onClick = { navController.navigate("playlists") },
                    label = { Text("Playlists") },
                )
            }
        }
        if (tracks.isEmpty()) {
            item {
                EmptyCard(
                    title = "Nothing to show",
                    body = if (query.isBlank()) "Your local songs will appear here." else "No tracks match your search.",
                )
            }
        } else {
            items(tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    favorite = track.id in favourites,
                    onPlay = { viewModel.playTrack(track) },
                    onFavorite = { viewModel.toggleFavorite(track.id) },
                    onArtist = {
                        viewModel.selectArtist(track.artist)
                        navController.navigate("artist")
                    },
                    onAlbum = {
                        viewModel.selectAlbum(track.album)
                        navController.navigate("album")
                    },
                )
            }
        }
    }
}

@Composable
private fun NowPlayingScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val duration = playback.durationMs.coerceAtLeast(1L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = "Now Playing",
            onBack = { navController.popBackStack() },
            action = {
                IconButton(onClick = { navController.navigate("more") }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                }
            },
        )
        Spacer(Modifier.height(18.dp))
        ArtworkPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            icon = Icons.Rounded.MusicNote,
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playback.title.ifBlank { "Nothing playing" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    playback.artist,
                    color = MuseMuted,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (track != null) {
                IconButton(onClick = { viewModel.toggleFavorite(track!!.id) }) {
                    Icon(
                        if (track!!.id in favourites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (track!!.id in favourites) MuseGreen else Color.White,
                    )
                }
            }
        }
        Slider(
            value = playback.positionMs.coerceIn(0L, duration).toFloat(),
            onValueChange = { viewModel.playback.seekTo(it.roundToLong()) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(formatDuration(playback.positionMs), color = MuseMuted)
            Spacer(Modifier.weight(1f))
            Text(formatDuration(playback.durationMs), color = MuseMuted)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel.playback::toggleShuffle) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playback.shuffleEnabled) MuseGreen else Color.White,
                )
            }
            IconButton(onClick = viewModel.playback::previous) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
            }
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = Color(0x2215FF4F),
                border = androidx.compose.foundation.BorderStroke(2.dp, MuseGreen),
                onClick = viewModel.playback::playPause,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playback.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(38.dp),
                    )
                }
            }
            IconButton(onClick = viewModel.playback::next) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
            }
            IconButton(onClick = viewModel.playback::cycleRepeat) {
                Icon(
                    Icons.Rounded.Repeat,
                    contentDescription = "Repeat",
                    tint = if (playback.repeatMode == androidx.media3.common.Player.REPEAT_MODE_OFF) Color.White else MuseGreen,
                )
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = { navController.navigate("queue") }) {
                Icon(Icons.Rounded.QueueMusic, contentDescription = null)
                Text(" Queue")
            }
            TextButton(onClick = { navController.navigate("lyrics") }) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null)
                Text(" Lyrics")
            }
            TextButton(onClick = { navController.navigate("more") }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = null)
                Text(" More")
            }
        }
    }
}

@Composable
private fun QueueScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = "Play Queue",
                onBack = { navController.popBackStack() },
                action = {
                    IconButton(onClick = viewModel.playback::clearQueue) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear queue")
                    }
                },
            )
        }
        if (playback.queue.isEmpty()) {
            item { EmptyCard("Queue is empty", "Choose a song from your library to start listening.") }
        } else {
            itemsIndexed(playback.queue, key = { index, item -> "${item.mediaId}:$index" }) { index, item ->
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.title.ifBlank { "Unknown title" },
                                fontWeight = if (index == playback.currentIndex) FontWeight.Bold else FontWeight.Medium,
                                color = if (index == playback.currentIndex) MuseGreen else Color.White,
                            )
                            Text(item.artist, color = MuseMuted)
                        }
                        TextButton(
                            enabled = index > 0,
                            onClick = { viewModel.playback.moveQueueItem(index, index - 1) },
                        ) { Text("↑") }
                        TextButton(
                            enabled = index < playback.queue.lastIndex,
                            onClick = { viewModel.playback.moveQueueItem(index, index + 1) },
                        ) { Text("↓") }
                        IconButton(onClick = { viewModel.playback.removeQueueItem(index) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Remove from queue")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsScreen(
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Lyrics", onBack = { navController.popBackStack() })
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyCard(
                title = "No local lyrics found",
                body = "Muse will use embedded lyrics, local LRC files, or user-imported lyrics. It will not fabricate song lyrics.",
            )
        }
    }
}

@Composable
private fun LikedScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val tracks by viewModel.likedTracks.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeader("Liked Songs", { navController.popBackStack() }) }
        if (tracks.isEmpty()) {
            item { EmptyCard("No liked songs", "Tap the heart on a track to keep it here.") }
        } else {
            items(tracks, key = { it.id }) { track ->
                TrackRow(
                    track = track,
                    favorite = true,
                    onPlay = { viewModel.playTrack(track) },
                    onFavorite = { viewModel.toggleFavorite(track.id) },
                    onArtist = {
                        viewModel.selectArtist(track.artist)
                        navController.navigate("artist")
                    },
                    onAlbum = {
                        viewModel.selectAlbum(track.album)
                        navController.navigate("album")
                    },
                )
            }
        }
    }
}

@Composable
private fun PlaylistsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val liked by viewModel.likedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Playlists", { navController.popBackStack() }) }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("New playlist") },
                placeholder = { Text("Playlist name") },
                trailingIcon = {
                    TextButton(
                        enabled = name.isNotBlank(),
                        onClick = {
                            viewModel.createPlaylist(name)
                            name = ""
                        },
                    ) { Text("Create") }
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        item {
            GlassCard(
                modifier = Modifier.clickable { navController.navigate("liked") },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Favorite, contentDescription = null, tint = MuseGreen)
                    Spacer(Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Liked Songs", fontWeight = FontWeight.SemiBold)
                        Text("${liked.size} songs", color = MuseMuted)
                    }
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                EmptyCard(
                    title = "No playlists yet",
                    body = "Create a playlist above. Muse stores it locally on this device.",
                )
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                val tracks = viewModel.playlistTracks(playlist)
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = MuseGreen)
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(playlist.name, fontWeight = FontWeight.SemiBold)
                            Text("${tracks.size} songs", color = MuseMuted)
                        }
                        IconButton(
                            enabled = tracks.isNotEmpty(),
                            onClick = { viewModel.playTracks(tracks) },
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play playlist")
                        }
                        IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete playlist")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val artist by viewModel.selectedArtist.collectAsStateWithLifecycle()
    val tracks = viewModel.artistTracks(artist)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeader(artist ?: "Artist", { navController.popBackStack() }) }
        item {
            Text(
                "${tracks.size} local tracks",
                color = MuseMuted,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (tracks.isNotEmpty()) {
            item {
                Button(
                    onClick = { viewModel.playTracks(tracks) },
                    colors = ButtonDefaults.buttonColors(containerColor = MuseGreen, contentColor = MuseBackground),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Text(" Play")
                }
            }
        }
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                favorite = false,
                onPlay = { viewModel.playTrack(track) },
                onFavorite = { viewModel.toggleFavorite(track.id) },
                onArtist = {},
                onAlbum = {
                    viewModel.selectAlbum(track.album)
                    navController.navigate("album")
                },
            )
        }
    }
}

@Composable
private fun AlbumScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val album by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val tracks = viewModel.albumTracks(album)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeader(album ?: "Album", { navController.popBackStack() }) }
        item {
            Text(
                "${tracks.size} tracks",
                color = MuseMuted,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (tracks.isNotEmpty()) {
            item {
                Button(
                    onClick = { viewModel.playTracks(tracks) },
                    colors = ButtonDefaults.buttonColors(containerColor = MuseGreen, contentColor = MuseBackground),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Text(" Play album")
                }
            }
        }
        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                favorite = false,
                onPlay = { viewModel.playTrack(track) },
                onFavorite = { viewModel.toggleFavorite(track.id) },
                onArtist = {
                    viewModel.selectArtist(track.artist)
                    navController.navigate("artist")
                },
                onAlbum = {},
            )
        }
    }
}

@Composable
private fun DownloadsScreen(
    navController: NavHostController,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("Downloads", { navController.popBackStack() })
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyCard(
                title = "No Muse-managed downloads",
                body = "This local-first build only shows real app-managed media here. Device songs remain in your Library.",
            )
        }
    }
}

@Composable
private fun SleepTimerScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val timer by viewModel.sleepTimer.collectAsStateWithLifecycle()
    val presets = listOf(10, 30, 60, 90)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader("Sleep Timer", { navController.popBackStack() })
        Spacer(Modifier.height(36.dp))
        Surface(
            modifier = Modifier.size(240.dp),
            shape = CircleShape,
            color = Color(0x5513301A),
            border = androidx.compose.foundation.BorderStroke(2.dp, MuseGreen),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Timer, contentDescription = null, tint = MuseGreen)
                    Text(
                        if (timer.active) formatDuration(timer.remainingMs) else "Off",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("remaining", color = MuseMuted)
                }
            }
        }
        Spacer(Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            presets.forEach { minutes ->
                Button(
                    onClick = { viewModel.startSleepTimer(minutes) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timer.active && timer.remainingMs <= minutes * 60_000L) Color(0x443DFF5E) else MuseSurface
                    ),
                ) {
                    Text("$minutes")
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (timer.active) {
            Button(
                onClick = viewModel::cancelSleepTimer,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Cancel Timer")
            }
        } else {
            Text("Choose a duration. Muse pauses playback when the timer expires.", color = MuseMuted)
        }
    }
}

@Composable
private fun SettingsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.libraryState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("Settings", { navController.popBackStack() })
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsRow(
                icon = Icons.Rounded.LibraryMusic,
                title = "Library",
                subtitle = "${state.tracks.size} local tracks",
                onClick = viewModel::refreshLibrary,
            )
            SettingsRow(
                icon = Icons.Rounded.Timer,
                title = "Sleep Timer",
                subtitle = "Listening-session timer",
                onClick = { navController.navigate("sleep") },
            )
            SettingsRow(
                icon = Icons.Rounded.Equalizer,
                title = "Audio Enhancement",
                subtitle = "Capability-aware processing",
                onClick = { navController.navigate("equalizer") },
            )
            GlassCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MuseGreen)
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text("About", fontWeight = FontWeight.Medium)
                        Text("Muse 0.1.0 • local-first • no account required", color = MuseMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreOptionsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("More Options", { navController.popBackStack() })
        if (track == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyCard("Nothing playing", "Start a track to see song actions.")
            }
        } else {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OptionRow(
                    icon = if (track!!.id in favourites) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    title = if (track!!.id in favourites) "Remove from Liked Songs" else "Add to Liked Songs",
                    onClick = { viewModel.toggleFavorite(track!!.id) },
                )
                OptionRow(
                    icon = Icons.Rounded.Album,
                    title = "View Album",
                    onClick = {
                        viewModel.selectAlbum(track!!.album)
                        navController.navigate("album")
                    },
                )
                OptionRow(
                    icon = Icons.Rounded.Person,
                    title = "View Artist",
                    onClick = {
                        viewModel.selectArtist(track!!.artist)
                        navController.navigate("artist")
                    },
                )
                OptionRow(
                    icon = Icons.Rounded.Share,
                    title = "Share",
                    onClick = {
                        val text = "${track!!.title} — ${track!!.artist}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share with"))
                    },
                )
                GlassCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = MuseGreen)
                        Spacer(Modifier.size(14.dp))
                        Column {
                            Text("Song Info")
                            Text(
                                buildString {
                                    append(track!!.album)
                                    track!!.mimeType?.let { append(" • $it") }
                                },
                                color = MuseMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EqualizerFoundationScreen(
    playback: PlaybackUiState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("Equalizer")
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ArtworkPlaceholder(
                modifier = Modifier.size(110.dp),
                icon = Icons.Rounded.Equalizer,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                if (playback.currentMediaId == null) "Start playback to initialise an audio session"
                else "Playback session is active",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Muse will only expose EQ, bass, virtualizer and spatial controls after runtime capability detection. Unsupported effects will never be shown as working.",
                color = MuseMuted,
            )
        }
    }
}

@Composable
private fun MuseLabScreen(
    navController: NavHostController,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Muse Lab") }
        item {
            SettingsRow(Icons.Rounded.QueueMusic, "Play Queue", "Current session", { navController.navigate("queue") })
        }
        item {
            SettingsRow(Icons.Rounded.PlaylistPlay, "Playlists", "Liked songs and playlists", { navController.navigate("playlists") })
        }
        item {
            SettingsRow(Icons.Rounded.Timer, "Sleep Timer", "10 / 30 / 60 / 90 minutes", { navController.navigate("sleep") })
        }
        item {
            SettingsRow(Icons.Rounded.Download, "Downloads", "Muse-managed offline files", { navController.navigate("downloads") })
        }
        item {
            SettingsRow(Icons.Rounded.Settings, "Settings", "Library, privacy and playback", { navController.navigate("settings") })
        }
    }
}

@Composable
private fun MiniPlayer(
    state: PlaybackUiState,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
) {
    Surface(
        color = Color(0xF016321D),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkPlaceholder(
                modifier = Modifier.size(46.dp),
                icon = Icons.Rounded.MusicNote,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(state.title.ifBlank { "Muse" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(state.artist, color = MuseMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier
            .height(94.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription = label, tint = MuseGreen)
            Spacer(Modifier.height(7.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TrackPoster(
    track: Track,
    favorite: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(width = 142.dp, height = 190.dp)
            .clickable(onClick = onClick),
    ) {
        ArtworkPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            icon = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.MusicNote,
        )
        Spacer(Modifier.height(8.dp))
        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(track.artist, color = MuseMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TrackRow(
    track: Track,
    favorite: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    onArtist: () -> Unit,
    onAlbum: () -> Unit,
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlay)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtworkPlaceholder(
                modifier = Modifier.size(58.dp),
                icon = Icons.Rounded.MusicNote,
            )
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(
                    track.artist,
                    color = MuseMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onArtist),
                )
                Text(
                    track.album,
                    color = MuseMuted.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onAlbum),
                )
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (favorite) "Remove favorite" else "Favorite",
                    tint = if (favorite) MuseGreen else Color.White,
                )
            }
        }
    }
}

@Composable
private fun ArtworkPlaceholder(
    modifier: Modifier,
    icon: ImageVector,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF173B20),
                        Color(0xFF07150B),
                        Color(0xFF285B2C),
                    )
                )
            )
            .border(1.dp, MuseBorder, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MuseGreen,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.border(1.dp, MuseBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MuseSurface),
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(
    title: String,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = MuseGreen)
    }
}

@Composable
private fun EmptyCard(
    title: String,
    body: String,
) {
    GlassCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = MuseMuted)
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    GlassCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Library error", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(message, color = MuseMuted)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MuseGreen)
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(subtitle, color = MuseMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color(0x99112718),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MuseGreen)
            Spacer(Modifier.size(14.dp))
            Column {
                Text(title)
                if (subtitle != null) Text(subtitle, color = MuseMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val safe = durationMs.coerceAtLeast(0L)
    val totalSeconds = safe / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
