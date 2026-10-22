// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/WholesalerOfferDetailView.swift
package com.jewelindia.app.feature.manufacturing

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerOfferDetailScreen(
    offerId: String,
    onBack: () -> Unit
) {
    var offer by remember { mutableStateOf<ManufacturingOffer?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showAcceptSheet by remember { mutableStateOf(false) }
    var showDeclineSheet by remember { mutableStateOf(false) }
    val acceptSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val declineSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            offer = try {
                ManufacturingApi.fetchWholesalerOfferDetail(offerId)
            } catch (_: Exception) {
                null
            }
            loading = false
        }
    }

    LaunchedEffect(offerId) {
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
                text = "Offer Details",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (offer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unable to load offer details", color = Palette.muted)
            }
        } else {
            val currentOffer = offer!!
            val req = currentOffer.request

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Request Image
                val img = currentOffer.imageUrl ?: req?.imageUrl
                if (!img.isNullOrBlank()) {
                    CachedImage(
                        url = img,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Info card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.cream)
                        .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = req?.let { "${it.material} ${it.category}" } ?: "Manufacturing Request",
                            style = Typography.cirka(20.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )

                        Text(
                            text = "Status: ${currentOffer.status.replaceFirstChar { it.uppercase() }}",
                            style = Typography.santoshi(14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = when (currentOffer.status) {
                                "accepted" -> Palette.statusVerified
                                "active", "open" -> Palette.goldPrimary
                                "declined" -> Palette.accentRed
                                else -> Palette.muted
                            }
                        )

                        if (!req?.notes.isNullOrBlank()) {
                            Text(
                                text = req.notes.orEmpty(),
                                style = Typography.santoshi(14.sp),
                                color = Palette.dark
                            )
                        }
                    }
                }

                // Specifications
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.cream)
                        .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Specifications",
                            style = Typography.cirka(16.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        req?.material?.let { Text("Material: $it", style = Typography.santoshi(13.sp), color = Palette.dark) }
                        req?.purity?.let { Text("Purity: $it", style = Typography.santoshi(13.sp), color = Palette.dark) }
                        req?.formattedWeightRange?.let { Text("Weight Range: $it", style = Typography.santoshi(13.sp), color = Palette.dark) }
                        req?.quantity?.let { Text("Quantity: $it pieces", style = Typography.santoshi(13.sp), color = Palette.dark) }
                        req?.deliveryNeededDate?.let { Text("Needed By: $it", style = Typography.santoshi(13.sp), color = Palette.dark) }
                    }
                }

                // Actions if open or active
                if (currentOffer.status in listOf("active", "open")) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeclineSheet = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Decline", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAcceptSheet = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
                        ) {
                            Text("Quote Now", color = Palette.light, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showAcceptSheet) {
                AcceptQuoteSheet(
                    offer = currentOffer,
                    sheetState = acceptSheetState,
                    onDismiss = { showAcceptSheet = false },
                    onSuccess = { load() }
                )
            }

            if (showDeclineSheet) {
                DeclineOfferSheet(
                    offer = currentOffer,
                    sheetState = declineSheetState,
                    onDismiss = { showDeclineSheet = false },
                    onSuccess = { load() }
                )
            }
        }
    }
}
