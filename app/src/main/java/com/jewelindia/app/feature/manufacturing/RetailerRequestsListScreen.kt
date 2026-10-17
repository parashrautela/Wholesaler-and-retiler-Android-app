// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/RetailerRequestsListView.swift
package com.jewelindia.app.feature.manufacturing

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.retailer.CategoryFilterPill
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerRequestsListScreen(
    userId: String,
    onSelectRequest: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf<List<ManufacturingRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }

    suspend fun loadRequests() {
        try {
            requests = ManufacturingApi.fetchRetailerRequests()
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadRequests()
    }

    val filterOptions = listOf("All", "Routing", "Quoted", "Awarded", "Cancelled")

    val filteredRequests = remember(requests, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> requests.filter { it.state == "routing" || it.state == "open" }
            2 -> requests.filter { it.state == "quoted" }
            3 -> requests.filter { it.state == "awarded" }
            4 -> requests.filter { it.state == "cancelled" }
            else -> requests
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Custom Enquiries",
                        style = Typography.cirka(22.sp),
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
                    IconButton(onClick = { showCreateSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New Enquiry",
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
                scope.launch { loadRequests() }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Pills
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions.size) { idx ->
                        CategoryFilterPill(
                            label = filterOptions[idx],
                            isSelected = selectedFilterIndex == idx,
                            onClick = { selectedFilterIndex = idx }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Palette.goldPrimary)
                            }
                        }

                        filteredRequests.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Handyman,
                                    contentDescription = null,
                                    tint = Palette.textTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Manufacturing Enquiries",
                                    style = Typography.cirka(20.sp),
                                    color = Palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Broadcast custom design specs, weight budgets, and sketches to verified manufacturers across India.",
                                    style = Typography.santoshi(14.sp),
                                    color = Palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { showCreateSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("New Custom Enquiry", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredRequests, key = { it.id }) { req ->
                                    RetailerRequestCard(
                                        request = req,
                                        onClick = { onSelectRequest(req.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateManufacturingRequestSheet(
            userId = userId,
            onDismiss = { showCreateSheet = false },
            onRequestCreated = {
                showCreateSheet = false
                scope.launch { loadRequests() }
            }
        )
    }
}

@Composable
fun RetailerRequestCard(
    request: ManufacturingRequest,
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
                url = request.imageUrl,
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
                    text = "${request.material} ${request.category}",
                    style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RequestStatusPill(state = request.state)
            }

            Text(
                text = "${request.purity ?: "22KT"} · ${request.formattedWeightRange}",
                style = Typography.santoshi(13.sp),
                color = Palette.goldPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${request.quotes?.size ?: 0} quotes received",
                    style = Typography.santoshi(12.sp),
                    color = Palette.textSecondary
                )
                Text(
                    text = "Need by ${request.deliveryNeededDate.take(10)}",
                    style = Typography.santoshi(12.sp),
                    color = Palette.textSecondary
                )
            }
        }
    }
}
