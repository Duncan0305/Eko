package com.musicplayer.scanner

import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import com.musicplayer.data.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import org.robolectric.shadows.ShadowMediaMetadataRetriever
import org.robolectric.shadows.util.DataSource

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MusicScannerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val tree = Uri.parse("content://eko.test/tree/root")
    private lateinit var provider: TestMusicProvider

    @Before fun setup() {
        provider = TestMusicProvider()
        provider.attachInfo(context, ProviderInfo().apply { authority = "eko.test"; grantUriPermissions = true })
        ShadowContentResolver.registerProviderInternal("eko.test", provider)
        context.grantUriPermission(context.packageName, tree, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    }

    private val reader = SongMetadataReader { uri, name, modified ->
        if (name == "broken.mp3") throw IllegalArgumentException("Damaged fixture")
        Song(uri.toString(), uri, name, "Artista desconocido", "Álbum desconocido", 0, null, null, null, name, modified)
    }

    @Test fun recursiveScanSkipsOtherFilesDeduplicatesAndSurvivesCorruption() = runTest {
        val found = mutableListOf<Song>()
        val result = MusicScanner(context, reader).scan(tree, { found.add(it) }, {})
        assertEquals(setOf("first.mp3", "nested.flac"), found.map { it.fileName }.toSet())
        assertEquals(2, result.found)
        assertEquals(1, result.skipped)
    }

    @Test fun emptyFolderCompletesWithoutInventedTracks() = runTest {
        provider.empty = true
        val result = MusicScanner(context, reader).scan(tree, { fail("No songs expected") }, {})
        assertEquals(ScanProgress(), result)
    }

    @Test fun directoryFailureIsNotReportedAsEmptySuccess() = runTest {
        provider.failChildren = true
        try {
            MusicScanner(context, reader).scan(tree, {}, {})
            fail("Expected access failure")
        } catch (_: FolderAccessException) { }
    }

    @Test fun cancellationStopsTraversal() = runTest {
        val started = CompletableDeferred<Unit>()
        val job = launch {
            MusicScanner(context, SongMetadataReader { _, _, _ -> started.complete(Unit); awaitCancellation() })
                .scan(tree, {}, {})
        }
        started.await()
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
    }

    @Test fun platformMetadataIsMappedAndNonAudioRejected() = runTest {
        val uri = DocumentsContract.buildDocumentUriUsingTree(tree, "first")
        val data = DataSource.toDataSource(context, uri)
        ShadowMediaMetadataRetriever.addMetadata(data, MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO, "yes")
        ShadowMediaMetadataRetriever.addMetadata(data, MediaMetadataRetriever.METADATA_KEY_TITLE, "  Amanecer  ")
        ShadowMediaMetadataRetriever.addMetadata(data, MediaMetadataRetriever.METADATA_KEY_DURATION, "123000")
        val extractor = MetadataExtractor(context, ArtworkCache(context))
        val song = extractor.extract(uri, "first.mp3", 123)
        assertEquals("Amanecer", song?.title)
        assertEquals("Artista desconocido", song?.artist)
        assertEquals(123000L, song?.duration)
        assertNull(song?.artworkPath)
        assertNull(extractor.extract(Uri.parse("content://eko.test/document/image"), "fake.mp3", 0))
    }
}

/** Only in test sources. Production always uses the Android user's chosen documents provider. */
class TestMusicProvider : ContentProvider() {
    var empty = false
    var failChildren = false
    override fun onCreate() = true
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val columns = projection ?: arrayOf("document_id", "_display_name", "mime_type", "last_modified")
        val cursor = MatrixCursor(columns)
        fun add(id: String, name: String, mime: String) {
            cursor.addRow(columns.map<String, Any?> { when (it) {
                "document_id" -> id; "_display_name" -> name; "mime_type" -> mime; "last_modified" -> 100L; "flags" -> 0; else -> null
            } }.toTypedArray())
        }
        if (uri.lastPathSegment == "children") {
            if (failChildren) throw SecurityException("Revoked fixture")
            if (empty) return cursor
            if (DocumentsContract.getDocumentId(uri) == "root") {
                add("first", "first.mp3", "audio/mpeg")
                add("first", "first.mp3", "audio/mpeg")
                add("cover", "cover.jpg", "image/jpeg")
                add("broken", "broken.mp3", "audio/mpeg")
                add("sub", "Disco", DocumentsContract.Document.MIME_TYPE_DIR)
            } else {
                add("nested", "nested.flac", "audio/flac")
                add("root", "Cyclic root", DocumentsContract.Document.MIME_TYPE_DIR)
            }
        } else add("root", "Música", DocumentsContract.Document.MIME_TYPE_DIR)
        return cursor
    }
    override fun getType(uri: Uri) = DocumentsContract.Document.MIME_TYPE_DIR
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
}
