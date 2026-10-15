package com.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.components.*
import com.musicplayer.viewmodel.LibraryUiState

@Composable
fun SettingsScreen(
    state: LibraryUiState,
    onChangeFolder: () -> Unit,
    bottomContentPadding: Dp = 32.dp,
) {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 720.dp).fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = bottomContentPadding),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            BrandHeader()
            Text("AJUSTES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text("Ajustes", style = MaterialTheme.typography.headlineLarge)
            Text("Tu biblioteca", style = MaterialTheme.typography.titleLarge)
            Text("EKO solo accede a la carpeta que seleccionas y a sus subcarpetas. Tus archivos permanecen en su lugar.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FolderHeader(state.folder?.name ?: "Sin carpeta", !state.selecting, onChangeFolder)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Hecho para estar contigo", style = MaterialTheme.typography.titleLarge)
            Text("Tu biblioteca funciona sin conexión. No hay cuentas, servidores ni recopilación de tu música.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("EKO · Versión 0.2.0\nReproductor local · Fase 2", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
