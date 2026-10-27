// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakSliderFormView.swift (Sub-components)
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.Stage1Analysis

data class ArtisanChip(
    val id: String,
    val title: String,
    val elaboratedPrompt: String
)

fun buildAvailableArtisanChips(
    d1Name: String,
    d2Name: String,
    analysis: Stage1Analysis?,
    defaultStrengths1: List<String>,
    defaultStrengths2: List<String>
): List<ArtisanChip> {
    val list = mutableListOf<ArtisanChip>()
    list.add(
        ArtisanChip(
            id = "integrity_d2",
            title = "More of $d2Name",
            elaboratedPrompt = "Bring in more of the look of $d2Name: its motifs, stones and details. Keep the type and shape of $d1Name."
        )
    )
    list.add(
        ArtisanChip(
            id = "heritage_d1",
            title = "More of $d1Name",
            elaboratedPrompt = "Keep the piece close to $d1Name: its shape, motifs and details lead, with only light touches from $d2Name."
        )
    )
    val s1 = (analysis?.image1Strengths ?: defaultStrengths1).firstOrNull()
    if (s1 != null) {
        list.add(
            ArtisanChip(
                id = "strength_d1",
                title = "Highlight: ${s1.replaceFirstChar { it.titlecase() }}",
                elaboratedPrompt = "Make the $s1 from $d1Name stand out as a main feature of the piece."
            )
        )
    }
    val s2 = (analysis?.image2Strengths ?: defaultStrengths2).firstOrNull()
    if (s2 != null) {
        list.add(
            ArtisanChip(
                id = "strength_d2",
                title = "Add: ${s2.replaceFirstChar { it.titlecase() }}",
                elaboratedPrompt = "Bring the $s2 from $d2Name into the piece."
            )
        )
    }
    list.add(
        ArtisanChip(
            id = "gold_finish",
            title = "Rich yellow gold",
            elaboratedPrompt = "Use a rich 22K yellow gold colour for all the metal."
        )
    )
    list.add(
        ArtisanChip(
            id = "filigree",
            title = "Fine wire work",
            elaboratedPrompt = "Add neat, even, fine gold wirework (filigree) in clean open patterns."
        )
    )
    list.add(
        ArtisanChip(
            id = "gemstones",
            title = "Bright, clear stones",
            elaboratedPrompt = "Use bright, clear stones, each set cleanly in a proper setting."
        )
    )
    list.add(
        ArtisanChip(
            id = "temple",
            title = "Temple style",
            elaboratedPrompt = "Use traditional South Indian temple motifs, such as a deity, peacock or lotus, cleanly sculpted."
        )
    )
    return list
}

@Composable
fun DesignThumbnailCard(
    title: String,
    subtitle: String,
    design: ChamakDesignItem?,
    accentColor: Color,
    onZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF3F4F6))
            .border(1.5.dp, accentColor, RoundedCornerShape(22.dp))
    ) {
        val url = design?.displayURL(ImageSize.Card)
        if (url != null) {
            ProtectedImage(
                url = url,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onZoom,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Expand",
                    tint = Palette.dark,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontFamily = Manrope,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = Palette.muted,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtisanNoteSection(
    availableChips: List<ArtisanChip>,
    selectedChipIds: List<String>,
    onChipTap: (ArtisanChip) -> Unit,
    noteText: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Artisan Customization Note (Optional)",
                fontFamily = Manrope,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            Text(
                text = if (selectedChipIds.isEmpty()) "Select 1 or 2" else "${selectedChipIds.size}/2 selected",
                fontFamily = Manrope,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedChipIds.isEmpty()) Palette.muted else Color(0xFFCA8A04),
                modifier = Modifier
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableChips.forEach { chip ->
                val isSelected = selectedChipIds.contains(chip.id)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFF212120) else Color(0xFFF3F4F6))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onChipTap(chip) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
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
                        text = chip.title,
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Palette.dark
                    )
                }
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            placeholder = {
                Text(
                    "Select chips above or write artisan guidance here...",
                    fontFamily = Manrope,
                    fontSize = 13.sp,
                    color = Palette.muted
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
