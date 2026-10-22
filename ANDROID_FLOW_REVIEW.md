# Android flow and endpoint review

18 October 2026 · Current Android working tree · Read-only source review

This report records the original review findings. Subsequent Android fixes and their validation are tracked in [Android fix progress](ANDROID_FIX_PROGRESS.md).

**What this repo does:** The Android app connects wholesaler, retailer, and staff screens to the website API, AI pipeline, Supabase authentication, database functions, and storage. I traced the normal mobile flows, including what happens when requests fail or return late.

**Conclusion: the feedback is supported.** There are concrete Android-side blockers, actions that do nothing, and failures presented as successful or empty results. This does **not** establish that the deployed endpoints are down. Several flows fail before an endpoint is contacted.

**As requested, no tests, builds, emulator interactions, live API calls, or application-code changes were performed for this review.** Only Android project files were inspected. Backend repositories, production logs, credentials, and deployed database policies were not inspected.

All code paths below are relative to `app/src/main/java/com/jewelindia/app/`, except where stated otherwise. [The endpoint inventory](ANDROID_ENDPOINT_INVENTORY.md) lists the literal routes, database function names, and resource names found in Android source. It records references, not live availability.

## Must fix: defects visible in Android source

### 1. AI consent is enforced but never initialized or granted

- **What this is:** Product processing, reprocessing, Chamak analysis, and generation check whether the user agreed to AI processing.
- **Problem:** `AIConsent` starts with `prefs = null`; `isGranted()` consequently returns false. There are no Android calls to `AIConsent.initialize()` or `AIConsent.grant()`, and no consent prompt wired to these flows. The guards throw before the corresponding pipeline requests are built/sent.
- **Affected calls:** `/process`, product reprocessing, `/api/chamak/analyze`, `/api/chamak/generate-v2`, and `/api/set-creation/generate`. Chamak may already have uploaded source photos or inserted a generation before reaching the failing guard.
- **Fix / if skipped:** Wire actual user consent and initialization into the flow; keep the guard. Until then these features remain blocked even if their servers are healthy.

Evidence: `core/consent/AIConsent.kt:21,23,34,38,46`; `JewelApplication.kt:14`; `data/api/WholesalerApi.kt:340,420`; `data/api/ChamakApi.kt:184,272,341`. Also use the same account key throughout: analysis currently calls `requireGranted()` without the user ID, while generation supplies one.

### 2. Staff-management requests lose their serialization type

- **What this is:** Suggesting usernames, creating staff logins, inviting Google users, resetting passwords, changing status, and removing staff call the `staff-accounts` function.
- **Problem:** `StaffAccountsApi` passes a typed `Request` into a helper accepting `body: Any?`. The helper then calls `JewelJson.encodeToString(body)` using the erased `Any` type. Its JSON configuration registers no serializer for `Any`, so this path cannot encode the staff request normally and can fail before HTTP execution.
- **Fix:** Serialize the concrete request before passing it to the helper, or preserve its serializer/type through the helper.
- **If skipped:** Multiple staff-management actions remain broken together. Credits and onboarding fee calls use the helper's explicit `JsonObject` branch and do not have this particular defect.

Evidence: `data/api/StaffAccountsApi.kt:42,69`; `data/net/EdgeFunctions.kt:20,27,31`; `data/supabase/Supabase.kt` (`JewelJson` configuration).

### 3. Saving a product draft reports success without saving anything

- **What this is:** Add Product offers a draft submission path.
- **Problem:** For `publish = false`, `submit()` sets the state to Done and calls `onSuccess(true)`. There is no database write, endpoint call, or local draft persistence in that branch.
- **Fix:** Persist the draft and use the saved result before showing success.
- **If skipped:** The user is told the draft was saved, but it is absent after leaving the form.

Evidence: `feature/wholesaler/AddProductForm.kt:147–151`; caller `feature/wholesaler/AddProductScreen.kt:230–242`.

### 4. Publishing a product drops most entered product details

- **What this is:** The form collects and validates category, style, size, purity, gross/stone/net weight, stock availability, and production time.
- **Problem:** The publish call only forwards the image, title, jewellery type, owner ID, image count, and submission key. The `/process` multipart body contains none of the other entered fields, and the caller makes no later metadata update. The returned product ID is discarded.
- **Fix:** Persist the entered metadata for the returned product, or extend the agreed processing contract to accept it.
- **If skipped:** Even after fixing AI consent, publishing cannot preserve those user-entered values. Server defaults cannot reconstruct values Android never sends.

Evidence: `feature/wholesaler/AddProductForm.kt:78–100,129–142`; `data/api/WholesalerApi.kt:330–355`. Contrast the existing complete edit payload in `feature/wholesaler/EditProductScreen.kt:145–164`.

### 5. Opening a wishlist board asks for an empty customer ID

- **What this is:** A customer's board opens `YourTasteScreen` with a board ID, and that screen loads its saved product IDs.
- **Problem:** It calls `WishlistApi.fetchBoards("")`. That API filters `customer_boards` by `customer_id = ""`, rather than loading the selected board/customer. An ordinary ID column will reject the empty value or match no customer; the exception is swallowed.
- **Fix:** Pass the owning customer ID or fetch board items by the supplied board ID.
- **If skipped:** Existing saved items appear unselected. A tap can attempt to save an already-saved item instead of removing it.

Evidence: `feature/retailer/YourTasteScreen.kt:110–123`; `data/api/WishlistApi.kt:133–137`; board navigation in `navigation/RetailerShell.kt`.

### 6. Several result cards have no destination

- **What this is:** Users tap search results, dashboard products, or staff design cards expecting a detail screen.
- **Problem:** The retailer shell supplies empty callbacks for visual search and dashboard products. The staff shell supplies an empty callback for its design list. The child screens invoke those callbacks, but nothing opens.
- **Fix:** Connect each callback to the existing relevant detail flow.
- **If skipped:** Data may have loaded successfully, but tapping it looks like a broken app or endpoint.

Evidence: `navigation/RetailerShell.kt:290,492`; `navigation/EmployeeShell.kt:228`; callback invocation in `feature/retailer/CatalogueImageSearch.kt:474`, `feature/retailer/RetailerDashboardScreen.kt`, and `feature/employee/EmployeeDesignsScreen.kt`.

### 7. Visual search hides server and decoding failures as no matches

- **What this is:** The visible visual-search flow calls `CatalogueImageSearchApi.searchByImage()` and hydrates returned product IDs.
- **Problem:** Non-success HTTP responses return an empty list. A decoding failure becomes an empty response, and hydration failures also return an empty list. Authentication failures, server errors, changed response formats, and genuine zero matches can therefore look identical.
- **Fix:** Propagate request/decoding failures to the screen's existing error display; reserve an empty list for a successful search with no matches.
- **If skipped:** Users cannot tell whether their photo had no matches or search is actually unavailable.

Evidence: `feature/retailer/CatalogueImageSearch.kt:199–230,257–272`. This is failure handling visible in source, not a claim that the live service currently returns an error.

### 8. Incoming chat messages do not update an open conversation

- **What this is:** The thread screen loads messages when opened and after the local user sends a message.
- **Problem:** There is no realtime subscription, polling loop, or resume refresh for the open thread. Messages sent by the other party do not trigger another fetch. Message-loading exceptions are also swallowed.
- **Fix:** Connect an update mechanism and expose loading failures instead of silently showing an empty/stale conversation.
- **If skipped:** A conversation appears unresponsive until it is reopened or the user sends another message, even if `chat_send` and the messages query work.

Evidence: `feature/chat/ChatThreadScreen.kt:89–117,132–133`; `data/api/ChatApi.kt:64–75`. The SDK configuration does not install a realtime plugin, and no alternate chat update listener is wired in Android.

### 9. Order screens do not reliably communicate unsuccessful actions

- **What this is:** Retailers/staff create orders; wholesalers accept, advance, or reject them.
- **Problem:** Create-order callers discard `CreateOrderResponse.success` and display success whenever HTTP/decoding did not throw. A response represented by the model with `success = false` therefore still leads to a sent confirmation. On the wholesaler side, both status-update and rejection callers pass `onError = {}`, discarding the error that the API/ViewModel already supplies.
- **Fix:** Validate the returned business result before confirming an order, and connect the existing error callbacks to visible feedback.
- **If skipped:** Users can see false success or tap an order action with no explanation when it fails.

Evidence: `data/api/JewelApiModels.kt:198–202`; `feature/retailer/RetailerOrderRequestSheet.kt:254–260`; `feature/employee/EmployeeReviewScreen.kt:167–173`; `feature/wholesaler/WholesalerOrdersScreen.kt:283,292`; `data/api/OrdersApi.kt:19–37`.

Whether the deployed create endpoint uses HTTP 200 for business failures requires its contract or response logs; the Android response model supports that case and its callers ignore it.

### 10. Referral cancellation discards the endpoint's result

- **What this is:** Invitation History lets the wholesaler cancel a referral link.
- **Problem:** `cancelInvitation()` executes DELETE `/api/referral/manage` without checking the HTTP status or body. It also swallows exceptions. The screen reloads afterward without showing cancellation errors.
- **Fix:** Check the response and surface unsuccessful cancellation through the screen.
- **If skipped:** A rejected or unreachable cancellation looks like a tap that did nothing; the link may remain active.

Evidence: `data/api/JewelApi.kt:198–212`; `feature/wholesaler/InvitationHistoryScreen.kt:124–132`.

Related concern: referral-generation errors trigger a direct database fallback. Its insert contains no gift-credit amount, yet the returned object displays the requested gift amount. Consequently a displayed gift is not proof that the requested credit transfer was recorded. That accounting contract needs backend evidence before treating the fallback as equivalent to the normal API.

### 11. Invitation links are not reliably routed into signup

- **What this is:** Wholesalers share links and retailers open them to begin signup.
- **Problem:** Android-generated fallback links use `https://jewelindia.shop/join/...`, while the manifest registers `https://app.jewelindia.shop/join/...`. A website redirect could bridge the hosts, but Android alone does not show that. Separately, even a delivered invite is only captured when the session phase is already Unauthenticated; if it arrives during Loading, there is no deferred capture/replay.
- **Fix:** Use an agreed app-link host consistently and retain/reprocess invite intents after session initialization.
- **If skipped:** Opening an invite can land in the browser or open the app without its invitation context, depending on redirect and startup timing.

Evidence: `data/api/JewelApi.kt:249,323` and its other fallback link construction; `app/src/main/AndroidManifest.xml` join intent filter; `MainActivity.kt:103–107`; `core/session/SessionStore.kt` initial Loading phase.

## Should fix: additional data-flow risks

### 12. Board loading expects nested items that its query does not request

- **What this is:** `BoardRow` expects `customer_board_items`, then builds board products from those items.
- **Problem:** `fetchBoards()` performs a plain board select with no embedded relationship selection or separate item query. With a normal PostgREST table response, the nested field is absent and defaults to `emptyList()`, making populated boards look empty.
- **Fix:** Explicitly select the relationship or load board items separately.
- **If skipped:** Correcting the empty customer ID alone may still leave board product lists/counts empty.

Evidence: `data/api/WishlistApi.kt:121–152`. **Contract-dependent:** a custom server-side column/view supplying this nested JSON could change the outcome; no schema/backend inspection was done.

### 13. An older search response can overwrite a newer search

- **What this is:** Marketplace search debounces typed text before requesting filtered products.
- **Problem:** The debounced job calls `loadFiltered()`, which launches a separate coroutine in the store scope. Cancelling the debounce job does not cancel an already-started request. There is no request-version check before applying results.
- **Fix:** Keep the request in the cancellable search job or reject responses for an older query/category.
- **If skipped:** On a slow connection, results for an earlier word/category can replace the current search results.

Evidence: `feature/retailer/MarketplaceCatalogueStore.kt:143–181`.

### 14. Store theme selection looks saved when the server update failed

- **What this is:** Retailers choose or unlock a showroom theme.
- **Problem:** The screen changes the selection and local preferences before the server update. `setSelectedTheme()` returns false on failure, but the callers ignore that result; there is no rollback/error. Unlocking and selecting are separate operations, so a successful unlock is also not proof that selection saved.
- **Fix:** Handle the returned result, retain valid unlock ownership, and show/retry a failed selection instead of silently treating it as saved.
- **If skipped:** The theme can appear selected locally while other devices or a later reload show the old theme.

Evidence: `feature/retailer/StoreThemeScreen.kt:224–229,249–258`; `data/api/RetailerApi.kt:174–187`.

### 15. Retrying a partly completed staff batch can duplicate orders

- **What this is:** Staff can select more than 20 products; submission sends groups of up to 20.
- **Problem:** If the first group succeeds and a later group fails, all selections remain. A retry sends the successful first group again. The order API call supplies no idempotency key or client-side record of completed groups.
- **Fix:** Preserve completed progress or use an agreed idempotent batch contract before retrying.
- **If skipped:** A partial failure can leave some orders created while the screen shows failure, and a retry can create duplicates unless the backend independently deduplicates them.

Evidence: `feature/employee/EmployeeReviewScreen.kt:167–175`; `data/api/JewelApi.kt:428–442`. Backend duplicate prevention was not inspected.

## Flow coverage and endpoint status

“Connected” below means Android has a call path. It does not mean a live request succeeded.

| Android flow | Endpoint / dependency family | Source-review result |
| --- | --- | --- |
| Email/password, OTP, Google login, password recovery | Site `/api/auth/*`, Supabase Auth, `set_my_role`, auth callback | Connected. OTP token-import errors are swallowed, so an OTP HTTP success alone is not proof of a usable local session. Live contracts not verified. |
| Role selection and staff account claim | `validate_referral_code`, `claim_staff_invite`, profile/gate queries | Connected. Invite delivery has finding 11; staff administration has finding 2. |
| Wholesaler/retailer onboarding | Five document/logo storage buckets; `wholesalers` / `retailers` upserts | Connected. Document/storage policies and schema are not verified. Wholesaler fee-status errors fall through to normal submission instead of displaying the fee-check failure. |
| Onboarding payment | `credits-topup`: `onboarding_status`, `onboarding_pay`, `onboarding_confirm` | Status, link creation, browser return, and confirmation are wired. Payment/provider/webhook health is unknown. |
| Wholesaler products and catalogue | Product CRUD, `/process`, `/api/upload-usage`, `/product/{id}`, reprocess | Blocked processing and false draft success: findings 1, 3, 4. Usage GET sends no bearer token; whether that endpoint requires one is a backend question. |
| Retailer catalogue and marketplace | `/api/retailer/marketplace*`, `/api/retailer/your-taste`, retailer-design storage/DB | Connected. Search response race: finding 13. |
| Image search / dashboard product taps | Pipeline visual search plus marketplace hydration | Failure/selection handling defects: findings 6, 7. |
| Customer wishlists and boards | `retailer_customers`, `customer_boards`, `customer_board_items` | Findings 5 and 12. |
| Catalogue and wishlist share links | Site `/api/catalogue-shares*`, `/api/wishlist-shares*` | Create/list/revoke are connected. Deployed authorization and reply shapes are unverified. |
| Retailer/staff order creation | Site `/api/orders/create` | Findings 9 and 15. |
| Wholesaler order workflow / retailer order list | `order_set_status`, `wholesaler_orders`, `/api/retailer/orders` | Connected; wholesaler action errors are hidden: finding 9. |
| Chat, read state, reports | `chat_*` RPCs and `messages` queries | Read/send/report connected; incoming refresh defect: finding 8. Block/unblock API methods exist but no corresponding chat-screen controls are wired. |
| Staff showroom/design list | Employee/store resolution, product/design queries | Connected data loading; design navigation is disconnected: finding 6. |
| Referrals | Site `/api/referral/*`, `referral_links`, validation/attachment RPCs | Findings 10 and 11; fallback credit accounting needs backend verification. |
| Chamak fusion / set creation / regeneration | Generation DB/storage, pipeline analyze/generate/set endpoints | Consent blocker: finding 1. Polling and result-signing paths exist, but do not establish live service availability. |
| Manufacturing requests and quotes | Pipeline `/api/retailer/manufacturing-*`, `/api/wholesaler/manufacturing-offers*` | Create/upload/list/detail/cancel/award/accept/decline are connected. Android sends bearer auth and preserves a submission key for request retries; server contracts are unverified. |
| Wallet, history, entitlements | `credits_wallet`, `credits_history`, entitlement RPCs / DB | Connected. Several refresh errors are swallowed or retain previous values; that is not evidence of a healthy endpoint. |
| Membership plans and credit top-ups | Plan RPCs, `credits-topup` options/create/purchase helpers | **Not available as a complete Android purchase flow.** PlansScreen explicitly says subscription sales/renewals are paused. Top-up creation helpers have no UI callers, and Treasure Chest displays balance/history/rates without a purchase flow. This is missing/paused wiring, not a verified server outage. |
| Store themes | `purchase_entitlement`, selected-theme read/write | Finding 14. |
| Reports, profile, account deletion | `wholesaler_report`, profile queries, `delete-account` function | Connected. Live permissions/replies not verified. |
| Device/activity tracking | `register_device`, `store_events`; `/api/devices/register` helper | Device/activity logging is wired with best-effort error handling. The manufacturing token-registration helper has no Android caller; push delivery was not verified. |

## What this review can and cannot confirm

The Android files confirm broken call wiring and failure handling. They cannot confirm deployed route existence, current HTTP status, authentication acceptance, row-level security, storage policies, database functions, response formats, or payment fulfillment. No endpoint is labelled “working live” in this report.

**Verdict:** Fix AI consent wiring and staff request encoding first, then draft persistence, product metadata, board loading, and disconnected navigation. A clean lint/build result does not validate these business flows.

**Not checked:** Tests, builds, live requests, emulator/device behavior, backend source, production logs, deployed schemas/policies, and payment actions. No application code was changed in this review.
