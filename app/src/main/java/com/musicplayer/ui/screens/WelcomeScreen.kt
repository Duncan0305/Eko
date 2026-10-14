package com.musicplayer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*

@Composable
fun WelcomeScreen(selecting: Boolean, onSelectFolder: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val wide = maxWidth >= 700.dp
        Column(Modifier.widthIn(max = 1000.dp).fillMaxSize().align(Alignment.TopCenter)
            .verticalScroll(rememberScrollState()).padding(horizontal = if (wide) 48.dp else 28.dp)) {
            BrandHeader()
            if (wide) {
                Row(Modifier.fillMaxWidth().padding(vertical = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                    WelcomeArtwork(Modifier.weight(1f))
                    WelcomeCopy(selecting, onSelectFolder, Modifier.weight(1f).padding(start = 36.dp))
                }
            } else {
                Spacer(Modifier.height(32.dp))
                WelcomeArtwork(Modifier.fillMaxWidth())
                Spacer(Modifier.height(32.dp))
                WelcomeCopy(selecting, onSelectFolder)
            }
            Spacer(Modifier.height(36.dp))
        }
    }
}

@Composable
private fun WelcomeCopy(selecting: Boolean, onSelectFolder: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("TU COLECCIÓN, EN SU LUGAR", style = MaterialTheme.typography.labelSmall, color = EkoTurquoise)
        Spacer(Modifier.height(14.dp))
        Text("Tu música.\nTu espacio.", style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(18.dp))
        Text("Hay canciones que siempre van contigo. Reúne las tuyas en una biblioteca hecha para ellas.",
            style = MaterialTheme.typography.bodyLarge, color = EkoSecondaryText)
        Spacer(Modifier.height(30.dp))
        Button(onClick = onSelectFolder, enabled = !selecting,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
            EkoIcon(Symbol.Folder)
            Text(if (selecting) "Abriendo tu carpeta…" else "Seleccionar mi música", Modifier.weight(1f).padding(horizontal = 12.dp))
            EkoIcon(Symbol.Arrow)
        }
        Spacer(Modifier.height(18.dp))
        Text("Elige una carpeta de tu dispositivo. EKO encontrará también la música de sus subcarpetas.",
            style = MaterialTheme.typography.bodySmall, color = EkoSecondaryText)
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EkoIcon(Symbol.Check, modifier = Modifier.size(16.dp), tint = EkoGreen)
            Text("Sin conexión. Sin cuentas. Solo tu música.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
                color = EkoSecondaryText)
        }
    }
}

@Composable
private fun WelcomeArtwork(modifier: Modifier) {
    Box(modifier.height(224.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(224.dp)) {
            val radius = size.minDimension / 2
            drawCircle(EkoSurface, radius)
            listOf(.98f,.86f,.74f,.62f).forEach { drawCircle(EkoOutline, radius * it, style = Stroke(1.dp.toPx())) }
            drawCircle(EkoBackground, radius * .49f)
            drawArc(Brush.linearGradient(listOf(EkoGreen,EkoTurquoise,EkoBlue)), -78f, 74f, false,
                topLeft = Offset(2.dp.toPx(), 2.dp.toPx()), size = size.copy(width = size.width - 4.dp.toPx(), height = size.height - 4.dp.toPx()),
                style = Stroke(2.dp.toPx()))
        }
        EkoMark(Modifier.size(104.dp))
    }
}
