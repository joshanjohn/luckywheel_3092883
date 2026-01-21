package com.griffith.luckywheel.ui.screens.playground.gold_wheel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.deepForest
import kotlinx.coroutines.delay

@Composable
fun SpinButton(
    isEnabled: Boolean,
    isSpinning: Boolean,
    onSpinStart: () -> Unit,
    onSpinEnd: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableStateOf(0L) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onSpinStart()
            delay(5000)
            if (isPressed) {
                isPressed = false
                onSpinEnd()
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(
            onClick = {},
            enabled = isEnabled,
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPressed) magicGreen else deepForest,
                disabledContainerColor = Color(0xFF616161)
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (isEnabled) {
                                    pressStartTime = System.currentTimeMillis()
                                    isPressed = true
                                    tryAwaitRelease()
                                    val pressDuration = System.currentTimeMillis() - pressStartTime
                                    isPressed = false
                                    onSpinEnd()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        !isEnabled -> "Wait..."
                        isPressed -> "Spinning"
                        else -> "Hold to\nSpin"
                    },
                    color = Color.White,
                    fontFamily = ArcadeFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
