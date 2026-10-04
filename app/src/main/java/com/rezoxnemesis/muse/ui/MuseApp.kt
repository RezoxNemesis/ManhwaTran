package com.rezoxnemesis.muse.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.rezoxnemesis.muse.R
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.data.UserPlaylist
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

private enum class LibraryTab {
    Songs,
    Albums,
    Artists,
    Playlists,
}

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
                                track = currentTrack,
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
                    EqualizerScreen(viewModel, playback)
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
                    LyricsScreen(viewModel, navController)
                }
                composable("liked") {
                    LikedScreen(viewModel, navController)
                }
                composable("recent") {
                    RecentScreen(viewModel, navController)
                }
                composable("playlists") {
                    PlaylistsScreen(viewModel, navController)
                }
                composable("playlistDetail") {
                    PlaylistDetailScreen(viewModel, navController)
                }
                composable("artist") {
                    ArtistScreen(viewModel, navController)
                }
                composable("album") {
                    AlbumScreen(viewModel, navController)
                }
                composable("downloads") {
                    DownloadsScreen(viewModel, navController)
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
                Image(
                    painter = painterResource(R.drawable.muse_logo),
                    contentDescription = "Muse logo",
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(26.dp)),
                    contentScale = ContentScale.Fit,
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
                    onClick = { navController.navigate("recent") },
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
    val trending by viewModel.localTrendingTracks.collectAsStateWithLifecycle()
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
        items(trending.take(8), key = { it.id }) { track ->
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
    val liked by viewModel.likedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }

    val albums = tracks
        .groupBy { it.album }
        .entries
        .sortedBy { it.key.lowercase() }
    val artists = tracks
        .groupBy { it.artist }
        .entries
        .sortedBy { it.key.lowercase() }
    val visiblePlaylists = playlists.filter { playlist ->
        query.isBlank() || playlist.name.contains(query.trim(), ignoreCase = true)
    }

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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LibraryTab.entries) { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.name) },
                    )
                }
            }
        }

        when (selectedTab) {
            LibraryTab.Songs -> {
                if (tracks.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "Nothing to show",
                            body = if (query.isBlank()) {
                                "Your local songs will appear here."
                            } else {
                                "No songs match your search."
                            },
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

            LibraryTab.Albums -> {
                if (albums.isEmpty()) {
                    item {
                        EmptyCard(
                            "No albums found",
                            if (query.isBlank()) {
                                "Albums from your local music metadata will appear here."
                            } else {
                                "No albums match your search."
                            },
                        )
                    }
                } else {
                    items(albums, key = { it.key }) { entry ->
                        val albumTracks = entry.value
                        val representative = albumTracks.first()
                        GlassCard(
                            modifier = Modifier.clickable {
                                viewModel.selectAlbum(entry.key)
                                navController.navigate("album")
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TrackArtwork(
                                    track = representative,
                                    modifier = Modifier.size(62.dp),
                                    contentDescription = null,
                                )
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        entry.key,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        representative.artist,
                                        color = MuseMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${albumTracks.size} songs",
                                        color = MuseMuted.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.Artists -> {
                if (artists.isEmpty()) {
                    item {
                        EmptyCard(
                            "No artists found",
                            if (query.isBlank()) {
                                "Artists from your local music metadata will appear here."
                            } else {
                                "No artists match your search."
                            },
                        )
                    }
                } else {
                    items(artists, key = { it.key }) { entry ->
                        val artistTracks = entry.value
                        GlassCard(
                            modifier = Modifier.clickable {
                                viewModel.selectArtist(entry.key)
                                navController.navigate("artist")
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MuseGreen,
                                )
                                Spacer(Modifier.size(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        entry.key,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "${artistTracks.size} songs • " +
                                            "${artistTracks.map { it.album }.distinct().size} albums",
                                        color = MuseMuted,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.Playlists -> {
                item {
                    GlassCard(
                        modifier = Modifier.clickable {
                            navController.navigate("liked")
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                            Spacer(Modifier.size(14.dp))
                            Column {
                                Text("Liked Songs", fontWeight = FontWeight.SemiBold)
                                Text("${liked.size} songs", color = MuseMuted)
                            }
                        }
                    }
                }

                if (visiblePlaylists.isEmpty()) {
                    item {
                        EmptyCard(
                            "No playlists found",
                            if (query.isBlank()) {
                                "Create a playlist to organise your music."
                            } else {
                                "No playlists match your search."
                            },
                        )
                    }
                } else {
                    items(visiblePlaylists, key = { it.id }) { playlist ->
                        val playlistTracks = viewModel.playlistTracks(playlist)
                        GlassCard(
                            modifier = Modifier.clickable {
                                viewModel.selectPlaylist(playlist.id)
                                navController.navigate("playlistDetail")
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.PlaylistPlay,
                                    contentDescription = null,
                                    tint = MuseGreen,
                                )
                                Spacer(Modifier.size(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        playlist.name,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        "${playlistTracks.size} songs",
                                        color = MuseMuted,
                                    )
                                }
                            }
                        }
                    }
                }
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
        TrackArtwork(
            track = track,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
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
        if (playback.errorMessage != null) {
            Spacer(Modifier.height(14.dp))
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        playback.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = viewModel.playback::next) {
                        Text("Skip")
                    }
                }
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
    var saveName by remember { mutableStateOf("") }

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
                    Row {
                        IconButton(onClick = viewModel.playback::toggleShuffle) {
                            Icon(
                                Icons.Rounded.Shuffle,
                                contentDescription = "Toggle shuffle",
                                tint = if (playback.shuffleEnabled) MuseGreen else Color.White,
                            )
                        }
                        IconButton(onClick = viewModel.playback::clearUpcomingQueue) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear upcoming queue")
                        }
                    }
                },
            )
        }
        if (playback.queue.isEmpty()) {
            item { EmptyCard("Queue is empty", "Choose a song from your library to start listening.") }
        } else {
            item {
                OutlinedTextField(
                    value = saveName,
                    onValueChange = { saveName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Save queue as playlist") },
                    placeholder = { Text("Playlist name") },
                    trailingIcon = {
                        TextButton(
                            enabled = saveName.isNotBlank(),
                            onClick = {
                                viewModel.saveCurrentQueueAsPlaylist(saveName)
                                saveName = ""
                            },
                        ) {
                            Text("Save")
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                )
            }

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
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val track by viewModel.currentTrack.collectAsStateWithLifecycle()
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyricsState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var autoFollow by remember { mutableStateOf(true) }
    var confirmRemove by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        val activeTrack = track
        if (uri != null && activeTrack != null) {
            viewModel.importLyrics(activeTrack.id, uri)
        }
    }

    LaunchedEffect(track?.id) {
        track?.let { viewModel.loadLyrics(it.id) }
    }

    val document = lyrics.document
    val activeIndex = if (document?.synced == true) {
        document.lines.indexOfLast { line ->
            val time = line.timeMs
            time != null && time <= playback.positionMs
        }
    } else {
        -1
    }

    LaunchedEffect(activeIndex, autoFollow) {
        if (autoFollow && activeIndex >= 0 && document != null) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Lyrics",
            onBack = { navController.popBackStack() },
            action = {
                if (track != null) {
                    TextButton(onClick = { importLauncher.launch("text/*") }) {
                        Text(if (document == null) "Import" else "Replace")
                    }
                }
            },
        )

        if (track == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                EmptyCard(
                    title = "Nothing playing",
                    body = "Start a track, then open Lyrics to attach or view local lyrics.",
                )
            }
            return@Column
        }

        if (lyrics.loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading lyrics…", color = MuseMuted)
            }
            return@Column
        }

        if (lyrics.error != null) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ErrorCard(
                    message = lyrics.error.orEmpty(),
                    onRetry = { viewModel.loadLyrics(track!!.id) },
                )
            }
        }

        if (document == null || document.lines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                GlassCard {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "No local lyrics found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Import a .lrc or plain-text lyric file you own. Muse stores the imported copy locally and never fabricates lyrics.",
                            color = MuseMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { importLauncher.launch("text/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MuseGreen,
                                contentColor = MuseBackground,
                            ),
                        ) {
                            Text("Import Lyrics")
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        track!!.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (document.synced) "Synced local lyrics" else "Plain local lyrics",
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (document.synced) {
                    Text("Auto-follow", color = MuseMuted)
                    Switch(
                        checked = autoFollow,
                        onCheckedChange = { autoFollow = it },
                    )
                }
                IconButton(onClick = { confirmRemove = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Remove imported lyrics")
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 18.dp,
                    bottom = 56.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                itemsIndexed(document.lines) { index, line ->
                    val active = index == activeIndex
                    val textColor = if (active) MuseGreen else Color.White.copy(alpha = 0.78f)
                    Text(
                        text = line.text.ifBlank { "♪" },
                        color = textColor,
                        style = if (active) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = line.timeMs != null,
                                onClick = {
                                    line.timeMs?.let {
                                        viewModel.playback.seekTo(it)
                                        autoFollow = true
                                    }
                                },
                            ),
                    )
                }
            }
        }
    }

    if (confirmRemove && track != null) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Remove imported lyrics?") },
            text = {
                Text(
                    "This removes Muse’s local lyric copy for “${track!!.title}”. Your music file is not changed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeLyrics(track!!.id)
                        confirmRemove = false
                    },
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun RecentScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val tracks by viewModel.recentTracks.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { ScreenHeader("Recently Played", { navController.popBackStack() }) }
        if (tracks.isEmpty()) {
            item { EmptyCard("No listening history yet", "Tracks you play in Muse will appear here.") }
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
    var playlistPendingDelete by remember { mutableStateOf<UserPlaylist?>(null) }

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
                GlassCard(
                    modifier = Modifier.clickable {
                        viewModel.selectPlaylist(playlist.id)
                        navController.navigate("playlistDetail")
                    },
                ) {
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
                        IconButton(onClick = { playlistPendingDelete = playlist }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete playlist")
                        }
                    }
                }
            }
        }
    }

    playlistPendingDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistPendingDelete = null },
            title = { Text("Delete playlist?") },
            text = {
                Text(
                    "Delete “${playlist.name}”? The playlist will be removed, but your music files will stay on the device."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlaylist(playlist.id)
                        playlistPendingDelete = null
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistPendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun PlaylistDetailScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val playlist = playlists.firstOrNull { it.id == selectedId }
    val tracks = playlist?.let(viewModel::playlistTracks).orEmpty()
    var renameText by remember(playlist?.id, playlist?.name) {
        mutableStateOf(playlist?.name.orEmpty())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = playlist?.name ?: "Playlist",
                onBack = { navController.popBackStack() },
            )
        }

        if (playlist == null) {
            item {
                EmptyCard(
                    title = "Playlist unavailable",
                    body = "This playlist may have been deleted.",
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        label = { Text("Playlist name") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                    )
                    Button(
                        enabled = renameText.isNotBlank() && renameText.trim() != playlist.name,
                        onClick = { viewModel.renamePlaylist(playlist.id, renameText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseGreen,
                            contentColor = MuseBackground,
                        ),
                    ) {
                        Text("Save")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        enabled = tracks.isNotEmpty(),
                        onClick = { viewModel.playTracks(tracks) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseGreen,
                            contentColor = MuseBackground,
                        ),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Text(" Play")
                    }
                    Text(
                        text = "${tracks.size} songs",
                        color = MuseMuted,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }

            if (tracks.isEmpty()) {
                item {
                    EmptyCard(
                        title = "This playlist is empty",
                        body = "Open a song’s More Options menu and add it to this playlist.",
                    )
                }
            } else {
                itemsIndexed(tracks, key = { index, track -> "${track.id}:$index" }) { index, track ->
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.playTracks(tracks, index) },
                            ) {
                                Text(
                                    track.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    track.artist,
                                    color = MuseMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(
                                enabled = index > 0,
                                onClick = {
                                    viewModel.moveTrackInPlaylist(
                                        playlist.id,
                                        index,
                                        index - 1,
                                    )
                                },
                            ) { Text("↑") }
                            TextButton(
                                enabled = index < tracks.lastIndex,
                                onClick = {
                                    viewModel.moveTrackInPlaylist(
                                        playlist.id,
                                        index,
                                        index + 1,
                                    )
                                },
                            ) { Text("↓") }
                            IconButton(
                                onClick = {
                                    viewModel.removeTrackFromPlaylist(playlist.id, track.id)
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Remove from playlist",
                                )
                            }
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
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.managedMediaState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Track?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importManagedMedia)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Downloads",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Imported Files",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Add audio files you already have access to. Muse keeps persistent read access through Android’s document picker and does not bypass protected sources.",
                        color = MuseMuted,
                    )
                    Button(
                        onClick = { importLauncher.launch(arrayOf("audio/*")) },
                        enabled = !state.importing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseGreen,
                            contentColor = MuseBackground,
                        ),
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null)
                        Text(if (state.importing) " Importing…" else " Import Audio")
                    }
                }
            }
        }

        if (state.error != null) {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            state.error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = viewModel::clearManagedMediaError) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        if (state.tracks.isEmpty() && !state.importing) {
            item {
                EmptyCard(
                    title = "No imported files",
                    body = "Use Import Audio to add a file through Android’s secure document picker.",
                )
            }
        } else {
            items(state.tracks, key = { it.id }) { track ->
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrackArtwork(
                            track = track,
                            modifier = Modifier.size(60.dp),
                            contentDescription = null,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.playTrack(track) },
                        ) {
                            Text(
                                track.title,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                track.artist,
                                color = MuseMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                listOfNotNull(
                                    track.sizeBytes?.let(::formatFileSize),
                                    track.mimeType,
                                ).joinToString(" • "),
                                color = MuseMuted.copy(alpha = 0.82f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { viewModel.playTrack(track) }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play imported track")
                        }
                        IconButton(onClick = { pendingDelete = track }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Remove imported track")
                        }
                    }
                }
            }

            item {
                val totalBytes = state.tracks.mapNotNull { it.sizeBytes }.sum()
                Text(
                    buildString {
                        append(state.tracks.size)
                        append(if (state.tracks.size == 1) " file" else " files")
                        if (totalBytes > 0L) {
                            append(" • ")
                            append(formatFileSize(totalBytes))
                        }
                    },
                    color = MuseMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }

    pendingDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove from Muse?") },
            text = {
                Text(
                    "Muse will forget “${track.title}” and release its saved document access. The original file will not be deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeManagedMedia(track)
                        pendingDelete = null
                    },
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SleepTimerScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val timer by viewModel.playback.sleepTimer.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.playback.refreshSleepTimer()
    }
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
                    onClick = { viewModel.playback.startSleepTimer(minutes) },
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
                onClick = viewModel.playback::cancelSleepTimer,
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
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
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
                if (playlists.isEmpty()) {
                    OptionRow(
                        icon = Icons.Rounded.PlaylistPlay,
                        title = "Create a Playlist",
                        subtitle = "Create one before adding this track",
                        onClick = { navController.navigate("playlists") },
                    )
                } else {
                    playlists.forEach { playlist ->
                        OptionRow(
                            icon = Icons.Rounded.PlaylistPlay,
                            title = "Add to ${playlist.name}",
                            subtitle = "${playlist.trackIds.size} songs",
                            onClick = { viewModel.addTrackToPlaylist(playlist.id, track!!.id) },
                        )
                    }
                }
                OptionRow(
                    icon = Icons.Rounded.SkipNext,
                    title = "Play Next",
                    onClick = {
                        viewModel.playback.playNext(track!!)
                        navController.popBackStack()
                    },
                )
                OptionRow(
                    icon = Icons.Rounded.QueueMusic,
                    title = "Add to Queue",
                    onClick = {
                        viewModel.playback.addToQueue(track!!)
                        navController.popBackStack()
                    },
                )
                OptionRow(
                    icon = Icons.Rounded.Tune,
                    title = "Muse Local Radio",
                    subtitle = "Build a mix from related music already in your library",
                    onClick = {
                        viewModel.playLocalSongRadio(track!!)
                        navController.navigate("nowPlaying") {
                            popUpTo("more") { inclusive = true }
                        }
                    },
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
private fun EqualizerScreen(
    viewModel: MuseViewModel,
    playback: PlaybackUiState,
) {
    val effects by viewModel.playback.audioEffects.collectAsStateWithLifecycle()

    LaunchedEffect(playback.currentMediaId) {
        viewModel.playback.refreshAudioEffects()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 30.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ScreenHeader(
                title = "Equalizer",
                action = {
                    Switch(
                        checked = effects.masterEnabled,
                        onCheckedChange = viewModel.playback::setAudioEffectsEnabled,
                        enabled = effects.connected,
                    )
                },
            )
        }

        if (!effects.connected) {
            item {
                EmptyCard(
                    title = "Connecting to Muse audio…",
                    body = "Audio tools are provided by the playback session so they stay consistent in the background.",
                )
            }
        } else if (!effects.sessionReady) {
            item {
                EmptyCard(
                    title = "Start a track to activate audio tools",
                    body = "Muse attaches effects only to the real playback audio session. No fake controls are enabled before a session exists.",
                )
            }
        } else {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "A/B Tune",
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (effects.bypass) {
                                    "Original signal is active"
                                } else {
                                    "Muse processing is active"
                                },
                                color = MuseMuted,
                            )
                        }
                        FilterChip(
                            selected = effects.bypass,
                            onClick = {
                                viewModel.playback.setAudioBypass(!effects.bypass)
                            },
                            label = {
                                Text(if (effects.bypass) "Original" else "Processed")
                            },
                        )
                    }
                }
            }

            if (effects.equalizerAvailable) {
                item {
                    SectionTitle(
                        title = "Equalizer",
                        trailing = "${effects.bandCentersHz.size} bands",
                    )
                }

                if (effects.presetNames.isNotEmpty()) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            itemsIndexed(effects.presetNames) { index, name ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        viewModel.playback.useEqualizerPreset(index)
                                    },
                                    label = { Text(name) },
                                )
                            }
                        }
                    }
                }

                itemsIndexed(
                    effects.bandCentersHz,
                    key = { index, frequency -> "$frequency:$index" },
                ) { index, frequency ->
                    val level = effects.bandLevelsMb.getOrNull(index) ?: 0
                    GlassCard {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    formatFrequency(frequency),
                                    fontWeight = FontWeight.Medium,
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    formatMillibels(level),
                                    color = MuseGreen,
                                )
                            }
                            Slider(
                                value = level.toFloat(),
                                onValueChange = { value ->
                                    viewModel.playback.setEqualizerBand(
                                        index,
                                        value.roundToLong().toInt(),
                                    )
                                },
                                valueRange = effects.bandMinMb.toFloat()..
                                    effects.bandMaxMb.toFloat(),
                                enabled = effects.masterEnabled && !effects.bypass,
                            )
                        }
                    }
                }
            } else {
                item {
                    EmptyCard(
                        title = "Equalizer unavailable",
                        body = "This device or current audio route did not expose an attachable Android Equalizer effect.",
                    )
                }
            }

            if (effects.bassAvailable) {
                item {
                    AudioEffectControl(
                        title = "Bass Boost",
                        subtitle = "Conservative low-frequency enhancement",
                        checked = effects.bassEnabled,
                        onCheckedChange = viewModel.playback::setBassEnabled,
                        value = effects.bassStrength,
                        valueRange = 0..700,
                        onValueChange = viewModel.playback::setBassStrength,
                        controlsEnabled = effects.masterEnabled && !effects.bypass,
                    )
                }
            }

            if (effects.virtualizerAvailable) {
                item {
                    AudioEffectControl(
                        title = "Virtualizer",
                        subtitle = "Device-supported spatial widening",
                        checked = effects.virtualizerEnabled,
                        onCheckedChange = viewModel.playback::setVirtualizerEnabled,
                        value = effects.virtualizerStrength,
                        valueRange = 0..1000,
                        onValueChange = viewModel.playback::setVirtualizerStrength,
                        controlsEnabled = effects.masterEnabled && !effects.bypass,
                    )
                }
            }

            if (effects.loudnessAvailable) {
                item {
                    AudioEffectControl(
                        title = "Loudness Enhancer",
                        subtitle = "Capped at +6 dB to reduce clipping risk",
                        checked = effects.loudnessEnabled,
                        onCheckedChange = viewModel.playback::setLoudnessEnabled,
                        value = effects.loudnessGainMb,
                        valueRange = 0..600,
                        onValueChange = viewModel.playback::setLoudnessGainMb,
                        controlsEnabled = effects.masterEnabled && !effects.bypass,
                        valueLabel = { formatMillibels(it) },
                    )
                }
            }

            if (
                !effects.equalizerAvailable &&
                !effects.bassAvailable &&
                !effects.virtualizerAvailable &&
                !effects.loudnessAvailable
            ) {
                item {
                    EmptyCard(
                        title = "No compatible audio effects",
                        body = "Muse keeps playback untouched rather than pretending unsupported enhancement features are active.",
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioEffectControl(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    controlsEnabled: Boolean,
    valueLabel: (Int) -> String = { "${it / 10}%" },
) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(
                        subtitle,
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = controlsEnabled,
                )
            }
            if (checked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = value.toFloat(),
                        onValueChange = { onValueChange(it.roundToLong().toInt()) },
                        valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                        enabled = controlsEnabled,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        valueLabel(value),
                        color = MuseGreen,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
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
    track: Track?,
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
            TrackArtwork(
                track = track,
                modifier = Modifier.size(46.dp),
                contentDescription = null,
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
        TrackArtwork(
            track = track,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
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
            TrackArtwork(
                track = track,
                modifier = Modifier.size(58.dp),
                contentDescription = null,
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

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex += 1
    }
    return if (unitIndex == 0) {
        "${value.toLong()} ${units[unitIndex]}"
    } else {
        String.format(java.util.Locale.US, "%.1f %s", value, units[unitIndex])
    }
}

private fun formatFrequency(frequencyHz: Int): String =
    when {
        frequencyHz >= 1_000 -> {
            val khz = frequencyHz / 1_000.0
            if (khz % 1.0 == 0.0) {
                "${khz.toInt()} kHz"
            } else {
                String.format(java.util.Locale.US, "%.1f kHz", khz)
            }
        }
        else -> "$frequencyHz Hz"
    }

private fun formatMillibels(valueMb: Int): String =
    String.format(
        java.util.Locale.US,
        "%+.1f dB",
        valueMb / 100.0,
    )

private fun formatDuration(durationMs: Long): String {
    val safe = durationMs.coerceAtLeast(0L)
    val totalSeconds = safe / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
