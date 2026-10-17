// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/AddEmployeeSheet.swift
package com.jewelindia.app.feature.retailer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.data.api.StaffAccountsApi
import com.jewelindia.app.data.api.StaffCredentials
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeSheet(
    onDismiss: () -> Unit,
    onEmployeeCreated: (StaffCredentials?) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Username & Password, 1: Google Account

    // Form Fields
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var isSuggesting by remember { mutableStateOf(false) }
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
                    text = "Add Staff Member",
                    style = Typography.cirka(22.sp),
                    color = Palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Palette.textSecondary)
                }
            }

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Palette.surfaceElevated,
                contentColor = Palette.goldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; errorMessage = null },
                    text = {
                        Text(
                            text = "Username & Password",
                            style = Typography.santoshi(14.sp, if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium),
                            color = if (selectedTab == 0) Palette.goldPrimary else Palette.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; errorMessage = null },
                    text = {
                        Text(
                            text = "Google Account",
                            style = Typography.santoshi(14.sp, if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium),
                            color = if (selectedTab == 1) Palette.goldPrimary else Palette.textSecondary
                        )
                    }
                )
            }

            // Common: Full Name
            OutlinedTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    if (selectedTab == 0 && username.isBlank() && it.isNotBlank()) {
                        scope.launch {
                            try {
                                username = StaffAccountsApi.suggestUsername(it)
                            } catch (_: Exception) {}
                        }
                    }
                },
                label = { Text("Full Name *", color = Palette.textSecondary) },
                placeholder = { Text("e.g. Ramesh Kumar", color = Palette.textTertiary) },
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

            if (selectedTab == 0) {
                // Username field with suggest button
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { char -> char.isLetterOrDigit() || char == '.' || char == '_' } },
                    label = { Text("Username *", color = Palette.textSecondary) },
                    placeholder = { Text("e.g. ramesh.jewels", color = Palette.textTertiary) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (fullName.isNotBlank() && !isSuggesting) {
                                    isSuggesting = true
                                    scope.launch {
                                        try {
                                            username = StaffAccountsApi.suggestUsername(fullName)
                                        } catch (_: Exception) {
                                        } finally {
                                            isSuggesting = false
                                        }
                                    }
                                }
                            }
                        ) {
                            if (isSuggesting) {
                                CircularProgressIndicator(color = Palette.goldPrimary, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Suggest", tint = Palette.goldPrimary)
                            }
                        }
                    },
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
            } else {
                // Google Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Google Account Email *", color = Palette.textSecondary) },
                    placeholder = { Text("e.g. ramesh@gmail.com", color = Palette.textTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
            }

            // Designation
            OutlinedTextField(
                value = designation,
                onValueChange = { designation = it },
                label = { Text("Designation / Role", color = Palette.textSecondary) },
                placeholder = { Text("e.g. Senior Sales Executive", color = Palette.textTertiary) },
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

            // Phone
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

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }

            Button(
                onClick = {
                    if (fullName.isBlank()) {
                        errorMessage = "Please enter staff member's full name."
                        return@Button
                    }

                    if (selectedTab == 0 && username.isBlank()) {
                        errorMessage = "Please enter or generate a username."
                        return@Button
                    }

                    if (selectedTab == 1 && (email.isBlank() || !email.contains("@"))) {
                        errorMessage = "Please enter a valid Google email address."
                        return@Button
                    }

                    isSubmitting = true
                    errorMessage = null

                    scope.launch {
                        try {
                            if (selectedTab == 0) {
                                val creds = StaffAccountsApi.createLogin(
                                    fullName = fullName.trim(),
                                    username = username.trim(),
                                    designation = designation.ifBlank { null },
                                    phone = phone.ifBlank { null }
                                )
                                onEmployeeCreated(creds)
                            } else {
                                StaffAccountsApi.inviteGoogle(
                                    fullName = fullName.trim(),
                                    email = email.trim(),
                                    designation = designation.ifBlank { null },
                                    phone = phone.ifBlank { null }
                                )
                                onEmployeeCreated(null)
                            }
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to create staff account. Please try again."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                } else {
                    Text(
                        text = if (selectedTab == 0) "Generate Staff Login" else "Send Staff Invite",
                        style = Typography.santoshi(16.sp, FontWeight.Bold),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCredentialsSheet(
    credentials: StaffCredentials,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val shareText = "Jewel India Staff Login Credentials:\n" +
            "Name: ${credentials.member.fullName}\n" +
            "Username: ${credentials.member.authUsername}\n" +
            "Password: ${credentials.password}\n" +
            "Portal: https://app.jewelindia.shop/staff"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Palette.surfaceElevated,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Palette.goldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Staff Login Created",
                style = Typography.cirka(22.sp),
                color = Palette.textPrimary
            )

            Text(
                text = "Please share these one-time credentials with ${credentials.member.fullName}. They can sign in on the staff portal immediately.",
                style = Typography.santoshi(14.sp),
                color = Palette.textSecondary,
                textAlign = TextAlign.Center
            )

            // Credentials Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Palette.surface)
                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Username", style = Typography.santoshi(13.sp), color = Palette.textSecondary)
                    Text(
                        text = credentials.member.authUsername ?: credentials.member.email,
                        style = Typography.santoshi(14.sp, FontWeight.Bold),
                        color = Palette.textPrimary
                    )
                }

                HorizontalDivider(color = Palette.surfaceBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Temporary Password", style = Typography.santoshi(13.sp), color = Palette.textSecondary)
                    Text(
                        text = credentials.password,
                        style = Typography.santoshi(14.sp, FontWeight.Bold),
                        fontFamily = FontFamily.Monospace,
                        color = Palette.goldPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(shareText))
                        Toast.makeText(context, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.surface),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Palette.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", color = Palette.textPrimary)
                }

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Staff Credentials"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
