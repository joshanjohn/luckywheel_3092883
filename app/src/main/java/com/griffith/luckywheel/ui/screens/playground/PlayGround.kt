package com.griffith.luckywheel.ui.screens.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.luckywheel.data.BottomNavItem
import com.griffith.luckywheel.screens.playground.custom_wheel.CustomWheelScreen
import com.griffith.luckywheel.screens.playground.gold_wheel.GoldWheelScreen
import com.griffith.luckywheel.screens.playground.components.BottomNavBar

// ------------------------ MAIN PLAYGROUND WITH BOTTOM NAV ------------------------

@Composable
fun PlayGround(navController: NavHostController, playerId: String?) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem("GoldWheel", "goldwheel", Icons.Default.Star),
        BottomNavItem("Custom Pi", "custompi", Icons.Default.Add)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF033E14),
                        Color(0xFF01150B),
                        Color(0xFF01150B)
                    )
                )
            ),
        containerColor = Color.Transparent,
        bottomBar = { BottomNavBar(navController = bottomNavController, items = items) }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "goldwheel",
            modifier = Modifier.padding(
                PaddingValues(bottom = innerPadding.calculateBottomPadding())
            )
        ) {
            composable("goldwheel") { GoldWheelScreen(navController, playerId) }
            composable("custompi") { CustomWheelScreen(navController) }
        }
    }
}





