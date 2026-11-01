package com.griffith.luckywheel.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.BungeeFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.surfaceDark
import com.griffith.luckywheel.ui.theme.surfaceCard

/**
 * Main dashboard screen of the application.
 * Shows main games (Gold Arcade, Custom Wheel) and divider-separated mini luck games.
 * Features neon glowing card designs and high premium details.
 */
@Composable
fun HomeScreen(navController: NavHostController, playerId: String?) {
    val context = LocalContext.current
    val soundEffectService = remember { SoundEffectService(context) }

    DisposableEffect(Unit) {
        onDispose { soundEffectService.release() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalFAB(
                onClick = {
                    soundEffectService.playClickSound()
                    playerId?.let { navController.navigate("settings/$it") }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isSmallScreen = maxHeight < 700.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = if (isSmallScreen) 32.dp else 60.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 16.dp else 24.dp)
            ) {
                // Header
                HeaderSection(isSmallScreen)

                // ── Main games ────────────────────────────────────────────
                GameCard(
                    title = "GOLD ARCADE",
                    subtitle = "Spin to win big prizes!",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = arcadeGold,
                    iconId = R.drawable.icon_trophy,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("goldwheel/$playerId")
                    }
                )

                GameCard(
                    title = "CUSTOM WHEEL",
                    subtitle = "Design your own wheel",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = primaryNeon,
                    iconId = R.drawable.icon_custom_game,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("custompi/$playerId")
                    }
                )

                // ── Luck Arcade divider ────────────────────────────────────
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.15f))
                    Text(
                        "LUCK ARCADE",
                        fontSize = 11.sp, fontFamily = ArcadeFontFamily,
                        color = arcadeGold.copy(0.8f), letterSpacing = 2.sp
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(0.15f))
                }

                // ── Luck mini-games ───────────────────────────────────────
                GameCard(
                    title = "COIN FLIP",
                    subtitle = "Heads or Tails? Best of 3!",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = arcadeGold,
                    iconId = R.drawable.icon_gold_coin,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("coinflip/$playerId")
                    }
                )

                GameCard(
                    title = "DICE ROLLER",
                    subtitle = "Roll up to 6 dice at once",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = primaryNeon,
                    iconId = R.drawable.icon_game_control,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("diceroller/$playerId")
                    }
                )

                GameCard(
                    title = "MAGIC 8-BALL",
                    subtitle = "Ask the oracle your fate",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = accentMagenta,
                    iconId = R.drawable.icon_rank,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("magicball/$playerId")
                    }
                )

                GameCard(
                    title = "SLOT MACHINE",
                    subtitle = "Spin the reels for a jackpot",
                    gradient = listOf(surfaceCard, surfaceDark.copy(alpha = 0.8f)),
                    accentColor = Color(0xFFFF3366),
                    iconId = R.drawable.icon_trophy,
                    isSmallScreen = isSmallScreen,
                    onClick = {
                        soundEffectService.playBubbleClickSound()
                        navController.navigate("slotmachine/$playerId")
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(isSmallScreen: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "LUCKY WHEEL",
            fontSize = if (isSmallScreen) 36.sp else 48.sp,
            fontFamily = BungeeFontFamily,
            color = arcadeGold,
            letterSpacing = (-1).sp,
            textAlign = TextAlign.Center
        )
        
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .height(3.dp)
                .width(if (isSmallScreen) 140.dp else 180.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, primaryNeon, Color.Transparent)
                    )
                )
        )

        Text(
            text = "CHOOSE YOUR ARCADE",
            fontSize = if (isSmallScreen) 12.sp else 14.sp,
            fontFamily = ArcadeFontFamily,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun GameCard(
    title: String,
    subtitle: String,
    gradient: List<Color>,
    accentColor: Color,
    iconId: Int,
    isSmallScreen: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 130.dp else 145.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = accentColor.copy(alpha = 0.5f),
                spotColor = accentColor.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .border(1.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = if (isSmallScreen) 20.sp else 24.sp,
                        fontFamily = BungeeFontFamily,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = subtitle,
                        fontSize = if (isSmallScreen) 10.sp else 12.sp,
                        fontFamily = ArcadeFontFamily,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Surface(
                        color = accentColor,
                        shape = CircleShape,
                        modifier = Modifier.height(24.dp)
                    ) {
                        Box(Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "PLAY", 
                                fontSize = 10.sp, 
                                fontFamily = ArcadeFontFamily, 
                                color = Color.Black, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).alpha(0.8f),
                    tint = accentColor
                )
            }
        }
    }
}

@Composable
fun MagicalFAB(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(primaryNeon.copy(alpha = 0.25f), CircleShape)
        )
        
        FloatingActionButton(
            onClick = onClick,
            containerColor = primaryNeon,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(28.dp))
        }
    }
}
