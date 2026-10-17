// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerModels.swift
package com.jewelindia.app.core.model

import com.jewelindia.app.core.text.nilIfEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max

// MARK: - Wholesaler

@Serializable
data class Wholesaler(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("business_name") val businessName: String? = null,
    val state: String? = null,
    val city: String? = null,
    @SerialName("business_logo_url") val businessLogoURL: String? = null,
    @SerialName("verification_status") val verificationStatus: VerificationStatus? = null,
    @SerialName("has_visited_dashboard") val hasVisitedDashboard: Boolean? = null,
    @SerialName("last_checked_orders_at") val lastCheckedOrdersAt: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null
) {
    /**
     * `business_name || full_name || email.split("@")[0] || ""` — the exact
     * fallback chain from `app/dashboard/wholesaler/page.jsx`.
     */
    val displayName: String
        get() {
            val b = businessName?.trim()?.nilIfEmpty
            if (b != null) return b
            val f = fullName?.trim()?.nilIfEmpty
            if (f != null) return f
            val e = email?.substringBefore('@')?.trim()?.nilIfEmpty
            if (e != null) return e
            return ""
        }
}

// MARK: - Orders

@Serializable
enum class OrderStatus {
    @SerialName("pending") pending,
    @SerialName("accepted") accepted,
    @SerialName("rejected") rejected,
    @SerialName("in_production") inProduction,
    @SerialName("packed") packed,
    @SerialName("dispatched") dispatched,
    @SerialName("received") received,
    @SerialName("completed") completed
}

@Serializable
data class Order(
    val id: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("employee_id") val employeeId: String? = null,
    @SerialName("retailer_id") val retailerId: String? = null,
    @SerialName("wholesaler_id") val wholesalerId: String? = null,
    @SerialName("customization_note") val customizationNote: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val status: OrderStatus? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    val shortId: String
        get() = id.take(8).uppercase()
}

// MARK: - Chat

@Serializable
data class ChatMessage(
    val id: String,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("sender_type") val senderType: String? = null,
    val content: String? = null,
    @SerialName("is_read") val isRead: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// MARK: - AI pipeline

/**
 * `GET {API}/api/upload-usage?wholesaler_id=` → `{used, limit, resets_at}`.
 */
@Serializable
data class UploadUsage(
    val used: Int = 0,
    val limit: Int? = null,
    @SerialName("resets_at") val resetsAt: String? = null
) {
    val isUnlimited: Boolean
        get() = limit == null

    /** `"{used}/{limit}"`, or just `"{used}"` when the limit is unknown. */
    val display: String
        get() = if (limit == null) "$used" else "$used/$limit"

    val remaining: Int?
        get() = limit?.let { max(0, it - used) }

    companion object {
        val unknown = UploadUsage(used = 0, limit = null, resetsAt = null)
    }
}

/**
 * `POST {API}/process` → `{message, product_id, raw_image_url}`.
 */
@Serializable
data class ProcessResponse(
    val message: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("raw_image_url") val rawImageURL: String? = null
)

// MARK: - Catalogue categories

/**
 * `lib/config/catalogueCategories.js` — same order, same slugs. The web's
 * remote/`public` images are bundled as `Cat*` assets.
 */
data class CatalogueCategory(
    val name: String,
    val slug: String,
    val asset: String
) {
    val id: String get() = slug

    companion object {
        val all: List<CatalogueCategory> = listOf(
            CatalogueCategory(name = "Necklace", slug = "necklace", asset = "cat_necklace"),
            CatalogueCategory(name = "Haram", slug = "haram", asset = "cat_haram"),
            CatalogueCategory(name = "Pendants", slug = "pendants", asset = "cat_pendants"),
            CatalogueCategory(name = "Mangalsutras", slug = "mangalsutras", asset = "cat_mangalsutras"),
            CatalogueCategory(name = "Chains", slug = "chains", asset = "cat_chains"),
            CatalogueCategory(name = "Bangles", slug = "bangles", asset = "cat_bangles"),
            CatalogueCategory(name = "Rings", slug = "rings", asset = "cat_rings"),
            CatalogueCategory(name = "Earrings", slug = "earrings", asset = "cat_earrings"),
            CatalogueCategory(name = "Nosepins", slug = "nosepins", asset = "cat_nosepins")
        )
    }
}
