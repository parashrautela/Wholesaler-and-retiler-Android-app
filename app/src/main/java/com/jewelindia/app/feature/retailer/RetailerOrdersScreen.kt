// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/RetailerOrdersView.swift
package com.jewelindia.app.feature.retailer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.Order
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RetailerOrdersUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val orders: List<Order> = emptyList(),
    val products: Map<String, Product> = emptyMap(),
    val suppliers: Map<String, Wholesaler> = emptyMap(),
    val error: String? = null
)

class RetailerOrdersStore(
    private val jewelApi: JewelApi = JewelApi
) {
    private val _state = MutableStateFlow(RetailerOrdersUiState())
    val state: StateFlow<RetailerOrdersUiState> = _state.asStateFlow()

    suspend fun loadIfNeeded() {
        if (_state.value.orders.isEmpty() && _state.value.loading) {
            refresh()
        }
    }

    suspend fun refresh() {
        _state.value = _state.value.copy(refreshing = true, error = null)
        try {
            val response = jewelApi.fetchRetailerOrders()
            _state.value = RetailerOrdersUiState(
                loading = false,
                refreshing = false,
                orders = response.orders,
                products = response.products,
                suppliers = response.suppliers,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                refreshing = false,
                error = e.message ?: "Failed to load orders"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrdersScreen(
    store: RetailerOrdersStore,
    modifier: Modifier = Modifier
) {
    val state by store.state.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        store.loadIfNeeded()
    }

    val filterOptions = listOf("All", "Pending", "Active", "Dispatched", "Completed", "Rejected")

    val filteredOrders = remember(state.orders, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> state.orders.filter { it.status == null || it.status == OrderStatus.pending }
            2 -> state.orders.filter { it.status == OrderStatus.accepted || it.status == OrderStatus.inProduction }
            3 -> state.orders.filter { it.status == OrderStatus.dispatched || it.status == OrderStatus.packed }
            4 -> state.orders.filter { it.status == OrderStatus.completed || it.status == OrderStatus.received }
            5 -> state.orders.filter { it.status == OrderStatus.rejected }
            else -> state.orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store Orders",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surface)
            )
        },
        containerColor = Palette.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { scope.launch { store.refresh() } },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Tabs
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions.size) { index ->
                        CategoryFilterPill(
                            label = filterOptions[index],
                            isSelected = selectedFilterIndex == index,
                            onClick = { selectedFilterIndex = index }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.loading && state.orders.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Palette.goldPrimary)
                            }
                        }

                        filteredOrders.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingBag,
                                    contentDescription = null,
                                    tint = Palette.textTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Orders in ${filterOptions[selectedFilterIndex]}",
                                    style = Typography.cirka(20.sp),
                                    color = Palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Wholesale jewellery orders you request from manufacturers appear here with live tracking.",
                                    style = Typography.santoshi(14.sp),
                                    color = Palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredOrders, key = { it.id }) { order ->
                                    val product = order.productId?.let { state.products[it] }
                                    val supplier = order.wholesalerId?.let { state.suppliers[it] }

                                    RetailerOrderCard(
                                        order = order,
                                        product = product,
                                        supplier = supplier,
                                        onClick = { selectedOrderForDetail = order }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Sheet
    selectedOrderForDetail?.let { order ->
        val product = order.productId?.let { state.products[it] }
        val supplier = order.wholesalerId?.let { state.suppliers[it] }

        RetailerOrderDetailSheet(
            order = order,
            product = product,
            supplier = supplier,
            onDismiss = { selectedOrderForDetail = null }
        )
    }
}

@Composable
fun RetailerOrderCard(
    order: Order,
    product: Product?,
    supplier: Wholesaler?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.surface)
        ) {
            CachedImage(
                url = product?.primaryImage,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.shortId}",
                    style = Typography.santoshi(12.sp, FontWeight.Bold),
                    color = Palette.goldPrimary
                )
                OrderStatusChip(status = order.status)
            }

            Text(
                text = product?.displayTitle ?: "Jewellery Order",
                style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                color = Palette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = supplier?.businessName ?: product?.purity?.let { "$it Gold" } ?: "Wholesale Manufacturer",
                style = Typography.santoshi(12.sp),
                color = Palette.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus?) {
    val (bgColor, textColor, label) = when (status) {
        OrderStatus.pending, null -> Triple(Palette.goldPrimary.copy(alpha = 0.15f), Palette.goldPrimary, "Requested")
        OrderStatus.accepted -> Triple(Color(0xFF2196F3).copy(alpha = 0.15f), Color(0xFF2196F3), "Accepted")
        OrderStatus.inProduction -> Triple(Color(0xFFFF9800).copy(alpha = 0.15f), Color(0xFFFF9800), "In Production")
        OrderStatus.packed, OrderStatus.dispatched -> Triple(Color(0xFF9C27B0).copy(alpha = 0.15f), Color(0xFF9C27B0), "Dispatched")
        OrderStatus.completed, OrderStatus.received -> Triple(Palette.accentGreen.copy(alpha = 0.15f), Palette.accentGreen, "Completed")
        OrderStatus.rejected -> Triple(Palette.accentRed.copy(alpha = 0.15f), Palette.accentRed, "Rejected")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            style = Typography.santoshi(11.sp, FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrderDetailSheet(
    order: Order,
    product: Product?,
    supplier: Wholesaler?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order Details",
                    style = Typography.cirka(22.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            // Product & Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.surface)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.surfaceElevated)
                ) {
                    CachedImage(
                        url = product?.primaryImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OrderStatusChip(status = order.status)
                    Text(
                        text = product?.displayTitle ?: "Jewellery Order",
                        style = Typography.santoshi(16.sp, FontWeight.Bold),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "Order ID: #${order.shortId}",
                        style = Typography.santoshi(12.sp),
                        color = Palette.goldPrimary
                    )
                }
            }

            HorizontalDivider(color = Palette.surfaceBorder)

            // Specs
            product?.let { prod ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecColumn("Purity", prod.purity ?: "22KT")
                    SpecColumn("Weight", prod.weightGrams?.let { "${it}g" } ?: "Standard")
                    SpecColumn("Category", prod.category ?: "Jewellery")
                }
            }

            // Supplier
            supplier?.let { supp ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Manufacturer", style = Typography.santoshi(12.sp), color = Palette.textSecondary)
                    Text(
                        text = supp.businessName ?: "Wholesaler",
                        style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = listOfNotNull(supp.city, supp.state).joinToString(", "),
                        style = Typography.santoshi(13.sp),
                        color = Palette.textSecondary
                    )
                }
            }

            // Customization Notes
            if (!order.customizationNote.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Customization Notes", style = Typography.santoshi(12.sp), color = Palette.textSecondary)
                    Text(
                        text = order.customizationNote,
                        style = Typography.santoshi(14.sp),
                        color = Palette.textPrimary
                    )
                }
            }

            // Rejection reason if any
            if (!order.rejectionReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.accentRed.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Rejection Reason", style = Typography.santoshi(12.sp, FontWeight.Bold), color = Palette.accentRed)
                        Text(
                            text = order.rejectionReason,
                            style = Typography.santoshi(14.sp),
                            color = Palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = Typography.santoshi(12.sp),
            color = Palette.textSecondary
        )
        Text(
            text = value,
            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
            color = Palette.textPrimary
        )
    }
}
