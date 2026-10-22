// Port of: wholesaler ios/JewelIndia/Sources/Networking/OnboardingFeeAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.EdgeFunctions
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class FeeException(message: String) : RuntimeException(message)

object OnboardingFeeApi {

    @Serializable
    data class Status(
        @SerialName("amount_inr") val amountINR: Int,
        val required: Boolean,
        val payable: Boolean,
        val paid: Boolean
    ) {
        val mustPay: Boolean get() = required && payable && !paid
    }

    @Serializable
    data class Link(
        val paid: Boolean? = null,
        @SerialName("link_id") val linkID: String? = null,
        val url: String? = null
    )

    suspend fun status(): Status {
        return call(buildJsonObject { put("action", "onboarding_status") })
    }

    suspend fun createLink(): Link {
        return call(buildJsonObject { put("action", "onboarding_pay") })
    }

    suspend fun confirm(linkID: String): Boolean {
        @Serializable
        data class Reply(val paid: Boolean)

        val reply: Reply = call(
            buildJsonObject {
                put("action", "onboarding_confirm")
                put("link_id", linkID)
            }
        )
        return reply.paid
    }

    private suspend inline fun <reified T : Any> call(body: kotlinx.serialization.json.JsonObject): T {
        val token = AuthToken.fresh()

        return try {
            EdgeFunctions.invoke<T>("credits-topup", body = body, token = token)
        } catch (e: Exception) {
            throw FeeException(e.message ?: "Payments aren't reachable right now. Please try again.")
        }
    }
}
