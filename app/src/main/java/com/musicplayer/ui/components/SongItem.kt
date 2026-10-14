package com.musicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Song
import com.musicplayer.domain.formatDuration

@Composable
fun SongItem(song: Song, isActive: Boolean, onPlay: (Song) -> Unit, onDetails: (Song) -> Unit, modifier: Modifier = Modifier) {
    var menu by remember { mutableStateOf(false) }
    Row(modifier.fillMaxWidth().clickable(role = Role.Button, onClickLabel = "Reproducir ${song.title}") { onPlay(song) }
        .padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        SongArtwork(song.artworkPath)
        Column(Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(formatDuration(song.duration), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            IconButton(onClick = { menu = true }) { EkoIcon(Symbol.More, description = "Opciones de ${song.title}") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Información de la canción") },
                    onClick = { menu = false; onDetails(song) }, leadingIcon = { EkoIcon(Symbol.Info) })
            }
        }
    }
}
