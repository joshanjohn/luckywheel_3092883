package com.griffith.luckywheel.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking


// Service to handle one-time sound effects (win/lose/click sounds) - separate from background music
class SoundEffectService(private val context: Context) {
    
    private var winPlayer: MediaPlayer? = null
    private var losePlayer: MediaPlayer? = null
    private var clickPlayer: MediaPlayer? = null
    private var bubbleClickPlayer: MediaPlayer? = null
    private var addItemPlayer: MediaPlayer? = null
    private var progressPlayer: MediaPlayer? = null
    private val dataStoreService = DataStoreService(context)
    private var currentVolume: Float = 0.7f
    private var isMuted: Boolean = false
    
    init {
        // Load saved preferences
        runBlocking {
            currentVolume = dataStoreService.getSoundEffectsVolume().first()
            isMuted = dataStoreService.getSoundEffectsMuted().first()
        }
        initializePlayers()
    }
    
    private fun initializePlayers() {
        try {
            // Load win sound
            val winResourceId = context.resources.getIdentifier("gold_game_win", "raw", context.packageName)
            if (winResourceId != 0) {
                winPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$winResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Win sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "gold_game_win not found in raw resources")
            }
            
            // Load lose sound
            val loseResourceId = context.resources.getIdentifier("gold_game_fail", "raw", context.packageName)
            if (loseResourceId != 0) {
                losePlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$loseResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Lose sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "gold_game_fail not found in raw resources")
            }
            
            // Load click sound
            val clickResourceId = context.resources.getIdentifier("single_click", "raw", context.packageName)
            if (clickResourceId != 0) {
                clickPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$clickResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Click sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "single_click not found in raw resources")
            }
            
            // Load bubble click sound
            val bubbleClickResourceId = context.resources.getIdentifier("bubble_single_click", "raw", context.packageName)
            if (bubbleClickResourceId != 0) {
                bubbleClickPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$bubbleClickResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Bubble click sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "bubble_single_click not found in raw resources")
            }
            
            // Load add item sound
            val addItemResourceId = context.resources.getIdentifier("add_item_single_click", "raw", context.packageName)
            if (addItemResourceId != 0) {
                addItemPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$addItemResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Add item sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "add_item_single_click not found in raw resources")
            }
            
            // Load progress sound
            val progressResourceId = context.resources.getIdentifier("progress_sound", "raw", context.packageName)
            if (progressResourceId != 0) {
                progressPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .build()
                    )
                    setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$progressResourceId"))
                    prepare()
                    setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
                }
                Log.d("SoundEffectService", "Progress sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "progress_sound not found in raw resources")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to initialize sound effects", e)
        }
    }
    
    // Play win sound effect
    fun playWinSound() {
        try {
            winPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing win sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play win sound", e)
        }
    }
    
    // Play lose sound effect
    fun playLoseSound() {
        try {
            losePlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing lose sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play lose sound", e)
        }
    }
    
    // Play click sound effect
    fun playClickSound() {
        try {
            clickPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing click sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play click sound", e)
        }
    }
    
    // Play bubble click sound effect (for navigation)
    fun playBubbleClickSound() {
        try {
            bubbleClickPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing bubble click sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play bubble click sound", e)
        }
    }
    
    // Play add item sound effect (when adding custom wheel items)
    fun playAddItemSound() {
        try {
            addItemPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing add item sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play add item sound", e)
        }
    }
    
    // Play progress sound effect (for tutorial navigation)
    fun playProgressSound() {
        try {
            progressPlayer?.let { player ->
                if (player.isPlaying) {
                    player.seekTo(0)
                } else {
                    player.start()
                }
                Log.d("SoundEffectService", "Playing progress sound")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to play progress sound", e)
        }
    }
    
    // Set sound effects volume (0.0 to 1.0)
    suspend fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        dataStoreService.saveSoundEffectsVolume(currentVolume)
        updateAllVolumes()
        Log.d("SoundEffectService", "Volume set to: $currentVolume")
    }
    
    // Toggle mute state
    suspend fun setMuted(muted: Boolean) {
        isMuted = muted
        dataStoreService.saveSoundEffectsMuted(isMuted)
        updateAllVolumes()
        Log.d("SoundEffectService", "Muted: $isMuted")
    }
    
    // Get current mute state
    fun isMuted(): Boolean = isMuted
    
    // Get current volume
    fun getVolume(): Float = currentVolume
    
    private fun updateAllVolumes() {
        val effectiveVolume = if (isMuted) 0f else currentVolume
        winPlayer?.setVolume(effectiveVolume, effectiveVolume)
        losePlayer?.setVolume(effectiveVolume, effectiveVolume)
        clickPlayer?.setVolume(effectiveVolume, effectiveVolume)
        bubbleClickPlayer?.setVolume(effectiveVolume, effectiveVolume)
        addItemPlayer?.setVolume(effectiveVolume, effectiveVolume)
        progressPlayer?.setVolume(effectiveVolume, effectiveVolume)
    }
    
    // Release all resources
    fun release() {
        try {
            winPlayer?.release()
            winPlayer = null
            losePlayer?.release()
            losePlayer = null
            clickPlayer?.release()
            clickPlayer = null
            bubbleClickPlayer?.release()
            bubbleClickPlayer = null
            addItemPlayer?.release()
            addItemPlayer = null
            progressPlayer?.release()
            progressPlayer = null
            Log.d("SoundEffectService", "Sound effects released")
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to release resources", e)
        }
    }
}
