package com.griffith.luckywheel.data

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.griffith.luckywheel.ui.screens.playground.enums.SpinActionType
import kotlinx.parcelize.Parcelize


@Parcelize
data class SavedWheelItem(
    val label: String = "",
    val colorHex: String = "", // Store color as hex string for Firebase
    val type: String = "", // Store enum as string
    val value: Int = 0,
    val percent: Float = 0f
) : Parcelable


@Parcelize
data class SavedGame(
    val gameId: String = "",
    val gameName: String = "",
    val playerId: String = "",
    val wheelItems: List<SavedWheelItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable



// Helper extension function to convert SavedGame back to SpinWheelItem list
fun SavedGame.toSpinWheelItems(): List<SpinWheelItem> {
    return wheelItems.map { saved ->
        SpinWheelItem(
            label = saved.label,
            color = Color(android.graphics.Color.parseColor(saved.colorHex)),
            type = try {
                SpinActionType.valueOf(saved.type)
            } catch (e: Exception) {
                SpinActionType.CUSTOM
            },
            value = saved.value,
            percent = saved.percent
        )
    }
}
