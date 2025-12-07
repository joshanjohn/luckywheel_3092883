package com.griffith.luckywheel.ui.screens.loadgames

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.screens.loadgames.components.GameCard
import com.griffith.luckywheel.ui.theme.MeriendaFontFamily
import kotlinx.coroutines.launch

@Composable
fun LoadGamesScreen(
    navController: NavHostController,
    playerId: String?
) {
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()
    var savedGames by remember { mutableStateOf<List<SavedGame>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(playerId) {
        if (playerId != null) {
            android.util.Log.d("LoadGamesScreen", "Loading games for player: $playerId")
            val result = firebaseService.getPlayerGames(playerId)
            
            result.onSuccess { games ->
                android.util.Log.d("LoadGamesScreen", "Successfully loaded ${games.size} games")
                savedGames = games
                isLoading = false
            }.onFailure { exception ->
                android.util.Log.e("LoadGamesScreen", "Failed to load games", exception)
                isLoading = false
                Toast.makeText(
                    navController.context,
                    "Failed to load games: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            android.util.Log.w("LoadGamesScreen", "No player ID provided")
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                navController = navController,
                title = "Load Game",
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
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF20B652)
                    )
                }
                savedGames.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Saved Games",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = MeriendaFontFamily
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Create and save your first custom wheel game!",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(savedGames, key = { it.gameId }) { game ->
                            GameCard(
                                game = game,
                                onLoad = {
                                    // Navigate to PlayGround with the loaded game
                                    navController.navigate("play/$playerId") {
                                        popUpTo("loadgames/{playerId}") { inclusive = true }
                                    }
                                    // Set the loaded game and flag to show custom wheel
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("loaded_game", game)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("navigate_to_custom", true)
                                },
                                onDelete = { gameToDelete ->
                                    coroutineScope.launch {
                                        val result = firebaseService.deleteGame(gameToDelete.gameId)
                                        
                                        result.onSuccess {
                                            savedGames = savedGames.filter { it.gameId != gameToDelete.gameId }
                                            Toast.makeText(
                                                navController.context,
                                                "Game deleted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure { exception ->
                                            Toast.makeText(
                                                navController.context,
                                                "Failed to delete: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onRename = { gameToRename, newName ->
                                    coroutineScope.launch {
                                        val result = firebaseService.updateGameName(gameToRename.gameId, newName)
                                        
                                        result.onSuccess {
                                            savedGames = savedGames.map {
                                                if (it.gameId == gameToRename.gameId) {
                                                    it.copy(gameName = newName)
                                                } else it
                                            }
                                            Toast.makeText(
                                                navController.context,
                                                "Game renamed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }.onFailure { exception ->
                                            Toast.makeText(
                                                navController.context,
                                                "Failed to rename: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}