package com.rezoxnemesis.muse.ui

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.rezoxnemesis.muse.ui.theme.MuseGlow
import com.rezoxnemesis.muse.ui.theme.MuseMuted
import com.rezoxnemesis.muse.ui.theme.MuseSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

private fun museNotificationPermissionGranted(
    context: android.content.Context,
): Boolean =
    android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

private fun openMuseNotificationSettings(
    context: android.content.Context,
) {
    val intent = Intent(
        android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
    ).apply {
        putExtra(
            android.provider.Settings.EXTRA_APP_PACKAGE,
            context.packageName,
        )
    }
    context.startActivity(intent)
}

private data class PrimaryDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private enum class ExploreCategory(
    val label: String,
) {
    All("All"),
    Songs("Songs"),
    Albums("Albums"),
    Artists("Artists"),
    Playlists("Playlists"),
}

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

private enum class PlaylistTab {
    All,
    Created,
    Liked,
}

private enum class DownloadTab {
    Songs,
    Albums,
    Playlists,
}

private val PrimaryDestinations = listOf(
    PrimaryDestination("home", "Home", Icons.Rounded.Home),
    PrimaryDestination("explore", "Explore", Icons.Rounded.Explore),
    PrimaryDestination("library", "Library", Icons.Rounded.LibraryMusic),
    PrimaryDestination("equalizer", "Equalizer", Icons.Rounded.Equalizer),
    PrimaryDestination("tools", "Muse Lab", Icons.Rounded.Tune),
)

private val PrimaryRoutes = PrimaryDestinations.mapTo(mutableSetOf()) { it.route }

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
    val context = LocalContext.current
    val visualPreferences = remember(context) {
        context.getSharedPreferences(
            "muse_visual_preferences",
            android.content.Context.MODE_PRIVATE,
        )
    }
    var visualIntensity by remember {
        mutableStateOf(
            MuseVisualIntensity.fromStored(
                visualPreferences.getString(
                    "visual_intensity",
                    null,
                )
            )
        )
    }
    var visualMode by remember {
        mutableStateOf(
            MuseVisualMode.fromStored(
                visualPreferences.getString(
                    "visual_mode",
                    null,
                )
            )
        )
    }
    var rainLevel by remember {
        mutableStateOf(
            MuseRainLevel.fromStored(
                visualPreferences.getString(
                    "rain_level",
                    null,
                )
            )
        )
    }
    val visualDescriptor = buildString {
        currentTrack?.let { track ->
            append(track.title)
            append(' ')
            append(track.artist)
            append(' ')
            append(track.album)
            track.genre?.let {
                append(' ')
                append(it)
            }
        }
    }
    val resolvedVisualProfile = remember(
        visualMode,
        visualDescriptor,
    ) {
        resolveMuseVisualProfile(
            mode = visualMode,
            descriptor = visualDescriptor,
        )
    }
    var touchRipple by remember {
        mutableStateOf<MuseTouchRipple?>(null)
    }
    val showPrimaryNav = currentRoute in PrimaryRoutes

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
            .background(MuseBackground)
            .pointerInput(Unit) {
                var sequence = 0L
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                    )
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val height = size.height.toFloat().coerceAtLeast(1f)
                    sequence += 1L
                    touchRipple = MuseTouchRipple(
                        id = sequence,
                        xFraction = (down.position.x / width)
                            .coerceIn(0f, 1f),
                        yFraction = (down.position.y / height)
                            .coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        MuseNativeBotanicalBackdrop(
            route = currentRoute,
            active = playback.isPlaying,
            intensity = visualIntensity,
            profile = resolvedVisualProfile,
            rainLevel = rainLevel,
            touchRipple = touchRipple,
            modifier = Modifier.fillMaxSize(),
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            bottomBar = {
                if (showPrimaryNav) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AnimatedVisibility(
                            visible = playback.currentMediaId != null,
                            enter = fadeIn(animationSpec = tween(140)) +
                                slideInVertically(
                                    animationSpec = tween(180),
                                    initialOffsetY = { it / 2 },
                                ),
                            exit = fadeOut(animationSpec = tween(100)) +
                                slideOutVertically(
                                    animationSpec = tween(135),
                                    targetOffsetY = { it / 3 },
                                ),
                        ) {
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
                enterTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val primaryTabHop =
                        fromRoute in PrimaryRoutes && toRoute in PrimaryRoutes

                    if (primaryTabHop) {
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = 120,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    } else {
                        val nowPlaying = toRoute == "nowPlaying"
                        fadeIn(
                            animationSpec = tween(
                                durationMillis = if (nowPlaying) 180 else 135,
                            ),
                        ) +
                            scaleIn(
                                animationSpec = tween(
                                    durationMillis = if (nowPlaying) 205 else 160,
                                ),
                                initialScale = if (nowPlaying) 0.945f else 0.982f,
                            ) +
                            slideInVertically(
                                animationSpec = tween(
                                    durationMillis = if (nowPlaying) 205 else 165,
                                ),
                                initialOffsetY = { height ->
                                    if (nowPlaying) height / 9 else height / 24
                                },
                            )
                    }
                },
                exitTransition = {
                    val fromRoute = initialState.destination.route
                    val toRoute = targetState.destination.route
                    val primaryTabHop =
                        fromRoute in PrimaryRoutes && toRoute in PrimaryRoutes

                    if (primaryTabHop) {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 90,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    } else {
                        fadeOut(
                            animationSpec = tween(
                                durationMillis = 105,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    }
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(135)) +
                        slideInVertically(
                            animationSpec = tween(165),
                            initialOffsetY = { height -> -height / 28 },
                        )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(100)) +
                        slideOutVertically(
                            animationSpec = tween(145),
                            targetOffsetY = { height -> height / 24 },
                        )
                },
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
                    MuseLabScreen(
                        viewModel = viewModel,
                        navController = navController,
                        visualMode = visualMode,
                        resolvedVisualProfile = resolvedVisualProfile,
                        rainLevel = rainLevel,
                        visualIntensity = visualIntensity,
                        onVisualModeChange = { next ->
                            visualMode = next
                            visualPreferences.edit()
                                .putString("visual_mode", next.storedValue)
                                .apply()
                        },
                        onRainLevelChange = { next ->
                            rainLevel = next
                            visualPreferences.edit()
                                .putString("rain_level", next.storedValue)
                                .apply()
                        },
                        onVisualIntensityChange = { next ->
                            visualIntensity = next
                            visualPreferences.edit()
                                .putString(
                                    "visual_intensity",
                                    next.storedValue,
                                )
                                .apply()
                        },
                    )
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
                    SettingsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        visualIntensity = visualIntensity,
                        onVisualIntensityChange = { next ->
                            visualIntensity = next
                            visualPreferences.edit()
                                .putString(
                                    "visual_intensity",
                                    next.storedValue,
                                )
                                .apply()
                        },
                    )
                }
                composable("diagnostics") {
                    DiagnosticsScreen(viewModel, navController)
                }
                composable("more") {
                    MoreOptionsScreen(viewModel, navController)
                }
            }
        }
    }
}

private fun NavHostController.popBackOrHome() {
    if (!popBackStack()) {
        goHomeFromDetail()
    }
}

private fun NavHostController.goHomeFromDetail() {
    // Reveal the existing Home destination when it is already underneath the
    // detail screen. This avoids the blank botanical frame seen on real devices.
    if (popBackStack("home", inclusive = false)) {
        return
    }

    navigate("home") {
        popUpTo(graph.startDestinationId) {
            inclusive = false
        }
        launchSingleTop = true
        restoreState = false
    }
}

@Composable
private fun MuseBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val routeIndex = PrimaryDestinations
        .indexOfFirst { it.route == currentRoute }
        .coerceAtLeast(0)

    var visualIndex by remember { mutableStateOf(routeIndex) }
    var travelFromIndex by remember { mutableStateOf(routeIndex) }
    val travel = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    LaunchedEffect(routeIndex) {
        if (routeIndex != visualIndex) {
            travelFromIndex = visualIndex
            visualIndex = routeIndex
            travel.snapTo(0f)
            travel.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 340,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    MuseGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        variant = MuseGlassVariant.Strong,
        cornerRadius = 30.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 5.dp, vertical = 5.dp),
        ) {
            val itemWidth = maxWidth / PrimaryDestinations.size
            val itemWidthPx = with(density) { itemWidth.toPx() }
            val progress = travel.value.coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(progress)
            val from = travelFromIndex.toFloat()
            val to = visualIndex.toFloat()
            val direction = kotlin.math.sign(to - from)
            val distance = kotlin.math.abs(to - from).coerceAtLeast(1f)
            val animatedIndex = from + (to - from) * eased
            val stretchPhase = kotlin.math.sin(
                Math.PI.toFloat() * progress,
            ).coerceAtLeast(0f)

            // This indicator never changes its layout width during travel.
            // Translation/stretch are GPU transforms, so the bottom bar no
            // longer remeasures every animation frame and therefore does not
            // "jump" when navigation content is doing work at the same time.
            val stretch = 1f +
                stretchPhase *
                (0.40f + 0.10f * distance.coerceAtMost(3f))
            val squashY = 1f + stretchPhase * 0.055f

            Box(
                modifier = Modifier
                    .offset(x = itemWidth * 0.04f)
                    .width(itemWidth * 0.92f)
                    .height(52.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        translationX = itemWidthPx * animatedIndex
                        scaleX = stretch
                        scaleY = squashY
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(
                            pivotFractionX = when {
                                direction > 0f -> 0.18f
                                direction < 0f -> 0.82f
                                else -> 0.5f
                            },
                            pivotFractionY = 0.5f,
                        )
                    }
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                MuseGreen.copy(alpha = 0.12f),
                                Color(0xFF93FF7B).copy(alpha = 0.35f),
                                MuseGreen.copy(alpha = 0.15f),
                            )
                        ),
                        shape = RoundedCornerShape(26.dp),
                    )
                    .border(
                        width = 0.9.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color(0xFFB6FF98).copy(alpha = 0.68f),
                                MuseGreen.copy(alpha = 0.44f),
                            )
                        ),
                        shape = RoundedCornerShape(26.dp),
                    ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrimaryDestinations.forEachIndexed { index, destination ->
                    MuseMotionNavItem(
                        label = destination.label,
                        icon = destination.icon,
                        selected = visualIndex == index,
                        onClick = {
                            if (index == visualIndex) {
                                onNavigate(destination.route)
                                return@MuseMotionNavItem
                            }

                            travelFromIndex = visualIndex
                            visualIndex = index

                            scope.launch {
                                travel.stop()
                                travel.snapTo(0f)
                                travel.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 340,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                            }

                            onNavigate(destination.route)
                        },
                    )
                }
            }
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
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    val context = LocalContext.current
    val voiceSearchIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Search your Muse library",
            )
        }
    }
    val voiceSearchAvailable = remember(context) {
        voiceSearchIntent.resolveActivity(context.packageManager) != null
    }
    var homeNotificationPermissionGranted by remember {
        mutableStateOf(museNotificationPermissionGranted(context))
    }
    val homeNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        homeNotificationPermissionGranted = granted
        if (granted) {
            openMuseNotificationSettings(context)
        }
    }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let(viewModel::setSearchQuery)
        }
    }
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
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth(),
            ) {
                MuseGlassAction(
                    onClick = { navController.navigate("tools") },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(48.dp),
                    variant = MuseGlassVariant.Elevated,
                    cornerRadius = 18.dp,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = "Open Muse menu",
                            tint = MuseGreen,
                        )
                    }
                }

                MuseGlassAction(
                    onClick = {
                        if (
                            android.os.Build.VERSION.SDK_INT >=
                            android.os.Build.VERSION_CODES.TIRAMISU &&
                            !homeNotificationPermissionGranted
                        ) {
                            homeNotificationPermissionLauncher.launch(
                                android.Manifest.permission.POST_NOTIFICATIONS,
                            )
                        } else {
                            openMuseNotificationSettings(context)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp),
                    variant = MuseGlassVariant.Elevated,
                    cornerRadius = 18.dp,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Notifications,
                            contentDescription = "Open notification settings",
                            tint = Color.White,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MuseBrandMark(
                        modifier = Modifier.size(126.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        greeting,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "FEEL THE MUSIC",
                        color = MuseGreen,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 28.dp,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                    },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear search",
                                )
                            }
                        } else if (voiceSearchAvailable) {
                            IconButton(
                                onClick = {
                                    voiceSearchLauncher.launch(
                                        voiceSearchIntent
                                    )
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Mic,
                                    contentDescription = "Voice search",
                                    tint = MuseGreen,
                                )
                            }
                        }
                    },
                    placeholder = {
                        Text(
                            "Search songs, artists, albums…",
                            color = MuseMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { navController.navigate("library") }
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
            }
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
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var selectedMood by remember { mutableStateOf<MuseMood?>(null) }
    var selectedCategory by remember {
        mutableStateOf(ExploreCategory.All)
    }
    var exploreQuery by remember { mutableStateOf("") }

    val needle = exploreQuery.trim()
    val categoryTracks = if (needle.isBlank()) {
        library.tracks
    } else {
        library.tracks.filter { track ->
            track.title.contains(needle, ignoreCase = true) ||
                track.artist.contains(needle, ignoreCase = true) ||
                track.album.contains(needle, ignoreCase = true) ||
                track.genre?.contains(needle, ignoreCase = true) == true
        }
    }
    val categoryAlbums = categoryTracks
        .groupBy { it.album }
        .entries
        .sortedBy { it.key.lowercase() }
    val categoryArtists = categoryTracks
        .groupBy { it.artist }
        .entries
        .sortedBy { it.key.lowercase() }
    val categoryPlaylists = playlists.filter { playlist ->
        needle.isBlank() ||
            playlist.name.contains(needle, ignoreCase = true)
    }
    val moodTracks = selectedMood?.let(viewModel::moodTracks).orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Explore",
                action = {
                    IconButton(
                        onClick = {
                            if (exploreQuery.isBlank()) {
                                selectedCategory = ExploreCategory.All
                            } else {
                                exploreQuery = ""
                            }
                        },
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Explore search",
                            tint = MuseGreen,
                        )
                    }
                },
            )
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 28.dp,
            ) {
                OutlinedTextField(
                    value = exploreQuery,
                    onValueChange = { exploreQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
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
                        Text(
                            "Search songs, artists, albums…",
                            color = MuseMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    shape = RoundedCornerShape(28.dp),
                )
            }
        }

        item {
            MuseSlidingTabRow(
                labels = ExploreCategory.entries.map { it.label },
                selectedIndex = ExploreCategory.entries.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth(),
                onSelected = { index ->
                    selectedCategory = ExploreCategory.entries[index]
                    selectedMood = null
                },
            )
        }

        when {
            selectedCategory == ExploreCategory.Songs -> {
                item {
                    SectionTitle(
                        title = if (needle.isBlank()) "Songs" else "Song Results",
                        trailing = "${categoryTracks.size} local",
                    )
                }
                if (categoryTracks.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local songs",
                            body = "Muse only shows music available in your local library.",
                        )
                    }
                } else {
                    items(categoryTracks.take(40), key = { it.id }) { track ->
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
            }

            selectedCategory == ExploreCategory.Albums -> {
                item {
                    SectionTitle(
                        title = if (needle.isBlank()) "Albums" else "Album Results",
                        trailing = "${categoryAlbums.size} local",
                    )
                }
                if (categoryAlbums.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local albums",
                            body = "Album results come from your device metadata.",
                        )
                    }
                } else {
                    items(
                        categoryAlbums.take(40),
                        key = { it.key },
                    ) { (album, tracks) ->
                        SettingsRow(
                            icon = Icons.Rounded.Album,
                            title = album,
                            subtitle = buildString {
                                append(tracks.size)
                                append(if (tracks.size == 1) " song" else " songs")
                                tracks.firstOrNull()
                                    ?.artist
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let {
                                        append(" • ")
                                        append(it)
                                    }
                            },
                            onClick = {
                                viewModel.selectAlbum(album)
                                navController.navigate("album")
                            },
                        )
                    }
                }
            }

            selectedCategory == ExploreCategory.Artists -> {
                item {
                    SectionTitle(
                        title = if (needle.isBlank()) "Artists" else "Artist Results",
                        trailing = "${categoryArtists.size} local",
                    )
                }
                if (categoryArtists.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local artists",
                            body = "Artist results come from your device metadata.",
                        )
                    }
                } else {
                    items(
                        categoryArtists.take(40),
                        key = { it.key },
                    ) { (artist, tracks) ->
                        SettingsRow(
                            icon = Icons.Rounded.Person,
                            title = artist,
                            subtitle = "${tracks.size} " +
                                if (tracks.size == 1) "song" else "songs",
                            onClick = {
                                viewModel.selectArtist(artist)
                                navController.navigate("artist")
                            },
                        )
                    }
                }
            }

            selectedCategory == ExploreCategory.Playlists -> {
                item {
                    SectionTitle(
                        title = if (needle.isBlank()) {
                            "Playlists"
                        } else {
                            "Playlist Results"
                        },
                        trailing = "${categoryPlaylists.size} local",
                    )
                }
                if (categoryPlaylists.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local playlists",
                            body = if (needle.isBlank()) {
                                "Create a playlist and it will appear here."
                            } else {
                                "No playlist name matches this search."
                            },
                        )
                    }
                } else {
                    items(
                        categoryPlaylists.take(40),
                        key = { it.id },
                    ) { playlist ->
                        SettingsRow(
                            icon = Icons.Rounded.PlaylistPlay,
                            title = playlist.name,
                            subtitle = "${playlist.trackIds.size} " +
                                if (playlist.trackIds.size == 1) {
                                    "song"
                                } else {
                                    "songs"
                                },
                            onClick = {
                                viewModel.selectPlaylist(playlist.id)
                                navController.navigate("playlistDetail")
                            },
                        )
                    }
                }
            }

            needle.isNotBlank() -> {
                item {
                    SectionTitle(
                        title = "Search Results",
                        trailing = "${categoryTracks.size} local",
                    )
                }
                if (categoryTracks.isEmpty()) {
                    item {
                        EmptyCard(
                            title = "No local matches",
                            body = "Muse searches only music already available to you on this device.",
                        )
                    }
                } else {
                    items(categoryTracks.take(30), key = { it.id }) { track ->
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
            }

            else -> {
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
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(trending.take(8), key = { it.id }) { track ->
                                TrackPoster(
                                    track = track,
                                    favorite = track.id in favourites,
                                    onClick = { viewModel.playTrack(track) },
                                )
                            }
                        }
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MuseMood.entries
                            .chunked(3)
                            .forEach { moods ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    moods.forEach { mood ->
                                        MuseMoodTile(
                                            mood = mood,
                                            selected = selectedMood == mood,
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                selectedMood =
                                                    if (selectedMood == mood) {
                                                        null
                                                    } else {
                                                        mood
                                                    }
                                            },
                                        )
                                    }
                                }
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
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "Built only from your local library using metadata, favourites, recent listening and deterministic rules.",
                                    color = MuseMuted,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
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
    val recent by viewModel.recentTracks.collectAsStateWithLifecycle()
    val managed by viewModel.managedMediaState.collectAsStateWithLifecycle()
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
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 26.dp,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                    },
                    placeholder = {
                        Text(
                            "Search your library",
                            color = MuseMuted,
                        )
                    },
                    shape = RoundedCornerShape(26.dp),
                )
            }
        }
        item {
            MuseSlidingTabRow(
                labels = LibraryTab.entries.map { it.name },
                selectedIndex = LibraryTab.entries.indexOf(selectedTab),
                modifier = Modifier.fillMaxWidth(),
                onSelected = { index ->
                    selectedTab = LibraryTab.entries[index]
                },
            )
        }

        if (selectedTab == LibraryTab.Songs && query.isBlank()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    SettingsRow(
                        icon = Icons.Rounded.Favorite,
                        title = "Liked Songs",
                        subtitle = "${liked.size} local favourites",
                        onClick = { navController.navigate("liked") },
                    )
                    SettingsRow(
                        icon = Icons.Rounded.AccessTime,
                        title = "Recently Played",
                        subtitle = "${recent.size} tracks in local history",
                        onClick = { navController.navigate("recent") },
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Download,
                        title = "Imported Audio",
                        subtitle = "${managed.tracks.size} Muse-managed files",
                        onClick = { navController.navigate("downloads") },
                    )
                    SettingsRow(
                        icon = Icons.Rounded.PlaylistPlay,
                        title = "My Playlists",
                        subtitle = "${playlists.size} local playlists",
                        onClick = { navController.navigate("playlists") },
                    )
                }
            }
            item {
                SectionTitle(
                    title = "Recently Added",
                    trailing = "${sortedTracks.size} local",
                )
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

    BackHandler {
        navController.goHomeFromDetail()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 22.dp,
                end = 22.dp,
                bottom = 28.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(
            title = "Now Playing",
            onBack = { navController.goHomeFromDetail() },
            action = {
                IconButton(onClick = { navController.navigate("more") }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                }
            },
        )
        Spacer(Modifier.height(18.dp))
        MuseGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            variant = MuseGlassVariant.Elevated,
            cornerRadius = 30.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            ) {
                TrackArtwork(
                    track = track,
                    modifier = Modifier.fillMaxSize(),
                )
                track?.bitrateBps?.takeIf { it > 0 }?.let { bitrate ->
                    MuseGlassSurface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        variant = MuseGlassVariant.Strong,
                        cornerRadius = 18.dp,
                    ) {
                        Text(
                            text = "${bitrate / 1_000} kbps",
                            color = MuseGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 7.dp,
                            ),
                        )
                    }
                }
            }
        }
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
            MuseGlassAction(
                onClick = viewModel.playback::playPause,
                modifier = Modifier.size(76.dp),
                variant = MuseGlassVariant.Selected,
                cornerRadius = 38.dp,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playback.isPlaying) "Pause" else "Play",
                        tint = Color.White,
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NowPlayingAction(
                icon = Icons.Rounded.Explore,
                label = "Flow",
                modifier = Modifier.weight(1f),
                onClick = {
                    track?.let(viewModel::playMuseFlow)
                },
            )
            NowPlayingAction(
                icon = Icons.Rounded.QueueMusic,
                label = "Queue",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("queue") },
            )
            NowPlayingAction(
                icon = Icons.Rounded.MusicNote,
                label = "Lyrics",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("lyrics") },
            )
            NowPlayingAction(
                icon = Icons.Rounded.MoreVert,
                label = "More",
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("more") },
            )
        }

        Spacer(Modifier.height(18.dp))
        MuseGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            variant = MuseGlassVariant.Standard,
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Playback Tuning",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Session controls • new queues start at 1.0×",
                            color = MuseMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(
                        onClick = viewModel.playback::resetPlaybackTuning,
                        enabled = playback.connected,
                    ) {
                        Text("Reset")
                    }
                }

                Text(
                    String.format(
                        java.util.Locale.US,
                        "Speed  %.2f×",
                        playback.playbackSpeed,
                    ),
                    color = MuseGreen,
                    style = MaterialTheme.typography.labelLarge,
                )
                Slider(
                    value = playback.playbackSpeed,
                    onValueChange = viewModel.playback::setPlaybackSpeed,
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    enabled = playback.connected,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    if (playback.playbackPitch in 0.995f..1.005f) {
                        "Pitch  Original"
                    } else {
                        String.format(
                            java.util.Locale.US,
                            "Pitch  %.2f×",
                            playback.playbackPitch,
                        )
                    },
                    color = MuseGreen,
                    style = MaterialTheme.typography.labelLarge,
                )
                Slider(
                    value = playback.playbackPitch,
                    onValueChange = viewModel.playback::setPlaybackPitch,
                    valueRange = 0.75f..1.25f,
                    steps = 9,
                    enabled = playback.connected,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Speed changes preserve the original pitch by default. Pitch shift is separate and deliberately limited.",
                    color = MuseMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
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
    val dragThresholdPx = with(LocalDensity.current) {
        48.dp.toPx()
    }
    val queue = playback.queue
    val currentQueueIndex = playback.currentIndex
        .takeIf { it in queue.indices }
        ?: if (queue.isNotEmpty()) 0 else -1
    val currentItem = queue.getOrNull(currentQueueIndex)
    val upcoming = queue
        .mapIndexed { index, item -> index to item }
        .filter { (index, _) -> index > currentQueueIndex }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Play Queue",
                onBack = { navController.popBackStack() },
            )
        }

        if (queue.isEmpty()) {
            item {
                EmptyCard(
                    "Queue is empty",
                    "Choose a song from your library to start listening.",
                )
            }
        } else {
            currentItem?.let { item ->
                item {
                    SectionTitle(
                        title = "Now Playing",
                        trailing = if (playback.isPlaying) "Playing" else "Paused",
                    )
                }
                item {
                    MuseGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        variant = MuseGlassVariant.Selected,
                        cornerRadius = 24.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            MuseGlassSurface(
                                modifier = Modifier.size(52.dp),
                                variant = MuseGlassVariant.Strong,
                                cornerRadius = 18.dp,
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Rounded.MusicNote,
                                        contentDescription = null,
                                        tint = MuseGreen,
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.size(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.title.ifBlank { "Unknown title" },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    item.artist,
                                    color = MuseMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                Icons.Rounded.Equalizer,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                        }
                    }
                }
            }

            if (upcoming.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Up Next",
                        trailing = "${upcoming.size}",
                    )
                }

                items(
                    items = upcoming,
                    key = { (index, item) -> "${item.mediaId}:$index" },
                ) { (index, item) ->
                    MuseGlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        variant = MuseGlassVariant.Standard,
                        cornerRadius = 20.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.title.ifBlank { "Unknown title" },
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    item.artist,
                                    color = MuseMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            TextButton(
                                enabled = index > currentQueueIndex + 1,
                                onClick = {
                                    viewModel.playback.moveQueueItem(index, index - 1)
                                },
                            ) { Text("↑") }
                            TextButton(
                                enabled = index < queue.lastIndex,
                                onClick = {
                                    viewModel.playback.moveQueueItem(index, index + 1)
                                },
                            ) { Text("↓") }

                            var dragIndex by remember(item.mediaId, index) {
                                mutableStateOf(index)
                            }
                            var dragDistance by remember(item.mediaId, index) {
                                mutableStateOf(0f)
                            }
                            Icon(
                                Icons.Rounded.DragHandle,
                                contentDescription = "Drag to reorder queue",
                                tint = MuseMuted,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp)
                                    .pointerInput(
                                        item.mediaId,
                                        index,
                                        queue.size,
                                    ) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                dragIndex = index
                                                dragDistance = 0f
                                            },
                                            onDragEnd = {
                                                dragDistance = 0f
                                            },
                                            onDragCancel = {
                                                dragDistance = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragDistance += dragAmount.y

                                                while (
                                                    dragDistance >= dragThresholdPx &&
                                                    dragIndex < queue.lastIndex
                                                ) {
                                                    viewModel.playback.moveQueueItem(
                                                        dragIndex,
                                                        dragIndex + 1,
                                                    )
                                                    dragIndex += 1
                                                    dragDistance -= dragThresholdPx
                                                }

                                                while (
                                                    dragDistance <= -dragThresholdPx &&
                                                    dragIndex > currentQueueIndex + 1
                                                ) {
                                                    viewModel.playback.moveQueueItem(
                                                        dragIndex,
                                                        dragIndex - 1,
                                                    )
                                                    dragIndex -= 1
                                                    dragDistance += dragThresholdPx
                                                }
                                            },
                                        )
                                    },
                            )
                            IconButton(
                                onClick = {
                                    viewModel.playback.removeQueueItem(index)
                                },
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Remove from queue",
                                )
                            }
                        }
                    }
                }
            }

            item {
                MuseGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    variant = MuseGlassVariant.Strong,
                    cornerRadius = 22.dp,
                ) {
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Save queue as playlist") },
                        placeholder = { Text("Playlist name") },
                        shape = RoundedCornerShape(22.dp),
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QueueAction(
                        icon = Icons.Rounded.Clear,
                        label = "Clear",
                        enabled = upcoming.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        onClick = viewModel.playback::clearUpcomingQueue,
                    )
                    QueueAction(
                        icon = Icons.Rounded.Shuffle,
                        label = "Shuffle",
                        selected = playback.shuffleEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = viewModel.playback::toggleShuffle,
                    )
                    QueueAction(
                        icon = Icons.Rounded.PlaylistPlay,
                        label = "Save",
                        enabled = saveName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.saveCurrentQueueAsPlaylist(saveName)
                            saveName = ""
                        },
                    )
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (track != null) {
            TrackArtwork(
                track = track,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 0.30f
                        scaleX = 1.18f
                        scaleY = 1.18f
                    },
                contentDescription = null,
                shape = RoundedCornerShape(0.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xE9020905),
                                Color(0xAD041108),
                                Color(0xD4020805),
                            ),
                        )
                    ),
            )
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MuseGlassAction(
                    onClick = { navController.navigate("nowPlaying") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    variant = MuseGlassVariant.Standard,
                    cornerRadius = 18.dp,
                    enabled = track != null,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Song", color = MuseMuted, fontWeight = FontWeight.SemiBold)
                    }
                }
                MuseGlassSurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    variant = MuseGlassVariant.Selected,
                    cornerRadius = 18.dp,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Lyrics", color = MuseGreen, fontWeight = FontWeight.Bold)
                    }
                }
                MuseGlassAction(
                    onClick = {
                        track?.let { seed ->
                            viewModel.playMuseFlow(seed)
                            navController.navigate("nowPlaying")
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    variant = MuseGlassVariant.Standard,
                    cornerRadius = 18.dp,
                    enabled = track != null,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Muse Flow", color = MuseMuted, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            when {
                track == null -> {
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
                }

                lyrics.loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Loading lyrics…", color = MuseMuted)
                    }
                }

                document == null || document.lines.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        MuseGlassSurface(
                            variant = MuseGlassVariant.Elevated,
                            cornerRadius = 28.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
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
                                    textAlign = TextAlign.Center,
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
                }

                else -> {
                    if (lyrics.error != null) {
                        Box(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                        ) {
                            ErrorCard(
                                message = lyrics.error.orEmpty(),
                                onRetry = { track?.let(viewModel::loadLyrics) },
                            )
                        }
                    }

                    MuseGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 8.dp),
                        variant = MuseGlassVariant.Strong,
                        cornerRadius = 22.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TrackArtwork(
                                track = track,
                                modifier = Modifier.size(58.dp),
                                contentDescription = null,
                            )
                            Spacer(Modifier.size(12.dp))
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
                                            "Embedded lyrics"
                                        document.synced ->
                                            "Synced local lyrics"
                                        else ->
                                            "Local lyrics"
                                    },
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (document.synced) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Follow",
                                        color = MuseMuted,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                    MuseMotionToggle(
                                        checked = autoFollow,
                                        onCheckedChange = { autoFollow = it },
                                    )
                                }
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
                            bottom = 20.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        itemsIndexed(document.lines) { index, line ->
                            val active = index == activeIndex
                            if (active) {
                                MuseGlassSurface(
                                    modifier = Modifier.fillMaxWidth(),
                                    variant = MuseGlassVariant.Selected,
                                    cornerRadius = 18.dp,
                                ) {
                                    Text(
                                        text = line.text.ifBlank { "♪" },
                                        color = MuseGreen,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
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
                                            )
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                    )
                                }
                            } else {
                                Text(
                                    text = line.text.ifBlank { "♪" },
                                    color = Color.White.copy(alpha = 0.82f),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
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
                                        )
                                        .padding(vertical = 6.dp),
                                )
                            }
                        }
                    }

                    val duration = playback.durationMs.coerceAtLeast(1L)
                    MuseGlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        variant = MuseGlassVariant.Elevated,
                        cornerRadius = 26.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Slider(
                                value = playback.positionMs.coerceIn(0L, duration).toFloat(),
                                onValueChange = {
                                    viewModel.playback.seekTo(it.roundToLong())
                                },
                                valueRange = 0f..duration.toFloat(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    formatDuration(playback.positionMs),
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    formatDuration(playback.durationMs),
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
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
                                MuseGlassAction(
                                    onClick = viewModel.playback::playPause,
                                    modifier = Modifier.size(64.dp),
                                    variant = MuseGlassVariant.Selected,
                                    cornerRadius = 32.dp,
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (playback.isPlaying) {
                                                Icons.Rounded.Pause
                                            } else {
                                                Icons.Rounded.PlayArrow
                                            },
                                            contentDescription = if (playback.isPlaying) "Pause" else "Play",
                                            tint = MuseGreen,
                                            modifier = Modifier.size(34.dp),
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
                                        tint = if (
                                            playback.repeatMode ==
                                            androidx.media3.common.Player.REPEAT_MODE_OFF
                                        ) {
                                            Color.White
                                        } else {
                                            MuseGreen
                                        },
                                    )
                                }
                            }
                        }
                    }
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
    val transfer by viewModel.playlistTransferState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(PlaylistTab.All) }
    var showCreate by remember { mutableStateOf(false) }

    val importPlaylistLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importM3uPlaylist)
    }
    var playlistPendingDelete by remember { mutableStateOf<UserPlaylist?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Playlists",
                onBack = { navController.popBackStack() },
                action = {
                    MuseGlassAction(
                        onClick = { showCreate = !showCreate },
                        modifier = Modifier.size(46.dp),
                        variant = if (showCreate) {
                            MuseGlassVariant.Selected
                        } else {
                            MuseGlassVariant.Elevated
                        },
                        cornerRadius = 18.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Create playlist",
                                tint = MuseGreen,
                            )
                        }
                    }
                },
            )
        }

        item {
            MuseSlidingTabRow(
                labels = PlaylistTab.entries.map { it.name },
                selectedIndex = PlaylistTab.entries.indexOf(selectedTab),
                modifier = Modifier.fillMaxWidth(),
                onSelected = { index ->
                    selectedTab = PlaylistTab.entries[index]
                },
            )
        }

        if (showCreate) {
            item {
                MuseGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    variant = MuseGlassVariant.Strong,
                    cornerRadius = 22.dp,
                ) {
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
                                    showCreate = false
                                },
                            ) {
                                Text("Create")
                            }
                        },
                        shape = RoundedCornerShape(22.dp),
                    )
                }
            }
        }

        item {
            SettingsRow(
                icon = Icons.Rounded.PlaylistPlay,
                title = "Import M3U / M3U8",
                subtitle = if (transfer.busy) {
                    "Import in progress…"
                } else {
                    "Match playlist entries against your local Muse library"
                },
                onClick = {
                    if (!transfer.busy) {
                        importPlaylistLauncher.launch(
                            arrayOf(
                                "audio/x-mpegurl",
                                "application/vnd.apple.mpegurl",
                                "text/plain",
                            )
                        )
                    }
                },
            )
        }

        if (transfer.message != null || transfer.error != null) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            transfer.message ?: transfer.error.orEmpty(),
                            color = if (transfer.error == null) {
                                MuseGreen
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        )
                        if (transfer.unresolvedEntries.isNotEmpty()) {
                            Text(
                                transfer.unresolvedEntries
                                    .take(3)
                                    .joinToString(
                                        prefix = "Unmatched: ",
                                        separator = " • ",
                                    ) { entry ->
                                        entry.substringAfterLast('/')
                                            .substringAfterLast('\\')
                                    },
                                color = MuseMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(
                            onClick = viewModel::clearPlaylistTransferMessage,
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        if (selectedTab != PlaylistTab.Created) {
            item {
                MuseGlassAction(
                    onClick = { navController.navigate("liked") },
                    modifier = Modifier.fillMaxWidth(),
                    variant = MuseGlassVariant.Elevated,
                    cornerRadius = 22.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrackArtwork(
                            track = liked.firstOrNull(),
                            modifier = Modifier.size(68.dp),
                            contentDescription = null,
                        )
                        Spacer(Modifier.size(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Liked Songs",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "${liked.size} songs",
                                color = MuseMuted,
                            )
                        }
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                    }
                }
            }
        }

        if (selectedTab != PlaylistTab.Liked) {
            if (playlists.isEmpty()) {
                item {
                    EmptyCard(
                        title = "No playlists yet",
                        body = "Tap + to create a local playlist, or import an M3U/M3U8 file.",
                    )
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    val tracks = viewModel.playlistTracks(playlist)
                    MuseGlassAction(
                        onClick = {
                            viewModel.selectPlaylist(playlist.id)
                            navController.navigate("playlistDetail")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = MuseGlassVariant.Standard,
                        cornerRadius = 22.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TrackArtwork(
                                track = tracks.firstOrNull(),
                                modifier = Modifier.size(68.dp),
                                contentDescription = null,
                            )
                            Spacer(Modifier.size(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    playlist.name,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "${tracks.size} songs",
                                    color = MuseMuted,
                                )
                            }
                            IconButton(
                                enabled = tracks.isNotEmpty(),
                                onClick = { viewModel.playTracks(tracks) },
                            ) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = "Play playlist",
                                    tint = MuseGreen,
                                )
                            }
                            IconButton(
                                onClick = { playlistPendingDelete = playlist },
                            ) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = "Delete playlist",
                                )
                            }
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
    val transfer by viewModel.playlistTransferState.collectAsStateWithLifecycle()
    val playlist = playlists.firstOrNull { it.id == selectedId }
    val tracks = playlist?.let(viewModel::playlistTracks).orEmpty()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "application/vnd.apple.mpegurl"
        ),
    ) { uri ->
        val id = playlist?.id
        if (uri != null && id != null) {
            viewModel.exportPlaylistM3u(id, uri)
        }
    }
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
                    TextButton(
                        enabled = tracks.isNotEmpty() && !transfer.busy,
                        onClick = {
                            val safeName = playlist.name
                                .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                                .ifBlank { "Muse-playlist" }
                            exportLauncher.launch("$safeName.m3u8")
                        },
                    ) {
                        Text("Export")
                    }
                }
            }

            if (transfer.message != null || transfer.error != null) {
                item {
                    GlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                transfer.message ?: transfer.error.orEmpty(),
                                color = if (transfer.error == null) {
                                    MuseGreen
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(
                                onClick = viewModel::clearPlaylistTransferMessage,
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
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
    val playCounts by viewModel.playCounts.collectAsStateWithLifecycle()
    val tracks = if (artist == null) {
        emptyList()
    } else {
        library.tracks.filter { it.artist == artist }
    }
    val albumCount = tracks.map { it.album }.distinct().size
    val totalPlays = tracks.sumOf { playCounts[it.id] ?: 0 }
    val popularTracks = tracks.sortedWith(
        compareByDescending<Track> { playCounts[it.id] ?: 0 }
            .thenBy { it.title.lowercase() }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ScreenHeader(
                title = "Artist",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TrackArtwork(
                    track = tracks.firstOrNull(),
                    modifier = Modifier
                        .size(190.dp)
                        .border(
                            width = 1.5.dp,
                            color = MuseGreen.copy(alpha = 0.72f),
                            shape = CircleShape,
                        ),
                    contentDescription = artist?.let { "${it} artwork" },
                    shape = CircleShape,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    artist ?: "Artist",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(tracks.size)
                        append(if (tracks.size == 1) " local track" else " local tracks")
                        append(" • ")
                        append(albumCount)
                        append(if (albumCount == 1) " album" else " albums")
                        if (totalPlays > 0) {
                            append(" • ")
                            append(totalPlays)
                            append(if (totalPlays == 1) " play" else " plays")
                        }
                    },
                    color = MuseMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (tracks.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MuseGlassAction(
                        onClick = { viewModel.playTracksInOrder(tracks) },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        variant = MuseGlassVariant.Selected,
                        cornerRadius = 24.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                "Play All",
                                color = MuseGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    MuseGlassAction(
                        onClick = {
                            viewModel.playMuseFlow(tracks.first())
                            navController.navigate("nowPlaying")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        variant = MuseGlassVariant.Elevated,
                        cornerRadius = 24.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text("Muse Flow", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Popular",
                    trailing = if (totalPlays > 0) "Local plays" else "Local",
                )
            }

            itemsIndexed(
                popularTracks,
                key = { _, track -> track.id },
            ) { index, track ->
                MuseGlassAction(
                    onClick = { viewModel.playTrack(track) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = if (index == 0 && totalPlays > 0) {
                        MuseGlassVariant.Elevated
                    } else {
                        MuseGlassVariant.Standard
                    },
                    cornerRadius = 20.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            color = if (index < 3) MuseGreen else MuseMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center,
                        )
                        TrackArtwork(
                            track = track,
                            modifier = Modifier.size(58.dp),
                            contentDescription = null,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                track.title,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                (playCounts[track.id] ?: 0).let { count ->
                                    if (count == 0) {
                                        track.album
                                    } else {
                                        "${count} local ${if (count == 1) "play" else "plays"}"
                                    }
                                },
                                color = MuseMuted,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavorite(track.id) },
                        ) {
                            Icon(
                                if (track.id in favourites) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = if (track.id in favourites) {
                                    "Remove favorite"
                                } else {
                                    "Favorite"
                                },
                                tint = if (track.id in favourites) MuseGreen else Color.White,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                EmptyCard(
                    title = "No local tracks",
                    body = "Muse only shows artist content available in your own library.",
                )
            }
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 30.dp,
        ),
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
                MuseGlassSurface(
                    modifier = Modifier.fillMaxWidth(0.78f),
                    variant = MuseGlassVariant.Elevated,
                    cornerRadius = 30.dp,
                ) {
                    TrackArtwork(
                        track = representative,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(6.dp),
                        contentDescription = album?.let { "${it} artwork" },
                        shape = RoundedCornerShape(25.dp),
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    album ?: "Album",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                if (representative != null) {
                    Text(
                        representative.artist,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            viewModel.selectArtist(representative.artist)
                            navController.navigate("artist")
                        },
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MuseGlassAction(
                        onClick = { viewModel.playTracksInOrder(tracks) },
                        modifier = Modifier
                            .weight(1.15f)
                            .height(58.dp),
                        variant = MuseGlassVariant.Selected,
                        cornerRadius = 24.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MuseGreen,
                            )
                            Spacer(Modifier.size(6.dp))
                            Text("Play", color = MuseGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    MuseGlassAction(
                        onClick = { viewModel.playTracksShuffled(tracks) },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp),
                        variant = MuseGlassVariant.Elevated,
                        cornerRadius = 24.dp,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = null)
                            Spacer(Modifier.size(6.dp))
                            Text("Shuffle", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    MuseGlassAction(
                        onClick = {
                            representative?.let {
                                viewModel.playMuseFlow(it)
                                navController.navigate("nowPlaying")
                            }
                        },
                        modifier = Modifier
                            .weight(0.72f)
                            .height(58.dp),
                        variant = MuseGlassVariant.Standard,
                        cornerRadius = 24.dp,
                        enabled = representative != null,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = "Muse Flow",
                                tint = MuseGreen,
                            )
                        }
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

        itemsIndexed(
            tracks,
            key = { _, track -> track.id },
        ) { index, track ->
            MuseGlassAction(
                onClick = { viewModel.playTracks(tracks, index) },
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Standard,
                cornerRadius = 19.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = (track.trackNumber ?: index + 1).toString(),
                        color = MuseMuted,
                        modifier = Modifier.width(30.dp),
                        textAlign = TextAlign.Center,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            track.title,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            if (track.durationMs > 0L) {
                                formatDuration(track.durationMs)
                            } else {
                                track.artist
                            },
                            color = MuseMuted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite(track.id) },
                    ) {
                        Icon(
                            if (track.id in favourites) {
                                Icons.Rounded.Favorite
                            } else {
                                Icons.Rounded.FavoriteBorder
                            },
                            contentDescription = if (track.id in favourites) {
                                "Remove favorite"
                            } else {
                                "Favorite"
                            },
                            tint = if (track.id in favourites) MuseGreen else Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val state by viewModel.managedMediaState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(DownloadTab.Songs) }
    var pendingDelete by remember { mutableStateOf<Track?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(viewModel::importManagedMedia)
    }

    val albumGroups = state.tracks
        .groupBy { it.album.ifBlank { "Unknown Album" } }
        .entries
        .sortedBy { it.key.lowercase() }
    val managedIds = state.tracks.map { it.id }.toSet()
    val managedPlaylists = playlists.mapNotNull { playlist ->
        val managedTracks = viewModel.playlistTracks(playlist)
            .filter { it.id in managedIds }
        if (managedTracks.isEmpty()) null else playlist to managedTracks
    }
    val totalBytes = state.tracks.mapNotNull { it.sizeBytes }.sum()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 30.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Downloads",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            MuseSlidingTabRow(
                labels = DownloadTab.entries.map { it.name },
                icons = DownloadTab.entries.map { tab ->
                    when (tab) {
                        DownloadTab.Songs -> Icons.Rounded.MusicNote
                        DownloadTab.Albums -> Icons.Rounded.Album
                        DownloadTab.Playlists -> Icons.Rounded.PlaylistPlay
                    }
                },
                selectedIndex = DownloadTab.entries.indexOf(selectedTab),
                modifier = Modifier.fillMaxWidth(),
                onSelected = { index ->
                    selectedTab = DownloadTab.entries[index]
                },
            )
        }

        if (state.error != null) {
            item {
                MuseGlassSurface(
                    variant = MuseGlassVariant.Destructive,
                    cornerRadius = 20.dp,
                ) {
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
                    title = "No imported audio yet",
                    body = "Choose audio you already have access to. Muse keeps Android-scoped persistent access and never pretends protected sources were downloaded.",
                )
            }
        } else {
            when (selectedTab) {
                DownloadTab.Songs -> {
                    items(state.tracks, key = { it.id }) { track ->
                        MuseGlassAction(
                            onClick = { viewModel.playTrack(track) },
                            modifier = Modifier.fillMaxWidth(),
                            variant = MuseGlassVariant.Standard,
                            cornerRadius = 20.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TrackArtwork(
                                    track = track,
                                    modifier = Modifier.size(68.dp),
                                    contentDescription = null,
                                )
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
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
                                IconButton(onClick = { pendingDelete = track }) {
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = "Imported track options",
                                    )
                                }
                            }
                        }
                    }
                }

                DownloadTab.Albums -> {
                    if (albumGroups.isEmpty()) {
                        item {
                            EmptyCard(
                                title = "No imported albums",
                                body = "Album groups appear automatically from real imported track metadata.",
                            )
                        }
                    } else {
                        items(albumGroups, key = { it.key }) { entry ->
                            val albumTracks = entry.value
                            MuseGlassAction(
                                onClick = { viewModel.playTracks(albumTracks) },
                                modifier = Modifier.fillMaxWidth(),
                                variant = MuseGlassVariant.Standard,
                                cornerRadius = 20.dp,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TrackArtwork(
                                        track = albumTracks.firstOrNull(),
                                        modifier = Modifier.size(68.dp),
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
                                            "${albumTracks.size} imported ${if (albumTracks.size == 1) "song" else "songs"}",
                                            color = MuseMuted,
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play imported album",
                                        tint = MuseGreen,
                                    )
                                }
                            }
                        }
                    }
                }

                DownloadTab.Playlists -> {
                    if (managedPlaylists.isEmpty()) {
                        item {
                            EmptyCard(
                                title = "No imported-audio playlists",
                                body = "Playlists containing Muse-managed files will appear here automatically.",
                            )
                        }
                    } else {
                        items(managedPlaylists, key = { it.first.id }) { (playlist, playlistTracks) ->
                            MuseGlassAction(
                                onClick = { viewModel.playTracks(playlistTracks) },
                                modifier = Modifier.fillMaxWidth(),
                                variant = MuseGlassVariant.Standard,
                                cornerRadius = 20.dp,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TrackArtwork(
                                        track = playlistTracks.firstOrNull(),
                                        modifier = Modifier.size(68.dp),
                                        contentDescription = null,
                                    )
                                    Spacer(Modifier.size(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            playlist.name,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            "${playlistTracks.size} imported ${if (playlistTracks.size == 1) "song" else "songs"}",
                                            color = MuseMuted,
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play imported playlist",
                                        tint = MuseGreen,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    buildString {
                        append(state.tracks.size)
                        append(if (state.tracks.size == 1) " song" else " songs")
                        if (totalBytes > 0L) {
                            append(" • ")
                            append(formatFileSize(totalBytes))
                        }
                    },
                    color = MuseMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        item {
            MuseGlassAction(
                onClick = { importLauncher.launch(arrayOf("audio/*")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                variant = MuseGlassVariant.Selected,
                cornerRadius = 28.dp,
                enabled = !state.importing,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Download,
                        contentDescription = null,
                        tint = MuseGreen,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        if (state.importing) "Importing…" else "Import More Audio",
                        color = MuseGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
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
    var selectedMinutes by remember { mutableStateOf(30) }
    var customMinutes by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.playback.refreshSleepTimer()
    }

    val presets = listOf(10, 30, 60, 90)
    val timerSweep by animateFloatAsState(
        targetValue = if (timer.active) 318f else {
            190f + (selectedMinutes.coerceIn(1, 120) / 120f) * 145f
        },
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 420f,
        ),
        label = "MuseSleepTimerSweep",
    )
    val mainLabel = when (timer.mode) {
        SleepTimerProtocol.ModeAfterCurrent -> "After"
        SleepTimerProtocol.ModeEndOfQueue -> "Queue"
        SleepTimerProtocol.ModeDuration -> formatDuration(timer.remainingMs)
        else -> selectedMinutes.toString()
    }
    val caption = when (timer.mode) {
        SleepTimerProtocol.ModeAfterCurrent -> "Current Track"
        SleepTimerProtocol.ModeEndOfQueue -> "End of Queue"
        SleepTimerProtocol.ModeDuration -> "Remaining"
        else -> "Minutes"
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
                modifier = Modifier.size(272.dp),
                shape = CircleShape,
                color = Color(0xD407170C),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MuseGreen.copy(alpha = 0.76f),
                ),
                shadowElevation = 18.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val inset = 14.dp.toPx()
                        val arcSize = Size(
                            width = size.width - inset * 2,
                            height = size.height - inset * 2,
                        )
                        drawArc(
                            color = MuseGreen.copy(alpha = 0.12f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = 12.dp.toPx()),
                        )
                        drawArc(
                            color = MuseGreen.copy(alpha = 0.28f),
                            startAngle = -90f,
                            sweepAngle = timerSweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = 18.dp.toPx()),
                        )
                        drawArc(
                            color = MuseGreen,
                            startAngle = -90f,
                            sweepAngle = if (timer.active) 318f else 260f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = 5.dp.toPx()),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = MuseGreen,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            mainLabel,
                            style = if (timer.mode == SleepTimerProtocol.ModeDuration) {
                                MaterialTheme.typography.displaySmall
                            } else {
                                MaterialTheme.typography.headlineLarge
                            },
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            caption,
                            color = MuseMuted,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
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
                    MuseGlassAction(
                        onClick = {
                            selectedMinutes = minutes
                            customMinutes = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(86.dp),
                        variant = if (
                            selectedMinutes == minutes &&
                            timer.mode != SleepTimerProtocol.ModeAfterCurrent &&
                            timer.mode != SleepTimerProtocol.ModeEndOfQueue
                        ) {
                            MuseGlassVariant.Selected
                        } else {
                            MuseGlassVariant.Standard
                        },
                        cornerRadius = 22.dp,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                minutes.toString(),
                                color = if (selectedMinutes == minutes) MuseGreen else Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "min",
                                color = MuseMuted,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }

        item {
            MuseGlassAction(
                onClick = {
                    viewModel.playback.startSleepTimer(selectedMinutes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                variant = MuseGlassVariant.Selected,
                cornerRadius = 30.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MuseGreen,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        if (timer.active) "Restart Timer" else "Start Timer",
                        color = MuseGreen,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 24.dp,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Custom & Playback Boundaries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = customMinutes,
                            onValueChange = { value ->
                                customMinutes = value.filter(Char::isDigit).take(3)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            shape = RoundedCornerShape(18.dp),
                        )
                        TextButton(
                            enabled = customMinutes.toIntOrNull()
                                ?.let { it in 1..720 } == true,
                            onClick = {
                                customMinutes.toIntOrNull()?.let { minutes ->
                                    selectedMinutes = minutes
                                    viewModel.playback.startSleepTimer(minutes)
                                }
                            },
                        ) {
                            Text("Start")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MuseGlassAction(
                            onClick = viewModel.playback::startSleepAfterCurrentTrack,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            variant = if (
                                timer.mode == SleepTimerProtocol.ModeAfterCurrent
                            ) {
                                MuseGlassVariant.Selected
                            } else {
                                MuseGlassVariant.Standard
                            },
                            cornerRadius = 18.dp,
                            enabled = playback.currentMediaId != null,
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("After Track", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        MuseGlassAction(
                            onClick = viewModel.playback::startSleepAtEndOfQueue,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            variant = if (
                                timer.mode == SleepTimerProtocol.ModeEndOfQueue
                            ) {
                                MuseGlassVariant.Selected
                            } else {
                                MuseGlassVariant.Standard
                            },
                            cornerRadius = 18.dp,
                            enabled = playback.queue.isNotEmpty(),
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("End Queue", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        if (timer.active) {
            item {
                MuseGlassAction(
                    onClick = viewModel.playback::cancelSleepTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    variant = MuseGlassVariant.Destructive,
                    cornerRadius = 22.dp,
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Cancel Sleep Timer",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
    visualIntensity: MuseVisualIntensity,
    onVisualIntensityChange: (MuseVisualIntensity) -> Unit,
) {
    val state by viewModel.libraryState.collectAsStateWithLifecycle()
    val backup by viewModel.backupState.collectAsStateWithLifecycle()
    val hiddenTracks by viewModel.hiddenTrackIds.collectAsStateWithLifecycle()
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
    var notificationPermissionGranted by remember {
        mutableStateOf(museNotificationPermissionGranted(context))
    }
    val settingsNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionGranted = granted
        if (granted) {
            openMuseNotificationSettings(context)
        }
    }

    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 30.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader("Settings", { navController.popBackStack() })
        }

        item {
            SectionTitle(
                title = "Muse",
                trailing = "Local-first",
            )
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Elevated,
                cornerRadius = 26.dp,
            ) {
                Column {
                    SettingsPanelItem(
                        icon = Icons.Rounded.LibraryMusic,
                        title = "Library",
                        subtitle = "${state.tracks.size} local tracks • tap to rescan",
                        onClick = viewModel::refreshLibrary,
                    )
                    if (!libraryPermissionGranted) {
                        SettingsPanelDivider()
                        SettingsPanelItem(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "Enable Device Library",
                            subtitle = "Grant Android music access",
                            onClick = {
                                libraryPermissionLauncher.launch(libraryPermission)
                            },
                        )
                    }
                    if (hiddenTracks.isNotEmpty()) {
                        SettingsPanelDivider()
                        SettingsPanelItem(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "Hidden from Muse",
                            subtitle = "${hiddenTracks.size} track(s) hidden • tap to restore all",
                            onClick = viewModel::restoreHiddenTracks,
                        )
                    }
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.MusicNote,
                        title = "Audio Quality",
                        subtitle = "Original source • no fake upscaling or transcoding",
                        onClick = { navController.navigate("diagnostics") },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Download,
                        title = "Download Quality",
                        subtitle = "Save-copy keeps the original audio bytes",
                        onClick = { navController.navigate("downloads") },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Palette,
                        title = "Theme",
                        subtitle = "Nature • ${visualIntensity.label} botanical intensity",
                        onClick = {
                            onVisualIntensityChange(
                                visualIntensity.next()
                            )
                        },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Timer,
                        title = "Sleep Timer",
                        subtitle = "Timer and playback boundaries",
                        onClick = { navController.navigate("sleep") },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Equalizer,
                        title = "Audio Enhancement",
                        subtitle = "Muse DSP • EQ, bass, virtualizer, loudness & Spatial 3D",
                        onClick = { navController.navigate("equalizer") },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        subtitle = if (notificationPermissionGranted) {
                            "Enabled • tap to manage playback notifications"
                        } else {
                            "Permission required • tap to enable"
                        },
                        onClick = {
                            if (
                                android.os.Build.VERSION.SDK_INT >=
                                android.os.Build.VERSION_CODES.TIRAMISU &&
                                !notificationPermissionGranted
                            ) {
                                settingsNotificationPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS,
                                )
                            } else {
                                openMuseNotificationSettings(context)
                            }
                        },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Download,
                        title = "Imported Audio",
                        subtitle = "Manage files chosen through Android",
                        onClick = { navController.navigate("downloads") },
                    )
                }
            }
        }

        item {
            SectionTitle(
                title = "Backup & Restore",
                trailing = "Local JSON",
            )
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 26.dp,
            ) {
                Column {
                    SettingsPanelItem(
                        icon = Icons.Rounded.Share,
                        title = "Export Muse Backup",
                        subtitle = "Playlists, favourites, history and sound profiles",
                        onClick = {
                            if (!backup.busy) {
                                exportLauncher.launch("Muse-backup.json")
                            }
                        },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Download,
                        title = "Restore Muse Backup",
                        subtitle = "Restore organisation data from a Muse JSON backup",
                        onClick = {
                            if (!backup.busy) {
                                restoreLauncher.launch(
                                    arrayOf(
                                        "application/json",
                                        "text/json",
                                        "text/plain",
                                    )
                                )
                            }
                        },
                    )
                }
            }
        }

        if (backup.busy) {
            item {
                MuseGlassSurface(
                    variant = MuseGlassVariant.Selected,
                    cornerRadius = 20.dp,
                ) {
                    Text(
                        "Working on backup…",
                        color = MuseGreen,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        if (backup.message != null || backup.error != null) {
            item {
                MuseGlassSurface(
                    variant = if (backup.error == null) {
                        MuseGlassVariant.Selected
                    } else {
                        MuseGlassVariant.Destructive
                    },
                    cornerRadius = 20.dp,
                ) {
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
            SectionTitle(title = "Privacy & About")
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Standard,
                cornerRadius = 26.dp,
            ) {
                Column {
                    SettingsPanelItem(
                        icon = Icons.Rounded.Language,
                        title = "Language",
                        subtitle = "System / Android app-language settings",
                        onClick = {
                            val intent = if (
                                android.os.Build.VERSION.SDK_INT >=
                                android.os.Build.VERSION_CODES.TIRAMISU
                            ) {
                                Intent(
                                    android.provider.Settings.ACTION_APP_LOCALE_SETTINGS,
                                    android.net.Uri.parse(
                                        "package:${context.packageName}"
                                    ),
                                )
                            } else {
                                Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.parse(
                                        "package:${context.packageName}"
                                    ),
                                )
                            }
                            context.startActivity(intent)
                        },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Security,
                        title = "Privacy & Security",
                        subtitle = "No account required • Android-scoped file access",
                        onClick = { showPrivacyDialog = true },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Info,
                        title = "About",
                        subtitle = "Muse ${BuildConfig.VERSION_NAME} • local-first music player",
                        onClick = { showAboutDialog = true },
                    )
                    SettingsPanelDivider()
                    SettingsPanelItem(
                        icon = Icons.Rounded.Info,
                        title = "Diagnostics",
                        subtitle = "Playback, permissions and device audio capability",
                        onClick = { navController.navigate("diagnostics") },
                    )
                }
            }
        }

        item {
            Text(
                "Muse does not require an account or paid cloud service for core playback. Backups exclude audio files and imported-file permissions.",
                color = MuseMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy & Security") },
            text = {
                Text(
                    "Muse is local-first: no account is required for core playback, listening history stays on device, and file access uses Android-scoped permissions. Backups contain organisation data, not your audio files."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showPrivacyDialog = false },
                ) {
                    Text("Close")
                }
            },
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Muse") },
            text = {
                Text(
                    "Version ${BuildConfig.VERSION_NAME}. Muse is a local-first Android music player using the approved botanical visual system and real device media state."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showAboutDialog = false },
                ) {
                    Text("Close")
                }
            },
        )
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
private fun SettingsPanelItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
) {
    val interactionModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(15.dp),
            color = Color(0x5A123B1D),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MuseGreen.copy(alpha = 0.22f),
            ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MuseGreen,
                    modifier = Modifier.size(25.dp),
                )
            }
        }
        Spacer(Modifier.size(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                color = MuseMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClick != null) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable
private fun SettingsPanelDivider() {
    HorizontalDivider(
        color = MuseBorder.copy(alpha = 0.42f),
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}

@Composable
private fun DiagnosticsScreen(
    viewModel: MuseViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val library by viewModel.libraryState.collectAsStateWithLifecycle()
    val playback by viewModel.playback.state.collectAsStateWithLifecycle()
    val effects by viewModel.playback.audioEffects.collectAsStateWithLifecycle()
    val timer by viewModel.playback.sleepTimer.collectAsStateWithLifecycle()
    val managed by viewModel.managedMediaState.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()

    val libraryPermission = if (
        android.os.Build.VERSION.SDK_INT >=
        android.os.Build.VERSION_CODES.TIRAMISU
    ) {
        android.Manifest.permission.READ_MEDIA_AUDIO
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionGranted =
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            libraryPermission,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun bool(value: Boolean): String =
        if (value) "Yes" else "No"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ScreenHeader(
                title = "Diagnostics",
                onBack = { navController.popBackStack() },
            )
        }

        item {
            SectionTitle(
                title = "Build",
                trailing = if (BuildConfig.DEBUG) "Debug" else "Release",
            )
        }
        item {
            DiagnosticRow("Version", BuildConfig.VERSION_NAME)
        }
        item {
            DiagnosticRow(
                "Android",
                android.os.Build.VERSION.RELEASE +
                    " (API " + android.os.Build.VERSION.SDK_INT + ")",
            )
        }
        item {
            DiagnosticRow(
                "Device library permission",
                bool(permissionGranted),
            )
        }

        item {
            SectionTitle(title = "Library")
        }
        item {
            DiagnosticRow(
                "Tracks",
                library.tracks.size.toString(),
            )
        }
        item {
            DiagnosticRow(
                "Imported audio",
                managed.tracks.size.toString(),
            )
        }
        item {
            DiagnosticRow(
                "Playlists",
                playlists.size.toString(),
            )
        }

        item {
            SectionTitle(title = "Playback")
        }
        item {
            DiagnosticRow(
                "Media session connected",
                bool(playback.connected),
            )
        }
        item {
            DiagnosticRow(
                "Playing",
                bool(playback.isPlaying),
            )
        }
        item {
            DiagnosticRow(
                "Queue items",
                playback.queue.size.toString(),
            )
        }
        item {
            DiagnosticRow(
                "Sleep Scene active",
                bool(timer.active),
            )
        }
        if (BuildConfig.DEBUG) {
            item {
                DiagnosticRow(
                    "Audio session ID",
                    effects.audioSessionId
                        .takeIf { it > 0 }
                        ?.toString()
                        ?: "Unavailable",
                )
            }
        }

        item {
            SectionTitle(title = "Audio Capabilities")
        }
        item {
            DiagnosticRow(
                "Equalizer",
                bool(effects.equalizerAvailable),
            )
        }
        item {
            DiagnosticRow(
                "Bass Boost",
                bool(effects.bassAvailable),
            )
        }
        item {
            DiagnosticRow(
                "Virtualizer",
                bool(effects.virtualizerAvailable),
            )
        }
        item {
            DiagnosticRow(
                "Loudness Enhancer",
                bool(effects.loudnessAvailable),
            )
        }
        item {
            DiagnosticRow(
                "Spatial audio supported",
                bool(effects.spatialSupported),
            )
        }
        item {
            DiagnosticRow(
                "Spatial audio available",
                bool(effects.spatialAvailable),
            )
        }

        item {
            GlassCard {
                Text(
                    "Diagnostics stay on-device and intentionally avoid file paths, content URIs and other private media identifiers.",
                    color = MuseMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                color = MuseMuted,
            )
            Text(
                value,
                fontWeight = FontWeight.SemiBold,
            )
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
    var confirmRemove by remember(track?.id) {
        mutableStateOf(false)
    }
    var pendingRingtoneTrack by remember(track?.id) {
        mutableStateOf<Track?>(null)
    }
    var ringtoneMessage by remember(track?.id) {
        mutableStateOf<String?>(null)
    }
    var pendingExportTrack by remember(track?.id) {
        mutableStateOf<Track?>(null)
    }
    var exportMessage by remember(track?.id) {
        mutableStateOf<String?>(null)
    }
    val exportScope = rememberCoroutineScope()

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

    val exportCopyLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("audio/*"),
    ) { destination ->
        val candidate = pendingExportTrack
        pendingExportTrack = null
        if (destination != null && candidate != null) {
            exportScope.launch {
                exportMessage = withContext(Dispatchers.IO) {
                    runCatching {
                        val resolver = context.contentResolver
                        resolver.openInputStream(candidate.uri).use { input ->
                            requireNotNull(input) {
                                "Muse could not read the source audio."
                            }
                            resolver.openOutputStream(destination, "w").use { output ->
                                requireNotNull(output) {
                                    "Muse could not open the selected destination."
                                }
                                input.copyTo(output)
                            }
                        }
                        "Saved a copy of “${candidate.title}”."
                    }.getOrElse { error ->
                        error.message ?: "Muse could not save this audio copy."
                    }
                }
            }
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
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MuseMuted.copy(alpha = 0.52f))
                .align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(10.dp))

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
                    MuseGlassSurface(
                        variant = MuseGlassVariant.Elevated,
                        cornerRadius = 28.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TrackArtwork(
                                track = activeTrack,
                                modifier = Modifier.size(88.dp),
                                contentDescription = null,
                            )
                            Spacer(Modifier.size(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    activeTrack.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    activeTrack.artist,
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            MuseGlassAction(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(48.dp),
                                variant = MuseGlassVariant.Strong,
                                cornerRadius = 24.dp,
                            ) {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = "Close",
                                    )
                                }
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
                        icon = Icons.Rounded.Download,
                        title = "Download / Save Copy",
                        subtitle = "Save a user-owned copy through Android's file picker",
                        onClick = {
                            pendingExportTrack = activeTrack
                            val rawName = activeTrack.title
                                .replace(
                                    Regex("""[\\/:*?"<>|]"""),
                                    "_",
                                )
                                .trim()
                                .ifBlank { "Muse track" }
                            val sourceExtension = activeTrack.uri
                                .lastPathSegment
                                ?.substringAfterLast('.', "")
                                ?.takeIf { extension ->
                                    extension.length in 2..5 &&
                                        extension.all { char ->
                                            char.isLetterOrDigit()
                                        }
                                }
                            val suggestedName = if (sourceExtension == null) {
                                rawName
                            } else {
                                "$rawName.$sourceExtension"
                            }
                            exportCopyLauncher.launch(suggestedName)
                        },
                    )
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
                        title = "Song Radio",
                        subtitle = "Uses Muse Flow to build an adaptive private queue from this track and your local listening signals",
                        onClick = {
                            viewModel.playMuseFlow(activeTrack)
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

                exportMessage?.let { message ->
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
                                    onClick = { exportMessage = null },
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
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
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    "Song Info",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
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
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                item {
                    MuseGlassAction(
                        onClick = { confirmRemove = true },
                        modifier = Modifier.fillMaxWidth(),
                        variant = MuseGlassVariant.Destructive,
                        cornerRadius = 20.dp,
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
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    "Remove from Library",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    if (activeTrack.managedByMuse) {
                                        "Release Muse access; the original file is not deleted"
                                    } else {
                                        "Hide this track from Muse; the device file is not deleted"
                                    },
                                    color = MuseMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
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
                            if (activeTrack.managedByMuse) {
                                "Muse will forget “${activeTrack.title}” and release saved access. The original file is not deleted."
                            } else {
                                "Muse will hide “${activeTrack.title}” from the library and current queue. The device file is not deleted, and hidden tracks can be restored from Settings."
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.hideFromLibrary(activeTrack)
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

    val selectedProfile = soundProfiles.firstOrNull { it.id == selectedSoundProfileId }
    val controlsEnabled = effects.connected && effects.sessionReady && effects.masterEnabled && !effects.bypass

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 30.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(
                title = "Equalizer",
                action = {
                    MuseMotionToggle(
                        checked = effects.masterEnabled,
                        onCheckedChange = viewModel.playback::setAudioEffectsEnabled,
                        enabled = effects.connected,
                    )
                },
            )
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 28.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it.take(32) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Preset") },
                        placeholder = {
                            Text(selectedProfile?.name ?: "Custom")
                        },
                        shape = RoundedCornerShape(22.dp),
                    )
                    MuseGlassAction(
                        onClick = {
                            val name = profileName.trim().ifBlank {
                                "Custom ${soundProfiles.size + 1}"
                            }
                            viewModel.saveCurrentSoundProfile(name)
                            profileName = ""
                        },
                        modifier = Modifier
                            .width(92.dp)
                            .height(58.dp),
                        variant = MuseGlassVariant.Selected,
                        cornerRadius = 22.dp,
                        enabled = effects.sessionReady,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Save",
                                color = MuseGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        if (!effects.connected || !effects.sessionReady) {
            item {
                EmptyCard(
                    title = if (!effects.connected) {
                        "Connecting to Muse audio…"
                    } else {
                        "Start a track to activate the equalizer"
                    },
                    body = "Muse's software curve EQ and spatial compatibility DSP will become interactive as soon as the playback service connects.",
                )
            }
        }

        if (effects.equalizerAvailable) {
            item {
                MuseGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    variant = MuseGlassVariant.Elevated,
                    cornerRadius = 30.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 18.dp,
                        ),
                    ) {
                        MuseEqualizerRack(
                            frequencies = effects.bandCentersHz,
                            levelsMb = effects.bandLevelsMb,
                            minMb = effects.bandMinMb,
                            maxMb = effects.bandMaxMb,
                            enabled = controlsEnabled,
                            onBandChange = { index, level ->
                                viewModel.playback.setEqualizerBand(index, level)
                            },
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReferenceEffectOrb(
                    title = "Bass Boost",
                    icon = Icons.Rounded.Equalizer,
                    active = effects.bassEnabled,
                    enabled = effects.bassAvailable && controlsEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.playback.setBassEnabled(!effects.bassEnabled)
                    },
                )
                ReferenceEffectOrb(
                    title = "Virtualizer",
                    icon = Icons.Rounded.Tune,
                    active = effects.virtualizerEnabled,
                    enabled = effects.virtualizerAvailable && controlsEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.playback.setVirtualizerEnabled(!effects.virtualizerEnabled)
                    },
                )
                ReferenceEffectOrb(
                    title = "Spatial 3D",
                    icon = Icons.Rounded.Explore,
                    active = effects.spatialEnabled,
                    enabled = effects.spatialAvailable && controlsEnabled,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.playback.setSpatialEnabled(
                            !effects.spatialEnabled,
                        )
                    },
                )
            }
        }

        item {
            SectionTitle(
                title = "Enhancement Strength",
                trailing = "Muse DSP",
            )
        }

        item {
            AudioEffectControl(
                title = "Bass Boost",
                subtitle = "Low-end lift with a conservative software shelf",
                checked = effects.bassEnabled,
                onCheckedChange = viewModel.playback::setBassEnabled,
                value = effects.bassStrength,
                valueRange = 0..700,
                onValueChange = viewModel.playback::setBassStrength,
                controlsEnabled = controlsEnabled,
            )
        }

        item {
            AudioEffectControl(
                title = "Virtualizer",
                subtitle = "Stereo width without requiring OEM AudioFX",
                checked = effects.virtualizerEnabled,
                onCheckedChange = viewModel.playback::setVirtualizerEnabled,
                value = effects.virtualizerStrength,
                valueRange = 0..1000,
                onValueChange = viewModel.playback::setVirtualizerStrength,
                controlsEnabled = controlsEnabled,
            )
        }

        item {
            AudioEffectControl(
                title = "Loudness",
                subtitle = "Up to +6 dB with Muse output protection",
                checked = effects.loudnessEnabled,
                onCheckedChange = viewModel.playback::setLoudnessEnabled,
                value = effects.loudnessGainMb,
                valueRange = 0..600,
                onValueChange = viewModel.playback::setLoudnessGainMb,
                controlsEnabled = controlsEnabled,
                valueLabel = { value -> "+" + (value / 100f) + " dB" },
            )
        }

        item {
            MuseGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                variant = MuseGlassVariant.Strong,
                cornerRadius = 26.dp,
            ) {
                Column {
                    ReferenceToggleRow(
                        title = "Audio Processing",
                        subtitle = if (effects.bypass) "Original signal" else "Muse processing",
                        checked = effects.masterEnabled && !effects.bypass,
                        enabled = effects.connected,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                viewModel.playback.setAudioBypass(true)
                            } else {
                                viewModel.playback.setAudioEffectsEnabled(true)
                                viewModel.playback.setAudioBypass(false)
                            }
                        },
                    )
                    HorizontalDivider(color = MuseBorder.copy(alpha = 0.35f))
                    ReferenceToggleRow(
                        title = "Muse Spatial 3D",
                        subtitle = when {
                            effects.spatialCompatibilityMode ->
                                "Compatibility DSP • works without Android Spatializer"
                            effects.headTrackerAvailable && effects.spatialEnabled ->
                                "System spatial route • head tracking available"
                            effects.spatialEnabled ->
                                "Muse stereo spatial field active"
                            else ->
                                "Software-compatible spatial widening"
                        },
                        checked = effects.spatialEnabled,
                        enabled = effects.spatialAvailable && controlsEnabled,
                        onCheckedChange = viewModel.playback::setSpatialEnabled,
                    )
                    AnimatedVisibility(
                        visible = effects.spatialEnabled,
                        enter = fadeIn(animationSpec = tween(140)) +
                            expandVertically(animationSpec = tween(180)),
                        exit = fadeOut(animationSpec = tween(100)) +
                            shrinkVertically(animationSpec = tween(140)),
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 14.dp,
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Spatial Width",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "${effects.spatialWidth / 10}%",
                                    color = MuseGreen,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Slider(
                                value = effects.spatialWidth.toFloat(),
                                onValueChange = {
                                    viewModel.playback.setSpatialWidth(
                                        it.roundToLong().toInt(),
                                    )
                                },
                                valueRange = 0f..1000f,
                                enabled = controlsEnabled,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        if (soundProfiles.isNotEmpty()) {
            item {
                SectionTitle(
                    title = "Saved Presets",
                    trailing = selectedProfile?.name ?: "${soundProfiles.size} saved",
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(soundProfiles, key = { it.id }) { profile ->
                        MuseGlassAction(
                            onClick = { viewModel.applySoundProfile(profile) },
                            modifier = Modifier.height(46.dp),
                            variant = if (selectedSoundProfileId == profile.id) {
                                MuseGlassVariant.Selected
                            } else {
                                MuseGlassVariant.Standard
                            },
                            cornerRadius = 23.dp,
                            enabled = effects.sessionReady,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    profile.name,
                                    color = if (selectedSoundProfileId == profile.id) {
                                        MuseGreen
                                    } else {
                                        Color.White
                                    },
                                    fontWeight = FontWeight.SemiBold,
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
private fun ReferenceEffectOrb(
    title: String,
    icon: ImageVector,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MuseGlassAction(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            variant = if (active) {
                MuseGlassVariant.Selected
            } else {
                MuseGlassVariant.Elevated
            },
            cornerRadius = 999.dp,
            enabled = enabled,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (active) MuseGreen else Color.White,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            color = if (active) MuseGreen else Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReferenceToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(
                subtitle,
                color = MuseMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        MuseMotionToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun MuseEqualizerRack(
    frequencies: List<Int>,
    levelsMb: List<Int>,
    minMb: Int,
    maxMb: Int,
    enabled: Boolean,
    onBandChange: (Int, Int) -> Unit,
) {
    val safeRange = (maxMb - minMb).coerceAtLeast(1)
    var curveEntered by remember { mutableStateOf(false) }
    var activeBand by remember { mutableStateOf<Int?>(null) }
    var activeTouch by remember { mutableStateOf<Offset?>(null) }
    var displayLevels by remember(frequencies) {
        mutableStateOf(levelsMb)
    }

    LaunchedEffect(frequencies) {
        if (frequencies.isNotEmpty()) {
            curveEntered = true
        }
    }
    LaunchedEffect(levelsMb, activeBand) {
        if (
            activeBand == null &&
            levelsMb.size == frequencies.size
        ) {
            displayLevels = levelsMb
        }
    }

    val curveReveal by animateFloatAsState(
        targetValue = if (curveEntered) 1f else 0f,
        animationSpec = tween(
            durationMillis = 420,
            easing = FastOutSlowInEasing,
        ),
        label = "MuseEqualizerCurveReveal",
    )

    MuseGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        variant = MuseGlassVariant.Elevated,
        cornerRadius = 28.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Live Curve",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (enabled) "TOUCH + SLIDE" else "BYPASSED",
                    color = if (enabled) MuseGreen else MuseMuted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .pointerInput(
                        enabled,
                        frequencies,
                        minMb,
                        maxMb,
                    ) {
                        if (!enabled || frequencies.isEmpty()) {
                            return@pointerInput
                        }

                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                            )

                            val graphTop = 14.dp.toPx()
                            val graphBottom = size.height * 0.72f
                            val left = 18.dp.toPx()
                            val right = size.width - 18.dp.toPx()
                            val span = (right - left).coerceAtLeast(1f)
                            val xStep = if (frequencies.size <= 1) {
                                span
                            } else {
                                span / (frequencies.size - 1)
                            }

                            var lastDispatchAt = 0L
                            var lastChangedBands = emptyList<Pair<Int, Int>>()

                            fun updateFromTouch(
                                position: Offset,
                                forceDispatch: Boolean,
                            ) {
                                val clampedX = position.x.coerceIn(left, right)
                                val clampedY = position.y.coerceIn(
                                    graphTop,
                                    graphBottom,
                                )
                                activeTouch = Offset(clampedX, clampedY)

                                val bandPosition = if (frequencies.size <= 1) {
                                    0f
                                } else {
                                    (
                                        (clampedX - left) / span *
                                            (frequencies.size - 1)
                                        ).coerceIn(
                                            0f,
                                            (frequencies.size - 1).toFloat(),
                                        )
                                }

                                val normalized = (
                                    (graphBottom - clampedY) /
                                        (graphBottom - graphTop)
                                ).coerceIn(0f, 1f)
                                val targetLevel = (
                                    minMb + normalized * safeRange
                                    )
                                    .roundToLong()
                                    .toInt()
                                    .coerceIn(minMb, maxMb)

                                val nearest = kotlin.math.round(
                                    bandPosition,
                                ).toInt().coerceIn(frequencies.indices)
                                activeBand = nearest

                                if (displayLevels.size == frequencies.size) {
                                    val updated = displayLevels.toMutableList()
                                    val low = kotlin.math.floor(
                                        bandPosition,
                                    ).toInt().coerceIn(frequencies.indices)
                                    val high = kotlin.math.ceil(
                                        bandPosition,
                                    ).toInt().coerceIn(frequencies.indices)
                                    val fraction = bandPosition - low

                                    val changed = linkedMapOf<Int, Int>()
                                    if (low == high) {
                                        updated[low] = targetLevel
                                        changed[low] = targetLevel
                                    } else {
                                        val lowWeight = kotlin.math.cos(
                                            fraction * Math.PI.toFloat() / 2f,
                                        ).coerceIn(0f, 1f)
                                        val highWeight = kotlin.math.sin(
                                            fraction * Math.PI.toFloat() / 2f,
                                        ).coerceIn(0f, 1f)

                                        val lowValue = (
                                            updated[low] +
                                                (targetLevel - updated[low]) *
                                                lowWeight
                                            )
                                            .roundToLong()
                                            .toInt()
                                            .coerceIn(minMb, maxMb)
                                        val highValue = (
                                            updated[high] +
                                                (targetLevel - updated[high]) *
                                                highWeight
                                            )
                                            .roundToLong()
                                            .toInt()
                                            .coerceIn(minMb, maxMb)

                                        updated[low] = lowValue
                                        updated[high] = highValue
                                        changed[low] = lowValue
                                        changed[high] = highValue
                                    }

                                    displayLevels = updated
                                    lastChangedBands = changed.entries.map {
                                        it.key to it.value
                                    }
                                }

                                // UI updates on every pointer event. Service/DSP
                                // writes are intentionally coalesced to ~30 Hz so
                                // command traffic cannot make the finger tracking
                                // feel sticky or rigid.
                                val now =
                                    android.os.SystemClock.uptimeMillis()
                                if (
                                    forceDispatch ||
                                    now - lastDispatchAt >= 32L
                                ) {
                                    lastDispatchAt = now
                                    lastChangedBands.forEach { (index, level) ->
                                        onBandChange(index, level)
                                    }
                                }
                            }

                            updateFromTouch(
                                down.position,
                                forceDispatch = true,
                            )
                            down.consume()

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes
                                    .firstOrNull {
                                        it.id == down.id
                                    }
                                    ?: break

                                if (!change.pressed) {
                                    lastChangedBands.forEach { (index, level) ->
                                        onBandChange(index, level)
                                    }
                                    activeBand = null
                                    activeTouch = null
                                    break
                                }

                                updateFromTouch(
                                    change.position,
                                    forceDispatch = false,
                                )
                                change.consume()
                            }
                        }
                    },
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    if (frequencies.isEmpty()) return@Canvas

                    val graphTop = 14.dp.toPx()
                    val graphBottom = size.height * 0.72f
                    val left = 18.dp.toPx()
                    val right = size.width - 18.dp.toPx()
                    val span = (right - left).coerceAtLeast(1f)
                    val xStep = if (frequencies.size <= 1) {
                        0f
                    } else {
                        span / (frequencies.size - 1)
                    }

                    fun rawPoint(index: Int): Offset {
                        val level = displayLevels.getOrNull(index) ?: 0
                        val normalized = (
                            (level - minMb).toFloat() /
                                safeRange.toFloat()
                        ).coerceIn(0f, 1f)
                        return Offset(
                            x = if (frequencies.size <= 1) {
                                size.width / 2f
                            } else {
                                left + xStep * index
                            },
                            y = graphBottom -
                                normalized *
                                (graphBottom - graphTop),
                        )
                    }

                    val zeroNormalized = (
                        (0 - minMb).toFloat() /
                            safeRange.toFloat()
                    ).coerceIn(0f, 1f)
                    val zeroY = graphBottom -
                        zeroNormalized *
                        (graphBottom - graphTop)

                    fun point(index: Int): Offset {
                        val target = rawPoint(index)
                        return Offset(
                            x = target.x,
                            y = zeroY +
                                (target.y - zeroY) *
                                curveReveal,
                        )
                    }

                    drawLine(
                        color = MuseGreen.copy(alpha = 0.12f),
                        start = Offset(left, zeroY),
                        end = Offset(right, zeroY),
                        strokeWidth = 1.dp.toPx(),
                    )

                    frequencies.indices.forEach { index ->
                        val p = point(index)
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MuseGreen.copy(alpha = 0.18f),
                                    MuseGreen.copy(alpha = 0.05f),
                                ),
                                startY = graphTop,
                                endY = graphBottom,
                            ),
                            start = Offset(p.x, graphTop),
                            end = Offset(p.x, graphBottom),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    for (index in 0 until frequencies.lastIndex) {
                        val startPoint = point(index)
                        val endPoint = point(index + 1)
                        drawLine(
                            color = MuseGreen.copy(alpha = 0.20f),
                            start = startPoint,
                            end = endPoint,
                            strokeWidth = 9.dp.toPx(),
                        )
                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFFCFFF9B),
                                    MuseGreen,
                                    Color(0xFF72F767),
                                )
                            ),
                            start = startPoint,
                            end = endPoint,
                            strokeWidth = 2.7.dp.toPx(),
                        )
                    }

                    frequencies.indices.forEach { index ->
                        val p = point(index)
                        val selected = activeBand == index
                        drawCircle(
                            color = MuseGreen.copy(
                                alpha = if (selected) 0.28f else 0.16f,
                            ),
                            center = p,
                            radius = (
                                if (selected) 16.dp else 11.dp
                            ).toPx(),
                        )
                        drawCircle(
                            color = MuseGreen.copy(
                                alpha = if (selected) 0.64f else 0.42f,
                            ),
                            center = p,
                            radius = (
                                if (selected) 9.dp else 7.dp
                            ).toPx(),
                        )
                        drawCircle(
                            color = Color(0xFFE4FFD9),
                            center = p,
                            radius = (
                                if (selected) 5.dp else 4.dp
                            ).toPx(),
                        )
                    }

                    activeTouch?.let { touch ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.36f),
                                    MuseGreen.copy(alpha = 0.24f),
                                    Color.Transparent,
                                ),
                                center = touch,
                                radius = 24.dp.toPx(),
                            ),
                            center = touch,
                            radius = 24.dp.toPx(),
                        )
                        drawCircle(
                            color = Color(0xFFE9FFD6),
                            center = touch,
                            radius = 4.5.dp.toPx(),
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                ) {
                    frequencies.forEachIndexed { index, frequency ->
                        val level =
                            displayLevels.getOrNull(index) ?: 0
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                        ) {
                            Text(
                                formatMillibels(level),
                                color = if (activeBand == index) {
                                    Color(0xFFD7FF9B)
                                } else {
                                    MuseGreen
                                },
                                style =
                                    MaterialTheme.typography.labelSmall,
                                fontWeight = if (activeBand == index) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Medium
                                },
                                maxLines = 1,
                            )
                            Text(
                                formatFrequency(frequency),
                                color = MuseMuted,
                                style =
                                    MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
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
    MuseGlassSurface(
        variant = if (checked) {
            MuseGlassVariant.Selected
        } else {
            MuseGlassVariant.Standard
        },
        cornerRadius = 22.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        color = if (checked) MuseGreen else Color.White,
                    )
                    Text(
                        subtitle,
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                MuseMotionToggle(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = controlsEnabled,
                )
            }
            AnimatedVisibility(
                visible = checked,
                enter = fadeIn(animationSpec = tween(140)) +
                    expandVertically(animationSpec = tween(180)),
                exit = fadeOut(animationSpec = tween(100)) +
                    shrinkVertically(animationSpec = tween(140)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Slider(
                        value = value.toFloat(),
                        onValueChange = {
                            onValueChange(it.roundToLong().toInt())
                        },
                        valueRange = valueRange.first.toFloat()..
                            valueRange.last.toFloat(),
                        enabled = controlsEnabled,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        valueLabel(value),
                        color = MuseGreen,
                        fontWeight = FontWeight.SemiBold,
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
    visualMode: MuseVisualMode,
    resolvedVisualProfile: MuseVisualProfile,
    rainLevel: MuseRainLevel,
    visualIntensity: MuseVisualIntensity,
    onVisualModeChange: (MuseVisualMode) -> Unit,
    onRainLevelChange: (MuseRainLevel) -> Unit,
    onVisualIntensityChange: (MuseVisualIntensity) -> Unit,
) {
    val playCounts by viewModel.playCounts.collectAsStateWithLifecycle()
    val topPlayed by viewModel.topPlayedTracks.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val recentTracks by viewModel.recentTracks.collectAsStateWithLifecycle()
    val flowSeed = currentTrack ?: recentTracks.firstOrNull()
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = MuseGreen,
                        )
                        Spacer(Modifier.size(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Atmosphere Studio",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                if (visualMode == MuseVisualMode.Auto) {
                                    "Auto is using ${resolvedVisualProfile.label} for the current local track."
                                } else {
                                    resolvedVisualProfile.description
                                },
                                color = MuseMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = MuseVisualMode.entries,
                            key = { it.storedValue },
                        ) { mode ->
                            val profile = mode.fixedProfile ?: resolvedVisualProfile
                            MuseVisualSceneCard(
                                mode = mode,
                                profile = profile,
                                selected = visualMode == mode,
                                onClick = { onVisualModeChange(mode) },
                            )
                        }
                    }

                    Text(
                        "Rain",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = MuseRainLevel.entries,
                            key = { it.storedValue },
                        ) { level ->
                            FilterChip(
                                selected = rainLevel == level,
                                onClick = { onRainLevelChange(level) },
                                label = { Text(level.label) },
                            )
                        }
                    }

                    Text(
                        "Motion intensity",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = MuseVisualIntensity.entries,
                            key = { it.storedValue },
                        ) { intensity ->
                            FilterChip(
                                selected = visualIntensity == intensity,
                                onClick = { onVisualIntensityChange(intensity) },
                                label = { Text(intensity.label) },
                            )
                        }
                    }

                    Text(
                        "These scenes are rendered by live Compose layers. They do not replace the interface with a screenshot.",
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

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

        item {
            SettingsRow(
                Icons.Rounded.QueueMusic,
                "Muse Flow",
                if (flowSeed == null) {
                    "Play a track first, then Muse can build your adaptive local queue"
                } else {
                    "Start from ${flowSeed.title} using private on-device listening signals"
                },
                {
                    flowSeed?.let { seed ->
                        viewModel.playMuseFlow(seed)
                        navController.navigate("nowPlaying")
                    }
                },
            )
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
private fun MuseVisualSceneCard(
    mode: MuseVisualMode,
    profile: MuseVisualProfile,
    selected: Boolean,
    onClick: () -> Unit,
) {
    MuseGlassAction(
        onClick = onClick,
        modifier = Modifier
            .width(154.dp)
            .height(112.dp),
        variant = if (selected) {
            MuseGlassVariant.Selected
        } else {
            MuseGlassVariant.Elevated
        },
        cornerRadius = 22.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = profile.previewColors(),
                    ),
                )
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    mode.label,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (mode == MuseVisualMode.Auto) {
                        "Follows local song mood"
                    } else {
                        profile.description
                    },
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
    MuseGlassAction(
        onClick = onOpen,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .height(72.dp),
        variant = MuseGlassVariant.Elevated,
        cornerRadius = 24.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TrackArtwork(
                    track = track,
                    modifier = Modifier.size(50.dp),
                    contentDescription = null,
                )
                Spacer(Modifier.size(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        state.title.ifBlank { "Muse" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        state.artist,
                        color = MuseMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                MusePlayingPulse(
                    playing = state.isPlaying,
                    modifier = Modifier
                        .size(width = 28.dp, height = 30.dp)
                        .padding(horizontal = 2.dp),
                )

                IconButton(onClick = onPlayPause) {
                    MuseGlassSurface(
                        modifier = Modifier.size(38.dp),
                        variant = if (state.isPlaying) {
                            MuseGlassVariant.Selected
                        } else {
                            MuseGlassVariant.Standard
                        },
                        cornerRadius = 19.dp,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = MuseGreen,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            MuseMiniProgress(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
            )
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
    MuseGlassAction(
        onClick = onClick,
        modifier = modifier.height(98.dp),
        variant = MuseGlassVariant.Elevated,
        cornerRadius = 22.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MuseGreen,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun QueueAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    MuseGlassAction(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        variant = if (selected) {
            MuseGlassVariant.Selected
        } else {
            MuseGlassVariant.Elevated
        },
        cornerRadius = 22.dp,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) MuseGreen else Color.White,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.height(7.dp))
            Text(
                label,
                color = if (selected) MuseGreen else Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun MusePillTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.035f else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 560f,
        ),
        label = "MusePillScale",
    )
    val color by animateColorAsState(
        targetValue = if (selected) MuseGreen else Color.White,
        animationSpec = tween(145),
        label = "MusePillColor",
    )

    MuseGlassAction(
        onClick = onClick,
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        variant = if (selected) {
            MuseGlassVariant.Selected
        } else {
            MuseGlassVariant.Standard
        },
        cornerRadius = 23.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                color = color,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun NowPlayingAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    MuseGlassAction(
        onClick = onClick,
        modifier = modifier.height(74.dp),
        variant = MuseGlassVariant.Standard,
        cornerRadius = 20.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MuseGreen,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(5.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun MuseMoodTile(
    mood: MuseMood,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val accent = when (mood) {
        MuseMood.Chill -> Color(0xFF5EF2D0)
        MuseMood.Workout -> Color(0xFFFFC857)
        MuseMood.Love -> Color(0xFFFF75A8)
        MuseMood.Focus -> Color(0xFF68B9FF)
        MuseMood.Party -> Color(0xFFC98BFF)
        MuseMood.Sleep -> Color(0xFFAAA8FF)
    }
    val icon = when (mood) {
        MuseMood.Chill -> Icons.Rounded.MusicNote
        MuseMood.Workout -> Icons.Rounded.Equalizer
        MuseMood.Love -> Icons.Rounded.Favorite
        MuseMood.Focus -> Icons.Rounded.Tune
        MuseMood.Party -> Icons.Rounded.Explore
        MuseMood.Sleep -> Icons.Rounded.Timer
    }

    MuseGlassAction(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        variant = if (selected) {
            MuseGlassVariant.Selected
        } else {
            MuseGlassVariant.Elevated
        },
        cornerRadius = 22.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = mood.name,
                tint = accent,
                modifier = Modifier.size(30.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                mood.name,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MuseGreen else Color.White,
            )
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
    MuseGlassSurface(
        modifier = modifier,
        variant = MuseGlassVariant.Standard,
        cornerRadius = 20.dp,
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
        Text(
            title,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Text(
                trailing,
                color = MuseGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
    MuseGlassAction(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        variant = MuseGlassVariant.Standard,
        cornerRadius = 20.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MuseGreen)
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    color = MuseMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
    MuseGlassAction(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        variant = MuseGlassVariant.Standard,
        cornerRadius = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0x56123B1D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MuseGreen.copy(alpha = 0.22f),
                ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MuseGreen,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(Modifier.size(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = MuseMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.82f),
            )
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
