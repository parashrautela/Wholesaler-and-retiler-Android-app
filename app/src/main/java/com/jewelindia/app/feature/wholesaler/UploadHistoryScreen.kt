// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/UploadHistoryView.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadHistoryScreen(
    wholesalerId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var usage by remember { mutableStateOf(UploadUsage(used = 0, limit = 0)) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            usage = WholesalerApi.fetchUploadUsage(wholesalerId)
            try {
                val page = WholesalerApi.fetchCatalogue(wholesalerId = wholesalerId, pageSize = 50)
                val today = LocalDate.now()
                products = page.products.filter { p ->
                    val created = p.createdAt
                    if (created.isNullOrBlank()) false
                    else {
                        runCatching {
                            Instant.parse(created).atZone(ZoneId.systemDefault()).toLocalDate() == today
                        }.getOrDefault(false)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't load your uploads. Check your connection and try again."
            }
            isLoading = false
        }
    }

    LaunchedEffect(wholesalerId) {
        if (wholesalerId.isNotBlank()) loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Uploads Today",
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
        containerColor = Palette.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // Usage Progress Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.surfaceElevated)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Upload Progress",
                        style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "${usage.display} Used",
                        style = Typography.santoshi(12.sp, FontWeight.Bold),
                        color = Palette.textSecondary
                    )
                }

                val limit = usage.limit
                if (limit != null && limit > 0) {
                    val progress = (usage.used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = Palette.goldPrimary,
                        trackColor = Palette.goldPrimary.copy(alpha = 0.2f)
                    )
                }
            }

            HorizontalDivider(color = Palette.surfaceBorder)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Palette.goldPrimary)
                }
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Palette.accentRed,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { loadData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.goldPrimary,
                            contentColor = Palette.dark
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Retry", style = Typography.santoshi(14.sp, FontWeight.SemiBold))
                    }
                }
            } else if (products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = Palette.textTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No Uploads Today",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Products you upload today will show up here.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(products, key = { it.id }) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val url = product.displayImageUrl()
                            if (url != null) {
                                ProtectedImage(
                                    url = url,
                                    contentDescription = product.title,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Palette.surfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null, tint = Palette.textTertiary)
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = product.title ?: "Untitled Upload",
                                    style = Typography.santoshi(14.sp, FontWeight.Bold),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = (product.category ?: "Jewellery").replaceFirstChar { it.uppercase() },
                                    style = Typography.santoshi(12.sp),
                                    color = Palette.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Palette.accentGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Uploaded Today",
                                    style = Typography.santoshi(10.sp, FontWeight.SemiBold),
                                    color = Palette.accentGreen
                                )
                            }
                        }
                        HorizontalDivider(color = Palette.surfaceBorder.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
