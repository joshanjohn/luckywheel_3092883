package com.griffith.luckywheel.screens.custom_wheel.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.screens.gold_wheel.model.SpinActionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    wheelItems: List<SpinWheelItem>,
    onUpdateItems: (List<SpinWheelItem>) -> Unit
) {
    if (!showBottomSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF022C14)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Edit Wheel Items", color = Color.White, fontWeight = FontWeight.Bold)

            // --- Item Rows ---
            wheelItems.forEachIndexed { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingIndex == index) {
                        TextField(
                            value = editingText,
                            onValueChange = { editingText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedContainerColor = Color.DarkGray,
                                unfocusedContainerColor = Color.DarkGray,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                        TextButton(onClick = {
                            val updated = wheelItems.toMutableList().apply {
                                this[index] = this[index].copy(label = editingText)
                            }
                            onUpdateItems(updated)
                            editingIndex = null
                        }) {
                            Text("Save", color = Color.Green)
                        }
                    } else {
                        Text(item.label, color = Color.White)
                        Row {
                            IconButton(onClick = {
                                editingIndex = index
                                editingText = item.label
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Item",
                                    tint = Color.Yellow
                                )
                            }
                            TextButton(onClick = {
                                val updated = wheelItems.toMutableList().apply {
                                    removeAt(index)
                                }
                                onUpdateItems(updated)
                            }) {
                                Text("Remove", color = Color.Red)
                            }
                        }
                    }
                }
            }

            // --- Add New Item Button ---
            Button(
                onClick = {
                    val newItem = SpinWheelItem(
                        label = "New Item ${wheelItems.size + 1}",
                        color = Color(
                            listOf(
                                0xFF4CAF50, 0xFFFFC107, 0xFF2196F3, 0xFFFF5722, 0xFF9C27B0
                            ).random()
                        ),
                        type = SpinActionType.GAIN_GOLD,
                        value = 0,
                        percent = 0f
                    )
                    onUpdateItems(wheelItems + newItem)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Item")
            }
        }
    }
}
