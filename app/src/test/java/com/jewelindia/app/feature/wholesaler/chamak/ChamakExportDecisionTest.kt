package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.data.api.EntitlementPurchase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChamakExportDecisionTest {

    @Test
    fun testExportDecisionOkReturnsProceed() {
        val res = EntitlementPurchase(ok = true, charged = 10)
        val decision = exportDecision(res)
        assertEquals(ExportDecision.Proceed, decision)
    }

    @Test
    fun testExportDecisionAlreadyOwnedReturnsProceed() {
        val res = EntitlementPurchase(ok = false, alreadyOwned = true)
        val decision = exportDecision(res)
        assertEquals(ExportDecision.Proceed, decision)
    }

    @Test
    fun testExportDecisionInsufficientCreditsReturnsNeedCredits() {
        val res = EntitlementPurchase(ok = false, error = "INSUFFICIENT_CREDITS", shortBy = 5)
        val decision = exportDecision(res)
        assertEquals(ExportDecision.NeedCredits, decision)
    }

    @Test
    fun testExportDecisionOtherErrorReturnsFail() {
        val res = EntitlementPurchase(ok = false, error = "GENERATION_NOT_FOUND")
        val decision = exportDecision(res)
        assertTrue(decision is ExportDecision.Fail)
        assertEquals("GENERATION_NOT_FOUND", (decision as ExportDecision.Fail).message)
    }
}
