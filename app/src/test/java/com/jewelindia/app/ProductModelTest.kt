package com.jewelindia.app

import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UploadUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductModelTest {

    @Test
    fun testCanPublish() {
        // AI ready with verified output
        val p1 = Product(
            id = "1",
            aiProcessingState = "ready",
            aiVerifiedOutputURLs = listOf("https://example.com/verified.jpg")
        )
        assertTrue(p1.canPublish)

        // AI processing
        val p2 = Product(
            id = "2",
            aiProcessingState = "processing",
            aiVerifiedOutputURLs = emptyList()
        )
        assertFalse(p2.canPublish)

        // Legacy product with processed image
        val p3 = Product(
            id = "3",
            processedImageURL = "https://example.com/processed.jpg"
        )
        assertTrue(p3.canPublish)

        // Empty product
        val p4 = Product(id = "4")
        assertFalse(p4.canPublish)
    }

    @Test
    fun testDisplayImageUrlPriority() {
        val p = Product(
            id = "1",
            rawImageURL = "https://example.com/raw.jpg",
            processedImageURL = "https://example.com/processed.jpg",
            generatedImageURLs = listOf("https://example.com/gen.jpg"),
            showcaseImageURLs = listOf("https://example.com/showcase.jpg"),
            imageVariants = mapOf(
                "https://example.com/showcase.jpg" to mapOf(
                    "card" to "https://example.com/showcase_card.jpg",
                    "detail" to "https://example.com/showcase_detail.jpg"
                )
            )
        )

        // Priority 1: showcase image, with "card" variant lookup
        assertEquals("https://example.com/showcase_card.jpg", p.displayImageUrl(ImageSize.CARD))
        assertEquals("https://example.com/showcase_detail.jpg", p.displayImageUrl(ImageSize.DETAIL))
        // If variant not present, returns original
        assertEquals("https://example.com/showcase.jpg", p.displayImageUrl(ImageSize.FULL))
    }

    @Test
    fun testThumbnailUrlsDedup() {
        val p = Product(
            id = "1",
            generatedImageURLs = listOf(
                "https://example.com/img1.jpg",
                "https://example.com/img2.jpg"
            ),
            processedImageURL = "https://example.com/img1.jpg", // duplicate of gen
            customImageURLs = listOf(
                "https://example.com/img3.jpg",
                "https://example.com/img4.jpg",
                "https://example.com/img5.jpg" // 5th item, should be truncated to 4
            )
        )

        val thumbs = p.thumbnailUrls(ImageSize.CARD)
        assertEquals(4, thumbs.size)
        assertEquals("https://example.com/img1.jpg", thumbs[0])
        assertEquals("https://example.com/img2.jpg", thumbs[1])
        assertEquals("https://example.com/img3.jpg", thumbs[2])
        assertEquals("https://example.com/img4.jpg", thumbs[3])
    }

    @Test
    fun testUploadUsage() {
        val u1 = UploadUsage(used = 5, limit = 10)
        assertFalse(u1.isUnlimited)
        assertEquals("5/10", u1.display)
        assertEquals(5, u1.remaining)

        val u2 = UploadUsage(used = 12, limit = null)
        assertTrue(u2.isUnlimited)
        assertEquals("12", u2.display)
        assertNull(u2.remaining)
    }
}
