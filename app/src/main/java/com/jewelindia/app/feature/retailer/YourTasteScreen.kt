// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/YourTasteView.swift
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.derivedStateOf
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
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun MarketplaceProductCard(
    product: Product,
    isShortlisted: Boolean,
    onToggleShortlist: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Product Image with Like Button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Palette.surface)
            ) {
                CachedImage(
                    url = product.primaryImage,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                LikeButton(
                    isLiked = isShortlisted,
                    onToggle = onToggleShortlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Weight badge
                if (product.weightGrams != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${product.weightGrams}g",
                            style = Typography.santoshi(11.sp, FontWeight.Medium),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Product Meta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.displayTitle,
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.purity ?: "22KT",
                        style = Typography.santoshi(12.sp),
                        color = Palette.goldPrimary
                    )

                    product.category?.let { cat ->
                        Text(
                            text = cat,
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceProductDetailSheet(
    product: Product,
    isShortlisted: Boolean,
    onToggleShortlist: () -> Unit,
    onDismiss: () -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    onOpenOrderRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var startingChat by remember { mutableStateOf(false) }

    LaunchedEffect(product.id) {
        StoreActivity.log(StoreActivity.Event.DesignViewed, productId = product.id)
    }

    val images = remember(product) {
        val list = mutableListOf<String>()
        product.primaryImage?.let { list.add(it) }
        product.allImages.forEach { if (!list.contains(it)) list.add(it) }
        list.ifEmpty { listOf("") }
    }
    val pagerState = rememberPagerState(pageCount = { images.size })

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Image Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .background(Palette.surface)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    CachedImage(
                        url = images[page],
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Page indicator
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(images.size) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == idx) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == idx) Palette.goldPrimary
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }

                // Heart button on top right
                LikeButton(
                    isLiked = isShortlisted,
                    onToggle = onToggleShortlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    size = 40.dp,
                    iconSize = 22.dp
                )
            }

            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = product.displayTitle,
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = product.category ?: "Fine Jewellery",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Specification Grid
                Text(
                    text = "SPECIFICATIONS",
                    style = Typography.santoshi(12.sp, FontWeight.Bold),
                    color = Palette.goldPrimary,
                    letterSpacing = 1.sp
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecColumn("Purity", product.purity ?: "22KT")
                    SpecColumn("Net Weight", product.weightGrams?.let { "${it}g" } ?: "N/A")
                    SpecColumn("Gross Weight", product.grossWeightGrams?.let { "${it}g" } ?: "N/A")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecColumn("Jewellery Type", product.type ?: "N/A")
                    SpecColumn("Style Aesthetic", product.styleAesthetic ?: "Traditional")
                    SpecColumn("In Stock", if (product.inStock == true) "Ready to ship" else "Made to order")
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Actions: Chat & Order Request
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!startingChat) {
                                startingChat = true
                                scope.launch {
                                    try {
                                        val convId = ChatApi.open(product.id)
                                        val thread = ChatThread(
                                            id = convId,
                                            side = "retailer",
                                            productID = product.id,
                                            productTitle = product.title,
                                            productImage = product.primaryImage
                                        )
                                        onOpenChat(thread)
                                    } catch (_: Exception) {
                                    } finally {
                                        startingChat = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.surfaceElevated),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, Palette.goldPrimary, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (startingChat) {
                            CircularProgressIndicator(color = Palette.goldPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Palette.goldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ask Wholesaler",
                                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                    color = Palette.goldPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenOrderRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingBag,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Request Order",
                                style = Typography.santoshi(14.sp, FontWeight.Bold),
                                color = Color.Black
                            )
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrderRequestSheet(
    product: Product,
    onDismiss: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (submitted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Palette.accentGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Order Request Sent!",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "The manufacturer will review your request and confirm dispatch timeline.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            onDismiss()
                            onOrderSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Request Order",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                    }
                }

                // Product summary banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Palette.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CachedImage(
                        url = product.primaryImage,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.displayTitle,
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${product.purity ?: "22KT"} · ${product.weightGrams?.let { "${it}g" } ?: "Weight on order"}",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                }

                // Quantity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantity",
                        style = Typography.santoshi(16.sp, FontWeight.Medium),
                        color = Palette.textPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Palette.surface)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = Palette.textPrimary)
                        }

                        Text(
                            text = quantity.toString(),
                            style = Typography.santoshi(18.sp, FontWeight.Bold),
                            color = Palette.textPrimary,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { if (quantity < 100) quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Palette.surface)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Palette.textPrimary)
                        }
                    }
                }

                // Customization / notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Customization Notes",
                        style = Typography.santoshi(14.sp, FontWeight.Medium),
                        color = Palette.textPrimary
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Specify ring size, gemstone preference, or engraving...", color = Palette.textTertiary) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Palette.goldPrimary,
                            unfocusedBorderColor = Palette.surfaceBorder,
                            focusedContainerColor = Palette.surface,
                            unfocusedContainerColor = Palette.surface,
                            focusedTextColor = Palette.textPrimary,
                            unfocusedTextColor = Palette.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = Typography.santoshi(13.sp),
                        color = Palette.accentRed
                    )
                }

                Button(
                    onClick = {
                        submitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                JewelApi.createRetailerOrder(
                                    productId = product.id,
                                    quantity = quantity,
                                    notes = notes
                                )
                                StoreActivity.log(StoreActivity.Event.OrderRequested, productId = product.id)
                                submitted = true
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to place order request. Please try again."
                            } finally {
                                submitting = false
                            }
                        }
                    },
                    enabled = !submitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "Send Order Request",
                            style = Typography.santoshi(16.sp, FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourTasteScreen(
    store: MarketplaceCatalogueStore,
    onOpenChat: (ChatThread) -> Unit,
    boardId: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by store.state.collectAsState()
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var selectedProductForOrder by remember { mutableStateOf<Product?>(null) }
    var showImageSearch by remember { mutableStateOf(false) }

    // Board-specific bookmark state if boardId is passed
    var boardSavedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(boardId) {
        if (boardId != null) {
            // Load designs on board
            try {
                val boards = WishlistApi.fetchBoards("")
                boards.find { it.id == boardId }?.let { b ->
                    boardSavedIds = b.products.map { it.id }.toSet()
                }
            } catch (_: Exception) {}
        }
    }

    // Pagination trigger when approaching list end
    val shouldLoadMore by remember {
        derivedStateOf {
            val total = gridState.layoutInfo.totalItemsCount
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            store.loadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (boardId != null) "Curate Board" else "Marketplace",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Palette.textPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showImageSearch = true }) {
                        Icon(
                            imageVector = Icons.Filled.ImageSearch,
                            contentDescription = "Visual Search",
                            tint = Palette.goldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surface)
            )
        },
        containerColor = Palette.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { store.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { store.onSearchQueryChanged(it) },
                        placeholder = { Text("Search by design, style, or type...", color = Palette.textTertiary, style = Typography.santoshi(14.sp)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = Palette.textSecondary)
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { store.onSearchQueryChanged("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Palette.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Palette.goldPrimary,
                            unfocusedBorderColor = Palette.surfaceBorder,
                            focusedContainerColor = Palette.surfaceElevated,
                            unfocusedContainerColor = Palette.surfaceElevated,
                            focusedTextColor = Palette.textPrimary,
                            unfocusedTextColor = Palette.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Categories horizontal selector
                if (state.categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        item {
                            CategoryFilterPill(
                                label = "All Designs",
                                isSelected = state.selectedCategory == null,
                                onClick = { store.selectCategory(null) }
                            )
                        }
                        items(state.categories, key = { it.name }) { cat ->
                            CategoryFilterPill(
                                label = cat.name,
                                isSelected = state.selectedCategory == cat.name,
                                onClick = { store.selectCategory(cat.name) }
                            )
                        }
                    }
                }

                // Products Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (state.loading && state.products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.goldPrimary)
                        }
                    } else if (state.products.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "No designs found",
                                    style = Typography.cirka(20.sp),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = if (state.searchQuery.isNotEmpty()) "Try a different search query or category filter." else "The marketplace catalogue is currently updating.",
                                    style = Typography.santoshi(14.sp),
                                    color = Palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.products, key = { it.id }) { product ->
                                val isShortlisted = if (boardId != null) {
                                    boardSavedIds.contains(product.id)
                                } else {
                                    state.shortlistedIds.contains(product.id)
                                }

                                MarketplaceProductCard(
                                    product = product,
                                    isShortlisted = isShortlisted,
                                    onToggleShortlist = {
                                        if (boardId != null) {
                                            val newTarget = !isShortlisted
                                            boardSavedIds = if (newTarget) boardSavedIds + product.id else boardSavedIds - product.id
                                            scope.launch {
                                                try {
                                                    WishlistApi.setDesign(product.id, boardId, newTarget)
                                                    StoreActivity.log(StoreActivity.Event.DesignSavedToBoard, productId = product.id)
                                                } catch (_: Exception) {
                                                    // Rollback
                                                    boardSavedIds = if (isShortlisted) boardSavedIds + product.id else boardSavedIds - product.id
                                                }
                                            }
                                        } else {
                                            store.toggleShortlist(product.id)
                                            StoreActivity.log(StoreActivity.Event.DesignShortlisted, productId = product.id)
                                        }
                                    },
                                    onClick = { selectedProductForDetail = product }
                                )
                            }

                            if (state.loadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Palette.goldPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Sheet
    selectedProductForDetail?.let { prod ->
        val isShortlisted = if (boardId != null) {
            boardSavedIds.contains(prod.id)
        } else {
            state.shortlistedIds.contains(prod.id)
        }

        MarketplaceProductDetailSheet(
            product = prod,
            isShortlisted = isShortlisted,
            onToggleShortlist = {
                if (boardId != null) {
                    val newTarget = !isShortlisted
                    boardSavedIds = if (newTarget) boardSavedIds + prod.id else boardSavedIds - prod.id
                    scope.launch {
                        try {
                            WishlistApi.setDesign(prod.id, boardId, newTarget)
                            StoreActivity.log(StoreActivity.Event.DesignSavedToBoard, productId = prod.id)
                        } catch (_: Exception) {
                            boardSavedIds = if (isShortlisted) boardSavedIds + prod.id else boardSavedIds - prod.id
                        }
                    }
                } else {
                    store.toggleShortlist(prod.id)
                    StoreActivity.log(StoreActivity.Event.DesignShortlisted, productId = prod.id)
                }
            },
            onDismiss = { selectedProductForDetail = null },
            onOpenChat = { thread ->
                selectedProductForDetail = null
                onOpenChat(thread)
            },
            onOpenOrderRequest = {
                selectedProductForDetail = null
                selectedProductForOrder = prod
            }
        )
    }

    // Order Request Sheet
    selectedProductForOrder?.let { prod ->
        RetailerOrderRequestSheet(
            product = prod,
            onDismiss = { selectedProductForOrder = null },
            onOrderSuccess = { store.refresh() }
        )
    }

    // Visual Search Screen
    if (showImageSearch) {
        CatalogueImageSearchScreen(
            onDismiss = { showImageSearch = false },
            onProductSelected = { prod ->
                showImageSearch = false
                selectedProductForDetail = prod
            },
            onToggleShortlist = { store.toggleShortlist(it) },
            shortlistedIds = state.shortlistedIds
        )
    }
}

@Composable
fun CategoryFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Palette.goldPrimary else Palette.surfaceElevated,
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Palette.surfaceBorder),
        modifier = Modifier
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = Typography.santoshi(13.sp, if (isSelected) FontWeight.Bold else FontWeight.Medium),
            color = if (isSelected) Color.Black else Palette.textPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
