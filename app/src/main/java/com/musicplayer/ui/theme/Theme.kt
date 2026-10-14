package com.musicplayer.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EkoColors = darkColorScheme(
    primary = EkoGreen, onPrimary = EkoBackground, primaryContainer = EkoSurface, onPrimaryContainer = EkoGreen,
    secondary = EkoTurquoise, onSecondary = EkoBackground,
    tertiary = EkoBlue, onTertiary = EkoBackground,
    background = EkoBackground, onBackground = EkoText,
    surface = EkoBackground, onSurface = EkoText,
    surfaceVariant = EkoElevated, onSurfaceVariant = EkoSecondaryText,
    surfaceContainer = EkoSurface, surfaceContainerHigh = EkoElevated,
    surfaceContainerHighest = EkoElevated, surfaceContainerLow = EkoSurface,
    surfaceContainerLowest = EkoBlack,
    outline = EkoInactive, outlineVariant = EkoOutline,
    error = EkoError, onError = EkoBackground, errorContainer = EkoSurface, onErrorContainer = EkoError,
    inverseSurface = EkoText, inverseOnSurface = EkoBackground, inversePrimary = EkoBackground,
    surfaceTint = EkoGreen,
)

/** Integration point: frontend-design. Brand colors intentionally ignore dynamic wallpaper colors. */
@Composable
fun EkoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EkoColors, typography = EkoTypography,
        shapes = Shapes(extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp), large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(24.dp)),
        content = content)
}
