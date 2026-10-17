// Port of: wholesaler ios/JewelIndia/Sources/ScreenProtection/ProtectedImageView.swift
package com.jewelindia.app.core.security

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.design.Palette

/**
 * Image view that is protected from screenshots and screen captures.
 */
@Composable
fun ProtectedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color? = Palette.taupe.copy(alpha = 0.35f)
) {
    CaptureProtected(modifier = modifier) {
        CachedImage(
            url = url,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = contentScale,
            placeholderColor = placeholderColor
        )
    }
}
