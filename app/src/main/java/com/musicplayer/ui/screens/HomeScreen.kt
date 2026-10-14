package com.musicplayer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*
import com.musicplayer.viewmodel.LibraryUiState

@Composable
fun HomeScreen(state: LibraryUiState, onOpenLibrary: () -> Unit) {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 840.dp).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            BrandHeader()
            Spacer(Modifier.height(28.dp))
            Text("INICIO", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
            Spacer(Modifier.height(8.dp))
            Text("Tu música, cerca.", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Vuelve a tu colección local y encuentra lo que quieres escuchar.",
                style = MaterialTheme.typography.bodyLarge,
                color = EkoSecondaryText,
            )
            Spacer(Modifier.height(28.dp))
            Box(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)
                    .background(Brush.linearGradient(listOf(EkoElevated.copy(alpha = .96f), EkoSurface.copy(alpha = .9f)))),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    border = BorderStroke(1.dp, EkoOutline.copy(alpha = .85f)),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = EkoGreen.copy(alpha = .12f),
                            contentColor = EkoGreen,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                EkoIcon(Symbol.Library, Modifier.size(25.dp), "Biblioteca local", EkoGreen)
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Text("Biblioteca local", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${state.totalSongs} ${if (state.totalSongs == 1) "canción" else "canciones"} · ${state.folder?.name ?: "Sin carpeta"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EkoSecondaryText,
                        )
                        Spacer(Modifier.height(22.dp))
                        Button(
                            onClick = onOpenLibrary,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text("Abrir biblioteca", Modifier.weight(1f))
                            EkoIcon(Symbol.Arrow, description = "Abrir biblioteca")
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
