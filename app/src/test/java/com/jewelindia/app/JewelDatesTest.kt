package com.jewelindia.app

import com.jewelindia.app.core.time.ChatTime
import com.jewelindia.app.core.time.JewelDates
import com.jewelindia.app.core.time.OrderTime
import com.jewelindia.app.core.time.TreasureChestFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class JewelDatesTest {

    @Test
    fun testParseIsoDates() {
        // Plain UTC
        val d1 = JewelDates.parse("2026-10-17T09:30:00Z")
        assertNotNull(d1)

        // With fractional seconds
        val d2 = JewelDates.parse("2026-10-17T09:30:00.123456Z")
        assertNotNull(d2)

        // With offset
        val d3 = JewelDates.parse("2026-10-17T15:00:00+05:30")
        assertNotNull(d3)
        assertEquals(d1, d3)

        // Invalid
        assertNull(JewelDates.parse(null))
        assertNull(JewelDates.parse(""))
        assertNull(JewelDates.parse("invalid-date"))
    }

    @Test
    fun testChatTimeAndOrderTime() {
        val now = Instant.now().toString()
        val shortTime = ChatTime.short(now)
        assertNotNull(shortTime)
        // Today should format as time, e.g., "3:30 PM" (or "3:30 pm")
        assertTrue(shortTime!!.uppercase().contains("AM") || shortTime.uppercase().contains("PM"))

        // Past date
        val pastDate = "2025-01-15T10:00:00Z"
        val pastShort = ChatTime.short(pastDate)
        assertEquals("15 Jan", pastShort)

        val orderFull = OrderTime.full(pastDate)
        assertNotNull(orderFull)
        assertTrue(orderFull!!.contains("15 Jan 2025"))
    }

    @Test
    fun testTreasureChestFormat() {
        val instant = Instant.parse("2026-10-17T10:00:00Z")
        val formatted = TreasureChestFormat.formatResetsAt(instant)
        // 10:00 UTC = 15:30 IST
        assertTrue(formatted.contains("IST"))
        assertTrue(formatted.contains("17 Oct"))
    }
}
