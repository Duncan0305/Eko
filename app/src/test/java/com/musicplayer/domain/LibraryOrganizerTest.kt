package com.musicplayer.domain

import android.net.Uri
import com.musicplayer.data.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LibraryOrganizerTest {
    private fun song(id: String, title: String, artist: String = "Artista", album: String = "Álbum", duration: Long = 1000, modified: Long = 0) =
        Song(id, Uri.parse("content://test/$id"), title, artist, album, duration, null, null, null, "$id.mp3", modified)
    private val songs = listOf(song("1", "Órbita", "Érika", "Noches", 3000, 20),
        song("2", "Amanecer", "Sol", "Luz", 1000, 30), song("3", "Brisa", "Mar", "Costa", 2000, 0))

    @Test fun searchesAcrossAllThreeFieldsIgnoringAccentsAndCase() {
        for (query in listOf("orbita", "ERIKA", "noches", "  erika  orbita  "))
            assertEquals(listOf("1"), LibraryOrganizer.organize(songs, query, SongSort.TITLE).map { it.id })
        assertTrue(LibraryOrganizer.organize(songs, "missing", SongSort.TITLE).isEmpty())
    }

    @Test fun allOrdersAreDeterministicAndMissingDatesLast() {
        assertEquals(listOf("2", "3", "1"), LibraryOrganizer.organize(songs, "", SongSort.TITLE).map { it.id })
        assertEquals(listOf("2", "3", "1"), LibraryOrganizer.organize(songs, "", SongSort.DURATION).map { it.id })
        assertEquals(listOf("2", "1", "3"), LibraryOrganizer.organize(songs, "", SongSort.MODIFIED).map { it.id })
        assertEquals(listOf("1", "2", "3"), songs.map { it.id })
    }

    @Test fun thousandsOfSongsCanBeFilteredWithoutStorage() {
        val library = (1..10000).map { song("$it", "Canción $it", album = if (it % 20 == 0) "Selección" else "Colección") }
        assertEquals(500, LibraryOrganizer.organize(library, "seleccion", SongSort.TITLE).size)
        assertEquals(10000, LibraryOrganizer.organize(library, "   ", SongSort.TITLE).size)
    }

    @Test fun groupsAlbumsByTitleAndArtistAndOrdersTheirTracks() {
        val library = listOf(
            song("1", "Final", artist = "Aurora", album = "Luz").copy(trackNumber = 2, year = "2025"),
            song("2", "Inicio", artist = "Aurora", album = "Luz").copy(trackNumber = 1),
            song("3", "Otra luz", artist = "Bruma", album = "Luz"),
        )

        val albums = LibraryOrganizer.albums(library)

        assertEquals(2, albums.size)
        assertEquals(listOf("2", "1"), albums.first { it.artist == "Aurora" }.songs.map { it.id })
        assertEquals("2025", albums.first { it.artist == "Aurora" }.year)
        assertEquals(listOf("Aurora", "Bruma"), albums.map { it.artist })
    }
}
