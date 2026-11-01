package com.griffith.luckywheel.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.griffith.luckywheel.models.data.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class AppSettings(
    val musicVolume: Float = 0.5f,
    val musicMuted: Boolean = false,
    val soundEffectsVolume: Float = 0.7f,
    val soundEffectsMuted: Boolean = false,
    val sensorSpinEnabled: Boolean = true,
)

private val Context.playerPrefDataStore by preferencesDataStore(name = "player_pref")

// Handles all local data storage operations for player preferences
class DataStoreService(private val context: Context) {

    private val dataStore = context.playerPrefDataStore

    private companion object {
        val PLAYER_ID = stringPreferencesKey("player_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val GOLD = intPreferencesKey("gold")
       private val MUSIC_VOLUME = floatPreferencesKey("music_volume")
    private val MUSIC_MUTED = booleanPreferencesKey("music_muted")
    private val SOUND_EFFECTS_VOLUME = floatPreferencesKey("sound_effects_volume")
    private val SOUND_EFFECTS_MUTED = booleanPreferencesKey("sound_effects_muted")
    private val SENSOR_SPIN_ENABLED = booleanPreferencesKey("sensor_spin_enabled")
    }

    // Save (or replace) player data - runs on IO thread
    suspend fun savePlayer(player: Player) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[PLAYER_ID] = player.playerId
            prefs[PLAYER_NAME] = player.playerName
            prefs[GOLD] = player.gold
        }
    }

    // Read player data once - runs on IO thread
    suspend fun getPlayer(): Player = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        Player(
            playerId = prefs[PLAYER_ID] ?: "",
            playerName = prefs[PLAYER_NAME] ?: "",
            gold = prefs[GOLD] ?: 0
        )
    }

    // Update only gold value - runs on IO thread
    suspend fun updateGold(newGold: Int) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            val currentId = prefs[PLAYER_ID] ?: ""
            val currentName = prefs[PLAYER_NAME] ?: ""
            if (currentId.isNotEmpty() && currentName.isNotEmpty()) {
                prefs[GOLD] = newGold
            }
        }
    }

    // Clear all player data - runs on IO thread
    suspend fun clear() = withContext(Dispatchers.IO) {
        dataStore.edit { prefs -> prefs.clear() }
    }
    
    // Save music volume (0.0 to 1.0)
    suspend fun saveMusicVolume(volume: Float) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[MUSIC_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
    
    // Get music volume
    fun getMusicVolume() = dataStore.data.map { prefs ->
        prefs[MUSIC_VOLUME] ?: 0.5f
    }
    
    // Save music muted state
    suspend fun saveMusicMuted(muted: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[MUSIC_MUTED] = muted
        }
    }
    
    // Get music muted state
    fun getMusicMuted() = dataStore.data.map { prefs ->
        prefs[MUSIC_MUTED] ?: false
    }
    
    // Save sound effects volume (0.0 to 1.0)
    suspend fun saveSoundEffectsVolume(volume: Float) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[SOUND_EFFECTS_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
    
    // Get sound effects volume
    fun getSoundEffectsVolume() = dataStore.data.map { prefs ->
        prefs[SOUND_EFFECTS_VOLUME] ?: 0.7f
    }
    
    // Save sound effects muted state
    suspend fun saveSoundEffectsMuted(muted: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[SOUND_EFFECTS_MUTED] = muted
        }
    }
    
    // Get sound effects muted state
    fun getSoundEffectsMuted() = dataStore.data.map { prefs ->
        prefs[SOUND_EFFECTS_MUTED] ?: false
    }

    // Save sensor spin enabled state (true = shake to spin, false = tap to spin)
    suspend fun saveSensorSpinEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        dataStore.edit { prefs ->
            prefs[SENSOR_SPIN_ENABLED] = enabled
        }
    }

    // Get sensor spin enabled state (defaults to true - shake mode on)
    fun getSensorSpinEnabled() = dataStore.data.map { prefs ->
        prefs[SENSOR_SPIN_ENABLED] ?: true
    }

    /**
     * Single flow that emits all audio + spin settings together.
     * Use this in screens that need multiple settings to avoid opening
     * the DataStore backing file 5 separate times.
     */
    fun getAppSettings() = dataStore.data.map { prefs ->
        AppSettings(
            musicVolume       = prefs[MUSIC_VOLUME] ?: 0.5f,
            musicMuted        = prefs[MUSIC_MUTED] ?: false,
            soundEffectsVolume = prefs[SOUND_EFFECTS_VOLUME] ?: 0.7f,
            soundEffectsMuted = prefs[SOUND_EFFECTS_MUTED] ?: false,
            sensorSpinEnabled = prefs[SENSOR_SPIN_ENABLED] ?: true,
        )
    }
}
