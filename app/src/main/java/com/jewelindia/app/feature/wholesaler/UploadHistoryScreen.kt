// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/UploadHistoryView.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.jewelindia.app.design.readableContentWidth
import kotlinx.coroutines.launch

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
                products = page.products
            } catch (e: Exception) {
                errorMessage = "Couldn't load your uploads. Check your connection and try again."
            }
            isLoading = false
        }
    }

    LaunchedEffect(wholesalerId) {
        if (wholesalerId.isNotBlank()) loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .readableContentWidth()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // iOS Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }

            Text(
                text = "Uploads Today",
                style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                color = Palette.foreground
            )
        }

        // Usage Progress Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
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
                    color = Palette.foreground
                )
                Text(
                    text = "${usage.used} / ${usage.limit ?: 0} Used",
                    style = Typography.santoshi(12.sp, FontWeight.Bold),
                    color = Palette.muted
                )
            }

            val limit = usage.limit
            if (limit != null && limit > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Palette.cream)
                ) {
                    val fraction = (usage.used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Palette.dark)
                    )
                }
            }
        }

        HorizontalDivider(color = Palette.border, thickness = 1.dp)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Palette.dark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = errorMessage!!,
                        style = Typography.santoshi(14.sp),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Palette.cream)
                            .clickable { loadData() }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Retry",
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Palette.foreground
                        )
                    }
                }
            }
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Palette.muted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Uploads Today",
                        style = Typography.cirka(24.sp),
                        color = Palette.foreground
                    )
                    Text(
                        text = "Products you upload today will show up here.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(products, key = { it.id }) { product ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val imgUrl = product.processedImageURL ?: product.imageURL ?: product.rawImageURL
                        ProtectedImage(
                            url = imgUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = product.title ?: "Untitled Upload",
                                style = Typography.santoshi(14.sp, FontWeight.Bold),
                                color = Palette.foreground,
                                maxLines = 1
                            )
                            Text(
                                text = (product.category ?: "Jewellery").replaceFirstChar { it.uppercase() },
                                style = Typography.santoshi(12.sp),
                                color = Palette.muted,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Uploaded Today",
                                style = Typography.santoshi(10.sp, FontWeight.SemiBold),
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }
        }
    }
}
