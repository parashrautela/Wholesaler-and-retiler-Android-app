package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.data.api.WholesalerApi.ProductEdit
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.decodeFromString
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.ImageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductEditEncodingTest {

    @Test
    fun testNullsAreOmittedInEncoding() {
        val edit = ProductEdit(
            title = "Royal Diamond Necklace",
            category = "gold",
            jewelleryType = "necklace"
            // all other fields are null
        )

        val jsonString = JewelJson.encodeToString(edit)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        // Present fields
        assertEquals("Royal Diamond Necklace", json["title"]?.toString()?.trim('"'))
        assertEquals("gold", json["category"]?.toString()?.trim('"'))
        assertEquals("necklace", json["jewellery_type"]?.toString()?.trim('"'))

        // Null fields must NOT be in the serialized JSON
        assertFalse("style should be omitted", json.containsKey("style"))
        assertFalse("size should be omitted", json.containsKey("size"))
        assertFalse("metal_purity should be omitted", json.containsKey("metal_purity"))
        assertFalse("net_weight should be omitted", json.containsKey("net_weight"))
        assertFalse("gross_weight should be omitted", json.containsKey("gross_weight"))
        assertFalse("stone_weight should be omitted", json.containsKey("stone_weight"))
        assertFalse("stock_available should be omitted", json.containsKey("stock_available"))
        assertFalse("make_to_order_days should be omitted", json.containsKey("make_to_order_days"))
        assertFalse("is_published should be omitted", json.containsKey("is_published"))
        assertFalse("custom_image_urls should be omitted", json.containsKey("custom_image_urls"))
        assertFalse("showcase_image_urls should be omitted", json.containsKey("showcase_image_urls"))
    }

    @Test
    fun testCategoryVsJewelleryTypeMapping() {
        // In the Wholesaler API contract and iOS EditProductView:
        // `category` is the material dropdown (gold, silver, diamond, etc.)
        // `jewellery_type` is the jewellery type (necklace, rings, earrings, etc.)
        val edit = ProductEdit(
            category = "gold",
            jewelleryType = "rings",
            metalPurity = "22k"
        )

        val jsonString = JewelJson.encodeToString(edit)
        val json = Json.parseToJsonElement(jsonString).jsonObject

        // Verify JSON keys match the DB column names exactly
        assertTrue(json.containsKey("category"))
        assertTrue(json.containsKey("jewellery_type"))
        assertFalse("jewelleryType camelCase must not be used", json.containsKey("jewelleryType"))

        assertEquals("gold", json["category"]?.toString()?.trim('"'))
        assertEquals("rings", json["jewellery_type"]?.toString()?.trim('"'))
        assertEquals("22k", json["metal_purity"]?.toString()?.trim('"'))
    }
    @Test
    fun reorderedShowcaseSavesAndReloadsWithTheChosenCover() {
        val images = mutableListOf("https://example.com/front.jpg", "https://example.com/blue.jpg", "https://example.com/back.jpg")
        java.util.Collections.swap(images, 2, 1)
        java.util.Collections.swap(images, 1, 0)
        val edit = ProductEdit(showcaseImageUrls = images, customImageUrls = listOf(images[2]))
        val saved = Json.parseToJsonElement(JewelJson.encodeToString(edit)).jsonObject
        assertEquals(images, saved["showcase_image_urls"]!!.jsonArray.map { it.jsonPrimitive.content })
        val reloaded = JewelJson.decodeFromString<Product>("""{"id":"ring","showcase_image_urls":${saved["showcase_image_urls"]},"generated_image_urls":["https://example.com/old.jpg"]}""")
        assertEquals("https://example.com/back.jpg", reloaded.primaryImage)
        assertEquals(images, reloaded.thumbnailUrls(ImageSize.FULL).take(3))
    }

}
