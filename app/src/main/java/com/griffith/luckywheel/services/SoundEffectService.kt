package com.griffith.luckywheel.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Service to handle one-time sound effects (win/lose/click sounds)
class SoundEffectService(private val context: Context) {
    
    private var winPlayer: MediaPlayer? = null
    private var losePlayer: MediaPlayer? = null
    private var clickPlayer: MediaPlayer? = null
    private var bubbleClickPlayer: MediaPlayer? = null
    private var addItemPlayer: MediaPlayer? = null
    private var progressPlayer: MediaPlayer? = null
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private val dataStoreService = DataStoreService(context)
    private var currentVolume: Float = 0.7f
    private var isMuted: Boolean = false
    
    init {
        // Load preferences asynchronously
        scope.launch {
            try {
                currentVolume = dataStoreService.getSoundEffectsVolume().first()
                isMuted = dataStoreService.getSoundEffectsMuted().first()
                initializePlayers()
            } catch (e: Exception) {
                Log.e("SoundEffectService", "Init failed", e)
            }
        }
    }
    
    private fun initializePlayers() {
        winPlayer = createPlayer("gold_game_win")
        losePlayer = createPlayer("gold_game_fail")
        clickPlayer = createPlayer("single_click")
        bubbleClickPlayer = createPlayer("bubble_single_click")
        addItemPlayer = createPlayer("add_item_single_click")
        progressPlayer = createPlayer("progress_sound")
    }

    private fun createPlayer(rawName: String): MediaPlayer? {
        val resId = context.resources.getIdentifier(rawName, "raw", context.packageName)
        if (resId == 0) return null
        
        return try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build()
                )
                setDataSource(context, android.net.Uri.parse("android.resource://${context.packageName}/$resId"))
                prepare()
                setVolume(if (isMuted) 0f else currentVolume, if (isMuted) 0f else currentVolume)
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Failed to load $rawName", e)
            null
        }
    }
    
    fun playWinSound() = play(winPlayer)
    fun playLoseSound() = play(losePlayer)
    fun playClickSound() = play(clickPlayer)
    fun playBubbleClickSound() = play(bubbleClickPlayer)
    fun playAddItemSound() = play(addItemPlayer)
    fun playProgressSound() = play(progressPlayer)

    private fun play(player: MediaPlayer?) {
        try {
            player?.let {
                if (it.isPlaying) it.seekTo(0) else it.start()
            }
        } catch (e: Exception) {
            Log.e("SoundEffectService", "Playback failed", e)
        }
    }
    
    suspend fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        dataStoreService.saveSoundEffectsVolume(currentVolume)
        updateAllVolumes()
    }
    
    suspend fun setMuted(muted: Boolean) {
        isMuted = muted
        dataStoreService.saveSoundEffectsMuted(isMuted)
        updateAllVolumes()
    }
    
    private fun updateAllVolumes() {
        val vol = if (isMuted) 0f else currentVolume
        listOf(winPlayer, losePlayer, clickPlayer, bubbleClickPlayer, addItemPlayer, progressPlayer).forEach {
            it?.setVolume(vol, vol)
        }
    }
    
    fun release() {
        listOf(winPlayer, losePlayer, clickPlayer, bubbleClickPlayer, addItemPlayer, progressPlayer).forEach {
            it?.release()
        }
        winPlayer = null
        losePlayer = null
        clickPlayer = null
        bubbleClickPlayer = null
        addItemPlayer = null
        progressPlayer = null
    }
}
