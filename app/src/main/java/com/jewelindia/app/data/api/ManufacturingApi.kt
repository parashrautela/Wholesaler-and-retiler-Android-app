// Port of: wholesaler ios/JewelIndia/Sources/Networking/ManufacturingAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.AcceptOfferQuoteParams
import com.jewelindia.app.core.model.CreateManufacturingRequestParams
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.net.parseApiError
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

object ManufacturingApi {
    private val supabase: SupabaseClient by lazy { JewelSupabase.client }
    private val http: HttpClients = HttpClients()

    @Serializable
    private data class FastApiDetailError(val detail: String? = null)

    @Serializable
    data class AssetUploadResponse(
        val ok: Boolean = true,
        @SerialName("asset_id") val assetId: String,
        @SerialName("preview_url") val previewUrl: String? = null,
        val width: Int? = null,
        val height: Int? = null,
        @SerialName("byte_size") val byteSize: Long? = null
    )

    @Serializable
    data class CreateRequestResponse(
        val ok: Boolean = true,
        @SerialName("request_id") val requestId: String? = null,
        val state: String? = null,
        @SerialName("active_offer_id") val activeOfferId: String? = null,
        @SerialName("expires_at") val expiresAt: String? = null
    )

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

    typealias CreateRequestParams = CreateManufacturingRequestParams

    suspend fun uploadAsset(imageData: ByteArray): AssetUploadResponse = withContext(Dispatchers.IO) {
        val token = AuthToken.fresh(supabase)
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-assets"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "file",
                filename = "reference.jpg",
                body = imageData.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val req = Request.Builder()
            .url(url.toHttpUrl())
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        http.pipeline.newCall(req).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching {
                    JewelJson.decodeFromString(FastApiDetailError.serializer(), body).detail
                }.getOrNull()
                throw RuntimeException(detail ?: "Image upload failed with status ${response.code}.")
            }
            JewelJson.decodeFromString(AssetUploadResponse.serializer(), body)
        }
    }

    suspend fun createRequest(
        params: CreateManufacturingRequestParams,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): CreateRequestResponse = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests"
        val json = JewelJson.encodeToString(params)
        val body = executeAuthorized(
            url = url,
            method = "POST",
            bodyJson = json,
            idempotencyKey = idempotencyKey,
            fallbackErrorMessage = "Request creation failed."
        )
        JewelJson.decodeFromString(CreateRequestResponse.serializer(), body)
    }

    suspend fun createRetailerRequest(
        params: CreateManufacturingRequestParams,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): CreateRequestResponse = createRequest(params, idempotencyKey)

    suspend fun fetchRetailerRequests(status: String? = null): List<ManufacturingRequest> = withContext(Dispatchers.IO) {
        val urlBuilder = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests".toHttpUrl().newBuilder()
        if (status != null) urlBuilder.addQueryParameter("status", status)
        val body = executeAuthorized(
            url = urlBuilder.build().toString(),
            method = "GET",
            fallbackErrorMessage = "Failed to fetch manufacturing requests."
        )
        JewelJson.decodeFromString(ListRequestsResponse.serializer(), body).requests
    }

    suspend fun fetchRetailerRequestDetail(id: String): ManufacturingRequest = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$id"
        val body = executeAuthorized(
            url = url,
            method = "GET",
            fallbackErrorMessage = "Failed to load request details."
        )
        JewelJson.decodeFromString(RequestDetailResponse.serializer(), body).request
    }

    suspend fun cancelRequest(id: String): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$id/cancel"
        executeAuthorized(url, "POST", fallbackErrorMessage = "Cancellation failed.")
    }

    suspend fun awardQuote(requestId: String, quoteId: String): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/manufacturing-requests/$requestId/award"
        val json = buildJsonObject { put("quote_id", quoteId) }.toString()
        executeAuthorized(url, "POST", json, fallbackErrorMessage = "Could not select this quote. Refresh the enquiry and try again.")
    }

    suspend fun fetchWholesalerOffers(status: String? = null): List<ManufacturingOffer> = withContext(Dispatchers.IO) {
        val urlBuilder = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers".toHttpUrl().newBuilder()
        if (status != null) urlBuilder.addQueryParameter("status", status)
        val body = executeAuthorized(
            url = urlBuilder.build().toString(),
            method = "GET",
            fallbackErrorMessage = "Failed to fetch offers."
        )
        JewelJson.decodeFromString(ListOffersResponse.serializer(), body).offers
    }

    suspend fun fetchWholesalerOfferDetail(id: String): ManufacturingOffer = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers/$id"
        val body = executeAuthorized(
            url = url,
            method = "GET",
            fallbackErrorMessage = "Failed to load offer."
        )
        JewelJson.decodeFromString(OfferDetailResponse.serializer(), body).offer
    }

    fun acceptOfferPath(offerId: String, parallel: Boolean): String =
        "/api/wholesaler/manufacturing-offers/$offerId/${if (parallel) "quotes" else "accept"}"

    suspend fun acceptOffer(
        id: String,
        params: AcceptOfferQuoteParams,
        parallel: Boolean = false,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Unit = withContext(Dispatchers.IO) {
        val path = acceptOfferPath(id, parallel)
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}$path"
        val json = JewelJson.encodeToString(params)
        executeAuthorized(url, "POST", json, idempotencyKey = idempotencyKey, fallbackErrorMessage = "Acceptance failed.")
    }

    suspend fun declineOffer(
        id: String,
        reason: String? = null,
        idempotencyKey: String = UUID.randomUUID().toString()
    ): Unit = withContext(Dispatchers.IO) {
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/wholesaler/manufacturing-offers/$id/decline"
        val json = buildJsonObject { put("reason", reason) }.toString()
        executeAuthorized(url, "POST", json, idempotencyKey = idempotencyKey, fallbackErrorMessage = "Decline failed.")
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
        idempotencyKey: String? = null,
        fallbackErrorMessage: String? = null
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
                val detail = runCatching {
                    JewelJson.decodeFromString(FastApiDetailError.serializer(), body).detail
                }.getOrNull()
                if (!detail.isNullOrBlank()) {
                    throw RuntimeException(detail)
                }
                if (fallbackErrorMessage != null) {
                    throw RuntimeException(fallbackErrorMessage)
                }
                throw parseApiError(response.code, body)
            }
            body
        }
    }
}
