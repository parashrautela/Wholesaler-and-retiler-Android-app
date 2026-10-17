// Port of: wholesaler ios/JewelIndia/Sources/Networking/StaffAccountsAPI.swift (Models)
package com.jewelindia.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StaffStatus {
    @SerialName("active") Active,
    @SerialName("inactive") Inactive,
    @SerialName("invited") Invited
}

@Serializable
data class StaffMember(
    val id: String,
    @SerialName("full_name") val fullName: String = "Staff",
    val email: String = "",
    @SerialName("invite_email") val inviteEmail: String? = null,
    val designation: String? = null,
    val phone: String? = null,
    val status: StaffStatus = StaffStatus.Inactive,
    @SerialName("join_method") val joinMethod: String = "password",
    @SerialName("is_system_generated") val isSystemGenerated: Boolean = false
) {
    val signsInWithGoogle: Boolean get() = joinMethod == "google"

    val username: String?
        get() {
            if (signsInWithGoogle) return null
            val at = email.lastIndexOf('@')
            if (at <= 0) return null
            return email.substring(0, at)
        }

    val authUsername: String? get() = username
    val isLoginAccount: Boolean get() = !signsInWithGoogle

    val signInLabel: String
        get() = if (signsInWithGoogle) (inviteEmail ?: email) else email
}
