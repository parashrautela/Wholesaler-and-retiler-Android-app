// Port of: wholesaler ios/JewelIndia/Sources/Core/Images/CachedImage.swift
package com.jewelindia.app.core.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jewelindia.app.design.Palette

/**
 * A remote image loaded through Coil: downloaded once, size-aware decoded,
 * cached in memory and on disk.
 *
 * Displays a subtle [Palette.Taupe] (35% opacity) placeholder while loading or when URL is empty.
 */
@Composable
fun CachedImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        modifier = modifier.background(Palette.taupe.copy(alpha = 0.35f))
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
