// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakSetStylingView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakSlot
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.api.SetStylingChip
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChamakSetStylingScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var previewImage by remember { mutableStateOf<ChamakViewerImage?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F6))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { vm.resetToPicker() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Palette.dark
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Set Styling",
                    fontFamily = Cirka,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pieces comparison cards
                val pieces = vm.setPieces
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pieces.forEachIndexed { index, piece ->
                        PieceThumbnailCard(
                            slot = index + 1,
                            label = "Piece ${index + 1}",
                            design = piece,
                            accentColor = Color(ChamakSlot.colorHex(index + 1)),
                            onZoom = {
                                piece.imageURL?.let {
                                    previewImage = ChamakViewerImage("piece_${index + 1}", "Piece ${index + 1}", it)
                                }
                            },
                            modifier = Modifier.weight(1f, fill = true)
                        )
                    }
                }

                // Backdrop Presets Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose Backdrop",
                        fontFamily = Manrope,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    SetBackdrop.entries.forEach { preset ->
                        val isSelected = vm.selectedBackdrop == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFFDF8EE) else Color(0xFFF9FAFB))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFCA8A04) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { vm.selectedBackdrop = preset }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { vm.selectedBackdrop = preset },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFCA8A04)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = preset.label,
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Palette.dark
                                )
                                Text(
                                    text = preset.blurb,
                                    fontFamily = Manrope,
                                    fontSize = 11.sp,
                                    color = Palette.muted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Quick Styling Chips Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Styling",
                            fontFamily = Manrope,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "Optional",
                            fontFamily = Manrope,
                            fontSize = 11.sp,
                            color = Palette.muted
                        )
                    }

                    Text(
                        text = "Choose any options to guide composition. Your jewelry stays unchanged.",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        color = Palette.muted
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SetStylingChip.all.forEach { chip ->
                            val isSelected = vm.selectedStylingChips.contains(chip)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF111827) else Color(0xFFF9FAFB))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { vm.toggleStylingChip(chip) }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = chip.label,
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Palette.dark
                                )
                            }
                        }
                    }
                }

                // Artisan Staging Note
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Artisan Staging Note (Optional)",
                        fontFamily = Manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    OutlinedTextField(
                        value = vm.noteText,
                        onValueChange = { vm.noteText = it },
                        placeholder = {
                            Text(
                                "e.g. Place necklace prominent on velvet bust, earrings arranged symmetrically...",
                                fontFamily = Manrope,
                                fontSize = 13.sp,
                                color = Palette.muted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            vm.submitSetAndGenerate(wholesalerId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212120)),
                    enabled = !vm.isSubmitting
                ) {
                    if (vm.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Generate Set",
                                fontFamily = Manrope,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        previewImage?.let { img ->
            ChamakImageViewer(
                images = listOf(img),
                startIndex = 0,
                onDismiss = { previewImage = null }
            )
        }
    }
}

@Composable
private fun PieceThumbnailCard(
    slot: Int,
    label: String,
    design: ChamakDesignItem,
    accentColor: Color,
    onZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.5.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.04f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF3F4F6))
        ) {
            val url = design.displayURL(com.jewelindia.app.core.model.ImageSize.Card)
            if (url != null) {
                ProtectedImage(
                    url = url,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
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
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F8))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            Text(
                text = design.title,
                fontFamily = Manrope,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Palette.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
