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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

// Handles all Firebase Realtime Database operations for players and games
class FireBaseService {
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // Player database operations
    
    // Check if player exists, create if needed 
    suspend fun checkAndCreatePlayerIfNeeded(
        userId: String,
        displayName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("players").child(userId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            continuation.resume(Result.success("Sign in successful"))
                        } else {
                            val newPlayer = Player(
                                playerId = userId,
                                playerName = displayName,
                                gold = 0
                            )
                            database.child("players").child(userId).setValue(newPlayer)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        continuation.resume(Result.success("Account created successfully"))
                                    } else {
                                        continuation.resume(
                                            Result.failure(
                                                Exception("Failed to create player profile: ${dbTask.exception?.message}")
                                            )
                                        )
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get player info by ID 
    suspend fun getPlayerInfo(playerId: String): Result<Player> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("players").child(playerId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val player = snapshot.getValue(Player::class.java)
                        if (player != null) {
                            continuation.resume(Result.success(player))
                        } else {
                            continuation.resume(Result.failure(Exception("Player not found")))
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get player info by ID (alternative name for compatibility)
    suspend fun getPlayerInfoById(playerId: String): Result<Player> {
        if (playerId.isBlank()) {
            return Result.failure(Exception("Invalid player ID"))
        }
        return getPlayerInfo(playerId)
    }

    // Update player gold 
    suspend fun updatePlayerGold(playerId: String, newGold: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("players").child(playerId).child("gold").setValue(newGold)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Failed to update gold"))
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update player info 
    suspend fun updatePlayerInfo(playerId: String, updatedPlayer: Player): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("players").child(playerId).setValue(updatedPlayer)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Failed to update player info"))
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real-time listeners - Keep callback-based for continuous updates
    
    // Get player ranking with real-time updates
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

    // Listen to real-time updates for a specific player
    fun listenToPlayerUpdates(
        playerId: String,
        onPlayerUpdated: (Player?) -> Unit
    ): ValueEventListener {
        val playerRef = database.child("players").child(playerId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val player = snapshot.getValue(Player::class.java)
                onPlayerUpdated(player)
            }

            override fun onCancelled(error: DatabaseError) {
                onPlayerUpdated(null)
            }
        }
        playerRef.addValueEventListener(listener)
        return listener
    }

    // Remove a specific listener
    fun removePlayerListener(playerId: String, listener: ValueEventListener) {
        database.child("players").child(playerId).removeEventListener(listener)
    }

    // Game database operations - Suspend Functions
    
    // Save or update custom game 
    suspend fun saveCustomGame(
        playerId: String,
        gameName: String,
        wheelItems: List<SpinWheelItem>,
        gameId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val id = gameId ?: database.child("savedGames").push().key
            if (id.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Failed to generate game ID"))
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

            suspendCancellableCoroutine { continuation ->
                if (gameId != null) {
                    // Updating existing game - preserve createdAt
                    database.child("savedGames").child(id).child("createdAt").get()
                        .addOnSuccessListener { snapshot ->
                            val createdAt = snapshot.getValue(Long::class.java) ?: System.currentTimeMillis()
                            val savedGame = SavedGame(
                                gameId = id,
                                gameName = gameName,
                                playerId = playerId,
                                wheelItems = savedWheelItems,
                                createdAt = createdAt,
                                updatedAt = System.currentTimeMillis()
                            )
                            database.child("savedGames").child(id).setValue(savedGame)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        continuation.resume(Result.success(id))
                                    } else {
                                        continuation.resume(
                                            Result.failure(task.exception ?: Exception("Failed to update game"))
                                        )
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            continuation.resume(Result.failure(exception))
                        }
                } else {
                    // Creating new game
                    val savedGame = SavedGame(
                        gameId = id,
                        gameName = gameName,
                        playerId = playerId,
                        wheelItems = savedWheelItems,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    database.child("savedGames").child(id).setValue(savedGame)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                continuation.resume(Result.success(id))
                            } else {
                                continuation.resume(
                                    Result.failure(task.exception ?: Exception("Failed to save game"))
                                )
                            }
                        }
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load player games once (one-time fetch) 
    suspend fun getPlayerGames(playerId: String): Result<List<SavedGame>> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("savedGames")
                    .orderByChild("playerId")
                    .equalTo(playerId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val games = mutableListOf<SavedGame>()
                        for (gameSnapshot in snapshot.children) {
                            val game = gameSnapshot.getValue(SavedGame::class.java)
                            if (game != null) {
                                games.add(game)
                            }
                        }
                        continuation.resume(Result.success(games.sortedByDescending { it.updatedAt }))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Load player games with real-time updates (keep for backward compatibility)
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

    // Delete game 
    suspend fun deleteGame(gameId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                database.child("savedGames").child(gameId).removeValue()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Failed to delete game"))
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update game name 
    suspend fun updateGameName(gameId: String, newName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { continuation ->
                val updates = mapOf(
                    "gameName" to newName,
                    "updatedAt" to System.currentTimeMillis()
                )
                database.child("savedGames").child(gameId).updateChildren(updates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(Result.success(Unit))
                        } else {
                            continuation.resume(
                                Result.failure(task.exception ?: Exception("Failed to update game name"))
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete all player data including profile and saved games 
    suspend fun deleteAllPlayerData(playerId: String): Result<String> = withContext(Dispatchers.IO) {
        if (playerId.isBlank()) {
            return@withContext Result.failure(Exception("Invalid player ID"))
        }
        
        try {
            suspendCancellableCoroutine { continuation ->
                var playerDeleted = false
                var gamesDeleted = false
                var playerError: String? = null
                var gamesError: String? = null
                
                fun checkDeletionComplete() {
                    if (playerDeleted && gamesDeleted) {
                        if (playerError == null && gamesError == null) {
                            continuation.resume(Result.success("All player data deleted successfully"))
                        } else {
                            val errorMsg = listOfNotNull(playerError, gamesError).joinToString("; ")
                            continuation.resume(Result.failure(Exception("Partial deletion failure: $errorMsg")))
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}