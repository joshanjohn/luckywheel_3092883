package com.griffith.luckywheel.ui.components.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.ui.theme.accentGold
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.primaryNeon
import kotlin.random.Random

/**
 * Burst-style particle explosion used when a winning result is shown.
 * Driven by withFrameMillis — one state update per vsync frame,
 * no recomposition inside the Canvas draw phase.
 */
@Composable
fun ParticleExplosion(
    isTriggered: Boolean,
    onFinish: () -> Unit,
    origin: Offset = Offset(500f, 500f)
) {
    if (!isTriggered) return

    val particles = remember { List(50) { ExplosionParticle(origin) } }
    // elapsed drives the Canvas read — a single Float state means minimal
    // recomposition cost compared to storing a Long timestamp.
    var elapsed by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isTriggered) {
        elapsed = 0f
        val startMs = withFrameMillis { it }
        while (elapsed < 2f) {
            val nowMs = withFrameMillis { it }
            elapsed = ((nowMs - startMs) / 1000f).coerceAtMost(2f)
        }
        onFinish()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val x = particle.origin.x + particle.vx * elapsed
            val y = particle.origin.y + particle.vy * elapsed + 0.5f * 500f * elapsed * elapsed
            val alpha = (1f - elapsed / 2f).coerceIn(0f, 1f)
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * alpha.coerceAtLeast(0.1f),
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
