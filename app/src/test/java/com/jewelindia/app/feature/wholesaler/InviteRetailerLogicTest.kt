package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.data.api.RetailerInvitation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InviteRetailerLogicTest {

    @Test
    fun testClampGiftNormalStep() {
        val next = InviteRetailerLogic.clampGift(
            current = 1000,
            delta = 500,
            minimum = 1000,
            maximum = 10000,
            available = 5000
        )
        assertEquals(1500, next)
    }

    @Test
    fun testClampGiftLowerBound() {
        val next = InviteRetailerLogic.clampGift(
            current = 1000,
            delta = -500,
            minimum = 1000,
            maximum = 10000,
            available = 5000
        )
        assertEquals(1000, next)
    }

    @Test
    fun testClampGiftUpperBoundByMaximum() {
        val next = InviteRetailerLogic.clampGift(
            current = 10000,
            delta = 500,
            minimum = 1000,
            maximum = 10000,
            available = 20000
        )
        assertEquals(10000, next)
    }

    @Test
    fun testClampGiftUpperBoundByAvailable() {
        val next = InviteRetailerLogic.clampGift(
            current = 2000,
            delta = 500,
            minimum = 1000,
            maximum = 10000,
            available = 2000
        )
        assertEquals(2000, next)
    }

    @Test
    fun testClampGiftWhenAvailableLessThanMinimum() {
        val next = InviteRetailerLogic.clampGift(
            current = 1000,
            delta = 500,
            minimum = 1000,
            maximum = 10000,
            available = 500
        )
        assertEquals(1000, next)
    }

    @Test
    fun testPickNewestUnclaimedFindsFirstUnclaimed() {
        val inv1 = RetailerInvitation(
            id = "1",
            code = "C1",
            link = "https://jewelindia.shop/join/C1",
            expiresAt = "2026-10-30",
            status = "accepted",
            giftCredits = 1000
        )
        val inv2 = RetailerInvitation(
            id = "2",
            code = "C2",
            link = "https://jewelindia.shop/join/C2",
            expiresAt = "2026-10-30",
            status = "unclaimed",
            giftCredits = 1500
        )
        val inv3 = RetailerInvitation(
            id = "3",
            code = "C3",
            link = "https://jewelindia.shop/join/C3",
            expiresAt = "2026-10-30",
            status = "unclaimed",
            giftCredits = 2000
        )

        val result = InviteRetailerLogic.pickNewestUnclaimed(listOf(inv1, inv2, inv3))
        assertEquals("2", result?.id)
        assertEquals(1500, result?.giftCredits)
    }

    @Test
    fun testPickNewestUnclaimedReturnsNullWhenNoneUnclaimed() {
        val inv1 = RetailerInvitation(
            id = "1",
            code = "C1",
            link = "https://jewelindia.shop/join/C1",
            expiresAt = "2026-10-30",
            status = "accepted"
        )
        val inv2 = RetailerInvitation(
            id = "2",
            code = "C2",
            link = "https://jewelindia.shop/join/C2",
            expiresAt = "2026-10-30",
            status = "cancelled"
        )

        assertNull(InviteRetailerLogic.pickNewestUnclaimed(listOf(inv1, inv2)))
        assertNull(InviteRetailerLogic.pickNewestUnclaimed(emptyList()))
        assertNull(InviteRetailerLogic.pickNewestUnclaimed(null))
    }
}
