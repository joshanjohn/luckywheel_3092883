package com.griffith.goldshake.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.griffith.goldshake.data.Player

class FireBaseService {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // 1️⃣ Register user and create player info
    fun registerUserWithPlayer(
        playerName: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val player = Player(playerName = playerName)
                    val playerId = player.playerId
                    database.child("players").child(playerId).setValue(player)
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

    // 2️⃣ Get player info by playerId
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

    // 3️⃣ Update gold count by playerId
    fun updatePlayerGold(playerId: String, newGold: Int, onResult: (Boolean) -> Unit) {
        database.child("players").child(playerId).child("gold").setValue(newGold)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // 4️⃣ Get player gold by playerId
    fun getPlayerGold(playerId: String, onResult: (Int?) -> Unit) {
        database.child("players").child(playerId).child("gold")
            .get()
            .addOnSuccessListener { snapshot ->
                val gold = snapshot.getValue(Int::class.java)
                onResult(gold)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // 5️⃣ Update full player info
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
