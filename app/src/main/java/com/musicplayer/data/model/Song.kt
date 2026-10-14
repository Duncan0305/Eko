package com.musicplayer.data.model

import android.net.Uri

data class Song(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val genre: String?,
    val year: String?,
    val trackNumber: Int?,
    val fileName: String,
    val lastModified: Long = 0,
    val artworkPath: String? = null,
)

data class MusicFolder(val uri: String, val name: String)

enum class SongSort(val label: String) {
    TITLE("Título · A–Z"), DURATION("Duración · menor primero"), MODIFIED("Modificación · recientes")
}
