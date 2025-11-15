package com.griffith.luckywheel.ui.screens.playground.gold_wheel.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.models.enum.SpinActionType
import com.griffith.luckywheel.ui.theme.darkGreenColor
import com.griffith.luckywheel.ui.theme.darkerGreenColor
import com.griffith.luckywheel.ui.theme.lightGreenColor
import com.griffith.luckywheel.ui.theme.darkerRedColor
import com.griffith.luckywheel.ui.theme.lightRedColor

@Composable
fun ResultCard(
    wheelResult: SpinWheelItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(450.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (wheelResult.type == SpinActionType.LOSE_GOLD)
                            listOf(darkerRedColor, lightRedColor,darkerRedColor)
                        else listOf(darkerGreenColor, darkGreenColor, darkerGreenColor)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ),
        ) {
            Column(
                modifier = Modifier
                    .size(450.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = if (wheelResult.type == SpinActionType.LOSE_GOLD)
                        "😢 Try Again 😢"
                    else "🎉 Congratulations 🎉",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (wheelResult.type == SpinActionType.LOSE_GOLD)
                        lightRedColor
                    else lightGreenColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You got\n${wheelResult.label}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (wheelResult.type == SpinActionType.LOSE_GOLD)
                            Color(0xFFD32F2F)
                        else Color(0xFF388E3C)
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .height(48.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
