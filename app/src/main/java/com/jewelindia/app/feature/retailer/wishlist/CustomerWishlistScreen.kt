// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/Wishlist/CustomerWishlistView.swift
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.data.api.StoreCustomer
import com.jewelindia.app.data.api.WishlistApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerWishlistScreen(
    onOpenMarketplace: () -> Unit,
    onCustomerClick: (StoreCustomer) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var customers by remember { mutableStateOf<List<StoreCustomer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var showAddCustomerSheet by remember { mutableStateOf(false) }
    var customerToDelete by remember { mutableStateOf<StoreCustomer?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    suspend fun loadCustomers() {
        try {
            customers = WishlistApi.fetchCustomers()
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Customer Wishlists",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { showAddCustomerSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Customer",
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
                scope.launch { loadCustomers() }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Hero Card: Discover & Curate
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.surfaceElevated)
                            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenMarketplace)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = Palette.goldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Discover & Shortlist",
                                        style = Typography.cirka(17.sp),
                                        color = Palette.textPrimary
                                    )
                                    Text(
                                        text = "Browse wholesale designs and save them directly into customer boards",
                                        style = Typography.santoshi(12.sp),
                                        color = Palette.textSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Palette.goldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Store Customers (${customers.size})",
                        style = Typography.cirka(18.sp),
                        color = Palette.textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (loading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.goldPrimary)
                        }
                    }
                } else if (customers.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = null,
                                tint = Palette.textTertiary,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "No Customers Added",
                                style = Typography.cirka(18.sp),
                                color = Palette.textPrimary
                            )
                            Text(
                                text = "Add customers visiting your showroom to build custom wishlists for weddings, anniversaries, and gifts.",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { showAddCustomerSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add Customer", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    items(customers, key = { it.id }) { customer ->
                        CustomerRow(
                            customer = customer,
                            onClick = { onCustomerClick(customer) },
                            onDelete = { customerToDelete = customer }
                        )
                    }
                }
            }
        }
    }

    // Add Customer Sheet
    if (showAddCustomerSheet) {
        AddCustomerSheet(
            onDismiss = { showAddCustomerSheet = false },
            onCustomerAdded = {
                showAddCustomerSheet = false
                scope.launch { loadCustomers() }
            }
        )
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) customerToDelete = null },
            title = {
                Text(
                    text = "Delete Customer?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${cust.name}? All wishlists and saved boards for this customer will be removed.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            try {
                                WishlistApi.deleteCustomer(cust.id)
                                customers = customers.filter { it.id != cust.id }
                            } catch (_: Exception) {
                            } finally {
                                isDeleting = false
                                customerToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }, enabled = !isDeleting) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}

@Composable
fun CustomerRow(
    customer: StoreCustomer,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Palette.goldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = customer.name.take(1).uppercase(),
                style = Typography.cirka(18.sp),
                color = Palette.goldPrimary
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = customer.name,
                style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                color = Palette.textPrimary
            )
            customer.phone?.let { ph ->
                Text(
                    text = ph,
                    style = Typography.santoshi(13.sp),
                    color = Palette.textSecondary
                )
            }
            customer.note?.let { nt ->
                Text(
                    text = nt,
                    style = Typography.santoshi(12.sp),
                    color = Palette.goldPrimary,
                    maxLines = 1
                )
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Palette.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Palette.textTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerSheet(
    onDismiss: () -> Unit,
    onCustomerAdded: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Store Customer",
                    style = Typography.cirka(22.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Customer Name *", color = Palette.textSecondary) },
                placeholder = { Text("e.g. Priya Sharma", color = Palette.textTertiary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.surfaceBorder,
                    focusedTextColor = Palette.textPrimary,
                    unfocusedTextColor = Palette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Mobile Number (Optional)", color = Palette.textSecondary) },
                placeholder = { Text("e.g. 9876543210", color = Palette.textTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.surfaceBorder,
                    focusedTextColor = Palette.textPrimary,
                    unfocusedTextColor = Palette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Preference Notes (Optional)", color = Palette.textSecondary) },
                placeholder = { Text("e.g. Interested in heavy uncut polki bridal necklace", color = Palette.textTertiary) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.surfaceBorder,
                    focusedTextColor = Palette.textPrimary,
                    unfocusedTextColor = Palette.textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter customer's name."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null

                    scope.launch {
                        try {
                            WishlistApi.addCustomer(
                                name = name.trim(),
                                phone = phone.ifBlank { null },
                                note = note.ifBlank { null }
                            )
                            StoreActivity.log(StoreActivity.Event.CustomerAdded)
                            onCustomerAdded()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to add customer. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                } else {
                    Text("Save Customer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
