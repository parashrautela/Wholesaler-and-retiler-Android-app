// Port of: wholesaler ios/JewelIndia/Sources/Core/AIConsent.swift
package com.jewelindia.app.core.consent

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Permission to send jewellery photos to third-party AI providers. App Review
 * (5.1.2(i)) requires saying who receives the data and getting an explicit
 * yes before the first upload, so every AI entry point asks once per account.
 */
object AIConsent {
    /**
     * Who actually receives the photos — keep in step with the ai-pipeline
     * service and the privacy policy.
     */
    const val PROVIDERS = "Reve, NanoBanana (Google image models) and OpenAI"

    private const val PREFS_NAME = "jewel_ai_consent"
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun key(userId: String): String {
        val user = userId.lowercase()
        return "ai-consent.v1.$user"
    }

    fun isGranted(userId: String): Boolean {
        return userId.isNotBlank() && (prefs?.getBoolean(key(userId), false) ?: false)
    }

    fun grant(userId: String) {
        if (userId.isNotBlank()) {
            prefs?.edit { putBoolean(key(userId), true) }
        }
    }

    /**
     * The last line of defence: every call that sends photos to the AI
     * service checks this, whichever screen it came from.
     */
    fun requireGranted(userId: String) {
        if (!isGranted(userId)) {
            throw RequiredException()
        }
    }

    class RequiredException(
        message: String = "Allow AI image processing to use this feature."
    ) : RuntimeException(message)
}
