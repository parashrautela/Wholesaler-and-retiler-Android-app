package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.WholesalerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    @Test
    fun loadsAll400ProductsAndRetriesTheFailedPage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val pages = mutableListOf<Int>()
            var failThirdPage = true
            val vm = WholesalerCatalogViewModel("user-123", fetchPage = { _, _, page, size ->
                pages.add(page)
                if (page == 2 && failThirdPage) {
                    failThirdPage = false
                    error("Temporary connection failure")
                }
                WholesalerApi.CataloguePage(
                    products = (page * size until minOf((page + 1) * size, 400)).map {
                        Product(id = "product-$it")
                    },
                    total = 400,
                    hasMore = (page + 1) * size < 400
                )
            })
            vm.load()
            advanceUntilIdle()
            assertEquals(100, vm.allProducts.size)
            assertTrue(vm.hasMore.value)
            vm.loadAllRemaining()
            advanceUntilIdle()
            assertEquals(200, vm.allProducts.size)
            assertTrue(vm.refreshError.value != null)
            vm.loadAllRemaining()
            advanceUntilIdle()
            assertEquals(listOf(0, 1, 2, 2, 3), pages)
            assertEquals(400, vm.allProducts.size)
            assertEquals(400, vm.allProducts.map { it.id }.distinct().size)
            assertEquals(false, vm.hasMore.value)
            assertEquals(null, vm.refreshError.value)
            assertEquals(false, vm.isRefreshing.value)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun testReplaceLocalUpdatesListWithoutCallingApi() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var apiCalls = 0
            val initialProduct = Product(id = "p-1", title = "Old Title", isPublished = false)
            val vm = WholesalerCatalogViewModel(
                wholesalerUserId = "user-1",
                fetchPage = { _, _, _, _ ->
                    apiCalls++
                    WholesalerApi.CataloguePage(
                        products = listOf(initialProduct),
                        total = 1,
                        hasMore = false
                    )
                }
            )
            vm.load()
            advanceUntilIdle()
            assertEquals(1, apiCalls)
            val stateBefore = vm.uiState.value as CatalogUiState.Content
            assertEquals(false, stateBefore.products.first().isPublished)

            val updatedProduct = initialProduct.copy(title = "New Title", isPublished = true)
            vm.replaceLocal(updatedProduct)

            assertEquals(1, apiCalls)
            val stateAfter = vm.uiState.value as CatalogUiState.Content
            assertEquals(1, stateAfter.products.size)
            assertEquals(true, stateAfter.products.first().isPublished)
            assertEquals("New Title", stateAfter.products.first().title)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
