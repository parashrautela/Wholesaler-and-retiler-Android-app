// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/EntryView.swift
package com.jewelindia.app.feature.auth

import android.app.Activity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.session.AuthRouter
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.text.Credentials
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.net.ApiError
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.navigation.AuthRoute
import com.jewelindia.app.navigation.EntryMode
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun EntryScreen(
    mode: EntryMode,
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    authRouter: AuthRouter,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var identity by remember { mutableStateOf(signupFlow.identity ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }

    val heading = when (mode) {
        EntryMode.SignIn -> Copy.entrySignInHeading
        is EntryMode.Signup -> when (mode.role) {
            UserRole.retailer -> Copy.entryRetailerHeading
            else -> Copy.entryWholesalerHeading
        }
    }

    val subheading = when (mode) {
        EntryMode.SignIn -> Copy.entrySignInSubheading
        is EntryMode.Signup -> Copy.entrySignupSubheading
    }

    val identityLabel = if (mode is EntryMode.SignIn) {
        Copy.signInIdentityLabel
    } else {
        Copy.entryFieldLabel
    }

    val identityPlaceholder = if (mode is EntryMode.SignIn) {
        Copy.signInEmailPlaceholder
    } else {
        Copy.entryFieldPlaceholder
    }

    LaunchedEffect(mode) {
        if (mode is EntryMode.Signup) {
            signupFlow.chosenRole = mode.role
        }
    }

    AuthLayout(
        title = heading,
        subtitle = subheading,
        onBack = onBack
    ) {
        if (mode is EntryMode.Signup && mode.role == UserRole.retailer) {
            InvitationBanner(
                wholesaler = signupFlow.invitedBy,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        AuthFieldLabel(text = identityLabel)

        AuthTextField(
            value = identity,
            onValueChange = {
                identity = it
                if (error != null) error = null
            },
            placeholder = identityPlaceholder,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (error != null) {
            AuthErrorRow(
                message = error!!,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        AuthOrDivider()

        GoogleButton(
            isBusy = loading || googleLoading,
            onClick = {
                scope.launch {
                    val activity = context as? Activity ?: return@launch
                    googleLoading = true
                    SignupFlow.isCompletingSignup = true
                    when (val outcome = GoogleSignIn.signIn(activity)) {
                        GoogleOutcome.Cancelled -> {
                            SignupFlow.isCompletingSignup = false
                            googleLoading = false
                        }
                        is GoogleOutcome.Failed -> {
                            SignupFlow.isCompletingSignup = false
                            error = outcome.message
                            googleLoading = false
                        }
                        GoogleOutcome.SignedIn -> {
                            val user = JewelSupabase.client.auth.currentSessionOrNull()?.user
                            val existingRole = user?.let { authRouter.resolveRole(it) }
                            SignupFlow.isCompletingSignup = false
                            googleLoading = false
                            if (existingRole == null && mode is EntryMode.Signup) {
                                val msg = sessionStore.setUserRole(mode.role)
                                if (msg != null) error = msg
                            } else {
                                sessionStore.refreshDestination()
                            }
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthPrimaryButton(
            title = if (loading) Copy.entrySubmitBusy else Copy.entrySubmitIdle,
            isEnabled = !loading && !googleLoading && identity.trim().isNotEmpty(),
            isLoading = loading,
            onClick = {
                val raw = identity.trim()
                if (raw.isEmpty()) return@AuthPrimaryButton

                if (mode is EntryMode.SignIn && !Credentials.isEmail(raw)) {
                    error = "Sign in with your email and password. Phone number sign-in isn't available."
                    return@AuthPrimaryButton
                }

                scope.launch {
                    loading = true
                    error = null

                    val normalized: String
                    if (Credentials.isEmail(raw)) {
                        normalized = raw
                    } else {
                        val check = Credentials.validateIndianMobile(raw)
                        if (!check.valid || check.normalized == null) {
                            error = Copy.invalidMobile
                            loading = false
                            return@launch
                        }
                        normalized = check.normalized
                    }

                    try {
                        val result = JewelApi.checkUser(normalized)
                        if (result.exists) {
                            if (result.provider == "google") {
                                val activity = context as? Activity
                                if (activity != null) {
                                    googleLoading = true
                                    SignupFlow.isCompletingSignup = true
                                    when (val outcome = GoogleSignIn.signIn(activity)) {
                                        GoogleOutcome.Cancelled -> {
                                            SignupFlow.isCompletingSignup = false
                                        }
                                        is GoogleOutcome.Failed -> {
                                            SignupFlow.isCompletingSignup = false
                                            error = outcome.message
                                        }
                                        GoogleOutcome.SignedIn -> {
                                            SignupFlow.isCompletingSignup = false
                                            sessionStore.refreshDestination()
                                        }
                                    }
                                    googleLoading = false
                                }
                                loading = false
                                return@launch
                            }

                            signupFlow.identity = normalized
                            onNavigate(AuthRoute.SignIn(normalized))
                            loading = false
                            return@launch
                        }

                        // Account does not exist
                        if (mode is EntryMode.SignIn) {
                            error = Copy.entryNoAccount
                            loading = false
                            return@launch
                        }

                        // Send OTP for new user
                        val otp = JewelApi.sendOtp(normalized)
                        signupFlow.identity = normalized
                        signupFlow.otpSentAt = System.currentTimeMillis()
                        if (otp.remainingResends != null) {
                            signupFlow.remainingResends = otp.remainingResends
                        }
                        onNavigate(AuthRoute.VerifyOtp)
                    } catch (e: Exception) {
                        error = e.userMessage()
                    } finally {
                        loading = false
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AuthLegalText()
    }
}
