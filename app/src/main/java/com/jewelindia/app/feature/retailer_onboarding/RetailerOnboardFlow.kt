// Port of: wholesaler ios/JewelIndia/Sources/Features/RetailerOnboarding/RetailerOnboardFlow.swift
package com.jewelindia.app.feature.retailer_onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.images.PickedFile
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.net.DbRefusal
import com.jewelindia.app.data.net.JewelNetwork
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.feature.onboarding.OnboardFlow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Onboarding form state for retailers.
 */
class RetailerOnboardFlow(
    private val clientProvider: () -> SupabaseClient = { JewelSupabase.client }
) : ViewModel() {
    constructor(supabase: SupabaseClient) : this({ supabase })

    private val supabase: SupabaseClient get() = clientProvider()

    // Step 1 — identity
    var name by mutableStateOf("")
    var aadhar by mutableStateOf("")
    var frontImage by mutableStateOf<PickedFile?>(null)
    var backImage by mutableStateOf<PickedFile?>(null)

    // Step 2 — store
    var storeName by mutableStateOf("")
    var selectedState by mutableStateOf("")
    var selectedCity by mutableStateOf("")
    var logoImage by mutableStateOf<PickedFile?>(null)

    // Step 3 — documents
    var panFile by mutableStateOf<PickedFile?>(null)
    var gstFile by mutableStateOf<PickedFile?>(null)

    var submitAttempted by mutableStateOf(false)
    var isSubmitting by mutableStateOf(false)
    var submitError by mutableStateOf<String?>(null)

    companion object {
        fun filterName(input: String): String = OnboardFlow.filterName(input)
        fun formatAadhar(input: String): String = OnboardFlow.formatAadhar(input)

        /**
         * 29 states in web order with Delhi at the end.
         */
        val states = listOf(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya",
            "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim",
            "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand",
            "West Bengal", "Delhi"
        )
    }

    val aadharDigits: String
        get() = aadhar.filter { it.isDigit() }

    val nameError: String?
        get() {
            if (!submitAttempted) return null
            return if (name.trim().isEmpty()) "Name is required" else null
        }

    val aadharError: String?
        get() {
            if (!submitAttempted) return null
            return if (aadharDigits.length == 12) null else "Enter a valid 12-digit Aadhar number"
        }

    val frontError: String?
        get() {
            if (!submitAttempted) return null
            return if (frontImage == null) "Please upload Aadhar Front" else null
        }

    val backError: String?
        get() {
            if (!submitAttempted) return null
            return if (backImage == null) "Please upload Aadhar Back" else null
        }

    val step1Valid: Boolean
        get() = name.trim().isNotEmpty() &&
                aadharDigits.length == 12 &&
                frontImage != null &&
                backImage != null

    val storeNameError: String?
        get() {
            if (!submitAttempted) return null
            return if (storeName.trim().length >= 2) null else "Store name is required"
        }

    val stateError: String?
        get() {
            if (!submitAttempted) return null
            return if (selectedState.isEmpty()) "Please select a state" else null
        }

    val cityError: String?
        get() {
            if (!submitAttempted) return null
            return if (selectedCity.trim().isEmpty()) "Please enter a city" else null
        }

    val logoError: String?
        get() {
            if (!submitAttempted) return null
            return if (logoImage == null) "Please upload your store logo" else null
        }

    val step2Valid: Boolean
        get() = storeName.trim().length >= 2 &&
                selectedState.isNotEmpty() &&
                selectedCity.trim().isNotEmpty() &&
                logoImage != null

    val panError: String?
        get() {
            if (!submitAttempted) return null
            return if (panFile == null) "Please upload your PAN Card" else null
        }

    val gstError: String?
        get() {
            if (!submitAttempted) return null
            return if (gstFile == null) "Please upload your GST Certificate" else null
        }

    val step3Valid: Boolean
        get() = panFile != null && gstFile != null

    fun compress(file: PickedFile): PickedFile {
        return ImageNormalizer.normalized(
            file,
            maxDimension = ImageNormalizer.MAX_DOCUMENT_DIMENSION,
            quality = 0.70f
        )
    }

    suspend fun submit(user: UserInfo, referralCode: String?): Boolean {
        isSubmitting = true
        submitError = null

        val code = referralCode?.trim()
        if (code.isNullOrEmpty()) {
            submitError = Copy.inviteCodeEmpty
            isSubmitting = false
            return false
        }

        val currentSession = supabase.auth.currentSessionOrNull()
        if (currentSession == null || currentSession.user?.id != user.id) {
            submitError = "Your session isn't active on this device. Please sign out and sign in again."
            isSubmitting = false
            return false
        }

        val uid = user.id.lowercase()
        val stamp = System.currentTimeMillis()

        return try {
            kotlinx.coroutines.supervisorScope {
                val frontDeferred = async { runCatching { upload(frontImage, "aadhaar-documents", "$uid/aadhaar-front-$stamp", "Aadhar Front") } }
                val backDeferred = async { runCatching { upload(backImage, "aadhaar-documents", "$uid/aadhaar-back-$stamp", "Aadhar Back") } }
                val panDeferred = async { runCatching { upload(panFile, "pan-documents", "$uid/pan-card-$stamp", "PAN Card") } }
                val gstDeferred = async { runCatching { upload(gstFile, "gst-documents", "$uid/gst-certificate-$stamp", "GST Certificate") } }
                val logoDeferred = async { runCatching { upload(logoImage, "business-logos", "$uid/business-logo-$stamp", "Store Logo") } }

                val frontRes = frontDeferred.await()
                if (frontRes.isFailure) throw frontRes.exceptionOrNull() ?: Exception("Failed to upload Aadhar Front")
                val backRes = backDeferred.await()
                if (backRes.isFailure) throw backRes.exceptionOrNull() ?: Exception("Failed to upload Aadhar Back")
                val panRes = panDeferred.await()
                if (panRes.isFailure) throw panRes.exceptionOrNull() ?: Exception("Failed to upload PAN Card")
                val gstRes = gstDeferred.await()
                if (gstRes.isFailure) throw gstRes.exceptionOrNull() ?: Exception("Failed to upload GST Certificate")
                val logoRes = logoDeferred.await()
                if (logoRes.isFailure) throw logoRes.exceptionOrNull() ?: Exception("Failed to upload Store Logo")

                val frontUrl = frontRes.getOrNull()
                val backUrl = backRes.getOrNull()
                val panUrl = panRes.getOrNull()
                val gstUrl = gstRes.getOrNull()
                val logoUrl = logoRes.getOrNull()

                @Serializable
                data class Row(
                    @SerialName("user_id") val userId: String,
                    val email: String?,
                    @SerialName("full_name") val fullName: String,
                    @SerialName("aadhar_number") val aadharNumber: String,
                    @SerialName("business_name") val businessName: String,
                    val state: String,
                    val city: String,
                    @SerialName("aadhaar_front_url") val aadhaarFrontUrl: String?,
                    @SerialName("aadhaar_back_url") val aadhaarBackUrl: String?,
                    @SerialName("pan_card_url") val panCardUrl: String?,
                    @SerialName("gst_certificate_url") val gstCertificateUrl: String?,
                    @SerialName("business_logo_url") val businessLogoUrl: String?,
                    @SerialName("referral_code") val referralCode: String?,
                    @SerialName("verification_status") val verificationStatus: String
                )

                val row = Row(
                    userId = uid,
                    email = user.email ?: user.phone,
                    fullName = name.trim(),
                    aadharNumber = aadharDigits,
                    businessName = storeName.trim(),
                    state = selectedState,
                    city = selectedCity.trim(),
                    aadhaarFrontUrl = frontUrl,
                    aadhaarBackUrl = backUrl,
                    panCardUrl = panUrl,
                    gstCertificateUrl = gstUrl,
                    businessLogoUrl = logoUrl,
                    referralCode = code,
                    verificationStatus = VerificationStatus.pending.name
                )

                JewelNetwork.withRetry {
                    supabase.from("retailers").upsert(row) {
                        onConflict = "user_id"
                    }
                }
                true
            }
        } catch (e: Exception) {
            submitError = DbRefusal.message(e)
            false
        } finally {
            isSubmitting = false
        }
    }

    private suspend fun upload(
        file: PickedFile?,
        bucket: String,
        basePath: String,
        label: String
    ): String? {
        if (file == null) return null
        val prepared = compress(file)
        val ext = if (prepared.isPDF) "pdf" else "jpg"
        val path = "$basePath.$ext"

        return try {
            JewelNetwork.withRetry {
                WholesalerApi.upload(
                    bucket = bucket,
                    path = path,
                    data = prepared.data,
                    contentType = prepared.mimeType
                )
            }
        } catch (e: Exception) {
            throw Exception("Couldn't upload your $label: ${e.message}")
        }
    }
}
