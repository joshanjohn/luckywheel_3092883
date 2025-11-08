package com.griffith.luckywheel.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.darkerRedColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import com.griffith.luckywheel.ui.theme.lightRedColor
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.settings.components.SettingsChoiceButton
import kotlinx.coroutines.launch

@Composable

fun SettingsScreen(
    navController: NavHostController
) {

    val dataStoreService = remember { DataStoreService(navController.context) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "Leaderboard",
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
        bottomBar = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                SettingsChoiceButton(
                    label = "logout",
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
                    },
                    lightColor = lightRedColor,
                    darkerColor = darkerRedColor
                )
            }
        }
        //containerColor = Color.Transparent
    ) { innerPadding ->


        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start,
        ) {


            SettingsChoiceButton(
                onClick = {},
                label = "GOLD GAME",
                lightColor = Color(0xFF39DE3E),
                darkerColor = Color(0xFF3F0948)
            )
            Spacer(Modifier.height(18.dp))


            SettingsChoiceButton(
                onClick = {},
                label = "CUSTOM GAME",
                lightColor=Color(0xFFE8FA3A),
                darkerColor = Color(0xFF736708)
            )

            Spacer(Modifier.height(18.dp))



            SettingsChoiceButton(
                onClick = {},
                label = "LOAD GAME",
                lightColor = lightGreenColor,
                darkerColor = darkerGreenColor
            )
        }
    }
}