// Tablet / large-screen layout helpers (no iOS counterpart — the iOS app is iPhone-only).
package com.jewelindia.app.design

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Both helpers below are no-ops at phone widths, so phone layouts stay exactly as they were;
 * they only change anything once the screen is wider than the thresholds here.
 */
object AdaptiveLayout {
    /** Readable column width for forms, lists and detail pages on tablets. */
    val contentMaxWidth = 720.dp

    /** Below this grid width the phone column count is used unchanged. */
    val phoneGridMaxWidth = 600.dp
}

/**
 * Caps the content at [maxWidth] and centres it when more room is available (tablets,
 * landscape). Place it after `.background(...)` so the background still fills the screen.
 * When the available width is at most [maxWidth] it measures and places exactly as if it
 * weren't there.
 */
fun Modifier.readableContentWidth(maxWidth: Dp = AdaptiveLayout.contentMaxWidth): Modifier =
    this then ReadableContentWidthElement(maxWidth)

private data class ReadableContentWidthElement(val maxWidth: Dp) :
    ModifierNodeElement<ReadableContentWidthNode>() {
    override fun create() = ReadableContentWidthNode(maxWidth)

    override fun update(node: ReadableContentWidthNode) {
        node.maxWidth = maxWidth
        node.invalidateMeasurement()
    }
}

private class ReadableContentWidthNode(var maxWidth: Dp) : Modifier.Node(), LayoutModifierNode {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val capPx = maxWidth.roundToPx()
        if (!constraints.hasBoundedWidth || constraints.maxWidth <= capPx) {
            val placeable = measurable.measure(constraints)
            return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
        }
        val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = capPx))
        val width = constraints.maxWidth
        return layout(width, placeable.height) { placeable.place((width - placeable.width) / 2, 0) }
    }
}

/**
 * Phone-first grid columns: exactly [phoneColumns] while the grid is narrower than
 * [AdaptiveLayout.phoneGridMaxWidth] (every phone in portrait), otherwise as many
 * ~[minCellWidth] columns as fit, never fewer than [phoneColumns].
 * Cell sizes are computed the same way as [GridCells.Fixed].
 */
data class PhoneFirstGridCells(
    val phoneColumns: Int = 2,
    val minCellWidth: Dp = 180.dp
) : GridCells {
    override fun Density.calculateCrossAxisCellSizes(availableSize: Int, spacing: Int): List<Int> {
        val count = if (availableSize < AdaptiveLayout.phoneGridMaxWidth.roundToPx()) {
            phoneColumns
        } else {
            maxOf(phoneColumns, (availableSize + spacing) / (minCellWidth.roundToPx() + spacing))
        }
        val gridSize = availableSize - spacing * (count - 1)
        val slotSize = gridSize / count
        val remaining = gridSize % count
        return List(count) { slotSize + if (it < remaining) 1 else 0 }
    }
}
