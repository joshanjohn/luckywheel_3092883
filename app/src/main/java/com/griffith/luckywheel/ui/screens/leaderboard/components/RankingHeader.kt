package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.R

// Modern Ranking Header with gradient text and animations
@Composable
fun RankingHeader() {
    // Pulse animation for trophy icons
    val infiniteTransition = rememberInfiniteTransition(label = "trophy_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left decorative line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .padding(horizontal = 8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color(0xFFFFD700).copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Left trophy icon
            Image(
                painter = painterResource(R.drawable.icon_rank),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title with gradient effect (simulated with shadow)
            Text(
                text = "Top Ranking",
                color = Color(0xFFFFD700),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Right trophy icon
            Image(
                painter = painterResource(R.drawable.icon_rank),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .scale(scale)
            )

            // Right decorative line
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .padding(horizontal = 8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.8f),
                                Color(0xFFFFD700).copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Hall of Champions",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}