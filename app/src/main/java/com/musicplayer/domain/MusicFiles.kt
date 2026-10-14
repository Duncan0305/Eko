package com.musicplayer.domain

import java.util.Locale

object MusicFiles {
    private val extensions = setOf("mp3", "flac", "wav", "ogg", "oga", "m4a", "aac", "opus")
    fun isCandidate(name: String, mime: String?): Boolean {
        val type = mime?.lowercase(Locale.ROOT)
        if (type?.startsWith("audio/") == true || type == "application/ogg") return true
        if (type != null && type != "application/octet-stream" && type.isNotBlank()) return false
        return name.substringAfterLast('.', "").lowercase(Locale.ROOT) in extensions
    }
}
