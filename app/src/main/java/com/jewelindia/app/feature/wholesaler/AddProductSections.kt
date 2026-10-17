// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/AddProductView.swift (Sub-sections)
package com.jewelindia.app.feature.wholesaler

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.design.Typography

@Composable
fun AddProductProgressBanner(
    usage: UploadUsage,
    isLimitReached: Boolean
) {
    val limit = usage.limit ?: return
    if (limit <= 0) return

    if (isLimitReached) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFDF5))
                .border(1.dp, Color(0xFFFBEFBE), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⚠️", fontSize = 18.sp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Daily upload limit reached",
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Color(0xFF856404)
                )
                Text(
                    text = "You have used all $limit uploads for today.",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF856404)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFBEFBE).copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${usage.used} / $limit Used",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFAFAFA))
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Daily Uploads",
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE5E7EB))
                ) {
                    val frac = (usage.used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(frac)
                            .height(6.dp)
                            .background(Color.Black)
                    )
                }
            }
            Text(
                text = "${usage.used} / $limit",
                style = Typography.santoshi(14.sp, FontWeight.Bold),
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
fun AddProductPhotoSection(
    form: AddProductFormState,
    onPickImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            number = 1,
            title = "Product image",
            subtitle = "Upload a clear image. We'll remove the background first, then enhance it."
        )

        Column(
            modifier = Modifier.padding(start = 2.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Get the best result from your photo:",
                style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                color = Color(0xFF111827)
            )
            listOf(
                "Place the jewellery on a background that contrasts with the product.",
                "Upload a clear well-lit photo.",
                "Keep only the product in the frame"
            ).forEachIndexed { idx, tip ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${idx + 1}.",
                        style = Typography.santoshi(12.sp),
                        color = Color(0xFF6B7280)
                    )
                    Text(
                        text = tip,
                        style = Typography.santoshi(12.sp),
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        // Image dropzone
        val imageBytes = form.imageBytes
        if (imageBytes != null) {
            val bitmap = remember(imageBytes) {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected product photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable {
                            form.selectedUri = null
                            form.imageBytes = null
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F5FF))
                    .clickable { onPickImage() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = Color(0xFF3B82F6),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        ),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload Photo",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddProductDetailsSection(form: AddProductFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            number = 2,
            title = "Essential details",
            subtitle = "Add the key information that helps retailers understand and find this piece."
        )

        LabelledField(label = "Product Title", error = form.errors["title"]) {
            JewelTextInput(
                value = form.title,
                onValueChange = { form.title = it },
                placeholder = "eg. Vintage gold Necklace"
            )
        }

        FieldPair(
            leading = {
                JewelSelect(
                    label = "Type",
                    options = WholesalerProductOptions.types,
                    selection = form.jewelleryType,
                    onSelectionChange = { form.jewelleryType = it },
                    error = form.errors["jewellery_type"]
                )
            },
            trailing = {
                JewelSelect(
                    label = "Material",
                    options = WholesalerProductOptions.categories,
                    selection = form.category,
                    onSelectionChange = { form.category = it },
                    error = form.errors["category"]
                )
            }
        )

        FieldPair(
            leading = {
                JewelSelect(
                    label = "Style",
                    options = WholesalerProductOptions.styles,
                    selection = form.style,
                    onSelectionChange = { form.style = it },
                    error = form.errors["style"]
                )
            },
            trailing = {
                JewelSelect(
                    label = "Size",
                    options = WholesalerProductOptions.sizes,
                    selection = form.size,
                    onSelectionChange = { form.size = it },
                    error = form.errors["size"]
                )
            }
        )

        JewelSelect(
            label = "Karatage",
            options = WholesalerProductOptions.purities,
            selection = form.metalPurity,
            onSelectionChange = { form.metalPurity = it },
            error = form.errors["metalPurity"]
        )
    }
}

@Composable
fun AddProductSpecsSection(form: AddProductFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            number = 3,
            title = "Specifications",
            subtitle = "Weight, pricing, manufacturing and dispatch estimates."
        )

        FieldPair(
            leading = {
                InputWithSuffix(
                    label = "Gross weight",
                    suffix = "gm",
                    text = form.grossWeight,
                    onTextChange = { form.grossWeight = it },
                    placeholder = "0.0",
                    error = form.errors["grossWeight"]
                )
            },
            trailing = {
                InputWithSuffix(
                    label = "Net weight",
                    suffix = "gm",
                    text = form.netWeight,
                    onTextChange = { form.netWeight = it },
                    placeholder = "0.0",
                    error = form.errors["netWeight"]
                )
            }
        )

        FieldPair(
            leading = {
                InputWithSuffix(
                    label = "Stone weight",
                    suffix = "gm",
                    text = form.stoneWeight,
                    onTextChange = { form.stoneWeight = it },
                    placeholder = "0.0",
                    error = form.errors["stoneWeight"]
                )
            },
            trailing = {
                if (!form.stockAvailable) {
                    InputWithSuffix(
                        label = "Dispatch time",
                        suffix = "days",
                        text = form.makeToOrderDays,
                        onTextChange = { form.makeToOrderDays = it },
                        placeholder = "3",
                        error = form.errors["makeToOrderDays"]
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(44.dp))
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "In Stock",
                    style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Text(
                    text = if (form.stockAvailable) "Ready for dispatch" else "Made to order",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF6B7280)
                )
            }
            JewelToggle(
                isOn = form.stockAvailable,
                onToggle = { form.stockAvailable = it }
            )
        }
    }
}

@Composable
fun AddProductSubmitSection(
    form: AddProductFormState,
    previewBalance: Int?,
    onSubmit: (publish: Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // AI Images generated selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Images to be generated",
                style = Typography.santoshi(14.sp, FontWeight.Medium),
                color = Color(0xFF111827)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(2, 3, 4).forEach { count ->
                    val isSelected = form.imageCount == count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { form.imageCount = count }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count images",
                            style = Typography.santoshi(14.sp, if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                            color = if (isSelected) Color.Black else Color(0xFF6B7280)
                        )
                    }
                }
            }

            val balanceStr = previewBalance?.let { " You have %,d credits.".format(it) } ?: ""
            Text(
                text = "12 credits per image. 2 is the base.$balanceStr",
                style = Typography.santoshi(12.sp),
                color = Color(0xFF6B7280)
            )
        }

        // Submit button
        val cost = form.imageCount * 12
        val isLimitReached = form.isLimitReached
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (isLimitReached) Color(0xFFD1D5DB) else Color.Black)
                .clickable(enabled = !isLimitReached) { onSubmit(true) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Submit · $cost credits",
                style = Typography.santoshi(16.sp, FontWeight.SemiBold),
                color = if (isLimitReached) Color(0xFF6B7280) else Color.White
            )
        }

        // Save & Upload Later
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color.Black, RoundedCornerShape(24.dp))
                .clickable { onSubmit(false) }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Save & Upload Later",
                style = Typography.santoshi(16.sp, FontWeight.SemiBold),
                color = Color.Black
            )
        }

        Text(
            text = "*By submitting, you allow us to display your product details and\nimages to retailers on the platform.",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AddProductFooter() {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Rights Reserved © Jewels India",
                style = Typography.santoshi(13.sp),
                color = Color(0xFF6B7280)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Crafted with ",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF374151)
                )
                Text(
                    text = "❤️",
                    fontSize = 12.sp
                )
                Text(
                    text = " in blr",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF374151)
                )
            }
        }
    }
}
