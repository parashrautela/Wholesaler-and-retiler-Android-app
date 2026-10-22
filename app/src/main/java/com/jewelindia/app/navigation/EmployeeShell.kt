// Port of: wholesaler ios/JewelIndia/Sources/Navigation/EmployeeShell.swift
package com.jewelindia.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.JewelApplication
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.ViewMode
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.chat.ChatThreadScreen
import com.jewelindia.app.feature.chat.ChatThreadsScreen
import com.jewelindia.app.feature.employee.EmployeeCatalogueScreen
import com.jewelindia.app.feature.employee.EmployeeDesignsScreen
import com.jewelindia.app.feature.employee.EmployeeDetailSheet
import com.jewelindia.app.feature.employee.EmployeeHomeScreen
import com.jewelindia.app.feature.employee.EmployeeInfiniteCanvas
import com.jewelindia.app.feature.employee.EmployeeOrdersScreen
import com.jewelindia.app.feature.employee.EmployeePhase
import com.jewelindia.app.feature.employee.EmployeeReviewScreen
import com.jewelindia.app.feature.employee.EmployeeRoute
import com.jewelindia.app.feature.employee.EmployeeStore
import com.jewelindia.app.feature.employee.EmployeeStyle
import com.jewelindia.app.feature.employee.EmployeeTab
import com.jewelindia.app.feature.employee.LocalEmployeeStyle
import com.jewelindia.app.feature.employee.RequestSentScreen
import com.jewelindia.app.feature.profile.StaffProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeShell(sessionStore: SessionStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appGraph = remember { (context.applicationContext as JewelApplication).graph }

    val user by sessionStore.user.collectAsState()
    val userId = user?.id.orEmpty()
    val isRetailerOwner = appGraph.viewModeStore.mode(userId) == ViewMode.employee

    val store = remember(userId) {
        EmployeeStore(
            context = context,
            userId = userId,
            isRetailer = isRetailerOwner,
            scope = scope
        )
    }

    val state by store.state.collectAsState()

    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
    var activeChatThread by remember { mutableStateOf<ChatThread?>(null) }
    var showStylePicker by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }

    when (val phase = state.phase) {
        is EmployeePhase.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Palette.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        }

        is EmployeePhase.Failed -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Palette.background)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Access Unavailable",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = phase.message,
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { store.loadSession() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.goldPrimary,
                            contentColor = Palette.light
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Try Again", style = Typography.santoshi(14.sp, FontWeight.Bold))
                    }
                    TextButton(onClick = { scope.launch { sessionStore.signOut() } }) {
                        Text("Log Out", style = Typography.santoshi(14.sp), color = Palette.accentRed)
                    }
                }
            }
        }

        is EmployeePhase.Ready -> {
            val session = phase.session

            CompositionLocalProvider(LocalEmployeeStyle provides state.style) {
                // Staff Profile overlay
                if (showProfile) {
                    StaffProfileScreen(
                        session = session,
                        sessionStore = sessionStore,
                        onDismiss = { showProfile = false },
                        onBackToDashboard = {
                            showProfile = false
                            appGraph.viewModeStore.set(ViewMode.retailer, userId)
                            scope.launch { sessionStore.refreshDestination() }
                        }
                    )
                    return@CompositionLocalProvider
                }

                // Active Chat Thread overlay
                activeChatThread?.let { thread ->
                    ChatThreadScreen(
                        thread = thread,
                        side = "retailer",
                        onBack = { activeChatThread = null }
                    )
                    return@CompositionLocalProvider
                }

                // Full screen sub-routes
                when (val route = state.activeRoute) {
                    is EmployeeRoute.Review -> {
                        EmployeeReviewScreen(
                            store = store,
                            onBack = { store.popRoute() },
                            onRequestSent = { count ->
                                store.openRoute(EmployeeRoute.RequestSent(count))
                            }
                        )
                        return@CompositionLocalProvider
                    }

                    is EmployeeRoute.Canvas -> {
                        EmployeeInfiniteCanvas(
                            session = session,
                            store = store,
                            onClose = { store.popRoute() },
                            onOpenDetail = { selectedProductForDetail = it },
                            onOpenReview = { store.openRoute(EmployeeRoute.Review) }
                        )
                        return@CompositionLocalProvider
                    }

                    is EmployeeRoute.Designs -> {
                        EmployeeDesignsScreen(
                            session = session,
                            onBack = { store.popRoute() },
                            onSelectDesign = { /* design detail */ }
                        )
                        return@CompositionLocalProvider
                    }

                    is EmployeeRoute.RequestSent -> {
                        RequestSentScreen(
                            count = route.count,
                            onDone = { store.popRoute() }
                        )
                        return@CompositionLocalProvider
                    }

                    is EmployeeRoute.Detail -> {
                        EmployeeDetailSheet(
                            product = route.product,
                            session = session,
                            store = store,
                            onDismiss = { store.popRoute() },
                            onOpenChat = { activeChatThread = it }
                        )
                    }

                    null -> {}
                }

                // Main Shell layout
                Box(modifier = Modifier.fillMaxSize().background(Palette.background)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Retailer Owner Mode Banner
                        if (session.isRetailer) {
                            EmployeeViewBanner(
                                onSwitchBack = {
                                    appGraph.viewModeStore.set(ViewMode.retailer, userId)
                                    scope.launch { sessionStore.refreshDestination() }
                                }
                            )
                        }

                        // Tab content
                        Box(modifier = Modifier.weight(1f)) {
                            when (state.selectedTab) {
                                EmployeeTab.Home -> {
                                    EmployeeHomeScreen(
                                        session = session,
                                        store = store,
                                        onOpenCatalogue = { store.selectTab(EmployeeTab.Catalogue) },
                                        onOpenCanvas = { store.openRoute(EmployeeRoute.Canvas) },
                                        onOpenDetail = { selectedProductForDetail = it },
                                        onOpenStylePicker = { showStylePicker = true },
                                        onOpenProfile = { showProfile = true }
                                    )
                                }

                                EmployeeTab.Catalogue -> {
                                    EmployeeCatalogueScreen(
                                        session = session,
                                        store = store,
                                        onOpenDetail = { selectedProductForDetail = it },
                                        onOpenReview = { store.openRoute(EmployeeRoute.Review) }
                                    )
                                }

                                EmployeeTab.Queries -> {
                                    ChatThreadsScreen(
                                        title = "Showroom Queries",
                                        emptyTitle = "No queries yet",
                                        emptyMessage = "Queries sent to manufacturers regarding custom pieces will appear here.",
                                        onOpenThread = { activeChatThread = it }
                                    )
                                }

                                EmployeeTab.Orders -> {
                                    EmployeeOrdersScreen(store = store)
                                }
                            }
                        }
                    }

                    // Floating Pill Navigation Bar
                    EmployeePillNav(
                        selectedTab = state.selectedTab,
                        hasUnreadQueries = state.hasUnreadQueries,
                        hasNewOrders = state.hasNewOrders,
                        onSelectTab = { store.selectTab(it) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                    )

                    // Detail Sheet
                    selectedProductForDetail?.let { product ->
                        EmployeeDetailSheet(
                            product = product,
                            session = session,
                            store = store,
                            onDismiss = { selectedProductForDetail = null },
                            onOpenChat = { activeChatThread = it }
                        )
                    }

                    // Style Picker Sheet
                    if (showStylePicker) {
                        ModalBottomSheet(
                            onDismissRequest = { showStylePicker = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                            containerColor = Palette.surfaceElevated
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Personal Visual Theme",
                                    style = Typography.cirka(22.sp),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = "Customise the presentation aesthetic of your showroom view.",
                                    style = Typography.santoshi(13.sp),
                                    color = Palette.textSecondary
                                )

                                HorizontalDivider(color = Palette.surfaceBorder)

                                EmployeeStyle.entries.forEach { style ->
                                    val isSelected = style == state.style
                                    Surface(
                                        onClick = {
                                            store.updateStyle(style)
                                            showStylePicker = false
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Palette.goldPrimary.copy(alpha = 0.12f) else Palette.surfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(style.primaryTint)
                                                )
                                                Column {
                                                    Text(
                                                        text = style.title,
                                                        style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                                                        color = Palette.textPrimary
                                                    )
                                                    Text(
                                                        text = style.subtitle,
                                                        style = Typography.santoshi(12.sp),
                                                        color = Palette.textSecondary
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Palette.goldPrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Employee View Banner

@Composable
fun EmployeeViewBanner(
    onSwitchBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E242B),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Palette.goldPrimary)
                )
                Text(
                    text = "Staff Showroom View",
                    style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Surface(
                onClick = onSwitchBack,
                shape = RoundedCornerShape(12.dp),
                color = Palette.goldPrimary.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Palette.goldPrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Store Owner View ↗",
                    style = Typography.santoshi(11.sp, FontWeight.Bold),
                    color = Palette.goldPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// MARK: - Floating Pill Navigation

@Composable
fun EmployeePillNav(
    selectedTab: EmployeeTab,
    hasUnreadQueries: Boolean,
    hasNewOrders: Boolean,
    onSelectTab: (EmployeeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF14171C),
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeePillItem(
                tab = EmployeeTab.Home,
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                isSelected = selectedTab == EmployeeTab.Home,
                hasPing = false,
                onClick = { onSelectTab(EmployeeTab.Home) }
            )

            EmployeePillItem(
                tab = EmployeeTab.Catalogue,
                label = "Catalogue",
                selectedIcon = Icons.Filled.GridView,
                unselectedIcon = Icons.Outlined.GridView,
                isSelected = selectedTab == EmployeeTab.Catalogue,
                hasPing = false,
                onClick = { onSelectTab(EmployeeTab.Catalogue) }
            )

            EmployeePillItem(
                tab = EmployeeTab.Queries,
                label = "Queries",
                selectedIcon = Icons.AutoMirrored.Filled.Chat,
                unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
                isSelected = selectedTab == EmployeeTab.Queries,
                hasPing = hasUnreadQueries,
                onClick = { onSelectTab(EmployeeTab.Queries) }
            )

            EmployeePillItem(
                tab = EmployeeTab.Orders,
                label = "Orders",
                selectedIcon = Icons.Filled.ShoppingBag,
                unselectedIcon = Icons.Outlined.ShoppingBag,
                isSelected = selectedTab == EmployeeTab.Orders,
                hasPing = hasNewOrders,
                onClick = { onSelectTab(EmployeeTab.Orders) }
            )
        }
    }
}

@Composable
private fun EmployeePillItem(
    tab: EmployeeTab,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    hasPing: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Palette.goldPrimary else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )

                if (hasPing) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Palette.accentRed)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = label,
                    style = Typography.santoshi(12.sp, FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
