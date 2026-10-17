// Port of: UserDefaults keys from iOS (IMPLEMENTATION_PLAN.md §7.9)
package com.jewelindia.app.core.prefs

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Type-safe SharedPreferences wrapper corresponding to iOS `UserDefaults.standard`.
 * Backed by a single file `jewel_prefs`.
 */
class Prefs(val sharedPreferences: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        const val PREFS_NAME = "jewel_prefs"

        const val KEY_AUTH_IDENTITY = "auth_identity"
        const val KEY_REFERRAL_CODE = "referral_code"
        const val KEY_REFERRAL_ROLE = "referral_role"
        const val KEY_SIGNUP_ROLE = "signup_role"
        const val KEY_REFERRAL_WHOLESALER = "referral_wholesaler"
        const val KEY_OTP_SENT_AT = "otp_sent_at"
        const val KEY_OTP_REMAINING_RESENDS = "otp_remaining_resends"
        const val KEY_OTP_LOCKED_UNTIL = "otp_locked_until"
        const val KEY_STORE_THEME = "jewel_store_theme"
        const val KEY_EMPLOYEE_STYLE = "jewel_employee_style"
        const val KEY_EMPLOYEE_SARVAM = "jewel_employee_sarvam"
        const val KEY_DEVICE_ID = "device_id"

        fun keyViewMode(userId: String) = "jewel_view_mode.$userId"
        fun keyEmployeeOrdersLastChecked(seedId: String) = "employee_orders_last_checked.$seedId"
        fun keyInviteSkipGuide(userId: String) = "invite_skip_guide.$userId"
    }

    // Device installation ID (§8.2: random UUID generated once and stored in Prefs)
    val deviceId: String
        get() {
            var id = sharedPreferences.getString(KEY_DEVICE_ID, null)
            if (id.isNullOrBlank()) {
                id = UUID.randomUUID().toString()
                sharedPreferences.edit().putString(KEY_DEVICE_ID, id).apply()
            }
            return id
        }

    // View mode
    fun getViewMode(userId: String): String? = sharedPreferences.getString(keyViewMode(userId), null)
    fun setViewMode(userId: String, mode: String) {
        sharedPreferences.edit().putString(keyViewMode(userId), mode).apply()
    }
    fun clearViewMode(userId: String) {
        sharedPreferences.edit().remove(keyViewMode(userId)).apply()
    }

    // Auth identity & signup
    var authIdentity: String?
        get() = sharedPreferences.getString(KEY_AUTH_IDENTITY, null)
        set(value) = edit { if (value != null) putString(KEY_AUTH_IDENTITY, value) else remove(KEY_AUTH_IDENTITY) }

    var referralCode: String?
        get() = sharedPreferences.getString(KEY_REFERRAL_CODE, null)
        set(value) = edit { if (value != null) putString(KEY_REFERRAL_CODE, value) else remove(KEY_REFERRAL_CODE) }

    var referralRole: String?
        get() = sharedPreferences.getString(KEY_REFERRAL_ROLE, null)
        set(value) = edit { if (value != null) putString(KEY_REFERRAL_ROLE, value) else remove(KEY_REFERRAL_ROLE) }

    var signupRole: String?
        get() = sharedPreferences.getString(KEY_SIGNUP_ROLE, null)
        set(value) = edit { if (value != null) putString(KEY_SIGNUP_ROLE, value) else remove(KEY_SIGNUP_ROLE) }

    var referralWholesaler: String?
        get() = sharedPreferences.getString(KEY_REFERRAL_WHOLESALER, null)
        set(value) = edit { if (value != null) putString(KEY_REFERRAL_WHOLESALER, value) else remove(KEY_REFERRAL_WHOLESALER) }

    var otpSentAt: Long?
        get() = if (sharedPreferences.contains(KEY_OTP_SENT_AT)) sharedPreferences.getLong(KEY_OTP_SENT_AT, 0L) else null
        set(value) = edit { if (value != null) putLong(KEY_OTP_SENT_AT, value) else remove(KEY_OTP_SENT_AT) }

    var otpRemainingResends: Int
        get() = sharedPreferences.getInt(KEY_OTP_REMAINING_RESENDS, 3)
        set(value) = edit { putInt(KEY_OTP_REMAINING_RESENDS, value) }

    var otpLockedUntil: Long?
        get() = if (sharedPreferences.contains(KEY_OTP_LOCKED_UNTIL)) sharedPreferences.getLong(KEY_OTP_LOCKED_UNTIL, 0L) else null
        set(value) = edit { if (value != null) putLong(KEY_OTP_LOCKED_UNTIL, value) else remove(KEY_OTP_LOCKED_UNTIL) }

    // Retailer / Employee
    var storeTheme: String?
        get() = sharedPreferences.getString(KEY_STORE_THEME, null)
        set(value) = edit { if (value != null) putString(KEY_STORE_THEME, value) else remove(KEY_STORE_THEME) }

    var employeeStyle: String?
        get() = sharedPreferences.getString(KEY_EMPLOYEE_STYLE, null)
        set(value) = edit { if (value != null) putString(KEY_EMPLOYEE_STYLE, value) else remove(KEY_EMPLOYEE_STYLE) }

    var employeeSarvam: Boolean
        get() = sharedPreferences.getBoolean(KEY_EMPLOYEE_SARVAM, false)
        set(value) = edit { putBoolean(KEY_EMPLOYEE_SARVAM, value) }

    val employeeSarvamLegacy: Boolean
        get() = sharedPreferences.getBoolean(KEY_EMPLOYEE_SARVAM, false)

    fun getEmployeeOrdersLastChecked(seedId: String): Long? {
        val k = keyEmployeeOrdersLastChecked(seedId)
        return if (sharedPreferences.contains(k)) sharedPreferences.getLong(k, 0L) else null
    }

    fun setEmployeeOrdersLastChecked(seedId: String, timestamp: Long) {
        edit { putLong(keyEmployeeOrdersLastChecked(seedId), timestamp) }
    }

    fun getEmployeeOrdersLastCheckedString(seedId: String): String? {
        val k = keyEmployeeOrdersLastChecked(seedId)
        return sharedPreferences.getString(k, null)
    }

    fun setEmployeeOrdersLastCheckedString(seedId: String, isoTimestamp: String) {
        edit { putString(keyEmployeeOrdersLastChecked(seedId), isoTimestamp) }
    }

    fun getInviteSkipGuide(userId: String): Boolean {
        return sharedPreferences.getBoolean(keyInviteSkipGuide(userId), false)
    }

    fun setInviteSkipGuide(userId: String, skip: Boolean) {
        edit { putBoolean(keyInviteSkipGuide(userId), skip) }
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    private inline fun edit(action: SharedPreferences.Editor.() -> Unit) {
        sharedPreferences.edit().apply(action).apply()
    }
}
