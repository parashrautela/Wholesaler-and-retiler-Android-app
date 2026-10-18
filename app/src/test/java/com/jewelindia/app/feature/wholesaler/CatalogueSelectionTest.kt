package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.feature.wholesaler.share.CatalogueSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueSelectionTest {

    private fun makeProduct(id: String, isPublished: Boolean = true, title: String = "Design $id"): Product {
        return Product(
            id = id,
            title = title,
            isPublished = isPublished
        )
    }

    @Test
    fun testToggleAddsAndRemovesProduct() {
        var selection = CatalogueSelection()
        val p1 = makeProduct("p1")

        // Add
        val (s1, err1) = selection.toggle(p1)
        assertNull(err1)
        assertEquals(1, s1.count)
        assertTrue(s1.contains("p1"))
        assertEquals(listOf(p1), s1.products)

        // Remove
        val (s2, err2) = s1.toggle(p1)
        assertNull(err2)
        assertEquals(0, s2.count)
        assertFalse(s2.contains("p1"))
        assertTrue(s2.isEmpty)
    }

    @Test
    fun testSelectionOrderPreserved() {
        var selection = CatalogueSelection()
        val p3 = makeProduct("p3")
        val p1 = makeProduct("p1")
        val p2 = makeProduct("p2")

        selection = selection.toggle(p3).first
        selection = selection.toggle(p1).first
        selection = selection.toggle(p2).first

        assertEquals(listOf("p3", "p1", "p2"), selection.ids)
        assertEquals(listOf(p3, p1, p2), selection.products)
    }

    @Test
    fun testUnpublishedProductRejectedWithExactCopy() {
        val selection = CatalogueSelection()
        val unpublished = makeProduct("p-draft", isPublished = false)

        val (next, error) = selection.toggle(unpublished)
        assertEquals(Copy.CatalogueShare.selectUnpublished, error)
        assertEquals("Publish this design to share it.", error)
        assertTrue(next.isEmpty)
        assertFalse(next.contains("p-draft"))
    }

    @Test
    fun testFiftyDesignLimitWithExactCopy() {
        var selection = CatalogueSelection()
        for (i in 1..50) {
            val (next, err) = selection.toggle(makeProduct("p-$i"))
            assertNull(err)
            selection = next
        }
        assertEquals(50, selection.count)

        // Attempting to select 51st
        val p51 = makeProduct("p-51")
        val (capped, error) = selection.toggle(p51)
        assertEquals(Copy.CatalogueShare.selectLimit, error)
        assertEquals("You can share up to 50 designs in one link.", error)
        assertEquals(50, capped.count)
        assertFalse(capped.contains("p-51"))
    }

    @Test
    fun testClearEmptiesSelection() {
        var selection = CatalogueSelection()
        selection = selection.toggle(makeProduct("p1")).first
        selection = selection.toggle(makeProduct("p2")).first
        assertEquals(2, selection.count)

        val cleared = selection.clear()
        assertEquals(0, cleared.count)
        assertTrue(cleared.isEmpty)
        assertTrue(cleared.ids.isEmpty())
        assertTrue(cleared.products.isEmpty())
    }

    @Test
    fun testViewModelSelectionIntegration() {
        val vm = WholesalerCatalogViewModel(wholesalerUserId = "wholesaler-1")
        assertFalse(vm.isSelecting.value)
        assertTrue(vm.selection.value.isEmpty)

        vm.setSelecting(true)
        assertTrue(vm.isSelecting.value)

        val p1 = makeProduct("p1")
        val err = vm.toggleProductSelection(p1)
        assertNull(err)
        assertEquals(1, vm.selection.value.count)
        assertTrue(vm.selection.value.contains("p1"))

        // Disabling select mode clears selection
        vm.setSelecting(false)
        assertFalse(vm.isSelecting.value)
        assertTrue(vm.selection.value.isEmpty)
    }
}
