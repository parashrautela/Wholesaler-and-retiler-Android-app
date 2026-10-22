// Port of: wholesaler ios/JewelIndia/Sources/Networking/WholesalerAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.consent.AIConsent
import com.jewelindia.app.core.model.CatalogueCategory
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.ProcessResponse
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.EdgeFunctions
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.net.JewelNetwork
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLDecoder

typealias PickedFile = com.jewelindia.app.core.images.PickedFile

object WholesalerApi {
    private val db get() = JewelSupabase.client

    // MARK: - Wholesaler record

    suspend fun fetchWholesaler(userId: String, email: String?): Wholesaler? {
        if (!email.isNullOrEmpty()) {
            val byEmail = db.from("wholesalers")
                .select {
                    filter {
                        eq("email", email)
                    }
                    limit(1)
                }.decodeList<Wholesaler>()
            if (byEmail.isNotEmpty()) return byEmail.first()
        }
        val byUser = db.from("wholesalers")
            .select {
                filter {
                    eq("user_id", userId)
                }
                limit(1)
            }.decodeList<Wholesaler>()
        return byUser.firstOrNull()
    }

    suspend fun markDashboardVisited(userId: String) {
        @Serializable
        data class Patch(@SerialName("has_visited_dashboard") val hasVisitedDashboard: Boolean)

        try {
            db.from("wholesalers").update(Patch(true)) {
                filter {
                    eq("user_id", userId)
                }
            }
        } catch (_: Exception) {
            // Best effort
        }
    }

    // MARK: - Home counts

    suspend fun countProducts(wholesalerId: String): Int {
        return count("products", "wholesaler_id", wholesalerId)
    }

    suspend fun countPendingOrders(wholesalerId: String): Int {
        return try {
            val res = db.from("orders").select {
                count(Count.EXACT)
                filter {
                    eq("wholesaler_id", wholesalerId)
                    eq("status", OrderStatus.pending.name)
                }
                limit(0)
            }
            res.countOrNull()?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
    }

    suspend fun countUnreadConversations(wholesalerId: String): Int {
        @Serializable
        data class Row(val id: String)
        return try {
            val rows = db.from("conversations").select(Columns.raw("id, messages!inner(id)")) {
                filter {
                    eq("wholesaler_id", wholesalerId)
                    eq("messages.sender_type", "employee")
                    eq("messages.is_read", false)
                }
            }.decodeList<Row>()
            rows.size
        } catch (_: Exception) {
            0
        }
    }

    private suspend fun count(table: String, column: String, value: String): Int {
        return try {
            val res = db.from(table).select {
                count(Count.EXACT)
                filter {
                    eq(column, value)
                }
                limit(0)
            }
            res.countOrNull()?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
    }

    // MARK: - Catalogue

    const val CATALOGUE_PAGE_SIZE = 20

    data class CataloguePage(
        val products: List<Product>,
        val total: Int,
        val hasMore: Boolean = false
    )

    suspend fun fetchCatalogue(
        wholesalerId: String,
        category: String? = null,
        page: Int = 0,
        pageSize: Int = CATALOGUE_PAGE_SIZE
    ): CataloguePage {
        val from = page.toLong() * pageSize
        val to = from + pageSize - 1

        val result = db.from("products").select {
            count(Count.EXACT)
            filter {
                eq("wholesaler_id", wholesalerId)
                if (!category.isNullOrEmpty() && !category.equals("all", ignoreCase = true)) {
                    val baseSlug = category.lowercase()
                    val normalized = if (baseSlug.endsWith("s")) baseSlug.dropLast(1) else baseSlug
                    isIn("jewellery_type", listOf(normalized, "${normalized}s"))
                }
            }
            order("created_at", order = Order.DESCENDING)
            order("id", order = Order.DESCENDING)
            range(from, to)
        }

        val products = result.decodeList<Product>()
        val total = result.countOrNull()?.toInt() ?: products.size
        return CataloguePage(products, total, hasMore = (from + products.size) < total)
    }

    suspend fun fetchProduct(id: String): Product? {
        val rows = db.from("products").select {
            filter {
                eq("id", id)
            }
            limit(1)
        }.decodeList<Product>()
        return rows.firstOrNull()
    }

    suspend fun updateProductFlags(
        id: String,
        inStock: Boolean? = null,
        isPublished: Boolean? = null
    ) {
        @Serializable
        data class Patch(
            @SerialName("stock_available") val stockAvailable: Boolean? = null,
            @SerialName("is_published") val isPublished: Boolean? = null
        )
        val patch = Patch(stockAvailable = inStock, isPublished = isPublished)
        JewelNetwork.withRetry {
            db.from("products").update(patch) {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    @Serializable
    data class ProductEdit(
        val title: String? = null,
        val category: String? = null,
        @SerialName("jewellery_type") val jewelleryType: String? = null,
        val style: String? = null,
        val size: String? = null,
        @SerialName("metal_purity") val metalPurity: String? = null,
        @SerialName("net_weight") val netWeight: Double? = null,
        @SerialName("gross_weight") val grossWeight: Double? = null,
        @SerialName("stone_weight") val stoneWeight: Double? = null,
        @SerialName("stock_available") val stockAvailable: Boolean? = null,
        @SerialName("make_to_order_days") val makeToOrderDays: Int? = null,
        @SerialName("is_published") val isPublished: Boolean? = null,
        @SerialName("custom_image_urls") val customImageUrls: List<String>? = null,
        @SerialName("showcase_image_urls") val showcaseImageUrls: List<String>? = null
    )

    suspend fun updateProduct(id: String, edit: ProductEdit) {
        JewelNetwork.withRetry {
            db.from("products").update(edit) {
                filter {
                    eq("id", id)
                }
            }
        }
    }

    @Serializable
    private data class IdRow(val id: String)

    suspend fun completeProduct(productId: String, payload: JsonObject) = withContext(Dispatchers.IO) {
        JewelNetwork.withRetry {
            val rows = db.from("products").update(payload) {
                select(Columns.raw("id"))
                filter {
                    eq("id", productId)
                }
            }.decodeList<IdRow>()
            if (rows.isEmpty()) {
                throw PipelineException("Your design was uploaded, but its details couldn't be saved. Open it in your catalogue and tap Edit to add them.")
            }
        }
    }

    suspend fun insertDraftProduct(payload: JsonObject) = withContext(Dispatchers.IO) {
        JewelNetwork.withRetry {
            db.from("products").insert(payload)
        }
    }

    suspend fun deleteProduct(id: String) {
        @Serializable
        data class DeletedRow(val id: String)

        val deleted = JewelNetwork.withRetry {
            db.from("products").delete {
                select()
                filter {
                    eq("id", id)
                }
            }.decodeList<DeletedRow>()
        }
        if (deleted.isNotEmpty()) return

        val stillExists = db.from("products").select {
            filter {
                eq("id", id)
            }
            limit(1)
        }.decodeList<DeletedRow>().firstOrNull()

        if (stillExists != null) {
            throw ProductDeleteException()
        }
    }

    class ProductDeleteException(
        message: String = "The product is still in the cloud. Check your connection or account permissions and try again."
    ) : RuntimeException(message)

    suspend fun deleteProductImages(product: Product) {
        val urls = listOfNotNull(product.rawImageURL, product.processedImageURL, product.imageURL) +
                product.generatedImageURLs + product.customImageURLs + product.showcaseImageURLs
        val marker = "/storage/v1/object/public/plant-images/"
        val paths = urls.mapNotNull { raw ->
            val idx = raw.indexOf(marker)
            if (idx == -1) null
            else {
                val sub = raw.substring(idx + marker.length)
                val clean = if (sub.contains('?')) sub.substringBefore('?') else sub
                try {
                    URLDecoder.decode(clean, "UTF-8")
                } catch (_: Exception) {
                    clean
                }
            }
        }.filter { it.isNotEmpty() }.toSet()

        if (paths.isNotEmpty()) {
            try {
                db.storage.from("plant-images").delete(paths.toList())
            } catch (_: Exception) {
                // Best effort
            }
        }
    }

    // MARK: - Orders

    suspend fun updateOrderStatus(
        id: String,
        status: OrderStatus,
        rejectionReason: String? = null
    ) {
        OrdersApi.setStatus(orderId = id, status = status, reason = rejectionReason)
    }

    // MARK: - AI pipeline

    class PipelineException(
        message: String,
        val isInsufficientCredits: Boolean = false
    ) : RuntimeException(message)

    suspend fun fetchUploadUsage(wholesalerId: String): UploadUsage = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/api/upload-usage?wholesaler_id=$wholesalerId"
        val request = Request.Builder()
            .url(url)
            .header("Cache-Control", "no-store")
            .build()

        try {
            HttpClients.pipeline.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext UploadUsage.unknown
                val body = response.body.string()
                JewelJson.decodeFromString<UploadUsage>(body)
            }
        } catch (_: Exception) {
            UploadUsage.unknown
        }
    }

    suspend fun processProduct(
        imageData: ByteArray,
        filename: String,
        mimeType: String,
        title: String,
        jewelleryType: String,
        wholesalerId: String,
        imageCount: Int,
        submissionKey: String
    ): ProcessResponse = withContext(Dispatchers.IO) {
        AIConsent.requireGranted(wholesalerId)

        val token = AuthToken.fresh()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .addFormDataPart("jewellery_type", jewelleryType)
            .addFormDataPart("wholesaler_id", wholesalerId)
            .addFormDataPart("image_count", imageCount.toString())
            .addFormDataPart("file", filename, imageData.toRequestBody(mimeType.toMediaType()))
            .build()

        val request = Request.Builder()
            .url("${AppConfig.aiPipelineURL.trimEnd('/')}/process")
            .post(requestBody)
            .header("Idempotency-Key", submissionKey)
            .header("Authorization", "Bearer $token")
            .build()

        try {
            HttpClients.pipeline.newCall(request).execute().use { response ->
                val bodyString = response.body.string()
                if (!response.isSuccessful) {
                    if (response.code == 402) {
                        val detail = try {
                            val json = Json.parseToJsonElement(bodyString).jsonObject
                            json["detail"]?.jsonObject
                        } catch (_: Exception) { null }
                        val shortBy = detail?.get("short_by")?.jsonPrimitive?.content?.toIntOrNull()
                        val msg = if (shortBy != null) {
                            "You need $shortBy more credits for this upload. Top up and try again."
                        } else {
                            "You don't have enough credits for this upload."
                        }
                        throw PipelineException(msg, isInsufficientCredits = true)
                    }

                    val message = try {
                        val json = Json.parseToJsonElement(bodyString).jsonObject
                        val detail = json["detail"]
                        if (detail is JsonObject) {
                            detail["message"]?.jsonPrimitive?.content
                        } else {
                            detail?.jsonPrimitive?.content ?: json["message"]?.jsonPrimitive?.content
                        }
                    } catch (_: Exception) { null }
                        ?: "Upload failed (${response.code}). Please try again."

                    throw PipelineException(message)
                }

                JewelJson.decodeFromString<ProcessResponse>(bodyString)
            }
        } catch (e: IOException) {
            throw PipelineException("Upload timed out or was cut off. Check your connection and try again.")
        }
    }

    suspend fun fetchPipelineProduct(id: String): JsonObject = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/product/$id"
        val request = Request.Builder().url(url).build()

        try {
            HttpClients.pipeline.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw PipelineException("Couldn't read processing status.")
                val body = response.body.string()
                Json.parseToJsonElement(body).jsonObject
            }
        } catch (e: Exception) {
            if (e is PipelineException) throw e
            throw PipelineException("Couldn't read processing status.")
        }
    }

    suspend fun reprocessProduct(
        productId: String,
        wholesalerId: String,
        file: PickedFile?,
        imageCount: Int
    ) = withContext(Dispatchers.IO) {
        require(imageCount in 1..4) { "Choose between 1 and 4 images." }
        AIConsent.requireGranted(wholesalerId)
        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/process/$productId"

        val requestBuilder = Request.Builder().url(url)
        if (file != null) {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("wholesaler_id", wholesalerId)
                .addFormDataPart("image_count", imageCount.toString())
                .addFormDataPart("file", file.filename, file.data.toRequestBody(file.mimeType.toMediaType()))
                .build()
            requestBuilder.post(body)
        } else {
            val json = buildJsonObject {
                put("wholesaler_id", wholesalerId)
                put("image_count", imageCount)
            }.toString()
            requestBuilder.post(json.toRequestBody("application/json".toMediaType()))
        }

        try {
            HttpClients.pipeline.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body.string()
                if (!response.isSuccessful) {
                    val fallback = if (response.code == 429) {
                        "Daily upload limit reached. Please try again tomorrow."
                    } else {
                        "Reprocessing failed (${response.code})"
                    }
                    val msg = try {
                        val obj = Json.parseToJsonElement(body).jsonObject
                        obj["detail"]?.jsonPrimitive?.content ?: fallback
                    } catch (_: Exception) { fallback }
                    throw PipelineException(msg)
                }
            }
        } catch (e: IOException) {
            throw PipelineException(Copy.networkError)
        }
    }

    suspend fun clearProductImages(id: String, wholesalerId: String) {
        val patch = buildJsonObject {
            put("processed_image_url", JsonNull)
            put("generated_image_urls", buildJsonArray { })
        }
        JewelNetwork.withRetry {
            db.from("products").update(patch) {
                filter {
                    eq("id", id)
                    eq("wholesaler_id", wholesalerId)
                }
            }
        }
    }

    const val POLL_TIMEOUT_SECONDS = 300L
    const val POLL_INTERVAL_MILLIS = 5000L

    sealed interface ReprocessPollTick {
        data class Pending(val backgroundRemoved: Boolean) : ReprocessPollTick
        data class Done(val urls: List<String>) : ReprocessPollTick
    }

    suspend fun reprocessPollTick(productId: String, backgroundRemovedAlready: Boolean): ReprocessPollTick = withContext(Dispatchers.IO) {
        val urls = try {
            val obj = fetchPipelineProduct(productId)
            obj["generated_image_urls"]?.jsonArray?.mapNotNull { it.jsonPrimitive.content } ?: emptyList()
        } catch (_: Exception) { emptyList() }

        if (urls.isNotEmpty()) {
            return@withContext ReprocessPollTick.Done(urls.take(4))
        }

        if (backgroundRemovedAlready) return@withContext ReprocessPollTick.Pending(backgroundRemoved = true)

        val probeUrl = "${AppConfig.supabaseURL.trimEnd('/')}/storage/v1/object/public/plant-images/products/temp/reve_$productId.png"
        val request = Request.Builder().url(probeUrl).head().build()
        val removed = try {
            HttpClients.plain.newCall(request).execute().use { it.isSuccessful }
        } catch (_: Exception) { false }

        ReprocessPollTick.Pending(backgroundRemoved = removed)
    }

    suspend fun upload(
        bucket: String,
        path: String,
        data: ByteArray,
        contentType: String,
        upsert: Boolean = true
    ): String {
        db.storage.from(bucket).upload(path, data) {
            this.contentType = ContentType.parse(contentType)
            this.upsert = upsert
        }
        return db.storage.from(bucket).publicUrl(path)
    }

    suspend fun deleteMyAccount() = AccountApi.deleteMyAccount()
}

object AccountApi {
    suspend fun deleteMyAccount() {
        val token = AuthToken.fresh()

        try {
            EdgeFunctions.invoke<JsonObject>("delete-account", token = token)
        } catch (e: Exception) {
            throw WholesalerApi.PipelineException(e.message ?: "Account deletion failed. Please try again.")
        }
    }
}
