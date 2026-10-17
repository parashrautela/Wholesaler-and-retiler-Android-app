// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeInfiniteCanvas.swift
package com.jewelindia.app.feature.employee

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmployeeInfiniteCanvas(
    session: EmployeeSession,
    store: EmployeeStore,
    onClose: () -> Unit,
    onOpenDetail: (Product) -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val state by store.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val offsetAnim = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(session.retailerId) {
        scope.launch {
            try {
                products = EmployeeApi.fetchSelectedProducts(session.retailerId)
            } catch (_: Exception) {
            } finally {
                loading = false
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1114))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        offsetAnim.snapTo(
                            Offset(
                                x = offsetAnim.value.x + dragAmount.x,
                                y = offsetAnim.value.y + dragAmount.y
                            )
                        )
                    }
                }
            }
    ) {
        val density = LocalDensity.current
        val cardWidthDp = 180.dp
        val cardHeightDp = 240.dp
        val gapDp = 20.dp

        val cardWidthPx = with(density) { cardWidthDp.toPx() }
        val cardHeightPx = with(density) { cardHeightDp.toPx() }
        val gapPx = with(density) { gapDp.toPx() }

        val stepX = cardWidthPx + gapPx
        val stepY = cardHeightPx + gapPx

        val cols = 6
        val rows = 6

        // Subtle decorative jali background
        EmployeeJaliPattern(
            modifier = Modifier.fillMaxSize(),
            color = Palette.goldPrimary.copy(alpha = 0.04f),
            cellSize = 50f
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No designs in catalogue to display on canvas.",
                    style = Typography.santoshi(15.sp),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        } else {
            // Render grid tiles relative to offsetAnim
            val currentOffset = offsetAnim.value
            val n = products.size

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val index = (r * cols + c) % n
                    val product = products[index]

                    val baseX = c * stepX
                    val baseY = r * stepY

                    // Wrap-around virtual coordinates
                    val totalW = cols * stepX
                    val totalH = rows * stepY

                    var posX = (baseX + currentOffset.x) % totalW
                    if (posX < -cardWidthPx) posX += totalW

                    var posY = (baseY + currentOffset.y) % totalH
                    if (posY < -cardHeightPx) posY += totalH

                    val isSelected = state.selectedProductIds.contains(product.id)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                            .size(cardWidthDp, cardHeightDp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    onClick = { onOpenDetail(product) },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        store.toggleSelection(product.id)
                                    },
                                    onLongClickLabel = "Select design"
                                )
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                ) {
                                    CachedImage(
                                        url = product.primaryImage,
                                        contentDescription = product.displayTitle,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.4f))
                                        )
                                        Surface(
                                            color = Palette.goldPrimary,
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = product.displayTitle,
                                        style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                                        color = Palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = product.purity ?: "22KT",
                                        style = Typography.santoshi(11.sp),
                                        color = Palette.goldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Bar Controls (Close button & instruction pill)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onClose,
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Palette.goldPrimary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "DRAG TO EXPLORE · TAP TO VIEW · HOLD TO SELECT",
                    style = Typography.santoshi(10.sp, FontWeight.Bold),
                    color = Palette.goldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // Bottom Tray with Selection & Review action
        if (state.selectedProductIds.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Palette.goldPrimary,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${state.selectedProductIds.size}",
                                    style = Typography.santoshi(13.sp, FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "pieces selected",
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onOpenReview,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Palette.goldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Review & Request",
                                style = Typography.santoshi(13.sp, FontWeight.Bold)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
