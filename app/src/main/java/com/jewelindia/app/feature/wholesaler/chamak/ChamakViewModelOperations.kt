// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakViewModel.swift (Operations)
package com.jewelindia.app.feature.wholesaler.chamak

import com.jewelindia.app.core.model.ImageSize
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.ChamakApi
import com.jewelindia.app.data.api.ChamakAttempt
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.data.api.ChamakGeneration
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.net.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

suspend fun ChamakViewModel.load(wholesalerId: String) {
    isLoadingProducts = true
    try {
        supervisorScope {
            val productsDeferred = async {
                runCatching { fetchProducts(catalogueSource, wholesalerId) }
            }
            val galleryDeferred = async {
                runCatching { ChamakApi.fetchWholesalerGallery(wholesalerId) }
            }

            val productsResult = productsDeferred.await()
            val productsError = productsResult.exceptionOrNull()
            if (productsError is CancellationException) throw productsError
            catalogProducts = productsResult.getOrDefault(emptyList())
            catalogErrorMessage = if (productsError == null) null else "Couldn't load your designs. Check your connection and retry."

            val galleryResult = galleryDeferred.await()
            if (galleryResult.isSuccess) {
                galleryGenerations = galleryResult.getOrDefault(emptyList())
                galleryErrorMessage = null
                signGalleryThumbnails()
            } else {
                val err = galleryResult.exceptionOrNull()
                if (err is CancellationException) throw err
                galleryErrorMessage = "Couldn't load your gallery. Check your connection and try again."
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        catalogProducts = emptyList()
        galleryErrorMessage = "Couldn't load your gallery. Check your connection and try again."
    } finally {
        isLoadingProducts = false
    }
}

suspend fun ChamakViewModel.fetchProducts(
    source: ChamakCatalogueSource,
    wholesalerId: String
): List<Product> = when (source) {
    ChamakCatalogueSource.OwnProducts -> ChamakApi.fetchWholesalerProducts(wholesalerId)
    ChamakCatalogueSource.StoreDesigns -> ChamakApi.fetchStoreProducts()
}

suspend fun ChamakViewModel.refreshGallery(wholesalerId: String) {
    isRefreshingGallery = true
    try {
        galleryGenerations = ChamakApi.fetchWholesalerGallery(wholesalerId)
        galleryErrorMessage = null
        signGalleryThumbnails()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        galleryErrorMessage = "Couldn't load your gallery. Check your connection and try again."
    } finally {
        isRefreshingGallery = false
    }
}

suspend fun ChamakViewModel.signGalleryThumbnails() {
    val paths = galleryGenerations.flatMap { listOfNotNull(it.outputPath(ImageSize.CARD), it.outputImageURL) }
    val signed = ChamakApi.getSignedURLs(paths)
    val byId = mutableMapOf<String, String>()
    for (gen in galleryGenerations) {
        val path = gen.outputPath(ImageSize.CARD)
        if (path != null) {
            (signed[path] ?: signed[gen.outputImageURL])?.let { byId[gen.id] = it }
        }
    }
    galleryThumbnailURLs = byId
}

suspend fun ChamakViewModel.signOutputs(generation: ChamakGeneration) {
    selectedAttempt = null
    retrievalMessage = null
    // Try the original if its derivative cannot be signed. Thumbnail failure must not hide output.
    suspend fun resolve(primary: String?, original: String?): String? {
        for (path in listOfNotNull(primary, original).distinct()) {
            try { return ChamakApi.getSignedURL(path) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { }
        }
        return null
    }
    signedOutputImageURLs = generation.outputImages.mapNotNull {
        resolve(it.variants[ImageSize.DETAIL.value], it.path)
    }
    signedOriginalOutputImageURL = resolve(generation.outputImageURL, null)
    signedOutputImageURL = resolve(generation.outputPath(ImageSize.DETAIL), generation.outputImageURL)
        ?: signedOutputImageURLs.firstOrNull()
    signedFullOutputImageURL = resolve(generation.outputPath(ImageSize.FULL), generation.outputImageURL)
        ?: signedOutputImageURL
    if (signedOutputImageURL == null) {
        retrievalMessage = "Your generation is complete, but its image couldn't be loaded. Retry loading the output."
    }
    signAttemptThumbnails(generation)
}

suspend fun ChamakViewModel.signAttemptThumbnails(generation: ChamakGeneration) {
    val history = generation.outputHistory
    if (history.size <= 1) {
        attemptThumbnailURLs = emptyMap()
        return
    }
    val paths = history.map { it.path(ImageSize.CARD) }
    val signed = ChamakApi.getSignedURLs(paths)
    val byAttempt = mutableMapOf<Int, String>()
    for (attempt in history) {
        val path = attempt.path(ImageSize.CARD)
        signed[path]?.let { byAttempt[attempt.attempt] = it }
    }
    attemptThumbnailURLs = byAttempt
}

suspend fun ChamakViewModel.selectAttempt(attempt: ChamakAttempt) {
    val latest = currentGeneration?.outputHistory?.lastOrNull() ?: return
    try {
        val detail = ChamakApi.getSignedURL(attempt.path(ImageSize.DETAIL))
        val full = ChamakApi.getSignedURL(attempt.path(ImageSize.FULL))
        val original = ChamakApi.getSignedURL(attempt.path)
        selectedAttempt = if (attempt.attempt == latest.attempt) null else attempt.attempt
        signedOutputImageURL = detail
        signedFullOutputImageURL = full
        signedOriginalOutputImageURL = original
        retrievalMessage = null
    } catch (e: CancellationException) { throw e }
    catch (_: Exception) { retrievalMessage = "Couldn't load that attempt. Retry loading output or choose another attempt." }
}

suspend fun ChamakViewModel.submitFeedback() {
    val gen = currentGeneration ?: return
    try {
        ChamakApi.submitFeedback(
            generationId = gen.id,
            satisfied = feedbackSatisfied,
            whatWentWrong = feedbackText.ifEmpty { null }
        )
        feedbackSubmitted = true
        isShowingFeedbackSheet = false
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        errorMessage = e.userMessage()
    }
}

suspend fun ChamakViewModel.openGalleryItem(item: ChamakGeneration) = restoreGeneration(item.id)

suspend fun ChamakViewModel.restoreGeneration(id: String) {
    val editing = currentGeneration?.id == id && step in listOf(ChamakViewModel.Step.SliderForm, ChamakViewModel.Step.SetStyling)
    val updated = ChamakApi.fetchGeneration(id)
    currentGeneration = updated
    if (editing) return
    errorMessage = if (updated.status == ChamakStatus.Failed) updated.failureUserMessage else null
    retrievalMessage = null
    mode = updated.mode
    sliderValues = updated.wholesalerFormJSON?.sliderWeights ?: sliderValues
    updated.wholesalerFormJSON?.wildness?.let { wildness = it.toFloat() }
    updated.wholesalerFormJSON?.structure?.let { structure = it.toFloat() }
    updated.wholesalerFormJSON?.let { removeBackground = it.background == "transparent" }
    if (updated.noteText != null) {
        noteText = updated.noteText
        selectedStylingChips = emptySet()
    }
    selectedBackdrop = updated.setBackdrop ?: selectedBackdrop
    when (updated.status) {
        ChamakStatus.Done -> {
            pendingGenerateKey = null
            stopQuoteRotation()
            step = ChamakViewModel.Step.Result
            signOutputs(updated)
        }
        ChamakStatus.Failed -> {
            stopQuoteRotation()
            step = ChamakViewModel.Step.Failed
        }
        ChamakStatus.AwaitingInput -> {
            stopQuoteRotation()
            setupSlidersFromAnalysis(updated.stage1AnalysisJSON)
            step = if (mode == ChamakMode.SetCreation) ChamakViewModel.Step.SetStyling else ChamakViewModel.Step.SliderForm
        }
        ChamakStatus.Queued -> {
            if (mode == ChamakMode.SetCreation && updated.wholesalerFormJSON == null && pendingGenerateKey == null) {
                step = ChamakViewModel.Step.SetStyling
            } else {
                retrievalMessage = "This request is queued. Check status again, or review and retry the same request."
                step = ChamakViewModel.Step.Recoverable
            }
        }
        ChamakStatus.Analyzing, ChamakStatus.Generating -> {
            step = if (updated.status == ChamakStatus.Analyzing) ChamakViewModel.Step.Analyzing else ChamakViewModel.Step.Generating
            startQuoteRotation()
            startPolling(updated.id, if (updated.status == ChamakStatus.Analyzing) ChamakStatus.AwaitingInput else ChamakStatus.Done)
        }
    }
}
