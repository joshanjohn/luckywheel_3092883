package com.griffith.luckywheel.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DiceRollerScreen(navController: NavHostController, playerId: String?) {
    val coroutineScope = rememberCoroutineScope()

    // ── State ──────────────────────────────────────────────────────────────
    var diceCount by remember { mutableIntStateOf(2) }
    var diceValues by remember { mutableStateOf(List(diceCount) { 1 }) }
    var isRolling by remember { mutableStateOf(false) }
    val rollHistory = remember { mutableStateListOf<List<Int>>() }

    // Target challenge
    var targetEnabled by remember { mutableStateOf(false) }
    var targetSum by remember { mutableIntStateOf(7) }
    var lastRollWon by remember { mutableStateOf<Boolean?>(null) }

    // Wobble rotation per die
    var wobble by remember { mutableFloatStateOf(0f) }
    val animWobble by animateFloatAsState(
        targetValue = wobble,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "wobble"
    )

    // Keep diceValues in sync with diceCount
    LaunchedEffect(diceCount) {
        diceValues = List(diceCount) { diceValues.getOrElse(it) { 1 } }
    }

    fun roll() {
        if (isRolling) return
        coroutineScope.launch {
            isRolling = true
            lastRollWon = null
            // Rapid random updates to fake tumbling
            repeat(8) {
                diceValues = List(diceCount) { Random.nextInt(1, 7) }
                wobble = if (it % 2 == 0) 15f else -15f
                delay(60)
            }
            // Final result
            val result = List(diceCount) { Random.nextInt(1, 7) }
            diceValues = result
            wobble = 0f
            rollHistory.add(0, result)
            if (rollHistory.size > 5) rollHistory.removeAt(rollHistory.lastIndex)
            if (targetEnabled) lastRollWon = result.sum() == targetSum
            isRolling = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppBar(navController = navController, title = "Dice Roller") },
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1B4A), Color(0xFF060C24), Color(0xFF030612))))
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── Dice count selector ──────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("DICE:", fontFamily = ArcadeFontFamily, fontSize = 14.sp, color = Color.White.copy(0.7f))
                IconButton(
                    onClick = { if (diceCount > 1) diceCount-- },
                    modifier = Modifier.size(36.dp).background(Color.White.copy(0.1f), CircleShape)
                ) { Text("−", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                Text(
                    diceCount.toString(), fontSize = 28.sp,
                    fontFamily = BubbleFontFamily, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { if (diceCount < 6) diceCount++ },
                    modifier = Modifier.size(36.dp).background(Color.White.copy(0.1f), CircleShape)
                ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            }

            // ── Dice display ─────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(diceValues) { value ->
                    Box(modifier = Modifier.graphicsLayer { rotationZ = animWobble }) {
                        DieFace(value = value, sizeDp = if (diceCount <= 3) 80 else 60)
                    }
                }
            }

            // ── Sum display ───────────────────────────────────────────────
            if (diceValues.isNotEmpty()) {
                val sum = diceValues.sum()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL", fontSize = 11.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.5f))
                    Text(
                        sum.toString(), fontSize = 48.sp, fontFamily = BungeeFontFamily,
                        color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold
                    )
                    if (targetEnabled && lastRollWon != null) {
                        Text(
                            text = if (lastRollWon == true) "🎯 EXACT HIT!" else "MISSED  (target $targetSum)",
                            fontSize = 14.sp, fontFamily = ArcadeFontFamily,
                            color = if (lastRollWon == true) neonLime else Color(0xFFFF5252)
                        )
                    }
                }
            }

            // ── Roll button ───────────────────────────────────────────────
            Button(
                onClick = { roll() },
                enabled = !isRolling,
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1565C0),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    if (isRolling) "ROLLING..." else "ROLL",
                    fontFamily = BungeeFontFamily, fontSize = 18.sp, color = Color.White
                )
            }

            // ── Target challenge ──────────────────────────────────────────
            TargetChallengeCard(
                enabled = targetEnabled,
                target = targetSum,
                diceCount = diceCount,
                onToggle = { targetEnabled = !targetEnabled; lastRollWon = null },
                onTargetChange = { targetSum = it }
            )

            // ── Roll history ──────────────────────────────────────────────
            if (rollHistory.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("HISTORY", fontSize = 11.sp, fontFamily = ArcadeFontFamily,
                        color = Color.White.copy(0.4f), modifier = Modifier.fillMaxWidth())
                    rollHistory.forEachIndexed { idx, roll ->
                        val alpha = 1f - idx * 0.15f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(0.04f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(roll.joinToString("  "), fontSize = 12.sp,
                                fontFamily = ArcadeFontFamily, color = Color.White.copy(alpha))
                            Text("= ${roll.sum()}", fontSize = 12.sp,
                                fontFamily = BubbleFontFamily, color = Color(0xFF64B5F6).copy(alpha))
                        }
                    }
                }
            }
        }
    }
}

// ── Die face drawn with Canvas ─────────────────────────────────────────────
@Composable
fun DieFace(value: Int, sizeDp: Int) {
    val pipColor = Color(0xFF1A237E)
    val dieSize = sizeDp.dp
    Canvas(modifier = Modifier.size(dieSize)) {
        val s = size.minDimension
        val r = s * 0.15f
        // Body
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFFF5F5F5), Color(0xFFBDBDBD))),
            size = Size(s, s), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
        )
        drawRoundRect(
            color = Color(0xFF9E9E9E),
            size = Size(s, s), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.03f)
        )
        // Pips
        val pipR = s * 0.08f
        val positions = getPipPositions(value, s)
        positions.forEach { pos ->
            drawCircle(color = pipColor, radius = pipR, center = pos)
        }
    }
}

private fun getPipPositions(value: Int, s: Float): List<Offset> {
    val m = s * 0.22f  // margin from edge
    val c = s / 2f     // center
    return when (value) {
        1 -> listOf(Offset(c, c))
        2 -> listOf(Offset(m, m), Offset(s - m, s - m))
        3 -> listOf(Offset(m, m), Offset(c, c), Offset(s - m, s - m))
        4 -> listOf(Offset(m, m), Offset(s - m, m), Offset(m, s - m), Offset(s - m, s - m))
        5 -> listOf(Offset(m, m), Offset(s - m, m), Offset(c, c), Offset(m, s - m), Offset(s - m, s - m))
        6 -> listOf(Offset(m, m), Offset(s - m, m), Offset(m, c), Offset(s - m, c), Offset(m, s - m), Offset(s - m, s - m))
        else -> emptyList()
    }
}

// ── Target challenge card ──────────────────────────────────────────────────
@Composable
private fun TargetChallengeCard(
    enabled: Boolean,
    target: Int,
    diceCount: Int,
    onToggle: () -> Unit,
    onTargetChange: (Int) -> Unit
) {
    val minSum = diceCount
    val maxSum = diceCount * 6

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B4A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TARGET CHALLENGE", fontSize = 13.sp, fontFamily = ArcadeFontFamily,
                    color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold)
                Switch(
                    checked = enabled, onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF1565C0))
                )
            }
            if (enabled) {
                Text("Hit exactly $target to win!", fontSize = 12.sp,
                    fontFamily = ArcadeFontFamily, color = Color.White.copy(0.7f))
                Slider(
                    value = target.toFloat(),
                    onValueChange = { onTargetChange(it.toInt()) },
                    valueRange = minSum.toFloat()..maxSum.toFloat(),
                    steps = maxSum - minSum - 1,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF64B5F6), activeTrackColor = Color(0xFF1565C0))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$minSum", fontSize = 11.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.4f))
                    Text("$maxSum", fontSize = 11.sp, fontFamily = ArcadeFontFamily, color = Color.White.copy(0.4f))
                }
            }
        }
    }
}
