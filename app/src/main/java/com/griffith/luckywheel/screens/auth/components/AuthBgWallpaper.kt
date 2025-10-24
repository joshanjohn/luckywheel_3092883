package com.griffith.luckywheel.screens.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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


//        Image(
//            painter = painterResource(id = R.drawable.bg),
//            contentDescription = "Background Overlay",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxSize()
//                .alpha(0.1f) // Adjust this for blend strength (0.0–1.0)
//        )
//
//        Image(
//            painter = painterResource(id = R.drawable.bgco),
//            contentDescription = "Background Overlay",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxSize()
//                .alpha(0.2f) // Adjust this for blend strength (0.0–1.0)
//        )


        content()
    }
}