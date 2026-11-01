package com.griffith.luckywheel.ui.screens.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.griffith.luckywheel.ui.theme.primaryNeon
import com.griffith.luckywheel.ui.theme.textSecondary

/**
 * Customized outlined text field component for inputs in Login/Registration.
 * Employs Neon Casino focus borders and labels.
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "$label Icon",
                tint = Color.White
            )
        },
        enabled = enabled,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryNeon,
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            cursorColor = primaryNeon,
            focusedLabelColor = primaryNeon,
            unfocusedLabelColor = textSecondary,
            focusedLeadingIconColor = primaryNeon,
            unfocusedLeadingIconColor = Color.White.copy(alpha = 0.6f),
            focusedTrailingIconColor = primaryNeon,
            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.6f)
        ),
        modifier = modifier.fillMaxWidth()
    )
}
