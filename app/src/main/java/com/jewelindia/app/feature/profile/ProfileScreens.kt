// Port of: wholesaler ios/JewelIndia/Sources/Features/Profile/ProfileViews.swift (WholesalerProfileView)
package com.jewelindia.app.feature.profile

import android.content.Context
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.jewelindia.app.data.net.userMessage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.core.legal.LegalLinks
import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.AccountApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.design.readableContentWidth
import kotlinx.coroutines.launch

@Composable
fun WholesalerProfileScreen(
    wholesaler: Wholesaler?,
    sessionStore: SessionStore,
    onBack: (() -> Unit)? = null,
    onOpenTreasureChest: () -> Unit = {},
    onInviteRetailer: () -> Unit = {}
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
            .background(Color(0xFFF2F2F7))
            .readableContentWidth()
            .statusBarsPadding()
    ) {
        // Centered iOS Profile title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Palette.dark
                    )
                }
            }
            Text(
                text = "Profile",
                style = Typography.santoshi(17.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circle Avatar 76dp
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E7571))
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JI",
                        style = Typography.cirka(32.sp),
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    )
                }

                Text(
                    text = wholesaler?.businessName ?: "Parash Jewels",
                    style = Typography.cirka(24.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1917),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = wholesaler?.fullName ?: "Parash Rautela",
                    style = Typography.santoshi(13.sp),
                    color = Color(0xFF78716C),
                    textAlign = TextAlign.Center
                )

                // Verified green pill
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Verified",
                        style = Typography.santoshi(11.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Account Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Account",
                    style = Typography.santoshi(13.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    ProfileDataRow(
                        label = "Email",
                        value = wholesaler?.email ?: "parash@example.com"
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF2F2F2), modifier = Modifier.padding(start = 16.dp))
                    ProfileDataRow(
                        label = "Location",
                        value = listOfNotNull(wholesaler?.city ?: "Bengaluru", wholesaler?.state ?: "Karnataka").joinToString(", ")
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF2F2F2), modifier = Modifier.padding(start = 16.dp))
                    ProfileDataRow(
                        label = "Member since",
                        value = "September 2026"
                    )
                }
            }

            // Shortcuts Section (Treasure Chest & Invite a Retailer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                ProfileNavRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Treasure Chest",
                    showChevron = true,
                    onClick = onOpenTreasureChest
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF2F2F2), modifier = Modifier.padding(start = 52.dp))
                ProfileNavRow(
                    icon = Icons.Default.PersonAdd,
                    title = "Invite a Retailer",
                    showChevron = false,
                    onClick = onInviteRetailer
                )
            }

            // Legal Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                ProfileNavRow(
                    icon = Icons.Default.Description,
                    title = "Terms & Conditions",
                    showChevron = false,
                    onClick = { LegalLinks.open(context, LegalLinks.terms) }
                )
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF2F2F2), modifier = Modifier.padding(start = 52.dp))
                ProfileNavRow(
                    icon = Icons.Default.PanTool,
                    title = "Privacy Policy",
                    showChevron = false,
                    onClick = { LegalLinks.open(context, LegalLinks.privacy) }
                )
            }

            // Delete Account Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    ProfileNavRow(
                        icon = Icons.Default.Delete,
                        title = "Delete account",
                        titleColor = Color(0xFFEF4444),
                        iconTint = Color(0xFFEF4444),
                        showChevron = false,
                        onClick = { showDeleteDialog = true }
                    )
                }
                Text(
                    text = "Permanently deletes your account and associated catalogue data.",
                    style = Typography.santoshi(12.sp),
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Log Out Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
            ) {
                ProfileNavRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = Copy.logoutConfirm,
                    titleColor = Color(0xFFEF4444),
                    iconTint = Color(0xFFEF4444),
                    showChevron = false,
                    onClick = { showSignOutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(Copy.logoutTitle, style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = { Text(Copy.logoutBody, style = Typography.santoshi(14.sp)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        scope.launch { sessionStore.signOut() }
                    }
                ) {
                    Text(Copy.logoutConfirm, color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(Copy.logoutCancel)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleting) showDeleteDialog = false },
            title = { Text("Delete your account?", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This permanently deletes your Jewel India account and associated data. This can't be undone.",
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
                                deleteError = e.userMessage()
                                deleting = false
                            }
                        }
                    },
                    enabled = !deleting
                ) {
                    if (deleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete account permanently", color = Palette.accentRed, fontWeight = FontWeight.Bold)
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
private fun ProfileDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Typography.santoshi(14.sp),
            color = Color(0xFF8E8E93)
        )
        Text(
            text = value,
            style = Typography.santoshi(14.sp),
            color = Palette.dark,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    titleColor: Color = Palette.dark,
    iconTint: Color = Palette.dark,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = Typography.santoshi(14.sp),
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFC7C7CC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = Palette.dark,
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
