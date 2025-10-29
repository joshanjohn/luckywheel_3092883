package com.griffith.luckywheel.routes

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.luckywheel.screens.auth.LoginScreen
import com.griffith.luckywheel.screens.auth.RegisterScreen
import com.griffith.luckywheel.screens.playground.PlayGround
import com.griffith.luckywheel.screens.leaderboard.LeaderBoardScreen
import com.griffith.luckywheel.screens.settings.SettingsScreen
import com.griffith.luckywheel.services.DataStoreService
import kotlinx.coroutines.launch

@Composable
fun AppRoute() {
    val navController = rememberNavController()
    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val coroutineScope = rememberCoroutineScope()
    var startDestination by remember { mutableStateOf("login") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val player = dataStoreService.getPlayer()
            if (player.playerId.isNotEmpty()) {
                startDestination = "play/${player.playerId}"
                navController.navigate(startDestination) { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("play/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            PlayGround(navController, playerId)
        }
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("leaderboard") { LeaderBoardScreen(navController) }
    }
}
