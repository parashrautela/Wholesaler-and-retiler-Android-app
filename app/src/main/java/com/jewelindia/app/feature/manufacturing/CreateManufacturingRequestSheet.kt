// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/CreateManufacturingRequestView.swift
package com.jewelindia.app.feature.manufacturing

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.retailer.CategoryFilterPill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateManufacturingRequestSheet(
    userId: String,
    onDismiss: () -> Unit,
    onRequestCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf("Necklace") }
    var material by remember { mutableStateOf("Gold") }
    var purity by remember { mutableStateOf("22KT") }
    var minWeightText by remember { mutableStateOf("20") }
    var maxWeightText by remember { mutableStateOf("25") }
    var quantity by remember { mutableIntStateOf(1) }
    var makingBudgetMode by remember { mutableStateOf("per_gram") } // "per_gram" or "total"
    var makingBudgetAmountText by remember { mutableStateOf("450") }
    var deliveryDaysText by remember { mutableStateOf("21") }
    var notes by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
    }

    val categoryOptions = listOf("Necklace", "Choker", "Bangle", "Ring", "Earrings", "Chain", "Bracelet")
    val materialOptions = listOf("Gold", "Diamond", "Polki", "Kundan", "Platinum")
    val purityOptions = listOf("22KT", "18KT", "14KT", "24KT")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Custom Manufacturing Enquiry",
                    style = Typography.cirka(22.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            // Reference Photo
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Reference Design Photo",
                    style = Typography.santoshi(14.sp, FontWeight.Medium),
                    color = Palette.textPrimary
                )

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Reference",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { selectedImageUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Palette.surface)
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = Palette.goldPrimary)
                            Text("Upload Jewellery Sketch or Photo", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                        }
                    }
                }
            }

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Category", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { cat ->
                        CategoryFilterPill(label = cat, isSelected = category == cat, onClick = { category = cat })
                    }
                }
            }

            // Material & Purity
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Material & Purity", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(materialOptions) { mat ->
                            CategoryFilterPill(label = mat, isSelected = material == mat, onClick = { material = mat })
                        }
                    }
                    LazyRow(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(purityOptions) { pur ->
                            CategoryFilterPill(label = pur, isSelected = purity == pur, onClick = { purity = pur })
                        }
                    }
                }
            }

            // Weight Range (Min & Max)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = minWeightText,
                    onValueChange = { minWeightText = it },
                    label = { Text("Min Weight (g) *", color = Palette.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.goldPrimary,
                        unfocusedBorderColor = Palette.surfaceBorder,
                        focusedTextColor = Palette.textPrimary,
                        unfocusedTextColor = Palette.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = maxWeightText,
                    onValueChange = { maxWeightText = it },
                    label = { Text("Max Weight (g) *", color = Palette.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.goldPrimary,
                        unfocusedBorderColor = Palette.surfaceBorder,
                        focusedTextColor = Palette.textPrimary,
                        unfocusedTextColor = Palette.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Making Budget & Delivery Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = makingBudgetAmountText,
                    onValueChange = { makingBudgetAmountText = it },
                    label = { Text("Making Budget (₹/g)", color = Palette.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.goldPrimary,
                        unfocusedBorderColor = Palette.surfaceBorder,
                        focusedTextColor = Palette.textPrimary,
                        unfocusedTextColor = Palette.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = deliveryDaysText,
                    onValueChange = { deliveryDaysText = it },
                    label = { Text("Delivery Needed (Days)", color = Palette.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.goldPrimary,
                        unfocusedBorderColor = Palette.surfaceBorder,
                        focusedTextColor = Palette.textPrimary,
                        unfocusedTextColor = Palette.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Detailed Requirements / Notes", color = Palette.textSecondary) },
                placeholder = { Text("Specify lock mechanism, rhodium finish, or stone details...", color = Palette.textTertiary) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.surfaceBorder,
                    focusedTextColor = Palette.textPrimary,
                    unfocusedTextColor = Palette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            Button(
                onClick = {
                    val minW = minWeightText.toDoubleOrNull()
                    val maxW = maxWeightText.toDoubleOrNull()
                    if (minW == null || maxW == null || minW <= 0 || maxW < minW) {
                        errorMessage = "Please enter a valid weight range in grams."
                        return@Button
                    }

                    val days = deliveryDaysText.toIntOrNull() ?: 21
                    val neededDate = LocalDate.now().plusDays(days.toLong()).toString()

                    isSubmitting = true
                    errorMessage = null

                    scope.launch {
                        try {
                            var uploadedImageUrl: String? = null
                            selectedImageUri?.let { uri ->
                                val bytes = withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                }
                                if (bytes != null) {
                                    val normalized = ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION) ?: bytes
                                    uploadedImageUrl = ManufacturingApi.uploadManufacturingAsset(normalized, userId)
                                }
                            }

                            val params = ManufacturingApi.CreateRequestParams(
                                category = category,
                                material = material,
                                purity = purity,
                                minWeightGrams = minW,
                                maxWeightGrams = maxW,
                                quantity = quantity,
                                makingBudgetMode = makingBudgetMode,
                                makingBudgetAmount = makingBudgetAmountText.toDoubleOrNull(),
                                deliveryNeededDate = neededDate,
                                notes = notes.ifBlank { null },
                                imageUrl = uploadedImageUrl
                            )

                            ManufacturingApi.createRetailerRequest(params)
                            onRequestCreated()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to submit enquiry. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                } else {
                    Text("Broadcast Enquiry to Wholesalers", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
