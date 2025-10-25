package com.griffith.luckywheel.views.screens.leaderboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.screens.AppBar
import java.util.Locale

// --- Custom Colors (Matching the original requested design) ---
val DarkPurpleStart = Color(0xFF1A103A)
val DarkPurpleEnd = Color(0xFF2C1C5B)
val SelectedTabPink = Color(0xFFE95D70)
val UnselectedTabGray = Color(0xFF3A2D6C)
val GemColor = Color(0xFF4DFFBF)
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFC0C0C0)
val Bronze = Color(0xFFCD7F32)

// --- Data Class and Dummy Data ---

data class LeaderboardEntry(
    val id: Int,
    val name: String,
    val score: Int,
    val rank: Int,
    val avatarRes: ImageVector = Icons.Default.Person
)

val leaderboardData = listOf(
    LeaderboardEntry(1, "Jordyn Kenter", 96239, 1),
    LeaderboardEntry(2, "Alena Bator", 84787, 2),
    LeaderboardEntry(3, "Carl Oliver", 82139, 3),
    LeaderboardEntry(4, "Davis Curtis", 80857, 4),
    LeaderboardEntry(5, "Isona Othid", 76128, 5),
    LeaderboardEntry(6, "Makenna George", 71667, 6),
    LeaderboardEntry(7, "Kianna Batista", 68439, 7),
    LeaderboardEntry(8, "Maxith Cullep", 66981, 8),
    LeaderboardEntry(9, "Zain Dias", 65000, 9),
)

// --- Main Screen Composable (Completed) ---

@Composable
fun LeaderBoardScreen(
    navController: NavHostController
) {
    Scaffold(
        // Set the background gradient for the main content area
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "Leaderboard",
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
                .padding(horizontal = 16.dp), // Adjust padding to match design
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {


            Spacer(modifier = Modifier.height(40.dp))


            // 3. "Top Ranking" Header
            RankingHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Leaderboard List
            LeaderboardList(entries = leaderboardData)
        }
    }
}

// ------------------------------------------------------------------
// --- Helper Composable Functions (Reused from previous response) ---
// ------------------------------------------------------------------

@Composable
fun LeaderboardTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Logo",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "Leaderboard",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}


@Composable
fun RankingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Top Ranking",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun LeaderboardList(entries: List<LeaderboardEntry>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            LeaderboardItem(entry = entry)
        }
    }
}

@Composable
fun LeaderboardItem(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Avatar (Using a placeholder Icon)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = entry.avatarRes,
                contentDescription = "Avatar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // 2. Name and Score
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = entry.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Score",
                    tint = GemColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%,d", entry.score),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // 3. Rank Badge
        RankBadge(rank = entry.rank)
    }
}

@Composable
fun RankBadge(rank: Int) {
    val (color, icon) = when (rank) {
        1 -> Gold to Icons.Default.Person
        2 -> Silver to Icons.Default.Person
        3 -> Bronze to Icons.Default.Person
        else -> Color.Gray to Icons.Default.Star
    }

    val rankText = "$rank${getRankSuffix(rank)}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.width(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Rank $rank",
            tint = color,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = rankText,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
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