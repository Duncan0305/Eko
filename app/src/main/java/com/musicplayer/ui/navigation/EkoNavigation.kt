package com.musicplayer.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.musicplayer.data.model.Song
import com.musicplayer.playback.PlaybackController
import com.musicplayer.ui.components.MiniPlayer
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
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    LaunchedEffect(playback.error) {
        playback.error?.let { snackbar.showSnackbar(it); playbackController.clearError() }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (playback.currentSong != null && route != "player" && route != "welcome") {
                MiniPlayer(playback, { nav.navigate("player") { launchSingleTop = true } },
                    playbackController::togglePlayPause, playbackController::next, reducedMotion)
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.starting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (reducedMotion) Text("Abriendo tu biblioteca…") else CircularProgressIndicator()
                }
            } else {
                val destination = if (state.folder == null) "welcome" else "library"
                LaunchedEffect(destination, route) {
                    val mustNavigate = (destination == "welcome" && route != "welcome") ||
                        (destination == "library" && route == "welcome")
                    if (mustNavigate) nav.navigate(destination) {
                        popUpTo(nav.graph.id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                NavHost(navController = nav, startDestination = destination,
                    enterTransition = { fadeIn(tween(if (reducedMotion) 0 else EkoMotion.Enter)) },
                    exitTransition = { fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit)) }) {
                    composable("welcome") { WelcomeScreen(state.selecting, onSelectFolder) }
                    composable("library") {
                        LibraryScreen(state, viewModel::search, viewModel::sort, onSelectFolder,
                            viewModel::refresh, viewModel::cancelScan, { nav.navigate("settings") },
                            { song ->
                                onPlaybackStarted()
                                playbackController.playQueue(state.songs, song)
                            },
                            { detail = it }, playback.currentSong?.id, reducedMotion)
                    }
                    composable("settings") { SettingsScreen(state, { nav.popBackStack() }, onSelectFolder) }
                    composable(
                        "player",
                        enterTransition = { fadeIn(tween(if (reducedMotion) 0 else EkoMotion.Player)) + scaleIn(tween(if (reducedMotion) 0 else EkoMotion.Player), initialScale = 0.96f) },
                        exitTransition = { fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit)) + scaleOut(tween(if (reducedMotion) 0 else EkoMotion.Exit), targetScale = 0.98f) },
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
}
