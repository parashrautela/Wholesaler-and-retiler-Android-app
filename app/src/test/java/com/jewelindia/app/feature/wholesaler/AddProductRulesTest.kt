package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.feature.wholesaler.chamak.catalogueType
import com.jewelindia.app.feature.wholesaler.chamak.isDeviceUpload
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Only the type and net weight are required; the rest is optional but still checked. */
class AddProductRulesTest {

    private fun form(edit: AddProductFormState.() -> Unit = {}) = AddProductFormState().apply {
        jewelleryType = "necklace"
        netWeight = "12.5"
        edit()
    }

    private fun assertRules(valid: Boolean, errors: Set<String> = emptySet(), edit: AddProductFormState.() -> Unit = {}) {
        val state = form(edit)
        assertEquals(valid, state.validate(requiresImage = false))
        assertEquals(errors, state.errors.keys.toSet())
    }

    @Test fun onlyTypeAndNetWeightAreNeeded() = assertRules(true)

    @Test fun everythingFilledIn() = assertRules(true) {
        title = "Haar"; category = "gold"; style = "bridal"; size = "m"; metalPurity = "22k"
        grossWeight = "14"; stoneWeight = "1.5"
    }

    @Test fun typeIsRequired() = assertRules(false, setOf("jewellery_type")) { jewelleryType = "" }
    @Test fun netWeightIsRequired() = assertRules(false, setOf("netWeight")) { netWeight = "" }
    @Test fun spacesAreNotAWeight() = assertRules(false, setOf("netWeight")) { netWeight = "   " }
    @Test fun zeroNetWeightIsRefused() = assertRules(false, setOf("netWeight")) { netWeight = "0" }
    @Test fun negativeNetWeightIsRefused() = assertRules(false, setOf("netWeight")) { netWeight = "-3" }
    @Test fun textNetWeightIsRefused() = assertRules(false, setOf("netWeight")) { netWeight = "abc" }
    @Test fun commaDecimalIsAccepted() = assertRules(true) { netWeight = "12,5" }
    @Test fun blankGrossIsFine() = assertRules(true) { grossWeight = "  " }
    @Test fun wrongGrossIsStillChecked() = assertRules(false, setOf("grossWeight")) { grossWeight = "abc" }
    @Test fun zeroGrossIsRefused() = assertRules(false, setOf("grossWeight")) { grossWeight = "0" }
    @Test fun zeroStoneIsFine() = assertRules(true) { stoneWeight = "0" }
    @Test fun negativeStoneIsRefused() = assertRules(false, setOf("stoneWeight")) { stoneWeight = "-1" }
    @Test fun outOfStockWithoutDaysIsFine() = assertRules(true) { stockAvailable = false }
    @Test fun outOfStockZeroDaysIsRefused() = assertRules(false, setOf("makeToOrderDays")) { stockAvailable = false; makeToOrderDays = "0" }
    @Test fun inStockIgnoresDays() = assertRules(true) { makeToOrderDays = "abc" }

    @Test fun photoIsRequiredWhenSubmitting() {
        val state = form()
        assertFalse(state.validate(requiresImage = true))
        assertEquals(setOf("image"), state.errors.keys.toSet())
    }

    @Test fun blankOptionalFieldsAreStoredEmptyNotBlankText() {
        val payload = productPayload(
            wholesalerId = "w", title = "Necklace", jewelleryType = "necklace",
            category = "", style = "  ", size = "", metalPurity = "",
            stockAvailable = true, grossWeight = "", stoneWeight = "", netWeight = "12,5",
            makeToOrderDays = ""
        )
        for (key in listOf("category", "style", "size", "metal_purity", "gross_weight", "stone_weight", "make_to_order_days")) {
            assertEquals(key, JsonNull, payload[key])
        }
        assertEquals(12.5, payload["net_weight"]!!.jsonPrimitive.double, 0.0)
    }

    @Test fun filledOptionalFieldsAreKept() {
        val payload = productPayload(
            wholesalerId = "w", title = "Haar", jewelleryType = "haram",
            category = "gold", style = "bridal", size = "m", metalPurity = "22k",
            stockAvailable = true, grossWeight = "14", stoneWeight = "1.5", netWeight = "12.5",
            makeToOrderDays = ""
        )
        assertEquals("gold", payload["category"]!!.jsonPrimitive.content)
        assertEquals("22k", payload["metal_purity"]!!.jsonPrimitive.content)
        assertEquals(14.0, payload["gross_weight"]!!.jsonPrimitive.double, 0.0)
    }

    @Test fun commaWeightsAreParsed() {
        assertEquals(12.5, weightNumber(" 12,5 ")!!, 0.0)
        assertNull(weightNumber("abc"))
        assertNull(weightNumber(null))
    }

    @Test fun deviceUploadsAreToldApartFromCataloguePhotos() {
        val base = "https://x.supabase.co/storage/v1/object/public/plant-images/"
        assertTrue(isDeviceUpload(base + "raw/abc/chamak_1_1234.jpg"))
        assertTrue(isDeviceUpload(base + "raw/abc/setcreation_3_1234.jpg"))
        assertFalse(isDeviceUpload(base + "raw/abc/product-draft_1234.jpg"))
        assertFalse(isDeviceUpload(base + "products/processed/abc_v1.png"))
        assertFalse(isDeviceUpload(base + "raw/abc/my_chamak_photo.jpg"))
        assertFalse(isDeviceUpload(""))
    }

    @Test fun analysisTypesMapToFormTypes() {
        val mapped = mapOf(
            "necklace" to "necklace", "choker" to "necklace", "long_necklace" to "haram",
            "pendant" to "pendant", "mangalsutra" to "mangalsutra", "earrings" to "earrings",
            "jhumka" to "earrings", "ring" to "rings", "bangle" to "bangles", "kada" to "bangles",
            "nose_ring" to "nosepins"
        )
        mapped.forEach { (analysis, form) -> assertEquals(analysis, form, catalogueType(analysis)) }
        for (unknown in listOf("bracelet", "maang_tikka", "anklet", "other", "", null)) {
            assertEquals("", catalogueType(unknown))
        }
        val formTypes = WholesalerProductOptions.types.map { it.value }.toSet()
        mapped.values.forEach { assertTrue(it, it in formTypes) }
    }
}
