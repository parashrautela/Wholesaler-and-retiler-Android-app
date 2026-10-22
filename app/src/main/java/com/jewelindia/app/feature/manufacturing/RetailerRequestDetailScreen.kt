// Port of: wholesaler ios/JewelIndia/Sources/Features/Manufacturing/RetailerRequestDetailView.swift
package com.jewelindia.app.feature.manufacturing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.ManufacturingQuote
import com.jewelindia.app.core.model.ManufacturingRequest
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerRequestDetailScreen(
    requestId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var request by remember { mutableStateOf<ManufacturingRequest?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var quoteToAward by remember { mutableStateOf<ManufacturingQuote?>(null) }
    var isAwarding by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isCancelling by remember { mutableStateOf(false) }

    suspend fun loadDetail() {
        try {
            request = ManufacturingApi.fetchRetailerRequestDetail(requestId)
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(requestId) {
        loadDetail()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enquiry #${requestId.take(8).uppercase()}",
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
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                scope.launch { loadDetail() }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.goldPrimary)
                        }
                    }

                    request == null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Enquiry not found", style = Typography.santoshi(15.sp), color = Palette.textSecondary)
                        }
                    }

                    else -> {
                        val req = request!!
                        val quotes = req.quotes.orEmpty()

                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Reference photo if available
                            if (!req.imageUrl.isNullOrBlank()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Palette.surfaceElevated)
                                    ) {
                                        CachedImage(
                                            url = req.imageUrl,
                                            contentDescription = "Reference",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            // Request Overview Card
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Palette.surfaceElevated)
                                        .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${req.material} ${req.category}",
                                                style = Typography.cirka(20.sp),
                                                color = Palette.textPrimary
                                            )
                                            Text(
                                                text = req.formattedWeightRange,
                                                style = Typography.santoshi(13.sp),
                                                color = Palette.goldPrimary
                                            )
                                        }
                                        RequestStatusPill(state = req.state)
                                    }

                                    HorizontalDivider(color = Palette.surfaceBorder)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        SpecColumn("Purity", req.purity ?: "22KT")
                                        SpecColumn("Quantity", "${req.quantity} pcs")
                                        SpecColumn("Need By", req.deliveryNeededDate.take(10))
                                    }

                                    req.makingBudgetAmount?.let { budget ->
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            SpecColumn("Making Budget", "₹$budget${if (req.makingBudgetMode == "per_gram") "/g" else " Total"}")
                                            SpecColumn("Metal Rate Basis", req.metalRateBasis ?: "Current market")
                                        }
                                    }

                                    if (!req.notes.isNullOrBlank()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = "Requirement Notes", style = Typography.santoshi(12.sp), color = Palette.textSecondary)
                                            Text(text = req.notes, style = Typography.santoshi(14.sp), color = Palette.textPrimary)
                                        }
                                    }
                                }
                            }

                            // Quotes Received Section
                            item {
                                Text(
                                    text = "Manufacturer Quotes (${quotes.size})",
                                    style = Typography.cirka(18.sp),
                                    color = Palette.textPrimary
                                )
                            }

                            if (quotes.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Palette.surfaceElevated)
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (req.state == "cancelled") "Enquiry cancelled." else "Matching manufacturers are reviewing your specs. Quotes will appear here.",
                                            style = Typography.santoshi(14.sp),
                                            color = Palette.textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                items(quotes, key = { it.id }) { quote ->
                                    val isAwarded = req.state == "awarded" && req.activeOfferId == quote.id

                                    QuoteCard(
                                        quote = quote,
                                        isAwarded = isAwarded,
                                        canAward = req.state != "awarded" && req.state != "cancelled",
                                        onAwardClick = { quoteToAward = quote }
                                    )
                                }
                            }

                            // Cancel Request Option
                            if (req.state != "cancelled" && req.state != "awarded") {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { showCancelDialog = true },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Palette.accentRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Palette.accentRed.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cancel This Enquiry", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Award Quote Confirmation Dialog
    quoteToAward?.let { quote ->
        AlertDialog(
            onDismissRequest = { if (!isAwarding) quoteToAward = null },
            title = {
                Text(
                    text = "Award Quote to Manufacturer?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "You are awarding this enquiry for ₹${quote.totalQuoteAmount?.toLong() ?: 0}. The manufacturer will begin production immediately.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isAwarding = true
                        scope.launch {
                            try {
                                ManufacturingApi.awardQuote(requestId, quote.id)
                                loadDetail()
                            } catch (_: Exception) {
                            } finally {
                                isAwarding = false
                                quoteToAward = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isAwarding) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Confirm Award", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { quoteToAward = null }, enabled = !isAwarding) {
                    Text("Dismiss", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }

    // Cancel Request Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCancelling) showCancelDialog = false },
            title = {
                Text(
                    text = "Cancel Manufacturing Enquiry?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel this enquiry? Manufacturers will stop quoting.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isCancelling = true
                        scope.launch {
                            try {
                                ManufacturingApi.cancelRequest(requestId)
                                loadDetail()
                            } catch (_: Exception) {
                            } finally {
                                isCancelling = false
                                showCancelDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Cancel Enquiry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }, enabled = !isCancelling) {
                    Text("Keep Active", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}

@Composable
fun QuoteCard(
    quote: ManufacturingQuote,
    isAwarded: Boolean,
    canAward: Boolean,
    onAwardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.surfaceElevated)
            .border(
                width = if (isAwarded) 2.dp else 1.dp,
                color = if (isAwarded) Palette.goldPrimary else Palette.surfaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${quote.totalQuoteAmount?.toLong() ?: "Quote Pending"}",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                    quote.proposedDeliveryDate.let { date ->
                        Text(
                            text = "Delivery by ${date.take(10)}",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
                }

                if (isAwarded) {
                    Surface(
                        color = Palette.accentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AWARDED",
                            style = Typography.santoshi(11.sp, FontWeight.Bold),
                            color = Palette.accentGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (canAward) {
                    Button(
                        onClick = onAwardClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Award", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (!quote.comments.isNullOrBlank()) {
                Text(
                    text = quote.comments,
                    style = Typography.santoshi(13.sp),
                    color = Palette.textSecondary
                )
            }
        }
    }
}

@Composable
fun RequestStatusPill(state: String) {
    val (bgColor, textColor, label) = when (state.lowercase()) {
        "routing", "open" -> Triple(Palette.goldPrimary.copy(alpha = 0.15f), Palette.goldPrimary, "Routing")
        "quoted" -> Triple(Color(0xFF2196F3).copy(alpha = 0.15f), Color(0xFF2196F3), "Quoted")
        "awarded" -> Triple(Palette.accentGreen.copy(alpha = 0.15f), Palette.accentGreen, "Awarded")
        "cancelled" -> Triple(Palette.accentRed.copy(alpha = 0.15f), Palette.accentRed, "Cancelled")
        else -> Triple(Palette.surface, Palette.textSecondary, state.replaceFirstChar { it.titlecase() })
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            style = Typography.santoshi(11.sp, FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SpecColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, style = Typography.santoshi(12.sp), color = Palette.textSecondary)
        Text(text = value, style = Typography.santoshi(14.sp, FontWeight.SemiBold), color = Palette.textPrimary)
    }
}
