# Fix brief — pre-release Batch 1 + Batch 2 (wholesaler app)

**For:** Antigravity
**From:** the review in `PRE_RELEASE_AUDIT_2026-10-22.md` (read it for background; this brief is self-contained)
**Goal:** make the wholesaler app safe to hand to a client. Batch 1 is the blockers; Batch 2 is the broken flows.

## Rules (same as before)

- Change files **only inside this Android project folder**. No backend, website or iOS changes. The backend is shared and stays as it is.
- **No pushes.** Commit locally only (see Step 0 and "Commits").
- Never print, log or commit secrets (`local.properties`, keys, tokens). Never put a service-role key in the app.
- **Don't send OTPs to real phone numbers or emails you don't own**, and don't create real orders, invitations or Chamak generations on other people's accounts. Use the test wholesaler account Parash gives you. If you don't have one, ask before testing anything that needs sign-in.
- Retailer and store-employee flows are **out of scope**. Don't fix them, but don't break them either.
- Paths below are relative to `app/src/main/java/com/jewelindia/app/` unless they start with `app/`.
- When this brief says "show the error", use `e.userMessage()` from `data/net/ApiError.kt` after you've done B1.7. Never show `e.message` directly.

## Step 0 — baseline commit (do this first)

130 files have changed since `a188904` and none of them are committed. Before touching anything:

```bash
git add -A && git commit -m "chore: baseline before pre-release fixes"
```

Check `git status` first so you're not committing a keystore, `local.properties` or build output. `.gitignore` should already cover those; if not, fix `.gitignore` first.

**Commits:** make one commit per item below, with a message like `fix(B1.3): publish switch writes once`. That's how the review will check your work.

**Known test failures (already failing before you start, not yours):**
- `ChamakPickerValidationTest.testFusionModeNeverBlocks`
- `ChamakStepMachineTest.testOpenGalleryItemFailedStatusSetsStepFailed`

Don't add new failures.

---

# Batch 1 — blockers

### B1.1 Create / Forgot / Update Password crash on open

**Problem:** `feature/auth/AuthKit.kt:153-157` — `AuthLayout` already wraps its content in `.verticalScroll(...)`. These three screens add a second `verticalScroll` inside it:
- `feature/auth/SetPasswordScreen.kt:62`
- `feature/auth/PasswordResetScreens.kt:52` (Forgot Password)
- `feature/auth/PasswordResetScreens.kt:161` (Update Password)

Compose throws `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`. Every email or phone signup crashes right after the OTP.

**Do:**
- Delete the `.verticalScroll(rememberScrollState())` modifier from the inner `Column` in those three places. Keep `fillMaxWidth()` and the `spacedBy` arrangement.
- Remove the now-unused imports.
- Grep `feature/` for any other screen that puts `verticalScroll` or `LazyColumn` inside `AuthLayout { }`, and fix it the same way.

**Done when:**
- `adb shell am start -n com.jewelindia.app/.MainActivity --es jewelPeek setpassword` on a debug build shows the Create Password screen and the process is still alive after 5 s (`adb shell pidof com.jewelindia.app`).
- On a device: Sign in → enter the test account's email → Continue → "Forgot Password?" opens without crashing. Don't press "Send Reset Link" unless it's the test account.
- The keyboard can scroll the form on a small screen (the outer `AuthLayout` scroll still works).

### B1.2 Remove the debug backdoor

**Problem:** `MainActivity.kt:94-98` reads an intent extra `DEBUG_DESTINATION=WholesalerDashboard` and calls `SessionStore.setDebugDestination(...)` (`core/session/SessionStore.kt:71-74`). That cancels the auth watcher and forces the wholesaler dashboard. `MainActivity` is exported, so any app can trigger it. It works on a signed-out release build (confirmed).

**Do:**
- Delete the `DEBUG_DESTINATION` branch in `handleIncomingIntent`.
- Delete `setDebugDestination` from `SessionStore`. Nothing else uses it.
- The `jewelPeek` harness lives in `src/debug` with a release stub. Leave it as it is.

**Done when:**
- `grep -rn "DEBUG_DESTINATION\|setDebugDestination" app/src` returns nothing.
- On a signed-out install, `adb shell am start -n com.jewelindia.app/.MainActivity --es DEBUG_DESTINATION WholesalerDashboard` stays on the front door.

### B1.3 Publish switch in the product sheet does the opposite

**Problem:** `feature/wholesaler/ProductDetailSheet.kt:408-419` makes two writes:
1. It writes `is_published = v` itself.
2. It then calls `onPublishToggle(updated)`. That is wired in `WholesalerCatalogScreen.kt:594` to `viewModel.togglePublish(updated)`, which writes `!updated.isPublished` (`WholesalerCatalogViewModel.kt:196-216`).

So the second write undoes the first. Errors are swallowed by `catch (_: Exception) {}`. The admin `publish_to_retailer` lock (PT403) is live, so a refused publish looks like it succeeded.

**Do:**
- Keep exactly **one** network write. Recommended:
  - The sheet keeps its `WholesalerApi.updateProductFlags(id, isPublished = v)` call.
  - Rename the callback to `onPublishedChanged: (Product) -> Unit`.
  - Add `fun replaceLocal(product: Product)` to the catalogue view model. It swaps the product in `loadedProducts` and re-emits `CatalogUiState.Content`, with **no network**.
  - Wire `onPublishedChanged = { viewModel.replaceLocal(it) }`.
- **On failure:** set the switch back (`isPublished = !v`) and show the error (Toast, or an error line under the switch).
- Disable the switch while the write is in flight, so it can't be double-tapped.
- If `togglePublish` has no other callers after this, delete it. If something else uses it, leave it.

**Done when:**
- A unit test on the view model shows `replaceLocal` updates the list without calling the API.
- On a device with the test account: open a processed product → switch Publish ON → close and reopen the sheet → it's ON. Refresh the catalogue → still ON. Switch it OFF → OFF after reopening. Check the `is_published` value of that product on the website or in Supabase.

### B1.4 Hide the retailer and store-employee doors

**Problem:** `feature/auth/RoleChoiceScreen.kt:161-196` shows "I'm a retailer" (featured, highlighted) and "I'm a store employee". This build is wholesaler only.

**Do:**
- In `app/build.gradle.kts` `defaultConfig`, add `buildConfigField("boolean", "SHOW_RETAILER_AND_STAFF_DOORS", "false")`.
- Wrap both `DoorCard`s in `if (BuildConfig.SHOW_RETAILER_AND_STAFF_DOORS) { … }`.
- Don't delete the retailer or staff code.
- Make sure the "Sign in" link and the wholesaler door still work, both signed-in (Select Role) and signed-out.

**Done when:**
- `--es jewelPeek doors` shows only the wholesaler door.
- Flipping the flag to `true` brings the other two back.

### B1.5 System Back closes the app

**Problem:** `feature/wholesaler/WholesalerShell.kt:140-206` draws every overlay with an early `return` and no `BackHandler`, so Back or the back gesture exits the app. The overlays are:
- chat thread
- offer detail
- offers list
- report
- profile
- Treasure Chest
- invitation history
- upload history

Back on a non-Home tab also exits.

**Do:** in `WholesalerShell`, **before** the first overlay `return`, add one handler that closes the top-most overlay in the same order the overlays are drawn, otherwise goes to Home:

```kotlin
val anyOverlay = activeChatThread != null || selectedOfferId != null || showOffersList ||
    showReport || showProfile || showTreasureChest || showInvitationHistory || showUploadHistory
BackHandler(enabled = anyOverlay || selectedTab != WholesalerTab.Home) {
    when {
        activeChatThread != null -> activeChatThread = null
        selectedOfferId != null -> selectedOfferId = null
        showOffersList -> showOffersList = false
        showReport -> showReport = false
        showProfile -> showProfile = false
        showTreasureChest -> showTreasureChest = false
        showInvitationHistory -> showInvitationHistory = false
        showUploadHistory -> showUploadHistory = false
        else -> selectedTab = WholesalerTab.Home
    }
}
```

In `WholesalerCatalogScreen.kt`:
- Add `BackHandler(enabled = showSharedLinksScreen) { showSharedLinksScreen = false }`. Shared links is drawn at line ~582.
- Add `BackHandler(enabled = isSelecting) { viewModel.setSelecting(false) }`.

Chamak already has its own `BackHandler`s (`ChamakFlowCoordinator.kt:53`, `ChamakHubScreen.kt:86`). Child handlers are composed later and win, so they keep working. Bottom sheets already close on Back.

**Done when:** on a device or emulator, Back does the following:
- In each overlay listed above, it returns to the screen underneath.
- On Catalogue, Chamak, Orders or Chat, it goes to Home.
- On Home, it exits.
- In catalogue select mode, it leaves select mode.
- In Shared links, it closes Shared links.
- Inside a Chamak step, it still steps back inside Chamak.

### B1.6 Delete Product needs a confirmation and an error

**Problem:** `feature/wholesaler/CatalogueProductCard.kt:217-227` → `WholesalerCatalogScreen.kt:524` `onDelete = { viewModel.deleteProduct(product) }` deletes immediately, and there's no `onError`.

**Do:**
- Show an `AlertDialog` before deleting:
  - Title: "Delete this design?"
  - Text: "This permanently removes the design and its photos from your catalogue and from retailers. This can't be undone."
  - Buttons: a red "Delete" and "Cancel".
- Pass `onError = { Toast.makeText(context, it, LENGTH_SHORT).show() }`. The screen already uses Toasts at lines 151/158.
- In `WholesalerCatalogViewModel.deleteProduct`, map the error with `e.userMessage()`.

**Done when:** Delete shows the dialog, Cancel does nothing, and Delete removes the card. With airplane mode on, Delete shows an error and the card stays.

### B1.7 Stop showing raw database errors (one helper, used everywhere)

**Problem:** supabase-kt's `RestException.message` (and so `PostgrestRestException` and `AuthRestException`) is a multi-line dump:

```
<msg>
Code: …
Hint: …
Details: …
URL: https://…/rest/v1/…
Headers: …
Http Method: …
```

`data/net/ApiError.kt:36,57,96-106` (`DbRefusal` / `userMessage()`) falls back to that text. About 41 places in `feature/` show `e.message` directly. Since 18 Oct, admin feature locks are raised as `PT403` with "locked by Jewel India admin" for `chamak`, `set_creation`, `publish_to_retailer`, `custom_inquiries`, `orders` and `messages`. Users currently see a raw dump.

**Do, in `data/net/ApiError.kt`:**
- Add a branch for `io.github.jan.supabase.exceptions.RestException` in `userMessage()` (before `else`) that uses `this.error`, the short first line, **never** `message`.
- Keep `PostgrestRestException` going through `DbRefusal` first so the known codes still map. `DbRefusal.code()` can keep scanning `e.message` for codes, but `DbRefusal.message()`'s `else` branch must return the short text (`(e as? RestException)?.error ?: e.message`).
- Add `Copy.featureLocked = "This feature has been turned off for your account by Jewel India. Contact support if you think this is a mistake."`. Return it when the code is `PT403` (`(e as? PostgrestRestException)?.code == "PT403"`) or the text contains "locked by Jewel India admin".
- Put the text mapping in a pure function, for example `internal fun friendlyDbText(error: String, code: String?): String`, so it can be unit tested without building exceptions.
- Strip anything after the first newline as a last line of defence.

**Do, in `feature/`:**
- Replace every user-visible `e.message` (`grep -rn "e\.message\|it\.message" feature`) with `e.userMessage()`.
- View model callbacks typed `(String) -> Unit` should pass `e.userMessage()`.
- Leave logging calls alone.

**Done when:**
- Unit tests for `friendlyDbText`:
  - A PT403 lock maps to `featureLocked`.
  - Multi-line text keeps only the first line.
  - Known `DbRefusal` codes still map.
  - Blank text maps to `Copy.genericFailure`.
- `grep -rn "\.message" feature | grep -v userMessage` only shows non-UI uses. List any you deliberately left in your report.

### B1.8 "Take Photo" crashes on a fresh install

**Problem:** `app/src/main/AndroidManifest.xml:5` declares `android.permission.CAMERA`, but the app never requests it. When an app *declares* CAMERA without having it granted, `ActivityResultContracts.TakePicture()` throws `SecurityException`. The only camera button is `feature/onboarding/ImageUploadBox.kt:110,296-300` (Aadhaar, PAN and GST uploads), and it has no try/catch.

**Do:**
- Remove the `CAMERA` `<uses-permission>` line. The system camera app doesn't need our permission when we don't declare it, and nothing in the app uses CameraX. Then check the merged manifest: after building, `app/build/intermediates/merged_manifests/release/processReleaseManifest/AndroidManifest.xml` must not contain CAMERA.
- Wrap `cameraLauncher.launch(uri)` in `try { … } catch (e: ActivityNotFoundException) { loadError = "No camera app found on this phone." }`. Also catch `SecurityException` the same way.
- While you're there: `ImageNormalizer.normalized(...)` in both launchers' callbacks (lines ~100 and ~120) runs on the main thread. Wrap it in `withContext(Dispatchers.Default)`.

**Done when:** on a fresh install (`adb shell pm clear com.jewelindia.app`), Onboarding → Aadhaar Front → Take Photo opens the camera, and the captured photo shows in the box. Use the emulator's virtual camera; no real documents needed.

---

# Batch 2 — broken flows

### B2.1 Add Product must save the details (and "Save as draft" must save)

**Problem:** `feature/wholesaler/AddProductForm.kt:107-174`.
- `WholesalerApi.processProduct(...)` only sends title, jewellery type, wholesaler id and image count. The pipeline creates the row with just those fields.
- Category, style, size, purity, the three weights, stock and make-to-order days are **never saved**.
- iOS saves them right after `/process` (`wholesaler ios/JewelIndia/Sources/Features/Wholesaler/AddProductForm.swift:253-345`, `saveProduct`).
- The `else` branch at the end ("Save as draft") saves nothing and reports success. iOS does a real insert (`insertDraftProduct`, lines 347-380).

**Do, step 1 — add the API in `data/api/WholesalerApi.kt`:**

`suspend fun completeProduct(productId: String, payload: JsonObject)` does the following:
- Updates `products` with the payload, filtered by `eq("id", productId)`.
- Uses `select(Columns.raw("id"))` and `decodeList<IdRow>()`.
- Runs inside `JewelNetwork.withRetry`.
- Throws `PipelineException("Your design was uploaded, but its details couldn't be saved. Open it in your catalogue and tap Edit to add them.")` if 0 rows come back.

**Do, step 2 — add `suspend fun insertDraftProduct(payload: JsonObject)`**, which does a plain insert.

**Do, step 3 — build the payload** with a pure function in the form, for example `internal fun productPayload(...)`, using `buildJsonObject` so nulls are explicit:

| Key | Value |
|---|---|
| `wholesaler_id` | the wholesaler id |
| `title` | the title |
| `jewellery_type` | the jewellery type |
| `category` | the material category |
| `style` | the style |
| `size` | the size |
| `metal_purity` | the purity |
| `stock_available` | Bool |
| `net_weight` | Double, or `JsonNull` |
| `gross_weight` | Double, or `JsonNull` |
| `stone_weight` | Double, or `JsonNull` |
| `make_to_order_days` | `JsonNull` if in stock, else Int |
| `wholesaler_email` | the user's email, if known |
| `raw_image_url` | the raw image URL, if known |

**Do, step 4 — the AI path:**
- After `processProduct` returns the id, call `completeProduct(id, productPayload(..., rawImageUrl = result.rawImageURL))`.
- If that call fails, keep the form open with the banner above and set `status = Error`.
- Change the primary button to **"Retry saving details"**. It calls `completeProduct` again for the same id and does **not** re-upload.
- `submissionKey` is a `val` (line 37). Make it a `var` and set a new UUID after a successful upload, as iOS does.

**Do, step 5 — the draft path:**
- Check the session user matches `wholesalerId`.
- If a photo is selected, normalise it on `Dispatchers.IO`.
- Upload it with `WholesalerApi.upload("plant-images", "raw/${uid.lowercase()}/product-draft_${UUID.randomUUID()}.jpg", jpeg, "image/jpeg", upsert = false)`.
- Then `insertDraftProduct(productPayload(..., isPublished = false))`. **No pipeline call** — drafts must not cost credits.
- On failure, use iOS's stage messages ("Uploading the product image failed: …" / "Saving the product draft failed: …") and **don't** call `onSuccess`.

**Done when:**
- A unit test for `productPayload` covers:
  - The in-stock case: `make_to_order_days` is null.
  - The make-to-order case.
  - Blank weights are null.
  - Every key name matches the table above.
- On a device with the test account:
  - Add a product with purity 22K, gross 12.5, stone 0.5, net 12.0, in stock. The catalogue card and the product sheet show those values.
  - Save a draft with a photo. It appears in the catalogue unpublished, and the credits balance is unchanged.
  - Delete both afterwards.

### B2.2 Order actions fail silently

**Problem:** `feature/wholesaler/WholesalerOrdersScreen.kt:276-295` passes `onError = {}` for Accept, Mark as Packed, Mark as Dispatched (`updateStatus`) and Reject (`rejectOrder`).

**Do:**
- Add `isUpdating: Boolean` and `actionError: String?` parameters to `WholesalerOrderDetailSheet` (and `RejectOrderSheet` if reject has its own sheet).
- Show the error in red above the action buttons, and disable the buttons while updating.
- In the screen, set `actionError = it` in `onError`, and clear it when the sheet opens or an action starts.
- The view model must pass `e.userMessage()`, so the `orders` lock shows `Copy.featureLocked`.

**Done when:**
- With airplane mode on, Accept shows an error and the sheet stays open with the buttons enabled again.
- Online, Accept → Packed → Dispatched works on a **test** order. Ask Parash for one; don't act on real customer orders.

### B2.3 Forgot Password and Update Password ignore failures

**Problem:**
- `feature/auth/PasswordResetScreens.kt:107-110`: `sessionStore.requestPasswordReset(...)` **returns** an error string (`core/session/SessionStore.kt:323-335`) instead of throwing. The result is ignored, so the screen always shows "Check Your Email".
- `PasswordResetScreens.kt:253-258`: `sessionStore.updatePassword(...)` (`SessionStore.kt:311-321`) has the same problem. It exits recovery even when the update failed.

**Do:**
- Forgot Password: `val err = sessionStore.requestPasswordReset(email.trim()); if (err == null) isSubmitted = true else errorMessage = err`.
- Update Password: if `err != null`, set `errorMessage = err` and **don't** call `exitPasswordRecovery()`, `refreshDestination()` or `onPasswordUpdated()`.
- In `SessionStore`, return `e.userMessage()` instead of `e.message`, so auth errors are short (relies on B1.7's `RestException` branch).

**Done when:**
- With airplane mode on, "Send Reset Link" shows an error and stays on the form.
- With the test account, a real reset email arrives.
- Opening a reset link and entering a too-short password shows an error and stays on Update Password.

### B2.4 Google sign-in via the browser always reports "Cancelled"

**Problem:** `feature/auth/GoogleSignIn.kt:101-122` (`signInInBrowser`). `sessionStatus` is a `StateFlow` that is already `NotAuthenticated`, so `first { Authenticated || NotAuthenticated }` returns at once and the function returns `Cancelled` before the user has finished in the browser. This path runs whenever the phone has no Google account (`NoCredentialException`).

**Do:** wait for real success, or a real cancel:
- Success: wait for `SessionStatus.Authenticated` only, skipping the current value.
- Cancel: the user comes back to the app (the activity reaches `RESUMED` again after having left it) and there's still no session **3 seconds** after that resume. The deep-link code exchange needs a moment.
- Use `activity.lifecycle.currentStateFlow` (lifecycle 2.11 is already a dependency): wait for it to drop below `RESUMED` (the browser opened), then for `RESUMED` again, then `withTimeoutOrNull(3.seconds)` waiting for `Authenticated`.
- Keep the 5-minute overall timeout.
- Return `SignedIn` or `Cancelled` accordingly. Callers already handle both.

**Done when:**
- On an emulator **with no Google account** added, tapping "Continue with Google" opens the browser. Then:
  - Pressing Back in the browser returns to the app with the spinner gone and no error.
  - Signing in with a test Google account lands in the app on the right screen (a new user goes to wholesaler onboarding when they came through the wholesaler door).
- A unit test with a fake status flow and lifecycle covers both paths.

### B2.5 Invite a retailer: the right gift, no surprise invitations, real errors

All in `feature/wholesaler/InviteRetailerSheet.kt`, `feature/wholesaler/InvitationHistoryScreen.kt` and `data/api/JewelApi.kt`. Server rules, from `Jewel-India-Frontend/app/api/referral/*` and `lib/supabase/referral-api.js`:
- **Range:** gifts are 1,000–10,000 credits in steps of 500.
- **Settings:** `GET /api/referral/manage?page=N` returns real settings (`available`, `minimum`, `step`, `maximum`) and `links` with a server-computed `status`. Pages are 0-based, 20 per page.
- **Errors:** they come back as `{ error: "<human text>", code }`. `parseApiError` already picks up the human text.

1. **Don't create an invitation when the sheet opens** (lines 95-105).
   - On open: load `invitationSettings(page = 0)`.
   - Show the newest link whose `status == "unclaimed"`, if any.
   - Only call `createRetailerInvitation(gift = pointsOffered)` when the user taps "Invite retailer now" and there's no unclaimed link (this is what iOS's `handleInviteNow` does).
2. **Make the stepper drive the real gift.**
   - Initialise `pointsOffered` from `settings.minimum`.
   - −/+ move by `settings.step`, clamped to `minimum..min(maximum, available)`. Put this in a pure function and unit test it.
   - If an unclaimed link is showing, show its real `giftCredits`, hide the stepper, and offer "Create new link" (which creates with the stepper value).
3. **Share text** (line 126) must use `inv.giftCredits`, not `pointsOffered`.
4. **`createRetailerInvitation`** (`JewelApi.kt:256-329`): delete the catch-all DB fallback that inserts `referral_links` directly. It skips the server's credit reservation and hides the real reason ("You need more credits for this gift.", "Only verified wholesalers can invite retailers."). Let `ApiError` propagate and show its message in the sheet.
5. **`fetchLatestActiveInvitation`** (`JewelApi.kt:214-254`): either delete it and use item 1, or add `accepted_at is null` and `expires_at > now()` filters. Prefer deleting it; the server already computes status.
6. **`invitationSettings`** (`JewelApi.kt:119-186`): stop returning made-up settings (`available = 2000`) on failure. Rethrow, so the sheet and history show an error with Retry.
7. **`cancelInvitation`** (`JewelApi.kt:198-212`): check `response.isSuccessful`; if not, `throw parseApiError(response.code, body)`. Don't swallow exceptions. In `InvitationHistoryScreen.kt:123-128`, show the error.
8. **History page** (`InvitationHistoryScreen.kt:55`): use `page = 0`, and add "Load more" while `links.size < count`. Show an error with Retry on load failure (line 56-58 currently swallows it).

**Done when:**
- Unit tests cover the stepper clamp and "pick newest unclaimed".
- With the test account:
  - Opening and closing the sheet without tapping creates **no** invitation (history count unchanged).
  - Choosing 1,500 and tapping Invite creates a link whose history row shows 1,500, and the share text says 1,500.
  - Cancelling it from history works, and the row shows cancelled.

### B2.6 "New Chats" KPI on Home is always 0

**Problem:** `data/api/WholesalerApi.kt:111-126` filters on `messages.sender_type` and `messages.is_read` without selecting the embed. PostgREST rejects the query, and the error is swallowed to `0`.

**Do:** `select(Columns.raw("id, messages!inner(id)")) { filter { … same three filters … } }`. This matches iOS `WholesalerAPI.swift:75-87`. Keep returning `rows.size`.

**Done when:** with the test account and one unread retailer message in a conversation, Home shows New Chats = 1, and the Chat tab badge matches.

### B2.7 Chat thread never shows new messages

**Problem:** `feature/chat/ChatThreadScreen.kt:89-101` loads messages once, and load errors are swallowed (`catch (_: Exception) {}`).

**Do:**
- Poll `ChatApi.fetchMessages(thread.id)` every **4 seconds** while the screen is open **and** the app is resumed. iOS does exactly this (`ChatThreadView.swift:32,230`).
- Use `LaunchedEffect` + `repeatOnLifecycle(RESUMED)` or `LifecycleResumeEffect`.
- Merge by message id, and keep the user at the bottom only if they were already at the bottom.
- Call `markRead` when new incoming messages arrive.
- If the first load fails, show an error row with Retry instead of an empty thread. Later poll failures stay silent.

**Done when:** with the thread open on the device, a reply sent from the retailer side (website, test retailer account) appears within about 5 s without leaving the screen. Airplane mode on first open shows the error row.

### B2.8 Shared-quotation enquiries post to the wrong endpoint

**Problem:** `feature/manufacturing/AcceptQuoteSheet.kt:131` calls `ManufacturingApi.acceptOffer(offer.id, params)` without `parallel`, so shared-quotation (parallel) enquiries hit `/accept` instead of `/quotes` (`ManufacturingApi.kt:190-203`). The Android `ManufacturingRequest` model (`core/model/ManufacturingModels.kt:89-112`) doesn't have the field at all.

**Do:**
- Add `@SerialName("broadcast_mode") val broadcastMode: String? = null` to `ManufacturingRequest`.
- Call `acceptOffer(offer.id, params, parallel = offer.request?.broadcastMode == "parallel", idempotencyKey = key)`, where `val key = remember { UUID.randomUUID().toString() }` per sheet, so a double tap can't create two quotes.
- Button copy: "Submit Quote" when parallel, else "Accept Enquiry with Quote" (iOS `AcceptQuoteSheet.swift:46`).
- In the offer detail screen, mirror iOS `WholesalerOfferDetailView.swift:101,104,231` copy for parallel ("Shared Quotation Deadline" / "Submit a quote. The retailer selects the supplier.").

**Done when:**
- A unit test decodes an offer JSON with `"broadcast_mode": "parallel"` and asserts the path ends in `/quotes`.
- The existing `acceptOfferPath` tests still pass.

### B2.9 Chamak export gives the image away when the purchase fails

**Problem:** `feature/wholesaler/chamak/ChamakExportButton.kt:57-99`.
- `CreditsApi.purchaseExport(...)` exceptions are swallowed (`catch (_: Exception) {}`).
- Any refusal other than `INSUFFICIENT_CREDITS` is ignored.
- Either way the image is downloaded and shared, which is a free paid export.
- Download and share errors are silent too.

**Do:**
- Continue to download **only** if `res.ok || res.alreadyOwned`.
- `res.isInsufficientCredits` → `onInsufficientCredits()` (as now).
- Any other `res.error` → show a message: map known codes if there are any in `CreditsModels`, else "Couldn't export this design. Please try again."
- Exception → show `e.userMessage()`. **Don't** export.
- Download or share failure → Toast "Couldn't download the image. Please try again.".
- Keep the `exporting` spinner logic.

**Done when:** a unit test (extract the decision into a pure function `exportDecision(res): Proceed | NeedCredits | Fail(msg)`) covers all four cases. On a device with airplane mode on, Export shows an error and **no** share sheet.

### B2.10 Reprocess re-upload picker can crash or freeze

**Problem:** `feature/wholesaler/ReprocessFlow.kt:203-217` reads the picked photo and runs `ImageNormalizer.jpeg(...)` on the main thread with no try/catch. If normalisation fails, it falls back to uploading the raw bytes labelled as JPEG (`?: bytes`).

**Do:** copy the pattern already used in `feature/wholesaler/EditProductScreen.kt:110-135`:
- `scope.launch`, with the read and normalisation inside `withContext(Dispatchers.IO)`.
- Show a loading state.
- Use `error("Couldn't read this photo. Please choose another image.")` when the read or the decode fails, and show that error in the sheet.
- No raw-bytes fallback.
- Disable "Confirm" while it's loading.

**Done when:** picking a large photo (12 MP+) doesn't freeze the sheet. Picking a non-image or unreadable file shows the error, and Confirm stays disabled.

---

## Final checks (after all items)

1. `./gradlew assembleDebug assembleRelease lintDebug testDebugUnitTest --continue`. Both APKs must build, lint must have 0 errors, and the only test failures must be the 2 known ones.
2. Re-run the screen sweep on an emulator with the **debug** build. Every id must print a pid, and the crash buffer must have no `com.jewelindia.app` entries:

```bash
ADB=~/Library/Android/sdk/platform-tools/adb; $ADB logcat -b crash -c
for id in addproduct addproduct-sheet catalogue-cards catalogue-links catalogue-select catalogue-share chamak-card chamak-hub chamak-hub-empty chamak-picker chamak-picker-sample chamak-result chamak-result-attempts chamak-slider-retry chamak-viewer chat-thread chats-wholesaler daily-credits doors entry-wholesaler home-creations home-lowbalance invite-card invite-guide manufacturing-offer nocredits onboard onboard2 onboard3 onboarding-fee order-detail-active order-detail-new otp productdetail profile-wholesaler reject-order report report-teaser setcreation-picker setcreation-styling setpassword signin submitted upload-card wholesaler wholesaler-orders; do
  $ADB shell am force-stop com.jewelindia.app
  $ADB shell am start -n com.jewelindia.app/.MainActivity --es jewelPeek $id >/dev/null; sleep 5
  echo "$id pid=$($ADB shell pidof com.jewelindia.app || echo DEAD)"
done
$ADB logcat -d -b crash | grep -A6 "Process: com.jewelindia.app"
```

3. Install the **release** build and confirm it starts. Then run `--es DEBUG_DESTINATION WholesalerDashboard` and confirm it stays on the front door.

## Not in this brief — don't touch

- **Onboarding fee** (`OnboardSteps.kt` ~264-277): iOS removed the in-app fee, Android still charges it, and it's skipped when the check fails. **Waiting on Parash's decision.**
- **"Resubmit" loop** for rejected wholesalers (`OnboardSubmittedScreen.kt:322-324`). It needs a product decision on where they go.
- **Invite deep links** (manifest host vs generated links vs `SITE_URL`). Parked with the retailer side.
- Everything in audit sections 4–7 (P2/P3, retailer/staff, backend). The exceptions are the small "while you're there" items named above.
- **Release signing, the Google SHA-1, version bump and the 2 known failing tests.** These are handled separately (audit section 2).

## Report back

For each item, give:
- the commit hash
- the files changed
- how you verified it: test names, the device or emulator steps you actually ran, and what you saw
- anything you skipped or couldn't verify, and why

Don't mark something done if you only built it. Say "built, not run on a device" instead.
