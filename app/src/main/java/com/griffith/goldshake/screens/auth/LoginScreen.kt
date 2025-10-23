package com.griffith.goldshake.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.griffith.goldshake.screens.auth.components.AuthBgWallpaper
import com.griffith.goldshake.screens.auth.components.CustomTextField
import com.griffith.goldshake.services.DataStoreService
import com.griffith.goldshake.services.FireBaseService
import com.griffith.goldshake.services.validateEmail
import com.griffith.goldshake.services.validatePassword
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()

    fun onLogin() {
        email = email.trim()
        password = password.trim()

        // validate email
        val emailError = validateEmail(email)
        if (emailError != null) {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
            return
        }

        // validate password
        val passwordError = validatePassword(password)
        if (passwordError != null) {
            Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
            return
        }

        firebaseService.loginUser(email, password) { success, message ->
            if (success) {
                val playerId = firebaseService.getCurrentUserId() ?: ""
                if (playerId.isNotEmpty()) {
                    firebaseService.getPlayerInfo(playerId) { player ->
                        if (player != null) {
                            coroutineScope.launch {
                                dataStoreService.clear()      // optional: clear old player
                                dataStoreService.savePlayer(player)

                                // Debug print
                                val prefs = dataStoreService.getPlayer()
                                println("Player DataStore: $prefs")
                            }
                        }
                    }
                    navController.navigate("play/$playerId") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            } else {
                Toast.makeText(
                    context,
                    message ?: "Failed to login",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AuthBgWallpaper {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Login", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(24.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = Icons.Default.Email
                )

                Spacer(Modifier.height(16.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onLogin() }
                ) {
                    Text("Login", style = MaterialTheme.typography.titleMedium)
                }

                TextButton(
                    onClick = {
                        navController.navigate("register") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                ) {
                    Text("Don't have an account? Register", color = Color.White)
                }

            }

        }
    }

}
