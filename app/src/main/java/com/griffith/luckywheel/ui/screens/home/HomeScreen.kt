package com.griffith.luckywheel.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.BungeeFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.magicGreen
import com.griffith.luckywheel.ui.theme.neonLime

@Composable
fun HomeScreen(navController: NavHostController, playerId: String?) {
    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            MagicalFAB(
                onClick = { 
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
            val screenHeight = maxHeight
            val screenWidth = maxWidth
            val isSmallScreen = screenHeight < 700.dp
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = if (isSmallScreen) 24.dp else 40.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Section - More compact
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = if (isSmallScreen) 16.dp else 24.dp)
                ) {
                    Text(
                        text = "LUCKY WHEEL",
                        fontSize = if (isSmallScreen) 32.sp else 42.sp,
                        fontFamily = BungeeFontFamily,
                        color = arcadeGold,
                        letterSpacing = (-1.5).sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .height(2.dp)
                            .width(if (isSmallScreen) 120.dp else 160.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, neonLime, Color.Transparent)
                                )
                            )
                    )

                    Text(
                        text = "CHOOSE YOUR ARCADE",
                        fontSize = if (isSmallScreen) 12.sp else 14.sp,
                        fontFamily = ArcadeFontFamily,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                // Game Selection Cards - Fixed height, better spacing
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 16.dp else 20.dp)
                ) {
                    GameCard(
                        title = "GOLD ARCADE",
                        subtitle = "Spin to win big prizes!",
                        gradient = listOf(Color(0xFF1B5E20).copy(alpha = 0.8f), magicGreen.copy(alpha = 0.6f)),
                        accentColor = neonLime,
                        iconId = R.drawable.icon_trophy,
                        isSmallScreen = isSmallScreen,
                        onClick = {
                            navController.navigate("goldwheel/$playerId")
                        }
                    )

                    GameCard(
                        title = "CUSTOM WHEEL",
                        subtitle = "Design your own wheel",
                        gradient = listOf(Color(0xFF4A148C).copy(alpha = 0.8f), Color(0xFF8E44AD).copy(alpha = 0.6f)),
                        accentColor = Color(0xFFD488FF),
                        iconId = R.drawable.icon_game_control,
                        isSmallScreen = isSmallScreen,
                        onClick = {
                            navController.navigate("custompi/$playerId")
                        }
                    )
                }
                
                // Bottom spacer for balance
                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
            }
        }
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
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 12.dp,
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 140.dp else 160.dp) // Fixed height instead of heightIn
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(24.dp), ambientColor = accentColor, spotColor = accentColor)
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
            // Inner Glow/Glow effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(23.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isSmallScreen) 16.dp else 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = if (isSmallScreen) 22.sp else 26.sp,
                        fontFamily = BungeeFontFamily,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = subtitle,
                        fontSize = if (isSmallScreen) 10.sp else 12.sp,
                        fontFamily = ArcadeFontFamily,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(Modifier.height(if (isSmallScreen) 6.dp else 8.dp))
                    
                    // Simple "PLAY NOW" pill
                    Box(
                        modifier = Modifier
                            .background(accentColor, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "PLAY NOW", 
                            fontSize = 9.sp, 
                            fontFamily = ArcadeFontFamily, 
                            color = Color.Black, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier.size(if (isSmallScreen) 50.dp else 64.dp).alpha(0.5f),
                    tint = accentColor
                )
            }
            
            // Subtle "Shine" Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun MagicalFAB(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Pulse Effect
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .background(arcadeGold.copy(alpha = 0.3f), CircleShape)
        )
        
        FloatingActionButton(
            onClick = onClick,
            containerColor = arcadeGold,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(28.dp))
        }
    }
}
