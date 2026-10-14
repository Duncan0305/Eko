package com.musicplayer.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.musicplayer.data.model.Song
import com.musicplayer.ui.screens.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryViewModel

@Composable
fun EkoNavigation(viewModel: LibraryViewModel, onSelectFolder: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val reducedMotion = rememberReducedMotion()
    val snackbar = remember { SnackbarHostState() }
    var detail by remember { mutableStateOf<Song?>(null) }
    LaunchedEffect(state.message) {
        state.message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, contentWindowInsets = WindowInsets(0)) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (state.starting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (reducedMotion) Text("Abriendo tu biblioteca…") else CircularProgressIndicator()
                }
            } else {
                val destination = if (state.folder == null) "welcome" else "library"
                LaunchedEffect(destination) {
                    if (nav.currentDestination?.route != destination) nav.navigate(destination) {
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
                            viewModel::refresh, viewModel::cancelScan, { nav.navigate("settings") }, { detail = it }, reducedMotion)
                    }
                    composable("settings") { SettingsScreen(state, { nav.popBackStack() }, onSelectFolder) }
                }
            }
            detail?.let { SongDetailsDialog(it) { detail = null } }
        }
    }
}
