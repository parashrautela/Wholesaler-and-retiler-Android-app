// Port of: wholesaler ios/JewelIndia/Sources/Core/Images/CachedImage.swift
package com.jewelindia.app.core.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CancellationException
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color? = Palette.taupe.copy(alpha = 0.35f),
    fallbackUrl: String? = null
) {
    var retry by remember(url) { mutableIntStateOf(0) }
    var useFallback by remember(url, retry) { mutableStateOf(false) }
    var resolved by remember(url, fallbackUrl, retry) { mutableStateOf<String?>(null) }
    var failed by remember(url, fallbackUrl, retry) { mutableStateOf(false) }
    val source = if (useFallback) fallbackUrl else url
    LaunchedEffect(source, retry) {
        resolved = null
        failed = false
        if (!source.isNullOrBlank()) {
            try {
                resolved = StorageImageURL.resolve(source)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (!useFallback && !fallbackUrl.isNullOrBlank() && fallbackUrl != url) useFallback = true
                else failed = true
            }
        }
    }
    Box(
        modifier = if (placeholderColor != null) modifier.background(placeholderColor) else modifier,
        contentAlignment = Alignment.Center
    ) {
        if (resolved != null && !failed) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(resolved).crossfade(true).build(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    if (!useFallback && !fallbackUrl.isNullOrBlank() && fallbackUrl != url) useFallback = true
                    else failed = true
                }
            )
        }
        if (failed) {
            TextButton(onClick = { retry++ }) { Text("Retry image") }
        }
    }
}
