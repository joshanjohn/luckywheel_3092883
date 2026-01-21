package com.griffith.luckywheel.services

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.griffith.luckywheel.BuildConfig
import com.griffith.luckywheel.models.data.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Handles all authentication operations: Google Sign-In, email/password auth, and logout
class AuthenticationService(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firebaseService = FireBaseService()
    private val locationService = LocationService(context)
    
    private fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    // Returns intent to launch Google account picker
    fun getGoogleSignInIntent(): Intent {
        return getGoogleSignInClient().signInIntent
    }
    
    // Processes Google Sign-In result and authenticates with Firebase
    fun handleGoogleSignInResult(
        data: Intent?,
        onResult: (success: Boolean, message: String?, userId: String?) -> Unit
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
    
    // Authenticates with Firebase using Google credentials
    private fun signInWithGoogleAccount(
        account: GoogleSignInAccount,
        onResult: (success: Boolean, message: String?, userId: String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val displayName = user?.displayName ?: account.displayName ?: "User"
                    
                    if (userId != null) {
                        // Check if player exists in database, create if not
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            val result = firebaseService.checkAndCreatePlayerIfNeeded(userId, displayName)
                            
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                result.onSuccess { message ->
                                    // Fetch location in background after successful sign-in (non-blocking)
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                        try {
                                            val (city, country, countryCode) = locationService.getLocationOrDefault()
                                            firebaseService.updatePlayerLocation(userId, city, country, countryCode)
                                        } catch (e: Exception) {
                                            // Silently fail - location is optional
                                        }
                                    }
                                    onResult(true, message, userId)
                                }.onFailure { exception ->
                                    onResult(false, exception.message, userId)
                                }
                            }
                        }
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
    
    // Creates new user account with email and password
    fun registerWithEmailPassword(
        playerName: String,
        email: String,
        password: String,
        onResult: (success: Boolean, message: String?, userId: String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    
                    if (userId != null) {
                        // Set display name for Firebase Auth user
                        val profileUpdates = userProfileChangeRequest {
                            displayName = playerName
                        }
                        
                        auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            // Create player in database
                            CoroutineScope(Dispatchers.IO).launch {
                                val player = Player(
                                    playerId = userId,
                                    playerName = playerName,
                                    gold = 0
                                )
                                firebaseService.database.child("players").child(userId).setValue(player)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            // Fetch location in background (non-blocking)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val (city, country, countryCode) = locationService.getLocationOrDefault()
                                                    firebaseService.updatePlayerLocation(userId, city, country, countryCode)
                                                } catch (e: Exception) {
                                                    // Silently fail - location is optional
                                                }
                                            }
                                            onResult(true, "Registration successful", userId)
                                        } else {
                                            onResult(false, dbTask.exception?.message, userId)
                                        }
                                    }
                            }
                        }
                    } else {
                        onResult(false, "Failed to get user ID", null)
                    }
                } else {
                    onResult(false, task.exception?.message, null)
                }
            }
    }
    
    // Authenticates existing user with email and password
    fun loginWithEmailPassword(
        email: String,
        password: String,
        onResult: (success: Boolean, message: String?, userId: String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    onResult(true, "Login successful", userId)
                } else {
                    onResult(false, task.exception?.message, null)
                }
            }
    }
    
    // Sends password reset email to user
    fun sendPasswordResetEmail(
        email: String,
        onResult: (success: Boolean, message: String?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent")
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
    
    // Signs out from both Firebase and Google, ensuring account picker shows on next login
    fun logout(onComplete: () -> Unit = {}) {
        auth.signOut()
        getGoogleSignInClient().signOut().addOnCompleteListener {
            onComplete()
        }
    }
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun isUserAuthenticated(): Boolean = auth.currentUser != null
    
    // Permanently deletes the users Firebase auth account
    fun deleteAccount(onResult: (success: Boolean, message: String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Also sign out from Google
                        getGoogleSignInClient().signOut()
                        onResult(true, "Account deleted successfully")
                    } else {
                        // Log the error for debugging
                        val errorMessage = task.exception?.message ?: "Failed to delete account"
                        android.util.Log.e("AuthenticationService", "Delete account failed: $errorMessage", task.exception)
                        onResult(false, errorMessage)
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("AuthenticationService", "Delete account exception: ${exception.message}", exception)
                    onResult(false, exception.message ?: "Failed to delete account")
                }
        } else {
            onResult(false, "No user is currently signed in")
        }
    }
}
