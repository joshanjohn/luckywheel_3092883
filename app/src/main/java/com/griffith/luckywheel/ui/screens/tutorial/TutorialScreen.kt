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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.griffith.luckywheel.R
import com.griffith.luckywheel.constants.TutorialConstants
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlinx.coroutines.launch

// Tutorial screen with carousel-style instructions
@Composable
fun TutorialScreen(
    navController: NavHostController,
    playerId: String? = null
) {
    val context = LocalContext.current
    val soundEffectService = remember { SoundEffectService(context) }
    val pagerState = rememberPagerState(pageCount = { TutorialConstants.TOTAL_PAGES })
    val coroutineScope = rememberCoroutineScope()
    
    // Debug logging
    LaunchedEffect(playerId) {
        Log.d("TutorialScreen", "TutorialScreen loaded with playerId: $playerId")
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = TutorialConstants.SCREEN_TITLE,
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
                repeat(TutorialConstants.TOTAL_PAGES) { index ->
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
                    onClick = { 
                        soundEffectService.playWinSound()
                        navController.popBackStack() 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightGreenColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = TutorialConstants.BUTTON_GET_STARTED,
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
                        soundEffectService.playProgressSound()
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
                        contentDescription = TutorialConstants.BUTTON_PREVIOUS,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Next button (right side)
            if (pagerState.currentPage < 4) {
                IconButton(
                    onClick = {
                        soundEffectService.playProgressSound()
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
                        contentDescription = TutorialConstants.BUTTON_NEXT,
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
    // carousel indication
    val (icon, title, description) = when (page) {
        0 -> Triple(
            R.drawable.icon_rank,
            TutorialConstants.Page0.TITLE,
            TutorialConstants.Page0.DESCRIPTION
        )
        1 -> Triple(
            R.drawable.icon_gold_coin,
            TutorialConstants.Page1.TITLE,
            TutorialConstants.Page1.DESCRIPTION
        )
        2 -> Triple(
            R.drawable.icon_custom_game,
            TutorialConstants.Page2.TITLE,
            TutorialConstants.Page2.DESCRIPTION
        )
        3 -> Triple(
            R.drawable.icon_rank,
            TutorialConstants.Page3.TITLE,
            TutorialConstants.Page3.DESCRIPTION
        )
        4 -> Triple(
            R.drawable.icon_profile,
            TutorialConstants.Page4.TITLE,
            TutorialConstants.Page4.DESCRIPTION
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
