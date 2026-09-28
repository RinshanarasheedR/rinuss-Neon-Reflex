package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF04141E),
    primaryContainer = Color(0xFF0A2E3D),
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = Color(0xFF2E0014),
    secondaryContainer = Color(0xFF4A0826),
    onSecondaryContainer = NeonMagenta,
    tertiary = NeonGold,
    onTertiary = Color(0xFF2E2200),
    tertiaryContainer = Color(0xFF4A3800),
    onTertiaryContainer = NeonGold,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    error = NeonDanger,
    onError = Color.White
)

@Composable
fun NeonReflexTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = Typography,
        content = content
    )
}
