// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardingFeeSheet.swift
package com.jewelindia.app.feature.onboarding

import androidx.core.net.toUri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.jewelindia.app.data.net.userMessage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.jewelindia.app.data.api.OnboardingFeeApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface FeePhase {
    data object Ready : FeePhase
    data object Opening : FeePhase
    data object Checking : FeePhase
    data object NotYet : FeePhase
    data class Failed(val message: String) : FeePhase
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFeeSheet(
    amountInr: Int,
    onDismiss: () -> Unit,
    onPaid: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var phase by remember { mutableStateOf<FeePhase>(FeePhase.Ready) }
    var currentLink by remember { mutableStateOf<OnboardingFeeApi.Link?>(null) }
    var waitingForTabReturn by remember { mutableStateOf(false) }

    suspend fun checkPayment(linkId: String) {
        phase = FeePhase.Checking
        for (attempt in 0 until 4) {
            val confirmed = try {
                OnboardingFeeApi.confirm(linkId)
            } catch (_: Exception) {
                false
            }
            if (confirmed) {
                onPaid()
                return
            }
            if (attempt < 3) delay(2000)
        }
        phase = FeePhase.NotYet
    }

    suspend fun startPayment() {
        phase = FeePhase.Opening
        try {
            val link = OnboardingFeeApi.createLink()
            if (link.paid == true) {
                onPaid()
                return
            }
            val url = link.url ?: throw Exception("Payment URL not available.")
            currentLink = link
            phase = FeePhase.Ready
            waitingForTabReturn = true

            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(context, url.toUri())
        } catch (e: Exception) {
            phase = FeePhase.Failed(e.userMessage())
        }
    }

    // Detect when returning to app from Custom Tab
    LifecycleResumeEffect(waitingForTabReturn) {
        val linkId = currentLink?.linkID
        if (waitingForTabReturn && linkId != null) {
            waitingForTabReturn = false
            scope.launch {
                checkPayment(linkId)
            }
        }
        onPauseOrDispose { }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (phase !is FeePhase.Opening && phase !is FeePhase.Checking) {
                onDismiss()
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified Fee",
                tint = OnboardColor.text,
                modifier = Modifier.size(48.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "One-time onboarding fee",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardColor.text
                )
                Text(
                    text = "₹$amountInr",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardColor.text
                )
                Text(
                    text = "Paid once, before your application is sent for verification.",
                    fontSize = 14.sp,
                    color = OnboardColor.subtle,
                    textAlign = TextAlign.Center
                )
            }

            when (val p = phase) {
                is FeePhase.NotYet -> {
                    Text(
                        text = "We haven't received the payment yet. If you've paid, give it a moment and check again.",
                        fontSize = 13.sp,
                        color = OnboardColor.subtle,
                        textAlign = TextAlign.Center
                    )
                }
                is FeePhase.Failed -> {
                    Text(
                        text = p.message,
                        fontSize = 13.sp,
                        color = OnboardColor.danger,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isBusy = phase is FeePhase.Opening || phase is FeePhase.Checking
            val buttonTitle = when (phase) {
                is FeePhase.NotYet -> "Check Again"
                else -> "Pay ₹$amountInr"
            }
            val busyTitle = when (phase) {
                is FeePhase.Checking -> "Checking payment..."
                else -> "Opening payment..."
            }

            OnboardPrimaryButton(
                title = buttonTitle,
                busyTitle = busyTitle,
                isBusy = isBusy,
                isEnabled = true,
                action = {
                    scope.launch {
                        val linkId = currentLink?.linkID
                        if (phase is FeePhase.NotYet && linkId != null) {
                            checkPayment(linkId)
                        } else {
                            startPayment()
                        }
                    }
                }
            )

            if (phase is FeePhase.NotYet) {
                Text(
                    text = "Open the payment page again",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnboardColor.text,
                    modifier = Modifier.clickable {
                        scope.launch { startPayment() }
                    }
                )
            }

            Text(
                text = "Not now",
                fontSize = 14.sp,
                color = OnboardColor.subtle,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}
