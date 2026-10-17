package com.jewelindia.app

import com.jewelindia.app.data.api.CreditLedgerEntry
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.data.supabase.JewelJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditWalletDecodeTest {

    @Test
    fun testFullCreditWalletDecode() {
        val json = """
            {
                "ok": true,
                "available": 1250,
                "lifetime_granted": 5000,
                "lifetime_spent": 3750,
                "lifetime_expired": 0,
                "expiring_soon": 100,
                "next_expiry": "2026-11-01T00:00:00Z",
                "low_balance": false,
                "low_balance_threshold": 25,
                "recovery_owed": 0,
                "mode": "daily",
                "daily_allowance": 2000,
                "resets_at": "2026-10-07T00:00:00+05:30",
                "server_now": "2026-10-06T18:00:00+00:00",
                "shared_business_wallet": true,
                "legacy_preserved": 500
            }
        """.trimIndent()

        val wallet = JewelJson.decodeFromString<CreditWallet>(json)
        assertTrue(wallet.ok)
        assertEquals(1250, wallet.available)
        assertEquals(5000, wallet.lifetimeGranted)
        assertEquals(3750, wallet.lifetimeSpent)
        assertEquals("daily", wallet.mode)
        assertEquals(2000, wallet.dailyAllowance)
        assertEquals("2026-10-07T00:00:00+05:30", wallet.resetsAt)
        assertEquals("2026-10-06T18:00:00+00:00", wallet.serverNow)
        assertTrue(wallet.sharedBusinessWallet)
        assertEquals(500, wallet.legacyPreserved)
        assertFalse(wallet.lowBalance)
    }

    @Test
    fun testMinimalAndTolerantDecode() {
        // Wholesaler with no credit_accounts row yet
        val json = "{}"
        val wallet = JewelJson.decodeFromString<CreditWallet>(json)
        assertFalse(wallet.ok)
        assertEquals(0, wallet.available)
        assertEquals(0, wallet.lifetimeGranted)
        assertEquals(0, wallet.lifetimeSpent)
        assertFalse(wallet.lowBalance)
        assertNull(wallet.mode)
        assertNull(wallet.dailyAllowance)
    }

    @Test
    fun testErrorWalletDecode() {
        val json = """{"ok": false, "error": "NOT_VERIFIED"}"""
        val wallet = JewelJson.decodeFromString<CreditWallet>(json)
        assertFalse(wallet.ok)
        assertEquals("NOT_VERIFIED", wallet.errorCode)
    }

    @Test
    fun testRollingAndPausedWalletDecode() {
        val rollingJson = """
            {
                "ok": true,
                "policy_type": "rolling_24h",
                "daily_allowance": 5000,
                "next_allowance": 1000,
                "paid_available": 500,
                "gift_available": 300,
                "is_paused": false
            }
        """.trimIndent()
        val rolling = JewelJson.decodeFromString<CreditWallet>(rollingJson)
        assertEquals("rolling_24h", rolling.policyType)
        assertEquals(1000, rolling.nextAllowance)
        assertEquals(500, rolling.paidAvailable)
        assertEquals(300, rolling.giftAvailable)
        assertFalse(rolling.isPaused)

        val pausedJson = """
            {
                "ok": true,
                "is_paused": true,
                "daily_allowance": 0
            }
        """.trimIndent()
        val paused = JewelJson.decodeFromString<CreditWallet>(pausedJson)
        assertTrue(paused.isPaused)
        assertEquals(0, paused.dailyAllowance)
    }

    @Test
    fun testCreditLedgerEntryDisplayTitles() {
        fun makeEntry(kind: String, featureKey: String? = null, refType: String? = null) = CreditLedgerEntry(
            id = "test",
            delta = 10,
            kind = kind,
            featureKey = featureKey,
            referenceType = refType,
            createdAt = "2026-10-10T00:00:00Z"
        )

        // Debits
        assertEquals("Chamak Combine", makeEntry("debit", featureKey = "chamak.generate").displayTitle)
        assertEquals("Chamak Combine (Custom Photos)", makeEntry("debit", featureKey = "chamak.generate_custom").displayTitle)
        assertEquals("Set Creation", makeEntry("debit", featureKey = "chamak.set_creation").displayTitle)
        assertEquals("Set Creation (Custom Photos)", makeEntry("debit", featureKey = "chamak.set_creation_custom").displayTitle)
        assertEquals("Try Again", makeEntry("debit", featureKey = "chamak.reroll").displayTitle)
        assertEquals("Product Upload", makeEntry("debit", featureKey = "product.upload").displayTitle)
        assertEquals("Reprocess Product", makeEntry("debit", featureKey = "product.reprocess").displayTitle)

        // Grants
        assertEquals("Credits purchased", makeEntry("grant", refType = "purchase").displayTitle)
        assertEquals("Daily allowance", makeEntry("grant", refType = "daily").displayTitle)
        assertEquals("Welcome gift", makeEntry("grant", refType = "welcome").displayTitle)
        assertEquals("Refunded — generation failed", makeEntry("grant", refType = "chamak_generation").displayTitle)

        // Special reference types
        assertEquals("Invitation gift reserved", makeEntry("debit", refType = "invitation_funding").displayTitle)
        assertEquals("Invitation gift refund", makeEntry("refund", refType = "invitation_funding").displayTitle)
        assertEquals("Retailer invitation gift", makeEntry("grant", refType = "invitation_gift").displayTitle)
        assertEquals("Purchased credits preserved", makeEntry("grant", refType = "purchased_carryover").displayTitle)
        assertEquals("Verified referral reward", makeEntry("grant", refType = "referral_bonus").displayTitle)

        // Others
        assertEquals("Refunded — generation failed", makeEntry("refund").displayTitle)
        assertEquals("Credits expired", makeEntry("expiry").displayTitle)
        assertEquals("Adjustment", makeEntry("adjustment").displayTitle)
    }
}
