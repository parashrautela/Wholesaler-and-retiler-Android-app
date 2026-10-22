// Port of: wholesaler ios/JewelIndia/Sources/Networking/CreditsAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.EdgeFunctions
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object CreditsApi {
    private val db get() = JewelSupabase.client

    suspend fun fetchWallet(): CreditWallet {
        val wallet = db.postgrest.rpc("credits_wallet").decodeAs<CreditWallet>()
        if (!wallet.ok) {
            throw TopUpException("Credit balance is unavailable. Please try again.")
        }
        return wallet
    }

    suspend fun fetchRateCard(): List<CreditPrice> {
        return db.from("credit_prices").select {
            filter {
                eq("is_active", true)
            }
            order("sort_order", order = Order.ASCENDING)
        }.decodeList<CreditPrice>()
    }

    suspend fun fetchLedger(limit: Int = 50, before: String? = null, beforeId: String? = null): List<CreditLedgerEntry> {
        @Serializable
        data class History(val ok: Boolean, val data: List<CreditLedgerEntry>? = null)

        val params = buildJsonObject {
            put("p_limit", limit)
            if (before != null) put("p_before", before)
            if (beforeId != null) put("p_before_id", beforeId)
        }

        val result = db.postgrest.rpc("credits_history", params).decodeAs<History>()
        if (!result.ok) {
            throw TopUpException("Credit history is unavailable.")
        }
        return result.data ?: emptyList()
    }

    suspend fun fetchEntitlementKeys(): Set<String> {
        @Serializable
        data class Row(
            @SerialName("entitlement_key") val entitlementKey: String,
            @SerialName("expires_at") val expiresAt: String? = null
        )

        val rows = db.from("entitlements").select {
            // columns: entitlement_key, expires_at
        }.decodeList<Row>()

        val nowIso = java.time.Instant.now().toString()
        val keys = rows.filter { row ->
            row.expiresAt == null || row.expiresAt > nowIso
        }.map { it.entitlementKey }.toMutableSet()

        val myPlan = try { fetchMyPlan() } catch (_: Exception) { null }
        if (myPlan?.temporaryAccess == true) {
            keys.add("theme.utsav")
            keys.add("theme.neelam")
        }
        return keys
    }

    suspend fun purchaseEntitlement(key: String): EntitlementPurchase {
        return db.postgrest.rpc(
            "purchase_entitlement",
            buildJsonObject { put("p_key", key) }
        ).decodeAs<EntitlementPurchase>()
    }

    suspend fun purchaseExport(generationId: String): EntitlementPurchase {
        return db.postgrest.rpc(
            "purchase_export",
            buildJsonObject { put("p_generation", generationId) }
        ).decodeAs<EntitlementPurchase>()
    }

    suspend fun fetchPlans(): List<Plan> {
        return db.from("plans").select {
            order("sort_order", order = Order.ASCENDING)
        }.decodeList<Plan>()
    }

    suspend fun fetchMyPlan(): PlanStatus {
        return db.postgrest.rpc("my_plan").decodeAs<PlanStatus>()
    }

    suspend fun subscribe(planKey: String): PlanPurchase {
        return db.postgrest.rpc(
            "subscribe_plan",
            buildJsonObject { put("p_plan", planKey) }
        ).decodeAs<PlanPurchase>()
    }

    suspend fun setPlanAutoRenew(on: Boolean) {
        db.postgrest.rpc(
            "set_plan_auto_renew",
            buildJsonObject { put("p_on", on) }
        )
    }

    suspend fun fetchTopUpOptions(): TopUpOptions {
        return invokeTopUp(
            buildJsonObject {
                put("action", "options")
            }
        )
    }

    suspend fun createTopUpLink(packKey: String): TopUpLink {
        return invokeTopUp(
            buildJsonObject {
                put("action", "create")
                put("pack_key", packKey)
            }
        )
    }

    suspend fun createTopUpLink(amountINR: Int): TopUpLink {
        return invokeTopUp(
            buildJsonObject {
                put("action", "create")
                put("pack_key", "custom")
                put("amount_inr", amountINR)
            }
        )
    }

    suspend fun purchase(linkId: String): CreditPurchaseReceipt? {
        val rows = db.from("credit_purchases").select {
            filter {
                eq("provider_ref", linkId)
            }
            limit(1)
        }.decodeList<CreditPurchaseReceipt>()
        return rows.firstOrNull()
    }

    private suspend inline fun <reified T : Any> invokeTopUp(body: kotlinx.serialization.json.JsonObject): T {
        val token = AuthToken.fresh()
        return try {
            EdgeFunctions.invoke<T>("credits-topup", body = body, token = token)
        } catch (e: Exception) {
            throw TopUpException(e.message ?: "Payments aren't reachable right now. Please try again.")
        }
    }
}
