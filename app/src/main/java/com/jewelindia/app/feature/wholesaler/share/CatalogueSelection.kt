package com.jewelindia.app.feature.wholesaler.share

import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy

/**
 * Tracks the wholesaler's ordered selection of published designs for catalogue sharing.
 *
 * Rules (CONTRACT.md §2):
 * - Order is preserved (ordered list of design IDs).
 * - Max 50 designs in one link (exceeding returns Copy.CatalogueShare.selectLimit).
 * - Only published designs can be selected (unpublished returns Copy.CatalogueShare.selectUnpublished).
 * - Toggling an already-selected design removes it.
 */
data class CatalogueSelection(
    val ids: List<String> = emptyList(),
    val snapshots: Map<String, Product> = emptyMap()
) {
    val products: List<Product>
        get() = ids.mapNotNull { snapshots[it] }

    val count: Int
        get() = ids.size

    val isEmpty: Boolean
        get() = ids.isEmpty()

    fun contains(productId: String): Boolean = ids.contains(productId)

    /**
     * Toggles selection of [product].
     * Returns a pair of the new [CatalogueSelection] state and an optional user-facing message
     * if the selection was rejected (e.g. unpublished, limit reached).
     */
    fun toggle(product: Product): Pair<CatalogueSelection, String?> {
        if (ids.contains(product.id)) {
            val newIds = ids.filter { it != product.id }
            val newSnapshots = snapshots.toMutableMap().apply { remove(product.id) }
            return Pair(copy(ids = newIds, snapshots = newSnapshots), null)
        }
        if (product.isPublished != true) {
            return Pair(this, Copy.CatalogueShare.selectUnpublished)
        }
        if (ids.size >= 50) {
            return Pair(this, Copy.CatalogueShare.selectLimit)
        }
        val newIds = ids + product.id
        val newSnapshots = snapshots + (product.id to product)
        return Pair(copy(ids = newIds, snapshots = newSnapshots), null)
    }

    fun clear(): CatalogueSelection = CatalogueSelection()
}
