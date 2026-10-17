# Jewel India Android — Port Status

Last updated: 2026-10-17 · Current phase: 4 (Complete) · iOS reference build: 40 (feat/retailer-monetisation)

## Phase checklist
| Phase | Status | Build | Tests | Commit | Notes |
|---|---|---|---|---|---|
| 0 Scaffold | ✅ | ok | 0/0 | cb3e2a7 | Empty Compose app running on Pixel 6 AVD, displaying LaunchScreen with Cirka 32 |
| 1 Design + core | ✅ | ok | 10/10 | b751b94 | Tokens (Palette, Typography, Layout, Motion), core models, Copy, Credentials, JewelDates, components, and unit tests passing |
| 2 Data layer | ✅ | ok | 22/22 | cbeb1f9 | Supabase client, OkHttp clients, SsrSessionBridge, withRetry, ApiError, all API modules (Wholesaler, Chamak, Credits, Orders, Chat, Retailer, Staff, Wishlist, LikeBook, Invites, OnboardingFee, StoreActivity, WholesalerReport, ManufacturingApi), unit tests passing |
| 3 Auth + routing | ✅ | ok | 59/59 | aba91dd | SessionStore, AuthRouter, SignupFlow, ViewModeStore, 8 auth screens, AuthNavHost, RootScreen, deep link handling, WebShuffle, Prefs, ImageNormalizer, CachedImage, verified on Pixel 6 |
| 4 Onboarding | ✅ | ok | 68/68 | | Wholesaler & Retailer 3-step flows, OnboardingFeeSheet (Razorpay link + Custom Tab), OnboardSubmitted views with stage timeline, invite code linking, RootScreen phase routing, verified on Pixel 6 |
| 5 Wholesaler | ⬜ | | | | |
| 6 Chamak | ⬜ | | | | |
| 7 Retailer | ⬜ | | | | |
| 8 Employee | ⬜ | | | | |
| 9 Protection/links/polish | ⬜ | | | | |
| 10 Tests/release | ⬜ | | | | |

## Deviations from iOS
- None.

## Version changes from §4.2
- `compileSdk`: updated from 36 to 37 (automatically downloaded platform 37.0 to satisfy Compose 1.12.1 / Core 1.19.1 dependencies).

## Protocol & Parity Audits
- `JewelApi.createRetailerInvitation`: sends `idempotency_key` (not `generation_key`), and uses `source = "web"` in the fallback insert (plan §8.5/§18 D9).
- `WholesalerApi.clearProductImages`: sends explicit `JsonNull` for `processed_image_url` and empty json array for `generated_image_urls` (plan §7.6, D13).
- `StoreActivity`: persists random UUID in `Prefs.deviceId` (`device_id`), avoiding `ANDROID_ID` (plan §8.2).
- `WebShuffle`: JavaScript 64-bit Double arithmetic reproduced bit-for-bit with `jsToInt32` and verified by unit tests.
- `Prefs`: typed SharedPreferences wrapper backed by `jewel_prefs` handling all §7.9 keys.
- `ImageNormalizer`: PickedFile normalizer handling 2048px products, 1200px documents, quality stepping down to 40% under 8 MB cap.
- `CachedImage`: size-aware Coil async image with 35% Taupe placeholder.
- `ManufacturingApi`: full models and client for retailer manufacturing enquiries and wholesaler quotation/acceptance.

## Verification log
- 2026-10-17: Phase 0 verified on Pixel_6 emulator (API 34/35). APK installed, launched, and visual inspection of `docs/screens/phase0_launch.png` confirmed centered "Jewel India" in Cirka font against `#FEFEFE` background.
- 2026-10-17: Phase 1 verified with 10 unit tests in `CredentialsTest`, `JewelDatesTest`, and `ProductModelTest`. `./gradlew :app:assembleDebug :app:lintDebug` passed with 0 errors.
- 2026-10-17: Phase 2 verified with 22 unit tests across `SsrSessionBridgeTest`, `ApiErrorTest`, `MarketplaceContractTest`, and core model suites. `./gradlew :app:testDebugUnitTest :app:assembleDebug` passed with 0 errors.
- 2026-10-17: Phase 3 verified with 59 unit tests across `AuthRouterTest`, `SignupFlowTest`, `SessionEventMappingTest`, `WebShuffleTest`, `PrefsTest`, `ImageNormalizerTest`, and `DbRefusalTest`. Installed and launched on Pixel 6 emulator: verified launch crossfade, RoleChoiceScreen rendering with 3 doors, navigation to Sign In and EntryScreen, and back navigation (`docs/screens/role_choice.png`, `docs/screens/entry_signin.png`).
- 2026-10-17: Phase 4 verified with 68 unit tests (+9 in `OnboardFlowTest` and `RetailerOnboardFlowTest`). Wholesaler and Retailer 3-step flows, Razorpay onboarding fee sheet with Custom Tabs, submitted verification screens with animated timelines, invite code linking, and JVM fallback for Supabase session storage. `./gradlew :app:testDebugUnitTest :app:assembleDebug` clean; verified install and launch on Pixel 6 emulator (`docs/screens/phase4_screen.png`).
