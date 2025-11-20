package com.griffith.luckywheel.ui.screens.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.luckywheel.models.data.BottomNavItem
import com.griffith.luckywheel.ui.screens.playground.components.BottomNavBar
import com.griffith.luckywheel.ui.screens.playground.custom_wheel.CustomWheelScreen
import com.griffith.luckywheel.ui.screens.playground.gold_wheel.GoldWheelScreen

@Composable
fun PlaygroundScreen(navController: NavHostController, playerId: String?) {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem("Gold Game", "goldwheel", Icons.Default.Home),
        BottomNavItem("Custom Wheel", "custompi", Icons.Default.Add)
    )

    // Check if we should navigate to custom wheel
    val navigateToCustom = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.get<Boolean>("navigate_to_custom") ?: false

    // Check if there's a loaded game
    val hasLoadedGame = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.contains("loaded_game") ?: false

    // Set start destination based on flags
    val startDestination = when {
        hasLoadedGame -> "custompi"  // If there's a loaded game, go to custom wheel
        navigateToCustom -> "custompi"
        else -> "goldwheel"
    }

    // Clear the navigate_to_custom flag after using it
    LaunchedEffect(navigateToCustom) {
        if (navigateToCustom) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.remove<Boolean>("navigate_to_custom")
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
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
            startDestination = startDestination,
            modifier = Modifier.padding(
                PaddingValues(bottom = innerPadding.calculateBottomPadding())
            )
        ) {
            composable("goldwheel") { GoldWheelScreen(navController, playerId) }
            composable("custompi") { CustomWheelScreen(navController, playerId) }
        }
    }
}