// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakFlowCoordinator.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import kotlinx.coroutines.delay

@Composable
fun ChamakFlowCoordinator(
    wholesalerId: String,
    mode: ChamakMode = ChamakMode.Fusion,
    openingGeneration: ChamakGeneration? = null,
    catalogueSource: ChamakCatalogueSource = ChamakCatalogueSource.OwnProducts,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val vm = remember {
        ChamakViewModel().apply {
            this.catalogueSource = catalogueSource
            this.mode = openingGeneration?.mode ?: mode
            if (openingGeneration != null) {
                this.currentGeneration = openingGeneration
                this.errorMessage = if (openingGeneration.status == ChamakStatus.Failed) {
                    openingGeneration.failureUserMessage
                } else null
                this.step = if (openingGeneration.status == ChamakStatus.Failed) {
                    ChamakViewModel.Step.Failed
                } else {
                    ChamakViewModel.Step.Result
                }
            }
        }
    }

    LaunchedEffect(wholesalerId) {
        if (openingGeneration != null && (vm.step == ChamakViewModel.Step.Result || vm.step == ChamakViewModel.Step.Failed)) {
            vm.openGalleryItem(openingGeneration)
        }
        vm.load(wholesalerId)
    }

    // Dismiss toast after 3 seconds
    LaunchedEffect(vm.showToast) {
        if (vm.showToast) {
            delay(3000)
            vm.showToast = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
            ChamakViewModel.Step.Gallery -> {
                ChamakGalleryScreen(
                    vm = vm,
                    wholesalerId = wholesalerId
                )
            }
        }

        // Toast message overlay
        AnimatedVisibility(
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
