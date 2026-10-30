package com.jewelindia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.jewelindia.app.data.api.ChamakAttempt
import com.jewelindia.app.data.api.ChamakAttemptSettings
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.Stage1Analysis
import com.jewelindia.app.design.JewelTheme
import com.jewelindia.app.feature.wholesaler.AddProductScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakResultScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewModel

/**
 * Opens a screen with sample data and no account, for checking flows on an emulator:
 * adb shell am start -n com.jewelindia.app/.DebugPeekActivity --es peek uploaded
 *
 * Device photos are served by `python3 -m http.server 8765` from a folder holding
 * raw/peek/chamak_1_a.jpg and chamak_2_b.jpg, after `adb reverse tcp:8765 tcp:8765`.
 */
class DebugPeekActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val peek = intent.getStringExtra("peek") ?: "uploaded"
        setContent {
            JewelTheme {
                if (peek == "add") {
                    AddProductScreen(userId = "peek")
                } else {
                    val vm = remember { sampleResult(peek) }
                    ChamakResultScreen(vm = vm, wholesalerId = "peek")
                }
            }
        }
    }

    private fun sampleResult(peek: String): ChamakViewModel {
        val device = "http://localhost:8765/raw/peek"
        val catalogue = "http://localhost:8765/products/processed/x_v1.jpg"
        val (first, second, status) = when (peek) {
            "mixed" -> Triple("$device/chamak_1_a.jpg", catalogue, ChamakStatus.Done)
            "catalogue" -> Triple(catalogue, catalogue, ChamakStatus.Done)
            "broken" -> Triple("$device/chamak_1_gone.jpg", "$device/chamak_2_b.jpg", ChamakStatus.Done)
            "failed" -> Triple("$device/chamak_1_a.jpg", "$device/chamak_2_b.jpg", ChamakStatus.Failed)
            else -> Triple("$device/chamak_1_a.jpg", "$device/chamak_2_b.jpg", ChamakStatus.Done)
        }
        return ChamakViewModel().apply {
            currentGeneration = ChamakGeneration(
                id = "gen-peek",
                wholesalerId = "peek",
                sourceImage1URL = first,
                sourceImage2URL = second,
                createdAt = "2026-10-30T10:30:00Z",
                status = status,
                outputImageURL = "$device/chamak_1_a.jpg",
                stage1AnalysisJSON = Stage1Analysis(image1Type = "long_necklace", image2Type = "jhumka"),
                outputHistory = listOf(
                    ChamakAttempt(
                        attempt = 1,
                        path = "peek/attempt1.png",
                        settings = ChamakAttemptSettings(
                            mix = listOf(ChamakAttemptSettings.Mix("Flower pattern", 70)),
                            wildness = 5, structure = 2, background = "kept", note = "Make the pendant bigger"
                        )
                    )
                )
            )
            signedOutputImageURL = "$device/chamak_1_a.jpg"
            step = if (status == ChamakStatus.Failed) ChamakViewModel.Step.Failed else ChamakViewModel.Step.Result
        }
    }
}
