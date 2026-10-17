package com.jewelindia.app

import com.jewelindia.app.core.model.CreateManufacturingRequestParams
import com.jewelindia.app.data.api.WholesalerApi.ProductEdit
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonEncodingTest {

    @Test
    fun testClearProductImagesSendsExplicitJsonNull() {
        // Plan §7.6 and D13:
        // On iOS, a nil String? is omitted by the synthesized encoder.
        // On Android, clearProductImages explicitly sends null to actually clear the column in the database.
        val patch = buildJsonObject {
            put("processed_image_url", JsonNull)
            put("generated_image_urls", buildJsonArray { })
        }
        val serialized = patch.toString()

        assertTrue("Must contain explicit null", serialized.contains("\"processed_image_url\":null"))
        assertTrue("Must contain empty array", serialized.contains("\"generated_image_urls\":[]"))

        val parsed = Json.parseToJsonElement(serialized).jsonObject
        assertTrue(parsed.containsKey("processed_image_url"))
        assertEquals(JsonNull, parsed["processed_image_url"])
    }

    @Test
    fun testProductEditOmissionOfNulls() {
        val edit = ProductEdit(
            title = "Gold Bangle",
            category = "gold",
            jewelleryType = "bangle"
        )

        val jsonString = JewelJson.encodeToString(edit)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        // Present fields
        assertEquals("Gold Bangle", json["title"]?.toString()?.trim('"'))
        assertEquals("gold", json["category"]?.toString()?.trim('"'))
        assertEquals("bangle", json["jewellery_type"]?.toString()?.trim('"'))

        // Null fields MUST be omitted, not serialized as null
        assertFalse("style must be omitted", json.containsKey("style"))
        assertFalse("size must be omitted", json.containsKey("size"))
        assertFalse("metal_purity must be omitted", json.containsKey("metal_purity"))
        assertFalse("net_weight must be omitted", json.containsKey("net_weight"))
        assertFalse("gross_weight must be omitted", json.containsKey("gross_weight"))
        assertFalse("stone_weight must be omitted", json.containsKey("stone_weight"))
        assertFalse("stock_available must be omitted", json.containsKey("stock_available"))
        assertFalse("is_published must be omitted", json.containsKey("is_published"))
    }

    @Test
    fun testManufacturingEnquiryRequestEncoding() {
        val enquiry = CreateManufacturingRequestParams(
            assetId = "asset-uuid-1",
            category = "necklace",
            material = "gold",
            purity = "22K",
            minWeightGrams = 24.0,
            maxWeightGrams = 27.0,
            gemstonePreference = "Emerald accents",
            deliveryNeededDate = "2026-11-01",
            notes = "Urgent requirement"
        )

        val jsonString = JewelJson.encodeToString(enquiry)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        assertEquals("asset-uuid-1", json["asset_id"]?.toString()?.trim('"'))
        assertEquals("necklace", json["category"]?.toString()?.trim('"'))
        assertEquals("gold", json["material"]?.toString()?.trim('"'))
        assertEquals("22K", json["purity"]?.toString()?.trim('"'))
        assertEquals("24.0", json["min_weight_grams"]?.toString())
        assertEquals("27.0", json["max_weight_grams"]?.toString())
        assertEquals("Emerald accents", json["gemstone_preference"]?.toString()?.trim('"'))
        assertEquals("2026-11-01", json["delivery_needed_date"]?.toString()?.trim('"'))
        assertEquals("Urgent requirement", json["notes"]?.toString()?.trim('"'))
    }
}
