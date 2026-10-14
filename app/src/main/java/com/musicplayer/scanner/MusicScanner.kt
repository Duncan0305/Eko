package com.musicplayer.scanner

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.musicplayer.data.model.Song
import com.musicplayer.domain.MusicFiles
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException

data class ScanProgress(val inspected: Int = 0, val found: Int = 0, val skipped: Int = 0)
class FolderAccessException : IOException("No se puede acceder a la carpeta seleccionada.")

class MusicScanner(private val context: Context, private val extractor: SongMetadataReader) {
    suspend fun scan(
        treeUri: Uri,
        onSong: suspend (Song) -> Unit,
        onProgress: (ScanProgress) -> Unit,
    ): ScanProgress = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: throw FolderAccessException()
        if (!root.isDirectory || !root.canRead()) throw FolderAccessException()
        val folders = ArrayDeque<Uri>().apply { add(root.uri) }
        val visited = HashSet<String>()
        val songs = HashSet<String>()
        var progress = ScanProgress()
        onProgress(progress)
        while (folders.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val folder = folders.removeLast()
            if (!visited.add(folder.toString())) continue
            // Query once per directory, instead of separate Binder calls for each field.
            // DocumentFile resolves/validates the SAF tree; DocumentsContract gives a streaming cursor.
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                DocumentsContract.getDocumentId(folder))
            try {
                val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val cursor = context.contentResolver.query(children, projection, null, null, null)
                    ?: throw IOException("El proveedor no respondió")
                cursor.use {
                    while (it.moveToNext()) {
                        currentCoroutineContext().ensureActive()
                        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, it.getString(0))
                        val name = it.getString(1) ?: "Sin nombre"
                        val mime = it.getString(2)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            folders.add(uri)
                            continue
                        }
                        progress = progress.copy(inspected = progress.inspected + 1)
                        if (MusicFiles.isCandidate(name, mime) && songs.add(uri.toString())) {
                            val song = try { extractor.extract(uri, name, it.getLong(3)) }
                            catch (cancel: CancellationException) { throw cancel }
                            catch (_: Exception) { null }
                            if (song != null) {
                                onSong(song)
                                progress = progress.copy(found = progress.found + 1)
                            } else progress = progress.copy(skipped = progress.skipped + 1)
                        }
                        if (progress.inspected % 10 == 0) onProgress(progress)
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                // A failed directory must never be mistaken for an empty one and erase the index.
                throw FolderAccessException().apply { initCause(error) }
            }
        }
        onProgress(progress)
        progress
    }
}
