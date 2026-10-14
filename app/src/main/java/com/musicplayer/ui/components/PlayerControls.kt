package com.musicplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.EkoMotion

@Composable
fun PlayerIconButton(
    symbol: Symbol,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    reducedMotion: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed && enabled && !reducedMotion) 0.92f else 1f,
        tween(if (reducedMotion) 0 else EkoMotion.Press),
        label = "Respuesta de $description",
    )
    Surface(
        onClick = onClick,
        modifier = modifier.size(size).scale(scale),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        enabled = enabled,
        interactionSource = interaction,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EkoIcon(symbol, Modifier.size(iconSize), description)
        }
    }
}
