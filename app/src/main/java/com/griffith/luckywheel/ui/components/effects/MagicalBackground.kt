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
 * Full-screen animated background for main app screens.
 * Features floating casino-chip hexagons, card-suit particles, and
 * neon laser shooting stars on a deep navy-black gradient.
 */
@Composable
fun MagicalBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "casino_bg")

    // Subtle pulsing glow intensity
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Time driver for all particle movement
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
                        surfaceDark,
                        Color(0xFF050815),
                        surfaceDark
                    )
                )
            )
    ) {
        val particles = remember { List(50) { NeonParticle() } }
        val shootingStars = remember { List(6) { NeonShootingStar() } }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw ambient floating particles
            particles.forEach { particle ->
                var currentX = (particle.x + particle.vx * time) % 1f
                var currentY = (particle.y + particle.vy * time) % 1f

                if (currentX < 0) currentX += 1f
                if (currentY < 0) currentY += 1f

                val alpha = (particle.baseAlpha * pulse).coerceIn(0f, 1f)
                val center = Offset(currentX * size.width, currentY * size.height)

                when (particle.type) {
                    NeonParticleType.CHIP -> drawCasinoChip(center, particle.size, particle.color, alpha)
                    NeonParticleType.DUST -> drawCircle(
                        color = particle.color.copy(alpha = alpha),
                        radius = particle.size,
                        center = center
                    )
                }
            }

            // Draw neon laser shooting stars with gradient trails
            shootingStars.forEach { star ->
                var currentX = (star.x + star.vx * time * 3f) % 1.2f - 0.1f
                var currentY = (star.y + star.vy * time * 3f) % 1.2f - 0.1f

                if (currentX in 0f..1f && currentY in 0f..1f) {
                    val center = Offset(currentX * size.width, currentY * size.height)
                    val trailLength = 50f
                    val trailStart = Offset(
                        center.x - star.vx * trailLength,
                        center.y - star.vy * trailLength
                    )

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                star.color.copy(alpha = 0.5f * pulse),
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

                    // Bright head of the shooting star
                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f * pulse),
                        radius = 3.5f,
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

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Draws a small hexagonal casino-chip shape at [center]. */
private fun DrawScope.drawCasinoChip(
    center: Offset,
    size: Float,
    color: Color,
    alpha: Float
) {
    val path = Path()
    val sides = 6
    for (i in 0 until sides) {
        val angle = (PI / 3.0 * i - PI / 6.0).toFloat()
        val x = center.x + size * cos(angle)
        val y = center.y + size * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path = path, color = color.copy(alpha = alpha * 0.35f))
    drawPath(
        path = path,
        color = color.copy(alpha = alpha),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
    )
}

// ── Particle types ────────────────────────────────────────────────────────────

private enum class NeonParticleType { DUST, CHIP }

private class NeonParticle {
    val x = Random.nextFloat()
    val y = Random.nextFloat()

    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.08f + 0.04f
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed

    val size = Random.nextFloat() * 5f + 2f
    val baseAlpha = Random.nextFloat() * 0.4f + 0.1f
    val type = if (Random.nextFloat() > 0.75f) NeonParticleType.CHIP else NeonParticleType.DUST

    val color = when (Random.nextInt(3)) {
        0 -> primaryNeon
        1 -> accentGold
        else -> accentMagenta
    }
}

private class NeonShootingStar {
    val x = Random.nextFloat()
    val y = Random.nextFloat()

    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 0.3f + 0.2f
    val vx = cos(angle) * speed
    val vy = sin(angle) * speed

    val color = when (Random.nextInt(3)) {
        0 -> primaryNeon
        1 -> accentMagenta
        else -> accentGold
    }
}
