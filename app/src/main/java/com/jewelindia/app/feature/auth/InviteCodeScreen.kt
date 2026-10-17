// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/InviteCodeView.swift
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.InviteApi
import com.jewelindia.app.data.api.InviteCheck
import com.jewelindia.app.design.Typography
import com.jewelindia.app.navigation.AuthRoute
import com.jewelindia.app.navigation.EntryMode
import kotlinx.coroutines.delay

sealed interface InviteCodeCheck {
    data object Idle : InviteCodeCheck
    data object Checking : InviteCodeCheck
    data class Valid(val code: String, val wholesaler: String?) : InviteCodeCheck
    data class Invalid(val message: String) : InviteCodeCheck
    data object Failed : InviteCodeCheck

    val isValid: Boolean get() = this is Valid
}

@Composable
fun InviteCodeScreen(
    signupFlow: SignupFlow,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    onValid: ((String) -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var code by remember { mutableStateOf(signupFlow.referralCode ?: "") }
    var check by remember { mutableStateOf<InviteCodeCheck>(InviteCodeCheck.Idle) }

    AuthLayout(
        title = Copy.inviteHeading,
        subtitle = Copy.inviteSubheading,
        onBack = onBack
    ) {
        InviteCodeField(
            code = code,
            onCodeChange = { code = it },
            check = check,
            onCheckChange = { check = it },
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthPrimaryButton(
            title = Copy.inviteContinue,
            isEnabled = check.isValid,
            onClick = {
                val validState = check as? InviteCodeCheck.Valid ?: return@AuthPrimaryButton
                signupFlow.referralCode = validState.code
                signupFlow.referralRole = UserRole.retailer
                signupFlow.invitedBy = validState.wholesaler

                if (onValid != null) {
                    onValid(validState.code)
                } else {
                    signupFlow.chosenRole = UserRole.retailer
                    onNavigate(AuthRoute.Entry(EntryMode.Signup(UserRole.retailer)))
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = Copy.inviteNoCode,
            fontSize = 13.sp,
            fontFamily = Typography.Manrope,
            color = AuthColor.muted2,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (onBack != null) onBack() else onNavigate(AuthRoute.Entry(EntryMode.SignIn))
                }
                .padding(bottom = 16.dp)
        )

        AuthLegalText()
    }
}

@Composable
fun InviteCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    check: InviteCodeCheck,
    onCheckChange: (InviteCodeCheck) -> Unit,
    modifier: Modifier = Modifier,
    label: String = Copy.inviteFieldLabel
) {
    LaunchedEffect(code) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            onCheckChange(InviteCodeCheck.Idle)
            return@LaunchedEffect
        }
        onCheckChange(InviteCodeCheck.Checking)
        delay(400) // 400ms debounce
        try {
            when (val result = InviteApi.validate(trimmed)) {
                is InviteCheck.Valid -> onCheckChange(InviteCodeCheck.Valid(result.code, result.wholesaler))
                is InviteCheck.Invalid -> onCheckChange(InviteCodeCheck.Invalid(result.message))
            }
        } catch (_: Exception) {
            onCheckChange(InviteCodeCheck.Failed)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AuthFieldLabel(text = label)

        AuthTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = Copy.inviteFieldPlaceholder,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            when (check) {
                is InviteCodeCheck.Idle -> {}
                is InviteCodeCheck.Checking -> {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = Copy.inviteChecking,
                        fontSize = 13.sp,
                        fontFamily = Typography.Manrope,
                        color = AuthColor.muted2
                    )
                }
                is InviteCodeCheck.Valid -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = check.wholesaler?.let { Copy.invitedBy(it) } ?: Copy.inviteValid,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Typography.Manrope,
                        color = Color(0xFF15803D)
                    )
                }
                is InviteCodeCheck.Invalid -> {
                    AuthErrorRow(message = check.message)
                }
                is InviteCodeCheck.Failed -> {
                    AuthErrorRow(message = Copy.networkError)
                }
            }
        }
    }
}
