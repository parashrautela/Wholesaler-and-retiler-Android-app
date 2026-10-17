// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/CatalogueView.swift
package com.jewelindia.app.feature.wholesaler

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.security.ProtectedImage
import coil3.compose.AsyncImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerCatalogScreen(
    wholesaler: Wholesaler?,
    sessionStore: SessionStore
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showUploadSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(wholesaler?.id) {
        val wid = wholesaler?.id ?: return@LaunchedEffect
        loading = true
        products = try { WholesalerApi.fetchCatalogue(wid, pageSize = 200).products } catch (_: Exception) { emptyList() }
        loading = false
    }

    val filtered = remember(products, query) {
        if (query.isBlank()) products
        else products.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                    it.jewelleryType?.contains(query, ignoreCase = true) == true ||
                    it.category?.contains(query, ignoreCase = true) == true
        }
    }

    com.jewelindia.app.core.security.CaptureProtected(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "My Catalog",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Palette.dark
                    )
                    Text(
                        text = "${filtered.size} designs",
                        fontSize = 13.sp,
                        color = Palette.muted
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Palette.dark)
                }
            }

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search designs…", color = Palette.muted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Palette.muted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Palette.cream,
                    focusedContainerColor = Palette.cream,
                    unfocusedBorderColor = Palette.border,
                    focusedBorderColor = Palette.goldPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            )

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Palette.goldPrimary)
                }
            } else if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (query.isBlank()) "No designs yet.\nTap + to add your first." else "No results for \"$query\"",
                        color = Palette.muted,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { product ->
                        ProductCard(
                            product = product,
                            onClick = { selectedProduct = product }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showUploadSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Palette.goldPrimary,
            contentColor = Palette.light,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Product")
        }
    }

    // Product Detail Sheet
    selectedProduct?.let { product ->
        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            sheetState = sheetState
        ) {
            ProductDetailSheet(
                product = product,
                wholesalerId = wholesaler?.id ?: "",
                onDismiss = { selectedProduct = null },
                onPublishToggle = { updated ->
                    products = products.map { if (it.id == updated.id) updated else it }
                }
            )
        }
    }

    // Upload Sheet
    if (showUploadSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUploadSheet = false },
            sheetState = sheetState
        ) {
            AddProductSheet(
                wholesalerId = wholesaler?.id ?: "",
                userId = sessionStore.user.value?.id ?: "",
                onDismiss = { showUploadSheet = false },
                onAdded = { newProduct ->
                    products = listOf(newProduct) + products
                    showUploadSheet = false
                }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    val imageUrl = product.displayImageUrl()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .clickable(onClick = onClick)
    ) {
        if (imageUrl != null) {
            ProtectedImage(
                url = imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Publication badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (product.isPublished == true) Color(0x99FFFFFF) else Color(0x80000000)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (product.isPublished == true) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = if (product.isPublished == true) Palette.accentGreen else Palette.light,
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = if (product.isPublished == true) "Live" else "Draft",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Palette.light
                )
            }
        }

        // Title overlay
        if (!product.title.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color(0x80000000))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = product.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Palette.light,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
