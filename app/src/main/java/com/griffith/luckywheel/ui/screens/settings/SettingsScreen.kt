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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import android.content.Intent
import android.net.Uri
import com.griffith.luckywheel.R
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.services.BackgroundMusicService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.settings.components.GameModeButton
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val authService = remember { AuthenticationService(context) }
    val coroutineScope = rememberCoroutineScope()

    val musicService = remember { BackgroundMusicService.getInstance(context) }
    val soundEffectService = remember { SoundEffectService(context) }
    
    // Music state
    val musicVolume by dataStoreService.getMusicVolume().collectAsState(initial = 0.5f)
    val musicMuted by dataStoreService.getMusicMuted().collectAsState(initial = false)
    var volumeSlider by remember { mutableFloatStateOf(0.5f) }
    
    // Sound effects state
    val soundEffectsVolume by dataStoreService.getSoundEffectsVolume().collectAsState(initial = 0.7f)
    val soundEffectsMuted by dataStoreService.getSoundEffectsMuted().collectAsState(initial = false)
    var soundEffectsVolumeSlider by remember { mutableFloatStateOf(0.7f) }
    
    // Initialize volume sliders
    LaunchedEffect(musicVolume) {
        volumeSlider = musicVolume
    }
    
    LaunchedEffect(soundEffectsVolume) {
        soundEffectsVolumeSlider = soundEffectsVolume
    }

    // Cleanup sound service on dispose
    DisposableEffect(Unit) {
        onDispose {
            soundEffectService.release()
        }
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
                        soundEffectService.playBubbleClickSound()
                        playerId?.let { id ->
                            navController.navigate("play/$id") {
                                launchSingleTop = true // avoid the screen being re-created on top of stack
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
                        soundEffectService.playBubbleClickSound()
                        playerId?.let { id ->
                            // First navigate to the play screen
                            navController.navigate("play/$id") {
                                launchSingleTop = true
                            }
                            // Then set the flag on the destination's saved state
                            navController.getBackStackEntry("play/$id")
                                .savedStateHandle
                                .set("navigate_to_custom", true)
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
                        soundEffectService.playBubbleClickSound()
                        playerId?.let { id ->
                            navController.navigate("loadgames/$id") {
                                launchSingleTop = true
                            }
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
                        soundEffectService.playBubbleClickSound()
                        playerId?.let { id ->
                            navController.navigate("profile/$id") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
                
                // How to Play Card
                GameModeButton(
                    title = "HOW TO PLAY",
                    description = "Learn how to use the app",
                    icon = R.drawable.icon_how_to_play,
                    borderColor = Color(0xFF49A84D),
                    gradientColors = listOf(Color(0xFF07361D), Color(0xFF0BA136), Color(0xFF07361D)),
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("tutorial") {
                            launchSingleTop = true
                        }
                    }
                )
                
                // Privacy Policy Card
                GameModeButton(
                    title = "PRIVACY POLICY",
                    description = "View app privacy policies",
                    icon = R.drawable.icon_profile,
                    borderColor = Color(0xFF2196F3),
                    gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF0D47A1)),
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.privacypolicies.com/live/be6e6547-82a4-43fd-bd06-c7b669b4c117"))
                        context.startActivity(intent)
                    }
                )
                
                // Music Controls Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0A2818)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Music Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BACKGROUND MUSIC",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                fontFamily = BubbleFontFamily
                            )
                            // Mute Toggle
                            Button(
                                onClick = {
                                    soundEffectService.playClickSound()
                                    coroutineScope.launch {
                                        musicService.setMuted(!musicMuted)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (musicMuted) Color.Gray else lightGreenColor
                                )
                            ) {
                                Text(
                                    text = if (musicMuted) "UNMUTE" else "MUTE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Volume Slider
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Volume: ${(volumeSlider * 100).toInt()}%",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = volumeSlider,
                                onValueChange = { newValue ->
                                    volumeSlider = newValue
                                },
                                onValueChangeFinished = {
                                    coroutineScope.launch {
                                        musicService.setVolume(volumeSlider)
                                    }
                                },
                                enabled = !musicMuted,
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = lightGreenColor,
                                    inactiveTrackColor = Color.Gray,
                                    disabledThumbColor = Color.Gray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }
            
            // Sound Effects Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sound Effects Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOUND EFFECTS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor,
                                fontFamily = BubbleFontFamily
                            )
                            // Mute Toggle
                            Button(
                                onClick = {
                                    soundEffectService.playClickSound()
                                    coroutineScope.launch {
                                        soundEffectService.setMuted(!soundEffectsMuted)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (soundEffectsMuted) Color.Gray else lightGreenColor
                                )
                            ) {
                                Text(
                                    text = if (soundEffectsMuted) "UNMUTE" else "MUTE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Volume Slider
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Volume: ${(soundEffectsVolumeSlider * 100).toInt()}%",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = soundEffectsVolumeSlider,
                                onValueChange = { newValue ->
                                    soundEffectsVolumeSlider = newValue
                                },
                                onValueChangeFinished = {
                                    coroutineScope.launch {
                                        soundEffectService.setVolume(soundEffectsVolumeSlider)
                                    }
                                },
                                enabled = !soundEffectsMuted,
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = lightGreenColor,
                                    inactiveTrackColor = Color.Gray,
                                    disabledThumbColor = Color.Gray,
                                    disabledActiveTrackColor = Color.DarkGray
                                )
                            )
                        }
                    }
                }
            }

            // Logout Button
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
                            soundEffectService.playClickSound()
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

