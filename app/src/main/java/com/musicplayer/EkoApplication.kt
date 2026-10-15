package com.musicplayer

import android.app.Application
import androidx.room.Room
import com.musicplayer.data.local.FolderPreferences
import com.musicplayer.data.local.MusicDatabase
import com.musicplayer.data.repository.MusicRepository
import com.musicplayer.scanner.*
import com.musicplayer.playback.PlaybackController

class EkoApplication : Application() {
    val repository by lazy {
        val database = Room.databaseBuilder(this, MusicDatabase::class.java, "eko-library.db")
            .addMigrations(MusicDatabase.MIGRATION_1_2)
            .build()
        val artwork = ArtworkCache(this)
        MusicRepository(this, FolderPreferences(this), database.musicDao(),
            MusicScanner(this, MetadataExtractor(this, artwork)), artwork)
    }

    /** Registra cada canción que empieza a sonar para alimentar las estadísticas de uso. */
    val playbackController by lazy { PlaybackController(this) { uri -> repository.recordPlay(uri) } }
}
