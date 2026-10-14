package com.musicplayer.playback

import android.net.Uri
import com.musicplayer.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaybackMediaItemTest {
    @Test
    fun mediaItemConservaLosMetadatosNecesariosParaLaSesion() {
        val original = Song(
            id = "content://music/7",
            uri = Uri.parse("content://music/7"),
            title = "Luz de octubre",
            artist = "EKO Test",
            album = "Horizonte",
            duration = 243_100,
            genre = "Ambient",
            year = "2026",
            trackNumber = 4,
            fileName = "luz.flac",
            lastModified = 1234,
            artworkPath = "/cache/luz.webp",
        )

        assertEquals(original, original.toMediaItem().toSong())
    }

    @Test
    fun mediaItemIncompletoUsaValoresLegibles() {
        val song = androidx.media3.common.MediaItem.Builder()
            .setMediaId("")
            .setUri("content://music/sin-etiquetas.mp3")
            .build()
            .toSong()

        assertEquals("sin-etiquetas.mp3", song.title)
        assertEquals("Artista desconocido", song.artist)
        assertEquals("Álbum desconocido", song.album)
    }
}
