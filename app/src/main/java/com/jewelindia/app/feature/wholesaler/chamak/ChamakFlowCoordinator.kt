// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakFlowCoordinator.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.imePadding
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.delay

@Composable
fun ChamakFlowCoordinator(
    wholesalerId: String,
    vm: ChamakViewModel,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val requestAIConsent = rememberAIConsentRequest(wholesalerId)
    BackHandler(enabled = onClose != null) {
        when (vm.step) {
            ChamakViewModel.Step.SliderForm, ChamakViewModel.Step.SetStyling,
            ChamakViewModel.Step.Result, ChamakViewModel.Step.Failed -> vm.resetToPicker()
            ChamakViewModel.Step.Gallery -> vm.step = ChamakViewModel.Step.CatalogPicker
            else -> onClose?.invoke()
        }
    }
    LifecycleStartEffect(vm) {
        vm.resumeFlow()
        onStopOrDispose { vm.pauseFlow() }
    }
    LaunchedEffect(wholesalerId) { vm.load(wholesalerId) }

    // Dismiss toast after 3 seconds
    LaunchedEffect(vm.showToast) {
        if (vm.showToast) {
            delay(3000)
            vm.showToast = false
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            if (vm.step in listOf(ChamakViewModel.Step.Analyzing, ChamakViewModel.Step.Generating, ChamakViewModel.Step.Recoverable) && onClose != null) {
                TextButton(onClick = onClose) { Text("Back to Studio") }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (vm.step) {
                    ChamakViewModel.Step.CatalogPicker -> {
                        ChamakCatalogPickerScreen(
                            vm = vm,
                            wholesalerId = wholesalerId,
                            onClose = onClose
                        )
                    }
                    ChamakViewModel.Step.Analyzing, ChamakViewModel.Step.Generating -> {
                        ChamakGeneratingScreen(vm = vm)
                    }
                    ChamakViewModel.Step.SliderForm -> {
                        ChamakSliderFormScreen(
                            vm = vm,
                            wholesalerId = wholesalerId
                        )
                    }
                    ChamakViewModel.Step.SetStyling -> {
                        ChamakSetStylingScreen(
                            vm = vm,
                            wholesalerId = wholesalerId
                        )
                    }
                    ChamakViewModel.Step.Result, ChamakViewModel.Step.Failed -> {
                        ChamakResultScreen(
                            vm = vm,
                            wholesalerId = wholesalerId
                        )
                    }
                    ChamakViewModel.Step.Recoverable -> {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(vm.retrievalMessage ?: vm.errorMessage ?: "This request may still be processing.")
                            Button(onClick = { vm.resumeFlow() }, enabled = !vm.isSubmitting) {
                                Text("Check status")
                            }
                            TextButton(onClick = {
                                if (vm.currentGeneration == null) vm.resetToPicker()
                                else requestAIConsent { vm.retryPendingRequest(wholesalerId) }
                            }, enabled = !vm.isSubmitting) {
                                Text(if (vm.currentGeneration == null) "Choose designs again" else "Retry same request")
                            }
                        }
                    }
                    ChamakViewModel.Step.Gallery -> {
                        ChamakGalleryScreen(
                            vm = vm,
                            wholesalerId = wholesalerId
                        )
                    }
                }

                // Toast message overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = vm.showToast && vm.toastMessage != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.15f))
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, Palette.border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFBB8651),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = vm.toastMessage ?: "",
                            fontFamily = Manrope,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
    if (vm.showInsufficientCreditsSheet) {
        AlertDialog(
            onDismissRequest = {
                vm.showInsufficientCreditsSheet = false
                vm.insufficientCreditsError = null
            },
            title = { Text("Not enough credits") },
            text = {
                Text(vm.insufficientCreditsError?.let {
                    "This action requires ${it.required} credits. Your balance is ${it.balance}; you need ${it.shortBy} more."
                } ?: "Your credit balance is too low for this action. Check your balance in Treasure Chest.")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.showInsufficientCreditsSheet = false
                    vm.insufficientCreditsError = null
                }) { Text("OK") }
            }
        )
    }

}
