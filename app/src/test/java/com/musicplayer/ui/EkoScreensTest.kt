package com.musicplayer.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.musicplayer.data.model.*
import com.musicplayer.scanner.ScanProgress
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
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, { query = it }, {}, {}, {}, {}, {}, { selected = it }, true) } } }
        compose.onNodeWithText("Canción, artista o álbum").performTextInput("órbita")
        assertEquals("órbita", query)
        compose.onNodeWithText("Órbita").performClick()
        assertEquals("Órbita", selected?.title)
        capture("library")
    }

    @Test fun revokedPermissionOffersRecovery() {
        var selected = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), accessError = true)
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, { selected = true }, {}, {}, {}, {}, true) } } }
        compose.onNodeWithText("Seleccionar carpeta").performScrollTo().performClick()
        assertTrue(selected)
        capture("access-error")
    }

    @Test fun scanCanBeCancelledWithReducedMotion() {
        var cancelled = false
        val state = LibraryUiState(starting = false, folder = MusicFolder("content://test/tree", "Música"), scanning = true,
            progress = ScanProgress(40, 12, 1))
        compose.setContent { EkoTheme { Surface { LibraryScreen(state, {}, {}, {}, {}, { cancelled = true }, {}, {}, true) } } }
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

    private fun capture(name: String) {
        compose.waitForIdle()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
