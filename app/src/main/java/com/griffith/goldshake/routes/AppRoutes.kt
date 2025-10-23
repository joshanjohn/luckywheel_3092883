package com.griffith.goldshake.routes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.griffith.goldshake.screens.auth.LoginScreen
import com.griffith.goldshake.screens.auth.RegisterScreen
import com.griffith.goldshake.screens.playground.PlayGround


@Composable
fun AppRoute() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {

        // play ground screen
        composable("play/{playerId}") {
            val playerId = it.arguments?.getString("playerId")
            PlayGround(navController, playerId)
        }
        composable("register") { RegisterScreen(navController) } // register screen
        composable("login") { LoginScreen(navController) }  // login screen
    }
}