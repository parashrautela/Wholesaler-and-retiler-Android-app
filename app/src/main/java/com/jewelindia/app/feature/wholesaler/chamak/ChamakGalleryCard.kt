// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakGalleryCard.swift
package com.jewelindia.app.feature.wholesaler.chamak

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChamakGalleryCard(
    generation: ChamakGeneration,
    thumbnailURL: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSet = generation.mode == ChamakMode.SetCreation

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
    ) {
        // Photo area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF3F4F6))
        ) {
            when (generation.status) {
                ChamakStatus.Done -> {
                    if (thumbnailURL != null) {
                        ProtectedImage(
                            url = thumbnailURL,
                            contentDescription = "Chamak Generation",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Placeholder(
                            icon = Icons.Default.AutoAwesome,
                            tint = Color(0xFFBB8651),
                            text = null
                        )
                    }
                }
                ChamakStatus.Queued, ChamakStatus.Analyzing, ChamakStatus.Generating -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFBB8651),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Creating…",
                            fontFamily = Manrope,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
                ChamakStatus.AwaitingInput -> {
                    Placeholder(
                        icon = Icons.Default.Tune,
                        tint = Color(0xFF2563EB),
                        text = "Needs your input"
                    )
                }
                ChamakStatus.Failed -> {
                    Placeholder(
                        icon = Icons.Default.Warning,
                        tint = Color(0xFFDC2626),
                        text = "Didn't finish"
                    )
                }
            }

            // Stacked source designs in the corner
            val sources = listOfNotNull(
                generation.sourceImage1URL.takeIf { it.startsWith("http") },
                generation.sourceImage2URL.takeIf { it.startsWith("http") }
            )
            if (sources.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    sources.forEach { url ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3F4F6))
                                .border(1.5.dp, Color.White, RoundedCornerShape(6.dp))
                        ) {
                            ProtectedImage(
                                url = url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Subtitle + Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            Text(
                text = if (isSet) "Set Creation" else "Chamak Studio",
                fontFamily = Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.dark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val date = formatDate(generation.createdAt)
            val subtitle = listOfNotNull(date, if (isSet) generation.setBackdrop?.label else null)
                .joinToString(" · ")
            Text(
                text = subtitle,
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

@Composable
private fun Placeholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            if (text != null) {
                Text(
                    text = text,
                    fontFamily = Manrope,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tint
                )
            }
        }
    }
}

private fun formatDate(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(raw.substringBefore(".")) ?: return "Recent"
        val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        "Recent"
    }
}
