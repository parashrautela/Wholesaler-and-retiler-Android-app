// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/ProductDetailSheet.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import com.jewelindia.app.core.consent.AIConsent
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import com.jewelindia.app.core.images.PickedFile
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.cirka
import com.jewelindia.app.design.manrope
import com.jewelindia.app.feature.wholesaler.chamak.ChamakImageViewer
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewerImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProductDetailSheet(
    product: Product,
    wholesalerId: String = "",
    onDismiss: () -> Unit,
    onPublishedChanged: ((Product) -> Unit)? = null,
    onEdit: ((Product) -> Unit)? = null,
    onUpdated: ((Product) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val requestAIConsent = rememberAIConsentRequest(wholesalerId)
    var currentProduct by remember(product) { mutableStateOf(product) }
    var activeImageURL by remember(product) { mutableStateOf(currentProduct.displayImageUrl(ImageSize.DETAIL)) }
    var viewerImageURL by remember(product) { mutableStateOf<String?>(null) }
    var isPublished by remember(product) { mutableStateOf(currentProduct.isPublished == true) }
    var isUpdatingPublish by remember { mutableStateOf(false) }
    var publishError by remember { mutableStateOf<String?>(null) }
    var inStock by remember(product) { mutableStateOf(currentProduct.stockAvailable != false) }
    var usage by remember { mutableStateOf<UploadUsage?>(null) }
    val reprocess = remember { ReprocessState() }
    var showReprocessConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(wholesalerId) {
        if (wholesalerId.isNotBlank()) {
            usage = try { WholesalerApi.fetchUploadUsage(wholesalerId) } catch (_: Exception) { null }
        }
    }

    val sku = remember(currentProduct.id) {
        val suffix = currentProduct.id.takeLast(6).uppercase()
        if (suffix.isEmpty()) "JWL-000000" else "JWL-$suffix"
    }

    val isLimitReached = usage?.let { it.limit != null && it.used >= it.limit } ?: false
    val ctaTitle = if (isLimitReached) {
        "Daily upload limit reached"
    } else if (currentProduct.hasDisplayImage) {
        "Re-upload to AI"
    } else {
        "Upload Image to AI"
    }

    fun startReprocess(baseFile: PickedFile?, imageCount: Int) {
        reprocess.status = "Initiating re-upload..."
        scope.launch {
            try {
                AIConsent.requireGranted(wholesalerId)
                reprocess.status = "Clearing old images..."
                WholesalerApi.clearProductImages(currentProduct.id, wholesalerId)

                reprocess.status = "Queueing AI pipeline..."
                WholesalerApi.reprocessProduct(currentProduct.id, wholesalerId, baseFile, imageCount)

                reprocess.status = "AI processing in progress..."
                val urls = pollReprocess(
                    productId = currentProduct.id,
                    onStatusChange = { reprocess.status = it }
                )

                val updated = currentProduct.copy(
                    processedImageURL = urls.firstOrNull(),
                    generatedImageURLs = urls
                )
                currentProduct = updated
                activeImageURL = updated.displayImageUrl(ImageSize.DETAIL)
                onUpdated?.invoke(updated)
                usage = try { WholesalerApi.fetchUploadUsage(wholesalerId) } catch (_: Exception) { null }

                reprocess.status = "Processing complete!"
                delay(1500)
                reprocess.reset()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                reprocess.failure = e.userMessage()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar: Close pill button on left, Title centered
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Close",
                        style = manrope(14f, JewelWeight.Semibold),
                        color = Palette.dark
                    )
                }

                Text(
                    text = currentProduct.title ?: "Product",
                    style = manrope(17f, JewelWeight.Bold),
                    color = Palette.dark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                // Placeholder to balance the Close button for perfect centering
                Spacer(modifier = Modifier.width(68.dp))
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F7)),
                    contentAlignment = Alignment.Center
                ) {
                    val url = activeImageURL
                    if (url != null) {
                        ProtectedImage(
                            url = url,
                            contentDescription = currentProduct.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        Button(
                            onClick = {
                                viewerImageURL = currentProduct.originalFor(url)
                                    ?.let { currentProduct.url(it, ImageSize.FULL) }
                                    ?: currentProduct.displayImageUrl(ImageSize.FULL)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                        ) {
                            Icon(Icons.Outlined.Fullscreen, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Full view")
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Diamond,
                                contentDescription = null,
                                tint = Palette.muted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Image Found",
                                style = manrope(13f, JewelWeight.Medium),
                                color = Palette.muted
                            )
                        }
                    }
                }

                // Thumbnail strip
                val thumbnails = currentProduct.thumbnailUrls()
                if (thumbnails.size > 1) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(thumbnails) { thumbUrl ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Palette.cream)
                                    .border(
                                        width = 2.dp,
                                        color = if (currentProduct.originalFor(thumbUrl) == currentProduct.originalFor(activeImageURL)) Palette.dark.copy(alpha = 0.3f) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        activeImageURL = currentProduct.originalFor(thumbUrl)
                                            ?.let { currentProduct.url(it, ImageSize.DETAIL) } ?: thumbUrl
                                    }
                            ) {
                                ProtectedImage(
                                    url = thumbUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Header
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentProduct.title ?: "Untitled Product",
                        style = cirka(22f, JewelWeight.Regular),
                        color = Palette.foreground
                    )

                    Text(
                        text = "${(currentProduct.category ?: "Jewellery").replaceFirstChar { it.uppercase() }} • $sku",
                        style = manrope(13f, JewelWeight.Regular),
                        color = Palette.muted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (inStock) Color(0xFF16A34A) else Color(0xFFDC2626))
                        )
                        Text(
                            text = if (inStock) "In stock" else "Out of stock",
                            style = manrope(12f, JewelWeight.Semibold),
                            color = if (inStock) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                        Text(
                            text = "|",
                            style = manrope(12f, JewelWeight.Regular),
                            color = Palette.muted
                        )
                        Text(
                            text = currentProduct.netWeight?.let { String.format(Locale.US, "%.2fg", it) } ?: "20g",
                            style = manrope(12f, JewelWeight.Medium),
                            color = Palette.muted
                        )
                    }
                }

                // Specifications
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF7F7F8))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Specifications",
                        style = manrope(14f, JewelWeight.Bold),
                        color = Palette.foreground
                    )

                    SpecRow("Purity", currentProduct.metalPurity?.uppercase() ?: "24K")
                    SpecRow("Gross weight", currentProduct.grossWeight?.let { String.format(Locale.US, "%.2fg", it) } ?: "-")
                    SpecRow("Stone weight", currentProduct.stoneWeight?.let { String.format(Locale.US, "%.2fg", it) } ?: "-")
                    SpecRow("Net weight", currentProduct.netWeight?.let { String.format(Locale.US, "%.2fg", it) } ?: "-")
                }

                // CTA: Upload Image to AI / Re-upload
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isLimitReached) Palette.border else Palette.dark,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = !isLimitReached) {
                            showReprocessConfirm = true
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ctaTitle,
                        style = manrope(14f, JewelWeight.Bold),
                        color = if (isLimitReached) Palette.muted else Palette.dark
                    )
                }

                // Publish to Retailer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Publish to Retailer",
                            style = manrope(15f, JewelWeight.Semibold),
                            color = if (currentProduct.canPublish) Palette.foreground else Palette.muted
                        )
                        if (!currentProduct.canPublish) {
                            Text(
                                text = "Available after AI pipeline processing is complete",
                                style = manrope(11f, JewelWeight.Regular),
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        publishError?.let { err ->
                            Text(
                                text = err,
                                style = manrope(12f, JewelWeight.Regular),
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    if (currentProduct.canPublish) {
                        Switch(
                            checked = isPublished,
                            enabled = !isUpdatingPublish,
                            onCheckedChange = { v ->
                                isPublished = v
                                isUpdatingPublish = true
                                publishError = null
                                scope.launch {
                                    try {
                                        WholesalerApi.updateProductFlags(currentProduct.id, isPublished = v)
                                        val updated = currentProduct.copy(isPublished = v)
                                        currentProduct = updated
                                        onPublishedChanged?.invoke(updated)
                                    } catch (e: Exception) {
                                        isPublished = !v
                                        val msg = e.userMessage()
                                        publishError = msg
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isUpdatingPublish = false
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF34C759)
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Palette.muted,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (reprocess.isActive) {
            ReprocessOverlay(
                state = reprocess,
                onDismiss = { reprocess.reset() }
            )
        }
    }

    viewerImageURL?.let { url ->
        Dialog(
            onDismissRequest = { viewerImageURL = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                securePolicy = SecureFlagPolicy.SecureOn
            )
        ) {
            ChamakImageViewer(
                images = listOf(ChamakViewerImage(currentProduct.id, currentProduct.title ?: "Product", url)),
                onDismiss = { viewerImageURL = null }
            )
        }
    }

    if (showReprocessConfirm) {
        ReprocessConfirmSheet(
            hasImages = currentProduct.hasDisplayImage,
            onConfirm = { file, imageCount ->
                showReprocessConfirm = false
                requestAIConsent { startReprocess(file, imageCount) }
            },
            onDismiss = { showReprocessConfirm = false }
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = manrope(13f, JewelWeight.Regular),
            color = Palette.muted
        )
        Text(
            text = value,
            style = manrope(13f, JewelWeight.Semibold),
            color = Palette.foreground
        )
    }
}
