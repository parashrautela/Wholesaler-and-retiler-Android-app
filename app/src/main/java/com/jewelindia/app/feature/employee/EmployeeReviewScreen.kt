// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeReviewView.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeReviewScreen(
    store: EmployeeStore,
    onBack: () -> Unit,
    onRequestSent: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState()

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var submitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedIds = state.selectedProductIds.toList()

    LaunchedEffect(selectedIds) {
        if (selectedIds.isEmpty()) {
            products = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        try {
            val list = EmployeeApi.fetchProducts(selectedIds)
            products = list
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Selected Pieces (${selectedIds.size})",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Palette.textPrimary
                        )
                    }
                },
                actions = {
                    if (selectedIds.isNotEmpty()) {
                        TextButton(onClick = { store.clearSelections() }) {
                            Text(
                                text = "Clear All",
                                style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                                color = Palette.accentRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surfaceElevated)
            )
        },
        bottomBar = {
            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = Palette.surfaceElevated,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Palette.surfaceBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        errorMessage?.let { err ->
                            Text(
                                text = err,
                                style = Typography.santoshi(12.sp),
                                color = Palette.accentRed
                            )
                        }

                        Button(
                            onClick = {
                                submitting = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        // Batch into groups of up to 20 IDs per iOS spec
                                        val chunks = selectedIds.chunked(20)
                                        for (chunk in chunks) {
                                            JewelApi.createOrders(chunk)
                                        }
                                        val totalCount = selectedIds.size
                                        store.clearSelections()
                                        onRequestSent(totalCount)
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Failed to submit order requests."
                                    } finally {
                                        submitting = false
                                    }
                                }
                            },
                            enabled = !submitting && selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.goldPrimary,
                                contentColor = Palette.light
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (submitting) {
                                CircularProgressIndicator(color = Palette.light, modifier = Modifier.size(22.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Send Request to Wholesalers (${selectedIds.size})",
                                        style = Typography.santoshi(15.sp, FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Palette.background,
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Palette.goldPrimary)
                }
            } else if (selectedIds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "No pieces selected yet.",
                            style = Typography.cirka(20.sp),
                            color = Palette.textPrimary
                        )
                        Text(
                            text = "Long-press any design in the catalogue or canvas to select pieces for wholesale orders.",
                            style = Typography.santoshi(14.sp),
                            color = Palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Palette.goldPrimary,
                                contentColor = Palette.light
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Browse Catalogue", style = Typography.santoshi(14.sp, FontWeight.Bold))
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        Card(
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
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    CachedImage(
                                        url = product.primaryImage,
                                        contentDescription = product.displayTitle,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = product.displayTitle,
                                        style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                                        color = Palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${product.purity ?: "22KT"} · ${product.category ?: "Fine Jewellery"}",
                                        style = Typography.santoshi(12.sp),
                                        color = Palette.goldPrimary
                                    )
                                    product.netWeight?.let { w ->
                                        Text(
                                            text = "Net weight: ${w}g",
                                            style = Typography.santoshi(12.sp),
                                            color = Palette.textSecondary
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { store.toggleSelection(product.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove",
                                        tint = Palette.accentRed
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

// MARK: - Celebratory Request Sent Screen

@Composable
fun RequestSentScreen(
    count: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Palette.goldPrimary.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(2.dp, Palette.goldPrimary),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Palette.goldPrimary,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Text(
                text = "Order Requests Sent!",
                style = Typography.cirka(26.sp),
                color = Palette.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your request for $count ${if (count == 1) "jewellery piece" else "jewellery pieces"} has been transmitted directly to verified manufacturers. They will review stock availability and production timelines.",
                style = Typography.santoshi(14.sp),
                color = Palette.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.goldPrimary,
                    contentColor = Palette.light
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Back to Showroom",
                    style = Typography.santoshi(15.sp, FontWeight.Bold)
                )
            }
        }
    }
}
