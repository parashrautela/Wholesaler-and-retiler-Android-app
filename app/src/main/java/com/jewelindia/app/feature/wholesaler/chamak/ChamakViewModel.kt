// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakViewModel.swift
package com.jewelindia.app.feature.wholesaler.chamak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakAttempt
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakDesignItem
import com.jewelindia.app.data.api.ChamakException
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakQuote
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.InsufficientCreditsException
import com.jewelindia.app.data.api.JewelleryTypeCanonical
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.api.SetCreationOutput
import com.jewelindia.app.data.api.SetStylingChip
import com.jewelindia.app.data.api.Stage1Analysis
import com.jewelindia.app.data.api.WeightedAttribute
import com.jewelindia.app.data.api.WholesalerFormInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class ChamakViewModel(
    private val scopeOverride: CoroutineScope? = null,
    savedStateHandle: SavedStateHandle? = null
) : ViewModel() {
    private val state = ChamakState(savedStateHandle)
    private val scope get() = scopeOverride ?: viewModelScope
    private var actionJob: Job? = null
    private var flowVisible = true
    var activeLaunch by mutableStateOf<ChamakLaunch?>(
        if (state.flag("active") == true) {
            ChamakLaunch(ChamakMode.entries.firstOrNull { it.name == state.string("mode") } ?: ChamakMode.Fusion)
        } else null
    )
        private set

    fun beginFlow(mode: ChamakMode, generation: ChamakGeneration? = null) {
        if (actionJob?.isActive == true) {
            state["active"] = true
            activeLaunch = ChamakLaunch(this.mode, currentGeneration)
            return
        }
        resetToPicker()
        this.mode = generation?.mode ?: mode
        currentGeneration = generation
        state["active"] = true
        state["mode"] = this.mode.name
        activeLaunch = ChamakLaunch(this.mode, generation)
    }

    fun closeFlow() {
        pauseFlow()
        state["active"] = false
        activeLaunch = null
    }

    fun runAction(action: suspend () -> Unit) {
        if (actionJob?.isActive == true) return
        actionJob = scope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                stopQuoteRotation()
                retrievalMessage = "Couldn't confirm this request. Check its status or retry the same request."
                step = Step.Recoverable
            }
        }
    }

    fun pauseFlow() {
        flowVisible = false
        stopPolling()
        stopQuoteRotation()
    }

    fun resumeFlow() {
        flowVisible = true
        if (actionJob?.isActive == true) return
        val id = currentGeneration?.id ?: state.string("generationId")
            ?: if (state.flag("creationAttempted") == true) state.string("pendingGenerationId") else null
        if (id == null) return
        runAction { restoreGeneration(id) }
    }

    enum class Step {
        CatalogPicker,
        Analyzing,
        SliderForm,
        SetStyling,
        Generating,
        Result,
        Failed,
        Recoverable,
        Gallery
    }

    var step by mutableStateOf(Step.CatalogPicker)
    var mode by mutableStateOf(activeLaunch?.mode ?: ChamakMode.Fusion)
    var catalogProducts by mutableStateOf<List<Product>>(emptyList())
    var catalogueSource by mutableStateOf(ChamakCatalogueSource.OwnProducts)
    var galleryGenerations by mutableStateOf<List<ChamakGeneration>>(emptyList())
    var galleryThumbnailURLs by mutableStateOf<Map<String, String>>(emptyMap())

    var selectedDesign1 by mutableStateOf<ChamakDesignItem?>(null)
    var selectedDesign2 by mutableStateOf<ChamakDesignItem?>(null)
    var selectedDesign3 by mutableStateOf<ChamakDesignItem?>(null)
    var selectedDesign4 by mutableStateOf<ChamakDesignItem?>(null)

    val setPieces: List<ChamakDesignItem>
        get() {
            val all = listOfNotNull(selectedDesign1, selectedDesign2) +
                if (mode == ChamakMode.SetCreation) listOfNotNull(selectedDesign3, selectedDesign4) else emptyList()
            return all
        }

    val setPriceKey: String
        get() {
            val gen = currentGeneration
            val count = if (gen?.mode == ChamakMode.SetCreation) {
                listOfNotNull(gen.sourceImage1URL, gen.sourceImage2URL, gen.sourceImage3URL, gen.sourceImage4URL).count { it.isNotBlank() }
            } else setPieces.size
            val pieces = maxOf(2, count)
            return if (pieces == 2) "chamak.set_creation" else "chamak.set_creation_$pieces"
        }

    private var generation by mutableStateOf<ChamakGeneration?>(null)
    var currentGeneration: ChamakGeneration?
        get() = generation
        set(value) {
            generation = value
            state["generationId"] = value?.id
            if (value != null) state["mode"] = value.mode.name
        }
    var signedOriginalOutputImageURL by mutableStateOf<String?>(null)
    var signedOutputImageURL by mutableStateOf<String?>(null)
    var signedFullOutputImageURL by mutableStateOf<String?>(null)
    var signedOutputImageURLs by mutableStateOf<List<String>>(emptyList())
    var attemptThumbnailURLs by mutableStateOf<Map<Int, String>>(emptyMap())
    var selectedAttempt by mutableStateOf<Int?>(null)

    val exportImagePath: String?
        get() {
            val generation = currentGeneration ?: return null
            val attemptId = selectedAttempt
            if (attemptId != null) {
                val attempt = generation.outputHistory.firstOrNull { it.attempt == attemptId }
                if (attempt != null) return attempt.path
            }
            return generation.outputImageURL
        }

    var sliderValues by mutableStateOf<Map<String, Double>>(emptyMap())
    var noteText by mutableStateOf("")
    var selectedBackdrop by mutableStateOf(SetBackdrop.VelvetBust)
    var selectedStylingChips by mutableStateOf<Set<SetStylingChip>>(emptySet())

    val composedSetNote: String?
        get() {
            val chipNotes = SetStylingChip.all
                .filter { selectedStylingChips.contains(it) }
                .map { it.instruction }
            val manual = noteText.trim()
            val notes = chipNotes + if (manual.isEmpty()) emptyList() else listOf(manual)
            return if (notes.isEmpty()) null else notes.joinToString(" ")
        }

    fun toggleStylingChip(chip: SetStylingChip) {
        selectedStylingChips = if (selectedStylingChips.contains(chip)) {
            selectedStylingChips - chip
        } else {
            selectedStylingChips + chip
        }
    }

    var pendingGenerateKey: String?
        get() = state.string("submissionKey")
        set(value) { state["submissionKey"] = value }
    private var pendingGenerationId: String
        get() = state.string("pendingGenerationId") ?: UUID.randomUUID().toString().also { state["pendingGenerationId"] = it }
        set(value) { state["pendingGenerationId"] = value }
    var analysisKey: String?
        get() = state.string("analysisKey")
        set(value) { state["analysisKey"] = value }
    var retrievalMessage by mutableStateOf<String?>(null)
    var catalogErrorMessage by mutableStateOf<String?>(null)
    var insufficientCreditsError by mutableStateOf<InsufficientCreditsException?>(null)
    var showInsufficientCreditsSheet by mutableStateOf(false)
    var toastMessage by mutableStateOf<String?>(null)
    var showToast by mutableStateOf(false)

    var isLoadingProducts by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var galleryErrorMessage by mutableStateOf<String?>(null)
    var isRefreshingGallery by mutableStateOf(false)

    var activeQuoteIndex by mutableIntStateOf(0)
    private var quoteJob: Job? = null
    private var pollJob: Job? = null

    var isShowingFeedbackSheet by mutableStateOf(false)
    var feedbackSatisfied by mutableStateOf(true)
    var feedbackText by mutableStateOf("")
    var feedbackSubmitted by mutableStateOf(false)



    // MARK: - Selection

    typealias SelectionBlockReason = ChamakSelectionBlockReason


    fun selectionBlockReason(product: Product): SelectionBlockReason? {
        if (slot(product) != null) return null

        val url = product.displaySource ?: ""
        if (url.isEmpty()) return ChamakSelectionBlockReason.MissingImage

        if (mode != ChamakMode.SetCreation) return null

        val cType = JewelleryTypeCanonical.canonicalize(product.jewelleryType)
            ?: return ChamakSelectionBlockReason.MissingType

        val existing = setPieces.firstOrNull { it.canonicalJewelleryType == cType }
        if (existing != null) {
            val label = JewelleryTypeCanonical.displayLabel(cType)
            return ChamakSelectionBlockReason.SameTypeSelected(label)
        }

        if (setPieces.size >= slotCount) {
            return ChamakSelectionBlockReason.SetFull
        }
        return null
    }

    fun slot(product: Product): Int? {
        val slots = listOf(selectedDesign1, selectedDesign2, selectedDesign3, selectedDesign4)
        val index = slots.indexOfFirst { it?.product?.id == product.id }
        return if (index != -1) index + 1 else null
    }

    fun design(inSlot: Int): ChamakDesignItem? = when (inSlot) {
        1 -> selectedDesign1
        2 -> selectedDesign2
        3 -> selectedDesign3
        else -> selectedDesign4
    }

    fun clearSlot(slot: Int) {
        when (slot) {
            1 -> selectedDesign1 = null
            2 -> selectedDesign2 = null
            3 -> selectedDesign3 = null
            else -> selectedDesign4 = null
        }
        if (mode == ChamakMode.SetCreation) {
            compactSlots()
        }
    }

    private fun compactSlots() {
        if (mode != ChamakMode.SetCreation) return
        val active = setPieces
        selectedDesign1 = active.getOrNull(0)
        selectedDesign2 = active.getOrNull(1)
        selectedDesign3 = active.getOrNull(2)
        selectedDesign4 = active.getOrNull(3)
    }

    val slotCount: Int get() = if (mode == ChamakMode.SetCreation) 4 else 2

    fun selectProduct(product: Product) {
        val currentSlot = slot(product)
        if (currentSlot != null && currentSlot <= slotCount) {
            clearSlot(currentSlot)
            return
        }

        val blocked = selectionBlockReason(product)
        if (blocked != null) { errorMessage = blocked.message; return }

        if (mode == ChamakMode.SetCreation) {
            val reason = selectionBlockReason(product)
            if (reason != null) {
                errorMessage = reason.message
                return
            }
            val target = (1..slotCount).firstOrNull { design(it) == null }
            if (target == null) {
                errorMessage = "Remove a piece to add another."
                return
            }
            place(ChamakDesignItem.from(product), target)
            compactSlots()
            errorMessage = null
        } else {
            val target = (1..slotCount).firstOrNull { design(it) == null } ?: slotCount
            place(ChamakDesignItem.from(product), target)
        }
    }

    fun setCustomImage(data: ByteArray, slot: Int, declaredJewelleryType: String? = null) {
        if (mode == ChamakMode.SetCreation && declaredJewelleryType != null) {
            val cType = JewelleryTypeCanonical.canonicalize(declaredJewelleryType)
            if (cType != null && setPieces.any { it.canonicalJewelleryType == cType }) {
                val label = JewelleryTypeCanonical.displayLabel(cType)
                errorMessage = "A ${label.lowercase()} is already in your set. Remove it to choose another."
                return
            }
        }
        place(ChamakDesignItem.from(data, slot, declaredJewelleryType), slot)
        if (mode == ChamakMode.SetCreation) {
            compactSlots()
        }
    }

    private fun place(item: ChamakDesignItem, slot: Int) {
        when (slot) {
            1 -> selectedDesign1 = item
            2 -> selectedDesign2 = item
            3 -> selectedDesign3 = item
            else -> selectedDesign4 = item
        }
    }

    val canStartAnalysis: Boolean
        get() {
            if (mode == ChamakMode.SetCreation) {
                val pieces = setPieces
                if (pieces.size !in 2..4) return false
                if (!pieces.all { it.hasImage }) return false
                if (pieces.map { it.id }.toSet().size != pieces.size) return false
                val hashes = pieces.mapNotNull { it.contentHash }
                if (hashes.toSet().size != hashes.size) return false
                val types = pieces.mapNotNull { it.canonicalJewelleryType }
                if (types.size != pieces.size) return false
                return types.toSet().size == pieces.size
            } else {
                if (selectedDesign1 == null || selectedDesign2 == null) return false
                val pieces = setPieces
                if (!pieces.all { it.hasImage }) return false
                if (pieces.map { it.id }.toSet().size != pieces.size) return false
                val hashes = pieces.mapNotNull { it.contentHash }
                return hashes.toSet().size == hashes.size
            }
        }

    // MARK: - Stage 1 Vision Analysis / Row Creation

    suspend fun startVisionAnalysis(wholesalerId: String) {
        val d1 = selectedDesign1 ?: return
        val d2 = selectedDesign2 ?: return

        if (!canStartAnalysis || isSubmitting) return
        isSubmitting = true
        errorMessage = null
        retrievalMessage = null
        step = Step.Analyzing
        startQuoteRotation()

        try {
            var url1 = d1.imageURL ?: ""
            val local1 = d1.localImageData
            if (url1.isEmpty() && local1 != null) {
                url1 = ChamakApi.uploadSourceImage(wholesalerId, local1, 1, mode)
                selectedDesign1?.imageURL = url1
            }

            var url2 = d2.imageURL ?: ""
            val local2 = d2.localImageData
            if (url2.isEmpty() && local2 != null) {
                url2 = ChamakApi.uploadSourceImage(wholesalerId, local2, 2, mode)
                selectedDesign2?.imageURL = url2
            }

            if (url1.isEmpty() || url2.isEmpty()) {
                throw ChamakException("Both designs must have valid uploaded images.")
            }

            val extraURLs = mutableListOf<String>()
            if (mode == ChamakMode.SetCreation) {
                for (slot in listOf(3, 4)) {
                    val piece = design(slot) ?: continue
                    var url = piece.imageURL ?: ""
                    val pieceData = piece.localImageData
                    if (url.isEmpty() && pieceData != null) {
                        url = ChamakApi.uploadSourceImage(wholesalerId, pieceData, slot, mode)
                        if (slot == 3) selectedDesign3?.imageURL = url else selectedDesign4?.imageURL = url
                    }
                    if (url.isEmpty()) {
                        throw ChamakException("Every piece needs a valid uploaded image.")
                    }
                    extraURLs.add(url)
                }
            }

            state["creationAttempted"] = true
            val gen = currentGeneration ?: ChamakApi.createGeneration(
                wholesalerId = wholesalerId,
                source1URL = url1,
                source2URL = url2,
                extraSourceURLs = extraURLs,
                mode = mode,
                generationId = pendingGenerationId
            )
            currentGeneration = gen
            if (gen.status !in listOf(ChamakStatus.Queued, ChamakStatus.Failed)) {
                restoreGeneration(gen.id)
                return
            }

            if (mode == ChamakMode.SetCreation) {
                stopQuoteRotation()
                step = Step.SetStyling
            } else {
                if (analysisKey == null) analysisKey = UUID.randomUUID().toString()
                ChamakApi.triggerStage1Analysis(gen.id, wholesalerId, analysisKey)
                startPolling(gen.id, ChamakStatus.AwaitingInput)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (err: InsufficientCreditsException) {
            pendingGenerateKey = null
            analysisKey = null
            insufficientCreditsError = err
            showInsufficientCreditsSheet = true
            step = Step.CatalogPicker
            stopQuoteRotation()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to start analysis."
            step = if (currentGeneration == null) Step.Failed else Step.Recoverable
            stopQuoteRotation()
        } finally {
            isSubmitting = false
        }
    }

    // MARK: - Stage 3 & 4: Submit Form & Generate

    internal fun buildAttributeContext(): List<WeightedAttribute> {
        val attributes = currentGeneration?.stage1AnalysisJSON?.dynamicAttributes ?: emptyList()
        return attributes.map { attr ->
            WeightedAttribute(
                id = attr.id,
                attribute = attr.name,
                image1Feature = attr.source1Feature,
                image2Feature = attr.source2Feature,
                weight = sliderValues[attr.id] ?: attr.defaultValue
            )
        }
    }

    suspend fun submitFormAndGenerate(wholesalerId: String, onRefreshWallet: (suspend () -> Unit)? = null) {
        if (isSubmitting) return
        val gen = currentGeneration ?: return
        if (pendingGenerateKey == null) {
            pendingGenerateKey = UUID.randomUUID().toString()
        }

        isSubmitting = true
        step = Step.Generating
        startQuoteRotation()

        val formInput = WholesalerFormInput(
            sliderWeights = sliderValues,
            attributeContext = buildAttributeContext(),
            note = noteText.ifEmpty { null }
        )

        try {
            ChamakApi.submitFormAndGenerate(
                generationId = gen.id,
                wholesalerId = wholesalerId,
                formInput = formInput,
                note = composedSetNote,
                idempotencyKey = pendingGenerateKey
            )
            startPolling(gen.id, ChamakStatus.Done, onRefreshWallet)
        } catch (e: CancellationException) {
            throw e
        } catch (err: InsufficientCreditsException) {
            pendingGenerateKey = null
            analysisKey = null
            insufficientCreditsError = err
            showInsufficientCreditsSheet = true
            step = Step.SliderForm
            stopQuoteRotation()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Submission could not be confirmed. Check status before retrying."
            step = Step.Recoverable
            stopQuoteRotation()
        } finally {
            isSubmitting = false
        }
    }

    suspend fun submitSetAndGenerate(wholesalerId: String, onRefreshWallet: (suspend () -> Unit)? = null) {
        if (isSubmitting) return
        val gen = currentGeneration ?: return
        if (pendingGenerateKey == null) {
            pendingGenerateKey = UUID.randomUUID().toString()
        }

        isSubmitting = true
        step = Step.Generating
        startQuoteRotation()

        try {
            ChamakApi.submitSetAndGenerate(
                generationId = gen.id,
                wholesalerId = wholesalerId,
                backdrop = selectedBackdrop,
                note = composedSetNote,
                idempotencyKey = pendingGenerateKey
            )
            startPolling(gen.id, ChamakStatus.Done, onRefreshWallet)
        } catch (e: CancellationException) {
            throw e
        } catch (err: InsufficientCreditsException) {
            pendingGenerateKey = null
            analysisKey = null
            insufficientCreditsError = err
            showInsufficientCreditsSheet = true
            step = Step.SetStyling
            stopQuoteRotation()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Submission could not be confirmed. Check status before retrying."
            step = Step.Recoverable
            stopQuoteRotation()
        } finally {
            isSubmitting = false
        }
    }

    fun retryPendingRequest(wholesalerId: String) {
        val gen = currentGeneration ?: return
        runAction {
            if (mode == ChamakMode.Fusion && gen.stage1AnalysisJSON == null && pendingGenerateKey == null) {
                if (analysisKey == null) analysisKey = UUID.randomUUID().toString()
                step = Step.Analyzing
                startQuoteRotation()
                try {
                    ChamakApi.triggerStage1Analysis(gen.id, wholesalerId, analysisKey)
                    startPolling(gen.id, ChamakStatus.AwaitingInput)
                } catch (e: CancellationException) { throw e }
                catch (e: InsufficientCreditsException) {
                    analysisKey = null
                    insufficientCreditsError = e
                    showInsufficientCreditsSheet = true
                    step = Step.CatalogPicker
                    stopQuoteRotation()
                }
            } else if (pendingGenerateKey != null) {
                if (mode == ChamakMode.SetCreation) submitSetAndGenerate(wholesalerId)
                else submitFormAndGenerate(wholesalerId)
            } else {
                reviseAndRetry()
            }
        }
    }

    fun reviseAndRetry() {
        if (currentGeneration?.status in listOf(ChamakStatus.Done, ChamakStatus.Failed)) {
            pendingGenerateKey = null
            analysisKey = null
        }
        retrievalMessage = null
        errorMessage = null
        stopQuoteRotation()
        step = if (mode == ChamakMode.SetCreation) {
            if (currentGeneration != null) Step.SetStyling else Step.CatalogPicker
        } else {
            if (currentGeneration?.stage1AnalysisJSON != null) Step.SliderForm else Step.CatalogPicker
        }
    }



    // MARK: - Polling Engine

    internal fun startPolling(
        generationId: String,
        targetStatus: ChamakStatus,
        onRefreshWallet: (suspend () -> Unit)? = null
    ) {
        pollJob?.cancel()
        if (!flowVisible) return
        retrievalMessage = null
        pollJob = scope.launch {
            var attempts = 0
            var consecutiveFailures = 0

            while (attempts < 80) {
                delay(3000)
                if (!isActive) return@launch
                attempts++

                try {
                    val updated = ChamakApi.fetchGeneration(generationId)
                    currentGeneration = updated

                    if (updated.status == ChamakStatus.Done || updated.status == targetStatus) {
                        if (updated.status == ChamakStatus.AwaitingInput) {
                            stopQuoteRotation()
                            setupSlidersFromAnalysis(updated.stage1AnalysisJSON)
                            step = Step.SliderForm
                        } else if (updated.status == ChamakStatus.Done) {
                            pendingGenerateKey = null
                            stopQuoteRotation()
                            step = Step.Result
                            signOutputs(updated)

                            if (onRefreshWallet != null) {
                                try {
                                    onRefreshWallet()
                                    toastMessage = "Design generation complete"
                                    showToast = true
                                } catch (e: CancellationException) { throw e } catch (_: Exception) {}
                            }
                        }
                        return@launch
                    } else if (updated.status == ChamakStatus.Failed) {
                        stopQuoteRotation()
                        errorMessage = updated.failureUserMessage
                        step = Step.Failed
                        return@launch
                    }
                    consecutiveFailures = 0
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    consecutiveFailures++
                }
            }

            if (!isActive) return@launch
            stopQuoteRotation()
            retrievalMessage = if (consecutiveFailures >= 5) {
                "Couldn't read this generation's status. It may still have completed — check your gallery."
            } else {
                "Generation is taking longer than expected. Please check your gallery shortly."
            }
            step = Step.Recoverable
        }
    }

    internal fun setupSlidersFromAnalysis(analysis: Stage1Analysis?) {
        if (analysis == null) return
        val currentSliders = sliderValues.toMutableMap()
        for (attr in analysis.dynamicAttributes) {
            if (!currentSliders.containsKey(attr.id)) {
                currentSliders[attr.id] = attr.defaultValue
            }
        }
        sliderValues = currentSliders
    }

    // MARK: - Quote Rotation

    internal fun startQuoteRotation() {
        quoteJob?.cancel()
        if (!flowVisible) return
        activeQuoteIndex = 0
        quoteJob = scope.launch {
            while (isActive) {
                delay(4000)
                activeQuoteIndex = (activeQuoteIndex + 1) % ChamakQuote.quotes.size
            }
        }
    }

    internal fun stopQuoteRotation() {
        quoteJob?.cancel()
        quoteJob = null
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }



    // MARK: - Navigation Helpers

    fun resetToPicker() {
        stopQuoteRotation()
        stopPolling()
        selectedDesign1 = null
        selectedDesign2 = null
        selectedDesign3 = null
        selectedDesign4 = null
        currentGeneration = null
        pendingGenerateKey = null
        state["creationAttempted"] = false
        pendingGenerationId = UUID.randomUUID().toString()
        analysisKey = null
        retrievalMessage = null
        insufficientCreditsError = null
        showInsufficientCreditsSheet = false
        signedOriginalOutputImageURL = null
        signedOutputImageURL = null
        signedFullOutputImageURL = null
        signedOutputImageURLs = emptyList()
        attemptThumbnailURLs = emptyMap()
        selectedAttempt = null
        sliderValues = emptyMap()
        noteText = ""
        selectedBackdrop = SetBackdrop.VelvetBust
        selectedStylingChips = emptySet()
        errorMessage = null
        step = Step.CatalogPicker
    }


}

// Fixture models need only memory; production supplies the owner's restorable handle.
private class ChamakState(private val saved: SavedStateHandle?) {
    private val memory = mutableMapOf<String, Any?>()

    fun string(key: String): String? = saved?.get<String>(key) ?: memory[key] as? String
    fun flag(key: String): Boolean? = saved?.get<Boolean>(key) ?: memory[key] as? Boolean

    operator fun set(key: String, value: Any?) {
        if (saved != null) saved[key] = value else memory[key] = value
    }
}
