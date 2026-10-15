package com.musicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Song
import com.musicplayer.domain.LibraryAlbum
import com.musicplayer.domain.formatDuration
import com.musicplayer.ui.theme.*

@Composable
fun AlbumCard(
    album: LibraryAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.clickable(
            role = Role.Button,
            onClickLabel = "Abrir ${album.title}",
            onClick = onClick,
        ),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            SongArtwork(
                path = album.artworkPath,
                modifier = Modifier.fillMaxWidth(),
                size = maxWidth,
                cornerRadius = 18.dp,
            )
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(9.dp),
                shape = CircleShape,
                color = EkoBackground.copy(alpha = .78f),
                contentColor = EkoText,
                border = BorderStroke(1.dp, EkoOutline.copy(alpha = .72f)),
            ) {
                Text(
                    "${album.songs.size}",
                    Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Spacer(Modifier.height(11.dp))
        Text(
            album.title,
            style = MaterialTheme.typography.titleMedium,
            color = EkoText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = EkoSecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "${album.songs.size} ${if (album.songs.size == 1) "canción" else "canciones"}",
            style = MaterialTheme.typography.labelMedium,
            color = EkoInactive,
        )
    }
}

@Composable
fun AlbumDetailHeader(
    album: LibraryAlbum,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp)) {
            EkoIcon(Symbol.Back, Modifier.size(18.dp), description = null)
            Spacer(Modifier.width(8.dp))
            Text("Todos los álbumes")
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            SongArtwork(album.artworkPath, size = 116.dp, cornerRadius = 20.dp)
            Column(Modifier.weight(1f).padding(start = 18.dp, bottom = 2.dp)) {
                Text("ÁLBUM", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
                Spacer(Modifier.height(5.dp))
                Text(
                    album.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EkoSecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    buildString {
                        append(album.songs.size)
                        append(if (album.songs.size == 1) " canción" else " canciones")
                        album.year?.let { append(" · "); append(it) }
                        append(" · ")
                        append(formatDuration(album.duration))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = EkoInactive,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = EkoOutline)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun AlbumTrackItem(
    song: Song,
    position: Int,
    isActive: Boolean,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menu by remember { mutableStateOf(false) }
    Row(
        modifier.fillMaxWidth()
            .clickable(role = Role.Button, onClickLabel = "Reproducir ${song.title}", onClick = onPlay)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(38.dp), contentAlignment = Alignment.Center) {
            if (isActive) EkoIcon(Symbol.Play, Modifier.size(15.dp), tint = EkoGreen)
            else Text(
                "${song.trackNumber ?: position}",
                style = MaterialTheme.typography.labelMedium,
                color = EkoInactive,
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive) EkoGreen else EkoText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = EkoSecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(formatDuration(song.duration), style = MaterialTheme.typography.labelMedium, color = EkoInactive)
        Box {
            IconButton(onClick = { menu = true }) {
                EkoIcon(Symbol.More, description = "Opciones de ${song.title}")
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text("Información de la canción") },
                    onClick = { menu = false; onDetails() },
                    leadingIcon = { EkoIcon(Symbol.Info) },
                )
            }
        }
    }
}
