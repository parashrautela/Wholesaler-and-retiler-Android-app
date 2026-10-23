// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakSliderFormView.swift
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.jewelindia.app.design.readableContentWidth
import com.jewelindia.app.data.api.ChamakAttribute
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ContentFlag
import kotlin.math.roundToInt


@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChamakSliderFormScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier
) {
    val requestAIConsent = rememberAIConsentRequest(wholesalerId)
    val scrollState = rememberScrollState()

    var previewImage by remember { mutableStateOf<ChamakViewerImage?>(null) }
    val selectedChipIds = remember { mutableStateListOf<String>() }

    val analysis = vm.currentGeneration?.stage1AnalysisJSON
    val isHardBlocked = analysis?.contentFlag != null && analysis.contentFlag != ContentFlag.Ok

    val d1 = vm.selectedDesign1
    val d2 = vm.selectedDesign2

    val defaultStrengths1 = listOf(
        "Intricate floral motifs", "Vibrant gemstone colors", "Balanced symmetry", "Elegant gold finish", "Lightweight design"
    )
    val defaultStrengths2 = listOf(
        "Intricate floral motifs", "Vibrant gemstone colors", "Balanced symmetry", "Elegant gold finish", "Lightweight design"
    )

    val fallbackAttributes = listOf(
        ChamakAttribute("attr_0", "Floral Motifs", "Motif", "Motif", 0.5),
        ChamakAttribute("attr_1", "Gemstone Colors", "Gemstone", "Gemstone", 0.5),
        ChamakAttribute("attr_2", "Symmetry", "Symmetry", "Symmetry", 0.5),
        ChamakAttribute("attr_3", "Gold Finish", "Finish", "Finish", 0.5)
    )

    val dynamic = analysis?.dynamicAttributes ?: emptyList()
    val attributes = if (dynamic.isEmpty()) fallbackAttributes else dynamic

    val d1Name = d1?.title ?: "Core Design"
    val d2Name = d2?.title ?: "New Direction"

    val availableChips = remember(d1Name, d2Name, analysis) {
        buildAvailableArtisanChips(d1Name, d2Name, analysis, defaultStrengths1, defaultStrengths2)
    }

    fun handleChipTap(chip: ArtisanChip) {
        if (selectedChipIds.contains(chip.id)) {
            selectedChipIds.remove(chip.id)
        } else {
            if (selectedChipIds.size >= 2) {
                selectedChipIds.removeAt(0)
            }
            selectedChipIds.add(chip.id)
        }
        val active = availableChips.filter { selectedChipIds.contains(it.id) }
        vm.noteText = active.joinToString(" ") { it.elaboratedPrompt }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F6))
    ) {
        Column(modifier = Modifier.fillMaxSize().readableContentWidth()) {
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
                    text = "Design Harmony",
                    fontFamily = Cirka,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

            // Scrollable form body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Comparison Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DesignThumbnailCard(
                        title = d1?.title ?: "Core design",
                        subtitle = "Keeps its identity",
                        design = d1,
                        accentColor = Color(0xFFCA8A04),
                        onZoom = {
                            d1?.imageURL?.let {
                                previewImage = ChamakViewerImage("d1", d1.title, it)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    DesignThumbnailCard(
                        title = d2?.title ?: "New direction",
                        subtitle = "New expression",
                        design = d2,
                        accentColor = Color(0xFF3B82F6),
                        onZoom = {
                            d2?.imageURL?.let {
                                previewImage = ChamakViewerImage("d2", d2.title, it)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Content Flag Banner
                if (analysis?.contentFlag != null && analysis.contentFlag != ContentFlag.Ok) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = analysis.contentFlag.userMessage ?: "Unsupported image content.",
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                // Warning Banners
                if (analysis != null) {
                    if (analysis.nearIdentical) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEFCE8))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFFCA8A04), modifier = Modifier.size(18.dp))
                            Text(
                                "These two designs look very similar. The fusion will emphasize subtle styling details.",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                color = Color(0xFF854D0E)
                            )
                        }
                    }
                    if (analysis.typeMismatch) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEFCE8))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFCA8A04), modifier = Modifier.size(18.dp))
                            Text(
                                "Designs appear to be different jewelry types. The model will create a hybrid blend.",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                color = Color(0xFF854D0E)
                            )
                        }
                    }
                }

                // AI Report Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ai Report",
                            fontFamily = Manrope,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "•••",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.muted
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Design 1 Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Core design",
                                fontFamily = Manrope,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                maxLines = 1
                            )
                            val list1 = (analysis?.image1Strengths ?: defaultStrengths1).take(5)
                            list1.forEach { item ->
                                val titleCased = item.split(" ").joinToString(" ") { word ->
                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                }
                                Text(
                                    text = "• $titleCased",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4B5563),
                                    maxLines = 2
                                )
                            }
                        }

                        // Design 2 Column
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "New direction",
                                fontFamily = Manrope,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6),
                                maxLines = 1
                            )
                            val list2 = (analysis?.image2Strengths ?: defaultStrengths2).take(5)
                            list2.forEach { item ->
                                val titleCased = item.split(" ").joinToString(" ") { word ->
                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                }
                                Text(
                                    text = "• $titleCased",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4B5563),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }

                // Fine Tune Sliders Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Fine Tune",
                        fontFamily = Manrope,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Core design",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "New direction",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6)
                        )
                    }

                    attributes.forEach { attr ->
                        val currentVal = vm.sliderValues[attr.id] ?: attr.defaultValue
                        val p1 = ((1.0 - currentVal) * 100).roundToInt()
                        val p2 = (currentVal * 100).roundToInt()

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = attr.name,
                                fontFamily = Manrope,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Palette.dark
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "$p1%",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Palette.muted,
                                    modifier = Modifier.width(36.dp)
                                )
                                Slider(
                                    value = currentVal.toFloat(),
                                    onValueChange = { newVal ->
                                        vm.sliderValues = vm.sliderValues + (attr.id to newVal.toDouble())
                                    },
                                    valueRange = 0f..1f,
                                    thumb = {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 32.dp, height = 22.dp)
                                                .shadow(3.dp, RoundedCornerShape(11.dp), spotColor = Color.Black.copy(alpha = 0.2f))
                                                .background(Color.White, RoundedCornerShape(11.dp))
                                        )
                                    },
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            sliderState = sliderState,
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = Color(0xFFD97706),
                                                inactiveTrackColor = Color(0xFFE5E7EB)
                                            ),
                                            modifier = Modifier.height(4.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$p2%",
                                    fontFamily = Manrope,
                                    fontSize = 13.sp,
                                    color = Palette.muted,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }
                    }
                }

                // Last Result Card (shown on revision)
                if (vm.currentGeneration?.outputImageURL != null && vm.signedOutputImageURL != null) {
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
                            text = if (vm.selectedAttempt != null) "Comparing with attempt #${vm.selectedAttempt}" else "Your last result",
                            fontFamily = Manrope,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "Adjust the sliders above against this design, then generate again. Earlier attempts stay in the result screen.",
                            fontFamily = Manrope,
                            fontSize = 12.sp,
                            color = Palette.muted
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.05f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF9F9F8))
                                .padding(12.dp)
                        ) {
                            vm.signedOutputImageURL?.let { url ->
                                ProtectedImage(
                                    url = url,
                                    contentDescription = "Last Result",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Customization Note Card
                ArtisanNoteSection(
                    availableChips = availableChips,
                    selectedChipIds = selectedChipIds,
                    onChipTap = { handleChipTap(it) },
                    noteText = vm.noteText,
                    onNoteChange = { vm.noteText = it }
                )

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
                                vm.submitFormAndGenerate(wholesalerId)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHardBlocked) Color(0xFF9CA3AF) else Color(0xFF212120)
                    ),
                    enabled = !isHardBlocked && !vm.isSubmitting
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
                                text = "Generate Design",
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

        // Full screen viewer preview
        previewImage?.let { img ->
            ChamakImageViewer(
                images = listOf(img),
                startIndex = 0,
                onDismiss = { previewImage = null }
            )
        }
    }
}


