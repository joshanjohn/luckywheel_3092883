package com.griffith.luckywheel.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.services.AuthenticationService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.screens.AppBar
import com.griffith.luckywheel.ui.theme.BubbleFontFamily
import com.griffith.luckywheel.ui.theme.goldColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, playerId: String?) {
    val context = navController.context
    val dataStoreService = remember { DataStoreService(context) }
    val authService = remember { AuthenticationService(context) }
    val firebaseService = remember { FireBaseService() }
    val coroutineScope = rememberCoroutineScope()

    var player by remember { mutableStateOf<Player?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Real-time Firebase listener for player updates
    DisposableEffect(playerId) {
        if (playerId.isNullOrBlank()) {
            onDispose {}
        } else {
            val listener = firebaseService.listenToPlayerUpdates(playerId) { updatedPlayer ->
                updatedPlayer?.let {
                    player = it
                    if (!isEditing) {
                        editedName = it.playerName
                    }
                    // Update DataStore with latest data
                    coroutineScope.launch {
                        dataStoreService.savePlayer(it)
                    }
                }
            }
            onDispose {
                firebaseService.removePlayerListener(playerId, listener)
            }
        }
    }

    fun savePlayerName() {
        val currentPlayer = player ?: return
        if (editedName.isBlank()) {
            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedPlayer = currentPlayer.copy(playerName = editedName)
        firebaseService.updatePlayerInfo(currentPlayer.playerId, updatedPlayer) { success ->
            if (success) {
                coroutineScope.launch {
                    dataStoreService.savePlayer(updatedPlayer)
                    player = updatedPlayer
                    isEditing = false
                    Toast.makeText(context, "Name updated successfully", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to update name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteAccount() {
        val currentPlayer = player ?: return
        isDeleting = true

        // First delete Firebase Auth account
        authService.deleteAccount { authSuccess, authMessage ->
            if (authSuccess) {
                // Then delete all player data from database
                firebaseService.deleteAllPlayerData(currentPlayer.playerId) { dbSuccess, dbMessage ->
                    isDeleting = false
                    coroutineScope.launch {
                        // Clear local data store
                        dataStoreService.clear()
                        
                        if (dbSuccess) {
                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            // Auth account is deleted, but database cleanup failed
                            Toast.makeText(context, "Account deleted, but some data cleanup failed", Toast.LENGTH_LONG).show()
                        }
                        
                        // Navigate to login screen regardless of database deletion result
                        // since the auth account is already deleted
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            } else {
                isDeleting = false
                Toast.makeText(context, authMessage ?: "Failed to delete account", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                navController = navController,
                title = "Profile"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF07361D).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = goldColor,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = editedName,
                                onValueChange = { editedName = it },
                                label = { Text("Player Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = goldColor,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    focusedLabelColor = goldColor,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editedName = player?.playerName ?: ""
                                        isEditing = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = { savePlayerName() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = goldColor,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text("Save", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = player?.playerName ?: "Loading...",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = BubbleFontFamily
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Gold: ${player?.gold ?: 0}",
                                fontSize = 18.sp,
                                color = goldColor,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { isEditing = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = goldColor,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Edit Name", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Delete Account Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF450303),
                                        Color(0xFF8B0000),
                                        Color(0xFF450303)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(2.dp, Color(0xFFD50505).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "DELETE ACCOUNT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = BubbleFontFamily
                            )
                        }
                    }
                }

                Text(
                    text = "Warning: This action cannot be undone. All your data will be permanently deleted.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = goldColor)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This will permanently delete:\n\n• Your profile\n• All your saved games\n• All your progress\n\nThis action cannot be undone!",
                    color = Color.White.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD50505)
                    )
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
