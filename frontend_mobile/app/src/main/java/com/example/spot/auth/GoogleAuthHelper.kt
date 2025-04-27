package com.example.spot.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task

/**
 * Helper class to manage Google Sign-In functionality
 */
class GoogleAuthHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleAuthHelper"
        const val RC_SIGN_IN = 9001
        private const val WEB_CLIENT_ID = "503985630476-48eoddohffjqobh2r1tpm7ldhclfc4ak.apps.googleusercontent.com" // Web Client ID
    }

    private val googleSignInClient: GoogleSignInClient

    init {
        // Configure sign-in to request the user's ID, email address, and basic profile
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Request email scope
            .requestIdToken(WEB_CLIENT_ID) // Add Web client ID for backend verification
            .requestProfile() // Request profile for additional user info
            .build()

        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Get the sign in intent to start the Google Sign-In flow
     * Use this to start an activity for result.
     */
    fun getSignInIntent(): Intent {
        // Sign out first to ensure the account picker dialog shows every time
        googleSignInClient.signOut()
        Log.d(TAG, "Getting sign-in intent after signing out previous accounts")
        return googleSignInClient.signInIntent
    }

    /**
     * Process the result from the sign-in Intent
     * @param data The Intent data returned from onActivityResult
     * @return The GoogleSignInAccount if successful, null otherwise
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        try {
            Log.d(TAG, "Processing Google sign-in result")
            if (data == null) {
                Log.e(TAG, "Sign-in intent data is null")
                return null
            }
            
            // The Task returned from this call is always completed, no need to attach a listener
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            
            // Log additional details about the task
            Log.d(TAG, "Google Sign-In task created, checking for result")
            
            if (task.isSuccessful) {
                Log.d(TAG, "Google Sign-In task successful, extracting account")
                val account = task.getResult(ApiException::class.java)
                
                if (account != null) {
                    Log.d(TAG, "Google Sign-In successful: ${account.email}, ID: ${account.id}, Token: ${account.idToken?.take(10)}...")
                    return account
                } else {
                    Log.e(TAG, "Google Sign-In result account is null even though task was successful")
                    return null
                }
            } else {
                Log.e(TAG, "Google Sign-In task unsuccessful: ${task.exception?.message}")
                throw task.exception ?: ApiException(Status(CommonStatusCodes.ERROR))
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason
            val statusCode = e.statusCode
            Log.e(TAG, "Google Sign-In failed with code: $statusCode, message: ${e.message}")
            
            // Better handling of specific error codes based on Google Sign-In API
            when (statusCode) {
                12501 -> { // SIGN_IN_CANCELLED
                    Log.d(TAG, "Sign-in was cancelled by the user")
                }
                7 -> { // NETWORK_ERROR
                    Log.e(TAG, "Network error during sign-in")
                }
                5 -> { // INVALID_ACCOUNT
                    Log.e(TAG, "Invalid account specified")
                }
                8 -> { // INTERNAL_ERROR
                    Log.e(TAG, "Internal error with Google Play Services")
                }
                13 -> { // TIMEOUT
                    Log.e(TAG, "Connection timeout")
                }
                14 -> { // INTERRUPTED
                    Log.e(TAG, "Operation interrupted")
                }
                16 -> { // API_NOT_CONNECTED
                    Log.e(TAG, "API not connected")
                }
                17 -> { // CANCELED
                    Log.e(TAG, "Operation canceled")
                }
                4 -> { // SIGN_IN_REQUIRED
                    Log.e(TAG, "Sign-in required")
                }
                else -> {
                    Log.e(TAG, "Other sign-in error with code: $statusCode")
                }
            }
            
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            return null
        }
    }

    /**
     * Check if the user is already signed in
     * @return The GoogleSignInAccount if the user is signed in, null otherwise
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        googleSignInClient.signOut()
    }
}