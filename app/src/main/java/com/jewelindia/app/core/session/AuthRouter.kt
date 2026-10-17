// Port of: wholesaler ios/JewelIndia/Sources/Core/AuthRouter.swift
package com.jewelindia.app.core.session

import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.model.EmployeeGate
import com.jewelindia.app.core.model.ProfileRole
import com.jewelindia.app.core.model.RetailerGate
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.model.ViewMode
import com.jewelindia.app.core.model.WholesalerGate
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * A row lookup with the three answers it can actually give.
 */
sealed interface Gate<out T> {
    data class Found<T>(val row: T) : Gate<T>
    data object Missing : Gate<Nothing>
    data object Unreachable : Gate<Nothing>
}

/**
 * Interface allowing AuthRouter to be thoroughly tested with a test double
 * covering all routing branches and side effects.
 */
interface GateProvider {
    suspend fun fetchProfileRole(userId: String): Gate<ProfileRole>
    suspend fun fetchWholesalerGate(userId: String): Gate<WholesalerGate>
    suspend fun fetchRetailerGate(userId: String): Gate<RetailerGate>
    suspend fun fetchEmployeeGate(userId: String): Gate<EmployeeGate>
    suspend fun signOut()
}

class SupabaseGateProvider(
    private val supabase: SupabaseClient = JewelSupabase.client
) : GateProvider {

    private suspend inline fun <reified T : Any> querySingle(
        table: String,
        columns: String,
        column: String,
        value: String
    ): Gate<T> {
        return try {
            val list = supabase.postgrest[table]
                .select(Columns.raw(columns)) {
                    filter { eq(column, value) }
                    limit(1)
                }
                .decodeList<T>()
            val first = list.firstOrNull()
            if (first != null) Gate.Found(first) else Gate.Missing
        } catch (_: Exception) {
            Gate.Unreachable
        }
    }

    override suspend fun fetchProfileRole(userId: String): Gate<ProfileRole> {
        return querySingle("profiles", "role", "id", userId)
    }

    override suspend fun fetchWholesalerGate(userId: String): Gate<WholesalerGate> {
        return querySingle("wholesalers", "verification_status, has_visited_dashboard", "user_id", userId)
    }

    override suspend fun fetchRetailerGate(userId: String): Gate<RetailerGate> {
        return querySingle("retailers", "verification_status", "user_id", userId)
    }

    override suspend fun fetchEmployeeGate(userId: String): Gate<EmployeeGate> {
        return querySingle("employees", "status", "auth_user_id", userId)
    }

    override suspend fun signOut() {
        runCatching { supabase.auth.signOut() }
    }
}

/**
 * The launch/login router — the native replacement for the Next proxy
 * (`lib/supabase/middleware.js`) plus the destination helpers in `lib/actions/auth.js`.
 */
class AuthRouter(
    private val viewModeStore: ViewModeStore,
    private val gateProvider: GateProvider = SupabaseGateProvider()
) {

    /**
     * Resolves where a signed-in user belongs.
     */
    suspend fun destination(user: UserInfo): AppDestination {
        val role: UserRole? = metadataRole(user) ?: run {
            when (val profile = gateProvider.fetchProfileRole(user.id)) {
                is Gate.Found -> profile.row.role
                is Gate.Missing -> null
                is Gate.Unreachable -> return AppDestination.Unreachable
            }
        }

        return when (role) {
            UserRole.wholesaler -> wholesalerDestination(user.id)
            UserRole.retailer -> retailerDestination(user.id)
            UserRole.employee -> employeeDestination(user.id)
            null -> AppDestination.SelectRole
        }
    }

    suspend fun resolveRole(user: UserInfo): UserRole? {
        metadataRole(user)?.let { return it }
        val profile = gateProvider.fetchProfileRole(user.id)
        return if (profile is Gate.Found) profile.row.role else null
    }

    fun metadataRole(user: UserInfo): UserRole? {
        val raw = user.userMetadata?.get("role")?.jsonPrimitive?.contentOrNull ?: return null
        return runCatching { UserRole.valueOf(raw) }.getOrNull()
    }

    suspend fun wholesalerDestination(userId: String): AppDestination {
        return when (val result = gateProvider.fetchWholesalerGate(userId)) {
            is Gate.Unreachable -> AppDestination.Unreachable
            is Gate.Missing -> AppDestination.WholesalerOnboarding
            is Gate.Found -> {
                when (result.row.verificationStatus) {
                    VerificationStatus.banned -> {
                        gateProvider.signOut()
                        AppDestination.Entry(Copy.bannedError)
                    }
                    VerificationStatus.verified -> {
                        if (result.row.hasVisitedDashboard == true) {
                            AppDestination.WholesalerDashboard
                        } else {
                            AppDestination.WholesalerSubmitted
                        }
                    }
                    else -> AppDestination.WholesalerSubmitted
                }
            }
        }
    }

    suspend fun retailerDestination(userId: String): AppDestination {
        return when (val result = gateProvider.fetchRetailerGate(userId)) {
            is Gate.Unreachable -> AppDestination.Unreachable
            is Gate.Missing -> AppDestination.RetailerOnboarding
            is Gate.Found -> {
                when (result.row.verificationStatus) {
                    VerificationStatus.banned -> {
                        gateProvider.signOut()
                        AppDestination.Entry(Copy.bannedError)
                    }
                    VerificationStatus.verified -> {
                        if (viewModeStore.mode(userId) == ViewMode.retailer) {
                            AppDestination.RetailerDashboard
                        } else {
                            AppDestination.EmployeeDashboard
                        }
                    }
                    else -> AppDestination.RetailerSubmitted
                }
            }
        }
    }

    suspend fun employeeDestination(userId: String): AppDestination {
        return when (val result = gateProvider.fetchEmployeeGate(userId)) {
            is Gate.Unreachable -> AppDestination.Unreachable
            is Gate.Missing -> {
                gateProvider.signOut()
                AppDestination.EmployeeLogin(Copy.employeeDeactivated)
            }
            is Gate.Found -> {
                if (result.row.status != "active") {
                    gateProvider.signOut()
                    AppDestination.EmployeeLogin(Copy.employeeDeactivated)
                } else {
                    AppDestination.EmployeeDashboard
                }
            }
        }
    }
}
