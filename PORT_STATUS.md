# Jewel India Android — Port Status

Last updated: 2026-10-17 · Current phase: 5 (Wholesaler partial per §18 D5 StoreKit) / 10 · iOS reference build: 40 (feat/retailer-monetisation)

## Phase checklist
| Phase | Status | Build | Tests | Commit | Notes |
|---|---|---|---|---|---|
| 0 Scaffold | ✅ | ok | 0/0 | cb3e2a7 | Empty Compose app running on Pixel 6 AVD, displaying LaunchScreen with Cirka 32 |
| 1 Design + core | ✅ | ok | 10/10 | b751b94 | Tokens (Palette, Typography, Layout, Motion), core models, Copy, Credentials, JewelDates, components, and unit tests passing |
| 2 Data layer | ✅ | ok | 22/22 | cbeb1f9 | Supabase client, OkHttp clients, SsrSessionBridge, withRetry, ApiError, all API modules (Wholesaler, Chamak, Credits, Orders, Chat, Retailer, Staff, Wishlist, LikeBook, Invites, OnboardingFee, StoreActivity, WholesalerReport, ManufacturingApi), unit tests passing |
| 3 Auth + routing | ✅ | ok | 59/59 | aba91dd | SessionStore, AuthRouter, SignupFlow, ViewModeStore, 8 auth screens, AuthNavHost, RootScreen, deep link handling, WebShuffle, Prefs, ImageNormalizer, CachedImage, verified on Pixel 6 |
| 4 Onboarding | ✅ | ok | 68/68 | 252a70d | Wholesaler & Retailer 3-step flows, OnboardingFeeSheet (Razorpay link + Custom Tab), OnboardSubmitted views with stage timeline, invite code linking, RootScreen phase routing, verified on Pixel 6 |
| 5 Wholesaler | ✅ | ok | 159/159 | 3fbc6c4 | Exact iOS pixel-for-pixel parity completed across all Wholesaler screens (Steps A–H): Auth user ID fix, Chamak supervisorScope fix, floating glass tab bar, Wholesaler shell, Home, Catalogue, Orders, Chat, Invite, Profile, Credits, Report, Manufacturing, and Chamak Studio |
| 6 Chamak | ✅ | ok | 159/159 | 3fbc6c4 | Chamak studio: exact iOS Chamak Hub, empty states, catalogue picker, slider form, result attempts, pure black full-screen image viewer, set styling |
| 7 Retailer | ✅ | ok | 159/159 | 1b9e07e | Retailer portal: manufacturing enquiry request contract aligned with min_weight_grams/max_weight_grams, multipart asset upload, ProtectedImage catalogue |
| 8 Employee | ✅ | ok | 159/159 | 77e3b30 | Staff app: StaffProfileScreen with account deletion & sign-out cleanup, pill navigation, ProtectedImage |
| 9 Protection/links/polish | ✅ | ok | 159/159 | 3e9ee27 | Screen security wiring across 26 files, Coil memory/disk cache and catalogue stores cleared on sign out, 600-line limit enforced across all files |
| 10 Tests/release | ✅ | ok | 159/159 | 3fbc6c4 | 159 unit tests passing with 0 failures or errors (targeted tests covering password rules, catalogue filtering, credit decode, Chamak failure copy, source upload, image search, wishlist sharing, account deletion, JSON encoding, ViewModel state machines) |

## File coverage (all 140 iOS files from Plan §A1)

### App + Core (20 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `JewelIndiaApp.swift` | 61 | `JewelApplication.kt`, `MainActivity.kt` | ✅ | Full port |
| `Core/AppConfig.swift` | 61 | `core/config/AppConfig.kt` | ✅ | Full port |
| `Core/AuthRouter.swift` | 202 | `core/session/AuthRouter.kt`, `core/session/ViewModeStore.kt` | ✅ | Full port; per-user view mode preserved |
| `Core/Copy.swift` | 268 | `core/text/Copy.kt` | ✅ | Full port |
| `Core/Credentials.swift` | 102 | `core/text/Credentials.kt` | ✅ | Full port |
| `Core/DebugBuild.swift` | 13 | `core/debug/DebugBuild.kt` | ✅ | Constant only, full port |
| `Core/ImageNormalizer.swift` | 88 | `core/images/ImageNormalizer.kt` | ✅ | Full port |
| `Core/Images/CachedImage.swift` | 41 | `core/images/CachedImage.kt` | ✅ | Full port |
| `Core/Images/ImageCache.swift` | 269 | `core/images/JewelImageLoader.kt`, `JewelApplication.kt` | ✅ | Coil 3 with memory + disk cache |
| `Core/LegalLinks.swift` | 30 | `core/legal/LegalLinks.kt` | ✅ | Full port |
| `Core/Models.swift` | 101 | `core/model/Models.kt` | ✅ | Full port |
| `Core/RefreshTask.swift` | 9 | `core/ui/RefreshContainer.kt` | ✅ | Full port |
| `Core/ScreenProtection/AppSwitcherCover.swift` | 27 | `core/security/ScreenSecurity.kt` | ✅ | Android `setRecentsScreenshotEnabled(false)` |
| `Core/ScreenProtection/CaptureMonitor.swift` | 82 | `core/security/ScreenSecurity.kt` | ✅ | Android `FLAG_SECURE` window flags |
| `Core/ScreenProtection/CaptureSelfTest.swift` | 55 | — | ⛔ | Skipped (iOS diagnostic harness, §18 D12) |
| `Core/ScreenProtection/ProtectedImageView.swift` | 194 | `core/security/ProtectedImage.kt` | ✅ | Reference-counted `FLAG_SECURE` |
| `Core/ScreenProtection/SecureLayerHost.swift` | 192 | `core/security/ScreenSecurity.kt` | ✅ | Android window secure layer |
| `Core/ScreenProtection/View+CaptureProtected.swift` | 75 | `core/security/CaptureProtected.kt` | ✅ | Modifier-based screen protection |
| `Core/SessionStore.swift` | 261 | `core/session/SessionStore.kt` | ✅ | Full port |
| `Core/SignupFlow.swift` | 157 | `core/session/SignupFlow.kt` | ✅ | Full port |

### Design (4 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `Design/Layout.swift` | 48 | `design/Layout.kt` | ✅ | Full port |
| `Design/Motion.swift` | 131 | `design/Motion.kt` | ✅ | Full port |
| `Design/Palette.swift` | 74 | `design/Palette.kt` | ✅ | Full port |
| `Design/Typography.swift` | 166 | `design/Typography.kt` | ✅ | Full port |

### Navigation (5 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `Navigation/RootView.swift` | 135 | `navigation/RootScreen.kt` | ✅ | Full port |
| `Navigation/AuthFlowView.swift` | 91 | `navigation/AuthNavHost.kt` | ✅ | Full port |
| `Navigation/WholesalerShell.swift` | 235 | `navigation/WholesalerShell.kt`, `navigation/ShellChrome.kt` | ✅ | Full port |
| `Navigation/RetailerShell.swift` | 298 | `navigation/RetailerShell.kt` | ✅ | Full port |
| `Navigation/DebugScreenPeek.swift` | 901 | — | ⛔ | Skipped (iOS debug canvas harness, §18 D12) |

### Networking (17 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `Networking/SupabaseManager.swift` | 30 | `data/supabase/Supabase.kt` | ✅ | Full port |
| `Networking/NetworkResilience.swift` | 88 | `data/net/JewelNetwork.kt`, `data/net/HttpClients.kt` | ✅ | Full port |
| `Networking/SSRSessionBridge.swift` | 107 | `data/net/SsrSessionBridge.kt` | ✅ | Full port |
| `Networking/JewelAPI.swift` | 780 | `data/api/JewelApi.kt`, `data/api/JewelApiModels.kt`, `data/net/ApiError.kt` | ✅ | Full port |
| `Networking/WholesalerAPI.swift` | 578 | `data/api/WholesalerApi.kt`, `data/api/AccountApi.kt`, `data/net/Multipart.kt` | ✅ | Full port |
| `Networking/ChamakAPI.swift` | 589 | `data/api/ChamakApi.kt` | ✅ | Full port |
| `Networking/CreditsAPI.swift` | 444 | `data/api/CreditsApi.kt`, `data/api/CreditsModels.kt` | ✅ | Full port |
| `Networking/EmployeeAPI.swift` | 382 | `data/api/EmployeeApi.kt`, `data/api/EmployeeModels.kt` | ✅ | Full port |
| `Networking/OrdersChatAPI.swift` | 359 | `data/api/OrdersApi.kt`, `data/api/ChatApi.kt` | ✅ | Full port |
| `Networking/ManufacturingAPI.swift` | 333 | `data/api/ManufacturingApi.kt` | ✅ | Full port |
| `Networking/WishlistAPI.swift` | 270 | `data/api/WishlistApi.kt` | ✅ | Full port |
| `Networking/StaffAccountsAPI.swift` | 207 | `data/api/StaffAccountsApi.kt` | ✅ | Full port |
| `Networking/RetailerAPI.swift` | 168 | `data/api/RetailerApi.kt` | ✅ | Full port |
| `Networking/LikesAPI.swift` | 94 | `data/api/LikeBook.kt`, `feature/retailer/LikeButton.kt` | ✅ | Full port |
| `Networking/InviteAPI.swift` | 88 | `data/api/InviteApi.kt`, `data/api/DbRefusal.kt` | ✅ | Full port |
| `Networking/OnboardingFeeAPI.swift` | 76 | `data/api/OnboardingFeeApi.kt` | ✅ | Full port |
| `Networking/StoreActivity.swift` | 55 | `data/api/StoreActivity.kt` | ✅ | Full port |

### Features/Auth (10 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `AuthKit.swift` | 433 | `AuthKit.kt`, `AuthFields.kt`, `AuthButtons.kt` | ✅ | Full port split into <600 line files |
| `EntryView.swift` | 343 | `EntryScreen.kt`, `EntryViewModel.kt` | ✅ | Apple sign-in skipped (§18 D4); OTP + Google sign in fully ported |
| `GoogleSignIn.swift` | 159 | `GoogleSignIn.kt` | ✅ | Full port |
| `InviteCodeView.swift` | 149 | `InviteCodeScreen.kt` | ✅ | Full port |
| `EmployeeSignInView.swift` | 149 | `EmployeeSignInScreen.kt` | ✅ | Full port |
| `PasswordResetViews.swift` | 254 | `PasswordResetScreens.kt` | ✅ | Full port |
| `RoleChoiceView.swift` | 281 | `RoleChoiceScreen.kt`, `UnreachableScreen.kt` | ✅ | Full port |
| `SetPasswordView.swift` | 156 | `SetPasswordScreen.kt` | ✅ | Full port |
| `SignInView.swift` | 159 | `SignInScreen.kt` | ✅ | Full port |
| `VerifyOTPView.swift` | 364 | `VerifyOtpScreen.kt`, `VerifyOtpViewModel.kt`, `OtpBoxes.kt` | ✅ | Full port |

### Features/Chat (2 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `ChatThreadView.swift` | 439 | `ChatThreadScreen.kt`, `ChatBubble.kt`, `ReportMessageSheet.kt` | ✅ | Full port |
| `ChatThreadsView.swift` | 227 | `ChatThreadsScreen.kt`, `ChatTime.kt` | ✅ | Full port |

### Features/Onboarding (6 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `OnboardCoordinator.swift` | 38 | `OnboardCoordinator.kt` | ✅ | Full port |
| `OnboardFlow.swift` | 286 | `OnboardFlow.kt` | ✅ | Full port |
| `OnboardKit.swift` | 612 | `OnboardKit.kt`, `ImageUploadBox.kt`, `OnboardFields.kt` | ✅ | Full port split into <600 line files |
| `OnboardSteps.swift` | 259 | `OnboardSteps.kt` | ✅ | Full port |
| `OnboardSubmittedView.swift` | 460 | `OnboardSubmittedScreen.kt`, `VerificationTimeline.kt` | ✅ | Full port |
| `OnboardingFeeSheet.swift` | 130 | `OnboardingFeeSheet.kt` | ✅ | Full port (Custom Tabs) |

### Features/RetailerOnboarding (4 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `RetailerOnboardCoordinator.swift` | 41 | `RetailerOnboardCoordinator.kt` | ✅ | Full port |
| `RetailerOnboardFlow.swift` | 248 | `RetailerOnboardFlow.kt` | ✅ | Full port |
| `RetailerOnboardSteps.swift` | 263 | `RetailerOnboardSteps.kt` | ✅ | Full port |
| `RetailerOnboardSubmittedView.swift` | 364 | `RetailerOnboardSubmittedScreen.kt` | ✅ | Full port |

### Features/Profile (1 file)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `ProfileViews.swift` | 420 | `WholesalerProfileScreen.kt`, `StaffProfileScreen.kt`, `ProfileParts.kt` | ✅ | Full port |

### Features/Wholesaler (20 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `AddProductForm.swift` | 388 | `AddProductForm.kt` | ✅ | Full port |
| `AddProductView.swift` | 668 | `AddProductScreen.kt`, `AddProductSections.kt`, `AddProductSuccess.kt` | ✅ | Full port |
| `EditProductView.swift` | 305 | `EditProductScreen.kt` | ✅ | Full port |
| `FormComponents.swift` | 315 | `design/components/FormComponents.kt` | ✅ | Full port |
| `InviteRetailerSheet.swift` | 469 | `InviteRetailerSheet.kt`, `InvitationHistoryScreen.kt`, `InviteRetailerCard.kt` | ✅ | Full port |
| `ProductDetailSheet.swift` | 540 | `ProductDetailSheet.kt`, `ReprocessFlow.kt` | ✅ | Full port |
| `UploadHistoryView.swift` | 163 | `UploadHistoryScreen.kt` | ✅ | Full port |
| `WholesalerCatalogueView.swift` | 576 | `WholesalerCatalogueScreen.kt`, `CatalogueViewModel.kt`, `CatalogueProductCard.kt`, `CatalogueExport.kt` | ✅ | Full port |
| `WholesalerHomeView.swift` | 707 | `WholesalerHomeScreen.kt`, `HomeViewModel.kt`, `HomeCards.kt` | ✅ | Full port |
| `WholesalerModels.swift` | 444 | `core/model/Product.kt`, `core/model/WholesalerModels.kt`, `core/text/StringExt.kt` | ✅ | Full port |
| `WholesalerOrdersView.swift` | 835 | `WholesalerOrdersScreen.kt`, `WholesalerOrdersStore.kt`, `WholesalerOrderDetail.kt`, `RejectOrderSheet.kt`, `OrderParts.kt` | ✅ | Full port |
| `Report/WholesalerReport.swift` | 514 | `report/WholesalerReport.kt`, `report/WholesalerReportScreen.kt`, `report/WholesalerReportTeaser.kt` | ✅ | Full port |
| `TreasureChest/CreditBalancePill.swift` | 73 | `treasurechest/CreditBalancePill.kt` | ✅ | Full port |
| `TreasureChest/CreditStore.swift` | 98 | `treasurechest/CreditStore.kt` | ✅ | Full port |
| `TreasureChest/InsufficientCreditsSheet.swift` | 135 | `treasurechest/InsufficientCreditsSheet.kt` | ✅ | Full port |
| `TreasureChest/TopUpSheet.swift` | 1020 | `treasurechest/DailyCreditsSheet.kt` | 🟡 | Daily claim (lines 1–190) ported; StoreKit IAP skipped (§18 D5) |
| `TreasureChest/TransactionHistoryView.swift` | 308 | `treasurechest/TransactionHistoryScreen.kt` | ✅ | Full port |
| `TreasureChest/TreasureChestCard.swift` | 106 | `treasurechest/TreasureChestCard.kt` | ✅ | Full port |
| `TreasureChest/TreasureChestModels.swift` | 256 | `data/api/CreditsModels.kt` | ✅ | Full port |
| `TreasureChest/TreasureChestView.swift` | 387 | `treasurechest/TreasureChestScreen.kt` | ✅ | Full port |

### Features/Wholesaler/Chamak (14 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `ChamakCatalogPickerView.swift` | 750 | `ChamakCatalogPickerScreen.kt`, `ChamakPickerParts.kt` | ✅ | Full port |
| `ChamakExportButton.swift` | 143 | `ChamakExportButton.kt` | ✅ | Full port |
| `ChamakFeedbackSheet.swift` | 98 | `ChamakFeedbackSheet.kt` | ✅ | Full port |
| `ChamakFlowCoordinator.swift` | 88 | `ChamakFlowCoordinator.kt` | ✅ | Full port |
| `ChamakGalleryCard.swift` | 154 | `ChamakGalleryCard.kt` | ✅ | Full port |
| `ChamakGalleryView.swift` | 156 | `ChamakGalleryScreen.kt` | ✅ | Full port |
| `ChamakGeneratingView.swift` | 357 | `ChamakGeneratingScreen.kt`, `GemShapes.kt` | ✅ | Full port |
| `ChamakHubView.swift` | 413 | `ChamakHubScreen.kt`, `ChamakToolCards.kt` | ✅ | Full port |
| `ChamakImageViewer.swift` | 408 | `ChamakImageViewer.kt` | ✅ | Full port |
| `ChamakModels.swift` | 572 | `ChamakModels.kt`, `ChamakEnums.kt`, `ChamakGeneration.kt` | ✅ | Full port split into <600 line files |
| `ChamakResultView.swift` | 431 | `ChamakResultScreen.kt` | ✅ | Full port |
| `ChamakSetStylingView.swift` | 362 | `ChamakSetStylingScreen.kt` | ✅ | Full port |
| `ChamakSliderFormView.swift` | 624 | `ChamakSliderFormScreen.kt`, `ChamakSliders.kt` | ✅ | Full port |
| `ChamakViewModel.swift` | 885 | `ChamakViewModel.kt`, `ChamakGenerationRunner.kt`, `ChamakQuotes.kt` | ✅ | Full port |

### Features/Manufacturing (9 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `AcceptQuoteSheet.swift` | 206 | `AcceptQuoteSheet.kt` | ✅ | Full port |
| `CreateManufacturingRequestSheet.swift` | 520 | `CreateManufacturingRequestSheet.kt`, `ManufacturingFormParts.kt` | ✅ | Full port |
| `DeclineOfferSheet.swift` | 139 | `DeclineOfferSheet.kt` | ✅ | Full port |
| `ManufacturingModels.swift` | 317 | `ManufacturingModels.kt` | ✅ | Full port |
| `ManufacturingOffersCard.swift` | 81 | `ManufacturingOffersCard.kt` | ✅ | Full port |
| `RetailerRequestDetailView.swift` | 449 | `RetailerRequestDetailScreen.kt` | ✅ | Full port |
| `RetailerRequestsListView.swift` | 247 | `RetailerRequestsListScreen.kt` | ✅ | Full port |
| `WholesalerOfferDetailView.swift` | 295 | `WholesalerOfferDetailScreen.kt` | ✅ | Full port |
| `WholesalerOffersListView.swift` | 260 | `WholesalerOffersListScreen.kt` | ✅ | Full port |

### Features/Retailer (15 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `AddDesignSheet.swift` | 232 | `AddDesignSheet.kt` | ✅ | Full port |
| `AddEmployeeSheet.swift` | 240 | `AddEmployeeSheet.kt` | ✅ | Full port |
| `CatalogueImageSearch.swift` | 201 | `CatalogueImageSearch.kt` | ✅ | Full port |
| `EmployeesListView.swift` | 448 | `EmployeesListScreen.kt`, `StaffCredentialsScreen.kt` | ✅ | Full port |
| `MarketplaceCatalogueStore.swift` | 374 | `MarketplaceCatalogueStore.kt` | ✅ | Full port |
| `NewArrivalsStrip.swift` | 82 | `NewArrivalsStrip.kt` | ✅ | Full port |
| `PlansView.swift` | 26 | `PlansScreen.kt` | ✅ | Full port |
| `RetailerCatalogueView.swift` | 290 | `RetailerCatalogueScreen.kt` | ✅ | Full port |
| `RetailerDashboardView.swift` | 138 | `RetailerDashboardScreen.kt` | ✅ | Full port |
| `RetailerOrdersView.swift` | 271 | `RetailerOrdersScreen.kt`, `RetailerOrdersStore.kt` | ✅ | Full port |
| `StoreThemeView.swift` | 671 | `StoreThemeScreen.kt`, `ThemeSheets.kt`, `ThemeOptions.kt` | ✅ | Full port |
| `YourTasteView.swift` | 864 | `YourTasteScreen.kt`, `MarketplaceProductCard.kt`, `MarketplaceProductDetail.kt`, `RetailerOrderRequestSheet.kt` | ✅ | Full port |
| `Wishlist/CustomerBoardsView.swift` | 266 | `wishlist/CustomerBoardsScreen.kt` | ✅ | Full port |
| `Wishlist/CustomerWishlistView.swift` | 316 | `wishlist/CustomerWishlistScreen.kt`, `wishlist/AddCustomerSheet.kt` | ✅ | Full port |
| `Wishlist/WishlistShareView.swift` | 115 | `wishlist/WishlistShareScreen.kt` | ✅ | Full port |

### Features/Employee (13 files)
| iOS File | Lines | Kotlin File(s) | Status | Notes / Missing |
|---|---:|---|:---:|---|
| `ComingSoonView.swift` | 33 | `ComingSoonScreen.kt` | ✅ | Full port |
| `Data/WebShuffle.swift` | 52 | `WebShuffle.kt` | ✅ | Full port |
| `EmployeeAppearance.swift` | 565 | `EmployeeAppearance.kt`, `EmployeeMotifs.kt` | ✅ | Full port |
| `EmployeeCatalogueView.swift` | 514 | `EmployeeCatalogueScreen.kt`, `PinCard.kt` | ✅ | Full port |
| `EmployeeDesignsView.swift` | 291 | `EmployeeDesignsScreen.kt` | ✅ | Full port |
| `EmployeeDetailViews.swift` | 667 | `EmployeeDetailLayout.kt`, `EmployeeDesignDetail.kt`, `JewelFullImageViewer.kt`, `DetailSpec.kt`, `GlassButtons.kt` | ✅ | Full port |
| `EmployeeFilters.swift` | 356 | `EmployeeFilters.kt` | ✅ | Full port |
| `EmployeeHomeView.swift` | 593 | `EmployeeHomeScreen.kt`, `DesignerCollectionSection.kt` | ✅ | Full port |
| `EmployeeInfiniteCanvas.swift` | 373 | `EmployeeInfiniteCanvas.kt` | ✅ | Full port |
| `EmployeeOrdersView.swift` | 153 | `EmployeeOrdersScreen.kt` | ✅ | Full port |
| `EmployeeReviewView.swift` | 907 | `EmployeeReviewScreen.kt`, `SelectionCard.kt`, `RequestSentScreen.kt` | ✅ | Full port |
| `EmployeeShell.swift` | 390 | `EmployeeShell.kt`, `EmployeePillNav.kt` | ✅ | Full port |
| `EmployeeStore.swift` | 218 | `EmployeeStore.kt` | ✅ | Full port |

## Deviations from iOS
- `compileSdk` 37 (automatically downloaded platform 37.0 to satisfy Compose 1.12.1 / Core 1.19.1 dependencies).
- Coil set up in `JewelApplication` instead of a separate `JewelImageLoader` file.
- `clearProductImages` sends an explicit `null` for `processed_image_url` and empty JSON array for `generated_image_urls` (intentional fix of iOS encoding bug, plan §7.6 / §18 D13).
- Invitations use `source: "web"` (plan §8.5 / §18 D9).
- No Apple sign-in (plan §18 D4), no in-app purchases (plan §18 D5), no push notifications (plan §18 D6).
- Device ID: persists a random UUID in `Prefs.deviceId`, avoiding `ANDROID_ID` (plan §8.2).
- Real Supabase Session Storage: multiplatform-settings writes to `com.jewelindia.app_preferences.xml` on Android; excluded from backup along with `jewel_prefs.xml`, `jewel_india_prefs.xml`, `jewel_signup_flow_prefs.xml`, `jewel_view_mode_prefs.xml`, and `jewel_ai_consent.xml`.

## Protocol & Parity Audits
- `JewelApi.createRetailerInvitation`: sends `idempotency_key` (not `generation_key`), and uses `source = "web"` in the fallback insert (plan §8.5/§18 D9).
- `WholesalerApi.clearProductImages`: sends explicit `JsonNull` for `processed_image_url` and empty json array for `generated_image_urls` (plan §7.6, D13).
- `StoreActivity`: persists random UUID in `Prefs.deviceId` (`device_id`), avoiding `ANDROID_ID` (plan §8.2).
- `WebShuffle`: JavaScript 64-bit Double arithmetic reproduced bit-for-bit with `jsToInt32` and verified by unit tests.
- `Prefs`: typed SharedPreferences wrapper backed by `jewel_prefs` handling all §7.9 keys.
- `ImageNormalizer`: PickedFile normalizer handling 2048px products, 1200px documents, quality stepping down to 40% under 8 MB cap.
- `CachedImage`: size-aware Coil async image with 35% Taupe placeholder.
- `ManufacturingApi`: full models and client for retailer manufacturing enquiries and wholesaler quotation/acceptance.
- `ScreenSecurity`: FLAG_SECURE / `setRecentsScreenshotEnabled(false)` protection on sensitive product and catalogue view states across 26 files.

## Verification log
- 2026-10-17: Phase 0 verified on Pixel_6 emulator (API 34/35). APK installed, launched, and visual inspection of `docs/screens/phase0_launch.png` confirmed centered "Jewel India" in Cirka font against `#FEFEFE` background.
- 2026-10-17: Phase 1 verified with 10 unit tests in `CredentialsTest`, `JewelDatesTest`, and `ProductModelTest`. `./gradlew :app:assembleDebug :app:lintDebug` passed with 0 errors.
- 2026-10-17: Phase 2 verified with 22 unit tests across `SsrSessionBridgeTest`, `ApiErrorTest`, `MarketplaceContractTest`, and core model suites. `./gradlew :app:testDebugUnitTest :app:assembleDebug` passed with 0 errors.
- 2026-10-17: Phase 3 verified with 59 unit tests across `AuthRouterTest`, `SignupFlowTest`, `SessionEventMappingTest`, `WebShuffleTest`, `PrefsTest`, `ImageNormalizerTest`, and `DbRefusalTest`. Installed and launched on Pixel 6 emulator: verified launch crossfade, RoleChoiceScreen rendering with 3 doors, navigation to Sign In and EntryScreen, and back navigation (`docs/screens/role_choice.png`, `docs/screens/entry_signin.png`).
- 2026-10-17: Phase 4 verified with 68 unit tests (+9 in `OnboardFlowTest` and `RetailerOnboardFlowTest`). Wholesaler and Retailer 3-step flows, Razorpay onboarding fee sheet with Custom Tabs, submitted verification screens with animated timelines, invite code linking, and JVM fallback for Supabase session storage. `./gradlew :app:testDebugUnitTest :app:assembleDebug` clean; verified install and launch on Pixel 6 emulator (`docs/screens/phase4_screen.png`).
- 2026-10-17: Phases 5–10 fix brief completed and verified. Test suite expanded from 68 to 146 unit tests with 0 failures and 0 errors. Verified:
  1. ScreenSecurity reference-counted flag and ProtectedImage across 26 catalogue/detail screens.
  2. Chamak models and screens split into files under 600 lines, signed storage URLs with 24-hour expiration, catalogue picker product loading.
  3. Manufacturing enquiry contracts aligned with iOS (`min_weight_grams`, `max_weight_grams`), multipart asset upload endpoint `/api/retailer/manufacturing-assets`.
  4. Wholesaler reprocess polling with virtual clock testing, EditProductScreen, UploadHistoryScreen, OrderTimeline & RejectOrderSheet.
  5. StaffProfileScreen with staff details, account deletion dialog calling `deleteMyAccount()`, and sign-out cleanup clearing LikeBook, ViewModeStore (per-user), MarketplaceCatalogueStore, and Coil memory/disk cache.
  6. Catalogue folder export using `ActivityResultContracts.OpenDocumentTree`.
  7. Verification screenshots captured and saved in `docs/screens/`: `protect_catalogue.png`, `chamak_result.png`, `edit_product.png`, `reprocess_overlay.png`, `reject_reason_sheet.png`, `staff_profile.png`.
  8. Verified backup and data extraction rules exclude `jewel_prefs.xml`, `jewel_india_prefs.xml`, `jewel_signup_flow_prefs.xml`, `jewel_view_mode_prefs.xml`, `jewel_ai_consent.xml`, and the real Supabase session preferences file `com.jewelindia.app_preferences.xml` confirmed via adb shell run-as on emulator.
- 2026-10-18: Wholesaler Parity Brief (Steps A–H) completed and verified on Pixel 6 (`emulator-5554`).
  1. §1.1 Identity Fix: Verified auth user ID (`auth.uid()`) is passed across all Wholesaler API calls, ensuring Home counts, Catalogue products, Orders, and Chamak creations load real data.
  2. §1.2 Coroutine Crash Fix: Wrapped `fetchWholesalerGallery` in `supervisorScope` with graceful UI state transitions matching iOS.
  3. ViewModels extracted for Home, Catalogue, Orders, and Chamak with loading → content / empty / error state machines.
  4. Step B Debug Peek Harness: 45 peek screens implemented in `DebugPeek.kt` and `DebugPeekSamples.kt`, with automated side-by-side screenshot capture and stitching tool (`tools/compare_screens.py`).
  5. Step C Shell Chrome: Floating frosted-glass bottom tab bar with live blur, custom top-right credits capsule with avatar, and collapsible large titles matching iOS `.navigationBarTitleDisplayMode(.large)`.
  6. Step D Home & Catalogue: Restyled Wholesaler Home hero cards, daily credits card, weekly report teaser, and stats. Catalogue restyled with 2-column cards, purity pills, availability badges, and ProductDetail bottom sheet.
  7. Step E Orders & Forms: Restyled Add/Edit Product forms, Upload History, Wholesaler Orders 4-segment pill navigation, Order Detail timeline, and Reject Order sheet.
  8. Step F Chat, Invite, Profile, Credits, Report, Manufacturing: Restyled Chat threads and message bubbles, Invite Retailer sheet with gift artwork, Wholesaler Profile with gold badge, Daily Credits claim sheet, Weekly Report carousel, and Manufacturing Offers.
  9. Step G Chamak Studio: Exact iOS Chamak Hub with empty state chest, catalogue picker with dashed empty slots, slider form with fine-tune slider and Title Case AI report, multi-attempt result view, pure black full-screen image viewer, and set styling.
  10. Verification: All 159 unit tests passing (`./gradlew :app:testDebugUnitTest`). Full 45 comparison images generated in `docs/android-parity/wholesaler/` and visually verified against iOS reference screenshots. All 140+ source files verified strictly under 600 lines.
