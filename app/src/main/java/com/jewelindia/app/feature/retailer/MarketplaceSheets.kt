// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/YourTasteView.swift (Product sheets and cards)
package com.jewelindia.app.feature.retailer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun MarketplaceProductCard(
    product: Product,
    isShortlisted: Boolean,
    onToggleShortlist: () -> Unit,
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
            // Product Image with Like Button overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Palette.surface)
            ) {
                ProtectedImage(
                    url = product.primaryImage,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                LikeButton(
                    isLiked = isShortlisted,
                    onToggle = onToggleShortlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Weight badge
                if (product.weightGrams != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "${product.weightGrams}g",
                            style = Typography.santoshi(11.sp, FontWeight.Medium),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Product Meta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.displayTitle,
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.purity ?: "22KT",
                        style = Typography.santoshi(12.sp),
                        color = Palette.goldPrimary
                    )

                    product.category?.let { cat ->
                        Text(
                            text = cat,
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceProductDetailSheet(
    product: Product,
    isShortlisted: Boolean,
    onToggleShortlist: () -> Unit,
    onDismiss: () -> Unit,
    onOpenChat: (ChatThread) -> Unit,
    onOpenOrderRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var startingChat by remember { mutableStateOf(false) }

    LaunchedEffect(product.id) {
        StoreActivity.log(StoreActivity.Event.DesignViewed, productId = product.id)
    }

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
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Image Carousel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .background(Palette.surface)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    ProtectedImage(
                        url = images[page],
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Page indicator
                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(images.size) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == idx) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == idx) Palette.goldPrimary
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }

                // Heart button on top right
                LikeButton(
                    isLiked = isShortlisted,
                    onToggle = onToggleShortlist,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    size = 40.dp,
                    iconSize = 22.dp
                )
            }

            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = product.displayTitle,
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = product.category ?: "Fine Jewellery",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Specification Grid
                Text(
                    text = "SPECIFICATIONS",
                    style = Typography.santoshi(12.sp, FontWeight.Bold),
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
                    SpecColumn("In Stock", if (product.inStock == true) "Ready to ship" else "Made to order")
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                // Actions: Chat & Order Request
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!startingChat) {
                                startingChat = true
                                scope.launch {
                                    try {
                                        val convId = ChatApi.open(product.id)
                                        val thread = ChatThread(
                                            id = convId,
                                            side = "retailer",
                                            productID = product.id,
                                            productTitle = product.title,
                                            productImage = product.primaryImage
                                        )
                                        onOpenChat(thread)
                                    } catch (_: Exception) {
                                    } finally {
                                        startingChat = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.surfaceElevated),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .border(1.dp, Palette.goldPrimary, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (startingChat) {
                            CircularProgressIndicator(color = Palette.goldPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Palette.goldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Ask Wholesaler",
                                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                    color = Palette.goldPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onOpenOrderRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingBag,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Request Order",
                                style = Typography.santoshi(14.sp, FontWeight.Bold),
                                color = Color.Black
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

