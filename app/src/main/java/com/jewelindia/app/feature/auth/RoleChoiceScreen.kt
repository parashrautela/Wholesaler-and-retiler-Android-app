// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/RoleChoiceView.swift
package com.jewelindia.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.navigation.AuthRoute
import com.jewelindia.app.navigation.EntryMode
import kotlinx.coroutines.launch

@Composable
fun RoleChoiceScreen(
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    onNavigate: (AuthRoute) -> Unit,
    modifier: Modifier = Modifier,
    signedIn: Boolean = false,
    initialError: String? = null
) {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf(initialError) }
    var busyRole by remember { mutableStateOf<UserRole?>(null) }
    var showInviteCodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialError) {
        if (error == null) error = initialError
    }

    AuthLayout(
        title = if (signedIn) Copy.doorsSignedInHeading else Copy.doorsHeading,
        subtitle = Copy.doorsSubheading,
        topTrailing = if (signedIn) null else {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuthColor.fill2)
                        .border(1.dp, AuthColor.border2, CircleShape)
                        .clickable { onNavigate(AuthRoute.Entry(EntryMode.SignIn)) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = Copy.doorsSignIn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Typography.Manrope,
                        color = AuthColor.ink
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AuthColor.ink,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    ) {
        if (!signedIn && signupFlow.referralRole == UserRole.retailer && signupFlow.referralCode != null) {
            InvitationBanner(
                wholesaler = signupFlow.invitedBy,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (!signedIn) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = Copy.doorsHaveAccount,
                    fontSize = 14.sp,
                    fontFamily = Typography.Manrope,
                    color = Color(0xFF4B5563)
                )
                Text(
                    text = Copy.doorsSignIn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Typography.Manrope,
                    color = AuthColor.ink,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onNavigate(AuthRoute.Entry(EntryMode.SignIn)) }
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DoorCard(
                title = Copy.doorWholesalerTitle,
                description = Copy.doorWholesalerBody,
                icon = Icons.Default.Inventory2,
                isFeatured = false,
                isBusy = busyRole == UserRole.wholesaler,
                onClick = {
                    error = null
                    if (signedIn) {
                        scope.launch {
                            busyRole = UserRole.wholesaler
                            val msg = sessionStore.setUserRole(UserRole.wholesaler)
                            busyRole = null
                            if (msg != null) error = msg
                        }
                    } else {
                        signupFlow.chosenRole = UserRole.wholesaler
                        onNavigate(AuthRoute.Entry(EntryMode.Signup(UserRole.wholesaler)))
                    }
                }
            )

            DoorCard(
                title = Copy.doorRetailerTitle,
                description = Copy.doorRetailerBody,
                icon = Icons.Default.Storefront,
                isFeatured = true,
                isBusy = busyRole == UserRole.retailer,
                onClick = {
                    error = null
                    if (signedIn) {
                        showInviteCodeDialog = true
                    } else {
                        onNavigate(AuthRoute.InviteCode)
                    }
                }
            )

            DoorCard(
                title = Copy.doorStaffTitle,
                description = Copy.doorStaffBody,
                icon = Icons.Default.Badge,
                isFeatured = false,
                isBusy = busyRole == UserRole.employee,
                onClick = {
                    error = null
                    if (signedIn) {
                        scope.launch {
                            busyRole = UserRole.employee
                            val msg = sessionStore.claimStaffInvite()
                            busyRole = null
                            if (msg != null) error = msg
                        }
                    } else {
                        onNavigate(AuthRoute.EmployeeSignIn)
                    }
                }
            )
        }

        if (error != null) {
            AuthErrorRow(
                message = error!!,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (signedIn) {
            Text(
                text = Copy.logoutConfirm,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Typography.Manrope,
                color = AuthColor.focusBorder,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { scope.launch { sessionStore.signOut() } }
                    .padding(bottom = 16.dp)
            )
        }

        AuthLegalText()
    }

    if (showInviteCodeDialog) {
        Dialog(onDismissRequest = { showInviteCodeDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                InviteCodeScreen(
                    signupFlow = signupFlow,
                    onNavigate = onNavigate,
                    onValid = { code ->
                        showInviteCodeDialog = false
                        scope.launch {
                            signupFlow.referralCode = code
                            signupFlow.referralRole = UserRole.retailer
                            busyRole = UserRole.retailer
                            val msg = sessionStore.setUserRole(UserRole.retailer)
                            busyRole = null
                            if (msg != null) error = msg
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DoorCard(
    title: String,
    description: String,
    icon: ImageVector,
    isFeatured: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFeatured) Color(0xFFFFFDF7) else Color.White)
            .border(
                width = if (isFeatured) 1.5.dp else 1.dp,
                color = if (isFeatured) Color(0xD4AF37).copy(alpha = 0.45f) else AuthColor.hairline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isBusy, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isFeatured) Color(0xFEF3C7).copy(alpha = 0.55f) else AuthColor.fill2,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isFeatured) Color(0xFF854D0E) else AuthColor.ink,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Typography.Manrope,
                color = AuthColor.ink
            )
            Text(
                text = description,
                fontSize = 13.sp,
                fontFamily = Typography.Manrope,
                color = Color(0xFF4B5563)
            )
        }

        if (isBusy) {
            CircularProgressIndicator(
                color = AuthColor.ink,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
