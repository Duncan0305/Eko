package com.musicplayer.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.musicplayer.data.model.Song
import com.musicplayer.playback.PlaybackController
import com.musicplayer.ui.components.*
import com.musicplayer.ui.screens.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryViewModel

@Composable
fun EkoNavigation(
    viewModel: LibraryViewModel,
    playbackController: PlaybackController,
    onSelectFolder: () -> Unit,
    onPlaybackStarted: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val reducedMotion = rememberReducedMotion()
    val snackbar = remember { SnackbarHostState() }
    var detail by remember { mutableStateOf<Song?>(null) }
    var bottomChromeHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val mainDestination = MainDestination.entries.firstOrNull { it.route == route }
    val bottomContentPadding = if (mainDestination == null) 32.dp else with(density) {
        bottomChromeHeight.toDp() + 16.dp
    }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    LaunchedEffect(playback.error) {
        playback.error?.let { snackbar.showSnackbar(it); playbackController.clearError() }
    }
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier.padding(bottom = bottomContentPadding),
                )
            },
            contentWindowInsets = WindowInsets(0),
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.starting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (reducedMotion) Text("Abriendo tu biblioteca…") else CircularProgressIndicator()
                }
            } else {
                val destination = if (state.folder == null) "welcome" else "home"
                LaunchedEffect(destination, route) {
                    val mustNavigate = (destination == "welcome" && route != "welcome") ||
                        (destination == "home" && route == "welcome")
                    if (mustNavigate) nav.navigate(destination) {
                        popUpTo(nav.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                NavHost(navController = nav, startDestination = destination,
                    enterTransition = { fadeIn(tween(if (reducedMotion) 0 else EkoMotion.Enter)) },
                    exitTransition = { fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit)) }) {
                    composable("welcome") { WelcomeScreen(state.selecting, onSelectFolder) }
                    composable("home") {
                        HomeScreen(
                            state = state,
                            currentSongId = playback.currentSong?.id,
                            reducedMotion = reducedMotion,
                            onPlay = { queue, song ->
                                onPlaybackStarted()
                                playbackController.playQueue(queue, song)
                            },
                            onOpenLibrary = {
                                nav.navigate("library") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            bottomContentPadding = bottomContentPadding,
                        )
                    }
                    composable("library") {
                        LibraryScreen(state, viewModel::search, viewModel::sort, onSelectFolder,
                            viewModel::refresh, viewModel::cancelScan,
                            { queue, song ->
                                onPlaybackStarted()
                                playbackController.playQueue(queue, song)
                            },
                            { detail = it }, playback.currentSong?.id, reducedMotion,
                            bottomContentPadding)
                    }
                    composable("settings") {
                        SettingsScreen(state, onSelectFolder, bottomContentPadding)
                    }
                    composable(
                        "player",
                        enterTransition = {
                            val duration = if (reducedMotion) 0 else EkoMotion.Player
                            fadeIn(tween(duration)) + slideInVertically(
                                animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                initialOffsetY = { height -> if (reducedMotion) 0 else height / 7 },
                            )
                        },
                        exitTransition = {
                            fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit))
                        },
                        popEnterTransition = {
                            fadeIn(tween(if (reducedMotion) 0 else EkoMotion.PlayerDismiss))
                        },
                        popExitTransition = {
                            val duration = if (reducedMotion) 0 else EkoMotion.PlayerDismiss
                            fadeOut(tween(duration)) + slideOutHorizontally(
                                animationSpec = tween(duration, easing = FastOutSlowInEasing),
                                targetOffsetX = { width -> if (reducedMotion) 0 else width / 4 },
                            )
                        },
                    ) {
                        PlayerScreen(playback, { nav.popBackStack() }, playbackController::togglePlayPause,
                            playbackController::previous, playbackController::next, playbackController::seekTo,
                            playbackController::toggleShuffle, playbackController::cycleRepeatMode,
                            playbackController::skipTo, { detail = it }, reducedMotion)
                    }
                }
            }
            detail?.let { SongDetailsDialog(it) { detail = null } }
        }
        }
        if (mainDestination != null) {
            Column(
                Modifier.align(Alignment.BottomCenter)
                    .onSizeChanged { bottomChromeHeight = it.height },
            ) {
                if (playback.currentSong != null) {
                    MiniPlayer(
                        state = playback,
                        onOpen = { nav.navigate("player") { launchSingleTop = true } },
                        onToggle = playbackController::togglePlayPause,
                        onPrevious = playbackController::previous,
                        onNext = playbackController::next,
                        reducedMotion = reducedMotion,
                    )
                }
                EkoBottomNavigation(mainDestination, { destination ->
                    nav.navigate(destination.route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, reducedMotion)
            }
        }
    }
}
