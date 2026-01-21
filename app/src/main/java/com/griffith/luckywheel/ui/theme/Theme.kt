package com.griffith.luckywheel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = magicGreen,
    onPrimary = Color.White,
    primaryContainer = neonLime.copy(alpha = 0.2f),
    onPrimaryContainer = deepForest,
    secondary = magicPurple,
    onSecondary = Color.White,
    secondaryContainer = magicPurple.copy(alpha = 0.2f),
    onSecondaryContainer = magicPurple,
    tertiary = arcadeGold,
    onTertiary = Color.Black,
    tertiaryContainer = arcadeGold.copy(alpha = 0.2f),
    onTertiaryContainer = arcadeGold,
    background = Color(0xFF092609), // Deep forest background
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF0E1416),
    onSurface = Color(0xFFDEE3E5),
)

private val darkScheme = darkColorScheme(
    primary = neonLime,
    onPrimary = deepForest,
    primaryContainer = magicGreen.copy(alpha = 0.2f),
    onPrimaryContainer = magicGreen,
    secondary = magicPurple,
    onSecondary = Color.White,
    secondaryContainer = magicPurple.copy(alpha = 0.2f),
    onSecondaryContainer = magicPurple,
    tertiary = arcadeGold,
    onTertiary = Color.Black,
    tertiaryContainer = arcadeGold.copy(alpha = 0.2f),
    onTertiaryContainer = arcadeGold,
    background = Color(0xFF051505),
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF051505),
    onSurface = Color(0xFFDEE3E5),
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


