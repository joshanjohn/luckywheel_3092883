package com.griffith.luckywheel.ui.screens.playground.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.lightGreenColor
import com.griffith.luckywheel.ui.theme.MeriendaFontFamily

@Composable
fun AnimatedText(
    text: String,
    baseColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_animation")

    // Smooth glowing pulse effect
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
    )

    // Optional color shimmer between gold tones
    val glowColor by infiniteTransition.animateColor(
        initialValue = baseColor,
        targetValue = lightGreenColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
    )

    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = glowColor.copy(alpha = alpha),
        fontWeight = FontWeight.Bold,
        fontFamily = MeriendaFontFamily,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
}
