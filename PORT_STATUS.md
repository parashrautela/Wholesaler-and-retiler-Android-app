# Jewel India Android — Port Status

Last updated: 2026-10-17 · Current phase: 10 (Completed & Verified) · iOS reference build: 40 (feat/retailer-monetisation)

## Phase checklist
| Phase | Status | Build | Tests | Commit | Notes |
|---|---|---|---|---|---|
| 0 Scaffold | ✅ | ok | 0/0 | cb3e2a7 | Empty Compose app running on Pixel 6 AVD, displaying LaunchScreen with Cirka 32 |
| 1 Design + core | ✅ | ok | 10/10 | b751b94 | Tokens (Palette, Typography, Layout, Motion), core models, Copy, Credentials, JewelDates, components, and unit tests passing |
| 2 Data layer | ✅ | ok | 22/22 | cbeb1f9 | Supabase client, OkHttp clients, SsrSessionBridge, withRetry, ApiError, all API modules (Wholesaler, Chamak, Credits, Orders, Chat, Retailer, Staff, Wishlist, LikeBook, Invites, OnboardingFee, StoreActivity, WholesalerReport, ManufacturingApi), unit tests passing |
| 3 Auth + routing | ✅ | ok | 59/59 | aba91dd | SessionStore, AuthRouter, SignupFlow, ViewModeStore, 8 auth screens, AuthNavHost, RootScreen, deep link handling, WebShuffle, Prefs, ImageNormalizer, CachedImage, verified on Pixel 6 |
| 4 Onboarding | ✅ | ok | 68/68 | 252a70d | Wholesaler & Retailer 3-step flows, OnboardingFeeSheet (Razorpay link + Custom Tab), OnboardSubmitted views with stage timeline, invite code linking, RootScreen phase routing, verified on Pixel 6 |
| 5 Wholesaler | ✅ | ok | 146/146 | c1d066a | Wholesaler portal: reprocess polling with virtual clock, edit product sheet, upload history screen, order timeline & reject reasons, catalogue folder export |
| 6 Chamak | ✅ | ok | 146/146 | 0ce2862 | Chamak studio: signed storage URLs, catalogue picker flow, ChamakResultScreen, GemShapes, canonical jewellery taxonomy split into <600 line files |
| 7 Retailer | ✅ | ok | 146/146 | 1b9e07e | Retailer portal: manufacturing enquiry request contract aligned with min_weight_grams/max_weight_grams, multipart asset upload, ProtectedImage catalogue |
| 8 Employee | ✅ | ok | 146/146 | 77e3b30 | Staff app: StaffProfileScreen with account deletion & sign-out cleanup, pill navigation, ProtectedImage |
| 9 Protection/links/polish | ✅ | ok | 146/146 | 3e9ee27 | Screen security wiring across 26 files, Coil memory/disk cache and catalogue stores cleared on sign out, 600-line limit enforced |
| 10 Tests/release | ✅ | ok | 146/146 | b484deb | 146 unit tests passing with 0 failures or errors (11 targeted tests covering password rules, catalogue filtering, credit decode, Chamak failure copy, source upload, image search, wishlist sharing, account deletion, JSON encoding) |

## Deviations from iOS
- `compileSdk` 37 (automatically downloaded platform 37.0 to satisfy Compose 1.12.1 / Core 1.19.1 dependencies).
- Coil set up in `JewelApplication` instead of a separate `JewelImageLoader` file.
- `clearProductImages` sends an explicit `null` for `processed_image_url` and empty JSON array for `generated_image_urls` (intentional fix of iOS encoding bug, plan §7.6 / §18 D13).
- Invitations use `source: "web"` (plan §8.5 / §18 D9).
- No Apple sign-in (plan §18 D4), no in-app purchases (plan §18 D5), no push notifications (plan §18 D6).
- Device ID: persists a random UUID in `Prefs.deviceId`, avoiding `ANDROID_ID` (plan §8.2).

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
  5. StaffProfileScreen with staff details, account deletion dialog calling `deleteMyAccount()`, and sign-out cleanup clearing LikeBook, ViewModeStore, MarketplaceCatalogueStore, and Coil memory/disk cache.
  6. Catalogue folder export using `ActivityResultContracts.OpenDocumentTree`.
  7. Verification screenshots captured and saved in `docs/screens/`: `protect_catalogue.png`, `chamak_result.png`, `edit_product.png`, `reprocess_overlay.png`, `reject_reason_sheet.png`, `staff_profile.png`.
