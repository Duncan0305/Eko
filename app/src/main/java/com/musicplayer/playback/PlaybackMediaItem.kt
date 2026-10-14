package com.musicplayer.playback

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.musicplayer.data.model.Song
import java.io.File

private const val KEY_URI = "eko_uri"
private const val KEY_DURATION = "eko_duration"
private const val KEY_GENRE = "eko_genre"
private const val KEY_YEAR = "eko_year"
private const val KEY_TRACK = "eko_track"
private const val KEY_FILE = "eko_file"
private const val KEY_MODIFIED = "eko_modified"
private const val KEY_ARTWORK = "eko_artwork"

fun Song.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putString(KEY_URI, uri.toString())
        putLong(KEY_DURATION, duration)
        putString(KEY_GENRE, genre)
        putString(KEY_YEAR, year)
        putInt(KEY_TRACK, trackNumber ?: -1)
        putString(KEY_FILE, fileName)
        putLong(KEY_MODIFIED, lastModified)
        putString(KEY_ARTWORK, artworkPath)
    }
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setGenre(genre)
            .setTrackNumber(trackNumber)
            .setArtworkUri(artworkPath?.let { Uri.fromFile(File(it)) })
            .setExtras(extras)
            .build())
        .build()
}

fun MediaItem.toSong(): Song {
    val metadata = mediaMetadata
    val extras = metadata.extras
    val resolvedUri = extras?.getString(KEY_URI)?.toUri()
        ?: localConfiguration?.uri
        ?: Uri.EMPTY
    val fileName = extras?.getString(KEY_FILE)
        ?: resolvedUri.lastPathSegment
        ?: "Audio local"
    return Song(
        id = mediaId.ifBlank { resolvedUri.toString() },
        uri = resolvedUri,
        title = metadata.title?.toString()?.ifBlank { fileName } ?: fileName,
        artist = metadata.artist?.toString()?.ifBlank { "Artista desconocido" } ?: "Artista desconocido",
        album = metadata.albumTitle?.toString()?.ifBlank { "Álbum desconocido" } ?: "Álbum desconocido",
        duration = extras?.getLong(KEY_DURATION, 0) ?: 0,
        genre = extras?.getString(KEY_GENRE),
        year = extras?.getString(KEY_YEAR),
        trackNumber = extras?.getInt(KEY_TRACK, -1)?.takeIf { it > 0 },
        fileName = fileName,
        lastModified = extras?.getLong(KEY_MODIFIED, 0) ?: 0,
        artworkPath = extras?.getString(KEY_ARTWORK),
    )
}
