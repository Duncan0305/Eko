package com.musicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrandHeader(onSettings: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        EkoMark(Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text("EKO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(Modifier.weight(1f))
        if (onSettings != null) IconButton(onClick = onSettings) { EkoIcon(Symbol.Settings, description = "Ajustes") }
        else Text("TU MÚSICA. CONTIGO.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
