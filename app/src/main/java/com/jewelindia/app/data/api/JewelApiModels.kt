// Port of: wholesaler ios/JewelIndia/Sources/Networking/JewelAPI.swift (Models & Serializers)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.text.nilIfEmpty
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    val orders: List<com.jewelindia.app.core.model.Order> = emptyList(),
    val products: Map<String, Product> = emptyMap(),
    val suppliers: Map<String, com.jewelindia.app.core.model.Wholesaler> = emptyMap()
)

@Serializable
data class SuccessResponse(
    val success: Boolean = true
)
