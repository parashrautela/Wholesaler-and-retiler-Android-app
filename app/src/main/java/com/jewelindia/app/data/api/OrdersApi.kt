// Port of: wholesaler ios/JewelIndia/Sources/Networking/OrdersChatAPI.swift (OrdersAPI)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class OrderActionException(message: String) : RuntimeException(message)

object OrdersApi {
    private val db get() = JewelSupabase.client

    suspend fun setStatus(orderId: String, status: OrderStatus, reason: String? = null) {
        @Serializable
        data class Reply(val ok: Boolean, val error: String? = null, val from: String? = null)

        val params = buildJsonObject {
            put("p_order", orderId)
            put("p_status", status.name)
            if (reason != null) put("p_reason", reason)
        }

        val reply = db.postgrest.rpc("order_set_status", params).decodeAs<Reply>()
        if (reply.ok) return

        when (reply.error) {
            "INVALID_TRANSITION" -> throw OrderActionException("This order has already moved on. Pull down to refresh.")
            "NOT_FOUND" -> throw OrderActionException("This order is no longer available.")
            "REASON_REQUIRED" -> throw OrderActionException("Choose a reason for rejecting this order.")
            else -> throw OrderActionException("Couldn't update the order. Please try again.")
        }
    }

    suspend fun fetchWholesalerOrders(): List<WholesalerOrder> {
        return db.postgrest.rpc("wholesaler_orders").decodeList<WholesalerOrder>()
    }

    suspend fun fetchStaffOrders(): List<StaffOrder> {
        return db.from("orders").select {
            // RLS scopes to employees_own_orders
            order("created_at", order = Order.DESCENDING)
        }.decodeList<StaffOrder>()
    }
}
