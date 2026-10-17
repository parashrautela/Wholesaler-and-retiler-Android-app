// Port of: wholesaler ios/JewelIndia/Sources/Features/RetailerOnboarding/RetailerOnboardCoordinator.swift
package com.jewelindia.app.feature.retailer_onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import kotlinx.coroutines.launch

private enum class RetailerStep {
    ONE, TWO, THREE
}

@Composable
fun RetailerOnboardCoordinator(
    sessionStore: SessionStore,
    signupFlow: SignupFlow
) {
    val flow: RetailerOnboardFlow = viewModel()
    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf(RetailerStep.ONE) }
    val currentStep = backStack.lastOrNull() ?: RetailerStep.ONE

    BackHandler(enabled = backStack.size > 1) {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                slideOutHorizontally { width -> -width } + fadeOut()
            )
        },
        label = "retailer_onboard_transition"
    ) { step ->
        when (step) {
            RetailerStep.ONE -> {
                RetailerOnboardStep1Screen(
                    flow = flow,
                    onNext = { backStack.add(RetailerStep.TWO) },
                    onBack = {
                        scope.launch { sessionStore.returnToRoleChoice() }
                    },
                    onSignOut = {
                        scope.launch { sessionStore.signOut() }
                    }
                )
            }
            RetailerStep.TWO -> {
                RetailerOnboardStep2Screen(
                    flow = flow,
                    onNext = { backStack.add(RetailerStep.THREE) },
                    onBack = { backStack.removeAt(backStack.size - 1) },
                    onSignOut = {
                        scope.launch { sessionStore.signOut() }
                    }
                )
            }
            RetailerStep.THREE -> {
                RetailerOnboardStep3Screen(
                    flow = flow,
                    session = sessionStore,
                    signupFlow = signupFlow,
                    onCompleted = {
                        scope.launch { sessionStore.refreshDestination() }
                    },
                    onBack = { backStack.removeAt(backStack.size - 1) },
                    onSignOut = {
                        scope.launch { sessionStore.signOut() }
                    }
                )
            }
        }
    }
}
