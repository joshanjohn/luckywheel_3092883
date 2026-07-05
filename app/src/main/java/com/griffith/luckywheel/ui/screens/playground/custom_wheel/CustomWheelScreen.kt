package com.griffith.luckywheel.ui.screens.playground.custom_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.FolderOpen
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Trophy
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.data.toSpinWheelItems
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.HapticFeedbackService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.custom_wheel.components.EditBottomSheet
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.SpinButton
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random

// Physics Constants
private const val FRICTION = 0.985f
private const val MIN_SPEED = 0.1f
private const val GRAVITY = 9.8f
private const val SHAKE_THRESHOLD = 2.0f
private const val TICK_RATE_MS = 16L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWheelScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val hapticService = remember { HapticFeedbackService(context) }
    val soundEffectService = remember { SoundEffectService(context) }
    val dataStoreService = remember { DataStoreService(context) }

    // --- Spin mode preference ---
    val sensorSpinEnabled by dataStoreService.getSensorSpinEnabled().collectAsState(initial = true)

    // --- State Management ---
    var currentGameId by remember { mutableStateOf<String?>(null) }
    var currentGameName by remember { mutableStateOf<String?>(null) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var isSensorEnabled by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var chosenItem by remember { mutableStateOf<SpinWheelItem?>(null) }
    var lastSegmentIndex by remember { mutableIntStateOf(-1) }
    var isEditSheetVisible by remember { mutableStateOf(false) }

    val wheelItems = remember {
        mutableStateListOf(
            SpinWheelItem("Choice A", Color(0xFF4CAF50), SpinActionType.CUSTOM, 0, 0.5f),
            SpinWheelItem("Choice B", Color(0xFFFFC107), SpinActionType.CUSTOM, 0, 0.5f)
        )
    }

    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }

    // --- Load External Game Hook ---
    val loadedGame = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<SavedGame>("loaded_game")

    LaunchedEffect(loadedGame) {
        loadedGame?.let { game ->
            currentGameId = game.gameId
            currentGameName = game.gameName
            wheelItems.clear()
            wheelItems.addAll(game.toSpinWheelItems())
            navController.currentBackStackEntry?.savedStateHandle?.remove<SavedGame>("loaded_game")
        }
    }

    // --- Physics & Logic ---
    val normalizedItems = remember(wheelItems.toList()) {
        val total = wheelItems.sumOf { it.percent.toDouble() }.toFloat()
        if (total == 0f) {
            val share = 1f / wheelItems.size.coerceAtLeast(1)
            wheelItems.map { it.copy(percent = share) }
        } else {
            wheelItems.map { it.copy(percent = it.percent / total) }
        }
    }

    fun onSpinStopped() {
        chosenItem = getResultFromAngle(rotationDegrees, normalizedItems)
        showResultDialog = true
        isSensorEnabled = false
        hapticService.strongVibration()
    }

    // Accelerometer Effect (only active in shake mode)
    DisposableEffect(isSensorEnabled, sensorSpinEnabled) {
        if (!sensorSpinEnabled) { onDispose {} }
        else {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (!isSensorEnabled || event == null) return
                    val (x, y, z) = event.values
                    val magnitude = sqrt(x * x + y * y + z * z) - GRAVITY
                    if (magnitude > SHAKE_THRESHOLD) {
                        rotationSpeed += magnitude * 0.55f
                    }
                }
                override fun onAccuracyChanged(s: Sensor?, a: Int) {}
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { manager.unregisterListener(listener) }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            hapticService.cancel()
            soundEffectService.release()
        }
    }

    // Physics Engine Loop
    LaunchedEffect(Unit) {
        while (true) {
            if (rotationSpeed > 0f) {
                rotationDegrees = (rotationDegrees + rotationSpeed) % 360f
                rotationSpeed *= FRICTION
                if (rotationSpeed < MIN_SPEED) {
                    rotationSpeed = 0f
                    onSpinStopped()
                }
            }
            delay(TICK_RATE_MS)
        }
    }

    // --- UI Structure ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalGameFAB(
                icon = PhosphorIcons.Regular.Trophy,
                label = "GOLD ARCADE",
                onClick = {
                    navController.navigate("goldwheel/$playerId") {
                        popUpTo("home/$playerId") { inclusive = false }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            HeaderSection(
                navController = navController,
                playerId = playerId,
                currentGameName = currentGameName,
                onEdit = { 
                    soundEffectService.playClickSound()
                    isEditSheetVisible = true 
                },
                onSettings = {
                    soundEffectService.playClickSound()
                    navController.navigate("settings/$playerId")
                },
                onLoad = {
                    soundEffectService.playClickSound()
                    navController.navigate("loadgames/$playerId") { launchSingleTop = true }
                }
            )

            WheelArea(
                modifier = Modifier.align(Alignment.Center),
                items = normalizedItems,
                rotationDegrees = rotationDegrees,
                isSpinning = isSpinning,
                rotationSpeed = rotationSpeed,
                lastSegment = lastSegmentIndex,
                onSegmentChange = { lastSegmentIndex = it },
                onTick = { hapticService.tick() },
                onTap = {
                    if (!isSpinning) {
                        soundEffectService.playClickSound()
                        isEditSheetVisible = true
                    }
                }
            )

            ControlsSection(
                modifier = Modifier.align(Alignment.BottomCenter),
                isSpinning = isSpinning,
                rotationSpeed = rotationSpeed,
                sensorSpinEnabled = sensorSpinEnabled,
                onSpinStart = { isSensorEnabled = true },
                onSpinEnd = { isSensorEnabled = false },
                onTapSpin = { rotationSpeed = Random.nextFloat() * 17f + 18f }
            )
        }

        if (showResultDialog) {
            chosenItem?.let { item ->
                ResultCard(wheelResult = item, onDismiss = { showResultDialog = false })
            }
        }
    }

    EditBottomSheet(
        showBottomSheet = isEditSheetVisible,
        onDismiss = { isEditSheetVisible = false },
        wheelItems = wheelItems.toList(),
        onUpdateItems = { updated ->
            wheelItems.clear()
            wheelItems.addAll(updated)
        },
        playerId = playerId,
        currentGameId = currentGameId,
        currentGameName = currentGameName,
        onGameSaved = { id, name ->
            currentGameId = id
            currentGameName = name
        }
    )
}

@Composable
private fun HeaderSection(
    navController: NavHostController,
    playerId: String?,
    currentGameName: String?,
    onEdit: () -> Unit,
    onSettings: () -> Unit,
    onLoad: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleNavIconButton(Icons.Default.Home) { navController.navigate("home/$playerId") }
                CircleNavIconButton(PhosphorIcons.Regular.Gear, onSettings)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeaderChip(label = "Edit", icon = Icons.Default.Edit, color = magicGreen, onClick = onEdit)
                HeaderChip(label = null, icon = PhosphorIcons.Regular.FolderOpen, color = deepForest, onClick = onLoad, isIconOnly = true)
            }
        }

        if (currentGameName != null) {
            Text(
                text = "Playing: $currentGameName",
                color = arcadeGold,
                fontWeight = FontWeight.Bold,
                fontFamily = ArcadeFontFamily,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun WheelArea(
    modifier: Modifier,
    items: List<SpinWheelItem>,
    rotationDegrees: Float,
    isSpinning: Boolean,
    rotationSpeed: Float,
    lastSegment: Int,
    onSegmentChange: (Int) -> Unit,
    onTick: () -> Unit,
    onTap: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .pointerInput(isSpinning) {
                detectTapGestures(onTap = { if (!isSpinning) onTap() })
            },
        contentAlignment = Alignment.Center
    ) {
        SpinWheel(
            items = items,
            rotationDegrees = rotationDegrees,
            onSegmentChange = { segment ->
                if (isSpinning && rotationSpeed > 1.2f && segment != lastSegment) {
                    onSegmentChange(segment)
                    onTick()
                }
            }
        )
    }
}

@Composable
private fun ControlsSection(
    modifier: Modifier,
    isSpinning: Boolean,
    rotationSpeed: Float,
    sensorSpinEnabled: Boolean,
    onSpinStart: () -> Unit,
    onSpinEnd: () -> Unit,
    onTapSpin: () -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val hintText = when {
            rotationSpeed > 0f -> "Spinning..."
            sensorSpinEnabled -> "Hold & Shake to spin!  📳"
            else -> "Tap the button to spin!  👆"
        }
        AnimatedText(text = hintText)

        Spacer(modifier = Modifier.height(16.dp))

        var btnActive by remember { mutableStateOf(true) }
        LaunchedEffect(rotationSpeed) { if (rotationSpeed == 0f) btnActive = true }

        SpinButton(
            isEnabled = btnActive,
            isSpinning = isSpinning,
            sensorMode = sensorSpinEnabled,
            onSpinStart = onSpinStart,
            onSpinEnd = {
                onSpinEnd()
                if (rotationSpeed > 0f) btnActive = false
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
private fun CircleNavIconButton(icon: ImageVector, onClick: () -> Unit) {
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
private fun HeaderChip(
    label: String?,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    isIconOnly: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.9f),
        modifier = (if (isIconOnly) Modifier.size(42.dp) else Modifier.height(42.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), Color.Transparent)),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (isIconOnly) 0.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
            if (!isIconOnly && label != null) {
                Spacer(Modifier.width(8.dp))
                Text(label, color = Color.White, fontFamily = ArcadeFontFamily, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MagicalGameFAB(icon: ImageVector, label: String, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(primaryNeon.copy(alpha = 0.2f), CircleShape)
            )

            FloatingActionButton(
                onClick = onClick,
                containerColor = Color.Black,
                contentColor = primaryNeon,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, primaryNeon.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(icon, label, modifier = Modifier.size(28.dp))
            }
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = ArcadeFontFamily,
            color = primaryNeon,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
