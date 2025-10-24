package com.griffith.luckywheel.screens.playground.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
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
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    "GOLD SHAKE",
                    fontSize = 24.sp,
                    fontFamily = BubbleFontFamily,
                    fontWeight = FontWeight.Bold,
                    color =Color(0xFFFFD700),
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
    )
}
