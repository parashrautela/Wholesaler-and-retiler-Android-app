package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope
import com.jewelindia.app.design.sfPro
import com.jewelindia.app.feature.wholesaler.share.CatalogueSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogSharePreviewSheetHost(
    selection: CatalogueSelection,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = Copy.CatalogueShare.sheetTitle,
                style = sfPro(22f, JewelWeight.Bold),
                color = Palette.dark
            )
            Text(
                text = Copy.CatalogueShare.sheetIntro,
                style = manrope(14f, JewelWeight.Regular),
                color = Palette.muted
            )
            Text(
                text = "${selection.count} designs selected",
                style = manrope(15f, JewelWeight.Semibold),
                color = Palette.dark
            )
            Text(
                text = Copy.CatalogueShare.privacyNote,
                style = manrope(12f, JewelWeight.Regular),
                color = Palette.muted
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogProductDetailSheetHost(
    product: Product,
    wholesalerId: String,
    onDismiss: () -> Unit,
    onPublishToggle: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onUpdated: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        ProductDetailSheet(
            product = product,
            wholesalerId = wholesalerId,
            onDismiss = onDismiss,
            onPublishToggle = onPublishToggle,
            onEdit = onEdit,
            onUpdated = { onUpdated() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogEditProductSheetHost(
    product: Product,
    onDismiss: () -> Unit,
    onSaved: (Product) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        EditProductScreen(
            product = product,
            onDismiss = onDismiss,
            onSaved = onSaved
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogAddProductSheetHost(
    wholesalerId: String,
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
    onOpenUploadHistory: (() -> Unit)?
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        AddProductSheet(
            wholesalerId = wholesalerId,
            userId = wholesalerId,
            onDismiss = onDismiss,
            onAdded = { onAdded() },
            onOpenUploadHistory = {
                onDismiss()
                onOpenUploadHistory?.invoke()
            }
        )
    }
}
