package com.jewelindia.app.feature.wholesaler.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.CatalogueShare
import com.jewelindia.app.data.api.CatalogueShareApi
import com.jewelindia.app.data.api.CatalogueShareService
import com.jewelindia.app.data.net.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

enum class ShareDurationOption(val minutes: Int, val label: String) {
    ONE_HOUR(60, Copy.CatalogueShare.validity1h),
    TWENTY_FOUR_HOURS(1440, Copy.CatalogueShare.validity24h),
    SEVEN_DAYS(10080, Copy.CatalogueShare.validity7d),
    CUSTOM(0, Copy.CatalogueShare.validityCustom);
}

class CatalogueShareViewModel(
    val selectedProducts: List<Product> = emptyList(),
    private val api: CatalogueShareService = CatalogueShareApi,
    initialCreatedShare: CatalogueShare? = null
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _viewers = MutableStateFlow(1)
    val viewers: StateFlow<Int> = _viewers.asStateFlow()

    private val _durationOption = MutableStateFlow(ShareDurationOption.TWENTY_FOUR_HOURS)
    val durationOption: StateFlow<ShareDurationOption> = _durationOption.asStateFlow()

    private val _customMinutes = MutableStateFlow<Int?>(30)
    val customMinutes: StateFlow<Int?> = _customMinutes.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _creationError = MutableStateFlow<String?>(null)
    val creationError: StateFlow<String?> = _creationError.asStateFlow()

    private val _createdShare = MutableStateFlow<CatalogueShare?>(initialCreatedShare)
    val createdShare: StateFlow<CatalogueShare?> = _createdShare.asStateFlow()

    private val _hasCreatedLink = MutableStateFlow(false)
    val hasCreatedLink: StateFlow<Boolean> = _hasCreatedLink.asStateFlow()

    private val _shares = MutableStateFlow<List<CatalogueShare>>(emptyList())
    val shares: StateFlow<List<CatalogueShare>> = _shares.asStateFlow()

    private val _isLoadingShares = MutableStateFlow(false)
    val isLoadingShares: StateFlow<Boolean> = _isLoadingShares.asStateFlow()

    private val _sharesLoadError = MutableStateFlow(false)
    val sharesLoadError: StateFlow<Boolean> = _sharesLoadError.asStateFlow()

    private val _revokingShareId = MutableStateFlow<String?>(null)
    val revokingShareId: StateFlow<String?> = _revokingShareId.asStateFlow()

    private val _actionNotice = MutableStateFlow<String?>(null)
    val actionNotice: StateFlow<String?> = _actionNotice.asStateFlow()

    init {
        loadShares()
    }

    fun setTitle(value: String) {
        _title.value = value.take(80)
    }

    fun setViewers(value: Int) {
        _viewers.value = value.coerceIn(1, 100)
    }

    fun incrementViewers() {
        _viewers.value = (_viewers.value + 1).coerceAtMost(100)
    }

    fun decrementViewers() {
        _viewers.value = (_viewers.value - 1).coerceAtLeast(1)
    }

    fun setDurationOption(option: ShareDurationOption) {
        _durationOption.value = option
    }

    fun setCustomMinutes(minutes: Int?) {
        _customMinutes.value = minutes
    }

    fun clearActionNotice() {
        _actionNotice.value = null
    }

    fun setActionNotice(message: String) {
        _actionNotice.value = message
    }

    val isDurationValid: Boolean
        get() {
            return when (_durationOption.value) {
                ShareDurationOption.CUSTOM -> {
                    val m = _customMinutes.value ?: return false
                    m in 5..10080
                }
                else -> true
            }
        }

    val isCreateEnabled: Boolean
        get() {
            if (_isCreating.value) return false
            if (selectedProducts.isEmpty() || selectedProducts.size > 50) return false
            return isDurationValid
        }

    fun createShare() {
        if (!isCreateEnabled) return
        val productIds = selectedProducts.map { it.id }
        val durationMinutes = when (_durationOption.value) {
            ShareDurationOption.ONE_HOUR -> 60
            ShareDurationOption.TWENTY_FOUR_HOURS -> 1440
            ShareDurationOption.SEVEN_DAYS -> 10080
            ShareDurationOption.CUSTOM -> _customMinutes.value ?: 1440
        }

        val requestTitle = _title.value.trim().takeIf { it.isNotBlank() }

        _isCreating.value = true
        _creationError.value = null

        viewModelScope.launch {
            try {
                val newShare = api.createShare(
                    productIds = productIds,
                    maxViewers = _viewers.value,
                    durationMinutes = durationMinutes,
                    title = requestTitle
                )
                _createdShare.value = newShare
                _hasCreatedLink.value = true
                loadShares()
            } catch (e: Exception) {
                _creationError.value = e.userMessage()
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun loadShares() {
        _isLoadingShares.value = true
        _sharesLoadError.value = false
        viewModelScope.launch {
            try {
                val fetched = api.fetchShares()
                _shares.value = fetched
                _sharesLoadError.value = false
            } catch (_: Exception) {
                _sharesLoadError.value = true
            } finally {
                _isLoadingShares.value = false
            }
        }
    }

    fun revokeShare(share: CatalogueShare) {
        if (_revokingShareId.value != null) return
        _revokingShareId.value = share.id
        viewModelScope.launch {
            try {
                api.revokeShare(share.id)
                _actionNotice.value = Copy.CatalogueShare.revokeDone
                if (_createdShare.value?.id == share.id) {
                    _createdShare.value = _createdShare.value?.copy(revokedAt = Instant.now().toString())
                }
                loadShares()
            } catch (e: Exception) {
                _actionNotice.value = e.userMessage()
            } finally {
                _revokingShareId.value = null
            }
        }
    }
}
