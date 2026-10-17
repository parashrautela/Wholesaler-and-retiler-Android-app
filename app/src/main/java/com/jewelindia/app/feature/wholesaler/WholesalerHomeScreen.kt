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
import kotlinx.coroutines.launch

@Composable
fun WholesalerHomeScreen(
    wholesaler: Wholesaler?,
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
    onSelectCategory: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var productsCount by remember { mutableIntStateOf(0) }
    var pendingOrdersCount by remember { mutableIntStateOf(0) }
    var unreadChatsCount by remember { mutableIntStateOf(0) }
    var uploadUsageText by remember { mutableStateOf("0") }
    var activeOffersCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    fun refreshData() {
        val wid = wholesaler?.id ?: return
        scope.launch {
            loading = true
            val pDef = async { try { WholesalerApi.countProducts(wid) } catch (_: Exception) { 0 } }
            val oDef = async { try { WholesalerApi.countPendingOrders(wid) } catch (_: Exception) { 0 } }
            val cDef = async { try { WholesalerApi.countUnreadConversations(wid) } catch (_: Exception) { 0 } }
            val uDef = async { try { WholesalerApi.fetchUploadUsage(wid).display } catch (_: Exception) { "0" } }
            val mDef = async { try { ManufacturingApi.fetchWholesalerOffers("active").size } catch (_: Exception) { 0 } }

            productsCount = pDef.await()
            pendingOrdersCount = oDef.await()
            unreadChatsCount = cDef.await()
            uploadUsageText = uDef.await()
            activeOffersCount = mDef.await()
            loading = false
        }
    }

    LaunchedEffect(wholesaler?.id) {
        refreshData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                style = Typography.cirka(26.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreditBalancePill(
                    creditStore = creditStore,
                    onClick = onTreasureClick
                )

                // Menu Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Palette.goldPrimary)
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (wholesaler?.businessName?.firstOrNull() ?: 'J').uppercase(),
                            style = Typography.cirka(15.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.light
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile & Settings") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Palette.dark) },
                            onClick = {
                                showMenu = false
                                onProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Invite Retailer") },
                            leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Palette.dark) },
                            onClick = {
                                showMenu = false
                                onInviteClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Manufacturing Offers") },
                            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Palette.dark) },
                            onClick = {
                                showMenu = false
                                onOffersClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Log Out", color = Palette.accentRed) },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Palette.accentRed) },
                            onClick = {
                                showMenu = false
                                showSignOutDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
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

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Log Out", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of Jewel India?", style = Typography.santoshi(14.sp)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch { sessionStore.signOut() }
                    }
                ) {
                    Text("Log Out", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
