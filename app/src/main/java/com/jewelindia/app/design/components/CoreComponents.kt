// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift (PressableButtonStyle) & SvgImage
package com.jewelindia.app.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Scale-down effect on touch, mirroring iOS PressableButtonStyle and PressScaleStyle.
 */
fun Modifier.pressScale(
    targetScale: Float = 0.97f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pressScale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Helper to display SVG assets bundled in assets/svg/.
 */
@Composable
fun SvgImage(
    name: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null,
    contentScale: ContentScale = ContentScale.Fit
) {
    AsyncImage(
        model = "file:///android_asset/svg/$name.svg",
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = colorFilter,
        contentScale = contentScale
    )
}
