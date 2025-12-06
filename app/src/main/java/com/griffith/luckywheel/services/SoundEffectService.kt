package com.griffith.luckywheel.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

/**
 * Service to handle one-time sound effects (win/lose sounds).
 * Separate from BackgroundMusicService to allow simultaneous playback.
 */
class SoundEffectService(private val context: Context) {
    
    private var winPlayer: MediaPlayer? = null
    private var losePlayer: MediaPlayer? = null
    
    init {
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
                }
                Log.d("SoundEffectService", "Lose sound loaded successfully")
            } else {
                Log.w("SoundEffectService", "gold_game_fail not found in raw resources")
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to initialize sound effects", e)
        }
    }
    
    /**
     * Play win sound effect
     */
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
    
    /**
     * Play lose sound effect
     */
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
    
    /**
     * Release all resources
     */
    fun release() {
        try {
            winPlayer?.release()
            winPlayer = null
            losePlayer?.release()
            losePlayer = null
            Log.d("SoundEffectService", "Sound effects released")
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to release resources", e)
        }
    }
}
