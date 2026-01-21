package com.griffith.luckywheel.ui.screens.playground.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.utils.isColorDark
import com.griffith.luckywheel.utils.truncateText
import kotlin.math.cos
import kotlin.math.sin

// Draws the spinning wheel with segments, labels, and pointer
@Composable
fun SpinWheel(
    items: List<com.griffith.luckywheel.models.data.SpinWheelItem>,
    rotationDegrees: Float, // Current rotation angle of the wheel
    onSegmentChange: ((Int) -> Unit)? = null // Callback when pointer moves to new segment
) {
    val textMeasurer = rememberTextMeasurer()
    
    // Calculate which segment the pointer is currently on
    val currentSegment = remember(rotationDegrees) {
        val normalizedAngle = (rotationDegrees + 90f) % 360f // Align with pointer at top
        val correctedAngle = (360f - normalizedAngle) % 360f // Reverse for clockwise spin
        var cumulativeAngle = 0f
        var segmentIndex = 0
        
        // Find which segment contains the corrected angle
        for ((index, item) in items.withIndex()) {
            val sliceAngle = item.percent * 360f // Convert percentage to degrees
            if (correctedAngle in cumulativeAngle..(cumulativeAngle + sliceAngle)) {
                segmentIndex = index
                break
            }
            cumulativeAngle += sliceAngle
        }
        segmentIndex
    }
    
    // Notify when segment changes (for haptic feedback)
    androidx.compose.runtime.LaunchedEffect(currentSegment) {
        onSegmentChange?.invoke(currentSegment)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)
        val radius = (size.minDimension / 2) * 0.9f

        // Outer Magical Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(com.griffith.luckywheel.ui.theme.neonLime.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = radius * 1.2f
            ),
            radius = radius * 1.2f,
            center = center
        )

        // Neon Border Outer
        drawCircle(
            color = com.griffith.luckywheel.ui.theme.magicGreen,
            radius = radius + 10f,
            center = center,
            style = Stroke(width = 12f)
        )

        // Neon Glow Inner
        drawCircle(
            color = com.griffith.luckywheel.ui.theme.neonLime.copy(alpha = 0.6f),
            radius = radius + 6f,
            center = center,
            style = Stroke(width = 4f)
        )

        rotate(rotationDegrees, pivot = center) {
            var startAngle = 0f

            items.forEachIndexed { index, item ->
                val sweepAngle = item.percent * 360f

                // Segment Arc
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                // Neon Segment Divider
                val dividerAngle = Math.toRadians(startAngle.toDouble())
                val endX = centerX + radius * cos(dividerAngle).toFloat()
                val endY = centerY + radius * sin(dividerAngle).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )

                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + sweepAngle / 2f
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.7f

                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    val textColor = if (item.color.isColorDark()) {
                        android.graphics.Color.WHITE
                    } else {
                        android.graphics.Color.BLACK
                    }

                    val textPaint = Paint().apply {
                        color = textColor
                        textSize = radius * 0.12f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    val displayText = truncateText(item.label, maxLength = 10)
                    val textBounds = Rect()
                    textPaint.getTextBounds(displayText, 0, displayText.length, textBounds)
                    val textHeight = textBounds.height()

                    // Rotate text to follow segment
                    canvas.save()
                    canvas.nativeCanvas.rotate(textAngleDegrees + 90f, textX, textY)
                    canvas.nativeCanvas.drawText(
                        displayText,
                        textX,
                        textY + textHeight / 2f,
                        textPaint
                    )
                    canvas.restore()
                }

                startAngle += sweepAngle
            }
        }

        // Center hub with multiple layers for depth
        drawCircle(
            color = com.griffith.luckywheel.ui.theme.deepForest,
            radius = radius * 0.32f,
            center = center
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(com.griffith.luckywheel.ui.theme.magicGreen, com.griffith.luckywheel.ui.theme.deepForest),
                center = center,
                radius = radius * 0.3f
            ),
            radius = radius * 0.3f,
            center = center,
            style = Stroke(width = 4f)
        )

        // Center branding background
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = radius * 0.28f,
            center = center
        )

        val centerText = buildAnnotatedString {
            withStyle(
                style = TextStyle(
                    fontSize = 20.sp,
                    color = com.griffith.luckywheel.ui.theme.arcadeGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = com.griffith.luckywheel.ui.theme.RetroFontFamily,
                    textAlign = TextAlign.Center
                ).toSpanStyle()
            ) {
                append("SPIN")
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

        // Magical neon pointer
        val pointerPath = Path().apply {
            val pointerWidth = radius * 0.2f
            val pointerHeight = radius * 0.22f
            moveTo(centerX - pointerWidth / 2f, 0f)
            lineTo(centerX, pointerHeight)
            lineTo(centerX + pointerWidth / 2f, 0f)
            close()
        }
        
        // Pointer shadow/glow
        drawPath(
            path = pointerPath,
            color = com.griffith.luckywheel.ui.theme.neonLime.copy(alpha = 0.5f),
            style = Stroke(width = 8f)
        )
        
        drawPath(pointerPath, color = Color.White)
    }
}