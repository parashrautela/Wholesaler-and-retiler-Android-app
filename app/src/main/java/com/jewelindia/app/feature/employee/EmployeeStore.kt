// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeStore.swift
package com.jewelindia.app.feature.employee

import android.content.Context
import androidx.compose.runtime.Immutable
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.prefs.Prefs
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.EmployeeSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EmployeeRoute {
    data object Review : EmployeeRoute
    data object Designs : EmployeeRoute
    data class Detail(val product: Product) : EmployeeRoute
    data object Canvas : EmployeeRoute
    data class RequestSent(val count: Int) : EmployeeRoute
}

enum class EmployeeTab(val title: String) {
    Home("Home"),
    Catalogue("Catalogue"),
    Queries("Queries"),
    Orders("Orders")
}

sealed interface EmployeePhase {
    data object Loading : EmployeePhase
    data class Failed(val message: String) : EmployeePhase
    data class Ready(val session: EmployeeSession) : EmployeePhase
}

@Immutable
data class EmployeeUiState(
    val phase: EmployeePhase = EmployeePhase.Loading,
    val selectedTab: EmployeeTab = EmployeeTab.Home,
    val activeRoute: EmployeeRoute? = null,
    val selectedProductIds: Set<String> = emptySet(),
    val hasUnreadQueries: Boolean = false,
    val hasNewOrders: Boolean = false,
    val style: EmployeeStyle = EmployeeStyle.Classic
)

class EmployeeStore(
    private val context: Context,
    private val userId: String,
    private val isRetailer: Boolean,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(
        EmployeeUiState(style = EmployeeStyle.load(context))
    )
    val state: StateFlow<EmployeeUiState> = _state.asStateFlow()

    init {
        loadSession()
    }

    fun loadSession() {
        scope.launch(Dispatchers.IO) {
            _state.update { it.copy(phase = EmployeePhase.Loading) }
            try {
                val session = EmployeeApi.resolveSession(userId, isRetailer)
                _state.update {
                    it.copy(phase = EmployeePhase.Ready(session))
                }
                checkFlags(session)
            } catch (e: Exception) {
                _state.update {
                    it.copy(phase = EmployeePhase.Failed(e.message ?: "Could not load employee session."))
                }
            }
        }
    }

    private suspend fun checkFlags(session: EmployeeSession) {
        val unreadQueries = EmployeeApi.hasUnreadQueries(session)
        val latestOrderUpdate = EmployeeApi.latestOrderUpdate(session)
        val prefs = Prefs(context)
        val lastChecked = prefs.getEmployeeOrdersLastCheckedString(session.seedId)
        val newOrders = if (latestOrderUpdate != null && lastChecked != null) {
            latestOrderUpdate.compareTo(lastChecked) > 0
        } else latestOrderUpdate != null

        _state.update {
            it.copy(
                hasUnreadQueries = unreadQueries,
                hasNewOrders = newOrders
            )
        }
    }

    fun markOrdersChecked() {
        val ready = _state.value.phase as? EmployeePhase.Ready ?: return
        val now = java.time.Instant.now().toString()
        Prefs(context).setEmployeeOrdersLastCheckedString(ready.session.seedId, now)
        _state.update { it.copy(hasNewOrders = false) }
    }

    fun selectTab(tab: EmployeeTab) {
        _state.update { it.copy(selectedTab = tab, activeRoute = null) }
        if (tab == EmployeeTab.Orders) {
            markOrdersChecked()
        }
    }

    fun openRoute(route: EmployeeRoute) {
        _state.update { it.copy(activeRoute = route) }
    }

    fun popRoute() {
        _state.update { it.copy(activeRoute = null) }
    }

    fun toggleSelection(productId: String) {
        _state.update { current ->
            val set = current.selectedProductIds.toMutableSet()
            if (set.contains(productId)) {
                set.remove(productId)
            } else {
                set.add(productId)
            }
            current.copy(selectedProductIds = set)
        }
    }

    fun clearSelections() {
        _state.update { it.copy(selectedProductIds = emptySet()) }
    }

    fun updateStyle(style: EmployeeStyle) {
        EmployeeStyle.save(context, style)
        _state.update { it.copy(style = style) }
    }
}
