// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakCatalogPickerView.swift
package com.jewelindia.app.feature.wholesaler.chamak

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
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
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.design.Cirka
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakSlot
import com.jewelindia.app.data.api.JewelleryTypeCanonical
import com.jewelindia.app.core.images.ImageNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamakCatalogPickerScreen(
    vm: ChamakViewModel,
    wholesalerId: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val requestAIConsent = rememberAIConsentRequest(wholesalerId)

    var selectedCategory by remember { mutableStateOf("All") }
    var previewImage by remember { mutableStateOf<ChamakViewerImage?>(null) }

    var pendingUploadSlot by remember { mutableIntStateOf(1) }
    var pendingUploadBytes by remember { mutableStateOf<ByteArray?>(null) }
    var showTypePickerSheet by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        ImageNormalizer.jpeg(stream.readBytes(), ImageNormalizer.MAX_PRODUCT_DIMENSION)
                    }
                }
                if (bytes == null || bytes.isEmpty()) {
                    vm.errorMessage = "Couldn't read that photo. Choose another image."
                } else if (vm.mode == ChamakMode.SetCreation) {
                    pendingUploadBytes = bytes
                    showTypePickerSheet = true
                } else {
                    vm.setCustomImage(bytes, pendingUploadSlot)
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { vm.errorMessage = "Couldn't open that photo. Please choose it again." }
        }
    }

    val categories = remember(vm.catalogProducts) {
        val raw = vm.catalogProducts.mapNotNull { it.jewelleryType ?: it.category }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
            .sorted()
        listOf("All") + raw
    }

    val visibleProducts = remember(selectedCategory, vm.catalogProducts) {
        if (selectedCategory == "All") vm.catalogProducts
        else vm.catalogProducts.filter {
            (it.jewelleryType ?: it.category)?.equals(selectedCategory, ignoreCase = true) == true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F6))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onClose != null) {
                    IconButton(onClick = onClose, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Palette.dark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = "Chamak Studio",
                    fontFamily = Cirka,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )

                Spacer(modifier = Modifier.weight(1f))

                // Credits pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFDF8EE))
                        .border(1.dp, Color(0xFFF6E8CD), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFB4833E),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Credits",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB4833E)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)

            // Scrollable content via LazyVerticalGrid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Subheader & Archive link
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (vm.mode == ChamakMode.SetCreation) "Stage your set (2–4 pieces)." else "Blend two designs.",
                            fontFamily = Manrope,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { vm.step = ChamakViewModel.Step.Gallery }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = Color(0xFF5E5D5A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Design Archive",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5E5D5A)
                            )
                        }
                    }
                }

                // Error banner
                val err = vm.errorMessage
                if (err != null) {
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = err,
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { vm.errorMessage = null }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, "Dismiss", tint = Color(0xFF92400E), modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }

                vm.catalogErrorMessage?.let { message ->
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Text(message, color = Palette.muted)
                            TextButton(onClick = { vm.runAction { vm.load(wholesalerId) } }) { Text("Retry catalogue") }
                        }
                    }
                }
                item(span = { GridItemSpan(2) }) {
                    val count = vm.setPieces.size
                    Text(
                        text = if (vm.mode == ChamakMode.SetCreation)
                            "$count of 2–4 pieces selected. Choose a different jewellery type for each piece."
                        else "$count of 2 designs selected. Select two different images to continue.",
                        color = Palette.muted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Selection Slots
                if (vm.mode == ChamakMode.SetCreation) {
                    items((1..4).toList(), span = { GridItemSpan(1) }) { slotNum ->
                        SlotCard(
                            slotNumber = slotNum,
                            emptyTitle = "Piece $slotNum",
                            subtitle = if (slotNum <= 2) "Required" else "Optional",
                            designItem = vm.design(slotNum),
                            accentColor = Color(ChamakSlot.colorHex(slotNum)),
                            mode = vm.mode,
                            onAddPhoto = {
                                pendingUploadSlot = slotNum
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = { vm.clearSlot(slotNum) }
                        )
                    }
                } else {
                    item(span = { GridItemSpan(1) }) {
                        SlotCard(
                            slotNumber = 1,
                            emptyTitle = "Core design",
                            subtitle = "Keeps its Identity",
                            designItem = vm.selectedDesign1,
                            accentColor = Color(0xFFCA8A04),
                            mode = vm.mode,
                            onAddPhoto = {
                                pendingUploadSlot = 1
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = { vm.clearSlot(1) }
                        )
                    }
                    item(span = { GridItemSpan(1) }) {
                        SlotCard(
                            slotNumber = 2,
                            emptyTitle = "New Direction",
                            subtitle = "New Expression",
                            designItem = vm.selectedDesign2,
                            accentColor = Color(0xFF3B82F6),
                            mode = vm.mode,
                            onAddPhoto = {
                                pendingUploadSlot = 2
                                photoPickerLauncher.launch("image/*")
                            },
                            onClear = { vm.clearSlot(2) }
                        )
                    }
                }

                // Catalogue section header + filter
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pick from Catalogue",
                                fontFamily = Cirka,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Palette.dark
                            )
                            if (vm.catalogProducts.isNotEmpty()) {
                                Text(
                                    text = "${vm.catalogProducts.size} items",
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    color = Palette.muted
                                )
                            }
                        }

                        // Category horizontal chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                Text(
                                    text = cat,
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Palette.dark,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0xFF111827) else Color.White)
                                        .border(
                                            width = if (isSelected) 0.dp else 1.dp,
                                            color = Color(0xFFE5E7EB),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }

                // Catalogue Product Grid
                if (vm.isLoadingProducts) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Palette.dark)
                        }
                    }
                } else if (visibleProducts.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                                .padding(vertical = 44.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(com.jewelindia.app.R.drawable.ic_chamak_empty_catalog),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No catalogue items found",
                                fontFamily = Manrope,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Text(
                                text = "Upload photos directly using the + buttons in the\nslots above.",
                                fontFamily = Manrope,
                                fontSize = 13.sp,
                                color = Color(0xFF8E8E93),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    items(visibleProducts, key = { it.id }) { product ->
                        val slot = vm.slot(product)
                        val isSelected = slot != null && slot <= vm.slotCount
                        val blockReason = vm.selectionBlockReason(product)
                        val isBlocked = blockReason != null && !isSelected

                        CatalogProductCard(
                            product = product,
                            slot = slot,
                            isSelected = isSelected,
                            isBlocked = isBlocked,
                            blockReason = blockReason,
                            mode = vm.mode,
                            onClick = { vm.selectProduct(product) },
                            onZoom = {
                                val url = product.displaySource
                                if (url != null) {
                                    previewImage = ChamakViewerImage(product.id, product.title ?: "Catalogue Design", url)
                                }
                            }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Bottom Action Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                val isEnabled = vm.canStartAnalysis && !vm.isSubmitting
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            if (isEnabled) {
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF2C2B29), Color(0xFF1C1C1E))
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(Color(0xFF8B8E99), Color(0xFF6B6E79))
                                )
                            }
                        )
                        .clickable(enabled = isEnabled) {
                            requestAIConsent {
                                vm.runAction {
                                    vm.startVisionAnalysis(wholesalerId)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
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
                                text = if (vm.mode == ChamakMode.SetCreation) "Choose Backdrop" else "Start Analyze",
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

        // Preview dialog
        previewImage?.let { img ->
            ChamakImageViewer(
                images = listOf(img),
                startIndex = 0,
                onDismiss = { previewImage = null }
            )
        }

        // Direct upload Jewellery Type Picker Sheet (Set Creation)
        val uploadBytes = pendingUploadBytes
        if (showTypePickerSheet && uploadBytes != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = {
                    showTypePickerSheet = false
                    pendingUploadBytes = null
                },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Piece $pendingUploadSlot Jewellery Type",
                        fontFamily = Cirka,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                    Text(
                        text = "Select the jewellery type for this piece. Each item in your set must be a distinct type.",
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        color = Palette.muted
                    )

                    JewelleryTypeCanonical.entries.forEach { canonical ->
                        val alreadyUsed = vm.setPieces.any { it.canonicalJewelleryType == canonical.key }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (alreadyUsed) Color(0xFFF9FAFB) else Color.White)
                                .clickable(enabled = !alreadyUsed) {
                                    vm.setCustomImage(uploadBytes, pendingUploadSlot, canonical.key)
                                    showTypePickerSheet = false
                                    pendingUploadBytes = null
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = canonical.displayLabel,
                                fontFamily = Manrope,
                                fontSize = 14.sp,
                                fontWeight = if (alreadyUsed) FontWeight.Normal else FontWeight.SemiBold,
                                color = if (alreadyUsed) Palette.muted else Palette.dark
                            )
                            if (alreadyUsed) {
                                Text(
                                    text = "Already in set",
                                    fontFamily = Manrope,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
