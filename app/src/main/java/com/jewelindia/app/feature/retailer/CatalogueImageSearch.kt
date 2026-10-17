// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/CatalogueImageSearch.swift
package com.jewelindia.app.feature.retailer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.images.ImageNormalizer
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.data.api.JewelApi
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class ImageSearchResponse(
    @SerialName("product_ids") val productIds: List<String> = emptyList(),
    val ids: List<String> = emptyList(),
    val results: List<ResultItem> = emptyList()
) {
    @Serializable
    data class ResultItem(
        @SerialName("product_id") val productId: String? = null,
        val id: String? = null,
        val score: Double? = null
    )

    val matchedIds: List<String>
        get() = (productIds + ids + results.mapNotNull { it.productId ?: it.id }).distinct()
}

object CatalogueImageSearchApi {
    suspend fun searchByImage(jpegBytes: ByteArray): List<Product> = withContext(Dispatchers.IO) {
        val token = AuthToken.fresh(JewelSupabase.client)
        val url = "${AppConfig.aiPipelineUrl.trimEnd('/')}/api/retailer/image-search"

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "search.jpg",
                jpegBytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        val responseBody = HttpClients.pipeline.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }
            response.body?.string().orEmpty()
        }

        val decoded = try {
            JewelJson.decodeFromString<ImageSearchResponse>(responseBody)
        } catch (_: Exception) {
            ImageSearchResponse()
        }

        if (decoded.matchedIds.isEmpty()) return@withContext emptyList()

        try {
            JewelApi.hydrateMarketplaceProducts(decoded.matchedIds).products
        } catch (_: Exception) {
            emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueImageSearchScreen(
    onDismiss: () -> Unit,
    onProductSelected: (Product) -> Unit,
    onToggleShortlist: (String) -> Unit,
    shortlistedIds: Set<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var searching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Product>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun processUri(uri: Uri) {
        selectedUri = uri
        searching = true
        errorMessage = null
        results = null

        scope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null || bytes.isEmpty()) {
                    errorMessage = "Could not read image"
                    searching = false
                    return@launch
                }
                val normalized = ImageNormalizer.jpeg(bytes, ImageNormalizer.MAX_PRODUCT_DIMENSION) ?: bytes
                val matches = CatalogueImageSearchApi.searchByImage(normalized)
                results = matches
            } catch (e: Exception) {
                errorMessage = e.message ?: "Visual search failed. Please try another image."
            } finally {
                searching = false
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) processUri(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Visual Search",
                        style = Typography.cirka(20.sp),
                        color = Palette.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Palette.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Palette.surface)
            )
        },
        containerColor = Palette.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Section: Image selector / Query Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Palette.surfaceElevated)
                    .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Search Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Palette.surfaceBorder, RoundedCornerShape(12.dp))
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Searching by photo",
                                style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                                color = Palette.textPrimary
                            )
                            Text(
                                text = if (searching) "Analyzing catalogue visual embeddings..." else "${results?.size ?: 0} matching designs found",
                                style = Typography.santoshi(13.sp),
                                color = Palette.textSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedUri = null
                                results = null
                                errorMessage = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear",
                                tint = Palette.textSecondary
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Palette.goldPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Palette.goldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = "Upload or Take a Photo",
                            style = Typography.santoshi(15.sp, FontWeight.SemiBold),
                            color = Palette.textPrimary
                        )

                        Text(
                            text = "Find visually similar designs across the marketplace",
                            style = Typography.santoshi(13.sp),
                            color = Palette.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Body: Search state / Results grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    searching -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Palette.goldPrimary)
                            Text(
                                text = "Matching visual patterns...",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary
                            )
                        }
                    }

                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.santoshi(14.sp),
                            color = Palette.accentRed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    results != null -> {
                        val items = results.orEmpty()
                        if (items.isEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "No matching designs found",
                                    style = Typography.cirka(18.sp),
                                    color = Palette.textPrimary
                                )
                                Text(
                                    text = "Try picking a clearer image or a different angle.",
                                    style = Typography.santoshi(14.sp),
                                    color = Palette.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(items, key = { it.id }) { product ->
                                    MarketplaceProductCard(
                                        product = product,
                                        isShortlisted = shortlistedIds.contains(product.id),
                                        onToggleShortlist = { onToggleShortlist(product.id) },
                                        onClick = { onProductSelected(product) }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ImageSearch,
                                contentDescription = null,
                                tint = Palette.textTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Visual Catalogue Search",
                                style = Typography.cirka(20.sp),
                                color = Palette.textPrimary
                            )
                            Text(
                                text = "Select any jewellery photo from your gallery to instantly find identical or matching jewellery across all verified wholesalers.",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
