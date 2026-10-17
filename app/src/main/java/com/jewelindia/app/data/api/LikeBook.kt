// Port of: wholesaler ios/JewelIndia/Sources/Networking/LikesAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException

object LikeBook {
    private val db get() = JewelSupabase.client

    private val _liked = MutableStateFlow<Set<String>>(emptySet())
    val liked: StateFlow<Set<String>> = _liked.asStateFlow()

    private val inFlight = mutableSetOf<String>()
    private var loadedAt: Long = 0

    fun isLiked(productId: String): Boolean {
        return _liked.value.contains(productId.lowercase())
    }

    suspend fun load(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && loadedAt > 0 && (now - loadedAt) < 60_000) return

        @Serializable
        data class Row(@SerialName("product_id") val productId: String)

        val rows = try {
            db.from("product_likes").select {}.decodeList<Row>()
        } catch (_: Exception) {
            return
        }

        _liked.value = rows.map { it.productId.lowercase() }.toSet()
        loadedAt = now
    }

    suspend fun toggle(productId: String) {
        val id = productId.lowercase()
        synchronized(inFlight) {
            if (inFlight.contains(id)) return
            inFlight.add(id)
        }

        val wasLiked = _liked.value.contains(id)
        _liked.value = if (wasLiked) _liked.value - id else _liked.value + id

        @Serializable
        data class Reply(val ok: Boolean, val liked: Boolean? = null)

        try {
            val reply = db.postgrest.rpc(
                "toggle_product_like",
                buildJsonObject { put("p_product", productId) }
            ).decodeAs<Reply>()

            if (!reply.ok || reply.liked == null) throw IOException("Bad response")

            _liked.value = if (reply.liked) _liked.value + id else _liked.value - id
        } catch (_: Exception) {
            _liked.value = if (wasLiked) _liked.value + id else _liked.value - id
        } finally {
            synchronized(inFlight) {
                inFlight.remove(id)
            }
        }
    }
}
