// Port of: wholesaler ios/JewelIndia/Sources/Navigation/RootView.swift
package com.jewelindia.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.session.AuthRouter
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import androidx.compose.foundation.clickable
import com.jewelindia.app.feature.auth.AuthPrimaryButton
import com.jewelindia.app.feature.auth.RoleChoiceScreen
import com.jewelindia.app.feature.auth.UnreachableScreen
import com.jewelindia.app.feature.auth.UpdatePasswordScreen
import kotlinx.coroutines.launch

@Composable
fun LaunchScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Jewel India",
            style = Typography.cirka(32.sp),
            color = Palette.goldPrimary
        )
    }
}

@Composable
fun PhaseGatePlaceholder(
    title: String,
    note: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Palette.surfaceElevated)
                .padding(24.dp)
        ) {
            Text(
                text = title,
                style = Typography.cirka(24.sp),
                color = Palette.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = note,
                style = Typography.santoshi(14.sp),
                color = Palette.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (onRefresh != null) {
                AuthPrimaryButton(
                    title = "Check Status",
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSignOut)
            ) {
                Text(
                    text = "Sign Out",
                    style = Typography.santoshi(15.sp),
                    color = Palette.accentRed
                )
            }
        }
    }
}

@Composable
fun RootScreen(
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    authRouter: AuthRouter,
    modifier: Modifier = Modifier
) {
    val phase by sessionStore.phase.collectAsState()
    val scope = rememberCoroutineScope()

    AnimatedContent(
        targetState = phase,
        transitionSpec = {
            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
        },
        label = "RootPhaseTransition",
        modifier = modifier
    ) { currentPhase ->
        when (currentPhase) {
            is SessionStore.Phase.Loading -> {
                LaunchScreen()
            }
            is SessionStore.Phase.Unauthenticated -> {
                AuthNavHost(
                    destination = currentPhase.destination,
                    sessionStore = sessionStore,
                    signupFlow = signupFlow,
                    authRouter = authRouter
                )
            }
            is SessionStore.Phase.Authenticated -> {
                when (val dest = currentPhase.destination) {
                    is AppDestination.SelectRole -> {
                        RoleChoiceScreen(
                            sessionStore = sessionStore,
                            signupFlow = signupFlow,
                            onNavigate = {},
                            signedIn = true
                        )
                    }
                    is AppDestination.Unreachable -> {
                        UnreachableScreen(sessionStore = sessionStore)
                    }
                    is AppDestination.WholesalerOnboarding -> {
                        PhaseGatePlaceholder(
                            title = "Wholesaler Application",
                            note = "Wholesaler onboarding workflow will be available in Phase 4.",
                            onSignOut = { scope.launch { sessionStore.signOut() } }
                        )
                    }
                    is AppDestination.WholesalerSubmitted -> {
                        PhaseGatePlaceholder(
                            title = "Wholesaler Application Submitted",
                            note = "Your application is currently under review by Jewel India admin.",
                            onSignOut = { scope.launch { sessionStore.signOut() } },
                            onRefresh = { scope.launch { sessionStore.refreshDestination() } }
                        )
                    }
                    is AppDestination.WholesalerDashboard -> {
                        PhaseGatePlaceholder(
                            title = "Wholesaler Dashboard",
                            note = "Wholesaler catalogue and portal will be available in Phase 5.",
                            onSignOut = { scope.launch { sessionStore.signOut() } }
                        )
                    }
                    is AppDestination.RetailerOnboarding -> {
                        PhaseGatePlaceholder(
                            title = "Retailer Application",
                            note = "Retailer onboarding workflow will be available in Phase 4.",
                            onSignOut = { scope.launch { sessionStore.signOut() } }
                        )
                    }
                    is AppDestination.RetailerSubmitted -> {
                        PhaseGatePlaceholder(
                            title = "Retailer Application Submitted",
                            note = "Your application is currently under review by Jewel India admin.",
                            onSignOut = { scope.launch { sessionStore.signOut() } },
                            onRefresh = { scope.launch { sessionStore.refreshDestination() } }
                        )
                    }
                    is AppDestination.RetailerDashboard -> {
                        PhaseGatePlaceholder(
                            title = "Retailer Dashboard",
                            note = "Retailer catalogue and store will be available in Phase 6.",
                            onSignOut = { scope.launch { sessionStore.signOut() } }
                        )
                    }
                    is AppDestination.EmployeeDashboard -> {
                        PhaseGatePlaceholder(
                            title = "Employee Dashboard",
                            note = "Employee operations dashboard will be available in Phase 8.",
                            onSignOut = { scope.launch { sessionStore.signOut() } }
                        )
                    }
                    is AppDestination.UpdatePassword -> {
                        UpdatePasswordScreen(
                            sessionStore = sessionStore,
                            onPasswordUpdated = { scope.launch { sessionStore.refreshDestination() } }
                        )
                    }
                    else -> {
                        AuthNavHost(
                            destination = dest,
                            sessionStore = sessionStore,
                            signupFlow = signupFlow,
                            authRouter = authRouter
                        )
                    }
                }
            }
        }
    }
}
