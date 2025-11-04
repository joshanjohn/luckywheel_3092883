package com.griffith.luckywheel.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector


data class PlayerRank(
    val id: Int,
    val name: String,
    val score: Int,
    val rank: Int,
    val avatarRes: ImageVector = Icons.Default.Person
)