package com.griffith.goldshake.screens.playground.components


import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedText(text: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = keyframes { 0f at 0 // ms  // Optional
            0.4f at 75 // ms
            0.4f at 225 // ms
            0.6f at 375 // ms  // Optional
            0.8f at 658 // ms  // Optional
            durationMillis = 1000 })
    )

    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = Color.White.copy(alpha = alpha),
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    )
}