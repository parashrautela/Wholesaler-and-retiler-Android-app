// Port of: wholesaler ios/JewelIndia/Sources/Core/Models.swift
package com.jewelindia.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The three roles stored in `auth.users.raw_user_meta_data.role` and mirrored
 * into `public.profiles.role`.
 */
@Serializable
enum class UserRole {
    @SerialName("wholesaler") wholesaler,
    @SerialName("retailer") retailer,
    @SerialName("employee") employee
}

/**
 * `WHOLESALERS_TABLE.sql`:
 * `check (verification_status in ('pending','verified','rejected','on_hold','resubmission_required','banned'))`
 */
@Serializable
enum class VerificationStatus {
    @SerialName("pending") pending,
    @SerialName("verified") verified,
    @SerialName("rejected") rejected,
    @SerialName("on_hold") onHold,
    @SerialName("resubmission_required") resubmissionRequired,
    @SerialName("banned") banned
}

/**
 * `jewel_view_mode` — a 7-day cookie on the web, written only by
 * `POST /api/auth/toggle-view`. Absent is treated as `.employee` for a
 * verified retailer.
 */
@Serializable
enum class ViewMode {
    @SerialName("employee") employee,
    @SerialName("retailer") retailer
}

// MARK: - Row shapes

@Serializable
data class WholesalerGate(
    @SerialName("verification_status") val verificationStatus: VerificationStatus? = null,
    @SerialName("has_visited_dashboard") val hasVisitedDashboard: Boolean? = null
)

@Serializable
data class RetailerGate(
    @SerialName("verification_status") val verificationStatus: VerificationStatus? = null
)

@Serializable
data class Retailer(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    val city: String? = null,
    val state: String? = null,
    val phone: String? = null
)

@Serializable
data class EmployeeGate(
    val status: String? = null
)

@Serializable
data class ProfileRole(
    val role: UserRole? = null
)

// MARK: - Destinations

/**
 * Every terminal destination the web router can produce, from the
 * role × verification_status table in the auth spec (§5.4).
 */
sealed interface AppDestination {
    /** `/entry_page/signup` — the real front door. */
    data class Entry(val error: String? = null) : AppDestination

    /** `/entry_page/signin?identity=…` */
    data class SignIn(val identity: String) : AppDestination

    /** `/select-role` — signed in with no role. */
    data object SelectRole : AppDestination

    /** `/onboard` */
    data object WholesalerOnboarding : AppDestination

    /** `/onboard/submitted` */
    data object WholesalerSubmitted : AppDestination

    /** `/dashboard/wholesaler` */
    data object WholesalerDashboard : AppDestination

    /** `/onboard-retailer` */
    data object RetailerOnboarding : AppDestination

    /** `/onboard-retailer/submitted` */
    data object RetailerSubmitted : AppDestination

    /** `/dashboard/retailer` */
    data object RetailerDashboard : AppDestination

    /** `/dashboard/employee` */
    data object EmployeeDashboard : AppDestination

    /** `/employee-login?error=deactivated` */
    data class EmployeeLogin(val error: String? = null) : AppDestination

    /** Network/Supabase lookup failed. */
    data object Unreachable : AppDestination

    /** `/update-password` — reached by following a recovery link. */
    data object UpdatePassword : AppDestination
}
