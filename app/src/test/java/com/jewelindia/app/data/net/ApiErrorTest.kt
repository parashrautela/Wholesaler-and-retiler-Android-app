// Port of: wholesaler ios/JewelIndia/Sources/Networking/InviteAPI.swift (DBRefusal Tests)
package com.jewelindia.app.data.net

import com.jewelindia.app.core.text.Copy
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorTest {

    @Test
    fun testDbRefusalMapping() {
        val err1 = Exception("new row violates check constraint: INVITE_CODE_REQUIRED")
        assertEquals(Copy.inviteCodeEmpty, DbRefusal.message(err1))

        val err2 = Exception("ERROR: INVITE_CODE_USED (SQLSTATE P0001)")
        assertEquals(Copy.inviteCodeReason("used"), DbRefusal.message(err2))

        val err3 = Exception("ROLE_ALREADY_SET")
        assertEquals(Copy.roleAlreadySet, DbRefusal.message(err3))

        val err4 = Exception("NOT_INVITED")
        assertEquals(Copy.staffNotInvited, DbRefusal.message(err4))

        val err5 = Exception("ACCOUNT_HAS_ANOTHER_ROLE")
        assertEquals(Copy.staffAccountHasAnotherRole, DbRefusal.message(err5))
    }

    @Test
    fun testParseApiError() {
        val jsonError = """{"code":"404","message":"Item not found","details":"Detailed description"}"""
        val parsed = parseApiError(404, jsonError)
        assertEquals(404, parsed.status)
        assertEquals("Item not found", parsed.message)
        assertEquals("Item not found", parsed.userMessage())
    }
}
