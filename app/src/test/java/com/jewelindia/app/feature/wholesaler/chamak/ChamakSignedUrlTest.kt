package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakGeneration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChamakSignedUrlTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testVariantPathSelectionWithFallback() {
        val generation = ChamakGeneration(
            id = "g-1",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            outputImageURL = "chamak-outputs/raw_render.png",
            outputVariants = mapOf(
                "card" to "chamak-outputs/card_small.webp"
            )
        )

        // Card size finds specific variant
        assertEquals("chamak-outputs/card_small.webp", generation.outputPath(ImageSize.CARD))

        // Detail and Full fall back to outputImageURL when missing from variants
        assertEquals("chamak-outputs/raw_render.png", generation.outputPath(ImageSize.DETAIL))
        assertEquals("chamak-outputs/raw_render.png", generation.outputPath(ImageSize.FULL))
    }

    @Test
    fun testEmptyVariantsAllFallback() {
        val generation = ChamakGeneration(
            id = "g-2",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            outputImageURL = "chamak-outputs/legacy_render.png",
            outputVariants = emptyMap()
        )

        assertEquals("chamak-outputs/legacy_render.png", generation.outputPath(ImageSize.CARD))
        assertEquals("chamak-outputs/legacy_render.png", generation.outputPath(ImageSize.DETAIL))
        assertEquals("chamak-outputs/legacy_render.png", generation.outputPath(ImageSize.FULL))
    }

    @Test
    fun testAlreadySignedUrlPassesThroughUnchanged() = runTest(testDispatcher) {
        val signedUrlWithToken = "https://example.supabase.co/storage/v1/object/sign/chamak-outputs/abc.png?token=eyJhbGciOiJIUzI1NiJ9.test"
        val result = ChamakApi.getSignedURL(signedUrlWithToken)
        assertEquals(signedUrlWithToken, result)
    }

    @Test
    fun testStorageUrlPassesThroughUnchanged() = runTest(testDispatcher) {
        val publicStorageUrl = "https://myproject.supabase.co/storage/v1/object/public/catalogue/item.jpg"
        val result = ChamakApi.getSignedURL(publicStorageUrl)
        assertEquals(publicStorageUrl, result)
    }
}
