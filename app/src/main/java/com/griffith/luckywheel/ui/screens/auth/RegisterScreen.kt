package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.common.SignInButton
import com.griffith.luckywheel.models.data.Player
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
fun RegisterScreen(navController: NavHostController) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        firebaseService.handleGoogleSignInResult(result.data) { success, message, userId ->
            isLoading = false
            if (success && userId != null) {
                firebaseService.getPlayerInfo(userId) { player ->
                    if (player != null) {
                        coroutineScope.launch {
                            dataStoreService.clear()
                            dataStoreService.savePlayer(player)

                            Toast.makeText(context, message ?: "Google sign in successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("play/$userId") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to load player data", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, message ?: "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onGoogleSignIn() {
        if (!isLoading) {
            val signInIntent = firebaseService.getGoogleSignInIntent(context)
            googleSignInLauncher.launch(signInIntent)
        }
    }

    fun onRegister() {
        name = name.trim()
        email = email.trim()
        password = password.trim()

        val emailError = validateEmail(email)
        if (emailError != null) {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
            return
        }

        val passwordError = validatePassword(password)
        if (passwordError != null) {
            Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        firebaseService.registerUserWithPlayer(name, email, password) { success, message ->
            isLoading = false
            if (success) {
                val userId = firebaseService.getCurrentUserId()
                if (userId != null) {
                    firebaseService.getPlayerInfoById(userId) { player ->
                        if (player != null) {
                            coroutineScope.launch {
                                dataStoreService.clear()
                                dataStoreService.savePlayer(player)

                                val prefs: Player = dataStoreService.getPlayer()
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
                                append("SHAKE & EARN\n\n")
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
                        value = name,
                        onValueChange = { name = it },
                        label = "Name",
                        leadingIcon = Icons.Default.Person,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(16.dp))

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

                    Spacer(Modifier.height(8.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (!isLoading) {
                                    navController.navigate("forgotpassword")
                                }
                            }
                        ) {
                            Text(
                                "Forgot Password?",
                                color = goldColor,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AuthSubmitBtn(
                        onSubmit = { if (!isLoading) onRegister() },
                        label = if (isLoading) "Registering..." else "Register",
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(24.dp))

                    // Divider with "OR"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.3f)
                        )
                        Text(
                            text = " OR ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Google Sign-In Button
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            SignInButton(context).apply {
                                setSize(SignInButton.SIZE_WIDE)
                                setOnClickListener {
                                    onGoogleSignIn()
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(40.dp))

                    TextButton(
                        onClick = {
                            if (!isLoading) {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text("Already have an account? Login", color = Color.White)
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