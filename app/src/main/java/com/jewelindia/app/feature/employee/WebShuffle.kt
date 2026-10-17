// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/Data/WebShuffle.swift
package com.jewelindia.app.feature.employee

import kotlin.math.ceil
import kotlin.math.floor

/**
 * The web home's "deterministic shuffle" (`app/dashboard/employee/page.jsx`),
 * reproduced bit for bit so an employee sees the same six designs, in the
 * same order, on the phone as in the browser.
 *
 * The obvious Kotlin port — wrapping Int32 arithmetic — gets a different
 * answer for almost every id. JavaScript evaluates
 * `1103515245 * seedVal` as a Double, which passes 2^53 whenever the seed
 * is above ~8 million, so its low bits are rounded away *before*
 * `& 0x7fffffff` converts back to an integer. This does the same.
 */
object WebShuffle {

    /**
     * ECMA-262 ToInt32 applied to a Double.
     */
    fun jsToInt32(value: Double): Int {
        if (!value.isFinite()) return 0
        val two32 = 4294967296.0
        val towardZero = if (value < 0.0) ceil(value) else floor(value)
        var wrapped = towardZero % two32
        if (wrapped < 0.0) wrapped += two32
        val u = wrapped.toLong() and 0xFFFFFFFFL
        return u.toInt()
    }

    /**
     * `seedId` must be the id exactly as the database returns it — a
     * lowercase `employees.id`. `UUID.toString()` uppercase would give a
     * different order.
     */
    fun <T> shuffled(items: List<T>, seedId: String, keep: Int = 6): List<T> {
        var seed = 0
        for (ch in seedId) {
            val folded = seed.toDouble() * 31.0
            seed = jsToInt32(folded + ch.code.toDouble())
        }

        val result = items.toMutableList()
        var i = result.size - 1
        while (i > 0) {
            // Two statements on purpose: a fused multiply-add would not
            // round the way JavaScript does.
            val product = 1103515245.0 * seed.toDouble()
            val sum = product + 12345.0
            seed = jsToInt32(sum) and 0x7FFFFFFF
            val j = (seed.toLong() % (i + 1).toLong()).toInt()
            val temp = result[i]
            result[i] = result[j]
            result[j] = temp
            i--
        }
        return result.take(keep)
    }

    /**
     * Which of the four tall collection images this person gets: the last
     * hex digit of their id, mod 4 (`parseInt(lastChar, 16) || 0`).
     */
    fun imageIndex(seedId: String, count: Int = 4): Int {
        val last = seedId.lastOrNull() ?: return 0
        val digit = last.toString().toIntOrNull(16) ?: return 0
        return digit % count
    }
}
