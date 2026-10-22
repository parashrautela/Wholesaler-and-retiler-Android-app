// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardKit.swift
package com.jewelindia.app.feature.onboarding

import com.jewelindia.app.design.components.SvgImage
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

object OnboardColor {
    val text = Color(0xFF374151)
    val heading = Color(0xFF111827)
    val subtle = Color(0xFF9CA3AF)
    val fieldFill = Color(0xFFF5F5F5)
    val border = Color(0xFFE5E7EB)
    val navBorder = Color(0xFFE0E0E0)
    val danger = Color(0xFFEF4444)
    val uploadFill = Color(0xFFEEF4FF)
    val uploadBorder = Color(0xFF2563EB)
    val uploadFilled = Color(0xFF22C55E)
    val disabled = Color(0xFFD1D5DB)
    val submitting = Color(0xFF6B7280)
}

/**
 * Top bar matching iOS `OnboardNavbar`.
 * Shows Back button, optional Refresh button, and Sign out button.
 */
@Composable
fun OnboardNavbar(
    gutter: Dp,
    showsBack: Boolean,
    onBack: (() -> Unit)?,
    onSignOut: () -> Unit,
    onRefresh: (suspend () -> Unit)? = null,
    isRefreshing: Boolean = false
) {
    var confirmSignOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = gutter, vertical = 10.dp)
            .drawBehind {
                drawLine(
                    color = OnboardColor.navBorder,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showsBack && onBack != null) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnboardColor.text,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Back",
                    fontSize = 14.sp,
                    color = OnboardColor.text,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (onRefresh != null) {
            val infiniteTransition = rememberInfiniteTransition(label = "refresh")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isRefreshing) {
                        scope.launch { onRefresh() }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) OnboardColor.subtle else OnboardColor.text,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (isRefreshing) rotation else 0f)
                )
                Text(
                    text = if (isRefreshing) "Checking..." else "Refresh",
                    fontSize = 14.sp,
                    color = if (isRefreshing) OnboardColor.subtle else OnboardColor.text
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { confirmSignOut = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sign out",
                tint = OnboardColor.text,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Sign out",
                fontSize = 14.sp,
                color = OnboardColor.text
            )
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need to sign in again to finish onboarding.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    onSignOut()
                }) {
                    Text("Sign out", color = OnboardColor.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Left branding & step panel matching iOS `LeftPanel`.
 */
@Composable
fun LeftPanel(
    heading: String,
    subheading: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            SvgImage(
                name = "JewelLogo",
                contentDescription = "Jewel Logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(7.dp))
            )
            Text(
                text = "Jewels India",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = OnboardColor.heading,
                letterSpacing = 0.4.sp
            )
        }

        Text(
            text = heading,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = OnboardColor.heading,
            lineHeight = 26.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = subheading,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = OnboardColor.subtle,
            lineHeight = 18.sp
        )
    }
}

/**
 * Step progress bar matching iOS `StepIndicator`.
 */
@Composable
fun StepIndicator(
    step: Int,
    totalSteps: Int = 3
) {
    val progress = remember { Animatable(0f) }
    val target = (step - 1) / totalSteps.toFloat()

    LaunchedEffect(step) {
        progress.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 800,
                easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row {
            Text(
                text = "STEP $step",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = OnboardColor.heading,
                letterSpacing = 1.2.sp
            )
            Text(
                text = " OF $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = OnboardColor.subtle,
                letterSpacing = 1.2.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(OnboardColor.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.value.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(OnboardColor.heading)
            )
        }
    }
}

/**
 * Onboard layout wrapper matching iOS `OnboardLayout`.
 */
@Composable
fun OnboardLayout(
    step: Int,
    heading: String,
    subheading: String,
    showsBack: Boolean = true,
    onBack: (() -> Unit)? = null,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit
) {
    val gutter = 20.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        OnboardNavbar(
            gutter = gutter,
            showsBack = showsBack,
            onBack = onBack,
            onSignOut = onSignOut
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = gutter)
                .padding(top = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LeftPanel(heading = heading, subheading = subheading)
                StepIndicator(step = step, totalSteps = 3)
            }

            content()
        }
    }
}
