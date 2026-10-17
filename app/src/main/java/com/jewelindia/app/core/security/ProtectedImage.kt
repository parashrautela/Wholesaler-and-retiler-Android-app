// Port of: wholesaler ios/JewelIndia/Sources/ScreenProtection/ProtectedImageView.swift
package com.jewelindia.app.core.security

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.jewelindia.app.core.images.CachedImage

/**
 * Image view that is protected from screenshots and screen captures.
 */
@Composable
fun ProtectedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    CaptureProtected(modifier = modifier) {
        CachedImage(
            url = url,
            contentDescription = contentDescription,
            contentScale = contentScale
        )
    }
}
