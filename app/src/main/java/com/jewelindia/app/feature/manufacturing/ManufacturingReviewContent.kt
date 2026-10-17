// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/CreateManufacturingRequestSheet.swift (Review & form helpers)
package com.jewelindia.app.feature.manufacturing

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ManufacturingReviewContent(
    selectedImageUri: Uri?,
    category: String,
    isRangeWeight: Boolean,
    minWeightText: String,
    maxWeightText: String,
    singleWeightText: String,
    material: String,
    quantity: Int,
    quotationWindowHours: Int,
    deliveryDate: LocalDate,
    notes: String,
    errorMessage: String?,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        selectedImageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Reference",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryRow("Category", category)
            SummaryRow(
                "Weight",
                if (isRangeWeight) "$minWeightText - $maxWeightText g" else "$singleWeightText g"
            )
            SummaryRow("Material", material)
            SummaryRow("Quantity", "$quantity piece(s)")
            SummaryRow("Quotation Window", "$quotationWindowHours hours")
            SummaryRow(
                "Delivery Deadline",
                deliveryDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            )
            if (notes.isNotBlank()) {
                SummaryRow("Design Notes", notes)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.goldPrimary.copy(alpha = 0.08f))
                .border(1.dp, Palette.goldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "How Supplier Quotes Work",
                style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                color = Palette.textPrimary
            )
            Text(
                text = "All verified wholesalers receive your enquiry at the same time. They submit quotes before the shared deadline. Compare the making charges, estimates and delivery dates, then select your preferred supplier. Submitting a quote does not reserve the project.",
                style = Typography.santoshi(12.sp),
                color = Palette.textSecondary,
                lineHeight = 18.sp
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = Typography.santoshi(13.sp),
                color = Palette.accentRed
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Palette.goldPrimary,
                contentColor = Palette.dark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Broadcast Request to Wholesalers", style = Typography.santoshi(16.sp, FontWeight.SemiBold))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = Typography.santoshi(13.sp),
            color = Palette.textSecondary
        )
        Text(
            text = value,
            style = Typography.santoshi(13.sp, FontWeight.SemiBold),
            color = Palette.textPrimary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun QuotationWindowSelector(
    selectedHours: Int,
    onHoursSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(text = "Time for Wholesalers to Quote", style = Typography.santoshi(13.sp, FontWeight.SemiBold), color = Palette.textPrimary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Palette.surface)
                .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            listOf(1, 6, 24, 48).forEach { hours ->
                val selected = selectedHours == hours
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) Palette.goldPrimary else Color.Transparent)
                        .clickable { onHoursSelected(hours) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (hours == 1) "1 hr" else "$hours hrs",
                        style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                        color = if (selected) Palette.dark else Palette.textSecondary
                    )
                }
            }
        }
        Text(
            text = "All verified wholesalers receive the enquiry together. You choose the supplier from their quotes.",
            style = Typography.santoshi(12.sp),
            color = Palette.textSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManufacturingDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onDateSelected(selected)
                }
                onDismiss()
            }) {
                Text("OK", color = Palette.goldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Palette.textSecondary)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
