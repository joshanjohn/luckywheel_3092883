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
import com.griffith.luckywheel.ui.screens.tutorial.TutorialScreen

// Main navigation router for the app - defines all screen routes
@Composable
fun AppRoute(startDestination: String = "login") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        // Playground screen - contains gold wheel and custom wheel tabs
        composable("play/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            PlaygroundScreen(navController, playerId)
        }

        // Load saved custom wheel games
        composable("loadgames/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            LoadGamesScreen(navController, playerId)
        }

        // Authentication screens - no playerId needed
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("forgotpassword") { ForgotPasswordScreen(navController) }
        
        // Settings screen - requires playerId for navigation to game screens
        composable("settings/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            SettingsScreen(navController, playerId)
        }
        
        // Profile screen - view and edit player information
        composable("profile/{playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            ProfileScreen(navController, playerId)
        }
        
        // Leaderboard - optional playerId for navigation back to settings
        composable("leaderboard?playerId={playerId}") { backStackEntry ->
            val playerId = backStackEntry.arguments?.getString("playerId")
            LeaderboardScreen(navController, playerId)
        }
        
        // Tutorial screen - how to play guide
        composable("tutorial") {
            TutorialScreen(navController)
        }
    }
}