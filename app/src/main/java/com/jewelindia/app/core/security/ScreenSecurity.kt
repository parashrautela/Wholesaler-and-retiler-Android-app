// Port of: wholesaler ios/JewelIndia/Sources/ScreenProtection/SecureLayerHost.swift
package com.jewelindia.app.core.security

import android.os.Handler
import android.os.Looper
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
            notifyChange(true)
        }
    }

    fun release() {
        var transitionedToZero = false
        counter.updateAndGet { current ->
            if (current > 0) {
                if (current == 1) transitionedToZero = true
                current - 1
            } else {
                0
            }
        }
        if (transitionedToZero) {
            notifyChange(false)
        }
    }

    private fun notifyChange(isSecure: Boolean) {
        try {
            val looper = Looper.getMainLooper()
            if (looper != null && Looper.myLooper() == looper) {
                onSecureChange(isSecure)
            } else if (looper != null) {
                Handler(looper).post { onSecureChange(isSecure) }
            } else {
                onSecureChange(isSecure)
            }
        } catch (_: Throwable) {
            onSecureChange(isSecure)
        }
    }

    val isProtected: Boolean get() = counter.get() > 0
}

val LocalScreenSecurity = compositionLocalOf<ScreenSecurity> {
    ScreenSecurity {}
}

