package com.griffith.luckywheel.screens.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthSubmitBtn(label: String, onSubmit: () -> Unit) {
    Button(
        onClick = { onSubmit() },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),                         // border radius
        border = BorderStroke(1.5.dp, Color(0xFF010300)),                   // border color & width
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107807)), // fill color
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,          // text font size
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}