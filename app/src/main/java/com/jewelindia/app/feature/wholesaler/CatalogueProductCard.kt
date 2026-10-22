// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerCatalogueView.swift (CatalogueProductCard)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import java.util.Locale

@Composable
fun CatalogueProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    isSelecting: Boolean = false,
    isSelected: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    val metadataLine = remember(product) {
        val parts = mutableListOf<String>()
        val category = product.category
        if (!category.isNullOrBlank()) {
            parts.add(category.replaceFirstChar { it.uppercase() })
        }
        val netWeight = product.netWeight
        if (netWeight != null) {
            parts.add(String.format(Locale.US, "Net %.2f g", netWeight))
        }
        parts.joinToString(" · ")
    }

    val cardAlpha = if (isSelecting && product.isPublished != true) 0.45f else 1f
    val borderColor = if (isSelecting && isSelected) Palette.dark else Color(0xFFF0F0F0)
    val borderWidth = if (isSelecting && isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
            .clickable(onClick = onClick)
    ) {
        // 1:1 Square Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Palette.cream)
        ) {
            val imageUrl = product.displayImageUrl(ImageSize.CARD)
            if (imageUrl != null) {
                ProtectedImage(
                    url = imageUrl,
                    fallbackUrl = product.displaySource,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = Palette.muted,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }

            product.processingLabel?.let { label ->
                Text(
                    text = label,
                    color = Palette.dark,
                    style = manrope(10f, JewelWeight.Semibold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.94f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Selection Checkmark Circle Overlay (Top-Left)
            if (isSelecting) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(26.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isSelected) Palette.dark else Color.Transparent)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Options Menu (•••) — hidden in select mode
            if (!isSelecting) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Palette.dark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = null,
                                        tint = Palette.dark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Edit Details", color = Palette.dark)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Delete Product", color = Color(0xFFDC2626))
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }

        // Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Row 1: Title
            Text(
                text = product.title ?: "Untitled Product",
                style = manrope(14f, JewelWeight.Bold),
                color = Palette.foreground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 2: Metadata + Purity Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (metadataLine.isNotEmpty()) {
                    Text(
                        text = metadataLine,
                        modifier = Modifier.weight(1f),
                        style = manrope(12f, JewelWeight.Medium),
                        color = Palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (metadataLine.isEmpty()) Spacer(Modifier.weight(1f))
                val purity = product.metalPurity
                if (!purity.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 64.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFFBF4))
                            .border(1.dp, Color(0xFFF3E8D6), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = purity.uppercase(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = manrope(10f, JewelWeight.Bold),
                            color = Color(0xFF9A6B2F)
                        )
                    }
                }
            }

            // Row 3: Availability
            if (product.stockAvailable == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "In Stock",
                        style = manrope(11f, JewelWeight.Semibold),
                        color = Color(0xFF16A34A)
                    )
                }
            } else if (product.makeToOrderDays != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Made to order · ${product.makeToOrderDays} days",
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = manrope(11f, JewelWeight.Semibold),
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}
