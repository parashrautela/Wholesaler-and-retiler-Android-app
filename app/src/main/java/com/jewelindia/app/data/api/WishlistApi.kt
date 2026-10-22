// Port of: wholesaler ios/JewelIndia/Sources/Networking/WishlistAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WishlistApiException(message: String) : RuntimeException(message)

@Serializable
private data class WishlistRefusal(val message: String? = null)

object WishlistApi {
    private val db get() = JewelSupabase.client

    // MARK: - Shares

    suspend fun createShare(boardId: String, viewers: Int, minutes: Int): WishlistShare {
        val body = buildJsonObject {
            put("board_id", boardId)
            put("max_viewers", viewers)
            put("duration_minutes", minutes)
        }.toString()
        return shareRequest("/api/wishlist-shares", body)
    }

    suspend fun fetchShares(boardId: String): List<WishlistShare> {
        @Serializable
        data class Response(val shares: List<WishlistShare>)

        val base = AppConfig.siteURL.trimEnd('/')
        val url = "$base/api/wishlist-shares?board_id=$boardId"
        val res: Response = shareRequestUrl(url, null)
        return res.shares
    }

    suspend fun revokeShare(id: String) {
        val base = AppConfig.siteURL.trimEnd('/')
        val url = "$base/api/wishlist-shares/$id/revoke"
        @Serializable
        data class Response(val ok: Boolean)
        shareRequestUrl<Response>(url, "{}")
    }

    private suspend inline fun <reified T : Any> shareRequest(path: String, body: String?): T {
        val base = AppConfig.siteURL.trimEnd('/')
        val relative = path.trimStart('/')
        return shareRequestUrl("$base/$relative", body)
    }

    private suspend inline fun <reified T : Any> shareRequestUrl(url: String, body: String?): T = withContext(Dispatchers.IO) {
        val token = AuthToken.fresh()

        val reqBuilder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Cache-Control", "no-store")

        if (body != null) {
            reqBuilder.post(body.toRequestBody("application/json".toMediaType()))
        }

        try {
            HttpClients.site.newCall(reqBuilder.build()).execute().use { response ->
                val bodyString = response.body.string()
                if (!response.isSuccessful) {
                    val msg = try {
                        JewelJson.decodeFromString<WishlistRefusal>(bodyString).message
                    } catch (_: Exception) { null } ?: "Wishlists are unavailable right now. Please try again."
                    throw WishlistApiException(msg)
                }
                JewelJson.decodeFromString<T>(bodyString)
            }
        } catch (e: Exception) {
            if (e is WishlistApiException) throw e
            throw WishlistApiException("Wishlists are unavailable right now. Please try again.")
        }
    }

    // MARK: - Customers

    suspend fun fetchCustomers(): List<StoreCustomer> {
        return db.from("retailer_customers").select {
            // customer_boards(customer_board_items(product_id))
            order("created_at", order = Order.DESCENDING)
        }.decodeList<StoreCustomer>()
    }

    suspend fun addCustomer(name: String, phone: String?, note: String?) {
        @Serializable
        data class NewCustomer(val name: String, val phone: String?, val note: String?)

        db.from("retailer_customers").insert(NewCustomer(name, phone, note))
    }

    suspend fun deleteCustomer(id: String) {
        db.from("retailer_customers").delete {
            filter {
                eq("id", id)
            }
        }
    }

    // MARK: - Boards

    @Serializable
    private data class BoardRow(
        val id: String,
        val title: String,
        @SerialName("customer_board_items") val items: List<Item> = emptyList()
    ) {
        @Serializable
        data class Item(
            @SerialName("product_id") val productId: String,
            @SerialName("created_at") val createdAt: String
        )
    }

    suspend fun fetchBoards(customerId: String): List<CustomerBoard> {
        val rows = db.from("customer_boards").select {
            filter {
                eq("customer_id", customerId)
            }
            order("created_at", order = Order.ASCENDING)
        }.decodeList<BoardRow>()

        val productIds = rows.flatMap { it.items.map { item -> item.productId } }.toSet()
        val products = fetchProducts(productIds.toList())
        val byId = products.associateBy { it.id.lowercase() }

        return rows.map { row ->
            CustomerBoard(
                id = row.id,
                title = row.title,
                products = row.items
                    .sortedByDescending { it.createdAt }
                    .mapNotNull { byId[it.productId.lowercase()] }
            )
        }
    }

    suspend fun addBoard(customerId: String, title: String) {
        @Serializable
        data class NewBoard(
            @SerialName("customer_id") val customerId: String,
            val title: String
        )
        db.from("customer_boards").insert(NewBoard(customerId, title))
    }

    suspend fun deleteBoard(id: String) {
        db.from("customer_boards").delete {
            filter {
                eq("id", id)
            }
        }
    }

    // MARK: - Designs on a board

    suspend fun setDesign(productId: String, onBoardId: String, saved: Boolean) {
        if (saved) {
            @Serializable
            data class NewItem(
                @SerialName("board_id") val boardId: String,
                @SerialName("product_id") val productId: String
            )
            db.from("customer_board_items").upsert(
                NewItem(boardId = onBoardId, productId = productId)
            )
        } else {
            db.from("customer_board_items").delete {
                filter {
                    eq("board_id", onBoardId)
                    eq("product_id", productId)
                }
            }
        }
    }

    private suspend fun fetchProducts(ids: List<String>): List<Product> {
        if (ids.isEmpty()) return emptyList()
        return db.from("products").select {
            filter {
                eq("is_published", true)
                isIn("id", ids)
            }
        }.decodeList<Product>()
    }
}
