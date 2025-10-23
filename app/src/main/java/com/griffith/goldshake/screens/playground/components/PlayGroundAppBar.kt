package com.griffith.goldshake.screens.playground.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.goldshake.R
import com.griffith.goldshake.services.DataStoreService
import com.griffith.goldshake.services.FireBaseService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayGroundAppBar(
    navController: NavHostController
) {
    val dataStoreService = remember { DataStoreService(navController.context) }
    val coroutineScope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Spin The Wheel!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    val firebaseService = FireBaseService()
                    firebaseService.logout()

                    coroutineScope.launch {
                        dataStoreService.clear()

                        // Navigate back to login screen and clear history
                        navController.navigate("login") {
                            popUpTo(0) // clear back stack
                        }
                    }
                }

            ) {
                Icon(Icons.Default.Close, contentDescription = "Menu", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C273A))
    )
}
