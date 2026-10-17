package com.jewelindia.app.feature.wholesaler

import androidx.lifecycle.ViewModel
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.util.launchSafely
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.data.api.WholesalerApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.supervisorScope

data class HomeData(
    val businessName: String = "",
    val productCount: Int = 0,
    val pendingOrders: Int = 0,
    val unreadChats: Int = 0,
    val uploadUsage: UploadUsage = UploadUsage.unknown,
    val activeOffersCount: Int = 0
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val data: HomeData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class WholesalerHomeViewModel(
    private val wholesalerUserId: String,
    private val userEmail: String? = null,
    private val api: WholesalerApi = WholesalerApi,
    private val mfgApi: ManufacturingApi = ManufacturingApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cachedWholesaler: Wholesaler? = null

    val wholesaler: Wholesaler?
        get() = cachedWholesaler

    fun load() {
        if (wholesalerUserId.isBlank()) {
            _uiState.value = HomeUiState.Error("Session missing. Please sign in again.")
            return
        }

        launchSafely(onError = {
            _uiState.value = HomeUiState.Error("Couldn't load your dashboard. Check your connection and try again.")
        }) {
            _uiState.value = HomeUiState.Loading

            // 1. Fetch wholesaler row by auth user ID & email
            val ws = try {
                api.fetchWholesaler(wholesalerUserId, userEmail)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            cachedWholesaler = ws

            if (ws?.hasVisitedDashboard != true) {
                try {
                    api.markDashboardVisited(wholesalerUserId)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Best effort
                }
            }

            // 2. Fetch parallel counts using wholesalerUserId (auth UID)
            supervisorScope {
                val pDef = async { runCatching { api.countProducts(wholesalerUserId) }.getOrDefault(0) }
                val oDef = async { runCatching { api.countPendingOrders(wholesalerUserId) }.getOrDefault(0) }
                val cDef = async { runCatching { api.countUnreadConversations(wholesalerUserId) }.getOrDefault(0) }
                val uDef = async {
                    runCatching { api.fetchUploadUsage(wholesalerUserId) }
                        .getOrDefault(UploadUsage.unknown)
                }
                val mDef = async { runCatching { mfgApi.fetchWholesalerOffers("active").size }.getOrDefault(0) }

                val data = HomeData(
                    businessName = ws?.displayName ?: ws?.businessName ?: "",
                    productCount = pDef.await(),
                    pendingOrders = oDef.await(),
                    unreadChats = cDef.await(),
                    uploadUsage = uDef.await(),
                    activeOffersCount = mDef.await()
                )

                _uiState.value = HomeUiState.Content(data)
            }
        }
    }
}
