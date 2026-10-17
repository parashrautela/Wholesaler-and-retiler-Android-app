// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/ManufacturingModels.swift
package com.jewelindia.app.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MakingBudgetMode(val rawValue: String, val title: String, val unitLabel: String) {
    @SerialName("per_gram") perGram("per_gram", "Per Gram (₹/g)", "₹/g"),
    @SerialName("fixed_total") fixedTotal("fixed_total", "Fixed Total (₹)", "₹"),
    @SerialName("percentage") percentage("percentage", "Percentage (%)", "%");
}

@Serializable
enum class ManufacturingRequestState(val title: String) {
    @SerialName("routing") routing("Routing to Wholesalers"),
    @SerialName("collecting") collecting("Collecting Quotes"),
    @SerialName("reviewing") reviewing("Compare Quotes"),
    @SerialName("assigned") assigned("Supplier Selected"),
    @SerialName("exhausted") exhausted("No Suppliers Available"),
    @SerialName("cancelled") cancelled("Cancelled");
}

@Serializable
enum class ManufacturingOfferStatus(val title: String) {
    @SerialName("active") active("Active"),
    @SerialName("open") `open`("Open"),
    @SerialName("quoted") quoted("Quoted"),
    @SerialName("not_selected") notSelected("Not Selected"),
    @SerialName("accepted") accepted("Accepted"),
    @SerialName("declined") declined("Declined"),
    @SerialName("expired") expired("Expired"),
    @SerialName("cancelled") cancelled("Cancelled"),
    @SerialName("skipped") skipped("Skipped");
}

@Serializable
data class WholesalerBusinessSummary(
    val id: String,
    @SerialName("business_name") val businessName: String? = null,
    val city: String? = null,
    val state: String? = null
)

@Serializable
data class ManufacturingOfferSummary(
    val id: String,
    val rank: Int,
    @SerialName("offered_at") val offeredAt: String,
    @SerialName("expires_at") val expiresAt: String,
    val status: String
)

@Serializable
data class ManufacturingQuote(
    val id: String,
    @SerialName("offer_id") val offerId: String,
    @SerialName("request_id") val requestId: String,
    @SerialName("wholesaler_id") val wholesalerId: String,
    @SerialName("making_charge_mode") val makingChargeMode: String,
    @SerialName("making_charge_amount") val makingChargeAmount: Double,
    @SerialName("metal_estimate_amount") val metalEstimateAmount: Double? = null,
    @SerialName("gemstone_estimate_amount") val gemstoneEstimateAmount: Double? = null,
    @SerialName("other_estimate_amount") val otherEstimateAmount: Double? = null,
    @SerialName("total_quote_amount") val totalAmount: Double? = null,
    val currency: String = "INR",
    @SerialName("proposed_delivery_date") val proposedDeliveryDate: String,
    val comments: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val wholesaler: WholesalerBusinessSummary? = null
) {
    val totalQuoteAmount: Double?
        get() = totalAmount ?: run {
            val sum = (metalEstimateAmount ?: 0.0) +
                (gemstoneEstimateAmount ?: 0.0) +
                (otherEstimateAmount ?: 0.0) +
                (if (makingChargeMode == "fixed_total" || makingChargeMode == "total_quote") makingChargeAmount else 0.0)
            if (sum > 0.0) sum else makingChargeAmount
        }

    val formattedMakingCharge: String
        get() = when (makingChargeMode) {
            "fixed_total", "total_quote" -> "₹${makingChargeAmount.toLong()}"
            "percentage" -> "${makingChargeAmount}%"
            else -> "₹${makingChargeAmount.toLong()}/g"
        }
}

@Serializable
data class ManufacturingRequest(
    val id: String,
    val category: String,
    val material: String,
    val purity: String? = null,
    @SerialName("min_weight_grams") val minWeightGrams: Double,
    @SerialName("max_weight_grams") val maxWeightGrams: Double,
    @SerialName("gemstone_preference") val gemstonePreference: String? = null,
    val quantity: Int = 1,
    @SerialName("making_budget_mode") val makingBudgetMode: String? = null,
    @SerialName("making_budget_amount") val makingBudgetAmount: Double? = null,
    val currency: String = "INR",
    @SerialName("metal_rate_snapshot") val metalRateSnapshot: Double? = null,
    @SerialName("metal_rate_basis") val metalRateBasis: String? = null,
    @SerialName("delivery_needed_date") val deliveryNeededDate: String,
    val notes: String? = null,
    val state: String = "routing",
    @SerialName("active_offer_id") val activeOfferId: String? = null,
    @SerialName("assigned_wholesaler_id") val assignedWholesalerId: String? = null,
    @SerialName("assigned_wholesaler_name") val assignedWholesalerName: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quote: ManufacturingQuote? = null,
    val quotes: List<ManufacturingQuote>? = null,
    @SerialName("broadcast_mode") val broadcastMode: String? = null,
    @SerialName("quotation_deadline") val quotationDeadline: String? = null,
    @SerialName("assigned_wholesaler") val assignedWholesaler: WholesalerBusinessSummary? = null,
    @SerialName("active_offer") val activeOffer: ManufacturingOfferSummary? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("assigned_at") val assignedAt: String? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null
) {
    val formattedWeightRange: String
        get() {
            if (kotlin.math.abs(minWeightGrams - maxWeightGrams) < 0.001) {
                return "%.1f g".format(minWeightGrams)
            }
            return "%.1f - %.1f g".format(minWeightGrams, maxWeightGrams)
        }
}

@Serializable
data class ManufacturingOffer(
    val id: String,
    @SerialName("request_id") val requestId: String,
    val rank: Int,
    val status: String,
    @SerialName("offered_at") val offeredAt: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("remaining_seconds") val remainingSeconds: Int = 0,
    val request: ManufacturingRequest? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val quote: ManufacturingQuote? = null,
    @SerialName("responded_at") val respondedAt: String? = null,
    @SerialName("decline_reason") val declineReason: String? = null
) {
    val isActive: Boolean
        get() = (status == "active" || status == "open") && remainingSeconds > 0
}

@Serializable
data class CreateManufacturingRequestParams(
    @SerialName("asset_id") val assetId: String,
    val category: String,
    @SerialName("min_weight_grams") val minWeightGrams: Double,
    @SerialName("max_weight_grams") val maxWeightGrams: Double,
    val material: String,
    val purity: String? = null,
    @SerialName("gemstone_preference") val gemstonePreference: String? = null,
    val quantity: Int = 1,
    @SerialName("making_budget_mode") val makingBudgetMode: String? = null,
    @SerialName("making_budget_amount") val makingBudgetAmount: Double? = null,
    val currency: String = "INR",
    @SerialName("metal_rate_snapshot") val metalRateSnapshot: Double? = null,
    @SerialName("metal_rate_basis") val metalRateBasis: String? = null,
    @SerialName("delivery_needed_date") val deliveryNeededDate: String,
    val notes: String? = null,
    @SerialName("superseded_request_id") val supersededRequestId: String? = null,
    @SerialName("quotation_window_hours") val quotationWindowHours: Int = 24
)

@Serializable
data class AcceptOfferQuoteParams(
    @SerialName("total_quote_amount") val totalQuoteAmount: Double,
    @SerialName("proposed_delivery_date") val proposedDeliveryDate: String,
    val comments: String? = null,
    @SerialName("expected_version") val expectedVersion: Int? = null
)
