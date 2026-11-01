package com.griffith.luckywheel.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.settings.components.GameModeButton
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.surfaceDark
import com.griffith.luckywheel.ui.theme.surfaceCard
import com.griffith.luckywheel.ui.theme.winGreen
import com.griffith.luckywheel.ui.theme.loseRed
import com.griffith.luckywheel.ui.theme.silverColor
import com.griffith.luckywheel.ui.theme.bronzeColor
import kotlinx.coroutines.launch

/**
 * Settings Screen of the application.
 * Contains user settings, sound configurations, and secondary game mode launch triggers.
 */
@Composable
fun SettingsScreen(
    navController: NavHostController,
    playerId: String?
) {
    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val soundEffectService = remember { SoundEffectService(context) }
    val coroutineScope = rememberCoroutineScope()

    // Single flow — one DataStore disk read instead of five
    val settings by dataStoreService.getAppSettings()
        .collectAsState(initial = com.griffith.luckywheel.services.AppSettings())
    val musicVolume = settings.musicVolume
    val musicMuted = settings.musicMuted
    val soundEffectsVolume = settings.soundEffectsVolume
    val soundEffectsMuted = settings.soundEffectsMuted
    val sensorSpinEnabled = settings.sensorSpinEnabled

    DisposableEffect(Unit) {
        onDispose { soundEffectService.release() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { AppBar(navController = navController, title = "Settings") },
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(surfaceDark, Color(0xFF060A18), surfaceDark)
                )
            ),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Navigation Options
            NavigationGrid(navController, playerId, soundEffectService, context)

            // Spin Mode Toggle
            SpinModeCard(
                sensorEnabled = sensorSpinEnabled,
                onToggle = {
                    soundEffectService.playClickSound()
                    coroutineScope.launch { dataStoreService.saveSensorSpinEnabled(!sensorSpinEnabled) }
                }
            )

            // Audio Settings
            AudioSettingsCard(
                title = "BACKGROUND MUSIC",
                volume = musicVolume,
                isMuted = musicMuted,
                onVolumeChange = { coroutineScope.launch { dataStoreService.saveMusicVolume(it) } },
                onToggleMute = {
                    soundEffectService.playClickSound()
                    coroutineScope.launch { dataStoreService.saveMusicMuted(!musicMuted) }
                }
            )

            AudioSettingsCard(
                title = "SOUND EFFECTS",
                volume = soundEffectsVolume,
                isMuted = soundEffectsMuted,
                onVolumeChange = { coroutineScope.launch { dataStoreService.saveSoundEffectsVolume(it) } },
                onToggleMute = {
                    soundEffectService.playClickSound()
                    coroutineScope.launch { dataStoreService.saveSoundEffectsMuted(!soundEffectsMuted) }
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NavigationGrid(
    navController: NavHostController,
    playerId: String?,
    soundEffectService: SoundEffectService,
    context: android.content.Context
) {
    val navigate: (String) -> Unit = { route ->
        soundEffectService.playBubbleClickSound()
        playerId?.let { id ->
            navController.navigate(route.replace("{playerId}", id)) { launchSingleTop = true }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GameModeButton(
            title = "GOLD WHEEL",
            description = "Spin to win gold coins",
            icon = R.drawable.icon_gold_coin,
            borderColor = arcadeGold,
            onClick = { navigate("goldwheel/{playerId}") }
        )

        GameModeButton(
            title = "CUSTOM WHEEL",
            description = "Create your own wheel",
            icon = R.drawable.icon_custom_game,
            borderColor = primaryNeon,
            onClick = { navigate("custompi/{playerId}") }
        )

        GameModeButton(
            title = "LOAD GAME",
            description = "Continue saved games",
            icon = R.drawable.icon_load_game,
            borderColor = winGreen,
            onClick = { navigate("loadgames/{playerId}") }
        )

        GameModeButton(
            title = "PROFILE",
            description = "Manage your account",
            icon = R.drawable.icon_profile,
            borderColor = accentMagenta,
            onClick = { navigate("profile/{playerId}") }
        )

        GameModeButton(
            title = "HOW TO PLAY",
            description = "Learn how to use the app",
            icon = R.drawable.icon_how_to_play,
            borderColor = arcadeGold,
            onClick = { navigate("tutorial?playerId={playerId}") }
        )

        GameModeButton(
            title = "PRIVACY POLICY",
            description = "View app privacy policies",
            icon = R.drawable.icon_profile,
            borderColor = primaryNeon,
            gradientColors = listOf(surfaceCard, Color(0xFF0F172A), surfaceCard),
            onClick = {
                soundEffectService.playBubbleClickSound()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.privacypolicies.com/live/be6e6547-82a4-43fd-bd06-c7b669b4c117"))
                context.startActivity(intent)
            }
        )

        // ── Luck Games section ────────────────────────────────────────
        Spacer(Modifier.height(4.dp))
        Text(
            text = "LUCK GAMES",
            fontSize = 13.sp,
            fontFamily = ArcadeFontFamily,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        )

        GameModeButton(
            title = "COIN FLIP",
            description = "Heads or Tails? Best of 3",
            icon = R.drawable.icon_gold_coin,
            borderColor = arcadeGold,
            gradientColors = listOf(surfaceCard, Color(0xFF1E1E2E), surfaceCard),
            onClick = { navigate("coinflip/{playerId}") }
        )

        GameModeButton(
            title = "DICE ROLLER",
            description = "Roll up to 6 dice at once",
            icon = R.drawable.icon_game_control,
            borderColor = primaryNeon,
            gradientColors = listOf(surfaceCard, Color(0xFF1E1E2E), surfaceCard),
            onClick = { navigate("diceroller/{playerId}") }
        )

        GameModeButton(
            title = "MAGIC 8-BALL",
            description = "Ask the oracle your fate",
            icon = R.drawable.icon_rank,
            borderColor = accentMagenta,
            gradientColors = listOf(surfaceCard, Color(0xFF1E1E2E), surfaceCard),
            onClick = { navigate("magicball/{playerId}") }
        )

        GameModeButton(
            title = "SLOT MACHINE",
            description = "Spin the reels for a jackpot",
            icon = R.drawable.icon_trophy,
            borderColor = loseRed,
            gradientColors = listOf(surfaceCard, Color(0xFF1E1E2E), surfaceCard),
            onClick = { navigate("slotmachine/{playerId}") }
        )
    }
}

@Composable
private fun SpinModeCard(
    sensorEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPIN MODE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = arcadeGold,
                        fontFamily = BubbleFontFamily
                    )
                    Text(
                        text = if (sensorEnabled) "📳  Shake your phone to spin the wheel"
                               else "👆  Tap the button to spin at random speed",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = ArcadeFontFamily
                    )
                }
                Switch(
                    checked = sensorEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = winGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }

            // Mode badge
            Surface(
                color = if (sensorEnabled) winGreen.copy(alpha = 0.15f)
                        else arcadeGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (sensorEnabled) "SHAKE TO SPIN  ●  ACTIVE" else "TAP TO SPIN  ●  ACTIVE",
                    fontSize = 11.sp,
                    fontFamily = ArcadeFontFamily,
                    color = if (sensorEnabled) winGreen else arcadeGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioSettingsCard(
    title: String,
    volume: Float,
    isMuted: Boolean,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = arcadeGold,
                    fontFamily = BubbleFontFamily
                )
                
                Button(
                    onClick = onToggleMute,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) Color.Gray else primaryNeon
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isMuted) "UNMUTE" else "MUTE", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMuted) Color.White else Color.Black
                    )
                }
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                enabled = !isMuted,
                colors = SliderDefaults.colors(
                    thumbColor = arcadeGold,
                    activeTrackColor = primaryNeon,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}
