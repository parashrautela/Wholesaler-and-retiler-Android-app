// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeCatalogueView.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeCatalogueScreen(
    session: EmployeeSession,
    store: EmployeeStore,
    onOpenDetail: (Product) -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState()

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }
    var selectedPurity by remember { mutableStateOf("All") }
    var inStockOnly by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            try {
                products = EmployeeApi.fetchSelectedProducts(session.retailerId)
            } catch (_: Exception) {
            } finally {
                loading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(session.retailerId) {
        loadData()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategoryId, selectedPurity, inStockOnly) {
        products.filter { p ->
            val matchSearch = searchQuery.isBlank() ||
                (p.displayTitle.contains(searchQuery, ignoreCase = true)) ||
                (p.category?.contains(searchQuery, ignoreCase = true) == true)

            val matchCategory = selectedCategoryId == "all" ||
                (p.category?.contains(selectedCategoryId, ignoreCase = true) == true) ||
                (p.jewelleryType?.contains(selectedCategoryId, ignoreCase = true) == true)

            val matchPurity = selectedPurity == "All" ||
                (p.purity?.contains(selectedPurity, ignoreCase = true) == true)

            val matchStock = !inStockOnly || p.stockAvailable == true

            matchSearch && matchCategory && matchPurity && matchStock
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Curated Designs",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    loadData()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = {
                            Text("Search designs, categories...", style = Typography.santoshi(14.sp), color = Palette.textTertiary)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Palette.textSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Palette.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Palette.surfaceElevated,
                            unfocusedContainerColor = Palette.surfaceElevated,
                            focusedBorderColor = Palette.goldPrimary,
                            unfocusedBorderColor = Palette.surfaceBorder
                        )
                    )

                    // Categories
                    EmployeeCategoryBar(
                        selectedCategoryId = selectedCategoryId,
                        onSelectCategory = { selectedCategoryId = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Filters
                    EmployeeFilterRow(
                        selectedPurity = selectedPurity,
                        onSelectPurity = { selectedPurity = it },
                        inStockOnly = inStockOnly,
                        onToggleInStock = { inStockOnly = it },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.goldPrimary)
                        }
                    } else if (filteredProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (products.isEmpty()) {
                                    "No designs in the store catalogue yet."
                                } else {
                                    "No designs match the selected filters."
                                },
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // 2-Column Staggered Masonry Grid
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                val isSelected = state.selectedProductIds.contains(product.id)
                                // Varied heights for staggered Pinterest layout
                                val isTall = (product.id.hashCode() % 3 == 0)
                                val aspectRatio = if (isTall) 0.85f else 1.1f

                                EmployeeMasonryCard(
                                    product = product,
                                    aspectRatio = aspectRatio,
                                    isSelected = isSelected,
                                    onClick = { onOpenDetail(product) },
                                    onLongClick = { store.toggleSelection(product.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Floating bottom selection bar when items are selected
            if (state.selectedProductIds.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Palette.goldPrimary,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 76.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${state.selectedProductIds.size}",
                                        style = Typography.santoshi(13.sp, FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                text = "pieces selected",
                                style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onOpenReview,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Palette.goldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Review & Request",
                                    style = Typography.santoshi(13.sp, FontWeight.Bold)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmployeeMasonryCard(
    product: Product,
    aspectRatio: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onLongClickLabel = "Toggle product selection"
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                ProtectedImage(
                    url = product.primaryImage,
                    contentDescription = product.displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                    Surface(
                        color = Palette.goldPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.displayTitle,
                    style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.purity ?: "22KT",
                        style = Typography.santoshi(11.sp),
                        color = Palette.goldPrimary
                    )
                    product.netWeight?.let { w ->
                        Text(
                            text = "${w}g",
                            style = Typography.santoshi(11.sp),
                            color = Palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}
