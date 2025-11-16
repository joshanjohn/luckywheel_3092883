package com.griffith.luckywheel.ui.screens.playground.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.bodyFontFamily
import com.griffith.luckywheel.utils.isColorDark
import kotlin.math.cos
import kotlin.math.sin

// Helper function to calculate luminance and determine if color is dark

@Composable
fun SpinWheel(
    items: List<com.griffith.luckywheel.models.data.SpinWheelItem>,
    rotationDegrees: Float,
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)
        val radius = (size.minDimension / 2) * 0.9f

        // Text sizes based on wheel size
        val sliceTextSize = radius * 0.11f      // labels on slices
        val centerTextSize = radius * 0.08f     // "Lucky WHEEL" in center

        // Outer Wheel Border 1
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

        // Outer Circle Border 2
        drawCircle(
            color = Color(0xFF23B62A),
            radius = radius + 8f,
            center = center,
            style = Stroke(width = 10f)
        )

        // Wheel Slices (percent-based)
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

                // Draw slice text
                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + sweepAngle / 2f
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.65f

                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    val textColor = if (item.color.isColorDark()) {
                        android.graphics.Color.WHITE
                    } else {
                        android.graphics.Color.BLACK
                    }

                    val textPaint = Paint().apply {
                        color = textColor
                        textSize = sliceTextSize
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }

                    val displayText = item.label
                    val textBounds = Rect()
                    textPaint.getTextBounds(displayText, 0, displayText.length, textBounds)
                    val textHeight = textBounds.height()

                    canvas.nativeCanvas.drawText(
                        displayText,
                        textX,
                        textY + textHeight / 2f,
                        textPaint
                    )
                }

                startAngle += sweepAngle
            }
        }

        // Center Circle Hub
        drawCircle(
            color = Color(0xFF1C273A),
            radius = radius * 0.12f,
            center = center
        )

        drawCircle(
            color = Color(0xFF02610C),
            radius = radius * 0.3f,
            center = center
        )

        // Center Text: "Lucky WHEEL" with BubbleFontFamily
        val centerText = buildAnnotatedString {
            withStyle(
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BubbleFontFamily,
                    textAlign = TextAlign.Center
                ).toSpanStyle()
            ) {
                append("Lucky\nWHEEL")
            }
        }

        val textLayoutResult = textMeasurer.measure(centerText)
        drawText(
            textMeasurer = textMeasurer,
            text = centerText,
            topLeft = Offset(
                x = centerX - textLayoutResult.size.width / 2,
                y = centerY - textLayoutResult.size.height / 2
            )
        )

        // Pointer Triangle
        val pointerPath = Path().apply {
            val pointerWidth = radius * 0.25f
            val pointerHeight = radius * 0.18f
            moveTo(centerX - pointerWidth / 2f, 0f)
            lineTo(centerX, pointerHeight)
            lineTo(centerX + pointerWidth / 2f, 0f)
            close()
        }
        drawPath(pointerPath, color = Color(0xFF05F50F))
    }
}