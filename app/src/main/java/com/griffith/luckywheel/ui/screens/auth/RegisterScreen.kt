package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.griffith.luckywheel.data.Player
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.validateEmail
import com.griffith.luckywheel.services.validatePassword
import com.griffith.luckywheel.ui.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.ui.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.ui.screens.auth.components.CustomTextField
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.GoldColor
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

                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                                navController.navigate("play/${prefs.playerId}") {
                                    popUpTo("register") { inclusive = true }
                                }
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
            containerColor = Color.Transparent
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                            append("SHAKE & EARN\n\n")
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

                AuthSubmitBtn(
                    onSubmit = { onRegister() },
                    label = "Register",
                )


                Spacer(Modifier.height(40.dp))

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
