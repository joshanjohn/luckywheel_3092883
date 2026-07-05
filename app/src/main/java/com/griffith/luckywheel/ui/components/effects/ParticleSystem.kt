package com.griffith.luckywheel.ui.components.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.ui.theme.accentGold
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.primaryNeon
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Burst-style particle explosion used when a winning result is shown.
 * Emits 50 neon-casino coloured particles that fan out and fade over 2 seconds.
 */
@Composable
fun ParticleExplosion(
    isTriggered: Boolean,
    onFinish: () -> Unit,
    origin: Offset = Offset(500f, 500f)
) {
    if (!isTriggered) return

    val particles = remember { List(50) { ExplosionParticle(origin) } }
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
            val y = particle.origin.y + particle.vy * elapsed + 0.5f * 500f * elapsed * elapsed

            val alpha = (1f - elapsed / 2f).coerceIn(0f, 1f)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (1f - elapsed / 2f).coerceIn(0.1f, 1f),
                center = Offset(x, y)
            )
        }
    }
}

private class ExplosionParticle(val origin: Offset) {
    val vx = (Random.nextFloat() - 0.5f) * 1000f
    val vy = (Random.nextFloat() - 1f) * 1200f
    val size = Random.nextFloat() * 8f + 4f
    val color: Color = when (Random.nextInt(3)) {
        0 -> primaryNeon
        1 -> accentGold
        else -> accentMagenta
    }
}
