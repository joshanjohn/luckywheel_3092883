package com.griffith.luckywheel.ui.screens.auth.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.accentMagenta
import com.griffith.luckywheel.ui.theme.ArcadeFontFamily

/**
 * Custom submission button used in Authentication screens.
 * Styled with a cyan/magenta gradient and arcade typography.
 */
@Composable
fun AuthSubmitBtn(
    label: String,
    onSubmit: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = { onSubmit() },
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryNeon,
                        accentMagenta,
                        primaryNeon
                    )
                ),
                shape = RoundedCornerShape(50.dp)
            ),
        shape = RoundedCornerShape(50.dp),
        enabled = enabled,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontFamily = ArcadeFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}