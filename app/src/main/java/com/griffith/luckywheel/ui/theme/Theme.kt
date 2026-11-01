package com.griffith.luckywheel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Light scheme — Neon Casino light variant ─────────────────────────────────
private val lightScheme = lightColorScheme(
    primary = primaryNeon,
    onPrimary = surfaceDark,
    primaryContainer = primaryNeon.copy(alpha = 0.15f),
    onPrimaryContainer = surfaceDark,
    secondary = accentMagenta,
    onSecondary = Color.White,
    secondaryContainer = accentMagenta.copy(alpha = 0.15f),
    onSecondaryContainer = accentMagenta,
    tertiary = accentGold,
    onTertiary = Color.Black,
    tertiaryContainer = accentGold.copy(alpha = 0.15f),
    onTertiaryContainer = accentGold,
    background = surfaceDark,
    onBackground = textPrimary,
    surface = surfaceCard,
    onSurface = textPrimary,
    surfaceVariant = surfaceElevated,
    onSurfaceVariant = textSecondary,
)

// ── Dark scheme — Primary Neon Casino experience ──────────────────────────────
private val darkScheme = darkColorScheme(
    primary = primaryNeon,
    onPrimary = surfaceDark,
    primaryContainer = primaryNeon.copy(alpha = 0.15f),
    onPrimaryContainer = primaryNeon,
    secondary = accentMagenta,
    onSecondary = Color.White,
    secondaryContainer = accentMagenta.copy(alpha = 0.15f),
    onSecondaryContainer = accentMagenta,
    tertiary = accentGold,
    onTertiary = Color.Black,
    tertiaryContainer = accentGold.copy(alpha = 0.15f),
    onTertiaryContainer = accentGold,
    background = surfaceDark,
    onBackground = textPrimary,
    surface = surfaceCard,
    onSurface = textPrimary,
    surfaceVariant = surfaceElevated,
    onSurfaceVariant = textSecondary,
)

@Composable
fun LuckyWheelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
