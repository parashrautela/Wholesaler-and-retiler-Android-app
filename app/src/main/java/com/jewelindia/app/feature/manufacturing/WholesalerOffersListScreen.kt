// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/WholesalerOffersListView.swift
package com.jewelindia.app.feature.manufacturing

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun WholesalerOffersListScreen(
    onBack: () -> Unit,
    onSelectOffer: (ManufacturingOffer) -> Unit
) {
    var offers by remember { mutableStateOf<List<ManufacturingOffer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTabIdx by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Quoted", "Accepted", "Declined")
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            val status = when (selectedTabIdx) {
                1 -> "active"
                2 -> "quoted"
                3 -> "accepted"
                4 -> "declined"
                else -> null
            }
            offers = try {
                ManufacturingApi.fetchWholesalerOffers(status)
            } catch (_: Exception) {
                emptyList()
            }
            loading = false
        }
    }

    LaunchedEffect(selectedTabIdx) {
        load()
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
                text = "Manufacturing Offers",
                style = Typography.cirka(22.sp),
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

        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIdx,
            containerColor = Palette.background,
            contentColor = Palette.goldPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTabIdx == idx,
                    onClick = { selectedTabIdx = idx },
                    text = {
                        Text(
                            text = label,
                            style = Typography.santoshi(14.sp),
                            fontWeight = if (selectedTabIdx == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIdx == idx) Palette.dark else Palette.muted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (offers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No manufacturing offers found",
                    style = Typography.santoshi(15.sp),
                    color = Palette.muted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(offers, key = { it.id }) { offer ->
                    WholesalerOfferCard(
                        offer = offer,
                        onClick = { onSelectOffer(offer) }
                    )
                }
            }
        }
    }
}

@Composable
fun WholesalerOfferCard(
    offer: ManufacturingOffer,
    onClick: () -> Unit
) {
    val req = offer.request
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = req?.let { "${it.material} ${it.category}" } ?: "Manufacturing Request",
                    style = Typography.cirka(16.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )
                Text(
                    text = offer.status.replaceFirstChar { it.uppercase() },
                    style = Typography.santoshi(12.sp),
                    fontWeight = FontWeight.Bold,
                    color = when (offer.status) {
                        "accepted" -> Palette.statusVerified
                        "active", "open" -> Palette.goldPrimary
                        "declined" -> Palette.accentRed
                        else -> Palette.muted
                    }
                )
            }

            if (req != null) {
                val specs = listOfNotNull(
                    req.purity?.let { "Purity: $it" },
                    "Weight: ${req.formattedWeightRange}"
                ).joinToString(" · ")
                Text(
                    text = specs,
                    style = Typography.santoshi(13.sp),
                    color = Palette.muted
                )
            }

            if (!offer.offeredAt.isNullOrBlank()) {
                Text(
                    text = "Received: ${offer.offeredAt.take(10)}",
                    style = Typography.santoshi(12.sp),
                    color = Palette.muted
                )
            }
        }
    }
}
