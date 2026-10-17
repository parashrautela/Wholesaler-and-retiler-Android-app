package com.jewelindia.app.feature.wholesaler.chamak

sealed class ChamakSelectionBlockReason {
    data object MissingImage : ChamakSelectionBlockReason()
    data object MissingType : ChamakSelectionBlockReason()
    data class SameTypeSelected(val label: String) : ChamakSelectionBlockReason()
    data object SetFull : ChamakSelectionBlockReason()

    val message: String
        get() = when (this) {
            MissingImage -> "This product has no usable image."
            MissingType -> "Add a jewellery type to use this item in a set."
            is SameTypeSelected -> "A ${label.lowercase()} is already selected. Remove it to choose another."
            SetFull -> "Remove a piece to add another."
        }
}
