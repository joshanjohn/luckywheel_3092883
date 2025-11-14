package com.griffith.luckywheel.services

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.data.Player
import com.griffith.luckywheel.data.SavedGame
import com.griffith.luckywheel.data.SavedWheelItem
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.ui.screens.playground.enums.SpinActionType

class FireBaseService {
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    fun registerUserWithPlayer(
        playerName: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val regUserId = getCurrentUserId() ?: ""
                    val player = Player(playerId = regUserId, playerName = playerName, gold = 0)

                    database.child("players").child(player.playerId).setValue(player)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onResult(true, null)
                            } else {
                                onResult(false, dbTask.exception?.message)
                            }
                        }
                } else {
                    onResult(false, task.exception?.message)
                }
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

    fun getPlayerRanking(
        onPlayersUpdated: (List<Player>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        // Reference to 'players' in Firebase
        val playersRef = database.child("players")

        // Add a real-time listener
        playersRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val playerList = mutableListOf<Player>()

                for (playerSnap in snapshot.children) {
                    val player = playerSnap.getValue(Player::class.java)
                    if (player != null) {
                        playerList.add(player)
                    }
                }

                // Sort players by gold in descending order
                val sortedList = playerList.sortedByDescending { it.gold }

                // Return the updated sorted list in real-time
                onPlayersUpdated(sortedList)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                onError(Exception(error.message))
            }
        })
    }


    fun updatePlayerInfo(playerId: String, updatedPlayer: Player, onResult: (Boolean) -> Unit) {
        database.child("players").child(playerId).setValue(updatedPlayer)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()

    // Game management methods (moved from FireBaseGameService)
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

        // Preserve createdAt if updating
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
                    // Sort by most recently updated
                    onGamesLoaded(games.sortedByDescending { it.updatedAt })
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(Exception(error.message))
                }
            })
    }

    fun deleteGame(
        gameId: String,
        onResult: (Boolean) -> Unit
    ) {
        database.child("savedGames").child(gameId).removeValue()
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    fun updateGameName(
        gameId: String,
        newName: String,
        onResult: (Boolean) -> Unit
    ) {
        val updates = mapOf(
            "gameName" to newName,
            "updatedAt" to System.currentTimeMillis()
        )
        database.child("savedGames").child(gameId).updateChildren(updates)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }
}
