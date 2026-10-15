package com.musicplayer.domain

import android.net.Uri
import com.musicplayer.data.model.PlayRecord
import com.musicplayer.data.model.Song
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ListeningInsightsTest {
    private fun song(id: String, title: String, artist: String = "Artista", genre: String? = null) =
        Song(id, Uri.parse("content://test/$id"), title, artist, "Álbum", 1000, genre, null, null, "$id.mp3")

    private val songs = listOf(
        song("1", "Órbita", "Érika", "Ambient"),
        song("2", "Amanecer", "Érika", "Ambient"),
        song("3", "Brisa", "Mar", "Costa"),
        song("4", "Sol", "Mar", "Costa"),
        song("5", "Nieve", "Otra", null),
    )

    @Test fun emptyWhenNothingHasBeenPlayed() {
        val insights = ListeningInsights.build(songs, emptyMap())
        assertFalse(insights.hasHistory)
        assertTrue(insights.top.isEmpty())
        assertNull(insights.recommendation)
    }

    @Test fun ignoresUnknownSongsAndZeroCounts() {
        val plays = mapOf(
            "1" to PlayRecord(3, 10),
            "deleted" to PlayRecord(99, 99),
            "2" to PlayRecord(0, 5),
        )
        val insights = ListeningInsights.build(songs, plays)
        assertEquals(1, insights.stats.listenedSongs)
        assertEquals(3, insights.stats.totalPlays)
        assertEquals(listOf("1"), insights.top.map { it.song.id })
    }

    @Test fun ranksByPlaysThenRecencyThenTitle() {
        val plays = mapOf(
            "1" to PlayRecord(5, 100),
            "2" to PlayRecord(5, 200),
            "3" to PlayRecord(9, 1),
            "4" to PlayRecord(5, 200),
        )
        val insights = ListeningInsights.build(songs, plays)
        assertEquals(listOf("3", "2", "4", "1"), insights.top.map { it.song.id })
        assertEquals(24, insights.stats.totalPlays)
        assertEquals(4, insights.stats.listenedSongs)
    }

    @Test fun topArtistAndGenreAccumulateAcrossSongs() {
        val plays = mapOf(
            "1" to PlayRecord(2, 1),
            "2" to PlayRecord(3, 1), // Érika = 5, Ambient = 5
            "3" to PlayRecord(4, 1), // Mar = 4, Costa = 4
        )
        val insights = ListeningInsights.build(songs, plays)
        assertEquals("Érika", insights.stats.topArtist)
        assertEquals("Ambient", insights.stats.topGenre)
        assertEquals("3", insights.highlights.first().song.id)
        assertEquals(4, insights.highlights.first().playCount)
    }

    @Test fun recommendsPlaylistFromTopArtist() {
        val plays = mapOf("1" to PlayRecord(4, 1), "3" to PlayRecord(2, 1))
        val recommendation = ListeningInsights.build(songs, plays).recommendation
        assertNotNull(recommendation)
        assertEquals("Más de Érika", recommendation!!.title)
        assertEquals(listOf("2", "1"), recommendation.songs.map { it.id }) // ordenadas por título
    }

    @Test fun fallsBackToGenreWhenTopArtistHasOneSong() {
        val plays = mapOf("5" to PlayRecord(5, 1), "3" to PlayRecord(2, 1), "4" to PlayRecord(1, 1))
        val recommendation = ListeningInsights.build(songs, plays).recommendation
        assertNotNull(recommendation)
        assertEquals("Explora Costa", recommendation!!.title)
        assertEquals(listOf("3", "4"), recommendation.songs.map { it.id })
    }

    @Test fun topIsLimitedToTenAndHighlightsToFive() {
        val library = (1..14).map { song("$it", "Canción $it") }
        val plays = library.associate { it.id to PlayRecord(it.id.toInt(), it.id.toInt().toLong()) }
        val insights = ListeningInsights.build(library, plays)
        assertEquals(10, insights.top.size)
        assertEquals(5, insights.highlights.size)
        assertEquals("14", insights.top.first().song.id)
    }
}
