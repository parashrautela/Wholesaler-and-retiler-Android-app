// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakResultView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ContentFlag
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamakResultScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showImageViewer by remember { mutableStateOf(false) }
    var viewerStartIndex by remember { mutableIntStateOf(0) }

    val isSet = vm.mode == ChamakMode.SetCreation
    val gen = vm.currentGeneration
    val isFailed = vm.step == ChamakViewModel.Step.Failed

    // Collect viewer images
    val viewerImages = remember(gen, vm.signedFullOutputImageURL, vm.signedOutputImageURL, vm.signedOutputImageURLs) {
        buildList {
            val outputUrl = vm.signedFullOutputImageURL ?: vm.signedOutputImageURL
            if (outputUrl != null) add(ChamakViewerImage("result", if (isSet) "Your Set" else "Final Design", outputUrl, isResult = true))
            vm.signedOutputImageURLs.drop(1).forEachIndexed { index, url ->
                add(ChamakViewerImage("output-${index + 2}", "Output ${index + 2}", url, isResult = true))
            }
            listOfNotNull(
                (gen?.sourceImage1URL ?: vm.selectedDesign1?.imageURL)?.let { ChamakViewerImage("source1", if (isSet) "Piece 1" else "Core design", it) },
                (gen?.sourceImage2URL ?: vm.selectedDesign2?.imageURL)?.let { ChamakViewerImage("source2", if (isSet) "Piece 2" else "New direction", it) },
                (gen?.sourceImage3URL ?: vm.selectedDesign3?.imageURL)?.let { ChamakViewerImage("source3", "Piece 3", it) },
                (gen?.sourceImage4URL ?: vm.selectedDesign4?.imageURL)?.let { ChamakViewerImage("source4", "Piece 4", it) }
            ).let { addAll(it) }
        }
    }

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
                    text = "Final Design",
                    fontFamily = Cirka,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDF8EE))
                        .border(1.dp, Color(0xFFF6E8CD), RoundedCornerShape(16.dp))
                        .clickable { vm.isShowingFeedbackSheet = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color(0xFFB4833E),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (vm.feedbackSubmitted) "Feedback Sent" else "Feedback",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB4833E)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isFailed) {
                    // Failure Card
                    val isNotJewelry = gen?.contentFlagHit == ContentFlag.NotJewelry ||
                        gen?.stage1AnalysisJSON?.contentFlag == ContentFlag.NotJewelry

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(20.dp))
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (isNotJewelry) "Jewellery Not Detected" else "Generation Incomplete",
                            fontFamily = Cirka,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = vm.errorMessage
                                ?: "The AI model encountered an unexpected issue while synthesizing your piece.",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            color = Palette.muted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = { vm.isShowingFeedbackSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Report What Went Wrong", fontFamily = Manrope, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Section Header: New Direction / Your Matched Set
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSet) "Your Matched Set" else "New Direction",
                            fontFamily = Manrope,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        Text(
                            text = "•••",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.muted
                        )
                    }

                    vm.retrievalMessage?.let { message ->
                        Text(message, color = Palette.muted)
                        TextButton(onClick = { vm.runAction { gen?.let { vm.signOutputs(it) } } }) {
                            Text("Retry loading output")
                        }
                    }

                    if (vm.signedOutputImageURLs.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(vm.signedOutputImageURLs.size) { index ->
                                ProtectedImage(
                                    url = vm.signedOutputImageURLs[index],
                                    contentDescription = "Open output ${index + 1}",
                                    modifier = Modifier.size(72.dp).clickable {
                                        viewerStartIndex = index
                                        showImageViewer = true
                                    }
                                )
                            }
                        }
                    }

                    // Main Result Card
                    val signedUrl = vm.signedFullOutputImageURL ?: vm.signedOutputImageURL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(22.dp))
                            .padding(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.05f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF1E1E22))
                                .clickable {
                                    viewerStartIndex = 0
                                    showImageViewer = true
                                }
                        ) {
                            if (signedUrl != null) {
                                ProtectedImage(
                                    url = signedUrl,
                                    fallbackUrl = vm.signedOriginalOutputImageURL,
                                    contentDescription = "Chamak Output",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        viewerStartIndex = 0
                                        showImageViewer = true
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(48.dp)
                                        .background(Color.White.copy(alpha = 0.95f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Expand",
                                        tint = Palette.dark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (vm.retrievalMessage == null) CircularProgressIndicator(
                                        color = Color(0xFFCA8A04),
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = if (vm.retrievalMessage == null) "Loading private image..." else "Image unavailable. Use Retry loading output.",
                                        fontFamily = Manrope,
                                        fontSize = 12.sp,
                                        color = Palette.muted
                                    )
                                }
                            }
                        }
                    }

                    // History attempts row if multiple attempts exist
                    val history = gen?.outputHistory ?: emptyList()
                    if (!isSet && history.size > 1) {
                        val showing = vm.selectedAttempt ?: history.lastOrNull()?.attempt
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "All attempts",
                                fontFamily = Manrope,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Palette.muted
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                history.forEach { attempt ->
                                    val isShowing = attempt.attempt == showing
                                    val thumb = vm.attemptThumbnailURLs[attempt.attempt]
                                    val isLatest = attempt.attempt == history.lastOrNull()?.attempt
                                    val label = if (isLatest) "Latest" else "#${attempt.attempt}"
                                    val topLabel = when (attempt.attempt) {
                                        1 -> "Earrings"
                                        3 -> "Pendants"
                                        else -> null
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            scope.launch {
                                                vm.selectAttempt(attempt)
                                            }
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFF1E1E22))
                                                .border(
                                                    width = if (isShowing) 1.5.dp else 0.5.dp,
                                                    color = if (isShowing) Palette.dark else Color(0xFFE7E5E4),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                        ) {
                                            if (thumb != null) {
                                                ProtectedImage(
                                                    url = thumb,
                                                    contentDescription = "Attempt ${attempt.attempt}",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            if (topLabel != null) {
                                                Text(
                                                    text = topLabel,
                                                    fontFamily = Manrope,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier
                                                        .align(Alignment.TopCenter)
                                                        .padding(top = 4.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            fontFamily = Manrope,
                                            fontSize = 12.sp,
                                            fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isLatest) Palette.dark else Palette.muted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Source Designs Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (isSet) "Pieces in this Set" else "Core Design",
                            fontFamily = Manrope,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val s1 = gen?.sourceImage1URL ?: vm.selectedDesign1?.imageURL
                            val s2 = gen?.sourceImage2URL ?: vm.selectedDesign2?.imageURL

                            if (!s1.isNullOrEmpty()) {
                                SourcePieceTile(
                                    url = s1,
                                    title = "Necklace",
                                    onExpand = {
                                        viewerStartIndex = 1
                                        showImageViewer = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (!s2.isNullOrEmpty()) {
                                SourcePieceTile(
                                    url = s2,
                                    title = null,
                                    onExpand = {
                                        viewerStartIndex = 2
                                        showImageViewer = true
                                    },
                                    modifier = Modifier.weight(1f)
                                )
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isFailed) {
                        val exportUrl = vm.signedOutputImageURL
                        val genId = gen?.id
                        if (exportUrl != null && genId != null) {
                            ChamakExportButton(
                                generationId = genId,
                                imageUrl = exportUrl,
                                modifier = Modifier.fillMaxWidth(),
                                onInsufficientCredits = {
                                    vm.showInsufficientCreditsSheet = true
                                }
                            )
                        }

                        Button(
                            onClick = { vm.reviseAndRetry() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Palette.dark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp), tint = Palette.dark)
                                Text(
                                    text = "Try Again",
                                    fontFamily = Manrope,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Palette.dark
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { vm.reviseAndRetry() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(27.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Palette.dark)
                        ) {
                            Text(
                                text = "Adjust & Try Again",
                                fontFamily = Manrope,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen image viewer
        if (showImageViewer) {
            ChamakImageViewer(
                images = viewerImages,
                startIndex = viewerStartIndex,
                onDismiss = { showImageViewer = false }
            )
        }

        // Feedback Sheet
        if (vm.isShowingFeedbackSheet && gen != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ChamakFeedbackSheet(
                generationId = gen.id,
                sheetState = sheetState,
                onDismiss = { vm.isShowingFeedbackSheet = false }
            )
        }
    }
}

@Composable
private fun SourcePieceTile(
    url: String,
    title: String?,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1E1E22))
            .clickable(onClick = onExpand)
    ) {
        ProtectedImage(
            url = url,
            contentDescription = title ?: "Piece",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onExpand,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp)
                .background(Color.White.copy(alpha = 0.95f), CircleShape)
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
