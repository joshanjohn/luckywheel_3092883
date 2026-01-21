package com.griffith.luckywheel.ui.screens.loadgames.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.models.data.toSpinWheelItems
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GameCard(
    game: SavedGame,
    onLoad: () -> Unit,
    onDelete: (SavedGame) -> Unit,
    onRename: (SavedGame, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(game.gameName) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val updatedDate = remember(game.updatedAt) {
        dateFormat.format(Date(game.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)
                ),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF07361D).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.gameName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = goldColor,
                        fontFamily = com.griffith.luckywheel.ui.theme.ArcadeFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Updated: $updatedDate",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Row {
                    IconButton(
                        onClick = { showRenameDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Wheel Preview & Load Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color Preview
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${game.wheelItems.size} items",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.width(8.dp))
                    game.toSpinWheelItems().take(5).forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(item.color)
                        )
                    }
                }

                // Load Button - Premium Arcade Style
                Surface(
                    onClick = onLoad,
                    color = com.griffith.luckywheel.ui.theme.magicGreen,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Load",
                            tint = darkerGreenColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "LOAD", 
                            color = darkerGreenColor,
                            fontFamily = com.griffith.luckywheel.ui.theme.ArcadeFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Game?", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete \"${game.gameName}\"? This action cannot be undone.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(game)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF01150B)
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                newName = game.gameName
            },
            title = { Text("Rename Game", color = Color.White) },
            text = {
                Column {
                    Text("Enter a new name:", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Game Name") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0B3A24),
                            unfocusedContainerColor = Color(0xFF0B3A24),
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank() && newName != game.gameName) {
                            onRename(game, newName.trim())
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("Rename", color = lightGreenColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        newName = game.gameName
                    }
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF01150B)
        )
    }
}