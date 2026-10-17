// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/MarketplaceCatalogueStore.swift
package com.jewelindia.app.feature.retailer

import android.content.Context
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.MarketplaceCategoryItem
import com.jewelindia.app.data.api.PaginatedMarketplaceResponse
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

@Serializable
data class MarketplaceCacheData(
    val products: List<Product> = emptyList(),
    val categories: List<MarketplaceCategoryItem> = emptyList(),
    val nextCursor: String? = null
)

data class MarketplaceUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<MarketplaceCategoryItem> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val shortlistedIds: Set<String> = emptySet(),
    val error: String? = null,
    val hasMore: Boolean = true
)

class MarketplaceCatalogueStore(
    private val context: Context,
    private val userId: String,
    private val jewelApi: JewelApi = JewelApi
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(MarketplaceUiState())
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    private var nextCursor: String? = null
    private var searchJob: Job? = null
    private val cacheDir = File(context.filesDir, "MarketplaceCatalogueCache").apply { mkdirs() }
    private val cacheFile = File(cacheDir, "$userId.json")

    init {
        loadFromCache()
        refresh()
    }

    private fun loadFromCache() {
        if (!cacheFile.exists()) return
        scope.launch(Dispatchers.IO) {
            try {
                val json = cacheFile.readText()
                val data = JewelJson.decodeFromString<MarketplaceCacheData>(json)
                val shortlisted = data.products.filter { it.isShortlisted == true }.map { it.id }.toSet()
                nextCursor = data.nextCursor
                _state.update {
                    it.copy(
                        products = data.products,
                        categories = data.categories,
                        shortlistedIds = shortlisted,
                        hasMore = data.nextCursor != null
                    )
                }
            } catch (_: Exception) {
                // Ignore cache read failures
            }
        }
    }

    private fun saveToCache(products: List<Product>, categories: List<MarketplaceCategoryItem>, cursor: String?) {
        scope.launch(Dispatchers.IO) {
            try {
                val data = MarketplaceCacheData(products = products, categories = categories, nextCursor = cursor)
                val json = JewelJson.encodeToString(data)
                cacheFile.writeText(json)
            } catch (_: Exception) {
                // Ignore cache write errors
            }
        }
    }

    fun refresh() {
        scope.launch {
            _state.update { it.copy(refreshing = true, error = null) }
            try {
                // Load categories and first page of products in parallel
                val categories = runCatching { jewelApi.fetchMarketplaceCategories().categories }.getOrDefault(emptyList())
                val page = jewelApi.fetchPaginatedMarketplace(
                    category = _state.value.selectedCategory,
                    search = _state.value.searchQuery.ifBlank { null },
                    cursor = null,
                    limit = 24
                )

                nextCursor = page.nextCursor
                val shortlisted = page.products.filter { it.isShortlisted == true }.map { it.id }.toSet()

                _state.update {
                    it.copy(
                        refreshing = false,
                        loading = false,
                        products = page.products,
                        categories = if (categories.isNotEmpty()) categories else (page.fallbackCategories ?: it.categories),
                        shortlistedIds = shortlisted,
                        hasMore = page.nextCursor != null,
                        error = null
                    )
                }

                saveToCache(page.products, _state.value.categories, page.nextCursor)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        refreshing = false,
                        loading = false,
                        error = e.message ?: "Failed to refresh designs"
                    )
                }
            }
        }
    }

    fun selectCategory(category: String?) {
        if (_state.value.selectedCategory == category) return
        _state.update { it.copy(selectedCategory = category, loading = true) }
        loadFiltered()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300) // Debounce 300ms
            loadFiltered()
        }
    }

    private fun loadFiltered() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = jewelApi.fetchPaginatedMarketplace(
                    category = _state.value.selectedCategory,
                    search = _state.value.searchQuery.ifBlank { null },
                    cursor = null,
                    limit = 24
                )
                nextCursor = page.nextCursor
                val currentShortlisted = _state.value.shortlistedIds
                val newShortlisted = page.products.filter { it.isShortlisted == true }.map { it.id }.toSet()

                _state.update {
                    it.copy(
                        loading = false,
                        products = page.products,
                        shortlistedIds = currentShortlisted + newShortlisted,
                        hasMore = page.nextCursor != null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Failed to load products")
                }
            }
        }
    }

    fun loadMore() {
        val cursor = nextCursor ?: return
        if (_state.value.loadingMore || !_state.value.hasMore) return

        scope.launch {
            _state.update { it.copy(loadingMore = true) }
            try {
                val page = jewelApi.fetchPaginatedMarketplace(
                    category = _state.value.selectedCategory,
                    search = _state.value.searchQuery.ifBlank { null },
                    cursor = cursor,
                    limit = 24
                )
                nextCursor = page.nextCursor
                val newShortlisted = page.products.filter { it.isShortlisted == true }.map { it.id }.toSet()

                _state.update {
                    it.copy(
                        loadingMore = false,
                        products = it.products + page.products,
                        shortlistedIds = it.shortlistedIds + newShortlisted,
                        hasMore = page.nextCursor != null
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(loadingMore = false) }
            }
        }
    }

    fun toggleShortlist(productId: String) {
        val currentlyShortlisted = _state.value.shortlistedIds.contains(productId)
        val newTarget = !currentlyShortlisted

        // Optimistic update
        _state.update {
            val updated = if (newTarget) it.shortlistedIds + productId else it.shortlistedIds - productId
            it.copy(shortlistedIds = updated)
        }

        scope.launch {
            try {
                jewelApi.setRetailerSelection(productId, newTarget)
            } catch (_: Exception) {
                // Rollback on failure
                _state.update {
                    val rolledBack = if (currentlyShortlisted) it.shortlistedIds + productId else it.shortlistedIds - productId
                    it.copy(shortlistedIds = rolledBack)
                }
            }
        }
    }

    fun clear() {
        cacheFile.delete()
        _state.value = MarketplaceUiState()
    }
}
