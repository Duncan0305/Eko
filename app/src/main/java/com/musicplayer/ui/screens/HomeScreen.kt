package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicplayer.data.model.Song
import com.musicplayer.domain.ListeningInsights
import com.musicplayer.domain.ListeningStats
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryUiState

@Composable
fun HomeScreen(
    state: LibraryUiState,
    currentSongId: String?,
    reducedMotion: Boolean,
    onPlay: (List<Song>, Song) -> Unit,
    onOpenLibrary: () -> Unit,
    bottomContentPadding: Dp = 32.dp,
) {
    val insights = state.insights
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 840.dp).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = bottomContentPadding),
        ) {
            BrandHeader()
            Spacer(Modifier.height(28.dp))
            Text("INICIO", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
            Spacer(Modifier.height(8.dp))
            Text("Tu música, cerca.", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Vuelve a tu colección local y descubre cómo la escuchas.",
                style = MaterialTheme.typography.bodyLarge,
                color = EkoSecondaryText,
            )
            if (!insights.hasHistory) {
                Spacer(Modifier.height(28.dp))
                Reveal(0, reducedMotion) {
                    InsightsEmptyCard(state.totalSongs, onOpenLibrary)
                }
            } else {
                Spacer(Modifier.height(28.dp))
                Reveal(0, reducedMotion) { StatsSection(insights.stats, reducedMotion) }
                Spacer(Modifier.height(30.dp))
                Reveal(1, reducedMotion) { HighlightsSection(insights, currentSongId, onPlay) }
                insights.recommendation?.let { recommendation ->
                    Spacer(Modifier.height(34.dp))
                    Reveal(2, reducedMotion) {
                        Column {
                            InsightSectionLabel("RECOMENDADO PARA TI")
                            Spacer(Modifier.height(14.dp))
                            RecommendationCard(recommendation, onPlay = {
                                onPlay(recommendation.songs, recommendation.songs.first())
                            })
                        }
                    }
                }
                Spacer(Modifier.height(34.dp))
                Reveal(3, reducedMotion) { TopSection(insights, currentSongId, onPlay) }
            }
        }
    }
}

@Composable
private fun StatsSection(stats: ListeningStats, reducedMotion: Boolean) {
    Column {
        InsightSectionLabel("ESTADÍSTICAS DE USO")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Symbol.Stats, "${animatedCount(stats.totalPlays, reducedMotion)}",
                "REPRODUCCIONES", EkoGreen, Modifier.weight(1f))
            StatTile(Symbol.Music, "${animatedCount(stats.listenedSongs, reducedMotion)}",
                "ESCUCHADAS", EkoTurquoise, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Symbol.Artist, stats.topArtist ?: "—", "ARTISTA TOP", EkoBlue,
                Modifier.weight(1f), valueStyle = MaterialTheme.typography.titleMedium)
            StatTile(Symbol.Genre, stats.topGenre ?: "—", "GÉNERO TOP", EkoGreen,
                Modifier.weight(1f), valueStyle = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun HighlightsSection(insights: ListeningInsights, currentSongId: String?, onPlay: (List<Song>, Song) -> Unit) {
    Column {
        InsightSectionLabel("LO MÁS ESCUCHADO", trailing = "Tus favoritas")
        Spacer(Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            itemsIndexed(insights.highlights, key = { _, played -> played.song.id }) { index, played ->
                HighlightCard(
                    played = played,
                    rank = index + 1,
                    active = played.song.id == currentSongId,
                    onClick = { onPlay(insights.highlights.map { it.song }, played.song) },
                )
            }
        }
    }
}

@Composable
private fun TopSection(insights: ListeningInsights, currentSongId: String?, onPlay: (List<Song>, Song) -> Unit) {
    Column {
        InsightSectionLabel("TOP 10 MÁS USADAS", trailing = "1–${insights.top.size}")
        Spacer(Modifier.height(8.dp))
        val queue = insights.top.map { it.song }
        insights.top.forEachIndexed { index, played ->
            TopSongRow(
                rank = index + 1,
                played = played,
                active = played.song.id == currentSongId,
                onClick = { onPlay(queue, played.song) },
            )
        }
    }
}
