package com.jewelindia.app

import com.jewelindia.app.core.text.Credentials
import com.jewelindia.app.core.text.PasswordRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialsTest {

    @Test
    fun testIsEmail() {
        assertTrue(Credentials.isEmail("test@example.com"))
        assertTrue(Credentials.isEmail("user.name+tag@domain.co.in"))
        assertFalse(Credentials.isEmail("not-an-email"))
        assertFalse(Credentials.isEmail("@no-user.com"))
        assertFalse(Credentials.isEmail("spaces in@email.com"))
        assertFalse(Credentials.isEmail(""))
    }

    @Test
    fun testValidateIndianMobile() {
        // Plain 10-digit starting with 6-9
        val check1 = Credentials.validateIndianMobile("9876543210")
        assertTrue(check1.valid)
        assertEquals("+919876543210", check1.normalized)

        // Starting with 0
        val check2 = Credentials.validateIndianMobile("08876543210")
        assertTrue(check2.valid)
        assertEquals("+918876543210", check2.normalized)

        // Starting with 91
        val check3 = Credentials.validateIndianMobile("917876543210")
        assertTrue(check3.valid)
        assertEquals("+917876543210", check3.normalized)

        // Formatted with spaces or dashes
        val check4 = Credentials.validateIndianMobile("+91 98765-43210")
        assertTrue(check4.valid)
        assertEquals("+919876543210", check4.normalized)

        // Invalid: starts with 5
        val check5 = Credentials.validateIndianMobile("5876543210")
        assertFalse(check5.valid)
        assertEquals(null, check5.normalized)

        // Invalid length
        val check6 = Credentials.validateIndianMobile("98765")
        assertFalse(check6.valid)
        assertEquals(null, check6.normalized)
    }

    @Test
    fun testPasswordRules() {
        val validPass = "Jewel@1234"
        assertTrue(PasswordRule.Length.passes(validPass))
        assertTrue(PasswordRule.Upper.passes(validPass))
        assertTrue(PasswordRule.Lower.passes(validPass))
        assertTrue(PasswordRule.Special.passes(validPass))
        assertTrue(PasswordRule.allPass(validPass))
        assertTrue(Credentials.meetsServerPasswordRule(validPass))

        val shortPass = "J@123"
        assertFalse(PasswordRule.Length.passes(shortPass))
        assertFalse(PasswordRule.allPass(shortPass))
        assertFalse(Credentials.meetsServerPasswordRule(shortPass))

        val noUpper = "jewel@1234"
        assertFalse(PasswordRule.Upper.passes(noUpper))
        assertFalse(PasswordRule.allPass(noUpper))
        assertFalse(Credentials.meetsServerPasswordRule(noUpper))

        val noLower = "JEWEL@1234"
        assertFalse(PasswordRule.Lower.passes(noLower))
        assertFalse(PasswordRule.allPass(noLower))
        assertFalse(Credentials.meetsServerPasswordRule(noLower))

        val noSpecial = "Jewel12345"
        assertFalse(PasswordRule.Special.passes(noSpecial))
        assertFalse(PasswordRule.allPass(noSpecial))
        assertFalse(Credentials.meetsServerPasswordRule(noSpecial))
    }
}
