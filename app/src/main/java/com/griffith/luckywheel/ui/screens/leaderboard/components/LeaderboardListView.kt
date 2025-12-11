package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.models.data.PlayerRank
import com.griffith.luckywheel.ui.screens.leaderboard.LeaderboardItem
import kotlin.math.max
import kotlin.math.min

// Leaderboard List View with collapsible podium
@Composable
fun LeaderboardListView(entries: List<PlayerRank>) {
    if (entries.isEmpty()) return
    
    val topThree = entries.take(3)
    val remainingPlayers = entries.drop(3)
    
    // Track scroll state to shrink podium dynamically
    val listState = rememberLazyListState()
    
    // Calculate podium scale based on scroll offset (1.0 to 0.4)
    val scrollOffset = listState.firstVisibleItemScrollOffset.toFloat()
    val maxScrollForShrink = 400f // Pixels to scroll before podium reaches minimum size
    val podiumScale = max(0.4f, min(1f, 1f - (scrollOffset / maxScrollForShrink) * 0.6f))
    
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Podium for top 3 players (shrinks on scroll)
        item {
            PodiumDisplay(
                topPlayers = topThree,
                scale = podiumScale
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Remaining players (rank 4+) - no animations for better performance
        if (remainingPlayers.isNotEmpty()) {
            itemsIndexed(remainingPlayers, key = { _, item -> item.id }) { _, entry ->
                LeaderboardItem(entry = entry)
            }
        }
    }
}
