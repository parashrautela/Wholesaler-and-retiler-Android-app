// Port of: wholesaler ios/JewelIndia/Sources/Features/Profile/ProfileViews.swift (Staff profile section)
package com.jewelindia.app.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.legal.LegalLinks
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.EmployeeSession
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffDetails(
    @SerialName("full_name") val name: String? = null,
    @SerialName("email") val username: String? = null,
    val designation: String? = null,
    val phone: String? = null
)

@Composable
fun StaffProfileButton(
    action: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = action,
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StaffProfileScreen(
    session: EmployeeSession,
    sessionStore: SessionStore,
    onDismiss: () -> Unit,
    onBackToDashboard: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var me by remember { mutableStateOf<StaffDetails?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmDeleteAccount by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var accountError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(session) {
        val empId = (session.identity as? EmployeeSession.Identity.Employee)?.id
        if (!empId.isNullOrBlank()) {
            try {
                me = JewelSupabase.client.from("employees")
                    .select(Columns.list("full_name", "email", "designation", "phone")) {
                        filter { eq("id", empId) }
                    }
                    .decodeSingle<StaffDetails>()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = Typography.cirka(22.sp),
                color = Palette.textPrimary
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Palette.textPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!session.storeLogoURL.isNullOrBlank()) {
                    CachedImage(
                        url = session.storeLogoURL,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(1.dp, Palette.border, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Palette.taupe),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Palette.dark,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = me?.name ?: if (session.isRetailer) session.storeName else "Staff Member",
                        style = Typography.cirka(18.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                    Text(
                        text = if (session.isRetailer) "Store owner · Employee View" else session.storeName,
                        style = Typography.santoshi(13.sp),
                        color = Palette.muted
                    )
                    me?.designation?.let { desig ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Palette.goldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = desig,
                                style = Typography.santoshi(11.sp, FontWeight.SemiBold),
                                color = Palette.dark
                            )
                        }
                    }
                }
            }

            // If store owner: Back to Store Dashboard button
            if (session.isRetailer) {
                Surface(
                    onClick = {
                        onDismiss()
                        onBackToDashboard()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Palette.cream,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Palette.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.GridView, contentDescription = null, tint = Palette.dark)
                        Text(
                            text = "Back to Store Dashboard",
                            style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                            color = Palette.dark
                        )
                    }
                }
            } else {
                // Employee Account Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Palette.cream)
                        .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ACCOUNT",
                        style = Typography.santoshi(11.sp, FontWeight.Bold),
                        color = Palette.muted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ProfileRowItem("Username", me?.username ?: "—")
                    ProfileRowItem("Phone", me?.phone ?: "—")
                    ProfileRowItem("Store", session.storeName)
                }
            }

            // Legal Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.cream)
                    .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    onClick = {
                        LegalLinks.open(context, LegalLinks.terms)
                    }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy Policy",
                    onClick = {
                        LegalLinks.open(context, LegalLinks.privacy)
                    }
                )
            }

            accountError?.let { err ->
                Text(err, color = Palette.accentRed, style = Typography.santoshi(13.sp))
            }

            // Delete Account Section (only for employees, not retailer store owners)
            if (!session.isRetailer) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { confirmDeleteAccount = true },
                        enabled = !isDeletingAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Palette.accentRed.copy(alpha = 0.12f),
                            contentColor = Palette.accentRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isDeletingAccount) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Palette.accentRed)
                        } else {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Delete account", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(
                        text = "Permanently deletes your employee account. Your store's account and catalogue remain.",
                        style = Typography.santoshi(12.sp),
                        color = Palette.muted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Log Out Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { confirmLogout = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Palette.dark,
                        contentColor = Palette.light
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Copy.logoutConfirm, fontWeight = FontWeight.SemiBold)
                }
                if (!session.isRetailer) {
                    Text(
                        text = "Forgot your password? Ask your store owner to reset it.",
                        style = Typography.santoshi(12.sp),
                        color = Palette.muted,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(Copy.logoutTitle, style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = { Text(Copy.logoutBody, style = Typography.santoshi(14.sp)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        scope.launch { sessionStore.signOut() }
                    }
                ) {
                    Text(Copy.logoutConfirm, color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text(Copy.logoutCancel)
                }
            }
        )
    }

    if (confirmDeleteAccount) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAccount = false },
            title = { Text("Delete your account?", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = { Text("This permanently deletes your employee login and profile. This can't be undone.", style = Typography.santoshi(14.sp)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDeleteAccount = false
                        scope.launch {
                            isDeletingAccount = true
                            accountError = null
                            try {
                                WholesalerApi.deleteMyAccount()
                                sessionStore.signOut()
                            } catch (e: Exception) {
                                accountError = "Couldn't delete the account: ${e.message ?: "Please try again."}"
                                isDeletingAccount = false
                            }
                        }
                    }
                ) {
                    Text("Delete account permanently", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAccount = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = Typography.santoshi(14.sp), color = Palette.muted)
        Text(text = value, style = Typography.santoshi(14.sp, FontWeight.Medium), color = Palette.dark)
    }
}
