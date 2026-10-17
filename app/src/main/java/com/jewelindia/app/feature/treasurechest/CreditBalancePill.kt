// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/CreditBalancePill.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope

@Composable
fun CreditBalancePill(
    creditStore: CreditStore,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by creditStore.walletState.collectAsState()

    val wallet = (walletState as? WalletState.Available)?.wallet
    val isLowBalance = wallet?.lowBalance == true
    val isLoading = walletState is WalletState.Loading

    val iconColor = if (isLowBalance) Palette.statusPending else Color(0xFFBB8651)
    val textColor = if (isLowBalance) Palette.statusPending else Palette.dark
    val borderColor = if (isLowBalance) Palette.statusPending.copy(alpha = 0.8f) else Palette.border

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Palette.cream)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        if (isLoading || wallet == null) {
            Box(
                modifier = Modifier
                    .size(width = 22.dp, height = 12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Palette.muted.copy(alpha = 0.3f))
            )
        } else {
            Text(
                text = "${wallet.available}",
                style = manrope(13f, JewelWeight.Semibold),
                color = textColor
            )
        }
    }
}

