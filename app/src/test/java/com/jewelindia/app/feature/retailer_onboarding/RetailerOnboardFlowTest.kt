package com.jewelindia.app.feature.retailer_onboarding

import com.jewelindia.app.core.images.PickedFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetailerOnboardFlowTest {

    private lateinit var flow: RetailerOnboardFlow

    @Before
    fun setUp() {
        flow = RetailerOnboardFlow()
    }

    @Test
    fun testRetailerStatesCountAndDelhi() {
        assertEquals(29, RetailerOnboardFlow.states.size)
        assertEquals("Andhra Pradesh", RetailerOnboardFlow.states.first())
        assertEquals("Delhi", RetailerOnboardFlow.states.last())
        assertTrue(RetailerOnboardFlow.states.contains("Delhi"))
    }

    @Test
    fun testStoreNameValidation() {
        flow.storeName = "J"
        flow.submitAttempted = true
        assertNotNull(flow.storeNameError)

        flow.storeName = "Jewels Palace"
        assertNull(flow.storeNameError)
    }

    @Test
    fun testRetailerStep2Validation() {
        flow.storeName = "Kalyan Retail"
        flow.selectedState = "Delhi"
        flow.selectedCity = "New Delhi"
        flow.logoImage = PickedFile(byteArrayOf(1, 2), "store_logo.jpg", "image/jpeg")

        assertTrue(flow.step2Valid)
    }
}
