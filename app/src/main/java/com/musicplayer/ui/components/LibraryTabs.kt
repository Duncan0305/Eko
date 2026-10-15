package com.musicplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.musicplayer.ui.theme.*

enum class LibrarySection(val label: String, val icon: Symbol) {
    Songs("Canciones", Symbol.Music),
    Albums("Álbumes", Symbol.Album),
    Artists("Artistas", Symbol.Artist),
    Genres("Géneros", Symbol.Genre),
    Folders("Carpetas", Symbol.Folder),
}

@Composable
fun LibraryTabs(
    selected: LibrarySection,
    onSelect: (LibrarySection) -> Unit,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
    ) {
        items(LibrarySection.entries, key = { it.name }) { section ->
            val active = section == selected
            val container by animateColorAsState(
                if (active) EkoGreen.copy(alpha = .14f) else EkoElevated.copy(alpha = .78f),
                tween(if (reducedMotion) 0 else EkoMotion.Enter),
                label = "Fondo ${section.label}",
            )
            val content by animateColorAsState(
                if (active) EkoGreen else EkoSecondaryText,
                tween(if (reducedMotion) 0 else EkoMotion.Enter),
                label = "Color ${section.label}",
            )
            Surface(
                modifier = Modifier.selectable(
                    selected = active,
                    role = Role.Tab,
                    onClick = { onSelect(section) },
                ),
                shape = MaterialTheme.shapes.large,
                color = container,
                contentColor = content,
                border = BorderStroke(1.dp, if (active) EkoGreen.copy(alpha = .34f) else EkoOutline.copy(alpha = .8f)),
                tonalElevation = 0.dp,
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EkoIcon(section.icon, Modifier.size(18.dp), section.label, content)
                    Text(section.label, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun LibrarySectionPlaceholder(section: LibrarySection, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 52.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = EkoGreen.copy(alpha = .1f),
            contentColor = EkoGreen,
            shape = MaterialTheme.shapes.extraLarge,
            border = BorderStroke(1.dp, EkoGreen.copy(alpha = .2f)),
        ) {
            EkoIcon(section.icon, Modifier.padding(18.dp).size(30.dp), section.label, EkoGreen)
        }
        Text(section.label, style = MaterialTheme.typography.titleLarge)
        Text(
            "Sección preparada para la siguiente etapa",
            style = MaterialTheme.typography.bodySmall,
            color = EkoSecondaryText,
        )
    }
}
