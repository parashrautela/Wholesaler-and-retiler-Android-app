// Port of: wholesaler ios/JewelIndia/Sources/Networking/OrdersChatAPI.swift (Models)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.text.nilIfEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffOrder(
    val id: String,
    val status: OrderStatus? = null,
    @SerialName("customization_note") val note: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val product: DesignSummary? = null
) {
    @Serializable
    data class DesignSummary(
        val title: String? = null,
        @SerialName("processed_image_url") val processedImageURL: String? = null,
        @SerialName("image_url") val imageURL: String? = null,
        @SerialName("raw_image_url") val rawImageURL: String? = null
    ) {
        val image: String?
            get() = listOfNotNull(processedImageURL, imageURL, rawImageURL)
                .mapNotNull { it.trim().nilIfEmpty }
                .firstOrNull()
    }
}

@Serializable
data class WholesalerOrder(
    val id: String,
    val status: OrderStatus? = null,
    @SerialName("customization_note") val note: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("accepted_at") val acceptedAt: String? = null,
    @SerialName("rejected_at") val rejectedAt: String? = null,
    @SerialName("production_at") val productionAt: String? = null,
    @SerialName("packed_at") val packedAt: String? = null,
    @SerialName("dispatched_at") val dispatchedAt: String? = null,
    @SerialName("received_at") val receivedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("product_title") val productTitle: String? = null,
    @SerialName("product_image") val productImage: String? = null,
    @SerialName("product_type") val productType: String? = null,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("store_city") val storeCity: String? = null,
    @SerialName("placed_by") val placedBy: String? = null,
    @SerialName("placed_by_staff") val placedByStaff: Boolean = false
) {
    val shortId: String get() = id.take(8).lowercase()
    val imageURL: String? get() = productImage?.trim()?.nilIfEmpty
    val designTitle: String
        get() = productTitle?.trim()?.nilIfEmpty
            ?: productType?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: "Jewellery order"

    val storeLine: String
        get() = listOfNotNull(storeName?.trim()?.nilIfEmpty ?: "Retail store", storeCity)
            .joinToString(" · ")
}

@Serializable
data class ChatThread(
    val id: String,
    val side: String,
    @SerialName("product_id") val productID: String? = null,
    @SerialName("product_title") val productTitle: String? = null,
    @SerialName("product_image") val productImage: String? = null,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("asked_by") val askedBy: String? = null,
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_from") val lastFrom: String? = null,
    @SerialName("last_at") val lastAt: String? = null,
    val unread: Int = 0
) {
    val imageURL: String? get() = productImage?.trim()?.nilIfEmpty

    val title: String
        get() {
            if (side == "wholesaler") {
                storeName?.trim()?.nilIfEmpty?.let { return it }
            }
            return productTitle?.trim()?.nilIfEmpty ?: "Design enquiry"
        }

    val subtitle: String?
        get() {
            if (side != "wholesaler") return null
            val parts = listOfNotNull(
                productTitle?.trim()?.nilIfEmpty,
                askedBy?.trim()?.nilIfEmpty?.let { "asked by $it" }
            )
            return parts.joinToString(" · ").nilIfEmpty
        }
}
