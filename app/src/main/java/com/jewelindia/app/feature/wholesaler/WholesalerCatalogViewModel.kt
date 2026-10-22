package com.jewelindia.app.feature.wholesaler

import androidx.lifecycle.ViewModel
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.util.launchSafely
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.feature.wholesaler.share.CatalogueSelection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private val api: WholesalerApi = WholesalerApi,
    private val fetchPage: suspend (String, String?, Int, Int) -> WholesalerApi.CataloguePage =
        { userId, category, page, size -> api.fetchCatalogue(userId, category, page, size) }
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _isSelecting = MutableStateFlow(false)
    val isSelecting: StateFlow<Boolean> = _isSelecting.asStateFlow()

    private val _selection = MutableStateFlow(CatalogueSelection())
    val selection: StateFlow<CatalogueSelection> = _selection.asStateFlow()

    private var currentCategory: String? = null
    private var loadJob: Job? = null
    private var revision = 0
    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError = _refreshError.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    suspend fun refreshPendingProducts() {
        val version = revision
        val ids = loadedProducts.filter { it.isProcessing }.map { it.id }
        try {
            for (id in ids) {
                val product = api.fetchProduct(id) ?: continue
                if (revision != version) return
                val index = loadedProducts.indexOfFirst { it.id == id }
                if (index >= 0) loadedProducts[index] = product
            }
            if (revision == version && ids.isNotEmpty()) {
                _uiState.value = CatalogUiState.Content(loadedProducts.toList(), totalCount)
            }
            _refreshError.value = null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _refreshError.value = "Couldn't refresh processing status. Refresh the catalogue to retry."
        }
    }
    private val loadedProducts = mutableListOf<Product>()
    private var totalCount = 0
    private var nextPage = 0
    private val pageSize = 100
    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()

    fun loadAllRemaining() {
        if (!_isRefreshing.value && _hasMore.value) load(page = nextPage, loadRemaining = true)
    }

    val allProducts: List<Product>
        get() = loadedProducts.toList()

    fun toggleSelecting() {
        val next = !_isSelecting.value
        _isSelecting.value = next
        if (!next) {
            _selection.value = CatalogueSelection()
        }
    }

    fun setSelecting(value: Boolean) {
        _isSelecting.value = value
        if (!value) {
            _selection.value = CatalogueSelection()
        }
    }

    fun toggleProductSelection(product: Product): String? {
        val (newSelection, message) = _selection.value.toggle(product)
        _selection.value = newSelection
        return message
    }

    fun clearSelection() {
        _selection.value = CatalogueSelection()
    }


    fun load(category: String? = currentCategory, page: Int = 0, loadRemaining: Boolean = false) {
        loadJob?.cancel()
        revision++
        currentCategory = category
        if (wholesalerUserId.isBlank()) {
            _uiState.value = CatalogUiState.Error("Session missing. Please sign in again.")
            return
        }

        val version = revision
        _isRefreshing.value = true
        loadJob = launchSafely(onError = { e ->
            if (version == revision) {
                _isRefreshing.value = false
                val message = e.message ?: "Couldn't load your catalogue. Check your connection and try again."
                if (loadedProducts.isEmpty()) _uiState.value = CatalogUiState.Error(message)
                else _refreshError.value = message
            }
        }) {
            if (page == 0 && loadedProducts.isEmpty()) _uiState.value = CatalogUiState.Loading
            var pageIndex = page
            do {
                val result = fetchPage(wholesalerUserId, category, pageIndex, pageSize)
                if (version != revision) return@launchSafely
                if (pageIndex == 0) loadedProducts.clear()
                totalCount = result.total
                val existing = loadedProducts.map { it.id }.toHashSet()
                loadedProducts.addAll(result.products.filter { it.id !in existing })
                nextPage = pageIndex + 1
                _hasMore.value = result.products.isNotEmpty() && result.hasMore
                _refreshError.value = null
                _uiState.value = if (loadedProducts.isEmpty()) CatalogUiState.Empty else {
                    CatalogUiState.Content(loadedProducts.toList(), totalCount)
                }
                pageIndex = nextPage
            } while (loadRemaining && _hasMore.value)
            _isRefreshing.value = false
        }
    }

    fun deleteProduct(
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchSafely(onError = { e ->
            onError(e.userMessage())
        }) {
            api.deleteProduct(product.id)
            try {
                api.deleteProductImages(product)
            } catch (e: Exception) {
                // Partial cleanup
            }
            loadedProducts.removeAll { it.id == product.id }
            totalCount = maxOf(0, totalCount - 1)
            if (loadedProducts.isEmpty()) {
                _uiState.value = CatalogUiState.Empty
            } else {
                _uiState.value = CatalogUiState.Content(
                    products = loadedProducts.toList(),
                    totalCount = totalCount
                )
            }
            if (_hasMore.value) load()
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

    fun replaceLocal(product: Product) {
        val idx = loadedProducts.indexOfFirst { it.id == product.id }
        if (idx != -1) {
            loadedProducts[idx] = product
            _uiState.value = CatalogUiState.Content(
                products = loadedProducts.toList(),
                totalCount = totalCount
            )
        }
    }
}
