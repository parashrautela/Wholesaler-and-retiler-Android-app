package com.jewelindia.app.feature.wholesaler

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductPayloadTest {

    private val expectedKeys = setOf(
        "wholesaler_id",
        "title",
        "jewellery_type",
        "category",
        "style",
        "size",
        "metal_purity",
        "stock_available",
        "net_weight",
        "gross_weight",
        "stone_weight",
        "make_to_order_days",
        "wholesaler_email",
        "raw_image_url"
    )

    @Test
    fun testAllKeyNamesMatchTable() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Gold Necklace",
            jewelleryType = "necklace",
            category = "gold",
            style = "traditional",
            size = "standard",
            metalPurity = "22K",
            stockAvailable = true,
            grossWeight = "12.5",
            stoneWeight = "0.5",
            netWeight = "12.0",
            makeToOrderDays = null,
            wholesalerEmail = "ws@example.com",
            rawImageUrl = "https://example.com/image.jpg"
        )

        assertEquals(expectedKeys, payload.keys)
    }

    @Test
    fun testInStockCaseMakeToOrderDaysIsNull() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Ring",
            jewelleryType = "ring",
            category = "gold",
            style = "modern",
            size = "7",
            metalPurity = "18K",
            stockAvailable = true,
            grossWeight = "5.0",
            stoneWeight = "0.2",
            netWeight = "4.8",
            makeToOrderDays = "10" // even if string was present, stockAvailable = true makes it null
        )

        assertTrue(payload["stock_available"]!!.jsonPrimitive.boolean)
        assertEquals(JsonNull, payload["make_to_order_days"])
    }

    @Test
    fun testMakeToOrderCase() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Bangle",
            jewelleryType = "bangle",
            category = "gold",
            style = "bridal",
            size = "2-6",
            metalPurity = "22K",
            stockAvailable = false,
            grossWeight = "20.0",
            stoneWeight = "1.0",
            netWeight = "19.0",
            makeToOrderDays = "14"
        )

        assertFalse(payload["stock_available"]!!.jsonPrimitive.boolean)
        assertEquals(14, payload["make_to_order_days"]!!.jsonPrimitive.int)
    }

    @Test
    fun testBlankWeightsAreNull() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Earrings",
            jewelleryType = "earrings",
            category = "gold",
            style = "contemporary",
            size = "one-size",
            metalPurity = "22K",
            stockAvailable = true,
            grossWeight = "",
            stoneWeight = "   ",
            netWeight = null,
            makeToOrderDays = null
        )

        assertEquals(JsonNull, payload["gross_weight"])
        assertEquals(JsonNull, payload["stone_weight"])
        assertEquals(JsonNull, payload["net_weight"])
    }

    @Test
    fun testValidWeightsAreParsedProperly() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Earrings",
            jewelleryType = "earrings",
            category = "gold",
            style = "contemporary",
            size = "one-size",
            metalPurity = "22K",
            stockAvailable = true,
            grossWeight = "12.5",
            stoneWeight = "0.5",
            netWeight = "12.0",
            makeToOrderDays = null
        )

        assertEquals(12.5, payload["gross_weight"]!!.jsonPrimitive.double, 0.001)
        assertEquals(0.5, payload["stone_weight"]!!.jsonPrimitive.double, 0.001)
        assertEquals(12.0, payload["net_weight"]!!.jsonPrimitive.double, 0.001)
    }

    @Test
    fun testDraftIncludesIsPublishedFalse() {
        val payload = productPayload(
            wholesalerId = "ws-123",
            title = "Draft Ring",
            jewelleryType = "ring",
            category = "silver",
            style = "classic",
            size = "8",
            metalPurity = "925",
            stockAvailable = true,
            grossWeight = "6.0",
            stoneWeight = "0.0",
            netWeight = "6.0",
            makeToOrderDays = null,
            isPublished = false
        )

        assertFalse(payload["is_published"]!!.jsonPrimitive.boolean)
        assertEquals(expectedKeys + "is_published", payload.keys)
    }
}
