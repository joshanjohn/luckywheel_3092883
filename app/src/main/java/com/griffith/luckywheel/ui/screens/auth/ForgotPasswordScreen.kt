package com.griffith.luckywheel.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.validateEmail
import com.griffith.luckywheel.ui.screens.auth.components.AuthBgWallpaper
import com.griffith.luckywheel.ui.screens.auth.components.AuthSubmitBtn
import com.griffith.luckywheel.ui.screens.auth.components.CustomTextField
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }

    val context = navController.context
    val firebaseService = remember { FireBaseService() }

    fun onResetPassword() {
        email = email.trim()

        val emailError = validateEmail(email)
        if (emailError != null) {
            Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        firebaseService.sendPasswordResetEmail(email) { success, message ->
            isLoading = false
            if (success) {
                emailSent = true
                Toast.makeText(
                    context,
                    "Password reset email sent! Check your inbox.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    message ?: "Failed to send reset email",
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

                    if (!emailSent) {
                        // Initial form
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.White)) {
                                    append("FORGOT YOUR\n\n")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = goldColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("PASSWORD?")
                                }
                            },
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = BubbleFontFamily
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "Enter your email address and we'll send you a link to reset your password.",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(Modifier.height(32.dp))

                        CustomTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            leadingIcon = Icons.Default.Email,
                            enabled = !isLoading
                        )

                        Spacer(Modifier.height(24.dp))

                        AuthSubmitBtn(
                            label = if (isLoading) "Sending..." else "Send Reset Link",
                            onSubmit = { if (!isLoading) onResetPassword() },
                            enabled = !isLoading
                        )

                        Spacer(Modifier.height(40.dp))

                        TextButton(
                            onClick = {
                                if (!isLoading) {
                                    navController.popBackStack()
                                }
                            }
                        ) {
                            Text("Back to Login", color = Color.White)
                        }
                    } else {
                        // Success state
                        Image(
                            painter = painterResource(R.drawable.icon_mail_sent),
                            contentDescription = "icon of mail sent",
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = goldColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append("EMAIL SENT!")
                                }
                            },
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = BubbleFontFamily
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "We've sent a password reset link to:",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = email,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = goldColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(24.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF022C14).copy(alpha = 0.6f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Next Steps:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "1. Check your email inbox",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "2. Click the reset link in the email",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "3. Create a new password",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "4. Return to login with your new password",
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            text = "Didn't receive the email?",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                emailSent = false
                                email = ""
                            }
                        ) {
                            Text("Try Again", color = goldColor)
                        }

                        Spacer(Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                navController.navigate("login") {
                                    popUpTo("forgotpassword") { inclusive = true }
                                }
                            }
                        ) {
                            Text("Back to Login", color = Color.White)
                        }
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