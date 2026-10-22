# Android catalogue, Chamak Fusion, and Set Creation implementation plan

Status: Android implementation completed for the joint repair batch. The final debug APK build and debug/release lint checks passed with 0 errors and 0 warnings; runtime acceptance remains unverified. The source investigation and planning passes ran no tests, builds, emulator interaction, or live API calls. See [implementation and verification progress](ANDROID_FIX_PROGRESS.md) for the later code changes and their exact verification status.

## Scope and priority

Fix the newly reported Android usability and output problems before continuing the older flow backlog. All edits stay in the Android project. Any required AI pipeline, database policy, website, or other project change must be described concretely and approved by the user first. A successful build or clean lint report does not establish runtime correctness.

## Joint scope: Fusion and Set Creation

Treat both creation modes as one repair effort. Set Creation already uses the affected shared picker/model/coordinator and launches generation from the same screen-owned coroutine pattern. Its exposure is confirmed in source; the exact device symptoms have not been reproduced. Do not complete a shared fix for Fusion while leaving the equivalent Set Creation path unchanged.

| Area | Fusion coverage | Set Creation coverage |
| --- | --- | --- |
| Entry and selection | Two distinct designs; catalogue and local images | Two to four pieces; supported distinct types; catalogue and local images |
| Initial transition | Selection → analysis → customization | Selection → generation row creation → backdrop/styling |
| Submission | Customization → generation | Backdrop, notes, and pieces → generation |
| Layout | Picker, customization, progress, results, gallery | Picker, type chooser, backdrop/styling, progress, results, gallery |
| Recovery | Pending job, completed output, retry/revision, existing gallery entry | Same recovery, preserving set mode, pieces, and backdrop |
| Account/entry point | Wholesaler and existing retailer Fusion entry | Existing wholesaler Set Creation entry; do not add new role access |

Maintain the intentional difference: Fusion performs the analysis stage; Set Creation proceeds to styling after source upload/row creation. Sharing lifecycle and image handling must not route Set Creation through Fusion analysis or change its request payload.

## How we will proceed

The following batches are the execution order; the numbered sections below provide the detailed requirements.

1. **Shared request lifetime (section 1).** Define the flow ViewModel owner, then move both modes' work into that owner. Cover initial upload/row creation, generation, polling, cancellation, retry keys, and return-to-flow recovery together. Do not use a global coroutine scope or suppress cancellation to keep work alive.
2. **Shared screen layout (section 2).** Establish one inset/content-padding contract between the shell and active flow. Apply it to both modes, including Set Creation's type chooser and styling controls. Preserve scrollable content and reachable actions with the keyboard open.
3. **Images and selection (image resolution in section 3 plus section 4).** Resolve verified/generated images consistently in catalogue display and both pickers. Keep full source images for processing and derivatives for display. Preserve explicit selection rules and show reasons when continuation is blocked.
4. **Submission-to-output recovery (remaining section 3 plus section 5).** Repair catalogue completion events and pending-product refresh. Reconcile Fusion and Set Creation generation status and image access; refresh both galleries and retry failed retrieval without resubmitting a completed generation.
5. **Consolidation and handoff.** Review all matrix paths in source, compile/lint the combined Android changes, update the progress report, and produce one APK containing the complete repair batch. Mark runtime acceptance as unverified until device checks are authorized or results are supplied.

After each batch, record implementation status separately from verification status. Keep a short list of unresolved dependencies instead of treating a clean build as proof that either mode works. The planning turn updated this document only. The user subsequently authorized implementation of the full Android repair batch.

### State and retry rules for both modes

- Selecting, uploading, analyzing (Fusion only), styling/customizing, submitting, processing, completed, and recoverable retrieval failure must remain distinguishable.
- Preserve the account, mode, accepted generation ID, and pending submission key across relevant lifecycle events. Restore server state by ID; do not persist large image byte arrays in saved instance state. Handle unavailable local selections with a clear reselect action.
- A server-accepted generation must be resumed, not recreated, when navigating back or returning to the app. If acceptance is uncertain, reconcile using existing server contracts before permitting a retry that could duplicate work or charges.
- Disable duplicate submissions while work is active. An explicit new generation/revision and a retry of the same uncertain submission must not share the wrong idempotency key.
- Separate generation failure from status-fetch failure, image-signing failure, and image-download failure. Preserve completed output and server status during local retrieval problems.
- Keep AI consent and authentication checks in both modes. Any missing backend capability needed for safe reconciliation becomes a concrete approval request before an external-project edit.

## 1. Stop Chamak cancelling its own requests — P0

**Confirmed source cause:** `ChamakCatalogPickerScreen`, `ChamakSliderFormScreen`, and `ChamakSetStylingScreen` launch submission from `rememberCoroutineScope`. Their model methods immediately switch `step` to Analyzing/Generating. `ChamakFlowCoordinator` replaces the originating screen, cancelling that screen's coroutine. Broad exception handlers then put the cancellation message into the Failed screen. This explains the reported “rememberCoroutineScope left the composition” message. The current `ChamakViewModel` is a plain class with a separate unmanaged scope, despite its name.

**Implementation:**
- Give the flow a lifecycle-owned Android ViewModel and run analysis/submission/polling in its `viewModelScope`; step screens send actions rather than own long-running jobs. Update hub, coordinator, previews, and debug fixtures together.
- Rethrow `CancellationException` before handling actual failures in submission, polling, and affected API wrappers. Cancellation must not mark a server generation failed or show an internal exception to the user.
- Keep one submission and one polling job per generation. Preserve idempotency keys across retries.
- Retain the generation ID and resume status retrieval when returning to the flow. Leaving the UI does not imply cancelling a job already accepted by the server. Stop local polling when the owning flow is disposed, then reconcile on return.
- Show long-running or temporarily unreachable jobs as pending/recoverable, rather than declaring pipeline failure solely because a polling timeout elapsed.

**Acceptance:** Switching from selection/form to progress does not cancel the request. Back/return or rotation does not create a duplicate generation. A real API failure has an actionable message; normal cancellation never becomes “Generation incomplete.”

Primary files: `feature/wholesaler/chamak/ChamakViewModel.kt`, `ChamakViewModelOperations.kt`, `ChamakFlowCoordinator.kt`, `ChamakHubScreen.kt`, the three action screens, and `data/api/ChamakApi.kt`.

## 2. Make controls reachable and respect Android system areas — P0

**Confirmed source cause:** `WholesalerShell` draws full-size tab content and then overlays its top capsule and bottom tab bar. Chamak receives no reserved space for those controls. The bar's own navigation padding protects the bar, not the content underneath it. Picker headers and bottom actions do not handle system insets. `ChamakHubScreen` also drops its incoming modifier when switching to the active coordinator.

**Implementation:**
- Use a measured shell layout, preferably Material 3 `Scaffold`, that reserves top and bottom bar space and applies its content padding. Propagate and consume insets once, avoiding doubled padding in child screens.
- Keep the Studio hub inside tab navigation; present an active creation flow without the global floating capsule/tab bar competing with its own header and action bar. Give it an explicit Back action and Android system-back handling.
- Place each primary action in the flow's bottom action area above system navigation and the keyboard. Keep the body scrollable in the remaining space.
- Respect status bars, display cutouts, horizontal insets, and gesture/three-button navigation. Use inset APIs, not fixed guesses for status-bar height.
- Reduce duplicated headers and large fixed selection panels; let shorter screens scroll. Forward the hub modifier to both branches.
- Audit related Android screens for this same overlay pattern. Use at least 48dp interactive targets, readable labels, selected-state semantics, and logical focus order. Check the retailer Chamak entry separately because it has a different host.

**Acceptance:** No header overlaps the clock, battery, or cutout. Start Analyze, Choose Backdrop, and Generate remain visible and tappable. The content does not sit behind app navigation, and enlarging text or opening the keyboard does not trap controls.

Primary files: `feature/wholesaler/WholesalerShell.kt`, all Chamak screen/layout files, and `navigation/RetailerShell.kt`.

Guidance: [Android window inset setup](https://developer.android.com/develop/ui/compose/system/insets-ui), [display cutouts](https://developer.android.com/develop/ui/compose/system/cutouts), and [Android touch targets](https://support.google.com/accessibility/android/answer/7101858). Scaffold content must actually apply the supplied padding; adding Scaffold alone is insufficient.

## 3. Show completed catalogue output and refresh after upload — P0

**Confirmed source defects:**
- `core/model/Product.kt` decodes `ai_verified_output_urls` and uses it in `canPublish`, but `displaySource`, `thumbnailUrls`, and `allImages` omit it. A product can be considered publishable while its verified outputs never reach the image UI.
- `AddProductForm.submit` discards the processing response/product ID and immediately reports success. Submission acceptance is not proof that generated images are ready.
- `AddProductScreen` calls `onClose` instead of `onAdded` when both exist. `ProductSheets` supplies both, so the catalogue reload callback is skipped on that path.
- `WholesalerCatalogViewModel` loads snapshots without monitoring pending products. `CachedImage` has no explicit image-error/retry UI.

**Implementation:**
- Establish one product image ordering: explicit nonblank showcase selections, verified generated outputs, other generated/processed outputs, then appropriate original-photo fallback. Filter blank entries and deduplicate. Preserve publication readiness checks.
- Use that resolver consistently in catalogue cards, detail galleries, previews, and Chamak selection. Prefer existing derivative URLs where present; fall back to the original output if a derivative fails.
- Preserve the actual returned product ID. Trigger the completion/refresh callback exactly once, independently of closing the sheet; remove the fabricated `Product(id = "new")` event.
- Represent queued/processing/ready/failed states honestly. Refresh visible pending products with bounded lifecycle-aware polling, and refresh on resume/manual retry. Merge updates by ID without duplicating catalogue entries or resetting selection.
- Distinguish “processing”, “no output”, and “image could not load”. Expose retry for retrieval failures.
- Determine whether each returned image reference is a public URL, private object path, or expiring signed URL. Reuse Android's existing signing APIs where applicable. Keep account authorization; do not make storage public to hide access failures.

**Unverified:** The exact fields, URL validity, storage permissions, and current status of the user's affected product. Source defects are established, but the specific production cause requires a representative record or later authorized runtime inspection. A genuine server contract/storage issue will be reported for approval before any external-project edit.

**Acceptance:** An accepted upload appears as processing, then shows the generated output after completion without reinstalling the app. A verified-output-only product displays correctly in the grid and details. A failed or expired image request produces a useful retry state instead of an unexplained blank tile.

Primary files: `core/model/Product.kt`, `core/images/CachedImage.kt`, `feature/wholesaler/AddProductForm.kt`, `AddProductScreen.kt`, `ProductSheets.kt`, `CatalogSheetHosts.kt`, `WholesalerCatalogViewModel.kt`, `CatalogueProductCard.kt`, and `ProductDetailSheet.kt`.

## 4. Repair Chamak eligibility and selection feedback — P1

**Confirmed source inconsistency:** `ChamakApi.fetchWholesalerProducts`/`fetchStoreProducts`, `ChamakDesignItem.from`, and `selectionBlockReason` use processed/image/raw fields, ignoring generated-only and verified-only outputs. Null-coalescing also allows a blank processed field to hide a valid later field. This can remove usable products from the picker or make selected items lack usable images. Set mode also intentionally rejects duplicate types, missing types, and more than four pieces; those rules must remain visible to users.

**Implementation:**
- Use the common nonblank image resolver for filtering, display, validation, and submitted source URLs.
- Preserve fusion and set rules: two distinct fusion designs; two to four set pieces with distinct supported jewellery types.
- Show selection counts, clear selected states, and a specific reason beside a disabled action or blocked item. Explain missing type/image and duplicate type without silently dropping the tap.
- Surface catalogue-fetch failures separately from an empty catalogue, with Retry.
- Read picked images off the main thread, handle inaccessible files, and retain selections through the flow's screen transitions.

**Acceptance:** Eligible designs can be selected and deselected; valid selections enable continuation. Generated-only products are eligible when otherwise valid. Invalid selections explain exactly what must change.

## 5. Recover and display existing Chamak results — P1

**Inspection required during implementation:** Trace `openGalleryItem`, `signOutputs`, output variants, and current-generation status reconciliation together. Existing signing code is not proof that all deployed image references are usable.

**Implementation:** Re-fetch an opened generation instead of relying solely on a stale gallery item. Resume pending jobs, sign available outputs using existing contracts, refresh the gallery after generation/return, and show explicit output-load errors with retry. Keep completed output visible if a thumbnail or wallet refresh fails. Do not label a generation failed merely because image signing failed.

**Acceptance:** A completed existing generation opens its output; returning from a new generation updates the gallery. Temporary image access errors do not overwrite the server's generation status.

## Delivery and verification

Implement in the batch order under “How we will proceed”, covering both modes in every applicable batch and updating `ANDROID_FIX_PROGRESS.md` with the exact status. These issues take precedence over the older remaining findings 3–15; those findings remain tracked and are not implicitly fixed by this plan.

During implementation, compile/lint checks can establish source/build health. Preserve the user's no-test restriction: no automated tests, emulator sessions, or live requests are scheduled by this plan. Runtime acceptance remains unverified until those checks are authorized or the user supplies device results.

Future device acceptance coverage, when authorized: gesture and three-button navigation, a cutout device, small screen, large font, keyboard open, rotation, back/return, slow network, API failure, pending generation, completed generation, and expired/private image links. Do not certify Android guideline compliance or all flows as working from lint alone.
