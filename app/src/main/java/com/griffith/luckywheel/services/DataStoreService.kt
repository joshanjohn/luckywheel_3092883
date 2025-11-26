package com.griffith.luckywheel.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.griffith.luckywheel.models.data.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.playerPrefDataStore by preferencesDataStore(name = "player_pref")

// Handles all local data storage operations for player preferences
class DataStoreService(private val context: Context) {

    private val dataStore = context.playerPrefDataStore

    private companion object {
        val PLAYER_ID = stringPreferencesKey("player_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val GOLD = intPreferencesKey("gold")
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
}
