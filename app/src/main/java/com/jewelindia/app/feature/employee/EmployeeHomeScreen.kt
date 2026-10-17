// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeHomeView.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.CloudinaryArt
import com.jewelindia.app.data.api.EmployeeApi
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.profile.StaffProfileButton
import kotlinx.coroutines.launch

@Composable
fun EmployeeHomeScreen(
    session: EmployeeSession,
    store: EmployeeStore,
    onOpenCatalogue: () -> Unit,
    onOpenCanvas: () -> Unit,
    onOpenDetail: (Product) -> Unit,
    onOpenStylePicker: () -> Unit,
    onOpenProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var designs by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loadingDesigns by remember { mutableStateOf(true) }

    LaunchedEffect(session.retailerId) {
        scope.launch {
            try {
                val list = EmployeeApi.fetchSelectedProducts(session.retailerId)
                designs = list
            } catch (_: Exception) {
            } finally {
                loadingDesigns = false
            }
        }
    }

    val shuffledDesigns = remember(designs, session.seedId) {
        WebShuffle.shuffled(designs, session.seedId, 6)
    }

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(Palette.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 120.dp)
        ) {
            // Full-bleed Hero with Theme Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                // Background theme artwork
                CachedImage(
                    url = session.theme.heroBackground,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Decorative Jali Lattice
                EmployeeJaliPattern(
                    modifier = Modifier.fillMaxSize(),
                    color = Palette.goldPrimary.copy(alpha = 0.08f)
                )

                // Hero Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top row: Brand & Style Picker Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Palette.goldPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Palette.goldPrimary)
                                )
                                Text(
                                    text = "SHOWROOM PORTAL",
                                    style = Typography.santoshi(11.sp, FontWeight.Bold),
                                    color = Palette.goldPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = onOpenStylePicker,
                                color = Color.Black.copy(alpha = 0.45f),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = "Personal Style",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            StaffProfileButton(action = onOpenProfile)
                        }
                    }

                    // Bottom info: Store Logo, Name, Subtitle
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        session.storeLogoURL?.let { logoUrl ->
                            CachedImage(
                                url = logoUrl,
                                contentDescription = session.storeName,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Palette.goldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = session.storeName,
                            style = Typography.cirka(28.sp),
                            color = Color.White
                        )

                        Text(
                            text = "Exclusively curated bridal & fine jewellery collections",
                            style = Typography.santoshi(13.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // View Mode Card & Endless Canvas Entry
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Endless Canvas Card
                Card(
                    onClick = onOpenCanvas,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Palette.goldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Palette.goldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Palette.goldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Endless Canvas",
                                    style = Typography.santoshi(16.sp, FontWeight.Bold),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = "Interactive presentation wall for customers",
                                    style = Typography.santoshi(12.sp),
                                    color = Palette.textSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Palette.goldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Curated Designs Section (WebShuffle per-staff order)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TODAY'S SELECTION",
                            style = Typography.santoshi(11.sp, FontWeight.Bold),
                            color = Palette.goldPrimary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Personalised Curation",
                            style = Typography.cirka(20.sp),
                            color = Palette.textPrimary
                        )
                    }

                    Surface(
                        onClick = onOpenCatalogue,
                        color = Color.Transparent
                    ) {
                        Text(
                            text = "View All →",
                            style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                            color = Palette.goldPrimary
                        )
                    }
                }

                if (loadingDesigns) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Palette.goldPrimary, modifier = Modifier.size(32.dp))
                    }
                } else if (shuffledDesigns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .background(Palette.surfaceElevated, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No curated designs shortlisted yet.\nExplore the store catalogue to curate pieces.",
                            style = Typography.santoshi(14.sp),
                            color = Palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(shuffledDesigns, key = { it.id }) { product ->
                            EmployeeDesignCard(
                                product = product,
                                isSelected = store.state.value.selectedProductIds.contains(product.id),
                                onClick = { onOpenDetail(product) },
                                onLongClick = { store.toggleSelection(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmployeeDesignCard(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Palette.surfaceElevated),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
        ),
        modifier = modifier
            .width(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onLongClickLabel = "Select design for request"
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            ) {
                ProtectedImage(
                    url = product.primaryImage,
                    contentDescription = product.displayTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                    Surface(
                        color = Palette.goldPrimary,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
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
                    style = Typography.santoshi(13.sp, FontWeight.SemiBold),
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
