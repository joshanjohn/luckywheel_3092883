package com.griffith.luckywheel.ui.screens.playground.gold_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.adamglin.phosphoricons.PhosphorIcons
import com.adamglin.phosphoricons.regular.Gamecontroller
import com.adamglin.phosphoricons.regular.Gear
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.HapticFeedbackService
import com.griffith.luckywheel.services.SoundEffectService

import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.GoldCountComponent
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.ui.screens.playground.custom_wheel.MagicalGameFAB
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.SpinButton
import com.griffith.luckywheel.ui.screens.playground.logic.generateRandomGoldWheelItems
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.screens.playground.logic.updatePlayerGold
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.deepForest
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.neonLime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    var wheelItems by remember { 
        mutableStateOf(
            generateRandomGoldWheelItems(magicGreen, deepForest)
        )
    }

    var playerGold by remember { mutableIntStateOf(0) }
    var playerName by remember { mutableStateOf("") }

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

    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var showResultDialog by remember { mutableStateOf(false) }
    var lastSpinResult by remember { mutableStateOf<SpinWheelItem?>(null) }
    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }
    var sensorEnabled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var lastSegment by remember { mutableIntStateOf(-1) }
    var showExplosion by remember { mutableStateOf(false) }

    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees, wheelItems)
        lastSpinResult = resultItem
        playerGold = updatePlayerGold(playerGold, resultItem)
        playerId?.let { id ->
            coroutineScope.launch {
                fireBaseService.updatePlayerGold(id, playerGold)
            }
        }
        showResultDialog = true
        sensorEnabled = false
        hapticService.strongVibration()
        
        resultItem?.let { item ->
            when (item.type) {
                SpinActionType.GAIN_GOLD, SpinActionType.MULTIPLY_GOLD -> {
                    soundEffectService.playWinSound()
                    showExplosion = true
                }
                SpinActionType.LOSE_GOLD -> {
                    soundEffectService.playLoseSound()
                }
                else -> {}
            }
        }
        
        wheelItems = generateRandomGoldWheelItems(magicGreen, deepForest)
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalGameFAB(
                icon = PhosphorIcons.Regular.Gamecontroller,
                label = "CUSTOM WHEEL",
                onClick = { 
                    navController.navigate("custompi/$playerId") {
                        popUpTo("home/$playerId") { inclusive = false }
                    }
                }
            )
        }
    ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Header Section - Pinned to Top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            soundEffectService.playClickSound()
                            navController.navigate("settings/$playerId") 
                        },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Gear,
                            contentDescription = "Settings",
                            tint = arcadeGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                GoldCountComponent(playerName, playerGold)

                IconButton(
                    onClick = { 
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("leaderboard?playerId=$playerId") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Image(
                        painterResource(R.drawable.icon_trophy),
                        contentDescription = "leaderboard icon",
                        modifier = Modifier.size(28.dp).padding(4.dp)
                    )
                }
            }
        }

        // Wheel Section - Perfectly Centered
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            SpinWheel(
                items = wheelItems, 
                rotationDegrees = currentRotationDegrees,
                onSegmentChange = { segment ->
                    if (isSpinning && rotationSpeed > 1f && segment != lastSegment) {
                        lastSegment = segment
                        hapticService.tick()
                    }
                }
            )
        }

        // Controls Section - Pinned to Bottom Center
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedText(
                text = if (isSpinning) "Spinning..." else "Hold & Shake to spin!",
                baseColor = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            var isButtonEnabled by remember { mutableStateOf(true) }
            LaunchedEffect(rotationSpeed) {
                if (rotationSpeed == 0f) {
                    isButtonEnabled = true
                }
            }

            SpinButton(
                isEnabled = isButtonEnabled,
                isSpinning = isSpinning,
                onSpinStart = { sensorEnabled = true },
                onSpinEnd = {
                    sensorEnabled = false
                    if (rotationSpeed > 0f) {
                        isButtonEnabled = false
                    }
                }
            )
        }
    }

        if (showResultDialog) {
            lastSpinResult?.let { result ->
                ResultCard(
                    wheelResult = result,
                    onDismiss = {
                        showResultDialog = false
                        sensorEnabled = false
                    }
                )
            }
        }

        com.griffith.luckywheel.ui.components.effects.ParticleExplosion(
            isTriggered = showExplosion,
            onFinish = { showExplosion = false }
        )
    }
}

@Composable
fun MagicalGameFAB(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
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
            // Glow behind FAB
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
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, neonLime.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = ArcadeFontFamily,
            color = neonLime,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
