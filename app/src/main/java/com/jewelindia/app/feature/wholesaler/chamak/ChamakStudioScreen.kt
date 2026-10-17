// Facade and Preview definitions for Chamak Studio
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.design.JewelTheme
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.api.Stage1Analysis

@Composable
fun ChamakStudioScreen(
    wholesalerUserId: String = "",
    wholesaler: Wholesaler? = null,
    sessionStore: SessionStore? = null,
    catalogueSource: ChamakCatalogueSource = ChamakCatalogueSource.OwnProducts,
    onClose: (() -> Unit)? = null
) {
    val resolvedUserId = wholesalerUserId.ifBlank {
        sessionStore?.user?.value?.id ?: ""
    }
    if (resolvedUserId.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please sign in to access Chamak Studio.")
        }
    } else {
        ChamakHubScreen(
            wholesalerId = resolvedUserId,
            catalogueSource = catalogueSource,
            onClose = onClose
        )
    }
}

// MARK: - Previews for Chamak Screens

@Preview(showBackground = true)
@Composable
fun ChamakHubScreenPreview() {
    JewelTheme {
        ChamakHubScreen(
            wholesalerId = "test-wholesaler",
            catalogueSource = ChamakCatalogueSource.OwnProducts
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakCatalogPickerScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel()
        ChamakCatalogPickerScreen(
            vm = vm,
            wholesalerId = "test-wholesaler"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakSliderFormScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel().apply {
            currentGeneration = ChamakGeneration(
                id = "gen-1",
                wholesalerId = "wholesaler-1",
                sourceImage1URL = "https://example.com/1.jpg",
                sourceImage2URL = "https://example.com/2.jpg",
                createdAt = "2026-10-17T12:00:00Z",
                status = ChamakStatus.AwaitingInput,
                stage1AnalysisJSON = Stage1Analysis(
                    image1Strengths = listOf("Floral motif", "Gold filigree"),
                    image2Strengths = listOf("Emerald centerpiece", "Matte finish")
                )
            )
        }
        ChamakSliderFormScreen(
            vm = vm,
            wholesalerId = "test-wholesaler"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakSetStylingScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel().apply {
            mode = ChamakMode.SetCreation
        }
        ChamakSetStylingScreen(
            vm = vm,
            wholesalerId = "test-wholesaler"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakGeneratingScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel().apply {
            step = ChamakViewModel.Step.Generating
        }
        ChamakGeneratingScreen(vm = vm)
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakResultScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel().apply {
            currentGeneration = ChamakGeneration(
                id = "gen-1",
                wholesalerId = "wholesaler-1",
                sourceImage1URL = "https://example.com/1.jpg",
                sourceImage2URL = "https://example.com/2.jpg",
                createdAt = "2026-10-17T12:00:00Z",
                status = ChamakStatus.Done,
                outputImageURL = "https://example.com/output.png"
            )
            signedOutputImageURL = "https://example.com/output.png"
            step = ChamakViewModel.Step.Result
        }
        ChamakResultScreen(
            vm = vm,
            wholesalerId = "test-wholesaler"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChamakGalleryScreenPreview() {
    JewelTheme {
        val vm = ChamakViewModel().apply {
            galleryGenerations = listOf(
                ChamakGeneration(
                    id = "gen-1",
                    wholesalerId = "wholesaler-1",
                    sourceImage1URL = "https://example.com/1.jpg",
                    sourceImage2URL = "https://example.com/2.jpg",
                    createdAt = "2026-10-17T12:00:00Z",
                    status = ChamakStatus.Done,
                    outputImageURL = "https://example.com/output.png"
                ),
                ChamakGeneration(
                    id = "gen-2",
                    wholesalerId = "wholesaler-1",
                    sourceImage1URL = "https://example.com/3.jpg",
                    sourceImage2URL = "https://example.com/4.jpg",
                    createdAt = "2026-10-17T11:00:00Z",
                    status = ChamakStatus.Done,
                    mode = ChamakMode.SetCreation,
                    setBackdrop = SetBackdrop.VelvetBust,
                    outputImageURL = "https://example.com/output2.png"
                )
            )
        }
        ChamakGalleryScreen(
            vm = vm,
            wholesalerId = "test-wholesaler"
        )
    }
}
