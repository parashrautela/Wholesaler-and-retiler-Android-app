package com.jewelindia.app.feature.wholesaler.chamak

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.prefs.Prefs
import com.jewelindia.app.core.security.ProtectedImage
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.data.api.ChamakStatus
import com.jewelindia.app.design.Manrope
import com.jewelindia.app.design.Palette
import com.jewelindia.app.feature.wholesaler.AddProductPrefill
import com.jewelindia.app.feature.wholesaler.AddProductSheet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

private const val ADDED_KEY = "chamak.addedToCatalogue"

private class UploadedSource(val url: String, val title: String, val jewelleryType: String)

/** A stored source photo uploaded from the device, not a catalogue image. */
private fun isDeviceUpload(url: String): Boolean =
    "/raw/" in url && ("/chamak_" in url || "/setcreation_" in url)

/** The analysis's word for a piece, as the Add Product form's type. Blank when the form has none. */
private fun catalogueType(analysisType: String?): String = when (analysisType) {
    "necklace", "choker" -> "necklace"
    "long_necklace" -> "haram"
    "pendant" -> "pendant"
    "mangalsutra" -> "mangalsutra"
    "earrings", "jhumka" -> "earrings"
    "ring" -> "rings"
    "bangle", "kada" -> "bangles"
    "nose_ring" -> "nosepins"
    else -> ""
}

/**
 * Most wholesalers upload Chamak photos straight from their phone, so those
 * designs never reach their catalogue. This offers each one, one tap into the
 * normal Add Product flow. Read from the stored URLs, so it also works for a
 * design reopened from the archive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChamakAddToCatalogueSection(vm: ChamakViewModel, wholesalerId: String) {
    val gen = vm.currentGeneration ?: return
    if (gen.status != ChamakStatus.Done) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context).sharedPreferences }

    // Survives relaunches so a design isn't offered, and paid for, twice.
    var added by remember { mutableStateOf(prefs.getStringSet(ADDED_KEY, emptySet()).orEmpty()) }
    var loadingUrl by remember { mutableStateOf<String?>(null) }
    var failedUrl by remember { mutableStateOf<String?>(null) }
    var openSheet by remember { mutableStateOf<Pair<String, AddProductPrefill>?>(null) }

    val isSet = gen.mode == ChamakMode.SetCreation
    val analysis = gen.stage1AnalysisJSON
    val sources = listOfNotNull(
        UploadedSource(gen.sourceImage1URL, vm.selectedDesign1?.title ?: if (isSet) "Piece 1" else "Core design", catalogueType(analysis?.image1Type)),
        UploadedSource(gen.sourceImage2URL, vm.selectedDesign2?.title ?: if (isSet) "Piece 2" else "New direction", catalogueType(analysis?.image2Type)),
        gen.sourceImage3URL?.let { UploadedSource(it, vm.selectedDesign3?.title ?: "Piece 3", "") },
        gen.sourceImage4URL?.let { UploadedSource(it, vm.selectedDesign4?.title ?: "Piece 4", "") }
    ).filter { isDeviceUpload(it.url) }

    if (sources.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE7E5E4), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Add your designs to catalogue",
                fontFamily = Manrope,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
            Text(
                text = "Turn the photos you uploaded into studio images.",
                fontFamily = Manrope,
                fontSize = 12.sp,
                color = Palette.muted
            )
        }

        sources.forEach { source ->
            val failed = failedUrl == source.url
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProtectedImage(
                    url = source.url,
                    contentDescription = source.title,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(10.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.title,
                        fontFamily = Manrope,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.dark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (failed) {
                        Text(
                            text = "Couldn't load this photo. Please try again.",
                            fontFamily = Manrope,
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
                if (source.url in added) {
                    Text(
                        text = "✓ Added",
                        fontFamily = Manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .widthIn(min = 72.dp)
                            .clip(CircleShape)
                            .background(Palette.dark)
                            .clickable(enabled = loadingUrl == null) {
                                loadingUrl = source.url
                                failedUrl = null
                                scope.launch {
                                    try {
                                        val bytes = withContext(Dispatchers.IO) { fetchPhoto(source.url) }
                                        openSheet = source.url to AddProductPrefill(bytes, source.jewelleryType)
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (_: Exception) {
                                        failedUrl = source.url
                                    } finally {
                                        loadingUrl = null
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (loadingUrl == source.url) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (failed) "Try again" else "Add",
                                fontFamily = Manrope,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    openSheet?.let { (sourceUrl, prefill) ->
        ModalBottomSheet(
            onDismissRequest = { openSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            AddProductSheet(
                wholesalerId = wholesalerId,
                userId = wholesalerId,
                onDismiss = { openSheet = null },
                onAdded = {
                    added = added + sourceUrl
                    prefs.edit().putStringSet(ADDED_KEY, added).apply()
                },
                prefill = prefill
            )
        }
    }
}

private fun fetchPhoto(url: String): ByteArray {
    val connection = URL(url).openConnection().apply {
        connectTimeout = 20_000
        readTimeout = 30_000
    }
    val bytes = connection.getInputStream().use { it.readBytes() }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    check(bounds.outWidth > 0) { "Not an image" }
    return bytes
}
