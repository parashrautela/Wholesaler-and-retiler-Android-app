// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/ChamakExportButton.swift
package com.jewelindia.app.feature.wholesaler.chamak

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.jewelindia.app.data.api.CreditsApi
import com.jewelindia.app.data.api.StoreActivity
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun ChamakExportButton(
    generationId: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onInsufficientCredits: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (imageUrl.isNullOrBlank() || exporting) return@Button
            scope.launch {
                exporting = true
                try {
                    // Try to purchase export with credits
                    try {
                        val res = CreditsApi.purchaseExport(generationId)
                        if (res.isInsufficientCredits) {
                            exporting = false
                            onInsufficientCredits()
                            return@launch
                        }
                    } catch (_: Exception) {}

                    // Download clean image and share
                    val file = withContext(Dispatchers.IO) {
                        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                        val exportFile = File(exportDir, "chamak-${generationId.take(8)}.png")
                        URL(imageUrl).openStream().use { input ->
                            FileOutputStream(exportFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        exportFile
                    }

                    // Log activity
                    StoreActivity.log(StoreActivity.Event.ImageExported)

                    // Share intent
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Chamak Design"))
                } catch (_: Exception) {
                } finally {
                    exporting = false
                }
            }
        },
        enabled = !imageUrl.isNullOrBlank() && !exporting,
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22)),
        modifier = modifier.height(54.dp)
    ) {
        if (exporting) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Download 2K · Free",
                fontFamily = com.jewelindia.app.design.Manrope,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
