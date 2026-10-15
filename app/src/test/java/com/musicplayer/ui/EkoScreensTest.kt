package com.musicplayer.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.musicplayer.data.model.*
import com.musicplayer.domain.ListeningInsights
import com.musicplayer.scanner.ScanProgress
import com.musicplayer.playback.PlaybackState
import com.musicplayer.playback.RepeatMode
import com.musicplayer.ui.components.MiniPlayer
import com.musicplayer.ui.screens.*
import com.musicplayer.ui.theme.EkoBackground
import com.musicplayer.ui.theme.EkoBlue
import com.musicplayer.ui.theme.EkoGreen
import com.musicplayer.ui.theme.EkoTheme
import com.musicplayer.viewmodel.LibraryUiState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w393dp-h852dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EkoScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun welcomeCallsFolderPicker() {
        var calls = 0
        compose.setContent { EkoTheme { Surface { WelcomeScreen(false) { calls++ } } } }
        compose.onNodeWithText("Seleccionar mi música").performScrollTo().performClick()
        assertEquals(1, calls)
        capture("welcome")
    }

    @Test fun libraryShowsDetailsAndSearchCallbacks() {
        var query = ""
        var selected: Song? = null
        val songs = listOf("Órbita", "Donde empieza el mar", "Un lugar para volver", "Luz de octubre", "Todo lo que queda").mapIndexed { i, title ->
            Song("$i", Uri.parse("content://test/$i"), title, "Artista de prueba ${i+1}", "Álbum de prueba", 180000L + i*17000, null, null, null, "$i.flac")
        }
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Mi colección"), songs = songs, totalSongs = songs.size)
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, { query = it }, {}, {}, {}, {},
            { _, song -> selected = song }, {}, null, true) } } }
        compose.onNodeWithText("Canción, artista o álbum").performTextInput("órbita")
        assertEquals("órbita", query)
        compose.onNodeWithText("Órbita").performClick()
        assertEquals("Órbita", selected?.title)
        capture("library")
    }

    @Test fun libraryShowsAlbumsAndTheirTracks() {
        val songs = listOf(
            Song("1", Uri.parse("content://test/1"), "Primera luz", "Aurora", "Noches", 180000,
                null, "2026", 1, "1.flac"),
            Song("2", Uri.parse("content://test/2"), "Última luz", "Aurora", "Noches", 210000,
                null, "2026", 2, "2.flac"),
            Song("3", Uri.parse("content://test/3"), "Marea", "Bruma", "Costa", 190000,
                null, null, 1, "3.flac"),
        )
        val state = LibraryUiState(
            starting = false,
            folder = MusicFolder("content://test/tree", "Mi colección"),
            songs = songs,
            catalogSongs = songs,
            totalSongs = songs.size,
        )
        var played: Song? = null
        lateinit var activity: ComponentActivity
        compose.setContent { EkoTheme { Surface {
            activity = checkNotNull(LocalActivity.current) as ComponentActivity
            LibraryScreen(state, {}, {}, {}, {}, {}, { _, song -> played = song }, {}, null, true)
        } } }

        compose.onNodeWithText("Álbumes").performClick()
        compose.onNodeWithText("Todos tus discos").assertIsDisplayed()
        capture("library-albums")
        repeat(2) {
            compose.onNodeWithText("Noches").performClick()
            compose.onNodeWithText("Todos los álbumes").assertIsDisplayed().performClick()
            compose.onNodeWithText("Todos tus discos").assertIsDisplayed()
        }
        capture("library-albums-after-reentry")
        compose.onNodeWithText("Noches").performClick()
        compose.onNodeWithText("Todos los álbumes").assertIsDisplayed()
        compose.onNodeWithText("Primera luz").performClick()
        assertEquals("Primera luz", played?.title)
        capture("library-album-detail")
        compose.runOnUiThread { activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.onNodeWithText("Todos tus discos").assertIsDisplayed()
    }

    @Test fun revokedPermissionOffersRecovery() {
        var selected = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), accessError = true)
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, { selected = true }, {}, {}, { _, _ -> }, {}, null, true) } } }
        compose.onNodeWithText("Seleccionar carpeta").performScrollTo().performClick()
        assertTrue(selected)
        capture("access-error")
    }

    @Test fun scanCanBeCancelledWithReducedMotion() {
        var cancelled = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), scanning = true,
            progress = ScanProgress(40, 12, 1))
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, {}, {}, { cancelled = true }, { _, _ -> }, {}, null, true) } } }
        compose.onNodeWithText("Cancelar").performScrollTo().performClick()
        assertTrue(cancelled)
    }

    @Test @Config(qualifiers = "w320dp-h640dp-mdpi", fontScale = 1.5f)
    fun compactWelcomeKeepsPrimaryActionReachableWithLargeText() {
        compose.setContent { EkoTheme { Surface { WelcomeScreen(false) {} } } }
        compose.onNodeWithText("Seleccionar mi música").performScrollTo().assertIsDisplayed()
        capture("welcome-compact-large-text")
    }

    @Test @Config(qualifiers = "w900dp-h700dp-mdpi")
    fun tabletWelcomeUsesAvailableWidth() {
        compose.setContent { EkoTheme { Surface { WelcomeScreen(false) {} } } }
        compose.onNodeWithText("Seleccionar mi música").assertIsDisplayed()
        capture("welcome-tablet")
    }

    @Test fun playerControlsExposeRealActions() {
        val song = Song("1", Uri.parse("content://test/1"), "Órbita", "Érika", "Noches", 185000,
            "Ambient", "2026", 1, "orbita.flac")
        var toggles = 0
        var next = 0
        var repeat = 0
        val state = PlaybackState(connected = true, currentSong = song, duration = song.duration,
            position = 62000, nextSong = song, queue = listOf(song), currentIndex = 0, repeatMode = RepeatMode.ALL)
        compose.setContent { EkoTheme { Surface { PlayerScreen(state, {}, { toggles++ }, {}, { next++ }, {}, {},
            { repeat++ }, {}, {}, true) } } }
        compose.onNodeWithContentDescription("Reproducir").performClick()
        compose.onNodeWithContentDescription("Siguiente").performClick()
        compose.onNodeWithContentDescription("Activar repetir canción").performClick()
        assertEquals(1, toggles)
        assertEquals(1, next)
        assertEquals(1, repeat)
        compose.onNodeWithText("Órbita").assertIsDisplayed()
        compose.onNodeWithText("1:02").assertIsDisplayed()
        capture("player")
    }

    @Test @Config(qualifiers = "w360dp-h640dp-mdpi")
    fun glassMiniPlayerFloatsAndExposesPlaybackActions() {
        val current = Song("1", Uri.parse("content://test/1"), "Celestial Drift", "Aether Echo",
            "Horizonte", 236000, null, null, null, "celestial.flac")
        val nextSong = Song("2", Uri.parse("content://test/2"), "Órbita", "Aurora",
            "Horizonte", 198000, null, null, null, "orbita.flac")
        val state = PlaybackState(
            connected = true,
            currentSong = current,
            isPlaying = true,
            position = 105000,
            duration = current.duration,
            nextSong = nextSong,
            queue = listOf(current, nextSong),
            currentIndex = 0,
        )
        var opened = 0
        var toggled = 0
        var previous = 0
        var next = 0
        compose.setContent {
            EkoTheme {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(EkoBlue.copy(alpha = .16f), EkoBackground, EkoGreen.copy(alpha = .10f)),
                        ),
                    ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    MiniPlayer(
                        state = state,
                        onOpen = { opened++ },
                        onToggle = { toggled++ },
                        onPrevious = { previous++ },
                        onNext = { next++ },
                        reducedMotion = true,
                    )
                }
            }
        }
        compose.onNodeWithText("Celestial Drift").performClick()
        compose.onNodeWithContentDescription("Pausar").performClick()
        compose.onNodeWithContentDescription("Anterior").performClick()
        compose.onNodeWithContentDescription("Siguiente").performClick()
        assertEquals(1, opened)
        assertEquals(1, toggled)
        assertEquals(1, previous)
        assertEquals(1, next)
        capture("mini-player-glass")
    }

    @Test @Config(qualifiers = "w900dp-h600dp-mdpi")
    fun playerAdaptsToWideScreens() {
        val song = Song("1", Uri.parse("content://test/1"), "Donde empieza el mar", "EKO Test",
            "Horizonte", 242000, null, null, null, "mar.mp3")
        val state = PlaybackState(connected = true, currentSong = song, duration = song.duration,
            queue = listOf(song), currentIndex = 0)
        compose.setContent { EkoTheme { Surface { PlayerScreen(state, {}, {}, {}, {}, {}, {}, {}, {}, {}, true) } } }
        compose.onNodeWithText("Donde empieza el mar").assertIsDisplayed()
        capture("player-wide")
    }

    @Test @Config(qualifiers = "w360dp-h640dp-mdpi")
    fun compactPlayerKeepsPlaybackControlsVisible() {
        val song = Song("1", Uri.parse("content://test/1"), "Amanecer", "EKO Test",
            "Prueba local", 185000, null, null, null, "amanecer.mp3")
        val state = PlaybackState(connected = true, currentSong = song, duration = song.duration,
            position = 20000, nextSong = song, queue = listOf(song), currentIndex = 0)
        compose.setContent { EkoTheme { Surface { PlayerScreen(state, {}, {}, {}, {}, {}, {}, {}, {}, {}, true) } } }
        compose.onNodeWithContentDescription("Reproducir").assertIsDisplayed()
        compose.onNodeWithContentDescription("Siguiente").assertIsDisplayed()
        capture("player-compact")
    }

    @Test fun homeWithoutHistoryInvitesToStartListening() {
        var opened = false
        val songs = listOf(Song("0", Uri.parse("content://test/0"), "Órbita", "Érika", "Noches", 180000, null, null, null, "0.flac"))
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Mi colección"),
            songs = songs, totalSongs = songs.size)
        compose.setContent { EkoTheme { Surface {
            HomeScreen(state, null, true, { _, _ -> }, { opened = true })
        } } }
        compose.onNodeWithText("Aún sin estadísticas").assertIsDisplayed()
        compose.onNodeWithText("Ir a la biblioteca").performScrollTo().performClick()
        assertTrue(opened)
        capture("home-empty")
    }

    @Test fun homeShowsUsageStatsAndRecommendationPlays() {
        val songs = listOf("Órbita", "Amanecer", "Brisa").mapIndexed { i, title ->
            Song("$i", Uri.parse("content://test/$i"), title, "Artista ${i + 1}", "Álbum", 180000L + i, null, null, null, "$i.flac")
        }
        val plays = mapOf("0" to PlayRecord(9, 100), "1" to PlayRecord(4, 50), "2" to PlayRecord(1, 10))
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Mi colección"),
            songs = songs, totalSongs = songs.size, insights = ListeningInsights.build(songs, plays))
        var queue: List<Song>? = null
        var selected: Song? = null
        compose.setContent { EkoTheme { Surface {
            HomeScreen(state, null, true, { q, s -> queue = q; selected = s }, {})
        } } }
        compose.onNodeWithText("ESTADÍSTICAS DE USO").assertIsDisplayed()
        compose.onNodeWithText("LO MÁS ESCUCHADO").assertIsDisplayed()
        compose.onNodeWithText("TOP 10 MÁS USADAS").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Reproducir recomendación").performScrollTo().performClick()
        assertEquals(3, queue?.size)
        assertEquals("Órbita", selected?.title)
        capture("home-insights")
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
