// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/VerifyOTPView.swift
package com.jewelindia.app.feature.auth

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.net.ApiError
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.design.Typography
import com.jewelindia.app.navigation.AuthRoute
import com.jewelindia.app.navigation.EntryMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyOtpScreen(
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val identity = signupFlow.identity ?: ""

    var code by remember { mutableStateOf("") }
    var isWrongOTP by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var verifying by remember { mutableStateOf(false) }
    var resending by remember { mutableStateOf(false) }

    var secondsLeft by remember { mutableIntStateOf(signupFlow.otpSecondsLeft) }
    var resendCooldown by remember { mutableIntStateOf(0) }
    var showStopwatch by remember { mutableStateOf(signupFlow.otpSecondsLeft > 0) }

    val focusRequester = remember { FocusRequester() }

    // Live countdown timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (secondsLeft > 0) secondsLeft -= 1
            if (resendCooldown > 0) resendCooldown -= 1
        }
    }

    LaunchedEffect(Unit) {
        if (identity.isEmpty()) {
            onNavigate(AuthRoute.Entry(EntryMode.SignIn))
            return@LaunchedEffect
        }
        if (signupFlow.isLocked) {
            showStopwatch = false
        } else {
            secondsLeft = signupFlow.otpSecondsLeft
            showStopwatch = secondsLeft > 0
        }
        delay(200)
        focusRequester.requestFocus()
    }

    val formatSeconds: (Int) -> String = { sec ->
        String.format(Locale.ROOT, "%02d:%02d", sec / 60, sec % 60)
    }

    AuthLayout(
        titleContent = {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = Copy.otpHeadingLine1,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Typography.Cirka,
                    color = AuthColor.ink
                )
                Text(
                    text = if (signupFlow.signupRole == UserRole.retailer) {
                        Copy.otpHeadingRetailer
                    } else {
                        Copy.otpHeadingLine2
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Typography.Cirka,
                    color = AuthColor.ink
                )
            }
        },
        onBack = onBack
    ) {
        // Subline with edit action
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = Copy.otpSentPrefix,
                fontSize = 14.sp,
                fontFamily = Typography.Manrope,
                color = AuthColor.placeholder
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = identity,
                    fontSize = 14.sp,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.text2
                )
                Text(
                    text = Copy.otpEdit,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.text2,
                    modifier = Modifier.clickable {
                        onNavigate(AuthRoute.Entry(EntryMode.SignIn))
                    }
                )
            }
        }

        OtpBoxes(
            value = code,
            onValueChange = {
                code = it
                if (error != null) error = null
                isWrongOTP = false
            },
            isWrong = isWrongOTP,
            focusRequester = focusRequester
        )

        if (error != null) {
            Text(
                text = error!!,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.Manrope,
                color = AuthColor.errorText,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Lockout banner or countdown timer
        if (signupFlow.isLocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFECACA), RectangleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = Copy.otpLocked.uppercase(),
                    fontSize = 11.sp,
                    letterSpacing = 1.1.sp,
                    fontFamily = Typography.Manrope,
                    color = Color(0xFFDC2626)
                )
            }
        } else if (showStopwatch) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Copy.otpValidPrefix,
                        fontSize = 13.sp,
                        fontFamily = Typography.Manrope,
                        color = AuthColor.placeholder
                    )
                    Text(
                        text = formatSeconds(secondsLeft),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = AuthColor.text2
                    )
                }

                Text(
                    text = if (resending) Copy.otpResendBusy else Copy.otpResendIdle,
                    fontSize = 13.sp,
                    fontWeight = if (secondsLeft > 0) FontWeight.Normal else FontWeight.Bold,
                    fontFamily = Typography.Manrope,
                    color = if (secondsLeft > 0) AuthColor.otpBorder else AuthColor.text2,
                    modifier = Modifier.clickable(
                        enabled = !resending && secondsLeft <= 0 && signupFlow.remainingResends > 0 && !signupFlow.isLocked
                    ) {
                        scope.launch {
                            resending = true
                            try {
                                val result = JewelApi.sendOtp(identity)
                                signupFlow.otpSentAt = System.currentTimeMillis()
                                signupFlow.remainingResends = result.remainingResends
                                    ?: maxOf(0, signupFlow.remainingResends - 1)
                                secondsLeft = Copy.otpValiditySeconds
                                showStopwatch = true
                                isWrongOTP = false
                                code = ""
                                resendCooldown = Copy.otpResendCooldownSeconds
                                error = null
                            } catch (e: ApiError) {
                                if (e.locked) {
                                    signupFlow.lockedUntil = e.lockedUntil
                                    signupFlow.remainingResends = 0
                                } else if (e.cooldown) {
                                    resendCooldown = e.waitSeconds ?: Copy.otpResendCooldownSeconds
                                }
                                error = e.userMessage()
                            } catch (e: Exception) {
                                error = Copy.networkError
                            } finally {
                                resending = false
                            }
                        }
                    }
                )
            }
        }

        // Info note
        Text(
            text = if (signupFlow.signupRole == UserRole.retailer) {
                Copy.otpInfoNoteRetailer
            } else {
                Copy.otpInfoNoteWholesaler
            },
            fontSize = 12.sp,
            fontFamily = Typography.Manrope,
            color = AuthColor.placeholder,
            lineHeight = 16.sp,
            modifier = Modifier
                .padding(top = 40.dp)
                .padding(bottom = 20.dp)
        )

        AuthPrimaryButton(
            title = if (verifying) Copy.otpSubmitBusy else Copy.otpSubmitIdle,
            height = 48.dp,
            cornerRadius = 12.dp,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            enabledFill = AuthColor.slate,
            usesOpacityWhenDisabled = true,
            isEnabled = code.length == Copy.otpDigitCount && !verifying,
            isLoading = verifying,
            onClick = {
                if (code.length != Copy.otpDigitCount) {
                    error = Copy.otpIncomplete
                    return@AuthPrimaryButton
                }

                scope.launch {
                    verifying = true
                    error = null
                    SignupFlow.isCompletingSignup = true

                    try {
                        val result = JewelApi.verifyOtp(
                            identity = identity,
                            token = code,
                            referralCode = signupFlow.referralCode
                        )

                        if (result.isNewUser) {
                            verifying = false
                            onNavigate(AuthRoute.SetPassword)
                            return@launch
                        }

                        // Returning user
                        SignupFlow.isCompletingSignup = false
                        sessionStore.refreshDestination()
                        verifying = false
                    } catch (e: ApiError) {
                        SignupFlow.isCompletingSignup = false
                        isWrongOTP = true
                        showStopwatch = false
                        error = e.userMessage().ifEmpty { Copy.otpWrong }
                        code = ""
                        verifying = false
                    } catch (e: Exception) {
                        SignupFlow.isCompletingSignup = false
                        isWrongOTP = true
                        showStopwatch = false
                        error = Copy.otpGenericError
                        code = ""
                        verifying = false
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AuthLegalText(
            fontSize = 13.sp,
            emphasisColor = AuthColor.focusBorder,
            trailingPeriod = true,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
