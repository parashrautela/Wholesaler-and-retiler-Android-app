// Port of: wholesaler ios/wholesaler/Features/Auth/SetPasswordView.swift
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

private data class Rule(val label: String, val check: (String) -> Boolean)

private val rules = listOf(
    Rule("At least 8 characters") { it.length >= 8 },
    Rule("At least one uppercase letter") { it.any { ch -> ch.isUpperCase() } },
    Rule("At least one lowercase letter") { it.any { ch -> ch.isLowerCase() } },
    Rule("At least one number") { it.any { ch -> ch.isDigit() } }
)

@Composable
fun SetPasswordScreen(
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    onPasswordSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val allRulesPassed = rules.all { it.check(password) }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val canSubmit = allRulesPassed && passwordsMatch && !isLoading

    AuthLayout(
        title = "Create Password",
        subtitle = "Choose a strong password to secure your account.",
        onBack = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AuthFieldLabel("Password")
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
                rules.forEach { rule ->
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
                title = "Set Password",
                onClick = {
                    if (!canSubmit) return@AuthPrimaryButton
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val role = signupFlow.signupRole
                            JewelApi.setPassword(password, role)
                            signupFlow.markSignupCompleted()
                            sessionStore.refreshDestination()
                            onPasswordSet()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to set password. Please try again."
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
