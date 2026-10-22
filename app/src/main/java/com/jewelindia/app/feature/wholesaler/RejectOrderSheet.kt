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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.jewelindia.app.data.net.userMessage
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalLayoutApi::class)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // iOS Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable(enabled = !isSaving) { onDismiss() }
            ) {
                Text(
                    text = "Cancel",
                    style = Typography.santoshi(15.sp, FontWeight.Medium),
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "Reject Order",
                style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                color = Palette.foreground
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Why can't this order be fulfilled? The store will see this.",
                style = Typography.santoshi(14.sp),
                color = Palette.muted
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RejectOrderDefaults.reasons.forEach { item ->
                    val isChosen = chosen == item
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isChosen) Palette.dark else Color.White)
                            .border(1.dp, if (isChosen) Palette.dark else Palette.border, CircleShape)
                            .clickable {
                                chosen = if (isChosen) null else item
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item,
                            style = Typography.santoshi(13.sp, FontWeight.SemiBold),
                            color = if (isChosen) Color.White else Palette.foreground
                        )
                        if (isChosen) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (chosen != null) {
                Text(
                    text = "Tap the selected reason again to choose a different one.",
                    style = Typography.santoshi(11.sp),
                    color = Palette.muted
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Add details (optional)",
                    style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                    color = Palette.foreground
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Palette.border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (details.isEmpty()) {
                        Text(
                            text = "e.g. back in stock on 5 Oct",
                            style = Typography.santoshi(14.sp),
                            color = Palette.muted
                        )
                    }
                    BasicTextField(
                        value = details,
                        onValueChange = { details = it },
                        textStyle = TextStyle(
                            fontFamily = Typography.santoshi(14.sp).fontFamily,
                            fontSize = 14.sp,
                            color = Palette.foreground
                        ),
                        cursorBrush = SolidColor(Palette.dark),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Pinned Bottom Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = Typography.santoshi(12.sp),
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val canSubmit = fullReason != null && !isSaving
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (fullReason == null) Color(0xFFEF4444).copy(alpha = 0.35f)
                        else Color(0xFFEF4444)
                    )
                    .clickable(enabled = canSubmit) {
                        scope.launch {
                            isSaving = true
                            errorMessage = null
                            try {
                                onConfirm(fullReason ?: return@launch)
                                onDismiss()
                            } catch (e: Exception) {
                                errorMessage = e.userMessage()
                            }
                            isSaving = false
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = if (isSaving) "Rejecting…" else "Confirm Rejection",
                        style = Typography.santoshi(15.sp, FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
