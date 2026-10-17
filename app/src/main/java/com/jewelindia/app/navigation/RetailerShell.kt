// Port of: wholesaler ios/JewelIndia/Sources/Navigation/RetailerShell.swift
package com.jewelindia.app.navigation

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.JewelApplication
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Retailer
import com.jewelindia.app.core.model.ViewMode
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.AccountApi
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.data.api.CustomerBoard
import com.jewelindia.app.data.api.RetailerApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.data.api.StoreCustomer
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.chat.ChatThreadScreen
import com.jewelindia.app.feature.chat.ChatThreadsScreen
import com.jewelindia.app.feature.manufacturing.CreateManufacturingRequestSheet
import com.jewelindia.app.feature.manufacturing.RetailerRequestDetailScreen
import com.jewelindia.app.feature.manufacturing.RetailerRequestsListScreen
import com.jewelindia.app.feature.retailer.AddDesignSheet
import com.jewelindia.app.feature.retailer.AddEmployeeSheet
import com.jewelindia.app.feature.retailer.CatalogueImageSearchScreen
import com.jewelindia.app.feature.retailer.EmployeesListScreen
import com.jewelindia.app.feature.retailer.MarketplaceCatalogueStore
import com.jewelindia.app.feature.retailer.PlansScreen
import com.jewelindia.app.feature.retailer.RetailerCatalogueScreen
import com.jewelindia.app.feature.retailer.RetailerDashboardScreen
import com.jewelindia.app.feature.retailer.RetailerOrdersScreen
import com.jewelindia.app.feature.retailer.RetailerOrdersStore
import com.jewelindia.app.feature.retailer.StoreThemeScreen
import com.jewelindia.app.feature.retailer.YourTasteScreen
import com.jewelindia.app.feature.retailer.wishlist.CustomerBoardsScreen
import com.jewelindia.app.feature.retailer.wishlist.CustomerWishlistScreen
import com.jewelindia.app.feature.retailer.wishlist.WishlistShareScreen
import com.jewelindia.app.feature.treasurechest.CreditBalancePill
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.TreasureChestScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakStudioScreen
import kotlinx.coroutines.launch

enum class RetailerTab(val label: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Filled.GridView),
    Catalogue("Store", Icons.Outlined.GridView),
    Employees("Staff", Icons.Outlined.People),
    Wishlists("Wishlists", Icons.Outlined.FavoriteBorder),
    Orders("Orders", Icons.Outlined.ShoppingBag)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerShell(sessionStore: SessionStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appGraph = remember { (context.applicationContext as JewelApplication).graph }

    val user by sessionStore.user.collectAsState()
    var retailer by remember { mutableStateOf<Retailer?>(null) }
    val userId = user?.id.orEmpty()

    val creditStore = remember { CreditStore(scope) }
    val ordersStore = remember { RetailerOrdersStore() }
    val marketplaceStore = remember(userId) { MarketplaceCatalogueStore(context, userId) }

    var selectedTab by remember { mutableStateOf(RetailerTab.Dashboard) }

    // Top menu & Overlays
    var showTopMenu by remember { mutableStateOf(false) }
    var showChamakStudio by remember { mutableStateOf(false) }
    var showChats by remember { mutableStateOf(false) }
    var activeChatThread by remember { mutableStateOf<ChatThread?>(null) }
    var showEnquiries by remember { mutableStateOf(false) }
    var selectedRequestId by remember { mutableStateOf<String?>(null) }
    var showTreasureChest by remember { mutableStateOf(false) }
    var showPlans by remember { mutableStateOf(false) }
    var showStoreTheme by remember { mutableStateOf(false) }

    // Sub-screens & Sheets
    var showAddDesignSheet by remember { mutableStateOf(false) }
    var showAddStaffSheet by remember { mutableStateOf(false) }
    var showImageSearch by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    // Wishlist drilling
    var selectedCustomer by remember { mutableStateOf<com.jewelindia.app.data.api.StoreCustomer?>(null) }
    var selectedBoardForTaste by remember { mutableStateOf<com.jewelindia.app.data.api.CustomerBoard?>(null) }
    var selectedBoardForShare by remember { mutableStateOf<com.jewelindia.app.data.api.CustomerBoard?>(null) }

    LaunchedEffect(Unit) {
        StoreActivity.registerDevice(context)
        creditStore.refresh()
        creditStore.refreshPlan()
        ordersStore.loadIfNeeded()
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            retailer = com.jewelindia.app.data.api.RetailerApi.fetchRetailerProfile(userId)
        }
    }

    // Active Chat Thread overlay
    activeChatThread?.let { thread ->
        ChatThreadScreen(
            thread = thread,
            side = "retailer",
            onBack = { activeChatThread = null }
        )
        return
    }

    // Chamak Studio overlay (full screen)
    if (showChamakStudio) {
        ChamakStudioScreen(
            wholesaler = null,
            sessionStore = sessionStore,
            catalogueSource = ChamakCatalogueSource.StoreDesigns,
            onClose = { showChamakStudio = false }
        )
        return
    }

    // Chats Screen overlay
    if (showChats) {
        ChatThreadsScreen(
            title = "Store Queries",
            emptyTitle = "No chats yet",
            emptyMessage = "Conversations with manufacturers about designs will appear here.",
            onOpenThread = { activeChatThread = it },
            onBack = { showChats = false }
        )
        return
    }

    // Manufacturing Request Detail overlay
    selectedRequestId?.let { reqId ->
        RetailerRequestDetailScreen(
            requestId = reqId,
            onBack = { selectedRequestId = null }
        )
        return
    }

    // Manufacturing Requests List overlay
    if (showEnquiries) {
        RetailerRequestsListScreen(
            userId = userId,
            onSelectRequest = { selectedRequestId = it },
            onBack = { showEnquiries = false }
        )
        return
    }

    // Treasure Chest overlay
    if (showTreasureChest) {
        TreasureChestScreen(
            wholesaler = null,
            onBack = { showTreasureChest = false }
        )
        return
    }

    // Plans overlay
    if (showPlans) {
        PlansScreen(
            onBack = { showPlans = false },
            onOpenTreasureChest = {
                showPlans = false
                showTreasureChest = true
            }
        )
        return
    }

    // Store Theme overlay
    if (showStoreTheme) {
        StoreThemeScreen(
            userId = userId,
            creditStore = creditStore,
            onBack = { showStoreTheme = false }
        )
        return
    }

    // Wishlist board taste view overlay
    selectedBoardForTaste?.let { board ->
        YourTasteScreen(
            store = marketplaceStore,
            boardId = board.id,
            onOpenChat = { activeChatThread = it },
            onBack = { selectedBoardForTaste = null }
        )
        return
    }

    // Wishlist share view overlay
    selectedBoardForShare?.let { board ->
        WishlistShareScreen(
            board = board,
            onBack = { selectedBoardForShare = null }
        )
        return
    }

    // Wishlist customer boards list overlay
    selectedCustomer?.let { cust ->
        CustomerBoardsScreen(
            customer = cust,
            onBack = { selectedCustomer = null },
            onOpenBoard = { board -> selectedBoardForTaste = board },
            onShareBoard = { board -> selectedBoardForShare = board }
        )
        return
    }

    // Visual Search overlay
    if (showImageSearch) {
        CatalogueImageSearchScreen(
            onDismiss = { showImageSearch = false },
            onProductSelected = { /* Will open detail in YourTaste */ },
            onToggleShortlist = { marketplaceStore.toggleShortlist(it) },
            shortlistedIds = marketplaceStore.state.collectAsState().value.shortlistedIds
        )
        return
    }

    // Main Scaffold Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Jewel India",
                        style = Typography.cirka(22.sp),
                        color = Palette.goldPrimary
                    )
                },
                actions = {
                    CreditBalancePill(
                        creditStore = creditStore,
                        onClick = { showTreasureChest = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Store Menu",
                                tint = Palette.textPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            modifier = Modifier.background(Palette.surfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Chamak Studio", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Palette.goldPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showChamakStudio = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Chats", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Palette.textPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showChats = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Custom Enquiries", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Handyman, contentDescription = null, tint = Palette.textPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showEnquiries = true
                                }
                            )

                            HorizontalDivider(color = Palette.surfaceBorder)

                            DropdownMenuItem(
                                text = { Text("Treasure Chest", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null, tint = Palette.goldPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showTreasureChest = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Membership Plans", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Storefront, contentDescription = null, tint = Palette.textPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showPlans = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Store Showroom Theme", color = Palette.textPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Palette, contentDescription = null, tint = Palette.textPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    showStoreTheme = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Employee Showroom View", color = Palette.goldPrimary) },
                                leadingIcon = { Icon(Icons.Filled.People, contentDescription = null, tint = Palette.goldPrimary) },
                                onClick = {
                                    showTopMenu = false
                                    appGraph.viewModeStore.set(ViewMode.employee, userId)
                                    scope.launch { sessionStore.refreshDestination() }
                                }
                            )

                            HorizontalDivider(color = Palette.surfaceBorder)

                            DropdownMenuItem(
                                text = { Text("Delete Account", color = Palette.accentRed) },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Palette.accentRed) },
                                onClick = {
                                    showTopMenu = false
                                    showDeleteConfirm = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Log Out", color = Palette.textSecondary) },
                                leadingIcon = { Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Palette.textSecondary) },
                                onClick = {
                                    showTopMenu = false
                                    scope.launch { sessionStore.signOut() }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.surface)
            ) {
                HorizontalDivider(color = Palette.surfaceBorder, thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RetailerTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val color = if (isSelected) Palette.goldPrimary else Palette.textTertiary

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = color,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                style = Typography.santoshi(11.sp, if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                color = color
                            )
                        }
                    }
                }
            }
        },
        containerColor = Palette.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "retailer_tab_content"
            ) { tab ->
                when (tab) {
                    RetailerTab.Dashboard -> RetailerDashboardScreen(
                        retailer = retailer,
                        onNavigateToMarketplace = { selectedTab = RetailerTab.Wishlists },
                        onNavigateToCatalogue = { selectedTab = RetailerTab.Catalogue },
                        onNavigateToStaff = { selectedTab = RetailerTab.Employees },
                        onNavigateToOrders = { selectedTab = RetailerTab.Orders },
                        onNavigateToWishlists = { selectedTab = RetailerTab.Wishlists },
                        onOpenChamak = { showChamakStudio = true },
                        onOpenAddDesign = { showAddDesignSheet = true },
                        onOpenAddStaff = { showAddStaffSheet = true },
                        onOpenImageSearch = { showImageSearch = true },
                        onProductSelected = { /* Handled in detail */ }
                    )

                    RetailerTab.Catalogue -> RetailerCatalogueScreen(
                        userId = userId,
                        onOpenAddDesign = { showAddDesignSheet = true }
                    )

                    RetailerTab.Employees -> EmployeesListScreen(
                        onOpenAddEmployee = { showAddStaffSheet = true }
                    )

                    RetailerTab.Wishlists -> CustomerWishlistScreen(
                        onOpenMarketplace = {
                            selectedBoardForTaste = com.jewelindia.app.data.api.CustomerBoard(id = "", title = "Marketplace")
                        },
                        onCustomerClick = { customer -> selectedCustomer = customer }
                    )

                    RetailerTab.Orders -> RetailerOrdersScreen(
                        store = ordersStore
                    )
                }
            }
        }
    }

    // Add Design Sheet
    if (showAddDesignSheet) {
        AddDesignSheet(
            userId = userId,
            onDismiss = { showAddDesignSheet = false },
            onDesignAdded = { showAddDesignSheet = false }
        )
    }

    // Add Staff Sheet
    if (showAddStaffSheet) {
        AddEmployeeSheet(
            onDismiss = { showAddStaffSheet = false },
            onEmployeeCreated = { /* Credentials sheet shows inside */ }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete your account?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will permanently delete your store account, staff logins, customer wishlists, and order history. This action cannot be undone.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )
                    if (deleteError != null) {
                        Text(
                            text = deleteError ?: "",
                            style = Typography.santoshi(13.sp),
                            color = Palette.accentRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingAccount = true
                        deleteError = null
                        scope.launch {
                            try {
                                AccountApi.deleteMyAccount()
                                sessionStore.signOut()
                            } catch (e: Exception) {
                                deleteError = e.message ?: "Account deletion failed"
                                isDeletingAccount = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete account permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}
