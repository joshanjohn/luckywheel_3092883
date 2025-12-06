package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.utils.validateEmail
import com.griffith.luckywheel.utils.validatePassword
import com.griffith.luckywheel.ui.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.ui.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.ui.screens.auth.components.CustomTextField
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = navController.context
    val authService = remember { AuthenticationService(context) }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        authService.handleGoogleSignInResult(result.data) { success, message, userId ->
            isLoading = false
            if (success && userId != null) {
                Toast.makeText(context, message ?: "Google sign in successful", Toast.LENGTH_SHORT).show()
                navController.navigate("play/$userId") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                Toast.makeText(context, message ?: "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onGoogleSignIn() {
        if (!isLoading) {
            // Launch Google Sign-In intent to show default account picker
            val signInIntent = authService.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }
    }

    fun onLogin() {
        email = email.trim()
        password = password.trim()

        val emailError = validateEmail(email)
        if (emailError != null) {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
            return
        }

        val passwordError = validatePassword(password, disableStringValidations = true)
        if (passwordError != null) {
            Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        authService.loginWithEmailPassword(email, password) { success, message, userId ->
            isLoading = false
            if (success && userId != null) {
                navController.navigate("play/$userId") {
                    popUpTo("login") { inclusive = true }
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

                    Spacer(Modifier.height(8.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                if (!isLoading) {
                                    navController.navigate("forgotpassword") {
                                        launchSingleTop = true
                                    }
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
                        label = if (isLoading) "Logging in..." else "Login",
                        onSubmit = { if (!isLoading) onLogin() },
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

                    // Custom Google Sign-In Button
                    Button(
                        onClick = { onGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        ),
                        enabled = !isLoading
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.icon_google_logo),
                                contentDescription = "Google logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                        }
                    }

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