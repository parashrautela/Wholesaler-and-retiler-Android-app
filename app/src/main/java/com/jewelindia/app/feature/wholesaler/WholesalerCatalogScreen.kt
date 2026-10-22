// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerCatalogueView.swift
package com.jewelindia.app.feature.wholesaler

import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.security.CaptureProtected
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro
import com.jewelindia.app.feature.wholesaler.share.CatalogueSelection
import com.jewelindia.app.feature.wholesaler.share.SharedLinksScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString

private val CATALOG_CATEGORIES = listOf(
    "All", "Gold", "Diamond", "Polki", "Silver", "Platinum", "Gemstone"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerCatalogScreen(
    wholesalerUserId: String = "",
    wholesaler: Wholesaler? = null,
    sessionStore: SessionStore,
    peekProducts: List<Product>? = null,
    peekIsSelecting: Boolean = false,
    peekSelectionIds: List<String> = emptyList(),
    onOpenUploadHistory: (() -> Unit)? = null
) {
    val resolvedUserId = wholesalerUserId.ifBlank {
        wholesaler?.userId ?: sessionStore.user.value?.id ?: ""
    }
    val viewModel = viewModel<WholesalerCatalogViewModel>(
        key = "catalogue/$resolvedUserId",
        factory = viewModelFactory { initializer { WholesalerCatalogViewModel(resolvedUserId) } }
    )
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val refreshError by viewModel.refreshError.collectAsState()
    val catalogState by viewModel.uiState.collectAsState()
    val isSelectingByVm by viewModel.isSelecting.collectAsState()
    val selectionByVm by viewModel.selection.collectAsState()

    val isSelecting = if (peekProducts != null) peekIsSelecting else isSelectingByVm
    val selection = if (peekProducts != null) {
        val selectedProds = peekProducts.filter { peekSelectionIds.contains(it.id) }
        CatalogueSelection(ids = peekSelectionIds, snapshots = selectedProds.associateBy { it.id })
    } else {
        selectionByVm
    }

    val haptic = LocalHapticFeedback.current
    var actionNotice by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showSharedLinksScreen by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddProductSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionNotice) {
        actionNotice?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            actionNotice = null
        }
    }

    LaunchedEffect(exportFeedback) {
        exportFeedback?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(resolvedUserId, lifecycle) {
        if (peekProducts == null && resolvedUserId.isNotBlank()) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.load()
                repeat(80) {
                    delay(5000)
                    viewModel.refreshPendingProducts()
                }
            }
        }
    }

    val (products, loading, errorMessage) = if (peekProducts != null) {
        Triple(peekProducts, false, null)
    } else {
        when (val s = catalogState) {
            is CatalogUiState.Content -> Triple(s.products, false, null)
            is CatalogUiState.Empty -> Triple(emptyList<Product>(), false, null)
            is CatalogUiState.Loading -> Triple(emptyList<Product>(), true, null)
            is CatalogUiState.Error -> Triple(emptyList<Product>(), false, s.message)
        }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { p ->
            val matchesCategory = if (selectedCategory == null || selectedCategory == "All") true
            else p.category?.equals(selectedCategory, ignoreCase = true) == true ||
                    p.jewelleryType?.equals(selectedCategory, ignoreCase = true) == true

            val matchesQuery = if (searchQuery.isBlank()) true
            else p.title?.contains(searchQuery, ignoreCase = true) == true ||
                    p.jewelleryType?.contains(searchQuery, ignoreCase = true) == true ||
                    p.category?.contains(searchQuery, ignoreCase = true) == true

            matchesCategory && matchesQuery
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            scope.launch(Dispatchers.IO) {
                isExporting = true
                try {
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(
                        treeUri,
                        DocumentsContract.getTreeDocumentId(treeUri)
                    )
                    val fileUri = DocumentsContract.createDocument(
                        context.contentResolver,
                        docUri,
                        "application/json",
                        "catalog_export_${System.currentTimeMillis()}.json"
                    )
                    if (fileUri != null) {
                        context.contentResolver.openOutputStream(fileUri)?.use { stream ->
                            val jsonString = JewelJson.encodeToString(filteredProducts)
                            stream.write(jsonString.toByteArray())
                        }
                        exportFeedback = "Exported ${filteredProducts.size} designs to folder"
                    }
                } catch (e: Exception) {
                    exportFeedback = "Export failed: ${e.message}"
                } finally {
                    isExporting = false
                }
            }
        }
    }

    CaptureProtected(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Palette.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Navigation Bar Header: Title, Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isSelecting) Copy.CatalogueShare.selectCount(selection.count) else "Catalogue",
                        style = sfPro(34f, JewelWeight.Bold),
                        color = Palette.dark
                    )

                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.load() },
                        enabled = peekProducts == null && !isRefreshing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isRefreshing) "Refreshing…" else "Refresh catalogue", modifier = Modifier.fillMaxWidth())
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelecting) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSelecting(false) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Copy.CatalogueShare.selectCancel,
                                    style = manrope(15f, JewelWeight.Semibold),
                                    color = Palette.dark
                                )
                            }
                        } else {
                            // "Select" button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSelecting(true) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Copy.CatalogueShare.selectButton,
                                    style = manrope(15f, JewelWeight.Semibold),
                                    color = Palette.dark
                                )
                            }

                            // Add Product (+)
                            IconButton(
                                onClick = { showAddProductSheet = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add product",
                                    tint = Palette.dark
                                )
                            }

                            // More Overflow Menu (Shared links, Export to folder)
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More actions",
                                        tint = Palette.dark
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Share,
                                                    contentDescription = null,
                                                    tint = Palette.dark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(Copy.CatalogueShare.sharedLinksMenu, color = Palette.dark)
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            showSharedLinksScreen = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DriveFolderUpload,
                                                    contentDescription = null,
                                                    tint = Palette.dark,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text("Export to folder", color = Palette.dark)
                                            }
                                        },
                                        onClick = {
                                            showMoreMenu = false
                                            exportLauncher.launch(null)
                                        },
                                        enabled = !isExporting && filteredProducts.isNotEmpty()
                                    )
                                }
                            }
                        }
                    }
                }

            refreshError?.let { message ->
                Text(message, style = manrope(12f, JewelWeight.Medium), color = Palette.muted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            if (peekProducts == null && (products.isNotEmpty() || hasMore)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val total = (catalogState as? CatalogUiState.Content)?.totalCount ?: products.size
                    Text("${products.size} of $total products loaded", style = manrope(12f), color = Palette.muted)
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp).size(20.dp), strokeWidth = 2.dp)
                    } else if (hasMore) {
                        TextButton(onClick = { viewModel.loadAllRemaining() }) { Text("Load all remaining") }
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Palette.muted,
                        modifier = Modifier.size(18.dp)
                    )

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(Palette.dark),
                        textStyle = manrope(14f, JewelWeight.Regular).copy(color = Palette.dark),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search products by title or category...",
                                    style = manrope(14f, JewelWeight.Regular),
                                    color = Palette.muted
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Palette.muted,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Category Horizontal Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATALOG_CATEGORIES.forEach { category ->
                    val isSelected = if (category == "All") {
                        selectedCategory == null || selectedCategory == "All"
                    } else {
                        selectedCategory?.equals(category, ignoreCase = true) == true
                    }

                    CategoryChip(
                        title = category,
                        isSelected = isSelected,
                        onClick = {
                            selectedCategory = if (category == "All") null else category
                        }
                    )
                }
            }

            // Content Area
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Palette.dark,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else if (errorMessage != null && products.isEmpty()) {
                CatalogErrorView(message = errorMessage, onRetry = { viewModel.load() })
            } else if (filteredProducts.isEmpty()) {
                CatalogEmptyView(hasFilter = searchQuery.isNotBlank() || selectedCategory != null)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = if (isSelecting) 160.dp else 96.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        CatalogueProductCard(
                            product = product,
                            onClick = {
                                if (isSelecting) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val notice = viewModel.toggleProductSelection(product)
                                    if (notice != null) {
                                        actionNotice = notice
                                    }
                                } else {
                                    selectedProduct = product
                                }
                            },
                            onEdit = { productToEdit = product },
                            onDelete = { viewModel.deleteProduct(product) },
                            isSelecting = isSelecting,
                            isSelected = selection.contains(product.id)
                        )
                    }
                }
            }
        }

        // Bottom floating capsule button above tab bar
        if (isSelecting) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                val count = selection.count
                val isEnabled = count > 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Palette.dark.copy(alpha = if (isEnabled) 1f else 0.45f))
                        .clickable(enabled = isEnabled) {
                            showShareSheet = true
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Copy.CatalogueShare.shareBarButton(count),
                        style = manrope(14f, JewelWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

    // Share Sheet (M2)
    if (showShareSheet) {
        CatalogShareSheetHost(
            selectedProducts = selection.products,
            onDismiss = { linkCreated ->
                showShareSheet = false
                if (linkCreated) {
                    viewModel.clearSelection()
                    viewModel.setSelecting(false)
                }
            }
        )
    }

    // Shared Links Screen (M2)
    if (showSharedLinksScreen) {
        SharedLinksScreen(
            onBack = { showSharedLinksScreen = false }
        )
    }

    // Product Detail Sheet
    selectedProduct?.let { product ->
        CatalogProductDetailSheetHost(
            product = products.firstOrNull { it.id == product.id } ?: product,
            wholesalerId = resolvedUserId,
            onDismiss = { selectedProduct = null },
            onPublishedChanged = { viewModel.replaceLocal(it) },
            onEdit = { p ->
                selectedProduct = null
                productToEdit = p
            },
            onUpdated = { viewModel.load() }
        )
    }

    // Edit Product Sheet
    productToEdit?.let { prod ->
        CatalogEditProductSheetHost(
            product = prod,
            onDismiss = { productToEdit = null },
            onSaved = { _ ->
                viewModel.load()
                productToEdit = null
            }
        )
    }

    // Add Product Sheet
    if (showAddProductSheet) {
        CatalogAddProductSheetHost(
            wholesalerId = resolvedUserId,
            onDismiss = { showAddProductSheet = false },
            onAdded = { viewModel.load() },
            onOpenUploadHistory = onOpenUploadHistory
        )
    }
}


