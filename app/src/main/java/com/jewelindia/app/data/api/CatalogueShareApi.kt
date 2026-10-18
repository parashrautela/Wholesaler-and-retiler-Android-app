package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

interface CatalogueShareService {
    suspend fun createShare(
        productIds: List<String>,
        maxViewers: Int,
        durationMinutes: Int,
        title: String? = null
    ): CatalogueShare

    suspend fun fetchShares(): List<CatalogueShare>

    suspend fun revokeShare(id: String)
}

open class DefaultCatalogueShareApi(
    private val callFactory: Call.Factory = HttpClients.site,
    private val baseUrlProvider: () -> String = { AppConfig.siteURL }
) : CatalogueShareService {

    @Serializable
    private data class ListSharesResponse(
        val ok: Boolean = true,
        @SerialName("server_now") val serverNow: String? = null,
        val shares: List<CatalogueShare> = emptyList()
    )

    @Serializable
    private data class RevokeShareResponse(
        val ok: Boolean = true
    )

    override suspend fun createShare(
        productIds: List<String>,
        maxViewers: Int,
        durationMinutes: Int,
        title: String?
    ): CatalogueShare {
        val body = buildJsonObject {
            putJsonArray("product_ids") {
                for (id in productIds) {
                    add(JsonPrimitive(id))
                }
            }
            put("max_viewers", maxViewers)
            put("duration_minutes", durationMinutes)
            if (!title.isNullOrBlank()) {
                put("title", title.trim())
            }
        }.toString()

        val share: CatalogueShare = executeRequest("/api/catalogue-shares", method = "POST", body = body)
        if (share.url == null) {
            throw CatalogueShareException(
                message = "The server didn't return a valid sharing link. Please try again.",
                errorCode = "INVALID_LINK"
            )
        }
        return share
    }

    override suspend fun fetchShares(): List<CatalogueShare> {
        val res: ListSharesResponse = executeRequest("/api/catalogue-shares", method = "GET", body = null)
        return res.shares
    }

    override suspend fun revokeShare(id: String) {
        val trimmedId = id.trim()
        val res: RevokeShareResponse = executeRequest(
            path = "/api/catalogue-shares/$trimmedId/revoke",
            method = "POST",
            body = "{}"
        )
        if (!res.ok) {
            throw CatalogueShareException("Link not found.", errorCode = "NOT_FOUND")
        }
    }

    private suspend inline fun <reified T : Any> executeRequest(
        path: String,
        method: String,
        body: String?
    ): T = withContext(Dispatchers.IO) {
        val token = try {
            AuthToken.fresh()
        } catch (_: Exception) {
            throw CatalogueShareException(
                message = "Please sign in again to share designs.",
                errorCode = "UNAUTHORIZED",
                httpStatusCode = 401
            )
        }

        val base = baseUrlProvider().trimEnd('/')
        val rel = path.trimStart('/')
        val url = "$base/$rel"

        val reqBuilder = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Cache-Control", "no-store")

        if (method.equals("POST", ignoreCase = true)) {
            val requestBody = (body ?: "{}").toRequestBody("application/json".toMediaType())
            reqBuilder.post(requestBody)
        } else {
            reqBuilder.get()
        }

        try {
            callFactory.newCall(reqBuilder.build()).execute().use { response ->
                val bodyString = response.body.string()
                if (!response.isSuccessful) {
                    throw parseCatalogueShareError(response.code, bodyString)
                }
                JewelJson.decodeFromString<T>(bodyString)
            }
        } catch (e: Exception) {
            if (e is CatalogueShareException) throw e
            throw CatalogueShareException(
                message = "Sharing is temporarily unavailable. Please try again.",
                errorCode = "UNAVAILABLE"
            )
        }
    }
}

object CatalogueShareApi : DefaultCatalogueShareApi()
