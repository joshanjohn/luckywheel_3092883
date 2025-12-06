package com.griffith.luckywheel.ui.screens.playground.gold_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.HapticFeedbackService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.GoldCountComponent
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.screens.playground.logic.updatePlayerGold
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.math.sqrt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldWheelScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val fireBaseService = remember { FireBaseService() }
    val hapticService = remember { HapticFeedbackService(context) }
    val soundEffectService = remember { SoundEffectService(context) }

    //  Wheel Items Default
    val wheelItems = remember {
        listOf(
            SpinWheelItem("+100", lightGreenColor, SpinActionType.GAIN_GOLD, 100, 0.125f),
            SpinWheelItem("2x GOLD", Color(0xFF062E12), SpinActionType.MULTIPLY_GOLD, 2, 0.125f),
            SpinWheelItem("-200", lightGreenColor, SpinActionType.LOSE_GOLD, 200, 0.125f),
            SpinWheelItem("+500", Color(0xFF062E12), SpinActionType.GAIN_GOLD, 500, 0.125f),
            SpinWheelItem("LOSE ALL", lightGreenColor, SpinActionType.LOSE_GOLD, Int.MAX_VALUE, 0.125f),
            SpinWheelItem("+250", Color(0xFF062E12), SpinActionType.GAIN_GOLD, 250, 0.125f),
            SpinWheelItem("-1000", lightGreenColor, SpinActionType.LOSE_GOLD, 1000, 0.125f),
            SpinWheelItem("3x GOLD", Color(0xFF062E12), SpinActionType.MULTIPLY_GOLD, 3, 0.125f),
        )
    }

    var playerGold by remember { mutableIntStateOf(0) }
    var playerName by remember { mutableStateOf("") }

    // Firebase listener
    DisposableEffect(playerId) {
        if (playerId.isNullOrBlank()) onDispose {}
        else {
            val playerRef = fireBaseService.database.child("players").child(playerId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val player = snapshot.getValue(Player::class.java)
                    player?.let {
                        playerGold = it.gold
                        playerName = it.playerName
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            playerRef.addValueEventListener(listener)
            onDispose { playerRef.removeEventListener(listener) }
        }
    }

    //  Spin Logic variables
    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var showResultDialog by remember { mutableStateOf(false) }
    var lastSpinResult by remember { mutableStateOf<SpinWheelItem?>(null) }
    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }
    var sensorEnabled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var lastSegment by remember { mutableIntStateOf(-1) }

    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees, wheelItems)
        lastSpinResult = resultItem
        playerGold = updatePlayerGold(playerGold, resultItem)
        // Update gold in Firebase (fire-and-forget)
        playerId?.let { id ->
            coroutineScope.launch {
                fireBaseService.updatePlayerGold(id, playerGold)
            }
        }
        showResultDialog = true
        sensorEnabled = false
        // Strong vibration when wheel stops
        hapticService.strongVibration()
        
        // Play appropriate sound based on result
        resultItem?.let { item ->
            when (item.type) {
                SpinActionType.GAIN_GOLD, SpinActionType.MULTIPLY_GOLD -> {
                    soundEffectService.playWinSound()
                }
                SpinActionType.LOSE_GOLD -> {
                    soundEffectService.playLoseSound()
                }
                else -> {}
            }
        }
    }

    //  Shake Detection
    DisposableEffect(sensorEnabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (!sensorEnabled || event == null) return
                val (x, y, z) = event.values
                val magnitude = sqrt(x * x + y * y + z * z) - 9.8f
                if (magnitude > 2f) rotationSpeed += magnitude * 0.5f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    
    // Cleanup haptic service on dispose
    DisposableEffect(Unit) {
        onDispose { 
            hapticService.cancel()
            soundEffectService.release()
        }
    }

    // Rotation logic
    LaunchedEffect(Unit) {
        while (true) {
            if (rotationSpeed > 0f) {
                currentRotationDegrees = (currentRotationDegrees + rotationSpeed) % 360
                rotationSpeed *= 0.99f
                if (rotationSpeed < 0.1f) {
                    rotationSpeed = 0f
                    processResult()
                }
            }
            delay(16)
        }
    }

    // UI
    Scaffold(
        topBar = { AppBar(navController) },
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF033E14), Color(0xFF01150B), Color(0xFF01150B))
                )
            ),
        containerColor = Color.Transparent
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoldCountComponent(playerName, playerGold)
                // Trophy Icon - leaderboard
                Button(
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = { 
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("leaderboard") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Image(
                        painterResource(R.drawable.icon_trophy),
                        contentDescription = "leaderboard icon"
                    )
                }
            }

            // Wheel Display
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(
                    items = wheelItems, 
                    rotationDegrees = currentRotationDegrees,
                    onSegmentChange = { segment ->
                        // Only trigger feedback when wheel is actively spinning
                        if (isSpinning && rotationSpeed > 1f && segment != lastSegment) {
                            lastSegment = segment
                            hapticService.tick()
                        }
                    }
                )
            }

            //  Instruction Text
            AnimatedText(text = if (isSpinning) "Spinning..." else "Hold & Shake \nyour phone to spin!")

            // Spin Button with state management
            var isButtonPressed by remember { mutableStateOf(false) }
            var isButtonEnabled by remember { mutableStateOf(true) }
            var pressStartTime by remember { mutableStateOf(0L) }
            
            // Re-enable button when wheel settles
            LaunchedEffect(rotationSpeed) {
                if (rotationSpeed == 0f && !isButtonPressed) {
                    isButtonEnabled = true
                }
            }
            
            // Handle button press with 5-second timeout
            LaunchedEffect(isButtonPressed) {
                if (isButtonPressed) {
                    sensorEnabled = true
                    
                    // Auto-release after 5 seconds
                    kotlinx.coroutines.delay(5000)
                    if (isButtonPressed) {
                        isButtonPressed = false
                        sensorEnabled = false
                        // Only disable if wheel actually started spinning
                        if (rotationSpeed > 0f) {
                            isButtonEnabled = false
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {},
                    enabled = isButtonEnabled,
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonPressed) Color(0xFF4CAF50) else Color(0xFF2E7D32),
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
                                        if (isButtonEnabled) {
                                            pressStartTime = System.currentTimeMillis()
                                            isButtonPressed = true
                                            tryAwaitRelease()
                                            val pressDuration = System.currentTimeMillis() - pressStartTime
                                            isButtonPressed = false
                                            sensorEnabled = false
                                            
                                            // Only disable button if held for at least 500ms AND wheel started spinning
                                            if (pressDuration >= 500 && rotationSpeed > 0f) {
                                                isButtonEnabled = false
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                !isButtonEnabled -> "Wait..."
                                isButtonPressed -> "Spinning"
                                else -> "Hold to\nSpin"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        //  Result Dialog
        if (showResultDialog) {
            lastSpinResult?.let { result ->
                ResultCard (
                    wheelResult = result,
                    onDismiss = {
                        showResultDialog = false
                        sensorEnabled = false
                    }
                )
            }
        }
    }
}
