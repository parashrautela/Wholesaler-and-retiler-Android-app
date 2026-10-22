# Pre-release audit — wholesaler Android app

**Date:** 22 Oct 2026
**Scope:** wholesaler app only. Retailer and store-employee flows are parked (section 6), per Parash.
**Source audited:** last edit 19 Oct 13:39, which is commit `a188904` plus 130 uncommitted files.

Paths below are relative to `app/src/main/java/com/jewelindia/app/` unless they start with `app/`.

---

## Verdict

**Don't hand the current APK to a client yet.** These are what a client hits in the first few minutes:

- **New account by email or phone:** the app crashes on "Create Password", right after the OTP.
- **Back button:** pressing it inside chat, profile, report, Treasure Chest and so on closes the whole app.
- **Publish switch:** turning "Publish to retailer" on in a product's detail sheet unpublishes the product.
- **Add Product:** purity, weights, size, style, stock and make-to-order days are silently thrown away.
- **Front door:** it still offers "I'm a retailer" (highlighted) and "I'm a store employee", which lead into flows we aren't shipping.

On top of that:
- Any app on the phone can open the wholesaler dashboard without signing in (debug backdoor).
- The release APK isn't signed.
- Google sign-in only works on builds signed with this laptop's debug key.

Most blockers are a few lines each. The bigger items are Add Product metadata and release signing.

---

## How this was checked

- **Code reading:** every wholesaler screen, view model and API client (auth, onboarding, home, catalogue, add/edit/reprocess, orders, invites, chat, manufacturing offers, Chamak, profile, credits). Compared against the iOS app and the live backend routes (`Jewel-India-Frontend/app/api/*`, `ai-pipeline/app/main.py`, the feature-lock migrations).
- **Build:** debug and release both build. Lint: 0 errors, 2 hints. **Unit tests: 176/178 pass, 2 fail** (see 2.4).
- **Emulator, every screen:** opened all 46 preview screens on a Pixel 6 emulator (API 35). 45 render. **Set Password crashes** (stack trace in 1.1).
- **Emulator, release build:** installed the R8-minified release build, signed with the debug key for testing only. It starts in 0.7 s, the front door and dashboard render, and there are no crashes. **The debug backdoor works in it** (1.2).
- **Not covered at runtime:**
  - Anything that needs a signed-in account. There's no test login on the emulator, and I didn't send OTPs to real numbers.
  - Camera, Razorpay and real uploads.
  - These findings come from reading the code, checked against the backend source.

---

## 1. Blockers — fix before any client sees it

### 1.1 Create Password, Forgot Password and Update Password crash on open (runtime-confirmed)

`feature/auth/AuthKit.kt:153-157`
- `AuthLayout` already wraps its content in `verticalScroll`.

Each of these screens adds a second `verticalScroll` inside it:
- `feature/auth/SetPasswordScreen.kt:62`
- `feature/auth/PasswordResetScreens.kt:52` (Forgot Password)
- `feature/auth/PasswordResetScreens.kt:161` (Update Password, opened from the reset link)

Compose doesn't allow a scroll inside a scroll, so it throws on first layout:

```
IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints
```

- **Who it hits:** every new wholesaler who signs up with email or phone crashes right after entering the OTP, so they can never finish signing up. Google signups skip this screen. Everyone who taps "Forgot Password?" crashes too.
- **Not new:** this is in the last commit as well, not only in the uncommitted work.
- **Fix:** remove `.verticalScroll(...)` from the inner `Column` in those three places. `AuthLayout` already scrolls.

### 1.2 Debug backdoor in the release build (runtime-confirmed)

`MainActivity.kt:94-98` and `core/session/SessionStore.kt:71-74`

- **What happens:** an intent extra `DEBUG_DESTINATION=WholesalerDashboard` cancels the auth watcher and forces `Phase.Authenticated(WholesalerDashboard)`.
- **Who can send it:** `MainActivity` is exported, so any installed app (or adb) can send this.
- **Tested:** on a wiped, signed-out release install, it opened the dashboard straight away.
- **Impact:** data stays empty because the database still needs a session, but it skips sign-in and the verification/rejection gate.
- **Fix:** delete the branch, or wrap it in `if (BuildConfig.DEBUG)`. Move `setDebugDestination` into `src/debug`.

### 1.3 The Publish switch in the product sheet does the opposite

`feature/wholesaler/ProductDetailSheet.kt:408-419` and `WholesalerCatalogScreen.kt:594`

The switch makes two writes:
1. It writes `is_published = v`.
2. It then calls `onPublishToggle(updated)` → `viewModel.togglePublish(updated)`. That computes `!updated.isPublished` and writes it again.

- **Result:** switch ON leaves the product **unpublished** while the switch shows ON, and the reverse.
- **Errors swallowed:** `catch (_: Exception) {}`. A refusal from the admin `publish_to_retailer` lock also looks like success.
- **Fix:** do one write only. Either the sheet writes and the callback just updates the list (add `viewModel.setPublished(id, v)`), or the sheet only calls the view model. Show errors and revert the switch on failure.

### 1.4 Add Product throws away everything except title and type

`feature/wholesaler/AddProductForm.kt:121-152`

- **What the pipeline stores:** `/process` only stores `title`, `jewellery_type`, `wholesaler_id` and `image_count` (pipeline `create_product`).
- **What is lost:** material category, style, size, purity, gross/stone/net weight, stock and make-to-order days are never saved.
- **How iOS does it:** iOS runs `saveProduct` after `/process` to write them (`AddProductForm.swift` ~295-345).
- **Impact:** every design uploaded from Android reaches retailers with no purity or weights.
- **Fix:** after `/process` returns the product id, call `WholesalerApi.updateProduct(id, ProductEdit(...))` with the form values. If that second write fails, show an error ("Saved, but details failed — edit the product to add them").

### 1.5 System Back closes the app

`feature/wholesaler/WholesalerShell.kt:140-206`

- **Cause:** overlays are drawn with an early `return` and no `BackHandler`. A grep for `BackHandler` in the wholesaler code returns 0.
- **Where it happens:** in all of these, Back or the back gesture **exits the app** instead of closing the page:
  - chat thread
  - offers list and offer detail
  - weekly report
  - profile
  - Treasure Chest
  - invitation history
  - upload history
  - Shared links (`WholesalerCatalogScreen.kt:582`)
- **Tabs too:** Back on a non-Home tab also exits instead of going to Home.
- **Fix:** add `BackHandler(enabled = overlay != null) { overlay = null }` per overlay in the shell (and in the catalogue for Shared links), plus `BackHandler(enabled = tab != Home) { tab = Home }`.

### 1.6 Retailer and store-employee doors are still on the front door

`feature/auth/RoleChoiceScreen.kt:161-196`

- **What's wrong:** "I'm a retailer" is the highlighted, featured card. Both doors lead into flows we're not shipping, and the retailer side has decode bugs (section 6).
- **Fix:** for this build, show only "I'm a wholesaler" and "Sign in". Gate the other two behind a build flag so they can return later.

---

## 2. Release setup — required before the APK goes out

1. **Signing.** `app/build.gradle.kts:43` has no `signingConfig`, so the release APK is unsigned and can't be installed. Create an upload keystore, keep it out of git, back it up, and add a `signingConfig` that reads from `keystore.properties` or environment variables.
2. **Google sign-in SHA-1.**
   - Native Google sign-in only works when the APK's signing SHA-1 is registered on the Android OAuth client. Today only this laptop's debug key (`34:06:04:…`) is registered.
   - Register the release keystore's SHA-1, and the Play App Signing SHA-1 if it goes through Play.
   - Without this, every Google sign-in on the client's phone fails with a raw developer error.
3. **Commit.** 130 files have been changed since `a188904` and none of it is committed. Commit (no push needed) so the APK the client gets can be rebuilt exactly.
4. **Tests.** The 2 failing tests:
   - `ChamakPickerValidationTest.testFusionModeNeverBlocks` is stale: the code now blocks a missing image in fusion mode on purpose. Update the test.
   - `ChamakStepMachineTest.testOpenGalleryItemFailedStatusSetsStepFailed` calls the **production** Supabase REST API from a JVM unit test (`GET chamak_generations?id=eq.g-failed`). Fake the API. Unit tests must never touch prod.
5. **Don't send the debug APK.** It's debuggable and contains the preview harness (`--es jewelPeek …` opens any screen with sample data).
6. **Version.** `versionCode = 1`, `versionName = "1.0"`. Bump for every build you give out so the client can tell which one they have.

---

## 3. Broken flows (P1)

### Sign-in and passwords
- **Google sign-in via browser always reports "Cancelled"** (`feature/auth/GoogleSignIn.kt:109-113`).
  - **Cause:** it waits on `sessionStatus.first { Authenticated || NotAuthenticated }`. The flow is already `NotAuthenticated`, so it returns immediately.
  - **When it matters:** this path is used whenever the phone has no Google account.
  - **Knock-on:** when the deep link signs the user in later, the chosen door's role is lost, so new users land on Select Role.
  - **Fix:** skip the current value (`drop(1)`) or wait only for `Authenticated`, with a timeout.
- **Forgot Password always says "Check Your Email"** (`PasswordResetScreens.kt:109-110`). `requestPasswordReset()` returns an error string instead of throwing, and the result is ignored. Network and rate-limit failures look like success.
- **Update Password ignores failure** (`PasswordResetScreens.kt:255-258`).
  - **What happens:** same pattern. If the reset link is opened on another device (no PKCE verifier) or the new password matches the old one, it still leaves recovery mode.
  - **Effect:** the user believes the password changed.

### Onboarding
- **The onboarding fee is skipped whenever the fee check fails** (`feature/onboarding/OnboardSteps.kt` ~264-277). If the fee-status call fails for any reason (offline, 401, 500), it submits without payment.
  - ⚠️ **Decision needed from Parash.** iOS removed the in-app fee ("The App Store release does not collect an onboarding fee", `OnboardSteps.swift:236`), but Android still charges it through Razorpay. Pick one behaviour for both apps. If Android keeps it, fail closed.
- **Rejected wholesalers can never resubmit** (`OnboardSubmittedScreen.kt:322-324`). "Resubmit" only calls `refreshDestination()`, and the router sends them back to the same screen. iOS has the same loop.
- **"Take Photo" crashes on a fresh install** (`feature/onboarding/ImageUploadBox.kt:110,296-300`).
  - **Cause:** the manifest declares `CAMERA`, but the app never requests it at runtime. `TakePicture` then throws `SecurityException`, and there's no try/catch.
  - **Where:** this is the only camera button in the app (Aadhaar, PAN and GST uploads).
  - **Fix:** request the permission first, or remove `CAMERA` from the manifest. Without it, `TakePicture` doesn't need the permission.

### Catalogue and products
- **Delete Product has no confirmation and no error** (`CatalogueProductCard.kt:217-227`, `WholesalerCatalogScreen.kt:435`). One mis-tap permanently deletes a design and its images. A failed delete is silent.
- **"Save as draft" saves nothing** but shows success (`AddProductForm.kt:160-163`). iOS uses `insertDraftProduct`.
- **Re-upload in Reprocess can crash or freeze** (`ReprocessFlow.kt:203-216`). It reads the photo and runs `ImageNormalizer` on the main thread with no try/catch. A cloud-only or unreadable photo crashes the app, and a large one freezes it. Add/Edit Product already had this fixed; copy that.

### Orders
- **Order actions fail silently** (`WholesalerOrdersScreen.kt:279-292`).
  - **What happens:** Accept, Mark as Packed, Mark as Dispatched and Reject all pass `onError = {}`.
  - **Effect:** network errors, invalid transitions and the admin `orders` lock all look the same: nothing happens and the sheet stays open.

### Invite a retailer
- **Opening the invite sheet creates an invitation** with gift = 1000 before the user does anything (`InviteRetailerSheet.kt:95-105`). That reserves credits, and errors leave a blank sheet.
- **The gift stepper is decorative** (`InviteRetailerSheet.kt:232-273,126`).
  - **What's wrong:** the code was already created with 1000, but the share text promises the stepper amount (for example "3000 points").
  - **Effect:** the retailer is promised credits they won't get.
  - **Range:** the stepper also ignores the server's min/step/max (1000/500/10000). It moves in steps of 250 and can reach 0.
- **Any server refusal falls back to a direct DB insert** (`data/api/JewelApi.kt:256-329`).
  - **What's caught:** every error from `/api/referral/generate`, including "insufficient credits" (409), "not verified" (403) and validation errors (400).
  - **What happens instead:** it inserts a `referral_links` row directly, skipping the server's credit reservation and losing the real reason.
  - **Fix:** only fall back on 404/405.
- **"Latest invitation" can be dead** (`JewelApi.kt:214-254`). There's no `accepted_at is null` / `expires_at > now` filter, so an expired or used code can be shown as unclaimed and shared.
- **Cancel invitation can silently do nothing** (`JewelApi.kt:198-212`). It doesn't check the HTTP status, swallows every error, and has no DB fallback. iOS falls back to setting `is_active = false`.
- **Invitation history skips the newest 20** (`InvitationHistoryScreen.kt:55`). It asks for `page = 1`, but the API is 0-based. Most wholesalers see an empty history.
- **Invite links never open the app** (`core/session/SignupFlow.kt`, `AndroidManifest.xml`, `MainActivity.kt:103`). There are three mismatches:
  - The manifest only accepts `app.jewelindia.shop/join/*`.
  - The app generates `jewelindia.shop/join/…`.
  - `captureRetailerInvitation` requires the Vercel `SITE_URL` host.
  - On top of that, a cold start is in the `Loading` phase, so the link is dropped there too.
  - This is low priority while the retailer side is parked, but it's why shared links open the website instead of the app.

### Home
- **The "New Chats" KPI is always 0** (`data/api/WholesalerApi.kt:111-126`). It filters on `messages.*` without selecting the `messages!inner(id)` embed. PostgREST rejects the query, and the error is swallowed.

### Chat
- **New messages never appear until you reopen the thread** (`feature/chat/ChatThreadScreen.kt:89-101`). It loads once, with no polling, realtime or refresh on resume. A load error shows as an empty thread.

### Manufacturing offers
- **Shared-quotation enquiries post to the wrong endpoint** (`feature/manufacturing/AcceptQuoteSheet.kt:131`). It never passes `parallel = (broadcastMode == "parallel")`, so it hits `/accept` instead of `/quotes`. The copy also doesn't switch to "Submit Quote". See iOS `AcceptQuoteSheet.swift:194`.

### Chamak
- **A failed export purchase still exports** (`feature/wholesaler/chamak/ChamakExportButton.kt:60-68`).
  - **The leak:** a `purchase_export` failure is swallowed, and any refusal other than "insufficient credits" is ignored. The image is downloaded and shared anyway, which is a free paid export.
  - **Other problems:**
    - It exports the detail preview URL.
    - Download and share errors are silent.
    - The button shows even when export isn't priced (iOS hides it).

### Error text shown to users (affects many screens)
- **Raw database errors reach the UI** (`data/net/ApiError.kt:36,57,96-99`).
  - **Cause:** supabase-kt's `PostgrestRestException.message` is a multi-line dump:

    ```
    <msg>
    Code: …
    Hint: …
    Details: …
    URL: https://…/rest/v1/…
    Headers: …
    Http Method: …
    ```

    `DbRefusal` and `userMessage()` show that text.
  - **Where users see it:** any database refusal, including the admin feature locks that are live since 18 Oct (`chamak`, `set_creation`, `publish_to_retailer`, `custom_inquiries`, `orders`, `messages`). Those raise PT403 "locked by Jewel India admin".
  - **Fix:**
    - Use `e.error` (the first line) only.
    - Map PT403 / "locked by Jewel India admin" to friendly copy, for example "This feature is turned off for your account. Contact Jewel India."
    - Check every `e.message` that reaches a `Text`.

---

## 4. Edge cases and wrong behaviour (P2)

**Sign-in and OTP**
- **Staff gate** (`SessionStore.kt:209-222`): a failed staff-status lookup is treated as "deactivated", so flaky network signs active users out. iOS lets them through.
- **OTP Resend** (`VerifyOtpScreen.kt:63,85,313,320`): Resend disappears after one wrong code, and also when the screen opens after the 60 s window. The user has to go back and start again.
- **OTP session import** (`VerifyOtpScreen.kt:300-303`): if the OTP succeeds but the session import fails (swallowed in `JewelApi`), Set Password then fails with "Session expired". There's no way to recover.
- **Set Password checklist** (`SetPasswordScreen.kt:30-35`): it shows 4 rules but leaves out the special-character rule the server enforces. All ticks are green and the server still rejects the password.
- **Phone signups:** password reset is email-only, so wholesalers who signed up by phone can't reset.

**Onboarding**
- **Stuck submit** (`OnboardSteps.kt` ~247-254): `session.user.value ?: return` runs after `isSubmitting = true`, so the button sticks on "Submitting application…" if the user is null.
- **Main-thread image work** (`OnboardFlow.kt:260-283`): image normalisation (5 JPEG decode/encodes) runs on the main thread during submit, which freezes the screen. Upload errors also embed the raw exception.
- **"I Understand"** on a pending application signs the user out (`OnboardSubmittedScreen.kt:325-327`; iOS does the same). It feels like being logged out.
- **Upload preview** (`ImageUploadBox.kt:202-204`): it decodes the full-size photo on the main thread.

**Home and catalogue**
- **Category tap** (`WholesalerShell.kt:251-254`): `selectedCatalogueCategory` is set but never passed to the catalogue, so a Home category tap opens it unfiltered.
- **Latest Creations** (`LatestCreationsSection.kt:55,256-264`): tapping a Home "Latest Creations" card does nothing, because `onOpenGeneration` is never passed.
- **Profile rows** (`WholesalerShell.kt:176-180`): the Profile "Treasure Chest" and "Invite a Retailer" rows do nothing, because the callbacks aren't passed.
- **Chains filter** (`WholesalerApi.kt:166-170`): it misses the iOS aliases (chain/chains/neck chain/neck chains).
- **Reprocess:** product images are wiped before `/process/{id}` runs. If reprocessing fails (429, network, 422), the product is left with no AI images. Clear them only after success.
- **"Export to folder"** (`WholesalerCatalogScreen.kt:111-138`): it writes a raw JSON dump of product rows (internal IDs, storage URLs) but tells the user "Exported N designs". The toast also never resets.
- **Edit Product** (`EditProductScreen.kt:205` inside `CatalogSheetHosts.kt:62`): it's a sheet inside a sheet. The outer sheet can be swiped away mid-save.
- **Search and filter:** category chips and search only filter the pages loaded so far (100 per page).

**Errors that are swallowed** (offline looks like "empty")
- **Weekly report** (`WholesalerReportScreen.kt:78-88`): a failed load looks like an empty report.
- **Offers list and detail.**
- **Chat:** "Report message" (`ChatThreadScreen.kt:386-387`).
- **Invitation cancel** (`InvitationHistoryScreen.kt:123-128`).
- **Chamak feedback** (`ChamakFeedbackSheet.kt:128`).
- **Fake invitation settings** (`JewelApi.kt:119-186`): any failure, including 401/403, returns made-up settings (2000 credits available, gift 1000).

**Chamak**
- **Stuck "generating" rows** (`ChamakApi.kt:324-332`, `394-402`): Fusion and Set rows are marked `generating` before the pipeline call. If the pipeline refuses (402, a lock, any 4xx), the row stays "generating" in the gallery.

**Images**
- **Large photos and PDFs** (`core/images/ImageNormalizer.kt:117-126`): it only downsamples while *both* sides exceed the target, so panoramas decode at full size and can run out of memory. PDFs have no size limit.

---

## 5. Polish (P3)

**Sign-in**
- **"Remember me"** (`SignInScreen.kt:126-157`): the checkbox does nothing.
- **OTP "Edit"** goes to the Sign-in door, which rejects phone numbers, instead of back to signup.
- **Phone numbers:** the Sign-in door rejects them ("Phone number sign-in isn't available"). It's the same on iOS, but confusing.
- **Special characters:** `PasswordRule.Special` accepts `` ` `` and `~`, but the server doesn't.

**Home, products and credits**
- **Home "Upload" CTA** switches to the Catalogue tab. iOS opens Add Product instead.
- **Credits** load once when the shell opens and don't refresh on resume, even though the daily allowance resets at midnight.
- **Low-balance card:** its cost is hard-coded at 200.
- **Edit Product:**
  - Make-to-order defaults to "14" and is saved even for in-stock items.
  - The first save writes `showcase_image_urls`, which changes which photo shows first.
  - There's no field validation.
- **Upload History** (`UploadHistoryScreen.kt:66-78`) shows the last 50 uploads of all time, although it's titled "today".

**Chamak**
- **Result tile:** `ChamakResultScreen.kt:432` labels the first source tile "Necklace" whatever the design is.
- **Image viewer:** `ChamakImageViewer.kt:77` calls `coerceIn(0, size - 1)`, which throws on an empty list. Callers almost always pass at least one image.

**Other**
- **Fee formatting:** `OnboardingFeeSheet.kt:98-117` re-checks the payment on resume, which can say "not received yet" before the user has paid. It also shows "₹15000" with no thousands separator.
- **Daily credits:** when not verified, `CreditsApi.fetchWallet` shows generic copy. iOS explains that daily credits need an approved business account.
- **Sign-out:**
  - The OTP cookie jar isn't cleared.
  - Sign-out paths other than `SessionStore.signOut` (banned/deactivated) skip the cache cleanup.
- **Order status:** `OrdersApi.setStatus` sends `status.name`, so `inProduction` goes out as "inProduction", not "in_production". This is latent: the UI never sends that status.
- **Legal links:** `LegalLinks.kt:19` does nothing when the phone has no browser.

---

## 6. Parked — retailer and store employee (not in this build)

Hide the doors (1.6) and these can wait. They are JSON contract mismatches with the live routes:

- **Retailer Orders** (`JewelApiModels.kt:205-209`): `RetailerOrdersResponse.suppliers` requires `Wholesaler.id`, but `/api/retailer/orders` suppliers have no `id`. The screen fails for every retailer with an order.
- **Product detail** (`JewelApiModels.kt:191-195`): `ProductDetailResponse.isSelected` vs the server's `is_selected`. Retailer product detail never loads through the API.
- **Marketplace paging** (`JewelApiModels.kt:139-176`): it reads `selectedProductIDs`, `hasMore`, `nextCursor` and `pageSize`, but the server sends snake_case. The marketplace stops at the first 24 designs and shortlist hearts never show.
- **Referral code** (`SetPasswordScreen.kt:159`): `markSignupCompleted()` clears it before retailer onboarding attaches it.
- **Contract tests:** `MarketplaceContractTest` probably uses the same camelCase fixtures, so it passes while prod fails.

---

## 7. Backend (not Android — for whoever owns the pipeline)

- **Unauthenticated reprocessing:** `ai-pipeline` `POST /process/{id}` (`main.py:655`) has no auth, owner check, credit charge or consent check. Anyone with a product UUID can trigger free AI reprocessing.
- **Public product rows:** `GET /product/{id}` returns full product rows without authentication.

---

## 8. Suggested fix order (for Antigravity)

**Batch 1 — blockers.** Mostly one-line or few-line fixes.
1. 1.1 nested scroll (3 lines).
2. 1.2 backdoor.
3. 1.3 publish switch.
4. 1.5 BackHandlers.
5. 1.6 hide doors.
6. Delete confirmation.
7. Error-text helper (section 3, "Error text").
8. Camera permission.

**Batch 2 — broken flows.**
1. 1.4 Add Product metadata and the draft save.
2. Order `onError`.
3. Forgot/Update Password results.
4. Google browser fallback.
5. Invite sheet: no auto-create, a stepper that drives the real gift, fallback only on 404, filters, cancel, page 0.
6. Unread KPI.
7. Chat refresh.
8. Parallel quotes.
9. Chamak export.
10. Reprocess picker.

**Batch 3.** Section 4, then section 5.

**Release.** Section 2: keystore, SHA-1, commit, tests, version bump.

**Before handing over**, run this on a real phone with a real test wholesaler account:
1. Email signup through OTP, Create Password, onboarding and submit.
2. Sign out, then sign in with a password.
3. Google sign-in on the release-signed APK.
4. Add a product with purity and weights, and check them in the catalogue and on the website.
5. Publish on, then off, from the detail sheet.
6. Delete a product.
7. Accept → Packed → Dispatched on an order.
8. Send and receive a chat.
9. Run a Chamak generation and export.
10. Press Back on every page.
