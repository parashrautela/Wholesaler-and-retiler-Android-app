package com.jewelindia.app

import com.jewelindia.app.data.api.EntitlementPurchase
import com.jewelindia.app.data.api.Plan
import com.jewelindia.app.data.api.PlanPurchase
import com.jewelindia.app.data.api.PlanStatus
import com.jewelindia.app.data.supabase.JewelJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanStatusDecodeTest {

    @Test
    fun testActivePlanStatusDecode() {
        val json = """
            {
                "active": true,
                "plan_key": "enterprise",
                "expires_at": "2026-12-31T23:59:59Z",
                "auto_renew": true,
                "renewed_now": false,
                "temporary_access": false
            }
        """.trimIndent()

        val status = JewelJson.decodeFromString<PlanStatus>(json)
        assertTrue(status.active)
        assertEquals("enterprise", status.planKey)
        assertEquals("2026-12-31T23:59:59Z", status.expiresAt)
        assertTrue(status.autoRenew)
        assertFalse(status.renewedNow)
        assertFalse(status.temporaryAccess)
        assertNull(status.renewalError)
    }

    @Test
    fun testDefaultAndGracePeriodPlanStatus() {
        // Tolerant decode of empty object
        val defaultStatus = JewelJson.decodeFromString<PlanStatus>("{}")
        assertFalse(defaultStatus.active)
        assertNull(defaultStatus.planKey)
        assertTrue(defaultStatus.autoRenew)

        // Temporary grace period with renewal error
        val graceJson = """
            {
                "active": true,
                "plan_key": "pro",
                "temporary_access": true,
                "renewal_error": "INSUFFICIENT_FUNDS"
            }
        """.trimIndent()
        val grace = JewelJson.decodeFromString<PlanStatus>(graceJson)
        assertTrue(grace.active)
        assertTrue(grace.temporaryAccess)
        assertEquals("INSUFFICIENT_FUNDS", grace.renewalError)
    }

    @Test
    fun testPlanModelAndPriceKey() {
        val json = """
            {
                "key": "growth",
                "label": "Growth Tier",
                "period_days": 30,
                "perks": ["1000 AI Credits", "5 Staff Seats", "HD Exports"]
            }
        """.trimIndent()

        val plan = JewelJson.decodeFromString<Plan>(json)
        assertEquals("growth", plan.id)
        assertEquals("plan.growth", plan.priceKey)
        assertEquals(30, plan.periodDays)
        assertEquals(3, plan.perks.size)
        assertEquals("1000 AI Credits", plan.perks[0])
    }

    @Test
    fun testPlanPurchaseAndEntitlementErrors() {
        val purchaseJson = """
            {
                "ok": false,
                "error": "INSUFFICIENT_CREDITS",
                "short_by": 450
            }
        """.trimIndent()

        val purchase = JewelJson.decodeFromString<PlanPurchase>(purchaseJson)
        assertFalse(purchase.ok)
        assertEquals("INSUFFICIENT_CREDITS", purchase.error)
        assertTrue(purchase.isInsufficientCredits)
        assertEquals(450, purchase.shortBy)

        val entitlementJson = """
            {
                "ok": false,
                "error": "INSUFFICIENT_CREDITS",
                "already_owned": false,
                "charged": 0,
                "short_by": 100
            }
        """.trimIndent()

        val entitlement = JewelJson.decodeFromString<EntitlementPurchase>(entitlementJson)
        assertFalse(entitlement.ok)
        assertTrue(entitlement.isInsufficientCredits)
        assertEquals(100, entitlement.shortBy)
    }
}
