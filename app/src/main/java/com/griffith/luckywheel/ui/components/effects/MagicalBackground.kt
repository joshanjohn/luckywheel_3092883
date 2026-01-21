package com.griffith.luckywheel.ui.components.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.deepForest
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.magicPurple
import com.griffith.luckywheel.ui.theme.neonLime
import kotlin.random.Random

@Composable
fun MagicalBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "magical_bg")
    
    // Pulse effect for alpha/glow
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Time-driven animation for particle movement
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        deepForest,
                        Color(0xFF031003),
                        deepForest
                    )
                )
            )
    ) {
        // Star/Dust particles with movement
        val particles = remember { List(60) { Particle() } }
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                // Calculate current position based on time and velocity
                var currentX = (particle.x + particle.vx * time) % 1f
                var currentY = (particle.y + particle.vy * time) % 1f
                
                if (currentX < 0) currentX += 1f
                if (currentY < 0) currentY += 1f

                val alpha = (particle.baseAlpha * pulse).coerceIn(0f, 1f)
                val center = Offset(currentX * size.width, currentY * size.height)

                if (particle.type == ParticleType.COIN) {
                    // Draw Gold Coin
                    drawCircle(
                        color = arcadeGold.copy(alpha = alpha),
                        radius = particle.size,
                        center = center
                    )
                    // Draw Star highlight on coin
                    val starSize = particle.size * 0.4f
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = Offset(center.x - starSize, center.y),
                        end = Offset(center.x + starSize, center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = Offset(center.x, center.y - starSize),
                        end = Offset(center.x, center.y + starSize),
                        strokeWidth = 1.dp.toPx()
                    )
                } else {
                    // Draw Magic Dust (normal circle)
                    drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size,
                        center = center
                    )
                }
            }
        }
        
        content()
    }
}

private enum class ParticleType { DUST, COIN }

private class Particle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val vx = (Random.nextFloat() - 0.5f) * 0.15f
    val vy = (Random.nextFloat() - 0.5f) * 0.15f
    val size = Random.nextFloat() * 4f + 2f
    val baseAlpha = Random.nextFloat() * 0.5f + 0.1f
    val type = if (Random.nextFloat() > 0.8f) ParticleType.COIN else ParticleType.DUST
    val color = when (Random.nextInt(3)) {
        0 -> magicGreen
        1 -> com.griffith.luckywheel.ui.theme.neonLime
        else -> magicPurple
    }
}
