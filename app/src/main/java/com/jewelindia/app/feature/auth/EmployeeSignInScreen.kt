// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/EmployeeSignInView.swift
package com.jewelindia.app.feature.auth

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.StaffAccountsApi
import com.jewelindia.app.design.Typography
import com.jewelindia.app.navigation.AuthRoute
import kotlinx.coroutines.launch

@Composable
fun EmployeeSignInScreen(
    sessionStore: SessionStore,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    initialError: String? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(initialError) }
    var loading by remember { mutableStateOf(false) }
    var googleLoading by remember { mutableStateOf(false) }

    LaunchedEffect(initialError) {
        if (error == null) error = initialError
    }

    AuthLayout(
        titleContent = {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = Copy.employeeHeadingLine1,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Typography.Cirka,
                    color = AuthColor.ink
                )
                Text(
                    text = Copy.employeeHeadingLine2,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Typography.Cirka,
                    color = AuthColor.ink
                )
            }
        },
        subtitle = Copy.staffSubheading,
        onBack = onBack
    ) {
        AuthFieldLabel(text = Copy.staffUsernameLabel)
        AuthTextField(
            value = username,
            onValueChange = {
                username = it
                if (error != null) error = null
            },
            placeholder = Copy.staffUsernamePlaceholder,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AuthFieldLabel(text = Copy.employeePasswordLabel)
        AuthSecureField(
            value = password,
            onValueChange = {
                password = it
                if (error != null) error = null
            },
            placeholder = Copy.employeePasswordPlaceholder,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = Copy.staffForgotHint,
            fontSize = 12.sp,
            fontFamily = Typography.Manrope,
            color = AuthColor.placeholder,
            lineHeight = 16.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (error != null) {
            AuthErrorRow(
                message = error!!,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        AuthOrDivider()

        GoogleButton(
            isBusy = loading || googleLoading,
            title = Copy.staffGoogle,
            onClick = {
                val activity = context as? Activity ?: return@GoogleButton
                scope.launch {
                    googleLoading = true
                    error = null
                    SignupFlow.isCompletingSignup = true
                    when (val outcome = GoogleSignIn.signIn(activity)) {
                        GoogleOutcome.Cancelled -> {
                            SignupFlow.isCompletingSignup = false
                            googleLoading = false
                        }
                        is GoogleOutcome.Failed -> {
                            SignupFlow.isCompletingSignup = false
                            error = outcome.message
                            googleLoading = false
                        }
                        GoogleOutcome.SignedIn -> {
                            val msg = sessionStore.claimStaffInvite()
                            if (msg != null) error = msg
                            googleLoading = false
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        AuthPrimaryButton(
            title = if (loading) Copy.employeeSubmitBusy else Copy.employeeSubmitIdle,
            isEnabled = !loading && !googleLoading && username.trim().isNotEmpty() && password.isNotEmpty(),
            isLoading = loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    val identity = StaffAccountsApi.address(username.trim())
                    val msg = sessionStore.signIn(identity, password)
                    if (msg != null) {
                        error = if (msg.contains("invalid login", ignoreCase = true)) {
                            Copy.staffWrongCredentials
                        } else {
                            msg
                        }
                    }
                    loading = false
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AuthLegalText()
    }
}
