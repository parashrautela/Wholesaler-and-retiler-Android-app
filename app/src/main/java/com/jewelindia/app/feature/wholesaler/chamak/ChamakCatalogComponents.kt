package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakSlot

@Composable
fun SlotCard(
    slotNumber: Int,
    emptyTitle: String,
    subtitle: String,
    designItem: ChamakDesignItem?,
    accentColor: Color,
    mode: ChamakMode,
    onAddPhoto: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFilled = designItem != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = 1.5.dp,
                color = if (isFilled) accentColor else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF9FAFB))
        ) {
            if (designItem != null) {
                val url = designItem.displayURL(ImageSize.Card)
                if (url != null) {
                    ProtectedImage(
                        url = url,
                        contentDescription = designItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(accentColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onAddPhoto,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(26.dp)
                        .background(accentColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = emptyTitle,
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Text(
                        text = subtitle,
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }

        if (designItem != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F8))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = designItem.title,
                    fontFamily = Manrope,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (mode == ChamakMode.SetCreation) {
                    Text(
                        text = designItem.displayTypeLabel,
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                } else {
                    Text(
                        text = if (slotNumber == 1) "Keeps its identity" else "New expression",
                        fontFamily = Manrope,
                        fontSize = 10.sp,
                        color = Palette.muted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogProductCard(
    product: Product,
    slot: Int?,
    isSelected: Boolean,
    isBlocked: Boolean,
    blockReason: ChamakSelectionBlockReason?,
    mode: ChamakMode,
    onClick: () -> Unit,
    onZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageURL = product.processedImageURL ?: product.imageURL ?: product.rawImageURL
    val slotColor = if (slot != null) Color(ChamakSlot.colorHex(slot)) else Color(0xFFCA8A04)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) slotColor else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.03f))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF3F4F6))
        ) {
            if (imageURL != null) {
                ProtectedImage(
                    url = imageURL,
                    contentDescription = product.title ?: "Design",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        val text = when (blockReason) {
                            is ChamakSelectionBlockReason.SameTypeSelected -> "${blockReason.label} in set"
                            is ChamakSelectionBlockReason.SetFull -> "Set full (4/4)"
                            is ChamakSelectionBlockReason.MissingImage -> "No image"
                            is ChamakSelectionBlockReason.MissingType -> "No type"
                            null -> ""
                        }
                        Text(
                            text = text,
                            fontFamily = Manrope,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (imageURL != null && !isBlocked) {
                IconButton(
                    onClick = onZoom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(26.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand",
                        tint = Palette.dark,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            if (isSelected && slot != null) {
                Text(
                    text = ChamakSlot.label(slot, mode),
                    fontFamily = Manrope,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(slotColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = product.title ?: "Catalogue Design",
                fontFamily = Manrope,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isBlocked) Palette.muted else Palette.dark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.jewelleryType?.replaceFirstChar { it.titlecase() } ?: "Jewellery",
                fontFamily = Manrope,
                fontSize = 11.sp,
                color = Palette.muted,
                maxLines = 1
            )
        }
    }
}
