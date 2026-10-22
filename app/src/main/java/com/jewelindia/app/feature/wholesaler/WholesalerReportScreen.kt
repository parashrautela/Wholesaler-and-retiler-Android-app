// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Report/WholesalerReport.swift (WholesalerReportView)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class ReportCardKind {
    Summary,
    Liked1,
    Liked2,
    Orders,
    Attention,
    Reach,
    End
}

@Composable
fun WholesalerReportScreen(
    onBack: () -> Unit,
    peekReport: WholesalerReport? = null
) {
    var period by remember { mutableStateOf(WholesalerReport.Period.Week) }
    var report by remember { mutableStateOf<WholesalerReport?>(peekReport) }
    var loading by remember { mutableStateOf(peekReport == null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    fun load() {
        if (peekReport != null) return
        scope.launch {
            loading = true
            report = try {
                WholesalerReport.fetch(period)
            } catch (_: Exception) {
                null
            }
            loading = false
        }
    }

    LaunchedEffect(period) {
        if (peekReport == null) {
            load()
        }
    }

    val cards = remember(report) {
        val list = mutableListOf(ReportCardKind.Summary)
        val rep = report
        if (rep != null) {
            if (rep.topLiked.isNotEmpty()) list.add(ReportCardKind.Liked1)
            if (rep.topLiked.size > 1) list.add(ReportCardKind.Liked2)
            list.add(ReportCardKind.Orders)
            list.add(ReportCardKind.Attention)
            list.add(ReportCardKind.Reach)
            list.add(ReportCardKind.End)
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: Centered "Your Report" with "Done" pill on right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Your Report",
                style = Typography.santoshi(17.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                textAlign = TextAlign.Center
            )

            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(34.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Done",
                        style = Typography.santoshi(14.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.dark
                    )
                }
            }
        }

        // Segmented Control ("This Week" / "This Month")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFE5E5EA))
                .padding(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                val weekSelected = period == WholesalerReport.Period.Week
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (weekSelected) Color.White else Color.Transparent)
                        .clickable {
                            period = WholesalerReport.Period.Week
                            currentIndex = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This Week",
                        style = Typography.santoshi(13.sp),
                        fontWeight = if (weekSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Palette.dark
                    )
                }

                val monthSelected = period == WholesalerReport.Period.Month
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (monthSelected) Color.White else Color.Transparent)
                        .clickable {
                            period = WholesalerReport.Period.Month
                            currentIndex = 0
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "This Month",
                        style = Typography.santoshi(13.sp),
                        fontWeight = if (monthSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Palette.dark
                    )
                }
            }
        }

        // Card Stack Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loading || report == null) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Palette.dark
                )
            } else {
                val rep = report!!
                val activeKind = cards.getOrElse(currentIndex) { ReportCardKind.Summary }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(dragOffset.roundToInt(), 0) }
                        .pointerInput(cards.size) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragOffset < -100f && currentIndex < cards.size - 1) {
                                        currentIndex++
                                    } else if (dragOffset > 100f && currentIndex > 0) {
                                        currentIndex--
                                    }
                                    dragOffset = 0f
                                },
                                onDragCancel = { dragOffset = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragOffset += dragAmount
                                }
                            )
                        }
                        .clickable {
                            currentIndex = (currentIndex + 1) % cards.size
                        }
                ) {
                    when (activeKind) {
                        ReportCardKind.Summary -> SummaryCardFace(rep, period)
                        ReportCardKind.Liked1 -> LikedCardFace(rep.topLiked.firstOrNull(), 1, period)
                        ReportCardKind.Liked2 -> LikedCardFace(rep.topLiked.getOrNull(1), 2, period)
                        ReportCardKind.Orders -> OrdersCardFace(rep)
                        ReportCardKind.Attention -> AttentionCardFace(rep)
                        ReportCardKind.Reach -> ReachCardFace(rep)
                        ReportCardKind.End -> EndCardFace(period)
                    }
                }
            }
        }

        // Progress Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            cards.indices.forEach { i ->
                val active = i == currentIndex
                Box(
                    modifier = Modifier
                        .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                        .clip(CircleShape)
                        .background(if (active) Palette.dark else Color(0xFFD1D5DB))
                )
            }
        }
    }
}

@Composable
private fun SummaryCardFace(
    report: WholesalerReport,
    period: WholesalerReport.Period
) {
    val top = report.topLiked.firstOrNull()
    val headline = if (top != null) {
        "${top.name} was your star, with ${top.likes ?: 0} likes."
    } else {
        "Your collection was viewed by stores."
    }

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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "YOUR ${period.rawValue.uppercase()} ON JEWEL INDIA",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBB284),
                    letterSpacing = 1.sp
                )

                Text(
                    text = headline,
                    style = Typography.cirka(30.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 36.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    StatNumber(value = report.likes, label = "likes")
                    StatNumber(value = report.ordersNew, label = "new orders")
                    StatNumber(value = report.stores, label = "stores")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Swipe for more",
                        style = Typography.santoshi(12.sp),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatNumber(value: Int, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$value",
            style = Typography.cirka(34.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = Typography.santoshi(13.sp),
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LikedCardFace(
    design: WholesalerReport.Design?,
    rank: Int,
    period: WholesalerReport.Period
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF7ED), Color(0xFFFDE7C7))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (rank == 1) "TOP DESIGN THIS ${period.rawValue.uppercase()}" else "#$rank MOST LIKED",
                style = Typography.santoshi(11.sp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9A3412),
                letterSpacing = 1.sp
            )

            val url = design?.imageURL
            if (!url.isNullOrBlank()) {
                CachedImage(
                    url = url,
                    contentDescription = design.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = design?.name ?: "Jewellery", style = Typography.cirka(18.sp), color = Palette.dark)
                }
            }

            Text(
                text = "${design?.name ?: "Your design"} got ${design?.likes ?: 0} likes",
                style = Typography.cirka(26.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Text(
                text = if (rank == 1) "It did really well this week. Stores are noticing it." else "Liked by stores and their staff.",
                style = Typography.santoshi(13.sp),
                color = Palette.muted
            )
        }
    }
}

