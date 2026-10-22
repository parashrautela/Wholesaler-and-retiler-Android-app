// Port of: wholesaler ios/JewelIndia/Sources/Features/Onboarding/OnboardKit.swift (ImageUploadBox)
package com.jewelindia.app.feature.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.images.PickedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

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
                    val normalized = withContext(Dispatchers.Default) {
                        ImageNormalizer.normalized(picked, ImageNormalizer.MAX_DOCUMENT_DIMENSION)
                    }
                    onFileSelected(normalized)
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
        val uri = tempCameraUri
        if (success && uri != null) {
            scope.launch {
                isLoading = true
                loadError = null
                val picked = readUriToPickedFile(context, uri)
                isLoading = false
                if (picked != null) {
                    val normalized = withContext(Dispatchers.Default) {
                        ImageNormalizer.normalized(picked, ImageNormalizer.MAX_DOCUMENT_DIMENSION)
                    }
                    onFileSelected(normalized)
                } else {
                    loadError = "Couldn't read captured photo."
                }
            }
        }
    }

    // PDF/Document launcher
    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                loadError = null
                val picked = readUriToPickedFile(context, uri)
                isLoading = false
                if (picked != null) {
                    onFileSelected(picked)
                } else {
                    loadError = "Couldn't read selected document."
                }
            }
        }
    }

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OnboardColor.fieldFill)
                .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
                .clickable {
                    if (file == null) {
                        showSourceChoice = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }

                file != null -> {
                    // Preview
                    if (file.isPDF) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF Document",
                                tint = OnboardColor.text,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = file.filename,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = OnboardColor.text
                            )
                        }
                    } else {
                        val bitmap = remember(file) {
                            BitmapFactory.decodeByteArray(file.data, 0, file.data.size)
                        }
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = label,
                                contentScale = contentScale,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Remove button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onFileSelected(null) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload",
                            tint = OnboardColor.subtle,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "Tap to upload",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnboardColor.subtle
                        )
                        Text(
                            text = if (allowsPDF) "JPEG, PNG, WEBP or PDF" else "JPEG, PNG or WEBP",
                            fontSize = 11.sp,
                            color = OnboardColor.subtle
                        )
                    }
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
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSourceChoice = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Document Source",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnboardColor.heading
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSourceChoice = false
                            val uri = createCameraUri(context)
                            tempCameraUri = uri
                            if (uri != null) {
                                try {
                                    cameraLauncher.launch(uri)
                                } catch (e: ActivityNotFoundException) {
                                    loadError = "No camera app found on this phone."
                                } catch (e: SecurityException) {
                                    loadError = "No camera app found on this phone."
                                }
                            }
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Take Photo", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showSourceChoice = false
                            photoPickerLauncher.launch("image/*")
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Choose from Gallery", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                if (allowsPDF) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceChoice = false
                                docLauncher.launch("application/pdf")
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "PDF",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Select PDF Document", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
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
