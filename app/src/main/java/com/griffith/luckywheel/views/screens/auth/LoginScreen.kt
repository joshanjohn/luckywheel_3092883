package com.griffith.luckywheel.screens.auth

import android.widget.Toast
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
import com.griffith.luckywheel.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.screens.auth.components.CustomTextField
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.validateEmail
import com.griffith.luckywheel.services.validatePassword
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.GoldColor
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
        val passwordError = validatePassword(password, disable_string_validations = true)
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
            containerColor = Color.Transparent
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                ,
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
                                color = GoldColor,
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

                AuthSubmitBtn(
                    label = "Login",
                    onSubmit = { onLogin() }
                )

                Spacer(Modifier.height(40.dp))

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
