package com.jewelindia.app

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.JewelleryTypeCanonical
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueFilterTest {

    @Test
    fun testJewelleryTypeCanonicalAliases() {
        for (alias in listOf("chain", "chains", "Neck Chain", " neck chains ")) {
            assertEquals("chain", JewelleryTypeCanonical.canonicalize(alias))
        }
        assertTrue(JewelleryTypeCanonical.allCanonical.contains("chain"))
        assertEquals("Chains", JewelleryTypeCanonical.displayLabel("chain"))
        assertNull("Material must not match jewellery type", JewelleryTypeCanonical.canonicalize("gold"))
        assertNull("Material must not match jewellery type", JewelleryTypeCanonical.canonicalize("silver"))
        assertEquals("necklace", JewelleryTypeCanonical.canonicalize("necklace"))
        assertEquals("necklace", JewelleryTypeCanonical.canonicalize("necklaces"))
        assertEquals("ring", JewelleryTypeCanonical.canonicalize("rings"))
        assertEquals("earrings", JewelleryTypeCanonical.canonicalize("earring"))
        assertEquals("earrings", JewelleryTypeCanonical.canonicalize("jhumka"))
        assertEquals("bangle", JewelleryTypeCanonical.canonicalize("bangles"))
        assertEquals("pendant", JewelleryTypeCanonical.canonicalize("pendants"))
        assertEquals("nosepin", JewelleryTypeCanonical.canonicalize("nose pin"))
        assertEquals("haram", JewelleryTypeCanonical.canonicalize("harams"))
        assertEquals("mangalsutra", JewelleryTypeCanonical.canonicalize("mangalsutras"))
    }

    @Test
    fun testProductFilteringByQuery() {
        val products = listOf(
            Product(id = "1", title = "22K Gold Temple Haram", jewelleryType = "haram", category = "gold"),
            Product(id = "2", title = "Diamond Solitaire Ring", jewelleryType = "ring", category = "diamond"),
            Product(id = "3", title = "Classic Gold Neck Chain", jewelleryType = "chain", category = "gold"),
            Product(id = "4", title = "Silver Anklet", jewelleryType = "anklet", category = "silver")
        )

        // Query matches title
        val qTitle = "solitaire"
        val matchTitle = products.filter {
            it.title?.contains(qTitle, ignoreCase = true) == true ||
                    it.jewelleryType?.contains(qTitle, ignoreCase = true) == true ||
                    it.category?.contains(qTitle, ignoreCase = true) == true
        }
        assertEquals(1, matchTitle.size)
        assertEquals("2", matchTitle[0].id)

        // Query matches category
        val qCat = "gold"
        val matchCat = products.filter {
            it.title?.contains(qCat, ignoreCase = true) == true ||
                    it.jewelleryType?.contains(qCat, ignoreCase = true) == true ||
                    it.category?.contains(qCat, ignoreCase = true) == true
        }
        assertEquals(2, matchCat.size)

        // Query matches jewelleryType
        val qType = "chain"
        val matchType = products.filter {
            it.title?.contains(qType, ignoreCase = true) == true ||
                    it.jewelleryType?.contains(qType, ignoreCase = true) == true ||
                    it.category?.contains(qType, ignoreCase = true) == true
        }
        assertEquals(1, matchType.size)
        assertEquals("3", matchType[0].id)
    }

    @Test
    fun testProductFilteringByStockAndPurity() {
        val products = listOf(
            Product(id = "1", title = "P1", stockAvailable = true, metalPurity = "22K"),
            Product(id = "2", title = "P2", stockAvailable = false, metalPurity = "18K"),
            Product(id = "3", title = "P3", stockAvailable = true, metalPurity = "18K")
        )

        val inStockOnly = products.filter { it.stockAvailable == true }
        assertEquals(2, inStockOnly.size)

        val purity18k = products.filter { it.metalPurity?.equals("18K", ignoreCase = true) == true }
        assertEquals(2, purity18k.size)

        val inStock18k = products.filter { it.stockAvailable == true && it.metalPurity?.equals("18K", ignoreCase = true) == true }
        assertEquals(1, inStock18k.size)
        assertEquals("3", inStock18k[0].id)
    }
}
