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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.manufacturing.ManufacturingOffersCard
import com.jewelindia.app.feature.treasurechest.CreditBalancePill
import com.jewelindia.app.feature.treasurechest.CreditStore
import kotlinx.coroutines.async
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val resolvedUserId = wholesalerUserId.ifBlank { sessionStore.user.value?.id ?: "" }
    val homeVm = remember(resolvedUserId) {
        WholesalerHomeViewModel(
            wholesalerUserId = resolvedUserId,
            userEmail = sessionStore.user.value?.email
        )
    }
    val homeState by homeVm.uiState.collectAsState()

    LaunchedEffect(resolvedUserId) {
        if (resolvedUserId.isNotBlank()) {
            homeVm.load()
        }
    }

    val homeData = (homeState as? HomeUiState.Content)?.data ?: HomeData()
    val loading = homeState is HomeUiState.Loading
    val productsCount = homeData.productCount
    val pendingOrdersCount = homeData.pendingOrders
    val unreadChatsCount = homeData.unreadChats
    val uploadUsageText = homeData.uploadUsage.display
    val activeOffersCount = homeData.activeOffersCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.White)
            .statusBarsPadding()
    ) {
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacing to clear the floating top capsule
            Spacer(modifier = Modifier.height(44.dp))

            // iOS Large Title: "Home"
            Text(
                text = "Home",
                style = com.jewelindia.app.design.sfPro(34f, com.jewelindia.app.design.JewelWeight.Bold),
                color = Palette.dark,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Upload Card
                HeroUploadCard(onUploadClick = onUploadClick)

            // Insights Grid (4 KPI cards)
            Text(
                text = "Overview",
                style = Typography.cirka(18.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "Designs",
                    value = "$productsCount",
                    icon = Icons.Default.GridView,
                    onClick = onCatalogueClick,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Pending Orders",
                    value = "$pendingOrdersCount",
                    icon = Icons.Default.ShoppingBag,
                    onClick = onOrdersClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "Conversations",
                    value = "$unreadChatsCount",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "Uploads Today",
                    value = uploadUsageText,
                    icon = Icons.Default.CloudUpload,
                    onClick = { onOpenUploadHistory?.invoke() },
                    modifier = Modifier.weight(1f)
                )
            }

            // Chamak Studio Promo Card
            ChamakPromoCard(onClick = onChamakClick)

            // Invite Retailer Card
            InviteRetailerCard(onClick = onInviteClick)

            // Manufacturing Offers Card
            ManufacturingOffersCard(
                count = activeOffersCount,
                onClick = onOffersClick
            )

            // Performance Report Teaser
            WholesalerReportTeaser(onClick = onReportClick)

            // Category Quick Jump
            Text(
                text = "Catalogue Categories",
                style = Typography.cirka(18.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            val categories = listOf("Chains", "Necklaces", "Bangles", "Rings", "Earrings", "Mangalsutra", "Pendants", "Bracelets")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in categories.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryGridItem(
                            name = categories[i],
                            onClick = {
                                onSelectCategory(categories[i])
                                onCatalogueClick()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (i + 1 < categories.size) {
                            CategoryGridItem(
                                name = categories[i + 1],
                                onClick = {
                                    onSelectCategory(categories[i + 1])
                                    onCatalogueClick()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
}



