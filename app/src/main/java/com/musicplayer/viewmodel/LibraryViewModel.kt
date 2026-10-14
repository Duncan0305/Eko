package com.musicplayer.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.model.*
import com.musicplayer.data.repository.MusicRepository
import com.musicplayer.domain.LibraryOrganizer
import com.musicplayer.scanner.FolderAccessException
import com.musicplayer.scanner.ScanProgress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class LibraryUiState(
    val starting: Boolean = true,
    val folder: MusicFolder? = null,
    val songs: List<Song> = emptyList(),
    val totalSongs: Int = 0,
    val query: String = "",
    val sort: SongSort = SongSort.TITLE,
    val scanning: Boolean = false,
    val selecting: Boolean = false,
    val progress: ScanProgress = ScanProgress(),
    val accessError: Boolean = false,
    val message: String? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class LibraryViewModel(private val repository: MusicRepository) : ViewModel() {
    private val status = MutableStateFlow(LibraryUiState())
    private val allSongs = MutableStateFlow<List<Song>>(emptyList())
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SongSort.TITLE)
    private var scanJob: Job? = null
    private var selectJob: Job? = null
    private var accessJob: Job? = null

    private val results = combine(allSongs, query.debounce(180), sort) { songs, text, order ->
        Triple(songs, text, order)
    }.mapLatest { (songs, text, order) ->
        withContext(Dispatchers.Default) { LibraryOrganizer.organize(songs, text, order) }
    }

    val uiState = combine(status, results, query, sort) { state, songs, text, order ->
        state.copy(songs = if (state.accessError) emptyList() else songs,
            query = text, sort = order)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    init {
        viewModelScope.launch {
            try {
                repository.prepare()
                repository.folder.collectLatest { folder ->
                    allSongs.value = emptyList()
                    status.update { it.copy(starting = false, folder = folder, totalSongs = 0, accessError = false) }
                    if (folder != null) {
                        if (!repository.canAccess(folder)) {
                            status.update { it.copy(accessError = true) }
                        } else {
                            if (!repository.hasIndex(folder) && scanJob?.isActive != true && selectJob?.isActive != true) {
                                startScan(folder)
                            }
                        }
                        // Keep observing even after revocation, so regranting the same URI resumes normally.
                        repository.songs(folder).collect { songs ->
                            allSongs.value = songs
                            status.update { it.copy(totalSongs = songs.size) }
                        }
                    }
                }
            } catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) {
                status.update { it.copy(starting = false, message = "No se pudo abrir la biblioteca. Cierra EKO y vuelve a intentarlo.") }
            }
        }
    }

    fun selectFolder(uri: Uri) {
        if (selectJob?.isActive == true) return
        selectJob = viewModelScope.launch {
            status.update { it.copy(selecting = true, message = null) }
            scanJob?.cancelAndJoin()
            try {
                val folder = repository.selectFolder(uri)
                status.update { it.copy(folder = folder, accessError = false) }
                query.value = ""
                startScan(folder)
            } catch (cancel: CancellationException) { throw cancel }
            catch (_: Exception) {
                status.update { it.copy(message = "No se pudo guardar el acceso. Selecciona una carpeta local y concede permiso de lectura.") }
            } finally {
                status.update { it.copy(selecting = false) }
            }
        }
    }

    fun refresh() {
        val folder = status.value.folder ?: return
        if (scanJob?.isActive != true && selectJob?.isActive != true) startScan(folder)
    }

    private fun startScan(folder: MusicFolder) {
        scanJob = viewModelScope.launch {
            status.update { it.copy(scanning = true, progress = ScanProgress(), message = null) }
            try {
                val result = repository.scan(folder) { progress -> status.update { it.copy(progress = progress) } }
                status.update { it.copy(accessError = false, message = if (result.skipped > 0)
                    if (result.skipped == 1) "Biblioteca actualizada. Se omitió un archivo ilegible o no compatible."
                    else "Biblioteca actualizada. Se omitieron ${result.skipped} archivos ilegibles o no compatibles."
                    else "Biblioteca actualizada.") }
            } catch (cancel: CancellationException) {
                status.update { it.copy(message = "Escaneo cancelado. Se conserva el último índice completo.") }
                throw cancel
            } catch (_: FolderAccessException) {
                status.update { it.copy(accessError = true, message = null) }
            } catch (_: Exception) {
                status.update { it.copy(message = "No se pudo completar el escaneo. Tu biblioteca anterior se conserva; vuelve a intentarlo.") }
            } finally {
                status.update { it.copy(scanning = false) }
            }
        }
    }

    fun checkAccess() {
        if (status.value.starting || status.value.selecting || accessJob?.isActive == true) return
        val folder = status.value.folder ?: return
        accessJob = viewModelScope.launch {
            val accessible = repository.canAccess(folder)
            if (status.value.folder == folder && !accessible) {
                scanJob?.cancelAndJoin()
                status.update { it.copy(accessError = true, message = null) }
            }
        }
    }

    fun cancelScan() { scanJob?.cancel() }
    fun search(value: String) { query.value = value }
    fun sort(value: SongSort) { sort.value = value }
    fun dismissMessage() { status.update { it.copy(message = null) } }
    fun pickerUnavailable() { status.update { it.copy(message = "El selector de carpetas no está disponible en este dispositivo.") } }

    companion object {
        fun factory(repository: MusicRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LibraryViewModel(repository) as T
        }
    }
}
