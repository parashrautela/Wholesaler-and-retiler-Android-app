// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TopUpSheet.swift
package com.jewelindia.app.feature.treasurechest

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCreditsSheet(
    wallet: CreditWallet?,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    DailyCreditsView(
        wallet = wallet,
        onDismiss = onDismiss
    )
}

@Composable
fun DailyCreditsView(
    wallet: CreditWallet?,
    onDismiss: () -> Unit
) {
    val dailyAllowance = wallet?.dailyAllowance ?: 2000
    val dailyLeft = wallet?.dailyAvailable ?: 700
    val bonusLeft = wallet?.bonusAvailable ?: 500

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar: Close pill on left, Centered "Daily credits"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .height(34.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Close",
                            style = Typography.santoshi(14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = Palette.dark
                        )
                    }
                }

                Text(
                    text = "Daily credits",
                    style = Typography.santoshi(16.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark,
                    textAlign = TextAlign.Center
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sun Icon + "Your daily allowance"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFBB8651),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Your daily allowance",
                        style = Typography.santoshi(13.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBB8651)
                    )
                }

                // Cirka allowance headline
                Text(
                    text = String.format("%,d credits every day", dailyAllowance),
                    style = Typography.cirka(29.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )

                Text(
                    text = "Your business is eligible for daily credits.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.dark
                )

                // Two columns: Daily credits left & Gifts and paid credits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Daily credits left",
                            style = Typography.santoshi(12.sp),
                            color = Color(0xFF78716C)
                        )
                        Text(
                            text = String.format("%,d", dailyLeft),
                            style = Typography.santoshi(23.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Gifts and paid credits",
                            style = Typography.santoshi(12.sp),
                            color = Color(0xFF78716C)
                        )
                        Text(
                            text = String.format("%,d", bonusLeft),
                            style = Typography.santoshi(23.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Next reset countdown
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        text = String.format("Next %,d credits in", dailyAllowance),
                        style = Typography.santoshi(14.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.dark
                    )
                    Text(
                        text = "06:18:41",
                        style = Typography.santoshi(36.sp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Palette.dark
                    )
                    Text(
                        text = "Sun, 18 Oct at 12:00 AM IST",
                        style = Typography.santoshi(14.sp),
                        color = Palette.dark
                    )
                }

                Text(
                    text = "Resets at midnight India time (IST). No claim button is needed.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.dark
                )

                Text(
                    text = "Unused daily credits expire at the reset. Gift and referral bonuses stay until spent.",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF8E8E93)
                )

                // Refresh credits button
                Row(
                    modifier = Modifier
                        .clickable { }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Palette.dark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Refresh credits",
                        style = Typography.santoshi(14.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.dark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Who receives daily credits?
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Who receives daily credits?",
                        style = Typography.santoshi(15.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78716C)
                    )
                    Text(
                        text = "A verified business is one whose business application has been approved by Jewel India. Phone or email verification alone does not approve a business.",
                        style = Typography.santoshi(14.sp),
                        color = Color(0xFF78716C),
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "Complete business onboarding and wait for admin approval. Active staff use their approved retailer's shared allowance.",
                        style = Typography.santoshi(14.sp),
                        color = Color(0xFF78716C),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Sticky Bottom "Done" Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(Palette.dark)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    style = Typography.santoshi(15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
