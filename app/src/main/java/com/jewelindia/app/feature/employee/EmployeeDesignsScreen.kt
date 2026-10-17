// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeDesignsView.swift
package com.jewelindia.app.feature.employee

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.data.api.RetailerDesign
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDesignsScreen(
    session: EmployeeSession,
    onBack: () -> Unit,
    onSelectDesign: (RetailerDesign) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var designs by remember { mutableStateOf<List<RetailerDesign>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "In Stock", "Made to Order", "Newest")

    fun load() {
        scope.launch {
            try {
                val list = EmployeeApi.fetchDesigns(session.retailerId, newestFirst = (selectedFilter == "Newest"))
                designs = list
            } catch (_: Exception) {
            } finally {
                loading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(session.retailerId, selectedFilter) {
        load()
    }

    val filteredDesigns = remember(designs, selectedFilter) {
        when (selectedFilter) {
            "In Stock" -> designs.filter { it.isInStock == true }
            "Made to Order" -> designs.filter { it.isInStock == false }
            else -> designs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store In-House Designs",
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
                // Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Palette.goldPrimary else Palette.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                            )
                        ) {
                            Text(
                                text = filter,
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
                } else if (filteredDesigns.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No in-house designs found for this store.",
                            style = Typography.santoshi(14.sp),
                            color = Palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredDesigns, key = { it.id }) { design ->
                            Card(
                                onClick = { onSelectDesign(design) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Palette.surfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                    ) {
                                        CachedImage(
                                            url = design.imageLink,
                                            contentDescription = design.cardTitle,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = design.cardTitle,
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
                                                text = design.purity ?: "22KT",
                                                style = Typography.santoshi(11.sp),
                                                color = Palette.goldPrimary
                                            )
                                            design.netWeight?.let { w ->
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
                    }
                }
            }
        }
    }
}
