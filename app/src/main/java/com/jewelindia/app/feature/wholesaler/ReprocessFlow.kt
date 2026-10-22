// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/ProductDetailSheet.swift (ReprocessState, ReprocessOverlay, ReprocessConfirmSheet)
package com.jewelindia.app.feature.wholesaler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.images.PickedFile
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ReprocessState(
    status: String? = null,
    failure: String? = null
) {
    var status by mutableStateOf(status)
    var failure by mutableStateOf(failure)
    val isActive: Boolean get() = status != null || failure != null

    fun reset() {
        status = null
        failure = null
    }
}

suspend fun pollReprocess(
    productId: String,
    onStatusChange: (String) -> Unit,
    timeoutMillis: Long = WholesalerApi.POLL_TIMEOUT_SECONDS * 1000L,
    pollIntervalMillis: Long = WholesalerApi.POLL_INTERVAL_MILLIS,
    pollTickProvider: suspend (String, Boolean) -> WholesalerApi.ReprocessPollTick = { id, bg ->
        WholesalerApi.reprocessPollTick(id, bg)
    }
): List<String> {
    var elapsed = 0L
    var backgroundRemoved = false

    while (elapsed < timeoutMillis) {
        when (val tick = pollTickProvider(productId, backgroundRemoved)) {
            is WholesalerApi.ReprocessPollTick.Done -> return tick.urls
            is WholesalerApi.ReprocessPollTick.Pending -> {
                if (tick.backgroundRemoved && !backgroundRemoved) {
                    backgroundRemoved = true
                    onStatusChange("Background removed, enhancing...")
                }
            }
        }
        delay(pollIntervalMillis)
        elapsed += pollIntervalMillis
    }
    throw WholesalerApi.PipelineException("Timed out waiting for processed images (5 min). Please retry.")
}

@Composable
fun ReprocessOverlay(
    state: ReprocessState,
    onDismissFailure: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.97f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        val failure = state.failure
        if (failure != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Palette.accentRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PriorityHigh,
                        contentDescription = null,
                        tint = Palette.accentRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Reprocessing Failed",
                    style = Typography.santoshi(16.sp, FontWeight.Bold),
                    color = Palette.accentRed
                )

                Text(
                    text = failure,
                    style = Typography.santoshi(12.sp),
                    color = Palette.textSecondary,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        state.reset()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.dark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Close", style = Typography.santoshi(14.sp, FontWeight.Bold))
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(color = Palette.goldPrimary)
                Text(
                    text = state.status.orEmpty(),
                    style = Typography.santoshi(15.sp, FontWeight.Bold),
                    color = Palette.textPrimary
                )
                Text(
                    text = "This may take up to a minute",
                    style = Typography.santoshi(12.sp),
                    color = Palette.textSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReprocessConfirmSheet(
    hasImages: Boolean,
    onConfirm: (PickedFile?, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var newFile by remember { mutableStateOf<PickedFile?>(null) }
    var imageCount by remember { mutableStateOf(2) }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var isReadingImage by remember { mutableStateOf(false) }
    var readError by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && !isReadingImage) {
            previewUri = uri
            readError = null
            isReadingImage = true
            scope.launch {
                try {
                    val jpeg = withContext(Dispatchers.IO) {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("Couldn't read this photo. Please choose another image.")
                        ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION)
                            ?: error("Couldn't read this photo. Please choose another image.")
                    }
                    newFile = PickedFile(
                        data = jpeg,
                        filename = "reupload.jpg",
                        mimeType = "image/jpeg"
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    newFile = null
                    readError = e.message ?: "Couldn't read this photo. Please choose another image."
                } finally {
                    isReadingImage = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Palette.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Palette.accentOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Palette.accentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (hasImages) "Re-upload to AI?" else "Upload Image to AI",
                        style = Typography.santoshi(16.sp, FontWeight.Bold),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = if (hasImages) {
                            "This will clear the current processed results. You can optionally replace the base image below."
                        } else {
                            "Select a base photo below to start the AI processing pipeline for this product."
                        },
                        style = Typography.santoshi(13.sp),
                        color = Palette.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Text(
                text = "Base Photo Selection",
                style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                color = Palette.textSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.surface)
                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                    .clickable(enabled = !isReadingImage) {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isReadingImage) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Palette.dark,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Reading & normalizing photo...",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                } else if (previewUri != null && newFile != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = previewUri,
                            contentDescription = "New Base Photo",
                            modifier = Modifier
                                .height(80.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Palette.dark)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "New Base Photo",
                                style = Typography.santoshi(10.sp, FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Drag new image here, or browse",
                            style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary
                        )
                        Text(
                            text = if (hasImages) {
                                "JPEG, PNG, or WebP. Leave blank to keep current photo."
                            } else {
                                "JPEG, PNG, or WebP is required."
                            },
                            style = Typography.santoshi(11.sp),
                            color = Palette.textSecondary
                        )
                    }
                }
            }

            if (readError != null) {
                Text(
                    text = readError!!,
                    style = Typography.santoshi(12.sp),
                    color = Palette.accentRed
                )
            }

            Text("How many enhanced images would you like?", style = Typography.santoshi(14.sp, FontWeight.SemiBold))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { count ->
                    OutlinedButton(
                        onClick = { imageCount = count },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (imageCount == count) Palette.dark else Color.Transparent,
                            contentColor = if (imageCount == count) Color.White else Palette.dark
                        )
                    ) { Text("$count") }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", style = Typography.santoshi(14.sp, FontWeight.SemiBold))
                }

                val canProceed = !isReadingImage && (hasImages || newFile != null)
                Button(
                    onClick = {
                        onConfirm(newFile, imageCount)
                        onDismiss()
                    },
                    enabled = canProceed,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.dark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isReadingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (hasImages && newFile == null) "Re-upload" else "Upload & Process",
                            style = Typography.santoshi(14.sp, FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
