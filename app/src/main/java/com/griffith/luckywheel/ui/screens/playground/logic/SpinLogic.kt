package com.griffith.luckywheel.ui.screens.playground.logic


import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import kotlin.random.Random

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
        SpinActionType.CUSTOM -> currentGold
    }
}

fun generateRandomGoldWheelItems(lightGreenColor: Color, darkGreenColor: Color): List<SpinWheelItem> {
    val items = mutableListOf<SpinWheelItem>()
    
    // Define possible values for each action type
    val gainValues = listOf(50, 100, 150, 200, 250, 300, 400, 500, 750, 1000)
    val loseValues = listOf(50, 100, 150, 200, 300, 500, 750, 1000)
    val multiplyValues = listOf(2, 3, 4, 5)
    
    // Generate 8 random items with balanced distribution
    val actionTypes = mutableListOf<SpinActionType>()
    
    // Ensure at least 2 of each type for balance
    actionTypes.add(SpinActionType.GAIN_GOLD)
    actionTypes.add(SpinActionType.GAIN_GOLD)
    actionTypes.add(SpinActionType.LOSE_GOLD)
    actionTypes.add(SpinActionType.LOSE_GOLD)
    actionTypes.add(SpinActionType.MULTIPLY_GOLD)
    
    // Add 3 more random actions
    repeat(3) {
        when (Random.nextInt(3)) {
            0 -> actionTypes.add(SpinActionType.GAIN_GOLD)
            1 -> actionTypes.add(SpinActionType.LOSE_GOLD)
            2 -> actionTypes.add(SpinActionType.MULTIPLY_GOLD)
        }
    }
    
    // Shuffle to randomize positions
    actionTypes.shuffle()
    
    // Create wheel items
    actionTypes.forEachIndexed { index, actionType ->
        val color = if (index % 2 == 0) lightGreenColor else darkGreenColor
        
        when (actionType) {
            SpinActionType.GAIN_GOLD -> {
                val value = gainValues.random()
                items.add(SpinWheelItem("+$value", color, SpinActionType.GAIN_GOLD, value, 0.125f))
            }
            SpinActionType.LOSE_GOLD -> {
                // 10% chance for "LOSE ALL", otherwise random loss value
                if (Random.nextFloat() < 0.1f) {
                    items.add(SpinWheelItem("LOSE ALL", color, SpinActionType.LOSE_GOLD, Int.MAX_VALUE, 0.125f))
                } else {
                    val value = loseValues.random()
                    items.add(SpinWheelItem("-$value", color, SpinActionType.LOSE_GOLD, value, 0.125f))
                }
            }
            SpinActionType.MULTIPLY_GOLD -> {
                val value = multiplyValues.random()
                items.add(SpinWheelItem("${value}x GOLD", color, SpinActionType.MULTIPLY_GOLD, value, 0.125f))
            }
            else -> {}
        }
    }
    
    return items
}
