package com.griffith.luckywheel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = goldColor,
    onPrimary = Color(0xFF000000),
    primaryContainer = goldColor.copy(alpha = 0.2f),
    onPrimaryContainer = Color(0xFF000000),
    secondary = lightGreenColor,
    onSecondary = Color(0xFF000000),
    secondaryContainer = lightGreenColor.copy(alpha = 0.2f),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = lightRedColor,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = lightRedColor.copy(alpha = 0.2f),
    onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFFF5FAFC),
    onBackground = Color(0xFF171D1E),
    surface = Color(0xFFF5FAFC),
    onSurface = Color(0xFF171D1E),
)

private val darkScheme = darkColorScheme(
    primary = goldColor,
    onPrimary = Color(0xFF000000),
    primaryContainer = goldColor.copy(alpha = 0.2f),
    onPrimaryContainer = goldColor,
    secondary = lightGreenColor,
    onSecondary = Color(0xFF000000),
    secondaryContainer = lightGreenColor.copy(alpha = 0.2f),
    onSecondaryContainer = lightGreenColor,
    tertiary = lightRedColor,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = lightRedColor.copy(alpha = 0.2f),
    onTertiaryContainer = lightRedColor,
    background = Color(0xFF0E1416),
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF0E1416),
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


