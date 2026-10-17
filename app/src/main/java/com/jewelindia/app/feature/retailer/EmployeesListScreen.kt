// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/EmployeesListView.swift
package com.jewelindia.app.feature.retailer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.StaffAccountsApi
import com.jewelindia.app.data.api.StaffCredentials
import com.jewelindia.app.data.api.StaffMember
import com.jewelindia.app.data.api.StaffStatus
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesListScreen(
    onOpenAddEmployee: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var staffList by remember { mutableStateOf<List<StaffMember>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }

    var credentialsToShow by remember { mutableStateOf<StaffCredentials?>(null) }
    var memberToRemove by remember { mutableStateOf<StaffMember?>(null) }
    var isRemoving by remember { mutableStateOf(false) }

    suspend fun loadStaff() {
        try {
            staffList = StaffAccountsApi.fetchStaff()
        } catch (_: Exception) {
        } finally {
            loading = false
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadStaff()
        StaffAccountsApi.staffChanged.collect {
            loadStaff()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Showroom Staff",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onOpenAddEmployee) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Staff",
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
                scope.launch { loadStaff() }
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

                    staffList.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.People,
                                contentDescription = null,
                                tint = Palette.textTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Staff Added Yet",
                                style = Typography.cirka(20.sp),
                                color = Palette.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Give your sales team login access to present curated jewellery designs to store visitors.",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onOpenAddEmployee,
                                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Add First Staff Member", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(staffList, key = { it.id }) { member ->
                                StaffRow(
                                    member = member,
                                    onResetPassword = {
                                        scope.launch {
                                            try {
                                                val creds = StaffAccountsApi.resetPassword(member.id)
                                                credentialsToShow = creds
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    onToggleActive = {
                                        scope.launch {
                                            try {
                                                val target = member.status != StaffStatus.Active
                                                StaffAccountsApi.setActive(member.id, target)
                                                loadStaff()
                                            } catch (_: Exception) {}
                                        }
                                    },
                                    onRemove = { memberToRemove = member }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Credentials Sheet
    credentialsToShow?.let { creds ->
        StaffCredentialsSheet(
            credentials = creds,
            onDismiss = { credentialsToShow = null }
        )
    }

    // Remove Confirmation Dialog
    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { if (!isRemoving) memberToRemove = null },
            title = {
                Text(
                    text = "Remove Staff Member?",
                    style = Typography.cirka(20.sp),
                    color = Palette.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to revoke access for ${member.fullName}? They will no longer be able to log in to the staff showroom portal.",
                    style = Typography.santoshi(14.sp),
                    color = Palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRemoving = true
                        scope.launch {
                            try {
                                StaffAccountsApi.remove(member)
                                loadStaff()
                            } catch (_: Exception) {
                            } finally {
                                isRemoving = false
                                memberToRemove = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.accentRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isRemoving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Remove Access", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { memberToRemove = null },
                    enabled = !isRemoving
                ) {
                    Text("Cancel", color = Palette.textSecondary)
                }
            },
            containerColor = Palette.surfaceElevated
        )
    }
}

@Composable
fun StaffRow(
    member: StaffMember,
    onResetPassword: () -> Unit,
    onToggleActive: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.surfaceElevated)
            .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar Initial
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Palette.goldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.fullName.take(1).uppercase(),
                style = Typography.cirka(18.sp),
                color = Palette.goldPrimary
            )
        }

        // Details
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = member.fullName,
                style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                color = Palette.textPrimary
            )
            Text(
                text = member.authUsername ?: member.signInLabel,
                style = Typography.santoshi(13.sp),
                color = Palette.textSecondary
            )
            member.designation?.let { des ->
                Text(
                    text = des,
                    style = Typography.santoshi(12.sp),
                    color = Palette.goldPrimary
                )
            }
        }

        // Status Pill
        StaffStatusChip(status = member.status)

        // Actions Menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = Palette.textSecondary
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Palette.surfaceElevated)
            ) {
                if (member.isLoginAccount) {
                    DropdownMenuItem(
                        text = { Text("Reset Password", color = Palette.textPrimary) },
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = Palette.goldPrimary) },
                        onClick = {
                            showMenu = false
                            onResetPassword()
                        }
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (member.status == StaffStatus.Active) "Deactivate Account" else "Activate Account",
                            color = Palette.textPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (member.status == StaffStatus.Active) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                            contentDescription = null,
                            tint = Palette.textSecondary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onToggleActive()
                    }
                )

                HorizontalDivider(color = Palette.surfaceBorder)

                DropdownMenuItem(
                    text = { Text("Remove Staff", color = Palette.accentRed) },
                    leadingIcon = { Icon(Icons.Filled.RemoveCircleOutline, contentDescription = null, tint = Palette.accentRed) },
                    onClick = {
                        showMenu = false
                        onRemove()
                    }
                )
            }
        }
    }
}

@Composable
fun StaffStatusChip(status: StaffStatus) {
    val (bgColor, textColor, label) = when (status) {
        StaffStatus.Active -> Triple(Palette.accentGreen.copy(alpha = 0.15f), Palette.accentGreen, "Active")
        StaffStatus.Invited -> Triple(Palette.goldPrimary.copy(alpha = 0.15f), Palette.goldPrimary, "Invited")
        StaffStatus.Inactive -> Triple(Palette.surface, Palette.textSecondary, "Inactive")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = Typography.santoshi(11.sp, FontWeight.SemiBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
