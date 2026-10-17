# Fix Brief: Reopen Phases 5–10 (Android port)

**For:** the implementing agent working in this folder.
**From:** a review of commit `a96de2a` against `IMPLEMENTATION_PLAN.md` and the iOS source, on 2026-10-17.
**Status of the build:** `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes, 68/68 tests. That's good, but compiling is not the same as working. The review found the problems below by reading the code and comparing it with iOS.

---

## 0. Rules (unchanged from the plan, repeated because they matter)

- Write only inside `/Users/parashrautela/Documents/jewel india /jewel india android app /` (both folder names end with a space). Everything else is read-only.
- iOS source of truth: `/Users/parashrautela/Documents/jewel india /wholesaler ios/JewelIndia/Sources/`. Below it is written as `iOS:`. Android paths are relative to `app/src/main/java/com/jewelindia/app/` and written as `KT:`.
- No backend changes, no deployments, no pushes. Don't run live purchases, Chamak generations or manufacturing requests against production.
- **Do the tasks in the order below.** After each task: `./gradlew :app:testDebugUnitTest :app:assembleDebug` must pass, then make one local commit named `fix(<area>): <what>`.
- Don't mark anything ✅ in `PORT_STATUS.md` unless it meets that task's **Done when** list. Section 9 tells you how to update PORT_STATUS.
- Never invent product behaviour. If iOS doesn't do it, Android doesn't do it. If you can't tell what iOS does, write it in `OPEN_QUESTIONS.md` and move on.

---

## Task 1: Screen protection is built but never used (CRITICAL)

**Problem.** `KT: core/security/{ScreenSecurity,CaptureProtected,ProtectedImage}.kt` exist and `MainActivity` provides `LocalScreenSecurity`. But **no screen calls `CaptureProtected` or `ProtectedImage`** (`grep -rlE "CaptureProtected|ProtectedImage\(" feature navigation` returns nothing). On API 33+ design photos can be screenshotted and recorded freely. PORT_STATUS claims this is done.

**Fix.**
1. In `KT: core/security/ProtectedImage.kt`, pass a fill modifier to the inner image: `CachedImage(url, contentDescription, modifier = Modifier.matchParentSize(), contentScale)`. Without it, the image doesn't fill the protected box.
2. Replace `CachedImage(...)` with `ProtectedImage(...)` for **every photo of a jewellery design / product / Chamak result** in these files. This is the exact Android counterpart of each iOS file that uses `ProtectedImageView` / `ZoomableProtectedImage` / `.captureProtected`:

   | iOS file (protected) | Android file to change |
   |---|---|
   | `Features/Wholesaler/WholesalerCatalogueView.swift` | `KT: feature/wholesaler/WholesalerCatalogScreen.kt` |
   | `Features/Wholesaler/ProductDetailSheet.swift` | `KT: feature/wholesaler/ProductSheets.kt` (ProductDetailSheet) |
   | `Features/Wholesaler/EditProductView.swift` | `KT: feature/wholesaler/EditProductScreen.kt` (new, Task 4) |
   | `Features/Wholesaler/Chamak/ChamakCatalogPickerView.swift`, `ChamakGalleryCard.swift`, `ChamakImageViewer.swift`, `ChamakResultView.swift`, `ChamakSetStylingView.swift`, `ChamakSliderFormView.swift` | `KT: feature/wholesaler/chamak/*` (all Chamak image surfaces, including the new files from Task 2) |
   | `Features/Retailer/YourTasteView.swift` | `KT: feature/retailer/YourTasteScreen.kt` |
   | `Features/Retailer/NewArrivalsStrip.swift` | `KT: feature/retailer/NewArrivalsStrip.kt` |
   | `Features/Retailer/RetailerCatalogueView.swift` | `KT: feature/retailer/RetailerCatalogueScreen.kt` |
   | `Features/Retailer/RetailerOrdersView.swift` | `KT: feature/retailer/RetailerOrdersScreen.kt` |
   | `Features/Employee/EmployeeCatalogueView.swift` | `KT: feature/employee/EmployeeCatalogueScreen.kt` |
   | `Features/Employee/EmployeeDesignsView.swift` | `KT: feature/employee/EmployeeDesignsScreen.kt` |
   | `Features/Employee/EmployeeDetailViews.swift` | `KT: feature/employee/EmployeeDetailViews.kt` |
   | `Features/Employee/EmployeeHomeView.swift` | `KT: feature/employee/EmployeeHomeScreen.kt` |
   | `Features/Employee/EmployeeInfiniteCanvas.swift` | `KT: feature/employee/EmployeeInfiniteCanvas.kt` |
   | `Features/Employee/EmployeeReviewView.swift` | `KT: feature/employee/EmployeeReviewScreen.kt` |

   In each iOS file, look at *which* images are protected and protect the same ones. Logos, artwork, avatars, category tiles and theme art stay as plain `CachedImage`, exactly like iOS.
3. In `LazyColumn`/`LazyVerticalGrid`/staggered grids, many `ProtectedImage`s each acquire and release. That's fine because `ScreenSecurity` is reference-counted. But make sure `ScreenSecurity.acquire/release` run on the main thread and never let the count go negative (add a guard + a unit test).

**Done when.**
- `grep -rlE "ProtectedImage\(|CaptureProtected\(" feature navigation` lists every Android file in the table.
- New JVM test `ScreenSecurityTest`: acquire ×3 → secure; release ×2 → still secure; release → not secure; extra release → stays 0, no crash.
- On an emulator (API 34): `adb exec-out screencap -p > docs/screens/protect_catalogue.png` on the wholesaler catalogue gives a black image. The same on the Role Choice screen gives a normal image. Save both screenshots.

---

## Task 2: Chamak doesn't work end-to-end (CRITICAL)

The whole Chamak feature is one 1,130-line file (`KT: feature/wholesaler/chamak/ChamakStudioScreen.kt`) against about 5,000 lines on iOS. Three things are broken and two screens are missing.

### 2a. Results and gallery images are loaded from raw storage paths
**Problem.** `ChamakStudioScreen.kt` (~lines 482, 999, 1109) passes `gen.outputImageURL` straight into `CachedImage`. Chamak outputs live in the **private** bucket `chamak-outputs`. The stored value is only a path, so the images never load.
**Fix.** Port the signing logic from `iOS: Features/Wholesaler/Chamak/ChamakViewModel.swift` lines ~190–260:
- Result screen: `signedOutputImageURL = ChamakApi.getSignedURL(generation.path(.detail))` and `signedFullOutputImageURL = ChamakApi.getSignedURL(generation.path(.full))` (`KT: data/api/ChamakModels.kt` line ~259 already has the variant lookup via `outputVariants`).
- Multiple outputs: sign every output path (iOS line ~215).
- Gallery: `ChamakApi.getSignedURLs(history.map { it.path(card) })` (iOS line ~236), then pass the signed URL into each gallery card.
- Keep signed URLs in the ViewModel state, not recomputed per recomposition.

### 2b. The picker never loads catalogue products
**Problem.** The `catalogueSource` parameter (line ~107) is accepted but never used. `ChamakApi.fetchWholesalerProducts` and `fetchStoreProducts` are never called, so wholesalers can't pick their own designs, and retailer Chamak (`catalogueSource = StoreDesigns`, opened from `KT: navigation/RetailerShell.kt` ~line 187) has nothing to pick from.
**Fix.** Port `iOS: ChamakViewModel.swift` lines ~160–175: `OwnProducts → ChamakApi.fetchWholesalerProducts(wholesalerId)`, `StoreDesigns → ChamakApi.fetchStoreProducts()`. Then port the picker UI from `iOS: Features/Wholesaler/Chamak/ChamakCatalogPickerView.swift`: search/filter, two slots, slot rules, preview, custom-photo upload, and the validation errors `missingImage`, `missingType`, `sameTypeSelected`, `setFull` (iOS `ChamakViewModel.swift` ~line 270).

### 2c. Split the file and port the missing screens
Replace the single file with the structure from plan §A1 (Chamak section). Keep behaviour identical to iOS:

| New Android file | Port from iOS | Must include |
|---|---|---|
| `KT: feature/wholesaler/chamak/ChamakViewModel.kt` | `ChamakViewModel.swift` | The `Step` state machine **exactly**: `catalogPicker → analyzing → sliderForm` (combine mode) or `setStyling` (set mode) `→ generating → result`, plus `failed` and `gallery`. Mode is set once and survives "reset to picker". Credit check against the rate card + wallet. Idempotency key per submit, kept across retries of the same submit. Polling `fetchGeneration` until a terminal status. The failure path shows the **server's** failure message (see `wholesaler ios/tests/chamak-failure-message/`). |
| `ChamakHubScreen.kt` | `ChamakHubView.swift` | Tool cards, set-creation card, saved gallery entry. |
| `ChamakFlowCoordinator.kt` | `ChamakFlowCoordinator.swift` | Nested navigation; `openingGeneration` opens straight on a saved result. |
| `ChamakCatalogPickerScreen.kt` | `ChamakCatalogPickerView.swift` | 2b above. |
| **`ChamakSliderFormScreen.kt`** (missing today) | `ChamakSliderFormView.swift` | Reads `currentGeneration.stage1AnalysisJSON` (`Stage1Analysis`). Content-flag banner when `contentFlag != ok` (iOS lines ~18, ~558). Strength lists `image1Strengths`/`image2Strengths` with the iOS defaults (`defaultStrengths1/2`, lines ~230, ~252). `dynamicAttributes` sliders 0.0–1.0 (line ~379), with the empty-list case handled (line ~341). Suggestion chips (max 2 selectable, line ~500). Builds `WholesalerFormInput` and calls `submitFormAndGenerate`. This is the screen whose "missing sliders and Try Again" bug was fixed in iOS build 40. Read `git -C "../wholesaler ios" show b07a46f` before porting. |
| `ChamakSetStylingScreen.kt` | `ChamakSetStylingView.swift` | Backdrops (`SetBackdrop`, exact raw values) and styling chips → `submitSetAndGenerate`. |
| `ChamakGeneratingScreen.kt` + `GemShapes.kt` | `ChamakGeneratingView.swift` | The four gem shapes ported as `Path`s; status copy. |
| `ChamakResultScreen.kt` | `ChamakResultView.swift` | Signed images (2a), protected, export button, feedback, try again / new run. |
| `ChamakGalleryScreen.kt` + `ChamakGalleryCard.kt` | `ChamakGalleryView.swift`, `ChamakGalleryCard.swift` | Signed thumbnails, stacked source designs in the corner, unfinished state shown in the photo area. |
| **`ChamakImageViewer.kt`** (missing today) | `ChamakImageViewer.swift` | Full-screen pager with pinch-zoom and double-tap zoom (`detectTransformGestures`), protected (`ZoomableProtectedImage`). Opened from the picker, slider form, set styling and result screens (iOS opens it from all four). |

### 2d. Don't invent Chamak features
PORT_STATUS mentions "prompt builder" and "studio style presets (Courtyard, Velvet, Royal)". Check every option shown in the Android UI against `iOS: ChamakModels.swift` (`SetBackdrop`, `SetStylingChip`, `ChamakMode`, `JewelleryTypeCanonical`). Remove anything that isn't in iOS. Copy labels verbatim.

**Done when.**
- `ChamakApi.getSignedURL`/`getSignedURLs`, `fetchWholesalerProducts` and `fetchStoreProducts` are each called from the Chamak feature code.
- No Chamak file exceeds about 600 lines.
- New tests: `ChamakStepMachineTest` (combine → sliderForm, set → setStyling, failure → failed with the server message, reset keeps mode), `ChamakPickerValidationTest` (the four validation errors), `ChamakSignedUrlTest` (variant path selection: card/detail/full fall back to `output_image_url`; already-signed URLs containing `token=` pass through unchanged).
- `@Preview`s for every Chamak screen using sample data.

---

## Task 3: Custom manufacturing enquiries send the wrong request (CRITICAL)

**Problem.** In `KT: data/api/ManufacturingApi.kt`:
1. `uploadManufacturingAsset` (line ~95) uploads to a Supabase storage bucket `manufacturing-assets` and returns a public URL. **iOS doesn't do this.** It POSTs the photo to the AI pipeline, `POST {AI_PIPELINE_URL}/api/retailer/manufacturing-assets` (multipart, field `file`, filename `reference.jpg`, `image/jpeg`, `Authorization: Bearer`), and gets back `AssetUploadResponse {ok, asset_id, preview_url, width, height, byte_size}` (iOS: `Networking/ManufacturingAPI.swift` lines 65–110).
2. `CreateRequestParams` (line ~74) sends `image_url` and is missing fields. The server expects iOS's `CreateManufacturingRequestParams` (iOS: `Features/Manufacturing/ManufacturingModels.swift` lines 270–303).
3. `createRetailerRequest` (line ~88) decodes the reply as `RequestDetailResponse` (expects `request`). The server returns `CreateRequestResponse {ok, request_id, state, active_offer_id, expires_at}` (iOS lines 114–126). So even a successful create throws.
4. No `Idempotency-Key` is sent on create (iOS sends one).

**Fix.**
- Replace `uploadManufacturingAsset` with `uploadAsset(imageData: ByteArray): AssetUploadResponse` exactly as iOS. Error = FastAPI `detail`, falling back to `"Image upload failed with status <code>."`. Delete the storage-bucket code.
- Replace `CreateRequestParams` with all iOS fields and serial names: `asset_id` (required), `category`, `min_weight_grams`, `max_weight_grams`, `material`, `purity?`, `gemstone_preference?`, `quantity`, `making_budget_mode?`, `making_budget_amount?`, `currency`, `metal_rate_snapshot?`, `metal_rate_basis?`, `delivery_needed_date`, `notes?`, `superseded_request_id?`, `quotation_window_hours` (default 24).
- `createRequest(params, idempotencyKey)` returns `CreateRequestResponse`. The idempotency key is generated once per tap of Submit in the sheet's state and reused on retry. Error = `detail`, falling back to `"Request creation failed."`.
- Update `KT: feature/manufacturing/CreateManufacturingRequestSheet.kt` (~lines 325–365) to match `iOS: Features/Manufacturing/CreateManufacturingRequestSheet.swift`: normalise photo (product mode) → `uploadAsset` → `asset_id` → `createRequest`. Include every form field iOS has (gemstone preference, currency, metal rate snapshot/basis, quotation window) with the same defaults and validation. Follow the "simplified requirements" migration `wholesaler ios/supabase/migrations/20261010_05_simplified_manufacturing_requirements.sql` for which fields are required.
- While here, compare `acceptOffer`, `declineOffer`, `awardQuote`, `cancelRequest` bodies and response decoding line by line with iOS (`ManufacturingAPI.swift` lines 202–318) and fix any mismatch, including the `/accept` vs `/quotes` (parallel mode) path choice and `AcceptOfferQuoteParams` (`total_quote_amount`, `proposed_delivery_date`, `comments`, `expected_version`).

**Done when.**
- No reference to a `manufacturing-assets` storage bucket remains (`grep -rn '"manufacturing-assets"' app/src` only matches the URL path).
- New test `ManufacturingContractTest`: encodes `CreateRequestParams` and asserts the exact JSON key set matches iOS; decodes a sample `CreateRequestResponse` and `AssetUploadResponse`; the parallel flag picks `/quotes`, otherwise `/accept`.

---

## Task 4: Wholesaler product features missing

### 4a. Re-upload to AI (missing)
**Problem.** `reprocessProduct`, `reprocessPollTick` and `clearProductImages` exist in `KT: data/api/WholesalerApi.kt` (~437–520) but nothing calls them. iOS's ProductDetailSheet calls this "the only polling flow in the app".
**Fix.** Port into `KT: feature/wholesaler/ProductSheets.kt` (or split out `ReprocessFlow.kt`) from `iOS: Features/Wholesaler/ProductDetailSheet.swift` lines ~29, 74–75, 290–325, plus `ReprocessState`, `ReprocessOverlay` and `ReprocessConfirmSheet` (lines ~368–540):
- Confirm sheet: re-run on the existing photo, or pick a new photo (normalised, product mode).
- Status lines verbatim: "Initiating re-upload..." → "Clearing old images..." (`clearProductImages`) → "Queueing AI pipeline..." (`reprocessProduct`) → "AI processing in progress..." → poll `reprocessPollTick` every 5 s, up to 300 s. The first time the HEAD probe hits, show the iOS "background removed" status. On `done` → "Processing complete!" and show the new renders. On error → `PipelineError.message` in the overlay.
- Cancel polling when the sheet is dismissed (tie the job to the ViewModel/sheet scope).

### 4b. Product delete leaves files behind
**Problem.** `deleteProductImages` is never called.
**Fix.** Wherever a product is deleted (catalogue context menu and detail sheet), do what iOS does: `deleteProduct(id)`, then `deleteProductImages(product)` (best effort, as iOS). Check `iOS: WholesalerCatalogueView.swift` and `ProductDetailSheet.swift` for the exact order and error handling.

### 4c. Edit Product screen (missing)
**Problem.** iOS opens `EditProductView` from the catalogue (`WholesalerCatalogueView.swift` line ~131). Android has no edit screen.
**Fix.** New `KT: feature/wholesaler/EditProductScreen.kt` ported from `iOS: Features/Wholesaler/EditProductView.swift`: title, **`category` = the material dropdown** (gold/silver/diamond…, not `jewellery_type`, see the iOS comment in `WholesalerAPI.ProductEdit`), `jewellery_type`, style, size, metal purity, net/gross/stone weight, stock, make-to-order days, published, custom + showcase image upload to `plant-images` (iOS line ~296). Save → `WholesalerApi.updateProduct(id, ProductEdit(...))` (with retry). Protected images (Task 1). Open it from the catalogue card menu and from the detail sheet, exactly where iOS does.

### 4d. Upload History screen (missing)
**Problem.** iOS opens `UploadHistoryView` from Home (`WholesalerShell.swift` line ~73, `HomeRoute.uploadHistory`) and from Add Product (`AddProductView.swift` line ~49).
**Fix.** New `KT: feature/wholesaler/UploadHistoryScreen.kt` from `iOS: Features/Wholesaler/UploadHistoryView.swift`: `fetchUploadUsage` + `fetchCatalogue` (iOS lines ~154–156), filtered to today's uploads, each with its AI status. Empty state copy verbatim ("Products you upload today will show up here."). Add the route to the wholesaler Home tab's NavHost and the Add Product entry point.

### 4e. Orders: timeline and reject-reason sheet (simplified today)
**Problem.** `KT: feature/wholesaler/WholesalerOrdersScreen.kt` has an inline free-text rejection field (~lines 253–313) and no timeline.
**Fix.** Port from `iOS: Features/Wholesaler/WholesalerOrdersView.swift`:
- `OrderTimeline` (iOS line ~537, used at ~344).
- `RejectOrderSheet` (iOS ~605, opened at ~377) with the reason chips **verbatim** ("Design out of stock", "Can't meet the delivery time", "Customisation not possible", "Metal rate has changed", "Below minimum order", plus any others in the file), the optional details field, "Tap the selected reason again to choose a different one.", and `FlowLayout` → `FlowRow`.
- `OrderDesignThumb`, `OrderStatusBadge` colours, `OrderTime` formatting, and the order detail layout as iOS (`WholesalerOrderDetail`, ~306).
- Status changes go through `OrdersApi.setStatus` (the RPC `order_set_status`), as `WholesalerApi.updateOrderStatus` already forwards.
- Split the file per plan §A1 so none exceeds about 600 lines.

**Done when.**
- `reprocessProduct`, `reprocessPollTick`, `clearProductImages` and `deleteProductImages` are each called from feature code.
- `EditProductScreen.kt` and `UploadHistoryScreen.kt` exist and are reachable from the same places as iOS.
- New tests: `ReprocessPollTest` (fake API: pending → background removed → done; 300 s timeout with `runTest` virtual time; cancellation stops polling), `ProductEditEncodingTest` (nulls omitted; `category` vs `jewellery_type` mapped correctly), `RejectReasonsTest` (exact reason list).

---

## Task 5: Staff profile missing

**Problem.** The employee shell has no `StaffProfileScreen`. iOS opens `StaffProfileView(current:)` from `EmployeeShell.swift` line ~130. It includes the staff member's details, the store, legal links, log out, and **delete account** (`ProfileViews.swift` lines 175–322, delete at ~294).
**Fix.** Add `StaffProfileScreen` + `StaffDetails` + `StaffProfileButton` to `KT: feature/profile/ProfileScreens.kt` (split if over about 600 lines), ported from iOS. Show it from `KT: navigation/EmployeeShell.kt` where iOS shows it, including the "back to dashboard" action for a store owner in Employee View.
**Done when.** You can reach it in the employee shell, and delete account calls `WholesalerApi.deleteMyAccount()` (the `delete-account` edge function) followed by sign-out, with the iOS confirmation copy.

---

## Task 6: Sign-out doesn't clear the image cache

**Problem.** `KT: AppGraph.kt` line ~42: `onSignOutCleanup` only clears `LikeBook`. Plan §7.7 requires clearing images, so the next person on a shared phone doesn't see the previous account's designs.
**Fix.** In the cleanup:
```kotlin
val loader = SingletonImageLoader.get(app)
loader.memoryCache?.clear()
loader.diskCache?.clear()
```
Also clear the marketplace cache (`MarketplaceCatalogueStore.clear()`, if it persists anything) and any shell-scoped stores that iOS clears in `SessionStore.signOut()` (`ImageCache.shared.clear()`, `MarketplaceCatalogueStore.shared.clear()`, `LikeBook.shared.clear()`, `ViewModeStore.clear`).
**Done when.** A unit test (or a clearly documented manual check) confirms the cleanup lambda calls all of them.

---

## Task 7: Size and fidelity sweep (after Tasks 1–6)

Phases 5–10 are a compressed port: about 26,000 Android feature lines against about 32,000 iOS lines. Go through the plan's §A1 tables **row by row** and, for every iOS file, confirm the Android file exists and has the same behaviour: the same states (loading, empty, error), the same copy, the same actions and the same backend calls. Pay special attention to the areas that shrank most:
- Employee: `EmployeeAppearance` (iOS 565 → Android 215), `EmployeeDetailViews` (667 → 319), `EmployeeReviewView` (907 → 403), `EmployeeShell` (custom pill nav geometry, ping dots).
- Wholesaler catalogue (576 → 309), including the export-to-folder action (`OpenDocumentTree`), which is currently missing.
- `YourTasteView` (marketplace) and `MarketplaceCatalogueStore` (cache-then-revalidate, cursor paging, debounced search, optimistic selection with rollback).
- `StoreThemeView` (claim / selected / locked badges, unlock with credits).

For each gap you find: fix it, or add a line to `OPEN_QUESTIONS.md` with what's missing and why it's deferred.

**Done when.** `PORT_STATUS.md` has a "File coverage" table (one row per iOS file from §A1) marked ✅ ported / 🟡 partial (what's missing) / ⛔ skipped (reason from plan §18 D12).

---

## Task 8: Tests for Phases 5–10

The count hasn't moved since Phase 4 (68). Add at least the tests named in Tasks 1–6, plus these from plan §16d.1 that don't exist yet: `PasswordRuleTest`, `CatalogueFilterTest`, `CreditWalletDecodeTest`, `PlanStatusDecodeTest`, `DailyCreditScheduleTest`, `ChamakFailureMessageTest`, `ChamakSourceUploadTest`, `ImageSearchTest`, `WishlistShareTest`, `AccountDeletionTest`, `JsonEncodingTest`. Reuse the exact input/expected vectors from `wholesaler ios/tests/<name>/`.

**Done when.** All of them exist and pass, and PORT_STATUS shows the new total.

---

## Task 9: Make PORT_STATUS.md accurate

- Set Phases 5–10 back to 🟡 now. Turn each back to ✅ only when its tasks above are done.
- Fill in the missing commit hashes.
- "Deviations from iOS" must list real ones, at least:
  - `compileSdk` 37
  - Coil set up in `JewelApplication` instead of a separate `JewelImageLoader` file
  - `clearProductImages` sends an explicit null (an intentional fix of an iOS encoding bug, plan D13)
  - Invitations use `source: "web"` (plan D9)
  - No Apple sign-in (D4), no in-app purchases (D5), no push (D6)
  - Anything else you change from iOS
- Remove claims that aren't true. For example, "prompt builder" and "studio style presets" if they don't exist on iOS.

---

## Not a bug (don't change)

- The invite "How it works" guide / "Don't show me again": `saveInvitationGuide` isn't called on Android, **but the current iOS sheet doesn't show the guide either** (only the API exists in `JewelAPI.swift`). Leave it unused, for parity.
- `ManufacturingApi.registerDeviceToken` is unused. That's correct (push is out of scope, plan D6).

---

## Final check before reporting done

```bash
cd "/Users/parashrautela/Documents/jewel india /jewel india android app "
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
grep -rlE "ProtectedImage\(|CaptureProtected\(" app/src/main/java | wc -l   # expect ≥ 14 files
grep -rn "getSignedURL\|fetchStoreProducts\|fetchWholesalerProducts" app/src/main/java/com/jewelindia/app/feature | wc -l   # expect > 0
grep -rn "reprocessPollTick\|deleteProductImages" app/src/main/java/com/jewelindia/app/feature | wc -l   # expect > 0
grep -rn '"manufacturing-assets"' app/src/main/java | grep -v "/api/retailer/manufacturing-assets"   # expect nothing
```

Then report back with: each task's commit hash, the new test count, the list of anything left in `OPEN_QUESTIONS.md`, and screenshots in `docs/screens/` for the protected catalogue, a Chamak result, the edit product screen, the reprocess overlay, the reject-reason sheet and the staff profile.
