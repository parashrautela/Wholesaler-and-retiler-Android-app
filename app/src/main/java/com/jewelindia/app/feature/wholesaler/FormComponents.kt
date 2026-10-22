// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/FormComponents.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

data class FormOption(val value: String, val label: String)

object WholesalerProductOptions {
    val types = listOf(
        FormOption("necklace", "Necklace"),
        FormOption("chain", "Chains"),
        FormOption("rings", "Rings"),
        FormOption("earrings", "Earrings"),
        FormOption("haram", "Haram"),
        FormOption("pendant", "Pendant"),
        FormOption("bangles", "Bangles"),
        FormOption("nosepins", "Nosepins"),
        FormOption("mangalsutra", "Mangalsutra")
    )

    val categories = listOf(
        FormOption("gold", "Gold"),
        FormOption("silver", "Silver"),
        FormOption("diamond", "Diamond"),
        FormOption("platinum", "Platinum"),
        FormOption("gemstone", "Gemstone"),
        FormOption("pearl", "Pearl")
    )

    val styles = listOf(
        FormOption("traditional", "Traditional"),
        FormOption("modern", "Modern"),
        FormOption("fusion", "Fusion"),
        FormOption("antique", "Antique"),
        FormOption("minimalist", "Minimalist"),
        FormOption("bridal", "Bridal")
    )

    val sizes = listOf(
        FormOption("xs", "XS"),
        FormOption("s", "S"),
        FormOption("m", "M"),
        FormOption("l", "L"),
        FormOption("xl", "XL"),
        FormOption("freesize", "Free Size")
    )

    val purities = listOf(
        FormOption("24k", "24K (999)"),
        FormOption("22k", "22K (916)"),
        FormOption("18k", "18K (750)"),
        FormOption("14k", "14K (585)"),
        FormOption("925", "925 Silver"),
        FormOption("950pt", "950 Platinum")
    )
}

/**
 * A black circle badge with the step number, plus the section title and blurb.
 */
@Composable
fun SectionHeader(
    number: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            Text(
                text = title,
                style = Typography.santoshi(20.sp, FontWeight.Bold),
                color = Color(0xFF111827)
            )
        }
        Text(
            text = subtitle,
            style = Typography.santoshi(13.sp),
            color = Color(0xFF6B7280),
            lineHeight = 18.sp
        )
    }
}

/**
 * Label + control + error text.
 */
@Composable
fun LabelledField(
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = Typography.santoshi(13.sp, FontWeight.SemiBold),
            color = Color(0xFF374151)
        )
        content()
        if (error != null) {
            Text(
                text = error,
                style = Typography.santoshi(12.sp, FontWeight.SemiBold),
                color = Color(0xFFEF4444)
            )
        }
    }
}

/**
 * The headless Select from iOS: 44dp height, white bg, 8dp radius,
 * 1dp border (or red if error), chevron down on right.
 */
@Composable
fun JewelSelect(
    label: String,
    options: List<FormOption>,
    selection: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "select",
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.value == selection }?.label

    LabelledField(label = label, error = error, modifier = modifier) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(
                        width = if (error != null) 1.5.dp else 1.dp,
                        color = if (error != null) Color(0xFFEF4444) else Color(0xFFE5E5E5),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedLabel ?: placeholder,
                    style = Typography.santoshi(14.sp, FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    val isSelected = option.value == selection
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.label,
                                    style = Typography.santoshi(
                                        14.sp,
                                        if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = Color(0xFF111827)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Palette.dark,
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
 * Numeric field with a grey unit cell on the trailing edge (44dp high, 8dp radius).
 */
@Composable
fun InputWithSuffix(
    label: String,
    suffix: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "0.00",
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal
) {
    LabelledField(label = label, error = error, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(
                    width = if (error != null) 1.5.dp else 1.dp,
                    color = if (error != null) Color(0xFFEF4444) else Color(0xFFE5E5E5),
                    shape = RoundedCornerShape(8.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = Typography.santoshi(14.sp),
                        color = Color(0xFF9CA3AF)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(
                        fontFamily = Typography.santoshi(14.sp).fontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF111827)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = true,
                    cursorBrush = SolidColor(Palette.dark),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFE5E5E5))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suffix,
                    style = Typography.santoshi(14.sp),
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * Standard iOS field chrome text input (44dp high, 8dp radius).
 */
@Composable
fun JewelTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(
                width = if (error != null) 1.5.dp else 1.dp,
                color = if (error != null) Color(0xFFEF4444) else Color(0xFFE5E5E5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = Typography.santoshi(14.sp),
                    color = Color(0xFF9CA3AF)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = Typography.santoshi(14.sp).fontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFF111827)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                cursorBrush = SolidColor(Palette.dark),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 44×24 track, green (#22C55E) when on, 20pt white knob.
 */
@Composable
fun JewelToggle(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (isOn) Color(0xFF22C55E) else Color(0xFFE5E7EB),
        animationSpec = tween(durationMillis = 200),
        label = "trackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (isOn) 22.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle(!isOn) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToPx(), 0) }
                .size(20.dp)
                .background(Color.White, CircleShape)
        )
    }
}

/**
 * Two short fields on one row with 12dp spacing.
 */
@Composable
fun FieldPair(
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.weight(1f)) { leading() }
        Box(modifier = Modifier.weight(1f)) { trailing() }
    }
}

/**
 * Submission error banner with red '!' badge.
 */
@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Submission Error",
                style = Typography.santoshi(14.sp, FontWeight.Medium),
                color = Color(0xFF7F1D1D)
            )
            Text(
                text = message,
                style = Typography.santoshi(14.sp),
                color = Color(0xFFB91C1C),
                lineHeight = 18.sp
            )
        }
    }
}
