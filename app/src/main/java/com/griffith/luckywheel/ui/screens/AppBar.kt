package com.griffith.luckywheel.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    navController: NavHostController,
    title: String = "Lucky Wheel"
) {
    val context = LocalContext.current
    val soundEffectService = remember { SoundEffectService(context) }
    
    // Cleanup sound service on dispose
    DisposableEffect(Unit) {
        onDispose {
            soundEffectService.release()
        }
    }
    
    val canGoBack = navController.previousBackStackEntry != null
    val currentScreen = navController.currentBackStackEntryAsState().value?.destination?.route

    TopAppBar(
        title = {

            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    title,
                    fontSize = 24.sp,
                    fontFamily = BubbleFontFamily,
                    color = Color(0xFFFFD700),
                )
            }
        },
        navigationIcon = {
            if (canGoBack) {
                IconButton(
                    modifier = Modifier.width(30.dp),
                    onClick = { 
                        soundEffectService.playBubbleClickSound()
                        navController.popBackStack() 
                    }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft, // your back arrow drawable
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            } else {
                // Empty placeholder to satisfy type
                Box(
                    modifier = Modifier.width(30.dp),
                )
            }
        },
        actions = {

            if ((currentScreen != "settings")) {
                IconButton(
                    onClick = { 
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("settings") {
                            launchSingleTop = true
                        }
                    }
                ) {
                    Image(
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(R.drawable.icon_game_control),
                        contentDescription = "Settings button",
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(20.dp)
            )

        },
        // colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}

