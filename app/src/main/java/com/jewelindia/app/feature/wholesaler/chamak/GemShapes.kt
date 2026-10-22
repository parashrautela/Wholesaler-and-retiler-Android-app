package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 1. Soft Organic Floral Blossom with velvety rounded lobes
 */
class SoftBlossomShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = min(size.width, size.height) * 0.44f
        val innerRadius = radius * 0.72f
        val petals = 5

        for (i in 0 until petals) {
            val angle1 = (i * (360.0 / petals) - 90.0) * Math.PI / 180.0
            val angleMid = (i * (360.0 / petals) + (180.0 / petals) - 90.0) * Math.PI / 180.0
            val angle2 = ((i + 1) * (360.0 / petals) - 90.0) * Math.PI / 180.0

            val p1X = (centerX + cos(angle1) * innerRadius).toFloat()
            val p1Y = (centerY + sin(angle1) * innerRadius).toFloat()
            val tipX = (centerX + cos(angleMid) * radius).toFloat()
            val tipY = (centerY + sin(angleMid) * radius).toFloat()
            val p2X = (centerX + cos(angle2) * innerRadius).toFloat()
            val p2Y = (centerY + sin(angle2) * innerRadius).toFloat()

            if (i == 0) {
                path.moveTo(p1X, p1Y)
            }
            val ctrl1X = ((p1X + tipX) / 2f + cos(angleMid) * 5f).toFloat()
            val ctrl1Y = ((p1Y + tipY) / 2f + sin(angleMid) * 5f).toFloat()
            path.quadraticTo(ctrl1X, ctrl1Y, tipX, tipY)

            val ctrl2X = ((tipX + p2X) / 2f + cos(angleMid) * 5f).toFloat()
            val ctrl2Y = ((tipY + p2Y) / 2f + sin(angleMid) * 5f).toFloat()
            path.quadraticTo(ctrl2X, ctrl2Y, p2X, p2Y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * 2. Soft Cabochon Trillion: Outward-curving pillow triangle with large rounded vertices
 */
class SoftCabochonTrillionShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val inset = 8f
        val minX = inset
        val minY = inset
        val maxX = size.width - inset
        val maxY = size.height - inset
        val midX = size.width / 2f
        val midY = size.height / 2f

        val pTopX = midX
        val pTopY = minY + 6f
        val pBottomRightX = maxX - 6f
        val pBottomRightY = maxY - 6f
        val pBottomLeftX = minX + 6f
        val pBottomLeftY = maxY - 6f

        val ctrlRightX = maxX + 10f
        val ctrlRightY = midY - 2f
        val ctrlBottomX = midX
        val ctrlBottomY = maxY + 14f
        val ctrlLeftX = minX - 10f
        val ctrlLeftY = midY - 2f

        path.moveTo(midX - 12f, minY + 12f)
        path.quadraticTo(pTopX, pTopY, midX + 12f, minY + 12f)
        path.quadraticTo(ctrlRightX, ctrlRightY, maxX - 8f, maxY - 18f)
        path.quadraticTo(pBottomRightX, pBottomRightY, maxX - 20f, maxY - 6f)
        path.quadraticTo(ctrlBottomX, ctrlBottomY, minX + 20f, maxY - 6f)
        path.quadraticTo(pBottomLeftX, pBottomLeftY, minX + 8f, maxY - 18f)
        path.quadraticTo(ctrlLeftX, ctrlLeftY, midX - 12f, minY + 12f)
        path.close()
        return Outline.Generic(path)
    }
}

/**
 * 3. Soft Cushion Cabochon: Continuous squircle with generous radius
 */
class SoftCushionGemShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cornerRadius = min(size.width, size.height) * 0.36f
        val inset = 4f
        val roundRect = RoundRect(
            left = inset,
            top = inset,
            right = size.width - inset,
            bottom = size.height - inset,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
        return Outline.Rounded(roundRect)
    }
}

/**
 * 4. Soft Teardrop Pear: Gentle rounded pear/drop with smooth curvature
 */
class SoftTeardropGemShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val inset = 6f
        val minX = inset
        val minY = inset
        val maxX = size.width - inset
        val maxY = size.height - inset
        val midX = size.width / 2f
        val midY = size.height / 2f
        val height = maxY - minY

        val topX = midX
        val topY = minY + 8f
        val bottomX = midX
        val bottomY = maxY

        path.moveTo(midX - 8f, minY + 16f)
        path.quadraticTo(topX, topY, midX + 8f, minY + 16f)
        path.cubicTo(
            maxX + 10f, midY + height * 0.15f,
            maxX - 6f, maxY,
            bottomX, bottomY
        )
        path.cubicTo(
            minX + 6f, maxY,
            minX - 10f, midY + height * 0.15f,
            midX - 8f, minY + 16f
        )
        path.close()
        return Outline.Generic(path)
    }
}
