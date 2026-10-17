package com.jewelindia.app.feature.wholesaler

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WholesalerOrdersViewModelTest {

    @Test
    fun testEmptyWholesalerUserIdYieldsError() = runTest {
        val vm = WholesalerOrdersViewModel(wholesalerUserId = "")
        vm.load()
        val state = vm.uiState.first()
        assertTrue(state is OrdersUiState.Error)
        assertEquals("Session missing. Please sign in again.", (state as OrdersUiState.Error).message)
    }

    @Test
    fun testInitialStateIsLoading() {
        val vm = WholesalerOrdersViewModel(wholesalerUserId = "user-123")
        assertEquals(OrdersUiState.Loading, vm.uiState.value)
        assertEquals(0, vm.pendingCount)
    }
}
