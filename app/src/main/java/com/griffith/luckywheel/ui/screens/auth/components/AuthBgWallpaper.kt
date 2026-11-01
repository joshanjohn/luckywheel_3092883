package com.griffith.luckywheel.ui.screens.auth.components

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.ui.theme.accentGold
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.surfaceDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated Neon Casino background for the authentication flow.
 * Higher particle density and faster movement for an immediate WOW effect.
 */
@Composable
fun AuthBgWallpaper(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "auth_casino_bg")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Faster animation for the auth screen's explosive feel
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
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
                        surfaceDark,
                        Color(0xFF060A18),
                        Color(0xFF030610)
                    )
                )
            )
    ) {
        // Higher density for an explosive login screen effect
        val particles = remember { List(70) { AuthNeonParticle() } }
        val shootingStars = remember { List(9) { AuthNeonShootingStar() } }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Ambient floating neon particles
            particles.forEach { particle ->
                var currentX = (particle.x + particle.vx * time) % 1f
                var currentY = (particle.y + particle.vy * time) % 1f

                if (currentX < 0) currentX += 1f
                if (currentY < 0) currentY += 1f

                val alpha = (particle.baseAlpha * pulse).coerceIn(0f, 1f)
                val center = Offset(currentX * size.width, currentY * size.height)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size,
                    center = center
                )

                // Soft radial glow halo around each particle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(particle.color.copy(alpha = alpha * 0.4f), Color.Transparent),
                        center = center,
                        radius = particle.size * 3f
                    ),
                    radius = particle.size * 3f,
                    center = center
                )
            }

            // Fast neon laser shooting stars
            shootingStars.forEach { star ->
                var currentX = (star.x + star.vx * time * 4f) % 1.2f - 0.1f
                var currentY = (star.y + star.vy * time * 4f) % 1.2f - 0.1f

                if (currentX in 0f..1f && currentY in 0f..1f) {
                    val center = Offset(currentX * size.width, currentY * size.height)
                    val trailLength = 65f
                    val trailStart = Offset(
                        center.x - star.vx * trailLength,
                        center.y - star.vy * trailLength
                    )

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                star.color.copy(alpha = 0.4f * pulse),
                                Color.White.copy(alpha = 0.85f * pulse)
                            ),
                            start = trailStart,
                            end = center
                        ),
                        start = trailStart,
                        end = center,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f * pulse),
                        radius = 3f,
                        center = center
                    )
                    drawCircle(
                        color = star.color.copy(alpha = 0.6f * pulse),
                        radius = 5f,
                        center = center
                    )
                }
            }
        }

        content()
    }
}

private class AuthNeonParticle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.15f + 0.05f
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed
    val size = Random.nextFloat() * 3f + 1f
    val baseAlpha = Random.nextFloat() * 0.4f + 0.2f
    val color: Color = when (Random.nextInt(3)) {
        0 -> primaryNeon
        1 -> accentGold
        else -> accentMagenta
    }
}

private class AuthNeonShootingStar {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.4f + 0.3f
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed
    val color: Color = when (Random.nextInt(3)) {
        0 -> primaryNeon
        1 -> accentMagenta
        else -> accentGold
    }
}