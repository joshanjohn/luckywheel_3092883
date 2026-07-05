package com.griffith.luckywheel.ui.screens.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private val SLOT_SYMBOLS = listOf("🍒", "🍋", "🍊", "🔔", "💎", "⭐", "77", "JK")

private enum class WinType { JACKPOT, TWO_MATCH, NO_MATCH }

private fun detectWin(results: List<String>): WinType = when {
    results[0] == results[1] && results[1] == results[2] -> WinType.JACKPOT
    results[0] == results[1] || results[1] == results[2] || results[0] == results[2] -> WinType.TWO_MATCH
    else -> WinType.NO_MATCH
}

@Composable
fun SlotMachineScreen(navController: NavHostController, playerId: String?) {
    val coroutineScope = rememberCoroutineScope()

    // ── State ──────────────────────────────────────────────────────────────
    var reelResults by remember { mutableStateOf(listOf("🎰", "🎰", "🎰")) }
    var reelStopped by remember { mutableStateOf(listOf(true, true, true)) }
    var isSpinning by remember { mutableStateOf(false) }
    var winType by remember { mutableStateOf<WinType?>(null) }
    var spinCount by remember { mutableIntStateOf(0) }
    var jackpotCount by remember { mutableIntStateOf(0) }

    // Per-reel scroll offset drives the visual ticker
    val reel0Offset = remember { Animatable(0f) }
    val reel1Offset = remember { Animatable(0f) }
    val reel2Offset = remember { Animatable(0f) }

    // Reel contents (extend the symbol list for seamless scroll feel)
    val reelSymbols = remember { List(3) { (SLOT_SYMBOLS + SLOT_SYMBOLS + SLOT_SYMBOLS + SLOT_SYMBOLS + SLOT_SYMBOLS + SLOT_SYMBOLS).shuffled() } }

    // Win banner scale
    var bannerVisible by remember { mutableStateOf(false) }

    fun spin() {
        if (isSpinning) return
        coroutineScope.launch {
            isSpinning = true
            bannerVisible = false
            winType = null
            reelStopped = listOf(false, false, false)
            spinCount++

            val finalResults = List(3) { SLOT_SYMBOLS[Random.nextInt(SLOT_SYMBOLS.size)] }

            // Animate each reel with staggered stop
            val spinSpec: (Int) -> AnimationSpec<Float> = { delayMs ->
                tween(durationMillis = 600 + delayMs, easing = FastOutSlowInEasing)
            }

            // Rapid ticker for each reel
            launch {
                repeat(20) {
                    reel0Offset.snapTo(reel0Offset.value + 1f)
                    delay(30)
                }
                reel0Offset.animateTo(
                    targetValue = reel0Offset.value + 3f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
                reelResults = listOf(finalResults[0], reelResults[1], reelResults[2])
                reelStopped = listOf(true, false, false)
            }
            delay(300)
            launch {
                repeat(20) {
                    reel1Offset.snapTo(reel1Offset.value + 1f)
                    delay(30)
                }
                reel1Offset.animateTo(
                    targetValue = reel1Offset.value + 3f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
                reelResults = listOf(reelResults[0], finalResults[1], reelResults[2])
                reelStopped = listOf(true, true, false)
            }
            delay(600)
            launch {
                repeat(20) {
                    reel2Offset.snapTo(reel2Offset.value + 1f)
                    delay(30)
                }
                reel2Offset.animateTo(
                    targetValue = reel2Offset.value + 3f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
                reelResults = finalResults
                reelStopped = listOf(true, true, true)
                val result = detectWin(finalResults)
                winType = result
                if (result == WinType.JACKPOT) jackpotCount++
                delay(100)
                bannerVisible = true
                isSpinning = false
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppBar(navController = navController, title = "Slot Machine") },
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3D0000), Color(0xFF1A0000), Color(0xFF080000))))
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── Stats ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SlotStatChip("SPINS", spinCount.toString(), arcadeGold)
                SlotStatChip("JACKPOTS", jackpotCount.toString(), Color(0xFFFF5252))
            }

            // ── Machine cabinet ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF2D0000), Color(0xFF1A0000))),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.horizontalGradient(listOf(arcadeGold, Color(0xFFFF5252), arcadeGold)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title marquee
                    Text(
                        "★  LUCKY SLOTS  ★",
                        fontSize = 16.sp, fontFamily = ArcadeFontFamily,
                        color = arcadeGold, letterSpacing = 2.sp
                    )

                    // Reel window
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        reelResults.forEachIndexed { idx, symbol ->
                            ReelCell(
                                symbol = symbol,
                                stopped = reelStopped[idx],
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Win line indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, arcadeGold.copy(0.6f), Color.Transparent)
                                )
                            )
                    )
                }
            }

            // ── Win banner ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = bannerVisible && winType != null,
                enter = fadeIn() + scaleIn(spring(Spring.DampingRatioLowBouncy))
            ) {
                val (msg, color) = when (winType) {
                    WinType.JACKPOT   -> "🎉  JACKPOT!  🎉" to arcadeGold
                    WinType.TWO_MATCH -> "CLOSE!  Almost there 🤏" to neonLime
                    else              -> "TRY AGAIN  👾" to Color.White.copy(0.6f)
                }
                Text(
                    msg, fontSize = if (winType == WinType.JACKPOT) 28.sp else 18.sp,
                    fontFamily = BungeeFontFamily, color = color, textAlign = TextAlign.Center
                )
            }

            // ── Spin button ───────────────────────────────────────────────
            Button(
                onClick = { spin() },
                enabled = !isSpinning,
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    if (isSpinning) "SPINNING..." else "SPIN!",
                    fontFamily = BungeeFontFamily, fontSize = 20.sp, color = Color.White
                )
            }

            // ── Legend ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("3 of a kind", "JACKPOT", arcadeGold)
                LegendItem("2 of a kind", "CLOSE!", neonLime)
                LegendItem("No match", "TRY AGAIN", Color.White.copy(0.4f))
            }
        }
    }
}

// ── Slot-specific stat chip ────────────────────────────────────────────────────
@Composable
private fun SlotStatChip(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontFamily = BubbleFontFamily, color = accent, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.6f))
    }
}

// ── Reel cell ─────────────────────────────────────────────────────────────────
@Composable
private fun ReelCell(symbol: String, stopped: Boolean, modifier: Modifier) {
    val shimmer = rememberInfiniteTransition(label = "reelShimmer")
    val glowAlpha by shimmer.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A0000), Color(0xFF0D0000))),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (stopped) arcadeGold.copy(0.4f) else Color(0xFFFF5252).copy(glowAlpha),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = if (stopped) 1f else 0.85f
                scaleY = if (stopped) 1f else 0.85f
            }
        )
    }
}

// ── Legend item ───────────────────────────────────────────────────────────────
@Composable
private fun LegendItem(condition: String, result: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(result, fontSize = 11.sp, fontFamily = ArcadeFontFamily, color = color, fontWeight = FontWeight.Bold)
        Text(condition, fontSize = 9.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.4f))
    }
}
