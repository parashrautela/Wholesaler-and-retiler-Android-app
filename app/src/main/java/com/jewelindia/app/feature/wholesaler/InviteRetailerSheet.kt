// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/InviteRetailerSheet.swift
package com.jewelindia.app.feature.wholesaler

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.InvitationSettings
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.RetailerInvitation
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteRetailerSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf<InvitationSettings?>(null) }
    var activeInvitation by remember { mutableStateOf<RetailerInvitation?>(null) }
    var giftCredits by remember { mutableIntStateOf(1000) }
    var loading by remember { mutableStateOf(true) }
    var generating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val s = JewelApi.invitationSettings()
            settings = s
            giftCredits = s.minimum
        } catch (_: Exception) {}

        try {
            activeInvitation = JewelApi.fetchLatestActiveInvitation()
        } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Palette.light
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Invite a Retailer",
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )

            Text(
                text = "Retailers need an invitation code to register. When they sign up, they receive gift credits to browse your catalogue.",
                style = Typography.santoshi(14.sp),
                color = Palette.muted,
                textAlign = TextAlign.Center
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            if (loading) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            } else {
                val inv = activeInvitation
                if (inv != null && inv.status == "unclaimed") {
                    // Active code view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.cream)
                            .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ACTIVE INVITATION CODE",
                                style = Typography.santoshi(12.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.goldPrimary
                            )
                            Text(
                                text = inv.code,
                                style = Typography.cirka(28.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.dark
                            )
                            Text(
                                text = "Gift: ${inv.giftCredits} credits · Valid for 7 days",
                                style = Typography.santoshi(12.sp),
                                color = Palette.muted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Jewel India Invite Code", inv.code))
                                        copied = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Palette.dark),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (copied) "Copied!" else "Copy Code")
                                }

                                Button(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join me on Jewel India with my invitation code ${inv.code} to get ${inv.giftCredits} credits: ${inv.link}"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Invitation"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share Link")
                                }
                            }
                        }
                    }
                } else {
                    // Stepper & Generate
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.cream)
                            .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Gift Credits Amount",
                                style = Typography.santoshi(14.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Palette.dark
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val min = settings?.minimum ?: 1000
                                val max = settings?.maximum ?: 10000
                                val step = settings?.step ?: 500

                                IconButton(
                                    onClick = { if (giftCredits > min) giftCredits -= step },
                                    enabled = giftCredits > min,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Palette.light)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }

                                Text(
                                    text = "$giftCredits",
                                    style = Typography.cirka(26.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Palette.dark
                                )

                                IconButton(
                                    onClick = { if (giftCredits < max) giftCredits += step },
                                    enabled = giftCredits < max,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Palette.light)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }

                            Text(
                                text = "Available balance: ${settings?.available ?: 2000} credits",
                                style = Typography.santoshi(12.sp),
                                color = Palette.muted
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                generating = true
                                errorMessage = null
                                try {
                                    activeInvitation = JewelApi.createRetailerInvitation(giftCredits)
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to generate invitation"
                                } finally {
                                    generating = false
                                }
                            }
                        },
                        enabled = !generating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary)
                    ) {
                        if (generating) {
                            CircularProgressIndicator(color = Palette.light)
                        } else {
                            Text(
                                text = "Generate Invitation Code",
                                style = Typography.santoshi(15.sp),
                                fontWeight = FontWeight.Bold,
                                color = Palette.light
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = {
                    onDismiss()
                    onViewHistory()
                }
            ) {
                Text(
                    text = "View Invitation History",
                    style = Typography.santoshi(14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Palette.goldPrimary
                )
            }
        }
    }
}
