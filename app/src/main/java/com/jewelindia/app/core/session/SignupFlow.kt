// Port of: wholesaler ios/JewelIndia/Sources/Core/SignupFlow.swift
package com.jewelindia.app.core.session

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.text.Copy

/**
 * The signup hand-off state the web keeps in `sessionStorage`:
 * `auth_identity`, `otp_sent_at`, `otp_remaining_resends`, `otp_locked_until`,
 * `referral_code`, `referral_role`.
 *
 * The OTP timing keys are persisted in [SharedPreferences] so the countdown
 * and the 24-hour lockout survive an app relaunch.
 */
class SignupFlow(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("jewel_signup_flow_prefs", Context.MODE_PRIVATE)
    )

    companion object {
        /**
         * Latched for the OTP → set-password leg of a new signup.
         * While set, [SessionStore] adopts the user but leaves routing alone.
         */
        @Volatile
        var isCompletingSignup: Boolean = false

        private const val KEY_IDENTITY = "auth_identity"
        private const val KEY_REFERRAL_CODE = "referral_code"
        private const val KEY_REFERRAL_ROLE = "referral_role"
        private const val KEY_CHOSEN_ROLE = "signup_role"
        private const val KEY_INVITED_BY = "referral_wholesaler"
        private const val KEY_OTP_SENT_AT = "otp_sent_at"
        private const val KEY_REMAINING_RESENDS = "otp_remaining_resends"
        private const val KEY_LOCKED_UNTIL = "otp_locked_until"
    }

    var identity: String?
        get() = prefs.getString(KEY_IDENTITY, null)
        set(value) = prefs.edit {
            if (value != null) putString(KEY_IDENTITY, value) else remove(KEY_IDENTITY)
        }

    var referralCode: String?
        get() = prefs.getString(KEY_REFERRAL_CODE, null)
        set(value) = prefs.edit {
            if (value != null) putString(KEY_REFERRAL_CODE, value) else remove(KEY_REFERRAL_CODE)
        }

    var referralRole: UserRole?
        get() = prefs.getString(KEY_REFERRAL_ROLE, null)?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        }
        set(value) = prefs.edit {
            if (value != null) putString(KEY_REFERRAL_ROLE, value.name) else remove(KEY_REFERRAL_ROLE)
        }

    var chosenRole: UserRole?
        get() = prefs.getString(KEY_CHOSEN_ROLE, null)?.let {
            runCatching { UserRole.valueOf(it) }.getOrNull()
        }
        set(value) = prefs.edit {
            if (value != null) putString(KEY_CHOSEN_ROLE, value.name) else remove(KEY_CHOSEN_ROLE)
        }

    var invitedBy: String?
        get() = prefs.getString(KEY_INVITED_BY, null)
        set(value) = prefs.edit {
            if (value != null) putString(KEY_INVITED_BY, value) else remove(KEY_INVITED_BY)
        }

    var otpSentAt: Long?
        get() = if (prefs.contains(KEY_OTP_SENT_AT)) prefs.getLong(KEY_OTP_SENT_AT, 0L) else null
        set(value) = prefs.edit {
            if (value != null) putLong(KEY_OTP_SENT_AT, value) else remove(KEY_OTP_SENT_AT)
        }

    var remainingResends: Int
        get() = prefs.getInt(KEY_REMAINING_RESENDS, Copy.otpMaxResends)
        set(value) = prefs.edit { putInt(KEY_REMAINING_RESENDS, value) }

    var lockedUntil: Long?
        get() = if (prefs.contains(KEY_LOCKED_UNTIL)) prefs.getLong(KEY_LOCKED_UNTIL, 0L) else null
        set(value) = prefs.edit {
            if (value != null) putLong(KEY_LOCKED_UNTIL, value) else remove(KEY_LOCKED_UNTIL)
        }

    init {
        // Drop a stale lockout on initialization
        val until = lockedUntil
        if (until != null && until <= System.currentTimeMillis()) {
            lockedUntil = null
        }
    }

    /**
     * The role for this signup: the door chosen on the first screen,
     * or the referral role from an invitation link.
     */
    val signupRole: UserRole?
        get() = chosenRole ?: referralRole

    val isLocked: Boolean
        get() {
            val until = lockedUntil ?: return false
            return until > System.currentTimeMillis()
        }

    /**
     * Seconds left of the 60-second OTP validity window.
     */
    val otpSecondsLeft: Int
        get() {
            val sent = otpSentAt ?: return 0
            val elapsed = ((System.currentTimeMillis() - sent) / 1000).toInt()
            return maxOf(0, Copy.otpValiditySeconds - elapsed)
        }

    fun clearOTPState() {
        identity = null
        otpSentAt = null
        lockedUntil = null
        remainingResends = Copy.otpMaxResends
        chosenRole = null
    }

    fun clearIdentity() {
        identity = null
    }

    /**
     * Captures an installed-app universal link such as:
     * `https://app.jewelindia.shop/join/PJ-a8k3x2`.
     */
    fun captureRetailerInvitation(url: String): Boolean {
        val uri = try {
            java.net.URI.create(url)
        } catch (_: Exception) {
            return false
        }
        if (!uri.scheme.equals("https", ignoreCase = true)) return false

        val siteHost = try {
            java.net.URI.create(AppConfig.siteUrl).host ?: ""
        } catch (_: Exception) {
            ""
        }
        val linkHost = uri.host ?: ""
        if (!linkHost.equals(siteHost, ignoreCase = true)) return false

        val path = uri.path ?: ""
        val segments = path.split("/").filter { it.isNotEmpty() }
        if (segments.size != 2) return false
        if (!segments[0].equals("join", ignoreCase = true)) return false
        val code = segments[1].trim()
        if (code.isEmpty()) return false

        referralCode = code
        referralRole = UserRole.retailer
        return true
    }

    fun captureRetailerInvitation(uri: Uri): Boolean = captureRetailerInvitation(uri.toString())

    fun clearReferral() {
        referralCode = null
        referralRole = null
        invitedBy = null
    }

    fun markSignupCompleted() {
        isCompletingSignup = false
        clearOTPState()
        clearReferral()
    }
}
