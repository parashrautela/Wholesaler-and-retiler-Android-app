// Port of: wholesaler ios/JewelIndia/Sources/ScreenProtection/SecureLayerHost.swift
package com.jewelindia.app.core.security

import androidx.compose.runtime.compositionLocalOf
import java.util.concurrent.atomic.AtomicInteger

/**
 * Reference-counted screen protection coordinator.
 * Whenever any screen or image requiring screenshot protection is mounted,
 * it acquires this lock, which sets `FLAG_SECURE` on the Window.
 */
class ScreenSecurity(
    private val onSecureChange: (Boolean) -> Unit
) {
    private val counter = AtomicInteger(0)

    fun acquire() {
        val count = counter.incrementAndGet()
        if (count == 1) {
            onSecureChange(true)
        }
    }

    fun release() {
        val count = counter.decrementAndGet()
        if (count <= 0) {
            counter.set(0)
            onSecureChange(false)
        }
    }

    val isProtected: Boolean get() = counter.get() > 0
}

val LocalScreenSecurity = compositionLocalOf<ScreenSecurity> {
    ScreenSecurity {}
}
