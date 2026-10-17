// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/GoogleSignIn.swift
package com.jewelindia.app.feature.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.time.Duration.Companion.minutes

sealed interface GoogleOutcome {
    data object SignedIn : GoogleOutcome
    data object Cancelled : GoogleOutcome
    data class Failed(val message: String) : GoogleOutcome
}

/**
 * Google sign-in handler on Android supporting native CredentialManager
 * and fallback to in-browser OAuth.
 */
object GoogleSignIn {

    suspend fun signIn(
        activity: Activity,
        supabase: SupabaseClient = JewelSupabase.client
    ): GoogleOutcome {
        return if (AppConfig.supportsNativeGoogleSignIn) {
            signInNatively(activity, supabase)
        } else {
            signInInBrowser(supabase)
        }
    }

    private suspend fun signInNatively(
        activity: Activity,
        supabase: SupabaseClient
    ): GoogleOutcome = withContext(Dispatchers.Main) {
        val rawNonce = randomNonce()
        val hashedNonce = sha256Hex(rawNonce)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(AppConfig.googleWebClientId)
            .setAutoSelectEnabled(false)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activity)

        val result = try {
            credentialManager.getCredential(activity, request)
        } catch (_: GetCredentialCancellationException) {
            return@withContext GoogleOutcome.Cancelled
        } catch (_: NoCredentialException) {
            // No Google account configured on device — fall back to browser flow
            return@withContext signInInBrowser(supabase)
        } catch (e: Exception) {
            return@withContext GoogleOutcome.Failed(e.message ?: Copy.genericFailure)
        }

        val credential = result.credential
        val googleIdTokenCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: Exception) {
            return@withContext GoogleOutcome.Failed(e.message ?: Copy.genericFailure)
        }

        val idToken = googleIdTokenCredential.idToken
        try {
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = Google
                this.nonce = rawNonce
            }
            GoogleOutcome.SignedIn
        } catch (e: Exception) {
            GoogleOutcome.Failed(e.message ?: Copy.genericFailure)
        }
    }

    private suspend fun signInInBrowser(supabase: SupabaseClient): GoogleOutcome {
        return try {
            supabase.auth.signInWith(Google, redirectUrl = "${AppConfig.authCallbackScheme}://auth/callback") {
                queryParams["access_type"] = "offline"
                queryParams["prompt"] = "consent"
            }

            // Wait for session status to become Authenticated or timeout after 5 minutes
            val outcome = withTimeoutOrNull(5.minutes) {
                supabase.auth.sessionStatus.first { status ->
                    status is SessionStatus.Authenticated || status is SessionStatus.NotAuthenticated
                }
            }

            if (outcome is SessionStatus.Authenticated) {
                GoogleOutcome.SignedIn
            } else {
                GoogleOutcome.Cancelled
            }
        } catch (e: Exception) {
            GoogleOutcome.Failed(e.message ?: Copy.genericFailure)
        }
    }

    private fun randomNonce(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        val charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._"
        return bytes.map { charset[(it.toInt() and 0xFF) % charset.length] }.joinToString("")
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
