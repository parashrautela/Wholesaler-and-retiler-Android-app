// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/TreasureChest/TreasureChestModels.swift and CreditsAPI.swift
package com.jewelindia.app.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

// MARK: - Credit Wallet

@Serializable
data class CreditWallet(
    val ok: Boolean = false,
    @SerialName("error") val errorCode: String? = null,
    val available: Int = 0,
    @SerialName("lifetime_granted") val lifetimeGranted: Int = 0,
    @SerialName("lifetime_spent") val lifetimeSpent: Int = 0,
    @SerialName("lifetime_expired") val lifetimeExpired: Int = 0,
    @SerialName("expiring_soon") val expiringSoon: Int = 0,
    @SerialName("next_expiry") val nextExpiry: String? = null,
    @SerialName("low_balance") val lowBalance: Boolean = false,
    @SerialName("low_balance_threshold") val lowBalanceThreshold: Int = 20,
    @SerialName("recovery_owed") val recoveryOwed: Int = 0,
    val mode: String? = null,
    @SerialName("policy_type") val policyType: String? = null,
    @SerialName("next_allowance") val nextAllowance: Int? = null,
    @SerialName("is_paused") val isPaused: Boolean = false,
    @SerialName("paid_available") val paidAvailable: Int? = null,
    @SerialName("gift_available") val giftAvailable: Int? = null,
    @SerialName("daily_available") val dailyAvailable: Int? = null,
    @SerialName("bonus_available") val bonusAvailable: Int? = null,
    @SerialName("daily_allowance") val dailyAllowance: Int? = null,
    @SerialName("resets_at") val resetsAt: String? = null,
    @SerialName("server_now") val serverNow: String? = null,
    @SerialName("shared_business_wallet") val sharedBusinessWallet: Boolean = false,
    @SerialName("legacy_preserved") val legacyPreserved: Int = 0
)

// MARK: - Daily Credit Schedule

data class DailyCreditSchedule(
    val allowance: Int,
    val resetsAt: Instant,
    val secondsAtReceipt: Double,
    val receivedAtNanos: Long = System.nanoTime()
) {
    companion object {
        operator fun invoke(wallet: CreditWallet, receivedAtNanos: Long = System.nanoTime()): DailyCreditSchedule? =
            create(wallet, receivedAtNanos)

        fun create(wallet: CreditWallet, receivedAtNanos: Long = System.nanoTime()): DailyCreditSchedule? {
            if (!wallet.ok || wallet.mode != "daily" || wallet.isPaused) return null
            val currentAllowance = wallet.dailyAllowance ?: return null
            if (currentAllowance <= 0) return null
            val resetsAtRaw = wallet.resetsAt ?: return null
            val serverNowRaw = wallet.serverNow ?: return null
            val reset = parseInstant(resetsAtRaw) ?: return null
            val serverNow = parseInstant(serverNowRaw) ?: return null
            val interval = Duration.between(serverNow, reset).toNanos().toDouble() / 1_000_000_000.0
            if (interval < 0.0 || interval > 86401.0) return null
            val allowance = wallet.nextAllowance ?: currentAllowance
            return DailyCreditSchedule(
                allowance = allowance,
                resetsAt = reset,
                secondsAtReceipt = interval,
                receivedAtNanos = receivedAtNanos
            )
        }

        private fun parseInstant(raw: String): Instant? {
            return try {
                OffsetDateTime.parse(raw).toInstant()
            } catch (_: Exception) {
                try {
                    Instant.parse(raw)
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    fun remainingSeconds(advancedBySeconds: Double = 0.0): Int {
        val nonNegativeElapsed = maxOf(0.0, advancedBySeconds)
        return ceil(maxOf(0.0, secondsAtReceipt - nonNegativeElapsed)).toInt()
    }

    fun remainingSecondsAtNanos(nowNanos: Long): Int {
        val elapsed = (nowNanos - receivedAtNanos).toDouble() / 1_000_000_000.0
        return remainingSeconds(elapsed)
    }

    fun countdown(advancedBySeconds: Double = 0.0): String {
        val remaining = remainingSeconds(advancedBySeconds)
        val hours = remaining / 3600
        val minutes = (remaining % 3600) / 60
        val seconds = remaining % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    val resetDescription: String
        get() {
            val kolkata = ZoneId.of("Asia/Kolkata")
            val zdt = resetsAt.atZone(kolkata)
            val formatter = DateTimeFormatter.ofPattern("EEE, d MMM 'at' h:mm a 'IST'", Locale.ENGLISH)
            return zdt.format(formatter)
        }
}

// MARK: - Credit Price (Rate Card)

@Serializable
data class CreditPrice(
    @SerialName("feature_key") val featureKey: String,
    val credits: Int,
    val label: String,
    val description: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
    @SerialName("is_active") val isActive: Boolean = true
) {
    val id: String get() = featureKey
}

// MARK: - Credit Ledger Entry

@Serializable
data class CreditLedgerEntry(
    val id: String,
    val delta: Int,
    val kind: String,
    @SerialName("feature_key") val featureKey: String? = null,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("balance_after") val balanceAfter: Int = 0,
    @SerialName("created_at") val createdAt: String
) {
    val displayTitle: String
        get() {
            if (referenceType == "invitation_funding") return if (kind == "refund") "Invitation gift refund" else "Invitation gift reserved"
            if (referenceType == "invitation_gift") return "Retailer invitation gift"
            if (referenceType == "purchased_carryover") return "Purchased credits preserved"
            if (referenceType == "referral_bonus") return "Verified referral reward"
            return when (kind) {
                "debit" -> when (featureKey) {
                    "chamak.generate" -> "Chamak Combine"
                    "chamak.generate_custom" -> "Chamak Combine (Custom Photos)"
                    "chamak.set_creation" -> "Set Creation"
                    "chamak.set_creation_custom" -> "Set Creation (Custom Photos)"
                    "chamak.reroll" -> "Try Again"
                    "product.upload" -> "Product Upload"
                    "product.reprocess" -> "Reprocess Product"
                    else -> featureKey?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Credit Spend"
                }
                "grant" -> when (referenceType) {
                    "purchase" -> "Credits purchased"
                    "daily" -> "Daily allowance"
                    "welcome" -> "Welcome gift"
                    "chamak_generation" -> "Refunded — generation failed"
                    else -> "Credits added"
                }
                "refund" -> "Refunded — generation failed"
                "expiry" -> "Credits expired"
                "adjustment" -> "Adjustment"
                else -> kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
}

// MARK: - Entitlements

@Serializable
data class EntitlementPurchase(
    val ok: Boolean = false,
    val error: String? = null,
    @SerialName("already_owned") val alreadyOwned: Boolean = false,
    val charged: Int = 0,
    @SerialName("short_by") val shortBy: Int? = null
) {
    val isInsufficientCredits: Boolean get() = error == "INSUFFICIENT_CREDITS"
}

// MARK: - Plans

@Serializable
data class Plan(
    val key: String,
    val label: String,
    @SerialName("period_days") val periodDays: Int,
    val perks: List<String> = emptyList()
) {
    val id: String get() = key
    val priceKey: String get() = "plan.$key"
}

@Serializable
data class PlanStatus(
    val active: Boolean = false,
    @SerialName("plan_key") val planKey: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("auto_renew") val autoRenew: Boolean = true,
    @SerialName("renewed_now") val renewedNow: Boolean = false,
    @SerialName("renewal_error") val renewalError: String? = null,
    @SerialName("temporary_access") val temporaryAccess: Boolean = false
)

@Serializable
data class PlanPurchase(
    val ok: Boolean = false,
    val error: String? = null,
    @SerialName("short_by") val shortBy: Int? = null
) {
    val isInsufficientCredits: Boolean get() = error == "INSUFFICIENT_CREDITS"
}

// MARK: - Top Up

class TopUpException(message: String) : RuntimeException(message)

@Serializable
data class TopUpPack(
    val key: String,
    val label: String,
    @SerialName("price_inr") val priceINR: Double,
    @SerialName("gst_inr") val gstINR: Double,
    @SerialName("total_inr") val totalINR: Double,
    val credits: Int
) {
    val id: String get() = key
}

@Serializable
data class TopUpOptions(
    val packs: List<TopUpPack> = emptyList(),
    @SerialName("credits_per_rupee") val creditsPerRupee: Double = 10.0,
    @SerialName("gst_percent") val gstPercent: Int = 18,
    val custom: CustomAmountRange? = null
) {
    @Serializable
    data class CustomAmountRange(
        @SerialName("min_inr") val minINR: Int,
        @SerialName("max_inr") val maxINR: Int
    )
}

object TopUpQuote {
    data class QuoteResult(
        val gstINR: Double,
        val totalINR: Double,
        val credits: Int
    )

    fun of(amountExGST: Int, gstPercent: Int, creditsPerRupee: Double): QuoteResult {
        val totalPaise = ((amountExGST * 100 * (100 + gstPercent)) / 100)
        val taxablePaise = ((totalPaise * 100.0) / (100.0 + gstPercent)).roundToInt()
        val credits = floor((taxablePaise.toDouble() * creditsPerRupee) / 100.0).toInt()
        val gstINR = (totalPaise - taxablePaise).toDouble() / 100.0
        val totalINR = totalPaise.toDouble() / 100.0
        return QuoteResult(gstINR, totalINR, credits)
    }
}

@Serializable
data class TopUpLink(
    @SerialName("link_id") val linkID: String,
    val url: String,
    val credits: Int,
    @SerialName("total_inr") val totalINR: Double
)

@Serializable
data class CreditPurchaseReceipt(
    val id: String,
    val credits: Int
)
