// Port of: wholesaler ios/JewelIndia/Sources/Networking/JewelAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.text.Credentials
import com.jewelindia.app.core.text.nilIfEmpty
import com.jewelindia.app.data.net.ApiError
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.DbRefusal
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.net.SsrSessionBridge
import com.jewelindia.app.data.net.parseApiError
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.data.supabase.projectRef
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

open class JewelApi(
    private val supabase: SupabaseClient = JewelSupabase.client,
    private val http: HttpClients = HttpClients()
) {
    companion object : JewelApi(JewelSupabase.client, HttpClients())

    private suspend fun freshToken(): String = AuthToken.fresh(supabase)

    val hasLocalSession: Boolean
        get() = supabase.auth.currentSessionOrNull() != null

    suspend fun checkUser(identity: String): CheckUserResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject { put("identity", identity) }.toString()
        val body = postSite("/api/auth/check-user", json)
        JewelJson.decodeFromString(CheckUserResponse.serializer(), body)
    }

    suspend fun sendOtp(identity: String): SendOTPResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject { put("identity", identity) }.toString()
        val body = postSite("/api/auth/send-otp", json)
        JewelJson.decodeFromString(SendOTPResponse.serializer(), body)
    }

    suspend fun verifyOtp(
        identity: String,
        token: String,
        referralCode: String?
    ): VerifyOTPResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            put("identity", identity)
            put("token", token)
            if (referralCode != null) put("referralCode", referralCode)
        }.toString()

        val url = "${AppConfig.siteUrl.trimEnd('/')}/api/auth/verify-otp".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        http.site.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }

            val decoded = JewelJson.decodeFromString(VerifyOTPResponse.serializer(), body)

            val tokens = SsrSessionBridge.tokens(response.headers, url, projectRef)
            if (tokens != null) {
                try {
                    supabase.auth.importAuthToken(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                        retrieveUser = true,
                        autoRefresh = true
                    )
                } catch (_: Exception) {
                    // Swallow failure matching iOS try?
                }
            }

            decoded
        }
    }

    suspend fun invitationSettings(page: Int = 0): InvitationSettings = withContext(Dispatchers.IO) {
        val body = authenticatedGetSite("/api/referral/manage?page=$page")
        JewelJson.decodeFromString(InvitationSettings.serializer(), body)
    }

    suspend fun saveInvitationGuide(skip: Boolean): InvitationSettings = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject { put("skip_guide", skip) }.toString()
            val body = authenticatedPostSite("/api/referral/manage", json)
            JewelJson.decodeFromString(InvitationSettings.serializer(), body)
        } catch (_: Exception) {
            invitationSettings()
        }
    }

    suspend fun cancelInvitation(id: String): Unit = withContext(Dispatchers.IO) {
        val token = freshToken()
        val url = "${AppConfig.siteUrl.trimEnd('/')}/api/referral/manage"
        val json = buildJsonObject { put("id", id) }.toString()
        val req = Request.Builder()
            .url(url)
            .delete(json.toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer $token")
            .build()
        http.site.newCall(req).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
        }
    }

    suspend fun createRetailerInvitation(gift: Int = 1000, key: UUID = UUID.randomUUID()): RetailerInvitation = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            put("gift_credits", gift)
            put("idempotency_key", key.toString())
        }.toString()
        val body = authenticatedPostSite("/api/referral/generate", json)
        JewelJson.decodeFromString(RetailerInvitation.serializer(), body)
    }

    suspend fun claimRetailerInvitation(code: String): ClaimInvitationResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject { put("code", code) }.toString()
        val body = authenticatedPostSite("/api/referral/claim", json)
        JewelJson.decodeFromString(ClaimInvitationResponse.serializer(), body)
    }

    suspend fun fetchRetailerMarketplace(): RetailerMarketplaceResponse = withContext(Dispatchers.IO) {
        val body = authenticatedGetSite("/api/retailer/marketplace")
        JewelJson.decodeFromString(RetailerMarketplaceResponse.serializer(), body)
    }

    suspend fun fetchPaginatedMarketplace(
        category: String? = null,
        search: String? = null,
        cursor: String? = null,
        limit: Int = 24
    ): PaginatedMarketplaceResponse = withContext(Dispatchers.IO) {
        val urlBuilder = "${AppConfig.siteUrl.trimEnd('/')}/api/retailer/marketplace".toHttpUrl().newBuilder()
            .addQueryParameter("limit", "$limit")
            .addQueryParameter("version", "v2")

        category?.trim()?.nilIfEmpty?.let { urlBuilder.addQueryParameter("category", it) }
        search?.trim()?.nilIfEmpty?.let { urlBuilder.addQueryParameter("search", it) }
        cursor?.trim()?.nilIfEmpty?.let { urlBuilder.addQueryParameter("cursor", it) }

        val pathWithQuery = urlBuilder.build().encodedPath + "?" + urlBuilder.build().encodedQuery
        val body = authenticatedGetSite(pathWithQuery)
        val response = JewelJson.decodeFromString(PaginatedMarketplaceResponse.serializer(), body)
        response.filteringLegacy(category, search)
    }

    suspend fun fetchMarketplaceCategories(): MarketplaceCategoriesResponse = withContext(Dispatchers.IO) {
        val body = authenticatedGetSite("/api/retailer/marketplace/categories")
        JewelJson.decodeFromString(MarketplaceCategoriesResponse.serializer(), body)
    }

    suspend fun hydrateMarketplaceProducts(ids: List<String>): RetailerMarketplaceResponse = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext RetailerMarketplaceResponse()
        try {
            val json = buildJsonObject {
                putJsonArray("product_ids") { ids.forEach { add(it) } }
            }.toString()
            val body = authenticatedPostSite("/api/retailer/marketplace/hydrate", json)
            JewelJson.decodeFromString(RetailerMarketplaceResponse.serializer(), body)
        } catch (e: ApiError) {
            if (e.status == 404) {
                val catalogue = fetchRetailerMarketplace()
                val wanted = ids.toSet()
                RetailerMarketplaceResponse(
                    products = catalogue.products.filter { wanted.contains(it.id) },
                    selectedProductIDs = catalogue.selectedProductIDs
                )
            } else throw e
        }
    }

    suspend fun fetchProductDetail(id: String): ProductDetailResponse = withContext(Dispatchers.IO) {
        try {
            val body = authenticatedGetSite("/api/retailer/marketplace/$id")
            JewelJson.decodeFromString(ProductDetailResponse.serializer(), body)
        } catch (e: ApiError) {
            if (e.status == 404) {
                val catalogue = fetchRetailerMarketplace()
                val product = catalogue.products.firstOrNull { it.id == id } ?: throw e
                ProductDetailResponse(product = product, isSelected = catalogue.selectedProductIDs.contains(id))
            } else throw e
        }
    }

    suspend fun setRetailerSelection(productId: String, selected: Boolean): Unit = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            put("product_id", productId)
            put("selected", selected)
        }.toString()
        authenticatedPostSite("/api/retailer/your-taste", json)
    }

    suspend fun createRetailerOrder(
        productId: String,
        quantity: Int,
        notes: String
    ): CreateOrderResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            val itemsArr = kotlinx.serialization.json.buildJsonArray {
                add(buildJsonObject {
                    put("product_id", productId)
                    put("quantity", quantity)
                    put("customization_notes", notes)
                })
            }
            put("items", itemsArr)
        }.toString()
        val body = authenticatedPostSite("/api/orders/create", json)
        JewelJson.decodeFromString(CreateOrderResponse.serializer(), body)
    }

    suspend fun createOrders(productIds: List<String>): CreateOrderResponse = withContext(Dispatchers.IO) {
        val json = buildJsonObject {
            val itemsArr = kotlinx.serialization.json.buildJsonArray {
                productIds.forEach { id ->
                    add(buildJsonObject {
                        put("product_id", id)
                        put("quantity", 1)
                        put("customization_notes", "")
                    })
                }
            }
            put("items", itemsArr)
        }.toString()
        val body = authenticatedPostSite("/api/orders/create", json)
        JewelJson.decodeFromString(CreateOrderResponse.serializer(), body)
    }

    suspend fun fetchRetailerOrders(): RetailerOrdersResponse = withContext(Dispatchers.IO) {
        val body = authenticatedGetSite("/api/retailer/orders")
        JewelJson.decodeFromString(RetailerOrdersResponse.serializer(), body)
    }

    suspend fun setPassword(password: String, role: UserRole?): Boolean = withContext(Dispatchers.IO) {
        if (password.isEmpty()) {
            throw ApiError(status = 400, message = "Password is required.")
        }
        if (!Credentials.meetsServerPasswordRule(password)) {
            throw ApiError(
                status = 400,
                message = "Password must be at least 8 characters and include 1 uppercase, 1 lowercase, 1 number, and 1 special character."
            )
        }

        val auth = supabase.auth
        val user = auth.currentSessionOrNull()?.user
            ?: throw ApiError(status = 401, message = "Session expired. Please restart the signup process.")

        auth.updateUser {
            this.password = password
        }

        if (role != null) {
            try {
                supabase.postgrest.rpc(
                    function = "set_my_role",
                    parameters = buildJsonObject { put("p_role", role.name) }
                )
                try {
                    auth.retrieveUserForCurrentSession(updateSession = true)
                } catch (_: Exception) { }
            } catch (e: Exception) {
                throw ApiError(status = 400, message = DbRefusal.message(e))
            }
        }

        true
    }

    // MARK: - Private HTTP helpers

    private suspend fun postSite(path: String, json: String): String = withContext(Dispatchers.IO) {
        val url = "${AppConfig.siteUrl.trimEnd('/')}$path".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        http.site.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
            body
        }
    }

    private suspend fun authenticatedGetSite(path: String): String = withContext(Dispatchers.IO) {
        val token = freshToken()
        val url = "${AppConfig.siteUrl.trimEnd('/')}$path".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        http.site.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
            body
        }
    }

    private suspend fun authenticatedPostSite(path: String, json: String): String = withContext(Dispatchers.IO) {
        val token = freshToken()
        val url = "${AppConfig.siteUrl.trimEnd('/')}$path".toHttpUrl()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        http.site.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
            body
        }
    }
}
