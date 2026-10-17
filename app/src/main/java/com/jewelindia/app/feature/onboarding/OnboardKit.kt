// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardKit.swift
package com.jewelindia.app.feature.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.jewelindia.app.R
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.images.PickedFile
import com.jewelindia.app.core.model.VerificationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object OnboardColor {
    val text = Color(0xFF374151)
    val heading = Color(0xFF111827)
    val subtle = Color(0xFF9CA3AF)
    val fieldFill = Color(0xFFF5F5F5)
    val border = Color(0xFFE5E7EB)
    val navBorder = Color(0xFFE0E0E0)
    val danger = Color(0xFFEF4444)
    val uploadFill = Color(0xFFEEF4FF)
    val uploadBorder = Color(0xFF2563EB)
    val uploadFilled = Color(0xFF22C55E)
    val disabled = Color(0xFFD1D5DB)
    val submitting = Color(0xFF6B7280)
}

/**
 * Top bar matching iOS `OnboardNavbar`.
 * Shows Back button, optional Refresh button, and Sign out button.
 */
@Composable
fun OnboardNavbar(
    gutter: Dp,
    showsBack: Boolean,
    onBack: (() -> Unit)?,
    onSignOut: () -> Unit,
    onRefresh: (suspend () -> Unit)? = null,
    isRefreshing: Boolean = false
) {
    var confirmSignOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = gutter, vertical = 10.dp)
            .drawBehind {
                drawLine(
                    color = OnboardColor.navBorder,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showsBack && onBack != null) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnboardColor.text,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Back",
                    fontSize = 14.sp,
                    color = OnboardColor.text,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (onRefresh != null) {
            val infiniteTransition = rememberInfiniteTransition(label = "refresh")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isRefreshing) {
                        scope.launch { onRefresh() }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isRefreshing) OnboardColor.subtle else OnboardColor.text,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(if (isRefreshing) rotation else 0f)
                )
                Text(
                    text = if (isRefreshing) "Checking..." else "Refresh",
                    fontSize = 14.sp,
                    color = if (isRefreshing) OnboardColor.subtle else OnboardColor.text
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { confirmSignOut = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Sign out",
                tint = OnboardColor.text,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Sign out",
                fontSize = 14.sp,
                color = OnboardColor.text
            )
        }
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need to sign in again to finish onboarding.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    onSignOut()
                }) {
                    Text("Sign out", color = OnboardColor.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Left branding & step panel matching iOS `LeftPanel`.
 */
@Composable
fun LeftPanel(
    heading: String,
    subheading: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "Jewel Logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(7.dp))
            )
            Text(
                text = "Jewels India",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = OnboardColor.heading,
                letterSpacing = 0.4.sp
            )
        }

        Text(
            text = heading,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = OnboardColor.heading,
            lineHeight = 26.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = subheading,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Medium,
            color = OnboardColor.subtle,
            lineHeight = 18.sp
        )
    }
}

/**
 * Step progress bar matching iOS `StepIndicator`.
 */
@Composable
fun StepIndicator(
    step: Int,
    totalSteps: Int = 3
) {
    val progress = remember { Animatable(0f) }
    val target = (step - 1) / totalSteps.toFloat()

    LaunchedEffect(step) {
        progress.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = 800,
                easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row {
            Text(
                text = "STEP $step",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = OnboardColor.heading,
                letterSpacing = 1.2.sp
            )
            Text(
                text = " OF $totalSteps",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = OnboardColor.subtle,
                letterSpacing = 1.2.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(OnboardColor.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.value.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(OnboardColor.heading)
            )
        }
    }
}

/**
 * Onboard layout wrapper matching iOS `OnboardLayout`.
 */
@Composable
fun OnboardLayout(
    step: Int,
    heading: String,
    subheading: String,
    showsBack: Boolean = true,
    onBack: (() -> Unit)? = null,
    onSignOut: () -> Unit,
    content: @Composable () -> Unit
) {
    val gutter = 20.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        OnboardNavbar(
            gutter = gutter,
            showsBack = showsBack,
            onBack = onBack,
            onSignOut = onSignOut
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = gutter)
                .padding(top = 24.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                LeftPanel(heading = heading, subheading = subheading)
                StepIndicator(step = step, totalSteps = 3)
            }

            content()
        }
    }
}

/**
 * Text field with rounded 8dp corners, #F5F5F5 fill, error state, and keystroke filter.
 */
@Composable
fun OnboardTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    disabled: Boolean = false,
    filter: ((String) -> String)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnboardColor.text
        )

        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                val filtered = if (filter != null) filter(input) else input
                onValueChange(filtered)
            },
            placeholder = {
                Text(placeholder, fontSize = 15.sp, color = OnboardColor.subtle)
            },
            enabled = !disabled,
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = OnboardColor.fieldFill,
                unfocusedContainerColor = OnboardColor.fieldFill,
                disabledContainerColor = Color(0xFFF9FAFB),
                focusedBorderColor = if (error != null) OnboardColor.danger else Color.Black.copy(alpha = 0.15f),
                unfocusedBorderColor = if (error != null) OnboardColor.danger else Color.Transparent,
                errorBorderColor = OnboardColor.danger,
                focusedTextColor = OnboardColor.text,
                unfocusedTextColor = OnboardColor.text
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                fontSize = 12.sp,
                color = OnboardColor.danger
            )
        }
    }
}

/**
 * Image / document upload tile matching `components/onboard/ImageUploadBox.jsx`.
 * Supports camera capture, photo library picking, and PDF/document selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageUploadBox(
    label: String,
    file: PickedFile?,
    onFileSelected: (PickedFile?) -> Unit,
    error: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    allowsPDF: Boolean = false
) {
    val context = LocalContext.current
    var showSourceChoice by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()

    val displayedError = error ?: loadError
    val borderColor = when {
        file != null -> OnboardColor.uploadFilled
        displayedError != null -> OnboardColor.danger
        else -> OnboardColor.uploadBorder
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                loadError = null
                val picked = readUriToPickedFile(context, uri)
                isLoading = false
                if (picked != null) {
                    onFileSelected(ImageNormalizer.normalized(picked, ImageNormalizer.MAX_DOCUMENT_DIMENSION))
                } else {
                    loadError = "Couldn't read that file. Please try again."
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            scope.launch {
                isLoading = true
                loadError = null
                val picked = readUriToPickedFile(context, tempCameraUri!!)
                isLoading = false
                if (picked != null) {
                    onFileSelected(ImageNormalizer.normalized(picked, ImageNormalizer.MAX_DOCUMENT_DIMENSION))
                } else {
                    loadError = "Couldn't read photo. Please try again."
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnboardColor.text
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(OnboardColor.uploadFill)
                .drawBehind {
                    if (file != null) {
                        drawRoundRect(
                            color = borderColor,
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    } else {
                        drawRoundRect(
                            color = borderColor,
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                            )
                        )
                    }
                }
                .clickable(enabled = file == null && !isLoading) {
                    showSourceChoice = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = OnboardColor.uploadBorder,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(36.dp)
                )
            } else if (file != null) {
                if (file.isPDF) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "PDF Document",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = file.filename,
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1
                        )
                    }
                } else {
                    val bitmap = remember(file) {
                        try {
                            android.graphics.BitmapFactory.decodeByteArray(file.data, 0, file.data.size)
                        } catch (_: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = label,
                            contentScale = contentScale,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Remove button at top-right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onFileSelected(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = OnboardColor.text,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload",
                        tint = borderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (displayedError != null) {
            Text(
                text = displayedError,
                fontSize = 12.sp,
                color = OnboardColor.danger
            )
        }
    }

    if (showSourceChoice) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showSourceChoice = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add ${label.replace("*", "").trim()}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardColor.heading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showSourceChoice = false
                            val uri = createCameraUri(context)
                            tempCameraUri = uri
                            if (uri != null) {
                                cameraLauncher.launch(uri)
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Take Photo", fontSize = 16.sp, color = OnboardColor.text)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showSourceChoice = false
                            photoPickerLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Choose from Gallery", fontSize = 16.sp, color = OnboardColor.text)
                }

                if (allowsPDF) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showSourceChoice = false
                                photoPickerLauncher.launch("*/*")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Browse Documents (PDF or Images)", fontSize = 16.sp, color = OnboardColor.text)
                    }
                }
            }
        }
    }
}

/**
 * Onboard primary CTA button matching iOS `OnboardPrimaryButton`.
 */
@Composable
fun OnboardPrimaryButton(
    title: String,
    busyTitle: String? = null,
    isBusy: Boolean = false,
    isEnabled: Boolean = true,
    action: () -> Unit
) {
    val bgColor = when {
        isBusy -> OnboardColor.submitting
        isEnabled -> Color.Black
        else -> OnboardColor.disabled
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(enabled = isEnabled && !isBusy) { action() }
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isBusy) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = if (isBusy) (busyTitle ?: title) else title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Legal confidentiality note matching iOS `OnboardLegalNote`.
 */
@Composable
fun OnboardLegalNote() {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "*Your documents are encrypted and only used for verification.",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "We never share them.",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

/**
 * Two-stage verification timeline matching iOS `VerificationTimeline`.
 */
@Composable
fun VerificationTimeline(
    status: VerificationStatus,
    stage: Int // 0..3
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Step 1: Submission
        val step1Done = stage >= 1
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (step1Done) OnboardColor.uploadFilled else OnboardColor.border),
                contentAlignment = Alignment.Center
            ) {
                if (step1Done) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(36.dp)
                    .background(if (stage >= 2) OnboardColor.uploadFilled else OnboardColor.border)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Application Submitted",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OnboardColor.heading
            )
            Text(
                text = "All documents received securely",
                fontSize = 12.sp,
                color = OnboardColor.subtle
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Verification
            val step2Active = stage >= 2
            val isVerified = status == VerificationStatus.verified
            val isRejected = status == VerificationStatus.rejected || status == VerificationStatus.resubmissionRequired

            Text(
                text = when {
                    isVerified -> "Verification Approved"
                    isRejected -> "Verification Needs Attention"
                    else -> "Under Review"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isVerified -> OnboardColor.uploadFilled
                    isRejected -> OnboardColor.danger
                    else -> OnboardColor.heading
                }
            )
            Text(
                text = when {
                    isVerified -> "Account active and approved"
                    isRejected -> "Action required to proceed"
                    else -> "Reviewed by our compliance team"
                },
                fontSize = 12.sp,
                color = OnboardColor.subtle
            )
        }
    }
}

private suspend fun readUriToPickedFile(context: Context, uri: Uri): PickedFile? = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
        val filename = uri.lastPathSegment ?: "upload.jpg"
        PickedFile(data = bytes, filename = filename, mimeType = mimeType)
    } catch (_: Exception) {
        null
    }
}

private fun createCameraUri(context: Context): Uri? {
    return try {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}
