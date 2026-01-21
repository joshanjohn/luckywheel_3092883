package com.griffith.luckywheel

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.griffith.luckywheel.routes.AppRoute
import com.griffith.luckywheel.services.BackgroundMusicService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService

import com.griffith.luckywheel.ui.theme.LuckyWheelTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startMusicService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        
        checkPermissionsAndStartMusic()
        
        setContent {
            LuckyWheelTheme {
                com.griffith.luckywheel.ui.components.effects.MagicalBackground {
                    var initialRoute by remember { mutableStateOf<String?>(null) }
                    var isCheckingAuth by remember { mutableStateOf(true) }
                    val coroutineScope = rememberCoroutineScope()
                    
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            val auth = FirebaseAuth.getInstance()
                            val currentUser = auth.currentUser
                            
                            if (currentUser != null) {
                                val userId = currentUser.uid
                                val dataStoreService = DataStoreService(this@MainActivity)
                                val firebaseService = FireBaseService()
                                
                                val playerResult = firebaseService.getPlayerInfo(userId)
                                playerResult.onSuccess { player ->
                                    dataStoreService.clear()
                                    dataStoreService.savePlayer(player)

                                    initialRoute = "home/$userId"
                                }.onFailure {
                                    val cachedPlayer = dataStoreService.getPlayer()
                                    initialRoute = if (cachedPlayer.playerId.isNotEmpty()) "home/${cachedPlayer.playerId}" else "login"
                                }
                            } else {
                                val dataStoreService = DataStoreService(this@MainActivity)
                                val cachedPlayer = dataStoreService.getPlayer()
                                initialRoute = if (cachedPlayer.playerId.isNotEmpty()) "home/${cachedPlayer.playerId}" else "login"
                            }
                            isCheckingAuth = false
                        }
                    }
                    
                    if (!isCheckingAuth && initialRoute != null) {
                        AppRoute(startDestination = initialRoute!!)
                    }
                }
            }
        }
    }

    private fun checkPermissionsAndStartMusic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startMusicService()
            }
        } else {
            startMusicService()
        }
    }

    private fun startMusicService() {
        val intent = Intent(this, BackgroundMusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopMusicService() {
        val intent = Intent(this, BackgroundMusicService::class.java)
        stopService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // We usually don't want to stop the background music if the app is still "alive" in certain states, 
        // but for a game, stopping it on onDestroy is reasonable if we want it to cease when the task is cleared.
        stopMusicService()
    }
}
