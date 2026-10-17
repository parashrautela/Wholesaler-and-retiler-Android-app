// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TreasureChestCard.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.cirka
import com.jewelindia.app.design.gilroy
import com.jewelindia.app.design.manrope

@Composable
fun TreasureChestCard(
    wallet: CreditWallet? = null,
    creditStore: CreditStore? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storeState = creditStore?.walletState?.collectAsState()
    val resolvedWallet = wallet ?: (storeState?.value as? WalletState.Available)?.wallet
    val isLoading = creditStore != null && storeState?.value is WalletState.Loading && resolvedWallet == null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Palette.cream,
                        Color(0xFFF7F3EA),
                        Color(0xFFFDFBF7)
                    )
                )
            )
            .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header: sparkle + DAILY CREDITS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFBB8651),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "DAILY CREDITS",
                        style = manrope(12f, JewelWeight.Bold),
                        color = Color(0xFFBB8651),
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Available credits number or placeholder
                if (isLoading || resolvedWallet == null) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .size(width = 60.dp, height = 34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Palette.muted.copy(alpha = 0.2f))
                    )
                } else {
                    Text(
                        text = "${resolvedWallet.available}",
                        style = cirka(34f, JewelWeight.Bold),
                        color = Palette.dark
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Credits available",
                    style = gilroy(14f, JewelWeight.Medium),
                    color = Palette.muted
                )
            }

            // Top-right: Details button
            Row(
                modifier = Modifier
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(8.dp), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Palette.border, RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Palette.dark,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Details",
                    style = manrope(13f, JewelWeight.Bold),
                    color = Palette.dark
                )
            }
        }
    }
}
