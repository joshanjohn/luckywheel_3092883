package com.griffith.luckywheel

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.griffith.luckywheel.routes.AppRoute
import com.griffith.luckywheel.services.BackgroundMusicService
import com.griffith.luckywheel.services.DataStoreService
import com.griffith.luckywheel.services.FireBaseService
import com.griffith.luckywheel.ui.theme.LuckyWheelTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var musicService: BackgroundMusicService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        
        // Initialize and start background music
        musicService = BackgroundMusicService.getInstance(this)
        musicService?.play()
        
        setContent {
            LuckyWheelTheme {
                var initialRoute by remember { mutableStateOf<String?>(null) }
                var isCheckingAuth by remember { mutableStateOf(true) }
                val coroutineScope = rememberCoroutineScope()
                
                // Check authentication status on startup
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        val auth = FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser
                        
                        if (currentUser != null) {
                            // User is authenticated in Firebase
                            val userId = currentUser.uid
                            val dataStoreService = DataStoreService(this@MainActivity)
                            val firebaseService = FireBaseService()
                            
                            // Fetch latest player data and save to DataStore
                            val playerResult = firebaseService.getPlayerInfo(userId)
                            playerResult.onSuccess { player ->
                                dataStoreService.clear()
                                dataStoreService.savePlayer(player)
                                initialRoute = "play/$userId"
                            }.onFailure {
                                // If fetching fails, still try to use cached data
                                val cachedPlayer = dataStoreService.getPlayer()
                                if (cachedPlayer.playerId.isNotEmpty()) {
                                    initialRoute = "play/${cachedPlayer.playerId}"
                                } else {
                                    initialRoute = "login"
                                }
                            }
                        } else {
                            // No Firebase user, check DataStore for cached login
                            val dataStoreService = DataStoreService(this@MainActivity)
                            val cachedPlayer = dataStoreService.getPlayer()
                            
                            if (cachedPlayer.playerId.isNotEmpty()) {
                                // User was previously logged in, navigate to play screen
                                initialRoute = "play/${cachedPlayer.playerId}"
                            } else {
                                // No cached user, show login
                                initialRoute = "login"
                            }
                        }
                        
                        isCheckingAuth = false
                    }
                }
                
                // Show app content once auth check is complete
                if (!isCheckingAuth && initialRoute != null) {
                    AppRoute(startDestination = initialRoute!!)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        musicService?.play()
    }
    
    override fun onPause() {
        super.onPause()
        musicService?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        musicService?.release()
        musicService = null
    }
}

