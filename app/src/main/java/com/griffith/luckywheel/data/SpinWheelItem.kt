package com.griffith.luckywheel.data

import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.screens.playground.model.SpinActionType

data class SpinWheelItem(
    val label: String,
    val color: Color,
    val type: SpinActionType,
    val value: Int,
    val percent: Float // e.g., 0.10f for 10%, must sum to 1.0f across items
)