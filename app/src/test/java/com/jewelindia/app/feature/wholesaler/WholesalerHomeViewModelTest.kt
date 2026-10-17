package com.jewelindia.app.feature.wholesaler

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WholesalerHomeViewModelTest {

    @Test
    fun testEmptyWholesalerUserIdYieldsError() = runTest {
        val vm = WholesalerHomeViewModel(wholesalerUserId = "")
        vm.load()
        val state = vm.uiState.first()
        assertTrue("Empty user ID should yield Error state", state is HomeUiState.Error)
        assertEquals("Session missing. Please sign in again.", (state as HomeUiState.Error).message)
    }

    @Test
    fun testLoadingStateTransition() = runTest {
        val vm = WholesalerHomeViewModel(wholesalerUserId = "test-uid-123")
        assertEquals(HomeUiState.Loading, vm.uiState.value)
    }
}
