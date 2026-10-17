// Port of: wholesaler ios/JewelIndia/Sources/Design/Motion.swift
package com.jewelindia.app.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp

/**
 * Animation curves and durations transcribed from the `@keyframes` blocks in
 * `Jewel-India-Frontend/app/globals.css`.
 */
object Motion {

    // MARK: - Curves

    /** `cubic-bezier(0.16, 1, 0.3, 1)` — the app's signature ease-out. */
    val signatureEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    /** `cubic-bezier(0.4, 0, 0.2, 1)` — standard easing. */
    val standardEasing: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** `cubic-bezier(0.36, 0.07, 0.19, 0.97)` — rejection shake curve. */
    val shakeEasing: Easing = CubicBezierEasing(0.36f, 0.07f, 0.19f, 0.97f)

    // MARK: - Offsets

    val fadeInUpOffset = 10.dp
    val scaleInStart = 0.98f
    val cardEnterOffset = 28.dp
    val onboardPageOffset = 8.dp
    val stepFadeOffset = 6.dp

    // MARK: - Specs

    fun <T> signature(durationMillis: Int = 500) = tween<T>(
        durationMillis = durationMillis,
        easing = signatureEasing
    )

    fun <T> standard(durationMillis: Int = 350) = tween<T>(
        durationMillis = durationMillis,
        easing = standardEasing
    )

    val fadeIn = tween<Float>(durationMillis = 400, easing = FastOutSlowInEasing)
    val fadeInUp = signature<Float>(500)
    val scaleIn = signature<Float>(400)
    val cardEnter = signature<Float>(550)
    val onboardPage = signature<Float>(350)
    val stepFade = signature<Float>(400)
    val checkDraw = standard<Float>(350)
    val rippleGreen = signature<Float>(600)

    val pulseAmber = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )

    val spinSlow = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 8000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )

    val shimmer = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Restart
    )

    val catalogueShimmer = infiniteRepeatable<Float>(
        animation = tween(durationMillis = 1500, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )

    fun delayMillis(index: Int, stepMillis: Int = 100): Int = index * stepMillis
}
