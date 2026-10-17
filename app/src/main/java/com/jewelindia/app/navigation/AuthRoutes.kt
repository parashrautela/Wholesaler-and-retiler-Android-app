// Port of: wholesaler ios/JewelIndia/Sources/Navigation/AuthFlowView.swift (AuthRoute, EntryMode)
package com.jewelindia.app.navigation

import com.jewelindia.app.core.model.UserRole
import kotlinx.serialization.Serializable

@Serializable
sealed interface EntryMode {
    @Serializable
    data object SignIn : EntryMode

    @Serializable
    data class Signup(val role: UserRole) : EntryMode
}

@Serializable
sealed interface AuthRoute {
    @Serializable
    data object RoleChoice : AuthRoute

    @Serializable
    data class Entry(val isSignup: Boolean = false, val role: UserRole? = null) : AuthRoute {
        constructor(mode: EntryMode) : this(
            isSignup = mode is EntryMode.Signup,
            role = (mode as? EntryMode.Signup)?.role
        )

        val mode: EntryMode
            get() = if (isSignup && role != null) EntryMode.Signup(role) else EntryMode.SignIn
    }

    @Serializable
    data object InviteCode : AuthRoute

    @Serializable
    data object EmployeeSignIn : AuthRoute

    @Serializable
    data class SignIn(val identity: String) : AuthRoute

    @Serializable
    data object VerifyOtp : AuthRoute

    @Serializable
    data object SetPassword : AuthRoute

    @Serializable
    data object ForgotPassword : AuthRoute

    @Serializable
    data object UpdatePassword : AuthRoute
}
