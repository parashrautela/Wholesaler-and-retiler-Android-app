package com.jewelindia.app.feature.wholesaler

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.feature.wholesaler.share.CatalogueShareSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogShareSheetHost(
    selectedProducts: List<Product>,
    onDismiss: (linkCreated: Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss(false) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        CatalogueShareSheet(
            selectedProducts = selectedProducts,
            onDismiss = onDismiss
        )
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
