package com.griffith.goldshake.screens.playground.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import com.griffith.goldshake.data.SpinWheelItem
import kotlin.math.cos
import kotlin.math.sin


// --- UI Components ---
@Composable
fun SpinWheel(items: List<SpinWheelItem>, rotationDegrees: Float) {
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE // label colors
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
