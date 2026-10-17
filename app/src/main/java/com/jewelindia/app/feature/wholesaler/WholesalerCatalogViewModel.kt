package com.jewelindia.app.feature.wholesaler

import androidx.lifecycle.ViewModel
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.util.launchSafely
import com.jewelindia.app.data.api.WholesalerApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Content(val products: List<Product>, val totalCount: Int) : CatalogUiState
    data object Empty : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}

class WholesalerCatalogViewModel(
    private val wholesalerUserId: String,
    private val api: WholesalerApi = WholesalerApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var currentCategory: String? = null
    private val loadedProducts = mutableListOf<Product>()
    private var totalCount = 0

    val allProducts: List<Product>
        get() = loadedProducts.toList()

    fun load(category: String? = currentCategory, page: Int = 0) {
        currentCategory = category
        if (wholesalerUserId.isBlank()) {
            _uiState.value = CatalogUiState.Error("Session missing. Please sign in again.")
            return
        }

        launchSafely(onError = { e ->
            _uiState.value = CatalogUiState.Error(
                e.message ?: "Couldn't load your catalogue. Check your connection and try again."
            )
        }) {
            if (page == 0) {
                _uiState.value = CatalogUiState.Loading
                loadedProducts.clear()
            }

            val result = api.fetchCatalogue(
                wholesalerId = wholesalerUserId,
                category = category,
                page = page,
                pageSize = 200
            )

            totalCount = result.total
            loadedProducts.addAll(result.products)

            if (loadedProducts.isEmpty()) {
                _uiState.value = CatalogUiState.Empty
            } else {
                _uiState.value = CatalogUiState.Content(
                    products = loadedProducts.toList(),
                    totalCount = totalCount
                )
            }
        }
    }

    fun deleteProduct(
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchSafely(onError = { e ->
            onError(e.message ?: "Failed to delete product")
        }) {
            api.deleteProduct(product.id)
            try {
                api.deleteProductImages(product)
            } catch (e: Exception) {
                // Partial cleanup
            }
            loadedProducts.removeAll { it.id == product.id }
            if (loadedProducts.isEmpty()) {
                _uiState.value = CatalogUiState.Empty
            } else {
                _uiState.value = CatalogUiState.Content(
                    products = loadedProducts.toList(),
                    totalCount = maxOf(0, totalCount - 1)
                )
            }
            onSuccess()
        }
    }

    fun toggleStock(
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val newStatus = !(product.stockAvailable ?: true)
        launchSafely(onError = { e ->
            onError(e.message ?: "Failed to update stock status")
        }) {
            api.updateProductFlags(product.id, inStock = newStatus)
            val idx = loadedProducts.indexOfFirst { it.id == product.id }
            if (idx != -1) {
                loadedProducts[idx] = loadedProducts[idx].copy(stockAvailable = newStatus)
                _uiState.value = CatalogUiState.Content(
                    products = loadedProducts.toList(),
                    totalCount = totalCount
                )
            }
            onSuccess()
        }
    }

    fun togglePublish(
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val newStatus = !(product.isPublished ?: false)
        launchSafely(onError = { e ->
            onError(e.message ?: "Failed to update publish status")
        }) {
            api.updateProductFlags(product.id, isPublished = newStatus)
            val idx = loadedProducts.indexOfFirst { it.id == product.id }
            if (idx != -1) {
                loadedProducts[idx] = loadedProducts[idx].copy(isPublished = newStatus)
                _uiState.value = CatalogUiState.Content(
                    products = loadedProducts.toList(),
                    totalCount = totalCount
                )
            }
            onSuccess()
        }
    }
}
