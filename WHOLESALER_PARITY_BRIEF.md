# Wholesaler Parity Brief: make the Android wholesaler app work and look exactly like iOS

**For:** the implementing agent (Antigravity) working in this folder.
**Written:** 2026-10-17, after the founder tested the app on a real phone (Samsung Galaxy M17) with their real wholesaler account.
**Scope:** the **wholesaler** experience only: sign-in → wholesaler shell → every wholesaler screen and sheet, plus Chamak as used by wholesalers. Retailer and staff screens are **out of scope** for this brief. Don't touch them except where a shared component must change (then keep them compiling and looking the same or closer to iOS).

---

## 0. The North Star

> **The Android wholesaler app must be indistinguishable from the iOS wholesaler app, in what it shows (real data from the same backend), how it behaves, and how it looks.**

Concretely, three measurable goals, in priority order:

1. **It doesn't crash.** No uncaught exception, on any wholesaler screen, with or without network.
2. **It shows the real data.** Signed in as the founder's wholesaler account, Android shows the **same** products, images, counts, orders, chats, Chamak creations and credits as iOS shows for that account.
3. **It looks like iOS.** For every wholesaler screen, an Android screenshot next to the iOS reference screenshot (§3) shows the same layout, artwork, fonts, sizes, colours, spacing, copy and states. Not "inspired by". The same.

"Builds and tests pass" is **not** the goal. The founder judges this by opening the app on the phone.

### Rules (still in force from `IMPLEMENTATION_PLAN.md` §0)
- Write only inside `/Users/parashrautela/Documents/jewel india /jewel india android app /` (both folder names end with a space).
- The iOS source is read-only and is the **spec**: `/Users/parashrautela/Documents/jewel india /wholesaler ios/JewelIndia/Sources/` (written below as `iOS:`). Android paths below are relative to `app/src/main/java/com/jewelindia/app/` (written as `KT:`).
- No backend changes. The backend is identical for iOS and Android. If Android shows no data and iOS does, **the bug is in Android**.
- Never invent copy, features, sections or styling. If you can't find it in the iOS code or reference screenshots, it doesn't go in.
- Commit locally after each numbered task: `fix(wholesaler-<area>): …`. Never push.
- Don't mark anything done in `PORT_STATUS.md` until it meets that task's **Done when** checklist, **including the side-by-side screenshots**.

---

## 1. P0: Fix these first (they are why the founder saw no data and crashes)

### 1.1 Wrong wholesaler ID everywhere: catalogue, counts, orders, Chamak all empty

**Root cause (verified).** Android passes `wholesaler?.id` (the **primary key of the `wholesalers` table row**) as "the wholesaler ID" to every data call. In the database, `products.wholesaler_id`, `orders.wholesaler_id`, `conversations.wholesaler_id` and `chamak_generations.wholesaler_id` hold the **auth user ID** (`auth.uid()`). RLS also checks `auth.uid() = wholesaler_id`. iOS always passes `session.user.id`:

```
iOS: Features/Wholesaler/WholesalerHomeView.swift:277  WholesalerAPI.countProducts(wholesalerID: user.id)
iOS: Features/Wholesaler/WholesalerHomeView.swift:278  WholesalerAPI.countPendingOrders(wholesalerID: user.id)
iOS: Features/Wholesaler/WholesalerHomeView.swift:279  WholesalerAPI.countUnreadConversations(wholesalerID: user.id)
iOS: Features/Wholesaler/WholesalerHomeView.swift:280  WholesalerAPI.fetchUploadUsage(wholesalerID: user.id)
iOS: Features/Wholesaler/WholesalerHomeView.swift:668  gallery.refreshGallery(wholesalerID: user.id)
iOS: Features/Wholesaler/WholesalerCatalogueView.swift:286  WholesalerAPI.fetchCatalogue(wholesalerID: userId, …)
```

**Android call sites that are wrong** (each uses `wholesaler?.id`):
- `KT: feature/wholesaler/WholesalerHomeScreen.kt` ~96, ~114
- `KT: feature/wholesaler/WholesalerCatalogScreen.kt` ~104–105, ~286, ~326
- `KT: feature/wholesaler/WholesalerOrdersScreen.kt` ~78, ~87
- `KT: feature/wholesaler/WholesalerShell.kt` ~172
- `KT: feature/wholesaler/chamak/ChamakStudioScreen.kt` ~28 (`wholesaler?.id ?: user.id`, so the row ID wins)
- Anything else: `grep -rn "wholesaler?.id\|wholesaler.id" app/src/main/java`

**Fix.**
1. Introduce one source of truth: the shell resolves `val wholesalerUserId: String = sessionStore.user.value!!.id` once (it can't be null inside an authenticated shell; guard anyway and show the sign-in screen if it is). Pass **that** into every wholesaler screen and ViewModel. Name the parameter `wholesalerUserId` everywhere so this can't regress.
2. Keep `Wholesaler.id` (the row PK) **only** where iOS uses the row ID. That is the invitation code (`JewelAPI.swift`, `referral_links.wholesaler_id` = `wholesalers.id`) and nothing else. Grep the iOS sources for each usage before deciding.
3. `ChamakApi.requireLiveSession` and the gallery check compare against the auth user ID. They'll pass once the right ID is passed. Don't weaken the check.
4. Add a unit test `WholesalerIdentityTest` that fails if any wholesaler data function is called with something other than the session user ID. Use a fake session and a recording fake API.

**Done when.** Signed in on the phone as the founder's wholesaler account:
- Home "Insights" counts match iOS.
- Catalogue shows the same products with images.
- Orders and Chat show the same rows.
- Chamak gallery shows the same creations.

Write the numbers you saw into `PORT_STATUS.md` (counts only; no customer names or personal data).

### 1.2 Crash: Chamak load throws out of a coroutine and kills the app

**Root cause (verified from the phone's crash log).**
```
FATAL EXCEPTION: main  Process: com.jewelindia.app
com.jewelindia.app.data.api.ChamakException: Your session isn't active on this device, so your gallery can't be read…
  at ChamakApi.fetchWholesalerGallery(ChamakApi.kt:459)
  … LaunchedEffectImpl … StandaloneCoroutine{Cancelling}
```
`KT: feature/wholesaler/chamak/ChamakViewModelOperations.kt` lines ~20–44 run `fetchWholesalerGallery` inside `async { }` within `coroutineScope { }`. When an `async` child fails, `coroutineScope` cancels and **rethrows immediately**, so the `try/catch` around `await()` never runs. The outer block is `try { … } finally { … }` with **no catch**. The exception escapes into the `LaunchedEffect` and crashes the process. (The trigger here was 1.1, but any network error would crash the same way.)

**Fix.**
1. In `ChamakViewModel.load`, use `supervisorScope { … }`, **or** wrap each `async` body in `runCatching { … }` and handle the `Result`. Map failures to the iOS error copy, as iOS's `ChamakViewModel.load` does.
2. **App-wide rule (do it for every wholesaler screen in this brief):** a network or decoding error must never escape to a composable.
   - Move data loading out of composables into a `ViewModel` per screen (plan §7.3). Today only Chamak has a ViewModel. Home, Catalogue, Orders, Product sheets, Edit Product, Upload History, Invite, Report, Treasure Chest, Profile and Chat call APIs directly from `LaunchedEffect`/`rememberCoroutineScope`.
   - Each ViewModel exposes `StateFlow<UiState>` with `loading / content / empty / error(message)` and launches work with a `viewModelScope.launch` that catches `Exception` (rethrow `CancellationException`).
   - Shared helper: `fun ViewModel.launchSafely(onError: (Throwable) -> Unit, block: suspend CoroutineScope.() -> Unit)`.
3. Also fix `OnboardFlow.kt` and `RetailerOnboardFlow.kt`, which use the same `coroutineScope { async … }` pattern for uploads. A failing upload must surface the iOS error copy (which names the document, e.g. "Aadhar Front"), not crash.
4. Remove the 12 `!!` in `feature/wholesaler`, `feature/treasurechest`, `feature/chat` and `feature/profile`, or prove each one safe with a comment.

**Done when.**
- `grep -rn "Api\.[a-zA-Z]*(" app/src/main/java/com/jewelindia/app/feature/wholesaler --include=*Screen.kt --include=*Sheet.kt` returns nothing: all calls live in ViewModels.
- With **airplane mode on**, open every wholesaler tab, sheet and screen: no crash anywhere, and each shows the iOS error/empty state.
- `adb logcat -b crash` stays empty during that walk-through.

### 1.3 Data parity check against the live backend (after 1.1 and 1.2)

For every wholesaler screen, confirm that the Android request is **identical** to iOS: same table, same `select` columns, same filters, same order, same page size, same RPC parameters, same headers. Use the table in `IMPLEMENTATION_PLAN.md` §8 and the iOS file named in each screen spec (§5). Turn on OkHttp logging **in debug builds only** (`HttpLoggingInterceptor.Level.BASIC`, never `BODY`, and redact `Authorization`). Then compare the URLs Android sends with what the iOS code builds. Fix every mismatch.

Specific things to check:
- **Product images.** Screens must use `Product.displayImageUrl(size)` / `thumbnailUrls(size)` (variants first, then `processed_image_url` / `image_url` / `generated_image_urls` / `raw_image_url`, exactly as iOS `WholesalerModels.swift` ~258–315). If the founder's products show no images, log one product's raw JSON and check which fields are filled.
- `Product` JSON decoding is tolerant: unknown keys ignored, nulls → defaults, `generated_image_urls` might be a JSON array or null. **One bad row must not empty the whole list.** If decoding throws, log the row ID and skip that row, like a tolerant iOS decode, rather than showing nothing.
- The catalogue category filter uses `jewellery_type` with singular/plural and chain aliases (plan §11.1).

**Done when.** A short table in `PORT_STATUS.md`: screen → request(s) → "matches iOS ✅".

---

## 2. Why it doesn't look like iOS (verified causes)

Fix all of these as you rebuild each screen in §5:

1. **The iOS artwork isn't used.** 44 assets were copied into the project (`res/drawable-nodpi/`, `assets/svg/`), but only **2** are referenced anywhere (`google_g`, `auth_panel_image`). The wholesaler UI must use, at least:
   - **Tab bar:** `NavHome`, `NavCatalogue`, `NavOrders`, `NavChat` (SVGs, tinted, see plan §A2), and sparkles for Chamak.
   - **Top-right logo menu:** `JewelLogo`.
   - **Home hero:** `UploadDesignLeftHand`, `UploadDesignRightHand`, `UploadDesignCoin`, `UploadDesignWash`, `UploadDesignIcon`, `HeroFrame`.
   - **Chamak promo and hub:** `ChamakStudioArtwork`, `ChamakStudioArrow`, `ChamakNecklace`.
   - **Categories:** `CatNecklace`, `CatRings`, `CatBangles`, `CatEarrings`, `CatPendants`, `CatChains`, `CatHaram`, `CatMangalsutras`, `CatNosepins`.
   - **Invite sheet:** `InviteCardArtwork`, `InviteRewardArtwork`, `InvitePlus`, `InviteMinus`, `InviteCopy`, `InviteChecked`, `InviteConnector`, `InviteStepDivider`, `InviteUserPlus`, `InviteGuideLink`, `InviteGuideSignup`, `InviteGuideStore`.
   - **Add retailer:** `NavAddRetailer`. **Upload:** `NavUpload`.
2. **Generic Material components instead of the iOS ones.** 39 `Card(…)` and 14 `OutlinedTextField(…)` in `feature/wholesaler`. Material cards add elevation, tonal colour and their own corner radius. iOS uses flat white/cream surfaces with 1pt `Palette.border` hairlines and specific radii (`Layout.swift`: 4/6/8/12/16/pill). Replace them with small Jewel components that take the iOS metrics (`design/components/`). No Material elevation or tonal colours anywhere in the wholesaler UI.
3. **Invented copy.** For example, Android Home shows "STUDIO PIPELINE", "Transform Photos with AI", "Overview" and "Catalogue Categories", none of which exist on iOS. iOS Home says "Home", "Welcome", "Upload New Jewellery", "Reimagine Your Collection", "Upload Now", "Insights", "Chamak Studio", "Reimagine your jewellery.\nCreate something new.", "Get started", "Latest Creations", "Your newest Chamak designs and sets.", "Chamak", "Sets", "View All", "Running low on credits", "Buy". **Every visible string must be copied from the iOS file**, or from `Core/Copy.swift` via `Copy.kt`. Sweep every wholesaler file for strings that don't exist in the iOS sources:
   ```bash
   # for each string literal shown in an Android wholesaler screen, check it exists in iOS
   grep -rhoE '"[A-Z][^"]{3,80}"' app/src/main/java/com/jewelindia/app/feature/wholesaler | sort -u > /tmp/android_strings.txt
   # then for each line: grep -rF "<string>" "../wholesaler ios/JewelIndia/Sources" — anything not found is invented or reworded
   ```
4. **Hard-coded Tailwind greys instead of the palette.** `Color(0xFFE5E7EB)` ×14, `0xFFCA8A04` ×14, `0xFFE7E5E4` ×10, `0xFFF3F4F6` ×8 and so on. Use `Palette.*`, or the exact hex the iOS file uses at that spot (iOS also has some inline hexes, e.g. the low-balance banner `0xFFFBEB`/`0xFDE68A`/`0x92400E`; copy those exactly where iOS uses them).
5. **Typography.** Wholesaler headings are **Cirka** and body is **Manrope** (`RoleTheme.Wholesaler`), at the exact point sizes and weights in each iOS view (`.font(.manrope(13, weight: .bold))` → `manrope(13f, JewelWeight.Bold)`). Large navigation titles ("Home", "Catalogue", "Orders"…) are iOS **large titles** (34pt bold, SF Pro). On Android use `sfPro(34f, Bold)`, which maps to the system font, left-aligned, collapsing to the 17pt centred inline title on scroll (see §4.2).
6. **The shell chrome.** See §4.

---

## 3. The reference screenshots, and how to make the matching Android ones

### 3.1 iOS references (already captured for you)
`docs/ios-reference/wholesaler/<id>.png`: real iOS screens rendered on an iPhone 17 Simulator from the current iOS code, via the iOS app's DEBUG preview mode (`-JewelPeek <id>`, implemented in `iOS: Navigation/DebugScreenPeek.swift`, with sample data from `DebugPeekSamples`).

| Reference id | Screen |
|---|---|
| `wholesaler` | Wholesaler shell + Home (tab bar, top-right pill + logo, Home large title, hero, daily credits card, Insights) |
| `upload-card`, `chamak-card`, `home-creations`, `home-lowbalance`, `report-teaser` | Home cards: upload hero, Chamak promo, Latest Creations, low-balance banner, weekly report teaser |
| `report` | Weekly/monthly report sheet |
| `catalogue-cards` | Catalogue grid cards (photo, title, material · net weight, purity chip, stock line, ••• menu, category label on photo) |
| `productdetail` | Product detail sheet |
| `addproduct`, `addproduct-sheet` | Add product screen / sheet |
| `wholesaler-orders` | Orders tab (segmented control: New Orders · Active Orders · Completed · Rejected) |
| `order-detail-new`, `order-detail-active` | Order detail states |
| `reject-order` | Reject-order sheet with reason chips |
| `chats-wholesaler`, `chat-thread` | Queries & Chat list, a thread |
| `profile-wholesaler` | Wholesaler profile sheet |
| `invite-card`, `invite-gift`, `invite-guide` | Invite Retailer card, gift stepper, guide |
| `manufacturing-offer` | Manufacturing offer |
| `chamak-hub`, `chamak-hub-empty` | Chamak tab hub |
| `chamak-picker`, `chamak-picker-sample` | Chamak design picker |
| `chamak-slider-retry` | Chamak slider form (retry state) |
| `chamak-result`, `chamak-result-attempts` | Chamak result |
| `chamak-viewer` | Full-screen viewer |
| `setcreation-picker`, `setcreation-styling` | Set creation picker / styling |
| `daily-credits-sheet`, `daily-credits`, `nocredits` | Credits sheets |
| `onboard`, `onboard2`, `onboard3`, `submitted`, `onboarding-fee` | Wholesaler onboarding (for reference; lower priority) |
| `doors`, `entry-wholesaler`, `signin`, `otp`, `setpassword` | Auth screens a wholesaler passes through |

**Caveat:** the Simulator isn't signed in, so a few references show an error where the screen tried to load live data. For example `wholesaler-orders` shows "permission denied for function wholesaler_orders" under the tabs, and Home cards show grey loading placeholders. Copy the **layout and chrome** from those, and the **data state** from the screen's other references (e.g. `order-detail-*`, `catalogue-cards`) and from the iOS view code.

**Regenerating references** (if you need another iOS screen; this is read-only on the iOS folder because the build goes to a temp directory):
```bash
DD=/tmp/jewel-ios-dd
cd "/Users/parashrautela/Documents/jewel india /wholesaler ios"
xcodebuild -project JewelIndia.xcodeproj -scheme JewelIndia -sdk iphonesimulator -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' -derivedDataPath "$DD" CODE_SIGNING_ALLOWED=NO build
DEV=$(xcrun simctl list devices available | grep -m1 "iPhone 17 (" | grep -oE '[0-9A-F-]{36}')
xcrun simctl boot "$DEV" 2>/dev/null; xcrun simctl install "$DEV" "$DD/Build/Products/Debug-iphonesimulator/JewelIndia.app"
xcrun simctl terminate "$DEV" com.jewelindia.app; xcrun simctl launch "$DEV" com.jewelindia.app -JewelPeek <id>
# wait ~5 s, then:
xcrun simctl io "$DEV" screenshot "docs/ios-reference/wholesaler/<id>.png"
```
All peek ids are the `case "…"` labels in `iOS: Navigation/DebugScreenPeek.swift`.

### 3.2 Build the matching Android preview harness (debug only)
To compare like with like, Android needs the **same screens with the same sample data**, without an account:
1. Create `app/src/debug/java/com/jewelindia/app/debug/DebugPeek.kt`. If `MainActivity` is started with the extra `jewelPeek=<id>` (debug builds only, compiled out of release), it renders that screen with sample data instead of `RootScreen`.
2. Port the sample data from `DebugPeekSamples` in `iOS: Navigation/DebugScreenPeek.swift` (products, orders, chats, wallet, generations, reports) into `app/src/debug/.../DebugPeekSamples.kt`, using the **same values** so screenshots line up. Sample images: use the same URLs/assets iOS uses (the iOS peek uses files such as `peek-<asset>.jpg` and `employee-layout-<variant>.png` written into the app container; see `DebugScreenPeek.swift` for how they're produced). If you can't reproduce a remote image, bundle the same picture in `src/debug/res`.
3. Support at least every id in the §3.1 table.
4. Capture an Android screenshot for each:
   ```bash
   adb shell am start -n com.jewelindia.app/.MainActivity --es jewelPeek <id>
   # wait ~4 s
   adb exec-out screencap -p > docs/android-parity/wholesaler/<id>.png
   ```
   Use an emulator close to the iPhone 17's proportions (Pixel 7 / 1080×2400 is fine) at font scale 1.0.
5. Generate a side-by-side image per id (`docs/android-parity/wholesaler/<id>-compare.png`): iOS on the left, Android on the right, both scaled to the same height. A small script in `tools/compare_screens.sh` using `sips`, or Python with Pillow if available, is fine. Look at every comparison image yourself before marking a screen done.

---

## 4. Shell and chrome (applies to every wholesaler screen)

Port from `iOS: Navigation/WholesalerShell.swift`. Reference: `wholesaler.png`.

### 4.1 Bottom tab bar
- iOS 26 draws a **floating, rounded "glass" tab bar** inset from the screen edges: a white translucent pill with a soft shadow, about 64pt tall, about 16pt side margins, sitting above the home indicator. The **selected tab sits in a lighter rounded capsule** behind its icon and label. See the bottom of `wholesaler.png`.
- Android: build a custom `WholesalerTabBar` composable that reproduces this exactly. Don't use Material `NavigationBar`. Use a rounded-full shape, background `Color.White.copy(alpha = 0.92f)` (plus `Modifier.blur`-backed translucency on API 31+ if it helps match), a soft shadow (elevation about 12dp at about 8% black), and a selected capsule in `#F2F2F2`-ish. Sample the exact colours from the reference PNG.
- Five tabs, in order: **Home** (`NavHome`), **Catalogue** (`NavCatalogue`), **Chamak** (sparkles), **Orders** (`NavOrders`), **Chat** (`NavChat`). Labels from `Copy.WholesalerTab`. Icons about 22pt, label 11pt (SF/system font, medium). Selected = `Palette.dark`; unselected = `Palette.dark` at reduced opacity, matching the PNG.
- Content scrolls **under** the floating bar, with bottom padding so the last item can scroll clear of it.

### 4.2 Top bar
- **Large title** at the top-left of each tab root ("Home", "Catalogue", "Orders", "Queries & Chat", Chamak's title as in iOS). It collapses to a centred 17pt semibold inline title when scrolled, like iOS `.navigationBarTitleDisplayMode(.large)`. Use a `LargeTopAppBar`-style custom header driven by the scroll state, with **no** Material colours or elevation.
- **Top-right, on every tab:** the `CreditBalancePill` (sparkle + balance in a cream capsule) and the `JewelLogo` 26pt image in a 6pt-rounded square, inside one white floating capsule (see `wholesaler.png`, top right). Tapping the logo opens the menu with **Profile · Invite Retailer · Manufacturing Offers · (divider) · Log Out**, using the iOS icons and labels. Log Out → the confirmation dialog with `Copy.logoutTitle/logoutBody/logoutConfirm/logoutCancel`.
- The credit pill on Home pushes Treasure Chest. On other tabs it opens it as a sheet with "Done". The same split applies to Manufacturing Offers.

### 4.3 Sheets
iOS sheets are rounded-top cards with a grabber, at `.large` or a fixed height (Invite Retailer is 590pt, expandable to large). Use `ModalBottomSheet` with `containerColor = Color.White`, a 12–16dp top corner radius matching the reference, the drag handle, and `skipPartiallyExpanded` where iOS only uses `.large`. The "Done" / "Close" buttons sit where iOS puts them.

### 4.4 Global look
- Screen background **white** on Home (`.background(Color.white)`) and `Palette.background` (`#FEFEFE`) elsewhere, as each iOS view sets.
- Light status bar with dark icons. No dark mode.
- Pull to refresh on every screen that has `.refreshable` / `.refreshTask` in iOS.

---

## 5. Screen-by-screen spec (wholesaler)

For **each** screen: (a) read the iOS file end to end, **its current `body`, not the doc comment at the top** (some doc comments are stale; e.g. Home's comment mentions a category grid, but the current body is hero → error banner → low-balance banner → Daily Credits card → Insights → Latest Creations); (b) open the reference PNGs; (c) rebuild the Android screen to match; (d) capture the Android preview and the side-by-side; (e) tick the screen's checklist in `PORT_STATUS.md`.

### 5.1 Home: `iOS: Features/Wholesaler/WholesalerHomeView.swift` · refs `wholesaler`, `upload-card`, `chamak-card`, `home-creations`, `home-lowbalance`, `report-teaser`
Order, exactly as the iOS `body`:
1. **Hero** (`hero`, ~line 164): "Welcome" eyebrow (muted, small) then the wholesaler's display name (large, Cirka), then `UploadDesignCard` (~395): cream card with the hands/coin/wash artwork, "Upload New Jewellery" title in two lines ("Upload New" / "Jewellery") in Cirka, "Reimagine Your Collection" subtitle, and a dark capsule "Upload Now" button with the upload glyph. It opens `AddProductSheet` and reloads Home when the sheet closes.
2. **Error banner** (when the load fails), with iOS copy and colours.
3. **Low-balance banner** (when `wallet.lowBalance` and not dismissed): amber `#FFFBEB` fill, `#FDE68A` 1pt border, 10pt radius. Warning icon in `statusPending`, "Running low on credits" in Manrope 13 bold, "<n> credits left, about <k> more fusion(s)." in Manrope 12 `#92400E`, a dark "Buy" capsule (opens the Daily Credits sheet) and an × to dismiss.
4. **`TreasureChestCard`**: the "DAILY CREDITS" card with the sparkle, "Details" outline button and "Credits available" (see `wholesaler.png`).
5. **Insights** (~186): "Insights" title in Cirka, then the dark **weekly report teaser** ("YOUR WEEKLY REPORT" / "Likes, orders and store reach — swipe through your week." with a chevron, which opens the report sheet), then the **4 `StatCard`s** in a row (icon top-left, big number, label; products, pending orders with `PingDot` when > 0, unread chats, uploads today / usage), then `ManufacturingOffersCard`, then `InviteRetailerCard`.
6. **Latest Creations** (`LatestCreationsSection`, ~598): title + "Your newest Chamak designs and sets.", a "Chamak / Sets" segmented control, a 2-column grid of the newest generations (signed URLs, protected), and "View All →" switching to the Chamak tab.
- Data: `HomeModel.load(session:)` (~266–290). It loads the wholesaler row by email then user ID, marks the dashboard visited, then runs the four counts with **`user.id`** (§1.1) in parallel. Every count failure becomes 0 (iOS `try?`). Then `credits.refresh()`.

### 5.2 Catalogue: `iOS: Features/Wholesaler/WholesalerCatalogueView.swift` · ref `catalogue-cards`
- Large title "Catalogue". Category chips row (`CategoryChip`), search field, then a **2-column grid** of `CatalogueProductCard`: square photo (protected) with the category label overlaid in white at the top-centre, a ••• button top-right (menu: edit / publish / stock / re-upload / delete as iOS), title (1 line, ellipsis), "<Material> · Net <x> g" in muted, a purity chip ("22K") on the right with a cream border, and a stock line ("In Stock" green check, or "Made to order · <n> days" amber clock). White card, 16pt radius, soft shadow as in the PNG.
- "+" in the toolbar opens Add Product. The export action (folder export) sits where iOS has it.
- Data: `fetchCatalogue(wholesalerUserId, category, page)`, 20 per page, infinite scroll, pull to refresh, empty state copy from iOS.

### 5.3 Product detail sheet: `iOS: Features/Wholesaler/ProductDetailSheet.swift` · ref `productdetail`
- Image gallery pager (protected), title, spec rows, publish and stock toggles (optimistic + retry), edit, delete (with `deleteProductImages`), and **Re-upload to AI** with the overlay and status lines (already ported; restyle to the reference).

### 5.4 Add product: `iOS: Features/Wholesaler/AddProductView.swift`, `AddProductForm.swift` · refs `addproduct`, `addproduct-sheet`
- Three numbered sections (`SectionHeader` 1/2/3: image, essential details, specifications), the daily upload-limit banner, the iOS form controls (`JewelSelect`, `InputWithSuffix`, `JewelToggle`, `FieldPair` from `FormComponents.swift`, **not** `OutlinedTextField`), the image-count choice with credit cost, and the bottom note "*By submitting, you allow us to display your product details and images to retailers on the platform."
- Upload History clock button top-right. Submit → `processProduct` (idempotency key) → success view, or the insufficient-credits sheet on 402.

### 5.5 Edit product: `iOS: Features/Wholesaler/EditProductView.swift`
Restyle the existing `EditProductScreen.kt` with the iOS form controls. Field order and labels exactly as iOS.

### 5.6 Upload history: `iOS: Features/Wholesaler/UploadHistoryView.swift`
Today's uploads with AI status chips. Empty state: "Products you upload today will show up here."

### 5.7 Orders: `iOS: Features/Wholesaler/WholesalerOrdersView.swift` · refs `wholesaler-orders`, `order-detail-new`, `order-detail-active`, `reject-order`
- Centred inline title "Orders". An iOS-style **segmented control** (light grey track, white selected segment with a subtle shadow) with **New Orders · Active Orders · Completed · Rejected**. The list of `WholesalerOrderCard`s with `OrderDesignThumb` and `OrderStatusBadge`. Detail with `OrderTimeline`. Status actions through `order_set_status`. `RejectOrderSheet` with the reason chips (`FlowRow`) and details field. Error state: red triangle icon, message, and a cream "Retry" capsule (exactly as `wholesaler-orders.png`). Empty state with the Invite Retailer CTA.
- The orders store pre-loads when the shell opens, as iOS does.

### 5.8 Chat: `iOS: Features/Chat/ChatThreadsView.swift`, `ChatThreadView.swift` · refs `chats-wholesaler`, `chat-thread`
Title "Queries & Chat" (inline). Empty: "No Active Queries" / "When stores ask about your designs, their questions will appear here." Thread rows with product thumb, store name, last message, `ChatTime`, unread dot. Thread: bubbles (wholesaler on the right), input bar, report sheet.

### 5.9 Invite Retailer: `iOS: Features/Wholesaler/InviteRetailerSheet.swift` · refs `invite-card`, `invite-gift`, `invite-guide`
Reward banner artwork, gift stepper with `InvitePlus`/`InviteMinus`, code box with `InviteCopy` / `InviteChecked`, "Invite retailer now" share button, history (`InvitationHistoryView`). Use the Figma-matched spacing from the iOS file.

### 5.10 Profile: `iOS: Features/Profile/ProfileViews.swift` (`WholesalerProfileView`) · ref `profile-wholesaler`
iOS **inset-grouped list** look (see the PNG): a light grey page background (`#F2F2F7`-ish, sample it), centred inline title "Profile", a centred `ProfileHeader` (round avatar with the business initials or logo, business name in Cirka, owner name in muted Manrope, a green "Verified" chip), then white rounded sections with hairline separators: **Account** (Email, Location, Member since; label left in muted, value right), a shortcuts section (Treasure Chest with chevron, Invite a Retailer), the legal section (Terms & Conditions, Privacy Policy, …), Log Out, Delete account. Row icons are SF Symbols. Map them to the closest Material icons (plan §A2.3).

### 5.11 Treasure Chest / Daily credits: `iOS: Features/Wholesaler/TreasureChest/*` · refs `daily-credits-sheet`, `daily-credits`, `nocredits`
`TreasureChestView`, `TreasureChestCard`, `DailyCreditAllowanceView` (from `TopUpSheet.swift` lines 1–190), `TransactionHistoryView`, `InsufficientCreditsSheet`, `CreditBalancePill`.

### 5.12 Report: `iOS: Features/Wholesaler/Report/WholesalerReport.swift` · refs `report`, `report-teaser`
7/30-day toggle, swipeable report cards, each metric vs the previous period.

### 5.13 Manufacturing offers: `iOS: Features/Manufacturing/WholesalerOffersListView.swift`, `WholesalerOfferDetailView.swift`, `AcceptQuoteSheet.swift`, `DeclineOfferSheet.swift`, `ManufacturingOffersCard.swift` · ref `manufacturing-offer`

### 5.14 Chamak (wholesaler tab): `iOS: Features/Wholesaler/Chamak/*` · refs `chamak-hub`, `chamak-hub-empty`, `chamak-picker`, `chamak-picker-sample`, `chamak-slider-retry`, `chamak-result`, `chamak-result-attempts`, `chamak-viewer`, `setcreation-picker`, `setcreation-styling`
Restyle each of the 19 Android Chamak files to its reference. The hub (see `chamak-hub.png`) has the large title **"Chamak Studio"**, then two big rounded cards: **"Chamak Studio"** (Cirka title, "Reimagine your jewellery. Create something new.", a grey "Get started →" capsule, product photo on the right) and **"Set Creation"** ("Stage 2–4 pieces together. Choose one item from each jewellery type.", "Create a Set →", necklace photo on the right). Below them: **"Your Gallery"** (Cirka) with "<n> creations" on the right, the subtitle "Everything you make stays private here. Nothing is published to your catalogue.", and a 2-column grid of creations with the category label on the photo and the two source thumbnails stacked bottom-right. Pay particular attention to the hub tool cards (`ChamakToolCard`, `ChamakSetCreationCard` with the studio artwork), the gallery cards (square photo, two lines, stacked source thumbnails in the corner), and the generating animation (gem shapes, colours, timing from `ChamakGeneratingView.swift` and `Motion.swift`).

---

## 6. Order of work and checkpoints

| Step | Work | Checkpoint for the founder |
|---|---|---|
| A | §1.1 + §1.2 (ID fix, crash fix, ViewModels for Home/Catalogue/Orders/Chamak) | Install on the phone: Home counts, Catalogue products/images, Orders and Chamak gallery match iOS. No crashes in airplane mode. |
| B | §3.2 Android preview harness + comparison script | `docs/android-parity/wholesaler/*-compare.png` exist for every §3.1 id (showing the gap honestly). |
| C | §4 shell and chrome | `wholesaler-compare.png`: tab bar, top-right capsule and large title match. |
| D | §5.1 Home, §5.2 Catalogue, §5.3 Product detail | Comparisons match. |
| E | §5.4–5.7 Add/Edit/Upload history/Orders | Comparisons match. |
| F | §5.8–5.13 Chat, Invite, Profile, Credits, Report, Manufacturing | Comparisons match. |
| G | §5.14 Chamak | Comparisons match. |
| H | §1.3 data parity table + final sweep (§2.3 invented copy, §2.4 colours, §2.2 Material leftovers) | All greps clean. |

After each step: build, run the unit tests, install on the phone (`adb -s <phone-serial> install -r app/build/outputs/apk/debug/app-debug.apk`; `adb devices` shows the serial), commit, and update `PORT_STATUS.md`.

---

## 7. Definition of done (wholesaler)

All of these must be true:
- [ ] Signed in on the phone with the founder's wholesaler account: Home counts, catalogue products **with images**, orders, chats, Chamak creations and credit balance match iOS for the same account.
- [ ] No crash walking through every wholesaler screen online **and** in airplane mode. `adb logcat -b crash` is empty.
- [ ] No API calls from composables in `feature/wholesaler/**` (ViewModels only).
- [ ] `grep -rn "wholesaler?.id\|wholesaler.id" app/src/main/java/com/jewelindia/app/feature/wholesaler` finds only the invitation-code usage (with a comment explaining why).
- [ ] Every §3.1 reference id has `docs/android-parity/wholesaler/<id>.png` and `<id>-compare.png`, and you have looked at each one and fixed the differences.
- [ ] The wholesaler UI uses all the iOS artwork listed in §2.1, uses no Material `Card`/`OutlinedTextField`/`NavigationBar`, has no invented strings (§2.3 sweep clean), and has no Tailwind hex greys that iOS doesn't use (§2.4).
- [ ] `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug` passes. The new tests are `WholesalerIdentityTest`, `ChamakLoadFailureTest` (gallery failure doesn't throw), and a ViewModel test per wholesaler screen for loading → content / empty / error.
- [ ] `PORT_STATUS.md` has a "Wholesaler parity" section: each screen → reference ids → ✅/🟡 + what's still different. `OPEN_QUESTIONS.md` lists anything you couldn't match and why.

When you report back, include: the commit list, the test count, the data numbers you saw on the phone vs iOS (counts only), and the paths of the comparison images, the five most important ones first: Home, Catalogue, Orders, Chamak hub, Product detail.
