package com.jewelindia.app.core.session

import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.model.WholesalerGate
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionEventMappingTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createSessionStore(provider: FakeGateProvider = FakeGateProvider()): SessionStore {
        val prefs = FakeSharedPreferences()
        val router = AuthRouter(ViewModeStore(prefs), provider)
        return SessionStore(
            authRouter = router,
            viewModeStore = ViewModeStore(prefs),
            coroutineScope = testScope
        )
    }

    private fun dummySession(userId: String = "user-123"): UserSession {
        return UserSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            expiresIn = 3600,
            tokenType = "bearer",
            user = UserInfo(id = userId, aud = "authenticated")
        )
    }

    @Test
    fun testInitializingKeepsLoading() = testScope.runTest {
        val store = createSessionStore()
        store.handleSessionStatus(SessionStatus.Initializing)
        assertEquals(SessionStore.Phase.Loading, store.phase.value)
    }

    @Test
    fun testAuthenticatedFromStorageRoutesInitialSession() = testScope.runTest {
        val provider = FakeGateProvider()
        provider.wholesalerResult = Gate.Missing
        val store = createSessionStore(provider)

        val session = dummySession()
        store.handleSessionStatus(SessionStatus.Authenticated(session, SessionSource.Storage))

        assertEquals("user-123", store.user.value?.id)
    }

    @Test
    fun testNotAuthenticatedSignOutRoutesToEntry() = testScope.runTest {
        val store = createSessionStore()
        store.handleSessionStatus(SessionStatus.NotAuthenticated(isSignOut = true))

        assertEquals(SessionStore.Phase.Unauthenticated(AppDestination.Entry(null)), store.phase.value)
    }

    @Test
    fun testNotAuthenticatedInitialRoutesToEntry() = testScope.runTest {
        val store = createSessionStore()
        store.handleSessionStatus(SessionStatus.NotAuthenticated(isSignOut = false))

        assertEquals(SessionStore.Phase.Unauthenticated(AppDestination.Entry(null)), store.phase.value)
    }

    @Test
    fun testRefreshFailureLeavesCurrentPhaseIntact() = testScope.runTest {
        val store = createSessionStore()
        val initial = store.phase.value
        store.handleSessionStatus(SessionStatus.RefreshFailure(io.github.jan.supabase.auth.status.RefreshFailureCause.NetworkError(RuntimeException("network down"))))

        assertEquals(initial, store.phase.value)
    }
}
