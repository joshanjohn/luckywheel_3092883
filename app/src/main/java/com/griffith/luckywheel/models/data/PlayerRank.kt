package com.griffith.luckywheel.models.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector


data class PlayerRank(
    val id: Int,
    val name: String,
    val score: Int,
    val rank: Int,
    val playerIcon: ImageVector = Icons.Default.Person,
    val city: String = "",
    val country: String = "",
    val countryCode: String = "" // ISO country code for flag emoji
)