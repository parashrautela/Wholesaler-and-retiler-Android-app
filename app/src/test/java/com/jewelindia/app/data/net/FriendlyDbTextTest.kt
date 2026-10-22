package com.jewelindia.app.data.net

import com.jewelindia.app.core.text.Copy
import org.junit.Assert.assertEquals
import org.junit.Test

class FriendlyDbTextTest {

    @Test
    fun testPt403LockMapsToFeatureLocked() {
        assertEquals(Copy.featureLocked, friendlyDbText("Some generic error message", "PT403"))
        assertEquals(Copy.featureLocked, friendlyDbText("locked by Jewel India admin: custom_inquiries", null))
        assertEquals(Copy.featureLocked, friendlyDbText("Error: operation locked by Jewel India admin", "PT403"))
    }

    @Test
    fun testMultiLineTextKeepsOnlyTheFirstLine() {
        val multiLine = """
            Database update failed
            Code: 42501
            Hint: Check RLS policies
            URL: https://example.supabase.co/rest/v1/products
        """.trimIndent()
        assertEquals("Database update failed", friendlyDbText(multiLine, null))
    }

    @Test
    fun testKnownDbRefusalCodesStillMap() {
        assertEquals(Copy.roleAlreadySet, friendlyDbText("ROLE_ALREADY_SET", null))
        assertEquals(Copy.roleAlreadySet, friendlyDbText("Error: ROLE_ALREADY_SET occurred", null))
        assertEquals(Copy.inviteCodeEmpty, friendlyDbText("", "INVITE_CODE_REQUIRED"))
        assertEquals(Copy.staffAccountHasAnotherRole, friendlyDbText("ACCOUNT_HAS_ANOTHER_ROLE", null))
    }

    @Test
    fun testBlankTextMapsToGenericFailure() {
        assertEquals(Copy.genericFailure, friendlyDbText("", null))
        assertEquals(Copy.genericFailure, friendlyDbText("   ", null))
        assertEquals(Copy.genericFailure, friendlyDbText("   \n   \n", null))
    }
}
