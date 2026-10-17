// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/InsufficientCreditsSheet.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsufficientCreditsSheet(
    sheetState: SheetState,
    shortBy: Int? = null,
    onDismiss: () -> Unit,
    onOpenDailyCredits: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.light
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Palette.goldPrimary,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Insufficient Credits",
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Text(
                text = if (shortBy != null && shortBy > 0) {
                    "You need $shortBy more credits to complete this action. Your daily allowance refreshes every morning."
                } else {
                    "You don't have enough credits to complete this action. Your daily allowance refreshes every morning."
                },
                style = Typography.santoshi(14.sp),
                color = Palette.muted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onDismiss()
                    onOpenDailyCredits()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
            ) {
                Text(
                    text = "View Daily Allowance",
                    style = Typography.santoshi(15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.light
                )
            }

            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    style = Typography.santoshi(14.sp),
                    color = Palette.muted
                )
            }
        }
    }
}
