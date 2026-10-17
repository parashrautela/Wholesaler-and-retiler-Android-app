// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/CreditBalancePill.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@Composable
fun CreditBalancePill(
    creditStore: CreditStore,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by creditStore.walletState.collectAsState()

    val text = when (val state = walletState) {
        is WalletState.Available -> "${state.wallet.available}"
        WalletState.Loading -> "..."
        WalletState.Unavailable -> "—"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Palette.goldPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = Typography.santoshi(13.sp),
            fontWeight = FontWeight.Bold,
            color = Palette.dark
        )
    }
}
