package com.griffith.goldshake.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.griffith.goldshake.data.Player
import com.griffith.goldshake.screens.auth.components.AuthBgWallpaper
import com.griffith.goldshake.screens.auth.components.CustomTextField
import com.griffith.goldshake.services.DataStoreService
import com.griffith.goldshake.services.FireBaseService
import com.griffith.goldshake.services.validateEmail
import com.griffith.goldshake.services.validatePassword
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()

    fun onRegister() {
        name = name.trim()
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

        firebaseService.registerUserWithPlayer(name, email, password) { success, message ->
            if (success) {
                val userId = firebaseService.getCurrentUserId()
                if (userId != null) {
                    firebaseService.getPlayerInfoById(userId) { player ->
                        if (player != null) {
                            // Save player info in DataStore using coroutineScope
                            coroutineScope.launch {
                                dataStoreService.clear()  // optional: clear old player
                                dataStoreService.savePlayer(player)

                                // Debug print
                                val prefs: Player = dataStoreService.getPlayer()
                                println("Player DataStore: $prefs")
                            }

                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                            navController.navigate("play") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    }
                }

            } else {
                Toast.makeText(
                    context,
                    message ?: "Failed to register",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    AuthBgWallpaper {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Register", color = Color.White) },
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
                    text = "Join Now",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(24.dp))

                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(Modifier.height(16.dp))

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
                    onClick = { onRegister() }
                ) {
                    Text("Register", style = MaterialTheme.typography.titleMedium)
                }

                TextButton(
                    onClick = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                ) {
                    Text("Already have an account? Login", color = Color.White)
                }
            }
        }
    }
}
