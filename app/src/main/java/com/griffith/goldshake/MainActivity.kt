package com.griffith.goldshake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.griffith.goldshake.routes.AppRoute
import com.griffith.goldshake.ui.theme.GoldShakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoldShakeTheme {
                AppRoute()
            }
        }
    }
}

