package com.musicplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.musicplayer.data.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Único puente de la UI hacia el reproductor que vive en [PlaybackService]. */
class PlaybackController(
    context: Context,
    /** Se invoca una vez por cada canción que empieza a sonar, para alimentar las estadísticas de uso. */
    private val onSongStarted: (suspend (String) -> Unit)? = null,
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = mutableState.asStateFlow()

    private var controller: MediaController? = null
    private var positionJob: Job? = null
    private var pendingPlayback: Pair<List<Song>, Song>? = null
    private var recordedSongId: String? = null
    private var primed = false
    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java)),
    ).buildAsync()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = refresh(player)
        override fun onIsPlayingChanged(isPlaying: Boolean) = restartPositionUpdates()
        override fun onPlayerError(error: PlaybackException) {
            mutableState.value = mutableState.value.copy(
                isLoading = false,
                error = "No se pudo reproducir este archivo. Comprueba que siga disponible y que su formato sea compatible.",
            )
            controller?.takeIf { it.nextMediaItemIndex != C.INDEX_UNSET }?.run {
                seekToNextMediaItem()
                prepare()
                play()
            }
        }
    }

    init {
        controllerFuture.addListener({
            try {
                controller = controllerFuture.get().also {
                    it.addListener(listener)
                    refresh(it)
                    restartPositionUpdates()
                    pendingPlayback?.also { (queue, selected) ->
                        pendingPlayback = null
                        startQueue(it, queue, selected)
                    }
                }
            } catch (_: Exception) {
                mutableState.value = mutableState.value.copy(
                    connected = false,
                    error = "No se pudo iniciar el reproductor. Vuelve a intentarlo.",
                )
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun playQueue(queue: List<Song>, selected: Song) {
        val player = controller ?: run {
            pendingPlayback = queue to selected
            return
        }
        startQueue(player, queue, selected)
    }

    private fun startQueue(player: MediaController, queue: List<Song>, selected: Song) {
        val playableQueue = queue.distinctBy { it.uri.toString() }.ifEmpty { listOf(selected) }
        val selectedIndex = playableQueue.indexOfFirst { it.id == selected.id }.takeIf { it >= 0 } ?: 0
        player.setMediaItems(playableQueue.map(Song::toMediaItem), selectedIndex, C.TIME_UNSET)
        player.prepare()
        player.play()
        refresh(player)
    }

    fun togglePlayPause() {
        controller?.let { player ->
            if (player.isPlaying) player.pause()
            else {
                if (player.playbackState == Player.STATE_ENDED) player.seekToDefaultPosition()
                if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) player.prepare()
                player.play()
            }
        }
    }

    fun next() = controller?.seekToNextMediaItem() ?: Unit
    fun skipTo(index: Int) {
        controller?.takeIf { index in 0 until it.mediaItemCount }?.run {
            seekToDefaultPosition(index)
            play()
        }
    }
    fun previous() {
        controller?.let { player ->
            if (player.currentPosition > PREVIOUS_RESTART_THRESHOLD_MS) player.seekTo(0)
            else player.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = controller ?: return
        val duration = safeDuration(player)
        player.seekTo(positionMs.coerceIn(0, duration.coerceAtLeast(0)))
        refreshPosition(player)
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeatMode() {
        controller?.let { player ->
            player.repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun clearError() { mutableState.value = mutableState.value.copy(error = null) }

    private fun refresh(player: Player) {
        val queue = List(player.mediaItemCount) { player.getMediaItemAt(it).toSong() }
        val currentIndex = player.currentMediaItemIndex.takeIf { it in queue.indices } ?: -1
        val nextIndex = player.nextMediaItemIndex
        mutableState.value = mutableState.value.copy(
            connected = true,
            currentSong = queue.getOrNull(currentIndex),
            isPlaying = player.isPlaying,
            isLoading = player.playbackState == Player.STATE_BUFFERING,
            position = player.currentPosition.coerceAtLeast(0),
            duration = safeDuration(player),
            nextSong = queue.getOrNull(nextIndex),
            queue = queue,
            currentIndex = currentIndex,
            repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            },
            shuffleEnabled = player.shuffleModeEnabled,
        )
        trackPlayback()
        restartPositionUpdates()
    }

    /**
     * Registra una reproducción cuando empieza a sonar una canción distinta. La primera lectura
     * solo prepara el estado: reconectar con algo ya sonando no debe inflar el contador.
     */
    private fun trackPlayback() {
        val state = mutableState.value
        val song = state.currentSong
        if (!primed) {
            primed = true
            recordedSongId = if (state.isPlaying) song?.id else null
            return
        }
        if (song == null) { recordedSongId = null; return }
        if (!state.isPlaying || song.id == recordedSongId) return
        recordedSongId = song.id
        val recorder = onSongStarted ?: return
        scope.launch { recorder(song.uri.toString()) }
    }

    private fun safeDuration(player: Player): Long {
        val playerDuration = player.duration
        return if (playerDuration == C.TIME_UNSET || playerDuration < 0) {
            mutableState.value.currentSong?.duration ?: 0
        } else playerDuration
    }

    private fun restartPositionUpdates() {
        positionJob?.cancel()
        val player = controller ?: return
        refreshPosition(player)
        if (!player.isPlaying) return
        positionJob = scope.launch {
            while (isActive && player.isPlaying) {
                refreshPosition(player)
                delay(POSITION_UPDATE_MS)
            }
        }
    }

    private fun refreshPosition(player: Player) {
        mutableState.value = mutableState.value.copy(
            position = player.currentPosition.coerceAtLeast(0),
            duration = safeDuration(player),
            isPlaying = player.isPlaying,
            isLoading = player.playbackState == Player.STATE_BUFFERING,
        )
    }

    companion object {
        private const val POSITION_UPDATE_MS = 500L
        private const val PREVIOUS_RESTART_THRESHOLD_MS = 3_000L
    }
}
