// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/AddProductForm.swift
package com.jewelindia.app.feature.wholesaler

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

enum class FormSubmitStatus {
    Idle, Uploading, Saving, Done, Error;

    val isBusy: Boolean get() = this == Uploading || this == Saving || this == Done

    val caption: String
        get() = when (this) {
            Uploading -> "Uploading Image..."
            Saving -> "Saving metadata..."
            Done -> "Redirecting..."
            else -> ""
        }
}

/** Accepts "12,5" as well as "12.5": some phone keypads only offer a comma. */
internal fun weightNumber(text: String?): Double? =
    text?.trim()?.replace(',', '.')?.toDoubleOrNull()

internal fun productPayload(
    wholesalerId: String,
    title: String,
    jewelleryType: String,
    category: String,
    style: String,
    size: String,
    metalPurity: String,
    stockAvailable: Boolean,
    grossWeight: String?,
    stoneWeight: String?,
    netWeight: String?,
    makeToOrderDays: String?,
    wholesalerEmail: String? = null,
    rawImageUrl: String? = null,
    isPublished: Boolean? = null
): JsonObject = buildJsonObject {
    put("wholesaler_id", wholesalerId)
    put("title", title)
    put("jewellery_type", jewelleryType)
    // Optional fields: blank is stored as empty, never as "".
    put("category", category.ifBlank { null })
    put("style", style.ifBlank { null })
    put("size", size.ifBlank { null })
    put("metal_purity", metalPurity.ifBlank { null })
    put("stock_available", stockAvailable)

    val net = weightNumber(netWeight)
    if (net != null) put("net_weight", net) else put("net_weight", JsonNull)

    val gross = weightNumber(grossWeight)
    if (gross != null) put("gross_weight", gross) else put("gross_weight", JsonNull)

    val stone = weightNumber(stoneWeight)
    if (stone != null) put("stone_weight", stone) else put("stone_weight", JsonNull)

    if (stockAvailable) {
        put("make_to_order_days", JsonNull)
    } else {
        val days = makeToOrderDays?.trim()?.takeIf { it.isNotBlank() }?.toIntOrNull()
        if (days != null) put("make_to_order_days", days) else put("make_to_order_days", JsonNull)
    }

    if (!wholesalerEmail.isNullOrBlank()) {
        put("wholesaler_email", wholesalerEmail)
    } else {
        put("wholesaler_email", JsonNull)
    }

    if (!rawImageUrl.isNullOrBlank()) {
        put("raw_image_url", rawImageUrl)
    } else {
        put("raw_image_url", JsonNull)
    }

    if (isPublished != null) {
        put("is_published", isPublished)
    }
}

class AddProductFormState {
    var status by mutableStateOf(FormSubmitStatus.Idle)
    var submittedProduct by mutableStateOf<Product?>(null)
    var submissionKey by mutableStateOf(UUID.randomUUID().toString())
    var pendingProductId by mutableStateOf<String?>(null)
    var pendingRawImageUrl by mutableStateOf<String?>(null)
    var bannerError by mutableStateOf<String?>(null)
    val errors = mutableStateMapOf<String, String>()

    var selectedUri by mutableStateOf<Uri?>(null)
    var imageBytes by mutableStateOf<ByteArray?>(null)
    var title by mutableStateOf("")
    var jewelleryType by mutableStateOf("")
    var category by mutableStateOf("")
    var style by mutableStateOf("")
    var size by mutableStateOf("")
    var metalPurity by mutableStateOf("")
    var grossWeight by mutableStateOf("")
    var stoneWeight by mutableStateOf("")
    var netWeight by mutableStateOf("")
    var stockAvailable by mutableStateOf(true)
    var makeToOrderDays by mutableStateOf("")
    var imageCount by mutableIntStateOf(2)

    var usage by mutableStateOf(UploadUsage.unknown)
    var usageLoading by mutableStateOf(false)

    val isLimitReached: Boolean
        get() {
            val limit = usage.limit ?: return false
            return usage.used >= limit
        }

    val usageFraction: Float
        get() {
            val limit = usage.limit ?: return 0f
            if (limit <= 0) return 0f
            return (usage.used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
        }

    fun validate(requiresImage: Boolean): Boolean {
        val found = mutableMapOf<String, String>()
        if (requiresImage && selectedUri == null) {
            found["image"] = "Please upload a product image."
        }
        // Only the type and the net weight are required. Everything else is
        // optional, but what's typed into an optional field is still checked.
        if (jewelleryType.isEmpty()) found["jewellery_type"] = "Please select a jewellery type."
        if (weightNumber(netWeight)?.let { it > 0 } != true) {
            found["netWeight"] = "Please enter the net weight."
        }
        if (grossWeight.isNotBlank() && (weightNumber(grossWeight) ?: 0.0) <= 0) {
            found["grossWeight"] = "Gross weight must be greater than 0."
        }
        if (stoneWeight.isNotBlank() && (weightNumber(stoneWeight) ?: -1.0) < 0) {
            found["stoneWeight"] = "Stone weight must be 0 or greater."
        }
        if (!stockAvailable && makeToOrderDays.isNotBlank() && (makeToOrderDays.trim().toIntOrNull() ?: 0) <= 0) {
            found["makeToOrderDays"] = "Production time must be greater than 0 days."
        }

        errors.clear()
        errors.putAll(found)
        bannerError = if (found.isNotEmpty()) "Please correct the fields highlighted in red below." else null
        return found.isEmpty()
    }

    suspend fun submit(
        context: Context,
        wholesalerId: String,
        publish: Boolean,
        onSuccess: (savedAsDraft: Boolean) -> Unit
    ): Boolean {
        val requiresImage = publish && pendingProductId == null
        if (status.isBusy || !validate(requiresImage = requiresImage)) return false

        bannerError = null

        val finalTitle = title.trim().ifBlank {
            WholesalerProductOptions.types.firstOrNull { it.value == jewelleryType }?.label ?: "Untitled"
        }

        if (publish) {
            val userEmail = JewelSupabase.client.auth.currentUserOrNull()?.email

            // Retry saving details if processProduct already succeeded
            val existingId = pendingProductId
            if (existingId != null) {
                status = FormSubmitStatus.Saving
                try {
                    val payload = productPayload(
                        wholesalerId = wholesalerId,
                        title = finalTitle,
                        jewelleryType = jewelleryType,
                        category = category,
                        style = style,
                        size = size,
                        metalPurity = metalPurity,
                        stockAvailable = stockAvailable,
                        grossWeight = grossWeight,
                        stoneWeight = stoneWeight,
                        netWeight = netWeight,
                        makeToOrderDays = makeToOrderDays,
                        wholesalerEmail = userEmail,
                        rawImageUrl = pendingRawImageUrl
                    )
                    WholesalerApi.completeProduct(existingId, payload)
                    submittedProduct = Product(
                        id = existingId,
                        wholesalerId = wholesalerId,
                        title = finalTitle,
                        jewelleryType = jewelleryType,
                        rawImageURL = pendingRawImageUrl,
                        aiProcessingState = "queued"
                    )
                    pendingProductId = null
                    pendingRawImageUrl = null
                    submissionKey = UUID.randomUUID().toString()
                    status = FormSubmitStatus.Done
                    onSuccess(false)
                    return true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    bannerError = e.userMessage()
                    status = FormSubmitStatus.Error
                    return false
                }
            }

            // Normal AI upload path
            status = FormSubmitStatus.Uploading
            val uri = selectedUri
            if (uri == null) {
                bannerError = "Please upload a product image."
                status = FormSubmitStatus.Error
                return false
            }

            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw Exception("Could not read image file")

                val normalizedData = withContext(Dispatchers.IO) {
                    ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION)
                } ?: throw Exception("That file couldn't be decoded as a photo. Please choose another image.")

                status = FormSubmitStatus.Saving
                val result = WholesalerApi.processProduct(
                    imageData = normalizedData,
                    filename = "product.jpg",
                    mimeType = "image/jpeg",
                    title = finalTitle,
                    jewelleryType = jewelleryType,
                    wholesalerId = wholesalerId,
                    imageCount = imageCount,
                    submissionKey = submissionKey
                )

                val id = result.productId?.takeIf { it.isNotBlank() }
                    ?: throw Exception("Upload accepted, but its product ID wasn't returned. Check your catalogue before retrying.")

                pendingProductId = id
                pendingRawImageUrl = result.rawImageURL

                val payload = productPayload(
                    wholesalerId = wholesalerId,
                    title = finalTitle,
                    jewelleryType = jewelleryType,
                    category = category,
                    style = style,
                    size = size,
                    metalPurity = metalPurity,
                    stockAvailable = stockAvailable,
                    grossWeight = grossWeight,
                    stoneWeight = stoneWeight,
                    netWeight = netWeight,
                    makeToOrderDays = makeToOrderDays,
                    wholesalerEmail = userEmail,
                    rawImageUrl = result.rawImageURL
                )
                WholesalerApi.completeProduct(id, payload)

                submittedProduct = Product(
                    id = id,
                    wholesalerId = wholesalerId,
                    title = finalTitle,
                    jewelleryType = jewelleryType,
                    rawImageURL = result.rawImageURL,
                    aiProcessingState = "queued"
                )
                pendingProductId = null
                pendingRawImageUrl = null
                submissionKey = UUID.randomUUID().toString()
                status = FormSubmitStatus.Done
                onSuccess(false)
                return true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                bannerError = e.userMessage()
                status = FormSubmitStatus.Error
                return false
            }
        } else {
            // Draft path (Save as draft)
            val sessionUser = JewelSupabase.client.auth.currentUserOrNull()
            val uid = sessionUser?.id
            if (uid == null || !uid.equals(wholesalerId, ignoreCase = true)) {
                bannerError = "Your session isn't active on this device, so this can't be authorised. Please sign out and sign in again."
                status = FormSubmitStatus.Error
                return false
            }

            var draftImageUrl: String? = null
            val uri = selectedUri
            if (uri != null) {
                status = FormSubmitStatus.Uploading
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } ?: throw Exception("Could not read image file")

                    val normalizedData = withContext(Dispatchers.IO) {
                        ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION)
                    } ?: throw Exception("That file couldn't be decoded as a photo. Please choose another image.")

                    draftImageUrl = WholesalerApi.upload(
                        bucket = "plant-images",
                        path = "raw/${uid.lowercase()}/product-draft_${UUID.randomUUID()}.jpg",
                        data = normalizedData,
                        contentType = "image/jpeg",
                        upsert = false
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    bannerError = "Uploading the product image failed: ${e.userMessage()}"
                    status = FormSubmitStatus.Error
                    return false
                }
            }

            status = FormSubmitStatus.Saving
            try {
                val payload = productPayload(
                    wholesalerId = wholesalerId,
                    title = finalTitle,
                    jewelleryType = jewelleryType,
                    category = category,
                    style = style,
                    size = size,
                    metalPurity = metalPurity,
                    stockAvailable = stockAvailable,
                    grossWeight = grossWeight,
                    stoneWeight = stoneWeight,
                    netWeight = netWeight,
                    makeToOrderDays = makeToOrderDays,
                    wholesalerEmail = sessionUser.email,
                    rawImageUrl = draftImageUrl,
                    isPublished = false
                )
                WholesalerApi.insertDraftProduct(payload)
                submissionKey = UUID.randomUUID().toString()
                status = FormSubmitStatus.Done
                onSuccess(true)
                return true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                bannerError = "Saving the product draft failed: ${e.userMessage()}"
                status = FormSubmitStatus.Error
                return false
            }
        }
    }
}
