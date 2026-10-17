// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/AuthKit.swift
package com.jewelindia.app.feature.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Responsive breakpoints matching Tailwind specs used in iOS.
 */
object Breakpoint {
    val sm = 640.dp
    val md = 768.dp
    val lg = 1024.dp
    val xl = 1280.dp
}

/**
 * Palette local to the auth flow matching AuthKit.swift.
 */
object AuthColor {
    val brandSquare = Color(0xFF6B4F4F)
    val ink = Color(0xFF111111)
    val subheading = Color(0xFF4B5563)
    val labelDark = Color(0xFF333333)
    val fieldBorder = Color(0xFFD9D0C5)
    val fieldFill = Color(0xFFFAFAFA)
    val hairline = Color(0xFFE0E0E0)
    val dividerText = Color(0xFF999999)
    val buttonEnabled = Color(0xFF1A1A1A)
    val buttonDisabled = Color(0xFFBBBBBB)
    val errorDot = Color(0xFFEF4444)
    val errorText = Color(0xFFDC2626)

    val slate = Color(0xFF1F2937)
    val slateHover = Color(0xFF111827)
    val border2 = Color(0xFFE5E7EB)
    val fill2 = Color(0xFFF9FAFB)
    val text2 = Color(0xFF111827)
    val placeholder = Color(0xFF9CA3AF)
    val muted2 = Color(0xFF6B7280)
    val focusBorder = Color(0xFF374151)
    val otpBorder = Color(0xFFD1D5DB)
    val rulePass = Color(0xFF16A34A)
}

/**
 * Responsive auth layout shell.
 * On wide screens (>= 768dp), editorial photography appears on the left,
 * with the form taking 480-520dp on the right.
 */
@Composable
fun AuthLayout(
    title: String? = null,
    subtitle: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
    topTrailing: (@Composable () -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val isWide = maxWidth >= Breakpoint.md
        val formWidth = if (!isWide) maxWidth else (maxWidth * 0.45f).coerceIn(480.dp, 520.dp)
        val imageWidth = if (isWide) maxWidth - formWidth else 0.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWide && imageWidth > 0.dp) {
                Image(
                    painter = painterResource(id = R.drawable.auth_panel_image),
                    contentDescription = "Jewellery",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                )
            }

            Column(
                modifier = Modifier
                    .width(formWidth)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = if (isWide) 32.dp else 24.dp,
                        vertical = if (isWide) 40.dp else 28.dp
                    )
            ) {
                // Brand row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (isWide) 28.dp else 22.dp)
                ) {
                    if (onBack != null) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, AuthColor.border2, CircleShape)
                                .clip(CircleShape)
                                .clickable { onBack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AuthColor.ink,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Brand square with gold border
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .background(AuthColor.brandSquare, RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xD4AF37).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = Copy.brandMark,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Typography.Cirka
                        )
                    }

                    Text(
                        text = Copy.brandWordmark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthColor.ink,
                        fontFamily = Typography.Manrope
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    topTrailing?.invoke()
                }

                // Title
                if (titleContent != null) {
                    titleContent()
                } else if (title != null) {
                    Text(
                        text = title,
                        fontSize = if (isWide) 40.sp else 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Typography.Cirka,
                        color = AuthColor.ink,
                        lineHeight = if (isWide) 44.sp else 38.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Subtitle
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 15.sp,
                        color = AuthColor.subheading,
                        fontFamily = Typography.Manrope,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }

                content()
            }
        }
    }
}

@Composable
fun AuthFieldLabel(
    text: String,
    size: TextUnit = 13.sp,
    color: Color = AuthColor.labelDark
) {
    Text(
        text = text,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = color,
        fontFamily = Typography.Manrope,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    fill: Color = AuthColor.fieldFill,
    idleBorder: Color = AuthColor.fieldBorder,
    focusBorder: Color = AuthColor.ink,
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 8.dp,
    fontSize: TextUnit = 14.sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(
            fontSize = fontSize,
            fontFamily = Typography.Manrope,
            color = AuthColor.ink
        ),
        cursorBrush = SolidColor(AuthColor.ink),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(fill, RoundedCornerShape(cornerRadius))
            .border(
                borderWidth,
                if (isFocused) focusBorder else idleBorder,
                RoundedCornerShape(cornerRadius)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 14.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = fontSize,
                        fontFamily = Typography.Manrope,
                        color = AuthColor.placeholder
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun AuthSecureField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    fontSize: TextUnit = 15.sp,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    var revealed by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = TextStyle(
            fontSize = fontSize,
            fontFamily = Typography.Manrope,
            color = AuthColor.text2
        ),
        cursorBrush = SolidColor(AuthColor.focusBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(
                1.5.dp,
                if (isFocused) AuthColor.focusBorder else AuthColor.border2,
                RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(start = 12.dp, end = 4.dp),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = fontSize,
                            fontFamily = Typography.Manrope,
                            color = AuthColor.placeholder
                        )
                    }
                    innerTextField()
                }

                IconButton(
                    onClick = { revealed = !revealed },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Show password",
                        tint = AuthColor.placeholder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun AuthErrorRow(
    message: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 5.dp,
    dotTopPadding: Dp = 5.dp,
    fontSize: TextUnit = 12.sp
) {
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = dotTopPadding)
                    .size(dotSize)
                    .background(AuthColor.errorDot, CircleShape)
            )
            Text(
                text = message,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                fontFamily = Typography.Manrope,
                color = AuthColor.errorText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

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
                .launchUrl(context, Uri.parse(url))
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
