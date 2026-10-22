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
import androidx.compose.foundation.layout.heightIn
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
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChamakSetStylingScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier
) {
    val requestAIConsent = rememberAIConsentRequest(wholesalerId)
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
                    modifier = Modifier.size(48.dp)
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
                // Backdrop Presets Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Choose Backdrop",
                        fontFamily = Manrope,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    SetBackdrop.entries.forEach { preset ->
                        val isSelected = vm.selectedBackdrop == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFFFFDF8) else Color(0xFFF9FAFB))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFCA8A04) else Color(0xFFF3F4F6),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { vm.selectedBackdrop = preset }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(2.dp, Color(0xFFCA8A04), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .background(Color(0xFFCA8A04), CircleShape)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(0xFFE5E7EB), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = preset.label,
                                    fontFamily = Manrope,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Palette.dark
                                )
                                Text(
                                    text = preset.blurb,
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Palette.muted,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                // Quick Styling Chips Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Styling",
                            fontFamily = Manrope,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "Optional",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            color = Palette.muted
                        )
                    }

                    Text(
                        text = "Choose any options to guide composition. Your jewelry stays unchanged.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Palette.muted
                    )

                    val primaryChips = listOf(
                        Pair(SetStylingChip("balanced", "Balanced", ""), SetStylingChip("equal_focus", "Equal focus", "")),
                        Pair(SetStylingChip("luxury", "Luxury showroom", ""), SetStylingChip("minimal", "Minimal", "")),
                        Pair(SetStylingChip("soft_light", "Soft light", ""), SetStylingChip("detail", "Show detail", ""))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        primaryChips.forEach { (c1, c2) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(c1, c2).forEach { chip ->
                                    val isSelected = vm.selectedStylingChips.any { it.id == chip.id }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(21.dp))
                                            .background(if (isSelected) Color(0xFF212120) else Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                                                shape = RoundedCornerShape(21.dp)
                                            )
                                            .clickable {
                                                val match = SetStylingChip.all.find { it.id == chip.id } ?: chip
                                                vm.toggleStylingChip(match)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chip.label,
                                            fontFamily = Manrope,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Palette.dark
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                        requestAIConsent {
                            vm.runAction {
                                vm.submitSetAndGenerate(wholesalerId)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
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
