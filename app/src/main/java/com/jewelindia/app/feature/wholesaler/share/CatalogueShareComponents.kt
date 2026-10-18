package com.jewelindia.app.feature.wholesaler.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.CatalogueShare
import com.jewelindia.app.data.api.CatalogueShareStatus
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro
import java.time.Instant

@Composable
fun SelectedDesignsStrip(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    val displayProducts = products.take(6)
    val overflowCount = products.size - displayProducts.size

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayProducts.forEach { product ->
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(8.dp))
            ) {
                ProtectedImage(
                    url = product.displayImageUrl(ImageSize.CARD) ?: product.imageURL,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                )
            }
        }

        if (overflowCount > 0) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    style = manrope(13f, JewelWeight.Bold),
                    color = Palette.dark
                )
            }
        }
    }
}

@Composable
fun ViewersStepper(
    viewers: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Copy.CatalogueShare.viewersLabel(viewers),
            style = manrope(15f, JewelWeight.Medium),
            color = Palette.dark
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = viewers > 1,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (viewers > 1) Palette.cream else Palette.cream.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease viewers",
                    tint = if (viewers > 1) Palette.dark else Palette.muted
                )
            }

            Text(
                text = "$viewers",
                style = manrope(16f, JewelWeight.Bold),
                color = Palette.dark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = onIncrement,
                enabled = viewers < 100,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (viewers < 100) Palette.cream else Palette.cream.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase viewers",
                    tint = if (viewers < 100) Palette.dark else Palette.muted
                )
            }
        }
    }
}

@Composable
fun ValidityPicker(
    selectedOption: ShareDurationOption,
    customMinutes: Int?,
    onOptionSelected: (ShareDurationOption) -> Unit,
    onCustomMinutesChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = Copy.CatalogueShare.validityLabel,
            style = manrope(14f, JewelWeight.Semibold),
            color = Palette.dark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShareDurationOption.entries.forEach { option ->
                val isSelected = option == selectedOption
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Palette.dark else Palette.cream)
                        .border(
                            1.dp,
                            if (isSelected) Palette.dark else Palette.border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        style = manrope(11f, if (isSelected) JewelWeight.Bold else JewelWeight.Medium),
                        color = if (isSelected) Color.White else Palette.dark,
                        maxLines = 1
                    )
                }
            }
        }

        if (selectedOption == ShareDurationOption.CUSTOM) {
            OutlinedTextField(
                value = customMinutes?.toString() ?: "",
                onValueChange = { raw ->
                    val filtered = raw.filter { it.isDigit() }
                    val parsed = filtered.toIntOrNull()
                    onCustomMinutesChanged(parsed)
                },
                placeholder = {
                    Text(
                        text = Copy.CatalogueShare.customField,
                        style = manrope(13f, JewelWeight.Regular),
                        color = Palette.muted
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.dark,
                    unfocusedBorderColor = Palette.border,
                    cursorColor = Palette.dark
                )
            )
        }
    }
}

@Composable
fun LinkReadyCard(
    share: CatalogueShare,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Copy.CatalogueShare.readyHeader,
            style = sfPro(17f, JewelWeight.Bold),
            color = Palette.dark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onShare,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(
                        text = Copy.CatalogueShare.readyShare,
                        style = manrope(13f, JewelWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = onCopy,
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.cream),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Palette.dark)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Palette.dark
                    )
                    Text(
                        text = Copy.CatalogueShare.readyCopy,
                        style = manrope(13f, JewelWeight.Bold),
                        color = Palette.dark
                    )
                }
            }
        }

        Text(
            text = Copy.CatalogueShare.readyExpires(share.formattedExpiry()),
            style = manrope(12f, JewelWeight.Medium),
            color = Palette.muted
        )

        Text(
            text = Copy.CatalogueShare.readyWarning,
            style = manrope(12f, JewelWeight.Regular),
            color = Palette.muted
        )
    }
}

@Composable
fun RecentShareRow(
    share: CatalogueShare,
    isRevoking: Boolean,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = share.status(Instant.now())
    val statusBg = when (status) {
        CatalogueShareStatus.ACTIVE -> Palette.accentGreen.copy(alpha = 0.15f)
        CatalogueShareStatus.REVOKED -> Color(0xFFE5E5EA)
        CatalogueShareStatus.EXPIRED -> Palette.statusPending.copy(alpha = 0.15f)
        CatalogueShareStatus.FULL -> Color(0xFFEFEFF4)
    }
    val statusColor = when (status) {
        CatalogueShareStatus.ACTIVE -> Palette.accentGreen
        CatalogueShareStatus.REVOKED -> Palette.muted
        CatalogueShareStatus.EXPIRED -> Palette.statusPending
        CatalogueShareStatus.FULL -> Palette.muted
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!share.title.isNullOrBlank()) share.title else Copy.CatalogueShare.titlePlaceholder,
                style = manrope(15f, JewelWeight.Bold),
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = status.displayLabel,
                    style = manrope(11f, JewelWeight.Semibold),
                    color = statusColor
                )
            }
        }

        Text(
            text = Copy.CatalogueShare.listRow(share.itemCount, share.viewsUsed, share.maxViewers),
            style = manrope(13f, JewelWeight.Medium),
            color = Palette.muted
        )

        Text(
            text = Copy.CatalogueShare.readyExpires(share.formattedExpiry()),
            style = manrope(12f, JewelWeight.Regular),
            color = Palette.muted
        )

        // Show Revoke link for EVERY active share, regardless of whether link URL is known
        if (status.canRevoke) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRevoking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Palette.accentRed
                    )
                } else {
                    Text(
                        text = Copy.CatalogueShare.revokeButton,
                        style = manrope(13f, JewelWeight.Bold),
                        color = Palette.accentRed,
                        modifier = Modifier
                            .clickable { onRevoke() }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    )
                }
            }
        }
    }
}
