// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeDetailViews.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailSheet(
    product: Product,
    session: EmployeeSession,
    store: EmployeeStore,
    onDismiss: () -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    var startingChat by remember { mutableStateOf(false) }

    val isSelected = state.selectedProductIds.contains(product.id)

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
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Image Carousel with Theme Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
            ) {
                // Background theme
                CachedImage(
                    url = session.theme.detailBackground(landscape = false),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CachedImage(
                            url = images[page],
                            contentDescription = product.displayTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Page indicator pills
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(images.size) { index ->
                            val active = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .size(if (active) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (active) Palette.goldPrimary else Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }

                // Close Button
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Product Details
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.displayTitle,
                            style = Typography.cirka(22.sp),
                            color = Palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${product.purity ?: "22KT"} · ${product.category ?: "Fine Jewellery"}",
                            style = Typography.santoshi(14.sp),
                            color = Palette.goldPrimary
                        )
                    }
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Specifications Grid
                Text(
                    text = "SPECIFICATIONS",
                    style = Typography.santoshi(11.sp, FontWeight.Bold),
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
                    SpecColumn("Stock Status", if (product.inStock) "Ready to ship" else "Made to order")
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Actions: Toggle Selection & Enquire via Chat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Chat Enquiry Button
                    OutlinedButton(
                        onClick = {
                            startingChat = true
                            scope.launch {
                                try {
                                    val convId = ChatApi.open(productId = product.id)
                                    val thread = ChatThread(
                                        id = convId,
                                        side = "retailer",
                                        productID = product.id,
                                        productTitle = product.displayTitle,
                                        productImage = product.primaryImage
                                    )
                                    onDismiss()
                                    onOpenChat(thread)
                                } catch (_: Exception) {
                                } finally {
                                    startingChat = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Palette.surfaceBorder)
                    ) {
                        if (startingChat) {
                            CircularProgressIndicator(color = Palette.goldPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Palette.textPrimary)
                                Text("Ask Query", style = Typography.santoshi(14.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                            }
                        }
                    }

                    // Selection Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            store.toggleSelection(product.id)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Palette.goldPrimary else Palette.surfaceElevated,
                            contentColor = if (isSelected) Palette.light else Palette.goldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Palette.goldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isSelected) "Selected ✓" else "Select Piece",
                                style = Typography.santoshi(14.sp, FontWeight.Bold)
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
        Text(text = label, style = Typography.santoshi(11.sp), color = Palette.textSecondary)
        Text(text = value, style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
    }
}
