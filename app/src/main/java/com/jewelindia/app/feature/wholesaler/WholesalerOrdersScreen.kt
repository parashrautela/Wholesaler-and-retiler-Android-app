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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Wholesaler
import androidx.compose.runtime.collectAsState
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

private val ORDER_TABS = listOf("Pending", "Active", "History")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerOrdersScreen(
    wholesalerUserId: String = "",
    wholesaler: Wholesaler? = null,
    onOrderCountChange: (Int) -> Unit = {}
) {
    val resolvedUserId = wholesalerUserId.ifBlank {
        wholesaler?.userId ?: ""
    }
    val viewModel = remember(resolvedUserId) {
        WholesalerOrdersViewModel(wholesalerUserId = resolvedUserId)
    }
    val ordersState by viewModel.uiState.collectAsState()

    var selectedTabIdx by remember { mutableStateOf(0) }
    var selectedOrder by remember { mutableStateOf<WholesalerOrder?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(resolvedUserId) {
        viewModel.load()
    }

    val (orders, loading) = when (val s = ordersState) {
        is OrdersUiState.Content -> s.orders to false
        is OrdersUiState.Empty -> emptyList<WholesalerOrder>() to false
        is OrdersUiState.Loading -> emptyList<WholesalerOrder>() to true
        is OrdersUiState.Error -> emptyList<WholesalerOrder>() to false
    }

    LaunchedEffect(orders) {
        onOrderCountChange(orders.count { it.status == OrderStatus.pending })
    }

    val tabOrders = remember(orders, selectedTabIdx) {
        when (selectedTabIdx) {
            0 -> orders.filter { it.status == OrderStatus.pending }
            1 -> orders.filter {
                it.status in listOf(
                    OrderStatus.accepted, OrderStatus.inProduction,
                    OrderStatus.packed, OrderStatus.dispatched
                )
            }
            else -> orders.filter {
                it.status in listOf(OrderStatus.received, OrderStatus.completed, OrderStatus.rejected)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        Text(
            text = "Orders",
            style = Typography.cirka(22.sp),
            color = Palette.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIdx,
            containerColor = Palette.background,
            contentColor = Palette.goldPrimary,
            edgePadding = 16.dp
        ) {
            ORDER_TABS.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTabIdx == idx,
                    onClick = { selectedTabIdx = idx },
                    text = {
                        Text(
                            text = label,
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = if (selectedTabIdx == idx) Palette.textPrimary else Palette.textSecondary
                        )
                    }
                )
            }
        }

        HorizontalDivider(color = Palette.surfaceBorder)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (tabOrders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No ${ORDER_TABS[selectedTabIdx].lowercase()} orders",
                    style = Typography.santoshi(15.sp),
                    color = Palette.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(tabOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { selectedOrder = order }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    selectedOrder?.let { order ->
        ModalBottomSheet(
            onDismissRequest = { selectedOrder = null },
            sheetState = sheetState,
            containerColor = Palette.surfaceElevated
        ) {
            WholesalerOrderDetailSheet(
                order = order,
                onStatusUpdate = { next ->
                    viewModel.updateStatus(order.id, next, onSuccess = {
                        selectedOrder = null
                    })
                },
                onReject = { reason ->
                    viewModel.rejectOrder(order.id, reason, "", onSuccess = {
                        selectedOrder = null
                    })
                },
                onDismiss = { selectedOrder = null }
            )
        }
    }
}

@Composable
private fun OrderCard(order: WholesalerOrder, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.surface)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrderDesignThumb(
            imageUrl = order.imageURL,
            modifier = Modifier.size(56.dp)
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = order.designTitle,
                style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                color = Palette.textPrimary
            )
            Text(
                text = order.storeLine,
                style = Typography.santoshi(12.sp),
                color = Palette.textSecondary
            )
            OrderStatusBadge(order.status)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Palette.textTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun WholesalerOrderDetailSheet(
    order: WholesalerOrder,
    onStatusUpdate: (OrderStatus) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order #${order.shortId}",
                style = Typography.cirka(20.sp),
                color = Palette.textPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderDesignThumb(
                imageUrl = order.imageURL,
                modifier = Modifier.size(64.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = order.designTitle,
                    style = Typography.santoshi(15.sp, FontWeight.Bold),
                    color = Palette.textPrimary
                )
                Text(
                    text = order.storeLine,
                    style = Typography.santoshi(13.sp),
                    color = Palette.textSecondary
                )
                OrderStatusBadge(order.status)
            }
        }

        if (!order.note.isNullOrBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Palette.surface)
                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Customer Note",
                    style = Typography.santoshi(11.sp, FontWeight.SemiBold),
                    color = Palette.textSecondary
                )
                Text(
                    text = order.note.orEmpty(),
                    style = Typography.santoshi(13.sp),
                    color = Palette.textPrimary
                )
            }
        }

        if (!order.rejectionReason.isNullOrBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Palette.accentRed.copy(alpha = 0.08f))
                    .border(1.dp, Palette.accentRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Rejection Reason",
                    style = Typography.santoshi(11.sp, FontWeight.SemiBold),
                    color = Palette.accentRed
                )
                Text(
                    text = order.rejectionReason.orEmpty(),
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }
        }

        // Timeline Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.surface)
                .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Progress",
                style = Typography.santoshi(14.sp, FontWeight.Bold),
                color = Palette.textPrimary
            )
            OrderTimeline(order = order)
        }

        // Action buttons
        if (order.status == OrderStatus.pending) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        working = true
                        onStatusUpdate(OrderStatus.accepted)
                    },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Accept", style = Typography.santoshi(14.sp, FontWeight.Bold))
                }

                Button(
                    onClick = { showRejectSheet = true },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Reject", style = Typography.santoshi(14.sp, FontWeight.Bold))
                }
            }
        }

        val nextStatus = when (order.status) {
            OrderStatus.accepted -> OrderStatus.inProduction to "Mark In Production"
            OrderStatus.inProduction -> OrderStatus.packed to "Mark Packed"
            OrderStatus.packed -> OrderStatus.dispatched to "Mark Dispatched"
            else -> null
        }

        nextStatus?.let { (next, label) ->
            Button(
                onClick = {
                    working = true
                    onStatusUpdate(next)
                },
                enabled = !working,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary, contentColor = Palette.dark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (working) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Palette.dark, strokeWidth = 2.dp)
                } else {
                    Text(label, style = Typography.santoshi(14.sp, FontWeight.Bold))
                }
            }
        }
    }

    if (showRejectSheet) {
        RejectOrderSheet(
            onDismiss = { showRejectSheet = false },
            onConfirm = { reason ->
                onReject(reason)
                showRejectSheet = false
                onDismiss()
            }
        )
    }
}
