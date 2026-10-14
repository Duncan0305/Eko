package com.musicplayer.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.musicplayer.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MusicDatabaseTest {
    private lateinit var database: MusicDatabase
    private lateinit var dao: MusicDao
    @Before fun setup() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), MusicDatabase::class.java).build()
        dao = database.musicDao()
    }
    @After fun teardown() = database.close()
    private fun row(generation: String, uri: String = "song") = SongEntity(generation, uri, "Título", "Artista", "Álbum", 1000, null, null, null, "a.mp3", 0, null)

    @Test fun cancellationKeepsLastCommittedIndex() = runTest {
        dao.insert(listOf(row("old")))
        dao.commit(LibraryIndex("folder", "old", 1))
        dao.insert(listOf(row("new", "other")))
        assertEquals("song", dao.observeSongs("folder").first().single().uri)
        dao.discard("new")
        assertEquals("old", dao.index("folder")?.generation)
        assertEquals(1, dao.observeSongs("folder").first().size)
    }

    @Test fun successfulRefreshRemovesDeletedSongsAndDeduplicatesUris() = runTest {
        dao.insert(listOf(row("old", "deleted")))
        dao.commit(LibraryIndex("folder", "old", 1))
        dao.insert(listOf(row("new"), row("new")))
        dao.commit(LibraryIndex("folder", "new", 2))
        dao.discard("new") // Cancellation just after commit must not destroy the committed data.
        assertEquals(listOf("song"), dao.observeSongs("folder").first().map { it.uri })
        dao.commit(LibraryIndex("folder", "empty", 3))
        assertTrue(dao.observeSongs("folder").first().isEmpty())
        assertNotNull(dao.index("folder"))
    }

    @Test fun abandonedProcessDataIsNeverVisible() = runTest {
        dao.insert(listOf(row("abandoned")))
        dao.cleanUncommitted()
        assertNull(dao.index("folder"))
        assertTrue(dao.observeSongs("folder").first().isEmpty())
    }
}
