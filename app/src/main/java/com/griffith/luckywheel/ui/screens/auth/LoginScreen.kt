package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.validateEmail
import com.griffith.luckywheel.services.validatePassword
import com.griffith.luckywheel.ui.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.ui.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.ui.screens.auth.components.CustomTextField
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()

    fun onLogin() {
        email = email.trim()
        password = password.trim()

        val emailError = validateEmail(email)
        if (emailError != null) {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
            return
        }

        val passwordError = validatePassword(password, disable_string_validations = true)
        if (passwordError != null) {
            Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        firebaseService.loginUser(email, password) { success, message ->
            isLoading = false
            if (success) {
                val playerId = firebaseService.getCurrentUserId() ?: ""
                if (playerId.isNotEmpty()) {
                    firebaseService.getPlayerInfo(playerId) { player ->
                        if (player != null) {
                            coroutineScope.launch {
                                dataStoreService.clear()
                                dataStoreService.savePlayer(player)
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
        Scaffold(containerColor = Color.Transparent) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .focusGroup()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White)) {
                                append("BACK TO EARN\n\nMORE ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = goldColor,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("GOLD")
                            }
                        },
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = BubbleFontFamily
                    )

                    Spacer(Modifier.height(24.dp))

                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(16.dp))

                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(24.dp))

                    AuthSubmitBtn(
                        label = if (isLoading) "Logging in..." else "Login",
                        onSubmit = { if (!isLoading) onLogin() },
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(40.dp))

                    TextButton(
                        onClick = {
                            if (!isLoading) {
                                navController.navigate("register") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text("Don't have an account? Register", color = Color.White)
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = goldColor)
                    }
                }
            }
        }
    }
}
