package com.jewelindia.app.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenSecurityTest {

    @Test
    fun testScreenSecurityReferenceCounting() {
        val stateChanges = mutableListOf<Boolean>()
        val security = ScreenSecurity { isSecure ->
            stateChanges.add(isSecure)
        }

        // Initial state
        assertFalse(security.isProtected)
        assertEquals(0, stateChanges.size)

        // acquire x 3
        security.acquire()
        assertTrue(security.isProtected)
        assertEquals(listOf(true), stateChanges)

        security.acquire()
        assertTrue(security.isProtected)
        assertEquals(listOf(true), stateChanges)

        security.acquire()
        assertTrue(security.isProtected)
        assertEquals(listOf(true), stateChanges)

        // release x 2 -> still secure
        security.release()
        assertTrue(security.isProtected)
        assertEquals(listOf(true), stateChanges)

        security.release()
        assertTrue(security.isProtected)
        assertEquals(listOf(true), stateChanges)

        // release x 1 -> not secure
        security.release()
        assertFalse(security.isProtected)
        assertEquals(listOf(true, false), stateChanges)

        // extra release -> stays 0, no crash, no extra callback
        security.release()
        assertFalse(security.isProtected)
        assertEquals(listOf(true, false), stateChanges)
    }
}
