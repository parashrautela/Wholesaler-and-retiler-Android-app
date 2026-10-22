// Port of: wholesaler ios/wholesaler/Features/Auth/ForgotPasswordView.swift
//      and: wholesaler ios/wholesaler/Features/Auth/UpdatePasswordView.swift
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    sessionStore: SessionStore,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val emailValid = email.trim().contains("@") && email.trim().contains(".")
    val canSubmit = emailValid && !isLoading

    AuthLayout(
        title = if (isSubmitted) null else "Reset Password",
        subtitle = if (isSubmitted) null else "Enter your registered email address and we'll send you instructions to reset your password.",
        onBack = onBackClick
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isSubmitted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SuccessCheckCircle()

                    Text(
                        text = "Check Your Email",
                        style = Typography.cirka(26.sp),
                        color = Palette.textPrimary
                    )

                    Text(
                        text = "We have sent password reset instructions to\n${email.trim()}",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthPrimaryButton(
                        title = "Back to Sign In",
                        onClick = onBackClick
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuthFieldLabel("Email Address")
                    AuthTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        placeholder = "name@company.com",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading
                    )
                }

                errorMessage?.let { AuthErrorRow(message = it) }

                AuthPrimaryButton(
                    title = "Send Reset Link",
                    onClick = {
                        if (!canSubmit) return@AuthPrimaryButton
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                sessionStore.requestPasswordReset(email.trim())
                                isSubmitted = true
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to send reset email. Please try again."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    isEnabled = canSubmit,
                    isLoading = isLoading
                )
            }
        }
    }
}

private data class PwdRule(val label: String, val check: (String) -> Boolean)

private val updateRules = listOf(
    PwdRule("At least 8 characters") { it.length >= 8 },
    PwdRule("At least one uppercase letter") { it.any { ch -> ch.isUpperCase() } },
    PwdRule("At least one lowercase letter") { it.any { ch -> ch.isLowerCase() } },
    PwdRule("At least one number") { it.any { ch -> ch.isDigit() } }
)

@Composable
fun UpdatePasswordScreen(
    sessionStore: SessionStore,
    onPasswordUpdated: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val allRulesPassed = updateRules.all { it.check(password) }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val canSubmit = allRulesPassed && passwordsMatch && !isLoading

    AuthLayout(
        title = "Update Password",
        subtitle = "Enter a new password for your Jewel India account.",
        onBack = {
            sessionStore.exitPasswordRecovery()
        }
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthFieldLabel("New Password")
                AuthSecureField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = "Enter new password",
                    enabled = !isLoading
                )
            }

            // Checklist of rules
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Palette.surfaceElevated)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password requirements:",
                    style = Typography.santoshi(12.sp),
                    color = Palette.textSecondary
                )
                updateRules.forEach { rule ->
                    val passed = rule.check(password)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .then(
                                    if (passed) Modifier.background(Palette.accentGreen)
                                    else Modifier.border(1.dp, Palette.borderSubtle, CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (passed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Palette.white,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                        Text(
                            text = rule.label,
                            style = Typography.santoshi(13.sp),
                            color = if (passed) Palette.textPrimary else Palette.textMuted
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthFieldLabel("Confirm Password")
                AuthSecureField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    placeholder = "Re-enter new password",
                    enabled = !isLoading
                )
                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Text(
                        text = "Passwords do not match",
                        style = Typography.santoshi(12.sp),
                        color = Palette.accentRed
                    )
                }
            }

            errorMessage?.let { AuthErrorRow(message = it) }

            AuthPrimaryButton(
                title = "Update Password",
                onClick = {
                    if (!canSubmit) return@AuthPrimaryButton
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            sessionStore.updatePassword(password)
                            sessionStore.exitPasswordRecovery()
                            sessionStore.refreshDestination()
                            onPasswordUpdated()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to update password. Please try again."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                isEnabled = canSubmit,
                isLoading = isLoading
            )
        }
    }
}
