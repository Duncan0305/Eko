package com.musicplayer.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musicplayer.data.model.MusicFolder
import kotlinx.coroutines.flow.map

private val Context.folderStore by preferencesDataStore(name = "music_folder")

class FolderPreferences(context: Context) {
    private val store = context.applicationContext.folderStore
    private val uriKey = stringPreferencesKey("tree_uri")
    private val nameKey = stringPreferencesKey("tree_name")
    val folder = store.data.map { prefs ->
        prefs[uriKey]?.let { MusicFolder(it, prefs[nameKey] ?: "Mi carpeta") }
    }

    suspend fun save(folder: MusicFolder) {
        store.edit { it[uriKey] = folder.uri; it[nameKey] = folder.name }
    }
}
