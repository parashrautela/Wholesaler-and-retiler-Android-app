// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakGalleryView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.launch

@Composable
fun ChamakGalleryScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(wholesalerId) {
        vm.refreshGallery(wholesalerId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { vm.step = ChamakViewModel.Step.CatalogPicker },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Chamak Gallery",
                fontFamily = Cirka,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(36.dp))
        }

        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

        // Body content
        val error = vm.galleryErrorMessage
        val generations = vm.galleryGenerations

        if (error != null && generations.isEmpty()) {
            // Error State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Couldn't Load Your Gallery",
                        fontFamily = Cirka,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                    Text(
                        text = error,
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { scope.launch { vm.refreshGallery(wholesalerId) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !vm.isRefreshingGallery
                    ) {
                        if (vm.isRefreshingGallery) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Try Again",
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else if (generations.isEmpty() && !vm.isRefreshingGallery) {
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFBB8651),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Chamak Generations Yet",
                        fontFamily = Cirka,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                    Text(
                        text = "Combine your first pair of catalogue designs to see your AI creations stored here.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Palette.muted,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { vm.step = ChamakViewModel.Step.CatalogPicker },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Combine Designs",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(generations, key = { it.id }) { gen ->
                    ChamakGalleryCard(
                        generation = gen,
                        thumbnailURL = vm.galleryThumbnailURLs[gen.id],
                        onClick = {
                            scope.launch {
                                vm.openGalleryItem(gen)
                            }
                        }
                    )
                }
            }
        }
    }
}
