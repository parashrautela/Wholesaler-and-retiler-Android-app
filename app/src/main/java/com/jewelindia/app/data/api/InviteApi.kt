// Port of: wholesaler ios/JewelIndia/Sources/Networking/InviteAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.text.nilIfEmpty
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed interface InviteCheck {
    data class Valid(val code: String, val wholesaler: String?) : InviteCheck
    data class Invalid(val reason: String, val message: String) : InviteCheck

    val isValid: Boolean get() = this is Valid
}

object InviteApi {
    private val db get() = JewelSupabase.client

    @Serializable
    private data class Answer(
        val valid: Boolean,
        val reason: String? = null,
        val code: String? = null,
        @SerialName("wholesaler_name") val wholesalerName: String? = null
    )

    suspend fun validate(raw: String): InviteCheck {
        val code = raw.trim().uppercase()
        if (code.isEmpty()) {
            return InviteCheck.Invalid(reason = "no_code", message = Copy.inviteCodeEmpty)
        }

        val answer = db.postgrest.rpc(
            "validate_referral_code",
            buildJsonObject { put("p_code", code) }
        ).decodeAs<Answer>()

        if (answer.valid) {
            return InviteCheck.Valid(
                code = answer.code ?: code,
                wholesaler = answer.wholesalerName?.trim()?.nilIfEmpty
            )
        }
        val reason = answer.reason ?: "invalid"
        return InviteCheck.Invalid(reason = reason, message = Copy.inviteCodeReason(reason))
    }

    suspend fun attach(raw: String) {
        db.postgrest.rpc(
            "attach_referral_code",
            buildJsonObject { put("p_code", raw.trim().uppercase()) }
        )
    }
}
