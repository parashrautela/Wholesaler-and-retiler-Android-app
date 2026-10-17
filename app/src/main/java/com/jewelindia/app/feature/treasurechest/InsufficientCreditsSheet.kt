// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/InsufficientCreditsSheet.swift
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsufficientCreditsSheet(
    sheetState: SheetState,
    shortBy: Int? = 120,
    required: Int = 200,
    balance: Int = 80,
    onDismiss: () -> Unit,
    onOpenDailyCredits: () -> Unit = {}
) {
    InsufficientCreditsView(
        shortBy = shortBy ?: 120,
        required = required,
        balance = balance,
        onDismiss = onDismiss
    )
}

@Composable
fun InsufficientCreditsView(
    shortBy: Int = 120,
    required: Int = 200,
    balance: Int = 80,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Bar: Close pill on left
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
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
        }

        // Header with gold icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "More Credits Needed",
                    style = Typography.cirka(20.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )
                Text(
                    text = "You need $shortBy more credits to combine these designs.",
                    style = Typography.santoshi(13.sp),
                    color = Palette.muted,
                    lineHeight = 18.sp
                )
            }
        }

        // Balance Summary Card (3 columns)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Current Balance",
                        style = Typography.santoshi(11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.muted
                    )
                    Text(
                        text = "$balance",
                        style = Typography.cirka(22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFFE5E7EB))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Required",
                        style = Typography.santoshi(11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.muted
                    )
                    Text(
                        text = "$required",
                        style = Typography.cirka(22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFFE5E7EB))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Short By",
                        style = Typography.santoshi(11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD97706)
                    )
                    Text(
                        text = "$shortBy",
                        style = Typography.cirka(22.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }
        }

        // Note Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.cream)
                .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFFBB8651),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Your sliders and artisan notes are saved. Check Credits for your next allowance and refill time. Unused recurring credits expire at that deadline.",
                    style = Typography.santoshi(12.sp),
                    color = Palette.dark.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom "Back to Editor" Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Palette.border, RoundedCornerShape(10.dp))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Back to Editor",
                style = Typography.santoshi(14.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }
    }
}
