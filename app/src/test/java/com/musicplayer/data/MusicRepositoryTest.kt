package com.musicplayer.data

import android.content.Context
import android.content.Intent
import android.content.pm.ProviderInfo
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.musicplayer.data.local.*
import com.musicplayer.data.model.Song
import com.musicplayer.data.repository.MusicRepository
import com.musicplayer.scanner.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MusicRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val tree = Uri.parse("content://eko.test/tree/root")
    private lateinit var database: MusicDatabase
    private lateinit var preferences: FolderPreferences
    private lateinit var repository: MusicRepository
    private lateinit var provider: TestMusicProvider
    private var stopInsideMetadata = false
    private val enteredMetadata = CompletableDeferred<Unit>()

    @Before fun setup() {
        provider = TestMusicProvider().also {
            it.attachInfo(context, ProviderInfo().apply { authority = "eko.test"; grantUriPermissions = true })
            ShadowContentResolver.registerProviderInternal("eko.test", it)
        }
        context.grantUriPermission(context.packageName, tree, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
        database = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java).build()
        preferences = FolderPreferences(context)
        val artwork = ArtworkCache(context)
        val reader = SongMetadataReader { uri, name, date ->
            if (stopInsideMetadata) { enteredMetadata.complete(Unit); awaitCancellation() }
            if (name == "broken.mp3") null else
                Song(uri.toString(), uri, name, "Artist", "Album", 1000, null, null, null, name, date)
        }
        repository = MusicRepository(context, preferences, database.musicDao(), MusicScanner(context, reader), artwork)
    }
    @After fun teardown() { database.close() }

    @Test fun selectionPersistsAndPermissionRevocationCanBeRecoveredForSameUri() = runTest {
        val folder = repository.selectFolder(tree)
        assertEquals(folder, FolderPreferences(context).folder.first())
        assertTrue(repository.canAccess(folder))
        repository.scan(folder) {}
        context.contentResolver.releasePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        assertFalse(repository.canAccess(folder))
        try { repository.scan(folder) {}; fail("Revoked access must not scan") }
        catch (_: FolderAccessException) { }
        assertEquals(2, repository.songs(folder).first().size)
        repository.selectFolder(tree)
        assertTrue(repository.canAccess(folder))
        assertTrue(repository.hasIndex(folder))
    }

    @Test fun cancelledRefreshKeepsIndexAndNextEmptyScanRemovesDeletedSongs() = runTest {
        val folder = repository.selectFolder(tree)
        repository.scan(folder) {}
        stopInsideMetadata = true
        val job = launch { repository.scan(folder) {} }
        enteredMetadata.await()
        job.cancelAndJoin()
        assertEquals(2, repository.songs(folder).first().size)
        stopInsideMetadata = false
        provider.empty = true
        repository.scan(folder) {}
        assertTrue(repository.songs(folder).first().isEmpty())
        assertTrue(repository.hasIndex(folder))
    }
}
