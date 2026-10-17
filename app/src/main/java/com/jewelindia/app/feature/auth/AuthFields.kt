// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/AuthKit.swift (Fields)
package com.jewelindia.app.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Typography

@Composable
fun AuthFieldLabel(
    text: String,
    size: TextUnit = 13.sp,
    color: Color = AuthColor.labelDark
) {
    Text(
        text = text,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = color,
        fontFamily = Typography.Manrope,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    fill: Color = AuthColor.fieldFill,
    idleBorder: Color = AuthColor.fieldBorder,
    focusBorder: Color = AuthColor.ink,
    borderWidth: Dp = 1.5.dp,
    cornerRadius: Dp = 8.dp,
    fontSize: TextUnit = 14.sp,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(
            fontSize = fontSize,
            fontFamily = Typography.Manrope,
            color = AuthColor.ink
        ),
        cursorBrush = SolidColor(AuthColor.ink),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(fill, RoundedCornerShape(cornerRadius))
            .border(
                borderWidth,
                if (isFocused) focusBorder else idleBorder,
                RoundedCornerShape(cornerRadius)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 14.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = fontSize,
                        fontFamily = Typography.Manrope,
                        color = AuthColor.placeholder
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun AuthSecureField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    fontSize: TextUnit = 15.sp,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true
) {
    var revealed by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions = keyboardActions,
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        textStyle = TextStyle(
            fontSize = fontSize,
            fontFamily = Typography.Manrope,
            color = AuthColor.text2
        ),
        cursorBrush = SolidColor(AuthColor.focusBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(
                1.5.dp,
                if (isFocused) AuthColor.focusBorder else AuthColor.border2,
                RoundedCornerShape(8.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .padding(start = 12.dp, end = 4.dp),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.weight(1f)
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = fontSize,
                            fontFamily = Typography.Manrope,
                            color = AuthColor.placeholder
                        )
                    }
                    innerTextField()
                }

                IconButton(
                    onClick = { revealed = !revealed },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Show password",
                        tint = AuthColor.placeholder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun AuthErrorRow(
    message: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 5.dp,
    dotTopPadding: Dp = 5.dp,
    fontSize: TextUnit = 12.sp
) {
    AnimatedVisibility(
        visible = message.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = dotTopPadding)
                    .size(dotSize)
                    .background(AuthColor.errorDot, CircleShape)
            )
            Text(
                text = message,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                fontFamily = Typography.Manrope,
                color = AuthColor.errorText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
