package com.griffith.luckywheel.ui.screens.minigames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ── Fortune data ──────────────────────────────────────────────────────────────
private enum class FortuneType { POSITIVE, NEUTRAL, NEGATIVE }
private data class Fortune(val text: String, val type: FortuneType)

private val FORTUNES = listOf(
    Fortune("IT IS CERTAIN", FortuneType.POSITIVE),
    Fortune("IT IS DECIDEDLY SO", FortuneType.POSITIVE),
    Fortune("WITHOUT A DOUBT", FortuneType.POSITIVE),
    Fortune("YES, DEFINITELY", FortuneType.POSITIVE),
    Fortune("YOU MAY RELY ON IT", FortuneType.POSITIVE),
    Fortune("AS I SEE IT, YES", FortuneType.POSITIVE),
    Fortune("MOST LIKELY", FortuneType.POSITIVE),
    Fortune("OUTLOOK GOOD", FortuneType.POSITIVE),
    Fortune("SIGNS POINT TO YES", FortuneType.POSITIVE),
    Fortune("REPLY HAZY, TRY AGAIN", FortuneType.NEUTRAL),
    Fortune("ASK AGAIN LATER", FortuneType.NEUTRAL),
    Fortune("BETTER NOT TELL NOW", FortuneType.NEUTRAL),
    Fortune("CANNOT PREDICT NOW", FortuneType.NEUTRAL),
    Fortune("CONCENTRATE & ASK AGAIN", FortuneType.NEUTRAL),
    Fortune("DON'T COUNT ON IT", FortuneType.NEGATIVE),
    Fortune("MY REPLY IS NO", FortuneType.NEGATIVE),
    Fortune("MY SOURCES SAY NO", FortuneType.NEGATIVE),
    Fortune("OUTLOOK NOT SO GOOD", FortuneType.NEGATIVE),
    Fortune("VERY DOUBTFUL", FortuneType.NEGATIVE),
    Fortune("THE STARS ARE SILENT", FortuneType.NEGATIVE)
)

private fun FortuneType.color() = when (this) {
    FortuneType.POSITIVE -> Color(0xFF69FF8E)
    FortuneType.NEUTRAL  -> Color(0xFFFFD54F)
    FortuneType.NEGATIVE -> Color(0xFFFF6B6B)
}

@Composable
fun MagicBallScreen(navController: NavHostController, playerId: String?) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── State ──────────────────────────────────────────────────────────────
    var question by remember { mutableStateOf("") }
    var currentFortune by remember { mutableStateOf<Fortune?>(null) }
    var isRevealing by remember { mutableStateOf(false) }
    val fortuneHistory = remember { mutableStateListOf<Fortune>() }

    // Fortune alpha animation: 0 → 1 on reveal
    var fortuneTargetAlpha by remember { mutableFloatStateOf(0f) }
    val fortuneAlpha by animateFloatAsState(
        targetValue = fortuneTargetAlpha,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "fortuneAlpha"
    )

    // Ball shake animation
    val infiniteTransition = rememberInfiniteTransition(label = "ballShake")
    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake"
    )
    var shaking by remember { mutableStateOf(false) }

    val coroutineScopeLocal = rememberCoroutineScope()

    fun reveal() {
        if (isRevealing) return
        isRevealing = true
        shaking = true
        fortuneTargetAlpha = 0f
        val fortune = FORTUNES[Random.nextInt(FORTUNES.size)]
        coroutineScopeLocal.launch {
            delay(600)
            currentFortune = fortune
            fortuneTargetAlpha = 1f
            shaking = false
            fortuneHistory.add(0, fortune)
            if (fortuneHistory.size > 3) fortuneHistory.removeAt(fortuneHistory.lastIndex)
            isRevealing = false
        }
    }

    // ── Layout ─────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppBar(navController = navController, title = "Magic 8-Ball") },
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF120820), Color(0xFF06040F), Color(0xFF020108))))
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── Question input ────────────────────────────────────────────
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Ask your question...", fontFamily = ArcadeFontFamily, fontSize = 11.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = magicPurple,
                    unfocusedBorderColor = Color.White.copy(0.2f),
                    focusedLabelColor = magicPurple,
                    cursorColor = magicPurple,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(0.8f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // ── 8-Ball ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = if (shaking) shakeOffset.dp else 0.dp)
                    .clickable { reveal(); keyboardController?.hide() },
                contentAlignment = Alignment.Center
            ) {
                EightBallCanvas(
                    fortune = currentFortune,
                    fortuneAlpha = fortuneAlpha,
                    size = 220f
                )
            }

            // ── Tap hint / fortune text ────────────────────────────────────
            if (currentFortune == null) {
                Text(
                    "TAP THE BALL TO REVEAL YOUR FATE",
                    fontSize = 12.sp, fontFamily = ArcadeFontFamily,
                    color = magicPurple.copy(0.7f), textAlign = TextAlign.Center
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        currentFortune!!.text,
                        fontSize = 20.sp, fontFamily = BubbleFontFamily,
                        color = currentFortune!!.type.color().copy(alpha = fortuneAlpha),
                        textAlign = TextAlign.Center, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    val badge = when (currentFortune!!.type) {
                        FortuneType.POSITIVE -> "✨ POSITIVE"
                        FortuneType.NEUTRAL  -> "🌀 NEUTRAL"
                        FortuneType.NEGATIVE -> "⚠️ NEGATIVE"
                    }
                    Text(badge, fontSize = 11.sp, fontFamily = ArcadeFontFamily,
                        color = currentFortune!!.type.color().copy(alpha = fortuneAlpha * 0.8f))
                }
            }

            // ── Ask Again button ──────────────────────────────────────────
            Button(
                onClick = { reveal(); keyboardController?.hide() },
                enabled = !isRevealing,
                modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = magicPurple, disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    if (isRevealing) "REVEALING..." else "ASK THE ORACLE",
                    fontFamily = ArcadeFontFamily, fontSize = 12.sp, color = Color.White
                )
            }

            // ── Fortune history chips ──────────────────────────────────────
            if (fortuneHistory.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("PAST FORTUNES", fontSize = 10.sp, fontFamily = ArcadeFontFamily,
                        color = Color.White.copy(0.3f))
                    fortuneHistory.forEach { f ->
                        Surface(
                            color = f.type.color().copy(0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                f.text, fontSize = 11.sp, fontFamily = ArcadeFontFamily,
                                color = f.type.color().copy(0.8f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 8-Ball Canvas ─────────────────────────────────────────────────────────────
@Composable
private fun EightBallCanvas(fortune: Fortune?, fortuneAlpha: Float, size: Float) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val r = this.size.minDimension / 2f
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val center = Offset(cx, cy)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                listOf(magicPurple.copy(0.3f), Color.Transparent),
                center = center, radius = r * 1.3f
            ), radius = r * 1.3f, center = center
        )
        // Main black body
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF1A1A2E), Color.Black),
                center = Offset(cx - r * 0.2f, cy - r * 0.2f), radius = r
            ), radius = r, center = center
        )
        // Highlight gloss
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(0.18f), Color.Transparent),
                center = Offset(cx - r * 0.3f, cy - r * 0.35f), radius = r * 0.45f
            ), radius = r * 0.45f, center = Offset(cx - r * 0.3f, cy - r * 0.35f)
        )
        // Inner window (dark-blue circle)
        val windowR = r * 0.44f
        drawCircle(color = Color(0xFF0A0A2E), radius = windowR, center = center)
        drawCircle(color = magicPurple.copy(0.5f), radius = windowR, center = center,
            style = Stroke(width = r * 0.025f))

        // Triangle inside window
        val triR = windowR * 0.72f
        val trianglePath = Path().apply {
            moveTo(cx, cy - triR)
            lineTo(cx + triR * 0.866f, cy + triR * 0.5f)
            lineTo(cx - triR * 0.866f, cy + triR * 0.5f)
            close()
        }
        drawPath(trianglePath, color = Color(0xFF16085C))
        drawPath(trianglePath, color = magicPurple.copy(0.35f), style = Stroke(width = r * 0.02f))

        // "8" label on upper half (when no fortune shown)
        if (fortune == null || fortuneAlpha < 0.3f) {
            drawIntoCanvas { canvas ->
                val p = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = windowR * 0.9f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val b = android.graphics.Rect()
                p.getTextBounds("8", 0, 1, b)
                canvas.nativeCanvas.drawText("8", cx, cy + b.height() / 2f, p)
            }
        }

        // Fortune text inside triangle
        if (fortune != null && fortuneAlpha > 0.05f) {
            drawIntoCanvas { canvas ->
                val words = fortune.text.split(" ")
                val line1 = words.take(words.size / 2 + words.size % 2).joinToString(" ")
                val line2 = words.drop(words.size / 2 + words.size % 2).joinToString(" ")
                val paint = android.graphics.Paint().apply {
                    color = fortune.type.color().copy(fortuneAlpha).run {
                        android.graphics.Color.argb(
                            (alpha * 255).toInt(), (red * 255).toInt(),
                            (green * 255).toInt(), (blue * 255).toInt()
                        )
                    }
                    textSize = windowR * 0.22f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val lineH = paint.textSize * 1.3f
                canvas.nativeCanvas.drawText(line1, cx, cy - lineH / 2f + lineH * 0.1f, paint)
                if (line2.isNotEmpty()) canvas.nativeCanvas.drawText(line2, cx, cy + lineH / 2f + lineH * 0.1f, paint)
            }
        }
    }
}
