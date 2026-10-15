package com.musicplayer.domain

import com.musicplayer.data.model.PlayRecord
import com.musicplayer.data.model.Song

/** Una canción de la biblioteca junto a sus reproducciones acumuladas. */
data class PlayedSong(val song: Song, val playCount: Int, val lastPlayedAt: Long)

/** Resumen numérico del consumo musical del usuario. */
data class ListeningStats(
    val totalPlays: Int = 0,
    val listenedSongs: Int = 0,
    val topArtist: String? = null,
    val topGenre: String? = null,
)

/** Playlist sugerida a partir de lo que el usuario más escucha. */
data class Recommendation(val title: String, val reason: String, val songs: List<Song>)

/** Todo lo que la pantalla de Inicio necesita para contar la historia de uso. */
data class ListeningInsights(
    val stats: ListeningStats = ListeningStats(),
    val highlights: List<PlayedSong> = emptyList(),
    val top: List<PlayedSong> = emptyList(),
    val recommendation: Recommendation? = null,
) {
    val hasHistory: Boolean get() = stats.totalPlays > 0

    companion object {
        const val TOP_LIMIT = 10
        const val HIGHLIGHT_LIMIT = 5
        const val RECOMMENDATION_LIMIT = 20

        /**
         * Cruza la biblioteca actual con el historial de reproducciones. Solo cuenta canciones que
         * siguen existiendo, de modo que las estadísticas son coherentes tras reescanear o cambiar de carpeta.
         */
        fun build(
            songs: List<Song>,
            plays: Map<String, PlayRecord>,
            topLimit: Int = TOP_LIMIT,
            recommendationLimit: Int = RECOMMENDATION_LIMIT,
        ): ListeningInsights {
            val played = songs.mapNotNull { song ->
                val record = plays[song.id] ?: return@mapNotNull null
                if (record.playCount <= 0) null else PlayedSong(song, record.playCount, record.lastPlayedAt)
            }
            if (played.isEmpty()) return ListeningInsights()
            val ranked = played.sortedWith(
                compareByDescending<PlayedSong> { it.playCount }
                    .thenByDescending { it.lastPlayedAt }
                    .thenBy { it.song.title.lowercase() },
            )
            val stats = ListeningStats(
                totalPlays = played.sumOf { it.playCount },
                listenedSongs = played.size,
                topArtist = topBy(ranked) { it.song.artist },
                topGenre = topBy(ranked.filter { !it.song.genre.isNullOrBlank() }) { it.song.genre.orEmpty() },
            )
            return ListeningInsights(
                stats = stats,
                highlights = ranked.take(HIGHLIGHT_LIMIT),
                top = ranked.take(topLimit),
                recommendation = recommend(songs, ranked, stats, recommendationLimit),
            )
        }

        /** Agrupa por [key] y devuelve la que acumula más reproducciones; los empates siguen el ranking. */
        private fun topBy(played: List<PlayedSong>, key: (PlayedSong) -> String): String? =
            played.groupBy { key(it) }
                .maxByOrNull { (_, group) -> group.sumOf { it.playCount } }
                ?.key

        private fun recommend(
            songs: List<Song>,
            ranked: List<PlayedSong>,
            stats: ListeningStats,
            limit: Int,
        ): Recommendation? {
            val byArtist = stats.topArtist?.let { artist -> songs.filter { it.artist.equals(artist, true) } }.orEmpty()
            if (byArtist.size >= 2) {
                return Recommendation("Más de ${stats.topArtist}", "Tu artista más escuchado",
                    byArtist.sortedBy { it.title.lowercase() }.take(limit))
            }
            val byGenre = stats.topGenre?.let { genre -> songs.filter { it.genre.equals(genre, true) } }.orEmpty()
            if (byGenre.size >= 2) {
                return Recommendation("Explora ${stats.topGenre}", "Tu género más escuchado",
                    byGenre.sortedBy { it.title.lowercase() }.take(limit))
            }
            val favorites = ranked.map { it.song }
            return if (favorites.size >= 2) Recommendation("Tus favoritas", "Lo que más escuchas", favorites.take(limit))
            else null
        }
    }
}
