// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Report/WholesalerReport.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.text.nilIfEmpty
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class WholesalerReport(
    val likes: Int = 0,
    @SerialName("likes_prev") val likesPrev: Int = 0,
    @SerialName("top_liked") val topLiked: List<Design> = emptyList(),
    val views: Int = 0,
    @SerialName("views_prev") val viewsPrev: Int = 0,
    @SerialName("top_viewed") val topViewed: Design? = null,
    @SerialName("orders_new") val ordersNew: Int = 0,
    @SerialName("orders_new_prev") val ordersNewPrev: Int = 0,
    @SerialName("orders_accepted") val ordersAccepted: Int = 0,
    @SerialName("orders_completed") val ordersCompleted: Int = 0,
    @SerialName("orders_rejected") val ordersRejected: Int = 0,
    val questions: Int = 0,
    @SerialName("questions_prev") val questionsPrev: Int = 0,
    val conversations: Int = 0,
    val stores: Int = 0,
    @SerialName("stores_prev") val storesPrev: Int = 0
) {
    enum class Period(val rawValue: String, val title: String, val span: String, val previous: String) {
        Week("week", "This Week", "last 7 days", "last week"),
        Month("month", "This Month", "last 30 days", "last month")
    }

    @Serializable
    data class Design(
        @SerialName("product_id") val productId: String,
        val title: String? = null,
        val image: String? = null,
        val likes: Int? = null,
        val views: Int? = null
    ) {
        val id: String get() = productId
        val imageURL: String? get() = image?.trim()?.nilIfEmpty
        val name: String get() = title?.trim()?.nilIfEmpty ?: "Your design"
    }

    companion object {
        suspend fun fetch(period: Period): WholesalerReport {
            return JewelSupabase.client.postgrest.rpc(
                "wholesaler_report",
                parameters = buildJsonObject {
                    put("p_period", period.rawValue)
                }
            ).decodeAs()
        }
    }
}
