// Port of: wholesaler ios/JewelIndia/Sources/Design/Palette.swift
package com.jewelindia.app.design

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens transcribed verbatim from the web app's design system.
 *
 * Source of truth: `Jewel-India-Frontend/app/globals.css` — the Tailwind v4
 * `@theme` block (`--color-celestique-*`) and the `:root` block.
 * Every value here is an exact hex from that file; nothing is approximated.
 */
object Palette {
    // MARK: - Celestique tokens (@theme)

    /** `--color-celestique-taupe: #E6DFD3` — skeleton base, muted surfaces. */
    val taupe = Color(0xFFE6DFD3)

    /** `--color-celestique-cream: #F5F2EB` — page/section fill. */
    val cream = Color(0xFFF5F2EB)

    /** `--color-celestique-dark: #111111` — primary text, dark buttons. */
    val dark = Color(0xFF111111)

    /** `--color-celestique-light: #ffffff` */
    val light = Color(0xFFFFFFFF)

    /** `--color-celestique-muted: #8C857B` — secondary/helper text. */
    val muted = Color(0xFF8C857B)

    /** `--color-celestique-border: #D9D0C5` — hairlines, input borders. */
    val border = Color(0xFFD9D0C5)

    // MARK: - Root tokens (:root)

    /** `--background: #FEFEFE` — note this is *not* pure white; `body` uses it. */
    val background = Color(0xFFFEFEFE)

    /** `--foreground: var(--color-celestique-dark)` */
    val foreground = dark

    // MARK: - Skeleton gradients

    /** `.catalogue-skeleton-bg` gradient stops: #f0f0f0 → #e0e0e0 → #f0f0f0. */
    val catalogueSkeletonLight = Color(0xFFF0F0F0)
    val catalogueSkeletonMid = Color(0xFFE0E0E0)

    /** `.skeleton-shimmer::after` highlight: rgba(245,242,235,0.7). */
    val shimmerHighlight = Color(0xB3F5F2EB) // 0.7 * 255 ≈ 179 = 0xB3

    // MARK: - Status colours

    /** `rgba(34, 197, 94, …)` in `@keyframes rippleGreen` — Tailwind green-500. */
    val statusVerified = Color(0xFF22C55E)

    /** Amber pending pulse — Tailwind amber-500. */
    val statusPending = Color(0xFFF59E0B)

    /** Crimson warning shake — Tailwind red-500. */
    val statusRejected = Color(0xFFEF4444)

    val textPrimary: Color get() = foreground
    val textSecondary: Color get() = muted
    val textMuted: Color get() = muted
    val surfaceElevated: Color get() = cream
    val surface: Color get() = cream
    val white: Color get() = light
    val goldPrimary: Color get() = Color(0xFFC5A880)
    val gold: Color get() = Color(0xFFC5A880)
    val goldLight: Color get() = Color(0xFFD4AF37)
    val accentGreen: Color get() = statusVerified
    val accentRed: Color get() = statusRejected
    val borderSubtle: Color get() = border
    val surfaceBorder: Color get() = border
    val textTertiary: Color get() = muted.copy(alpha = 0.7f)
}

