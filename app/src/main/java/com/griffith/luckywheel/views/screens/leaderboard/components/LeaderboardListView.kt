package com.griffith.luckywheel.views.screens.leaderboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.data.PlayerRankModel
import com.griffith.luckywheel.views.screens.leaderboard.LeaderboardItem


@Composable
fun LeaderboardListView(entries: List<PlayerRankModel>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries, key = { it.id }) { entry ->
            LeaderboardItem(entry = entry)
        }
    }
}
