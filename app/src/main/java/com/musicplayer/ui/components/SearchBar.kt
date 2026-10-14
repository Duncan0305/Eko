package com.musicplayer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    OutlinedTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(),
        singleLine = true, shape = MaterialTheme.shapes.medium,
        placeholder = { Text("Canción, artista o álbum", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { EkoIcon(Symbol.Search, description = "Buscar en tu música") },
        trailingIcon = if (query.isNotEmpty()) ({ IconButton(onClick = { onQueryChange("") }) { EkoIcon(Symbol.Close, description = "Limpiar búsqueda") } }) else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant))
}
