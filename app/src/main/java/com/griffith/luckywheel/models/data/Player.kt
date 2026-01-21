package com.griffith.luckywheel.models.data

data class Player(
    val playerId: String = "",
    val playerName: String = "",
    val gold: Int = 0,
    val city: String = "",
    val country: String = "",
    val countryCode: String = "", // ISO country code for flag emoji
    val lastLocationUpdate: Long = 0
)
