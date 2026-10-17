// Port of: wholesaler ios/JewelIndia/Sources/Networking/RetailerAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

object RetailerApi {
    private val db get() = JewelSupabase.client

    suspend fun markDashboardVisited(userId: String) {
        @Serializable
        data class Patch(@SerialName("has_visited_dashboard") val hasVisitedDashboard: Boolean)

        try {
            db.from("retailers").update(Patch(true)) {
                filter {
                    eq("user_id", userId)
                }
            }
        } catch (_: Exception) {
            // Best effort
        }
    }

    suspend fun fetchStoreID(userId: String): String {
        @Serializable
        data class Row(val id: String)

        val row = db.from("retailers").select {
            filter {
                eq("user_id", userId)
            }
            limit(1)
        }.decodeSingle<Row>()
        return row.id
    }

    suspend fun fetchDesigns(): List<RetailerDesign> {
        return db.from("retailer_designs").select {
            filter {
                eq("is_archived", false)
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList<RetailerDesign>()
    }

    data class NewDesign(
        var title: String,
        var type: String? = null,
        var category: String? = null,
        var style: String? = null,
        var purity: String? = null,
        var netWeight: Double? = null,
        var inStock: Boolean = true,
        var productionDays: Int? = null
    )

    suspend fun addDesign(design: NewDesign, photos: List<ByteArray>, userId: String) {
        if (photos.isEmpty()) return
        val storeId = fetchStoreID(userId)
        val folder = userId.lowercase()
        val urls = mutableListOf<String>()

        for (photo in photos) {
            val path = "$folder/${UUID.randomUUID().toString().lowercase()}.jpg"
            db.storage.from("retailer-designs").upload(path, photo) {
                upsert = false
            }
            urls.add(db.storage.from("retailer-designs").publicUrl(path))
        }

        @Serializable
        data class Row(
            @SerialName("retailer_id") val retailerId: String,
            @SerialName("image_url") val imageUrl: String,
            @SerialName("image_urls") val imageUrls: List<String>,
            val title: String,
            val type: String?,
            val category: String?,
            @SerialName("style_aesthetic") val styleAesthetic: String?,
            val purity: String?,
            @SerialName("net_weight") val netWeight: Double?,
            @SerialName("is_in_stock") val isInStock: Boolean,
            @SerialName("production_time_days") val productionTimeDays: Int?
        )

        try {
            db.from("retailer_designs").insert(
                Row(
                    retailerId = storeId,
                    imageUrl = urls[0],
                    imageUrls = urls,
                    title = design.title,
                    type = design.type,
                    category = design.category ?: design.type,
                    styleAesthetic = design.style,
                    purity = design.purity,
                    netWeight = design.netWeight,
                    isInStock = design.inStock,
                    productionTimeDays = if (design.inStock) null else design.productionDays
                )
            )
        } catch (e: Exception) {
            removePhotos(urls)
            throw e
        }
    }

    suspend fun deleteDesign(design: RetailerDesign) {
        db.from("retailer_designs").delete {
            filter {
                eq("id", design.id)
            }
        }
        if (design.imageURL != null) {
            removePhotos(listOf(design.imageURL))
        }
    }

    private suspend fun removePhotos(urls: List<String>) {
        val marker = "/retailer-designs/"
        val paths = urls.mapNotNull { url ->
            val idx = url.indexOf(marker)
            if (idx == -1) null
            else {
                val sub = url.substring(idx + marker.length)
                if (sub.contains('?')) sub.substringBefore('?') else sub
            }
        }
        if (paths.isNotEmpty()) {
            try {
                db.storage.from("retailer-designs").delete(paths)
            } catch (_: Exception) {
                // Best effort
            }
        }
    }

    suspend fun fetchSelectedTheme(userId: String): String? {
        @Serializable
        data class Row(@SerialName("selected_theme") val selectedTheme: String? = null)

        return try {
            val row = db.from("retailers").select {
                filter {
                    eq("user_id", userId)
                }
                limit(1)
            }.decodeSingleOrNull<Row>()
            row?.selectedTheme
        } catch (_: Exception) {
            null
        }
    }

    suspend fun setSelectedTheme(themeId: String, userId: String): Boolean {
        @Serializable
        data class Patch(@SerialName("selected_theme") val selectedTheme: String)

        return try {
            db.from("retailers").update(Patch(themeId)) {
                filter {
                    eq("user_id", userId)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
