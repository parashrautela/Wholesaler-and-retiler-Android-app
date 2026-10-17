// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakFeedbackSheet.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakFeedback
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamakFeedbackSheet(
    generationId: String,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comments by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.light
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Rate this Generation",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            if (submitted) {
                Text(
                    text = "Thank you for your feedback! It helps improve Chamak's AI accuracy.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.statusVerified
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= rating) Palette.goldPrimary else Palette.muted.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("What could be improved? (optional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        scope.launch {
                            submitting = true
                            try {
                                ChamakApi.submitFeedback(
                                    generationId = generationId,
                                    satisfied = rating >= 4,
                                    whatWentWrong = if (rating < 4) comments.ifBlank { "Needs improvement" } else null
                                )
                                submitted = true
                            } catch (_: Exception) {}
                            submitting = false
                        }
                    },
                    enabled = !submitting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Palette.light)
                    } else {
                        Text(
                            text = "Submit Feedback",
                            style = Typography.santoshi(15.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.light
                        )
                    }
                }
            }
        }
    }
}
