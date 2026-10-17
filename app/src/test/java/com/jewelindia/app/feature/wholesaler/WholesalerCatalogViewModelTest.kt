package com.jewelindia.app.feature.wholesaler

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WholesalerCatalogViewModelTest {

    @Test
    fun testEmptyWholesalerUserIdYieldsError() = runTest {
        val vm = WholesalerCatalogViewModel(wholesalerUserId = "")
        vm.load()
        val state = vm.uiState.first()
        assertTrue(state is CatalogUiState.Error)
        assertEquals("Session missing. Please sign in again.", (state as CatalogUiState.Error).message)
    }

    @Test
    fun testInitialStateIsLoading() {
        val vm = WholesalerCatalogViewModel(wholesalerUserId = "user-123")
        assertEquals(CatalogUiState.Loading, vm.uiState.value)
        assertTrue(vm.allProducts.isEmpty())
    }
}
