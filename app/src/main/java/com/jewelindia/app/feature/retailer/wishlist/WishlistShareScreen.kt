// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/Wishlist/WishlistShareView.swift
package com.jewelindia.app.feature.retailer.wishlist

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.CustomerBoard
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.data.api.WishlistShare
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistShareScreen(
    board: CustomerBoard,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var shares by remember { mutableStateOf<List<WishlistShare>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var creating by remember { mutableStateOf(false) }

    var viewersLimit by remember { mutableIntStateOf(5) }
    var durationMinutes by remember { mutableIntStateOf(120) } // 2 hours
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun loadShares() {
        try {
            shares = WishlistApi.fetchShares(board.id)
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    LaunchedEffect(board.id) {
        loadShares()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Share Board",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Palette.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surface)
            )
        },
        containerColor = Palette.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Board Header Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Palette.surfaceElevated)
                        .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = board.title,
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "${board.products.size} curated designs",
                        style = Typography.santoshi(14.sp),
                        color = Palette.goldPrimary
                    )
                    Text(
                        text = "Generate a private link for your customer to review their curated jewellery shortlist.",
                        style = Typography.santoshi(13.sp),
                        color = Palette.textSecondary
                    )
                }
            }

            // Create New Share Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Palette.surfaceElevated)
                        .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Secure Link",
                        style = Typography.cirka(18.sp),
                        color = Palette.textPrimary
                    )

                    // Max Viewers Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Maximum Viewers", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                            Text(text = "Number of devices that can open link", style = Typography.santoshi(12.sp), color = Palette.textSecondary)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { if (viewersLimit > 1) viewersLimit-- },
                                enabled = viewersLimit > 1,
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(Palette.surface)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = null, tint = Palette.textPrimary)
                            }

                            Text(
                                text = viewersLimit.toString(),
                                style = Typography.santoshi(16.sp, FontWeight.Bold),
                                color = Palette.textPrimary,
                                modifier = Modifier.width(28.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = { if (viewersLimit < 50) viewersLimit++ },
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(Palette.surface)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Palette.textPrimary)
                            }
                        }
                    }

                    // Duration Minutes Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Link Expiry", style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.textPrimary)
                            Text(text = "Access expires automatically", style = Typography.santoshi(12.sp), color = Palette.textSecondary)
                        }

                        val durationHours = durationMinutes / 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { if (durationMinutes > 60) durationMinutes -= 60 },
                                enabled = durationMinutes > 60,
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(Palette.surface)
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = null, tint = Palette.textPrimary)
                            }

                            Text(
                                text = "${durationHours}h",
                                style = Typography.santoshi(16.sp, FontWeight.Bold),
                                color = Palette.textPrimary,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )

                            IconButton(
                                onClick = { if (durationMinutes < 1440) durationMinutes += 60 },
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(Palette.surface)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Palette.textPrimary)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.santoshi(13.sp),
                            color = Palette.accentRed
                        )
                    }

                    Button(
                        onClick = {
                            creating = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val newShare = WishlistApi.createShare(board.id, viewersLimit, durationMinutes)
                                    shares = listOf(newShare) + shares
                                    val url = newShare.url
                                    if (url != null) {
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_TEXT, "View our jewellery shortlist: $url")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Wishlist"))
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to create share link."
                                } finally {
                                    creating = false
                                }
                            }
                        },
                        enabled = !creating,
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (creating) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Link, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Text("Generate & Share Link", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Existing Shares Section
            item {
                Text(
                    text = "Active & Past Links (${shares.size})",
                    style = Typography.cirka(18.sp),
                    color = Palette.textPrimary
                )
            }

            if (loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Palette.goldPrimary)
                    }
                }
            } else if (shares.isEmpty()) {
                item {
                    Text(
                        text = "No share links created yet.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(shares, key = { it.id }) { share ->
                    val isRevoked = share.revokedAt != null

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Palette.surfaceElevated)
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = if (isRevoked) Palette.surface else Palette.accentGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isRevoked) "Revoked" else "Active",
                                        style = Typography.santoshi(11.sp, FontWeight.SemiBold),
                                        color = if (isRevoked) Palette.textSecondary else Palette.accentGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${share.viewsUsed}/${share.maxViewers} views used",
                                    style = Typography.santoshi(13.sp, FontWeight.Medium),
                                    color = Palette.textPrimary
                                )
                            }

                            share.url?.let { link ->
                                Text(
                                    text = link,
                                    style = Typography.santoshi(12.sp),
                                    color = Palette.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }

                        if (!isRevoked && share.url != null) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(share.url ?: ""))
                                    Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Palette.textSecondary, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            WishlistApi.revokeShare(share.id)
                                            loadShares()
                                        } catch (_: Exception) {}
                                    }
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Filled.Block, contentDescription = "Revoke", tint = Palette.accentRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
