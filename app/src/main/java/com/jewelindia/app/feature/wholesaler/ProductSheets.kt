// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/ProductDetailView.swift + AddProductView.swift
package com.jewelindia.app.feature.wholesaler

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.ui.text.style.TextAlign
import com.jewelindia.app.design.Typography
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import coil3.compose.AsyncImage
import com.jewelindia.app.core.images.PickedFile
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UploadUsage
import com.jewelindia.app.data.api.WholesalerApi
import com.jewelindia.app.design.Palette
import kotlinx.coroutines.launch
import java.util.UUID

private val JEWELLERY_TYPES = listOf(
    "Ring", "Necklace", "Earring", "Bracelet", "Bangle", "Pendant", "Chain",
    "Mangalsutra", "Nosering", "Haram", "Anklet", "Brooch"
)

private val PURITY_OPTIONS = listOf("18K", "22K", "24K", "Sterling Silver", "Other")



// ─── Add Product Sheet ──────────────────────────────────────────────────────

@Composable
fun AddProductSheet(
    wholesalerId: String,
    userId: String,
    onDismiss: () -> Unit,
    onAdded: (Product) -> Unit,
    onOpenUploadHistory: (() -> Unit)? = null
) {
    AddProductScreen(
        wholesalerId = wholesalerId,
        userId = userId,
        onClose = onDismiss,
        onOpenUploadHistory = onOpenUploadHistory,
        onAdded = onAdded
    )
}

/**
 * Success view matching `/dashboard/wholesaler/add-product/success`.
 */
@Composable
fun AddProductSuccessView(
    savedAsDraft: Boolean = false,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (savedAsDraft) "Saved for later" else "Submitted",
                style = Typography.cirka(44.sp),
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = if (savedAsDraft) {
                    "Your product details are saved as a draft. Add a photo and submit it whenever you're ready."
                } else {
                    "Your photo has been submitted for AI processing. Return to your catalogue to see its progress and finished images."
                },
                style = Typography.santoshi(16.sp),
                color = Color(0xFF6B6B6B),
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(340.dp)
                    .padding(bottom = 36.dp)
            )

            Box(
                modifier = Modifier
                    .width(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A0A0A))
                    .clickable { onClose() }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    style = Typography.santoshi(16.sp, FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}


