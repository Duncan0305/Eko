package com.musicplayer.data.local

import androidx.core.net.toUri
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.musicplayer.data.model.Song
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "songs", primaryKeys = ["generation", "uri"])
data class SongEntity(
    val generation: String, val uri: String, val title: String, val artist: String,
    val album: String, val duration: Long, val genre: String?, val year: String?,
    val trackNumber: Int?, val fileName: String, val lastModified: Long, val artworkPath: String?,
) {
    fun toSong() = Song(uri, uri.toUri(), title, artist, album, duration, genre, year,
        trackNumber, fileName, lastModified, artworkPath)

    companion object {
        fun from(song: Song, generation: String) = SongEntity(generation, song.uri.toString(),
            song.title, song.artist, song.album, song.duration, song.genre, song.year,
            song.trackNumber, song.fileName, song.lastModified, song.artworkPath)
    }
}

@Entity(tableName = "library_index")
data class LibraryIndex(@PrimaryKey val folderUri: String, val generation: String, val updatedAt: Long)

/** Estadística de uso de una canción. La clave es su `uri`, estable entre reescaneos. */
@Entity(tableName = "play_stats")
data class PlayStatEntity(
    @PrimaryKey val uri: String,
    val playCount: Int,
    val lastPlayedAt: Long,
)

@Dao
abstract class MusicDao {
    @Query("SELECT songs.* FROM songs INNER JOIN library_index ON songs.generation = library_index.generation WHERE library_index.folderUri = :folderUri")
    abstract fun observeSongs(folderUri: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM library_index WHERE folderUri = :folderUri")
    abstract suspend fun index(folderUri: String): LibraryIndex?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun setIndex(index: LibraryIndex)

    @Query("DELETE FROM songs WHERE generation NOT IN (SELECT generation FROM library_index)")
    abstract suspend fun cleanUncommitted()

    @Query("DELETE FROM songs WHERE generation = :generation AND generation NOT IN (SELECT generation FROM library_index)")
    abstract suspend fun discard(generation: String)

    @Query("DELETE FROM library_index WHERE folderUri != :folderUri")
    abstract suspend fun removeOtherFolders(folderUri: String)

    @Query("SELECT DISTINCT artworkPath FROM songs WHERE artworkPath IS NOT NULL")
    abstract suspend fun artworkPaths(): List<String>

    @Query("SELECT * FROM play_stats")
    abstract fun observePlayStats(): Flow<List<PlayStatEntity>>

    @Query("SELECT * FROM play_stats WHERE uri = :uri")
    abstract suspend fun playStat(uri: String): PlayStatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun putPlayStat(stat: PlayStatEntity)

    /** Incrementa el contador de reproducciones de una canción de forma serializada. */
    @Transaction
    open suspend fun recordPlay(uri: String, timestamp: Long) {
        val current = playStat(uri)?.playCount ?: 0
        putPlayStat(PlayStatEntity(uri, current + 1, timestamp))
    }

    @Transaction
    open suspend fun commit(index: LibraryIndex) {
        setIndex(index)
        removeOtherFolders(index.folderUri)
        cleanUncommitted()
    }
}

@Database(entities = [SongEntity::class, LibraryIndex::class, PlayStatEntity::class], version = 2, exportSchema = true)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        /** Añade la tabla de estadísticas de uso sin tocar el índice existente. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS play_stats (" +
                        "uri TEXT NOT NULL, playCount INTEGER NOT NULL, lastPlayedAt INTEGER NOT NULL, " +
                        "PRIMARY KEY(uri))",
                )
            }
        }
    }
}
