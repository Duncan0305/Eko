package com.musicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Song
import com.musicplayer.data.model.SongSort
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryUiState

@Composable
fun LibraryScreen(state: LibraryUiState, onQuery: (String) -> Unit, onSort: (SongSort) -> Unit,
                  onChangeFolder: () -> Unit, onRefresh: () -> Unit, onCancel: () -> Unit,
                  onSettings: () -> Unit, onPlay: (Song) -> Unit, onDetails: (Song) -> Unit,
                  currentSongId: String?, reducedMotion: Boolean) {
    var sortMenu by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 840.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp)) {
            item(key = "brand") { BrandHeader(onSettings) }
            item(key = "heading") {
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
            item(key = "search") { SearchBar(state.query, onQuery) }
            item(key = "folder") { FolderHeader(state.folder?.name.orEmpty(), !state.selecting, onChangeFolder) }
            item(key = "divider") { HorizontalDivider(color = EkoOutline) }
            if (state.scanning) item(key = "progress") {
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
            item(key = "sort") {
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
            if (emptyState != "loaded") item(key = "state") {
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
            if (!state.accessError) items(state.songs, key = { it.id }, contentType = { "song" }) { song ->
                SongItem(song, song.id == currentSongId, onPlay, onDetails,
                    if (reducedMotion) Modifier else Modifier.animateItem(
                        fadeInSpec = tween(EkoMotion.Enter), placementSpec = tween(EkoMotion.Enter), fadeOutSpec = tween(EkoMotion.Exit)))
            }
        }
    }
}
