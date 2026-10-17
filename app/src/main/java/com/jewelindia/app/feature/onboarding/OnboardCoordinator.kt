// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardCoordinator.swift
package com.jewelindia.app.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jewelindia.app.core.session.SessionStore
import kotlinx.coroutines.launch

private enum class WholesalerOnboardStep {
    ONE, TWO, THREE
}

@Composable
fun OnboardCoordinator(
    sessionStore: SessionStore
) {
    val flow: OnboardFlow = viewModel()
    val scope = rememberCoroutineScope()
    val backStack = remember { mutableStateListOf(WholesalerOnboardStep.ONE) }
    val currentStep = backStack.lastOrNull() ?: WholesalerOnboardStep.ONE

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
        label = "wholesaler_onboard_transition"
    ) { step ->
        when (step) {
            WholesalerOnboardStep.ONE -> {
                OnboardStep1Screen(
                    flow = flow,
                    onNext = { backStack.add(WholesalerOnboardStep.TWO) },
                    onBack = {
                        scope.launch { sessionStore.returnToRoleChoice() }
                    },
                    onSignOut = {
                        scope.launch { sessionStore.signOut() }
                    }
                )
            }
            WholesalerOnboardStep.TWO -> {
                OnboardStep2Screen(
                    flow = flow,
                    onNext = { backStack.add(WholesalerOnboardStep.THREE) },
                    onBack = { backStack.removeAt(backStack.size - 1) },
                    onSignOut = {
                        scope.launch { sessionStore.signOut() }
                    }
                )
            }
            WholesalerOnboardStep.THREE -> {
                OnboardStep3Screen(
                    flow = flow,
                    session = sessionStore,
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
