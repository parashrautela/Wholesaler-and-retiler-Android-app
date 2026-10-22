// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/AuthKit.swift (Buttons & Banners)
package com.jewelindia.app.feature.auth

import androidx.core.net.toUri
import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.core.legal.LegalLinks
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@Composable
fun AuthPrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    cornerRadius: Dp = 8.dp,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    enabledFill: Color = AuthColor.buttonEnabled,
    disabledFill: Color = AuthColor.buttonDisabled,
    usesOpacityWhenDisabled: Boolean = false,
    isEnabled: Boolean = true,
    isLoading: Boolean = false
) {
    val clickable = isEnabled && !isLoading
    val backgroundColor = if (usesOpacityWhenDisabled) {
        enabledFill.copy(alpha = if (clickable) 1f else 0.7f)
    } else {
        if (clickable) enabledFill else disabledFill
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(enabled = clickable, onClick = onClick)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = title,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontFamily = Typography.Manrope,
                color = Color.White
            )
        }
    }
}

@Composable
fun GoogleButton(
    isBusy: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = Copy.entryGoogleIdle
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF747775), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = !isBusy, onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    color = AuthColor.ink,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.google_g),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = if (isBusy) Copy.entryGoogleBusy else title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Typography.Manrope,
                color = AuthColor.ink.copy(alpha = if (isBusy) 0.6f else 1f)
            )
        }
    }
}

@Composable
fun AuthOrDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthColor.hairline)
        )
        Text(
            text = Copy.entryDivider,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = Typography.Manrope,
            color = AuthColor.dividerText
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AuthColor.hairline)
        )
    }
}

@Composable
fun AuthLegalText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 13.sp,
    color: Color = AuthColor.subheading,
    emphasisColor: Color = AuthColor.ink,
    trailingPeriod: Boolean = false,
    showsSupport: Boolean = true
) {
    val context = LocalContext.current

    val openCustomTab: (String) -> Unit = { url ->
        try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                .launchUrl(context, url.toUri())
        } catch (_: Exception) {}
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(fontSize.value.dp * 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        val annotatedAgreement = buildAnnotatedString {
            append(Copy.legal)
            pushStringAnnotation(tag = "URL", annotation = LegalLinks.terms)
            withStyle(
                SpanStyle(
                    color = emphasisColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(Copy.legalTerms)
            }
            pop()
            append(Copy.legalAnd)
            pushStringAnnotation(tag = "URL", annotation = LegalLinks.privacy)
            withStyle(
                SpanStyle(
                    color = emphasisColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(Copy.legalPrivacy)
            }
            pop()
            if (trailingPeriod) append(".")
        }

        @Suppress("DEPRECATION")
        androidx.compose.foundation.text.ClickableText(
            text = annotatedAgreement,
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = Typography.Manrope,
                color = color,
                lineHeight = fontSize * 1.5
            ),
            onClick = { offset ->
                annotatedAgreement.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { openCustomTab(it.item) }
            }
        )

        if (showsSupport) {
            val annotatedSupport = buildAnnotatedString {
                append(Copy.legalSupportLead)
                pushStringAnnotation(tag = "URL", annotation = LegalLinks.support)
                withStyle(
                    SpanStyle(
                        color = emphasisColor,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(Copy.legalSupport)
                }
                pop()
                if (trailingPeriod) append(".")
            }

            @Suppress("DEPRECATION")
            androidx.compose.foundation.text.ClickableText(
                text = annotatedSupport,
                style = TextStyle(
                    fontSize = fontSize,
                    fontFamily = Typography.Manrope,
                    color = color,
                    lineHeight = fontSize * 1.5
                ),
                onClick = { offset ->
                    annotatedSupport.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { openCustomTab(it.item) }
                }
            )
        }
    }
}

@Composable
fun SuccessCheckCircle(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .background(Color(0xFFDCFCE7), CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF16A34A),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun InvitationBanner(
    wholesaler: String?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF15803D).copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF15803D).copy(alpha = 0.22f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF15803D),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = wholesaler?.let { Copy.invitedBy(it) } ?: Copy.invitationDetected,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.Manrope,
                color = Palette.foreground
            )
            Text(
                text = Copy.invitationNext,
                fontSize = 12.sp,
                fontFamily = Typography.Manrope,
                color = Palette.muted
            )
        }
    }
}
