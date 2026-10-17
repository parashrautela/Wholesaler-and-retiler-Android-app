// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Report/WholesalerReport.swift (WholesalerReportView)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.data.api.WholesalerReport
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun WholesalerReportScreen(
    onBack: () -> Unit,
    peekReport: WholesalerReport? = null
) {
    var period by remember { mutableStateOf(WholesalerReport.Period.Week) }
    var report by remember { mutableStateOf<WholesalerReport?>(peekReport) }
    var loading by remember { mutableStateOf(peekReport == null) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }
            Text(
                text = "Performance Report",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { load() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Palette.muted
                )
            }
        }

        // Period tabs
        TabRow(
            selectedTabIndex = if (period == WholesalerReport.Period.Week) 0 else 1,
            containerColor = Palette.background,
            contentColor = Palette.goldPrimary,
            divider = {}
        ) {
            Tab(
                selected = period == WholesalerReport.Period.Week,
                onClick = { period = WholesalerReport.Period.Week },
                text = { Text("7 Days", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = period == WholesalerReport.Period.Month,
                onClick = { period = WholesalerReport.Period.Month },
                text = { Text("30 Days", fontWeight = FontWeight.Bold) }
            )
        }

        val rep = report
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (rep == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Report data unavailable", color = Palette.muted)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // KPI Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Views",
                        value = "${rep.views}",
                        prev = rep.viewsPrev,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Shortlists / Likes",
                        value = "${rep.likes}",
                        prev = rep.likesPrev,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "New Orders",
                        value = "${rep.ordersNew}",
                        prev = rep.ordersNewPrev,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Stores Reached",
                        value = "${rep.stores}",
                        prev = rep.storesPrev,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Top Liked Design
                if (rep.topLiked.isNotEmpty()) {
                    Text(
                        text = "Most Shortlisted",
                        style = Typography.cirka(17.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    rep.topLiked.take(3).forEach { design ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Palette.cream)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!design.imageURL.isNullOrBlank()) {
                                    CachedImage(
                                        url = design.imageURL ?: "",
                                        contentDescription = design.name,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = design.name,
                                        style = Typography.santoshi(14.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Palette.dark
                                    )
                                    Text(
                                        text = "${design.likes ?: 0} shortlists",
                                        style = Typography.santoshi(12.sp),
                                        color = Palette.goldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    prev: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                style = Typography.santoshi(12.sp),
                color = Palette.muted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = Typography.cirka(24.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "vs $prev prev",
                style = Typography.santoshi(11.sp),
                color = Palette.muted
            )
        }
    }
}
