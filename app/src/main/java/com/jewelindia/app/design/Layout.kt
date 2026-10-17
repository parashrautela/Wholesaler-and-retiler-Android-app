// Port of: wholesaler ios/JewelIndia/Sources/Design/Layout.swift
package com.jewelindia.app.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, radii and sizing tokens.
 *
 * The web app styles with Tailwind utilities rather than named spacing tokens,
 * so this reproduces Tailwind's default scale (0.25rem = 4pt base) that those
 * utilities compile to. `space(4)` is Tailwind's `p-4` / `gap-4` = 16dp.
 */
object Spacing {
    fun step(n: Int): Dp = (n * 4).dp

    val xs = step(1)     // 4dp
    val sm = step(2)     // 8dp
    val md = step(3)     // 12dp
    val base = step(4)   // 16dp
    val lg = step(5)     // 20dp
    val xl = step(6)     // 24dp
    val xxl = step(8)    // 32dp
    val xxxl = step(12)  // 48dp
    val huge = step(16)  // 64dp

    /** The horizontal gutter the web layouts use for mobile content. */
    val screenGutter = 20.dp

    /** Clearance reserved for the bottom navigation. */
    val floatingNavClearance = 72.dp
}

/** Corner radii observed across the web components. */
object Radius {
    val sm = 4.dp
    val md = 6.dp
    val lg = 8.dp
    val xl = 12.dp
    val xxl = 16.dp
    val pill = 999.dp

    val shapeSm = RoundedCornerShape(sm)
    val shapeMd = RoundedCornerShape(md)
    val shapeLg = RoundedCornerShape(lg)
    val shapeXl = RoundedCornerShape(xl)
    val shapeXxl = RoundedCornerShape(xxl)
    val shapePill = CircleShape
}

/** Hairline widths. */
object Stroke {
    val hairline = 1.dp
    val emphasis = 1.5.dp
}
