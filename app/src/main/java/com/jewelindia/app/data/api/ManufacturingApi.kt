// Port of: wholesaler ios/JewelIndia/Sources/Networking/ManufacturingAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.AcceptOfferQuoteParams
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.net.parseApiError
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

object ManufacturingApi {
    private val supabase: SupabaseClient = JewelSupabase.client
    private val http: HttpClients = HttpClients()

    @Serializable
    private data class ListRequestsResponse(
        val ok: Boolean = true,
        val requests: List<ManufacturingRequest> = emptyList(),
        @SerialName("server_time") val serverTime: String? = null
    )

    @Serializable
    private data class RequestDetailResponse(
        val ok: Boolean = true,
        val request: ManufacturingRequest,
        @SerialName("server_time") val serverTime: String? = null
    )

    @Serializable
    private data class ListOffersResponse(
        val ok: Boolean = true,
        val offers: List<ManufacturingOffer> = emptyList(),
        @SerialName("server_time") val serverTime: String? = null
    )

    @Serializable
    private data class OfferDetailResponse(
        val ok: Boolean = true,
        val offer: ManufacturingOffer,
        @SerialName("server_time") val serverTime: String? = null
    )

    suspend fun fetchRetailerRequests(status: String? = null): List<ManufacturingRequest> = withContext(Dispatchers.IO) {
        val urlBuilder = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests".toHttpUrl().newBuilder()
        if (status != null) urlBuilder.addQueryParameter("status", status)
        val body = executeAuthorized(urlBuilder.build().toString(), "GET")
        JewelJson.decodeFromString(ListRequestsResponse.serializer(), body).requests
    }

    suspend fun fetchRetailerRequestDetail(id: String): ManufacturingRequest = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$id"
        val body = executeAuthorized(url, "GET")
        JewelJson.decodeFromString(RequestDetailResponse.serializer(), body).request
    }

    @Serializable
    data class CreateRequestParams(
        val category: String,
        val material: String,
        val purity: String? = null,
        @SerialName("min_weight_grams") val minWeightGrams: Double,
        @SerialName("max_weight_grams") val maxWeightGrams: Double,
        val quantity: Int = 1,
        @SerialName("making_budget_mode") val makingBudgetMode: String? = null,
        @SerialName("making_budget_amount") val makingBudgetAmount: Double? = null,
        @SerialName("delivery_needed_date") val deliveryNeededDate: String,
        val notes: String? = null,
        @SerialName("image_url") val imageUrl: String? = null
    )

    suspend fun createRetailerRequest(params: CreateRequestParams): ManufacturingRequest = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests"
        val json = JewelJson.encodeToString(params)
        val body = executeAuthorized(url, "POST", json)
        JewelJson.decodeFromString(RequestDetailResponse.serializer(), body).request
    }

    suspend fun uploadManufacturingAsset(imageData: ByteArray, userId: String): String = withContext(Dispatchers.IO) {
        val path = "${userId.lowercase()}/${UUID.randomUUID().toString().lowercase()}.jpg"
        JewelSupabase.client.storage.from("manufacturing-assets").upload(path, imageData) {
            upsert = false
        }
        JewelSupabase.client.storage.from("manufacturing-assets").publicUrl(path)
    }

    suspend fun cancelRequest(id: String): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$id/cancel"
        executeAuthorized(url, "POST")
    }

    suspend fun awardQuote(requestId: String, quoteId: String): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$requestId/award"
        val json = buildJsonObject { put("quote_id", quoteId) }.toString()
        executeAuthorized(url, "POST", json)
    }

    suspend fun fetchWholesalerOffers(status: String? = null): List<ManufacturingOffer> = withContext(Dispatchers.IO) {
        val urlBuilder = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers".toHttpUrl().newBuilder()
        if (status != null) urlBuilder.addQueryParameter("status", status)
        val body = executeAuthorized(urlBuilder.build().toString(), "GET")
        JewelJson.decodeFromString(ListOffersResponse.serializer(), body).offers
    }

    suspend fun fetchWholesalerOfferDetail(id: String): ManufacturingOffer = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers/$id"
        val body = executeAuthorized(url, "GET")
        JewelJson.decodeFromString(OfferDetailResponse.serializer(), body).offer
    }

    suspend fun acceptOffer(
        id: String,
        params: AcceptOfferQuoteParams,
        parallel: Boolean = false,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Unit = withContext(Dispatchers.IO) {
        val action = if (parallel) "quotes" else "accept"
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers/$id/$action"
        val json = JewelJson.encodeToString(params)
        executeAuthorized(url, "POST", json, idempotencyKey = idempotencyKey)
    }

    suspend fun declineOffer(
        id: String,
        reason: String? = null,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers/$id/decline"
        val json = buildJsonObject { put("reason", reason) }.toString()
        executeAuthorized(url, "POST", json, idempotencyKey = idempotencyKey)
    }

    suspend fun registerDeviceToken(token: String, environment: String = "production"): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/devices/register"
        val json = buildJsonObject {
            put("device_token", token)
            put("environment", environment)
        }.toString()
        try {
            executeAuthorized(url, "POST", json)
        } catch (_: Exception) {
            // Drop best-effort registration errors
        }
    }

    private suspend fun executeAuthorized(
        url: String,
        method: String,
        bodyJson: String? = null,
        idempotencyKey: String? = null
    ): String {
        val token = AuthToken.fresh(supabase)
        val reqBuilder = Request.Builder()
            .url(url.toHttpUrl())
            .addHeader("Authorization", "Bearer $token")

        if (idempotencyKey != null) {
            reqBuilder.addHeader("Idempotency-Key", idempotencyKey)
        }

        val requestBody = bodyJson?.toRequestBody("application/json".toMediaType())
        when (method.uppercase()) {
            "GET" -> reqBuilder.get()
            "POST" -> reqBuilder.post(requestBody ?: ByteArray(0).toRequestBody(null))
            "DELETE" -> reqBuilder.delete(requestBody)
            else -> reqBuilder.method(method, requestBody)
        }

        return http.pipeline.newCall(reqBuilder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
            body
        }
    }
}
