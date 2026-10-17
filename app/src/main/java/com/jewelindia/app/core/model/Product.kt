// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerModels.swift (Product, ImageSize)
package com.jewelindia.app.core.model

import com.jewelindia.app.core.text.nilIfEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Which stored copy of an image to fetch. The pipeline writes all three
 * beside every original (`ai-pipeline/app/services/derivatives.py`):
 * a card is ~41 KB where the original is ~3 MB.
 */
enum class ImageSize(val value: String) {
    /** Grid tiles and thumbnails — 540px. */
    CARD("card"),

    /** A single image filling the screen — 1200px. */
    DETAIL("detail"),

    /** Pinch-to-zoom — 2048px. */
    FULL("full")
}

@Serializable
data class Product(
    val id: String,
    @SerialName("wholesaler_id") val wholesalerId: String? = null,
    @SerialName("wholesaler_email") val wholesalerEmail: String? = null,
    val title: String? = null,
    @SerialName("jewellery_type") val jewelleryType: String? = null,
    val category: String? = null,
    val style: String? = null,
    val size: String? = null,
    @SerialName("stock_available") val stockAvailable: Boolean? = null,
    @SerialName("make_to_order_days") val makeToOrderDays: Int? = null,
    @SerialName("metal_purity") val metalPurity: String? = null,
    @SerialName("net_weight") val netWeight: Double? = null,
    @SerialName("gross_weight") val grossWeight: Double? = null,
    @SerialName("stone_weight") val stoneWeight: Double? = null,
    @SerialName("raw_image_url") val rawImageURL: String? = null,
    @SerialName("processed_image_url") val processedImageURL: String? = null,
    @SerialName("image_url") val imageURL: String? = null,
    @SerialName("generated_image_urls") val generatedImageURLs: List<String> = emptyList(),
    @SerialName("custom_image_urls") val customImageURLs: List<String> = emptyList(),
    @SerialName("showcase_image_urls") val showcaseImageURLs: List<String> = emptyList(),
    @SerialName("image_variants") val imageVariants: Map<String, Map<String, String>> = emptyMap(),
    @SerialName("is_published") val isPublished: Boolean? = null,
    @SerialName("ai_processing_state") val aiProcessingState: String? = null,
    @SerialName("ai_processing_run_id") val aiProcessingRunId: String? = null,
    @SerialName("ai_verified_output_urls") val aiVerifiedOutputURLs: List<String> = emptyList(),
    @SerialName("ai_completed_at") val aiCompletedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    /**
     * Whether this product is eligible for publication to retailers / catalogue.
     * Strict rule: only products verified and completed by the AI pipeline can be published.
     */
    val canPublish: Boolean
        get() {
            if (aiProcessingState != null) {
                return aiProcessingState == "ready" && (aiVerifiedOutputURLs.isNotEmpty() || generatedImageURLs.isNotEmpty() || !processedImageURL.isNullOrEmpty())
            }
            return generatedImageURLs.isNotEmpty() || !processedImageURL.isNullOrEmpty()
        }

    /**
     * Prefer an explicitly chosen showcase, then generated/processed media,
     * and only use the uploader's original photo as a final fallback.
     */
    val displaySource: String?
        get() = showcaseImageURLs.firstOrNull()?.trim()?.nilIfEmpty
            ?: generatedImageURLs.firstOrNull()?.trim()?.nilIfEmpty
            ?: processedImageURL?.trim()?.nilIfEmpty
            ?: imageURL?.trim()?.nilIfEmpty
            ?: rawImageURL?.trim()?.nilIfEmpty

    /**
     * The display image, at the size the view actually draws.
     */
    fun displayImageUrl(size: ImageSize = ImageSize.CARD): String? {
        return displaySource?.let { url(it, size) }
    }

    /**
     * Whether there is anything to show at all, whatever the size.
     */
    val hasDisplayImage: Boolean
        get() = displaySource != null

    /**
     * The thumbnail strip in the detail modal: first 4 *unique* images, this
     * source first, then the generated set — matching §8.3 exactly.
     */
    fun thumbnailUrls(size: ImageSize = ImageSize.CARD): List<String> {
        val seen = mutableSetOf<String>()
        val ordered = mutableListOf<String>()
        val priority = if (showcaseImageURLs.isEmpty()) {
            generatedImageURLs + listOfNotNull(processedImageURL, imageURL) + customImageURLs + listOfNotNull(rawImageURL)
        } else {
            showcaseImageURLs
        }

        for (raw in priority) {
            val trimmed = raw.trim()
            if (trimmed.isNotEmpty() && seen.add(trimmed)) {
                ordered.add(trimmed)
            }
        }
        return ordered.take(4).mapNotNull { url(it, size) }
    }

    /**
     * The stored copy of [original] at [size], or [original] itself when the
     * pipeline hasn't made copies of it.
     */
    fun url(original: String, size: ImageSize): String {
        val variant = imageVariants[original]?.get(size.value)
        return if (!variant.isNullOrBlank()) variant else original
    }
}
