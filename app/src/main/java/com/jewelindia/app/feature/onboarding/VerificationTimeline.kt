// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardSubmittedView.swift (VerificationTimeline)
package com.jewelindia.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.VerificationStatus

/**
 * Two-stage verification timeline matching iOS `VerificationTimeline`.
 */
@Composable
fun VerificationTimeline(
    status: VerificationStatus,
    stage: Int // 0..3
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step 1: Submission
        val step1Done = stage >= 1
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (step1Done) OnboardColor.uploadFilled else OnboardColor.border),
                contentAlignment = Alignment.Center
            ) {
                if (step1Done) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(if (stage >= 2) OnboardColor.uploadFilled else OnboardColor.border)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Application Submitted",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardColor.heading
            )
            Text(
                text = "All documents received securely",
                fontSize = 12.sp,
                color = OnboardColor.subtle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Verification
            val isVerified = status == VerificationStatus.verified
            val isRejected = status == VerificationStatus.rejected || status == VerificationStatus.resubmissionRequired

            Text(
                text = when {
                    isVerified -> "Verification Approved"
                    isRejected -> "Verification Needs Attention"
                    else -> "Under Review"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isVerified -> OnboardColor.uploadFilled
                    isRejected -> OnboardColor.danger
                    else -> OnboardColor.heading
                }
            )
            Text(
                text = when {
                    isVerified -> "Account active and approved"
                    isRejected -> "Action required to proceed"
                    else -> "Reviewed by our compliance team"
                },
                fontSize = 12.sp,
                color = OnboardColor.subtle
            )
        }
    }
}
