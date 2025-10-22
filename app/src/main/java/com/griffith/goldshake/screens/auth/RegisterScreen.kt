package com.griffith.goldshake.screens.auth

import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.griffith.goldshake.screens.auth.components.CustomTextField
import com.griffith.goldshake.R
import com.griffith.goldshake.services.FireBaseService


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController
) {

    var name by remember { mutableStateOf<String>("") }
    var email by remember { mutableStateOf<String>("") }
    var password by remember { mutableStateOf<String>("") }

    fun onRegister(): Unit {
        val firebaseService = FireBaseService()

        firebaseService.registerUserWithPlayer(name, email, password, ) { success, message ->
            if (success) {
                Toast.makeText(navController.context, "Success", Toast.LENGTH_SHORT)
                    .show()
                navController.navigate("play") {
                    popUpTo("register") { inclusive = true }
                }
            } else {
                Toast.makeText(
                    navController.context,
                    message ?: "Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6A11CB), // Start (Purple)
                        Color(0xFF2575FC)  // End (Blue)
                    )
                )
            )
    ) {


        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background Overlay",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f) // Adjust this for blend strength (0.0–1.0)
        )

        Image(
            painter = painterResource(id = R.drawable.bgco),
            contentDescription = "Background Overlay",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f) // Adjust this for blend strength (0.0–1.0)
        )


        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Register", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                    onClick = {
                        onRegister()
                    }
                ) {
                    Text("Register")
                }

            }
        }
    }


}