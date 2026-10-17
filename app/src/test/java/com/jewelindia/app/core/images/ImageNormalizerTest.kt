package com.jewelindia.app.core.images

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageNormalizerTest {

    @Test
    fun testPickedFilePdfDetection() {
        val pdf1 = PickedFile(
            data = byteArrayOf(1, 2, 3),
            filename = "document.pdf",
            mimeType = "application/pdf"
        )
        assertTrue(pdf1.isPDF)

        val pdf2 = PickedFile(
            data = byteArrayOf(1, 2, 3),
            filename = "document.PDF",
            mimeType = "application/octet-stream"
        )
        assertTrue(pdf2.isPDF)

        val image = PickedFile(
            data = byteArrayOf(1, 2, 3),
            filename = "photo.jpg",
            mimeType = "image/jpeg"
        )
        assertFalse(image.isPDF)
    }

    @Test
    fun testNormalizedPassesPdfUntouched() {
        val pdf = PickedFile(
            data = byteArrayOf(0x25, 0x50, 0x44, 0x46), // %PDF
            filename = "gst_cert.pdf",
            mimeType = "application/pdf"
        )
        val result = ImageNormalizer.normalized(pdf, maxDimension = 1200)
        assertEquals(pdf, result)
        assertEquals("gst_cert.pdf", result.filename)
        assertEquals("application/pdf", result.mimeType)
    }

    @Test
    fun testInvalidImageDataReturnsNull() {
        val garbage = byteArrayOf(0, 1, 2, 3, 4, 5)
        val result = ImageNormalizer.jpeg(garbage, maxDimension = 1200)
        assertNull(result)
    }

    @Test
    fun testConstants() {
        assertEquals(8 * 1024 * 1024, ImageNormalizer.MAX_UPLOAD_BYTES)
        assertEquals(2048, ImageNormalizer.MAX_PRODUCT_DIMENSION)
        assertEquals(1200, ImageNormalizer.MAX_DOCUMENT_DIMENSION)
    }
}
