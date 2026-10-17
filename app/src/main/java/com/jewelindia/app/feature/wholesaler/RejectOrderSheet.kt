// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerOrdersView.swift (RejectOrderSheet)
package com.jewelindia.app.feature.wholesaler

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

object RejectOrderDefaults {
    val reasons = listOf(
        "Design out of stock",
        "Can't meet the delivery time",
        "Customisation not possible",
        "Metal rate has changed",
        "Below minimum order"
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RejectOrderSheet(
    onDismiss: () -> Unit,
    onConfirm: suspend (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var chosen by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fullReason: String? = remember(chosen, details) {
        val selected = chosen ?: return@remember null
        val extra = details.trim()
        if (extra.isEmpty()) selected else "$selected — $extra"
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reject Order",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            Text(
                text = "Why can't this order be fulfilled? The store will see this.",
                style = Typography.santoshi(14.sp),
                color = Palette.textSecondary
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RejectOrderDefaults.reasons.forEach { item ->
                    val isChosen = chosen == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isChosen) Palette.dark else Palette.surface)
                            .border(
                                1.dp,
                                if (isChosen) Palette.dark else Palette.surfaceBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                chosen = if (isChosen) null else item
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item,
                                style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                                color = if (isChosen) Color.White else Palette.textPrimary
                            )
                            if (isChosen) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (chosen != null) {
                Text(
                    text = "Tap the selected reason again to choose a different one.",
                    style = Typography.santoshi(11.sp),
                    color = Palette.textSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Add details (optional)",
                    style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                    color = Palette.textPrimary
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = { Text("e.g. back in stock on 5 Oct", color = Palette.textTertiary) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.goldPrimary,
                        unfocusedBorderColor = Palette.surfaceBorder
                    )
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = Typography.santoshi(12.sp),
                    color = Palette.accentRed
                )
            }

            Button(
                onClick = {
                    val r = fullReason ?: return@Button
                    isSaving = true
                    errorMessage = null
                    scope.launch {
                        try {
                            onConfirm(r)
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to reject order."
                            isSaving = false
                        }
                    }
                },
                enabled = fullReason != null && !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.accentRed,
                    contentColor = Color.White,
                    disabledContainerColor = Palette.accentRed.copy(alpha = 0.35f),
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Confirm Rejection", style = Typography.santoshi(15.sp, FontWeight.Bold))
                }
            }
        }
    }
}
