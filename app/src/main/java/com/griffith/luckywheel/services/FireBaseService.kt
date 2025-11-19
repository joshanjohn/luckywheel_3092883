package com.griffith.luckywheel.services

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.griffith.luckywheel.models.data.Player
import com.griffith.luckywheel.models.data.SavedGame
import com.griffith.luckywheel.models.data.SavedWheelItem
import com.griffith.luckywheel.models.data.SpinWheelItem
import com.griffith.luckywheel.BuildConfig


class FireBaseService {
    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    // Google Sign-In Configuration
    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(context: Context): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    // Handle Google Sign-In Result
    fun handleGoogleSignInResult(
        data: Intent?,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account != null) {
                signInWithGoogleAccount(account, onResult)
            } else {
                onResult(false, "Google sign-in failed: No account found", null)
            }
        } catch (e: ApiException) {
            onResult(false, "Google sign-in failed: ${e.message}", null)
        } catch (e: Exception) {
            onResult(false, "Google sign-in failed: ${e.message}", null)
        }
    }

    // Sign in with Google Account
    private fun signInWithGoogleAccount(
        account: GoogleSignInAccount,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val displayName = user?.displayName ?: account.displayName ?: "User"

                    if (userId != null) {
                        // Check if player exists in database
                        checkAndCreatePlayer(userId, displayName, onResult)
                    } else {
                        onResult(false, "Failed to get user ID", null)
                    }
                } else {
                    onResult(false, "Firebase authentication failed: ${task.exception?.message}", null)
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, "Firebase authentication error: ${exception.message}", null)
            }
    }

    // Check if player exists, create if not
    private fun checkAndCreatePlayer(
        userId: String,
        displayName: String,
        onResult: (Boolean, String?, String?) -> Unit
    ) {
        database.child("players").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Player exists, return success
                    onResult(true, "Sign in successful", userId)
                } else {
                    // Player doesn't exist, create new player
                    val newPlayer = Player(
                        playerId = userId,
                        playerName = displayName,
                        gold = 0
                    )

                    database.child("players").child(userId).setValue(newPlayer)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                onResult(true, "Account created successfully", userId)
                            } else {
                                onResult(false, "Failed to create player profile: ${dbTask.exception?.message}", userId)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                onResult(false, "Failed to check player data: ${exception.message}", null)
            }
    }

    // Sign out from Google
    fun signOutGoogle(context: Context, onComplete: () -> Unit = {}) {
        auth.signOut()
        getGoogleSignInClient(context).signOut().addOnCompleteListener {
            onComplete()
        }
    }

    // Email Password Registration
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

                    // Set display name for Firebase Auth user
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = playerName
                    }

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        // Continue with database save regardless of profile update
                        val player = Player(playerId = regUserId, playerName = playerName, gold = 0)

                        database.child("players").child(player.playerId).setValue(player)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    onResult(true, null)
                                } else {
                                    onResult(false, dbTask.exception?.message)
                                }
                            }
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Email/Password Login
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

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
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
        val playersRef = database.child("players")

        playersRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
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

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun logout() = auth.signOut()

    // Game management methods
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