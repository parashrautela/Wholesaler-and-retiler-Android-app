// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/OrdersView.swift
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.OrdersApi
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.launch

private val ORDER_TABS = listOf("Pending", "Active", "History")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerOrdersScreen(
    wholesaler: Wholesaler?,
    onOrderCountChange: (Int) -> Unit
) {
    var orders by remember { mutableStateOf<List<WholesalerOrder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTabIdx by remember { mutableStateOf(0) }
    var selectedOrder by remember { mutableStateOf<WholesalerOrder?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(wholesaler?.id) {
        val wid = wholesaler?.id ?: return@LaunchedEffect
        loading = true
        orders = try { OrdersApi.fetchWholesalerOrders() } catch (_: Exception) { emptyList() }
        loading = false
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Palette.dark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTabIdx,
            containerColor = Palette.background,
            contentColor = Palette.goldPrimary,
            edgePadding = 16.dp
        ) {
            ORDER_TABS.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTabIdx == idx,
                    onClick = { selectedTabIdx = idx },
                    text = { Text(label, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        HorizontalDivider(color = Palette.border)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (tabOrders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ${ORDER_TABS[selectedTabIdx].lowercase()} orders", color = Palette.muted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(tabOrders) { order ->
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
            sheetState = sheetState
        ) {
            OrderDetailSheet(
                order = order,
                onStatusUpdate = { updated ->
                    orders = orders.map { if (it.id == updated.id) updated else it }
                    onOrderCountChange(orders.count { it.status == OrderStatus.pending })
                    selectedOrder = null
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
            .background(Palette.light)
            .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        order.imageURL?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            )
        } ?: Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Palette.cream),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", color = Palette.muted, fontSize = 20.sp)
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = order.designTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.dark
            )
            Text(text = order.storeLine, fontSize = 12.sp, color = Palette.muted)
            OrderStatusBadge(order.status)
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Palette.muted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus?) {
    val (label, bg, fg) = when (status) {
        OrderStatus.pending -> Triple("Pending", Color(0xFFFFF3CD), Color(0xFFB45309))
        OrderStatus.accepted -> Triple("Accepted", Color(0xFFD1FAE5), Color(0xFF065F46))
        OrderStatus.inProduction -> Triple("In Production", Color(0xFFDBEAFE), Color(0xFF1E40AF))
        OrderStatus.packed -> Triple("Packed", Color(0xFFEDE9FE), Color(0xFF5B21B6))
        OrderStatus.dispatched -> Triple("Dispatched", Color(0xFFDCFCE7), Color(0xFF15803D))
        OrderStatus.received -> Triple("Received", Color(0xFFF0FDF4), Color(0xFF166534))
        OrderStatus.completed -> Triple("Completed", Color(0xFFF0FDF4), Color(0xFF15803D))
        OrderStatus.rejected -> Triple("Rejected", Color(0xFFFEE2E2), Color(0xFF991B1B))
        else -> Triple("Unknown", Palette.cream, Palette.muted)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun OrderDetailSheet(
    order: WholesalerOrder,
    onStatusUpdate: (WholesalerOrder) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }
    var showRejectionInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Order #${order.shortId}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Palette.dark)
        Text(text = order.designTitle, fontSize = 15.sp, color = Palette.muted)
        Text(text = order.storeLine, fontSize = 13.sp, color = Palette.muted)

        if (!order.note.isNullOrBlank()) {
            Text(text = "Note: ${order.note}", fontSize = 13.sp, color = Palette.dark)
        }

        OrderStatusBadge(order.status)

        HorizontalDivider(color = Palette.border)

        // Action buttons per status
        if (order.status == OrderStatus.pending) {
            if (showRejectionInput) {
                OutlinedTextField(
                    value = rejectionReason,
                    onValueChange = { rejectionReason = it },
                    label = { Text("Rejection reason") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Palette.accentRed)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            working = true
                            try {
                                WholesalerApi.updateOrderStatus(order.id, OrderStatus.accepted)
                                onStatusUpdate(order.copy(status = OrderStatus.accepted))
                            } catch (_: Exception) {}
                            working = false
                        }
                    },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Accept", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        if (!showRejectionInput) { showRejectionInput = true; return@Button }
                        scope.launch {
                            working = true
                            try {
                                WholesalerApi.updateOrderStatus(order.id, OrderStatus.rejected, rejectionReason.ifBlank { null })
                                onStatusUpdate(order.copy(status = OrderStatus.rejected))
                            } catch (_: Exception) {}
                            working = false
                        }
                    },
                    enabled = !working,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reject", fontWeight = FontWeight.SemiBold)
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
                    scope.launch {
                        working = true
                        try {
                            WholesalerApi.updateOrderStatus(order.id, next)
                            onStatusUpdate(order.copy(status = next))
                        } catch (_: Exception) {}
                        working = false
                    }
                },
                enabled = !working,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (working) CircularProgressIndicator(Modifier.size(18.dp), color = Palette.light, strokeWidth = 2.dp)
                else Text(label, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
