package com.griffith.luckywheel.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.screens.settings.components.SettingsChoiceButton
import com.griffith.luckywheel.ui.theme.DarkerGreenColor
import com.griffith.luckywheel.ui.theme.darkerRedColor
import com.griffith.luckywheel.ui.theme.LightGreenColor
import com.griffith.luckywheel.ui.theme.lightRedColor
import androidx.navigation.NavHostController
import com.griffith.luckywheel.screens.AppBar
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.theme.backgroundColor
import kotlinx.coroutines.launch

@Composable

fun SettingsScreen(
    navController: NavHostController
) {

    val dataStoreService = remember { DataStoreService(navController.context) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = backgroundColor,
        topBar = { AppBar(navController) },
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {


            SettingsChoiceButton(
                onClick = {},
                label = "Settings",
                lightColor = LightGreenColor,
                darkerColor = DarkerGreenColor
            )
        }
    }
}