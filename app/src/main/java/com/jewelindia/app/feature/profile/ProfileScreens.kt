// Port of: wholesaler ios/JewelIndia/Sources/Features/Profile/ProfileViews.swift
package com.jewelindia.app.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.legal.LegalLinks
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.data.api.AccountApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun WholesalerProfileScreen(
    wholesaler: Wholesaler?,
    sessionStore: SessionStore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
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
                text = "Profile & Settings",
                style = Typography.cirka(20.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Business Details Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (wholesaler?.businessName?.firstOrNull() ?: wholesaler?.fullName?.firstOrNull() ?: 'W').uppercase(),
                            style = Typography.cirka(24.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.goldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = wholesaler?.businessName ?: wholesaler?.fullName ?: "Wholesaler",
                            style = Typography.cirka(18.sp),
                            fontWeight = FontWeight.Bold,
                            color = Palette.dark
                        )
                        wholesaler?.fullName?.let {
                            Text(text = it, style = Typography.santoshi(14.sp), color = Palette.muted)
                        }
                        wholesaler?.city?.let { city ->
                            val loc = listOfNotNull(city, wholesaler.state).joinToString(", ")
                            Text(text = loc, style = Typography.santoshi(12.sp), color = Palette.muted)
                        }
                    }
                }
            }

            // Legal & Information
            Text(
                text = "Legal & Support",
                style = Typography.santoshi(13.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.muted,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    onClick = { LegalLinks.open(context, LegalLinks.terms) }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    onClick = { LegalLinks.open(context, LegalLinks.privacy) }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Support & Help",
                    onClick = { LegalLinks.open(context, LegalLinks.support) }
                )
            }

            // Account Actions
            Text(
                text = "Account",
                style = Typography.santoshi(13.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.muted,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Log Out",
                    titleColor = Palette.dark,
                    onClick = { showSignOutDialog = true }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    titleColor = Palette.accentRed,
                    onClick = { showDeleteDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Log Out", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of Jewel India?", style = Typography.santoshi(14.sp)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch { sessionStore.signOut() }
                    }
                ) {
                    Text("Log Out", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleting) showDeleteDialog = false },
            title = { Text("Delete Account", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Are you sure you want to permanently delete your Jewel India account? All your catalogues, products, and details will be erased.",
                        style = Typography.santoshi(14.sp)
                    )
                    if (deleteError != null) {
                        Text(text = deleteError ?: "", color = Palette.accentRed, style = Typography.santoshi(12.sp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            deleting = true
                            deleteError = null
                            try {
                                AccountApi.deleteMyAccount()
                                sessionStore.signOut()
                            } catch (e: Exception) {
                                deleteError = e.message ?: "Deletion failed"
                                deleting = false
                            }
                        }
                    },
                    enabled = !deleting
                ) {
                    if (deleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete Permanently", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !deleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = Palette.dark,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = titleColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = Typography.santoshi(15.sp),
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Palette.muted
        )
    }
}
