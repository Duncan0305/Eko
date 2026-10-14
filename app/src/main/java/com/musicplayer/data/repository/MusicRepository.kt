package com.musicplayer.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.musicplayer.data.local.*
import com.musicplayer.data.model.MusicFolder
import com.musicplayer.scanner.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import java.util.UUID

class MusicRepository(
    private val context: Context,
    private val preferences: FolderPreferences,
    private val dao: MusicDao,
    private val scanner: MusicScanner,
    private val artworkCache: ArtworkCache,
) {
    val folder = preferences.folder
    fun songs(folder: MusicFolder) = dao.observeSongs(folder.uri).map { rows -> rows.map { it.toSong() } }
    suspend fun hasIndex(folder: MusicFolder) = dao.index(folder.uri) != null

    suspend fun prepare() = withContext(Dispatchers.IO) {
        dao.cleanUncommitted()
        artworkCache.prune(dao.artworkPaths().toSet())
    }

    suspend fun canAccess(folder: MusicFolder): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = folder.uri.toUri()
            context.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission } &&
                DocumentFile.fromTreeUri(context, uri)?.let { it.isDirectory && it.canRead() } == true
        } catch (_: Exception) { false }
    }

    suspend fun selectFolder(uri: Uri): MusicFolder = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val alreadyGranted = resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }
        resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            val document = DocumentFile.fromTreeUri(context, uri) ?: throw FolderAccessException()
            if (!document.isDirectory || !document.canRead()) throw FolderAccessException()
            val selected = MusicFolder(uri.toString(), document.name ?: "Mi música")
            preferences.save(selected)
            // Release only old tree grants owned by EKO, after persisting the new reference.
            resolver.persistedUriPermissions.filter { it.uri != uri }.forEach {
                try { resolver.releasePersistableUriPermission(it.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                catch (_: SecurityException) { /* Provider may already have revoked it. */ }
            }
            selected
        } catch (error: Exception) {
            if (!alreadyGranted) try { resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            catch (_: SecurityException) { }
            throw error
        }
    }

    suspend fun scan(folder: MusicFolder, onProgress: (ScanProgress) -> Unit): ScanProgress = withContext(Dispatchers.IO) {
        if (!canAccess(folder)) throw FolderAccessException()
        val generation = UUID.randomUUID().toString()
        val batch = ArrayList<SongEntity>(50)
        try {
            val result = scanner.scan(folder.uri.toUri(), { song ->
                batch.add(SongEntity.from(song, generation))
                if (batch.size >= 50) { dao.insert(batch.toList()); batch.clear() }
            }, onProgress)
            if (batch.isNotEmpty()) dao.insert(batch)
            if (!canAccess(folder)) throw FolderAccessException()
            currentCoroutineContext().ensureActive()
            // Completion is atomic, including an empty scan (which removes deleted tracks).
            withContext(NonCancellable) {
                dao.commit(LibraryIndex(folder.uri, generation, System.currentTimeMillis()))
            }
            result
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                dao.discard(generation)
                artworkCache.prune(dao.artworkPaths().toSet())
            }
        }
    }
}
