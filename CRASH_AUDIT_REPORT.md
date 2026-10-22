# Jewel India Android crash audit

Original audit: 18 October 2026 · Source revision `a188904` · Application code was unchanged at the time of that audit.

## Follow-up: lint cleanup completed

Current working tree, verified on 18 October 2026:

- **Debug lint: 0 errors, 0 warnings, 0 hints.**
- **Release lint: 0 errors, 0 warnings, 0 hints.**
- **176 unit tests passed**, with no failures, errors, or skipped tests.
- Debug and minified release APKs built successfully. The release APK is unsigned.
- The final build emitted no Kotlin compiler warnings or Gradle deprecation warnings with `--warning-mode all`.
- All three formerly crashing wholesaler onboarding previews stayed open on the Android 15 emulator. A temporary, unsubmitted sign-in field retained its value when rotated to landscape; emulator rotation settings were restored afterward.

The cleanup fixed finding 1's shared onboarding logo, finding 8's navigation enum preservation, and the lint session-state error. It also corrected Compose parameters, color opacity, locale observation, deprecated APIs, and obsolete/unused resources. Preview-only category artwork now lives under the debug source set, and the asset scripts preserve that separation. The target SDK is now 37, and size/orientation changes are handled without recreating the Compose activity.

**One intentional advisory exclusion:** `app/lint.xml` ignores only the newer-Gradle-version recommendation for `gradle/wrapper/gradle-wrapper.properties`. Gradle 9.6 is pinned to the Android plugin's documented recommended version, because Gradle 9.8 reports deprecated `setVisible` calls inside AGP 9.4. App correctness warnings remain enabled; there is no lint baseline or blanket warning suppression. See [AGP compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes).

Evidence: [verification summary](docs/crash-audit/verification-after.json), [debug lint](docs/crash-audit/lint-debug-after.xml), [release lint](docs/crash-audit/lint-release-after.xml), [successful build](docs/crash-audit/build-after.log), [onboarding checks](docs/crash-audit/onboarding-after.json), and [rotation check](docs/crash-audit/rotation-after.json).

**Remaining runtime work:** Findings 2–7 below were not fixed by this lint cleanup. Physical devices, Android 17 runtime compatibility, authenticated end-to-end actions, and sustained memory/ANR profiling remain unverified. Zero lint issues does not certify a crash-free app.

The original audit below retains its before-fix evidence and counts for reference.

---

**What this repo does:** This is the native Android app for wholesalers, retailers, and showroom staff. It handles signup, catalogues, photos, AI designs, orders, chat, and credits. This audit assumes normal use on Android phones, including slow connections, large camera photos, and repeated navigation.

**Main result:** One shared onboarding component causes a confirmed fatal crash across multiple screens. There are also five code-backed crash or freeze risks and two follow-up reliability issues below. These are not eight independently observed crash signatures. The available emulator crash traces all identify the same onboarding cause; production-device logs are needed to explain every crash reported by users.

## Evidence and checks

| Check | Result |
| --- | --- |
| Existing emulator | Android 15 / API 35, `sdk_gphone64_arm64`, app version 1.0 / code 1 |
| Crash buffer | Existing fatal traces for onboarding steps 1 and 2; fresh fatal traces for steps 2 and 3 during this audit |
| Preview smoke checks | Opened 12 existing debug previews. Onboarding failed; nine other previews had a live process at the short observation point |
| Unit tests | 176 tests across 43 suites, zero failures, errors, or skipped tests |
| Android lint | Failed: 1 error, 72 warnings, 1 hint |
| Source changes | None. Only this report and diagnostic evidence were added |

Command: `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew --offline :app:testDebugUnitTest :app:lintDebug`.

The combined command exited unsuccessfully because lint failed. The unit-test results passed. The installed emulator APK was inspected without replacing it; its exact binary/source equivalence was not established. Fresh traces match the current source and shared resource bug.

Evidence: [emulator crash traces](docs/crash-audit/emulator-crashes.log), [preview observations](docs/crash-audit/preview-smoke-results.json), and [lint output](docs/crash-audit/lint-results-debug.txt). Emulator timestamps are in Asia/Kolkata.

The preview process check waits only 1.5 seconds after each launch returns. Step 3 initially appeared alive but then produced a fatal trace. Therefore a live process in this table is not a claim that a screen or its actions are fully tested.

## Must fix

### 1. All onboarding steps load an unsupported launcher icon — confirmed fatal crash

**Where:** `app/src/main/java/com/jewelindia/app/feature/onboarding/OnboardKit.kt:230`; `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml:2`.

- **What this is:** `LeftPanel` draws the logo inside the shared signup layout. Both wholesaler and retailer signup use it for steps 1, 2, and 3.
- **Problem:** `painterResource(R.mipmap.ic_launcher)` resolves to an `<adaptive-icon>` XML file. Compose's image painter rejects this drawable and throws `IllegalArgumentException: Only VectorDrawables and rasterized asset types are supported` on the main thread.
- **Fix:** Use a dedicated PNG or supported vector logo for this one in-app image. Keep the adaptive icon for the Android launcher; fix the shared `LeftPanel` once.
- **If we skip it:** Users cannot reliably open or advance through either signup flow. Changing each individual step will not solve the common cause.

Call sites: `feature/onboarding/OnboardSteps.kt:47,123,280` and `feature/retailer_onboarding/RetailerOnboardSteps.kt:50,126,260`. All paths are beneath `app/src/main/java/com/jewelindia/app/`.

The emulator traces show `LeftPanel → OnboardLayout → OnboardStep1/2/3Screen`. Retailer impact follows from its identical shared layout calls; retailer signup was not independently exercised. Android documents the supported image types in [Resources in Compose](https://developer.android.com/develop/ui/compose/resources).

### 2. Taking a document photo can crash because camera permission is never requested — code-backed

**Where:** `app/src/main/AndroidManifest.xml:5`; `feature/onboarding/ImageUploadBox.kt:304`.

- **What this is:** Signup lets the user take a photo of an identity or business document through the device camera app.
- **Problem:** The manifest declares `CAMERA`, but the source contains no runtime camera-permission request or check. On a fresh install or after permission is revoked, `cameraLauncher.launch(uri)` can throw an uncaught `SecurityException`; a missing camera handler can also fail at launch.
- **Fix:** Since this app delegates capture to another camera app, remove the unnecessary camera permission after confirming there is no direct camera access. Handle camera-launch failure and show a useful message.
- **If we skip it:** Fixing finding 1 exposes another signup crash when users choose “Take Photo.”

No camera-permission crash was captured in this audit because onboarding itself crashes first. The declared-permission behavior is explicitly documented by [Android's permission guidance](https://developer.android.com/privacy-and-security/minimize-permission-requests).

### 3. Several photo pickers let file-opening errors escape — code-backed

**Where:** `feature/wholesaler/AddProductScreen.kt:70`; `feature/wholesaler/chamak/ChamakCatalogPickerScreen.kt:103`; `feature/wholesaler/ReprocessFlow.kt:205`.

- **What this is:** These paths read the photo returned by the gallery or Android photo picker.
- **Problem:** `openInputStream()` and `readBytes()` run without error handling at these three entry points. A photo deleted after selection, an unavailable cloud provider, or a provider read failure can throw `FileNotFoundException`, `IOException`, or `SecurityException` and terminate the app; the Add Product coroutine also has no exception boundary.
- **Fix:** Catch file-access errors at each picker boundary, clear the pending selection, and display a retry message. Perform the read on the IO dispatcher; keep cancellation working.
- **If we skip it:** Add Product, Chamak custom upload, and Reprocess remain vulnerable even when ordinary local photos appear to work.

This was established from source, not reproduced with a failing content provider. Other upload paths already catch ordinary exceptions, so a global claim that every picker lacks error handling would be incorrect.

### 4. Photo imports and previews can exhaust phone memory — code-backed risk

**Where:** `core/images/ImageNormalizer.kt:60,66,109`; `feature/wholesaler/AddProductSections.kt:178`; `feature/onboarding/ImageUploadBox.kt:203,366`; the picker entry points in finding 3.

- **What this is:** The app reads entire selected files into byte arrays and decodes photos for upload and display.
- **Problem:** There is no input-size limit before `readBytes()`, and Add Product decodes the original full-size photo just to display a small preview. A 12,000 × 9,000 image needs about 432 MB for one ARGB bitmap alone, before the compressed input, upload copies, and other screens; on a constrained heap this produces `OutOfMemoryError`, which ordinary `catch (Exception)` blocks do not handle.
- **Fix:** Enforce a bounded read, reject oversized documents, and decode a sampled preview rather than the original. Correct the normalizer's sampling calculation for extreme aspect ratios and reuse the normalized bytes instead of retaining and rereading large originals.
- **If we skip it:** Crashes depend on photo dimensions and phone memory, making the same action work on one phone and fail on another.

The shared sampler only reduces resolution while **both** half-dimensions remain above the requested size. A 20,000 × 1,000 panorama is therefore decoded at full resolution even with a 2,048-pixel target. The 8 MB constant limits attempted JPEG output compression; it does not bound input file reads or peak bitmap memory. Onboarding PDFs pass through normalization unchanged.

Affected paths include wholesaler add/edit/reprocess, retailer design uploads, image search, document uploads, and manufacturing reference photos. Chamak custom inputs also retain the full imported byte array. Memory pressure was not profiled on a physical device, so this is a conditional failure risk, not an observed OOM trace.

### 5. An empty Chamak image viewer crashes immediately — code-backed

**Where:** `feature/wholesaler/chamak/ChamakImageViewer.kt:77,83`; `ChamakResultScreen.kt:84,253,500`; `ChamakFlowCoordinator.kt:48,67`.

- **What this is:** The fullscreen viewer displays generated output and the source photos.
- **Problem:** For an empty image list, `startIndex.coerceIn(0, images.size - 1)` becomes `coerceIn(0, -1)` and throws immediately. A gallery item with no source URLs can initially render the Result screen before output URLs finish signing; tapping the result placeholder then opens this empty list.
- **Fix:** Disable viewer opening until images exist and add an empty-list guard inside the shared viewer. Reset or clamp remembered selection when the image list changes so a shorter replacement list cannot leave an invalid index.
- **If we skip it:** Incomplete or slow-loading gallery results can turn a blank image into an app crash.

The result card's outer click target is enabled even when its output URL is null. The regular debug viewer fixture contains images and did not exercise this case. This failure is established from the empty-list code path, not from a captured emulator trace.

### 6. Photo reads and compression block the screen thread — freeze / ANR risk

**Where:** `feature/wholesaler/AddProductScreen.kt:70`; `AddProductForm.kt:123`; `ReprocessFlow.kt:205`; `feature/retailer/CatalogueImageSearch.kt:257`; `feature/onboarding/ImageUploadBox.kt:100,120`; `feature/wholesaler/EditProductScreen.kt:116`.

- **What this is:** Gallery callbacks and Compose coroutine scopes normally start on Android's main thread, which draws the screen and responds to taps.
- **Problem:** Several file reads run there, and JPEG normalization often runs there even when the preceding file read was moved to IO. A slow cloud-backed file or large photo can stop screen updates and input long enough for Android to report “Application Not Responding” (ANR).
- **Fix:** Move blocking file access to `Dispatchers.IO` and bitmap processing to a background dispatcher. Keep UI-state changes on the main thread and retain the existing loading/error state.
- **If we skip it:** Users continue to see hangs during photo selection or submission and may describe those as crashes.

This is separate from finding 4: moving work off the screen thread addresses responsiveness, but does not reduce the memory needed to decode an oversized photo. No ANR trace was obtained in this audit.

## Should fix

### 7. Screen-owned work is not consistently stopped when screens leave — verified lifecycle defect; crash impact unmeasured

**Where:** `feature/wholesaler/WholesalerHomeScreen.kt:81`; `WholesalerCatalogScreen.kt:104`; `WholesalerOrdersScreen.kt:85`; `feature/retailer/MarketplaceCatalogueStore.kt:49`; `feature/wholesaler/chamak/ChamakViewModel.kt:38,456,530`; `ChamakFlowCoordinator.kt:48`.

- **What this is:** Screens create holders that own network work and background timers.
- **Problem:** Wholesaler ViewModels are constructed using `remember { ... }`, bypassing the lifecycle ViewModel store, so their scope is not cleared automatically when forgotten. Marketplace and Chamak also create independent coroutine scopes without disposal; leaving an AI operation can leave polling and retained photo state alive until the operation finishes or times out.
- **Fix:** Obtain actual ViewModels from the lifecycle ViewModel store and bind plain screen holders to a screen scope or explicit disposal. Preserve intentional server-side generation; only stop abandoned UI work.
- **If we skip it:** Repeated navigation can retain unnecessary work and data, increasing memory and network pressure. This audit does not prove that it has already caused a production crash.

`resetToPicker()` stops Chamak timers, but there is no disposal hook that guarantees this runs when its owning composition disappears. Avoid introducing another app-wide scope as a workaround.

### 8. Release navigation has an unresolved enum-preservation warning — lint-backed risk

**Where:** `core/model/Models.kt:12`; `navigation/AuthRoutes.kt:25`; `app/build.gradle.kts:41`; `app/proguard-rules.pro`.

- **What this is:** Signup routes carry a `UserRole` enum, and release builds shrink and rename code.
- **Problem:** Navigation lint reports `MissingKeepAnnotation` for `UserRole`. The release build enables minification, and the existing rules do not explicitly preserve this navigation enum; this can produce a release-only route decoding failure.
- **Fix:** Apply the targeted keep annotation/rule recommended by Navigation, then test the minified release signup routes and saved-state restoration.
- **If we skip it:** Passing debug tests will not settle whether shipped signup navigation survives code shrinking.

This is a warning requiring release verification, not a confirmed cause of the crashes in the emulator. No minified release APK was built or exercised here.

## Other check failure: lint does not pass

`feature/wholesaler/WholesalerShell.kt:117` reads `sessionStore.user.value` directly during composition. Lint reports `StateFlowValueCalledInComposition`: the UI does not subscribe to later session changes. Collect the state as the surrounding Compose code does. This can leave stale UI and it currently fails the lint task; it is not evidence of a fatal exception by itself.

The remaining warning count includes layout, icon, locale, and convenience-API suggestions. Those were not presented as crash causes.

## Where the reported instability can appear

| Area | Relevant findings | Evidence limit |
| --- | --- | --- |
| Wholesaler signup steps 1–3 | 1; then 2–4 and 6 after the initial blocker is fixed | Fatal onboarding traces available |
| Retailer signup steps 1–3 | Same shared layout and document picker | Impact traced through callers; no independent retailer runtime pass |
| Wholesaler Add/Edit Product | 3, 4, 6, 7 | Source review; no large-file or failed-provider device test |
| Product reprocessing | 3, 4, 6 | Source review |
| Chamak custom uploads/results | 3–7 | Normal sample previews opened; empty/large inputs not exercised |
| Retailer image search/design uploads | 4, 6, 7 | Source review |
| Manufacturing reference photos | 4 | Source review; ordinary upload exceptions already have handling |
| Wholesaler home/catalogue/orders | 7 and the lint session issue | No separate fatal signature found in available logs |
| Staff catalogue/orders, chats, wishlists, payments/credits | No additional confirmed fatal cause established | Relevant stores/error handling inspected; authenticated end-to-end actions not exercised |

## Fix order and regression checks

1. Replace the shared onboarding logo and verify all six role/step combinations on API 26 and a current Android version.
2. Fix camera launch handling and photo import boundaries. Test fresh-install permissions, denied/revoked permissions, a missing photo, and an unavailable provider.
3. Bound file memory, sample previews, and move blocking work off the main thread. Test high-resolution photos, a very wide panorama, large PDFs, and multi-photo uploads on a low-memory phone.
4. Guard the Chamak viewer. Test no images, delayed output signing, one image, and an image list becoming shorter while open.
5. Bind background work to lifecycle ownership. Repeatedly enter/leave catalogue and AI flows; verify old requests and timers stop retaining screens and photos.
6. Fix the lint error, preserve the route enum, and run minified release signup/restoration checks.

The existing unit tests do not cover the actual Compose logo renderer, camera launch behavior, or real bitmap-memory pressure. `unitTests.isReturnDefaultValues = true` also means the local invalid-image test does not establish real Android decoding behavior. Add a small device/instrumentation regression check for these paths rather than relying on the current passing unit suite.

No crash-reporting SDK was found in the dependency/configuration scan. For crashes seen on user phones, collect the fatal exception or ANR trace, app build, Android version, screen/action, and timestamp. Matching those to these findings is necessary before claiming every reported crash is explained.

**Verdict:** Fix the shared onboarding logo first, then camera/photo imports and the empty viewer. Those changes address concrete failure paths across multiple screens without a rewrite.

**Not checked:** Physical devices, production crash dashboards, authenticated purchases or account mutations, every screen interaction, backend deployment/data contracts, native/GPU failures, minified release execution, and sustained memory/ANR profiling. Source review focused on shared startup/session/navigation, image handling, catalogue state, AI flows, and error boundaries; it was not a line-by-line review of every file.

**Risk:** The app is unchanged and remains vulnerable. This report identifies actionable causes and risks, but cannot certify all crashes resolved or all possible causes found.
