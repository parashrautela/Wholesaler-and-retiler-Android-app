// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/ProductDetailView.swift + AddProductView.swift
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.launch
import java.util.UUID

private val JEWELLERY_TYPES = listOf(
    "Ring", "Necklace", "Earring", "Bracelet", "Bangle", "Pendant", "Chain",
    "Mangalsutra", "Nosering", "Haram", "Anklet", "Brooch"
)

private val PURITY_OPTIONS = listOf("18K", "22K", "24K", "Sterling Silver", "Other")

@Composable
fun ProductDetailSheet(
    product: Product,
    wholesalerId: String,
    onDismiss: () -> Unit,
    onPublishToggle: (Product) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isPublished by remember { mutableStateOf(product.isPublished == true) }
    var inStock by remember { mutableStateOf(product.stockAvailable != false) }
    var working by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = product.title ?: "Product Details",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Palette.dark
        )

        // Image carousel
        val images = product.thumbnailUrls()
        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(images) { url ->
                    com.jewelindia.app.core.security.ProtectedImage(
                        url = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }

        // Metadata
        InfoRow("Type", product.jewelleryType ?: "—")
        InfoRow("Category", product.category ?: "—")
        InfoRow("Purity", product.metalPurity ?: "—")
        InfoRow("Net weight", product.netWeight?.let { "${it}g" } ?: "—")

        Spacer(Modifier.height(4.dp))

        // Toggles
        ToggleRow(
            label = "In Stock",
            checked = inStock,
            onCheckedChange = { v ->
                inStock = v
                scope.launch {
                    working = true
                    try { WholesalerApi.updateProductFlags(product.id, inStock = v) } catch (_: Exception) {}
                    working = false
                }
            }
        )

        val canPublish = product.canPublish
        ToggleRow(
            label = "Published",
            checked = isPublished,
            enabled = canPublish,
            onCheckedChange = { v ->
                isPublished = v
                scope.launch {
                    working = true
                    try {
                        WholesalerApi.updateProductFlags(product.id, isPublished = v)
                        onPublishToggle(product.copy(isPublished = v))
                    } catch (_: Exception) {}
                    working = false
                }
            }
        )
        if (!canPublish) {
            Text(
                text = "Run through Chamak Studio to enable publishing.",
                fontSize = 12.sp,
                color = Palette.muted
            )
        }

        Spacer(Modifier.height(4.dp))

        // Delete
        Button(
            onClick = {
                scope.launch {
                    working = true
                    try { WholesalerApi.deleteProduct(product.id) } catch (_: Exception) {}
                    onDismiss()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Palette.accentRed,
                contentColor = Palette.light
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (working) CircularProgressIndicator(Modifier.size(18.dp), color = Palette.light, strokeWidth = 2.dp)
            else {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete Product", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Palette.muted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Palette.dark)
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = if (enabled) Palette.dark else Palette.muted)
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Palette.light, checkedTrackColor = Palette.goldPrimary)
        )
    }
}

// ─── Add Product Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    wholesalerId: String,
    userId: String,
    onDismiss: () -> Unit,
    onAdded: (Product) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var jewelleryType by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }
    var metalPurity by remember { mutableStateOf("22K") }
    var purityExpanded by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedUri = uri }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add New Design", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Palette.dark)

        // Image picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.cream)
                .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null) {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Palette.muted,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Tap to choose photo", fontSize = 14.sp, color = Palette.muted)
                }
            }
        }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Design Title *") },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Palette.goldPrimary,
                unfocusedContainerColor = Palette.cream
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Jewellery type dropdown
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = jewelleryType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedContainerColor = Palette.cream
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                JEWELLERY_TYPES.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            jewelleryType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // Purity dropdown
        ExposedDropdownMenuBox(
            expanded = purityExpanded,
            onExpandedChange = { purityExpanded = it }
        ) {
            OutlinedTextField(
                value = metalPurity,
                onValueChange = {},
                readOnly = true,
                label = { Text("Metal Purity") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(purityExpanded) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedContainerColor = Palette.cream
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = purityExpanded,
                onDismissRequest = { purityExpanded = false }
            ) {
                PURITY_OPTIONS.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p) },
                        onClick = { metalPurity = p; purityExpanded = false }
                    )
                }
            }
        }

        error?.let { msg ->
            Text(msg, fontSize = 13.sp, color = Palette.accentRed)
        }

        Button(
            onClick = {
                val uri = selectedUri
                if (title.isBlank()) { error = "Please enter a title."; return@Button }
                if (jewelleryType.isBlank()) { error = "Please choose a type."; return@Button }
                if (uri == null) { error = "Please choose a photo."; return@Button }

                scope.launch {
                    submitting = true
                    error = null
                    try {
                        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                            ?: throw Exception("Could not read image")
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val filename = "design_${UUID.randomUUID()}.jpg"
                        val key = UUID.randomUUID().toString()

                        val response = WholesalerApi.processProduct(
                            imageData = bytes,
                            filename = filename,
                            mimeType = mimeType,
                            title = title.trim(),
                            jewelleryType = jewelleryType.lowercase(),
                            wholesalerId = wholesalerId,
                            imageCount = 1,
                            submissionKey = key
                        )

                        // Poll for the product
                        var product: Product? = null
                        repeat(10) {
                            if (product != null) return@repeat
                            kotlinx.coroutines.delay(2000)
                            product = WholesalerApi.fetchProduct(response.productId ?: return@repeat)
                        }
                        product?.let(onAdded) ?: run { error = "Upload queued — it'll appear shortly." }
                    } catch (e: Exception) {
                        error = e.message ?: "Something went wrong."
                    }
                    submitting = false
                }
            },
            enabled = !submitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = Palette.goldPrimary,
                contentColor = Palette.light
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (submitting) CircularProgressIndicator(Modifier.size(18.dp), color = Palette.light, strokeWidth = 2.dp)
            else Text("Upload & Process", fontWeight = FontWeight.SemiBold)
        }
    }
}
