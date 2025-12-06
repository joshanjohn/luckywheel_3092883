package com.griffith.luckywheel.ui.screens.playground.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.griffith.luckywheel.models.data.BottomNavItem
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.theme.extraDarkerGreenColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlin.collections.forEach


@Composable
fun BottomNavBar(navController: NavHostController, items: List<BottomNavItem>) {
    val context = LocalContext.current
    val soundEffectService = remember { SoundEffectService(context) }
    
    // Cleanup sound service on dispose
    DisposableEffect(Unit) {
        onDispose {
            soundEffectService.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.Black)
                .border(1.dp, Color(0xFF292929), RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination.isRouteSelected(item.route)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) extraDarkerGreenColor else Color.Transparent)
                            .clickable {
                                if (!selected) {
                                    soundEffectService.playBubbleClickSound()
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (selected) lightGreenColor else Color.White
                            )
                            Text(
                                text = item.label,
                                color = if (selected) lightGreenColor else Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun NavDestination?.isRouteSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}