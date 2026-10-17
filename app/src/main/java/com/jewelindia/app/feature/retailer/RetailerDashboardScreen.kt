// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/RetailerDashboardView.swift
package com.jewelindia.app.feature.retailer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Retailer
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.LikeBook
import com.jewelindia.app.data.api.OrdersApi
import com.jewelindia.app.data.api.RetailerApi
import com.jewelindia.app.data.api.StaffAccountsApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerDashboardScreen(
    retailer: Retailer?,
    onNavigateToMarketplace: () -> Unit,
    onNavigateToCatalogue: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToWishlists: () -> Unit,
    onOpenChamak: () -> Unit,
    onOpenAddDesign: () -> Unit,
    onOpenAddStaff: () -> Unit,
    onOpenImageSearch: () -> Unit,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var refreshing by remember { mutableStateOf(false) }
    var shortlistedCount by remember { mutableIntStateOf(0) }
    var staffCount by remember { mutableIntStateOf(0) }
    var wishlistsCount by remember { mutableIntStateOf(0) }
    var ordersCount by remember { mutableIntStateOf(0) }
    var ownDesignsCount by remember { mutableIntStateOf(0) }

    suspend fun loadMetrics() {
        val deferredLikes = scope.async {
            runCatching {
                LikeBook.load(force = true)
                LikeBook.liked.value.size
            }.getOrDefault(0)
        }
        val deferredStaff = scope.async {
            runCatching { StaffAccountsApi.fetchStaff().size }.getOrDefault(0)
        }
        val deferredWishlists = scope.async {
            runCatching { WishlistApi.fetchCustomers().size }.getOrDefault(0)
        }
        val deferredOrders = scope.async {
            runCatching { JewelApi.fetchRetailerOrders().orders.size }.getOrDefault(0)
        }
        val deferredDesigns = scope.async {
            runCatching { RetailerApi.fetchDesigns().size }.getOrDefault(0)
        }

        shortlistedCount = deferredLikes.await()
        staffCount = deferredStaff.await()
        wishlistsCount = deferredWishlists.await()
        ordersCount = deferredOrders.await()
        ownDesignsCount = deferredDesigns.await()
    }

    LaunchedEffect(Unit) {
        retailer?.userId?.let {
            RetailerApi.markDashboardVisited(it)
        }
        loadMetrics()
    }

    val staffPortalUrl = remember {
        val base = AppConfig.siteURL.trimEnd('/')
        "$base/staff"
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                loadMetrics()
                refreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Palette.background)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = retailer?.businessName?.ifBlank { "Retail Store" } ?: "Retail Store",
                    style = Typography.cirka(26.sp),
                    color = Palette.textPrimary
                )
                Text(
                    text = listOfNotNull(retailer?.city, retailer?.state).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Verified Jeweller Portal" },
                    style = Typography.santoshi(14.sp),
                    color = Palette.goldPrimary
                )
            }

            // Hero Metric Cards (2x2 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Shortlisted Designs",
                        count = shortlistedCount,
                        icon = Icons.Filled.Favorite,
                        iconTint = Palette.accentRed,
                        onClick = onNavigateToMarketplace,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Customer Boards",
                        count = wishlistsCount,
                        icon = Icons.Filled.GridView,
                        iconTint = Palette.goldPrimary,
                        onClick = onNavigateToWishlists,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Active Staff",
                        count = staffCount,
                        icon = Icons.Filled.People,
                        iconTint = Color(0xFF64B5F6),
                        onClick = onNavigateToStaff,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Placed Orders",
                        count = ordersCount,
                        icon = Icons.Filled.ShoppingBag,
                        iconTint = Palette.accentGreen,
                        onClick = onNavigateToOrders,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Action Shortcuts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    style = Typography.cirka(18.sp),
                    color = Palette.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "Discover",
                        icon = Icons.Filled.GridView,
                        onClick = onNavigateToMarketplace,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "Add Design",
                        icon = Icons.Filled.Add,
                        onClick = onOpenAddDesign,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "Visual Search",
                        icon = Icons.Filled.ImageSearch,
                        onClick = onOpenImageSearch,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionButton(
                        title = "Chamak AI",
                        icon = Icons.Filled.AutoAwesome,
                        onClick = onOpenChamak,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Staff Portal URL Link Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.surfaceElevated)
                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Badge,
                                    contentDescription = null,
                                    tint = Palette.goldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Staff Showroom Portal",
                                    style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = "Equip your counter sales staff on iPad or mobile",
                                    style = Typography.santoshi(12.sp),
                                    color = Palette.textSecondary
                                )
                            }
                        }
                    }

                    // Copy Link Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Palette.surface)
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = staffPortalUrl,
                            style = Typography.santoshi(13.sp),
                            color = Palette.goldPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(staffPortalUrl))
                                    Toast.makeText(context, "Staff portal link copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy link",
                                    tint = Palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, "Log in to our store showroom portal: $staffPortalUrl")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Staff Portal Link"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share link",
                                    tint = Palette.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onOpenAddStaff) {
                            Text(
                                text = "+ Add New Employee",
                                style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                                color = Palette.goldPrimary
                            )
                        }
                    }
                }
            }

            // New Arrivals Strip
            NewArrivalsStrip(
                onProductClick = onProductSelected,
                onSeeAllClick = onNavigateToMarketplace
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Palette.textTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = count.toString(),
                    style = Typography.cirka(26.sp),
                    color = Palette.textPrimary
                )
                Text(
                    text = title,
                    style = Typography.santoshi(13.sp),
                    color = Palette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Palette.goldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Palette.goldPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            style = Typography.santoshi(11.sp, FontWeight.Medium),
            color = Palette.textPrimary,
            maxLines = 1
        )
    }
}
