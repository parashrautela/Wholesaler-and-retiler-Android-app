// Port of: wholesaler ios/JewelIndia/Sources/ScreenProtection/View+CaptureProtected.swift
package com.jewelindia.app.core.security

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

/**
 * Composable wrapper that prevents screenshot and screen recording of its contents
 * by acquiring `LocalScreenSecurity` for its lifetime.
 */
@Composable
fun CaptureProtected(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val security = LocalScreenSecurity.current

    DisposableEffect(security) {
        security.acquire()
        onDispose {
            security.release()
        }
    }

    Box(modifier = modifier) {
        content()
    }
}
