package com.griffith.luckywheel.services

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.data.SavedGame
import com.griffith.luckywheel.data.SavedWheelItem
import com.griffith.luckywheel.data.SpinWheelItem
import com.griffith.luckywheel.ui.screens.playground.enums.SpinActionType

// Extension methods for FireBaseService to handle saved games
fun FireBaseService.saveCustomGame(
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

fun FireBaseService.loadPlayerGames(
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

fun FireBaseService.deleteGame(
    gameId: String,
    onResult: (Boolean) -> Unit
) {
    database.child("savedGames").child(gameId).removeValue()
        .addOnCompleteListener { task ->
            onResult(task.isSuccessful)
        }
}

fun FireBaseService.updateGameName(
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

// Helper to convert SavedGame back to SpinWheelItem list
fun SavedGame.toSpinWheelItems(): List<SpinWheelItem> {
    return wheelItems.map { saved ->
        SpinWheelItem(
            label = saved.label,
            color = Color(android.graphics.Color.parseColor(saved.colorHex)),
            type = try {
                SpinActionType.valueOf(saved.type)
            } catch (e: Exception) {
                SpinActionType.CUSTOM
            },
            value = saved.value,
            percent = saved.percent
        )
    }
}