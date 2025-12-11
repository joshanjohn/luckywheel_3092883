package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.PlayerRank
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.utils.formatNumberCompact

// Podium display for top 3 players with circular badge design
@Composable
fun PodiumDisplay(
    topPlayers: List<PlayerRank>,
    scale: Float = 1f // Scale factor for dynamic sizing (0.4 to 1.0)
) {
    if (topPlayers.isEmpty()) return

    // Arrange as: 2nd, 1st, 3rd (classic podium layout)
    val first = topPlayers.getOrNull(0)
    val second = topPlayers.getOrNull(1)
    val third = topPlayers.getOrNull(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top: 1st place (center, largest)
        first?.let { player ->
            PodiumPlayerBadge(
                player = player,
                rank = 1,
                size = (120 * scale).dp,
                animationDelay = 0,
                scale = scale
            )
        }

        Spacer(modifier = Modifier.height((24 * scale).dp))

        // Bottom row: 2nd and 3rd (side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            // 2nd place (left)
            second?.let { player ->
                PodiumPlayerBadge(
                    player = player,
                    rank = 2,
                    size = (100 * scale).dp,
                    animationDelay = 100,
                    scale = scale
                )
            }

            // 3rd place (right)
            third?.let { player ->
                PodiumPlayerBadge(
                    player = player,
                    rank = 3,
                    size = (90 * scale).dp,
                    animationDelay = 200,
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun PodiumPlayerBadge(
    player: PlayerRank,
    rank: Int,
    size: Dp,
    animationDelay: Int,
    scale: Float = 1f
) {
    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    val animScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "podium_scale"
    )

    // Colors based on rank
    val (gradientColors, medalIcon) = when (rank) {
        1 -> Pair(
            listOf(Color(0xFFFFD700), Color(0xFFFFA500)),
            R.drawable.icon_gold_badge
        )
        2 -> Pair(
            listOf(Color(0xFFC0C0C0), Color(0xFFA8A8A8)),
            R.drawable.icon_silver_badge
        )
        3 -> Pair(
            listOf(Color(0xFFCD7F32), Color(0xFFB87333)),
            R.drawable.icon_bronze_badge
        )
        else -> Pair(
            listOf(Color.Gray, Color.DarkGray),
            R.drawable.icon_player_circle
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(animScale)
            .width(size + (40 * scale).dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow effect behind avatar
            Box(
                modifier = Modifier
                    .size(size + (16 * scale).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gradientColors[0].copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Main circular avatar
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                gradientColors[0].copy(alpha = 0.6f),
                                gradientColors[1].copy(alpha = 0.8f),
                                Color(0xFF0B3A24)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Player",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.5f)
                )
            }

            // Crown for 1st place (floating above)
            if (rank == 1) {
                Image(
                    painter = painterResource(R.drawable.icon_rank),
                    contentDescription = "Crown",
                    modifier = Modifier
                        .size((36 * scale).dp)
                        .offset(y = -(size / 2 + (8 * scale).dp))
                )
            }

            // Medal badge (bottom right corner)
            Image(
                painter = painterResource(medalIcon),
                contentDescription = "Medal",
                modifier = Modifier
                    .size(if (rank == 1) (64 * scale).dp else (52 * scale).dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (8 * scale).dp, y = (8 * scale).dp)
            )
        }

        Spacer(modifier = Modifier.height((12 * scale).dp))

        // Player name
        Text(
            text = player.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (rank == 1) (18 * scale).sp else (16 * scale).sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height((4 * scale).dp))

        // Score with coin icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.icon_gold_coin),
                contentDescription = "Gold",
                modifier = Modifier.size(if (rank == 1) (20 * scale).dp else (18 * scale).dp)
            )
            Spacer(modifier = Modifier.width((4 * scale).dp))
            Text(
                text = formatNumberCompact(player.score),
                color = goldColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (rank == 1) (18 * scale).sp else (16 * scale).sp
            )
        }
    }
}
