package com.griffith.goldshake.screens.playground


import android.content.Context
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.goldshake.screens.playground.components.AnimatedText
import com.griffith.goldshake.screens.playground.components.ResultCard
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// --- Game Data Structures ---

enum class SpinActionType { GAIN_GOLD, LOSE_GOLD, MULTIPLY_GOLD }

data class SpinWheelItem(
    val label: String,
    val color: Color,
    val type: SpinActionType,
    val value: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayGround(navController: NavHostController) {
    val context = LocalContext.current

    val wheelItems = remember {
        listOf(
            SpinWheelItem("+100", Color(0xFF4CAF50), SpinActionType.GAIN_GOLD, 100),
            SpinWheelItem("2x GOLD", Color(0xFFFFC107), SpinActionType.MULTIPLY_GOLD, 2),
            SpinWheelItem("-200", Color(0xFFF44336), SpinActionType.LOSE_GOLD, 200),
            SpinWheelItem("+500", Color(0xFF2196F3), SpinActionType.GAIN_GOLD, 500),
            SpinWheelItem("LOSE ALL", Color(0xFF607D8B), SpinActionType.LOSE_GOLD, Int.MAX_VALUE),
            SpinWheelItem("+250", Color(0xFF9C27B0), SpinActionType.GAIN_GOLD, 250),
            SpinWheelItem("-1000", Color(0xFF795548), SpinActionType.LOSE_GOLD, 1000),
            SpinWheelItem("3x GOLD", Color(0xFFFF5722), SpinActionType.MULTIPLY_GOLD, 3)
        )
    }

    var playerGold by remember { mutableIntStateOf(500) }
    var currentRotationDegrees by remember { mutableFloatStateOf(0f) }
    var rotationSpeed by remember { mutableFloatStateOf(0f) }
    var showResultDialog by remember { mutableStateOf(false) }
    var lastSpinResult by remember { mutableStateOf<SpinWheelItem?>(null) }
    val isSpinning by remember { derivedStateOf { rotationSpeed > 0 } }

    var sensorEnabled by remember { mutableStateOf(true) }

    fun getResultFromAngle(angle: Float): SpinWheelItem {
        val degreesPerSlice = 360f / wheelItems.size
        val normalizedAngle = (angle + 90f) % 360
        val correctedAngle = (360f - normalizedAngle) % 360
        val winnerIndex =
            (correctedAngle / degreesPerSlice).toInt().coerceIn(0, wheelItems.size - 1)
        return wheelItems[winnerIndex]
    }

    fun processResult() {
        val resultItem = getResultFromAngle(currentRotationDegrees)
        lastSpinResult = resultItem

        when (resultItem.type) {
            SpinActionType.GAIN_GOLD -> playerGold += resultItem.value
            SpinActionType.LOSE_GOLD -> {
                playerGold =
                    if (resultItem.value == Int.MAX_VALUE) 0 else (playerGold - resultItem.value).coerceAtLeast(
                        0
                    )
            }

            SpinActionType.MULTIPLY_GOLD -> playerGold *= resultItem.value
        }
        showResultDialog = true
        sensorEnabled = false
    }

    DisposableEffect(sensorEnabled) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (!sensorEnabled || event == null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt(x * x + y * y + z * z) - 9.8f // Remove gravity

                if (magnitude > 2f) {
                    rotationSpeed += magnitude * 0.5f // Add more spin based on shake strength
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (rotationSpeed > 0f) {
                currentRotationDegrees = (currentRotationDegrees + rotationSpeed) % 360
                rotationSpeed *= 0.99f // Smooth friction

                if (rotationSpeed < 0.1f) {
                    rotationSpeed = 0f
                    processResult()
                }
            }
            delay(16)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Spin The Wheel!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C273A),         // Background color
                )
            )
        },
        containerColor = Color(0xFF151921)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                //   .background(Color(0xFF1C273A))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    "GOLD: $playerGold",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFD700)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpinWheelCanvas(items = wheelItems, rotationDegrees = currentRotationDegrees)
            }

            AnimatedText(text = if (isSpinning) "Spinning..." else "Shake your phone to spin!")
        }

        if (showResultDialog) {
            lastSpinResult?.let { result ->
                ResultCard(
                    resultText = "You landed on:\n${result.label}",
                    onDismiss = {
                        showResultDialog = false
                        sensorEnabled = true // Re-enable sensor
                    }
                )
            }
        }
    }


}

// --- UI Components ---

@Composable
private fun SpinWheelCanvas(items: List<SpinWheelItem>, rotationDegrees: Float) {
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 45f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = (size.minDimension / 2) * 0.95f
        val degreesPerSlice = 360f / items.size

        rotate(rotationDegrees, pivot = center) {
            items.forEachIndexed { index, item ->
                val startAngle = index * degreesPerSlice

                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = degreesPerSlice,
                    useCenter = true,
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + degreesPerSlice / 2f
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.65f

                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds(item.label, 0, item.label.length, textBounds)
                    val textHeight = textBounds.height()

                    canvas.nativeCanvas.drawText(
                        item.label,
                        textX,
                        textY + textHeight / 2,
                        textPaint
                    )
                }
            }
        }

        val pointerPath = Path().apply {
            val pointerWidth = 60f
            val pointerHeight = 50f
            moveTo(centerX - pointerWidth / 2, 0f)
            lineTo(centerX, pointerHeight)
            lineTo(centerX + pointerWidth / 2, 0f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFFEFEBEB))
    }
}


