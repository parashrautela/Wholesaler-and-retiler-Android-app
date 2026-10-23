// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TreasureChestView.swift
package com.jewelindia.app.feature.treasurechest

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.CreditPrice
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.design.readableContentWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasureChestScreen(
    wholesaler: Wholesaler? = null,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val creditStore = remember { CreditStore(scope) }
    val walletState by creditStore.walletState.collectAsState()
    val rateCard by creditStore.rateCard.collectAsState()

    var showHistory by remember { mutableStateOf(false) }
    var showDailySheet by remember { mutableStateOf(false) }
    val dailySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        creditStore.refresh()
    }

    if (showHistory) {
        TransactionHistoryScreen(onBack = { showHistory = false })
        return
    }

    val wallet = (walletState as? WalletState.Available)?.wallet

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .readableContentWidth()
            .statusBarsPadding()
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Palette.dark
                    )
                }
            }
            Text(
                text = "Treasure Chest",
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { creditStore.refresh() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Palette.muted
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Balance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Palette.dark)
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE BALANCE",
                            style = Typography.santoshi(12.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.goldPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Palette.goldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (walletState is WalletState.Loading) {
                        CircularProgressIndicator(
                            color = Palette.goldPrimary,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "${wallet?.available ?: 0}",
                            style = Typography.cirka(44.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.light
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Credits",
                        style = Typography.santoshi(14.sp),
                        color = Palette.muted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDailySheet = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
                        ) {
                            Text(
                                text = "Daily Allowance",
                                style = Typography.santoshi(13.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.light
                            )
                        }

                        Button(
                            onClick = { showHistory = true },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.surfaceElevated)
                        ) {
                            Text(
                                text = "History",
                                style = Typography.santoshi(13.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.light
                            )
                        }
                    }
                }
            }

            // Wallet Stats
            if (wallet != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        label = "Lifetime Granted",
                        value = "${wallet.lifetimeGranted}",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Lifetime Spent",
                        value = "${wallet.lifetimeSpent}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Rate Card Section
            if (rateCard.isNotEmpty()) {
                Text(
                    text = "Rate Card",
                    style = Typography.cirka(18.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.cream)
                        .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rateCard.forEach { price ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = price.label,
                                    style = Typography.santoshi(14.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Palette.dark
                                )
                                if (!price.description.isNullOrBlank()) {
                                    Text(
                                        text = price.description,
                                        style = Typography.santoshi(12.sp),
                                        color = Palette.muted
                                    )
                                }
                            }
                            Text(
                                text = "${price.credits} Credits",
                                style = Typography.santoshi(14.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.goldPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDailySheet) {
        DailyCreditsSheet(
            wallet = wallet,
            sheetState = dailySheetState,
            onDismiss = { showDailySheet = false }
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = label,
                style = Typography.santoshi(12.sp),
                color = Palette.muted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = Typography.santoshi(18.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }
    }
}
