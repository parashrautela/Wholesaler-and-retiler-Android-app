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
                            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
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
