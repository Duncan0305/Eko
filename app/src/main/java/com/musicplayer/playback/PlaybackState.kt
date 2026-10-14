package com.musicplayer.playback

import com.musicplayer.data.model.Song

enum class RepeatMode { OFF, ONE, ALL }

data class PlaybackState(
    val connected: Boolean = false,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val position: Long = 0,
    val duration: Long = 0,
    val nextSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleEnabled: Boolean = false,
    val error: String? = null,
)
