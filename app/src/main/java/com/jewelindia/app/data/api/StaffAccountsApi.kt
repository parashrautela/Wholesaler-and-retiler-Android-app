// Port of: wholesaler ios/JewelIndia/Sources/Networking/StaffAccountsAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.EdgeFunctions
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class StaffApiException(message: String) : RuntimeException(message)

data class StaffCredentials(
    val member: StaffMember,
    val password: String
)

object StaffAccountsApi {
    private val db get() = JewelSupabase.client
    const val USERNAME_DOMAIN = "jewelindia.shop"

    private val _staffChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val staffChanged: SharedFlow<Unit> = _staffChanged.asSharedFlow()

    fun address(typed: String): String {
        val text = typed.trim().lowercase()
        return if (text.contains("@")) text else "$text@$USERNAME_DOMAIN"
    }

    suspend fun fetchStaff(): List<StaffMember> {
        return db.from("employees").select {
            filter {
                eq("is_system_generated", false)
            }
            order("created_at", order = Order.ASCENDING)
        }.decodeList<StaffMember>()
    }

    @Serializable
    private data class Request(
        val action: String,
        @SerialName("full_name") val fullName: String? = null,
        val username: String? = null,
        val email: String? = null,
        val designation: String? = null,
        val phone: String? = null,
        @SerialName("employee_id") val employeeId: String? = null,
        val status: String? = null
    )

    @Serializable
    private data class Reply(
        val ok: Boolean = false,
        val employee: StaffMember? = null,
        val password: String? = null,
        val username: String? = null,
        val removed: String? = null
    )

    private suspend fun invoke(request: Request): Reply {
        val token = AuthToken.fresh()
            ?: throw StaffApiException("Please sign in again.")

        return try {
            EdgeFunctions.invoke<Reply>("staff-accounts", body = request, token = token)
        } catch (e: Exception) {
            throw StaffApiException(e.message ?: "Staff accounts aren't reachable right now. Please try again.")
        }
    }

    suspend fun suggestUsername(fullName: String): String {
        val reply = invoke(Request(action = "suggest", fullName = fullName))
        return reply.username ?: throw StaffApiException(Copy.genericFailure)
    }

    suspend fun createLogin(
        fullName: String,
        username: String,
        designation: String?,
        phone: String?
    ): StaffCredentials {
        val reply = invoke(
            Request(
                action = "create",
                fullName = fullName,
                username = username,
                designation = designation,
                phone = phone
            )
        )
        val member = reply.employee
        val password = reply.password
        if (member == null || password == null) {
            throw StaffApiException(Copy.genericFailure)
        }
        _staffChanged.tryEmit(Unit)
        return StaffCredentials(member, password)
    }

    suspend fun inviteGoogle(
        fullName: String,
        email: String,
        designation: String?,
        phone: String?
    ): StaffMember {
        val reply = invoke(
            Request(
                action = "invite_google",
                fullName = fullName,
                email = email,
                designation = designation,
                phone = phone
            )
        )
        val member = reply.employee ?: throw StaffApiException(Copy.genericFailure)
        _staffChanged.tryEmit(Unit)
        return member
    }

    suspend fun resetPassword(id: String): StaffCredentials {
        val reply = invoke(Request(action = "reset_password", employeeId = id))
        val member = reply.employee
        val password = reply.password
        if (member == null || password == null) {
            throw StaffApiException(Copy.genericFailure)
        }
        return StaffCredentials(member, password)
    }

    suspend fun setActive(id: String, active: Boolean): StaffMember {
        val reply = invoke(
            Request(
                action = "set_status",
                employeeId = id,
                status = if (active) "active" else "inactive"
            )
        )
        val member = reply.employee ?: throw StaffApiException(Copy.genericFailure)
        _staffChanged.tryEmit(Unit)
        return member
    }

    suspend fun remove(member: StaffMember) {
        val action = if (member.status == StaffStatus.Invited) "cancel_invite" else "remove"
        invoke(Request(action = action, employeeId = member.id))
        _staffChanged.tryEmit(Unit)
    }
}
