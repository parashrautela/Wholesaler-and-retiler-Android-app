// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardSteps.swift
package com.jewelindia.app.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.OnboardingFeeApi
import kotlinx.coroutines.launch

@Composable
fun OnboardStep1Screen(
    flow: OnboardFlow,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    OnboardLayout(
        step = 1,
        heading = "Tell us about yourself",
        subheading = "We need your legal name and Aadhar card to verify your identity.",
        showsBack = true,
        onBack = onBack,
        onSignOut = onSignOut
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OnboardTextField(
                label = "Full Name*",
                placeholder = "As printed on your Aadhar card",
                value = flow.name,
                onValueChange = { flow.name = it },
                error = flow.nameError,
                filter = OnboardFlow::filterName
            )

            OnboardTextField(
                label = "Aadhar Number*",
                placeholder = "12-digit Aadhar number",
                value = flow.aadhar,
                onValueChange = { flow.aadhar = OnboardFlow.formatAadhar(it) },
                error = flow.aadharError,
                keyboardType = KeyboardType.Number,
                capitalization = KeyboardCapitalization.None
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ImageUploadBox(
                        label = "Aadhar Front*",
                        file = flow.frontImage,
                        onFileSelected = { flow.frontImage = it },
                        error = flow.frontError
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ImageUploadBox(
                        label = "Aadhar Back*",
                        file = flow.backImage,
                        onFileSelected = { flow.backImage = it },
                        error = flow.backError
                    )
                }
            }

            OnboardLegalNote()

            OnboardPrimaryButton(
                title = "Next: Business Details",
                isEnabled = true,
                action = {
                    flow.submitAttempted = true
                    if (flow.step1Valid) {
                        flow.submitAttempted = false
                        onNext()
                    }
                }
            )
        }
    }
}

@Composable
fun OnboardStep2Screen(
    flow: OnboardFlow,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    var stateMenuExpanded by remember { mutableStateOf(false) }

    OnboardLayout(
        step = 2,
        heading = "Tell us about your business",
        subheading = "This information will be displayed on your profile.",
        showsBack = true,
        onBack = onBack,
        onSignOut = onSignOut
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OnboardTextField(
                label = "Business Name*",
                placeholder = "Your registered business or company name",
                value = flow.businessName,
                onValueChange = { flow.businessName = it },
                error = flow.businessNameError
            )

            // State dropdown
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "State*",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnboardColor.text
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = flow.selectedState.ifEmpty { "Select state" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                modifier = Modifier.clickable { stateMenuExpanded = true }
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = OnboardColor.fieldFill,
                            unfocusedContainerColor = OnboardColor.fieldFill,
                            focusedBorderColor = if (flow.stateError != null) OnboardColor.danger else Color.Black.copy(alpha = 0.15f),
                            unfocusedBorderColor = if (flow.stateError != null) OnboardColor.danger else Color.Transparent,
                            errorBorderColor = OnboardColor.danger,
                            focusedTextColor = if (flow.selectedState.isEmpty()) OnboardColor.subtle else OnboardColor.text,
                            unfocusedTextColor = if (flow.selectedState.isEmpty()) OnboardColor.subtle else OnboardColor.text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { stateMenuExpanded = true }
                    )

                    DropdownMenu(
                        expanded = stateMenuExpanded,
                        onDismissRequest = { stateMenuExpanded = false }
                    ) {
                        OnboardFlow.states.forEach { stateName ->
                            DropdownMenuItem(
                                text = { Text(stateName) },
                                onClick = {
                                    flow.selectedState = stateName
                                    stateMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (flow.stateError != null) {
                    Text(
                        text = flow.stateError!!,
                        fontSize = 12.sp,
                        color = OnboardColor.danger
                    )
                }
            }

            OnboardTextField(
                label = "City*",
                placeholder = "e.g. Mumbai, Surat, Jaipur",
                value = flow.selectedCity,
                onValueChange = { flow.selectedCity = it },
                error = flow.cityError
            )

            ImageUploadBox(
                label = "Business Logo*",
                file = flow.logoImage,
                onFileSelected = { flow.logoImage = it },
                error = flow.logoError,
                contentScale = ContentScale.Fit
            )

            OnboardPrimaryButton(
                title = "Next: Documents",
                isEnabled = true,
                action = {
                    flow.submitAttempted = true
                    if (flow.step2Valid) {
                        flow.submitAttempted = false
                        onNext()
                    }
                }
            )
        }
    }
}

@Composable
fun OnboardStep3Screen(
    flow: OnboardFlow,
    session: SessionStore,
    onCompleted: () -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var feeAmount by remember { mutableStateOf<Int?>(null) }
    var showFeeSheet by remember { mutableStateOf(false) }

    fun doSubmit() {
        val user = session.user.value ?: return
        scope.launch {
            val success = flow.submit(user)
            if (success) {
                onCompleted()
            }
        }
    }

    fun submitOrPay() {
        flow.submitAttempted = true
        if (!flow.step3Valid) return

        scope.launch {
            flow.isSubmitting = true
            flow.submitError = null
            try {
                val feeStatus = OnboardingFeeApi.status()
                if (feeStatus.mustPay) {
                    feeAmount = feeStatus.amountINR
                    showFeeSheet = true
                    flow.isSubmitting = false
                } else {
                    doSubmit()
                }
            } catch (_: Exception) {
                // If fee check fails or is bypassed, attempt normal submit
                doSubmit()
            }
        }
    }

    OnboardLayout(
        step = 3,
        heading = "Upload your business documents",
        subheading = "These documents are required for GST and tax compliance.",
        showsBack = true,
        onBack = onBack,
        onSignOut = onSignOut
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ImageUploadBox(
                label = "PAN Card*",
                file = flow.panFile,
                onFileSelected = { flow.panFile = it },
                error = flow.panError,
                allowsPDF = true
            )

            ImageUploadBox(
                label = "GST Certificate*",
                file = flow.gstFile,
                onFileSelected = { flow.gstFile = it },
                error = flow.gstError,
                allowsPDF = true
            )

            OnboardLegalNote()

            if (flow.submitError != null) {
                Text(
                    text = flow.submitError!!,
                    fontSize = 13.sp,
                    color = OnboardColor.danger
                )
            }

            OnboardPrimaryButton(
                title = "Submit Application",
                busyTitle = "Submitting application...",
                isBusy = flow.isSubmitting,
                isEnabled = !flow.isSubmitting,
                action = { submitOrPay() }
            )
        }
    }

    if (showFeeSheet && feeAmount != null) {
        OnboardingFeeSheet(
            amountInr = feeAmount!!,
            onDismiss = { showFeeSheet = false },
            onPaid = {
                showFeeSheet = false
                doSubmit()
            }
        )
    }
}
