package com.griffith.luckywheel.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.griffith.luckywheel.data.Player
import kotlinx.coroutines.tasks.await

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

    suspend fun getPlayerGold(playerId: String): Int? {
        return try {
            val snapshot = database.child("players")
                .child(playerId)
                .child("gold")
                .get()
                .await()  // Waits for Firebase Task to complete
            snapshot.getValue(Int::class.java)
        } catch (e: Exception) {
            null
        }
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
}
