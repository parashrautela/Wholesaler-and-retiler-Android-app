package com.jewelindia.app

import com.jewelindia.app.core.text.Credentials
import com.jewelindia.app.core.text.PasswordRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRuleTest {

    @Test
    fun testLengthRule() {
        assertFalse(PasswordRule.Length.passes(""))
        assertFalse(PasswordRule.Length.passes("1234567"))
        assertTrue(PasswordRule.Length.passes("12345678"))
        assertTrue(PasswordRule.Length.passes("1234567890"))
    }

    @Test
    fun testUpperRule() {
        assertFalse(PasswordRule.Upper.passes("lowercase123!"))
        assertTrue(PasswordRule.Upper.passes("A"))
        assertTrue(PasswordRule.Upper.passes("lowercaseA123!"))
    }

    @Test
    fun testLowerRule() {
        assertFalse(PasswordRule.Lower.passes("UPPERCASE123!"))
        assertTrue(PasswordRule.Lower.passes("a"))
        assertTrue(PasswordRule.Lower.passes("UPPERaCASE123!"))
    }

    @Test
    fun testSpecialRule() {
        // Must contain both a digit and a special character
        assertFalse(PasswordRule.Special.passes("NoNumberOrSpecial"))
        assertFalse(PasswordRule.Special.passes("OnlyNumber1234"))
        assertFalse(PasswordRule.Special.passes("OnlySpecial!@#$"))
        assertTrue(PasswordRule.Special.passes("1!"))
        assertTrue(PasswordRule.Special.passes("Pass1@Word"))
        assertTrue(PasswordRule.Special.passes("Pass#9Word"))
    }

    @Test
    fun testAllPassCombined() {
        val valid = "Jewel@1234"
        assertTrue(PasswordRule.allPass(valid))
        assertTrue(Credentials.meetsServerPasswordRule(valid))

        // Missing each component
        assertFalse("Too short", PasswordRule.allPass("J@1234"))
        assertFalse("Missing upper", PasswordRule.allPass("jewel@1234"))
        assertFalse("Missing lower", PasswordRule.allPass("JEWEL@1234"))
        assertFalse("Missing digit", PasswordRule.allPass("Jewel@Word"))
        assertFalse("Missing special", PasswordRule.allPass("Jewel12345"))
        assertFalse("Empty", PasswordRule.allPass(""))
    }

    @Test
    fun testRuleLabelsMatchSpec() {
        assertEquals("At least 8 characters", PasswordRule.Length.label)
        assertEquals("1 uppercase letter (A–Z)", PasswordRule.Upper.label)
        assertEquals("1 lowercase letter (a–z)", PasswordRule.Lower.label)
        assertEquals("1 number (0–9) and 1 special character (!@#$%^&*...)", PasswordRule.Special.label)
    }
}
