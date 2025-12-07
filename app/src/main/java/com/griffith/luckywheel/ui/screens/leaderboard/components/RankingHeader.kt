package com.griffith.luckywheel.ui.screens.leaderboard.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import  com.griffith.luckywheel.R

// Ranking Header component
@Composable
fun RankingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )

        Image(
            painter = painterResource(R.drawable.icon_rank),
            contentDescription = null,
        )

        Text(
            text = "Top Ranking",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Image(
            painter = painterResource(R.drawable.icon_rank),
            contentDescription = null,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .padding(horizontal = 8.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
    }
}