package com.griffith.luckywheel.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SavedGame(
    val gameId: String = "",
    val gameName: String = "",
    val playerId: String = "",
    val wheelItems: List<SavedWheelItem> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class SavedWheelItem(
    val label: String = "",
    val colorHex: String = "", // Store color as hex string for Firebase
    val type: String = "", // Store enum as string
    val value: Int = 0,
    val percent: Float = 0f
) : Parcelable