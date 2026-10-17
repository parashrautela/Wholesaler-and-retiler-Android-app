// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TransactionHistoryView.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.CreditLedgerEntry
import com.jewelindia.app.data.api.CreditsApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@Composable
fun TransactionHistoryScreen(onBack: () -> Unit) {
    var entries by remember { mutableStateOf<List<CreditLedgerEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        entries = try {
            CreditsApi.fetchLedger(limit = 100)
        } catch (_: Exception) {
            emptyList()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }
            Text(
                text = "Transaction History",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No transactions yet",
                    style = Typography.santoshi(15.sp),
                    color = Palette.muted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    TransactionRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(entry: CreditLedgerEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.cream, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayTitle,
                style = Typography.santoshi(14.sp),
                fontWeight = FontWeight.SemiBold,
                color = Palette.dark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Balance: ${entry.balanceAfter} credits",
                style = Typography.santoshi(12.sp),
                color = Palette.muted
            )
        }

        val isPositive = entry.delta > 0
        Text(
            text = "${if (isPositive) "+" else ""}${entry.delta}",
            style = Typography.santoshi(15.sp),
            fontWeight = FontWeight.Bold,
            color = if (isPositive) Palette.statusVerified else Palette.dark
        )
    }
}
