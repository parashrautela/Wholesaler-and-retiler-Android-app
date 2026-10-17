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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.security.CaptureProtected
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro
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
    onOpenUploadHistory: (() -> Unit)? = null
) {
    val resolvedUserId = wholesalerUserId.ifBlank {
        wholesaler?.userId ?: sessionStore.user.value?.id ?: ""
    }
    val viewModel = remember(resolvedUserId) {
        WholesalerCatalogViewModel(wholesalerUserId = resolvedUserId)
    }
    val catalogState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddProductSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(exportFeedback) {
        exportFeedback?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(resolvedUserId) {
        if (peekProducts == null && resolvedUserId.isNotBlank()) {
            viewModel.load()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Palette.background)
                .statusBarsPadding()
        ) {
            // Navigation Bar Header: Title, Add button, Export button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Catalogue",
                    style = sfPro(34f, JewelWeight.Bold),
                    color = Palette.dark
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { exportLauncher.launch(null) },
                        enabled = !isExporting && filteredProducts.isNotEmpty()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Palette.dark, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.DriveFolderUpload,
                                contentDescription = "Export to folder",
                                tint = Palette.dark
                            )
                        }
                    }

                    IconButton(
                        onClick = { showAddProductSheet = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add product",
                            tint = Palette.dark
                        )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        style = manrope(14f, JewelWeight.Medium),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Palette.cream)
                            .clickable { viewModel.load() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Try Again",
                            style = manrope(14f, JewelWeight.Semibold),
                            color = Palette.dark
                        )
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = Palette.muted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Products Found",
                        style = manrope(16f, JewelWeight.Bold),
                        color = Palette.dark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                            "Try adjusting your search or category filter."
                        } else {
                            "Tap + above to upload your first jewellery design."
                        },
                        style = manrope(13f, JewelWeight.Regular),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        CatalogueProductCard(
                            product = product,
                            onClick = { selectedProduct = product },
                            onEdit = { productToEdit = product },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }

    // Product Detail Sheet
    selectedProduct?.let { product ->
        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            ProductDetailSheet(
                product = product,
                wholesalerId = resolvedUserId,
                onDismiss = { selectedProduct = null },
                onPublishToggle = { updated ->
                    viewModel.togglePublish(updated)
                },
                onEdit = { p ->
                    selectedProduct = null
                    productToEdit = p
                },
                onUpdated = { _ ->
                    viewModel.load()
                }
            )
        }
    }

    // Edit Product Sheet
    productToEdit?.let { prod ->
        ModalBottomSheet(
            onDismissRequest = { productToEdit = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            EditProductScreen(
                product = prod,
                onDismiss = { productToEdit = null },
                onSaved = { _ ->
                    viewModel.load()
                    productToEdit = null
                }
            )
        }
    }

    // Add Product Sheet
    if (showAddProductSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddProductSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            AddProductSheet(
                wholesalerId = resolvedUserId,
                userId = resolvedUserId,
                onDismiss = { showAddProductSheet = false },
                onAdded = { _ ->
                    viewModel.load()
                    showAddProductSheet = false
                },
                onOpenUploadHistory = {
                    showAddProductSheet = false
                    onOpenUploadHistory?.invoke()
                }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) Palette.dark else Color.White)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Palette.border,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = manrope(13f, if (isSelected) JewelWeight.Bold else JewelWeight.Medium),
            color = if (isSelected) Color.White else Palette.foreground
        )
    }
}
