package com.musicplayer.domain

import com.musicplayer.data.model.Song
import com.musicplayer.data.model.SongSort
import java.text.Normalizer
import java.util.Locale

data class LibraryAlbum(
    val id: String,
    val title: String,
    val artist: String,
    val year: String?,
    val artworkPath: String?,
    val songs: List<Song>,
) {
    val duration: Long get() = songs.sumOf { it.duration }
}

object LibraryOrganizer {
    private val marks = "\\p{M}+".toRegex()
    private fun normalize(value: String) = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(marks, "").lowercase(Locale.ROOT)

    fun organize(songs: List<Song>, query: String, sort: SongSort): List<Song> {
        val words = normalize(query.trim()).split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val filtered = if (words.isEmpty()) songs else songs.filter { song ->
            val searchable = normalize("${song.title} ${song.artist} ${song.album}")
            words.all { it in searchable }
        }
        val byTitle = compareBy<Song> { normalize(it.title) }.thenBy { it.id }
        return filtered.sortedWith(when (sort) {
            SongSort.TITLE -> byTitle
            SongSort.DURATION -> compareBy<Song> { it.duration }.then(byTitle)
            SongSort.MODIFIED -> compareByDescending<Song> { it.lastModified }.then(byTitle)
        })
    }

    /** Agrupa álbum y artista para no mezclar discos homónimos de intérpretes distintos. */
    fun albums(songs: List<Song>): List<LibraryAlbum> = songs
        .groupBy { song -> "${normalize(song.artist)}::${normalize(song.album)}" }
        .map { (id, albumSongs) ->
            val first = albumSongs.first()
            val orderedSongs = albumSongs.sortedWith(
                compareBy<Song> { it.trackNumber ?: Int.MAX_VALUE }
                    .thenBy { normalize(it.title) }
                    .thenBy { it.id },
            )
            LibraryAlbum(
                id = id,
                title = first.album,
                artist = first.artist,
                year = albumSongs.firstNotNullOfOrNull { it.year?.takeIf(String::isNotBlank) },
                artworkPath = albumSongs.firstNotNullOfOrNull { it.artworkPath },
                songs = orderedSongs,
            )
        }
        .sortedWith(compareBy<LibraryAlbum> { normalize(it.title) }
            .thenBy { normalize(it.artist) }
            .thenBy { it.id })
}

fun formatDuration(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return if (seconds >= 3600) "%d:%02d:%02d".format(Locale.ROOT, seconds / 3600, seconds / 60 % 60, seconds % 60)
    else "%d:%02d".format(Locale.ROOT, seconds / 60, seconds % 60)
}
