// Port of: wholesaler ios/JewelIndia/Sources/JewelIndiaApp.swift + RootView.onOpenURL
package com.jewelindia.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.jewelindia.app.core.security.LocalScreenSecurity
import com.jewelindia.app.core.security.ScreenSecurity
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.design.JewelTheme
import com.jewelindia.app.navigation.RootScreen
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // §16c.1 App switcher snapshot protection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        } else {
            // On older APIs, permanently set FLAG_SECURE
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        val graph = (application as JewelApplication).graph
        handleIncomingIntent(intent, graph)

        setContent {
            val screenSecurity = remember {
                ScreenSecurity { isSecure ->
                    if (isSecure) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            }

            CompositionLocalProvider(
                LocalAppGraph provides graph,
                LocalScreenSecurity provides screenSecurity
            ) {
                JewelTheme {
                    RootScreen(
                        sessionStore = graph.sessionStore,
                        signupFlow = graph.signupFlow,
                        authRouter = graph.authRouter
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val graph = (application as JewelApplication).graph
        handleIncomingIntent(intent, graph)
    }

    private fun handleIncomingIntent(intent: Intent?, graph: AppGraph) {
        val uri = intent?.data ?: return

        // 1. If unauthenticated, check for retailer invitation link
        if (graph.sessionStore.phase.value is SessionStore.Phase.Unauthenticated) {
            if (graph.signupFlow.captureRetailerInvitation(uri)) {
                return
            }
        }

        // 2. Custom scheme deep links
        if (uri.scheme.equals("jewelindia", ignoreCase = true)) {
            val fragment = uri.fragment ?: ""
            val nextParam = uri.getQueryParameter("next")
            val isRecovery = nextParam == "/update-password" ||
                    fragment.contains("type=recovery") ||
                    uri.getQueryParameter("type") == "recovery"

            if (isRecovery) {
                graph.sessionStore.beginPasswordRecovery()
            }

            JewelSupabase.client.handleDeeplinks(intent)
        }
    }
}
