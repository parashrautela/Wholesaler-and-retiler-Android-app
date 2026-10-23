// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/InvitationHistoryView.swift
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.net.userMessage
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.api.RetailerInvitation
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.design.readableContentWidth
import kotlinx.coroutines.launch

@Composable
fun InvitationHistoryScreen(onBack: () -> Unit) {
    var invitations by remember { mutableStateOf<List<RetailerInvitation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalCount by remember { mutableIntStateOf(0) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var cancellingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            loadError = null
            actionError = null
            currentPage = 0
            try {
                val s = JewelApi.invitationSettings(page = 0)
                invitations = s.links ?: emptyList()
                totalCount = s.count ?: invitations.size
            } catch (e: Exception) {
                loadError = e.userMessage()
                invitations = emptyList()
            } finally {
                loading = false
            }
        }
    }

    fun loadMore() {
        if (loadingMore || invitations.size >= totalCount) return
        scope.launch {
            loadingMore = true
            loadError = null
            try {
                val nextPage = currentPage + 1
                val s = JewelApi.invitationSettings(page = nextPage)
                val newLinks = s.links ?: emptyList()
                invitations = invitations + newLinks
                currentPage = nextPage
                totalCount = s.count ?: totalCount
            } catch (e: Exception) {
                loadError = e.userMessage()
            } finally {
                loadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .readableContentWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }
            Text(
                text = "Invitation History",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { load() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Palette.muted
                )
            }
        }

        if (actionError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Palette.accentRed.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = actionError!!,
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Palette.goldPrimary)
            }
        } else if (loadError != null && invitations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = loadError!!,
                        style = Typography.santoshi(14.sp),
                        color = Palette.accentRed
                    )
                    TextButton(onClick = { load() }) {
                        Text("Retry", color = Palette.goldPrimary, style = Typography.santoshi(14.sp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (invitations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No invitations created yet",
                    style = Typography.santoshi(15.sp),
                    color = Palette.muted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(invitations, key = { it.id }) { inv ->
                    InvitationRow(
                        inv = inv,
                        isCancelling = cancellingId == inv.id,
                        onCancel = {
                            scope.launch {
                                cancellingId = inv.id
                                actionError = null
                                try {
                                    JewelApi.cancelInvitation(inv.id)
                                    load()
                                } catch (e: Exception) {
                                    actionError = e.userMessage()
                                } finally {
                                    cancellingId = null
                                }
                            }
                        }
                    )
                }

                if (invitations.size < totalCount) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (loadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Palette.goldPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                TextButton(onClick = { loadMore() }) {
                                    Text(
                                        text = "Load more (${invitations.size}/$totalCount)",
                                        style = Typography.santoshi(14.sp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Palette.goldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun InvitationRow(
    inv: RetailerInvitation,
    isCancelling: Boolean,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = inv.code,
                    style = Typography.cirka(17.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark
                )
                Spacer(modifier = Modifier.height(2.dp))
                val statusStr = inv.status?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Active"
                Text(
                    text = "Status: $statusStr · ${inv.giftCredits ?: 1000} credits",
                    style = Typography.santoshi(12.sp),
                    color = Palette.muted
                )
            }

            if (inv.status == "unclaimed") {
                if (isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Palette.accentRed,
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = Palette.accentRed, style = Typography.santoshi(13.sp))
                    }
                }
            }
        }
    }
}
