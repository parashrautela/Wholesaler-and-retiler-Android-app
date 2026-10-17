package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.ChamakMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChamakPickerValidationTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleProduct(
        id: String,
        title: String = "Test Product",
        imageURL: String? = "https://example.com/img.jpg",
        jewelleryType: String? = "Necklace",
        category: String? = "Gold"
    ) = Product(
        id = id,
        wholesalerId = "w1",
        title = title,
        jewelleryType = jewelleryType,
        category = category,
        imageURL = imageURL
    )

    @Test
    fun testFusionModeNeverBlocks() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.Fusion
        val p = sampleProduct(id = "p1", imageURL = null, jewelleryType = null)
        assertNull(vm.selectionBlockReason(p))
    }

    @Test
    fun testMissingImageBlockReason() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation
        val p = sampleProduct(id = "p-no-img", imageURL = "")
        val reason = vm.selectionBlockReason(p)
        assertEquals(ChamakSelectionBlockReason.MissingImage, reason)
        assertEquals("This product has no usable image.", reason?.message)
    }

    @Test
    fun testMissingTypeBlockReason() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation
        // Raw category is "gold" and jewelleryType is empty / uncanonicalized -> should block
        val p = sampleProduct(id = "p-no-type", jewelleryType = "", category = "gold")
        val reason = vm.selectionBlockReason(p)
        assertEquals(ChamakSelectionBlockReason.MissingType, reason)
        assertEquals("Add a jewellery type to use this item in a set.", reason?.message)
    }

    @Test
    fun testSameTypeSelectedBlockReason() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation

        val necklace1 = sampleProduct(id = "n1", jewelleryType = "Necklaces")
        vm.selectProduct(necklace1)

        val necklace2 = sampleProduct(id = "n2", jewelleryType = "Necklace")
        val reason = vm.selectionBlockReason(necklace2)

        assertTrue(reason is ChamakSelectionBlockReason.SameTypeSelected)
        val sameReason = reason as ChamakSelectionBlockReason.SameTypeSelected
        assertEquals("Necklace", sameReason.label)
        assertEquals("A necklace is already selected. Remove it to choose another.", sameReason.message)
    }

    @Test
    fun testSetFullBlockReason() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation

        vm.selectProduct(sampleProduct(id = "p1", jewelleryType = "Necklace"))
        vm.selectProduct(sampleProduct(id = "p2", jewelleryType = "Earrings"))
        vm.selectProduct(sampleProduct(id = "p3", jewelleryType = "Ring"))
        vm.selectProduct(sampleProduct(id = "p4", jewelleryType = "Bangle"))

        assertEquals(4, vm.setPieces.size)

        val fifth = sampleProduct(id = "p5", jewelleryType = "Pendant")
        val reason = vm.selectionBlockReason(fifth)
        assertEquals(ChamakSelectionBlockReason.SetFull, reason)
        assertEquals("Remove a piece to add another.", reason?.message)
    }

    @Test
    fun testAlreadySelectedCanBeDeselected() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation

        val product = sampleProduct(id = "p1", jewelleryType = "Ring")
        vm.selectProduct(product)
        assertEquals(1, vm.setPieces.size)

        // Reason for already selected product is null (can deselect)
        assertNull(vm.selectionBlockReason(product))

        // Deselect
        vm.selectProduct(product)
        assertEquals(0, vm.setPieces.size)
    }
}
