// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/EditProductView.swift
package com.jewelindia.app.feature.wholesaler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    product: Product,
    onDismiss: () -> Unit,
    onSaved: (Product) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf(product.title.orEmpty()) }
    var jewelleryType by remember {
        val raw = product.jewelleryType.orEmpty().trim().lowercase()
        mutableStateOf(if (raw in listOf("chain", "chains", "neck chain", "neck chains")) "chain" else raw)
    }
    var category by remember { mutableStateOf(product.category.orEmpty().trim().lowercase()) }
    var style by remember { mutableStateOf(product.style.orEmpty().trim().lowercase()) }
    var size by remember { mutableStateOf(product.size.orEmpty().trim().lowercase()) }
    var metalPurity by remember { mutableStateOf(product.metalPurity.orEmpty().trim().lowercase()) }
    var grossWeight by remember { mutableStateOf(product.grossWeight?.toString().orEmpty()) }
    var stoneWeight by remember { mutableStateOf(product.stoneWeight?.toString().orEmpty()) }
    var netWeight by remember { mutableStateOf(product.netWeight?.toString().orEmpty()) }
    var stockAvailable by remember { mutableStateOf(product.stockAvailable != false) }
    var makeToOrderDays by remember { mutableStateOf(product.makeToOrderDays?.toString() ?: "14") }
    var isPublished by remember { mutableStateOf(product.isPublished == true) }

    val customImages = remember { mutableStateListOf<String>().apply { addAll(product.customImageURLs) } }
    val initialShowcase = if (product.showcaseImageURLs.isNotEmpty()) {
        product.showcaseImageURLs.filter { it.isNotBlank() }.distinct().take(4)
    } else {
        (product.generatedImageURLs + listOfNotNull(product.processedImageURL, product.imageURL) +
                product.customImageURLs + listOfNotNull(product.rawImageURL)).distinct().take(4)
    }
    val showcaseImages = remember { mutableStateListOf<String>().apply { addAll(initialShowcase) } }

    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && !isUploading && !isSaving) {
            isUploading = true
            errorMessage = null
            scope.launch {
                try {
                    val owner = product.wholesalerId?.takeIf { it.isNotBlank() }
                        ?: error("Couldn't identify the product owner. Reopen the catalogue and try again.")
                    val jpeg = withContext(Dispatchers.IO) {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("Couldn't read this photo. Please choose another image.")
                        ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION)
                            ?: error("Couldn't read this photo. Please choose another image.")
                    }
                    val path = "raw/${owner.lowercase()}/custom-${UUID.randomUUID()}.jpg"
                    val url = WholesalerApi.upload("plant-images", path, jpeg, "image/jpeg", upsert = false)
                    customImages.add(url)
                    if (showcaseImages.size < 4) showcaseImages.add(url)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    errorMessage = "Couldn't upload your image: ${e.message}"
                } finally {
                    isUploading = false
                }
            }
        }
    }

    val availableShowcase = remember(product, customImages.toList()) {
        (product.showcaseImageURLs + product.aiVerifiedOutputURLs + product.generatedImageURLs + listOfNotNull(product.processedImageURL, product.imageURL) +
                customImages + listOfNotNull(product.rawImageURL)).distinct()
    }

    fun toggleShowcase(url: String) {
        if (showcaseImages.contains(url)) {
            showcaseImages.remove(url)
        } else if (showcaseImages.size < 4) {
            showcaseImages.add(url)
        }
    }

    fun saveChanges() {
        if (isSaving || isUploading) return
        isSaving = true
        errorMessage = null

        val finalPublished = if (product.canPublish) isPublished else false
        val edit = WholesalerApi.ProductEdit(
            title = title.trim().ifBlank { null },
            category = category.trim().ifBlank { null },
            jewelleryType = jewelleryType.trim().ifBlank { null },
            style = style.trim().ifBlank { null },
            size = size.trim().ifBlank { null },
            metalPurity = metalPurity.trim().ifBlank { null },
            netWeight = netWeight.toDoubleOrNull(),
            grossWeight = grossWeight.toDoubleOrNull(),
            stoneWeight = stoneWeight.toDoubleOrNull(),
            stockAvailable = stockAvailable,
            makeToOrderDays = makeToOrderDays.toIntOrNull(),
            isPublished = finalPublished,
            customImageUrls = customImages.toList(),
            showcaseImageUrls = showcaseImages.toList()
        )

        scope.launch {
            try {
                WholesalerApi.updateProduct(product.id, edit)
                val updatedProduct = product.copy(
                    title = title.trim(),
                    jewelleryType = jewelleryType,
                    category = category,
                    style = style,
                    size = size,
                    stockAvailable = stockAvailable,
                    makeToOrderDays = makeToOrderDays.toIntOrNull(),
                    metalPurity = metalPurity,
                    netWeight = netWeight.toDoubleOrNull(),
                    grossWeight = grossWeight.toDoubleOrNull(),
                    stoneWeight = stoneWeight.toDoubleOrNull(),
                    customImageURLs = customImages.toList(),
                    showcaseImageURLs = showcaseImages.toList(),
                    isPublished = finalPublished
                )
                onSaved(updatedProduct)
                onDismiss()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to save product changes."
                isSaving = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving && !isUploading) onDismiss() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // iOS Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(enabled = !isSaving && !isUploading) { onDismiss() }
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = Typography.santoshi(16.sp, FontWeight.Medium),
                        color = Color(0xFF111827)
                    )
                }

                Text(
                    text = "Edit Product",
                    style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = !isSaving && !isUploading) { saveChanges() }
                        .padding(vertical = 4.dp)
                ) {
                    if (isSaving || isUploading) {
                        CircularProgressIndicator(
                            color = Palette.dark,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "Save",
                            style = Typography.santoshi(16.sp, FontWeight.Bold),
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (errorMessage != null) {
                    ErrorBanner(message = errorMessage!!)
                }

                // Section: Product Info
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "PRODUCT INFO",
                        style = Typography.santoshi(12.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF6B7280)
                    )

                    LabelledField(label = "Product Title") {
                        JewelTextInput(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Product Title"
                        )
                    }

                    JewelSelect(
                        label = "Jewellery Type",
                        options = WholesalerProductOptions.types,
                        selection = jewelleryType,
                        onSelectionChange = { jewelleryType = it }
                    )

                    JewelSelect(
                        label = "Material",
                        options = WholesalerProductOptions.categories,
                        selection = category,
                        onSelectionChange = { category = it }
                    )

                    JewelSelect(
                        label = "Style",
                        options = WholesalerProductOptions.styles,
                        selection = style,
                        onSelectionChange = { style = it }
                    )

                    JewelSelect(
                        label = "Size",
                        options = WholesalerProductOptions.sizes,
                        selection = size,
                        onSelectionChange = { size = it }
                    )

                    JewelSelect(
                        label = "Purity",
                        options = WholesalerProductOptions.purities,
                        selection = metalPurity,
                        onSelectionChange = { metalPurity = it }
                    )
                }

                // Section: Specifications
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SPECIFICATIONS",
                        style = Typography.santoshi(12.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF6B7280)
                    )

                    InputWithSuffix(
                        label = "Gross Weight",
                        suffix = "g",
                        text = grossWeight,
                        onTextChange = { grossWeight = it }
                    )

                    InputWithSuffix(
                        label = "Stone Weight",
                        suffix = "g",
                        text = stoneWeight,
                        onTextChange = { stoneWeight = it }
                    )

                    InputWithSuffix(
                        label = "Net Weight",
                        suffix = "g",
                        text = netWeight,
                        onTextChange = { netWeight = it }
                    )
                }

                if (showcaseImages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("IMAGE ORDER · FIRST IMAGE IS THE COVER", style = Typography.santoshi(12.sp, FontWeight.Bold))
                        showcaseImages.toList().forEachIndexed { index, url ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ProtectedImage(
                                    url = url,
                                    contentDescription = "Image ${index + 1}",
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(if (index == 0) "1 · Cover" else "${index + 1}", modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { java.util.Collections.swap(showcaseImages, index, index - 1) },
                                    enabled = index > 0 && !isSaving && !isUploading
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move image ${index + 1} earlier")
                                }
                                IconButton(
                                    onClick = { java.util.Collections.swap(showcaseImages, index, index + 1) },
                                    enabled = index < showcaseImages.lastIndex && !isSaving && !isUploading
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move image ${index + 1} later")
                                }
                            }
                        }
                        Text("Use the arrows to reorder. Retailers see the cover image first after you save.",
                            style = Typography.santoshi(11.sp), color = Color(0xFF6B7280))
                    }
                }

                // Section: Showcase Images
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "SHOWCASE IMAGES (CHOOSE UP TO 4)",
                        style = Typography.santoshi(12.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF6B7280)
                    )

                    availableShowcase.forEachIndexed { index, url ->
                        val isSelected = showcaseImages.contains(url)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(8.dp))
                                .clickable { toggleShowcase(url) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProtectedImage(
                                url = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            val label = when {
                                customImages.contains(url) -> "My image ${customImages.indexOf(url) + 1}"
                                product.generatedImageURLs.contains(url) -> "Generated image ${product.generatedImageURLs.indexOf(url) + 1}"
                                url == product.rawImageURL -> "Original image"
                                else -> "Product image ${index + 1}"
                            }
                            Text(
                                text = label,
                                style = Typography.santoshi(13.sp),
                                color = Color(0xFF111827),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Palette.dark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9FAFB))
                            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(8.dp))
                            .clickable(enabled = !isUploading && !isSaving) { imagePicker.launch("image/*") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = if (isUploading || isSaving) Color(0xFF9CA3AF) else Color(0xFF111827),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Add my own images",
                            style = Typography.santoshi(14.sp, FontWeight.Medium),
                            color = if (isUploading || isSaving) Color(0xFF9CA3AF) else Color(0xFF111827)
                        )
                    }

                    Text(
                        text = if (isUploading) "Uploading your image…" else if (showcaseImages.size >= 4) {
                            "4 images selected. Deselect one to feature a different image. Your uploads are kept in this list."
                        } else "Tap images to select them. The selected images are shown first in the catalogue, in selection order.",
                        style = Typography.santoshi(11.sp),
                        color = Color(0xFF6B7280)
                    )
                }

                // Section: Availability
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "AVAILABILITY",
                        style = Typography.santoshi(12.sp, FontWeight.Bold),
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF6B7280)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "In Stock",
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Color(0xFF111827)
                        )
                        JewelToggle(
                            isOn = stockAvailable,
                            onToggle = { stockAvailable = it }
                        )
                    }

                    if (!stockAvailable) {
                        InputWithSuffix(
                            label = "Production Time",
                            suffix = "days",
                            placeholder = "14",
                            text = makeToOrderDays,
                            onTextChange = { makeToOrderDays = it },
                            keyboardType = KeyboardType.Number
                        )
                    }

                    if (product.canPublish) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Published to Catalogue",
                                style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                color = Color(0xFF111827)
                            )
                            JewelToggle(
                                isOn = isPublished,
                                onToggle = { isPublished = it }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Published to Catalogue",
                                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = "Unlocks once AI generation finishes",
                                    style = Typography.santoshi(11.sp),
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
