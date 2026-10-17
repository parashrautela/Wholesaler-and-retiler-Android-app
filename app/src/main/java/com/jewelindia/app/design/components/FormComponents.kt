// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/FormComponents.swift
package com.jewelindia.app.design.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Radius
import com.jewelindia.app.design.Spacing
import com.jewelindia.app.design.gilroy

/**
 * A black circle badge with the step number, plus the section title and subtitle.
 */
@Composable
fun SectionHeader(
    number: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = Spacing.sm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = gilroy(20f, JewelWeight.Bold),
                color = Color(0xFF111827)
            )
        }
        Text(
            text = subtitle,
            style = gilroy(13f, JewelWeight.Regular),
            color = Color(0xFF6B7280)
        )
    }
}

/**
 * Label + control + optional error text.
 */
@Composable
fun LabelledField(
    label: String,
    error: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = gilroy(13f, JewelWeight.Semibold),
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                style = gilroy(12f, JewelWeight.Semibold),
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

data class SelectOption(
    val value: String,
    val label: String
)

/**
 * Native drop-down selection field.
 */
@Composable
fun JewelSelect(
    label: String,
    options: List<SelectOption>,
    selection: String,
    onSelectionChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.firstOrNull { it.value == selection }

    LabelledField(label = label, error = error, modifier = modifier) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(Radius.shapeLg)
                    .background(Color.White)
                    .border(
                        width = if (error != null) 1.5.dp else 1.dp,
                        color = if (error != null) Color(0xFFEF4444) else Color(0xFFE5E5E5),
                        shape = Radius.shapeLg
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption?.label ?: "select",
                    style = gilroy(14f, JewelWeight.Semibold),
                    color = Color(0xFF111827),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.label,
                                    style = gilroy(14f, if (selection == option.value) JewelWeight.Bold else JewelWeight.Regular),
                                    color = Color(0xFF111827),
                                    modifier = Modifier.weight(1f)
                                )
                                if (selection == option.value) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF111827),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectionChange(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Numeric field with a grey unit cell on the trailing edge.
 */
@Composable
fun InputWithSuffix(
    label: String,
    suffix: String,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String = "0.00",
    error: String? = null,
    modifier: Modifier = Modifier
) {
    LabelledField(label = label, error = error, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(Radius.shapeLg)
                .background(Color.White)
                .border(
                    width = if (error != null) 1.5.dp else 1.dp,
                    color = if (error != null) Color(0xFFEF4444) else Color(0xFFE5E5E5),
                    shape = Radius.shapeLg
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                cursorBrush = SolidColor(Color(0xFF111827)),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    innerTextField()
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(Color(0xFFE5E5E5))
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suffix,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * 44×24 track, green when on, 20dp white knob toggle.
 */
@Composable
fun JewelToggle(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (isOn) Color(0xFF22C55E) else Color(0xFFE5E7EB)

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable { onToggle(!isOn) }
            .padding(2.dp),
        contentAlignment = if (isOn) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Submission error banner.
 */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(Radius.shapeLg)
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFECACA), Radius.shapeLg)
            .padding(Spacing.base),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFEF4444), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "!",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Submission Error",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7F1D1D)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFB91C1C)
            )
        }
    }
}

/**
 * 40×40 circular spinner ring.
 */
@Composable
fun SpinnerRing(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "SpinnerRing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900)
        ),
        label = "rotation"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .size(40.dp)
            .rotate(rotation)
    ) {
        val strokeWidth = 1.5.dp.toPx()
        drawArc(
            color = Palette.taupe,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color.Black,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Two short fields on one row.
 */
@Composable
fun FieldPair(
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.weight(1f)) { leading() }
        Spacer(modifier = Modifier.width(Spacing.md))
        Box(modifier = Modifier.weight(1f)) { trailing() }
    }
}

/**
 * Shimmer effect brush for placeholder surfaces.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600)
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = listOf(
            Palette.taupe.copy(alpha = 0.6f),
            Palette.shimmerHighlight,
            Palette.taupe.copy(alpha = 0.6f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}
