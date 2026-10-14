package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Song
import com.musicplayer.domain.formatDuration
import com.musicplayer.ui.components.SongArtwork

@Composable
fun SongDetailsDialog(song: Song, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text("Información") }, text = {
            SelectionContainer {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SongArtwork(song.artworkPath, Modifier.size(120.dp).align(Alignment.CenterHorizontally))
                    Detail("Título", song.title)
                    Detail("Artista", song.artist)
                    Detail("Álbum", song.album)
                    Detail("Duración", formatDuration(song.duration))
                    song.genre?.let { Detail("Género", it) }
                    song.year?.let { Detail("Año", it) }
                    song.trackNumber?.let { Detail("Pista", it.toString()) }
                    Detail("Archivo", song.fileName)
                    Detail("Ubicación", song.uri.toString())
                }
            }
        })
}

@Composable
private fun Detail(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
