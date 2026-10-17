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
import com.jewelindia.app.data.api.InsufficientCreditsException
import com.jewelindia.app.data.api.SetBackdrop
import com.jewelindia.app.data.api.WholesalerFormInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.UUID

suspend fun ChamakViewModel.load(wholesalerId: String) {
    isLoadingProducts = true
    try {
        coroutineScope {
            val productsDeferred = async { fetchProducts(catalogueSource, wholesalerId) }
            val galleryDeferred = async { ChamakApi.fetchWholesalerGallery(wholesalerId) }

            catalogProducts = try {
                productsDeferred.await()
            } catch (_: Exception) {
                emptyList()
            }

            try {
                galleryGenerations = galleryDeferred.await()
                galleryErrorMessage = null
                signGalleryThumbnails()
            } catch (e: Exception) {
                galleryGenerations = emptyList()
                galleryErrorMessage = "Couldn't load your gallery. Check your connection and try again."
            }
        }
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
    } catch (e: Exception) {
        if (e is CancellationException) return
        galleryErrorMessage = "Couldn't load your gallery. Check your connection and try again."
    } finally {
        isRefreshingGallery = false
    }
}

suspend fun ChamakViewModel.signGalleryThumbnails() {
    val paths = galleryGenerations.mapNotNull { it.outputPath(ImageSize.CARD) }
    val signed = ChamakApi.getSignedURLs(paths)
    val byId = mutableMapOf<String, String>()
    for (gen in galleryGenerations) {
        val path = gen.outputPath(ImageSize.CARD)
        if (path != null && signed.containsKey(path)) {
            byId[gen.id] = signed[path]!!
        }
    }
    galleryThumbnailURLs = byId
}

suspend fun ChamakViewModel.signOutputs(generation: ChamakGeneration) {
    selectedAttempt = null
    signAttemptThumbnails(generation)
    val outputPaths = generation.outputImages.map { it.variants[ImageSize.DETAIL.value] ?: it.path }
    if (outputPaths.isNotEmpty()) {
        val signed = ChamakApi.getSignedURLs(outputPaths)
        signedOutputImageURLs = outputPaths.mapNotNull { signed[it] }
    } else {
        signedOutputImageURLs = emptyList()
    }

    val detail = generation.outputPath(ImageSize.DETAIL)
    signedOutputImageURL = detail?.let { ChamakApi.getSignedURL(it) }

    val full = generation.outputPath(ImageSize.FULL)
    signedFullOutputImageURL = if (full != null && full != detail) {
        ChamakApi.getSignedURL(full)
    } else {
        signedOutputImageURL
    }
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
    selectedAttempt = if (attempt.attempt == latest.attempt) null else attempt.attempt
    val detail = attempt.path(ImageSize.DETAIL)
    signedOutputImageURL = ChamakApi.getSignedURL(detail)
    val full = attempt.path(ImageSize.FULL)
    signedFullOutputImageURL = if (full == detail) signedOutputImageURL else ChamakApi.getSignedURL(full)
}

suspend fun ChamakViewModel.regenerate(wholesalerId: String, onRefreshWallet: (suspend () -> Unit)? = null) {
    val gen = currentGeneration ?: return
    pendingGenerateKey = UUID.randomUUID().toString()

    step = ChamakViewModel.Step.Generating
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
            note = noteText.ifEmpty { null },
            idempotencyKey = pendingGenerateKey
        )
        startPolling(gen.id, ChamakStatus.Done, onRefreshWallet)
    } catch (err: InsufficientCreditsException) {
        insufficientCreditsError = err
        showInsufficientCreditsSheet = true
        step = ChamakViewModel.Step.SliderForm
        stopQuoteRotation()
    } catch (e: Exception) {
        errorMessage = e.message ?: "Regeneration failed."
        step = ChamakViewModel.Step.Failed
        stopQuoteRotation()
    }
}

suspend fun ChamakViewModel.regenerateSet(wholesalerId: String, onRefreshWallet: (suspend () -> Unit)? = null) {
    val gen = currentGeneration ?: return
    pendingGenerateKey = UUID.randomUUID().toString()

    step = ChamakViewModel.Step.Generating
    startQuoteRotation()

    try {
        ChamakApi.submitSetAndGenerate(
            generationId = gen.id,
            wholesalerId = wholesalerId,
            backdrop = selectedBackdrop,
            note = noteText.ifEmpty { null },
            idempotencyKey = pendingGenerateKey
        )
        startPolling(gen.id, ChamakStatus.Done, onRefreshWallet)
    } catch (err: InsufficientCreditsException) {
        insufficientCreditsError = err
        showInsufficientCreditsSheet = true
        step = ChamakViewModel.Step.SetStyling
        stopQuoteRotation()
    } catch (e: Exception) {
        errorMessage = e.message ?: "Regeneration failed."
        step = ChamakViewModel.Step.Failed
        stopQuoteRotation()
    }
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
    } catch (e: Exception) {
        errorMessage = e.message
    }
}

suspend fun ChamakViewModel.openGalleryItem(item: ChamakGeneration) {
    currentGeneration = item
    errorMessage = if (item.status == ChamakStatus.Failed) item.failureUserMessage else null
    mode = item.mode
    sliderValues = item.wholesalerFormJSON?.sliderWeights ?: emptyMap()
    noteText = item.noteText ?: ""
    selectedBackdrop = item.setBackdrop ?: SetBackdrop.VelvetBust
    selectedStylingChips = emptySet()
    pendingGenerateKey = null
    signOutputs(item)
    step = if (item.status == ChamakStatus.Failed) ChamakViewModel.Step.Failed else ChamakViewModel.Step.Result
}
