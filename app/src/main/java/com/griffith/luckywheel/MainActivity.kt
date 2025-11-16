package com.griffith.luckywheel

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.griffith.luckywheel.routes.AppRoute
import com.griffith.luckywheel.ui.theme.LuckyWheelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        setContent {
            LuckyWheelTheme {
                AppRoute()
            }
        }
    }
}

