// Port of: wholesaler ios/JewelIndia/Sources/Core/LegalLinks.swift
package com.jewelindia.app.core.legal

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object LegalLinks {
    const val terms = "https://jewelindia.shop/terms-conditions"
    const val privacy = "https://jewelindia.shop/privacy-policy"
    const val support = "https://jewelindia.shop/support"

    fun open(context: Context, url: String) {
        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, Uri.parse(url))
        } catch (_: Exception) {}
    }
}
