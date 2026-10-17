// Port of: wholesaler ios/JewelIndia/Sources/Core/Credentials.swift
package com.jewelindia.app.core.text

/**
 * Exact port of `Jewel-India-Frontend/lib/utils/credentials.js`.
 * The regexes and the normalisation branches are reproduced character for
 * character so the client accepts and rejects exactly what the web accepts.
 */
object Credentials {

    private val emailRegex = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
    private val indianMobileRegex1 = Regex("""^[6-9]\d{9}$""")
    private val indianMobileRegex2 = Regex("""^0[6-9]\d{9}$""")
    private val indianMobileRegex3 = Regex("""^91[6-9]\d{9}$""")
    private val serverPasswordRegex = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]).{8,}$""")

    /**
     * `/^[^\s@]+@[^\s@]+\.[^\s@]+$/` — the loose email test used by
     * EntryForm, check-user, send-otp, verify-otp and signIn.
     */
    fun isEmail(input: String): Boolean = emailRegex.matches(input)

    data class MobileCheck(
        val valid: Boolean,
        val normalized: String?
    )

    /**
     * Port of `validateIndianMobile(input)`.
     *
     * ```js
     * digits = String(input).replace(/\D/g, "")
     * /^[6-9]\d{9}$/   -> +91<digits>
     * /^0[6-9]\d{9}$/  -> +91<digits.slice(1)>
     * /^91[6-9]\d{9}$/ -> +<digits>
     * ```
     */
    fun validateIndianMobile(input: String): MobileCheck {
        val digits = input.filter { it.isDigit() }

        if (indianMobileRegex1.matches(digits)) {
            return MobileCheck(valid = true, normalized = "+91$digits")
        }
        if (indianMobileRegex2.matches(digits)) {
            return MobileCheck(valid = true, normalized = "+91${digits.drop(1)}")
        }
        if (indianMobileRegex3.matches(digits)) {
            return MobileCheck(valid = true, normalized = "+$digits")
        }
        return MobileCheck(valid = false, normalized = null)
    }

    /**
     * The server-side password rule enforced by `POST /api/auth/set-password`:
     * `/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/`
     *
     * The client checklist on the Set Password screen is expressed as four
     * separate predicates (see [PasswordRule]), which together are equivalent.
     */
    fun meetsServerPasswordRule(password: String): Boolean = serverPasswordRegex.matches(password)
}

/**
 * The four checklist rules rendered on `/entry_page/signup/set-password`,
 * ported from the `RULES` array in `components/auth/SetPasswordForm.jsx`.
 * Labels are verbatim, including the en-dashes.
 */
enum class PasswordRule(val id: String, val label: String) {
    Length("length", "At least 8 characters"),
    Upper("upper", "1 uppercase letter (A–Z)"),
    Lower("lower", "1 lowercase letter (a–z)"),
    Special("special", "1 number (0–9) and 1 special character (!@#$%^&*...)");

    fun passes(p: String): Boolean = when (this) {
        Length -> p.length >= 8
        Upper -> upperRegex.containsMatchIn(p)
        Lower -> lowerRegex.containsMatchIn(p)
        Special -> digitRegex.containsMatchIn(p) && specialRegex.containsMatchIn(p)
    }

    companion object {
        private val upperRegex = Regex("[A-Z]")
        private val lowerRegex = Regex("[a-z]")
        private val digitRegex = Regex("[0-9]")
        private val specialRegex = Regex("""[!@#$%^&*()\-_=+\[\]{};:'",.<>/?\\|`~]""")

        fun allPass(p: String): Boolean = entries.all { it.passes(p) }
    }
}
