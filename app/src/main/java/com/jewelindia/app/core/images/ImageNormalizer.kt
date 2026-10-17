// Port of: wholesaler ios/JewelIndia/Sources/Core/ImageNormalizer.swift
package com.jewelindia.app.core.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

data class PickedFile(
    val data: ByteArray,
    val filename: String,
    val mimeType: String
) {
    val isPDF: Boolean
        get() = mimeType.equals("application/pdf", ignoreCase = true) ||
                filename.endsWith(".pdf", ignoreCase = true)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PickedFile
        if (!data.contentEquals(other.data)) return false
        if (filename != other.filename) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Normalises picked images to JPEG before they leave the device.
 *
 * Product photos keep more detail (maxDimension = 2048).
 * Identity/business documents only need to be legible (maxDimension = 1200).
 * Re-encoding steps quality down until within maxUploadBytes (8 MB).
 */
object ImageNormalizer {
    const val MAX_UPLOAD_BYTES: Int = 8 * 1024 * 1024
    const val MAX_PRODUCT_DIMENSION: Int = 2048
    const val MAX_DOCUMENT_DIMENSION: Int = 1200

    fun jpeg(
        data: ByteArray,
        maxDimension: Int,
        quality: Float = 0.85f
    ): ByteArray? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        var width = options.outWidth
        var height = options.outHeight
        val sampleSize = calculateInSampleSize(width, height, maxDimension, maxDimension)

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, decodeOptions) ?: return null

        width = bitmap.width
        height = bitmap.height

        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            val newWidth = (width * scale).roundToInt().coerceAtLeast(1)
            val newHeight = (height * scale).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        var currentQuality = (quality * 100).roundToInt().coerceIn(40, 100)
        var stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)
        var encoded = stream.toByteArray()

        while (encoded.size > MAX_UPLOAD_BYTES && currentQuality > 40) {
            currentQuality -= 15
            stream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, stream)
            encoded = stream.toByteArray()
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()

        return encoded
    }

    fun normalized(
        file: PickedFile,
        maxDimension: Int,
        quality: Float = 0.85f
    ): PickedFile {
        if (file.isPDF) return file
        val data = jpeg(file.data, maxDimension, quality) ?: return file
        val stem = file.filename.substringBeforeLast(".")
        val name = (if (stem.isBlank()) "upload" else stem) + ".jpg"
        return PickedFile(
            data = data,
            filename = name,
            mimeType = "image/jpeg"
        )
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
