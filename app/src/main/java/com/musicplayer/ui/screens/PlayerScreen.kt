package com.musicplayer.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.musicplayer.R
import com.musicplayer.data.model.Song
import com.musicplayer.domain.formatDuration
import com.musicplayer.playback.PlaybackState
import com.musicplayer.playback.RepeatMode
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import java.io.File

@Composable
fun PlayerScreen(
    state: PlaybackState,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onQueueItem: (Int) -> Unit,
    onDetails: (Song) -> Unit,
    reducedMotion: Boolean,
) {
    val song = state.currentSong
    var queueVisible by remember { mutableStateOf(false) }
    var menuVisible by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val useWideLayout = maxWidth >= 700.dp && maxHeight < 760.dp
        val compactHeight = maxHeight < 800.dp
        if (song != null) PlayerBackdrop(song.artworkPath)
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlayerIconButton(Symbol.Back, "Volver a la biblioteca", onBack, reducedMotion = reducedMotion)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("REPRODUCIENDO AHORA", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
                    Text("Biblioteca local", style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText)
                }
                Box {
                    PlayerIconButton(Symbol.More, "Más opciones", { menuVisible = true }, reducedMotion = reducedMotion)
                    DropdownMenu(menuVisible, { menuVisible = false }) {
                        DropdownMenuItem(
                            text = { Text("Información de la canción") },
                            onClick = { menuVisible = false; song?.let(onDetails) },
                            leadingIcon = { EkoIcon(Symbol.Info) },
                            enabled = song != null,
                        )
                        DropdownMenuItem(
                            text = { Text("Ver cola") },
                            onClick = { menuVisible = false; queueVisible = true },
                            leadingIcon = { EkoIcon(Symbol.Queue) },
                        )
                    }
                }
            }

            if (song == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LibraryState("Nada sonando", "Elige una canción de tu biblioteca para comenzar.", Symbol.Music,
                        "Volver a la biblioteca", onBack)
                }
            } else if (useWideLayout) {
                Row(
                    Modifier.fillMaxSize().widthIn(max = 1040.dp).align(Alignment.CenterHorizontally)
                        .padding(horizontal = 40.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                ) {
                    PlayerArtwork(song, state.currentIndex, reducedMotion, Modifier.weight(1f))
                    PlayerPanel(song, state, onToggle, onPrevious, onNext, onSeek, onShuffle, onRepeat,
                        { queueVisible = true }, reducedMotion, false, Modifier.weight(1f))
                }
            } else {
                Column(
                    Modifier.fillMaxSize().widthIn(max = 620.dp).align(Alignment.CenterHorizontally)
                        .verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlayerArtwork(song, state.currentIndex, reducedMotion,
                        Modifier.widthIn(max = if (compactHeight) 230.dp else 420.dp).fillMaxWidth())
                    Spacer(Modifier.height(if (compactHeight) 18.dp else 30.dp))
                    PlayerPanel(song, state, onToggle, onPrevious, onNext, onSeek, onShuffle, onRepeat,
                        { queueVisible = true }, reducedMotion, compactHeight)
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
    if (queueVisible) QueueSheet(state, { queueVisible = false }, { index -> onQueueItem(index); queueVisible = false })
}

@Composable
private fun PlayerBackdrop(artworkPath: String?) {
    Box(Modifier.fillMaxSize().background(EkoBackground)) {
        AsyncImage(
            model = artworkPath?.let(::File),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.11f),
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            EkoTurquoise.copy(alpha = 0.13f), EkoBackground.copy(alpha = 0.78f), EkoBackground,
        ))))
    }
}

@Composable
private fun PlayerArtwork(song: Song, index: Int, reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val transition = if (reducedMotion) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
    else (slideInHorizontally(tween(EkoMotion.Player)) { it / 5 } + fadeIn(tween(EkoMotion.Player))) togetherWith
        (slideOutHorizontally(tween(EkoMotion.Exit)) { -it / 5 } + fadeOut(tween(EkoMotion.Exit)))
    AnimatedContent(song, modifier, transitionSpec = { transition }, contentKey = { "${it.id}:$index" }, label = "Cambio de carátula") { current ->
        Surface(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).widthIn(max = 420.dp),
            shape = RoundedCornerShape(28.dp),
            color = EkoElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, EkoOutline.copy(alpha = 0.8f)),
        ) {
            AsyncImage(
                model = current.artworkPath?.let(::File),
                contentDescription = "Carátula de ${current.title}",
                placeholder = painterResource(R.drawable.artwork_placeholder),
                fallback = painterResource(R.drawable.artwork_placeholder),
                error = painterResource(R.drawable.artwork_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlayerPanel(
    song: Song,
    state: PlaybackState,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onQueue: () -> Unit,
    reducedMotion: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(song.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Text(song.artist, style = MaterialTheme.typography.bodyLarge, color = EkoSecondaryText,
            textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (song.album != "Álbum desconocido") {
            Spacer(Modifier.height(2.dp))
            Text(song.album, style = MaterialTheme.typography.bodySmall, color = EkoInactive,
                textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(if (compact) 14.dp else 26.dp))
        SeekControl(state.position, state.duration, onSeek)
        Spacer(Modifier.height(if (compact) 10.dp else 18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            PlayerIconButton(Symbol.Shuffle, if (state.shuffleEnabled) "Desactivar aleatorio" else "Activar aleatorio",
                onShuffle, contentColor = if (state.shuffleEnabled) EkoGreen else EkoSecondaryText,
                reducedMotion = reducedMotion)
            PlayerIconButton(Symbol.Previous, "Anterior", onPrevious, size = if (compact) 48.dp else 54.dp, iconSize = 28.dp,
                enabled = state.currentIndex > 0 || state.repeatMode == RepeatMode.ALL || state.shuffleEnabled,
                reducedMotion = reducedMotion)
            if (state.isLoading) Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EkoGreen)
            } else PlayerIconButton(
                if (state.isPlaying) Symbol.Pause else Symbol.Play,
                if (state.isPlaying) "Pausar" else "Reproducir",
                onToggle,
                size = if (compact) 68.dp else 76.dp,
                iconSize = 34.dp,
                containerColor = EkoGreen,
                contentColor = EkoBackground,
                reducedMotion = reducedMotion,
            )
            PlayerIconButton(Symbol.Next, "Siguiente", onNext, size = if (compact) 48.dp else 54.dp, iconSize = 28.dp,
                enabled = state.nextSong != null, reducedMotion = reducedMotion)
            val repeatSymbol = if (state.repeatMode == RepeatMode.ONE) Symbol.RepeatOne else Symbol.Repeat
            val repeatLabel = when (state.repeatMode) {
                RepeatMode.OFF -> "Activar repetir todo"
                RepeatMode.ALL -> "Activar repetir canción"
                RepeatMode.ONE -> "Desactivar repetición"
            }
            PlayerIconButton(repeatSymbol, repeatLabel, onRepeat,
                contentColor = if (state.repeatMode == RepeatMode.OFF) EkoSecondaryText else EkoGreen,
                reducedMotion = reducedMotion)
        }
        Spacer(Modifier.height(if (compact) 8.dp else 18.dp))
        TextButton(onClick = onQueue) {
            EkoIcon(Symbol.Queue, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cola · ${state.queue.size} ${if (state.queue.size == 1) "canción" else "canciones"}")
        }
    }
}

@Composable
private fun SeekControl(position: Long, duration: Long, onSeek: (Long) -> Unit) {
    var dragging by remember { mutableStateOf(false) }
    var draggedPosition by remember { mutableLongStateOf(0L) }
    val safeDuration = duration.coerceAtLeast(1)
    val shownPosition = if (dragging) draggedPosition else position.coerceIn(0, safeDuration)
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = shownPosition.toFloat(),
            onValueChange = { dragging = true; draggedPosition = it.toLong() },
            onValueChangeFinished = { dragging = false; onSeek(draggedPosition) },
            valueRange = 0f..safeDuration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EkoGreen, activeTrackColor = EkoGreen,
                inactiveTrackColor = EkoOutline,
            ),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(formatDuration(shownPosition), style = MaterialTheme.typography.labelMedium, color = EkoSecondaryText)
            Spacer(Modifier.weight(1f))
            Text(formatDuration(duration), style = MaterialTheme.typography.labelMedium, color = EkoSecondaryText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(state: PlaybackState, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = EkoSurface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.78f).navigationBarsPadding().padding(bottom = 12.dp)) {
            Text("A continuación", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            Text("${state.queue.size} ${if (state.queue.size == 1) "canción" else "canciones"}",
                style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                itemsIndexed(state.queue, key = { _, song -> song.id }) { index, song ->
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { onSelect(index) }
                        .padding(horizontal = 24.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium,
                        color = if (index == state.currentIndex) EkoGreen else EkoInactive,
                        modifier = Modifier.width(28.dp))
                    SongArtwork(song.artworkPath, Modifier.size(46.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium,
                            color = if (index == state.currentIndex) EkoGreen else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(formatDuration(song.duration), style = MaterialTheme.typography.labelMedium, color = EkoSecondaryText)
                }
            }
            }
        }
    }
}
