// Port of: wholesaler ios/JewelIndia/Sources/Core/SessionStore.swift
package com.jewelindia.app.core.session

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.model.EmployeeGate
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.text.Credentials
import com.jewelindia.app.data.net.DbRefusal
import com.jewelindia.app.data.api.LikeBook
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The app's auth state — holds the user and phase, mirroring Supabase auth state
 * and owning the launch/login routing decision.
 */
class SessionStore(
    private val authRouter: AuthRouter,
    private val viewModeStore: ViewModeStore,
    private val supabase: SupabaseClient? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
    private val onSignOutCleanup: (suspend () -> Unit)? = null
) {
    private val client: SupabaseClient get() = supabase ?: JewelSupabase.client

    sealed interface Phase {
        data object Loading : Phase
        data class Unauthenticated(val destination: AppDestination) : Phase
        data class Authenticated(val destination: AppDestination) : Phase
    }

    enum class AuthEvent {
        InitialSession,
        SignedIn,
        TokenRefreshed,
        UserUpdated,
        SignedOut,
        PasswordRecovery
    }

    private val _phase = MutableStateFlow<Phase>(Phase.Loading)
    val phase: StateFlow<Phase> = _phase.asStateFlow()

    private val _user = MutableStateFlow<UserInfo?>(null)
    val user: StateFlow<UserInfo?> = _user.asStateFlow()

    private var watchJob: Job? = null

    /**
     * Starts mirroring Supabase auth state.
     */
    fun start() {
        if (watchJob != null) return
        watchJob = coroutineScope.launch {
            client.auth.sessionStatus.collect { status ->
                handleSessionStatus(status)
            }
        }
    }

    internal suspend fun handleSessionStatus(status: SessionStatus) {
        when (status) {
            is SessionStatus.Initializing -> {
                // Stay loading
            }
            is SessionStatus.Authenticated -> {
                val event = when (status.source) {
                    SessionSource.Storage -> AuthEvent.InitialSession
                    is SessionSource.Refresh -> AuthEvent.TokenRefreshed
                    is SessionSource.UserChanged, is SessionSource.UserIdentitiesChanged -> AuthEvent.UserUpdated
                    else -> AuthEvent.SignedIn
                }
                handle(event, status.session)
            }
            is SessionStatus.NotAuthenticated -> {
                if (status.isSignOut) {
                    handle(AuthEvent.SignedOut, null)
                } else {
                    handle(AuthEvent.InitialSession, null)
                }
            }
            is SessionStatus.RefreshFailure -> {
                // Ignore transient failure (SDK will retry)
            }
        }
    }

    /**
     * Begins password recovery flow. Sets the recovery phase latch so normal
     * sign-in routing doesn't destroy the password reset screen.
     */
    fun beginPasswordRecovery() {
        _phase.value = Phase.Unauthenticated(AppDestination.UpdatePassword)
    }

    internal suspend fun handle(event: AuthEvent, session: UserSession?) {
        when (event) {
            AuthEvent.PasswordRecovery -> {
                _user.value = session?.user
                _phase.value = Phase.Unauthenticated(AppDestination.UpdatePassword)
            }

            AuthEvent.InitialSession,
            AuthEvent.SignedIn,
            AuthEvent.TokenRefreshed,
            AuthEvent.UserUpdated -> {
                val currentUser = session?.user
                if (currentUser == null) {
                    _user.value = null
                    _phase.value = Phase.Unauthenticated(AppDestination.Entry(null))
                    return
                }
                _user.value = currentUser

                // If in update-password phase, it owns the app until password is changed
                if (_phase.value is Phase.Unauthenticated && (_phase.value as Phase.Unauthenticated).destination is AppDestination.UpdatePassword) {
                    return
                }

                // Mid-signup the auth stack owns navigation
                if (SignupFlow.isCompletingSignup) {
                    return
                }

                // Token refreshes must not re-route an authenticated user
                if (_phase.value is Phase.Authenticated && event == AuthEvent.TokenRefreshed) {
                    return
                }

                val destination = authRouter.destination(currentUser)
                _phase.value = if (isAuthFlow(destination)) {
                    Phase.Unauthenticated(destination)
                } else {
                    Phase.Authenticated(destination)
                }
            }

            AuthEvent.SignedOut -> {
                SignupFlow.isCompletingSignup = false
                _user.value = null
                _phase.value = Phase.Unauthenticated(AppDestination.Entry(null))
            }
        }
    }

    private fun isAuthFlow(destination: AppDestination): Boolean {
        return when (destination) {
            is AppDestination.Entry,
            is AppDestination.SignIn,
            is AppDestination.EmployeeLogin,
            AppDestination.UpdatePassword -> true
            else -> false
        }
    }

    // MARK: - Actions

    /**
     * Sign in with email or phone + password.
     */
    suspend fun signIn(identity: String, password: String): String? = withContext(Dispatchers.IO) {
        try {
            if (Credentials.isEmail(identity)) {
                client.auth.signInWith(Email) {
                    this.email = identity
                    this.password = password
                }
            } else {
                client.auth.signInWith(Phone) {
                    this.phone = identity
                    this.password = password
                }
            }
        } catch (e: Exception) {
            return@withContext e.message ?: Copy.genericFailure
        }

        val session = client.auth.currentSessionOrNull()
        val currentUser = session?.user ?: return@withContext Copy.genericFailure

        // Employee status gate
        if (authRouter.resolveRole(currentUser) == UserRole.employee) {
            val employeeGate = runCatching {
                client.postgrest["employees"]
                    .select(Columns.raw("status")) {
                        filter { eq("auth_user_id", currentUser.id) }
                        limit(1)
                    }
                    .decodeSingleOrNull<EmployeeGate>()
            }.getOrNull()

            if (employeeGate == null || employeeGate.status != "active") {
                runCatching { client.auth.signOut() }
                return@withContext Copy.employeeDeactivated
            }
        }

        withContext(Dispatchers.Main) {
            handle(AuthEvent.SignedIn, session)
        }
        null
    }

    /**
     * Signs out, clears per-user caches and returns to front door.
     */
    suspend fun signOut() {
        _user.value?.id?.let { viewModeStore.clear(it) }
        LikeBook.clear()
        onSignOutCleanup?.invoke()
        runCatching { client.auth.signOut() }
        _user.value = null
        _phase.value = Phase.Unauthenticated(AppDestination.Entry(null))
    }

    /**
     * Re-resolves destination from current session.
     */
    suspend fun refreshDestination() {
        val currentUser = client.auth.currentSessionOrNull()?.user
        if (currentUser == null) {
            _phase.value = Phase.Unauthenticated(AppDestination.Entry(null))
            return
        }
        _user.value = currentUser
        val destination = authRouter.destination(currentUser)
        _phase.value = if (isAuthFlow(destination)) {
            Phase.Unauthenticated(destination)
        } else {
            Phase.Authenticated(destination)
        }
    }

    fun returnToRoleChoice() {
        if (_phase.value is Phase.Authenticated) {
            _phase.value = Phase.Authenticated(AppDestination.SelectRole)
        }
    }

    suspend fun setUserRole(role: UserRole): String? = withContext(Dispatchers.IO) {
        if (client.auth.currentSessionOrNull()?.user == null) {
            return@withContext Copy.selectRoleNotAuthed
        }
        try {
            client.postgrest.rpc(
                "set_my_role",
                buildJsonObject { put("p_role", role.name) }
            )
            runCatching { client.auth.retrieveUserForCurrentSession() }
            refreshDestination()
            null
        } catch (e: Exception) {
            DbRefusal.message(e)
        }
    }

    suspend fun claimStaffInvite(signOutOnFailure: Boolean = true): String? = withContext(Dispatchers.IO) {
        try {
            if (client.auth.currentSessionOrNull()?.user == null) {
                return@withContext Copy.selectRoleNotAuthed
            }
            client.postgrest.rpc("claim_staff_invite")
            runCatching { client.auth.retrieveUserForCurrentSession() }
            refreshDestination()
            null
        } catch (e: Exception) {
            val message = DbRefusal.message(e)
            if (signOutOnFailure) {
                runCatching { client.auth.signOut() }
                _user.value = null
            }
            message
        } finally {
            SignupFlow.isCompletingSignup = false
        }
    }

    fun exitPasswordRecovery() {
        if (_phase.value is Phase.Unauthenticated && (_phase.value as Phase.Unauthenticated).destination is AppDestination.UpdatePassword) {
            _phase.value = Phase.Unauthenticated(AppDestination.Entry(null))
        }
    }

    suspend fun updatePassword(password: String): String? = withContext(Dispatchers.IO) {
        if (password.length < 6) return@withContext Copy.updateTooShort
        try {
            client.auth.updateUser {
                this.password = password
            }
            null
        } catch (e: Exception) {
            e.message ?: Copy.genericFailure
        }
    }

    suspend fun requestPasswordReset(email: String): String? = withContext(Dispatchers.IO) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return@withContext Copy.forgotEmailRequired
        try {
            client.auth.resetPasswordForEmail(
                trimmed.lowercase(),
                redirectUrl = "${AppConfig.authCallbackScheme}://auth/callback?next=/update-password"
            )
            null
        } catch (e: Exception) {
            e.message ?: Copy.genericFailure
        }
    }
}
