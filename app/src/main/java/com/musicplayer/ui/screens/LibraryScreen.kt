package com.musicplayer.ui.screens

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.musicplayer.data.model.Song
import com.musicplayer.data.model.SongSort
import com.musicplayer.domain.LibraryOrganizer
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryUiState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(state: LibraryUiState, onQuery: (String) -> Unit, onSort: (SongSort) -> Unit,
                  onChangeFolder: () -> Unit, onRefresh: () -> Unit, onCancel: () -> Unit,
                  onPlay: (List<Song>, Song) -> Unit, onDetails: (Song) -> Unit,
                  currentSongId: String?, reducedMotion: Boolean,
                  bottomContentPadding: Dp = 32.dp) {
    var sortMenu by remember { mutableStateOf(false) }
    var selectedSection by rememberSaveable { mutableStateOf(LibrarySection.Songs) }
    var selectedAlbumId by rememberSaveable { mutableStateOf<String?>(null) }
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val albums = remember(state.catalogSongs, state.accessError) {
        if (state.accessError) emptyList() else LibraryOrganizer.albums(state.catalogSongs)
    }
    val selectedAlbum = remember(albums, selectedAlbumId) { albums.firstOrNull { it.id == selectedAlbumId } }
    val closeAlbum: () -> Unit = {
        selectedAlbumId = null
        scope.launch { gridState.scrollToItem(2) }
    }
    PredictiveBackHandler(enabled = selectedAlbum != null) { progress ->
        progress.collect { /* El detalle permanece estable mientras el usuario decide el gesto. */ }
        closeAlbum()
    }
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = bottomContentPadding),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "brand", span = { GridItemSpan(maxLineSpan) }) { BrandHeader() }
            item(key = "heading", span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("BIBLIOTECA LOCAL", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
                        Spacer(Modifier.height(6.dp))
                        Text("Mi música", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(6.dp))
                        Text(if (state.accessError) "Acceso a la carpeta pendiente" else
                            "${state.totalSongs} ${if (state.totalSongs == 1) "canción" else "canciones"} · En tu dispositivo",
                            style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText)
                    }
                    FilledTonalIconButton(onClick = onRefresh, enabled = !state.scanning && !state.selecting,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = EkoSurface, contentColor = EkoGreen)) {
                        EkoIcon(Symbol.Refresh, description = "Actualizar biblioteca")
                    }
                }
            }
            item(key = "tabs", span = { GridItemSpan(maxLineSpan) }) {
                LibraryTabs(
                    selected = selectedSection,
                    onSelect = {
                        selectedSection = it
                        if (it != LibrarySection.Albums) selectedAlbumId = null
                        scope.launch {
                            if (reducedMotion) gridState.scrollToItem(2)
                            else gridState.animateScrollToItem(2)
                        }
                    },
                    reducedMotion = reducedMotion,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
            if (selectedSection == LibrarySection.Songs) {
                item(key = "search", span = { GridItemSpan(maxLineSpan) }) { SearchBar(state.query, onQuery) }
                item(key = "folder", span = { GridItemSpan(maxLineSpan) }) {
                    FolderHeader(state.folder?.name.orEmpty(), !state.selecting, onChangeFolder)
                }
                item(key = "divider", span = { GridItemSpan(maxLineSpan) }) { HorizontalDivider(color = EkoOutline) }
                if (state.scanning) item(key = "progress", span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.padding(top = 16.dp).semantics { liveRegion = LiveRegionMode.Polite }) {
                        if (reducedMotion) Text("Escaneo en curso", color = EkoGreen, style = MaterialTheme.typography.labelMedium)
                        else LinearProgressIndicator(Modifier.fillMaxWidth(), color = EkoGreen, trackColor = EkoElevated)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
                                Text("Encontrando tu música…", style = MaterialTheme.typography.titleMedium)
                                Text("${state.progress.inspected} archivos revisados · ${state.progress.found} canciones",
                                    style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText)
                            }
                            TextButton(onClick = onCancel) { Text("Cancelar") }
                        }
                    }
                }
                item(key = "sort", span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (state.query.isBlank()) "CANCIONES" else "${state.songs.size} RESULTADOS",
                            Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = EkoSecondaryText)
                        Box {
                            TextButton(onClick = { sortMenu = true }) {
                                EkoIcon(Symbol.Sort, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(when (state.sort) { SongSort.TITLE -> "Título"; SongSort.DURATION -> "Duración"; SongSort.MODIFIED -> "Recientes" })
                            }
                            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                                SongSort.entries.forEach { sort ->
                                    DropdownMenuItem(text = { Text(sort.label) }, onClick = { sortMenu = false; onSort(sort) },
                                        trailingIcon = if (state.sort == sort) ({ EkoIcon(Symbol.Check, description = "Seleccionado", tint = EkoGreen) }) else null)
                                }
                            }
                        }
                    }
                }
                val emptyState = when {
                    state.accessError -> "access"
                    state.scanning && state.songs.isEmpty() -> "scanning"
                    state.totalSongs == 0 -> "empty"
                    state.songs.isEmpty() -> "search"
                    else -> "loaded"
                }
                if (emptyState != "loaded") item(key = "state", span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedContent(targetState = emptyState, label = "Estado de biblioteca",
                        transitionSpec = { fadeIn(tween(if (reducedMotion) 0 else EkoMotion.Enter)) togetherWith
                            fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit)) }) { current ->
                        when (current) {
                            "access" -> LibraryState("Volvamos a conectar tu música",
                                "La carpeta ya no está disponible o su permiso cambió. Vuelve a seleccionarla para recuperar el acceso.",
                                Symbol.Folder, "Seleccionar carpeta", onChangeFolder)
                            "scanning" -> LibraryState("Tu colección está tomando forma",
                                "Estamos leyendo tus archivos. Puedes cancelar el escaneo en cualquier momento.")
                            "empty" -> LibraryState("Aquí empieza tu colección",
                                "Aún no hay canciones en la biblioteca. Actualiza el índice o elige una carpeta con archivos de audio.",
                                Symbol.Music, "Elegir otra carpeta", onChangeFolder)
                            "search" -> LibraryState("No encontramos coincidencias",
                                "Prueba con otro título, artista o álbum.", Symbol.Search, "Limpiar búsqueda", { onQuery("") })
                        }
                    }
                }
                if (!state.accessError) items(
                    items = state.songs,
                    key = { it.id },
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = { "song" },
                ) { song ->
                    SongItem(song, song.id == currentSongId, { onPlay(state.songs, it) }, onDetails,
                        if (reducedMotion) Modifier else Modifier.animateItem(
                            fadeInSpec = tween(EkoMotion.Enter), placementSpec = tween(EkoMotion.Enter), fadeOutSpec = tween(EkoMotion.Exit)))
                }
            } else if (selectedSection == LibrarySection.Albums) {
                if (selectedAlbum != null) {
                    item(key = "album-detail-${selectedAlbum.id}", span = { GridItemSpan(maxLineSpan) }) {
                        Reveal(0, reducedMotion) {
                            AlbumDetailHeader(
                                album = selectedAlbum,
                                onBack = closeAlbum,
                            )
                        }
                    }
                    itemsIndexed(
                        items = selectedAlbum.songs,
                        key = { _: Int, song: Song -> "album-track-${selectedAlbum.id}-${song.id}" },
                        span = { _: Int, _: Song -> GridItemSpan(maxLineSpan) },
                        contentType = { _: Int, _: Song -> "album-track" },
                    ) { index: Int, song: Song ->
                        Reveal((index + 1).coerceAtMost(4), reducedMotion) {
                            AlbumTrackItem(
                                song = song,
                                position = index + 1,
                                isActive = song.id == currentSongId,
                                onPlay = { onPlay(selectedAlbum.songs, song) },
                                onDetails = { onDetails(song) },
                            )
                        }
                    }
                } else {
                    item(key = "albums-heading", span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 18.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("ÁLBUMES", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
                                Spacer(Modifier.height(5.dp))
                                Text("Todos tus discos", style = MaterialTheme.typography.titleLarge)
                            }
                            Text(
                                "${albums.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = EkoSecondaryText,
                            )
                        }
                    }
                    if (albums.isEmpty()) {
                        item(key = "albums-empty", span = { GridItemSpan(maxLineSpan) }) {
                            LibraryState(
                                title = if (state.accessError) "Volvamos a conectar tu música" else "Aún no hay álbumes",
                                body = if (state.accessError)
                                    "Vuelve a seleccionar la carpeta para recuperar tus álbumes."
                                else "Los álbumes aparecerán cuando EKO encuentre canciones en tu biblioteca.",
                                symbol = if (state.accessError) Symbol.Folder else Symbol.Album,
                                action = if (state.accessError) "Seleccionar carpeta" else null,
                                onAction = onChangeFolder,
                            )
                        }
                    } else {
                        items(albums, key = { it.id }, contentType = { "album" }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = {
                                    selectedAlbumId = album.id
                                    scope.launch {
                                        gridState.scrollToItem(2)
                                    }
                                },
                                modifier = Modifier.padding(bottom = 24.dp),
                            )
                        }
                    }
                }
            } else {
                item(key = "placeholder-${selectedSection.name}", span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedContent(
                        targetState = selectedSection,
                        label = "Sección de biblioteca",
                        transitionSpec = { fadeIn(tween(if (reducedMotion) 0 else EkoMotion.Enter)) togetherWith
                            fadeOut(tween(if (reducedMotion) 0 else EkoMotion.Exit)) },
                    ) { section ->
                        LibrarySectionPlaceholder(section)
                    }
                }
            }
        }
    }
}
