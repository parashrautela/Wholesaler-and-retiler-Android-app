// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakHubView.swift + ChamakFlowCoordinator.swift
package com.jewelindia.app.feature.wholesaler.chamak

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.api.WholesalerFormInput
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.treasurechest.InsufficientCreditsSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ChamakScreenState {
    Hub,
    Picker,
    Sliders,
    SetStyling,
    Generating,
    Result,
    Gallery
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamakStudioScreen(
    wholesaler: Wholesaler?,
    sessionStore: SessionStore,
    catalogueSource: ChamakCatalogueSource = ChamakCatalogueSource.OwnProducts,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var screenState by remember { mutableStateOf(ChamakScreenState.Hub) }
    var selectedMode by remember { mutableStateOf(ChamakMode.Fusion) }
    var slot1 by remember { mutableStateOf<ChamakDesignItem?>(null) }
    var slot2 by remember { mutableStateOf<ChamakDesignItem?>(null) }
    var activeSlotIndex by remember { mutableStateOf(1) }

    var selectedBackdrop by remember { mutableStateOf(SetBackdrop.VelvetBust) }
    var sliderWeights by remember { mutableStateOf(mapOf("aesthetic" to 0.5, "gemstone" to 0.5, "craft" to 0.5)) }

    var activeGeneration by remember { mutableStateOf<ChamakGeneration?>(null) }
    var generationStatusText by remember { mutableStateOf("Initializing...") }
    var generationError by remember { mutableStateOf<String?>(null) }

    var galleryItems by remember { mutableStateOf<List<ChamakGeneration>>(emptyList()) }
    var loadingGallery by remember { mutableStateOf(false) }

    var showInsufficientCredits by remember { mutableStateOf(false) }
    val insufficientSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFeedbackSheet by remember { mutableStateOf(false) }
    val feedbackSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Load gallery on Hub
    LaunchedEffect(wholesaler?.id) {
        val wid = wholesaler?.id ?: return@LaunchedEffect
        loadingGallery = true
        try {
            galleryItems = ChamakApi.fetchWholesalerGallery(wid)
        } catch (_: Exception) {}
        loadingGallery = false
    }

    // Photo picker for custom photo upload in Chamak
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val normalized = ImageNormalizer.jpeg(
                        data = bytes,
                        maxDimension = ImageNormalizer.MAX_PRODUCT_DIMENSION
                    ) ?: bytes
                    val item = ChamakDesignItem(
                        id = "custom_${System.currentTimeMillis()}",
                        title = "Custom Photo",
                        subtitle = "Uploaded photo",
                        imageURL = null,
                        localImageData = normalized
                    )
                    if (activeSlotIndex == 1) slot1 = item else slot2 = item
                }
            }
        }
    }

    fun startGenerationFlow() {
        val wid = wholesaler?.id ?: return
        val s1 = slot1 ?: return
        val s2 = slot2 ?: return

        scope.launch {
            screenState = ChamakScreenState.Generating
            generationError = null
            generationStatusText = "Uploading source designs..."

            try {
                // Upload sources
                val path1 = if (s1.localImageData != null) {
                    ChamakApi.uploadSourceImage(wid, s1.localImageData!!, slot = 1, mode = selectedMode)
                } else {
                    s1.imageURL ?: ""
                }

                val path2 = if (s2.localImageData != null) {
                    ChamakApi.uploadSourceImage(wid, s2.localImageData!!, slot = 2, mode = selectedMode)
                } else {
                    s2.imageURL ?: ""
                }

                generationStatusText = "Creating Chamak run..."
                val gen = ChamakApi.createGeneration(
                    wholesalerId = wid,
                    source1URL = path1,
                    source2URL = path2,
                    mode = selectedMode
                )
                activeGeneration = gen

                // Trigger Stage 1 Vision Analysis
                generationStatusText = "Analyzing design aesthetics..."
                try {
                    ChamakApi.triggerStage1Analysis(gen.id)
                } catch (_: Exception) {}

                // Submit Form and Start Generation
                generationStatusText = if (selectedMode == ChamakMode.SetCreation) "Staging your set with AI..." else "Combining design aesthetics..."
                if (selectedMode == ChamakMode.SetCreation) {
                    ChamakApi.submitSetAndGenerate(gen.id, wid, selectedBackdrop, null)
                } else {
                    val form = WholesalerFormInput(sliderWeights = sliderWeights)
                    ChamakApi.submitFormAndGenerate(gen.id, wid, form, null)
                }

                // Poll status until done or failed
                var attempts = 0
                while (attempts < 60) {
                    delay(3000)
                    attempts++
                    val updated = try { ChamakApi.fetchGeneration(gen.id) } catch (_: Exception) { null }
                    if (updated != null) {
                        activeGeneration = updated
                        if (updated.status == ChamakStatus.Done) {
                            screenState = ChamakScreenState.Result
                            return@launch
                        } else if (updated.status == ChamakStatus.Failed) {
                            generationError = updated.contentFlagHit?.userMessage ?: "Generation failed. Please try again with clearer photos."
                            return@launch
                        }
                    }
                }
                generationError = "Generation took too long. Check back in your gallery later."
            } catch (e: Exception) {
                if (e.message?.contains("INSUFFICIENT_CREDITS", ignoreCase = true) == true) {
                    screenState = ChamakScreenState.Hub
                    showInsufficientCredits = true
                } else {
                    generationError = e.message ?: "Failed to start generation"
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
    ) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "chamak_nav"
        ) { state ->
            when (state) {
                ChamakScreenState.Hub -> {
                    ChamakHub(
                        wholesaler = wholesaler,
                        recentGenerations = galleryItems,
                        loadingGallery = loadingGallery,
                        onStartMode = { mode ->
                            selectedMode = mode
                            slot1 = null
                            slot2 = null
                            screenState = ChamakScreenState.Picker
                        },
                        onViewGallery = { screenState = ChamakScreenState.Gallery },
                        onSelectGeneration = { gen ->
                            activeGeneration = gen
                            screenState = ChamakScreenState.Result
                        },
                        onClose = onClose
                    )
                }

                ChamakScreenState.Picker -> {
                    ChamakPicker(
                        mode = selectedMode,
                        slot1 = slot1,
                        slot2 = slot2,
                        wholesalerId = wholesaler?.id,
                        onBack = { screenState = ChamakScreenState.Hub },
                        onPickCustomPhoto = { slotIdx ->
                            activeSlotIndex = slotIdx
                            photoPickerLauncher.launch("image/*")
                        },
                        onSelectProduct = { slotIdx, product ->
                            val item = ChamakDesignItem.from(product)
                            if (slotIdx == 1) slot1 = item else slot2 = item
                        },
                        onClearSlot = { slotIdx ->
                            if (slotIdx == 1) slot1 = null else slot2 = null
                        },
                        onNext = {
                            if (selectedMode == ChamakMode.SetCreation) {
                                screenState = ChamakScreenState.SetStyling
                            } else {
                                screenState = ChamakScreenState.Sliders
                            }
                        }
                    )
                }

                ChamakScreenState.Sliders -> {
                    ChamakSlidersView(
                        slot1 = slot1,
                        slot2 = slot2,
                        weights = sliderWeights,
                        onWeightsChange = { sliderWeights = it },
                        onBack = { screenState = ChamakScreenState.Picker },
                        onGenerate = { startGenerationFlow() }
                    )
                }

                ChamakScreenState.SetStyling -> {
                    ChamakSetStylingView(
                        slot1 = slot1,
                        slot2 = slot2,
                        selectedBackdrop = selectedBackdrop,
                        onBackdropChange = { selectedBackdrop = it },
                        onBack = { screenState = ChamakScreenState.Picker },
                        onGenerate = { startGenerationFlow() }
                    )
                }

                ChamakScreenState.Generating -> {
                    ChamakGeneratingView(
                        statusText = generationStatusText,
                        errorMessage = generationError,
                        onRetry = { startGenerationFlow() },
                        onCancel = { screenState = ChamakScreenState.Hub }
                    )
                }

                ChamakScreenState.Result -> {
                    ChamakResultView(
                        generation = activeGeneration,
                        onBack = { screenState = ChamakScreenState.Hub },
                        onFeedback = { showFeedbackSheet = true },
                        onInsufficientCredits = { showInsufficientCredits = true }
                    )
                }

                ChamakScreenState.Gallery -> {
                    ChamakGalleryView(
                        items = galleryItems,
                        onBack = { screenState = ChamakScreenState.Hub },
                        onSelect = { gen ->
                            activeGeneration = gen
                            screenState = ChamakScreenState.Result
                        }
                    )
                }
            }
        }

        if (showInsufficientCredits) {
            InsufficientCreditsSheet(
                sheetState = insufficientSheetState,
                onDismiss = { showInsufficientCredits = false },
                onOpenDailyCredits = { showInsufficientCredits = false }
            )
        }

        if (showFeedbackSheet && activeGeneration != null) {
            ChamakFeedbackSheet(
                generationId = activeGeneration!!.id,
                sheetState = feedbackSheetState,
                onDismiss = { showFeedbackSheet = false }
            )
        }
    }
}

@Composable
private fun ChamakHub(
    wholesaler: Wholesaler?,
    recentGenerations: List<ChamakGeneration>,
    loadingGallery: Boolean,
    onStartMode: (ChamakMode) -> Unit,
    onViewGallery: () -> Unit,
    onSelectGeneration: (ChamakGeneration) -> Unit,
    onClose: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Palette.dark)
                }
            }
            Text(
                text = "Chamak Studio",
                style = Typography.cirka(26.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            IconButton(onClick = onViewGallery) {
                Icon(Icons.Default.Collections, contentDescription = "Gallery", tint = Palette.dark)
            }
        }

        // Mode 1: Combine Designs
        ChamakToolCard(
            title = "Combine Designs",
            description = "Blend two complementary jewellery designs into one new, photorealistic concept with custom sliders.",
            icon = Icons.Default.AutoAwesome,
            badge = "FUSION",
            onClick = { onStartMode(ChamakMode.Fusion) }
        )

        // Mode 2: Set Creation
        ChamakToolCard(
            title = "Set Creation",
            description = "Stage two different real pieces together on luxury velvet busts or festive backgrounds as a matched set.",
            icon = Icons.Default.Style,
            badge = "STAGING",
            onClick = { onStartMode(ChamakMode.SetCreation) }
        )

        // Recent Creations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Creations",
                style = Typography.cirka(18.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            if (recentGenerations.isNotEmpty()) {
                Text(
                    text = "View All",
                    style = Typography.santoshi(13.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.goldPrimary,
                    modifier = Modifier.clickable(onClick = onViewGallery)
                )
            }
        }

        if (loadingGallery) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (recentGenerations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.cream)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Chamak creations yet. Pick a mode above to get started!",
                    style = Typography.santoshi(14.sp),
                    color = Palette.muted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                recentGenerations.take(3).forEach { gen ->
                    val img = gen.outputImageURL
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.cream)
                            .clickable { onSelectGeneration(gen) }
                    ) {
                        if (!img.isNullOrBlank()) {
                            CachedImage(
                                url = img,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(gen.status.name, style = Typography.santoshi(12.sp), color = Palette.muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChamakToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = Palette.goldPrimary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = Typography.cirka(18.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Palette.goldPrimary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = badge, style = Typography.santoshi(10.sp), fontWeight = FontWeight.Bold, color = Palette.light)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = description, style = Typography.santoshi(13.sp), color = Palette.muted)

            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Start Creating", style = Typography.santoshi(13.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Palette.dark, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ChamakPicker(
    mode: ChamakMode,
    slot1: ChamakDesignItem?,
    slot2: ChamakDesignItem?,
    wholesalerId: String?,
    onBack: () -> Unit,
    onPickCustomPhoto: (Int) -> Unit,
    onSelectProduct: (Int, Product) -> Unit,
    onClearSlot: (Int) -> Unit,
    onNext: () -> Unit
) {
    var catalogueProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var activeSlot by remember { mutableStateOf(1) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(wholesalerId) {
        val wid = wholesalerId ?: return@LaunchedEffect
        loading = true
        catalogueProducts = try {
            WholesalerApi.fetchCatalogue(wid, pageSize = 100).products
        } catch (_: Exception) {
            emptyList()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.dark)
            }
            Text(
                text = if (mode == ChamakMode.SetCreation) "Select Set Designs" else "Select 2 Designs",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PickerSlot(
                slotNumber = 1,
                item = slot1,
                isActive = activeSlot == 1,
                onClick = { activeSlot = 1 },
                onClear = { onClearSlot(1) },
                onUpload = { onPickCustomPhoto(1) },
                modifier = Modifier.weight(1f)
            )
            PickerSlot(
                slotNumber = 2,
                item = slot2,
                isActive = activeSlot == 2,
                onClick = { activeSlot = 2 },
                onClear = { onClearSlot(2) },
                onUpload = { onPickCustomPhoto(2) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pick from Catalogue for Slot $activeSlot",
            style = Typography.santoshi(14.sp),
            fontWeight = FontWeight.Bold,
            color = Palette.dark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Palette.goldPrimary)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(catalogueProducts, key = { it.id }) { prod ->
                        val url = prod.displayImageUrl()
                        Box(
                            modifier = Modifier
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Palette.cream)
                                .clickable { onSelectProduct(activeSlot, prod) }
                        ) {
                            if (!url.isNullOrBlank()) {
                                CachedImage(
                                    url = url,
                                    contentDescription = prod.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = slot1 != null && slot2 != null,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
        ) {
            Text(
                text = "Continue",
                style = Typography.santoshi(15.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.light
            )
        }
    }
}

@Composable
fun PickerSlot(
    slotNumber: Int,
    item: ChamakDesignItem?,
    isActive: Boolean,
    onClick: () -> Unit,
    onClear: () -> Unit,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Palette.goldPrimary else Palette.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            val url = item.imageURL
            if (!url.isNullOrBlank()) {
                CachedImage(
                    url = url,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(item.title, style = Typography.santoshi(12.sp), textAlign = TextAlign.Center)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Slot $slotNumber", style = Typography.santoshi(12.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onUpload) {
                    Text("+ Custom Photo", style = Typography.santoshi(11.sp), color = Palette.goldPrimary)
                }
            }
        }
    }
}

@Composable
private fun ChamakSlidersView(
    slot1: ChamakDesignItem?,
    slot2: ChamakDesignItem?,
    weights: Map<String, Double>,
    onWeightsChange: (Map<String, Double>) -> Unit,
    onBack: () -> Unit,
    onGenerate: () -> Unit
) {
    var aestheticWeight by remember { mutableDoubleStateOf(weights["aesthetic"] ?: 0.5) }
    var gemstoneWeight by remember { mutableDoubleStateOf(weights["gemstone"] ?: 0.5) }
    var craftWeight by remember { mutableDoubleStateOf(weights["craft"] ?: 0.5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.dark)
            }
            Text(
                text = "Blend Attributes",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Slide towards Design 1 or Design 2 to emphasize specific design characteristics in the blended output.",
            style = Typography.santoshi(13.sp),
            color = Palette.muted
        )

        // Attribute sliders
        AttributeSlider(
            label = "Overall Silhouette & Form",
            value = aestheticWeight,
            onValueChange = {
                aestheticWeight = it
                onWeightsChange(mapOf("aesthetic" to it, "gemstone" to gemstoneWeight, "craft" to craftWeight))
            }
        )

        AttributeSlider(
            label = "Gemstone & Setting Accents",
            value = gemstoneWeight,
            onValueChange = {
                gemstoneWeight = it
                onWeightsChange(mapOf("aesthetic" to aestheticWeight, "gemstone" to it, "craft" to craftWeight))
            }
        )

        AttributeSlider(
            label = "Metalwork & Textures",
            value = craftWeight,
            onValueChange = {
                craftWeight = it
                onWeightsChange(mapOf("aesthetic" to aestheticWeight, "gemstone" to gemstoneWeight, "craft" to it))
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
        ) {
            Text("Generate Blend (Chamak AI)", fontWeight = FontWeight.Bold, color = Palette.light)
        }
    }
}

@Composable
fun AttributeSlider(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .padding(14.dp)
    ) {
        Column {
            Text(label, style = Typography.santoshi(13.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toDouble()) },
                colors = SliderDefaults.colors(
                    thumbColor = Palette.goldPrimary,
                    activeTrackColor = Palette.goldPrimary,
                    inactiveTrackColor = Palette.border
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Design 1", style = Typography.santoshi(11.sp), color = Palette.muted)
                Text("Balanced", style = Typography.santoshi(11.sp), color = Palette.muted)
                Text("Design 2", style = Typography.santoshi(11.sp), color = Palette.muted)
            }
        }
    }
}

@Composable
private fun ChamakSetStylingView(
    slot1: ChamakDesignItem?,
    slot2: ChamakDesignItem?,
    selectedBackdrop: SetBackdrop,
    onBackdropChange: (SetBackdrop) -> Unit,
    onBack: () -> Unit,
    onGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.dark)
            }
            Text(
                text = "Set Staging Studio",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Select a luxury backdrop to stage both pieces together in a catalogue-ready showroom composition.",
            style = Typography.santoshi(13.sp),
            color = Palette.muted
        )

        SetBackdrop.entries.forEach { backdrop ->
            val isSelected = backdrop == selectedBackdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Palette.goldPrimary.copy(alpha = 0.1f) else Palette.cream)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Palette.goldPrimary else Palette.border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onBackdropChange(backdrop) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(backdrop.label, style = Typography.cirka(16.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(backdrop.blurb, style = Typography.santoshi(12.sp), color = Palette.muted)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
        ) {
            Text("Stage Matched Set (Chamak AI)", fontWeight = FontWeight.Bold, color = Palette.light)
        }
    }
}

@Composable
private fun ChamakGeneratingView(
    statusText: String,
    errorMessage: String?,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text("Generation Failed", style = Typography.cirka(22.sp), fontWeight = FontWeight.Bold, color = Palette.accentRed)
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, style = Typography.santoshi(14.sp), color = Palette.muted, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)) {
                Text("Try Again")
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onCancel) {
                Text("Return to Hub", color = Palette.dark)
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(56.dp), color = Palette.goldPrimary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Chamak AI is Working", style = Typography.cirka(22.sp), fontWeight = FontWeight.Bold, color = Palette.dark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(statusText, style = Typography.santoshi(14.sp), color = Palette.muted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ChamakResultView(
    generation: ChamakGeneration?,
    onBack: () -> Unit,
    onFeedback: () -> Unit,
    onInsufficientCredits: () -> Unit
) {
    val imgUrl = generation?.outputImageURL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.dark)
            }
            Text(
                text = "Chamak Result",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Palette.cream),
            contentAlignment = Alignment.Center
        ) {
            if (!imgUrl.isNullOrBlank()) {
                CachedImage(
                    url = imgUrl,
                    contentDescription = "Chamak Output",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("Result image unavailable", color = Palette.muted)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onFeedback,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Rate Output", color = Palette.dark)
            }

            if (generation != null) {
                ChamakExportButton(
                    generationId = generation.id,
                    imageUrl = imgUrl,
                    modifier = Modifier.weight(1f).height(48.dp),
                    onInsufficientCredits = onInsufficientCredits
                )
            }
        }
    }
}

@Composable
private fun ChamakGalleryView(
    items: List<ChamakGeneration>,
    onBack: () -> Unit,
    onSelect: (ChamakGeneration) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.dark)
            }
            Text(
                text = "Chamak Gallery",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No creations in gallery yet", color = Palette.muted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { gen ->
                    val url = gen.outputImageURL
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.cream)
                            .clickable { onSelect(gen) }
                    ) {
                        if (!url.isNullOrBlank()) {
                            CachedImage(
                                url = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
