// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/GoogleSignIn.swift
package com.jewelindia.app.feature.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.net.userMessage
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
import kotlin.time.Duration.Companion.seconds

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
            signInInBrowser(activity, supabase)
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
            return@withContext signInInBrowser(activity, supabase)
        } catch (e: Exception) {
            return@withContext GoogleOutcome.Failed(e.userMessage())
        }

        val credential = result.credential
        val googleIdTokenCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: Exception) {
            return@withContext GoogleOutcome.Failed(e.userMessage())
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
            GoogleOutcome.Failed(e.userMessage())
        }
    }

    private suspend fun signInInBrowser(activity: Activity, supabase: SupabaseClient): GoogleOutcome {
        val lifecycleOwner = activity as? LifecycleOwner
        val stateFlow = lifecycleOwner?.lifecycle?.currentStateFlow

        return try {
            supabase.auth.signInWith(Google, redirectUrl = "${AppConfig.authCallbackScheme}://auth/callback") {
                queryParams["access_type"] = "offline"
                queryParams["prompt"] = "consent"
            }

            if (stateFlow != null) {
                // Wait for lifecycle to drop below RESUMED (browser opened)
                val dropped = withTimeoutOrNull(3.seconds) {
                    stateFlow.first { it < Lifecycle.State.RESUMED }
                }

                if (dropped == null) {
                    // Custom tab couldn't launch, fall back to Intent(ACTION_VIEW)
                    try {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("${AppConfig.supabaseURL.trimEnd('/')}/auth/v1/authorize?provider=google&redirect_to=${AppConfig.authCallbackScheme}://auth/callback")
                        )
                        activity.startActivity(browserIntent)
                        withTimeoutOrNull(3.seconds) {
                            stateFlow.first { it < Lifecycle.State.RESUMED }
                        }
                    } catch (_: Exception) {
                        return GoogleOutcome.Cancelled
                    }
                }

                // Wait for lifecycle to return to RESUMED (user returned)
                stateFlow.first { it == Lifecycle.State.RESUMED }
            }

            // Once back at RESUMED, give the deep link a brief window to land
            val outcome = withTimeoutOrNull(3.seconds) {
                if (supabase.auth.currentSessionOrNull() != null) return@withTimeoutOrNull true
                supabase.auth.sessionStatus.first { status ->
                    status is SessionStatus.Authenticated
                }
                true
            }

            if (outcome == true || supabase.auth.currentSessionOrNull() != null) {
                GoogleOutcome.SignedIn
            } else {
                GoogleOutcome.Cancelled
            }
        } catch (e: Exception) {
            GoogleOutcome.Failed(e.userMessage())
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
