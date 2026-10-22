// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/AddProductView.swift
package com.jewelindia.app.feature.wholesaler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.consent.rememberAIConsentRequest
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun AddProductScreen(
    userId: String,
    wholesalerId: String = "",
    previewBalance: Int? = null,
    previewState: AddProductFormState? = null,
    onClose: (() -> Unit)? = null,
    onOpenUploadHistory: (() -> Unit)? = null,
    onAdded: ((Product) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val requestAIConsent = rememberAIConsentRequest(wholesalerId.ifBlank { userId })
    val form = previewState ?: remember { AddProductFormState() }

    var showSuccess by remember { mutableStateOf(false) }
    var savedAsDraft by remember { mutableStateOf(false) }
    var completionDelivered by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            form.selectedUri = uri
            scope.launch {
                try {
                    form.imageBytes = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                } catch (e: CancellationException) { throw e }
                catch (_: Exception) { form.bannerError = "Couldn't open that photo. Please choose it again." }
            }
        }
    }

    LaunchedEffect(wholesalerId) {
        if (wholesalerId.isNotBlank()) {
            form.usageLoading = true
            form.usage = WholesalerApi.fetchUploadUsage(wholesalerId)
            form.usageLoading = false
        }
    }

    if (showSuccess) {
        AddProductSuccessView(
            savedAsDraft = savedAsDraft,
            onClose = {
                if (!completionDelivered) {
                    completionDelivered = true
                    form.submittedProduct?.let { onAdded?.invoke(it) }
                    onClose?.invoke()
                }
            }
        )
        return
    }

    if (form.status.isBusy) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CircularProgressIndicator(
                    color = Palette.dark,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = form.status.caption.uppercase(),
                    style = Typography.santoshi(14.sp),
                    letterSpacing = 1.4.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // iOS Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (onClose != null) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { onClose() }
                ) {
                    Text(
                        text = "Cancel",
                        style = Typography.santoshi(15.sp, FontWeight.Medium),
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "Add new product",
                style = Typography.santoshi(17.sp, FontWeight.SemiBold),
                color = Color(0xFF111827)
            )

            if (onClose != null && onOpenUploadHistory != null) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .clickable { onOpenUploadHistory() }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Upload History",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Add new product",
                    style = Typography.cirka(28.sp, FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Enter the details below to create a sparkling new listing.",
                    style = Typography.santoshi(16.sp, FontWeight.Medium),
                    color = Color(0xFF6B7280)
                )
            }

            // Error Banner
            form.bannerError?.let { banner ->
                ErrorBanner(message = banner)
            }

            // Usage Banner
            AddProductProgressBanner(
                usage = form.usage,
                isLimitReached = form.isLimitReached
            )

            // Section 1: Product image
            AddProductPhotoSection(
                form = form,
                onPickImage = { pickImageLauncher.launch("image/*") }
            )

            // Section 2: Essential details
            AddProductDetailsSection(form = form)

            // Section 3: Specifications
            AddProductSpecsSection(form = form)

            // Submit area
            AddProductSubmitSection(
                form = form,
                previewBalance = previewBalance,
                onSubmit = { publish ->
                    val submit: () -> Unit = {
                        scope.launch {
                            form.submit(
                                context = context,
                                wholesalerId = wholesalerId.ifBlank { userId },
                                publish = publish,
                                onSuccess = { isDraft ->
                                    savedAsDraft = isDraft
                                    showSuccess = true
                                }
                            )
                        }
                    }
                    if (publish && form.pendingProductId == null) requestAIConsent(submit) else submit()
                }
            )

            // Footer
            AddProductFooter()
        }
    }
}
