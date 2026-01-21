package com.griffith.luckywheel.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.griffith.luckywheel.utils.CountryFlags

// Reusable country picker component with flag emojis
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPicker(
    selectedCountry: String,
    onCountrySelected: (String, String) -> Unit, // (countryName, countryCode)
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    val countries = remember { CountryFlags.getAllCountries() }
    
    // Find selected country's flag
    val selectedFlag = countries.find { it.first == selectedCountry }?.second ?: "🌍"
    
    // Country selector button
    OutlinedTextField(
        value = if (selectedCountry.isNotEmpty()) "$selectedFlag $selectedCountry" else "",
        onValueChange = {},
        label = { Text("Country") },
        readOnly = true,
        enabled = enabled,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select country"
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDialog = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedLabelColor = Color(0xFFFFD700),
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.7f),
            disabledBorderColor = Color.White.copy(alpha = 0.3f),
            disabledLabelColor = Color.White.copy(alpha = 0.5f)
        )
    )
    
    // Country selection dialog
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Dialog title
                    Text(
                        text = "Select Country",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Country list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(countries) { (countryName, flag) ->
                            // Get country code from name
                            val countryCode = CountryFlags.getCountryCodeFromName(countryName)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCountrySelected(countryName, countryCode)
                                        showDialog = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = flag,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = countryName,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
