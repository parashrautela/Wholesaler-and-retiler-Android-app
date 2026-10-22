// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/CreateManufacturingRequestSheet.swift
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.model.CreateManufacturingRequestParams
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.retailer.CategoryFilterPill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateManufacturingRequestSheet(
    userId: String = "",
    initialCategory: String? = null,
    onDismiss: () -> Unit,
    onRequestCreated: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf(initialCategory?.takeIf { it.isNotBlank() } ?: "Ring") }
    var isRangeWeight by remember { mutableStateOf(true) }
    var minWeightText by remember { mutableStateOf("10.0") }
    var maxWeightText by remember { mutableStateOf("15.0") }
    var singleWeightText by remember { mutableStateOf("12.0") }
    var material by remember { mutableStateOf("Gold") }
    var quantity by remember { mutableIntStateOf(1) }
    var quotationWindowHours by remember { mutableIntStateOf(24) }
    var deliveryDate by remember { mutableStateOf(LocalDate.now().plusDays(14)) }
    var notes by remember { mutableStateOf("") }

    var isReviewing by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submissionIdempotencyKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var uploadedAssetId by remember { mutableStateOf<String?>(null) }
    var lastSubmissionPayload by remember { mutableStateOf<String?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            uploadedAssetId = null
            lastSubmissionPayload = null
            submissionIdempotencyKey = UUID.randomUUID().toString()
        }
    }

    val categories = listOf(
        "Ring", "Necklace", "Earrings", "Bangles", "Bracelet", "Pendant", "Haram", "Chain", "Mangalsutra", "Other"
    )
    val materials = listOf("Gold", "Diamond", "Polki", "Kundan", "Platinum", "Silver")

    fun validateAndProceed() {
        if (selectedImageUri == null) {
            errorMessage = "Please attach a reference photo of the jewellery."
            return
        }
        val minW = if (isRangeWeight) (minWeightText.toDoubleOrNull() ?: 0.0) else (singleWeightText.toDoubleOrNull() ?: 0.0)
        val maxW = if (isRangeWeight) (maxWeightText.toDoubleOrNull() ?: 0.0) else minW

        if (minW <= 0.0 || maxW < minW) {
            errorMessage = "Please enter a valid weight in grams."
            return
        }
        errorMessage = null
        isReviewing = true
    }

    fun submitRequest() {
        val uri = selectedImageUri ?: return
        isSubmitting = true
        errorMessage = null

        scope.launch {
            try {
                // 1. Upload asset if not already uploaded
                val assetId = uploadedAssetId ?: run {
                    val rawBytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    } ?: throw RuntimeException("Could not process reference photo.")

                    val jpegBytes = ImageNormalizer.jpeg(rawBytes, ImageNormalizer.MAX_PRODUCT_DIMENSION) ?: rawBytes
                    val uploadRes = ManufacturingApi.uploadAsset(jpegBytes)
                    if (!uploadRes.ok) {
                        throw RuntimeException("The reference photo could not be saved. Please retry.")
                    }
                    uploadedAssetId = uploadRes.assetId
                    uploadRes.assetId
                }

                val minW = if (isRangeWeight) (minWeightText.toDoubleOrNull() ?: 0.0) else (singleWeightText.toDoubleOrNull() ?: 0.0)
                val maxW = if (isRangeWeight) (maxWeightText.toDoubleOrNull() ?: 0.0) else minW
                val dateStr = deliveryDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                val params = CreateManufacturingRequestParams(
                    assetId = assetId,
                    category = category,
                    minWeightGrams = minW,
                    maxWeightGrams = maxW,
                    material = material,
                    purity = null,
                    gemstonePreference = null,
                    quantity = quantity,
                    makingBudgetMode = null,
                    makingBudgetAmount = null,
                    currency = "INR",
                    metalRateSnapshot = null,
                    metalRateBasis = null,
                    deliveryNeededDate = dateStr,
                    notes = notes.ifBlank { null },
                    supersededRequestId = null,
                    quotationWindowHours = quotationWindowHours
                )

                val payloadJson = JewelJson.encodeToString(params)
                if (lastSubmissionPayload != null && lastSubmissionPayload != payloadJson) {
                    submissionIdempotencyKey = UUID.randomUUID().toString()
                }
                lastSubmissionPayload = payloadJson

                val res = ManufacturingApi.createRequest(params, submissionIdempotencyKey)
                if (!res.ok || res.requestId == null) {
                    throw RuntimeException("The server did not confirm your enquiry. Please retry.")
                }

                isSubmitting = false
                onRequestCreated()
                onDismiss()
            } catch (e: Exception) {
                isSubmitting = false
                errorMessage = e.message ?: "Failed to submit enquiry. Please try again."
            }
        }
    }

    if (showDatePickerDialog) {
        ManufacturingDatePickerDialog(
            initialDate = deliveryDate,
            onDateSelected = { deliveryDate = it },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isReviewing) {
                        IconButton(onClick = { isReviewing = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Palette.textPrimary)
                        }
                    }
                    Text(
                        text = if (isReviewing) "Review Enquiry" else "Request Custom Jewellery",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                    }
                }

                if (isReviewing) {
                    ManufacturingReviewContent(
                        selectedImageUri = selectedImageUri,
                        category = category,
                        isRangeWeight = isRangeWeight,
                        minWeightText = minWeightText,
                        maxWeightText = maxWeightText,
                        singleWeightText = singleWeightText,
                        material = material,
                        quantity = quantity,
                        quotationWindowHours = quotationWindowHours,
                        deliveryDate = deliveryDate,
                        notes = notes,
                        errorMessage = errorMessage,
                        isSubmitting = isSubmitting,
                        onSubmit = { submitRequest() }
                    )
                } else {
                    // Form View
                    // Reference Photo
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Reference Photo *",
                            style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary
                        )
                        if (selectedImageUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
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
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            selectedImageUri = null
                                            uploadedAssetId = null
                                            lastSubmissionPayload = null
                                            submissionIdempotencyKey = UUID.randomUUID().toString()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
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
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = Palette.goldPrimary, modifier = Modifier.size(32.dp))
                                    Text("Add reference design photo", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                                }
                            }
                        }
                    }

                    // Category
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Category", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                CategoryFilterPill(label = cat, isSelected = category == cat, onClick = { category = cat })
                            }
                        }
                    }

                    // Expected Weight
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Expected Weight (g)", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Palette.surface)
                                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isRangeWeight) Palette.goldPrimary else Color.Transparent)
                                        .clickable { isRangeWeight = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Range",
                                        style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                                        color = if (isRangeWeight) Palette.dark else Palette.textSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!isRangeWeight) Palette.goldPrimary else Color.Transparent)
                                        .clickable { isRangeWeight = false }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Single",
                                        style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                                        color = if (!isRangeWeight) Palette.dark else Palette.textSecondary
                                    )
                                }
                            }
                        }

                        if (isRangeWeight) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = minWeightText,
                                    onValueChange = { minWeightText = it },
                                    label = { Text("Min (grams)", color = Palette.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Palette.goldPrimary,
                                        unfocusedBorderColor = Palette.surfaceBorder,
                                        focusedTextColor = Palette.textPrimary,
                                        unfocusedTextColor = Palette.textPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = maxWeightText,
                                    onValueChange = { maxWeightText = it },
                                    label = { Text("Max (grams)", color = Palette.textSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Palette.goldPrimary,
                                        unfocusedBorderColor = Palette.surfaceBorder,
                                        focusedTextColor = Palette.textPrimary,
                                        unfocusedTextColor = Palette.textPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = singleWeightText,
                                onValueChange = { singleWeightText = it },
                                label = { Text("Approx weight (grams)", color = Palette.textSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Palette.goldPrimary,
                                    unfocusedBorderColor = Palette.surfaceBorder,
                                    focusedTextColor = Palette.textPrimary,
                                    unfocusedTextColor = Palette.textPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Material
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Material", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(materials) { mat ->
                                CategoryFilterPill(label = mat, isSelected = material == mat, onClick = { material = mat })
                            }
                        }
                    }

                    // Quantity Stepper
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Quantity", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Palette.surface)
                                .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                enabled = quantity > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = Palette.textPrimary)
                            }
                            Text(
                                text = "$quantity pc",
                                style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                                color = Palette.textPrimary
                            )
                            IconButton(
                                onClick = { if (quantity < 500) quantity++ },
                                enabled = quantity < 500,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Palette.textPrimary)
                            }
                        }
                    }

                    // Quotation Window
                    QuotationWindowSelector(
                        selectedHours = quotationWindowHours,
                        onHoursSelected = { quotationWindowHours = it }
                    )

                    // Delivery Needed By
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Delivery Needed By", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Palette.surface)
                                .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { showDatePickerDialog = true }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = deliveryDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                style = Typography.santoshi(14.sp),
                                color = Palette.textPrimary
                            )
                        }
                    }

                    // Design Notes
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Design Notes (Optional)", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Specific size, finish, stone placement, or details…", color = Palette.textTertiary) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Palette.goldPrimary,
                                unfocusedBorderColor = Palette.surfaceBorder,
                                focusedTextColor = Palette.textPrimary,
                                unfocusedTextColor = Palette.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.santoshi(13.sp),
                            color = Palette.accentRed
                        )
                    }

                    Button(
                        onClick = { validateAndProceed() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.goldPrimary,
                            contentColor = Palette.dark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Review", style = Typography.santoshi(16.sp, FontWeight.SemiBold))
                    }
                }
            }

            if (isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.surfaceElevated)
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = Palette.goldPrimary)
                        Text(
                            text = "Broadcasting enquiry…",
                            style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary
                        )
                    }
                }
            }
        }
    }
}
