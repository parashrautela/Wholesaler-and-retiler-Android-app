package com.jewelindia.app.core.session

import android.net.Uri
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignupFlowTest {

    @Test
    fun testPersistenceAndProperties() {
        val prefs = FakeSharedPreferences()
        val flow = SignupFlow(prefs)

        flow.identity = "+919876543210"
        flow.referralCode = "REF123"
        flow.chosenRole = UserRole.wholesaler
        flow.invitedBy = "Royal Jewels"
        flow.remainingResends = 3

        assertEquals("+919876543210", flow.identity)
        assertEquals("REF123", flow.referralCode)
        assertEquals(UserRole.wholesaler, flow.signupRole)
        assertEquals("Royal Jewels", flow.invitedBy)
        assertEquals(3, flow.remainingResends)

        // Relaunch simulation with same preferences
        val flow2 = SignupFlow(prefs)
        assertEquals("+919876543210", flow2.identity)
        assertEquals("REF123", flow2.referralCode)
        assertEquals(UserRole.wholesaler, flow2.signupRole)
        assertEquals("Royal Jewels", flow2.invitedBy)
        assertEquals(3, flow2.remainingResends)
    }

    @Test
    fun testStaleLockoutDroppedOnInit() {
        val prefs = FakeSharedPreferences()
        // Put an expired lockout in the past
        val past = System.currentTimeMillis() - 100_000
        prefs.edit().putLong("otp_locked_until", past).apply()

        val flow = SignupFlow(prefs)
        assertFalse(flow.isLocked)
        assertNull(flow.lockedUntil)
    }

    @Test
    fun testActiveLockoutHonored() {
        val prefs = FakeSharedPreferences()
        // Put a future lockout
        val future = System.currentTimeMillis() + 100_000
        prefs.edit().putLong("otp_locked_until", future).apply()

        val flow = SignupFlow(prefs)
        assertTrue(flow.isLocked)
        assertEquals(future, flow.lockedUntil)
    }

    @Test
    fun testOtpSecondsLeft() {
        val prefs = FakeSharedPreferences()
        val flow = SignupFlow(prefs)

        // No sent time
        assertEquals(0, flow.otpSecondsLeft)

        // Sent 10 seconds ago
        flow.otpSentAt = System.currentTimeMillis() - 10_000
        assertEquals(50, flow.otpSecondsLeft)

        // Sent 65 seconds ago (expired)
        flow.otpSentAt = System.currentTimeMillis() - 65_000
        assertEquals(0, flow.otpSecondsLeft)
    }

    @Test
    fun testCaptureRetailerInvitation() {
        val prefs = FakeSharedPreferences()
        val flow = SignupFlow(prefs)

        val siteHost = java.net.URI.create(AppConfig.siteUrl).host ?: "app.jewelindia.shop"

        // Valid link
        val validUrl = "https://$siteHost/join/PJ-a8k3x2"
        assertTrue(flow.captureRetailerInvitation(validUrl))
        assertEquals("PJ-a8k3x2", flow.referralCode)
        assertEquals(UserRole.retailer, flow.referralRole)

        // Non-https
        val httpUrl = "http://$siteHost/join/PJ-a8k3x2"
        assertFalse(flow.captureRetailerInvitation(httpUrl))

        // Different host
        val badHostUrl = "https://evil.example.com/join/PJ-a8k3x2"
        assertFalse(flow.captureRetailerInvitation(badHostUrl))

        // Extra segments
        val extraSegmentsUrl = "https://$siteHost/join/PJ-a8k3x2/extra"
        assertFalse(flow.captureRetailerInvitation(extraSegmentsUrl))

        // Wrong root path
        val wrongPathUrl = "https://$siteHost/other/PJ-a8k3x2"
        assertFalse(flow.captureRetailerInvitation(wrongPathUrl))
    }
}
