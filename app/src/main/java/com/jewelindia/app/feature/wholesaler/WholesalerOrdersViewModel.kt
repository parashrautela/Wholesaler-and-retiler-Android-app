package com.jewelindia.app.feature.wholesaler

import androidx.lifecycle.ViewModel
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.data.api.OrdersApi
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.core.util.launchSafely
import com.jewelindia.app.data.net.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data class Content(val orders: List<WholesalerOrder>) : OrdersUiState
    data object Empty : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

class WholesalerOrdersViewModel(
    private val wholesalerUserId: String,
    private val api: OrdersApi = OrdersApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrdersUiState>(OrdersUiState.Loading)
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private val allOrders = mutableListOf<WholesalerOrder>()

    val orders: List<WholesalerOrder>
        get() = allOrders.toList()

    val pendingCount: Int
        get() = allOrders.count { it.status == OrderStatus.pending }

    fun load() {
        if (wholesalerUserId.isBlank()) {
            _uiState.value = OrdersUiState.Error("Session missing. Please sign in again.")
            return
        }

        launchSafely(onError = { e ->
            _uiState.value = OrdersUiState.Error(
                e.userMessage()
            )
        }) {
            _uiState.value = OrdersUiState.Loading
            val fetched = api.fetchWholesalerOrders()
            allOrders.clear()
            allOrders.addAll(fetched)

            if (allOrders.isEmpty()) {
                _uiState.value = OrdersUiState.Empty
            } else {
                _uiState.value = OrdersUiState.Content(allOrders.toList())
            }
        }
    }

    fun updateStatus(
        orderId: String,
        newStatus: OrderStatus,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchSafely(onError = { e ->
            onError(e.userMessage())
        }) {
            api.setStatus(orderId, newStatus)
            val idx = allOrders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                allOrders[idx] = allOrders[idx].copy(status = newStatus)
                _uiState.value = OrdersUiState.Content(allOrders.toList())
            }
            onSuccess()
        }
    }

    fun rejectOrder(
        orderId: String,
        reason: String,
        details: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        launchSafely(onError = { e ->
            onError(e.userMessage())
        }) {
            val fullReason = if (details.isNotBlank()) "$reason: $details" else reason
            api.setStatus(orderId, OrderStatus.rejected, reason = fullReason)
            val idx = allOrders.indexOfFirst { it.id == orderId }
            if (idx != -1) {
                allOrders[idx] = allOrders[idx].copy(status = OrderStatus.rejected)
                _uiState.value = OrdersUiState.Content(allOrders.toList())
            }
            onSuccess()
        }
    }
}
