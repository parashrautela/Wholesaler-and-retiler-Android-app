package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ContentFlag
import com.jewelindia.app.data.api.Stage1Analysis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChamakFailureMessageTest {

    private fun makeGeneration(
        contentFlagHit: ContentFlag? = null,
        stage1Analysis: Stage1Analysis? = null,
        errorMessage: String? = null
    ): ChamakGeneration {
        return ChamakGeneration(
            id = "gen-123",
            wholesalerId = "ws-123",
            sourceImage1URL = "url1",
            sourceImage2URL = "url2",
            stage1AnalysisJSON = stage1Analysis,
            contentFlagHit = contentFlagHit,
            errorMessage = errorMessage,
            createdAt = "2026-10-10T00:00:00Z"
        )
    }

    @Test
    fun testNotJewelryRejectionMessage() {
        val rejected = makeGeneration(contentFlagHit = ContentFlag.NotJewelry)
        assertEquals(
            "Jewellery was not detected in one or both selected images. Please select two clear jewellery photos to continue.",
            rejected.failureUserMessage
        )
    }

    @Test
    fun testLegacyStage1AnalysisBlurryMessage() {
        val blurry = makeGeneration(
            contentFlagHit = null,
            stage1Analysis = Stage1Analysis(contentFlag = ContentFlag.TooUnclearToAssess)
        )
        assertTrue(blurry.failureUserMessage.contains("blurry"))
    }

    @Test
    fun testUnclassifiedAndOkFlagsDefaultMessage() {
        val unknown = makeGeneration(contentFlagHit = null, stage1Analysis = null)
        assertTrue(unknown.failureUserMessage.contains("try again"))

        val ok = makeGeneration(contentFlagHit = ContentFlag.Ok, stage1Analysis = null)
        assertEquals(unknown.failureUserMessage, ok.failureUserMessage)
    }

    @Test
    fun testExplicitErrorMessageTakesPrecedence() {
        val quota = makeGeneration(
            contentFlagHit = null,
            stage1Analysis = null,
            errorMessage = "Analysis service unavailable; contact support."
        )
        assertEquals(quota.errorMessage, quota.failureUserMessage)
    }
}
