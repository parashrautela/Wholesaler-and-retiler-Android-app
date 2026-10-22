# Android flow fix progress

18 October 2026. Changes are restricted to this Android project. AI pipeline, website, iOS, and other project files require the user's confirmation before modification.

## 1. AI consent blocker — implemented

- Initialize consent storage before constructing the application graph.
- Prompt explicitly before publishing, reprocessing, Chamak analysis, and fusion/set generation. Permission is remembered per account; declining or dismissing does not start the action.
- Require an account ID for every consent lookup. Chamak analysis now uses the same account key as uploads and generation; blank accounts cannot be granted consent.
- Guard Chamak source uploads and generation creation before any network mutation, including when the designs already have remote URLs.
- Check consent before clearing existing product images during reprocessing.
- Keep API consent enforcement; nothing automatically grants permission.

The prompt names the providers already listed in Android's consent configuration. The deployed provider configuration and service availability have not been verified.

## 2. Staff request serialization — implemented

- Encode the concrete staff request as a JSON object before passing it to the HTTP helper.
- Restrict the helper body to JSON objects, preventing accidental serialization through an erased `Any` type.
- Inspect all existing helper callers: credits and onboarding already use JSON objects; account deletion supplies no body.

## Remaining findings

Findings 3–15 in [the original review](ANDROID_FLOW_REVIEW.md) remain open. Next in sequence are draft persistence and preserving product metadata. No claim is made that all Android flows or deployed endpoints now work.

## Validation

- `:app:assembleDebug :app:lintDebug :app:lintRelease` — successful.
- Debug and release lint XML reports: **0 errors, 0 warnings, 0 informational findings** in both variants.
- Build output contains no Kotlin compiler errors or warnings.
- `git diff --check` — clean.
- [Build log](docs/flow-fixes/build-first-two.log), [debug lint XML](docs/flow-fixes/lint-debug.xml), [release lint XML](docs/flow-fixes/lint-release.xml).

No automated tests, emulator interaction, or live API requests were performed for this batch, as requested. The source defects above are addressed; their full production flows still need runtime verification.


## 3. Catalogue output, Fusion, and Set Creation repair batch

Implementation follows [the joint plan](ANDROID_OUTPUT_AND_CHAMAK_PLAN.md). Source changes cover all five batches; the final debug APK build and both lint variants passed. No AI pipeline, website, database policy, or iOS files were modified.

### Request lifetime and recovery

- Chamak is now an Android ViewModel, owned by the existing screen's ViewModel store and keyed by account/catalogue source. Requests use its lifecycle scope instead of the replaceable picker/form scope.
- Both modes serialize submissions, retain their accepted generation ID/mode and retry keys with the owner-provided SavedStateHandle, pause local polling when the flow is hidden, and reconcile server state when reopened. Preview/fixture models use transient memory rather than constructing an unbound saved-state handle. Unsaved form edits remain in the retained model when returning from the background.
- Generation creation uses a stable client-generated UUID. An uncertain insert reads that same ID instead of overwriting a running row or blindly creating another row.
- Cancellation is rethrown. Android no longer writes a server generation to Failed merely because an HTTP request was interrupted or rejected locally.
- Pending/status-retrieval problems have Check status and Retry same request actions; completed image-loading problems remain separate from generation failure. Actual server Failed state is still shown as failure.
- Removed two unused duplicate regeneration helpers; the visible retry/revision flow uses the common submission path and its key handling.

### Layout and selection

- The wholesaler shell measures its bottom navigation with Scaffold and reserves that space for the Studio hub. Its floating account capsule is excluded from Studio.
- Fusion and Set Creation run in a full-screen dialog above the app tab bar, with safe drawing/keyboard insets and explicit back actions. The retailer's existing Fusion entry also respects system areas.
- Primary buttons use minimum heights, small constrained icon targets were enlarged, and the progress screen can scroll on short screens.
- Catalogue selection, validation, previews, and API picker filtering use the same nonblank original image source, including verified/generated-only products. Display derivatives stay separate from processing source URLs.
- Both modes show selection counts/rules and accessible selected-state descriptions. Catalogue loading failures have Retry, and photo reading/normalization happens off the main thread. Android Back follows the visible back action.
- Insufficient-credit rejections now display a dialog rather than setting an invisible flag. Known rejected requests clear their retry key so a later funded attempt can be submitted.

### Catalogue and result visibility

- Product image ordering includes verified output URLs, ignores blanks, and deduplicates sources. Grid/detail derivative images can fall back to originals.
- Upload completion retains the actual returned product ID. The success close event delivers the completion callback once and closes independently; no fabricated `new` product ID is emitted.
- Catalogue pending products refresh in bounded polling while the screen is started, refresh on resume, merge by ID, and expose a manual refresh action/error. An open detail sheet receives updated product data.
- Grid badges distinguish processing, processing failure, and missing AI output. Submission copy describes acceptance without promising completion.
- Shared image loading can refresh recognized private Supabase links and shows a retry control after signing/download failure, without making storage public or altering policies.
- Creation, analysis, Fusion generation, and Set Creation generation also refresh those source links before pipeline use. Existing generations are checked against the signed-in owner before their link tokens are updated; all set pieces keep their original identities.
- Gallery entries re-fetch current status before opening; pending generations resume polling. Gallery refresh preserves prior entries on failure and retries while work is pending.
- Completed Chamak output survives thumbnail/signing failures. Result loading has explicit retry, original-image fallback, and access to additional returned outputs.

### Verification for this batch

- `:app:assembleDebug :app:lintDebug :app:lintRelease --max-workers=2 --no-parallel --no-daemon` — **BUILD SUCCESSFUL**.
- Final debug and release lint XML: **0 errors, 0 warnings, 0 informational findings** in each variant. No lint exclusions or baseline were added for this batch.
- Final build log contains no Kotlin compiler errors or warnings. `git diff --check` is clean.
- Latest installable debug APK: `app/build/outputs/apk/debug/app-debug.apk` (34,836,263 bytes), built 2026-10-18T21:38:49.975094+05:30. Its modification time is newer than the final Kotlin inputs.
- APK SHA-256: `e6112db4d0d3a791d21a0d9c3b572cd82b04aa8386c6520aaf9d8d458d257984`.
- [Build log](docs/flow-fixes/chamak-batch/build.log), [debug lint](docs/flow-fixes/chamak-batch/lint-debug.xml), [release lint](docs/flow-fixes/chamak-batch/lint-release.xml), [verification metadata](docs/flow-fixes/chamak-batch/verification.json).
- Release sources were compiled/linted; final signed/minified release packaging was not validated. Use the debug APK above for this delivery.

No automated tests, emulator interactions, or live API requests were run, per the user's restriction. Source review and build checks cannot verify the affected production record, deployed storage policies, endpoint idempotency behavior, or physical-device layouts. Runtime acceptance remains unverified.

### Scope limits

The original review's draft-persistence, product-metadata, staff navigation, orders, chat, invitations, themes, and unrelated flow findings remain separately tracked. This repair batch does not certify those flows. If the deployed service contract blocks an Android fix, describe that exact dependency and request confirmation before editing another project.


## Catalogue layout and Chamak visual polish — 19 October 2026

Android-only update based on IMG_3479.HEIC. Read the iOS ChamakGeneratingView.swift as a visual reference; no iOS or pipeline changes.

- Catalogue cards: metadata uses remaining row width, purity stays on one line, long availability copy wraps, and detail sections have a consistent minimum height. Processing labels use compact typography and rounded padding instead of default body text. Product menus have a 48 dp touch target.
- Moved Select/Cancel, Add, and overflow actions below the title onto the Refresh catalogue row, away from the floating credit/account controls. Refresh errors render separately. Grid items have stable product IDs.
- Catalogue refresh keeps existing products visible until replacement data arrives and preserves them on refresh failure. Refresh has a busy state to prevent repeated manual requests.
- Image resolution reuses own-server signed links while their expiry has at least 60 seconds remaining. Expired or unreadable links still use the existing signing path. Stable URLs allow Coil to reuse existing image cache entries and avoid a signing round trip for usable links. No storage policies changed.
- Fusion and Set processing now share the iOS white/lavender/blush gradient, diffused auras, dark and rose two-line headings, morphing white jewel and translucent progress pill. Layout scrolls on shorter displays. Radial brushes draw the large background glows without large blurred layers.

Validation: `:app:assembleDebug :app:lintDebug :app:lintRelease` succeeded; debug and release lint each report 0 errors and 0 warnings. Evidence: `docs/flow-fixes/catalogue-polish/`.

No tests, emulator/device checks, live endpoint checks or loading-time benchmarks were run, preserving the user's earlier no-test instruction. Server processing time is unchanged. Device appearance and measured loading improvement remain to be confirmed.
