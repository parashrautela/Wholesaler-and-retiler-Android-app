package com.jewelindia.app.feature.employee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebShuffleTest {

    @Test
    fun testJsToInt32() {
        assertEquals(0, WebShuffle.jsToInt32(Double.NaN))
        assertEquals(0, WebShuffle.jsToInt32(Double.POSITIVE_INFINITY))
        assertEquals(0, WebShuffle.jsToInt32(Double.NEGATIVE_INFINITY))
        assertEquals(0, WebShuffle.jsToInt32(0.0))
        assertEquals(42, WebShuffle.jsToInt32(42.0))
        assertEquals(-42, WebShuffle.jsToInt32(-42.0))
        assertEquals(0, WebShuffle.jsToInt32(4294967296.0))
        assertEquals(1, WebShuffle.jsToInt32(4294967297.0))
        assertEquals(-1, WebShuffle.jsToInt32(-1.0))
    }

    @Test
    fun testImageIndex() {
        assertEquals(0, WebShuffle.imageIndex(""))
        assertEquals(1, WebShuffle.imageIndex("abc1"))
        assertEquals(2, WebShuffle.imageIndex("employee-id-6")) // 6 % 4 = 2
        assertEquals(3, WebShuffle.imageIndex("some-uuid-f")) // 15 % 4 = 3
        assertEquals(0, WebShuffle.imageIndex("some-uuid-c")) // 12 % 4 = 0
    }

    @Test
    fun testDeterministicShuffle() {
        val items = (1..20).toList()
        val seed1 = "b9f9e723-5e8a-4d2c-8cb3-7a9182b8f840"
        val shuffled1 = WebShuffle.shuffled(items, seed1, keep = 6)
        val shuffled2 = WebShuffle.shuffled(items, seed1, keep = 6)

        assertEquals(6, shuffled1.size)
        assertEquals(shuffled1, shuffled2)

        // Different seed produces different shuffle
        val seed2 = "00000000-0000-0000-0000-000000000001"
        val shuffled3 = WebShuffle.shuffled(items, seed2, keep = 6)
        assertEquals(6, shuffled3.size)
        assertTrue(shuffled1 != shuffled3)

        // All items returned exist in original list
        assertTrue(items.containsAll(shuffled1))
    }
}
