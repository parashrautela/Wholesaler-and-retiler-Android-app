// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Report/WholesalerReport.swift (WholesalerReportTeaser)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.manrope

@Composable
fun WholesalerReportTeaser(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    peekReport: WholesalerReport? = null
) {
    var report by remember { mutableStateOf<WholesalerReport?>(peekReport) }

    LaunchedEffect(peekReport) {
        if (peekReport != null) {
            report = peekReport
            return@LaunchedEffect
        }
        try {
            report = WholesalerReport.fetch(WholesalerReport.Period.Week)
        } catch (_: Exception) {
            // fallback handled in line text
        }
    }

    val line = remember(report) {
        val r = report
        if (r == null) {
            "Likes, orders and store reach — swipe through your week."
        } else {
            val top = r.topLiked.firstOrNull()
            if (top != null && (top.likes ?: 0) > 0) {
                "${top.name} got ${top.likes} likes · ${r.ordersNew} new orders"
            } else {
                "${r.likes} likes · ${r.ordersNew} new orders · ${r.stores} stores"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 12.dp)
    ) {
        // Bottom card peeking out (offset 12dp, padding 20dp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 20.dp)
                .offset(y = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE9DFCF))
        )

        // Middle card peeking out (offset 6dp, padding 10dp)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 10.dp)
                .offset(y = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD9C9AE))
        )

        // Top main card with dark gradient
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1F1B16), Color(0xFF4A3B28))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "YOUR WEEKLY REPORT",
                    style = manrope(10f, JewelWeight.Bold),
                    letterSpacing = 1.sp,
                    color = Color(0xFFE4CC8F)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = line,
                    style = manrope(14f, JewelWeight.Semibold),
                    color = Color.White,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
