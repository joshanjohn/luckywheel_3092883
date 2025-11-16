package com.griffith.luckywheel.utils

import androidx.compose.ui.graphics.Color

fun Color.isColorDark(): Boolean {
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
    return luminance < 0.5
}