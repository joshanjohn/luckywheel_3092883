package com.griffith.luckywheel.models.data

import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.models.enum.SpinActionType

data class SpinWheelItem(
    val label: String,
    val color: Color,
    val type: SpinActionType,
    val value: Int,
    val percent: Float // 0-1
)