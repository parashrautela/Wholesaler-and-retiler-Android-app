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

@Serializable
data class CheckUserResponse(
    val exists: Boolean,
    val provider: String? = null,
    val isEmail: Boolean = false,
    val isPhone: Boolean = false
)

@Serializable
data class SendOTPResponse(
    val success: Boolean,
    @SerialName("remainingResends") val remainingResends: Int? = null,
    @SerialName("lastSentAt") val lastSentAt: String? = null
)

@Serializable
data class VerifyOTPResponse(
    val success: Boolean,
    @SerialName("userId") val userId: String? = null,
    @SerialName("isNewUser") val isNewUser: Boolean = false,
    @SerialName("userRole") val userRole: UserRole? = null
)

@Serializable
data class RetailerInvitation(
    val id: String,
    val code: String,
    val link: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("gift_credits") val giftCredits: Int? = null,
    @SerialName("extra_credits") val extraCredits: Int? = null,
    val status: String? = null,
    @SerialName("funding_state") val fundingState: String? = null,
    @SerialName("retailer_name") val retailerName: String? = null,
    @SerialName("generation_key") val generationKey: String? = null,
    @SerialName("refunded_credits") val refundedCredits: Int? = null
)

@Serializable
data class InvitationSettings(
    val enabled: Boolean,
    @SerialName("skip_guide") val skipGuide: Boolean = false,
    val available: Int = 2000,
    val minimum: Int = 1000,
    val step: Int = 500,
    val maximum: Int = 10000,
    val reward: Int = 1000,
    val links: List<RetailerInvitation>? = null,
    val count: Int? = null
)

@Serializable
data class ClaimInvitationResponse(
    val success: Boolean = true,
    val message: String? = null
)

@Serializable
data class RetailerMarketplaceResponse(
    val products: List<Product> = emptyList(),
    @SerialName("selectedProductIDs") val selectedProductIDs: List<String> = emptyList()
)

@Serializable
data class MarketplaceCategoryItem(
    val id: String,
    val name: String,
    val count: Int
)

@Serializable
data class MarketplaceCategoriesResponse(
    val categories: List<MarketplaceCategoryItem> = emptyList()
)

@Serializable(with = PaginatedMarketplaceResponseSerializer::class)
data class PaginatedMarketplaceResponse(
    val products: List<Product> = emptyList(),
    val selectedProductIDs: List<String> = emptyList(),
    val hasMore: Boolean = false,
    val nextCursor: String? = null,
    val pageSize: Int = 0,
    val isLegacyContract: Boolean = false,
    val fallbackCategories: List<MarketplaceCategoryItem>? = null
) {
    fun filteringLegacy(category: String?, search: String?): PaginatedMarketplaceResponse {
        if (!isLegacyContract) return this
        val needle = search?.trim()?.lowercase().orEmpty()
        val chainAliases = listOf("chain", "chains", "neck chain", "neck chains")
        val selected = category?.trim()?.lowercase()

        val filtered = products.filter { product ->
            val productType = product.jewelleryType?.trim()?.lowercase().orEmpty()
            val categoryMatches = selected == null || productType == selected ||
                (chainAliases.contains(selected) && chainAliases.contains(productType))

            val searchMatches = needle.isEmpty() || listOfNotNull(
                product.title, product.jewelleryType, product.category, product.style, product.metalPurity
            ).any { it.lowercase().contains(needle) }

            categoryMatches && searchMatches
        }
        return copy(products = filtered)
    }
}

object PaginatedMarketplaceResponseSerializer : KSerializer<PaginatedMarketplaceResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PaginatedMarketplaceResponse")

    override fun serialize(encoder: Encoder, value: PaginatedMarketplaceResponse) {
        throw UnsupportedOperationException("Serialization not required for response")
    }

    override fun deserialize(decoder: Decoder): PaginatedMarketplaceResponse {
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("Expected JsonDecoder")
        val obj = input.decodeJsonElement().jsonObject

        val products = obj["products"]?.let {
            JewelJson.decodeFromJsonElement<List<Product>>(it)
        } ?: emptyList()

        val selectedProductIDs = obj["selectedProductIDs"]?.let {
            JewelJson.decodeFromJsonElement<List<String>>(it)
        } ?: emptyList()

        val isLegacyContract = !obj.containsKey("hasMore") && !obj.containsKey("pageSize") && !obj.containsKey("nextCursor")

        return if (isLegacyContract) {
            val chainAliases = listOf("chain", "chains", "neck chain", "neck chains")
            val grouped = products.mapNotNull { it.jewelleryType?.trim()?.nilIfEmpty }
                .groupBy { type ->
                    val lower = type.lowercase()
                    if (chainAliases.contains(lower)) "chain" else lower
                }
            val fallbackCategories = grouped.map { (key, values) ->
                MarketplaceCategoryItem(
                    id = key,
                    name = if (key == "chain") "Chains" else values.first(),
                    count = values.size
                )
            }.sortedBy { it.name.lowercase() }

            PaginatedMarketplaceResponse(
                products = products,
                selectedProductIDs = selectedProductIDs,
                hasMore = false,
                nextCursor = null,
                pageSize = products.size,
                isLegacyContract = true,
                fallbackCategories = fallbackCategories
            )
        } else {
            val hasMore = obj["hasMore"]?.jsonPrimitive?.booleanOrNull ?: false
            val nextCursor = obj["nextCursor"]?.jsonPrimitive?.content
            val pageSize = obj["pageSize"]?.jsonPrimitive?.intOrNull ?: products.size

            PaginatedMarketplaceResponse(
                products = products,
                selectedProductIDs = selectedProductIDs,
                hasMore = hasMore,
                nextCursor = nextCursor,
                pageSize = pageSize,
                isLegacyContract = false,
                fallbackCategories = null
            )
        }
    }
}

@Serializable
data class ProductDetailResponse(
    val product: Product,
    val isSelected: Boolean
)

@Serializable
data class CreateOrderResponse(
    val success: Boolean = true,
    val orderId: String? = null,
    val message: String? = null
)

@Serializable
data class RetailerOrdersResponse(
    val orders: List<com.jewelindia.app.core.model.Order> = emptyList()
)

@Serializable
data class SuccessResponse(
    val success: Boolean = true
)

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
            val body = response.body?.string().orEmpty()
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
        try {
            val body = authenticatedGetSite("/api/referral/manage?page=$page")
            JewelJson.decodeFromString(InvitationSettings.serializer(), body)
        } catch (e: Exception) {
            val user = supabase.auth.currentSessionOrNull()?.user ?: throw e

            @Serializable
            data class WholesalerIDRow(val id: String)

            val ws = try {
                supabase.from("wholesalers")
                    .select(Columns.raw("id")) {
                        filter { eq("user_id", user.id) }
                        limit(1)
                    }.decodeSingle<WholesalerIDRow>()
            } catch (_: Exception) {
                WholesalerIDRow(id = "")
            }

            @Serializable
            data class DBRefRow(
                val id: String,
                val code: String,
                @SerialName("is_active") val isActive: Boolean,
                @SerialName("created_at") val createdAt: String,
                @SerialName("expires_at") val expiresAt: String,
                @SerialName("accepted_at") val acceptedAt: String? = null
            )

            val rows = try {
                supabase.from("referral_links")
                    .select(Columns.raw("id, code, is_active, created_at, expires_at, accepted_at")) {
                        filter { eq("wholesaler_id", ws.id) }
                        order("created_at", Order.DESCENDING)
                        range((page * 20).toLong(), ((page + 1) * 20 - 1).toLong())
                    }.decodeList<DBRefRow>()
            } catch (_: Exception) {
                emptyList()
            }

            val links = rows.map { row ->
                val linkUrl = "https://jewelindia.shop/join/${row.code}"
                val status = if (row.acceptedAt != null) "accepted" else if (!row.isActive) "cancelled" else "unclaimed"
                RetailerInvitation(
                    id = row.id,
                    code = row.code,
                    link = linkUrl,
                    expiresAt = row.expiresAt,
                    giftCredits = 1000,
                    extraCredits = 0,
                    status = status
                )
            }

            InvitationSettings(
                enabled = true,
                skipGuide = false,
                available = 2000,
                minimum = 1000,
                step = 500,
                maximum = 10000,
                reward = 1000,
                links = links,
                count = rows.size
            )
        }
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
        try {
            val token = freshToken()
            val url = "${AppConfig.siteUrl.trimEnd('/')}/api/referral/manage"
            val json = buildJsonObject { put("id", id) }.toString()
            val req = Request.Builder()
                .url(url)
                .delete(json.toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $token")
                .build()
            http.site.newCall(req).execute().use { }
        } catch (_: Exception) {
            // best effort fallback
        }
    }

    suspend fun fetchLatestActiveInvitation(): RetailerInvitation? = withContext(Dispatchers.IO) {
        val user = supabase.auth.currentSessionOrNull()?.user ?: return@withContext null

        @Serializable
        data class WsId(val id: String)

        val ws = try {
            supabase.from("wholesalers").select(Columns.raw("id")) {
                filter { eq("user_id", user.id) }
                limit(1)
            }.decodeSingleOrNull<WsId>()
        } catch (_: Exception) { null } ?: return@withContext null

        @Serializable
        data class RefRow(
            val id: String,
            val code: String,
            @SerialName("is_active") val isActive: Boolean,
            @SerialName("expires_at") val expiresAt: String
        )

        val row = try {
            supabase.from("referral_links").select(Columns.raw("id, code, is_active, expires_at")) {
                filter {
                    eq("wholesaler_id", ws.id)
                    eq("is_active", true)
                }
                order("created_at", Order.DESCENDING)
                limit(1)
            }.decodeSingleOrNull<RefRow>()
        } catch (_: Exception) { null } ?: return@withContext null

        RetailerInvitation(
            id = row.id,
            code = row.code,
            link = "https://jewelindia.shop/join/${row.code}",
            expiresAt = row.expiresAt,
            giftCredits = 1000,
            status = "unclaimed"
        )
    }

    suspend fun createRetailerInvitation(gift: Int = 1000, key: UUID = UUID.randomUUID()): RetailerInvitation = withContext(Dispatchers.IO) {
        try {
            val json = buildJsonObject {
                put("gift_credits", gift)
                put("idempotency_key", key.toString())
            }.toString()
            val body = authenticatedPostSite("/api/referral/generate", json)
            JewelJson.decodeFromString(RetailerInvitation.serializer(), body)
        } catch (_: Exception) {
            // DB fallback
            val user = supabase.auth.currentSessionOrNull()?.user
                ?: throw ApiError(401, "Please sign in to invite retailers.")

            @Serializable
            data class WsRow(val id: String, @SerialName("business_name") val businessName: String? = null, @SerialName("full_name") val fullName: String? = null)

            val ws = supabase.from("wholesalers").select(Columns.raw("id, business_name, full_name")) {
                filter { eq("user_id", user.id) }
                limit(1)
            }.decodeSingle<WsRow>()

            val name = (ws.businessName ?: ws.fullName ?: "JI").trim().replace("\\s+".toRegex(), " ")
            val parts = name.split(" ")
            val initials = if (parts.size >= 2) {
                "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            } else {
                name.take(2).uppercase()
            }

            val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
            val randomChars = (0 until 6).map { chars.random() }.joinToString("")
            val code = "$initials-$randomChars"
            val expiresAt = Instant.now().plus(7, ChronoUnit.DAYS).toString()

            @Serializable
            data class InsertBody(
                @SerialName("wholesaler_id") val wholesalerId: String,
                val code: String,
                @SerialName("max_uses") val maxUses: Int = 1,
                @SerialName("is_active") val isActive: Boolean = true,
                @SerialName("expires_at") val expiresAt: String,
                val source: String = "web"
            )

            @Serializable
            data class InsertResult(
                val id: String,
                val code: String,
                @SerialName("expires_at") val expiresAt: String
            )

            val inserted = supabase.from("referral_links").insert(
                InsertBody(
                    wholesalerId = ws.id,
                    code = code,
                    maxUses = 1,
                    isActive = true,
                    expiresAt = expiresAt,
                    source = "web"
                )
            ) {
                select(Columns.raw("id, code, expires_at"))
            }.decodeSingle<InsertResult>()

            RetailerInvitation(
                id = inserted.id,
                code = inserted.code,
                link = "https://jewelindia.shop/join/${inserted.code}",
                expiresAt = inserted.expiresAt,
                giftCredits = gift,
                extraCredits = 0,
                status = "unclaimed"
            )
        }
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
            val body = response.body?.string().orEmpty()
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
            val body = response.body?.string().orEmpty()
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
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiError(response.code, body)
            }
            body
        }
    }
}
