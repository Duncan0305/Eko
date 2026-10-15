package com.musicplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicplayer.playback.PlaybackState
import com.musicplayer.ui.theme.*

@Composable
fun MiniPlayer(
    state: PlaybackState,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong ?: return
    val targetProgress = if (state.duration > 0) {
        state.position.toFloat() / state.duration
    } else 0f
    val progress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(if (reducedMotion) 0 else EkoMotion.Enter, easing = LinearEasing),
        label = "Progreso del minirreproductor",
    )

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = EkoElevated.copy(alpha = .86f),
            border = BorderStroke(1.dp, EkoOutline.copy(alpha = .82f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            EkoTurquoise.copy(alpha = .07f),
                            Color.Transparent,
                            EkoBlue.copy(alpha = .045f),
                        ),
                    ),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 66.dp)
                        .padding(start = 9.dp, end = 7.dp, top = 8.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(19.dp))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Abrir reproductor",
                                onClick = onOpen,
                            )
                            .padding(end = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SongArtwork(
                            song.artworkPath,
                            Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)),
                        )
                        Column(Modifier.weight(1f).padding(start = 11.dp)) {
                            Text(
                                song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = EkoText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = EkoSecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    PlayerIconButton(
                        symbol = Symbol.Previous,
                        description = "Anterior",
                        onClick = onPrevious,
                        size = 36.dp,
                        iconSize = 19.dp,
                        contentColor = EkoSecondaryText,
                        enabled = state.position > 0 || state.currentIndex > 0,
                        reducedMotion = reducedMotion,
                    )
                    if (state.isLoading) {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                Modifier.size(23.dp),
                                color = EkoGreen,
                                strokeWidth = 2.dp,
                            )
                        }
                    } else {
                        PlayerIconButton(
                            symbol = if (state.isPlaying) Symbol.Pause else Symbol.Play,
                            description = if (state.isPlaying) "Pausar" else "Reproducir",
                            onClick = onToggle,
                            size = 44.dp,
                            iconSize = 22.dp,
                            containerColor = EkoGreen.copy(alpha = .15f),
                            contentColor = EkoGreen,
                            reducedMotion = reducedMotion,
                        )
                    }
                    PlayerIconButton(
                        symbol = Symbol.Next,
                        description = "Siguiente",
                        onClick = onNext,
                        size = 36.dp,
                        iconSize = 19.dp,
                        contentColor = EkoSecondaryText,
                        enabled = state.nextSong != null,
                        reducedMotion = reducedMotion,
                    )
                }

                MiniWaveformProgress(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                        .height(13.dp),
                )
                Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun MiniWaveformProgress(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val normalizedProgress = progress.coerceIn(0f, 1f)
    val amplitudes = miniWaveform
    Canvas(
        modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(normalizedProgress, 0f..1f)
        },
    ) {
        val gap = 2.dp.toPx()
        val barWidth = ((size.width - gap * (amplitudes.size - 1)) / amplitudes.size)
            .coerceAtLeast(1.dp.toPx())
        amplitudes.forEachIndexed { index, amplitude ->
            val fraction = (index + 1f) / amplitudes.size
            val barHeight = size.height * amplitude
            val color = if (fraction <= normalizedProgress) {
                lerp(EkoGreen, EkoTurquoise, fraction)
            } else {
                EkoInactive.copy(alpha = .34f)
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(index * (barWidth + gap), (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
        }
    }
}

private val miniWaveform = listOf(
    .30f, .46f, .70f, .48f, .35f, .58f, .88f, .62f, .40f, .76f, .54f, .34f,
    .52f, .82f, .96f, .68f, .43f, .60f, .80f, .51f, .32f, .47f, .73f, .91f,
    .64f, .44f, .59f, .84f, .66f, .38f, .50f, .72f, .55f, .31f, .45f, .67f,
)
