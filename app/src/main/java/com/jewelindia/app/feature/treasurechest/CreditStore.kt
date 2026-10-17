// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/CreditStore.swift
package com.jewelindia.app.feature.treasurechest

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jewelindia.app.data.api.CreditPrice
import com.jewelindia.app.data.api.CreditWallet
import com.jewelindia.app.data.api.CreditsApi
import com.jewelindia.app.data.api.Plan
import com.jewelindia.app.data.api.PlanStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WalletState {
    data object Loading : WalletState
    data class Available(val wallet: CreditWallet) : WalletState
    data object Unavailable : WalletState
}

class CreditStore(private val scope: CoroutineScope) {
    private val _walletState = MutableStateFlow<WalletState>(WalletState.Loading)
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _rateCard = MutableStateFlow<List<CreditPrice>>(emptyList())
    val rateCard: StateFlow<List<CreditPrice>> = _rateCard.asStateFlow()

    private val _myPlan = MutableStateFlow<PlanStatus?>(null)
    val myPlan: StateFlow<PlanStatus?> = _myPlan.asStateFlow()

    private val _plans = MutableStateFlow<List<Plan>>(emptyList())
    val plans: StateFlow<List<Plan>> = _plans.asStateFlow()

    private val _entitlements = MutableStateFlow<Set<String>>(emptySet())
    val entitlements: StateFlow<Set<String>> = _entitlements.asStateFlow()

    val availableCredits: Int?
        get() = (_walletState.value as? WalletState.Available)?.wallet?.available

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            try {
                val wallet = CreditsApi.fetchWallet()
                _walletState.value = WalletState.Available(wallet)
            } catch (_: Exception) {
                if (_walletState.value is WalletState.Loading) {
                    _walletState.value = WalletState.Unavailable
                }
            }

            try {
                _rateCard.value = CreditsApi.fetchRateCard()
            } catch (_: Exception) {}

            try {
                _entitlements.value = CreditsApi.fetchEntitlementKeys()
            } catch (_: Exception) {}
        }
    }

    fun refreshPlan() {
        scope.launch(Dispatchers.IO) {
            try {
                _myPlan.value = CreditsApi.fetchMyPlan()
                _plans.value = CreditsApi.fetchPlans()
            } catch (_: Exception) {}
        }
    }

    fun maintainDailyWallet() {
        refresh()
    }

    fun hasEntitlement(key: String): Boolean {
        return _entitlements.value.contains(key.lowercase())
    }
}
