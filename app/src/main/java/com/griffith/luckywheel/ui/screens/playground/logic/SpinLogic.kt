package com.griffith.luckywheel.ui.screens.playground.logic


import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.model.SpinActionType

fun getResultFromAngle(angle: Float, wheelItems: List<SpinWheelItem>): SpinWheelItem {
    val normalizedAngle = (angle + 90f) % 360f
    val correctedAngle = (360f - normalizedAngle) % 360f
    var cumulativeAngle = 0f

    for (item in wheelItems) {
        val sliceAngle = item.percent * 360f
        if (correctedAngle in cumulativeAngle..(cumulativeAngle + sliceAngle)) {
            return item
        }
        cumulativeAngle += sliceAngle
    }
    return wheelItems.last()
}

fun updatePlayerGold(currentGold: Int, resultItem: SpinWheelItem): Int {
    return when (resultItem.type) {
        SpinActionType.GAIN_GOLD -> currentGold + resultItem.value
        SpinActionType.LOSE_GOLD -> if (resultItem.value == Int.MAX_VALUE) 0 else (currentGold - resultItem.value).coerceAtLeast(0)
        SpinActionType.MULTIPLY_GOLD -> currentGold * resultItem.value
    }
}
