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
        val centerX = size.width / 2 // Horizontal center of canvas
        val centerY = size.height / 2 // Vertical center of canvas
        val center = Offset(centerX, centerY)
        val radius = (size.minDimension / 2) * 0.9f // Wheel radius (90% of available space)

        // Text sizes proportional to wheel size
        val sliceTextSize = radius * 0.11f // Labels on wheel segments
        val centerTextSize = radius * 0.08f // "Lucky WHEEL" text in center

        // Outer glow effect - radial gradient for depth
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF055C09), Color(0xFF4CAF50)),
                center = center,
                radius = radius * 1.1f
            ),
            radius = radius * 1.1f,
            center = center,
            alpha = 0.3f // Semi-transparent for glow effect
        )

        // Outer border ring - solid stroke around wheel
        drawCircle(
            color = Color(0xFF23B62A),
            radius = radius + 8f,
            center = center,
            style = Stroke(width = 10f)
        )

        // Draw wheel segments - rotated by current rotation angle
        rotate(rotationDegrees, pivot = center) {
            var startAngle = 0f // Track where each segment starts

            items.forEach { item ->
                val sweepAngle = item.percent * 360f // Convert percentage to degrees

                // Draw colored segment arc
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true, // Fill from center (pie slice)
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                // Draw text label on segment
                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + sweepAngle / 2f // Center of segment
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.65f // 65% from center to edge

                    // Calculate text position using polar coordinates: x = r*cos(θ), y = r*sin(θ)
                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    // Choose text color based on background darkness
                    val textColor = if (item.color.isColorDark()) {
                        android.graphics.Color.WHITE // Light text on dark background
                    } else {
                        android.graphics.Color.BLACK // Dark text on light background
                    }

                    val textPaint = Paint().apply {
                        color = textColor
                        textSize = sliceTextSize
                        textAlign = Paint.Align.CENTER // Center text horizontally
                        isAntiAlias = true // Smooth text edges
                        isFakeBoldText = true // Make text bold
                    }

                    // Truncate long text with ellipsis (max 12 characters)
                    val displayText = truncateText(item.label, maxLength = 12)
                    val textBounds = Rect()
                    textPaint.getTextBounds(displayText, 0, displayText.length, textBounds)
                    val textHeight = textBounds.height()

                    // Draw text centered on calculated position
                    canvas.nativeCanvas.drawText(
                        displayText,
                        textX,
                        textY + textHeight / 2f, // Adjust for vertical centering
                        textPaint
                    )
                }

                startAngle += sweepAngle // Move to next segment
            }
        }

        // Center hub - dark circle in middle
        drawCircle(
            color = Color(0xFF1C273A),
            radius = radius * 0.12f,
            center = center
        )

        // Center circle - larger green circle
        drawCircle(
            color = Color(0xFF02610C),
            radius = radius * 0.3f,
            center = center
        )

        // Center text - "Lucky WHEEL" branding
        val centerText = buildAnnotatedString {
            withStyle(
                style = TextStyle(
                    fontSize = 18.sp,
                    color = Color(0xFFFFD700), // Gold color
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = BubbleFontFamily,
                    textAlign = TextAlign.Center
                ).toSpanStyle()
            ) {
                append("Lucky\nWHEEL")
            }
        }

        val textLayoutResult = textMeasurer.measure(centerText)
        // Draw text centered in the wheel
        drawText(
            textMeasurer = textMeasurer,
            text = centerText,
            topLeft = Offset(
                x = centerX - textLayoutResult.size.width / 2,
                y = centerY - textLayoutResult.size.height / 2
            )
        )

        // Pointer triangle at top - shows which segment is selected
        val pointerPath = Path().apply {
            val pointerWidth = radius * 0.25f // Base width of triangle
            val pointerHeight = radius * 0.18f // Height of triangle
            moveTo(centerX - pointerWidth / 2f, 0f) // Left corner at top
            lineTo(centerX, pointerHeight) // Point down to center
            lineTo(centerX + pointerWidth / 2f, 0f) // Right corner at top
            close() // Complete triangle
        }
        drawPath(pointerPath, color = Color(0xFF05F50F)) // Bright green pointer
    }
}