// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerShell.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.runtime.collectAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.components.SvgImage
import com.jewelindia.app.design.manrope
import com.jewelindia.app.feature.chat.ChatThreadScreen
import com.jewelindia.app.feature.chat.ChatThreadsScreen
import com.jewelindia.app.feature.manufacturing.WholesalerOfferDetailScreen
import com.jewelindia.app.feature.manufacturing.WholesalerOffersListScreen
import com.jewelindia.app.feature.profile.WholesalerProfileScreen
import com.jewelindia.app.feature.treasurechest.CreditBalancePill
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.TreasureChestScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakStudioScreen
import kotlinx.coroutines.launch

enum class WholesalerTab(val label: String, val svgName: String?) {
    Home(Copy.WholesalerTab.home, "NavHome"),
    Catalog(Copy.WholesalerTab.catalogue, "NavCatalogue"),
    Chamak(Copy.WholesalerTab.chamak, null),
    Orders(Copy.WholesalerTab.orders, "NavOrders"),
    Chat(Copy.WholesalerTab.chat, "NavChat")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerShell(
    sessionStore: SessionStore,
    initialTab: WholesalerTab = WholesalerTab.Home,
    previewCreditStore: CreditStore? = null,
    previewWholesaler: Wholesaler? = null
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var wholesaler by remember { mutableStateOf<Wholesaler?>(previewWholesaler) }
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
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val creditStore = previewCreditStore ?: remember { CreditStore(scope) }

    val user by sessionStore.user.collectAsState()
    val wholesalerUserId = user?.id ?: ""

    LaunchedEffect(wholesalerUserId) {
        if (previewWholesaler != null) return@LaunchedEffect
        if (wholesalerUserId.isBlank()) return@LaunchedEffect
        creditStore.refresh()
        val email = sessionStore.user.value?.email
        val w = try { WholesalerApi.fetchWholesaler(wholesalerUserId, email) } catch (_: Exception) { null }
        wholesaler = w
        scope.launch {
            pendingOrderCount = try { WholesalerApi.countPendingOrders(wholesalerUserId) } catch (_: Exception) { 0 }
        }
        scope.launch {
            unreadChatCount = try { WholesalerApi.countUnreadConversations(wholesalerUserId) } catch (_: Exception) { 0 }
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
            wholesalerId = wholesalerUserId,
            onBack = { showUploadHistory = false }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            WholesalerTabBar(
                selectedTab = selectedTab,
                pendingOrderCount = pendingOrderCount,
                unreadChatCount = unreadChatCount,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { contentPadding ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (selectedTab == WholesalerTab.Home) Color.White else Palette.background)
    ) {
        // Tab Content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "wholesaler_tab_content",
            modifier = Modifier.fillMaxSize().then(
                if (selectedTab == WholesalerTab.Chamak) {
                    Modifier.padding(contentPadding).consumeWindowInsets(contentPadding).safeDrawingPadding()
                } else Modifier
            )
        ) { tab ->
            when (tab) {
                WholesalerTab.Home -> WholesalerHomeScreen(
                    wholesalerUserId = wholesalerUserId,
                    wholesaler = wholesaler,
                    sessionStore = sessionStore,
                    creditStore = creditStore,
                    onUploadClick = { selectedTab = WholesalerTab.Catalog },
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
                    wholesalerUserId = wholesalerUserId,
                    wholesaler = wholesaler,
                    sessionStore = sessionStore,
                    onOpenUploadHistory = { showUploadHistory = true }
                )
                WholesalerTab.Chamak -> ChamakStudioScreen(
                    wholesalerUserId = wholesalerUserId,
                    wholesaler = wholesaler,
                    sessionStore = sessionStore
                )
                WholesalerTab.Orders -> WholesalerOrdersScreen(
                    wholesalerUserId = wholesalerUserId,
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

        // Top-Right Floating Capsule
        if (selectedTab != WholesalerTab.Chamak) WholesalerTopCapsule(
            creditStore = creditStore,
            onCreditClick = { showTreasureChest = true },
            onProfileClick = { showProfile = true },
            onInviteClick = { showInviteSheet = true },
            onOffersClick = { showOffersList = true },
            onLogoutClick = { showLogoutConfirm = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 16.dp)
        )

    }
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    text = Copy.logoutTitle,
                    style = manrope(18f, JewelWeight.Bold),
                    color = Palette.dark
                )
            },
            text = {
                Text(
                    text = Copy.logoutBody,
                    style = manrope(14f, JewelWeight.Regular),
                    color = Palette.dark
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        scope.launch { sessionStore.signOut() }
                    }
                ) {
                    Text(
                        text = Copy.logoutConfirm,
                        style = manrope(14f, JewelWeight.Bold),
                        color = Color(0xFFDC2626)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(
                        text = Copy.logoutCancel,
                        style = manrope(14f, JewelWeight.Medium),
                        color = Palette.muted
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
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
fun WholesalerTopCapsule(
    creditStore: CreditStore,
    onCreditClick: () -> Unit,
    onProfileClick: () -> Unit,
    onInviteClick: () -> Unit,
    onOffersClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x1F000000),
                    spotColor = Color(0x1F000000)
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(0.5.dp, Color(0xFFE5E5EA), CircleShape)
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreditBalancePill(
                creditStore = creditStore,
                onClick = onCreditClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                SvgImage(
                    name = "JewelLogo",
                    contentDescription = "More options",
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(0.5.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
        ) {
            DropdownMenuItem(
                text = { Text("Profile", style = manrope(14f, JewelWeight.Medium), color = Palette.dark) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = Palette.dark,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onProfileClick()
                }
            )
            DropdownMenuItem(
                text = { Text(Copy.WholesalerTab.inviteRetailer, style = manrope(14f, JewelWeight.Medium), color = Palette.dark) },
                leadingIcon = {
                    SvgImage(
                        name = "NavAddRetailer",
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Palette.dark),
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onInviteClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Manufacturing Offers", style = manrope(14f, JewelWeight.Medium), color = Palette.dark) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        tint = Palette.dark,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onOffersClick()
                }
            )
            HorizontalDivider(
                color = Color(0xFFE5E5EA),
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text(Copy.logoutConfirm, style = manrope(14f, JewelWeight.Medium), color = Color(0xFFDC2626)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onLogoutClick()
                }
            )
        }
    }
}

@Composable
fun WholesalerTabBar(
    selectedTab: WholesalerTab,
    pendingOrderCount: Int,
    unreadChatCount: Int,
    onTabSelected: (WholesalerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
            .height(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x28000000),
                spotColor = Color(0x28000000)
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.94f))
            .border(0.5.dp, Color(0xFFE5E5EA), CircleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WholesalerTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val badge = when (tab) {
                WholesalerTab.Orders -> pendingOrderCount
                WholesalerTab.Chat -> unreadChatCount
                else -> 0
            }
            WholesalerTabItem(
                tab = tab,
                isSelected = isSelected,
                badge = badge,
                onClick = { onTabSelected(tab) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun WholesalerTabItem(
    tab: WholesalerTab,
    isSelected: Boolean,
    badge: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) Palette.dark else Color(0xFF1E2124).copy(alpha = 0.70f)
    val fontWeight = if (isSelected) JewelWeight.Semibold else JewelWeight.Medium

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isSelected) Modifier.background(Color(0xFFEFEFEF)) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            BadgedBox(
                badge = {
                    if (badge > 0) {
                        Badge(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ) {
                            Text(if (badge > 99) "99+" else badge.toString(), fontSize = 9.sp)
                        }
                    }
                }
            ) {
                if (tab.svgName != null) {
                    SvgImage(
                        name = tab.svgName,
                        contentDescription = tab.label,
                        colorFilter = ColorFilter.tint(contentColor),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                style = manrope(11f, fontWeight),
                color = contentColor
            )
        }
    }
}
