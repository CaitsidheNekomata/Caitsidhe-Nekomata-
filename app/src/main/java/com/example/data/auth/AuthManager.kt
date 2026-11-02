package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    val currentUser get() = auth.currentUser

    private fun getWebClientId(): String? {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e("AuthManager", "default_web_client_id not found in resources", e)
            null
        }
    }

    suspend fun attemptAutoSignIn(): Boolean {
        if (auth.currentUser != null) return true
        val clientId = getWebClientId() ?: return false

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            handleCredentialResult(result.credential)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInWithGoogle(activity: Activity): Result<Unit> {
        val clientId = getWebClientId()
            ?: return Result.failure(IllegalStateException("Google Sign-In configuration missing"))

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            if (handleCredentialResult(result.credential)) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Unknown credential received"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("AuthManager", "Sign-in cancelled by user: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    private suspend fun handleCredentialResult(credential: androidx.credentials.Credential): Boolean {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(authCredential).await()
            return true
        }
        return false
    }

    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthManager", "Error clearing credential state", e)
        }
    }
}
