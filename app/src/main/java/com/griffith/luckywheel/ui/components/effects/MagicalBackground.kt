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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.deepForest
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.magicPurple
import com.griffith.luckywheel.ui.theme.neonLime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
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
        // Regular particles with random directional movement
        val particles = remember { List(50) { Particle() } }
        
        // Shooting stars - faster moving particles with trails
        val shootingStars = remember { List(5) { ShootingStar() } }
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw regular particles
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
            
            // Draw shooting stars with trails
            shootingStars.forEach { star ->
                // Calculate position - shooting stars move much faster
                var currentX = (star.x + star.vx * time * 3f) % 1.2f - 0.1f
                var currentY = (star.y + star.vy * time * 3f) % 1.2f - 0.1f
                
                // Only draw if on screen
                if (currentX in 0f..1f && currentY in 0f..1f) {
                    val center = Offset(currentX * size.width, currentY * size.height)
                    
                    // Draw trail (line behind the star)
                    val trailLength = 40f
                    val trailStart = Offset(
                        center.x - star.vx * trailLength,
                        center.y - star.vy * trailLength
                    )
                    
                    // Gradient trail
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                star.color.copy(alpha = 0.6f * pulse),
                                star.color.copy(alpha = 0.9f * pulse)
                            ),
                            start = trailStart,
                            end = center
                        ),
                        start = trailStart,
                        end = center,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    
                    // Draw the star head (bright point)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f * pulse),
                        radius = 4f,
                        center = center
                    )
                    drawCircle(
                        color = star.color.copy(alpha = 0.7f * pulse),
                        radius = 6f,
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
    // Random starting position
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    
    // Random direction and speed - using angle for true random direction
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.1f + 0.05f
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed
    
    val size = Random.nextFloat() * 4f + 2f
    val baseAlpha = Random.nextFloat() * 0.5f + 0.1f
    val type = if (Random.nextFloat() > 0.8f) ParticleType.COIN else ParticleType.DUST
    val color = when (Random.nextInt(3)) {
        0 -> magicGreen
        1 -> neonLime
        else -> magicPurple
    }
}

private class ShootingStar {
    // Start from random edge
    val x = if (Random.nextBoolean()) Random.nextFloat() else if (Random.nextBoolean()) 0f else 1f
    val y = if (Random.nextBoolean()) Random.nextFloat() else if (Random.nextBoolean()) 0f else 1f
    
    // Diagonal movement - shooting across the screen
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.3f + 0.2f // Much faster than regular particles
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed
    
    val color = when (Random.nextInt(4)) {
        0 -> arcadeGold
        1 -> Color.White
        2 -> neonLime
        else -> Color.Cyan
    }
}
