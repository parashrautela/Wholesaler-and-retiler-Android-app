// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakHubView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette

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
            columns = GridCells.Fixed(2),
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
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )

                    // Chamak Studio Card
                    ChamakToolCard(
                        onClick = { launch = ChamakLaunch(ChamakMode.Fusion) }
                    )

                    // Set Creation Card
                    if (catalogueSource == ChamakCatalogueSource.OwnProducts) {
                        ChamakSetCreationCard(
                            onClick = { launch = ChamakLaunch(ChamakMode.SetCreation) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Gallery Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Gallery",
                            fontFamily = Cirka,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        val count = galleryVm.galleryGenerations.size
                        if (count > 0) {
                            Text(
                                text = "$count ${if (count == 1) "creation" else "creations"}",
                                fontFamily = Manrope,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }
                    Text(
                        text = "Everything you make stays private here. Nothing is published to your catalogue.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93)
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFAFAFA))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(20.dp))
                            .padding(vertical = 36.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chamak_empty_box),
                            contentDescription = null,
                            tint = Color(0xFFBB8651),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Nothing Here Yet",
                            fontFamily = Cirka,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "Your combined designs and sets will appear here as\nsoon as you make them.",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
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
private fun ChamakToolCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEDEDF0))
            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 20.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1.15f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Chamak Studio",
                fontFamily = Cirka,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reimagine your jewellery.\nCreate something new.",
                fontFamily = Manrope,
                fontSize = 12.5.sp,
                color = Color(0xFF6E6E73),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFC7C7CC))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Get started →",
                    fontFamily = Manrope,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.chamak_studio_artwork),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 130.dp, height = 180.dp)
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
        )
    }
}

@Composable
private fun ChamakSetCreationCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEDEDF0))
            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1.15f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Set Creation",
                fontFamily = Cirka,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Stage 2–4 pieces together. Choose\none item from each jewellery type.",
                fontFamily = Manrope,
                fontSize = 12.5.sp,
                color = Color(0xFF6E6E73),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFC7C7CC))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "Create a Set →",
                    fontFamily = Manrope,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.invite_reward_artwork),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 130.dp, height = 180.dp)
                .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
        )
    }
}
