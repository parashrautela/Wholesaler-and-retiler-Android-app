// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerShell.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.feature.chat.ChatThreadScreen
import com.jewelindia.app.feature.chat.ChatThreadsScreen
import com.jewelindia.app.feature.manufacturing.WholesalerOfferDetailScreen
import com.jewelindia.app.feature.manufacturing.WholesalerOffersListScreen
import com.jewelindia.app.feature.profile.WholesalerProfileScreen
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.TreasureChestScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakStudioScreen
import kotlinx.coroutines.launch

enum class WholesalerTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Catalog("Catalogue", Icons.Filled.GridView),
    Chamak("Chamak", Icons.Filled.AutoAwesome),
    Orders("Orders", Icons.Filled.ShoppingBag),
    Chat("Chat", Icons.AutoMirrored.Filled.Chat)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerShell(sessionStore: SessionStore) {
    var selectedTab by remember { mutableStateOf(WholesalerTab.Home) }
    var wholesaler by remember { mutableStateOf<Wholesaler?>(null) }
    var pendingOrderCount by remember { mutableIntStateOf(0) }
    var unreadChatCount by remember { mutableIntStateOf(0) }

    // Sub-screens & Sheets
    var activeChatThread by remember { mutableStateOf<ChatThread?>(null) }
    var showInviteSheet by remember { mutableStateOf(false) }
    val inviteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInvitationHistory by remember { mutableStateOf(false) }
    var showOffersList by remember { mutableStateOf(false) }
    var selectedOfferId by remember { mutableStateOf<String?>(null) }
    var showReport by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showTreasureChest by remember { mutableStateOf(false) }
    var showUploadHistory by remember { mutableStateOf(false) }
    var selectedCatalogueCategory by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val creditStore = remember { CreditStore(scope) }

    LaunchedEffect(Unit) {
        creditStore.refresh()
        val uid = sessionStore.user.value?.id ?: return@LaunchedEffect
        val email = sessionStore.user.value?.email
        val w = try { WholesalerApi.fetchWholesaler(uid, email) } catch (_: Exception) { null }
        wholesaler = w
        if (w != null) {
            scope.launch {
                pendingOrderCount = try { WholesalerApi.countPendingOrders(w.id) } catch (_: Exception) { 0 }
            }
            scope.launch {
                unreadChatCount = try { WholesalerApi.countUnreadConversations(w.id) } catch (_: Exception) { 0 }
            }
        }
    }

    // Active Chat Thread Screen overlay
    activeChatThread?.let { thread ->
        ChatThreadScreen(
            thread = thread,
            side = "wholesaler",
            onBack = { activeChatThread = null }
        )
        return
    }

    // Selected Offer Detail overlay
    selectedOfferId?.let { offerId ->
        WholesalerOfferDetailScreen(
            offerId = offerId,
            onBack = { selectedOfferId = null }
        )
        return
    }

    // Manufacturing Offers List overlay
    if (showOffersList) {
        WholesalerOffersListScreen(
            onBack = { showOffersList = false },
            onSelectOffer = { selectedOfferId = it.id }
        )
        return
    }

    // Performance Report overlay
    if (showReport) {
        WholesalerReportScreen(onBack = { showReport = false })
        return
    }

    // Profile Screen overlay
    if (showProfile) {
        WholesalerProfileScreen(
            wholesaler = wholesaler,
            sessionStore = sessionStore,
            onBack = { showProfile = false }
        )
        return
    }

    // Treasure Chest overlay
    if (showTreasureChest) {
        TreasureChestScreen(
            wholesaler = wholesaler,
            onBack = { showTreasureChest = false }
        )
        return
    }

    // Invitation History overlay
    if (showInvitationHistory) {
        InvitationHistoryScreen(onBack = { showInvitationHistory = false })
        return
    }

    // Upload History overlay
    if (showUploadHistory) {
        UploadHistoryScreen(
            wholesalerId = wholesaler?.id ?: "",
            onBack = { showUploadHistory = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
    ) {
        // Main Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "wholesaler_tab_content"
            ) { tab ->
                when (tab) {
                    WholesalerTab.Home -> WholesalerHomeScreen(
                        wholesaler = wholesaler,
                        sessionStore = sessionStore,
                        creditStore = creditStore,
                        onUploadClick = {
                            selectedTab = WholesalerTab.Catalog
                        },
                        onChamakClick = { selectedTab = WholesalerTab.Chamak },
                        onOrdersClick = { selectedTab = WholesalerTab.Orders },
                        onChatClick = { selectedTab = WholesalerTab.Chat },
                        onCatalogueClick = { selectedTab = WholesalerTab.Catalog },
                        onInviteClick = { showInviteSheet = true },
                        onOffersClick = { showOffersList = true },
                        onReportClick = { showReport = true },
                        onTreasureClick = { showTreasureChest = true },
                        onProfileClick = { showProfile = true },
                        onSelectCategory = { cat ->
                            selectedCatalogueCategory = cat
                            selectedTab = WholesalerTab.Catalog
                        },
                        onOpenUploadHistory = { showUploadHistory = true }
                    )
                    WholesalerTab.Catalog -> WholesalerCatalogScreen(
                        wholesaler = wholesaler,
                        sessionStore = sessionStore,
                        onOpenUploadHistory = { showUploadHistory = true }
                    )
                    WholesalerTab.Chamak -> ChamakStudioScreen(
                        wholesaler = wholesaler,
                        sessionStore = sessionStore
                    )
                    WholesalerTab.Orders -> WholesalerOrdersScreen(
                        wholesaler = wholesaler,
                        onOrderCountChange = { pendingOrderCount = it }
                    )
                    WholesalerTab.Chat -> ChatThreadsScreen(
                        title = "Queries & Chat",
                        emptyTitle = "No Active Queries",
                        emptyMessage = "When stores ask about your designs, their questions will appear here.",
                        onOpenThread = { thread -> activeChatThread = thread },
                        onUnreadChange = { unreadChatCount = it }
                    )
                }
            }
        }

        // Bottom Navigation Bar
        WholesalerBottomNav(
            selectedTab = selectedTab,
            pendingOrderCount = pendingOrderCount,
            unreadChatCount = unreadChatCount,
            onTabSelected = { selectedTab = it }
        )
    }

    // Invite Retailer Bottom Sheet
    if (showInviteSheet) {
        InviteRetailerSheet(
            sheetState = inviteSheetState,
            onDismiss = { showInviteSheet = false },
            onViewHistory = { showInvitationHistory = true }
        )
    }
}

@Composable
private fun WholesalerBottomNav(
    selectedTab: WholesalerTab,
    pendingOrderCount: Int,
    unreadChatCount: Int,
    onTabSelected: (WholesalerTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.light)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WholesalerTab.entries.forEach { tab ->
            val badge = when (tab) {
                WholesalerTab.Orders -> pendingOrderCount
                WholesalerTab.Chat -> unreadChatCount
                else -> 0
            }
            NavTabItem(
                tab = tab,
                isSelected = selectedTab == tab,
                badge = badge,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    tab: WholesalerTab,
    isSelected: Boolean,
    badge: Int,
    onClick: () -> Unit
) {
    val color = if (isSelected) Palette.goldPrimary else Palette.muted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        BadgedBox(
            badge = {
                if (badge > 0) {
                    Badge {
                        Text(text = if (badge > 99) "99+" else badge.toString(), fontSize = 9.sp)
                    }
                }
            }
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}
