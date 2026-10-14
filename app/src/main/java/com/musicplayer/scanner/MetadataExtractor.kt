package com.musicplayer.scanner

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.musicplayer.data.model.Song
import com.musicplayer.domain.MetadataMapper
import com.musicplayer.domain.RawMetadata
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

fun interface SongMetadataReader {
    suspend fun extract(uri: Uri, fileName: String, modified: Long): Song?
}

class MetadataExtractor(private val context: Context, private val artworks: ArtworkCache) : SongMetadataReader {
    override suspend fun extract(uri: Uri, fileName: String, modified: Long): Song? {
        currentCoroutineContext().ensureActive()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            // Extension/MIME only selects candidates; the platform validates the media itself.
            if (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) != "yes") return null
            fun meta(key: Int) = retriever.extractMetadata(key)
            val clean = MetadataMapper.map(RawMetadata(
                meta(MediaMetadataRetriever.METADATA_KEY_TITLE),
                meta(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                meta(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                meta(MediaMetadataRetriever.METADATA_KEY_DURATION),
                meta(MediaMetadataRetriever.METADATA_KEY_GENRE),
                meta(MediaMetadataRetriever.METADATA_KEY_YEAR),
                meta(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
            ), fileName)
            currentCoroutineContext().ensureActive()
            // A broken cover does not invalidate otherwise readable audio.
            val artwork = try { retriever.embeddedPicture?.let(artworks::store) } catch (_: Exception) { null }
            return Song(uri.toString(), uri, clean.title, clean.artist, clean.album, clean.duration,
                clean.genre, clean.year, clean.trackNumber, fileName, modified, artwork)
        } finally {
            retriever.release()
        }
    }
}
