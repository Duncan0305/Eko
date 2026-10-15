package com.musicplayer.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.musicplayer.data.local.MusicDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprueba que una instalación con la versión 1 de la base de datos se actualiza sin perder el
 * índice de la biblioteca y añadiendo la tabla de estadísticas de uso.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MusicDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "eko-migration-test.db"

    @Before @After fun clean() { context.deleteDatabase(name) }

    @Test fun migrationFromVersionOneKeepsLibraryAndAddsPlayStats() = runTest {
        createVersionOneDatabase()

        val database = Room.databaseBuilder(context, MusicDatabase::class.java, name)
            .addMigrations(MusicDatabase.MIGRATION_1_2)
            .build()
        try {
            val dao = database.musicDao()
            // Abrir la base ejecuta la migración: el índice existente debe sobrevivir.
            assertEquals("gen-1", dao.index("folder")?.generation)
            assertEquals(listOf("content://test/song"), dao.observeSongs("folder").first().map { it.uri })
            // La tabla nueva queda operativa.
            dao.recordPlay("content://test/song", 7)
            assertEquals(1, dao.observePlayStats().first().single().playCount)
        } finally {
            database.close()
        }
    }

    /** Recrea con SQL crudo exactamente el esquema y la identidad que exportó Room para la versión 1. */
    private fun createVersionOneDatabase() {
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        val legacy = SQLiteDatabase.openOrCreateDatabase(file, null)
        try {
            legacy.execSQL("CREATE TABLE IF NOT EXISTS `songs` (`generation` TEXT NOT NULL, `uri` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `album` TEXT NOT NULL, `duration` INTEGER NOT NULL, `genre` TEXT, `year` TEXT, `trackNumber` INTEGER, `fileName` TEXT NOT NULL, `lastModified` INTEGER NOT NULL, `artworkPath` TEXT, PRIMARY KEY(`generation`, `uri`))")
            legacy.execSQL("CREATE TABLE IF NOT EXISTS `library_index` (`folderUri` TEXT NOT NULL, `generation` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`folderUri`))")
            legacy.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            legacy.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2b815d1594069b8ea651043efcb2f6fe')")
            legacy.execSQL("INSERT INTO songs VALUES ('gen-1','content://test/song','Título','Artista','Álbum',1000,NULL,NULL,NULL,'a.mp3',0,NULL)")
            legacy.execSQL("INSERT INTO library_index VALUES ('folder','gen-1',1)")
            legacy.version = 1
        } finally {
            legacy.close()
        }
    }
}
