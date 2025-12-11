package com.griffith.luckywheel.ui.screens.leaderboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.PlayerRank
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.leaderboard.components.LeaderboardListView
import com.griffith.luckywheel.ui.screens.leaderboard.components.RankingHeader
import com.griffith.luckywheel.ui.theme.bronzeColor
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.silverColor
import com.griffith.luckywheel.utils.formatNumberCompact




@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    playerId: String? = null
) {
    val firebaseService = remember { FireBaseService() }

    // Holds the latest leaderboard data from Firebase
    var leaderboardList by remember { mutableStateOf<List<PlayerRank>>(emptyList()) }

    // Stream player updates in real time
    LaunchedEffect(Unit) {
        firebaseService.getPlayerRanking(
            onPlayersUpdated = { players ->
                // Map Player objects to PlayerRank with rank positions
                leaderboardList = players.mapIndexed { index, player ->
                    PlayerRank(
                        id = index,
                        name = player.playerName,
                        score = player.gold,
                        rank = index + 1
                    )
                }
            },
            onError = { e ->
                println("Error streaming players: ${e.message}")
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "Leaderboard",
                playerId = playerId
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            RankingHeader()
            Spacer(modifier = Modifier.height(16.dp))

            LeaderboardListView(entries = leaderboardList)
        }
    }
}



@Composable
fun LeaderboardItem(entry: PlayerRank) {
    // Determine if this is a top-tier player (4-10)
    val isTopTier = entry.rank in 4..10
    
    val borderColor = if (isTopTier) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF4CAF50).copy(alpha = 0.6f),
                Color(0xFF8BC34A).copy(alpha = 0.6f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.1f),
                Color.White.copy(alpha = 0.1f)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B3A24).copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTopTier) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(borderColor)
                .padding(2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF0B3A24),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Rank number
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${entry.rank}",
                        color = if (isTopTier) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF2E7D32),
                                    Color(0xFF1B5E20)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = entry.playerIcon,
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Player name & score
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = entry.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.icon_gold_coin),
                            contentDescription = "Score",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatNumberCompact(entry.score),
                            color = goldColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Rank suffix badge
                Text(
                    text = getRankSuffix(entry.rank),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Composable
fun RankBadge(rank: Int) {
    val (color, imgIcon) = when (rank) {
        1 -> goldColor to R.drawable.icon_gold_wreath
        2 -> silverColor to R.drawable.icon_silver_wreath
        3 -> bronzeColor to R.drawable.icon_bronze_wreath
        else -> Color.Transparent to R.drawable.icon_player_circle // replace with a generic image for others
    }

    val rankText = "$rank${getRankSuffix(rank)}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.width(48.dp)
    ) {
        Image(
            painter = painterResource(imgIcon),
            contentDescription = "Rank $rank",
            modifier = Modifier
                .size(55.dp)
                .background(color.copy(alpha = 0.4f), shape = CircleShape) // light tint
        )
        Text(
            text = rankText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}
fun getRankSuffix(rank: Int): String {
    return when {
        rank % 100 in 11..13 -> "th"
        rank % 10 == 1 -> "st"
        rank % 10 == 2 -> "nd"
        rank % 10 == 3 -> "rd"
        else -> "th"
    }
}
