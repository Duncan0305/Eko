package com.musicplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.domain.PlayedSong
import com.musicplayer.domain.Recommendation
import com.musicplayer.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Revela una sección sin modificar su tamaño durante la animación. Así las secciones que vienen
 * después conservan su posición y nunca se montan sobre el contenido que está apareciendo.
 */
@Composable
fun Reveal(index: Int, reducedMotion: Boolean, content: @Composable () -> Unit) {
    if (reducedMotion) { content(); return }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(index) {
        delay(index * EkoMotion.SectionStagger.toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(EkoMotion.SectionEnter, easing = FastOutSlowInEasing),
        )
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 14.dp.toPx()
        },
    ) { content() }
}

/** Cuenta hasta [value] con una animación breve; respeta animaciones reducidas. */
@Composable
fun animatedCount(value: Int, reducedMotion: Boolean): Int {
    var target by remember { mutableIntStateOf(0) }
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(if (reducedMotion) 0 else 720, easing = FastOutSlowInEasing),
        label = "Conteo de estadísticas",
    )
    LaunchedEffect(value) { target = value }
    return animated
}

@Composable
fun InsightSectionLabel(text: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
            color = EkoSecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailing != null) Text(trailing, style = MaterialTheme.typography.labelSmall, color = EkoInactive)
    }
}

@Composable
fun StatTile(symbol: Symbol, value: String, label: String, accent: Color,
             modifier: Modifier = Modifier, valueStyle: TextStyle = MaterialTheme.typography.titleLarge) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = EkoElevated.copy(alpha = .74f),
        border = BorderStroke(1.dp, EkoOutline.copy(alpha = .8f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = accent.copy(alpha = .14f), contentColor = accent) {
                Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    EkoIcon(symbol, Modifier.size(18.dp), label, accent)
                }
            }
            Column {
                Text(value, style = valueStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, style = MaterialTheme.typography.labelSmall, color = EkoSecondaryText,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** Tarjeta grande de una canción destacada, con su número de reproducciones. */
@Composable
fun HighlightCard(played: PlayedSong, rank: Int, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.width(152.dp).clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClickLabel = "Reproducir ${played.song.title}", onClick = onClick)
            .padding(bottom = 4.dp),
    ) {
        Box {
            SongArtwork(played.song.artworkPath, Modifier.fillMaxWidth(), size = 152.dp, cornerRadius = 18.dp)
            Surface(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                shape = CircleShape,
                color = EkoBackground.copy(alpha = .72f),
                contentColor = if (rank == 1) EkoGreen else EkoText,
            ) {
                Text(if (rank == 1) "Nº1" else "#$rank",
                    Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(played.song.title, style = MaterialTheme.typography.titleMedium,
            color = if (active) MaterialTheme.colorScheme.primary else EkoText,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(played.song.artist, style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            EkoIcon(Symbol.Play, Modifier.size(11.dp), tint = EkoGreen)
            Spacer(Modifier.width(5.dp))
            Text("${played.playCount}", style = MaterialTheme.typography.labelMedium, color = EkoGreen)
        }
    }
}

/** Fila del ranking: puesto, carátula, título y reproducciones. */
@Composable
fun TopSongRow(rank: Int, played: PlayedSong, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClickLabel = "Reproducir ${played.song.title}", onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$rank", Modifier.width(26.dp), style = MaterialTheme.typography.titleMedium,
            color = if (rank <= 3) EkoGreen else EkoInactive, textAlign = TextAlign.Center)
        SongArtwork(played.song.artworkPath, Modifier.padding(start = 4.dp), size = 48.dp, cornerRadius = 9.dp)
        Column(Modifier.weight(1f).padding(start = 13.dp, end = 8.dp)) {
            Text(played.song.title, style = MaterialTheme.typography.titleMedium,
                color = if (active) MaterialTheme.colorScheme.primary else EkoText,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(played.song.artist, style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Surface(shape = RoundedCornerShape(50), color = EkoGreen.copy(alpha = .12f), contentColor = EkoGreen) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                EkoIcon(Symbol.Play, Modifier.size(10.dp), tint = EkoGreen)
                Spacer(Modifier.width(5.dp))
                Text("${played.playCount}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun InsightsEmptyCard(catalogSize: Int, onOpenLibrary: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(
            Brush.linearGradient(listOf(EkoElevated.copy(alpha = .96f), EkoSurface.copy(alpha = .9f)))),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            border = BorderStroke(1.dp, EkoOutline.copy(alpha = .85f)),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(Modifier.padding(24.dp)) {
                Surface(shape = CircleShape, color = EkoGreen.copy(alpha = .12f), contentColor = EkoGreen) {
                    Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                        EkoIcon(Symbol.Stats, Modifier.size(25.dp), "Estadísticas de uso", EkoGreen)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Aún sin estadísticas", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Reproduce tu música y aquí verás tus canciones más escuchadas, tu artista favorito, un top 10 y recomendaciones hechas para ti.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EkoSecondaryText,
                )
                Spacer(Modifier.height(22.dp))
                Text(
                    "$catalogSize ${if (catalogSize == 1) "canción" else "canciones"} en tu biblioteca",
                    style = MaterialTheme.typography.bodySmall,
                    color = EkoInactive,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onOpenLibrary, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium) {
                    Text("Ir a la biblioteca", Modifier.weight(1f))
                    EkoIcon(Symbol.Arrow, description = null, tint = EkoBackground)
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(
            Brush.linearGradient(listOf(EkoElevated.copy(alpha = .96f), EkoSurface.copy(alpha = .9f)))),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            border = BorderStroke(1.dp, EkoOutline.copy(alpha = .85f)),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Surface(shape = CircleShape, color = EkoTurquoise.copy(alpha = .14f), contentColor = EkoTurquoise) {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            EkoIcon(Symbol.Music, Modifier.size(20.dp), "Recomendación", EkoTurquoise)
                        }
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp, end = 10.dp)) {
                        Text(
                            recommendation.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            recommendation.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EkoSecondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EkoTurquoise.copy(alpha = .1f),
                        contentColor = EkoSecondaryText,
                    ) {
                        Text(
                            "${recommendation.songs.size}",
                            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                recommendation.songs.take(3).forEach { song ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        EkoIcon(Symbol.Music, Modifier.size(14.dp), tint = EkoInactive)
                        Spacer(Modifier.width(10.dp))
                        Text(song.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 110.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium) {
                    EkoIcon(Symbol.Play, Modifier.size(18.dp), description = null, tint = EkoBackground)
                    Spacer(Modifier.width(10.dp))
                    Text("Reproducir recomendación")
                }
            }
        }
    }
}
