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
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rezoxnemesis.muse.BuildConfig
import com.rezoxnemesis.muse.MuseMood
import com.rezoxnemesis.muse.MuseViewModel
import com.rezoxnemesis.muse.R
import com.rezoxnemesis.muse.data.LyricsSource
import com.rezoxnemesis.muse.data.Track
import com.rezoxnemesis.muse.data.UserPlaylist
import com.rezoxnemesis.muse.playback.PlaybackUiState
import com.rezoxnemesis.muse.playback.SleepTimerProtocol
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

private enum class LibrarySort(
    val label: String,
) {
    Newest("Newest"),
    Title("Title"),
    Artist("Artist"),
    Album("Album"),
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
    requestedRoute: String? = null,
    onRouteHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val showPrimaryNav = currentRoute in PrimaryDestinations.map { it.route }

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
                    MuseLabScreen(viewModel, navController)
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
    val greeting = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

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
                Text(greeting, style = MaterialTheme.typography.headlineMedium)
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
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    val trending by viewModel.localTrendingTracks.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var selectedMood by remember { mutableStateOf<MuseMood?>(null) }
    var exploreQuery by remember { mutableStateOf("") }
    val moodTracks = selectedMood?.let(viewModel::moodTracks).orEmpty()
    val exploreResults = if (exploreQuery.isBlank()) {
        emptyList()
    } else {
        val needle = exploreQuery.trim()
        library.tracks.filter { track ->
            track.title.contains(needle, ignoreCase = true) ||
                track.artist.contains(needle, ignoreCase = true) ||
                track.album.contains(needle, ignoreCase = true) ||
                track.genre?.contains(needle, ignoreCase = true) == true
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(title = "Explore")
        }
        item {
            OutlinedTextField(
                value = exploreQuery,
                onValueChange = { exploreQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (exploreQuery.isNotBlank()) {
                        IconButton(onClick = { exploreQuery = "" }) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = "Clear Explore search",
                            )
                        }
                    }
                },
                placeholder = {
                    Text("Search songs, artists, albums…")
                },
                shape = RoundedCornerShape(20.dp),
            )
        }

        if (exploreQuery.isNotBlank()) {
            item {
                SectionTitle(
                    title = "Search Results",
                    trailing = "${exploreResults.size} local",
                )
            }

            if (exploreResults.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No local matches",
                        body = "Muse searches only music already available to you on this device.",
                    )
                }
            } else {
                items(exploreResults.take(30), key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        favorite = track.id in favourites,
                        onPlay = { viewModel.playTrack(track) },
                        onFavorite = {
                            viewModel.toggleFavorite(track.id)
                        },
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
        } else {
            item {
                SectionTitle(
                    "Trending in Your Library",
                    trailing = "Local",
                )
            }
            if (trending.isEmpty()) {
                item {
                    EmptyCard(
                        title = "Your library is quiet",
                        body = "Play and like music to shape local trends. Muse does not fabricate online popularity.",
                    )
                }
            } else {
                items(trending.take(8), key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        favorite = track.id in favourites,
                        onPlay = { viewModel.playTrack(track) },
                        onFavorite = {
                            viewModel.toggleFavorite(track.id)
                        },
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

            item {
                SectionTitle(
                    title = "Browse by Mood",
                    trailing = selectedMood?.let {
                        "Local ${it.name} mix"
                    } ?: "6 mixes",
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(MuseMood.entries) { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = {
                                selectedMood =
                                    if (selectedMood == mood) null else mood
                            },
                            label = { Text(mood.name) },
                        )
                    }
                }
            }

            if (selectedMood != null) {
                item {
                    GlassCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "${selectedMood!!.name} Mix",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Built only from your local library using metadata, favourites, recent listening and deterministic rules.",
                                color = MuseMuted,
                            )
                            Button(
                                enabled = moodTracks.isNotEmpty(),
                                onClick = {
                                    viewModel.playTracks(moodTracks)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MuseGreen,
                                    contentColor = MuseBackground,
                                ),
                            ) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                )
                                Text(" Play Mix")
                            }
                        }
                    }
                }

                if (moodTracks.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local matches",
                            body = "Add or play more music and Muse will have more signals to build this mix.",
                        )
                    }
                } else {
                    items(
                        moodTracks.take(12),
                        key = { it.id },
                    ) { track ->
                        TrackRow(
                            track = track,
                            favorite = track.id in favourites,
                            onPlay = {
                                val index = moodTracks
                                    .indexOfFirst { it.id == track.id }
                                viewModel.playTracks(
                                    moodTracks,
                                    index.coerceAtLeast(0),
                                )
                            },
                            onFavorite = {
                                viewModel.toggleFavorite(track.id)
                            },
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
    var selectedSort by remember { mutableStateOf(LibrarySort.Newest) }

    val sortedTracks = when (selectedSort) {
        LibrarySort.Newest -> tracks.sortedByDescending { it.dateAddedSeconds }
        LibrarySort.Title -> tracks.sortedBy { it.title.lowercase() }
        LibrarySort.Artist -> tracks.sortedWith(
            compareBy<Track> { it.artist.lowercase() }
                .thenBy { it.title.lowercase() }
        )
        LibrarySort.Album -> tracks.sortedWith(
            compareBy<Track> { it.album.lowercase() }
                .thenBy { it.trackNumber ?: Int.MAX_VALUE }
                .thenBy { it.title.lowercase() }
        )
    }

    val albums = sortedTracks
        .groupBy { it.album }
        .entries
        .sortedBy { it.key.lowercase() }
    val artists = sortedTracks
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

        if (selectedTab == LibraryTab.Songs) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(LibrarySort.entries) { sort ->
                        FilterChip(
                            selected = selectedSort == sort,
                            onClick = { selectedSort = sort },
                            label = { Text(sort.label) },
                        )
                    }
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
                    items(sortedTracks, key = { it.id }) { track ->
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
        track?.let { activeTrack ->
            val quality = buildList {
                activeTrack.bitrateBps?.let { bitrate ->
                    add("${bitrate / 1_000} kbps")
                }
                activeTrack.sampleRateHz?.let { sampleRate ->
                    add(
                        if (sampleRate >= 1_000) {
                            String.format(
                                java.util.Locale.US,
                                "%.1f kHz",
                                sampleRate / 1_000.0,
                            )
                        } else {
                            "$sampleRate Hz"
                        }
                    )
                }
                activeTrack.genre?.let(::add)
            }
            if (quality.isNotEmpty()) {
                Text(
                    text = quality.joinToString(" • "),
                    color = MuseGreen,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
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
        track?.let(viewModel::loadLyrics)
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
                    onRetry = { track?.let(viewModel::loadLyrics) },
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
                        when {
                            document.source == LyricsSource.Embedded ->
                                "Embedded in audio file"
                            document.synced ->
                                "Synced imported lyrics"
                            else ->
                                "Plain imported lyrics"
                        },
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
                if (document.source == LyricsSource.Imported) {
                    IconButton(onClick = { confirmRemove = true }) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Remove imported lyrics",
                        )
                    }
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
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val tracks = if (artist == null) {
        emptyList()
    } else {
        library.tracks.filter { it.artist == artist }
    }
    val albumCount = tracks.map { it.album }.distinct().size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ScreenHeader(
                title = artist ?: "Artist",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TrackArtwork(
                    track = tracks.firstOrNull(),
                    modifier = Modifier.size(164.dp),
                    contentDescription = artist?.let { "$it artwork" },
                    shape = CircleShape,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    artist ?: "Artist",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${tracks.size} local tracks • $albumCount albums",
                    color = MuseMuted,
                )
            }
        }

        if (tracks.isNotEmpty()) {
            item {
                Button(
                    onClick = { viewModel.playTracksInOrder(tracks) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MuseGreen,
                        contentColor = MuseBackground,
                    ),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Text(" Play Local Tracks")
                }
            }
            item {
                SectionTitle(
                    title = "Local Tracks",
                    trailing = "${tracks.size}",
                )
            }
        } else {
            item {
                EmptyCard(
                    title = "No local tracks",
                    body = "Muse only shows artist content available in your own library.",
                )
            }
        }

        items(tracks, key = { it.id }) { track ->
            TrackRow(
                track = track,
                favorite = track.id in favourites,
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
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    val favourites by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val tracks = if (album == null) {
        emptyList()
    } else {
        library.tracks
            .filter { it.album == album }
            .sortedWith(
                compareBy<Track> { it.trackNumber ?: Int.MAX_VALUE }
                    .thenBy { it.title }
            )
    }
    val representative = tracks.firstOrNull()
    val totalDuration = tracks.sumOf { it.durationMs }
    val year = tracks.mapNotNull { it.year }.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ScreenHeader(
                title = "Album",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TrackArtwork(
                    track = representative,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    album ?: "Album",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (representative != null) {
                    Text(
                        representative.artist,
                        color = MuseMuted,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    buildString {
                        year?.let {
                            append(it)
                            append(" • ")
                        }
                        append(tracks.size)
                        append(if (tracks.size == 1) " song" else " songs")
                        if (totalDuration > 0L) {
                            append(" • ")
                            append(formatDuration(totalDuration))
                        }
                    },
                    color = MuseMuted,
                )
            }
        }

        if (tracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { viewModel.playTracksInOrder(tracks) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseGreen,
                            contentColor = MuseBackground,
                        ),
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Text(" Play")
                    }
                    Button(
                        onClick = { viewModel.playTracksShuffled(tracks) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseSurface,
                        ),
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null)
                        Text(" Shuffle")
                    }
                }
            }
        } else {
            item {
                EmptyCard(
                    title = "Album unavailable",
                    body = "No matching local tracks are currently available.",
                )
            }
        }

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
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    var customMinutes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.playback.refreshSleepTimer()
    }

    val presets = listOf(10, 30, 60, 90)
    val timerLabel = when (timer.mode) {
        SleepTimerProtocol.ModeAfterCurrent -> "After Track"
        SleepTimerProtocol.ModeEndOfQueue -> "End Queue"
        SleepTimerProtocol.ModeDuration -> formatDuration(timer.remainingMs)
        else -> "Off"
    }
    val timerCaption = when (timer.mode) {
        SleepTimerProtocol.ModeAfterCurrent -> "pause when this track finishes"
        SleepTimerProtocol.ModeEndOfQueue -> "pause when the queue finishes"
        SleepTimerProtocol.ModeDuration -> "remaining"
        else -> "Sleep Scene"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 22.dp,
            end = 22.dp,
            bottom = 32.dp,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            ScreenHeader("Sleep Timer", { navController.popBackStack() })
        }

        item {
            Surface(
                modifier = Modifier.size(240.dp),
                shape = CircleShape,
                color = Color(0x5513301A),
                border = androidx.compose.foundation.BorderStroke(2.dp, MuseGreen),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                        Text(
                            timerLabel,
                            style = if (
                                timer.mode == SleepTimerProtocol.ModeDuration ||
                                !timer.active
                            ) {
                                MaterialTheme.typography.displaySmall
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            timerCaption,
                            color = MuseMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 18.dp),
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { minutes ->
                    Button(
                        onClick = {
                            viewModel.playback.startSleepTimer(minutes)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (
                                timer.active &&
                                timer.mode == SleepTimerProtocol.ModeDuration &&
                                timer.remainingMs <= minutes * 60_000L
                            ) {
                                Color(0x443DFF5E)
                            } else {
                                MuseSurface
                            },
                        ),
                    ) {
                        Text("$minutes")
                    }
                }
            }
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { value ->
                            customMinutes = value
                                .filter(Char::isDigit)
                                .take(3)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Custom minutes") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        shape = RoundedCornerShape(18.dp),
                    )
                    Button(
                        enabled = customMinutes.toIntOrNull()
                            ?.let { it in 1..720 } == true,
                        onClick = {
                            customMinutes.toIntOrNull()?.let {
                                viewModel.playback.startSleepTimer(it)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MuseGreen,
                            contentColor = MuseBackground,
                        ),
                    ) {
                        Text("Start")
                    }
                }
            }
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Sleep Scene",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Use a playback boundary instead of a clock.",
                        color = MuseMuted,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            enabled = playback.currentMediaId != null,
                            onClick = viewModel.playback::startSleepAfterCurrentTrack,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MuseSurface,
                            ),
                        ) {
                            Text("After Track")
                        }
                        Button(
                            enabled = playback.queue.isNotEmpty(),
                            onClick = viewModel.playback::startSleepAtEndOfQueue,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MuseSurface,
                            ),
                        ) {
                            Text("End of Queue")
                        }
                    }
                }
            }
        }

        item {
            if (timer.active) {
                Button(
                    onClick = viewModel.playback::cancelSleepTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Cancel Sleep Scene")
                }
            } else {
                Text(
                    "Muse pauses playback at the selected time or playback boundary.",
                    color = MuseMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.libraryState.collectAsStateWithLifecycle()
    val backup by viewModel.backupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val libraryPermission = if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.TIRAMISU
    ) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var libraryPermissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                libraryPermission,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val libraryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        libraryPermissionGranted = granted
        if (granted) {
            viewModel.refreshLibrary()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let(viewModel::exportBackup)
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingRestoreUri = uri
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader("Settings", { navController.popBackStack() })
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.LibraryMusic,
                title = "Library",
                subtitle = "${state.tracks.size} local tracks • tap to rescan",
                onClick = viewModel::refreshLibrary,
            )
        }
        if (!libraryPermissionGranted) {
            item {
                SettingsRow(
                    icon = Icons.Rounded.LibraryMusic,
                    title = "Enable Device Library",
                    subtitle = "Grant Android music access while keeping imported files available",
                    onClick = {
                        libraryPermissionLauncher.launch(libraryPermission)
                    },
                )
            }
        }
        item {
            SettingsRow(
                icon = Icons.Rounded.Timer,
                title = "Sleep Timer",
                subtitle = "Timer and Sleep Scene playback boundaries",
                onClick = { navController.navigate("sleep") },
            )
        }
        item {
            SettingsRow(
                icon = Icons.Rounded.Equalizer,
                title = "Audio Enhancement",
                subtitle = "Capability-aware EQ, bass, virtualizer and loudness",
                onClick = { navController.navigate("equalizer") },
            )
        }
        item {
            SettingsRow(
                icon = Icons.Rounded.Download,
                title = "Imported Audio",
                subtitle = "Manage files chosen through Android's secure picker",
                onClick = { navController.navigate("downloads") },
            )
        }

        item {
            SectionTitle(
                title = "Backup & Restore",
                trailing = "Local JSON",
            )
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.Share,
                title = "Export Muse Backup",
                subtitle = "Playlists, favourites, history and sound profiles",
                onClick = {
                    if (!backup.busy) {
                        exportLauncher.launch("Muse-backup.json")
                    }
                },
            )
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.Download,
                title = "Restore Muse Backup",
                subtitle = "Restore organisation data from a Muse JSON backup",
                onClick = {
                    if (!backup.busy) {
                        restoreLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
                    }
                },
            )
        }

        if (backup.busy) {
            item {
                GlassCard {
                    Text(
                        "Working on backup…",
                        color = MuseMuted,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (backup.message != null || backup.error != null) {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            backup.message ?: backup.error.orEmpty(),
                            color = if (backup.error == null) {
                                MuseGreen
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = viewModel::clearBackupMessage) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(title = "Privacy & Storage")
        }

        item {
            GlassCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                        Spacer(Modifier.size(14.dp))
                        Text(
                            "Local-first by design",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        "Muse does not require an account or paid cloud service for core playback. Imported-file permissions remain Android-scoped. Backups exclude imported file permissions and audio files.",
                        color = MuseMuted,
                    )
                }
            }
        }

        item {
            GlassCard {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MuseGreen,
                    )
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text("About", fontWeight = FontWeight.Medium)
                        Text(
                            "Muse ${BuildConfig.VERSION_NAME} • local-first • no account required",
                            color = MuseMuted,
                        )
                    }
                }
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore Muse backup?") },
            text = {
                Text(
                    "Restoring will replace your current favourites, recent history, playlists and saved sound profiles. Imported audio permissions and original music files are not changed."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingRestoreUri = null
                        viewModel.restoreBackup(uri)
                    },
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingRestoreUri = null },
                ) {
                    Text("Cancel")
                }
            },
        )
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
    var confirmRemove by remember(track?.id) {
        mutableStateOf(false)
    }
    var pendingRingtoneTrack by remember(track?.id) {
        mutableStateOf<Track?>(null)
    }
    var ringtoneMessage by remember(track?.id) {
        mutableStateOf<String?>(null)
    }

    fun applyRingtone(candidate: Track) {
        ringtoneMessage = runCatching {
            android.media.RingtoneManager.setActualDefaultRingtoneUri(
                context,
                android.media.RingtoneManager.TYPE_RINGTONE,
                candidate.uri,
            )
            "Set “${candidate.title}” as the default ringtone."
        }.getOrElse { error ->
            error.message ?: "Android could not set this track as the ringtone."
        }
    }

    val ringtoneSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val candidate = pendingRingtoneTrack
        pendingRingtoneTrack = null
        if (
            candidate != null &&
            android.provider.Settings.System.canWrite(context)
        ) {
            applyRingtone(candidate)
        } else if (candidate != null) {
            ringtoneMessage = "Ringtone permission was not granted."
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("More Options", { navController.popBackStack() })

        if (track == null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyCard(
                    "Nothing playing",
                    "Start a track to see song actions.",
                )
            }
        } else {
            val activeTrack = track!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    bottom = 32.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TrackArtwork(
                                track = activeTrack,
                                modifier = Modifier.size(76.dp),
                                contentDescription = null,
                            )
                            Spacer(Modifier.size(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    activeTrack.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    activeTrack.artist,
                                    color = MuseMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                item {
                    OptionRow(
                        icon = if (activeTrack.id in favourites) {
                            Icons.Rounded.Favorite
                        } else {
                            Icons.Rounded.FavoriteBorder
                        },
                        title = if (activeTrack.id in favourites) {
                            "Remove from Liked Songs"
                        } else {
                            "Add to Liked Songs"
                        },
                        onClick = {
                            viewModel.toggleFavorite(activeTrack.id)
                        },
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        OptionRow(
                            icon = Icons.Rounded.PlaylistPlay,
                            title = "Create a Playlist",
                            subtitle = "Create one before adding this track",
                            onClick = {
                                navController.navigate("playlists")
                            },
                        )
                    }
                } else {
                    items(playlists, key = { it.id }) { playlist ->
                        OptionRow(
                            icon = Icons.Rounded.PlaylistPlay,
                            title = "Add to ${playlist.name}",
                            subtitle = "${playlist.trackIds.size} songs",
                            onClick = {
                                viewModel.addTrackToPlaylist(
                                    playlist.id,
                                    activeTrack.id,
                                )
                            },
                        )
                    }
                }

                item {
                    OptionRow(
                        icon = Icons.Rounded.SkipNext,
                        title = "Play Next",
                        onClick = {
                            viewModel.playback.playNext(activeTrack)
                            navController.popBackStack()
                        },
                    )
                }
                item {
                    OptionRow(
                        icon = Icons.Rounded.QueueMusic,
                        title = "Add to Queue",
                        onClick = {
                            viewModel.playback.addToQueue(activeTrack)
                            navController.popBackStack()
                        },
                    )
                }
                item {
                    OptionRow(
                        icon = Icons.Rounded.Tune,
                        title = "Muse Local Radio",
                        subtitle = "Build a mix from related music already in your library",
                        onClick = {
                            viewModel.playLocalSongRadio(activeTrack)
                            navController.navigate("nowPlaying") {
                                popUpTo("more") { inclusive = true }
                            }
                        },
                    )
                }
                item {
                    OptionRow(
                        icon = Icons.Rounded.Album,
                        title = "View Album",
                        onClick = {
                            viewModel.selectAlbum(activeTrack.album)
                            navController.navigate("album")
                        },
                    )
                }
                item {
                    OptionRow(
                        icon = Icons.Rounded.Person,
                        title = "View Artist",
                        onClick = {
                            viewModel.selectArtist(activeTrack.artist)
                            navController.navigate("artist")
                        },
                    )
                }
                item {
                    OptionRow(
                        icon = Icons.Rounded.Share,
                        title = "Share",
                        subtitle = "Share track metadata through Android",
                        onClick = {
                            val text = "${activeTrack.title} — ${activeTrack.artist}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(
                                Intent.createChooser(intent, "Share with")
                            )
                        },
                    )
                }

                if (!activeTrack.managedByMuse) {
                    item {
                        OptionRow(
                            icon = Icons.Rounded.Notifications,
                            title = "Set as Ringtone",
                            subtitle = "Uses Android's protected system ringtone setting",
                            onClick = {
                                if (
                                    android.provider.Settings.System.canWrite(context)
                                ) {
                                    applyRingtone(activeTrack)
                                } else {
                                    pendingRingtoneTrack = activeTrack
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                        android.net.Uri.parse("package:${context.packageName}"),
                                    )
                                    ringtoneSettingsLauncher.launch(intent)
                                }
                            },
                        )
                    }
                }

                ringtoneMessage?.let { message ->
                    item {
                        GlassCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    message,
                                    color = MuseMuted,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = { ringtoneMessage = null },
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }

                if (activeTrack.managedByMuse) {
                    item {
                        OptionRow(
                            icon = Icons.Rounded.Download,
                            title = "Imported File",
                            subtitle = "Open Muse-managed imported media",
                            onClick = {
                                navController.navigate("downloads")
                            },
                        )
                    }
                }

                item {
                    GlassCard {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                            Spacer(Modifier.size(14.dp))
                            Column {
                                Text("Song Info")
                                Text(
                                    buildString {
                                        append(activeTrack.album)
                                        activeTrack.year?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                        activeTrack.albumArtist?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                        activeTrack.genre?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                        activeTrack.bitrateBps?.let {
                                            append(" • ")
                                            append(it / 1_000)
                                            append(" kbps")
                                        }
                                        activeTrack.sampleRateHz?.let {
                                            append(" • ")
                                            append(
                                                String.format(
                                                    java.util.Locale.US,
                                                    "%.1f kHz",
                                                    it / 1_000.0,
                                                )
                                            )
                                        }
                                        activeTrack.mimeType?.let {
                                            append(" • ")
                                            append(it)
                                        }
                                        activeTrack.sizeBytes?.let {
                                            append(" • ")
                                            append(formatFileSize(it))
                                        }
                                    },
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }

                if (activeTrack.managedByMuse) {
                    item {
                        GlassCard(
                            modifier = Modifier.clickable {
                                confirmRemove = true
                            },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.size(14.dp))
                                Text(
                                    "Remove imported file from Muse",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }

            if (confirmRemove) {
                AlertDialog(
                    onDismissRequest = { confirmRemove = false },
                    title = { Text("Remove from Muse?") },
                    text = {
                        Text(
                            "Muse will forget “${activeTrack.title}” and release saved access. The original file is not deleted."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.removeManagedMedia(activeTrack)
                                confirmRemove = false
                                navController.popBackStack()
                            },
                        ) {
                            Text(
                                "Remove",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { confirmRemove = false },
                        ) {
                            Text("Cancel")
                        }
                    },
                )
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
    val soundProfiles by viewModel.soundProfiles.collectAsStateWithLifecycle()
    val selectedSoundProfileId by viewModel.selectedSoundProfileId.collectAsStateWithLifecycle()
    var profileName by remember { mutableStateOf("") }

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

            item {
                SectionTitle(
                    title = "Sound Profiles",
                    trailing = selectedSoundProfileId
                        ?.let { selectedId ->
                            soundProfiles
                                .firstOrNull { it.id == selectedId }
                                ?.name
                                ?.let { "Active: $it" }
                        }
                        ?: if (soundProfiles.isEmpty()) {
                            "Local"
                        } else {
                            "${soundProfiles.size} saved"
                        },
                )
            }

            if (selectedSoundProfileId != null) {
                item {
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Smart Resume",
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "The active sound profile will be restored when Muse gets a playback audio session.",
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(
                                onClick = viewModel::clearSelectedSoundProfile,
                            ) {
                                Text("Stop Auto-Restore")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Save current tuning") },
                    placeholder = { Text("Profile name") },
                    trailingIcon = {
                        TextButton(
                            enabled = profileName.isNotBlank() && effects.sessionReady,
                            onClick = {
                                viewModel.saveCurrentSoundProfile(profileName)
                                profileName = ""
                            },
                        ) {
                            Text("Save")
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                )
            }

            if (soundProfiles.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No sound profiles yet",
                        body = "Tune the available effects and save the result here. Profiles stay local on this device.",
                    )
                }
            } else {
                items(soundProfiles, key = { it.id }) { profile ->
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    profile.name,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    buildString {
                                        append(profile.eqLevelsMb.size)
                                        append(" EQ bands")
                                        if (profile.bassEnabled) append(" • Bass")
                                        if (profile.virtualizerEnabled) append(" • Virtualizer")
                                        if (profile.loudnessEnabled) append(" • Loudness")
                                    },
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            TextButton(
                                enabled = effects.sessionReady,
                                onClick = {
                                    viewModel.applySoundProfile(profile)
                                },
                            ) {
                                Text(
                                    if (selectedSoundProfileId == profile.id) {
                                        "Active"
                                    } else {
                                        "Apply"
                                    }
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteSoundProfile(profile.id)
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete sound profile",
                                )
                            }
                        }
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
                                "3D / Spatial Audio",
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                when {
                                    !effects.spatialSupported ->
                                        "Not supported by this Android device"
                                    effects.spatialEnabled &&
                                        effects.spatialAvailable &&
                                        effects.headTrackerAvailable ->
                                        "System spatial audio active • head tracking available"
                                    effects.spatialEnabled &&
                                        effects.spatialAvailable ->
                                        "System spatial audio active"
                                    effects.spatialSupported &&
                                        !effects.spatialAvailable ->
                                        "Supported, but unavailable on the current audio route"
                                    else ->
                                        "Supported, but disabled in Android sound settings"
                                },
                                color = MuseMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (effects.spatialSupported) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_SOUND_SETTINGS
                                    )
                                    context.startActivity(intent)
                                },
                            ) {
                                Text("Settings")
                            }
                        }
                    }
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
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val playCounts by viewModel.playCounts.collectAsStateWithLifecycle()
    val topPlayed by viewModel.topPlayedTracks.collectAsStateWithLifecycle()
    val totalStarts = playCounts.values.sum()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Muse Lab") }

        item {
            GlassCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Listening Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        if (totalStarts == 0) {
                            "Play music in Muse to build private on-device listening insights."
                        } else {
                            "$totalStarts playback starts recorded locally"
                        },
                        color = MuseMuted,
                    )
                    Text(
                        "Counts are updated only when a new track actually begins playback. No listening data leaves this device.",
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        if (topPlayed.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Most Played",
                    trailing = "Local",
                )
            }
            items(topPlayed.take(5), key = { it.first.id }) { (track, count) ->
                GlassCard(
                    modifier = Modifier.clickable {
                        viewModel.playTrack(track)
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrackArtwork(
                            track = track,
                            modifier = Modifier.size(52.dp),
                            contentDescription = null,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
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
                        Text(
                            "$count×",
                            color = MuseGreen,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        item {
            SettingsRow(
                Icons.Rounded.QueueMusic,
                "Play Queue",
                "Current session",
                { navController.navigate("queue") },
            )
        }
        item {
            SettingsRow(
                Icons.Rounded.PlaylistPlay,
                "Playlists",
                "Liked songs and playlists",
                { navController.navigate("playlists") },
            )
        }
        item {
            SettingsRow(
                Icons.Rounded.Timer,
                "Sleep Scene",
                "Timers and playback-boundary stopping",
                { navController.navigate("sleep") },
            )
        }
        item {
            SettingsRow(
                Icons.Rounded.Download,
                "Downloads",
                "Muse-managed offline files",
                { navController.navigate("downloads") },
            )
        }
        item {
            SettingsRow(
                Icons.Rounded.Settings,
                "Settings",
                "Library, privacy and playback",
                { navController.navigate("settings") },
            )
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
