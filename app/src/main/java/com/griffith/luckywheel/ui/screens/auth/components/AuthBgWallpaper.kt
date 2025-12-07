package com.griffith.luckywheel.ui.screens.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


// for background wallpaper wrapper
@Composable
fun AuthBgWallpaper(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF033E14),
                        Color(0xFF01150B),
                                Color(0xFF01150B)
                    )
                )
            )
    ) {
        content()
    }
}