package com.jewelindia.app.core.session

import android.content.SharedPreferences
import com.jewelindia.app.core.model.AppDestination
import com.jewelindia.app.core.model.EmployeeGate
import com.jewelindia.app.core.model.ProfileRole
import com.jewelindia.app.core.model.RetailerGate
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.model.ViewMode
import com.jewelindia.app.core.model.WholesalerGate
import com.jewelindia.app.core.text.Copy
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeGateProvider : GateProvider {
    var profileResult: Gate<ProfileRole> = Gate.Missing
    var wholesalerResult: Gate<WholesalerGate> = Gate.Missing
    var retailerResult: Gate<RetailerGate> = Gate.Missing
    var employeeResult: Gate<EmployeeGate> = Gate.Missing
    var signOutCalled: Boolean = false

    override suspend fun fetchProfileRole(userId: String): Gate<ProfileRole> = profileResult
    override suspend fun fetchWholesalerGate(userId: String): Gate<WholesalerGate> = wholesalerResult
    override suspend fun fetchRetailerGate(userId: String): Gate<RetailerGate> = retailerResult
    override suspend fun fetchEmployeeGate(userId: String): Gate<EmployeeGate> = employeeResult
    override suspend fun signOut() {
        signOutCalled = true
    }
}

class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data
    override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = (data[key] as? Set<*>)?.filterIsInstance<String>()?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (data[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (data[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (data[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (data[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    inner class Editor : SharedPreferences.Editor {
        private val temp = mutableMapOf<String, Any?>()
        private val toRemove = mutableSetOf<String>()
        private var clear = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor { key?.let { temp[it] = value }; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { key?.let { temp[it] = values }; return this }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { key?.let { temp[it] = value }; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { key?.let { temp[it] = value }; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { key?.let { temp[it] = value }; return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { key?.let { temp[it] = value }; return this }
        override fun remove(key: String?): SharedPreferences.Editor { key?.let { toRemove.add(it) }; return this }
        override fun clear(): SharedPreferences.Editor { clear = true; return this }
        override fun commit(): Boolean { apply(); return true }
        override fun apply() {
            if (clear) data.clear()
            toRemove.forEach { data.remove(it) }
            data.putAll(temp)
        }
    }
}

class AuthRouterTest {

    private fun createUser(id: String = "user-123", roleMetadata: String? = null): UserInfo {
        val meta = roleMetadata?.let { buildJsonObject { put("role", it) } }
        return UserInfo(
            id = id,
            aud = "authenticated",
            userMetadata = meta
        )
    }

    @Test
    fun testSignedInNoRoleReturnsSelectRole() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser()
        provider.profileResult = Gate.Missing

        val dest = router.destination(user)
        assertEquals(AppDestination.SelectRole, dest)
    }

    @Test
    fun testWholesalerNoRowReturnsOnboarding() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Missing

        val dest = router.destination(user)
        assertEquals(AppDestination.WholesalerOnboarding, dest)
    }

    @Test
    fun testWholesalerBannedSignsOut() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Found(WholesalerGate(verificationStatus = VerificationStatus.banned))

        val dest = router.destination(user)
        assertTrue(provider.signOutCalled)
        assertEquals(AppDestination.Entry(Copy.bannedError), dest)
    }

    @Test
    fun testWholesalerVerifiedVisitedReturnsDashboard() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Found(WholesalerGate(verificationStatus = VerificationStatus.verified, hasVisitedDashboard = true))

        val dest = router.destination(user)
        assertEquals(AppDestination.WholesalerDashboard, dest)
    }

    @Test
    fun testWholesalerVerifiedNotVisitedReturnsSubmitted() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Found(WholesalerGate(verificationStatus = VerificationStatus.verified, hasVisitedDashboard = false))

        val dest = router.destination(user)
        assertEquals(AppDestination.WholesalerSubmitted, dest)
    }

    @Test
    fun testWholesalerPendingReturnsSubmitted() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Found(WholesalerGate(verificationStatus = VerificationStatus.pending))

        val dest = router.destination(user)
        assertEquals(AppDestination.WholesalerSubmitted, dest)
    }

    @Test
    fun testRetailerNoRowReturnsOnboarding() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "retailer")
        provider.retailerResult = Gate.Missing

        val dest = router.destination(user)
        assertEquals(AppDestination.RetailerOnboarding, dest)
    }

    @Test
    fun testRetailerBannedSignsOut() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "retailer")
        provider.retailerResult = Gate.Found(RetailerGate(verificationStatus = VerificationStatus.banned))

        val dest = router.destination(user)
        assertTrue(provider.signOutCalled)
        assertEquals(AppDestination.Entry(Copy.bannedError), dest)
    }

    @Test
    fun testRetailerVerifiedRetailerModeReturnsRetailerDashboard() = runTest {
        val prefs = FakeSharedPreferences()
        val viewModeStore = ViewModeStore(prefs)
        viewModeStore.set(ViewMode.retailer, "user-123")
        val provider = FakeGateProvider()
        val router = AuthRouter(viewModeStore, provider)

        val user = createUser(id = "user-123", roleMetadata = "retailer")
        provider.retailerResult = Gate.Found(RetailerGate(verificationStatus = VerificationStatus.verified))

        val dest = router.destination(user)
        assertEquals(AppDestination.RetailerDashboard, dest)
    }

    @Test
    fun testRetailerVerifiedEmployeeModeReturnsEmployeeDashboard() = runTest {
        val prefs = FakeSharedPreferences()
        val viewModeStore = ViewModeStore(prefs)
        viewModeStore.set(ViewMode.employee, "user-123")
        val provider = FakeGateProvider()
        val router = AuthRouter(viewModeStore, provider)

        val user = createUser(id = "user-123", roleMetadata = "retailer")
        provider.retailerResult = Gate.Found(RetailerGate(verificationStatus = VerificationStatus.verified))

        val dest = router.destination(user)
        assertEquals(AppDestination.EmployeeDashboard, dest)
    }

    @Test
    fun testEmployeeMissingRowSignsOut() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "employee")
        provider.employeeResult = Gate.Missing

        val dest = router.destination(user)
        assertTrue(provider.signOutCalled)
        assertEquals(AppDestination.EmployeeLogin(Copy.employeeDeactivated), dest)
    }

    @Test
    fun testEmployeeInactiveStatusSignsOut() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "employee")
        provider.employeeResult = Gate.Found(EmployeeGate(status = "suspended"))

        val dest = router.destination(user)
        assertTrue(provider.signOutCalled)
        assertEquals(AppDestination.EmployeeLogin(Copy.employeeDeactivated), dest)
    }

    @Test
    fun testEmployeeActiveStatusReturnsDashboard() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "employee")
        provider.employeeResult = Gate.Found(EmployeeGate(status = "active"))

        val dest = router.destination(user)
        assertEquals(AppDestination.EmployeeDashboard, dest)
    }

    @Test
    fun testNetworkFailureReturnsUnreachable() = runTest {
        val prefs = FakeSharedPreferences()
        val provider = FakeGateProvider()
        val router = AuthRouter(ViewModeStore(prefs), provider)

        val user = createUser(roleMetadata = "wholesaler")
        provider.wholesalerResult = Gate.Unreachable

        val dest = router.destination(user)
        assertEquals(AppDestination.Unreachable, dest)
    }
}
