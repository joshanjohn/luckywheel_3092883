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

class DataStoreService(context: Context) {

    // Create the DataStore once, internally
    private val Context.playerPrefDataStore by preferencesDataStore(name = "player_pref")
    private val dataStore by lazy { context.playerPrefDataStore }

    private companion object {
        val PLAYER_ID = stringPreferencesKey("player_id")
        val PLAYER_NAME = stringPreferencesKey("player_name")
        val GOLD = intPreferencesKey("gold")
    }

    /** Save (or replace) single player */
    suspend fun savePlayer(player: Player) {
        ensureDataStoreExists()
        dataStore.edit { prefs ->
            prefs.clear() // ensure only one player is stored
            prefs[PLAYER_ID] = player.playerId
            prefs[PLAYER_NAME] = player.playerName
            prefs[GOLD] = player.gold
        }
    }

    /** Get player as Flow (auto-updates UI) */
    val playerFlow: Flow<Player> = dataStore.data.map { prefs ->
        Player(
            playerId = prefs[PLAYER_ID] ?: "",
            playerName = prefs[PLAYER_NAME] ?: "",
            gold = prefs[GOLD] ?: 0
        )
    }

    /** Read current player once (non-flow) */
    suspend fun getPlayer(): Player {
        ensureDataStoreExists()
        val prefs = dataStore.data.first()
        return Player(
            playerId = prefs[PLAYER_ID] ?: "",
            playerName = prefs[PLAYER_NAME] ?: "",
            gold = prefs[GOLD] ?: 0
        )
    }

    /** Update only gold value for existing player */
    suspend fun updateGold(newGold: Int) {
        ensureDataStoreExists()
        dataStore.edit { prefs ->
            val currentName = prefs[PLAYER_NAME] ?: ""
            val currentId = prefs[PLAYER_ID] ?: ""
            if (currentId.isNotEmpty() && currentName.isNotEmpty()) {
                prefs[GOLD] = newGold
            }
        }
    }

    /** Remove the player info entirely */
    suspend fun clear() {
        ensureDataStoreExists()
        dataStore.edit { prefs -> prefs.clear() }
    }

    /** Ensure the datastore exists before any operation */
    private suspend fun ensureDataStoreExists() {
        // Reading once forces creation
        dataStore.data.first()
    }
}
