package com.griffith.luckywheel.ui.screens.playground.gold_wheel.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.griffith.luckywheel.models.data.ResultThemeData
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.theme.*

@Composable
fun ResultCard(
    wheelResult: SpinWheelItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundEffectService = remember { SoundEffectService(context) }
    
    // Cleanup sound service on dispose
    DisposableEffect(Unit) {
        onDispose {
            soundEffectService.release()
        }
    }
    
    val resultTheme = rememberResultTheme(wheelResult)
    val scale = rememberEntranceAnimation()
    val glowAlpha = rememberPulsingGlow()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF000000).copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            GlowEffect(
                color = resultTheme.primaryColor,
                alpha = glowAlpha,
                scale = scale
            )

            GlassCard(
                theme = resultTheme,
                scale = scale
            ) {
                ResultContent(
                    wheelResult = wheelResult,
                    theme = resultTheme,
                    onDismiss = onDismiss,
                    soundEffectService = soundEffectService
                )
            }
        }
    }
}

@Composable
private fun rememberEntranceAnimation(): Float {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    return scale
}

@Composable
private fun rememberPulsingGlow(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    return alpha
}

@Composable
private fun rememberResultTheme(wheelResult: SpinWheelItem): ResultThemeData {
    return remember(wheelResult) {
        when (wheelResult.type) {
            SpinActionType.CUSTOM -> ResultThemeData(
                primaryColor = wheelResult.color,
                title = "WINNER!",
                emoji = "🎯",
                buttonText = "SPIN AGAIN"
            )
            SpinActionType.LOSE_GOLD -> ResultThemeData(
                primaryColor = lightRedColor,
                title = "OH NO!",
                emoji = "💔",
                buttonText = "TRY AGAIN"
            )
            else -> ResultThemeData(
                primaryColor = lightGreenColor,
                title = "NICE SPIN!",
                emoji = "🎉",
                buttonText = "SPIN AGAIN"
            )
        }
    }
}

@Composable
private fun GlowEffect(
    color: Color,
    alpha: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .size(320.dp)
            .scale(scale)
            .blur(60.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha * 0.6f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

@Composable
private fun GlassCard(
    theme: ResultThemeData,
    scale: Float,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .widthIn(max = 380.dp)
            .scale(scale)
    ) {
        GlassBackground(theme)
        content()
    }
}

@Composable
private fun BoxScope.GlassBackground(theme: ResultThemeData) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        theme.primaryColor.copy(alpha = 0.3f),
                        theme.primaryColor.copy(alpha = 0.5f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        theme.primaryColor.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.2f),
                        theme.primaryColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    )
}

@Composable
private fun ResultContent(
    wheelResult: SpinWheelItem,
    theme: ResultThemeData,
    onDismiss: () -> Unit,
    soundEffectService: SoundEffectService
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        EmojiIcon(
            emoji = theme.emoji,
            backgroundColor = theme.primaryColor
        )

        Spacer(Modifier.height(8.dp))

        TitleText(
            text = theme.title,
            color = theme.primaryColor
        )

        ResultDisplay(wheelResult = wheelResult)

        Spacer(Modifier.height(4.dp))

        ActionButton(
            text = theme.buttonText,
            color = theme.primaryColor,
            onClick = {
                soundEffectService.playClickSound()
                onDismiss()
            }
        )
    }
}

@Composable
private fun EmojiIcon(
    emoji: String,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.8f),
                        backgroundColor.copy(alpha = 0.4f)
                    )
                )
            )
            .border(
                width = 3.dp,
                color = backgroundColor.copy(alpha = 0.7f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 64.sp
        )
    }
}

@Composable
private fun TitleText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = BubbleFontFamily,
        color = color,
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp
    )
}

@Composable
private fun ResultDisplay(wheelResult: SpinWheelItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF000000).copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "YOU GOT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp
            )

            Text(
                text = wheelResult.label,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BubbleFontFamily,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 42.sp
            )

            ResultSubtitle(wheelResult)
        }
    }
}

@Composable
private fun ResultSubtitle(wheelResult: SpinWheelItem) {
    val (text, color) = when (wheelResult.type) {
        SpinActionType.GAIN_GOLD -> "+${wheelResult.value} gold" to goldColor
        SpinActionType.MULTIPLY_GOLD -> "${wheelResult.value}x multiplier" to goldColor
        SpinActionType.LOSE_GOLD -> {
            val displayText = if (wheelResult.value == Int.MAX_VALUE) {
                "All gold lost"
            } else {
                "-${wheelResult.value} gold"
            }
            displayText to lightRedColor
        }
        SpinActionType.CUSTOM -> return
    }

    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = color.copy(alpha = 0.9f)
    )
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f),
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = BubbleFontFamily,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}

