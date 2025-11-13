package com.griffith.luckywheel.ui.screens.playground.custom_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.data.SavedGame
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.services.toSpinWheelItems
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.custom_wheel.components.EditBottomSheet
import com.griffith.luckywheel.ui.screens.playground.enums.SpinActionType
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWheelScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context

    // Check for loaded game from navigation
    val loadedGame = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<SavedGame>("loaded_game")

    // Current game tracking
    var currentGameId by remember { mutableStateOf<String?>(null) }
    var currentGameName by remember { mutableStateOf<String?>(null) }

    //  Wheel Items Default
    val wheelItems = remember {
        mutableStateListOf(
            SpinWheelItem("Alice", Color(0xFF4CAF50), SpinActionType.CUSTOM, 0, 0.5f),
            SpinWheelItem("Bob", Color(0xFFFFC107), SpinActionType.CUSTOM, 0, 0.5f)
        )
    }

    // Load saved game if provided
    LaunchedEffect(loadedGame) {
        loadedGame?.let { game ->
            currentGameId = game.gameId
            currentGameName = game.gameName
            wheelItems.clear()
            wheelItems.addAll(game.toSpinWheelItems())
            // Clear the saved state
            navController.currentBackStackEntry?.savedStateHandle?.remove<SavedGame>("loaded_game")
        }
    }

    //  Wheel Angles (auto-normalize every recomposition)
    val totalPercent = wheelItems.sumOf { it.percent.toDouble() }.toFloat()
    val wheelItemsWithAngles = if (totalPercent == 0f) {
        val equalFraction = 1f / wheelItems.size.coerceAtLeast(1)
        wheelItems.map { it.copy(percent = equalFraction) }
    } else {
        wheelItems.map { it.copy(percent = it.percent / totalPercent) }
    }

    val latestWheelItems = rememberUpdatedState(wheelItemsWithAngles)

    //  Spin Logic variables
    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var sensorEnabled by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var chosenItem by remember { mutableStateOf<SpinWheelItem?>(null) }

    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees, latestWheelItems.value)
        chosenItem = resultItem
        showResultDialog = true
        sensorEnabled = false
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

    //  Rotation Animation & logic
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

    //  Bottom Sheet State
    var showBottomSheet by remember { mutableStateOf(false) }

    //  UI
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Edit Button
                Button(
                    onClick = { showBottomSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF49A84D))
                ) {
                    Text("Edit Wheel", color = Color.White)
                }

                // Load Games Button
                Button(
                    onClick = {
                        navController.navigate("loadgames/$playerId")
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0BA136)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Load Games",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Load", color = Color.White)
                }
            }

            // Current Game Name Display
            if (currentGameName != null) {
                Text(
                    text = "Playing: $currentGameName",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Wheel Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(items = latestWheelItems.value, rotationDegrees = currentRotationDegrees)
            }

            //  Instruction Text
            AnimatedText(
                text = if (rotationSpeed > 0f) "Spinning..." else "Hold & Shake your phone!",
            )

            //  Spin Button
            var isButtonPressed by remember { mutableStateOf(false) }
            LaunchedEffect(isButtonPressed) { sensorEnabled = isButtonPressed }

            Button(
                onClick = {},
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isButtonPressed) Color(0xFF4CAF50) else Color(0xFF006400)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                isButtonPressed = true
                                tryAwaitRelease()
                                isButtonPressed = false
                            })
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isButtonPressed) "Spinning..." else "Hold to Spin",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            //  Result Dialog
            if (showResultDialog) {
                chosenItem?.let { item ->
                    AlertDialog(
                        onDismissRequest = { showResultDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showResultDialog = false }) {
                                Text("OK", color = Color.White)
                            }
                        },
                        title = { Text("Result", color = Color.White) },
                        text = {
                            Text(
                                text = "🎉 ${item.label} was chosen!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        containerColor = Color(0xFF01150B)
                    )
                }
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