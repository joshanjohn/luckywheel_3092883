package com.griffith.luckywheel.ui.screens.playground.logic


import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import kotlin.random.Random

// Determines which wheel segment the pointer is on based on rotation angle
fun getResultFromAngle(angle: Float, wheelItems: List<SpinWheelItem>): SpinWheelItem {
    // Add 90 degrees to align with pointer at top (12 o'clock position)
    val normalizedAngle = (angle + 90f) % 360f
    // Reverse direction because wheel spins clockwise but we measure counter-clockwise
    val correctedAngle = (360f - normalizedAngle) % 360f
    var cumulativeAngle = 0f

    // Check each wheel segment to find which one the pointer is on
    for (item in wheelItems) {
        val sliceAngle = item.percent * 360f // Convert percentage to degrees
        if (correctedAngle in cumulativeAngle..(cumulativeAngle + sliceAngle)) {
            return item
        }
        cumulativeAngle += sliceAngle
    }
    // Fallback to last item if no match found (shouldn't happen)
    return wheelItems.last()
}

// Updates player's gold based on the spin result
fun updatePlayerGold(currentGold: Int, resultItem: SpinWheelItem): Int {
    return when (resultItem.type) {
        SpinActionType.GAIN_GOLD -> currentGold + resultItem.value // Add gold
        SpinActionType.LOSE_GOLD -> if (resultItem.value == Int.MAX_VALUE) 0 else (currentGold - resultItem.value).coerceAtLeast(0) // Subtract gold, minimum 0
        SpinActionType.MULTIPLY_GOLD -> currentGold * resultItem.value // Multiply current gold
        SpinActionType.CUSTOM -> currentGold // No change for custom items
    }
}

// Generates 8 random wheel items with balanced distribution of actions
fun generateRandomGoldWheelItems(lightGreenColor: Color, darkGreenColor: Color): List<SpinWheelItem> {
    val items = mutableListOf<SpinWheelItem>()
    
    // Define possible values for each action type
    val gainValues = listOf(50, 100, 150, 200, 250, 300, 400, 500, 750, 1000)
    val loseValues = listOf(50, 100, 150, 200, 300, 500, 750, 1000)
    val multiplyValues = listOf(2, 3, 4, 5)
    
    // Create list of action types with guaranteed minimum distribution
    val actionTypes = mutableListOf<SpinActionType>()
    
    // Ensure at least 2 of each type for balanced gameplay
    actionTypes.add(SpinActionType.GAIN_GOLD)
    actionTypes.add(SpinActionType.GAIN_GOLD)
    actionTypes.add(SpinActionType.LOSE_GOLD)
    actionTypes.add(SpinActionType.LOSE_GOLD)
    actionTypes.add(SpinActionType.MULTIPLY_GOLD)
    
    // Add 3 more random actions to reach 8 total items
    repeat(3) {
        when (Random.nextInt(3)) {
            0 -> actionTypes.add(SpinActionType.GAIN_GOLD)
            1 -> actionTypes.add(SpinActionType.LOSE_GOLD)
            2 -> actionTypes.add(SpinActionType.MULTIPLY_GOLD)
        }
    }
    
    // Shuffle to randomize positions on the wheel
    actionTypes.shuffle()
    
    // Create wheel items with random values and alternating colors
    actionTypes.forEachIndexed { index, actionType ->
        val color = if (index % 2 == 0) lightGreenColor else darkGreenColor // Alternate colors for visual appeal
        
        when (actionType) {
            SpinActionType.GAIN_GOLD -> {
                val value = gainValues.random() // Pick random gain amount
                items.add(SpinWheelItem("+$value", color, SpinActionType.GAIN_GOLD, value, 0.125f))
            }
            SpinActionType.LOSE_GOLD -> {
                // 10% chance for "LOSE ALL", otherwise random loss value
                if (Random.nextFloat() < 0.1f) {
                    items.add(SpinWheelItem("LOSE ALL", color, SpinActionType.LOSE_GOLD, Int.MAX_VALUE, 0.125f))
                } else {
                    val value = loseValues.random() // Pick random loss amount
                    items.add(SpinWheelItem("-$value", color, SpinActionType.LOSE_GOLD, value, 0.125f))
                }
            }
            SpinActionType.MULTIPLY_GOLD -> {
                val value = multiplyValues.random() // Pick random multiplier (2x-5x)
                items.add(SpinWheelItem("${value}x GOLD", color, SpinActionType.MULTIPLY_GOLD, value, 0.125f))
            }
            else -> {}
        }
    }
    
    return items // Each item has equal size: 1/8 = 0.125 = 12.5% of wheel
}
