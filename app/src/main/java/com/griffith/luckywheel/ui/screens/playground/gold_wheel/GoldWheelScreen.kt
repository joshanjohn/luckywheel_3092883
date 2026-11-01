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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.GameController
import com.adamglin.phosphoricons.regular.Gear
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.HapticFeedbackService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.GoldCountComponent
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.SpinButton
import com.griffith.luckywheel.ui.screens.playground.logic.generateRandomGoldWheelItems
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.screens.playground.logic.updatePlayerGold
import com.griffith.luckywheel.ui.theme.*
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

// Physics Constants
private const val FRICTION = 0.99f
private const val MIN_SPEED = 0.1f
private const val SHAKE_SENSITIVITY = 2.0f
private const val GRAVITY = 9.8f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldWheelScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = LocalContext.current
    val fireBaseService = remember { FireBaseService() }
    val hapticService = remember { HapticFeedbackService(context) }
    val soundEffectService = remember { SoundEffectService(context) }
    val dataStoreService = remember { DataStoreService(context) }
    val coroutineScope = rememberCoroutineScope()

    // --- Spin mode preference ---
    val sensorSpinEnabled by dataStoreService.getSensorSpinEnabled().collectAsState(initial = true)

    // --- State ---
    var wheelItems by remember { mutableStateOf(generateRandomGoldWheelItems(primaryNeon, surfaceDark)) }
    var playerGold by remember { mutableIntStateOf(0) }
    var playerName by remember { mutableStateOf("") }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var sensorActive by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<SpinWheelItem?>(null) }
    var lastSegmentIndex by remember { mutableIntStateOf(-1) }
    var showExplosion by remember { mutableStateOf(false) }

    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }

    // --- Firebase Sync ---
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

    // --- Logic ---
    fun onSpinComplete() {
        val result = getResultFromAngle(rotationDegrees, wheelItems)
        lastResult = result
        playerGold = updatePlayerGold(playerGold, result)
        
        playerId?.let { id ->
            coroutineScope.launch { fireBaseService.updatePlayerGold(id, playerGold) }
        }
        
        showResult = true
        sensorActive = false
        hapticService.strongVibration()
        
        result?.let { item ->
            when (item.type) {
                SpinActionType.GAIN_GOLD, SpinActionType.MULTIPLY_GOLD -> {
                    soundEffectService.playWinSound()
                    showExplosion = true
                }
                SpinActionType.LOSE_GOLD -> soundEffectService.playLoseSound()
                else -> {}
            }
        }
        
        // Refresh items for next spin
        wheelItems = generateRandomGoldWheelItems(primaryNeon, surfaceDark)
    }

    // --- Sensors & Physics (only active in shake mode) ---
    DisposableEffect(sensorActive, sensorSpinEnabled) {
        if (!sensorSpinEnabled) { onDispose {} }
        else {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (!sensorActive || event == null) return
                    val (x, y, z) = event.values
                    val magnitude = sqrt(x * x + y * y + z * z) - GRAVITY
                    if (magnitude > SHAKE_SENSITIVITY) rotationSpeed += magnitude * 0.5f
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            hapticService.cancel()
            soundEffectService.release()
        }
    }

    // withFrameMillis ties physics ticks exactly to vsync — never faster,
    // never slower — eliminating the drift of a fixed delay() loop.
    LaunchedEffect(Unit) {
        var lastFrameMs = withFrameMillis { it }
        while (true) {
            val frameMs = withFrameMillis { it }
            val dtMs = (frameMs - lastFrameMs).coerceIn(1L, 48L) // cap at ~2 frames
            lastFrameMs = frameMs
            if (rotationSpeed > 0f) {
                val dtScale = dtMs / 16f   // normalise relative to 60 fps
                rotationDegrees = (rotationDegrees + rotationSpeed * dtScale) % 360f
                rotationSpeed *= FRICTION.pow(dtScale)
                if (rotationSpeed < MIN_SPEED) {
                    rotationSpeed = 0f
                    onSpinComplete()
                }
            }
        }
    }

    // --- UI Layout ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalGameFAB(
                icon = PhosphorIcons.Regular.GameController,
                label = "CUSTOM WHEEL",
                onClick = {
                    navController.navigate("custompi/$playerId") {
                        popUpTo("home/$playerId") { inclusive = false }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            HeaderArea(
                navController = navController,
                playerId = playerId,
                playerName = playerName,
                playerGold = playerGold,
                onSettings = {
                    soundEffectService.playClickSound()
                    navController.navigate("settings/$playerId")
                },
                onLeaderboard = {
                    soundEffectService.playBubbleClickSound()
                    navController.navigate("leaderboard?playerId=$playerId") { launchSingleTop = true }
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(
                    items = wheelItems,
                    rotationDegrees = rotationDegrees,
                    onSegmentChange = { segment ->
                        if (isSpinning && rotationSpeed > 1.2f && segment != lastSegmentIndex) {
                            lastSegmentIndex = segment
                            hapticService.tick()
                        }
                    }
                )
            }

            BottomArea(
                modifier = Modifier.align(Alignment.BottomCenter),
                isSpinning = isSpinning,
                sensorSpinEnabled = sensorSpinEnabled,
                onSpinStart = { sensorActive = true },
                onSpinEnd = { sensorActive = false },
                onTapSpin = { rotationSpeed = Random.nextFloat() * 17f + 18f }
            )
        }

        if (showResult) {
            lastResult?.let { result ->
                ResultCard(
                    wheelResult = result,
                    onDismiss = {
                        showResult = false
                        sensorActive = false
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
private fun HeaderArea(
    navController: NavHostController,
    playerId: String?,
    playerName: String,
    playerGold: Int,
    onSettings: () -> Unit,
    onLeaderboard: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CircleNavButton(Icons.Default.Home) { navController.navigate("home/$playerId") }
            CircleNavButton(PhosphorIcons.Regular.Gear, onSettings)
        }

        GoldCountComponent(playerName, playerGold)

        IconButton(
            onClick = onLeaderboard,
            modifier = Modifier
                .size(42.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Image(
                painterResource(R.drawable.icon_trophy),
                null,
                modifier = Modifier.size(26.dp).padding(4.dp)
            )
        }
    }
}

@Composable
private fun BottomArea(
    modifier: Modifier,
    isSpinning: Boolean,
    sensorSpinEnabled: Boolean,
    onSpinStart: () -> Unit,
    onSpinEnd: () -> Unit,
    onTapSpin: () -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val hintText = when {
            isSpinning -> "Spinning..."
            sensorSpinEnabled -> "Hold & Shake to spin!  📳"
            else -> "Tap the button to spin!  👆"
        }
        AnimatedText(text = hintText, baseColor = Color.White)

        Spacer(Modifier.height(16.dp))

        var btnActive by remember { mutableStateOf(true) }
        LaunchedEffect(isSpinning) { if (!isSpinning) btnActive = true }

        SpinButton(
            isEnabled = btnActive,
            isSpinning = isSpinning,
            sensorMode = sensorSpinEnabled,
            onSpinStart = onSpinStart,
            onSpinEnd = {
                onSpinEnd()
                if (isSpinning) btnActive = false
            },
            onTapSpin = {
                if (!isSpinning) {
                    btnActive = false
                    onTapSpin()
                }
            }
        )
    }
}

@Composable
private fun CircleNavButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .background(Color.White.copy(alpha = 0.1f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
    ) {
        Icon(icon, null, tint = arcadeGold, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun MagicalGameFAB(icon: ImageVector, label: String, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
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
                    .size(68.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .background(primaryNeon.copy(alpha = 0.2f), CircleShape)
            )
            
            FloatingActionButton(
                onClick = onClick,
                containerColor = Color.Black,
                contentColor = primaryNeon,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, primaryNeon.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(icon, label, modifier = Modifier.size(28.dp))
            }
        }
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = ArcadeFontFamily,
            color = primaryNeon,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
