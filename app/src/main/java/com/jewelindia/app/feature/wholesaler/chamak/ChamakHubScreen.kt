// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakHubView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode

data class ChamakLaunch(
    val mode: ChamakMode,
    val generation: ChamakGeneration? = null
)

@Composable
fun ChamakHubScreen(
    wholesalerId: String,
    catalogueSource: ChamakCatalogueSource = ChamakCatalogueSource.OwnProducts,
    previewViewModel: ChamakViewModel? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var launch by remember { mutableStateOf<ChamakLaunch?>(null) }
    val galleryVm = previewViewModel ?: remember {
        ChamakViewModel().apply {
            this.catalogueSource = catalogueSource
        }
    }

    LaunchedEffect(wholesalerId) {
        if (previewViewModel == null) {
            galleryVm.refreshGallery(wholesalerId)
        }
    }

    val activeLaunch = launch
    if (activeLaunch != null) {
        ChamakFlowCoordinator(
            wholesalerId = wholesalerId,
            mode = activeLaunch.mode,
            openingGeneration = activeLaunch.generation,
            catalogueSource = catalogueSource,
            onClose = {
                launch = null
                galleryVm.resetToPicker()
            }
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Title & Tools
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Chamak Studio",
                        fontFamily = Cirka,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    // Fusion Card (Gold)
                    ChamakFusionCard(
                        onClick = { launch = ChamakLaunch(ChamakMode.Fusion) }
                    )

                    // Set Creation Card
                    if (catalogueSource == ChamakCatalogueSource.OwnProducts) {
                        ChamakSetCreationCard(
                            onClick = { launch = ChamakLaunch(ChamakMode.SetCreation) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gallery Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Gallery",
                            fontFamily = Cirka,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        val count = galleryVm.galleryGenerations.size
                        if (count > 0) {
                            Text(
                                text = "$count ${if (count == 1) "creation" else "creations"}",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                color = Palette.muted
                            )
                        }
                    }
                    Text(
                        text = "Everything you make stays private here. Nothing is published to your catalogue.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Palette.muted
                    )
                }
            }

            // Gallery Cards
            if (galleryVm.isRefreshingGallery && galleryVm.galleryGenerations.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Palette.dark)
                    }
                }
            } else if (galleryVm.galleryGenerations.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFBB8651),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Chamak Generations Yet",
                            fontFamily = Cirka,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "Combine your first pair of catalogue designs to see your AI creations stored here.",
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            color = Palette.muted
                        )
                    }
                }
            } else {
                items(galleryVm.galleryGenerations, key = { it.id }) { gen ->
                    ChamakGalleryCard(
                        generation = gen,
                        thumbnailURL = galleryVm.galleryThumbnailURLs[gen.id],
                        onClick = {
                            launch = ChamakLaunch(mode = gen.mode, generation = gen)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChamakFusionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFBB8651), Color(0xFFF6E0A7))
                )
            )
            .border(1.dp, Color(0xFFE4CC8F).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Chamak Studio",
            fontFamily = Cirka,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Synthesizes the best elements of two catalogue designs into a bold, original piece.",
            fontFamily = Manrope,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.95f),
            lineHeight = 18.sp
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(
                text = "Combine Designs",
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ChamakSetCreationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F3F3))
            .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(20.dp))
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Set Creation",
            fontFamily = Cirka,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF323232)
        )
        Text(
            text = "Stage 2–4 pieces together. Choose one item from each jewellery type.",
            fontFamily = Manrope,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF494949).copy(alpha = 0.85f),
            lineHeight = 18.sp
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFB6B6B6), Color(0xFF9E9E9E))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Create a Set",
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Icon(Icons.Default.ChevronRight, null, tint = Color.Black, modifier = Modifier.size(16.dp))
        }
    }
}
