package com.griffith.luckywheel.screens.playground

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.data.Player
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.screens.playground.components.AnimatedText
import com.griffith.luckywheel.screens.playground.components.SpinWheel
import com.griffith.luckywheel.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.screens.playground.model.SpinActionType
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.R
import com.griffith.luckywheel.screens.AppBar
import com.griffith.luckywheel.ui.theme.DarkerGreenColor
import com.griffith.luckywheel.screens.playground.components.GoldCountComponent
import com.griffith.luckywheel.screens.playground.components.ResultCard
import com.griffith.luckywheel.screens.playground.logic.updatePlayerGold
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayGround(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val fireBaseService = remember { FireBaseService() }

    val wheelItems = remember {
        listOf(
            SpinWheelItem("+100", Color(0xFF4CAF50), SpinActionType.GAIN_GOLD, 100, 0.15f),
            SpinWheelItem("2x GOLD", Color(0xFFFFC107), SpinActionType.MULTIPLY_GOLD, 2, 0.10f),
            SpinWheelItem("-200", Color(0xFFF44336), SpinActionType.LOSE_GOLD, 200, 0.20f),
            SpinWheelItem("+500", Color(0xFF2196F3), SpinActionType.GAIN_GOLD, 500, 0.15f),
            SpinWheelItem("LOSE ALL", Color(0xFF607D8B), SpinActionType.LOSE_GOLD, Int.MAX_VALUE, 0.10f),
            SpinWheelItem("+250", Color(0xFF9C27B0), SpinActionType.GAIN_GOLD, 250, 0.10f),
            SpinWheelItem("-1000", Color(0xFF795548), SpinActionType.LOSE_GOLD, 1000, 0.10f),
            SpinWheelItem("3x GOLD", Color(0xFFFF5722), SpinActionType.MULTIPLY_GOLD, 3, 0.10f),
        )
    }


    var playerGold by remember { mutableIntStateOf(0) }
    var playerName by remember { mutableStateOf("") }

    // --- Real-time Firebase Updates ---
    DisposableEffect(playerId) {
        if (playerId.isNullOrBlank()) {
            onDispose { }
        } else {
            val playerRef = fireBaseService.database.child("players").child(playerId)
            val listener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val player = snapshot.getValue(Player::class.java)
                    player?.let {
                        playerGold = it.gold
                        playerName = it.playerName
                    }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            }
            playerRef.addValueEventListener(listener)
            onDispose {
                playerRef.removeEventListener(listener)
            }
        }
    }

    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var showResultDialog by remember { mutableStateOf(false) }
    var lastSpinResult by remember { mutableStateOf<SpinWheelItem?>(null) }
    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }
    var sensorEnabled by remember { mutableStateOf(false) }



    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees, wheelItems)
        lastSpinResult = resultItem

        playerGold = updatePlayerGold(playerGold, resultItem)

        // Update Firebase with new gold
        playerId?.let { fireBaseService.updatePlayerGold(it, playerGold) {} }

        showResultDialog = true
        sensorEnabled = false
    }

    // --- Sensor Handling ---
    DisposableEffect(sensorEnabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (!sensorEnabled || event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z) - 9.8f
                if (magnitude > 2f) rotationSpeed += magnitude * 0.5f
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                println("sensor accuracy changed: $accuracy")
            }
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // --- Rotation Logic ---
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

    // --- UI ---
    Scaffold(
//        containerColor = backgroundColor,
        topBar = {
            AppBar(navController)
        },
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF033E14),
                        Color(0xFF01150B),
                        Color(0xFF01150B)
                    )
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
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    GoldCountComponent( playerName, playerGold)


                    Button (
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        onClick = {navController.navigate("leaderboard")}
                    ){
                        Image(painterResource(R.drawable.leaderboard_icon), contentDescription = "leaderboard icon")
                    }
                }
            }

            // --- Spining Wheel ---

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(items = wheelItems, rotationDegrees = currentRotationDegrees)
            }

            AnimatedText(text = if (isSpinning) "Spinning..." else "Hold & Shake \nyour phone to spin!")

            // --- Push to Spin Wheel  Button ---
            var isButtonPressed by remember { mutableStateOf(false) }

            LaunchedEffect(isButtonPressed) { sensorEnabled = isButtonPressed }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (isButtonPressed)
                                Color(0xFF4CAF50)
                            else
                                DarkerGreenColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isButtonPressed = true
                                        tryAwaitRelease()
                                        isButtonPressed = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isButtonPressed) "Spining..." else "Hold to \nSpin",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
    }

}
