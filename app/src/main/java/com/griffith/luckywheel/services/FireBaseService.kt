package com.griffith.luckywheel.services

import androidx.compose.ui.graphics.toArgb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.models.data.SavedWheelItem
import com.griffith.luckywheel.models.data.SpinWheelItem

// Handles all Firebase Realtime Database operations for players and games
class FireBaseService {
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // Player database operations
    
    fun checkAndCreatePlayerIfNeeded(
        userId: String,
        displayName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        database.child("players").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onResult(true, "Sign in successful")
                } else {
                    val newPlayer = Player(
                        playerId = userId,
                        playerName = displayName,
                        gold = 0
                    )
                    database.child("players").child(userId).setValue(newPlayer)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onResult(true, "Account created successfully")
                            } else {
                                onResult(false, "Failed to create player profile: ${dbTask.exception?.message}")
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, "Failed to check player data: ${exception.message}")
            }
    }

    fun getPlayerInfo(playerId: String, onResult: (Player?) -> Unit) {
        database.child("players").child(playerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val player = snapshot.getValue(Player::class.java)
                onResult(player)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun getPlayerInfoById(playerId: String, onResult: (Player?) -> Unit) {
        if (playerId.isBlank()) {
            onResult(null)
            return
        }
        database.child("players").child(playerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val player = snapshot.getValue(Player::class.java)
                onResult(player)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun updatePlayerGold(playerId: String, newGold: Int, onResult: (Boolean) -> Unit) {
        database.child("players").child(playerId).child("gold").setValue(newGold)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun updatePlayerInfo(playerId: String, updatedPlayer: Player, onResult: (Boolean) -> Unit) {
        database.child("players").child(playerId).setValue(updatedPlayer)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun getPlayerRanking(
        onPlayersUpdated: (List<Player>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val playersRef = database.child("players")
        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playerList = mutableListOf<Player>()
                for (playerSnap in snapshot.children) {
                    val player = playerSnap.getValue(Player::class.java)
                    if (player != null) {
                        playerList.add(player)
                    }
                }
                val sortedList = playerList.sortedByDescending { it.gold }
                onPlayersUpdated(sortedList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(Exception(error.message))
            }
        })
    }

    // Game database operations
    
    fun saveCustomGame(
        playerId: String,
        gameName: String,
        wheelItems: List<SpinWheelItem>,
        gameId: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val id = gameId ?: database.child("savedGames").push().key ?: ""
        if (id.isEmpty()) {
            onResult(false, "Failed to generate game ID")
            return
        }

        val savedWheelItems = wheelItems.map { item ->
            SavedWheelItem(
                label = item.label,
                colorHex = String.format("#%08X", item.color.toArgb()),
                type = item.type.name,
                value = item.value,
                percent = item.percent
            )
        }

        val savedGame = SavedGame(
            gameId = id,
            gameName = gameName,
            playerId = playerId,
            wheelItems = savedWheelItems,
            createdAt = if (gameId == null) System.currentTimeMillis() else 0,
            updatedAt = System.currentTimeMillis()
        )

        if (gameId != null) {
            database.child("savedGames").child(id).child("createdAt").get()
                .addOnSuccessListener { snapshot ->
                    val createdAt = snapshot.getValue(Long::class.java) ?: System.currentTimeMillis()
                    val updatedGame = savedGame.copy(createdAt = createdAt)
                    database.child("savedGames").child(id).setValue(updatedGame)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onResult(true, id)
                            } else {
                                onResult(false, task.exception?.message)
                            }
                        }
                }
        } else {
            database.child("savedGames").child(id).setValue(savedGame)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(true, id)
                    } else {
                        onResult(false, task.exception?.message)
                    }
                }
        }
    }

    fun loadPlayerGames(
        playerId: String,
        onGamesLoaded: (List<SavedGame>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        database.child("savedGames")
            .orderByChild("playerId")
            .equalTo(playerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val games = mutableListOf<SavedGame>()
                    for (gameSnapshot in snapshot.children) {
                        val game = gameSnapshot.getValue(SavedGame::class.java)
                        if (game != null) {
                            games.add(game)
                        }
                    }
                    onGamesLoaded(games.sortedByDescending { it.updatedAt })
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(Exception(error.message))
                }
            })
    }

    fun deleteGame(gameId: String, onResult: (Boolean) -> Unit) {
        database.child("savedGames").child(gameId).removeValue()
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun updateGameName(gameId: String, newName: String, onResult: (Boolean) -> Unit) {
        val updates = mapOf(
            "gameName" to newName,
            "updatedAt" to System.currentTimeMillis()
        )
        database.child("savedGames").child(gameId).updateChildren(updates)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }
    
    // Deletes all player data including profile and saved games
    fun deleteAllPlayerData(playerId: String, onResult: (success: Boolean, message: String?) -> Unit) {
        if (playerId.isBlank()) {
            onResult(false, "Invalid player ID")
            return
        }
        
        var playerDeleted = false
        var gamesDeleted = false
        var playerError: String? = null
        var gamesError: String? = null
        
        fun checkDeletionComplete() {
            if (playerDeleted && gamesDeleted) {
                if (playerError == null && gamesError == null) {
                    onResult(true, "All player data deleted successfully")
                } else {
                    val errorMsg = listOfNotNull(playerError, gamesError).joinToString("; ")
                    onResult(false, "Partial deletion failure: $errorMsg")
                }
            }
        }
        
        // Delete player profile
        database.child("players").child(playerId).removeValue()
            .addOnCompleteListener { playerTask ->
                playerDeleted = true
                if (!playerTask.isSuccessful) {
                    playerError = playerTask.exception?.message
                }
                checkDeletionComplete()
            }
        
        // Delete all saved games by this player
        database.child("savedGames")
            .orderByChild("playerId")
            .equalTo(playerId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val deletePromises = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                    for (gameSnapshot in snapshot.children) {
                        deletePromises.add(gameSnapshot.ref.removeValue())
                    }
                    com.google.android.gms.tasks.Tasks.whenAll(deletePromises)
                        .addOnCompleteListener { gamesTask ->
                            gamesDeleted = true
                            if (!gamesTask.isSuccessful) {
                                gamesError = gamesTask.exception?.message
                            }
                            checkDeletionComplete()
                        }
                } else {
                    gamesDeleted = true
                    checkDeletionComplete()
                }
            }
            .addOnFailureListener { exception ->
                gamesDeleted = true
                gamesError = exception.message
                checkDeletionComplete()
            }
    }
}