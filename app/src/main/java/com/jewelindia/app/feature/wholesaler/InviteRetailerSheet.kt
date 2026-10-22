// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/InviteRetailerSheet.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.ui.platform.LocalConfiguration
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.RetailerInvitation
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteRetailerSheet(
    sheetState: SheetState,
    previewStage: Int? = null,
    onDismiss: () -> Unit,
    onViewHistory: () -> Unit = {}
) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var invitation by remember {
        mutableStateOf(
            if (previewStage != null) {
                RetailerInvitation(
                    id = "preview-id",
                    code = "PJ-b61rce",
                    link = "https://jewelindia.shop/join/PJ-b61rce",
                    expiresAt = "2026-10-12",
                    giftCredits = 1000
                )
            } else null
        )
    }
    var pointsOffered by remember { mutableIntStateOf(1000) }
    var busy by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(previewStage) {
        if (previewStage == null) {
            try {
                val existing = JewelApi.fetchLatestActiveInvitation()
                if (existing != null) {
                    invitation = existing
                } else {
                    invitation = JewelApi.createRetailerInvitation(gift = pointsOffered)
                }
            } catch (_: Exception) {}
        }
    }

    fun handleCopy() {
        val link = invitation?.link ?: "https://jewelindia.shop/join/${invitation?.code}"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Jewel India Invitation", link)
        clipboard.setPrimaryClip(clip)
        copied = true
        scope.launch {
            delay(2000)
            copied = false
        }
    }

    fun handleInviteNow() {
        val inv = invitation ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "You are invited to join Jewel India as a retailer! Use code ${inv.code} to get $pointsOffered points. Open link: ${inv.link}"
            )
        }
        context.startActivity(Intent.createChooser(shareIntent, "Invite retailer"))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF1F2F3),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Reward Banner Card (Figma Node 3156:1826)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Left Column
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Earn 1000 points pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE9E9E9), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Earn 1000 points",
                                style = Typography.santoshi(12.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E2D2A)
                            )
                        }

                        Column {
                            Text(
                                text = "Grow your network.\nGet rewarded.",
                                style = Typography.cirka(24.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E2D2A),
                                lineHeight = 28.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "For each retailer who joins",
                                style = Typography.santoshi(12.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF949494)
                            )
                        }
                    }

                    // Artwork on Right
                    Image(
                        painter = painterResource(id = R.drawable.invite_reward_artwork),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(width = 140.dp, height = 170.dp)
                    )
                }
            }

            // Subtitle / Heading
            Text(
                text = "Help a retailer discover a better way to do jewellery business.",
                style = Typography.santoshi(16.sp),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2D2A),
                lineHeight = 22.sp
            )

            // Stepper Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .width(152.dp)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEDEDED), CircleShape),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(enabled = pointsOffered >= 250) {
                                if (pointsOffered >= 250) pointsOffered -= 250
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_invite_minus),
                            contentDescription = "Minus",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFEDEDED))
                    )

                    Box(
                        modifier = Modifier.width(58.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(locale, "%,d", pointsOffered),
                            style = Typography.santoshi(16.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E2D2A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFEDEDED))
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { pointsOffered += 250 },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_invite_plus),
                            contentDescription = "Plus",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "(Give them a head start with points from your wallet.)",
                    style = Typography.santoshi(12.sp),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF949494)
                )
            }

            // Invitation Code Box
            invitation?.let { inv ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "YOUR INVITATION CODE",
                            style = Typography.santoshi(10.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF949494),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = inv.code,
                            style = Typography.santoshi(20.sp),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF2E2D2A)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFF1F2F3))
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                            .clickable { handleCopy() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (copied) Color(0xFF16A34A) else Color(0xFF2E2D2A),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (copied) "Copied" else "Copy link",
                                style = Typography.santoshi(12.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (copied) Color(0xFF16A34A) else Color(0xFF2E2D2A)
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(12.sp),
                    color = Color.Red
                )
            }

            // Primary "Invite retailer now" Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF4F4F4F), Color(0xFF232323))
                        )
                    )
                    .clickable(enabled = !busy) { handleInviteNow() },
                contentAlignment = Alignment.Center
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Invite retailer now",
                            style = Typography.santoshi(16.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
