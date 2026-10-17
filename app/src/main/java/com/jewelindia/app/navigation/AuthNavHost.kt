// Port of: wholesaler ios/JewelIndia/Sources/Navigation/AuthFlowView.swift
package com.jewelindia.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.session.AuthRouter
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.feature.auth.EmployeeSignInScreen
import com.jewelindia.app.feature.auth.EntryScreen
import com.jewelindia.app.feature.auth.ForgotPasswordScreen
import com.jewelindia.app.feature.auth.InviteCodeScreen
import com.jewelindia.app.feature.auth.RoleChoiceScreen
import com.jewelindia.app.feature.auth.SetPasswordScreen
import com.jewelindia.app.feature.auth.SignInScreen
import com.jewelindia.app.feature.auth.UpdatePasswordScreen
import com.jewelindia.app.feature.auth.VerifyOtpScreen

@Composable
fun AuthNavHost(
    destination: AppDestination,
    sessionStore: SessionStore,
    signupFlow: SignupFlow,
    authRouter: AuthRouter,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(destination) {
        when (destination) {
            is AppDestination.SignIn -> {
                navController.navigate(AuthRoute.Entry(EntryMode.SignIn)) {
                    popUpTo(AuthRoute.RoleChoice)
                }
                navController.navigate(AuthRoute.SignIn(destination.identity))
            }
            is AppDestination.EmployeeLogin -> {
                navController.navigate(AuthRoute.EmployeeSignIn) {
                    popUpTo(AuthRoute.RoleChoice)
                }
            }
            is AppDestination.UpdatePassword -> {
                navController.navigate(AuthRoute.UpdatePassword) {
                    popUpTo(AuthRoute.RoleChoice)
                }
            }
            else -> {
                navController.popBackStack(AuthRoute.RoleChoice, inclusive = false)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AuthRoute.RoleChoice,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() },
        modifier = modifier
    ) {
        composable<AuthRoute.RoleChoice> {
            val initialError = (destination as? AppDestination.Entry)?.error
            RoleChoiceScreen(
                sessionStore = sessionStore,
                signupFlow = signupFlow,
                onNavigate = { route -> navController.navigate(route) },
                initialError = initialError
            )
        }

        composable<AuthRoute.InviteCode> {
            InviteCodeScreen(
                signupFlow = signupFlow,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.EmployeeSignIn> {
            val initialError = (destination as? AppDestination.EmployeeLogin)?.error
            EmployeeSignInScreen(
                sessionStore = sessionStore,
                onNavigate = { route -> navController.navigate(route) },
                initialError = initialError,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.Entry> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.Entry>()
            EntryScreen(
                mode = route.mode,
                sessionStore = sessionStore,
                signupFlow = signupFlow,
                authRouter = authRouter,
                onNavigate = { nextRoute -> navController.navigate(nextRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.SignIn> { backStackEntry ->
            val route = backStackEntry.toRoute<AuthRoute.SignIn>()
            SignInScreen(
                identity = route.identity,
                sessionStore = sessionStore,
                signupFlow = signupFlow,
                onNavigate = { nextRoute -> navController.navigate(nextRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.VerifyOtp> {
            VerifyOtpScreen(
                sessionStore = sessionStore,
                signupFlow = signupFlow,
                onNavigate = { nextRoute -> navController.navigate(nextRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.SetPassword> {
            SetPasswordScreen(
                sessionStore = sessionStore,
                signupFlow = signupFlow,
                onPasswordSet = { scope.launch { sessionStore.refreshDestination() } }
            )
        }

        composable<AuthRoute.ForgotPassword> {
            ForgotPasswordScreen(
                sessionStore = sessionStore,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AuthRoute.UpdatePassword> {
            UpdatePasswordScreen(
                sessionStore = sessionStore,
                onPasswordUpdated = { scope.launch { sessionStore.refreshDestination() } }
            )
        }
    }
}
