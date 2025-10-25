package com.griffith.luckywheel.screens.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.lightGreenColor

@Composable
fun SettingsChoiceButton(
    label: String,
    onClick: () -> Unit,
    lightColor: Color = lightGreenColor,
    darkerColor: Color = darkerGreenColor,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        lightColor,
                        darkerColor,
                        lightColor
                    )
                ),
                shape = RoundedCornerShape(50.dp),
            ),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.5.dp, darkerColor),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
