package com.griffith.goldshake.data

import androidx.compose.ui.graphics.Color
import com.griffith.goldshake.screens.playground.SpinActionType

data class SpinWheelItem(
    val label: String,
    val color: Color,
    val type: SpinActionType,
    val value: Int
)