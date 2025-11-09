package com.griffith.luckywheel.ui.screens.playground.custom_wheel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.ui.screens.playground.enums.SpinActionType
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.goldColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import kotlin.random.Random

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
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    
    fun rebalanceItemPercentagesAfterChange(
        items: List<SpinWheelItem>,
        updatedIndex: Int,
        newPercent: Float
    ): List<SpinWheelItem> {
        val clampedPercent = newPercent.coerceIn(0f, 1f)
        if (items.isEmpty()) return items
        if (items.size == 1) return listOf(items[0].copy(percent = 1f))

        val remainingPercent = (1f - clampedPercent).coerceAtLeast(0f)
        val otherItems = items.filterIndexed { i, _ -> i != updatedIndex }
        val totalOtherPercent = otherItems.sumOf { it.percent.toDouble() }.toFloat()

        return if (totalOtherPercent <= 0f) {
            // if others had no share, divide remaining equally
            val equalShare = remainingPercent / otherItems.size
            items.mapIndexed { i, item ->
                if (i == updatedIndex) item.copy(percent = clampedPercent)
                else item.copy(percent = equalShare)
            }
        } else {
            // redistribute equaly
            items.mapIndexed { i, item ->
                if (i == updatedIndex) item.copy(percent = clampedPercent)
                else item.copy(percent = (item.percent / totalOtherPercent) * remainingPercent)
            }
        }
    }


    fun normalizePercentagesToOne(items: List<SpinWheelItem>): List<SpinWheelItem> {
        if (items.isEmpty()) return items
        val total = items.sumOf { it.percent.toDouble() }.toFloat()
        return if (total <= 0f) {
            val equal = 1f / items.size.coerceAtLeast(1)
            items.map { it.copy(percent = equal) }
        } else {
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
        ) {
            Text(
                "Edit Wheel Items",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            //  Expandable Cards List 
            wheelItems.forEachIndexed { index, item ->
                var tempLabel by remember(item) { mutableStateOf(item.label) }
                var tempColor by remember(item) { mutableStateOf(item.color) }
                var tempPercent by remember(item) { mutableStateOf(item.percent.coerceIn(0f, 1f)) }

                // Stable random dark colors
                val randomDarkColors = remember(item) {
                    List(5) {
                        Color(
                            red = Random.nextInt(0, 80),
                            green = Random.nextInt(30, 100),
                            blue = Random.nextInt(0, 80)
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

                        //  Card Header 
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
                                    text = item.label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row {
                                IconButton(onClick = {
                                    expandedIndex = if (expandedIndex == index) null else index
                                    if (expandedIndex == index) {
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

                                TextButton(onClick = {
                                    val updatedItems =
                                        wheelItems.toMutableList().apply { removeAt(index) }
                                    onUpdateItems(normalizePercentagesToOne(updatedItems))
                                    expandedIndex = null
                                }) {
                                    Text("Remove", color = Color.Red)
                                }
                            }
                        }

                        //  Expanded Edit Section 
                        if (expandedIndex == index) {
                            Spacer(Modifier.height(8.dp))

                            // Label Input
                            OutlinedTextField(
                                value = tempLabel,
                                onValueChange = { tempLabel = it },
                                label = { Text("Label") },
                                singleLine = true,
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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                randomDarkColors.forEach { color ->
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

                            Spacer(Modifier.height(8.dp))
                            val percentDisplay = (tempPercent * 100).toInt()
                            Text("Set Percentage: $percentDisplay%", color = Color.White)

                            Slider(
                                value = tempPercent,
                                onValueChange = { tempPercent = it.coerceIn(0f, 1f) },
                                valueRange = 0f..1f,
                                steps = 99,
                                colors = SliderDefaults.colors(
                                    thumbColor = goldColor,
                                    activeTrackColor = lightGreenColor
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val updatedList = rebalanceItemPercentagesAfterChange(
                                        wheelItems, index, tempPercent
                                    ).mapIndexed { i, wheelItem ->
                                        if (i == index)
                                            wheelItem.copy(label = tempLabel, color = tempColor)
                                        else wheelItem
                                    }

                                    onUpdateItems(normalizePercentagesToOne(updatedList))
                                    expandedIndex = null
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

            //  Add New Item Button 
            Button(
                onClick = {
                    val newItem = SpinWheelItem(
                        label = "New Item ${wheelItems.size + 1}",
                        color = Color(
                            red = Random.nextInt(0, 80),
                            green = Random.nextInt(30, 100),
                            blue = Random.nextInt(0, 80)
                        ),
                        type = SpinActionType.GAIN_GOLD,
                        value = 0,
                        percent = 0f
                    )
                    val addedList = wheelItems + newItem
                    onUpdateItems(normalizePercentagesToOne(addedList))
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Item", color = darkerGreenColor)
            }
        }
    }
}
