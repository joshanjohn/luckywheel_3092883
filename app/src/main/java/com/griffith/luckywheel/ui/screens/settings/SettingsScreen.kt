package com.griffith.luckywheel.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.domerrors.NamespaceError
import androidx.navigation.NavHostController
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.lightRedColor
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
                title = "Game Settings",
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
            // keep logout functionality
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        val firebaseService = FireBaseService()
                        firebaseService.logout()
                        coroutineScope.launch {
                            dataStoreService.clear()
                            navController.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = lightRedColor),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    Text(
                        text = "Logout",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { innerPadding ->
        SettingsList(innerPadding)
    }
}

@Composable
fun SettingsList(innerPadding: PaddingValues) {
    val items = listOf(
        "LOAD GAME" to { onLoadGameClick() },
        "SAVED GAME" to { onSavedGameClick() },
        "GOLD RANK" to { onGoldRankClick() },
        "GOLD GAME" to { onGoldGameClick() }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(items.size) { index ->
            val (label, onClick) = items[index]
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable { onClick() },
                horizontalAlignment = Alignment.Start,
            ) {

                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))

                HorizontalDivider(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(0.5.dp),
                    color = Color.White.copy(alpha = 0.3f)
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// Click Handlers
fun onLoadGameClick() { println("Load Game Clicked!") }
fun onSavedGameClick() { println("Saved Game Clicked!") }
fun onGoldRankClick() { println("Gold Rank Clicked!") }
fun onGoldGameClick() { println("Gold Game Clicked!") }
