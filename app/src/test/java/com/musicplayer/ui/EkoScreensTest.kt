package com.musicplayer.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.musicplayer.data.model.*
import com.musicplayer.scanner.ScanProgress
import com.musicplayer.playback.PlaybackState
import com.musicplayer.playback.RepeatMode
import com.musicplayer.ui.screens.*
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
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, { query = it }, {}, {}, {}, {}, {},
            { selected = it }, {}, null, true) } } }
        compose.onNodeWithText("Canción, artista o álbum").performTextInput("órbita")
        assertEquals("órbita", query)
        compose.onNodeWithText("Órbita").performClick()
        assertEquals("Órbita", selected?.title)
        capture("library")
    }

    @Test fun revokedPermissionOffersRecovery() {
        var selected = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), accessError = true)
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, { selected = true }, {}, {}, {}, {}, {}, null, true) } } }
        compose.onNodeWithText("Seleccionar carpeta").performScrollTo().performClick()
        assertTrue(selected)
        capture("access-error")
    }

    @Test fun scanCanBeCancelledWithReducedMotion() {
        var cancelled = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), scanning = true,
            progress = ScanProgress(40, 12, 1))
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, {}, {}, { cancelled = true }, {}, {}, {}, null, true) } } }
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

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
