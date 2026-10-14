package com.musicplayer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.components.*
import com.musicplayer.ui.theme.*

@Composable
fun WelcomeScreen(selecting: Boolean, onSelectFolder: () -> Unit) {
    val reducedMotion = rememberReducedMotion()
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val wide = maxWidth >= 700.dp
        Column(Modifier.widthIn(max = 1000.dp).fillMaxSize().align(Alignment.TopCenter)
            .verticalScroll(rememberScrollState()).padding(horizontal = if (wide) 48.dp else 28.dp)) {
            BrandHeader()
            if (wide) {
                Row(Modifier.fillMaxWidth().padding(vertical = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                    WelcomeArtwork(reducedMotion, Modifier.weight(1f))
                    WelcomeCopy(selecting, onSelectFolder, Modifier.weight(1f).padding(start = 36.dp))
                }
            } else {
                Spacer(Modifier.height(32.dp))
                WelcomeArtwork(reducedMotion, Modifier.fillMaxWidth())
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
private fun WelcomeArtwork(reducedMotion: Boolean, modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "Órbitas EKO")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(EkoMotion.WelcomeOrbit, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotación orbital"
    )
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(EkoMotion.WelcomePulse, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulso del icono"
    )
    val animatedOrbit = if (reducedMotion) 0f else orbit
    val animatedPulse = if (reducedMotion) 1f else pulse

    Box(modifier.height(224.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(224.dp)) {
            val radius = size.minDimension / 2
            drawCircle(EkoSurface, radius)
            listOf(.98f, .86f, .74f, .62f).forEachIndexed { index, fraction ->
                drawCircle(
                    color = EkoOutline.copy(alpha = 0.9f - index * 0.12f),
                    radius = radius * fraction,
                    style = Stroke(1.dp.toPx())
                )
            }
            drawCircle(EkoBackground, radius * .49f)

            val accent = Brush.sweepGradient(listOf(EkoGreen, EkoTurquoise, EkoBlue, EkoGreen))
            val outerInset = 2.dp.toPx()
            drawArc(
                brush = accent,
                startAngle = -78f + animatedOrbit,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = size.copy(width = size.width - outerInset * 2, height = size.height - outerInset * 2),
                style = Stroke(2.dp.toPx())
            )

            val middleInset = radius * .14f
            drawArc(
                brush = accent,
                startAngle = 108f - animatedOrbit * .62f,
                sweepAngle = 48f,
                useCenter = false,
                topLeft = Offset(middleInset, middleInset),
                size = size.copy(width = size.width - middleInset * 2, height = size.height - middleInset * 2),
                style = Stroke(1.5.dp.toPx())
            )

            val innerInset = radius * .26f
            drawArc(
                color = Color.White.copy(alpha = .48f),
                startAngle = 22f + animatedOrbit * .38f,
                sweepAngle = 30f,
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = size.copy(width = size.width - innerInset * 2, height = size.height - innerInset * 2),
                style = Stroke(1.dp.toPx())
            )
        }
        EkoMark(Modifier.size(104.dp).scale(animatedPulse))
    }
}
