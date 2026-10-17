package com.jewelindia.app.core.prefs

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrefsTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var prefs: Prefs

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
        prefs = Prefs(fakePrefs)
    }

    @Test
    fun testDeviceIdPersistence() {
        val id1 = prefs.deviceId
        assertNotNull(id1)
        assertTrue(id1.isNotEmpty())

        val id2 = prefs.deviceId
        assertEquals(id1, id2)
    }

    @Test
    fun testAuthAndSignupFields() {
        assertNull(prefs.authIdentity)
        prefs.authIdentity = "user@example.com"
        assertEquals("user@example.com", prefs.authIdentity)

        prefs.referralCode = "REF123"
        assertEquals("REF123", prefs.referralCode)

        prefs.referralRole = "retailer"
        assertEquals("retailer", prefs.referralRole)

        prefs.otpSentAt = 123456789L
        assertEquals(123456789L, prefs.otpSentAt)

        prefs.otpRemainingResends = 2
        assertEquals(2, prefs.otpRemainingResends)

        prefs.otpLockedUntil = 999999999L
        assertEquals(999999999L, prefs.otpLockedUntil)
    }

    @Test
    fun testViewModeAndThemes() {
        val uid = "user-abc-123"
        assertNull(prefs.getViewMode(uid))
        prefs.setViewMode(uid, "employee")
        assertEquals("employee", prefs.getViewMode(uid))

        prefs.clearViewMode(uid)
        assertNull(prefs.getViewMode(uid))

        prefs.storeTheme = "gold"
        assertEquals("gold", prefs.storeTheme)

        prefs.employeeStyle = "modern"
        assertEquals("modern", prefs.employeeStyle)

        assertFalse(prefs.employeeSarvamLegacy)
    }

    @Test
    fun testInviteSkipGuide() {
        val uid = "user-xyz"
        assertFalse(prefs.getInviteSkipGuide(uid))
        prefs.setInviteSkipGuide(uid, true)
        assertTrue(prefs.getInviteSkipGuide(uid))
    }

    /**
     * In-memory test implementation of SharedPreferences.
     */
    private class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = HashMap(map)
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
            @Suppress("UNCHECKED_CAST") (map[key] as? MutableSet<String> ?: defValues)
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class FakeEditor(private val storage: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val temp = mutableMapOf<String, Any?>()
            private var clearFlag = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = value
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = values
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = value
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = value
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = value
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = value
            }
            override fun remove(key: String?): SharedPreferences.Editor = apply {
                if (key != null) temp[key] = null
            }
            override fun clear(): SharedPreferences.Editor = apply { clearFlag = true }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                if (clearFlag) {
                    storage.clear()
                    clearFlag = false
                }
                for ((k, v) in temp) {
                    if (v == null) storage.remove(k) else storage[k] = v
                }
                temp.clear()
            }
        }
    }
}
