package com.griffith.luckywheel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.griffith.luckywheel.routes.AppRoute
import com.griffith.luckywheel.ui.theme.LuckyWheelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LuckyWheelTheme {
                AppRoute()
            }
        }
    }
}

