// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakImageViewer.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import kotlin.math.max
import kotlin.math.min

data class ChamakViewerImage(
    val id: String,
    val label: String,
    val url: String,
    val isResult: Boolean = false
)

@Composable
fun ChamakImageViewer(
    images: List<ChamakViewerImage>,
    startIndex: Int = 0,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var selection by remember { mutableIntStateOf(startIndex.coerceIn(0, images.size - 1)) }
    var isComparing by remember { mutableStateOf(false) }
    var compareIndex by remember { mutableIntStateOf(if (startIndex != 0) 0 else 1.coerceAtMost(images.size - 1)) }

    val hasResult = images.any { it.isResult }
    val canCompare = hasResult && images.size >= 2
    val currentImage = images[selection]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main image or compare view
        if (isComparing && canCompare) {
            val resultImage = images.first { it.isResult }
            val otherImage = images[compareIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    ZoomableProtectedImage(url = resultImage.url)
                    Text(
                        text = resultImage.label,
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .padding(12.dp)
                            .background(Color(0xFFE4CC8F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    ZoomableProtectedImage(url = otherImage.url)
                    Text(
                        text = otherImage.label,
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 120.dp)
            ) {
                ZoomableProtectedImage(url = currentImage.url)
            }
        }

            // Top Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterStart)
                        .background(Color(0xFF2C2C2E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (isComparing) "Compare" else currentImage.label,
                    fontFamily = Manrope,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center)
                )

                if (canCompare) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
                            .clickable { isComparing = !isComparing }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 11.dp)
                                .border(1.2.dp, Color.White, RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(5.dp)
                                    .background(Color.White)
                            )
                        }
                        Text(
                            text = if (isComparing) "Browse" else "Compare",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom Thumbnail Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isComparing) {
                    Text(
                        text = "Tap a design to compare it with the result",
                        fontFamily = Manrope,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    images.forEachIndexed { index, img ->
                        val isSelected = if (isComparing) index == compareIndex else index == selection
                        val isEnabled = !isComparing || !img.isResult
                        val topCat = when (img.label.lowercase()) {
                            "source" -> "Necklace"
                            "result" -> "Pendants"
                            else -> null
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(enabled = isEnabled) {
                                    if (isComparing) {
                                        compareIndex = index
                                    } else {
                                        selection = index
                                    }
                                }
                                .graphicsLayer { alpha = if (isEnabled) 1f else 0.4f }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E1E22))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFD4A359) else Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                ProtectedImage(
                                    url = img.url,
                                    contentDescription = img.label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (topCat != null) {
                                    Text(
                                        text = topCat,
                                        fontFamily = Manrope,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = img.label,
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun ZoomableProtectedImage(
    url: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val minScale = 1f
    val maxScale = 4f
    val doubleTapScale = 2.5f

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.1f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = doubleTapScale
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                    scale = newScale

                    if (newScale > 1f) {
                        val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                        val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentAlignment = Alignment.Center
        ) {
            ProtectedImage(
                url = url,
                contentDescription = "Zoomable Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                placeholderColor = null
            )
        }
    }
}
