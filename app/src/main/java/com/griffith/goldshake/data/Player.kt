package com.griffith.goldshake.data

import java.util.UUID
import kotlin.random.Random

data class Player(
    val playerId: String = UUID.randomUUID().toString(), // random UUID
    val playerName: String, // required
    val gold: Int = 0,
)

