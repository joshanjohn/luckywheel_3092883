package com.griffith.luckywheel.ui.components.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.magicPurple
import com.griffith.luckywheel.ui.theme.neonLime
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ParticleExplosion(
    isTriggered: Boolean,
    onFinish: () -> Unit,
    origin: Offset = Offset(500f, 500f) // Center of screen roughly
) {
    if (!isTriggered) return

    val particles = remember { List(50) { ShootingStarParticle(origin) } }
    val startTime = remember { System.currentTimeMillis() }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isTriggered) {
        while (System.currentTimeMillis() - startTime < 2000) {
            currentTime = System.currentTimeMillis()
            delay(16)
        }
        onFinish()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val elapsed = (currentTime - startTime).toFloat() / 1000f
        
        particles.forEach { particle ->
            val x = particle.origin.x + particle.vx * elapsed
            val y = particle.origin.y + particle.vy * elapsed + 0.5f * 500f * elapsed * elapsed // gravity
            
            val alpha = (1f - elapsed / 2f).coerceIn(0f, 1f)
            
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - elapsed / 2f).coerceIn(0.1f, 1f),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun FireSparkleEffect(
    isSpinning: Boolean,
    origin: Offset
) {
    if (!isSpinning) return

    val particles = remember { mutableStateListOf<SparkParticle>() }
    val infiniteTransition = rememberInfiniteTransition(label = "fire_sparks")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "time"
    )

    LaunchedEffect(time) {
        // Emit new particles periodically
        repeat(5) {
            particles.add(SparkParticle(origin))
        }
        // Remove dead particles
        if (particles.size > 100) {
            particles.removeRange(0, 20)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            particle.update()
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = particle.pos
            )
        }
    }
}

private class SparkParticle(val origin: Offset) {
    var pos by mutableStateOf(origin)
    val vx = (Random.nextFloat() - 0.5f) * 15f
    val vy = (Random.nextFloat() - 0.8f) * 20f
    var alpha by mutableFloatStateOf(1f)
    val size = Random.nextFloat() * 4f + 2f
    val color = when (Random.nextInt(3)) {
        0 -> Color(0xFFFF5722) // Deep Orange
        1 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFFFEB3B) // Yellow
    }

    fun update() {
        pos = Offset(pos.x + vx, pos.y + vy)
        alpha = (alpha - 0.02f).coerceAtLeast(0f)
    }
}

private class ShootingStarParticle(val origin: Offset) {
    val vx = (Random.nextFloat() - 0.5f) * 1000f
    val vy = (Random.nextFloat() - 1f) * 1200f
    val size = Random.nextFloat() * 8f + 4f
    val color = when(Random.nextInt(3)) {
        0 -> magicGreen
        1 -> neonLime
        else -> magicPurple
    }
}
