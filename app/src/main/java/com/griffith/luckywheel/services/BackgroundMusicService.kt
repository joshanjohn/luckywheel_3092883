package com.griffith.luckywheel.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


// Background music service with looping, volume control, and mute - singleton pattern
class BackgroundMusicService private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var instance: BackgroundMusicService? = null
        
        fun getInstance(context: Context): BackgroundMusicService {
            return instance ?: synchronized(this) {
                instance ?: BackgroundMusicService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private val dataStoreService = DataStoreService(context)
    private var currentVolume: Float = 0.50f
    private var isMuted: Boolean = false
    
    init {
        // Load saved preferences
        runBlocking {
            currentVolume = dataStoreService.getMusicVolume().first()
            isMuted = dataStoreService.getMusicMuted().first()
        }
        initializePlayer()
    }
    
    private fun initializePlayer() {
        try {
            // Try to load background music from raw resources
            val resourceId = context.resources.getIdentifier("bgmusic", "raw", context.packageName)
            if (resourceId != 0) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$resourceId"))
                    isLooping = true
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("BackgroundMusicService", "Background music loaded successfully")
            } else {
                Log.w("BackgroundMusicService", "bgmusic not found in raw resources")
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Failed to initialize background music", e)
        }
    }
    
    // Start playing background music
    fun play() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    Log.d("BackgroundMusicService", "Music started")
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Failed to start music", e)
        }
    }
    
    // Pause background music
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    Log.d("BackgroundMusicService", "Music paused")
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Failed to pause music", e)
        }
    }
    
    // Set music volume (0.0 to 1.0)
    suspend fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        dataStoreService.saveMusicVolume(currentVolume)
        updatePlayerVolume()
        Log.d("BackgroundMusicService", "Volume set to: $currentVolume")
    }
    
    // Toggle mute state
    suspend fun setMuted(muted: Boolean) {
        isMuted = muted
        dataStoreService.saveMusicMuted(isMuted)
        updatePlayerVolume()
        Log.d("BackgroundMusicService", "Muted: $isMuted")
    }
    
    // Get current mute state
    fun isMuted(): Boolean = isMuted
    
    // Get current volume
    fun getVolume(): Float = currentVolume
    
    private fun updatePlayerVolume() {
        try {
            val effectiveVolume = if (isMuted) 0f else currentVolume
            mediaPlayer?.setVolume(effectiveVolume, effectiveVolume)
            Log.d("BackgroundMusicService", "Player volume updated to: $effectiveVolume")
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Failed to update volume", e)
        }
    }
    
    // Release all resources
    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            Log.d("BackgroundMusicService", "Music service released")
        } catch (e: Exception) {
            Log.e("BackgroundMusicService", "Failed to release resources", e)
        }
    }
}
