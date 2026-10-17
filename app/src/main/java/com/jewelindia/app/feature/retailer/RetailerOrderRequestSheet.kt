// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/YourTasteView.swift (Order request sheet)
package com.jewelindia.app.feature.retailer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrderRequestSheet(
    product: Product,
    onDismiss: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var quantity by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (submitted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Palette.accentGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Order Request Sent!",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "The manufacturer will review your request and confirm dispatch timeline.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            onDismiss()
                            onOrderSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Request Order",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                    }
                }

                // Product summary banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Palette.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProtectedImage(
                        url = product.primaryImage,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.displayTitle,
                            style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${product.purity ?: "22KT"} · ${product.weightGrams?.let { "${it}g" } ?: "Weight on order"}",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                }

                // Quantity Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantity",
                        style = Typography.santoshi(16.sp, FontWeight.Medium),
                        color = Palette.textPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Palette.surface)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = Palette.textPrimary)
                        }

                        Text(
                            text = quantity.toString(),
                            style = Typography.santoshi(18.sp, FontWeight.Bold),
                            color = Palette.textPrimary,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { if (quantity < 100) quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Palette.surface)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase", tint = Palette.textPrimary)
                        }
                    }
                }

                // Customization / notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Customization Notes",
                        style = Typography.santoshi(14.sp, FontWeight.Medium),
                        color = Palette.textPrimary
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Specify ring size, gemstone preference, or engraving...", color = Palette.textTertiary) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Palette.goldPrimary,
                            unfocusedBorderColor = Palette.surfaceBorder,
                            focusedContainerColor = Palette.surface,
                            unfocusedContainerColor = Palette.surface,
                            focusedTextColor = Palette.textPrimary,
                            unfocusedTextColor = Palette.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                    onClick = {
                        submitting = true
                        errorMessage = null
                        scope.launch {
                            try {
                                JewelApi.createRetailerOrder(
                                    productId = product.id,
                                    quantity = quantity,
                                    notes = notes
                                )
                                StoreActivity.log(StoreActivity.Event.OrderRequested, productId = product.id)
                                submitted = true
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to place order request. Please try again."
                            } finally {
                                submitting = false
                            }
                        }
                    },
                    enabled = !submitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "Send Order Request",
                            style = Typography.santoshi(16.sp, FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
