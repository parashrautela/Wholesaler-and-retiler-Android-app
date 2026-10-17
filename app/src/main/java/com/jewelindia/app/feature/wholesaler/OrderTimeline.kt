// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerOrdersView.swift (OrderTimeline, OrderStatusBadge, OrderDesignThumb)
package com.jewelindia.app.feature.wholesaler

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object OrderTime {
    fun full(dateString: String?): String? {
        if (dateString.isNullOrBlank()) return null
        return runCatching {
            val instant = Instant.parse(dateString)
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrNull()
    }
}

private data class TimelineStep(
    val id: String,
    val title: String,
    val at: String?,
    val failed: Boolean = false
)

@Composable
fun OrderTimeline(
    order: WholesalerOrder,
    modifier: Modifier = Modifier
) {
    val steps = remember(order) {
        val all = mutableListOf<TimelineStep>()
        all.add(TimelineStep(id = "submitted", title = "Order submitted", at = order.createdAt))
        if (order.status == OrderStatus.rejected) {
            all.add(TimelineStep(id = "rejected", title = "Rejected", at = order.rejectedAt, failed = true))
            return@remember all
        }
        all.add(TimelineStep(id = "accepted", title = "Accepted", at = order.acceptedAt))
        if (order.productionAt != null) {
            all.add(TimelineStep(id = "production", title = "In production", at = order.productionAt))
        }
        all.addAll(
            listOf(
                TimelineStep(id = "packed", title = "Packed", at = order.packedAt),
                TimelineStep(id = "dispatched", title = "Dispatched", at = order.dispatchedAt),
                TimelineStep(id = "received", title = "Received by store", at = order.receivedAt),
                TimelineStep(id = "completed", title = "Completed", at = order.completedAt)
            )
        )
        all
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        steps.forEachIndexed { index, step ->
            val isDone = step.at != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    step.failed -> Palette.accentRed
                                    isDone -> Palette.dark
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                1.5.dp,
                                if (isDone || step.failed) Color.Transparent else Palette.surfaceBorder,
                                CircleShape
                            )
                    )
                    if (index < steps.size - 1) {
                        val nextDone = steps[index + 1].at != null
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(32.dp)
                                .background(if (nextDone) Palette.dark else Palette.surfaceBorder)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index == steps.size - 1) 0.dp else 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = step.title,
                        style = Typography.santoshi(14.sp, if (isDone) FontWeight.SemiBold else FontWeight.Normal),
                        color = when {
                            step.failed -> Palette.accentRed
                            isDone -> Palette.textPrimary
                            else -> Palette.textSecondary
                        }
                    )
                    Text(
                        text = OrderTime.full(step.at) ?: "Not yet",
                        style = Typography.santoshi(12.sp),
                        color = Palette.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(
    status: OrderStatus?,
    modifier: Modifier = Modifier
) {
    val currentStatus = status ?: OrderStatus.pending
    val (bg, fg, label) = when (currentStatus) {
        OrderStatus.pending -> Triple(Palette.accentOrange.copy(alpha = 0.15f), Palette.accentOrange, "Pending Approval")
        OrderStatus.accepted -> Triple(Palette.goldPrimary.copy(alpha = 0.15f), Palette.goldPrimary, "Accepted")
        OrderStatus.inProduction -> Triple(Color(0xFF3B82F6).copy(alpha = 0.15f), Color(0xFF3B82F6), "In Production")
        OrderStatus.packed -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.15f), Color(0xFF8B5CF6), "Packed")
        OrderStatus.dispatched -> Triple(Color(0xFF06B6D4).copy(alpha = 0.15f), Color(0xFF06B6D4), "Dispatched")
        OrderStatus.received -> Triple(Palette.accentGreen.copy(alpha = 0.15f), Palette.accentGreen, "Received")
        OrderStatus.completed -> Triple(Palette.accentGreen.copy(alpha = 0.15f), Palette.accentGreen, "Completed")
        OrderStatus.rejected -> Triple(Palette.accentRed.copy(alpha = 0.15f), Palette.accentRed, "Rejected")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = Typography.santoshi(11.sp, FontWeight.SemiBold),
            color = fg
        )
    }
}

@Composable
fun OrderDesignThumb(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!imageUrl.isNullOrBlank()) {
        ProtectedImage(
            url = imageUrl,
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Palette.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = Palette.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
