package com.jewelindia.app.feature.wholesaler

import org.junit.Assert.assertEquals
import org.junit.Test

class RejectReasonsTest {

    @Test
    fun testExactListOfFiveRejectReasonsMatchingIos() {
        // Source of truth: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerOrdersView.swift lines 610-616
        val expectedReasons = listOf(
            "Design out of stock",
            "Can't meet the delivery time",
            "Customisation not possible",
            "Metal rate has changed",
            "Below minimum order"
        )

        assertEquals("Must match the exact 5 rejection reasons from iOS RejectOrderSheet", expectedReasons, RejectOrderDefaults.reasons)
        assertEquals(5, RejectOrderDefaults.reasons.size)
    }
}
