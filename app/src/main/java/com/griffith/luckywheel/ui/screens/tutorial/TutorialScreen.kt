package com.griffith.luckywheel.ui.screens.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlinx.coroutines.launch

// Tutorial screen with carousel-style instructions
@Composable
fun TutorialScreen(navController: NavHostController) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "How to Play"
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
            )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Tutorial Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                TutorialPage(page = page)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Page Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .background(
                                color = if (pagerState.currentPage == index) goldColor else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    if (index < 4) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Get Started Button (only on last page)
            if (pagerState.currentPage == 4) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightGreenColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Get Started",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = BubbleFontFamily
                    )
                }
            }
        }

        // Arcade-style navigation buttons (on sides)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Previous button (left side)
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    goldColor.copy(alpha = 0.9f),
                                    goldColor.copy(alpha = 0.7f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_back),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Next button (right side)
            if (pagerState.currentPage < 4) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    goldColor.copy(alpha = 0.9f),
                                    goldColor.copy(alpha = 0.7f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_next),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorialPage(page: Int) {
    val (icon, title, description) = when (page) {
        0 -> Triple(
            R.drawable.icon_rank,
            "Welcome to Lucky Wheel!",
            "Spin the wheel to win gold coins, compete on the leaderboard, and create your own custom wheels. Let's learn how to play!"
        )
        1 -> Triple(
            R.drawable.icon_gold_coin,
            "Gold Wheel",
            "Tap and hold the spin button, then shake your phone to spin the wheel! Win gold coins based on where the wheel stops. The more you play, the more you earn!"
        )
        2 -> Triple(
            R.drawable.icon_custom_game,
            "Custom Wheel",
            "Create your own wheel with custom items! Tap the wheel or Edit button to add items, change colors, and adjust percentages. Save your wheels and load them anytime!"
        )
        3 -> Triple(
            R.drawable.icon_rank,
            "Leaderboard",
            "Compete with players worldwide! Earn gold coins to climb the rankings. The top 3 players get special badges. Check your rank and see how you compare!"
        )
        4 -> Triple(
            R.drawable.icon_profile,
            "Profile & Settings",
            "Manage your account, adjust music and sound effects volume, and access all game modes from the settings screen. You're all set to play!"
        )
        else -> Triple(R.drawable.icon_rank, "", "")
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0B3A24).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                goldColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = title,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor,
                textAlign = TextAlign.Center,
                fontFamily = BubbleFontFamily
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                text = description,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }
    }
}
