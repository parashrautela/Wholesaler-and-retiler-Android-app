// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/VerifyOTPView.swift (boxes)
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.Typography

@Composable
fun OtpBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isWrong: Boolean,
    modifier: Modifier = Modifier,
    count: Int = Copy.otpDigitCount,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onComplete: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
            }
    ) {
        // Hidden BasicTextField that receives all input, clipboard pastes, and autofill
        BasicTextField(
            value = value,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }.take(count)
                onValueChange(digitsOnly)
                if (digitsOnly.length == count) {
                    onComplete?.invoke()
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .size(1.dp)
                .alpha(0f)
                .focusRequester(focusRequester)
        )

        // Visual presentation: 8 digit boxes with hyphen between 4th and 5th
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 0 until count) {
                if (i == 4) {
                    Text(
                        text = "-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Light,
                        color = AuthColor.otpBorder,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }

                val char = value.getOrNull(i)?.toString() ?: ""
                val isCurrent = value.length == i

                val borderColor = when {
                    isWrong -> AuthColor.errorText
                    isCurrent -> AuthColor.focusBorder
                    else -> AuthColor.otpBorder
                }

                val textColor = if (isWrong) AuthColor.errorText else AuthColor.text2

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(32.dp)
                        .height(40.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = char,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = Typography.Manrope,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
