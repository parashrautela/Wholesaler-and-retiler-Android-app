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
import kotlinx.coroutines.CancellationException
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.net.userMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class AddProductFormState {
    var status by mutableStateOf(FormSubmitStatus.Idle)
    var submittedProduct by mutableStateOf<Product?>(null)
    private val submissionKey = UUID.randomUUID().toString()
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
        if (title.trim().isEmpty()) {
            found["title"] = "Product title is required."
        }
        if (jewelleryType.isEmpty()) found["jewellery_type"] = "Please select a jewellery type."
        if (category.isEmpty()) found["category"] = "Please select a material category."
        if (style.isEmpty()) found["style"] = "Please select a style aesthetic."
        if (size.isEmpty()) found["size"] = "Please select a size."
        if (metalPurity.isEmpty()) found["metalPurity"] = "Please select a purity."

        if (grossWeight.toDoubleOrNull()?.let { it > 0 } != true) {
            found["grossWeight"] = "Gross weight must be greater than 0."
        }
        if (stoneWeight.isBlank() || (stoneWeight.toDoubleOrNull() ?: -1.0) < 0) {
            found["stoneWeight"] = "Stone weight must be 0 or greater."
        }
        if (netWeight.toDoubleOrNull()?.let { it > 0 } != true) {
            found["netWeight"] = "Net weight must be greater than 0."
        }
        if (!stockAvailable && (makeToOrderDays.toIntOrNull() ?: 0) <= 0) {
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
        if (status.isBusy || !validate(requiresImage = publish)) return false

        bannerError = null
        status = FormSubmitStatus.Uploading

        val finalTitle = title.trim().ifBlank {
            WholesalerProductOptions.types.firstOrNull { it.value == jewelleryType }?.label ?: "Untitled"
        }

        val uri = selectedUri
        if (publish && uri != null) {
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
                submittedProduct = Product(
                    id = id, wholesalerId = wholesalerId, title = finalTitle,
                    jewelleryType = jewelleryType, rawImageURL = result.rawImageURL,
                    aiProcessingState = "queued"
                )
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
            status = FormSubmitStatus.Done
            onSuccess(true)
            return true
        }
    }
}
