package com.musicplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.*

enum class MainDestination(val route: String, val label: String, val icon: Symbol) {
    Home("home", "Inicio", Symbol.Home),
    Library("library", "Biblioteca", Symbol.Library),
    Settings("settings", "Ajustes", Symbol.Settings),
}

@Composable
fun EkoBottomNavigation(
    current: MainDestination,
    onSelect: (MainDestination) -> Unit,
    reducedMotion: Boolean,
) {
    Box(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, EkoBackground)))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = EkoElevated.copy(alpha = .94f),
            border = BorderStroke(1.dp, EkoOutline.copy(alpha = .86f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
            ) {
                MainDestination.entries.forEach { destination ->
                    val selected = current == destination
                    val iconScale by animateFloatAsState(
                        targetValue = if (selected && !reducedMotion) 1.08f else 1f,
                        animationSpec = tween(if (reducedMotion) 0 else EkoMotion.Press),
                        label = "Selección ${destination.label}",
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onSelect(destination) },
                        icon = {
                            EkoIcon(
                                destination.icon,
                                modifier = Modifier.size(23.dp).scale(iconScale),
                                description = destination.label,
                            )
                        },
                        label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EkoGreen,
                            selectedTextColor = EkoGreen,
                            indicatorColor = EkoGreen.copy(alpha = .13f),
                            unselectedIconColor = EkoInactive,
                            unselectedTextColor = EkoSecondaryText,
                        ),
                    )
                }
            }
        }
    }
}
