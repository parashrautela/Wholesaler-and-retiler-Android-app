package com.jewelindia.app

import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.data.api.DailyCreditSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCreditScheduleTest {

    private fun baseWallet(
        ok: Boolean = true,
        mode: String? = "daily",
        dailyAllowance: Int? = 2000,
        nextAllowance: Int? = null,
        serverNow: String? = "2026-10-06T18:00:00+00:00",
        resetsAt: String? = "2026-10-07T00:00:00+05:30",
        isPaused: Boolean = false,
        policyType: String? = null,
        paidAvailable: Int? = null,
        giftAvailable: Int? = null,
        error: String? = null
    ): CreditWallet {
        return CreditWallet(
            ok = ok,
            errorCode = error,
            mode = mode,
            dailyAllowance = dailyAllowance,
            nextAllowance = nextAllowance,
            serverNow = serverNow,
            resetsAt = resetsAt,
            isPaused = isPaused,
            policyType = policyType,
            paidAvailable = paidAvailable,
            giftAvailable = giftAvailable
        )
    }

    @Test
    fun testStandardCountdownAndISTReset() {
        val schedule = DailyCreditSchedule(baseWallet())
        assertNotNull(schedule)
        val s = schedule!!
        assertEquals(2000, s.allowance)
        assertEquals("00:30:00", s.countdown(advancedBySeconds = 0.0))
        assertEquals("00:29:59", s.countdown(advancedBySeconds = 1.0))
        assertEquals("00:00:00", s.countdown(advancedBySeconds = 1800.0))
        assertEquals(0, s.remainingSeconds(advancedBySeconds = 2000.0))
        assertEquals("00:30:00", s.countdown(advancedBySeconds = -10.0))

        assertTrue(s.resetDescription.contains("7 Oct"))
        assertTrue(s.resetDescription.contains("12:00"))
        assertTrue(s.resetDescription.endsWith("IST"))
    }

    @Test
    fun testFractionalSecondsSupabaseTimestamps() {
        val fractionalWallet = baseWallet(
            serverNow = "2026-10-06T18:29:58.750000+00:00",
            resetsAt = "2026-10-06T18:30:00.000000+00:00"
        )
        val schedule = DailyCreditSchedule(fractionalWallet)
        assertNotNull(schedule)
        val s = schedule!!
        // 1.25 seconds rounds up to 2 seconds
        assertEquals(2, s.remainingSeconds(advancedBySeconds = 0.0))
        // After 1.25 seconds (1250ms), deadline has passed
        assertEquals(0, s.remainingSeconds(advancedBySeconds = 1.25))
    }

    @Test
    fun testFullDayCountdown() {
        val fullDayWallet = baseWallet(
            serverNow = "2026-10-06T18:30:00Z",
            resetsAt = "2026-10-07T18:30:00Z"
        )
        val schedule = DailyCreditSchedule(fullDayWallet)
        assertNotNull(schedule)
        assertEquals("24:00:00", schedule!!.countdown(advancedBySeconds = 0.0))
    }

    @Test
    fun testInvalidAndInactiveWalletsRejected() {
        // ok = false
        assertNull(DailyCreditSchedule(baseWallet(ok = false, error = "NOT_VERIFIED")))
        // mode = legacy
        assertNull(DailyCreditSchedule(baseWallet(mode = "legacy")))
        // allowance = 0
        assertNull(DailyCreditSchedule(baseWallet(dailyAllowance = 0)))
        // missing serverNow
        assertNull(DailyCreditSchedule(baseWallet(serverNow = null)))
        // missing resetsAt
        assertNull(DailyCreditSchedule(baseWallet(resetsAt = null)))
        // invalid date format
        assertNull(DailyCreditSchedule(baseWallet(resetsAt = "invalid")))
        // resets_at in the past
        assertNull(DailyCreditSchedule(baseWallet(resetsAt = "2026-10-05T18:30:00Z")))
        // resets_at > 24 hours into the future
        assertNull(DailyCreditSchedule(baseWallet(resetsAt = "2026-10-09T18:30:00Z")))
    }

    @Test
    fun testPreservesErrorCodeAndAllowanceChanges() {
        val rejected = baseWallet(ok = false, error = "NOT_VERIFIED")
        assertEquals("NOT_VERIFIED", rejected.errorCode)

        val customAllowance = DailyCreditSchedule(baseWallet(dailyAllowance = 3000))
        assertNotNull(customAllowance)
        assertEquals(3000, customAllowance!!.allowance)
    }

    @Test
    fun testRollingAllowanceAndPausedState() {
        val rollingWallet = baseWallet(
            policyType = "rolling_24h",
            dailyAllowance = 5000,
            nextAllowance = 1000,
            serverNow = "2026-10-09T15:00:00Z",
            resetsAt = "2026-10-10T15:00:00Z",
            paidAvailable = 500,
            giftAvailable = 300
        )
        val rolling = DailyCreditSchedule(rollingWallet)
        assertNotNull(rolling)
        val r = rolling!!
        assertEquals(1000, r.allowance)
        assertEquals("24:00:00", r.countdown(advancedBySeconds = 0.0))
        assertEquals("rolling_24h", rollingWallet.policyType)
        assertEquals(500, rollingWallet.paidAvailable)

        val pausedWallet = baseWallet(isPaused = true, dailyAllowance = 0, resetsAt = null)
        assertTrue(pausedWallet.isPaused)
        assertNull(DailyCreditSchedule(pausedWallet))

        val scheduledPause = DailyCreditSchedule(baseWallet(nextAllowance = 0))
        assertNotNull(scheduledPause)
        val sp = scheduledPause!!
        assertEquals(0, sp.allowance)
        assertEquals(1800, sp.remainingSeconds(advancedBySeconds = 0.0))
    }
}
