package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.models.data.PlayerRank
import com.griffith.luckywheel.ui.screens.leaderboard.LeaderboardItem
import kotlinx.coroutines.delay

// Leaderboard List View with podium separation and animations
@Composable
fun LeaderboardListView(entries: List<PlayerRank>) {
    Column {
        // Podium for top 3 players
        if (entries.isNotEmpty()) {
            val topThree = entries.take(3)
            PodiumDisplay(topPlayers = topThree)
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Remaining players (rank 4+)
        val remainingPlayers = entries.drop(3)
        
        if (remainingPlayers.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(remainingPlayers, key = { _, item -> item.id }) { index, entry ->
                    // Staggered fade-in animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 30L) // 30ms stagger between items
                        visible = true
                    }
                    
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 })
                    ) {
                        LeaderboardItem(entry = entry)
                    }
                }
            }
        }
    }
}
