package com.musicplayer.domain

data class RawMetadata(
    val title: String? = null, val artist: String? = null, val album: String? = null,
    val duration: String? = null, val genre: String? = null, val year: String? = null,
    val trackNumber: String? = null,
)

data class CleanMetadata(
    val title: String, val artist: String, val album: String, val duration: Long,
    val genre: String?, val year: String?, val trackNumber: Int?,
)

object MetadataMapper {
    fun map(raw: RawMetadata, fileName: String) = CleanMetadata(
        title = raw.title.clean() ?: fileName,
        artist = raw.artist.clean() ?: "Artista desconocido",
        album = raw.album.clean() ?: "Álbum desconocido",
        duration = raw.duration?.toLongOrNull()?.coerceAtLeast(0) ?: 0,
        genre = raw.genre.clean(),
        year = raw.year.clean(),
        trackNumber = raw.trackNumber?.substringBefore('/')?.trim()?.toIntOrNull()?.takeIf { it > 0 },
    )

    private fun String?.clean() = this?.trim()?.takeUnless { it.isBlank() || it == "<unknown>" }
}
