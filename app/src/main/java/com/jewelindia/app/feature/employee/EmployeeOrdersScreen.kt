// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeOrdersView.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.time.ChatTime
import com.jewelindia.app.data.api.OrdersApi
import com.jewelindia.app.data.api.StaffOrder
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.retailer.OrderStatusChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeOrdersScreen(
    store: EmployeeStore,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf<List<StaffOrder>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var selectedOrderForDetail by remember { mutableStateOf<StaffOrder?>(null) }

    val filterOptions = listOf("All", "Pending", "Active", "Dispatched", "Completed", "Rejected")

    fun load() {
        scope.launch {
            try {
                orders = OrdersApi.fetchStaffOrders()
            } catch (_: Exception) {
            } finally {
                loading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    val filteredOrders = remember(orders, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> orders.filter { it.status == null || it.status == OrderStatus.pending }
            2 -> orders.filter { it.status == OrderStatus.accepted || it.status == OrderStatus.inProduction }
            3 -> orders.filter { it.status == OrderStatus.dispatched || it.status == OrderStatus.packed }
            4 -> orders.filter { it.status == OrderStatus.completed || it.status == OrderStatus.received }
            5 -> orders.filter { it.status == OrderStatus.rejected }
            else -> orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Showroom Orders",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surfaceElevated)
            )
        },
        containerColor = Palette.background,
        modifier = modifier
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                load()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEachIndexed { index, option ->
                        val isSelected = selectedFilterIndex == index
                        Surface(
                            onClick = { selectedFilterIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Palette.goldPrimary else Palette.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                            )
                        ) {
                            Text(
                                text = option,
                                style = Typography.santoshi(12.sp, if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                color = if (isSelected) Palette.light else Palette.textSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Palette.goldPrimary)
                    }
                } else if (filteredOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (orders.isEmpty()) "No orders have been requested yet." else "No orders match this status filter.",
                            style = Typography.santoshi(14.sp),
                            color = Palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            Card(
                                onClick = { selectedOrderForDetail = order },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Palette.surfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        ProtectedImage(
                                            url = order.product?.image,
                                            contentDescription = order.product?.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = order.product?.title ?: "Jewellery Order",
                                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                            color = Palette.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Order #${order.id.take(8).uppercase()}",
                                            style = Typography.santoshi(12.sp),
                                            color = Palette.textSecondary
                                        )
                                        order.createdAt?.let { created ->
                                            ChatTime.short(created)?.let { timeStr ->
                                                Text(
                                                    text = "Placed $timeStr",
                                                    style = Typography.santoshi(11.sp),
                                                    color = Palette.textSecondary
                                                )
                                            }
                                        }
                                    }

                                    OrderStatusChip(status = order.status)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Order Detail Modal Sheet
        selectedOrderForDetail?.let { order ->
            ModalBottomSheet(
                onDismissRequest = { selectedOrderForDetail = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Palette.surfaceElevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Order Details",
                            style = Typography.cirka(20.sp),
                            color = Palette.textPrimary
                        )
                        OrderStatusChip(status = order.status)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            ProtectedImage(
                                url = order.product?.image,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = order.product?.title ?: "Jewellery Piece",
                                style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                                color = Palette.textPrimary
                            )
                            Text(
                                text = "Order ID: ${order.id}",
                                style = Typography.santoshi(12.sp),
                                color = Palette.textSecondary
                            )
                        }
                    }

                    order.note?.takeIf { it.isNotBlank() }?.let { note ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "CUSTOMIZATION NOTE", style = Typography.santoshi(11.sp, FontWeight.Bold), color = Palette.goldPrimary)
                            Text(text = note, style = Typography.santoshi(13.sp), color = Palette.textPrimary)
                        }
                    }

                    order.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                        Surface(
                            color = Palette.accentRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Palette.accentRed.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Rejection Reason", style = Typography.santoshi(12.sp, FontWeight.Bold), color = Palette.accentRed)
                                Text(text = reason, style = Typography.santoshi(13.sp), color = Palette.textPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
