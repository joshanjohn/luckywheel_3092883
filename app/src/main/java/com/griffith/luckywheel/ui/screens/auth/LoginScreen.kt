package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
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
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authService = remember { AuthenticationService(context) }
    val firebaseService = remember { FireBaseService() }
    val dataStoreService = remember { DataStoreService(context) }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Launcher for Google Sign-In intent
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        authService.handleGoogleSignInResult(result.data) { success, message, userId ->
            if (success && userId != null) {
                coroutineScope.launch {
                    val playerResult = firebaseService.getPlayerInfo(userId)
                    playerResult.onSuccess { player ->
                        dataStoreService.savePlayer(player)
                        navController.navigate("home/$userId") {
                            popUpTo("login") { inclusive = true }
                        }
                    }.onFailure {
                        isLoading = false
                        Toast.makeText(context, "Failed to load player data", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                isLoading = false
                Toast.makeText(context, message ?: "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AuthBgWallpaper {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LUCKY\nWHEEL",
                fontFamily = ArcadeFontFamily,
                fontSize = 40.sp,
                color = arcadeGold,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                    label = "LOGIN",
                    onSubmit = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            return@AuthSubmitBtn
                        }
                        isLoading = true
                        authService.loginWithEmailPassword(email, password) { success, message, userId ->
                            if (success && userId != null) {
                                coroutineScope.launch {
                                    val result = firebaseService.getPlayerInfo(userId)
                                    result.onSuccess { player ->
                                        dataStoreService.savePlayer(player)
                                        navController.navigate("home/$userId") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }.onFailure {
                                        isLoading = false
                                        Toast.makeText(context, "Failed to load player data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, message ?: "Login failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Divider with OR label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
                    Text(
                        text = "  OR  ",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.3f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Google Sign-In button
                OutlinedButton(
                    onClick = {
                        googleSignInLauncher.launch(authService.getGoogleSignInIntent())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_google_logo),
                        contentDescription = "Google logo",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Don't have an account? Register",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}
