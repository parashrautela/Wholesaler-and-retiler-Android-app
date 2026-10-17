// Port of: wholesaler ios/JewelIndia/Sources/Networking/EmployeeAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.text.nilIfEmpty
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class EmployeeApiException(message: String) : RuntimeException(message)

object EmployeeApi {
    private val db get() = JewelSupabase.client

    suspend fun resolveSession(userId: String, isRetailer: Boolean): EmployeeSession {
        val uid = userId.lowercase()

        @Serializable
        data class EmployeeRow(
            val id: String,
            @SerialName("retailer_id") val retailerId: String,
            val status: String? = null
        )

        val rows = db.from("employees").select {
            filter {
                eq("auth_user_id", uid)
            }
            limit(1)
        }.decodeList<EmployeeRow>()
        val employee = rows.firstOrNull()

        if (!isRetailer) {
            if (employee == null || employee.status != "active") {
                throw EmployeeApiException(Copy.employeeDeactivated)
            }
        }

        val profile = storeProfile(uid, isRetailer)
        val retailerId = profile?.retailerId ?: employee?.retailerId
            ?: throw EmployeeApiException("No store is linked to this account.")

        val theme = EmployeeTheme.normalized(profile?.selectedTheme)

        return EmployeeSession(
            identity = employee?.let { EmployeeSession.Identity.Employee(it.id) } ?: EmployeeSession.Identity.Store,
            retailerId = retailerId.lowercase(),
            isRetailer = isRetailer,
            storeName = profile?.businessName?.trim()?.nilIfEmpty ?: "Your Store",
            storeLogoURL = profile?.businessLogoUrl?.trim()?.nilIfEmpty,
            theme = theme
        )
    }

    @Serializable
    private data class StoreProfile(
        @SerialName("retailer_id") val retailerId: String,
        @SerialName("business_name") val businessName: String? = null,
        @SerialName("business_logo_url") val businessLogoUrl: String? = null,
        @SerialName("selected_theme") val selectedTheme: String? = null
    )

    private suspend fun storeProfile(uid: String, isRetailer: Boolean): StoreProfile? {
        val rpcRows = try {
            db.postgrest.rpc("employee_store_profile").decodeList<StoreProfile>()
        } catch (_: Exception) { null }

        if (!rpcRows.isNullOrEmpty()) return rpcRows.first()
        if (!isRetailer) return null

        @Serializable
        data class OwnRow(
            val id: String,
            @SerialName("business_name") val businessName: String? = null,
            @SerialName("business_logo_url") val businessLogoUrl: String? = null,
            @SerialName("selected_theme") val selectedTheme: String? = null
        )

        val own = db.from("retailers").select {
            filter {
                eq("user_id", uid)
            }
            limit(1)
        }.decodeList<OwnRow>()

        return own.firstOrNull()?.let {
            StoreProfile(
                retailerId = it.id,
                businessName = it.businessName,
                businessLogoUrl = it.businessLogoUrl,
                selectedTheme = it.selectedTheme
            )
        }
    }

    suspend fun fetchDesigns(retailerId: String, newestFirst: Boolean = false): List<RetailerDesign> {
        return db.from("retailer_designs").select {
            filter {
                eq("retailer_id", retailerId)
                eq("is_archived", false)
            }
            if (newestFirst) {
                order("created_at", order = Order.DESCENDING)
            }
        }.decodeList<RetailerDesign>()
    }

    suspend fun fetchSelectedProducts(retailerId: String): List<Product> {
        @Serializable
        data class Selection(@SerialName("product_id") val productId: String)

        val selections = db.from("retailer_selections").select {
            filter {
                eq("retailer_id", retailerId)
            }
        }.decodeList<Selection>()

        val ids = selections.map { it.productId }
        if (ids.isEmpty()) return emptyList()

        return db.from("products").select {
            filter {
                eq("is_published", true)
                isIn("id", ids)
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList<Product>()
    }

    suspend fun fetchProducts(ids: List<String>): List<Product> {
        if (ids.isEmpty()) return emptyList()
        return db.from("products").select {
            filter {
                isIn("id", ids)
            }
        }.decodeList<Product>()
    }

    suspend fun hasUnreadQueries(session: EmployeeSession): Boolean {
        @Serializable
        data class Row(val id: String)

        return try {
            val rows = db.from("conversations").select {
                filter {
                    eq("messages.sender_type", "wholesaler")
                    eq("messages.is_read", false)
                    when (val id = session.identity) {
                        is EmployeeSession.Identity.Employee -> eq("employee_id", id.id)
                        EmployeeSession.Identity.Store -> eq("retailer_id", session.retailerId)
                    }
                }
                limit(1)
            }.decodeList<Row>()
            rows.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun latestOrderUpdate(session: EmployeeSession): String? {
        @Serializable
        data class Row(@SerialName("updated_at") val updatedAt: String? = null)

        return try {
            val rows = db.from("orders").select {
                filter {
                    neq("status", "pending")
                    when (val id = session.identity) {
                        is EmployeeSession.Identity.Employee -> eq("employee_id", id.id)
                        EmployeeSession.Identity.Store -> eq("retailer_id", session.retailerId)
                    }
                }
                order("updated_at", order = Order.DESCENDING)
                limit(1)
            }.decodeList<Row>()
            rows.firstOrNull()?.updatedAt
        } catch (_: Exception) {
            null
        }
    }
}
