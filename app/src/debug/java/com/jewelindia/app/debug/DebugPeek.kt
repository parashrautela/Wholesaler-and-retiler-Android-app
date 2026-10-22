package com.jewelindia.app.debug

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jewelindia.app.LocalAppGraph
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.model.UserRole
import com.jewelindia.app.data.api.ChamakMode
import com.jewelindia.app.design.Palette
import com.jewelindia.app.feature.wholesaler.CatalogueProductCard
import com.jewelindia.app.feature.wholesaler.LatestCreationsSection
import com.jewelindia.app.feature.auth.EntryScreen
import com.jewelindia.app.feature.auth.RoleChoiceScreen
import com.jewelindia.app.feature.auth.SetPasswordScreen
import com.jewelindia.app.feature.auth.SignInScreen
import com.jewelindia.app.feature.auth.VerifyOtpScreen
import com.jewelindia.app.feature.chat.ChatThreadScreen
import com.jewelindia.app.feature.chat.ChatThreadsScreen
import com.jewelindia.app.feature.manufacturing.WholesalerOfferDetailScreen
import com.jewelindia.app.feature.onboarding.OnboardCoordinator
import com.jewelindia.app.feature.onboarding.OnboardFlow
import com.jewelindia.app.feature.onboarding.OnboardStep1Screen
import com.jewelindia.app.feature.onboarding.OnboardStep2Screen
import com.jewelindia.app.feature.onboarding.OnboardStep3Screen
import com.jewelindia.app.feature.onboarding.OnboardSubmittedScreen
import com.jewelindia.app.feature.onboarding.OnboardingFeeSheet
import com.jewelindia.app.feature.profile.WholesalerProfileScreen
import com.jewelindia.app.feature.treasurechest.CreditStore
import com.jewelindia.app.feature.treasurechest.DailyCreditsSheet
import com.jewelindia.app.feature.treasurechest.InsufficientCreditsSheet
import com.jewelindia.app.feature.wholesaler.AddProductScreen
import com.jewelindia.app.feature.wholesaler.AddProductSheet
import com.jewelindia.app.feature.wholesaler.ChamakPromoCard
import com.jewelindia.app.feature.wholesaler.HeroUploadCard
import com.jewelindia.app.feature.wholesaler.InviteRetailerCard
import com.jewelindia.app.feature.wholesaler.InviteRetailerSheet
import com.jewelindia.app.feature.wholesaler.ProductDetailSheet
import com.jewelindia.app.feature.wholesaler.RejectOrderSheet
import com.jewelindia.app.feature.wholesaler.WholesalerCatalogScreen
import com.jewelindia.app.feature.wholesaler.WholesalerOrderDetailSheet
import com.jewelindia.app.feature.wholesaler.WholesalerOrdersScreen
import com.jewelindia.app.feature.wholesaler.WholesalerReportScreen
import com.jewelindia.app.feature.wholesaler.WholesalerReportTeaser
import com.jewelindia.app.feature.wholesaler.WholesalerShell
import com.jewelindia.app.feature.wholesaler.WholesalerTab
import com.jewelindia.app.feature.wholesaler.chamak.ChamakCatalogPickerScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakHubScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakImageViewer
import com.jewelindia.app.feature.wholesaler.chamak.ChamakResultScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakSetStylingScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakSliderFormScreen
import com.jewelindia.app.feature.wholesaler.share.CatalogueShareSheet
import com.jewelindia.app.feature.wholesaler.share.CatalogueShareViewModel
import com.jewelindia.app.feature.wholesaler.share.SharedLinksScreen
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewModel
import com.jewelindia.app.navigation.EntryMode

object DebugPeek {

    fun getPeekId(intent: Intent?): String? {
        return intent?.getStringExtra("jewelPeek")
            ?: intent?.getStringExtra("JewelPeek")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PeekScreen(id: String, onBack: () -> Unit = {}) {
        val context = LocalContext.current
        val graph = LocalAppGraph.current
        val scope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        when (id) {
            "wholesaler" -> {
                val store = remember {
                    CreditStore(scope).apply {
                        seedForPeek(DebugPeekSamples.creditWallet(1808))
                    }
                }
                WholesalerShell(
                    sessionStore = graph.sessionStore,
                    initialTab = WholesalerTab.Home,
                    previewCreditStore = store,
                    previewWholesaler = DebugPeekSamples.wholesaler()
                )
            }
            "upload-card" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.cream)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(354.dp)) {
                        HeroUploadCard(onUploadClick = {})
                    }
                }
            }
            "chamak-card" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.cream)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(353.dp)) {
                        ChamakPromoCard(onClick = {})
                    }
                }
            }
            "home-creations" -> {
                val vm = remember { DebugPeekSamples.galleryVM(context) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.background)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    LatestCreationsSection(
                        wholesalerUserId = "w-peek-0001",
                        onSeeAll = {},
                        peekGenerations = vm.galleryGenerations
                    )
                }
            }
            "home-lowbalance" -> {
                val store = remember {
                    CreditStore(scope).apply {
                        seedForPeek(DebugPeekSamples.creditWallet(280))
                    }
                }
                WholesalerShell(
                    sessionStore = graph.sessionStore,
                    initialTab = WholesalerTab.Home,
                    previewCreditStore = store,
                    previewWholesaler = DebugPeekSamples.wholesaler()
                )
            }
            "report-teaser" -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    WholesalerReportTeaser(
                        onClick = {},
                        peekReport = DebugPeekSamples.sampleReport(context)
                    )
                }
            }
            "report" -> {
                WholesalerReportScreen(
                    onBack = onBack,
                    peekReport = DebugPeekSamples.sampleReport(context)
                )
            }
            "catalogue-cards" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.background)
                        .statusBarsPadding()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(DebugPeekSamples.products(context)) { product ->
                            CatalogueProductCard(
                                product = product,
                                onClick = {},
                                onEdit = {},
                                onDelete = {}
                            )
                        }
                    }
                }
            }
            "productdetail" -> {
                val p = Product(
                    id = "00000000-0000-0000-0000-000000000001",
                    wholesalerId = null,
                    wholesalerEmail = null,
                    title = "Antique Kundan Necklace",
                    jewelleryType = "necklace",
                    category = "gold",
                    style = "antique",
                    size = "m",
                    stockAvailable = true,
                    makeToOrderDays = null,
                    metalPurity = "22k",
                    netWeight = 18.4,
                    grossWeight = 21.1,
                    stoneWeight = 2.7,
                    rawImageURL = null,
                    processedImageURL = null,
                    imageURL = null,
                    generatedImageURLs = emptyList(),
                    isPublished = true,
                    createdAt = null
                )
                ProductDetailSheet(
                    product = p,
                    wholesalerId = "w-peek-0001",
                    onDismiss = onBack,
                    onPublishedChanged = {}
                )
            }
            "addproduct" -> {
                AddProductScreen(
                    wholesalerId = "w-peek-0001",
                    userId = "w-peek-0001",
                    onClose = null,
                    onOpenUploadHistory = null,
                    onAdded = { onBack() },
                    previewBalance = 1808
                )
            }
            "addproduct-sheet" -> {
                AddProductScreen(
                    wholesalerId = "w-peek-0001",
                    userId = "w-peek-0001",
                    onClose = onBack,
                    onOpenUploadHistory = { },
                    onAdded = { onBack() },
                    previewBalance = 1808
                )
            }
            "wholesaler-orders" -> {
                WholesalerOrdersScreen(
                    wholesalerUserId = "w-peek-0001",
                    wholesaler = DebugPeekSamples.wholesaler(),
                    peekError = "permission denied for function wholesaler_orders"
                )
            }
            "order-detail-new" -> {
                WholesalerOrderDetailSheet(
                    order = DebugPeekSamples.wholesalerOrders(context)[0],
                    onStatusUpdate = {},
                    onReject = {},
                    onDismiss = onBack
                )
            }
            "order-detail-active" -> {
                WholesalerOrderDetailSheet(
                    order = DebugPeekSamples.wholesalerOrders(context)[1],
                    onStatusUpdate = {},
                    onReject = {},
                    onDismiss = onBack
                )
            }
            "reject-order" -> {
                RejectOrderSheet(onDismiss = onBack, onConfirm = { onBack() })
            }
            "chats-wholesaler" -> {
                ChatThreadsScreen(
                    title = "Queries & Chat",
                    onBack = null,
                    peekThreads = DebugPeekSamples.chatThreads(),
                    onOpenThread = {}
                )
            }
            "chat-thread" -> {
                val thread = DebugPeekSamples.chatThreads()[0]
                ChatThreadScreen(
                    thread = thread,
                    side = "employee",
                    title = "Temple Haram",
                    peekMessages = DebugPeekSamples.chatMessages(),
                    onBack = onBack
                )
            }
            "profile-wholesaler" -> {
                WholesalerProfileScreen(
                    wholesaler = DebugPeekSamples.wholesaler(),
                    sessionStore = graph.sessionStore,
                    onBack = null
                )
            }
            "invite-card" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Palette.background)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InviteRetailerCard(onClick = {})
                }
            }
            "invite-gift", "invite-guide" -> {
                InviteRetailerSheet(
                    sheetState = sheetState,
                    previewStage = if (id == "invite-gift") 1 else 0,
                    onDismiss = onBack,
                    onViewHistory = {}
                )
            }
            "manufacturing-offer" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Supply a simulator fixture offer ID.",
                        color = Color.Black
                    )
                }
            }
            "chamak-hub" -> {
                ChamakHubScreen(
                    wholesalerId = "w-peek-0001",
                    previewViewModel = DebugPeekSamples.galleryVM(context),
                    onClose = onBack
                )
            }
            "chamak-hub-empty" -> {
                ChamakHubScreen(
                    wholesalerId = "w-peek-0001",
                    previewViewModel = viewModel(key = "peek-chamak-empty") { ChamakViewModel() },
                    onClose = onBack
                )
            }
            "chamak-picker" -> {
                val vm = remember {
                    ChamakViewModel().apply {
                        mode = ChamakMode.Fusion
                        catalogProducts = emptyList()
                    }
                }
                ChamakCatalogPickerScreen(
                    vm = vm,
                    wholesalerId = "w-peek-0001",
                    onClose = onBack
                )
            }
            "chamak-picker-sample" -> {
                ChamakCatalogPickerScreen(
                    vm = DebugPeekSamples.pickerVM(context),
                    wholesalerId = "w-peek-0001",
                    onClose = onBack
                )
            }
            "chamak-slider-retry" -> {
                ChamakSliderFormScreen(
                    vm = DebugPeekSamples.attemptsVM(context, ChamakViewModel.Step.SliderForm),
                    wholesalerId = "w-peek-0001"
                )
            }
            "chamak-result" -> {
                ChamakResultScreen(
                    vm = DebugPeekSamples.resultVM(context),
                    wholesalerId = "w-peek-0001"
                )
            }
            "chamak-result-attempts" -> {
                ChamakResultScreen(
                    vm = DebugPeekSamples.attemptsVM(context, ChamakViewModel.Step.Result),
                    wholesalerId = "w-peek-0001"
                )
            }
            "chamak-viewer" -> {
                ChamakImageViewer(
                    images = DebugPeekSamples.viewerImages(context),
                    startIndex = 2,
                    onDismiss = onBack
                )
            }
            "setcreation-picker" -> {
                val vm = remember {
                    ChamakViewModel().apply {
                        mode = ChamakMode.SetCreation
                        catalogProducts = emptyList()
                    }
                }
                ChamakCatalogPickerScreen(
                    vm = vm,
                    wholesalerId = "w-peek-0001",
                    onClose = onBack
                )
            }
            "setcreation-styling" -> {
                val vm = remember {
                    ChamakViewModel().apply {
                        mode = ChamakMode.SetCreation
                        step = ChamakViewModel.Step.SetStyling
                    }
                }
                ChamakSetStylingScreen(
                    vm = vm,
                    wholesalerId = "w-peek-0001"
                )
            }
            "daily-credits-sheet", "daily-credits" -> {
                DailyCreditsSheet(
                    wallet = DebugPeekSamples.dailyCreditWallet(id),
                    sheetState = sheetState,
                    onDismiss = onBack
                )
            }
            "nocredits" -> {
                InsufficientCreditsSheet(
                    sheetState = sheetState,
                    shortBy = 120,
                    required = 200,
                    balance = 80,
                    onDismiss = onBack,
                    onOpenDailyCredits = onBack
                )
            }
            "onboard" -> {
                OnboardCoordinator(sessionStore = graph.sessionStore)
            }
            "onboard1" -> {
                val flow = remember { OnboardFlow() }
                OnboardStep1Screen(flow = flow, onNext = onBack, onBack = onBack, onSignOut = onBack)
            }
            "onboard2" -> {
                val flow = remember { OnboardFlow() }
                OnboardStep2Screen(flow = flow, onNext = onBack, onBack = onBack, onSignOut = onBack)
            }
            "onboard3" -> {
                val flow = remember { OnboardFlow() }
                OnboardStep3Screen(flow = flow, session = graph.sessionStore, onCompleted = onBack, onBack = onBack, onSignOut = onBack)
            }
            "submitted" -> {
                OnboardSubmittedScreen(sessionStore = graph.sessionStore)
            }
            "onboarding-fee" -> {
                OnboardingFeeSheet(
                    amountInr = 9,
                    onDismiss = onBack,
                    onPaid = { onBack() }
                )
            }
            "doors" -> {
                RoleChoiceScreen(
                    sessionStore = graph.sessionStore,
                    signupFlow = graph.signupFlow,
                    onNavigate = {}
                )
            }
            "entry-wholesaler" -> {
                EntryScreen(
                    mode = EntryMode.Signup(UserRole.wholesaler),
                    sessionStore = graph.sessionStore,
                    signupFlow = graph.signupFlow,
                    authRouter = graph.authRouter,
                    onNavigate = {},
                    onBack = onBack
                )
            }
            "signin" -> {
                SignInScreen(
                    identity = "+919876543210",
                    sessionStore = graph.sessionStore,
                    signupFlow = graph.signupFlow,
                    onNavigate = {},
                    onBack = onBack
                )
            }
            "otp" -> {
                VerifyOtpScreen(
                    sessionStore = graph.sessionStore,
                    signupFlow = graph.signupFlow,
                    onNavigate = {},
                    onBack = onBack
                )
            }
            "setpassword" -> {
                SetPasswordScreen(
                    sessionStore = graph.sessionStore,
                    signupFlow = graph.signupFlow,
                    onPasswordSet = onBack
                )
            }
            "catalogue-select" -> {
                val sampleProds = remember {
                    val all = DebugPeekSamples.products(context)
                    all.mapIndexed { idx, p ->
                        if (idx == all.lastIndex) p.copy(isPublished = false) else p
                    }
                }
                WholesalerCatalogScreen(
                    wholesalerUserId = "w-peek-0001",
                    sessionStore = graph.sessionStore,
                    peekProducts = sampleProds,
                    peekIsSelecting = true,
                    peekSelectionIds = listOf(
                        sampleProds[0].id,
                        sampleProds[1].id
                    )
                )
            }
            "catalogue-share" -> {
                val vm = remember { DebugPeekSamples.catalogueShareVM(context) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    CatalogueShareSheet(
                        selectedProducts = vm.selectedProducts,
                        onDismiss = { onBack() },
                        viewModel = vm
                    )
                }
            }
            "catalogue-links" -> {
                val vm = remember {
                    CatalogueShareViewModel(
                        api = DebugPeekSamples.FakeCatalogueShareService()
                    )
                }
                SharedLinksScreen(
                    onBack = onBack,
                    viewModel = vm
                )
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DebugPeek: Unknown preview id '$id'",
                        color = Palette.accentRed
                    )
                }
            }
        }
    }
}
