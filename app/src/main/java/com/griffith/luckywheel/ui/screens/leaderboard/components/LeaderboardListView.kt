package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.models.data.PlayerRank
import com.griffith.luckywheel.ui.screens.leaderboard.LeaderboardItem


@Composable
fun LeaderboardListView(entries: List<PlayerRank>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.id }) { entry ->
            LeaderboardItem(entry = entry)
        }
    }
}
