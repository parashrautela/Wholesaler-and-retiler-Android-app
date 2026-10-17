// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakModels.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.min

/** Whose designs the Chamak picker offers. */
enum class ChamakCatalogueSource {
    /** A wholesaler's own products. */
    OwnProducts,
    /** A retailer's shortlist and customer boards. */
    StoreDesigns
}

// MARK: - Chamak Status

@Serializable
enum class ChamakStatus {
    @SerialName("queued") Queued,
    @SerialName("analyzing") Analyzing,
    @SerialName("awaiting_input") AwaitingInput,
    @SerialName("generating") Generating,
    @SerialName("done") Done,
    @SerialName("failed") Failed;

    fun displayLabel(mode: ChamakMode = ChamakMode.Fusion): String = when (this) {
        Queued -> "Queued"
        Analyzing -> "Analyzing Designs"
        AwaitingInput -> "Awaiting Input"
        Generating -> if (mode == ChamakMode.SetCreation) "Staging Your Set" else "Combining Designs"
        Done -> if (mode == ChamakMode.SetCreation) "Set Complete" else "Designs Combined"
        Failed -> "Generation Failed"
    }
}

// MARK: - Chamak Mode

/**
 * `fusion` blends two designs of the same category into one new piece.
 * `set_creation` stages two different, real pieces together unchanged, as a
 * matched-set catalogue photo.
 */
@Serializable
enum class ChamakMode {
    @SerialName("fusion") Fusion,
    @SerialName("set_creation") SetCreation
}

// MARK: - Set Creation Backdrop

/**
 * The 4 staging presets a wholesaler picks between in Set Creation mode.
 */
@Serializable(with = SetBackdropSerializer::class)
enum class SetBackdrop(val rawValue: String, val label: String, val blurb: String) {
    VelvetBust("velvet_bust", "Velvet Bust", "Teal velvet bust and stands, maroon silk backdrop"),
    DarkSlate("dark_slate", "Dark Slate", "Charcoal stone surface, dramatic side light"),
    Festive("festive", "Festive", "Maroon and gold silk, warm bokeh, marigold accents"),
    CleanStudio("clean_studio", "Clean Studio", "Seamless light-grey sweep, soft even lighting");

    companion object {
        fun fromRaw(raw: String?): SetBackdrop =
            entries.find { it.rawValue.equals(raw, ignoreCase = true) } ?: VelvetBust
    }
}

object SetBackdropSerializer : KSerializer<SetBackdrop> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SetBackdrop", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SetBackdrop) = encoder.encodeString(value.rawValue)
    override fun deserialize(decoder: Decoder): SetBackdrop = SetBackdrop.fromRaw(decoder.decodeString())
}

data class SetStylingChip(
    val id: String,
    val label: String,
    val instruction: String
) {
    companion object {
        val all: List<SetStylingChip> = listOf(
            SetStylingChip("balanced", "Balanced", "Arrange both pieces in a balanced, harmonious composition with clear separation."),
            SetStylingChip("equal_focus", "Equal focus", "Give both jewelry pieces equal visual importance and prominence."),
            SetStylingChip("luxury", "Luxury showroom", "Use premium showroom spacing, refined presentation, and an elegant luxury mood."),
            SetStylingChip("minimal", "Minimal", "Use a clean, minimal arrangement with generous negative space."),
            SetStylingChip("soft_light", "Soft light", "Use soft diffused lighting with gentle shadows and subtle jewelry highlights."),
            SetStylingChip("detail", "Show detail", "Frame the pieces close enough to clearly show fine craftsmanship and gemstone details."),
            SetStylingChip("more_space", "More breathing room", "Keep generous space around both pieces so no component feels crowded or hidden."),
            SetStylingChip("ecommerce", "E-commerce ready", "Use a clean professional catalogue arrangement suitable for an online product listing.")
        )
    }
}

// MARK: - Content Flag

@Serializable(with = ContentFlagSerializer::class)
enum class ContentFlag(val rawValue: String) {
    Ok("ok"),
    NotJewelry("not_jewelry"),
    Inappropriate("inappropriate"),
    TooUnclearToAssess("too_unclear_to_assess");

    val userMessage: String?
        get() = when (this) {
            Ok -> null
            NotJewelry -> "Jewellery was not detected in one or both selected images. Please select two clear jewellery photos to continue."
            Inappropriate -> "Selected images contain inappropriate or unsupported visual content."
            TooUnclearToAssess -> "One or both images are too blurry or unclear for precision vision analysis."
        }

    companion object {
        fun fromRaw(raw: String?): ContentFlag =
            entries.find { it.rawValue.equals(raw, ignoreCase = true) } ?: Ok
    }
}

object ContentFlagSerializer : KSerializer<ContentFlag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ContentFlag", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ContentFlag) = encoder.encodeString(value.rawValue)
    override fun deserialize(decoder: Decoder): ContentFlag = ContentFlag.fromRaw(decoder.decodeString())
}

// MARK: - Stage 1 Vision Analysis

@Serializable
data class Stage1Analysis(
    @SerialName("jewelry_type") val jewelryType: String? = null,
    @SerialName("image1_strengths") val image1Strengths: List<String> = emptyList(),
    @SerialName("image1_weaknesses") val image1Weaknesses: List<String> = emptyList(),
    @SerialName("image2_strengths") val image2Strengths: List<String> = emptyList(),
    @SerialName("image2_weaknesses") val image2Weaknesses: List<String> = emptyList(),
    @SerialName("near_identical") val nearIdentical: Boolean = false,
    @SerialName("type_mismatch") val typeMismatch: Boolean = false,
    @SerialName("content_flag") val contentFlag: ContentFlag = ContentFlag.Ok
) {
    val dynamicAttributes: List<ChamakAttribute>
        get() {
            val pairCount = min(image1Strengths.size, image2Weaknesses.size)
            return (0 until pairCount).map { idx ->
                val strength = image1Strengths[idx]
                val weakness = image2Weaknesses[idx]
                ChamakAttribute(
                    id = "attr_$idx",
                    name = strength.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    source1Feature = strength,
                    source2Feature = weakness,
                    defaultValue = 0.5
                )
            }
        }

    val unmatchedImage1Strengths: List<String>
        get() {
            val pairCount = min(image1Strengths.size, image2Weaknesses.size)
            return image1Strengths.drop(pairCount)
        }
}

data class ChamakAttribute(
    val id: String,
    val name: String,
    val source1Feature: String,
    val source2Feature: String,
    var defaultValue: Double = 0.5
)

data class ChamakDesignItem(
    val id: String,
    var title: String,
    var subtitle: String? = null,
    var imageURL: String? = null,
    var localImageData: ByteArray? = null,
    var product: Product? = null,
    var contentHash: String? = null,
    var declaredJewelleryType: String? = null
) {
    val canonicalJewelleryType: String?
        get() {
            declaredJewelleryType?.let { declared ->
                return JewelleryTypeCanonical.canonicalize(declared)
            }
            product?.jewelleryType?.let { pType ->
                return JewelleryTypeCanonical.canonicalize(pType)
            }
            return null
        }

    val displayTypeLabel: String
        get() {
            canonicalJewelleryType?.let { c ->
                return JewelleryTypeCanonical.displayLabel(c)
            }
            return subtitle ?: "Piece"
        }

    val hasImage: Boolean
        get() = (!imageURL.isNullOrEmpty()) || (localImageData != null && localImageData!!.isNotEmpty())

    fun displayURL(size: ImageSize): String? {
        if (imageURL.isNullOrEmpty()) return null
        return product?.url(imageURL!!, size) ?: imageURL
    }

    companion object {
        fun from(product: Product): ChamakDesignItem {
            val url = product.processedImageURL ?: product.imageURL ?: product.rawImageURL ?: ""
            return ChamakDesignItem(
                id = product.id,
                title = product.title ?: "Catalogue Design",
                subtitle = product.jewelleryType?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Catalogue Item",
                imageURL = url,
                localImageData = null,
                product = product,
                contentHash = null,
                declaredJewelleryType = null
            )
        }

        fun from(imageData: ByteArray, slot: Int, declaredJewelleryType: String? = null): ChamakDesignItem {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = md.digest(imageData).joinToString("") { "%02x".format(it) }
            val label = declaredJewelleryType?.let { JewelleryTypeCanonical.displayLabel(it) } ?: "Direct Upload"
            return ChamakDesignItem(
                id = "custom_slot_${slot}_${java.util.UUID.randomUUID()}",
                title = "Custom Photo $slot",
                subtitle = label,
                imageURL = null,
                localImageData = imageData,
                product = null,
                contentHash = hash,
                declaredJewelleryType = declaredJewelleryType
            )
        }
    }
}

@Serializable
data class WeightedAttribute(
    val id: String,
    val attribute: String,
    @SerialName("image1_feature") val image1Feature: String,
    @SerialName("image2_feature") val image2Feature: String,
    val weight: Double
)

@Serializable
data class WholesalerFormInput(
    @SerialName("slider_weights") val sliderWeights: Map<String, Double> = emptyMap(),
    @SerialName("attribute_context") val attributeContext: List<WeightedAttribute> = emptyList(),
    val note: String? = null
)

@Serializable
data class SetCreationInput(
    val backdrop: SetBackdrop,
    val note: String? = null
)

@Serializable
data class SetCreationOutput(
    val path: String,
    val variants: Map<String, String> = emptyMap()
)

@Serializable
data class ChamakAttempt(
    val attempt: Int,
    val path: String,
    val variants: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null
) {
    fun path(size: ImageSize): String {
        return variants[size.value] ?: path
    }
}

@Serializable
data class ChamakGeneration(
    val id: String,
    @SerialName("wholesaler_id") val wholesalerId: String,
    @SerialName("source_image_1_url") val sourceImage1URL: String,
    @SerialName("source_image_2_url") val sourceImage2URL: String,
    @SerialName("source_image_3_url") val sourceImage3URL: String? = null,
    @SerialName("source_image_4_url") val sourceImage4URL: String? = null,
    @SerialName("stage1_analysis_json") val stage1AnalysisJSON: Stage1Analysis? = null,
    @SerialName("wholesaler_form_json") val wholesalerFormJSON: WholesalerFormInput? = null,
    @SerialName("note_text") val noteText: String? = null,
    @SerialName("compiled_prompt_text") val compiledPromptText: String? = null,
    @SerialName("prompt_version") val promptVersion: String = "v1.0-chamak",
    @SerialName("output_image_url") val outputImageURL: String? = null,
    @SerialName("output_variants") val outputVariants: Map<String, String> = emptyMap(),
    @SerialName("output_images") val outputImages: List<SetCreationOutput> = emptyList(),
    @SerialName("output_history") val outputHistory: List<ChamakAttempt> = emptyList(),
    val status: ChamakStatus = ChamakStatus.Queued,
    @SerialName("content_flag_hit") val contentFlagHit: ContentFlag? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("completed_at") val completedAt: String? = null,
    val mode: ChamakMode = ChamakMode.Fusion,
    @SerialName("set_backdrop") val setBackdrop: SetBackdrop? = null
) {
    val failureUserMessage: String
        get() = errorMessage
            ?: contentFlagHit?.userMessage
            ?: stage1AnalysisJSON?.contentFlag?.userMessage
            ?: "AI generation could not complete. Please try again or report the issue."

    fun outputPath(size: ImageSize): String? {
        return outputVariants[size.value] ?: outputImageURL
    }
}

@Serializable
data class ChamakFeedback(
    val id: String? = null,
    @SerialName("chamak_generation_id") val chamakGenerationId: String,
    val satisfied: Boolean,
    @SerialName("what_went_wrong_text") val whatWentWrongText: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

data class ChamakQuote(
    val text: String,
    val author: String
) {
    companion object {
        val quotes: List<ChamakQuote> = listOf(
            ChamakQuote(
                text = "Jewelry is not just ornament; it is the poetry of metal and stone.",
                author = "Master Craftsman"
            ),
            ChamakQuote(
                text = "True mastery lies in harmonizing classic tradition with bold modern design.",
                author = "Royal Gemologist"
            ),
            ChamakQuote(
                text = "Every jewel carries a soul forged in patience and refined by vision.",
                author = "Ancient Goldsmith Proverb"
            ),
            ChamakQuote(
                text = "Simplicity and luxury meet where flawless geometry meets radiant gold.",
                author = "Design Philosophy"
            ),
            ChamakQuote(
                text = "Synthesizing the best of two creations yields an unmatched masterpiece.",
                author = "JewelIndia Chamak"
            )
        )
    }
}

object ChamakSlot {
    fun label(slot: Int, mode: ChamakMode): String = when {
        mode == ChamakMode.SetCreation -> "Piece $slot"
        slot == 1 -> "Design 1"
        else -> "Design 2"
    }

    fun colorHex(slot: Int): Long = when (slot) {
        1 -> 0xFFCA8A04L
        2 -> 0xFF3B82F6L
        3 -> 0xFF10B981L
        else -> 0xFFE11D48L
    }
}

enum class JewelleryTypeCanonical(val key: String, val displayLabel: String) {
    Chain("chain", "Chains"),
    Necklace("necklace", "Necklace"),
    Ring("ring", "Ring"),
    Earrings("earrings", "Earrings"),
    Bangle("bangle", "Bangle"),
    Pendant("pendant", "Pendant"),
    Nosepin("nosepin", "Nosepin"),
    Haram("haram", "Haram"),
    Mangalsutra("mangalsutra", "Mangalsutra");

    companion object {
        val allCanonical: List<String> = entries.map { it.key }

        fun canonicalize(raw: String?): String? {
            val trimmed = raw?.trim()?.lowercase() ?: return null
            if (trimmed.isEmpty()) return null
            return when (trimmed) {
                "chain", "chains", "neck chain", "neck chains" -> Chain.key
                "necklace", "necklaces" -> Necklace.key
                "ring", "rings" -> Ring.key
                "earring", "earrings", "jhumka", "jhumkas" -> Earrings.key
                "bangle", "bangles" -> Bangle.key
                "pendant", "pendants" -> Pendant.key
                "nosepin", "nosepins", "nose pin", "nose pins" -> Nosepin.key
                "haram", "harams" -> Haram.key
                "mangalsutra", "mangalsutras" -> Mangalsutra.key
                else -> null
            }
        }

        fun displayLabel(canonicalKey: String): String =
            entries.find { it.key.equals(canonicalKey, ignoreCase = true) }?.displayLabel
                ?: canonicalKey.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

