package com.griffith.luckywheel.ui.screens.playground.components

import android.graphics.Paint
import android.graphics.Rect
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.RetroFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.surfaceDark
import com.griffith.luckywheel.ui.theme.surfaceCard
import com.griffith.luckywheel.utils.isColorDark
import com.griffith.luckywheel.utils.truncateText
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the circular wheel of fortune with segmented colors, labels,
 * neon cyan borders, and a pulsing central spin hub.
 */
@Composable
fun SpinWheel(
    items: List<com.griffith.luckywheel.models.data.SpinWheelItem>,
    rotationDegrees: Float,
    onSegmentChange: ((Int) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    
    val currentSegment = remember(rotationDegrees) {
        val normalizedAngle = (rotationDegrees + 90f) % 360f
        val correctedAngle = (360f - normalizedAngle) % 360f
        var cumulativeAngle = 0f
        var segmentIndex = 0
        
        for ((index, item) in items.withIndex()) {
            val sliceAngle = item.percent * 360f
            if (correctedAngle in cumulativeAngle..(cumulativeAngle + sliceAngle)) {
                segmentIndex = index
                break
            }
            cumulativeAngle += sliceAngle
        }
        segmentIndex
    }
    
    androidx.compose.runtime.LaunchedEffect(currentSegment) {
        onSegmentChange?.invoke(currentSegment)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val center = Offset(centerX, centerY)
        val radius = (size.minDimension / 2) * 0.9f

        // Outer Neon Cyan Glow - Casino style halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryNeon.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 1.25f
            ),
            radius = radius * 1.25f,
            center = center
        )

        // Outer solid neon border
        drawCircle(
            color = primaryNeon,
            radius = radius + 10f,
            center = center,
            style = Stroke(width = 10f)
        )

        // Inner magenta glow ring
        drawCircle(
            color = accentMagenta.copy(alpha = 0.6f),
            radius = radius + 5f,
            center = center,
            style = Stroke(width = 4f)
        )

        rotate(rotationDegrees, pivot = center) {
            var startAngle = 0f

            items.forEachIndexed { index, item ->
                val sweepAngle = item.percent * 360f

                // Draw segment slice
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(centerX - radius, centerY - radius)
                )

                // Neon segment divider lines
                val dividerAngle = Math.toRadians(startAngle.toDouble())
                val endX = centerX + radius * cos(dividerAngle).toFloat()
                val endY = centerY + radius * sin(dividerAngle).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2.5f
                )

                drawIntoCanvas { canvas ->
                    val textAngleDegrees = startAngle + sweepAngle / 2f
                    val textAngleRadians = Math.toRadians(textAngleDegrees.toDouble())
                    val textDistance = radius * 0.68f

                    val textX = centerX + (textDistance * cos(textAngleRadians)).toFloat()
                    val textY = centerY + (textDistance * sin(textAngleRadians)).toFloat()

                    val textColor = if (item.color.isColorDark()) {
                        android.graphics.Color.WHITE
                    } else {
                        android.graphics.Color.BLACK
                    }

                    val textPaint = Paint().apply {
                        color = textColor
                        textSize = radius * 0.11f
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }

                    val displayText = truncateText(item.label, maxLength = 10)
                    val textBounds = Rect()
                    textPaint.getTextBounds(displayText, 0, displayText.length, textBounds)
                    val textHeight = textBounds.height()

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

        // Center hub plate
        drawCircle(
            color = surfaceDark,
            radius = radius * 0.32f,
            center = center
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryNeon, surfaceDark),
                center = center,
                radius = radius * 0.3f
            ),
            radius = radius * 0.3f,
            center = center,
            style = Stroke(width = 4f)
        )

        // Center hub background tint
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = radius * 0.28f,
            center = center
        )

        val centerText = buildAnnotatedString {
            withStyle(
                style = TextStyle(
                    fontSize = 18.sp,
                    color = arcadeGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RetroFontFamily,
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

        // Central neon cyan indicator arrow pointer pointing downwards from top
        val pointerPath = Path().apply {
            val pointerWidth = radius * 0.18f
            val pointerHeight = radius * 0.20f
            moveTo(centerX - pointerWidth / 2f, 0f)
            lineTo(centerX, pointerHeight)
            lineTo(centerX + pointerWidth / 2f, 0f)
            close()
        }
        
        drawPath(
            path = pointerPath,
            color = primaryNeon.copy(alpha = 0.5f),
            style = Stroke(width = 6f)
        )
        
        drawPath(pointerPath, color = Color.White)
    }
}