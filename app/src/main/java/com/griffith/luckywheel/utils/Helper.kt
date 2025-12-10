package com.griffith.luckywheel.utils

import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.abs

fun Color.isColorDark(): Boolean {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return luminance < 0.5
}

/**
 * Formats large numbers into compact form with K (thousands) or M (millions) suffix
 * Examples:
 * - 1234 -> "1.2K"
 * - 45678 -> "45.7K"
 * - 1234567 -> "1.2M"
 * - 999 -> "999"
 */
fun formatNumberCompact(number: Int): String {
    val absNumber = abs(number)
    val sign = if (number < 0) "-" else ""
    
    return when {
        absNumber < 1000 -> {
            // Less than 1K, show full number
            String.format(Locale.getDefault(), "%,d", number)
        }
        absNumber < 1_000_000 -> {
            // Thousands (1K - 999.9K)
            val thousands = absNumber / 1000.0
            if (thousands >= 100) {
                // 100K+ show without decimal
                String.format(Locale.getDefault(), "%s%dK", sign, (thousands.toInt()))
            } else {
                // Under 100K show one decimal
                String.format(Locale.getDefault(), "%s%.1fK", sign, thousands)
            }
        }
        absNumber < 1_000_000_000 -> {
            // Millions (1M - 999.9M)
            val millions = absNumber / 1_000_000.0
            if (millions >= 100) {
                // 100M+ show without decimal
                String.format(Locale.getDefault(), "%s%dM", sign, (millions.toInt()))
            } else {
                // Under 100M show one decimal
                String.format(Locale.getDefault(), "%s%.1fM", sign, millions)
            }
        }
        else -> {
            // Billions (1B+)
            val billions = absNumber / 1_000_000_000.0
            String.format(Locale.getDefault(), "%s%.1fB", sign, billions)
        }
    }
}