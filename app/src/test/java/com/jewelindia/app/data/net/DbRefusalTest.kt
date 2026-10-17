package com.jewelindia.app.data.net

import com.jewelindia.app.core.text.Copy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DbRefusalTest {

    @Test
    fun testDbRefusalCodeExtraction() {
        val ex1 = Exception("Database error: RETAILER_ALREADY_VERIFIED happened")
        assertEquals("RETAILER_ALREADY_VERIFIED", DbRefusal.code(ex1))

        val ex2 = Exception("PGRST: INVITE_CODE_EXPIRED on invite record")
        assertEquals("INVITE_CODE_EXPIRED", DbRefusal.code(ex2))

        val unknown = Exception("Some completely random db glitch")
        assertNull(DbRefusal.code(unknown))
    }

    @Test
    fun testDbRefusalUserFacingMessages() {
        assertEquals(
            Copy.inviteCodeEmpty,
            DbRefusal.message(Exception("INVITE_CODE_REQUIRED"))
        )
        assertEquals(
            Copy.inviteCodeReason("not_found"),
            DbRefusal.message(Exception("INVITE_CODE_NOT_FOUND"))
        )
        assertEquals(
            Copy.inviteCodeReason("expired"),
            DbRefusal.message(Exception("INVITE_CODE_EXPIRED"))
        )
        assertEquals(
            Copy.inviteCodeReason("used"),
            DbRefusal.message(Exception("INVITE_CODE_USED"))
        )
        assertEquals(
            Copy.inviteCodeReason("inactive"),
            DbRefusal.message(Exception("INVITE_CODE_INACTIVE"))
        )
        assertEquals(
            "Your store is already linked to the wholesaler who invited you.",
            DbRefusal.message(Exception("RETAILER_ALREADY_ATTRIBUTED"))
        )
        assertEquals(
            "Your store is already verified — no code needed.",
            DbRefusal.message(Exception("RETAILER_ALREADY_VERIFIED"))
        )
        assertEquals(
            Copy.roleAlreadySet,
            DbRefusal.message(Exception("ROLE_ALREADY_SET"))
        )
        assertEquals(
            Copy.roleNotAllowed,
            DbRefusal.message(Exception("ROLE_NOT_ALLOWED"))
        )
        assertEquals(
            "Please sign in again.",
            DbRefusal.message(Exception("NOT_SIGNED_IN"))
        )
        assertEquals(
            Copy.staffNotInvited,
            DbRefusal.message(Exception("NOT_INVITED"))
        )
        assertEquals(
            Copy.staffGoogleRequired,
            DbRefusal.message(Exception("GOOGLE_SIGN_IN_REQUIRED"))
        )
        assertEquals(
            Copy.staffAccountHasAnotherRole,
            DbRefusal.message(Exception("ACCOUNT_HAS_ANOTHER_ROLE"))
        )
    }
}
