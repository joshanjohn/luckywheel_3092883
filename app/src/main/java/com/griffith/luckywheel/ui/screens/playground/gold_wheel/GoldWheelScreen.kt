package com.griffith.luckywheel.ui.screens.playground.gold_wheel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.R
import com.griffith.luckywheel.data.Player
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.playground.components.AnimatedText
import com.griffith.luckywheel.ui.screens.playground.components.SpinWheel
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.GoldCountComponent
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.components.ResultCard
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.model.SpinActionType
import com.griffith.luckywheel.ui.screens.playground.logic.getResultFromAngle
import com.griffith.luckywheel.ui.screens.playground.logic.updatePlayerGold
import com.griffith.luckywheel.ui.theme.LightGreenColor
import kotlinx.coroutines.delay
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

    val wheelItems = remember {
        listOf(
            SpinWheelItem("+100", LightGreenColor, SpinActionType.GAIN_GOLD, 100, 0.125f),
            SpinWheelItem("2x GOLD", Color(0xFF062E12), SpinActionType.MULTIPLY_GOLD, 2, 0.125f),
            SpinWheelItem("-200", LightGreenColor, SpinActionType.LOSE_GOLD, 200, 0.125f),
            SpinWheelItem("+500", Color(0xFF062E12), SpinActionType.GAIN_GOLD, 500, 0.125f),
            SpinWheelItem("LOSE ALL", LightGreenColor, SpinActionType.LOSE_GOLD, Int.MAX_VALUE, 0.125f),
            SpinWheelItem("+250", Color(0xFF062E12), SpinActionType.GAIN_GOLD, 250, 0.125f),
            SpinWheelItem("-1000", LightGreenColor, SpinActionType.LOSE_GOLD, 1000, 0.125f),
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
        playerId?.let { fireBaseService.updatePlayerGold(it, playerGold) {} }
        showResultDialog = true
        sensorEnabled = false
    }

    // Sensor Handling
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
                Button(
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    onClick = { navController.navigate("leaderboard") }
                ) {
                    Image(
                        painterResource(R.drawable.trophy),
                        contentDescription = "leaderboard icon"
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheel(items = wheelItems, rotationDegrees = currentRotationDegrees)
            }

            AnimatedText(text = if (isSpinning) "Spinning..." else "Hold & Shake \nyour phone to spin!")

            var isButtonPressed by remember { mutableStateOf(true) }

            LaunchedEffect(isButtonPressed) { sensorEnabled = isButtonPressed }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {},
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonPressed)  Color(0xFF4CAF50) else Color(0xFF006400)
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
                            text = if (isButtonPressed) "Spinning..." else "Hold to \nSpin",
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
