package com.griffith.luckywheel.ui.screens.playground.gold_wheel.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.adamglin.phosphoricons.PhosphorIcons
import com.adamglin.phosphoricons.regular.Handtap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.deepForest
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.neonLime

@Composable
fun SpinButton(
    isEnabled: Boolean,
    isSpinning: Boolean,
    onSpinStart: () -> Unit,
    onSpinEnd: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Pulse animation for the glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "spin_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Smooth scale animation when pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing ring - Restored to prominent size
        if (isEnabled && !isPressed) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = scale * 1.15f
                        scaleY = scale * 1.15f
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                neonLime.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
        }

        // Main Button Surface - Restored to 90dp
        Surface(
            onClick = { }, 
            enabled = isEnabled,
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(isEnabled) {
                    if (isEnabled) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                onSpinStart()
                                tryAwaitRelease()
                                isPressed = false
                                onSpinEnd()
                            }
                        )
                    }
                }
                .border(
                    width = 3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                ),
            shadowElevation = if (isPressed) 4.dp else 12.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = when {
                                !isEnabled -> listOf(Color(0xFF424242), Color(0xFF212121))
                                isPressed -> listOf(magicGreen, deepForest)
                                else -> listOf(neonLime, magicGreen)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Internal bezel
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .border(1.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hand Tap Icon for hold-to-spin action
                    Icon(
                        imageVector = PhosphorIcons.Regular.Handtap,
                        contentDescription = null,
                        tint = if (isEnabled) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp).graphicsLayer {
                            // Subtle pulse for the icon
                            alpha = if (isPressed) 1f else 0.8f
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when {
                            !isEnabled -> "WAIT"
                            isPressed -> "RELEASE"
                            else -> "HOLD TO\nSPIN"
                        },
                        color = if (isEnabled) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontFamily = ArcadeFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
