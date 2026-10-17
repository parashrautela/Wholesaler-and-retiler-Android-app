// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/Wishlist/CustomerBoardsView.swift
package com.jewelindia.app.feature.retailer.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.jewelindia.app.data.api.CustomerBoard
import com.jewelindia.app.data.api.StoreCustomer
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBoardsScreen(
    customer: StoreCustomer,
    onBack: () -> Unit,
    onOpenBoard: (CustomerBoard) -> Unit,
    onShareBoard: (CustomerBoard) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var boards by remember { mutableStateOf<List<CustomerBoard>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var showAddBoardDialog by remember { mutableStateOf(false) }
    var newBoardTitle by remember { mutableStateOf("") }
    var isAddingBoard by remember { mutableStateOf(false) }
    var boardToDelete by remember { mutableStateOf<CustomerBoard?>(null) }
    var isDeletingBoard by remember { mutableStateOf(false) }

    suspend fun loadBoards() {
        try {
            boards = WishlistApi.fetchBoards(customer.id)
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(customer.id) {
        loadBoards()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            style = Typography.cirka(20.sp),
                            color = Palette.textPrimary
                        )
                        Text(
                            text = customer.phone ?: "Customer Boards",
                            style = Typography.santoshi(12.sp),
                            color = Palette.textSecondary
                        )
                    }
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
                actions = {
                    IconButton(onClick = { showAddBoardDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Board",
                            tint = Palette.goldPrimary
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
                scope.launch { loadBoards() }
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

                    boards.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GridView,
                                contentDescription = null,
                                tint = Palette.textTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Boards Created",
                                style = Typography.cirka(20.sp),
                                color = Palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create custom boards like 'Bridal Choker Selection' or 'Daily Wear Rings' for ${customer.name}.",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showAddBoardDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Create Board", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(boards, key = { it.id }) { board ->
                                CustomerBoardCard(
                                    board = board,
                                    onClick = { onOpenBoard(board) },
                                    onShare = { onShareBoard(board) },
                                    onDelete = { boardToDelete = board }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Board Dialog
    if (showAddBoardDialog) {
        AlertDialog(
            onDismissRequest = { if (!isAddingBoard) showAddBoardDialog = false },
            title = {
                Text(
                    text = "New Customer Board",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter a title for this curation board.",
                        style = Typography.santoshi(14.sp),
                        color = Palette.textSecondary
                    )
                    OutlinedTextField(
                        value = newBoardTitle,
                        onValueChange = { newBoardTitle = it },
                        placeholder = { Text("e.g. Wedding Set Shortlist", color = Palette.textTertiary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Palette.goldPrimary,
                            unfocusedBorderColor = Palette.surfaceBorder,
                            focusedTextColor = Palette.textPrimary,
                            unfocusedTextColor = Palette.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBoardTitle.isNotBlank()) {
                            isAddingBoard = true
                            scope.launch {
                                try {
                                    WishlistApi.addBoard(customer.id, newBoardTitle.trim())
                                    newBoardTitle = ""
                                    loadBoards()
                                } catch (_: Exception) {
                                } finally {
                                    isAddingBoard = false
                                    showAddBoardDialog = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isAddingBoard) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBoardDialog = false }, enabled = !isAddingBoard) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }

    // Delete Board Dialog
    boardToDelete?.let { board ->
        AlertDialog(
            onDismissRequest = { if (!isDeletingBoard) boardToDelete = null },
            title = {
                Text(
                    text = "Delete Board?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${board.title}\"? Designs saved on this board will be unlinked.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingBoard = true
                        scope.launch {
                            try {
                                WishlistApi.deleteBoard(board.id)
                                boards = boards.filter { it.id != board.id }
                            } catch (_: Exception) {
                            } finally {
                                isDeletingBoard = false
                                boardToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isDeletingBoard) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { boardToDelete = null }, enabled = !isDeletingBoard) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}

@Composable
fun CustomerBoardCard(
    board: CustomerBoard,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = board.title,
                        style = Typography.cirka(18.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = "${board.products.size} designs saved",
                        style = Typography.santoshi(13.sp),
                        color = Palette.goldPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Palette.goldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Palette.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Preview thumbnails of designs on this board
            if (board.products.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    board.products.take(4).forEach { prod ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Palette.surface)
                        ) {
                            CachedImage(
                                url = prod.primaryImage,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
