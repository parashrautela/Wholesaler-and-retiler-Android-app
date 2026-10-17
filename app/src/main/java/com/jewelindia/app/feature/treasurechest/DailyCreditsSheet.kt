// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TopUpSheet.swift (lines 1-190)
package com.jewelindia.app.feature.treasurechest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                modifier = Modifier.size(44.dp)
            )

            Text(
                text = "Daily Credit Allowance",
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Text(
                text = "Jewel India provides an automatic daily credit grant so you can browse, upload, and design without interruption.",
                style = Typography.santoshi(14.sp),
                color = Palette.muted,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Allowance",
                            style = Typography.santoshi(14.sp),
                            color = Palette.muted
                        )
                        Text(
                            text = "${wallet?.dailyAllowance ?: 200} Credits / day",
                            style = Typography.santoshi(15.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Balance",
                            style = Typography.santoshi(14.sp),
                            color = Palette.muted
                        )
                        Text(
                            text = "${wallet?.available ?: 0} Credits",
                            style = Typography.santoshi(15.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.goldPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Next Reset",
                            style = Typography.santoshi(14.sp),
                            color = Palette.muted
                        )
                        Text(
                            text = "Tomorrow 6:00 AM IST",
                            style = Typography.santoshi(14.sp),
                            color = Palette.dark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.dark)
            ) {
                Text(
                    text = "Done",
                    style = Typography.santoshi(15.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.light
                )
            }
        }
    }
}
