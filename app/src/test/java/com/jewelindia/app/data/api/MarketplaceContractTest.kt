package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.supabase.JewelJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceContractTest {

    @Test
    fun testWholesalerDecoding() {
        val json = """
            {
                "id": "wh-123",
                "user_id": "usr-456",
                "business_name": "Kalyan Jewels",
                "email": "kalyan@example.com",
                "verification_status": "verified",
                "has_visited_dashboard": true
            }
        """.trimIndent()

        val wholesaler = JewelJson.decodeFromString<Wholesaler>(json)
        assertEquals("wh-123", wholesaler.id)
        assertEquals("Kalyan Jewels", wholesaler.displayName)
        assertTrue(wholesaler.hasVisitedDashboard == true)
    }

    @Test
    fun testProductDecoding() {
        val json = """
            {
                "id": "prod-1",
                "wholesaler_id": "wh-1",
                "title": "Diamond Choker",
                "jewellery_type": "necklace",
                "is_published": true,
                "stock_available": true,
                "processed_image_url": "https://img.co/1.jpg",
                "image_url": "https://img.co/1.jpg"
            }
        """.trimIndent()

        val product = JewelJson.decodeFromString<Product>(json)
        assertEquals("Diamond Choker", product.title)
        assertTrue(product.canPublish)
        assertEquals("https://img.co/1.jpg", product.displayImageUrl())
    }

    @Test
    fun testChamakStatusLabels() {
        assertEquals("Queued", ChamakStatus.Queued.displayLabel())
        assertEquals("Combining Designs", ChamakStatus.Generating.displayLabel(ChamakMode.Fusion))
        assertEquals("Staging Your Set", ChamakStatus.Generating.displayLabel(ChamakMode.SetCreation))
        assertEquals("Set Complete", ChamakStatus.Done.displayLabel(ChamakMode.SetCreation))
        assertEquals("Designs Combined", ChamakStatus.Done.displayLabel(ChamakMode.Fusion))
    }

    @Test
    fun testSetBackdropEnum() {
        assertEquals(SetBackdrop.VelvetBust, SetBackdrop.fromRaw("velvet_bust"))
        assertEquals(SetBackdrop.DarkSlate, SetBackdrop.fromRaw("dark_slate"))
        assertEquals(SetBackdrop.Festive, SetBackdrop.fromRaw("festive"))
        assertEquals(SetBackdrop.CleanStudio, SetBackdrop.fromRaw("clean_studio"))
        // fallback
        assertEquals(SetBackdrop.VelvetBust, SetBackdrop.fromRaw("unknown_future_preset"))
    }

    @Test
    fun testCreditLedgerEntryDisplayTitle() {
        val entry1 = CreditLedgerEntry(
            id = "1",
            delta = -5,
            kind = "debit",
            featureKey = "chamak.generate",
            createdAt = "2026-10-17T00:00:00Z"
        )
        assertEquals("Chamak Combine", entry1.displayTitle)

        val entry2 = CreditLedgerEntry(
            id = "2",
            delta = 100,
            kind = "grant",
            referenceType = "purchase",
            createdAt = "2026-10-17T00:00:00Z"
        )
        assertEquals("Credits purchased", entry2.displayTitle)
    }

    @Test
    fun testTopUpQuoteCalculation() {
        // ₹1000 + 18% GST = ₹1180 total
        // creditsPerRupee = 10
        val quote = TopUpQuote.of(amountExGST = 1000, gstPercent = 18, creditsPerRupee = 10.0)
        assertEquals(180.0, quote.gstINR, 0.01)
        assertEquals(1180.0, quote.totalINR, 0.01)
        assertEquals(10000, quote.credits)
    }
}
