package com.griffith.luckywheel.ui.screens.playground.custom_wheel.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import com.griffith.luckywheel.utils.truncateText
import kotlinx.coroutines.launch
import kotlin.random.Random


// Bottom sheet for editing custom wheel items - add, remove, and customize segments
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    wheelItems: List<SpinWheelItem>, // Current wheel configuration
    onUpdateItems: (List<SpinWheelItem>) -> Unit, // Callback when items change
    playerId: String?,
    currentGameId: String? = null, // If editing existing game
    currentGameName: String? = null,
    onGameSaved: (String, String) -> Unit = { _, _ -> } // Callback with gameId and gameName
) {
    if (!showBottomSheet) return // Don't render if not visible

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var expandedIndex by remember { mutableStateOf<Int?>(null) } // Track which item is being edited
    var showSaveDialog by remember { mutableStateOf(false) }
    var gameName by remember(currentGameName) { mutableStateOf(currentGameName ?: "") }
    val context = LocalContext.current
    val firebaseService = remember { FireBaseService() }
    val soundEffectService = remember { SoundEffectService(context) }
    
    // Clean up sound service when sheet is closed
    DisposableEffect(Unit) {
        onDispose {
            soundEffectService.release()
        }
    }

    // Redistributes percentages when one item changes - keeps total at 100%
    fun rebalanceItemPercentagesAfterChange(
        items: List<SpinWheelItem>,
        updatedIndex: Int, // Which item was changed
        newPercent: Float // New percentage for that item
    ): List<SpinWheelItem> {
        val clampedPercent = newPercent.coerceIn(0f, 1f) // Ensure 0-100%
        if (items.isEmpty()) return items
        if (items.size == 1) return listOf(items[0].copy(percent = 1f)) // Single item gets 100%

        val remainingPercent = (1f - clampedPercent).coerceAtLeast(0f) // What's left for other items
        val otherItems = items.filterIndexed { i, _ -> i != updatedIndex }
        val totalOtherPercent = otherItems.sumOf { it.percent.toDouble() }.toFloat()

        return if (totalOtherPercent <= 0f) {
            // If other items have no percentage, distribute equally
            val equalShare = remainingPercent / otherItems.size
            items.mapIndexed { i, item ->
                if (i == updatedIndex) item.copy(percent = clampedPercent)
                else item.copy(percent = equalShare)
            }
        } else {
            // Redistribute remaining percentage proportionally based on current ratios
            items.mapIndexed { i, item ->
                if (i == updatedIndex) item.copy(percent = clampedPercent)
                else item.copy(percent = (item.percent / totalOtherPercent) * remainingPercent)
            }
        }
    }

    // Ensures all percentages add up to exactly 100% (1.0)
    fun normalizePercentagesToOne(items: List<SpinWheelItem>): List<SpinWheelItem> {
        if (items.isEmpty()) return items
        val total = items.sumOf { it.percent.toDouble() }.toFloat()
        return if (total <= 0f) {
            // If total is 0, distribute equally
            val equal = 1f / items.size.coerceAtLeast(1)
            items.map { it.copy(percent = equal) }
        } else {
            // Scale all percentages proportionally to sum to 1.0
            items.map { it.copy(percent = it.percent / total) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF022C14)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Wheel Items",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = darkerGreenColor),
                    onClick = { 
                        soundEffectService.playClickSound()
                        showSaveDialog = true 
                    },
                    enabled = wheelItems.isNotEmpty() && playerId != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(R.drawable.icons_save_game),
                            contentDescription = "Save Game",
                            modifier = Modifier.size(18.dp),
                            tint = if (wheelItems.isNotEmpty() && playerId != null) goldColor else Color.Gray
                        )
                    }

                }
            }

            Spacer(Modifier.height(8.dp))

            //  Expandable Cards List
            wheelItems.forEachIndexed { index, item ->
                // Temporary state for editing - only applied when "Save Changes" is clicked
                var tempLabel by remember(item) { mutableStateOf(item.label) }
                var tempColor by remember(item) { mutableStateOf(item.color) }
                var tempPercent by remember(item) { mutableStateOf(item.percent.coerceIn(0f, 1f)) }

                // Generate 5 random vibrant colors for color picker
                var colorfulSuggestions by remember(item) {
                    mutableStateOf(
                        List(5) {
                            // Generate vibrant colors by maxing out at least one RGB channel
                            val channels = listOf(
                                Random.nextInt(50, 201), // Mid-range
                                Random.nextInt(50, 201), // Mid-range
                                255 // Always have one channel at max for vibrancy
                            ).shuffled() // Randomize which channel is maxed
                            Color(
                                red = channels[0],
                                green = channels[1],
                                blue = channels[2]
                            )
                        }
                    )
                }

                // Regenerate color palette with new random colors
                fun regenerateColors() {
                    colorfulSuggestions = List(5) {
                        val channels = listOf(
                            Random.nextInt(50, 201),
                            Random.nextInt(50, 201),
                            255
                        ).shuffled()
                        Color(
                            red = channels[0],
                            green = channels[1],
                            blue = channels[2]
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF07361D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(tempColor)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = truncateText(item.label, maxLength = 12),
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row {
                                // Toggle edit mode for this item
                                IconButton(onClick = {
                                    expandedIndex = if (expandedIndex == index) null else index
                                    if (expandedIndex == index) {
                                        // Reset temp values when opening editor
                                        tempLabel = item.label
                                        tempColor = item.color
                                        tempPercent = item.percent.coerceIn(0f, 1f)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (expandedIndex == index)
                                            Icons.Default.Clear else Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = goldColor
                                    )
                                }

                                // Remove this item from wheel
                                TextButton(onClick = {
                                    val updatedItems =
                                        wheelItems.toMutableList().apply { removeAt(index) }
                                    onUpdateItems(normalizePercentagesToOne(updatedItems)) // Rebalance after removal
                                    expandedIndex = null
                                }) {
                                    Text("Remove", color = Color.Red)
                                }
                            }
                        }

                        // Expanded editor section - only visible when item is being edited
                        if (expandedIndex == index) {
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = tempLabel,
                                onValueChange = { newValue ->
                                    // Limit input to 20 characters max
                                    if (newValue.length <= 20) {
                                        tempLabel = newValue
                                    }
                                },
                                label = { Text("Label (max 20 chars)") },
                                singleLine = true,
                                supportingText = {
                                    Text(
                                        "${tempLabel.length}/20",
                                        color = if (tempLabel.length > 12) Color(0xFFFFD700) else Color.Gray
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF0B3A24),
                                    unfocusedContainerColor = Color(0xFF0B3A24),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(12.dp))
                            Text("Pick Color", color = Color.White)

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color palette circles
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    colorfulSuggestions.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    if (tempColor == color) 2.dp else 1.dp,
                                                    if (tempColor == color) Color.White else Color.Gray,
                                                    CircleShape
                                                )
                                                .clickable { tempColor = color }
                                        )
                                    }
                                }
                                
                                // Random/Shuffle button
                                IconButton(
                                    onClick = {
                                        soundEffectService.playClickSound()
                                        regenerateColors()
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0B3A24))
                                        .border(1.dp, goldColor, CircleShape)
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.icon_refresh),
                                        contentDescription = "Shuffle Colors",
                                        tint = goldColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            val percentDisplay = (tempPercent * 100).toInt() // Convert 0-1 to 0-100 for display
                            Text("Set Percentage: $percentDisplay%", color = Color.White)

                            Slider(
                                value = tempPercent,
                                onValueChange = { tempPercent = it.coerceIn(0f, 1f) },
                                valueRange = 0f..1f,
                                steps = 99, // 100 discrete steps (0%, 1%, 2%, ... 100%)
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = lightGreenColor
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Apply changes to this item
                            Button(
                                onClick = {
                                    // Rebalance percentages and update label/color for this item
                                    val updatedList = rebalanceItemPercentagesAfterChange(
                                        wheelItems, index, tempPercent
                                    ).mapIndexed { i, wheelItem ->
                                        if (i == index)
                                            wheelItem.copy(label = tempLabel, color = tempColor)
                                        else wheelItem
                                    }

                                    onUpdateItems(normalizePercentagesToOne(updatedList))
                                    expandedIndex = null // Close editor
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = darkerGreenColor)
                            ) {
                                Text("Save Changes", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Add new item to wheel (max 6 items)
            Button(
                onClick = {
                    // Calculate equal percentage for all items including the new one
                    val totalItems = wheelItems.size + 1
                    val equalPercent = 1f / totalItems
                    
                    val newItem = SpinWheelItem(
                        label = "New Item ${wheelItems.size + 1}",
                        color = Color(
                            red = Random.nextInt(0, 255),
                            green = Random.nextInt(0, 255),
                            blue = Random.nextInt(0, 255)
                        ),
                        type = SpinActionType.CUSTOM,
                        value = 0,
                        percent = equalPercent
                    )
                    // Set all items to equal percentage
                    val addedList = (wheelItems.map { it.copy(percent = equalPercent) } + newItem)
                    onUpdateItems(addedList)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (wheelItems.size >= 6) Color.Gray else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = wheelItems.size < 6 // Limit to 6 items for readability
            ) {
                Text(
                    text = if (wheelItems.size >= 6) "Maximum only 6 Items Only" else "Add Item",
                    color = if (wheelItems.size >= 6) Color.White else darkerGreenColor
                )
            }
        }
    }

    // Save Game Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(if (currentGameId != null) "Update Game" else "Save Game", color = Color.White) },
            text = {
                Column {
                    Text("Enter a name for this game:", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gameName,
                        onValueChange = { gameName = it },
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
                        soundEffectService.playClickSound()
                        if (gameName.isBlank()) {
                            Toast.makeText(context, "Please enter a game name", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        playerId?.let { pid ->
                            coroutineScope.launch {
                                // Save or update game in Firebase
                                val result = firebaseService.saveCustomGame(
                                    playerId = pid,
                                    gameName = gameName.trim(),
                                    wheelItems = wheelItems,
                                    gameId = currentGameId // If null, creates new game; otherwise updates existing
                                )
                                
                                result.onSuccess { gameId ->
                                    Toast.makeText(
                                        context,
                                        if (currentGameId != null) "Game updated!" else "Game saved!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onGameSaved(gameId, gameName.trim())
                                    showSaveDialog = false
                                    onDismiss()
                                }.onFailure { exception ->
                                    Toast.makeText(
                                        context,
                                        "Failed to save game: ${exception.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text(if (currentGameId != null) "Update" else "Save", color = lightGreenColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    soundEffectService.playClickSound()
                    showSaveDialog = false 
                }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF01150B)
        )
    }
}