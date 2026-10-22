// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerOrdersView.swift (WholesalerOrderDetail)
package com.jewelindia.app.feature.wholesaler

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.OrderStatus
import com.jewelindia.app.data.api.WholesalerOrder
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerOrderDetailSheet(
    order: WholesalerOrder,
    onStatusUpdate: (OrderStatus) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit,
    isUpdating: Boolean = false,
    actionError: String? = null
) {
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // iOS Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onDismiss() }
            ) {
                Text(
                    text = "Close",
                    style = Typography.santoshi(15.sp, FontWeight.Medium),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "Order #${order.shortId}",
                style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                color = Palette.foreground
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                OrderDesignThumb(
                    imageUrl = order.imageURL,
                    size = 96.dp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = order.designTitle,
                        style = Typography.cirka(24.sp),
                        color = Palette.foreground
                    )

                    if (!order.productType.isNullOrBlank()) {
                        Text(
                            text = order.productType.trim().replaceFirstChar { it.uppercase() },
                            style = Typography.santoshi(12.sp),
                            color = Palette.muted
                        )
                    }

                    if (order.status != null) {
                        OrderStatusBadge(status = order.status)
                    }
                }
            }

            // Store section card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "STORE",
                    style = Typography.santoshi(11.sp, FontWeight.Bold),
                    letterSpacing = 0.8.sp,
                    color = Palette.muted
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = order.storeName?.ifBlank { "Retail store" } ?: "Retail store",
                        style = Typography.santoshi(15.sp, FontWeight.Bold),
                        color = Palette.foreground
                    )
                    if (!order.storeCity.isNullOrBlank()) {
                        Text(
                            text = order.storeCity,
                            style = Typography.santoshi(13.sp),
                            color = Palette.muted
                        )
                    }
                    if (!order.placedBy.isNullOrBlank()) {
                        val line = if (order.placedByStaff) "Placed by ${order.placedBy}, store staff"
                        else "Placed by ${order.placedBy}, owner"
                        Text(
                            text = line,
                            style = Typography.santoshi(13.sp),
                            color = Palette.muted
                        )
                    }
                }
            }

            // Note from the store section
            if (!order.note.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NOTE FROM THE STORE",
                        style = Typography.santoshi(11.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Palette.muted
                    )
                    Text(
                        text = order.note,
                        style = Typography.santoshi(14.sp),
                        color = Palette.foreground,
                        lineHeight = 20.sp
                    )
                }
            }

            // Why it was rejected section
            if (order.status == OrderStatus.rejected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "WHY IT WAS REJECTED",
                        style = Typography.santoshi(11.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Palette.muted
                    )
                    Text(
                        text = order.rejectionReason ?: "No reason was recorded.",
                        style = Typography.santoshi(14.sp),
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Progress timeline section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "PROGRESS",
                    style = Typography.santoshi(11.sp, FontWeight.Bold),
                    letterSpacing = 0.8.sp,
                    color = Palette.muted
                )
                OrderTimeline(order = order)
            }
        }

        // Pinned Bottom Action Bar
        val nextStatus = when (order.status) {
            OrderStatus.accepted, OrderStatus.inProduction -> OrderStatus.packed
            OrderStatus.packed -> OrderStatus.dispatched
            else -> null
        }

        if (order.status == OrderStatus.pending || nextStatus != null || order.status == OrderStatus.dispatched) {
            val updating = isUpdating || working
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!actionError.isNullOrBlank()) {
                    Text(
                        text = actionError,
                        style = Typography.santoshi(13.sp, FontWeight.Medium),
                        color = Color(0xFFEF4444),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (order.status == OrderStatus.pending) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Reject button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Red.copy(alpha = if (updating) 0.05f else 0.1f))
                                .clickable(enabled = !updating) { showRejectSheet = true }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Reject",
                                style = Typography.santoshi(15.sp, FontWeight.Bold),
                                color = if (updating) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFFEF4444)
                            )
                        }

                        // Accept Order button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (updating) Palette.dark.copy(alpha = 0.6f) else Palette.dark)
                                .clickable(enabled = !updating) {
                                    working = true
                                    onStatusUpdate(OrderStatus.accepted)
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (updating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "Accept Order",
                                    style = Typography.santoshi(15.sp, FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else if (nextStatus != null) {
                    val label = when (nextStatus) {
                        OrderStatus.packed -> "Mark as Packed"
                        OrderStatus.dispatched -> "Mark as Dispatched"
                        else -> "Update"
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (updating) Palette.dark.copy(alpha = 0.6f) else Palette.dark)
                            .clickable(enabled = !updating) {
                                working = true
                                onStatusUpdate(nextStatus)
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (updating) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = label,
                                style = Typography.santoshi(15.sp, FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else if (order.status == OrderStatus.dispatched) {
                    Text(
                        text = "Dispatched — waiting for the store to confirm they received it.",
                        style = Typography.santoshi(13.sp),
                        color = Palette.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Palette.background, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    if (showRejectSheet) {
        RejectOrderSheet(
            onDismiss = { showRejectSheet = false },
            onConfirm = { reason ->
                showRejectSheet = false
                onReject(reason)
            },
            isUpdating = isUpdating,
            actionError = actionError
        )
    }
}
