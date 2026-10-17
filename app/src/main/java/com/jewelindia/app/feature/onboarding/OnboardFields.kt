// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardKit.swift (Fields & Buttons)
package com.jewelindia.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Text field with rounded 8dp corners, #F5F5F5 fill, error state, and keystroke filter.
 */
@Composable
fun OnboardTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    disabled: Boolean = false,
    filter: ((String) -> String)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnboardColor.text
        )

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = if (filter != null) filter(input) else input
                onValueChange(filtered)
            },
            placeholder = {
                Text(placeholder, fontSize = 15.sp, color = OnboardColor.subtle)
            },
            enabled = !disabled,
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = OnboardColor.fieldFill,
                unfocusedContainerColor = OnboardColor.fieldFill,
                disabledContainerColor = Color(0xFFF9FAFB),
                focusedBorderColor = if (error != null) OnboardColor.danger else Color.Black.copy(alpha = 0.15f),
                unfocusedBorderColor = if (error != null) OnboardColor.danger else Color.Transparent,
                errorBorderColor = OnboardColor.danger,
                focusedTextColor = OnboardColor.text,
                unfocusedTextColor = OnboardColor.text
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = OnboardColor.danger
            )
        }
    }
}

/**
 * Onboard primary CTA button matching iOS `OnboardPrimaryButton`.
 */
@Composable
fun OnboardPrimaryButton(
    title: String,
    busyTitle: String? = null,
    isBusy: Boolean = false,
    isEnabled: Boolean = true,
    action: () -> Unit
) {
    val bgColor = when {
        isBusy -> OnboardColor.submitting
        isEnabled -> Color.Black
        else -> OnboardColor.disabled
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(enabled = isEnabled && !isBusy) { action() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = if (isBusy) (busyTitle ?: title) else title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Legal confidentiality note matching iOS `OnboardLegalNote`.
 */
@Composable
fun OnboardLegalNote() {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "*Your documents are encrypted and only used for verification.",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "We never share them.",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
