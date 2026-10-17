package com.jewelindia.app.feature.onboarding

import com.jewelindia.app.core.images.PickedFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardFlowTest {

    private lateinit var flow: OnboardFlow

    @Before
    fun setUp() {
        flow = OnboardFlow()
    }

    @Test
    fun testNameFiltering() {
        assertEquals("Rajesh Sharma", OnboardFlow.filterName("Rajesh Sharma"))
        assertEquals("Rajesh Sharma", OnboardFlow.filterName("Rajesh123 Sharma!"))
        assertEquals("Amit", OnboardFlow.filterName("Amit#$@!"))
    }

    @Test
    fun testAadharFormatting() {
        assertEquals("1234", OnboardFlow.formatAadhar("1234"))
        assertEquals("1234 5678", OnboardFlow.formatAadhar("12345678"))
        assertEquals("1234 5678 9012", OnboardFlow.formatAadhar("123456789012"))
        // Max 12 digits
        assertEquals("1234 5678 9012", OnboardFlow.formatAadhar("12345678901234567"))
        // Non-digits stripped
        assertEquals("1234 5678 9012", OnboardFlow.formatAadhar("1234-5678-9012"))
    }

    @Test
    fun testWholesalerStatesCount() {
        assertEquals(28, OnboardFlow.states.size)
        assertEquals("Andhra Pradesh", OnboardFlow.states.first())
        assertEquals("West Bengal", OnboardFlow.states.last())
        assertFalse(OnboardFlow.states.contains("Delhi"))
    }

    @Test
    fun testStep1Validation() {
        flow.name = ""
        flow.aadhar = ""
        flow.submitAttempted = false
        assertNull(flow.nameError)
        assertNull(flow.aadharError)
        assertFalse(flow.step1Valid)

        flow.submitAttempted = true
        assertNotNull(flow.nameError)
        assertNotNull(flow.aadharError)
        assertNotNull(flow.frontError)
        assertNotNull(flow.backError)

        flow.name = "Pawan Kumar"
        flow.aadhar = "1234 5678 9012"
        flow.frontImage = PickedFile(byteArrayOf(1), "front.jpg", "image/jpeg")
        flow.backImage = PickedFile(byteArrayOf(2), "back.jpg", "image/jpeg")

        assertNull(flow.nameError)
        assertNull(flow.aadharError)
        assertNull(flow.frontError)
        assertNull(flow.backError)
        assertTrue(flow.step1Valid)
    }

    @Test
    fun testStep2Validation() {
        flow.businessName = "A"
        flow.selectedState = ""
        flow.selectedCity = ""
        flow.submitAttempted = true

        assertNotNull(flow.businessNameError)
        assertNotNull(flow.stateError)
        assertNotNull(flow.cityError)
        assertNotNull(flow.logoError)
        assertFalse(flow.step2Valid)

        flow.businessName = "Jewel India Exports"
        flow.selectedState = "Maharashtra"
        flow.selectedCity = "Mumbai"
        flow.logoImage = PickedFile(byteArrayOf(3), "logo.png", "image/png")

        assertNull(flow.businessNameError)
        assertNull(flow.stateError)
        assertNull(flow.cityError)
        assertNull(flow.logoError)
        assertTrue(flow.step2Valid)
    }

    @Test
    fun testStep3Validation() {
        flow.submitAttempted = true
        assertNotNull(flow.panError)
        assertNotNull(flow.gstError)
        assertFalse(flow.step3Valid)

        flow.panFile = PickedFile(byteArrayOf(4), "pan.pdf", "application/pdf")
        flow.gstFile = PickedFile(byteArrayOf(5), "gst.pdf", "application/pdf")

        assertNull(flow.panError)
        assertNull(flow.gstError)
        assertTrue(flow.step3Valid)
    }
}
