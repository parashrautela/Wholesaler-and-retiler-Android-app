package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@Composable
internal fun OrdersCardFace(report: WholesalerReport) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFEEF2FF))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ORDERS",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4338CA),
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${report.ordersNew}", style = Typography.cirka(64.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                    Text(text = "new orders", style = Typography.santoshi(16.sp), fontWeight = FontWeight.SemiBold, color = Palette.dark, modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportMetricRow(label = "Accepted", value = report.ordersAccepted)
                ReportMetricRow(label = "Completed", value = report.ordersCompleted)
                ReportMetricRow(label = "Rejected", value = report.ordersRejected)
            }
        }
    }
}

@Composable
internal fun AttentionCardFace(report: WholesalerReport) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFECFDF5))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "VIEWS & QUESTIONS",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857),
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${report.views}", style = Typography.cirka(64.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                    Text(text = "design views", style = Typography.santoshi(16.sp), fontWeight = FontWeight.SemiBold, color = Palette.dark, modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportMetricRow(label = "Questions from stores", value = report.questions)
                ReportMetricRow(label = "Conversations", value = report.conversations)
            }
        }
    }
}

@Composable
internal fun ReachCardFace(report: WholesalerReport) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFFDF2F8))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "STORE REACH",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBE185D),
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "${report.stores}", style = Typography.cirka(64.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                    Text(text = "stores", style = Typography.santoshi(16.sp), fontWeight = FontWeight.SemiBold, color = Palette.dark, modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            Text(
                text = "Different stores that liked, opened, asked about or ordered your designs.",
                style = Typography.santoshi(13.sp),
                color = Palette.muted
            )
        }
    }
}

@Composable
internal fun EndCardFace(period: WholesalerReport.Period) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1F1B16), Color(0xFF4A3B28))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "THAT'S YOUR ${period.rawValue.uppercase()}",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBB284),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Upload new designs to give stores more to like.",
                    style = Typography.cirka(28.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = "Swipe to start again",
                style = Typography.santoshi(12.sp),
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
internal fun ReportMetricRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = Typography.santoshi(14.sp), color = Palette.dark)
        Text(text = "$value", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
    }
}
