package com.griffith.luckywheel.ui.screens.playground.custom_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.data.toSpinWheelItems
import com.griffith.luckywheel.services.HapticFeedbackService
import com.griffith.luckywheel.services.SoundEffectService

import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.custom_wheel.components.EditBottomSheet
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.neonLime
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWheelScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val hapticService = remember { HapticFeedbackService(context) }
    val soundEffectService = remember { SoundEffectService(context) }

    val loadedGame = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<SavedGame>("loaded_game")

    var currentGameId by remember { mutableStateOf<String?>(null) }
    var currentGameName by remember { mutableStateOf<String?>(null) }

    val wheelItems = remember {
        mutableStateListOf(
            SpinWheelItem("Alice", Color(0xFF4CAF50), SpinActionType.CUSTOM, 0, 0.5f),
            SpinWheelItem("Bob", Color(0xFFFFC107), SpinActionType.CUSTOM, 0, 0.5f)
        )
    }

    LaunchedEffect(loadedGame) {
        loadedGame?.let { game ->
            currentGameId = game.gameId
            currentGameName = game.gameName
            wheelItems.clear()
            wheelItems.addAll(game.toSpinWheelItems())
            navController.currentBackStackEntry?.savedStateHandle?.remove<SavedGame>("loaded_game")
        }
    }

    val totalPercent = wheelItems.sumOf { it.percent.toDouble() }.toFloat()
    val wheelItemsWithAngles = if (totalPercent == 0f) {
        val equalFraction = 1f / wheelItems.size.coerceAtLeast(1)
        wheelItems.map { it.copy(percent = equalFraction) }
    } else {
        wheelItems.map { it.copy(percent = it.percent / totalPercent) }
    }

    val latestWheelItems = rememberUpdatedState(wheelItemsWithAngles)

    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var sensorEnabled by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var chosenItem by remember { mutableStateOf<SpinWheelItem?>(null) }
    var lastSegment by remember { mutableIntStateOf(-1) }
    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }

    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees, latestWheelItems.value)
        chosenItem = resultItem
        showResultDialog = true
        sensorEnabled = false
        hapticService.strongVibration()
    }

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
    
    DisposableEffect(Unit) {
        onDispose { 
            hapticService.cancel()
            soundEffectService.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (rotationSpeed > 0f) {
                currentRotationDegrees = (currentRotationDegrees + rotationSpeed) % 360f
                rotationSpeed *= 0.98f
                if (rotationSpeed < 0.1f) {
                    rotationSpeed = 0f
                    processResult()
                }
            }
            delay(16)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalGameFAB(
                iconId = R.drawable.icon_trophy,
                label = "GOLD ARCADE",
                onClick = { 
                    navController.navigate("goldwheel/$playerId") {
                        popUpTo("home/$playerId") { inclusive = false }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Polished Home Button
                IconButton(
                    onClick = { navController.navigate("home/$playerId") },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = arcadeGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Modern Edit Wheel Button
                    Surface(
                        onClick = {
                            soundEffectService.playClickSound()
                            showBottomSheet = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = magicGreen.copy(alpha = 0.9f),
                        modifier = Modifier
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Edit Wheel",
                                color = Color.White,
                                fontFamily = com.griffith.luckywheel.ui.theme.ArcadeFontFamily,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Modern Load Button
                    Surface(
                        onClick = {
                            soundEffectService.playClickSound()
                            navController.navigate("loadgames/$playerId") {
                                launchSingleTop = true
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = com.griffith.luckywheel.ui.theme.deepForest.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(48.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Load Games",
                                tint = arcadeGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            if (currentGameName != null) {
                Text(
                    text = "Playing: $currentGameName",
                    color = arcadeGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = com.griffith.luckywheel.ui.theme.ArcadeFontFamily,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (!isSpinning) {
                                    soundEffectService.playClickSound()
                                    showBottomSheet = true
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(
                    items = latestWheelItems.value, 
                    rotationDegrees = currentRotationDegrees,
                    onSegmentChange = { segment ->
                        if (isSpinning && rotationSpeed > 1f && segment != lastSegment) {
                            lastSegment = segment
                            hapticService.tick()
                        }
                    }
                )

            }


            AnimatedText(
                text = if (rotationSpeed > 0f) "Spinning..." else "Hold & Shake your phone!",
            )

            var isButtonEnabled by remember { mutableStateOf(true) }
            LaunchedEffect(rotationSpeed) {
                if (rotationSpeed == 0f) {
                    isButtonEnabled = true
                }
            }

            com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.SpinButton(
                isEnabled = isButtonEnabled,
                isSpinning = isSpinning,
                onSpinStart = { 
                    sensorEnabled = true 
                },
                onSpinEnd = {
                    sensorEnabled = false
                    if (rotationSpeed > 0f) {
                        isButtonEnabled = false
                    }
                }
            )
        }

        if (showResultDialog) {
            chosenItem?.let { item ->
                ResultCard(
                    wheelResult = item,
                    onDismiss = { showResultDialog = false }
                )
            }
        }
    }

    EditBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false },
        wheelItems = wheelItems.toList(),
        onUpdateItems = { updated ->
            wheelItems.clear()
            wheelItems.addAll(updated)
        },
        playerId = playerId,
        currentGameId = currentGameId,
        currentGameName = currentGameName,
        onGameSaved = { gameId, gameName ->
            currentGameId = gameId
            currentGameName = gameName
        }
    )
}

// Reusing the same FAB style for consistency
@Composable
fun MagicalGameFAB(iconId: Int, label: String, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(neonLime.copy(alpha = 0.2f), CircleShape)
            )
            
            FloatingActionButton(
                onClick = onClick,
                containerColor = Color.Black,
                contentColor = neonLime,
                shape = CircleShape,
                modifier = Modifier.border(2.dp, neonLime.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = label,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = com.griffith.luckywheel.ui.theme.ArcadeFontFamily,
            color = neonLime,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}