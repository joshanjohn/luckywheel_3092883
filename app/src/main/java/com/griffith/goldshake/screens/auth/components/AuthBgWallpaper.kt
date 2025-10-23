package com.griffith.goldshake.screens.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.griffith.goldshake.R

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
                        Color(0xFF6A11CB), // Start (Purple)
                        Color(0xFF2575FC)  // End (Blue)
                    )
                )
            )
    ) {


        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background Overlay",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f) // Adjust this for blend strength (0.0–1.0)
        )

        Image(
            painter = painterResource(id = R.drawable.bgco),
            contentDescription = "Background Overlay",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f) // Adjust this for blend strength (0.0–1.0)
        )


        content()
    }
}