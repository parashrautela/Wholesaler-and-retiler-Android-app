// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/SignInView.swift
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.Typography
import com.jewelindia.app.navigation.AuthRoute
import com.jewelindia.app.navigation.EntryMode
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    identity: String,
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    AuthLayout(
        title = Copy.signInHeading,
        subtitle = Copy.signInSubheading,
        onBack = onBack
    ) {
        // Read-only identity chip with Change action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuthFieldLabel(text = Copy.signInIdentityLabel)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = Copy.signInChange,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.focusBorder,
                    modifier = Modifier
                        .clickable {
                            signupFlow.clearIdentity()
                            onNavigate(AuthRoute.Entry(EntryMode.SignIn))
                        }
                        .padding(bottom = 8.dp)
                )
            }

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(AuthColor.fill2, RoundedCornerShape(8.dp))
                    .border(1.5.dp, AuthColor.border2, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = if (identity.all { it.isDigit() } && identity.isNotEmpty()) "+91 $identity" else identity,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.text2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        AuthFieldLabel(text = Copy.signInPasswordLabel)
        AuthSecureField(
            value = password,
            onValueChange = {
                password = it
                if (error != null) error = null
            },
            placeholder = Copy.signInPasswordPlaceholder
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        ) {
            // Remember me checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (rememberMe) AuthColor.slate else Color.Transparent,
                            RoundedCornerShape(3.dp)
                        )
                        .border(1.dp, AuthColor.border2, RoundedCornerShape(3.dp))
                ) {
                    if (rememberMe) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = Copy.signInRemember,
                    fontSize = 14.sp,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.muted2
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = Copy.signInForgot,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Typography.Manrope,
                color = AuthColor.focusBorder,
                modifier = Modifier.clickable { onNavigate(AuthRoute.ForgotPassword) }
            )
        }

        if (error != null) {
            AuthErrorRow(
                message = error!!,
                dotSize = 4.dp,
                dotTopPadding = 6.dp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        AuthPrimaryButton(
            title = if (loading) Copy.signInSubmitBusy else Copy.signInSubmitIdle,
            height = 48.dp,
            cornerRadius = 12.dp,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            enabledFill = AuthColor.slate,
            usesOpacityWhenDisabled = true,
            isEnabled = !loading && password.isNotEmpty(),
            isLoading = loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    val msg = sessionStore.signIn(identity, password)
                    if (msg != null) {
                        error = msg
                    }
                    loading = false
                }
            },
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(bottom = 16.dp)
        )

        AuthLegalText(
            fontSize = 13.sp,
            emphasisColor = AuthColor.focusBorder,
            trailingPeriod = true,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
