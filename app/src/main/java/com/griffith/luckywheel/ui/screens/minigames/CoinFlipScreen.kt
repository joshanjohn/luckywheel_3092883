package com.griffith.luckywheel.ui.screens.minigames

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
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

// Best-of-3 game state
private enum class CoinSide { HEADS, TAILS }
private enum class BestOf3Phase { CHOOSING, FLIPPING, DONE }

@Composable
fun CoinFlipScreen(navController: NavHostController, playerId: String?) {
    val coroutineScope = rememberCoroutineScope()

    // ── State ──────────────────────────────────────────────────────────────
    var coinFace by remember { mutableStateOf(CoinSide.HEADS) }
    var isFlipping by remember { mutableStateOf(false) }
    var flipCount by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var lastSide by remember { mutableStateOf<CoinSide?>(null) }

    // Best-of-3
    var bo3Phase by remember { mutableStateOf(BestOf3Phase.CHOOSING) }
    var bo3Choice by remember { mutableStateOf<CoinSide?>(null) }
    var bo3Round by remember { mutableIntStateOf(0) }
    var bo3Wins by remember { mutableIntStateOf(0) }
    var bo3Losses by remember { mutableIntStateOf(0) }
    var bo3Active by remember { mutableStateOf(false) }

    // ── Coin 3-D flip animation ─────────────────────────────────────────────
    // scaleX: 1 → 0 → 1, face swaps at midpoint (scaleX ≈ 0)
    var scaleX by remember { mutableFloatStateOf(1f) }
    val animatedScaleX by animateFloatAsState(
        targetValue = scaleX,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "coinScaleX"
    )

    fun doFlip() {
        if (isFlipping) return
        coroutineScope.launch {
            isFlipping = true
            // Phase 1: collapse to edge
            scaleX = 0f
            delay(230)
            // Swap face at midpoint
            val result = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
            coinFace = result
            // Phase 2: expand back
            scaleX = 1f
            delay(230)

            // Streak tracking
            if (lastSide == result) currentStreak++ else currentStreak = 1
            if (currentStreak > bestStreak) bestStreak = currentStreak
            lastSide = result
            flipCount++

            // Best-of-3 logic
            if (bo3Active && bo3Phase == BestOf3Phase.FLIPPING) {
                bo3Round++
                if (result == bo3Choice) bo3Wins++ else bo3Losses++
                if (bo3Round >= 3 || bo3Wins >= 2 || bo3Losses >= 2) {
                    bo3Phase = BestOf3Phase.DONE
                }
            }
            isFlipping = false
        }
    }

    // ── Layout ─────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppBar(navController = navController, title = "Coin Flip") },
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3D2200), Color(0xFF1A0E00), Color(0xFF0A0500))))
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── Stats row ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip("Flips", flipCount.toString(), arcadeGold)
                StatChip("Streak", "$currentStreak 🔥", neonLime)
                StatChip("Best", bestStreak.toString(), magicGreen)
            }

            // ── Coin ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer { this.scaleX = animatedScaleX },
                contentAlignment = Alignment.Center
            ) {
                CoinCanvas(face = coinFace, size = 200.dp.value)
            }

            // ── Result label ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = !isFlipping && flipCount > 0,
                enter = fadeIn() + scaleIn()
            ) {
                Text(
                    text = if (coinFace == CoinSide.HEADS) "HEADS" else "TAILS",
                    fontSize = 36.sp,
                    fontFamily = BungeeFontFamily,
                    color = if (coinFace == CoinSide.HEADS) arcadeGold else silverColor,
                    letterSpacing = 2.sp
                )
            }

            // ── Flip button ──────────────────────────────────────────────
            Button(
                onClick = { doFlip() },
                enabled = !isFlipping,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = arcadeGold,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = if (isFlipping) "FLIPPING..." else "FLIP!",
                    fontFamily = BungeeFontFamily,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            // ── Best-of-3 section ────────────────────────────────────────
            BestOf3Card(
                phase = bo3Phase,
                choice = bo3Choice,
                round = bo3Round,
                wins = bo3Wins,
                losses = bo3Losses,
                active = bo3Active,
                onChoose = { side ->
                    bo3Choice = side
                    bo3Phase = BestOf3Phase.FLIPPING
                    bo3Round = 0; bo3Wins = 0; bo3Losses = 0
                    bo3Active = true
                },
                onFlip = { doFlip() },
                onReset = {
                    bo3Phase = BestOf3Phase.CHOOSING
                    bo3Choice = null
                    bo3Active = false
                }
            )
        }
    }
}

// ── Coin Canvas ──────────────────────────────────────────────────────────────
@Composable
private fun CoinCanvas(face: CoinSide, size: Float) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val r = this.size.minDimension / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(arcadeGold.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(cx, cy), radius = r * 1.3f
            ), radius = r * 1.3f, center = Offset(cx, cy)
        )
        // Main face
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFE066), arcadeGold, Color(0xFFA86600)),
                center = Offset(cx, cy), radius = r
            ), radius = r, center = Offset(cx, cy)
        )
        // Inner ring
        drawCircle(
            color = Color(0xFF7A4800).copy(alpha = 0.6f),
            radius = r * 0.85f,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = r * 0.06f)
        )

        // Label
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#5A2D00")
                textSize = r * 0.55f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val label = if (face == CoinSide.HEADS) "H" else "T"
            val bounds = android.graphics.Rect()
            paint.getTextBounds(label, 0, label.length, bounds)
            canvas.nativeCanvas.drawText(label, cx, cy + bounds.height() / 2f, paint)
        }
    }
}

// ── Best-of-3 card ───────────────────────────────────────────────────────────
@Composable
private fun BestOf3Card(
    phase: BestOf3Phase,
    choice: CoinSide?,
    round: Int,
    wins: Int,
    losses: Int,
    active: Boolean,
    onChoose: (CoinSide) -> Unit,
    onFlip: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0F00)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "BEST OF 3 CHALLENGE", fontSize = 13.sp,
                fontFamily = ArcadeFontFamily, color = arcadeGold, fontWeight = FontWeight.Bold
            )
            when (phase) {
                BestOf3Phase.CHOOSING -> {
                    Text("Pick your side:", fontSize = 12.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.7f))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { onChoose(CoinSide.HEADS) },
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.5.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = arcadeGold)) {
                            Text("HEADS", fontFamily = ArcadeFontFamily, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = { onChoose(CoinSide.TAILS) },
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(width = 1.5.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = silverColor)) {
                            Text("TAILS", fontFamily = ArcadeFontFamily, fontSize = 12.sp)
                        }
                    }
                }
                BestOf3Phase.FLIPPING -> {
                    Text("Your pick: ${choice?.name}  |  Round ${round + 1}/3",
                        fontSize = 12.sp, fontFamily = ArcadeFontFamily, color = neonLime)
                    Text("Wins: $wins  •  Losses: $losses",
                        fontSize = 12.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.7f))
                    Button(onClick = onFlip, colors = ButtonDefaults.buttonColors(containerColor = magicGreen),
                        shape = RoundedCornerShape(12.dp)) {
                        Text("FLIP ROUND ${round + 1}", fontFamily = ArcadeFontFamily, fontSize = 11.sp, color = Color.Black)
                    }
                }
                BestOf3Phase.DONE -> {
                    val won = wins > losses
                    Text(if (won) "🏆 YOU WIN THE CHALLENGE!" else "💀 CHALLENGE LOST!",
                        fontSize = 14.sp, fontFamily = BubbleFontFamily,
                        color = if (won) arcadeGold else Color(0xFFFF5252), textAlign = TextAlign.Center)
                    Text("$wins wins — $losses losses", fontSize = 12.sp,
                        fontFamily = ArcadeFontFamily, color = Color.White.copy(0.7f))
                    TextButton(onClick = onReset) {
                        Text("Play Again", fontFamily = ArcadeFontFamily, fontSize = 12.sp, color = neonLime)
                    }
                }
            }
        }
    }
}

// ── Stat chip ────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontFamily = BubbleFontFamily, color = accent, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.6f))
    }
}
