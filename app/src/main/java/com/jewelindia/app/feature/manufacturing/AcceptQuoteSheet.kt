// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/AcceptQuoteSheet.swift
package com.jewelindia.app.feature.manufacturing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jewelindia.app.data.net.userMessage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.AcceptOfferQuoteParams
import com.jewelindia.app.core.model.MakingBudgetMode
import com.jewelindia.app.core.model.ManufacturingOffer
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptQuoteSheet(
    offer: ManufacturingOffer,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var totalQuoteAmount by remember { mutableStateOf("") }
    var leadTimeDays by remember { mutableStateOf("14") }
    var notes by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.light
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Submit Quotation",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            OutlinedTextField(
                value = totalQuoteAmount,
                onValueChange = { totalQuoteAmount = it },
                label = { Text("Total Quote Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = leadTimeDays,
                onValueChange = { leadTimeDays = it },
                label = { Text("Delivery Lead Time (days)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Specifications (optional)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val amount = totalQuoteAmount.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid quote amount"
                        return@Button
                    }
                    scope.launch {
                        loading = true
                        errorMessage = null
                        try {
                            val days = leadTimeDays.toLongOrNull() ?: 14
                            val deliveryDate = java.time.Instant.now()
                                .plus(days, java.time.temporal.ChronoUnit.DAYS)
                                .toString().take(10)
                            val params = AcceptOfferQuoteParams(
                                totalQuoteAmount = amount,
                                proposedDeliveryDate = deliveryDate,
                                comments = notes.ifBlank { null }
                            )
                            ManufacturingApi.acceptOffer(offer.id, params)
                            onSuccess()
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.userMessage()
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Palette.light)
                } else {
                    Text(
                        text = "Send Quotation",
                        style = Typography.santoshi(15.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.light
                    )
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Palette.muted)
            }
        }
    }
}
