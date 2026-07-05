package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.ui.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.ui.screens.auth.components.CustomTextField
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthenticationService(context) }
    val firebaseService = remember { FireBaseService() }
    val dataStoreService = remember { DataStoreService(context) }
    val coroutineScope = rememberCoroutineScope()

    var playerName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AuthBgWallpaper {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CREATE\nACCOUNT",
                fontFamily = ArcadeFontFamily,
                fontSize = 36.sp,
                color = arcadeGold,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CustomTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = "Player Name",
                leadingIcon = Icons.Default.Person,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = com.griffith.luckywheel.ui.theme.primaryNeon)
            } else {
                AuthSubmitBtn(
                    label = "REGISTER",
                    onSubmit = {
                        if (playerName.isBlank() || email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            return@AuthSubmitBtn
                        }
                        isLoading = true
                        authService.registerWithEmailPassword(playerName, email, password) { success, message, userId ->
                            if (success && userId != null) {
                                coroutineScope.launch {
                                    val result = firebaseService.getPlayerInfo(userId)
                                    result.onSuccess { player ->
                                        dataStoreService.savePlayer(player)
                                        navController.navigate("home/$userId") {
                                            popUpTo("register") { inclusive = true }
                                        }
                                    }.onFailure {
                                        isLoading = false
                                        Toast.makeText(context, "Failed to load player data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Already have an account? Login",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
    }
}
