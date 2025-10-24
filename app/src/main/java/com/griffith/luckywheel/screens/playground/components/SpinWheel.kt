package com.griffith.luckywheel.screens.playground.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import android.graphics.Rect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpinWheel(
    items: List<SpinWheelItem>,
    rotationDegrees: Float,
) {
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 45f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)
        val radius = (size.minDimension / 2) * 0.9f

        // --- Outer Glow Border ---
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF055C09), Color(0xFF4CAF50)),
                center = center,
                radius = radius * 1.1f
            ),
            radius = radius * 1.1f,
            center = center,
            alpha = 0.3f
        )

        // --- Outer Circle Border ---
        drawCircle(
            color = Color(0xFF23B62A),
            radius = radius + 8f,
            center = center,
            style = Stroke(width = 10f)
        )

        // --- Wheel Slices (updated for percent) ---
        rotate(rotationDegrees, pivot = center) {
            var startAngle = 0f

            items.forEach { item ->
                val sweepAngle = item.percent * 360f

                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + sweepAngle / 2f
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.65f

                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    // Display original label + percentage
                    val displayText = "${item.label} (${(item.percent * 100).toInt()}%)"

                    val textBounds = Rect()
                    textPaint.getTextBounds(displayText, 0, displayText.length, textBounds)
                    val textHeight = textBounds.height()

                    canvas.nativeCanvas.drawText(
                        displayText,
                        textX,
                        textY + textHeight / 2,
                        textPaint
                    )
                }

                startAngle += sweepAngle
            }
        }

        // --- Center Circle Hub ---
        drawCircle(
            color = Color(0xFF1C273A),
            radius = radius * 0.12f,
            center = center
        )
        drawCircle(
            color = Color(0xFF1A1A1A),
            radius = radius * 0.3f,
            center = center
        )

        // --- Center Text: "GOLD SHAKE" ---
        val text = buildAnnotatedString {
            withStyle(
                style = TextStyle(
                    fontSize = 26.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontFamily = BubbleFontFamily,
                    textAlign = TextAlign.Center
                ).toSpanStyle()
            ) {
                append("GOLD\nSHAKE")
            }
        }

        val textLayoutResult = textMeasurer.measure(text)
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = Offset(
                x = centerX - textLayoutResult.size.width / 2,
                y = centerY - textLayoutResult.size.height / 2
            )
        )

        // --- Pointer Triangle ---
        val pointerPath = Path().apply {
            val pointerWidth = 60f
            val pointerHeight = 50f
            moveTo(centerX - pointerWidth / 2, 0f)
            lineTo(centerX, pointerHeight)
            lineTo(centerX + pointerWidth / 2, 0f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFF05F50F))
    }
}
