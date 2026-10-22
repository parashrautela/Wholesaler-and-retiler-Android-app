// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift (LatestCreationsSection)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.cirka
import com.jewelindia.app.design.gilroy
import com.jewelindia.app.design.manrope
import com.jewelindia.app.feature.wholesaler.chamak.ChamakGalleryCard

@Composable
fun LatestCreationsSection(
    wholesalerUserId: String,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenGeneration: (ChamakGeneration) -> Unit = {},
    peekGenerations: List<ChamakGeneration>? = null
) {
    var selectedMode by remember { mutableStateOf(ChamakMode.Fusion) }
    var generations by remember { mutableStateOf<List<ChamakGeneration>>(peekGenerations ?: emptyList()) }
    var thumbnailUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(peekGenerations == null && wholesalerUserId.isNotBlank()) }

    LaunchedEffect(wholesalerUserId, peekGenerations) {
        if (peekGenerations != null) {
            generations = peekGenerations
            thumbnailUrls = peekGenerations.associate { gen ->
                gen.id to (gen.outputPath(ImageSize.CARD) ?: gen.outputImageURL ?: "")
            }
            isLoading = false
            return@LaunchedEffect
        }
        if (wholesalerUserId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            isLoading = true
            val gens = ChamakApi.fetchWholesalerGallery(wholesalerUserId)
            generations = gens
            val paths = gens.mapNotNull { it.outputPath(ImageSize.CARD) }
            val signed = try { ChamakApi.getSignedURLs(paths) } catch (_: Exception) { emptyMap() }
            thumbnailUrls = gens.associate { gen ->
                gen.id to (signed[gen.outputPath(ImageSize.CARD)] ?: gen.outputPath(ImageSize.CARD) ?: gen.outputImageURL ?: "")
            }
        } catch (_: Exception) {
            // fallback to empty state
        } finally {
            isLoading = false
        }
    }

    val filtered = remember(generations, selectedMode) {
        generations.filter { it.mode == selectedMode && it.status == ChamakStatus.Done }.take(4)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Header Row: Latest Creations + See all
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Latest Creations",
                style = cirka(34f, JewelWeight.Bold),
                color = Palette.dark
            )
            Text(
                text = "See all",
                style = manrope(13f, JewelWeight.Semibold),
                color = Palette.dark,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onSeeAll() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your newest Chamak designs and sets.",
            style = gilroy(14f, JewelWeight.Medium),
            color = Palette.muted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Segmented Control: Chamak | Sets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFEFEFEF))
                .padding(2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Chamak tab
            val isChamak = selectedMode == ChamakMode.Fusion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .then(
                        if (isChamak) {
                            Modifier
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(7.dp))
                                .background(Color.White)
                        } else Modifier
                    )
                    .clickable { selectedMode = ChamakMode.Fusion },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chamak",
                    style = manrope(13f, if (isChamak) JewelWeight.Bold else JewelWeight.Medium),
                    color = if (isChamak) Palette.dark else Palette.dark.copy(alpha = 0.6f)
                )
            }

            // Sets tab
            val isSets = selectedMode == ChamakMode.SetCreation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .then(
                        if (isSets) {
                            Modifier
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(7.dp))
                                .background(Color.White)
                        } else Modifier
                    )
                    .clickable { selectedMode = ChamakMode.SetCreation },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sets",
                    style = manrope(13f, if (isSets) JewelWeight.Bold else JewelWeight.Medium),
                    color = if (isSets) Palette.dark else Palette.dark.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content: Grid or Empty or Loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Palette.dark,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }
        } else if (filtered.isEmpty()) {
            // Empty state row matching iOS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9F9F8))
                    .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(16.dp))
                    .clickable { onSeeAll() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFBB8651),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedMode == ChamakMode.SetCreation) {
                        "No sets yet. Create your first set."
                    } else {
                        "No designs yet. Create with Chamak."
                    },
                    style = manrope(13f, JewelWeight.Medium),
                    color = Palette.dark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Palette.muted,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            // 2-column Grid of Chamak cards
            for (i in filtered.indices step 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val item1 = filtered[i]
                    ChamakGalleryCard(
                        generation = item1,
                        thumbnailURL = thumbnailUrls[item1.id],
                        onClick = { onOpenGeneration(item1) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < filtered.size) {
                        val item2 = filtered[i + 1]
                        ChamakGalleryCard(
                            generation = item2,
                            thumbnailURL = thumbnailUrls[item2.id],
                            onClick = { onOpenGeneration(item2) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

typealias VoidUnit = () -> Unit
