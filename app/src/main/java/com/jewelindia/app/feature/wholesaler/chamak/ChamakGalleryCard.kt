// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakGalleryCard.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.design.Manrope

@Composable
fun ChamakGalleryCard(
    generation: ChamakGeneration,
    thumbnailURL: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF3F4F6))
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
            .shadow(2.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
    ) {
        when (generation.status) {
            ChamakStatus.Done -> {
                if (thumbnailURL != null) {
                    ProtectedImage(
                        url = thumbnailURL,
                        fallbackUrl = generation.outputImageURL?.let {
                            if (it.startsWith("http") || it.startsWith("chamak-outputs/")) it else "chamak-outputs/$it"
                        },
                        contentDescription = "Chamak Generation",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Placeholder(
                        icon = Icons.Default.AutoAwesome,
                        tint = Color(0xFFBB8651),
                        text = "Tap to load output"
                    )
                }

                // Stacked source thumbnails in bottom-right corner
                val sources = listOfNotNull(
                    generation.sourceImage1URL.takeIf { it.isNotBlank() },
                    generation.sourceImage2URL.takeIf { it.isNotBlank() }
                )
                if (sources.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(3.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            sources.forEach { url ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF222222))
                                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
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
