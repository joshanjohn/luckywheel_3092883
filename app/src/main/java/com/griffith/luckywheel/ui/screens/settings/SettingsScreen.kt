package com.griffith.luckywheel.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.settings.components.GameModeButton
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController
) {
    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val authService = remember { AuthenticationService(context) }
    val coroutineScope = rememberCoroutineScope()

    var playerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val player = dataStoreService.getPlayer()
        playerId = player.playerId.takeIf { it.isNotEmpty() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "Settings",
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF033E14),
                        Color(0xFF01150B),
                        Color(0xFF01150B)
                    )
                )
            ),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gold Game Card
                GameModeButton(
                    title = "GOLD WHEEL",
                    description = "Spin to win gold coins",
                    icon = R.drawable.icon_gold_coin,
                    gradientColors = listOf(Color(0xFF07361D), Color(0xFF0BA136), Color(0xFF07361D)),
                    borderColor = lightGreenColor,
                    onClick = {
                        playerId?.let { id ->
                            navController.navigate("play/$id") {
                                popUpTo("settings") { inclusive = true }
                            }
                        }
                    }
                )

                // Custom Wheel Card
                GameModeButton(
                    title = "CUSTOM WHEEL",
                    description = "Create your own wheel",
                    icon = R.drawable.icon_custom_game,
                    borderColor = goldColor,
                    onClick = {
                        playerId?.let { id ->
                            // Navigate to custom wheel via saved state
                            navController.navigate("play/$id") {
                                popUpTo("settings") { inclusive = true }
                            }
                            // Set a flag to navigate to custom wheel tab
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("navigate_to_custom", true)
                        }
                    }
                )

                // Load Games Card
                GameModeButton (
                    title = "LOAD GAME",
                    description = "Continue saved games",
                    icon = R.drawable.icon_load_game,
                    borderColor = Color(0xFF49A84D),
                    onClick = {
                        playerId?.let { id ->
                            navController.navigate("loadgames/$id")
                        }
                    }
                )

                // Profile Card
                GameModeButton(
                    title = "PROFILE",
                    description = "Manage your account",
                    icon = R.drawable.icon_profile,
                    borderColor = Color(0xFF9C27B0),
                    onClick = {
                        playerId?.let { id ->
                            navController.navigate("profile/$id")
                        }
                    }
                )
            }

            // Logout Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable {
                            // Logout from both Firebase and Google Sign-In
                            authService.logout {
                                coroutineScope.launch {
                                    dataStoreService.clear()
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF450303), Color(0xFF8B0000), Color(0xFF450303))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, Color(0xFFD50505).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.icon_game_control),
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "LOGOUT",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = BubbleFontFamily
                            )
                        }
                    }
                }
            }
        }
    }
}

