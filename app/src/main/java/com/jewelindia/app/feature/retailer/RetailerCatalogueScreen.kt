// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/RetailerCatalogueView.swift
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.RetailerApi
import com.jewelindia.app.data.api.RetailerDesign
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerCatalogueScreen(
    userId: String,
    onOpenAddDesign: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var designs by remember { mutableStateOf<List<RetailerDesign>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDesignForDetail by remember { mutableStateOf<RetailerDesign?>(null) }
    var designToDelete by remember { mutableStateOf<RetailerDesign?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    suspend fun loadDesigns() {
        try {
            designs = RetailerApi.fetchDesigns()
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadDesigns()
    }

    val categories = remember(designs) {
        designs.mapNotNull { it.category ?: it.type }.distinct()
    }

    val filteredDesigns = remember(designs, selectedCategory) {
        if (selectedCategory == null) designs
        else designs.filter { (it.category ?: it.type) == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store Catalogue",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onOpenAddDesign) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Design",
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
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                scope.launch { loadDesigns() }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Category Pills
                if (categories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            CategoryFilterPill(
                                label = "All (${designs.size})",
                                isSelected = selectedCategory == null,
                                onClick = { selectedCategory = null }
                            )
                        }
                        items(categories) { cat ->
                            CategoryFilterPill(
                                label = cat,
                                isSelected = selectedCategory == cat,
                                onClick = { selectedCategory = cat }
                            )
                        }
                    }
                }

                // Grid or Empty State
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Palette.goldPrimary)
                            }
                        }

                        filteredDesigns.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GridView,
                                    contentDescription = null,
                                    tint = Palette.textTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Designs Uploaded Yet",
                                    style = Typography.cirka(20.sp),
                                    color = Palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add your own store jewellery pieces so sales staff can present them to customers.",
                                    style = Typography.santoshi(14.sp),
                                    color = Palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = onOpenAddDesign,
                                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Add First Design", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        else -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredDesigns, key = { it.id }) { design ->
                                    StoreDesignCard(
                                        design = design,
                                        onClick = { selectedDesignForDetail = design }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Design Detail Sheet
    selectedDesignForDetail?.let { design ->
        StoreDesignDetailSheet(
            design = design,
            onDismiss = { selectedDesignForDetail = null },
            onDeleteClick = {
                selectedDesignForDetail = null
                designToDelete = design
            }
        )
    }

    // Delete Confirmation Dialog
    designToDelete?.let { design ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) designToDelete = null },
            title = {
                Text(
                    text = "Delete Design?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${design.cardTitle}\"? This will permanently remove it from your store catalogue.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                RetailerApi.deleteDesign(design)
                                designs = designs.filter { it.id != design.id }
                            } catch (_: Exception) {
                            } finally {
                                isDeleting = false
                                designToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { designToDelete = null },
                    enabled = !isDeleting
                ) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}

@Composable
fun StoreDesignCard(
    design: RetailerDesign,
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Palette.surface)
            ) {
                ProtectedImage(
                    url = design.imageLink,
                    contentDescription = design.cardTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (design.isInStock == true) {
                    Surface(
                        color = Palette.accentGreen.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "In Stock",
                            style = Typography.santoshi(11.sp, FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = design.cardTitle,
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = design.purity ?: "22KT",
                        style = Typography.santoshi(12.sp),
                        color = Palette.goldPrimary
                    )
                    design.netWeight?.let {
                        Text(
                            text = "${it}g",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDesignDetailSheet(
    design: RetailerDesign,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
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
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .background(Palette.surface)
            ) {
                ProtectedImage(
                    url = design.imageLink,
                    contentDescription = design.cardTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = design.cardTitle,
                            style = Typography.cirka(22.sp),
                            color = Palette.textPrimary
                        )
                        Text(
                            text = design.category ?: design.type ?: "Store Jewellery",
                            style = Typography.santoshi(14.sp),
                            color = Palette.textSecondary
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Palette.accentRed
                        )
                    }
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecColumn("Purity", design.purity ?: "22KT")
                    SpecColumn("Net Weight", design.netWeight?.let { "${it}g" } ?: "N/A")
                    SpecColumn("Gross Weight", design.grossWeight?.let { "${it}g" } ?: "N/A")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SpecColumn("Jewellery Type", design.type ?: "N/A")
                    SpecColumn("Style Aesthetic", design.styleAesthetic ?: "Traditional")
                    SpecColumn("Availability", if (design.isInStock == true) "In Stock" else "${design.productionTimeDays ?: 14} days")
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
