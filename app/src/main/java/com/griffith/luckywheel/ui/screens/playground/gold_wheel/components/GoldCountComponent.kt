package com.griffith.luckywheel.ui.screens.playground.gold_wheel.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.R
import java.util.Locale

@Composable
fun GoldCountComponent(playerName: String, playerGold: Int) {
    // Track previous gold value to detect changes
    var previousGold by remember { mutableIntStateOf(playerGold) }
    var triggerAnimation by remember { mutableStateOf(false) }
    
    // Detect gold change
    LaunchedEffect(playerGold) {
        if (playerGold != previousGold) {
            triggerAnimation = true
            previousGold = playerGold
        }
    }
    
    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (triggerAnimation) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = {
            triggerAnimation = false
        },
        label = "gold_scale_animation"
    )
    
    // Color pulse animation
    val colorAnimation by animateFloatAsState(
        targetValue = if (triggerAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "gold_color_animation"
    )
    
    // Interpolate between gold color and white for flash effect
    val animatedColor = Color(
        red = (0xFF + (0xFF - 0xFF) * colorAnimation) / 255f,
        green = (0xD7 + (0xFF - 0xD7) * colorAnimation) / 255f,
        blue = (0x00 + (0xFF - 0x00) * colorAnimation) / 255f
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.scale(scale)
    ) {
        Image(
            painterResource(R.drawable.icon_gold_coin_icon),
            contentDescription = "gold count icon"
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = String.format(Locale.getDefault(), "%,d", playerGold),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        )
    }
}

