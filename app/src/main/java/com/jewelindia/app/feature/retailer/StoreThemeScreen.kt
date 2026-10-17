// Port of: wholesaler ios/JewelIndia/Sources/Features/Retailer/StoreThemeView.swift
package com.jewelindia.app.feature.retailer

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.jewelindia.app.core.prefs.Prefs
import com.jewelindia.app.data.api.CloudinaryArt
import com.jewelindia.app.data.api.CreditsApi
import com.jewelindia.app.data.api.RetailerApi
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.InsufficientCreditsSheet
import kotlinx.coroutines.launch

data class ThemeOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewImage: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val entitlementKey: String?,
    val creditCost: Int = 0
)

val StoreThemesList = listOf(
    ThemeOption(
        id = "indian",
        title = "Heritage Classic",
        subtitle = "Rich regal crimson & antique gold inspired by timeless royal courts.",
        previewImage = CloudinaryArt.url("v1778318369/home_bg_ryyopk.svg", 800),
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFF800020),
        entitlementKey = null,
        creditCost = 0
    ),
    ThemeOption(
        id = "maharaja",
        title = "Maharaja Palace",
        subtitle = "Opulent midnight navy & imperial gold with arched courtyard motifs.",
        previewImage = CloudinaryArt.url("v1781085325/Maharaja_Theme_ymaqjt.svg", 800),
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFF0F1E36),
        entitlementKey = null,
        creditCost = 0
    ),
    ThemeOption(
        id = "utsav",
        title = "Utsav Festival",
        subtitle = "Vibrant auspicious saffron, marigold gold & celebratory celebratory tones.",
        previewImage = CloudinaryArt.url("v1781118947/theme1_vertical_sclwlj.svg", 800),
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFF5D101D),
        entitlementKey = "theme.utsav",
        creditCost = 150
    ),
    ThemeOption(
        id = "neelam",
        title = "Neelam Sapphire",
        subtitle = "Modern minimal deep royal blue and platinum lustre for contemporary stores.",
        previewImage = CloudinaryArt.url("v1778318369/emp_static1_ywv9ro.svg", 800),
        primaryColor = Color(0xFF64B5F6),
        secondaryColor = Color(0xFF10192C),
        entitlementKey = "theme.neelam",
        creditCost = 200
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreThemeScreen(
    userId: String,
    creditStore: CreditStore,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { Prefs(context) }
    val scope = rememberCoroutineScope()

    var activeThemeId by remember { mutableStateOf("indian") }
    var unlockedEntitlements by remember { mutableStateOf<Set<String>>(emptySet()) }
    var loading by remember { mutableStateOf(true) }
    var themeToUnlock by remember { mutableStateOf<ThemeOption?>(null) }
    var showInsufficientCredits by remember { mutableStateOf(false) }

    suspend fun loadState() {
        try {
            val serverTheme = RetailerApi.fetchSelectedTheme(userId)
            val current = serverTheme ?: prefs.sharedPreferences.getString("jewel_store_theme", "indian") ?: "indian"
            activeThemeId = current
            unlockedEntitlements = CreditsApi.fetchEntitlementKeys()
        } catch (_: Exception) {
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Store Showroom Theme",
                        style = Typography.cirka(22.sp),
                        color = Palette.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Palette.goldPrimary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Personalize Staff Portal",
                                style = Typography.cirka(18.sp),
                                color = Palette.textPrimary
                            )
                            Text(
                                text = "Choose the aesthetic theme for your store's employee showroom iPad and mobile experience.",
                                style = Typography.santoshi(14.sp),
                                color = Palette.textSecondary
                            )
                        }
                    }

                    items(StoreThemesList, key = { it.id }) { theme ->
                        val isSelected = activeThemeId.equals(theme.id, ignoreCase = true)
                        val isUnlocked = theme.entitlementKey == null || unlockedEntitlements.contains(theme.entitlementKey)

                        ThemeCard(
                            theme = theme,
                            isSelected = isSelected,
                            isUnlocked = isUnlocked,
                            onSelect = {
                                if (isUnlocked) {
                                    activeThemeId = theme.id
                                    prefs.sharedPreferences.edit().putString("jewel_store_theme", theme.id).apply()
                                    scope.launch {
                                        RetailerApi.setSelectedTheme(theme.id, userId)
                                    }
                                } else {
                                    themeToUnlock = theme
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Unlock Theme Sheet
    themeToUnlock?.let { theme ->
        UnlockThemeSheet(
            theme = theme,
            currentBalance = creditStore.availableCredits ?: 0,
            onDismiss = { themeToUnlock = null },
            onConfirmUnlock = {
                scope.launch {
                    try {
                        val result = CreditsApi.purchaseEntitlement(theme.entitlementKey ?: "")
                        if (result.ok) {
                            unlockedEntitlements = unlockedEntitlements + (theme.entitlementKey ?: "")
                            activeThemeId = theme.id
                            prefs.sharedPreferences.edit().putString("jewel_store_theme", theme.id).apply()
                            RetailerApi.setSelectedTheme(theme.id, userId)
                            creditStore.refresh()
                            themeToUnlock = null
                        } else {
                            themeToUnlock = null
                            showInsufficientCredits = true
                        }
                    } catch (_: Exception) {
                        themeToUnlock = null
                        showInsufficientCredits = true
                    }
                }
            }
        )
    }

    if (showInsufficientCredits) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        InsufficientCreditsSheet(
            sheetState = sheetState,
            shortBy = 150,
            onDismiss = { showInsufficientCredits = false },
            onOpenDailyCredits = { showInsufficientCredits = false }
        )
    }
}

@Composable
fun ThemeCard(
    theme: ThemeOption,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.surfaceElevated)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Palette.goldPrimary else Palette.surfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Theme Artwork Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(theme.secondaryColor)
            ) {
                AsyncImage(
                    model = theme.previewImage,
                    contentDescription = theme.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Badge
                Surface(
                    color = when {
                        isSelected -> Palette.goldPrimary
                        isUnlocked -> Color.Black.copy(alpha = 0.65f)
                        else -> Palette.accentRed.copy(alpha = 0.85f)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Text("ACTIVE", style = Typography.santoshi(11.sp, FontWeight.Bold), color = Color.Black)
                        } else if (isUnlocked) {
                            Text("UNLOCKED", style = Typography.santoshi(11.sp, FontWeight.Bold), color = Color.White)
                        } else {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("${theme.creditCost} CREDITS", style = Typography.santoshi(11.sp, FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }

            // Info & Color Palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = theme.title,
                        style = Typography.cirka(18.sp),
                        color = Palette.textPrimary
                    )
                    Text(
                        text = theme.subtitle,
                        style = Typography.santoshi(13.sp),
                        color = Palette.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Color Swatches
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.secondaryColor)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockThemeSheet(
    theme: ThemeOption,
    currentBalance: Int,
    onDismiss: () -> Unit,
    onConfirmUnlock: () -> Unit
) {
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
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Palette.goldPrimary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "Unlock ${theme.title}",
                style = Typography.cirka(22.sp),
                color = Palette.textPrimary
            )

            Text(
                text = "This premium showroom theme requires ${theme.creditCost} credits to unlock permanently for your store.",
                style = Typography.santoshi(14.sp),
                color = Palette.textSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Palette.surface)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Your Credit Balance", style = Typography.santoshi(14.sp), color = Palette.textSecondary)
                Text(
                    text = "$currentBalance Credits",
                    style = Typography.santoshi(16.sp, FontWeight.Bold),
                    color = Palette.goldPrimary
                )
            }

            Button(
                onClick = onConfirmUnlock,
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Unlock for ${theme.creditCost} Credits",
                    style = Typography.santoshi(16.sp, FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}
