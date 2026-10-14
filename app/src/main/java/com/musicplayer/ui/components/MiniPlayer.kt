package com.musicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.playback.PlaybackState
import com.musicplayer.ui.theme.EkoGreen
import com.musicplayer.ui.theme.EkoOutline

@Composable
fun MiniPlayer(
    state: PlaybackState,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    reducedMotion: Boolean,
) {
    val song = state.currentSong ?: return
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
        Column {
            val progress = if (state.duration > 0) state.position.toFloat() / state.duration else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = EkoGreen,
                trackColor = EkoOutline,
                drawStopIndicator = {},
            )
            Row(
                Modifier.fillMaxWidth().heightIn(min = 76.dp)
                    .clickable(role = Role.Button, onClickLabel = "Abrir reproductor") { onOpen() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongArtwork(song.artworkPath, Modifier.size(52.dp).clip(MaterialTheme.shapes.small))
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(song.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (state.isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else PlayerIconButton(
                    if (state.isPlaying) Symbol.Pause else Symbol.Play,
                    if (state.isPlaying) "Pausar" else "Reproducir",
                    onToggle,
                    reducedMotion = reducedMotion,
                )
                PlayerIconButton(Symbol.Next, "Siguiente", onNext, enabled = state.nextSong != null,
                    reducedMotion = reducedMotion)
            }
        }
    }
}
