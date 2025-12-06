package com.griffith.luckywheel.routes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.luckywheel.ui.screens.auth.ForgotPasswordScreen
import com.griffith.luckywheel.ui.screens.auth.LoginScreen
import com.griffith.luckywheel.ui.screens.auth.RegisterScreen
import com.griffith.luckywheel.ui.screens.leaderboard.LeaderboardScreen
import com.griffith.luckywheel.ui.screens.loadgames.LoadGamesScreen
import com.griffith.luckywheel.ui.screens.playground.PlaygroundScreen
import com.griffith.luckywheel.ui.screens.profile.ProfileScreen
import com.griffith.luckywheel.ui.screens.settings.SettingsScreen

@Composable
fun AppRoute(startDestination: String = "login") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("play/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            PlaygroundScreen(navController, playerId)
        }

        composable("loadgames/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            LoadGamesScreen(navController, playerId)
        }

        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("forgotpassword") { ForgotPasswordScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("profile/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            ProfileScreen(navController, playerId)
        }
        composable("leaderboard") { LeaderboardScreen(navController) }
    }
}