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
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.R
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.services.SoundEffectService
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily
import com.griffith.luckywheel.ui.theme.arcadeGold
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.winGreen
import com.griffith.luckywheel.ui.theme.surfaceDark
import com.griffith.luckywheel.ui.theme.surfaceElevated
import com.griffith.luckywheel.utils.truncateText
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * A bottom sheet for managing custom wheel segments.
 * Users can add, remove, and customize labels, colors, and probabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    wheelItems: List<SpinWheelItem>,
    onUpdateItems: (List<SpinWheelItem>) -> Unit,
    playerId: String?,
    currentGameId: String? = null,
    currentGameName: String? = null,
    onGameSaved: (String, String) -> Unit = { _, _ -> }
) {
    if (!showBottomSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var gameName by remember(currentGameName) { mutableStateOf(currentGameName ?: "") }
    
    val context = LocalContext.current
    val firebaseService = remember { FireBaseService() }
    val soundEffectService = remember { SoundEffectService(context) }

    DisposableEffect(Unit) {
        onDispose { soundEffectService.release() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = surfaceElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Header(
                onSaveClick = {
                    soundEffectService.playClickSound()
                    showSaveDialog = true
                },
                isSaveEnabled = wheelItems.isNotEmpty() && playerId != null
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                wheelItems.forEachIndexed { index, item ->
                    ItemEditor(
                        index = index,
                        item = item,
                        isExpanded = expandedIndex == index,
                        onToggleExpand = {
                            expandedIndex = if (expandedIndex == index) null else index
                        },
                        onRemove = {
                            val updated = wheelItems.toMutableList().apply { removeAt(index) }
                            onUpdateItems(normalizePercentages(updated))
                            if (expandedIndex == index) expandedIndex = null
                        },
                        onItemUpdate = { updatedItem ->
                            val updatedList = wheelItems.mapIndexed { i, old ->
                                if (i == index) updatedItem else old
                            }
                            onUpdateItems(updatedList)
                        },
                        onPercentUpdate = { newPercent ->
                            val updatedList = rebalancePercentages(wheelItems, index, newPercent)
                            onUpdateItems(normalizePercentages(updatedList))
                        },
                        soundEffectService = soundEffectService
                    )
                }

                if (wheelItems.size < 6) {
                    AddSegmentButton(
                        onClick = {
                            soundEffectService.playAddItemSound()
                            val nextCount = wheelItems.size + 1
                            val share = 1f / nextCount
                            val newItem = SpinWheelItem(
                                label = "Item $nextCount",
                                color = generateVibrantColor(),
                                type = SpinActionType.CUSTOM,
                                value = 0,
                                percent = share
                            )
                            val newList = wheelItems.map { it.copy(percent = share) } + newItem
                            onUpdateItems(newList)
                        }
                    )
                } else {
                    MaxItemsNote()
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveGameDialog(
            initialName = gameName,
            isUpdate = currentGameId != null,
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                playerId?.let { pid ->
                    coroutineScope.launch {
                        firebaseService.saveCustomGame(
                            playerId = pid,
                            gameName = name,
                            wheelItems = wheelItems,
                            gameId = currentGameId
                        ).onSuccess { id ->
                            Toast.makeText(context, "Game saved!", Toast.LENGTH_SHORT).show()
                            onGameSaved(id, name)
                            showSaveDialog = false
                            onDismiss()
                        }.onFailure { 
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun Header(onSaveClick: () -> Unit, isSaveEnabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Edit Wheel Items",
            color = Color.White,
            fontFamily = ArcadeFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = onSaveClick,
            enabled = isSaveEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryNeon.copy(alpha = 0.8f),
                disabledContainerColor = primaryNeon.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Save", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.icons_save_game),
                    contentDescription = null,
                    tint = arcadeGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ItemEditor(
    index: Int,
    item: SpinWheelItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onRemove: () -> Unit,
    onItemUpdate: (SpinWheelItem) -> Unit,
    onPercentUpdate: (Float) -> Unit,
    soundEffectService: SoundEffectService
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = surfaceDark.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(item.color))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = truncateText(item.label, 15),
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Clear else Icons.Default.Edit,
                            contentDescription = null,
                            tint = arcadeGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    TextButton(onClick = onRemove) {
                        Text("Remove", color = Color.Red.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }

            if (isExpanded) {
                ItemControls(
                    item = item,
                    onItemUpdate = onItemUpdate,
                    onPercentUpdate = onPercentUpdate,
                    soundEffectService = soundEffectService
                )
            }
        }
    }
}

@Composable
private fun ItemControls(
    item: SpinWheelItem,
    onItemUpdate: (SpinWheelItem) -> Unit,
    onPercentUpdate: (Float) -> Unit,
    soundEffectService: SoundEffectService
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        OutlinedTextField(
            value = item.label,
            onValueChange = { if (it.length <= 20) onItemUpdate(item.copy(label = it)) },
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF0B3A24),
                unfocusedContainerColor = Color(0xFF0B3A24)
            )
        )

        Spacer(Modifier.height(16.dp))
        
        ColorPicker(
            selectedColor = item.color,
            onColorSelect = { onItemUpdate(item.copy(color = it)) },
            onShuffle = { soundEffectService.playClickSound() }
        )

        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Probability: ${(item.percent * 100).toInt()}%",
            color = Color.White,
            fontSize = 12.sp
        )
        Slider(
            value = item.percent,
            onValueChange = onPercentUpdate,
            colors = SliderDefaults.colors(
                thumbColor = arcadeGold,
                activeTrackColor = primaryNeon
            )
        )
    }
}

@Composable
private fun ColorPicker(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit,
    onShuffle: () -> Unit
) {
    var colors by remember { mutableStateOf(List(5) { generateVibrantColor() }) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 2.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onColorSelect(color) }
                )
            }
        }
        
        IconButton(
            onClick = {
                colors = List(5) { generateVibrantColor() }
                onShuffle()
            },
            modifier = Modifier
                .background(surfaceDark, CircleShape)
                .size(36.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_refresh),
                contentDescription = null,
                tint = arcadeGold,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddSegmentButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
    ) {
        Text("Add New Segment", fontFamily = ArcadeFontFamily, fontSize = 14.sp)
    }
}

@Composable
private fun MaxItemsNote() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Maximum 6 items reached",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = ArcadeFontFamily
        )
    }
}

@Composable
private fun SaveGameDialog(
    initialName: String,
    isUpdate: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isUpdate) "Update Game" else "Save Game", color = Color.White) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Game Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = surfaceDark,
                    unfocusedContainerColor = surfaceDark
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name.trim()) }
            ) {
                Text(if (isUpdate) "Update" else "Save", color = winGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = surfaceElevated
    )
}

/**
 * Normalizes item percentages so they sum up to 1.0.
 */
private fun normalizePercentages(items: List<SpinWheelItem>): List<SpinWheelItem> {
    if (items.isEmpty()) return items
    val sum = items.sumOf { it.percent.toDouble() }.toFloat()
    return if (sum <= 0f) {
        val share = 1f / items.size
        items.map { it.copy(percent = share) }
    } else {
        items.map { it.copy(percent = it.percent / sum) }
    }
}

/**
 * Rebalances percentages when one item is changed, maintaining a sum of 1.0.
 */
private fun rebalancePercentages(
    items: List<SpinWheelItem>,
    targetIndex: Int,
    newPercent: Float
): List<SpinWheelItem> {
    if (items.size <= 1) return items.map { it.copy(percent = 1f) }
    
    val clampedNewPercent = newPercent.coerceIn(0.01f, 0.99f)
    val remaining = 1f - clampedNewPercent
    val otherItemsSum = items
        .filterIndexed { index, _ -> index != targetIndex }
        .sumOf { it.percent.toDouble() }
        .toFloat()
    
    return items.mapIndexed { index, item ->
        if (index == targetIndex) {
            item.copy(percent = clampedNewPercent)
        } else {
            val share = if (otherItemsSum > 0f) item.percent / otherItemsSum else 1f / (items.size - 1)
            item.copy(percent = share * remaining)
        }
    }
}

private fun generateVibrantColor(): Color {
    val channels = listOf(Random.nextInt(100, 255), Random.nextInt(50, 200), 255).shuffled()
    return Color(red = channels[0], green = channels[1], blue = channels[2])
}
