package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.ContentFlag
import com.jewelindia.app.data.api.Stage1Analysis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChamakStepMachineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStepAndMode() {
        val vm = ChamakViewModel(testScope)
        assertEquals(ChamakViewModel.Step.CatalogPicker, vm.step)
        assertEquals(ChamakMode.Fusion, vm.mode)
    }

    @Test
    fun testResetKeepsMode() {
        val vm = ChamakViewModel(testScope)
        vm.mode = ChamakMode.SetCreation
        vm.step = ChamakViewModel.Step.SetStyling
        vm.noteText = "Some notes"

        vm.resetToPicker()

        assertEquals(ChamakViewModel.Step.CatalogPicker, vm.step)
        assertEquals(ChamakMode.SetCreation, vm.mode)
        assertEquals("", vm.noteText)
        assertNull(vm.selectedDesign1)
        assertNull(vm.selectedDesign2)
        assertNull(vm.currentGeneration)
    }

    @Test
    fun testReviseAndRetryBranchesByMode() {
        val vm = ChamakViewModel(testScope)

        // Fusion mode: returns to SliderForm if stage1 analysis exists, else CatalogPicker
        vm.mode = ChamakMode.Fusion
        vm.step = ChamakViewModel.Step.Failed
        vm.currentGeneration = ChamakGeneration(
            id = "gen-1",
            wholesalerId = "w-1",
            sourceImage1URL = "url1",
            sourceImage2URL = "url2",
            createdAt = "2026-10-17T00:00:00Z",
            stage1AnalysisJSON = Stage1Analysis(image1Strengths = listOf("Motif"))
        )
        vm.reviseAndRetry()
        assertEquals(ChamakViewModel.Step.SliderForm, vm.step)

        // SetCreation mode: returns to SetStyling if currentGeneration exists
        vm.mode = ChamakMode.SetCreation
        vm.step = ChamakViewModel.Step.Result
        vm.reviseAndRetry()
        assertEquals(ChamakViewModel.Step.SetStyling, vm.step)
    }

    @Test
    fun testFailureMessageFromServer() {
        // 1. Rejected not jewelry
        val rejected = ChamakGeneration(
            id = "g-1",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            contentFlagHit = ContentFlag.NotJewelry
        )
        assertEquals(
            "Jewellery was not detected in one or both selected images. Please select two clear jewellery photos to continue.",
            rejected.failureUserMessage
        )

        // 2. Blurry image in legacy stage 1 analysis
        val blurry = ChamakGeneration(
            id = "g-2",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            stage1AnalysisJSON = Stage1Analysis(contentFlag = ContentFlag.TooUnclearToAssess)
        )
        assertTrue(blurry.failureUserMessage.contains("blurry"))

        // 3. Unknown failure
        val unknown = ChamakGeneration(
            id = "g-3",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z"
        )
        assertTrue(unknown.failureUserMessage.contains("try again"))

        // 4. ContentFlag.Ok falls back to unknown failure message
        val okFlag = ChamakGeneration(
            id = "g-4",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            contentFlagHit = ContentFlag.Ok
        )
        assertEquals(unknown.failureUserMessage, okFlag.failureUserMessage)

        // 5. Server error_message priority
        val serverError = ChamakGeneration(
            id = "g-5",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            errorMessage = "GPU out of memory during diffusion rendering."
        )
        assertEquals("GPU out of memory during diffusion rendering.", serverError.failureUserMessage)
    }

    @Test
    fun testOpenGalleryItemFailedStatusSetsStepFailed() = runTest(testDispatcher) {
        val vm = ChamakViewModel(testScope)
        val failedGen = ChamakGeneration(
            id = "g-failed",
            wholesalerId = "w-1",
            sourceImage1URL = "u1",
            sourceImage2URL = "u2",
            createdAt = "2026-10-17T00:00:00Z",
            status = ChamakStatus.Failed,
            contentFlagHit = ContentFlag.NotJewelry
        )

        vm.openGalleryItem(failedGen)

        assertEquals(ChamakViewModel.Step.Failed, vm.step)
        assertEquals(failedGen.failureUserMessage, vm.errorMessage)
    }
}
