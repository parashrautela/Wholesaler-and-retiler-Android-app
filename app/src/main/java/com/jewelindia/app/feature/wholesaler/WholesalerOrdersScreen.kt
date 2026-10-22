// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerOrdersView.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

enum class OrderTab(val title: String) {
    New("New Orders"),
    Active("Active Orders"),
    Completed("Completed"),
    Rejected("Rejected");

    fun contains(status: OrderStatus?): Boolean {
        return when (this) {
            New -> status == OrderStatus.pending
            Active -> status in listOf(
                OrderStatus.accepted,
                OrderStatus.inProduction,
                OrderStatus.packed,
                OrderStatus.dispatched
            )
            Completed -> status in listOf(OrderStatus.received, OrderStatus.completed)
            Rejected -> status == OrderStatus.rejected
        }
    }
}

@Composable
fun WholesalerOrdersScreen(
    wholesalerUserId: String = "",
    wholesaler: Wholesaler? = null,
    peekOrders: List<WholesalerOrder>? = null,
    peekError: String? = null,
    onOrderCountChange: (Int) -> Unit = {}
) {
    val resolvedUserId = wholesalerUserId.ifBlank {
        wholesaler?.userId ?: ""
    }
    val viewModel = remember(resolvedUserId) {
        WholesalerOrdersViewModel(wholesalerUserId = resolvedUserId)
    }
    val ordersState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(OrderTab.New) }
    var openedOrder by remember { mutableStateOf<WholesalerOrder?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resolvedUserId) {
        if (peekOrders == null && peekError == null) {
            viewModel.load()
        }
    }

    val (orders, loading, errorMessage) = when {
        peekError != null -> Triple(emptyList<WholesalerOrder>(), false, peekError)
        peekOrders != null -> Triple(peekOrders, false, null)
        else -> when (val s = ordersState) {
            is OrdersUiState.Content -> Triple(s.orders, false, null)
            is OrdersUiState.Empty -> Triple(emptyList<WholesalerOrder>(), false, null)
            is OrdersUiState.Loading -> Triple(emptyList<WholesalerOrder>(), true, null)
            is OrdersUiState.Error -> Triple(emptyList<WholesalerOrder>(), false, s.message)
        }
    }

    LaunchedEffect(orders) {
        onOrderCountChange(orders.count { it.status == OrderStatus.pending })
    }

    val filteredOrders = remember(orders, selectedTab) {
        orders.filter { selectedTab.contains(it.status) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        // iOS Navigation Bar: Centered inline title "Orders"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Orders",
                style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                color = Palette.foreground
            )
        }

        // iOS Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEEEEE))
                .padding(2.dp)
        ) {
            OrderTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { selectedTab = tab },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        style = Typography.santoshi(
                            12.sp,
                            if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        color = if (isSelected) Palette.foreground else Palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Content
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Palette.dark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (errorMessage != null && orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = Typography.santoshi(14.sp),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Palette.cream)
                            .clickable { viewModel.load() }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Retry",
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Palette.foreground
                        )
                    }
                }
            }
        } else if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = Palette.muted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No ${selectedTab.title}",
                        style = Typography.cirka(24.sp),
                        color = Palette.foreground
                    )
                    Text(
                        text = if (orders.isEmpty()) {
                            "Orders from your retailers will show up here. Invite retailers to start receiving them."
                        } else {
                            "No orders match this status at the moment."
                        },
                        style = Typography.santoshi(14.sp),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    WholesalerOrderCard(
                        order = order,
                        onClick = {
                            openedOrder = order
                            actionError = null
                            isUpdating = false
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    if (openedOrder != null) {
        WholesalerOrderDetailSheet(
            order = openedOrder!!,
            isUpdating = isUpdating,
            actionError = actionError,
            onStatusUpdate = { newStatus ->
                isUpdating = true
                actionError = null
                viewModel.updateStatus(
                    orderId = openedOrder!!.id,
                    newStatus = newStatus,
                    onSuccess = {
                        isUpdating = false
                        openedOrder = null
                    },
                    onError = {
                        isUpdating = false
                        actionError = it
                    }
                )
            },
            onReject = { reason ->
                isUpdating = true
                actionError = null
                viewModel.rejectOrder(
                    orderId = openedOrder!!.id,
                    reason = reason,
                    details = "",
                    onSuccess = {
                        isUpdating = false
                        openedOrder = null
                    },
                    onError = {
                        isUpdating = false
                        actionError = it
                    }
                )
            },
            onDismiss = {
                openedOrder = null
                actionError = null
                isUpdating = false
            }
        )
    }
}

@Composable
fun WholesalerOrderCard(
    order: WholesalerOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        OrderDesignThumb(
            imageUrl = order.imageURL,
            size = 72.dp
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = order.designTitle,
                    style = Typography.santoshi(14.sp, FontWeight.Bold),
                    color = Palette.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (order.status != null) {
                    OrderStatusBadge(status = order.status)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Palette.foreground,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = order.storeLine,
                    style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                    color = Palette.foreground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!order.note.isNullOrBlank()) {
                Text(
                    text = "“${order.note}”",
                    style = Typography.santoshi(12.sp),
                    color = Palette.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val dateStr = OrderTime.short(order.createdAt)?.let { " · $it" } ?: ""
            Text(
                text = "Order #${order.shortId}$dateStr",
                style = Typography.santoshi(11.sp),
                color = Palette.muted
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Palette.muted,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(16.dp)
        )
    }
}
