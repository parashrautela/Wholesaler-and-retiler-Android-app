// Port of: wholesaler ios/JewelIndia/Sources/JewelIndiaApp.swift (AppDependencies / Environment)
package com.jewelindia.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.jewelindia.app.core.session.AuthRouter
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.session.SignupFlow
import com.jewelindia.app.core.session.ViewModeStore
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.CreditsApi
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.InviteApi
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.LikeBook
import com.jewelindia.app.data.api.OnboardingFeeApi
import com.jewelindia.app.data.api.OrdersApi
import com.jewelindia.app.data.api.RetailerApi
import com.jewelindia.app.data.api.StaffAccountsApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.data.supabase.JewelSupabase

/**
 * Manual dependency injection container for Jewel India Android app.
 * Created once in [JewelApplication.onCreate].
 */
class AppGraph(val app: Application) {
    val prefs = app.getSharedPreferences("jewel_india_prefs", Context.MODE_PRIVATE)

    val viewModeStore = ViewModeStore(prefs)
    val signupFlow = SignupFlow(prefs)
    val authRouter = AuthRouter(viewModeStore)
    val sessionStore = SessionStore(
        authRouter = authRouter,
        viewModeStore = viewModeStore,
        supabase = JewelSupabase.client,
        onSignOutCleanup = {
            LikeBook.clear()
        }
    ).apply {
        start()
    }

    // API Singletons / Repositories
    val jewelApi = JewelApi
    val wholesalerApi = WholesalerApi
    val retailerApi = RetailerApi
    val employeeApi = EmployeeApi
    val inviteApi = InviteApi
    val staffAccountsApi = StaffAccountsApi
    val chamakApi = ChamakApi
    val creditsApi = CreditsApi
    val ordersApi = OrdersApi
    val onboardingFeeApi = OnboardingFeeApi
    val wishlistApi = WishlistApi
    val chatApi = ChatApi
    val storeActivity = StoreActivity
    val wholesalerReport = WholesalerReport
    val likeBook = LikeBook
}

val LocalAppGraph = staticCompositionLocalOf<AppGraph> {
    error("No AppGraph provided")
}
