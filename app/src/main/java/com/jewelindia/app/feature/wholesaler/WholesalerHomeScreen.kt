// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift
package com.jewelindia.app.feature.wholesaler

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.cirka
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro
import com.jewelindia.app.feature.manufacturing.ManufacturingOffersCard
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.TreasureChestCard
import com.jewelindia.app.feature.treasurechest.WalletState

@Composable
fun WholesalerHomeScreen(
    wholesalerUserId: String = "",
    wholesaler: Wholesaler? = null,
    sessionStore: SessionStore,
    creditStore: CreditStore,
    onUploadClick: () -> Unit,
    onChamakClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onChatClick: () -> Unit,
    onCatalogueClick: () -> Unit,
    onInviteClick: () -> Unit,
    onOffersClick: () -> Unit,
    onReportClick: () -> Unit,
    onTreasureClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenUploadHistory: (() -> Unit)? = null
) {
    val resolvedUserId = wholesalerUserId.ifBlank { sessionStore.user.value?.id ?: "" }
    val homeVm = remember(resolvedUserId) {
        WholesalerHomeViewModel(
            wholesalerUserId = resolvedUserId,
            userEmail = sessionStore.user.value?.email
        )
    }
    val homeState by homeVm.uiState.collectAsState()
    val walletState by creditStore.walletState.collectAsState()
    val wallet = (walletState as? WalletState.Available)?.wallet

    var isLowBalanceDismissed by remember { mutableStateOf(false) }
    var showAddProductSheet by remember { mutableStateOf(false) }

    LaunchedEffect(resolvedUserId) {
        if (resolvedUserId.isNotBlank()) {
            homeVm.load()
        }
    }

    val homeData = (homeState as? HomeUiState.Content)?.data ?: HomeData()
    val productsCount = homeData.productCount
    val pendingOrdersCount = homeData.pendingOrders
    val unreadChatsCount = homeData.unreadChats
    val uploadUsageText = homeData.uploadUsage.display
    val activeOffersCount = homeData.activeOffersCount

    val displayName = wholesaler?.businessName?.ifBlank { "Welcome" } ?: "Welcome"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            // Spacing to clear the floating top capsule
            Spacer(modifier = Modifier.height(44.dp))

            // iOS Large Title: "Home"
            Text(
                text = "Home",
                style = sfPro(34f, JewelWeight.Bold),
                color = Palette.dark,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )

            // Content section with 16dp horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero: Welcome eyebrow + display name + UploadDesignCard
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Welcome",
                        style = sfPro(13f, JewelWeight.Regular),
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayName,
                        style = sfPro(22f, JewelWeight.Medium),
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    UploadDesignCard(
                        onUploadClick = { showAddProductSheet = true }
                    )
                }

                // 2. Error banner if load failed
                val errorMessage = (homeState as? HomeUiState.Error)?.message
                if (errorMessage != null) {
                    HomeErrorBanner(
                        message = errorMessage,
                        onRetry = { homeVm.load() }
                    )
                }

                // 3. Low-balance banner if lowBalance and not dismissed
                if (wallet?.lowBalance == true && !isLowBalanceDismissed) {
                    LowBalanceBanner(
                        wallet = wallet,
                        onBuy = onTreasureClick,
                        onDismiss = { isLowBalanceDismissed = true }
                    )
                }

                // 4. TreasureChestCard (DAILY CREDITS)
                TreasureChestCard(
                    wallet = wallet,
                    creditStore = creditStore,
                    onClick = onTreasureClick
                )

                // 5. Insights section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Insights",
                        style = cirka(34f, JewelWeight.Bold),
                        color = Palette.dark
                    )

                    // Weekly report teaser
                    WholesalerReportTeaser(onClick = onReportClick)

                    // 4 StatCards in a single row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Live Products",
                            value = "$productsCount",
                            icon = Icons.Outlined.Inventory2,
                            showsBadge = false,
                            onClick = onCatalogueClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "New Orders",
                            value = "$pendingOrdersCount",
                            icon = Icons.Outlined.ShoppingBag,
                            showsBadge = pendingOrdersCount > 0,
                            onClick = onOrdersClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "New Chats",
                            value = "$unreadChatsCount",
                            icon = Icons.AutoMirrored.Outlined.Chat,
                            showsBadge = unreadChatsCount > 0,
                            onClick = onChatClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Uploads Today",
                            value = uploadUsageText,
                            icon = Icons.Outlined.ArrowCircleUp,
                            showsBadge = false,
                            onClick = { onOpenUploadHistory?.invoke() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Manufacturing Offers Card
                    ManufacturingOffersCard(
                        count = activeOffersCount,
                        onClick = onOffersClick
                    )

                    // Chamak Card promo
                    ChamakCard(onOpen = onChamakClick)

                    // Invite Retailer Card
                    InviteRetailerCard(onClick = onInviteClick)
                }

                // 6. Latest Creations Section
                LatestCreationsSection(
                    wholesalerUserId = resolvedUserId,
                    onSeeAll = onChamakClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Product Bottom Sheet
    if (showAddProductSheet) {
        AddProductSheet(
            wholesalerId = resolvedUserId,
            userId = resolvedUserId,
            onDismiss = {
                showAddProductSheet = false
                homeVm.load()
            },
            onAdded = {
                showAddProductSheet = false
                homeVm.load()
            },
            onOpenUploadHistory = onOpenUploadHistory
        )
    }
}

@Composable
fun LowBalanceBanner(
    wallet: CreditWallet,
    onBuy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cost = 200
    val fusionsLeft = if (cost > 0) wallet.available / cost else wallet.available

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Palette.statusPending,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Running low on credits",
                style = manrope(13f, JewelWeight.Bold),
                color = Palette.dark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${wallet.available} credits left, about $fusionsLeft more ${if (fusionsLeft == 1) "fusion" else "fusions"}.",
                style = manrope(12f, JewelWeight.Regular),
                color = Color(0xFF92400E)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Palette.dark)
                .clickable(onClick = onBuy)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Buy",
                style = manrope(12f, JewelWeight.Bold),
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = Palette.muted,
            modifier = Modifier
                .size(16.dp)
                .clickable(onClick = onDismiss)
        )
    }
}

@Composable
fun HomeErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = message,
                style = manrope(13f, JewelWeight.Regular),
                color = Palette.dark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Retry",
                style = manrope(13f, JewelWeight.Semibold),
                color = Palette.dark,
                modifier = Modifier.clickable(onClick = onRetry)
            )
        }
    }
}
