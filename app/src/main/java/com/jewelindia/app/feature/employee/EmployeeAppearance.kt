// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeAppearance.swift
package com.jewelindia.app.feature.employee

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.prefs.Prefs
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

/**
 * Personal style applied on top of the store theme.
 */
enum class EmployeeStyle(val id: String, val title: String, val subtitle: String) {
    Classic(
        id = "classic",
        title = "Heritage Classic",
        subtitle = "Timeless royal gold & warm terracotta tones"
    ),
    Sarvam(
        id = "sarvam",
        title = "Sarvam Creative",
        subtitle = "Rich jewel emeralds, royal blues & architectural arches"
    ),
    Modern(
        id = "modern",
        title = "Contemporary Studio",
        subtitle = "Sleek noir, champagne accents & minimalist lines"
    );

    val primaryTint: Color
        get() = when (this) {
            Classic -> Palette.goldPrimary
            Sarvam -> Color(0xFF0F5A47) // Rich emerald
            Modern -> Color(0xFF1E242B) // Deep slate
        }

    val accentTint: Color
        get() = when (this) {
            Classic -> Palette.goldLight
            Sarvam -> Color(0xFFD4AF37) // Bright gold accent
            Modern -> Color(0xFFB39257) // Champagne
        }

    val cardBackground: Color
        get() = when (this) {
            Classic -> Color(0xFFFFFFFF)
            Sarvam -> Color(0xFFFAF8F5)
            Modern -> Color(0xFFF9F9FA)
        }

    val heroGradient: Brush
        get() = when (this) {
            Classic -> Brush.verticalGradient(
                listOf(Color(0xFF1A140E).copy(alpha = 0.85f), Color(0xFF0B0907))
            )
            Sarvam -> Brush.verticalGradient(
                listOf(Color(0xFF0A261D).copy(alpha = 0.90f), Color(0xFF04120E))
            )
            Modern -> Brush.verticalGradient(
                listOf(Color(0xFF1B1E22).copy(alpha = 0.92f), Color(0xFF0E1012))
            )
        }

    companion object {
        fun load(context: Context): EmployeeStyle {
            val prefs = Prefs(context)
            val saved = prefs.employeeStyle
            if (!saved.isNullOrBlank()) {
                entries.find { it.id.equals(saved, ignoreCase = true) }?.let { return it }
            }
            return if (prefs.employeeSarvamLegacy) Sarvam else Classic
        }

        fun save(context: Context, style: EmployeeStyle) {
            val prefs = Prefs(context)
            prefs.employeeStyle = style.id
            prefs.employeeSarvam = (style == Sarvam)
        }
    }
}

val LocalEmployeeStyle = compositionLocalOf { EmployeeStyle.Classic }

// MARK: - Architectural Courtyard Arch Shape

/**
 * Traditional Indian scalloped/pointed courtyard archway path.
 */
val EmployeeCourtyardArchShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height

    reset()
    moveTo(0f, h)
    lineTo(0f, h * 0.38f)

    // Arch curve up to apex
    cubicTo(
        x1 = 0f, y1 = h * 0.15f,
        x2 = w * 0.28f, y2 = 0f,
        x3 = w * 0.5f, y3 = 0f
    )
    cubicTo(
        x1 = w * 0.72f, y1 = 0f,
        x2 = w, y2 = h * 0.15f,
        x3 = w, y3 = h * 0.38f
    )

    lineTo(w, h)
    close()
}

// MARK: - Jali Lattice Pattern

@Composable
fun EmployeeJaliPattern(
    modifier: Modifier = Modifier,
    color: Color = Palette.goldPrimary.copy(alpha = 0.08f),
    cellSize: Float = 36f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1f)

        var x = 0f
        while (x < w + cellSize) {
            var y = 0f
            while (y < h + cellSize) {
                // Diamond lattice with center flower point
                val path = Path().apply {
                    moveTo(x, y - cellSize * 0.5f)
                    lineTo(x + cellSize * 0.5f, y)
                    lineTo(x, y + cellSize * 0.5f)
                    lineTo(x - cellSize * 0.5f, y)
                    close()
                }
                drawPath(path, color = color, style = stroke)
                drawCircle(
                    color = color,
                    radius = 1.5f,
                    center = Offset(x, y)
                )
                y += cellSize
            }
            x += cellSize
        }
    }
}

// MARK: - Creative Backdrop

@Composable
fun EmployeeCreativeBackdrop(
    modifier: Modifier = Modifier,
    style: EmployeeStyle = LocalEmployeeStyle.current
) {
    Box(modifier = modifier.fillMaxSize().background(style.heroGradient)) {
        EmployeeJaliPattern(
            modifier = Modifier.fillMaxSize(),
            color = style.accentTint.copy(alpha = 0.06f),
            cellSize = 44f
        )
    }
}

// MARK: - View Mode Bar

@Composable
fun EmployeeModeBar(
    storeName: String,
    style: EmployeeStyle = LocalEmployeeStyle.current,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(style.primaryTint)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "SHOWROOM VIEW · $storeName".uppercase(),
            style = Typography.santoshi(11.sp, FontWeight.Bold),
            color = Color.White,
            letterSpacing = 1.2.sp
        )
    }
}
