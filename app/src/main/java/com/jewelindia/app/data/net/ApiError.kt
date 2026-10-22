// Port of: wholesaler ios/JewelIndia/Sources/Networking/JewelAPI.swift (APIError, throwIfError) & InviteAPI.swift (DBRefusal)
package com.jewelindia.app.data.net

import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.time.JewelDates
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException

class ApiError(
    val status: Int,
    override val message: String,
    val locked: Boolean = false,
    val lockedUntil: Long? = null,
    val cooldown: Boolean = false,
    val waitSeconds: Int? = null
) : Exception(message) {
    override fun toString(): String = "ApiError(status=$status, message='$message')"
}

object DbRefusal {
    val knownCodes = listOf(
        "INVITE_CODE_REQUIRED", "INVITE_CODE_NOT_FOUND", "INVITE_CODE_EXPIRED", "INVITE_CODE_USED",
        "INVITE_CODE_INACTIVE", "RETAILER_ALREADY_ATTRIBUTED", "RETAILER_ALREADY_VERIFIED",
        "RETAILER_HAS_NO_INVITER", "ROLE_ALREADY_SET", "ROLE_NOT_ALLOWED", "NOT_SIGNED_IN",
        "NOT_INVITED", "GOOGLE_SIGN_IN_REQUIRED", "ACCOUNT_HAS_ANOTHER_ROLE"
    )

    fun code(e: Throwable): String? {
        val text = when (e) {
            is PostgrestRestException -> e.message ?: e.error
            else -> e.message ?: ""
        }
        return knownCodes.firstOrNull { text.contains(it) }
    }

    fun message(e: Throwable): String {
        val pg = e as? PostgrestRestException
        val cd = pg?.code ?: code(e)
        val shortText = (e as? RestException)?.error ?: e.message ?: ""
        return friendlyDbText(shortText, cd)
    }
}

internal fun friendlyDbText(error: String, code: String?): String {
    if (code == "PT403" || error.contains("locked by Jewel India admin")) {
        return Copy.featureLocked
    }
    val matchedCode = listOfNotNull(code, error).firstNotNullOfOrNull { text ->
        DbRefusal.knownCodes.firstOrNull { text.contains(it) }
    }
    if (matchedCode != null) {
        return when (matchedCode) {
            "INVITE_CODE_REQUIRED" -> Copy.inviteCodeEmpty
            "INVITE_CODE_NOT_FOUND" -> Copy.inviteCodeReason("not_found")
            "INVITE_CODE_EXPIRED" -> Copy.inviteCodeReason("expired")
            "INVITE_CODE_USED" -> Copy.inviteCodeReason("used")
            "INVITE_CODE_INACTIVE" -> Copy.inviteCodeReason("inactive")
            "RETAILER_ALREADY_ATTRIBUTED" -> "Your store is already linked to the wholesaler who invited you."
            "RETAILER_ALREADY_VERIFIED" -> "Your store is already verified — no code needed."
            "ROLE_ALREADY_SET" -> Copy.roleAlreadySet
            "ROLE_NOT_ALLOWED" -> Copy.roleNotAllowed
            "NOT_SIGNED_IN" -> "Please sign in again."
            "NOT_INVITED" -> Copy.staffNotInvited
            "GOOGLE_SIGN_IN_REQUIRED" -> Copy.staffGoogleRequired
            "ACCOUNT_HAS_ANOTHER_ROLE" -> Copy.staffAccountHasAnotherRole
            else -> Copy.genericFailure
        }
    }
    val firstLine = error.substringBefore('\n').trim()
    return if (firstLine.isNotBlank()) firstLine else Copy.genericFailure
}

fun parseApiError(statusCode: Int, body: String): ApiError {
    return try {
        val element = Json.parseToJsonElement(body).jsonObject
        val msg = element["error"]?.jsonPrimitive?.content
            ?: element["message"]?.jsonPrimitive?.content
            ?: element["detail"]?.jsonPrimitive?.content
            ?: Copy.genericFailure

        val locked = element["locked"]?.jsonPrimitive?.booleanOrNull ?: false
        val cooldown = element["cooldown"]?.jsonPrimitive?.booleanOrNull ?: false
        val waitSeconds = element["waitSeconds"]?.jsonPrimitive?.intOrNull

        var lockedUntilEpoch: Long? = null
        val lockedUntilStr = element["lockedUntil"]?.jsonPrimitive?.content
        if (!lockedUntilStr.isNullOrBlank()) {
            lockedUntilEpoch = JewelDates.parse(lockedUntilStr)?.toEpochMilli()
        }

        ApiError(
            status = statusCode,
            message = msg,
            locked = locked,
            lockedUntil = lockedUntilEpoch,
            cooldown = cooldown,
            waitSeconds = waitSeconds
        )
    } catch (_: Exception) {
        ApiError(
            status = statusCode,
            message = if (body.isNotBlank() && body.length < 200) body else Copy.genericFailure
        )
    }
}

fun Throwable.userMessage(): String {
    return when (this) {
        is ApiError -> message
        is PostgrestRestException -> DbRefusal.message(this)
        is RestException -> friendlyDbText(this.error, null)
        is HttpRequestException -> {
            val c = cause
            if (c != null && JewelNetwork.isTransient(c)) Copy.networkError else friendlyDbText(message ?: "", null)
        }
        is IOException -> Copy.networkError
        else -> friendlyDbText(message ?: "", null)
    }
}
