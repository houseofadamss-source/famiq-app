package com.famiq.app

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.onesignal.OneSignal
import kotlinx.coroutines.tasks.await

object GoogleAuthHelper {

    private const val TAG = "GoogleAuthHelper"
    private val WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID

    suspend fun signIn(context: Context): Result<String> {
        return try {
            Log.d(TAG, "Starting sign in...")
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "Getting credential...")
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            Log.d(TAG, "Credential type: ${credential.type}")

            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdToken.idToken
            Log.d(TAG, "Got ID token, signing in to Firebase...")

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            Log.d(TAG, "Firebase sign in success: ${authResult.user?.displayName}")

            // ✅ LOGIN KE ONESIGNAL (Biar ID Sync)
            authResult.user?.email?.let { email ->
                OneSignal.login(email)
                OneSignal.User.pushSubscription.optIn()
            }

            val user = auth.currentUser
            Result.success(user?.displayName ?: "User")

        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: ${e.message}", e)
            Log.e(TAG, "Error type: ${e.type}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during sign in: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        OneSignal.logout()
    }

    fun getNamaUser(): String {
        return FirebaseAuth.getInstance().currentUser?.displayName ?: ""
    }

    fun getEmailUser(): String {
        return FirebaseAuth.getInstance().currentUser?.email ?: ""
    }

    fun getFotoUrl(): String? {                                          // ✅ di dalam object
        return FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
    }
}   // ✅ kurung tutup object di sini