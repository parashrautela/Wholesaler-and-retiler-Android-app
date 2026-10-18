package com.jewelindia.app.feature.wholesaler.share

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro

@Composable
fun CatalogueShareSheet(
    selectedProducts: List<Product>,
    onDismiss: (linkCreated: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogueShareViewModel = remember(selectedProducts) {
        CatalogueShareViewModel(selectedProducts = selectedProducts)
    }
) {
    val context = LocalContext.current
    val title by viewModel.title.collectAsState()
    val viewers by viewModel.viewers.collectAsState()
    val durationOption by viewModel.durationOption.collectAsState()
    val customMinutes by viewModel.customMinutes.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val creationError by viewModel.creationError.collectAsState()
    val createdShare by viewModel.createdShare.collectAsState()
    val hasCreatedLink by viewModel.hasCreatedLink.collectAsState()

    val shares by viewModel.shares.collectAsState()
    val isLoadingShares by viewModel.isLoadingShares.collectAsState()
    val sharesLoadError by viewModel.sharesLoadError.collectAsState()
    val revokingShareId by viewModel.revokingShareId.collectAsState()
    val actionNotice by viewModel.actionNotice.collectAsState()

    LaunchedEffect(actionNotice) {
        actionNotice?.let { notice ->
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            viewModel.clearActionNotice()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Copy.CatalogueShare.sheetTitle,
                style = sfPro(22f, JewelWeight.Bold),
                color = Palette.dark
            )

            Text(
                text = "Done",
                style = manrope(15f, JewelWeight.Bold),
                color = Palette.dark,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onDismiss(hasCreatedLink) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Selected Designs Strip
        if (selectedProducts.isNotEmpty()) {
            SelectedDesignsStrip(products = selectedProducts)
        }

        // Introduction text
        Text(
            text = Copy.CatalogueShare.sheetIntro,
            style = manrope(13f, JewelWeight.Regular),
            color = Palette.muted
        )

        // Link Settings Form (iOS grouped style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.cream)
                .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Optional Title
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = Copy.CatalogueShare.titleField,
                    style = manrope(14f, JewelWeight.Semibold),
                    color = Palette.dark
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.setTitle(it) },
                    placeholder = {
                        Text(
                            text = Copy.CatalogueShare.titlePlaceholder,
                            style = manrope(14f, JewelWeight.Regular),
                            color = Palette.muted
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Palette.dark,
                        unfocusedBorderColor = Palette.border,
                        cursorColor = Palette.dark
                    )
                )
            }

            // Viewers Stepper
            ViewersStepper(
                viewers = viewers,
                onIncrement = { viewModel.incrementViewers() },
                onDecrement = { viewModel.decrementViewers() }
            )

            // Validity Picker
            ValidityPicker(
                selectedOption = durationOption,
                customMinutes = customMinutes,
                onOptionSelected = { viewModel.setDurationOption(it) },
                onCustomMinutesChanged = { viewModel.setCustomMinutes(it) }
            )

            // Privacy Note
            Text(
                text = Copy.CatalogueShare.privacyNote,
                style = manrope(12f, JewelWeight.Regular),
                color = Palette.muted
            )

            // Create Link Button
            Button(
                onClick = { viewModel.createShare() },
                enabled = viewModel.isCreateEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Palette.dark,
                    disabledContainerColor = Palette.dark.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                if (isCreating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = Copy.CatalogueShare.createBusy,
                            style = manrope(14f, JewelWeight.Bold),
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = Copy.CatalogueShare.createButton,
                        style = manrope(14f, JewelWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Creation Error
            if (creationError != null) {
                Text(
                    text = creationError ?: "",
                    style = manrope(13f, JewelWeight.Medium),
                    color = Palette.accentRed
                )
            }
        }

        // Ready Card (shown when share is created and url is available)
        createdShare?.let { share ->
            val shareUrl = share.url
            if (shareUrl != null) {
                LinkReadyCard(
                    share = share,
                    onShare = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                Copy.CatalogueShare.shareText(share.title, shareUrl)
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Catalogue"))
                    },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Catalogue Share Link", shareUrl)
                        clipboard?.setPrimaryClip(clip)
                        viewModel.setActionNotice(Copy.CatalogueShare.readyCopied)
                    }
                )
            }
        }

        // Recent Links Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = Copy.CatalogueShare.listHeader,
                style = sfPro(17f, JewelWeight.Bold),
                color = Palette.dark
            )

            if (isLoadingShares && shares.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Palette.dark
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = Copy.CatalogueShare.listLoading,
                        style = manrope(13f, JewelWeight.Regular),
                        color = Palette.muted
                    )
                }
            } else if (sharesLoadError) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Copy.CatalogueShare.listOffline,
                        style = manrope(13f, JewelWeight.Regular),
                        color = Palette.muted
                    )
                    Button(
                        onClick = { viewModel.loadShares() },
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.cream),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Palette.border),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = Copy.CatalogueShare.listRefresh,
                            style = manrope(13f, JewelWeight.Semibold),
                            color = Palette.dark
                        )
                    }
                }
            } else if (shares.isEmpty()) {
                Text(
                    text = Copy.CatalogueShare.listEmpty,
                    style = manrope(13f, JewelWeight.Regular),
                    color = Palette.muted,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                shares.forEach { share ->
                    RecentShareRow(
                        share = share,
                        isRevoking = revokingShareId == share.id,
                        onRevoke = { viewModel.revokeShare(share) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
