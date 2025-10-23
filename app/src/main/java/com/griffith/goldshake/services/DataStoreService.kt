package com.griffith.goldshake.services

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.griffith.goldshake.data.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.playerPrefDataStore by preferencesDataStore(name = "player_pref")

class DataStoreService(private val context: Context) {

    private val dataStore = context.playerPrefDataStore

    private companion object {
        val PLAYER_ID = stringPreferencesKey("player_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val GOLD = intPreferencesKey("gold")
    }

    //Save (or replace) single player
    suspend fun savePlayer(player: Player) {
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[PLAYER_ID] = player.playerId
            prefs[PLAYER_NAME] = player.playerName
            prefs[GOLD] = player.gold
        }
    }

    // Get player as Flow
    val playerFlow: Flow<Player> = dataStore.data.map { prefs ->
        Player(
            playerId = prefs[PLAYER_ID] ?: "",
            playerName = prefs[PLAYER_NAME] ?: "",
            gold = prefs[GOLD] ?: 0
        )
    }

    // Read player once
    suspend fun getPlayer(): Player {
        val prefs = dataStore.data.first()
        return Player(
            playerId = prefs[PLAYER_ID] ?: "",
            playerName = prefs[PLAYER_NAME] ?: "",
            gold = prefs[GOLD] ?: 0
        )
    }

    // Update only gold value
    suspend fun updateGold(newGold: Int) {
        dataStore.edit { prefs ->
            val currentId = prefs[PLAYER_ID] ?: ""
            val currentName = prefs[PLAYER_NAME] ?: ""
            if (currentId.isNotEmpty() && currentName.isNotEmpty()) {
                prefs[GOLD] = newGold
            }
        }
    }

    // Clear player info
    suspend fun clear() {
        dataStore.edit { prefs -> prefs.clear() }
    }
}
