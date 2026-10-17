// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardSubmittedView.swift
package com.jewelindia.app.feature.onboarding

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.jewelindia.app.core.model.VerificationStatus
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerificationRow(
    @SerialName("verification_status") val verificationStatus: VerificationStatus? = null,
    @SerialName("notification_message") val notificationMessage: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    @SerialName("rejected_documents") val rejectedDocuments: List<String>? = null,
    @SerialName("referred_by") val referredBy: String? = null
)

@Composable
fun OnboardSubmittedScreen(
    sessionStore: SessionStore,
    supabase: SupabaseClient = JewelSupabase.client
) {
    val scope = rememberCoroutineScope()
    var row by remember { mutableStateOf<VerificationRow?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var timelineStage by remember { mutableIntStateOf(0) }

    val status = row?.verificationStatus ?: VerificationStatus.pending

    val heading = when (status) {
        VerificationStatus.pending, VerificationStatus.onHold -> "You're all submitted!"
        VerificationStatus.rejected -> "Application Rejected"
        VerificationStatus.resubmissionRequired -> "Resubmission Required"
        VerificationStatus.verified -> "Verification Complete!"
        VerificationStatus.banned -> "Application Rejected"
    }

    val bodyText = when (status) {
        VerificationStatus.pending -> "We'll verify your documents in 24–48 hours and notify you on your number once you're approved."
        VerificationStatus.onHold -> row?.notificationMessage ?: "Your account is on hold pending further review."
        VerificationStatus.verified -> "You're verified! You can now access your full dashboard."
        VerificationStatus.rejected, VerificationStatus.resubmissionRequired, VerificationStatus.banned ->
            row?.rejectionReason ?: row?.notificationMessage ?: "There was an issue with your submission. Please click below to resubmit your documents."
    }

    val buttonTitle = when (status) {
        VerificationStatus.pending, VerificationStatus.onHold -> "I Understand"
        VerificationStatus.rejected, VerificationStatus.resubmissionRequired, VerificationStatus.banned -> "Resubmit"
        VerificationStatus.verified -> "Go to Dashboard"
    }

    val isRejection = status == VerificationStatus.rejected || status == VerificationStatus.resubmissionRequired

    suspend fun fetchRow(): VerificationRow? {
        val uid = sessionStore.user.value?.id ?: return null
        return try {
            supabase.from("wholesalers").select(
                Columns.raw("verification_status, notification_message, rejection_reason, rejected_documents")
            ) {
                filter { eq("user_id", uid) }
                limit(1)
            }.decodeSingleOrNull<VerificationRow>()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun reload() {
        if (sessionStore.user.value == null || refreshing) return
        refreshing = true
        refreshError = null
        val fresh = fetchRow()
        refreshing = false
        if (fresh != null) {
            row = fresh
        } else {
            refreshError = "Couldn't check your status. Please try again."
        }
    }

    LaunchedEffect(Unit) {
        row = fetchRow()
        loading = false

        // Timeline choreography: 50ms -> 300ms -> 600ms
        delay(50)
        timelineStage = 1
        delay(250)
        timelineStage = 2
        delay(300)
        timelineStage = 3
    }

    LifecycleResumeEffect(Unit) {
        scope.launch { reload() }
        onPauseOrDispose { }
    }

    fun documentName(key: String): String {
        return when (key) {
            "aadhaar_front", "aadhaar_front_url" -> "Aadhaar Card (Front)"
            "aadhaar_back", "aadhaar_back_url" -> "Aadhaar Card (Back)"
            "pan_card", "pan_card_url" -> "PAN Card"
            "gst_certificate", "gst_certificate_url" -> "GST Certificate"
            "business_logo", "business_logo_url" -> "Business Logo"
            else -> key.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OnboardNavbar(
            gutter = 20.dp,
            showsBack = false,
            onBack = null,
            onSignOut = { scope.launch { sessionStore.signOut() } },
            onRefresh = { reload() },
            isRefreshing = refreshing
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            StepIndicator(step = 4, totalSteps = 3)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = heading,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = OnboardColor.heading
                )
                Text(
                    text = "we're reviewing your details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnboardColor.subtle
                )
            }

            VerificationTimeline(status = status, stage = timelineStage)

            if (isRejection) {
                // Rejection / revision panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x66FEF2F2))
                        .border(1.dp, Color(0x99FECACA), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xB3FEE2E2))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (status == VerificationStatus.rejected) "Application Rejected" else "Revision Required",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF450A0A)
                            )
                            Text(
                                text = row?.rejectionReason ?: "There was an issue with your submission. Please check the details below and resubmit.",
                                fontSize = 13.5.sp,
                                color = Color(0xE6991B1B)
                            )
                        }
                    }

                    val docs = row?.rejectedDocuments
                    if (!docs.isNullOrEmpty()) {
                        HorizontalDivider(color = Color(0x99FECACA))

                        Text(
                            text = "ITEMS TO RESUBMIT:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xB3450A0A)
                        )

                        docs.forEach { docKey ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x4DFEE2E2))
                                    .border(1.dp, Color(0x80FECACA), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Rejected",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = documentName(docKey),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = bodyText,
                    fontSize = 14.sp,
                    color = OnboardColor.subtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (refreshError != null) {
                Text(
                    text = refreshError!!,
                    fontSize = 13.sp,
                    color = OnboardColor.danger,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Need help in the meantime? Call us on 9897453396 — we're happy to assist.",
                    fontSize = 13.sp,
                    color = OnboardColor.subtle
                )

                OnboardPrimaryButton(
                    title = if (loading) "Loading..." else buttonTitle,
                    isEnabled = !loading,
                    action = {
                        scope.launch {
                            when (status) {
                                VerificationStatus.verified -> {
                                    sessionStore.user.value?.id?.let { uid ->
                                        WholesalerApi.markDashboardVisited(uid)
                                    }
                                    sessionStore.refreshDestination()
                                }
                                VerificationStatus.rejected, VerificationStatus.resubmissionRequired -> {
                                    sessionStore.refreshDestination()
                                }
                                VerificationStatus.pending, VerificationStatus.onHold, VerificationStatus.banned -> {
                                    sessionStore.signOut()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
