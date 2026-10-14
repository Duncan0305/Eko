package com.musicplayer.domain

import org.junit.Assert.*
import org.junit.Test

class MetadataMapperTest {
    @Test fun missingTagsUseReadableDefaults() {
        val metadata = MetadataMapper.map(RawMetadata(), "Mi canción.flac")
        assertEquals("Mi canción.flac", metadata.title)
        assertEquals("Artista desconocido", metadata.artist)
        assertEquals("Álbum desconocido", metadata.album)
        assertEquals(0L, metadata.duration)
        assertNull(metadata.trackNumber)
    }

    @Test fun trimsValuesAndParsesFractionalTrack() {
        val metadata = MetadataMapper.map(RawMetadata(" Amanecer ", " Sol ", " Luz ", "182000", " Rock ", "2024", "03/12"), "a.mp3")
        assertEquals("Amanecer", metadata.title)
        assertEquals("Sol", metadata.artist)
        assertEquals(3, metadata.trackNumber)
        assertEquals(182000L, metadata.duration)
    }

    @Test fun invalidTagsDoNotCrash() {
        val metadata = MetadataMapper.map(RawMetadata(" ", "<unknown>", "", "-10", " ", "", "bad"), "audio.wav")
        assertEquals("audio.wav", metadata.title)
        assertEquals("Artista desconocido", metadata.artist)
        assertEquals(0L, metadata.duration)
        assertNull(metadata.genre)
        assertNull(metadata.year)
        assertNull(metadata.trackNumber)
        assertEquals(0L, MetadataMapper.map(RawMetadata(duration = "overflow"), "a").duration)
    }

    @Test fun durationHandlesHoursAndInvalidValues() {
        assertEquals("0:00", formatDuration(-1))
        assertEquals("3:05", formatDuration(185000))
        assertEquals("1:01:02", formatDuration(3662000))
    }

    @Test fun filtersMimeAndExtensionsBeforeExtraction() {
        for (extension in listOf("MP3", "flac", "wav", "ogg", "m4a", "aac", "opus"))
            assertTrue(MusicFiles.isCandidate("song.$extension", "application/octet-stream"))
        assertTrue(MusicFiles.isCandidate("unnamed", "audio/mpeg"))
        assertTrue(MusicFiles.isCandidate("song.oga", "application/ogg"))
        assertFalse(MusicFiles.isCandidate("cover.mp3", "image/jpeg"))
        assertFalse(MusicFiles.isCandidate("notes.pdf", null))
        assertFalse(MusicFiles.isCandidate("clip.mp4", "video/mp4"))
    }
}
