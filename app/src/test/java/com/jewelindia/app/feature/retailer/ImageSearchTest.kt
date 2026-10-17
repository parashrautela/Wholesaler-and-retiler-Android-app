package com.jewelindia.app.feature.retailer

import com.jewelindia.app.data.supabase.JewelJson
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageSearchTest {

    @Test
    fun testMultipartBodyFormatting() {
        val dummyPhoto = "dummy-image-bytes".toByteArray()
        val body = CatalogueImageSearchApi.buildSearchBody(dummyPhoto, "necklace")

        val buffer = Buffer()
        body.writeTo(buffer)
        val bodyString = buffer.readUtf8()

        assertTrue(bodyString.contains("name=\"jewellery_type\""))
        assertTrue(bodyString.contains("necklace"))
        assertTrue(bodyString.contains("name=\"photo\""))
        assertTrue(bodyString.contains("filename=\"reference.jpg\""))
    }

    @Test
    fun testLegacyCutoffAndAllowedIdFiltering() {
        val json = """
            {
                "matches": [
                    {"id": "own", "similarity": 0.99},
                    {"id": "forbidden", "similarity": 1.0},
                    {"id": "weak", "similarity": 0.4}
                ],
                "checked": 3,
                "total": 3,
                "skipped": 0
            }
        """.trimIndent()

        val result = JewelJson.decodeFromString<ImageSearchResult>(json)
        val filtered = result.filterMatches(allowedIDs = setOf("own", "weak"))

        assertEquals(1, filtered.size)
        assertEquals("own", filtered[0].id)
    }

    @Test
    fun testJevDecisionSourceFiltering() {
        val json = """
            {
                "matches": [
                    {"id": "own", "similarity": 0.85, "decision": "similar", "jev_probability": 0.98},
                    {"id": "weak", "similarity": 0.99, "decision": "uncertain", "jev_probability": 0.8},
                    {"id": "forbidden", "similarity": 0.99, "decision": "similar", "jev_probability": 0.99}
                ],
                "checked": 3,
                "total": 3,
                "skipped": 0,
                "decision_source": "jev"
            }
        """.trimIndent()

        val result = JewelJson.decodeFromString<ImageSearchResult>(json)
        val filtered = result.filterMatches(allowedIDs = setOf("own", "weak"))

        assertEquals(1, filtered.size)
        assertEquals("own", filtered[0].id)
    }

    @Test
    fun testNonFiniteSimilarityDropped() {
        val matches = listOf(
            ImageSearchMatch("valid", 0.95),
            ImageSearchMatch("nan", Double.NaN),
            ImageSearchMatch("inf", Double.POSITIVE_INFINITY)
        )
        val result = ImageSearchResult(matches = matches)
        val filtered = result.filterMatches()

        assertEquals(1, filtered.size)
        assertEquals("valid", filtered[0].id)
    }

    @Test
    fun testMaxMatchesCappedAt20() {
        val matches = (1..30).map { ImageSearchMatch("id-$it", 0.95) }
        val result = ImageSearchResult(matches = matches)
        val filtered = result.filterMatches()

        assertEquals(20, filtered.size)
    }

    @Test
    fun testFailureMessagesAndByteLimit() {
        assertEquals("Choose a photo smaller than 10 MB.", ImageSearchFailure.TooLarge.messageText)
        assertEquals("Choose a readable photo of the jewellery.", ImageSearchFailure.InvalidImage.messageText)
        assertEquals(10 * 1024 * 1024, ImageSearchFailure.MAXIMUM_BYTES)

        val err404 = ImageSearchFailure.Message("The image search service needs an update. Please try again after the service is deployed.")
        assertTrue(err404.messageText.contains("needs an update"))

        val err503 = ImageSearchFailure.Message("Catalogue image search is updating. Please retry shortly.")
        assertTrue(err503.messageText.contains("updating"))
    }
}
