// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/EditProductView.swift
package com.jewelindia.app.feature.wholesaler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class FormOption(val value: String, val label: String)

object WholesalerProductOptions {
    val types = listOf(
        FormOption("necklace", "Necklace"),
        FormOption("chain", "Chains"),
        FormOption("rings", "Rings"),
        FormOption("earrings", "Earrings"),
        FormOption("haram", "Haram"),
        FormOption("pendant", "Pendant"),
        FormOption("bangles", "Bangles"),
        FormOption("nosepins", "Nosepins"),
        FormOption("mangalsutra", "Mangalsutra")
    )

    val categories = listOf(
        FormOption("gold", "Gold"),
        FormOption("silver", "Silver"),
        FormOption("diamond", "Diamond"),
        FormOption("platinum", "Platinum"),
        FormOption("gemstone", "Gemstone"),
        FormOption("pearl", "Pearl")
    )

    val styles = listOf(
        FormOption("traditional", "Traditional"),
        FormOption("modern", "Modern"),
        FormOption("fusion", "Fusion"),
        FormOption("antique", "Antique"),
        FormOption("minimalist", "Minimalist"),
        FormOption("bridal", "Bridal")
    )

    val sizes = listOf(
        FormOption("xs", "XS"),
        FormOption("s", "S"),
        FormOption("m", "M"),
        FormOption("l", "L"),
        FormOption("xl", "XL"),
        FormOption("freesize", "Free Size")
    )

    val purities = listOf(
        FormOption("24k", "24K (999)"),
        FormOption("22k", "22K (916)"),
        FormOption("18k", "18K (750)"),
        FormOption("14k", "14K (585)"),
        FormOption("925", "925 Silver"),
        FormOption("950pt", "950 Platinum")
    )
}

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
        product.showcaseImageURLs
    } else {
        (product.generatedImageURLs + listOfNotNull(product.processedImageURL, product.imageURL) +
                product.customImageURLs + listOfNotNull(product.rawImageURL)).distinct().take(4)
    }
    val showcaseImages = remember { mutableStateListOf<String>().apply { addAll(initialShowcase) } }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val owner = product.wholesalerId ?: return@rememberLauncherForActivityResult
            scope.launch {
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    if (bytes != null) {
                        val jpeg = ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION) ?: bytes
                        val path = "$owner/products/custom-${UUID.randomUUID()}.jpg"
                        val url = WholesalerApi.upload("plant-images", path, jpeg, "image/jpeg")
                        customImages.add(url)
                        if (showcaseImages.size < 4) showcaseImages.add(url)
                    }
                } catch (e: Exception) {
                    errorMessage = "Couldn't upload your image: ${e.message}"
                }
            }
        }
    }

    val availableShowcase = remember(product, customImages.toList()) {
        (product.generatedImageURLs + listOfNotNull(product.processedImageURL, product.imageURL) +
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
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Palette.surfaceElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Edit Product",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            // Product Info Section
            Text(text = "Product Info", style = Typography.santoshi(14.sp, FontWeight.Bold), color = Palette.textPrimary)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.surfaceBorder
                )
            )

            FormDropdownPicker("Jewellery Type", jewelleryType, WholesalerProductOptions.types) { jewelleryType = it }
            FormDropdownPicker("Material", category, WholesalerProductOptions.categories) { category = it }
            FormDropdownPicker("Style", style, WholesalerProductOptions.styles) { style = it }
            FormDropdownPicker("Size", size, WholesalerProductOptions.sizes) { size = it }
            FormDropdownPicker("Purity", metalPurity, WholesalerProductOptions.purities) { metalPurity = it }

            // Specifications Section
            Text(text = "Specifications", style = Typography.santoshi(14.sp, FontWeight.Bold), color = Palette.textPrimary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = grossWeight,
                    onValueChange = { grossWeight = it },
                    label = { Text("Gross Wt (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = stoneWeight,
                    onValueChange = { stoneWeight = it },
                    label = { Text("Stone Wt (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = netWeight,
                    onValueChange = { netWeight = it },
                    label = { Text("Net Wt (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Showcase Images Section
            Text(
                text = "Showcase images (choose up to 4)",
                style = Typography.santoshi(14.sp, FontWeight.Bold),
                color = Palette.textPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableShowcase.forEachIndexed { index, url ->
                    val isSelected = showcaseImages.contains(url)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Palette.surface)
                            .border(
                                1.dp,
                                if (isSelected) Palette.goldPrimary else Palette.surfaceBorder,
                                RoundedCornerShape(10.dp)
                            )
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
                        Text(
                            text = if (customImages.contains(url)) "My image ${customImages.indexOf(url) + 1}"
                            else if (product.generatedImageURLs.contains(url)) "Generated image ${product.generatedImageURLs.indexOf(url) + 1}"
                            else if (url == product.rawImageURL) "Original image"
                            else "Product image ${index + 1}",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Palette.goldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Palette.dark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                if (showcaseImages.size < 4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Palette.surface)
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = Palette.goldPrimary)
                            Text("Add my own images", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        }
                    }
                }

                Text(
                    text = "Tap images to select them. The selected images are shown first in the catalogue, in selection order.",
                    style = Typography.santoshi(11.sp),
                    color = Palette.textSecondary
                )
            }

            // Availability Section
            Text(text = "Availability", style = Typography.santoshi(14.sp, FontWeight.Bold), color = Palette.textPrimary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "In Stock", style = Typography.santoshi(14.sp), color = Palette.textPrimary)
                Switch(
                    checked = stockAvailable,
                    onCheckedChange = { stockAvailable = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Palette.goldPrimary)
                )
            }

            if (!stockAvailable) {
                OutlinedTextField(
                    value = makeToOrderDays,
                    onValueChange = { makeToOrderDays = it },
                    label = { Text("Production Time (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Published to Catalogue", style = Typography.santoshi(14.sp), color = Palette.textPrimary)
                    if (!product.canPublish) {
                        Text(
                            text = "Unlocks once AI generation finishes",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                }
                if (product.canPublish) {
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Palette.goldPrimary)
                    )
                } else {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Palette.textSecondary)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { saveChanges() },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.goldPrimary,
                    contentColor = Palette.dark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Palette.dark)
                } else {
                    Text("Save", style = Typography.santoshi(16.sp, FontWeight.Bold))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdownPicker(
    label: String,
    selectedValue: String,
    options: List<FormOption>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayLabel = options.firstOrNull { it.value.equals(selectedValue, ignoreCase = true) }?.label
        ?: selectedValue.ifBlank { "Select $label" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = displayLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Palette.goldPrimary,
                unfocusedBorderColor = Palette.surfaceBorder
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Select $label") },
                onClick = {
                    onSelect("")
                    expanded = false
                }
            )
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label) },
                    onClick = {
                        onSelect(opt.value)
                        expanded = false
                    }
                )
            }
        }
    }
}
