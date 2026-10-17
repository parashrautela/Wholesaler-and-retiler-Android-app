# Jewel India — Android App Implementation Plan

**What this is:** the complete, self-contained guide for porting the native iOS Jewel India app (wholesaler + retailer + staff, one binary) to a native Android app written in Kotlin and Jetpack Compose.

**Who it is for:** an implementing agent (or engineer) who has read access to the whole `jewel india ` folder and write access to **only** the Android project folder. You should not need to ask anyone anything to finish. Where a decision had to be made, it has been made here. Where something truly needs the founder, it is listed in §17 and you build around it.

**Written:** 2026-10-17, against the iOS working tree at build 40 (`feat/retailer-monetisation`, including its uncommitted manufacturing changes).

---

## Table of contents

- Part A — Orientation
  - §0 Ground rules (read first, obey always)
  - §1 What the app is
  - §2 Systems, hosts and configuration
  - §3 Reference map: what to read, where
- Part B — Architecture
  - §4 Tech stack and pinned versions
  - §5 Android project layout (every file)
  - §6 iOS → Android platform mapping
  - §7 Core patterns (state, navigation, DI, networking, errors, JSON, images, strings, theming)
- Part C — Backend contract
  - §8 Every backend call the app makes
- Part D — Build phases
  - §9 Phase 0 — Scaffold, build, toolchain
  - §10 Phase 1 — Design system and core utilities
  - §11 Phase 2 — Data layer (Supabase + HTTP APIs)
  - §12 Phase 3 — Auth, session and routing
  - §13 Phase 4 — Onboarding (wholesaler + retailer)
  - §14 Phase 5 — Wholesaler app
  - §15 Phase 6 — Chamak studio
  - §16 Phase 7 — Retailer app
  - §16b Phase 8 — Staff / employee app
  - §16c Phase 9 — Screen protection, deep links, account deletion, polish
  - §16d Phase 10 — Testing, release build, Play Store readiness
- Part E — Outside the agent's reach
  - §17 Founder-owned steps
  - §18 Decisions already made (and why)
- Appendix
  - §A1 Full file-by-file mapping (all 140 iOS Swift files)
  - §A2 Asset and font mapping
  - §A3 Progress log template

---

# Part A — Orientation

## §0 Ground rules

Read this section twice. Every rule here is a hard constraint.

### 0.1 Where you may write

- **The only folder you may create, modify or delete files in:**
  `/Users/parashrautela/Documents/jewel india /jewel india android app /`
- Both directory names end in a **trailing space**: `jewel india ` and `jewel india android app `. Always quote paths, and always include the trailing space. `ls "/Users/parashrautela/Documents/jewel india"` (no space) fails with "No such file or directory". This is not a bug to fix. Don't rename either folder.
- The folder already contains an `.idea/` directory (and `jewel india android app .iml`) because Android Studio opened it. Leave them alone. Add them to `.gitignore`.
- Everything else under `/Users/parashrautela/Documents/jewel india /` is **read-only reference**. In particular, never write to:
  - `wholesaler ios/`, `wholesaler-ios-b32/`, `admin panel ios/` (iOS apps)
  - `Jewel-India-Frontend/` (Next.js web app; it is also the backend for `/api/*` routes)
  - `ai-pipeline/` (FastAPI service)
  - `Admin-Panel-for-jewel-India-/`, `supabase/`, `plans/`, `pilots/`, `visuals assets `
- Copying files *out of* the read-only folders *into* the Android folder (fonts, images, SVGs) is expected and allowed.

### 0.2 What you may not do

- No backend changes of any kind. The backend is shared with iOS and the web, and it stays exactly as it is. Don't create Supabase migrations, edit edge functions, or change Next.js routes or the AI pipeline. If an Android feature seems to need a backend change, it is in §17. Build the client side and degrade gracefully.
- No deployments, no `supabase` CLI commands against the live project, no `git push`, no publishing to Google Play.
- Never put a Supabase **service-role** key in the app. Only the **anon** key ships. If you see a service-role key anywhere in the read-only folders, do not copy it.
- Never print secret values (anon key, client IDs) into docs, logs, commit messages or chat. Refer to them by key name.
- Don't run destructive commands outside the Android folder (`rm`, `mv`, `git checkout`, `git reset`, and so on).
- Don't sign up real accounts, place real orders, pay, or send OTPs to real numbers during testing unless the founder has explicitly given you a test account. Build and verify with `@Preview`s, unit tests, and the founder's test credentials if they are in §17's handoff file.

### 0.3 Sources of truth, in priority order

1. **`wholesaler ios/JewelIndia/Sources/`** — the iOS app as it is on disk now. This is the spec. Port *behaviour, copy, colours, layout and edge-case handling* from it. Its doc comments matter as much as its code: most of them record a real bug that was hit and fixed. Read every comment in a file before porting it.
2. **`wholesaler ios/_spec/01..06-*.md`** — the original design and contract specs. They explain *why* the iOS code does what it does. When the code and the spec disagree, **the code wins** (it is newer), but note the disagreement in `PORT_STATUS.md`.
3. **`Jewel-India-Frontend/app/api/**`** — the Next.js route handlers. Read these when you need the exact request/response shape of a `SITE_URL` endpoint.
4. **`ai-pipeline/app/main.py` and `ai-pipeline/app/routers/`** — the FastAPI handlers. Read these for the exact shape of an `AI_PIPELINE_URL` endpoint.
5. **`wholesaler ios/supabase/migrations/`** and **`wholesaler ios/supabase/functions/`** — table, RPC and edge-function definitions. Read them for column names, RPC parameter names and return shapes.

Ignore `wholesaler-ios-b32/`: it is an older snapshot. Ignore `admin panel ios/`: it is a different app.

### 0.4 The parity principle

The Android app should be the **same product** as the iOS app: same screens, same flows, same words (byte for byte, including `…` vs `...` and en-dashes), same colours, same fonts, same ordering, same validation rules, same error messages, and the same server calls with the same parameters.

You may deviate **only** where Android needs a different mechanism (listed in §6), or where a decision in §18 says so. Every other deviation is a bug.

Don't "improve" behaviour that iOS deliberately reproduces from the web. Several comments say things like "this is reproduced, not improved." Respect that.

### 0.5 Working method

- Work phase by phase (§9 → §16d). Don't start a phase until the previous phase's **acceptance criteria** pass.
- For each iOS file you port: open it, read it end to end, then write the Kotlin file(s) that §A1 maps it to. Put a header comment at the top of each Kotlin file:
  ```kotlin
  // Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift
  ```
  Then carry over the *meaningful* doc comments (the "why" ones) in Kotlin KDoc. Don't carry over comments about UIKit or SwiftUI mechanics that no longer apply.
- After each phase:
  1. `./gradlew :app:assembleDebug` must succeed with zero errors.
  2. `./gradlew :app:testDebugUnitTest` must pass.
  3. `./gradlew :app:lintDebug` must not report new errors (warnings are fine).
  4. Update `PORT_STATUS.md` (template in §A3).
  5. Commit to the Android folder's own local git repository (created in Phase 0). One commit per phase at minimum. Never add a remote. Never push.
- If you get stuck on one thing, log it in `OPEN_QUESTIONS.md`, put a visible placeholder screen in its place (see `PhasePlaceholder` in `RetailerShell.swift`), and keep going. Don't stop the whole port for one blocked screen.
- Keep each Kotlin file under about 600 lines. If an iOS file is bigger (several are 700–1,000 lines), split it along its own `struct` boundaries. §A1 already shows the split for the large ones.

---

## §1 What the app is

Jewel India is a B2B jewellery marketplace for India. One app serves three kinds of user. Which experience a person sees is decided at sign-in by their **role** and their **verification status**.

### 1.1 Roles

| Role | Who | How they get in | What they do |
|---|---|---|---|
| **Wholesaler** | Jewellery manufacturers and suppliers | Sign up with phone or email + OTP, or Google. Then complete a 3-step onboarding with documents and an onboarding fee (when the server requires one). Jewel India admins verify them. | Upload designs (AI studio-photo pipeline), manage the catalogue, receive and handle orders from retailers, chat with stores, invite retailers with a code, run **Chamak** (AI jewellery styling and set creation), respond to manufacturing requests, see reports and their credit wallet. |
| **Retailer** | Jewellery stores | Need a wholesaler's **invitation code** first. Then sign up, complete a 3-step onboarding, and get verified. | Browse the marketplace ("Discover", "Your Taste"), shortlist designs, request orders, add their own store designs, manage staff accounts, manage customer wishlists and boards with share links, pick a store theme, buy plans and unlocks with credits, raise custom manufacturing enquiries, chat with suppliers, use Chamak on their own designs. A verified retailer can switch into **Employee View**. |
| **Employee / Staff** | Store staff | Sign in with a username + password the store created, or with Google if the store invited their Google address. No self-signup. | A customer-facing showroom: home hero on the store's theme, catalogue (Pinterest-style masonry), infinite canvas, design detail, multi-select and "Send Request" orders, queries (chat), orders. |

### 1.2 Routing table

This is decided in `Core/AuthRouter.swift` and `Core/SessionStore.swift`. Port it exactly.

```
signed out ──────────────────────────────► Auth flow (RoleChoice front door)
signed in, no role ──────────────────────► Select role (RoleChoiceView, signedIn = true)
wholesaler, no wholesalers row ──────────► Wholesaler onboarding
wholesaler, banned ──────────────────────► sign out → Entry with "banned" error
wholesaler, verified + has_visited ──────► Wholesaler dashboard (tabs)
wholesaler, verified, not visited ───────► Submitted screen (which then marks visited)
wholesaler, any other status ────────────► Submitted screen
retailer, no retailers row ──────────────► Retailer onboarding
retailer, banned ────────────────────────► sign out → Entry with "banned" error
retailer, verified, view mode = retailer ► Retailer dashboard (tabs)   [default]
retailer, verified, view mode = employee ► Employee shell (Employee View)
retailer, any other status ──────────────► Retailer submitted screen
employee, row missing or not active ─────► sign out → Staff sign-in with "deactivated" error
employee, active ────────────────────────► Employee shell
any lookup failed (network) ─────────────► Unreachable screen (keeps session, retry)
password recovery link followed ─────────► Update password screen
```

### 1.3 Feature inventory

The size of each area tells you where the effort goes. Line counts are from iOS.

| Area | iOS lines | Notes |
|---|---:|---|
| Wholesaler (incl. Chamak, Treasure Chest, Report) | 13,748 | Largest. Chamak alone is about 5,000. |
| Employee | 5,112 | Custom chrome, masonry, infinite canvas, themes. |
| Networking | 4,648 | 17 API files. |
| Retailer | 4,534 | Marketplace store, wishlists, themes, staff. |
| Manufacturing | 2,514 | Shared by retailer (requests) and wholesaler (offers). |
| Auth | 2,447 | Three doors, OTP, Google, password reset. |
| Core | 2,227 | Session, router, images, screen protection. |
| Onboarding (wholesaler) | 1,785 | |
| Navigation | 1,660 | Of which `DebugScreenPeek.swift` (901) is not ported. |
| RetailerOnboarding | 916 | |
| Chat | 666 | |
| Design | 419 | Tokens. |
| Profile | 420 | |
| **Total** | **41,157** | |

---

## §2 Systems, hosts and configuration

### 2.1 The four backends

The app talks to four things. All of them already exist and are shared with iOS and the web.

| Name in code | What it is | Base URL source | Auth |
|---|---|---|---|
| **Supabase** | Postgres (PostgREST), Auth (GoTrue), Storage, Edge Functions | `SUPABASE_URL` | anon key + user JWT (handled by the SDK) |
| **Site API** | The Next.js app's `/api/*` routes (Vercel) | `SITE_URL` | `Authorization: Bearer <user access token>` on authenticated routes. Some auth routes are unauthenticated and set cookies. |
| **AI pipeline** | FastAPI on Railway: product image processing, Chamak, manufacturing marketplace, image search, device registration | `AI_PIPELINE_URL` | `Authorization: Bearer <user access token>` |
| **Edge functions** | `credits-topup`, `staff-accounts`, `delete-account` (and `apple-iap`, which Android does **not** use) | `SUPABASE_URL` + `/functions/v1/<name>` | `Authorization: Bearer <user access token>` + `apikey: <anon key>` |

### 2.2 Configuration keys

iOS reads these from `wholesaler ios/Config.xcconfig` via `Info.plist` (see `Core/AppConfig.swift`). Android reads them from a **gitignored** `secrets.properties` at the Android project root and exposes them as `BuildConfig` fields.

| Key | Required | Where the value comes from |
|---|---|---|
| `SUPABASE_URL` | yes | `wholesaler ios/Config.xcconfig` |
| `SUPABASE_ANON_KEY` | yes | `wholesaler ios/Config.xcconfig` |
| `AI_PIPELINE_URL` | yes | `wholesaler ios/Config.xcconfig` |
| `SITE_URL` | yes | `wholesaler ios/Config.xcconfig`. Read the comment above it: `app.jewelindia.shop` has no DNS record right now, so the value points at the live Vercel deployment. Use the value exactly as it is in the file. |
| `GOOGLE_WEB_CLIENT_ID` | no | **Not** in the xcconfig. The founder provides it (§17). Empty → Google sign-in uses the browser flow, exactly as iOS does when `GOOGLE_IOS_CLIENT_ID` is unset. |

**xcconfig escape gotcha:** in `.xcconfig` files `//` starts a comment, so URLs there are written as `https:/$()/host…`. When you copy a URL value, replace `/$()/` with `//`. A URL like `https:/$()/abc.supabase.co` becomes `https://abc.supabase.co`.

Don't copy `GOOGLE_IOS_CLIENT_ID` or `GOOGLE_IOS_REVERSED_CLIENT_ID`. They are iOS-only.

### 2.3 Fixed constants (not secrets)

From `Core/AppConfig.swift` and `Core/LegalLinks.swift`:

- Auth callback scheme: `jewelindia`. Callback URL: `jewelindia://auth/callback`. Password-reset redirect: `jewelindia://auth/callback?next=/update-password`.
- Legal pages: `https://jewelindia.shop/terms-conditions`, `https://jewelindia.shop/privacy-policy`, `https://jewelindia.shop/support`.
- Invitation links are built as `https://jewelindia.shop/join/<code>` (see `JewelAPI.swift`).
- Supabase project ref = first label of the `SUPABASE_URL` host. Used to find the `sb-<ref>-auth-token` cookie.
- App identity: application ID `com.jewelindia.app` (same as the iOS bundle ID), display name **Jewel India**, portrait only on phones, light theme only (`UIUserInterfaceStyle = Light`).

---

## §3 Reference map

Read these **before writing any code**, in this order. Budget real time for it. The `_spec` files alone are about 5,800 lines.

| # | File (all paths relative to `/Users/parashrautela/Documents/jewel india /`) | Why |
|---|---|---|
| 1 | `wholesaler ios/README.md` | Invitation flow contract, API notes. |
| 2 | `wholesaler ios/project.yml`, `wholesaler ios/JewelIndia/Info.plist` | Bundle, permissions copy, fonts, URL schemes. |
| 3 | `wholesaler ios/_spec/01-design-system.md` | Colours, type, spacing, motion, components. |
| 4 | `wholesaler ios/_spec/02-auth-and-session.md` | Auth flows and the routing table. |
| 5 | `wholesaler ios/_spec/03-shell-and-navigation.md` | Tabs, menus, shells. |
| 6 | `wholesaler ios/_spec/04-data-contracts.md` | Tables, columns, JSON shapes. |
| 7 | `wholesaler ios/_spec/05-wholesaler-screens.md` | Every wholesaler screen. |
| 8 | `wholesaler ios/_spec/06-retailer-screens.md` | Every retailer screen. |
| 9 | `wholesaler ios/JewelIndia/Sources/Core/*` | Session, routing, models. Port these first. |
| 10 | `wholesaler ios/JewelIndia/Sources/Networking/*` | Every backend call. |
| 11 | `wholesaler ios/JewelIndia/Sources/Navigation/*` (except `DebugScreenPeek.swift`) | Root switch and shells. |
| 12 | `wholesaler ios/JewelIndia/Sources/Features/Wholesaler/Chamak/CHAMAK_PIPELINE_CHANGES.md` and `CHAMAK_2_OPENAI_SPEC.md` (same folder) | Chamak pipeline contract notes. |
| 13 | `wholesaler ios/docs/retailer-image-search.md` | Image search contract. |
| 14 | `wholesaler ios/tests/*/` | Small Swift test harnesses. Port their assertions to JUnit (§16d). |
| 15 | `wholesaler ios/APP_REVIEW_NOTES_DRAFT.md` | What a reviewer is told about the app (reuse for Play review notes). |

Where to look up a backend shape:

- `SITE_URL/api/<x>` → `Jewel-India-Frontend/app/api/<x>/route.js` (or `.ts`). Folders present: `auth/{check-user,send-otp,set-password,toggle-view,verify-otp}`, `referral/{claim,generate,manage,validate}`, `retailer/{catalogue,marketplace,orders,your-taste}`, `orders`, `wishlist-shares`, `shared-wishlist`, `wishlists`, `chat`, `designs`, `employees`, `onboard`, `onboard-retailer`, `products`, `catalogue`.
- `AI_PIPELINE_URL/<x>` → `ai-pipeline/app/main.py` (`/process`, `/process/{id}`, `/product/{id}`, `/api/upload-usage`, `/api/chamak/*`, `/api/set-creation/generate`) and `ai-pipeline/app/routers/`. The manufacturing and `/devices/register` routers declare paths without `/api` and are mounted under an `/api` prefix, so the app calls `/api/retailer/manufacturing-requests`, `/api/devices/register`, and so on. The image-search router declares its full path `/api/retailer/image-search`. When in doubt, the URL the iOS file builds is the right one.
- Tables and RPCs → `wholesaler ios/supabase/migrations/*.sql`, `ai-pipeline/migrations/`, and `wholesaler ios/SUPABASE_CHAMAK_MIGRATION.sql`.
- Edge functions → `wholesaler ios/supabase/functions/{credits-topup,delete-account,razorpay-webhook}/`. The `staff-accounts` function's source lives in `Admin-Panel-for-jewel-India-/supabase/functions/staff-accounts/` (read-only, like everything else).

---

# Part B — Architecture

## §4 Tech stack and pinned versions

### 4.1 Decisions

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Native Android standard. |
| UI | Jetpack Compose + Material 3 (styled to the Jewel palette, **not** stock Material look) | Declarative, closest mental model to SwiftUI, so the iOS views port almost structure for structure. |
| Architecture | Single activity, MVVM: one `ViewModel` per screen exposing `StateFlow<UiState>` and event functions | Mirrors the iOS `@Observable` model classes (`HomeModel`, `AddProductForm`, `ChamakViewModel`, stores). |
| Navigation | Navigation Compose with **type-safe `@Serializable` routes** | Matches the iOS `enum Route: Hashable` + `NavigationStack(path:)` pattern. |
| DI | **Manual**: one `AppGraph` object created in `JewelApplication` | No Hilt/kapt complexity. The iOS app uses singletons and environment objects, so this is a 1:1 fit. |
| Supabase | `supabase-kt` (`io.github.jan-tennert.supabase`): `auth-kt`, `postgrest-kt`, `storage-kt`, `functions-kt` | Official community Kotlin SDK, the same feature set as `supabase-swift`. |
| HTTP (Site API, pipeline, edge functions) | OkHttp 5 | Full control over cookies, multipart, error bodies and timeouts, all of which iOS relies on. |
| Ktor engine for supabase-kt | `ktor-client-okhttp` | Shares the OkHttp stack. |
| JSON | `kotlinx.serialization` | Required by supabase-kt. Also used for our own DTOs. |
| Images | Coil 3 (`coil-compose`, `coil-network-okhttp`, `coil-svg`) | Memory + disk cache, size-aware decoding. Replaces `Core/Images/ImageCache.swift`. SVG support covers the 22 SVG assets. |
| Async | Kotlin coroutines + Flow | Equivalent of Swift `async/await` + `AsyncSequence`. |
| Local key-value storage | `SharedPreferences` (one file, `jewel_prefs`) wrapped in a small `Prefs` class | Equivalent of `UserDefaults`. Keys are listed in §7.9. |
| In-app browser | AndroidX Browser Custom Tabs | Equivalent of `SFSafariViewController` / `ASWebAuthenticationSession`. |
| Google sign-in | Credential Manager (`androidx.credentials` + `googleid`) → `signInWith(IDToken)`; browser OAuth fallback | Mirrors `Features/Auth/GoogleSignIn.swift` exactly, including the nonce. |
| Photo picking | `ActivityResultContracts.PickVisualMedia` (system photo picker) | Equivalent of `PhotosPicker`. No storage permission needed. |
| Camera | `ActivityResultContracts.TakePicture` + `FileProvider` | Equivalent of the camera option in the upload boxes. |
| Screenshot protection | `WindowManager.LayoutParams.FLAG_SECURE` (reference-counted per screen) + `setRecentsScreenshotEnabled(false)` | Android's real API for what iOS fakes with a secure text field canvas. See §16c. |
| Tests | JUnit 4 + `kotlinx-coroutines-test` for JVM unit tests; Compose `@Preview`s for visual checks | |

### 4.2 Versions

These were the current stable releases on 2026-10-17. Pin them exactly in `gradle/libs.versions.toml`. If one fails to resolve, use the nearest stable patch that does, and write down the change in `PORT_STATUS.md`. Don't use alpha, beta or RC builds.

```toml
[versions]
agp = "9.4.1"
kotlin = "2.4.21"
composeBom = "2026.09.00"
activityCompose = "1.13.0"
coreKtx = "1.19.1"
lifecycle = "2.11.0"
navigation = "2.10.2"
coroutines = "1.11.0"
serialization = "1.11.0"
supabase = "3.8.0"
ktor = "3.6.0"
okhttp = "5.5.0"
coil = "3.6.3"
browser = "1.10.0"
credentials = "1.6.0"
googleid = "1.2.1"
exifinterface = "1.4.2"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-process = { module = "androidx.lifecycle:lifecycle-process", version.ref = "lifecycle" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-foundation = { module = "androidx.compose.foundation:foundation" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
supabase-bom = { module = "io.github.jan-tennert.supabase:bom", version.ref = "supabase" }
supabase-auth = { module = "io.github.jan-tennert.supabase:auth-kt" }
supabase-postgrest = { module = "io.github.jan-tennert.supabase:postgrest-kt" }
supabase-storage = { module = "io.github.jan-tennert.supabase:storage-kt" }
supabase-functions = { module = "io.github.jan-tennert.supabase:functions-kt" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coil" }
coil-svg = { module = "io.coil-kt.coil3:coil-svg", version.ref = "coil" }
androidx-browser = { module = "androidx.browser:browser", version.ref = "browser" }
androidx-credentials = { module = "androidx.credentials:credentials", version.ref = "credentials" }
androidx-credentials-play = { module = "androidx.credentials:credentials-play-services-auth", version.ref = "credentials" }
googleid = { module = "com.google.android.libraries.identity.googleid:googleid", version.ref = "googleid" }
androidx-exifinterface = { module = "androidx.exifinterface:exifinterface", version.ref = "exifinterface" }
junit = { module = "junit:junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

**AGP 9 note:** AGP 9 has **built-in Kotlin support**. Do **not** apply `org.jetbrains.kotlin.android`. AGP will refuse to build if you do. Apply only `com.android.application`, `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.kotlin.plugin.serialization`. If the build reports a Kotlin or Gradle version mismatch, follow the exact fix in the error message and log it.

**Gradle:** wrapper `9.8.1`. **JDK:** use Android Studio's bundled JBR: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`. A JDK 24 is also installed system-wide. Don't use it for Gradle unless JBR fails.

**SDK:** `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26`. Platforms 34, 35, 36 and 36.1 and build-tools up to 37.0.0 are already installed at `/Users/parashrautela/Library/Android/sdk`.

## §5 Android project layout

Every file the finished project contains. Where a line says `← iOS:` it names the source to port from. The full 140-file table is in §A1.

```
jewel india android app /
├── IMPLEMENTATION_PLAN.md            this file (don't edit except to fix typos)
├── PORT_STATUS.md                    progress log (template §A3), updated every phase
├── OPEN_QUESTIONS.md                 anything blocked or ambiguous, with what you did instead
├── README.md                         how to configure, build and run (write in Phase 0, finish in Phase 10)
├── .gitignore
├── secrets.example.properties        committed; keys from §2.2 with empty values
├── secrets.properties                NOT committed; real values (from wholesaler ios/Config.xcconfig)
├── local.properties                  NOT committed; sdk.dir=/Users/parashrautela/Library/Android/sdk
├── settings.gradle.kts
├── build.gradle.kts                  root; plugins `apply false`
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml            §4.2
│   └── wrapper/gradle-wrapper.{jar,properties}
├── gradlew, gradlew.bat
├── tools/
│   ├── copy_assets.sh                copies fonts, PNG/JPG and SVG assets out of the iOS project (§A2)
│   └── make_launcher_icons.sh        builds mipmaps from AppIcon-1024.png with macOS `sips`
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── assets/svg/                       22 SVGs (§A2)
        │   ├── res/font/                         24 TTFs, renamed (§A2)
        │   ├── res/drawable-nodpi/               PNG/JPG imagesets (§A2)
        │   ├── res/drawable/ic_launcher_foreground.xml (or PNG layers)
        │   ├── res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.png, ic_launcher_round.png
        │   ├── res/mipmap-anydpi-v26/ic_launcher.xml, ic_launcher_round.xml
        │   ├── res/values/strings.xml            app_name = "Jewel India" only (all other copy lives in Copy.kt)
        │   ├── res/values/colors.xml             launch_background = #FEFEFE
        │   ├── res/values/themes.xml             Theme.JewelIndia (light, splash = background colour)
        │   ├── res/xml/file_paths.xml            FileProvider paths (camera captures, export shares)
        │   ├── res/xml/backup_rules.xml          exclude auth session + prefs from backup
        │   ├── res/xml/data_extraction_rules.xml same, Android 12+
        │   └── java/com/jewelindia/app/
        │       ├── JewelApplication.kt           creates AppGraph, Coil ImageLoader     ← iOS: JewelIndiaApp.swift
        │       ├── MainActivity.kt               single activity, deep links, secure flags ← iOS: JewelIndiaApp.swift + RootView.onOpenURL
        │       ├── AppGraph.kt                   manual DI container
        │       ├── core/
        │       │   ├── config/AppConfig.kt                      ← Core/AppConfig.swift
        │       │   ├── model/Models.kt                          ← Core/Models.swift
        │       │   ├── model/Product.kt                         ← Features/Wholesaler/WholesalerModels.swift (Product, ImageSize)
        │       │   ├── model/WholesalerModels.kt                ← WholesalerModels.swift (the rest)
        │       │   ├── session/SessionStore.kt                  ← Core/SessionStore.swift
        │       │   ├── session/AuthRouter.kt                    ← Core/AuthRouter.swift
        │       │   ├── session/ViewModeStore.kt                 ← Core/AuthRouter.swift (ViewModeStore)
        │       │   ├── session/SignupFlow.kt                    ← Core/SignupFlow.swift
        │       │   ├── prefs/Prefs.kt                           UserDefaults equivalent
        │       │   ├── text/Copy.kt                             ← Core/Copy.swift (verbatim)
        │       │   ├── text/Credentials.kt                      ← Core/Credentials.swift
        │       │   ├── text/StringExt.kt                        `trimmed`, `nilIfEmpty` helpers (WholesalerModels.swift tail)
        │       │   ├── time/JewelDates.kt                       ISO-8601 parse/format helpers
        │       │   ├── legal/LegalLinks.kt                      ← Core/LegalLinks.swift (+ Custom Tab opener)
        │       │   ├── images/ImageNormalizer.kt                ← Core/ImageNormalizer.swift
        │       │   ├── images/JewelImageLoader.kt               ← Core/Images/ImageCache.swift
        │       │   ├── images/CachedImage.kt                    ← Core/Images/CachedImage.swift
        │       │   ├── security/ScreenSecurity.kt               ← ScreenProtection/{SecureLayerHost,AppSwitcherCover}.swift
        │       │   ├── security/CaptureProtected.kt             ← ScreenProtection/View+CaptureProtected.swift
        │       │   ├── security/ProtectedImage.kt               ← ScreenProtection/ProtectedImageView.swift
        │       │   ├── security/CaptureMonitor.kt               ← ScreenProtection/CaptureMonitor.swift
        │       │   ├── ui/RefreshContainer.kt                   ← Core/RefreshTask.swift
        │       │   └── debug/DebugBuild.kt                      ← Core/DebugBuild.swift
        │       ├── design/
        │       │   ├── Palette.kt                               ← Design/Palette.swift
        │       │   ├── Typography.kt                            ← Design/Typography.swift
        │       │   ├── Layout.kt                                ← Design/Layout.swift
        │       │   ├── Motion.kt                                ← Design/Motion.swift
        │       │   ├── JewelTheme.kt                            MaterialTheme wiring (light only)
        │       │   └── components/                              shared widgets (see §A1: FormComponents, AuthKit, OnboardKit)
        │       ├── data/
        │       │   ├── supabase/Supabase.kt                     ← Networking/SupabaseManager.swift
        │       │   ├── net/HttpClients.kt                       OkHttp clients (site w/ cookie jar, pipeline, plain)
        │       │   ├── net/JewelNetwork.kt                      ← Networking/NetworkResilience.swift
        │       │   ├── net/SsrSessionBridge.kt                  ← Networking/SSRSessionBridge.swift
        │       │   ├── net/ApiError.kt                          ← JewelAPI.APIError + throwIfError
        │       │   ├── net/AuthToken.kt                         fresh access token helper (iOS `auth.session`)
        │       │   ├── net/EdgeFunctions.kt                     POST /functions/v1/<name>
        │       │   ├── net/Multipart.kt                         ← WholesalerAPI.swift (MultipartForm)
        │       │   └── api/  (one file per iOS Networking file; see §A1)
        │       ├── navigation/
        │       │   ├── RootScreen.kt                            ← Navigation/RootView.swift
        │       │   ├── AuthNavHost.kt                           ← Navigation/AuthFlowView.swift
        │       │   ├── WholesalerShell.kt                       ← Navigation/WholesalerShell.swift
        │       │   ├── RetailerShell.kt                         ← Navigation/RetailerShell.swift
        │       │   └── ShellChrome.kt                           shared bottom bar / top bar / profile menu
        │       └── feature/
        │           ├── auth/              ← Features/Auth/*
        │           ├── onboarding/        ← Features/Onboarding/*
        │           ├── retaileronboarding/← Features/RetailerOnboarding/*
        │           ├── profile/           ← Features/Profile/*
        │           ├── chat/              ← Features/Chat/*
        │           ├── wholesaler/        ← Features/Wholesaler/*.swift
        │           ├── wholesaler/chamak/ ← Features/Wholesaler/Chamak/*
        │           ├── wholesaler/treasurechest/ ← Features/Wholesaler/TreasureChest/*
        │           ├── wholesaler/report/ ← Features/Wholesaler/Report/*
        │           ├── manufacturing/     ← Features/Manufacturing/*
        │           ├── retailer/          ← Features/Retailer/*.swift
        │           ├── retailer/wishlist/ ← Features/Retailer/Wishlist/*
        │           └── employee/          ← Features/Employee/* (+ Data/WebShuffle.swift)
        └── test/java/com/jewelindia/app/  JVM unit tests (§16d)
```

Naming: Swift `FooView` becomes Kotlin `FooScreen` (a full screen) or `Foo` (a reusable composable). Its model becomes `FooViewModel`. Sheets keep the `Sheet` suffix.

## §6 iOS → Android platform mapping

Use this table every time you meet one of these iOS constructs.

| iOS (SwiftUI/UIKit) | Android (Compose) | Notes |
|---|---|---|
| `@Observable final class X` (model/store) | `class XViewModel : ViewModel()` with `private val _state = MutableStateFlow(UiState())`; `val state = _state.asStateFlow()` | Screens use `collectAsStateWithLifecycle()`. |
| Session-wide `@Observable` stores injected with `.environment(store)` (`SessionStore`, `SignupFlow`, `CreditStore`, `WholesalerOrdersStore`, `RetailerOrdersStore`, `EmployeeStore`) | Long-lived classes owned by `AppGraph` (session-wide) or by the shell's `ViewModel` (shell-scoped), passed down explicitly or through a `CompositionLocal` | Shell-scoped stores must be recreated when the shell is (sign-out → sign-in as someone else). |
| `@State` local view state | `remember { mutableStateOf(...) }` / `rememberSaveable` | |
| `.task { }` | `LaunchedEffect(Unit) { }` | |
| `.task(id: x) { }` | `LaunchedEffect(x) { }` | |
| `.task(id: scenePhase) { if active … }` | `LifecycleResumeEffect` or `repeatOnLifecycle(Lifecycle.State.RESUMED)` | Used for `credits.maintainDailyWallet()` and TopUp refresh. |
| `.refreshable` / `refreshTask` | `PullToRefreshBox` (Material 3). Launch the refresh in the ViewModel's scope so it survives recomposition. | |
| `NavigationStack(path:)` + `navigationDestination(for:)` | `NavHost` with `@Serializable` route classes; `navController.navigate(Route)` | One `NavHost` per tab inside shells, so each tab keeps its own back stack (§7.2). |
| `TabView` + `Tab(...)` | Custom bottom bar in `ShellChrome.kt` (Material 3 `NavigationBar` styled with `Palette`) | iOS 26 draws a floating glass bar. On Android draw a solid white bar with a hairline top border, dark selected tint and muted unselected tint, plus the iOS tab icons from assets. |
| `.toolbar { ToolbarItemGroup(.topBarTrailing) }` | `TopAppBar(actions = { … })` | The credit pill + logo menu sit on the right on every tab. |
| `.navigationTitle` + `.inline` | `CenterAlignedTopAppBar` title in the screen's heading font | |
| `Menu { Button … }` | `DropdownMenu` anchored to the logo/avatar button | Keep item order and labels, and the dividers (`HorizontalDivider`). Destructive items in `Palette.statusRejected`. |
| `.sheet(isPresented:)` | `ModalBottomSheet` (with `skipPartiallyExpanded` where iOS uses only `.large`) | `.presentationDetents([.medium, .large])` → allow partial expansion. `.height(590)` → content-sized sheet. |
| `.fullScreenCover` | A full-screen destination in the NavHost, or a `Dialog(properties = DialogProperties(usePlatformDefaultWidth = false))` | Chamak in retailer is a fullScreenCover: use a full-screen route. |
| `.confirmationDialog` | `AlertDialog` with confirm/dismiss buttons (destructive confirm in red) | Same title, message and button labels. |
| `.alert` | `AlertDialog` | |
| `ScrollView` + `VStack` | `Column(Modifier.verticalScroll(rememberScrollState()))` or `LazyColumn` for long/infinite lists | Use `LazyColumn`/`LazyVerticalGrid`/`LazyVerticalStaggeredGrid` for every product list. |
| `LazyVGrid` | `LazyVerticalGrid(GridCells.Fixed(n))` | |
| Masonry (`MasonryGrid`, Pinterest cards) | `LazyVerticalStaggeredGrid` | Employee catalogue. |
| Custom `Layout` (`FlowLayout`, `FlowRow`) | `FlowRow` (Compose foundation) | |
| `GeometryReader` | `BoxWithConstraints` | |
| `Shape` structs (gem shapes, arches) | `GenericShape { … }` or `Canvas` paths | Port path math 1:1. |
| `.matchedGeometryEffect` / transitions | `AnimatedContent`, `AnimatedVisibility`, `animate*AsState` using `Motion` easings | |
| `PhotosPicker` | `rememberLauncherForActivityResult(PickVisualMedia())` / `PickMultipleVisualMedia(n)` | |
| `UIImagePickerController(.camera)` | `TakePicture()` contract + `FileProvider` URI in `cacheDir/camera/` | Requires `CAMERA` permission at runtime. |
| `ShareLink` / `UIActivityViewController` | `Intent.createChooser(Intent(ACTION_SEND).setType(…).putExtra(…), null)` | For images, share a `content://` URI from `FileProvider` with `FLAG_GRANT_READ_URI_PERMISSION`. |
| `UIDocumentPickerViewController` export folder (`FolderExportPicker`) | `ActivityResultContracts.OpenDocumentTree()` then write with `DocumentFile` | Wholesaler catalogue "export". |
| Save image to Photos | `MediaStore.Images` insert into `Pictures/Jewel India` | No permission on API 29+. On 26–28, request `WRITE_EXTERNAL_STORAGE` (declare with `maxSdkVersion="28"`). |
| `UIPasteboard.general.string = …` | `LocalClipboardManager` / `ClipboardManager.setPrimaryClip` | Invite code copy, staff credentials copy. |
| `SFSafariViewController` / `InAppBrowser` | `CustomTabsIntent.Builder().setToolbarColor(Palette.dark)…launchUrl()` | Legal pages, Razorpay checkout. |
| `ASWebAuthenticationSession` (Supabase OAuth) | supabase-kt `signInWith(Google, redirectUrl = "jewelindia://auth/callback")` (opens Custom Tab) + `supabase.handleDeeplinks(intent)` in `MainActivity` | |
| `.onOpenURL` / universal links | `intent-filter`s in the manifest + `MainActivity.onNewIntent` → `RootViewModel.handleIncomingUri(uri)` | §16c. |
| `UserDefaults` | `Prefs` (SharedPreferences) | Keys §7.9. |
| Keychain (Supabase session) | supabase-kt's default session storage (SharedPreferences via `multiplatform-settings`) | Exclude it from cloud backup (`backup_rules.xml`). |
| `scenePhase != .active` → `AppSwitcherCover` | `Activity.setRecentsScreenshotEnabled(false)` (API 33+); `FLAG_SECURE` on API 26–32 | §16c. |
| `SecureLayerHost` / `ProtectedImageView` / `.captureProtected` | `CaptureProtected { }` composable that holds a reference-counted `FLAG_SECURE` while on screen | §16c. |
| `UIScreen.isCaptured`, screenshot notification | Not needed for protection (`FLAG_SECURE` blocks recording and screenshots). Optional: `Activity.ScreenCaptureCallback` (API 34) for the audit counter | |
| `sensoryFeedback` / impact generator | `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress / TextHandleMove)` | 3 call sites. |
| SF Symbols (`systemImage:`) | `androidx.compose.material.icons` (extended) nearest equivalent | §A2.3 gives the mapping for every symbol used in the shells. |
| `Image("AssetName")` | `painterResource(R.drawable.asset_name)` for PNG/JPG; `AsyncImage(model = "file:///android_asset/svg/Name.svg")` for SVG | §A2. |
| `Font.custom(face, size:)` | `TextStyle(fontFamily = …, fontWeight = …, fontSize = n.sp)` via `design/Typography.kt` helpers | iOS points → Android `sp` for text and `dp` for layout, 1:1. |
| `.presentationDragIndicator(.visible)` | `ModalBottomSheet(dragHandle = { BottomSheetDefaults.DragHandle() })` | |
| `Task.sleep(for:)` | `delay(ms)` | |
| `async let a = …; async let b = …` | `coroutineScope { val a = async { … }; val b = async { … } }` | |
| `actor` (`ImageCache`, `CatalogueImageSearch`) | A class guarded by a `Mutex`, or confined to one dispatcher | |
| `#if DEBUG` | `if (BuildConfig.DEBUG)` | |
| `DebugScreenPeek` (launch-argument screen previews) | **Not ported.** Use Compose `@Preview` functions with the same sample data instead. | |
| Apple Sign In (`AppleAuthButton`) | **Not on Android** (§18 D4) | |
| StoreKit (`AppleCreditPurchases`, `AppleTopUpView`) | **Not on Android.** It is dead code on iOS too (§18 D5). | |
| APNs device token → `ManufacturingAPI.registerDeviceToken` | **Not in v1.** iOS never asks for notification permission, so push is effectively off there too (§18 D6). | |

## §7 Core patterns

### 7.1 Dependency container

```kotlin
// AppGraph.kt — created once in JewelApplication.onCreate()
class AppGraph(app: Application) {
    val prefs = Prefs(app)
    val supabase: SupabaseClient = createJewelSupabase()           // data/supabase/Supabase.kt
    val http = HttpClients(app)                                      // data/net/HttpClients.kt
    val jewelApi = JewelApi(supabase, http)
    val wholesalerApi = WholesalerApi(supabase, http)
    val chamakApi = ChamakApi(supabase, http)
    val creditsApi = CreditsApi(supabase, http)
    // … one instance per data/api file
    val signupFlow = SignupFlow(prefs)
    val sessionStore = SessionStore(supabase, authRouter = AuthRouter(supabase, prefs), signupFlow, …)
}
```

Screens get the graph through `(LocalContext.current.applicationContext as JewelApplication).graph`, wrapped in a `LocalAppGraph` `CompositionLocal` provided at `RootScreen`. ViewModels are created with `viewModel { FooViewModel(graph.fooApi, …) }` (the `viewModelFactory`/initializer DSL).

The iOS `enum XAPI { static func … }` namespaces become **classes with instance methods** taking their dependencies in the constructor. Keep the iOS function names, converted to Kotlin style (`fetchCatalogue`, `updateProductFlags`, …), so a reader can grep across both codebases.

### 7.2 Navigation

- `RootScreen` observes `SessionStore.phase` (`Loading` / `Unauthenticated(destination)` / `Authenticated(destination)`) and switches content with `AnimatedContent` (cross-fade using `Motion.fadeIn`). There is **no** NavHost at the root. This mirrors `RootView`'s `switch` exactly, and it means a role change tears the whole shell down, which is the behaviour iOS relies on.
- `AuthNavHost` is one `NavHost` whose start destination is `RoleChoice` and whose routes are the `AuthRoute` cases (`Entry(mode)`, `InviteCode`, `EmployeeSignIn`, `SignIn(identity)`, `VerifyOtp`, `SetPassword`, `ForgotPassword`, `UpdatePassword`). `applyDestination()` logic from `AuthFlowView.swift` runs in a `LaunchedEffect(destinationKey)` and rebuilds the back stack: for `SignIn(identity)` the stack is `[Entry(SignIn), SignIn(identity)]`, for `EmployeeLogin` it is `[EmployeeSignIn]`, for `UpdatePassword` it is `[UpdatePassword]`, and anything else pops to `RoleChoice`.
- Shells (`WholesalerShell`, `RetailerShell`, `EmployeeShell`) own a selected-tab state and **one `NavHostController` per tab** (`rememberNavController()` per tab, kept in a map so switching tabs keeps each stack). Each tab's start destination is its root screen. Push routes inside a tab (for example, Home → `UploadHistory` / `TreasureChest` / `ManufacturingOffers`) with that tab's controller.
- Android system back: inside a tab, pop that tab's stack. At a tab root, if the tab isn't the first one, go to the first tab. Otherwise let the activity finish. Sheets close on back.
- Route classes are `@Serializable data class`/`object` in the same file as their shell.

### 7.3 State and errors in ViewModels

Every screen ViewModel follows this shape:

```kotlin
data class CatalogueUiState(
    val loading: Boolean = true,
    val products: List<Product> = emptyList(),
    val error: String? = null,           // already user-facing copy
    // …
)

class CatalogueViewModel(private val api: WholesalerApi, …) : ViewModel() {
    private val _state = MutableStateFlow(CatalogueUiState())
    val state: StateFlow<CatalogueUiState> = _state.asStateFlow()

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { api.fetchCatalogue(…) }
            .onSuccess { page -> _state.update { it.copy(loading = false, products = page.products) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.userMessage()) } }
    }
}
```

`Throwable.userMessage()` (in `data/net/ApiError.kt`) returns, in this order: `ApiError.message` (server copy, verbatim), `DbRefusal.message(e)` for `PostgrestRestException`, a mapped transport message for `IOException` (`Copy.networkError`), and otherwise `Copy.genericFailure`. iOS surfaces `error.localizedDescription` in many places. Where it does, surface `userMessage()`.

**Never swallow an error iOS shows, and never show one iOS swallows.** iOS uses `try?` deliberately in many places (counts default to 0, upload usage falls back to unlimited, and so on). Use `runCatching { … }.getOrDefault(…)` in exactly those places.

### 7.4 Supabase client

```kotlin
// data/supabase/Supabase.kt  ← Networking/SupabaseManager.swift
val JewelJson = Json {
    ignoreUnknownKeys = true      // PostgREST returns more columns than we model
    coerceInputValues = true      // null → default for non-null fields with defaults (e.g. generated_image_urls → [])
    explicitNulls = false         // omit null fields when ENCODING (matches Swift's encodeIfPresent)
    isLenient = true
}

fun createJewelSupabase() = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
) {
    defaultSerializer = KotlinXSerializer(JewelJson)
    httpEngine = OkHttp.create { /* 30 s request timeout, 120 s call timeout, as JewelNetwork.supabaseSession */ }
    install(Auth) {
        scheme = "jewelindia"
        host = "auth"                       // matches jewelindia://auth/callback
        flowType = FlowType.PKCE            // supabase-swift's default; recovery + OAuth links carry ?code=
        alwaysAutoRefresh = true
        autoLoadFromStorage = true
    }
    install(Postgrest)
    install(Storage)
    install(Functions)
}

val projectRef: String get() = Uri.parse(BuildConfig.SUPABASE_URL).host!!.substringBefore('.')
```

Query translation cheat sheet (supabase-swift → supabase-kt):

| Swift | Kotlin |
|---|---|
| `.from("t").select("a, b").eq("c", value: v).limit(1).execute().value` → `[Row]` | `supabase.from("t").select(Columns.raw("a, b")) { filter { eq("c", v) }; limit(1) }.decodeList<Row>()` |
| `.single()` | `.decodeSingle<Row>()` (throws if 0 or >1 rows) |
| `.maybeSingle()` | `.decodeSingleOrNull<Row>()` |
| `.select("*", head: true, count: .exact)` + `response.count` | `select(head = true) { count(Count.EXACT); filter { … } }.countOrNull() ?: 0` |
| `.select("*", count: .exact)` + range | `select { count(Count.EXACT); filter{…}; order("created_at", Order.DESCENDING); range(from, to) }` then `.decodeList()` and `.countOrNull()` |
| `.in("col", values: [...])` | `filter { isIn("col", values) }` |
| `.is("accepted_at", value: nil)` | `filter { exact("accepted_at", null) }` |
| `.gt("expires_at", value: now)` | `filter { gt("expires_at", now) }` |
| `.order("created_at", ascending: false)` | `order("created_at", Order.DESCENDING)` |
| `.range(from:to:)` | `range(from.toLong(), to.toLong())` |
| `.insert(row).select("…").single()` | `insert(row) { select(Columns.raw("…")) }.decodeSingle<R>()` |
| `.update(patch).eq("id", value: id)` | `update(patch) { filter { eq("id", id) } }` |
| `.delete().eq("id", value: id).select("id")` | `delete { select(Columns.raw("id")); filter { eq("id", id) } }.decodeList<R>()` |
| embedded filter `.eq("messages.sender_type", value: "employee")` | `filter { eq("messages.sender_type", "employee") }` with `Columns.raw("id, messages!inner(id)")` |
| `.rpc("fn", params: [...]).execute().value` | `supabase.postgrest.rpc("fn", buildJsonObject { put("p_x", v) }).decodeAs<T>()` |
| `.rpc("fn").execute()` (no result) | `supabase.postgrest.rpc("fn")` |
| `storage.from(b).upload(path, data:, options: FileOptions(contentType:, upsert:))` | `supabase.storage.from(b).upload(path, bytes) { upsert = true; contentType = ContentType.parse(mime) }` |
| `getPublicURL(path:)` | `supabase.storage.from(b).publicUrl(path)` |
| `createSignedURL(path:expiresIn:)` | `supabase.storage.from(b).createSignedUrl(path, expiresIn = 3600.seconds)` |
| `remove(paths:)` | `supabase.storage.from(b).delete(paths)` |
| `auth.signIn(email:password:)` / `(phone:)` | `auth.signInWith(Email) { email = …; password = … }` / `auth.signInWith(Phone) { phone = …; password = … }` |
| `auth.signInWithIdToken(OpenIDConnectCredentials(provider: .google, idToken:, accessToken:, nonce:))` | `auth.signInWith(IDToken) { idToken = …; provider = Google; nonce = rawNonce }` |
| `auth.setSession(accessToken:refreshToken:)` | `auth.importAuthToken(accessToken, refreshToken, retrieveUser = true, autoRefresh = true)` |
| `auth.update(user: UserAttributes(password:))` | `auth.updateUser { password = … }` |
| `auth.update(user: UserAttributes(data: [...]))` | `auth.updateUser { data = buildJsonObject { … } }` |
| `auth.user()` (refresh user) | `auth.retrieveUserForCurrentSession(updateSession = true)` |
| `auth.resetPasswordForEmail(email, redirectTo:)` | `auth.resetPasswordForEmail(email, redirectUrl = "jewelindia://auth/callback?next=/update-password")` |
| `auth.signOut()` | `auth.signOut()` |
| `auth.currentSession` | `auth.currentSessionOrNull()` |
| `auth.session` (async, refreshes if expired) | `AuthToken.fresh()` (§7.5) |
| `auth.session(from: url)` | `supabase.handleDeeplinks(intent)` |
| `user.userMetadata["role"]?.stringValue` | `user.userMetadata?.get("role")?.jsonPrimitive?.contentOrNull` |

Exact method names in supabase-kt can drift between minor versions. If one of these doesn't compile, check the supabase-kt 3.x docs/KDoc for the current name and keep the semantics.

### 7.5 HTTP for the Site API, pipeline and edge functions

```kotlin
// data/net/HttpClients.kt
class HttpClients(app: Application) {
    /** Site API. Keeps cookies like a browser, because the signup sequence relies on
     *  Supabase SSR cookies the Next server sets (JewelAPI.session in iOS). In-memory jar. */
    val site: OkHttpClient
    /** AI pipeline: 120 s call timeout for /process uploads. */
    val pipeline: OkHttpClient
    /** Everything else (HEAD probes, image search, edge functions): 30 s. */
    val plain: OkHttpClient
}
```

- Base URLs: `AppConfig.siteUrl`, `AppConfig.aiPipelineUrl`, `AppConfig.supabaseUrl`. Build URLs with `HttpUrl` (`toHttpUrl().newBuilder().addPathSegments(…)`). Don't concatenate strings, which breaks on trailing slashes.
- **Fresh token helper** (`data/net/AuthToken.kt`): iOS calls `try await auth.session` before every authenticated HTTP call because it refreshes an expired JWT. Port as:
  ```kotlin
  suspend fun fresh(): String {
      val s = auth.currentSessionOrNull() ?: throw ApiError(401, "Session expired. Please sign in again.")
      if (s.expiresAt <= Clock.System.now() + 60.seconds) auth.refreshCurrentSession()
      return auth.currentSessionOrNull()?.accessToken ?: throw ApiError(401, "Session expired. Please sign in again.")
  }
  ```
  Each API file keeps its own iOS copy for the "no session" message (ChamakAPI, CreditsAPI and others each have their own wording). Catch the 401 and rethrow with that file's message.
- **Error shape** (`ApiError`): `status`, `message`, plus the send-OTP metadata `locked`, `lockedUntil`, `cooldown`, `waitSeconds`. Parse exactly as `JewelAPI.throwIfError`. For pipeline errors, FastAPI puts the reason in `detail` (a string or an object with `message`/`short_by`). Parse as `WholesalerAPI.processProduct` does, including the 402 insufficient-credits branch and the 429 daily-limit fallback.
- **Edge functions** (`EdgeFunctions.kt`): POST `"$SUPABASE_URL/functions/v1/$name"` with headers `Authorization: Bearer <fresh token>`, `apikey: <anon key>`, `Content-Type: application/json`. On non-2xx, decode the body for `message` (credits-topup, staff-accounts) or `error` (delete-account) and throw with that text, falling back to the iOS fallback strings. Use this instead of `supabase.functions.invoke` so error bodies are always readable.
- **Retry** (`JewelNetwork.withRetry`): 3 attempts, 400 ms initial delay, doubling. Retry only *transient transport* failures: `SocketTimeoutException`, `ConnectException`, `UnknownHostException`, `SSLHandshakeException`, `java.io.InterruptedIOException`, `okhttp3` stream resets (`StreamResetException`), and supabase-kt's `HttpRequestException` when its cause is one of those. Never retry a 4xx/5xx. Use it exactly where iOS does. The complete list of call sites is: `Networking/WholesalerAPI.swift` lines ~172, 200, 208, 454 (`updateProductFlags`, `updateProduct`, `deleteProduct`, `clearProductImages`); `Networking/ChamakAPI.swift` ~192, 321, 420, 548 (generation insert, form update, set update, feedback insert); `Features/Wholesaler/AddProductForm.swift` ~326, 373 (product claim / draft save); `Features/Onboarding/OnboardFlow.swift` ~228, 265; `Features/RetailerOnboarding/RetailerOnboardFlow.swift` ~201, 227 (onboarding row writes).
- **Multipart**: OkHttp `MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(…)`. Field names and order as iOS (`title`, `jewellery_type`, `wholesaler_id`, `image_count`, then `file`).
- **Idempotency**: send the `Idempotency-Key` header wherever iOS does (`/process`, Chamak generate, manufacturing create/accept). Keep keys stable across retries of the *same* user action (store the key in the ViewModel and clear it on success), exactly like `submissionKey` in `AddProductForm.swift`.

### 7.6 JSON models

- Every Swift `Codable` struct becomes a `@Serializable data class` with `@SerialName("snake_case")` on every field whose Swift `CodingKeys` maps a name.
- Swift `decodeIfPresent(...) ?? default` → a Kotlin default value (with `coerceInputValues = true`, null also becomes the default).
- Swift custom `init(from:)` that does real work (for example `PaginatedMarketplaceResponse` detecting the legacy contract, `CreditWallet` tolerating a short object, `RetailerDesign.number(…)` accepting a number *or* a numeric string, `PlanStatus` parsing microsecond timestamps): write a custom `KSerializer`, or decode into a raw DTO and map it. Port the logic exactly and unit-test it (§16d).
- Dates: keep them as `String` in DTOs and parse them at the edge with `JewelDates.parse(s)`, which accepts ISO-8601 with or without fractional seconds and with microseconds (`java.time.OffsetDateTime.parse` handles all of these). Return `java.time.Instant`.
- UUIDs: use `String` everywhere. The iOS app uses `UUID` in some places and `.uuidString` (UPPERCASE) when it sends them. **Postgres compares uuid columns case-insensitively, but text columns don't.** Where iOS sends `uuidString` into a text comparison, Android must send the same case. When in doubt, send lowercase only if iOS lowercases (`LikeBook` lowercases product ids; `ChamakAPI` lowercases storage paths, see its comments).
- Encoding PATCH/INSERT bodies: `explicitNulls = false` omits null fields, which matches Swift's synthesized `Encodable`. If a body must **send** `null` to clear a column, build it with `buildJsonObject { put("col", JsonNull) }`. **Known iOS discrepancy:** `WholesalerAPI.clearProductImages` declares `processed_image_url: String?` = nil, which Swift's synthesized encoder *omits*, so iOS never actually clears that column. The comment says the intent is to clear it. On Android, send an explicit `JsonNull` (intent wins), and record this in `PORT_STATUS.md`.

### 7.7 Images

- `JewelImageLoader.kt` builds one Coil `ImageLoader` (set as the singleton via `SingletonImageLoader.setSafe`): OkHttp network fetcher, memory cache 25% of app memory, disk cache `cacheDir/JewelImages` at 250 MB, crossfade 150 ms, `SvgDecoder.Factory()` registered.
- `CachedImage(url, contentScale, modifier)` = `AsyncImage` with the same placeholder/shimmer the iOS views use (`ShimmerModifier` in `FormComponents.swift`).
- Product images: always go through `Product.displayImageUrl(size)` / `thumbnailUrls(size)` / `url(original, size)` (port them exactly from `WholesalerModels.swift`, lines ~258–315). They pick the `image_variants` card/detail/full copy when present and fall back to the original. Don't read `image_url` and friends directly in screens.
- On sign-out, clear the Coil memory and disk caches (iOS: `ImageCache.shared.clear()`).
- **Upload normalisation** (`ImageNormalizer.kt` ← `Core/ImageNormalizer.swift`): every picked or captured image is decoded with `ImageDecoder` (API 28+) or `BitmapFactory` with `inSampleSize` (26–27), EXIF-rotated (`androidx.exifinterface`), and downscaled so its long edge is at most `maxProductDimension = 2048` px for product/design photos or `maxDocumentDimension = 1200` px for onboarding documents. It is then re-encoded as JPEG starting at quality 0.85 (85), stepping quality down until the bytes fit `maxUploadBytes = 8 * 1024 * 1024`. Port the exact step-down loop from the Swift file. MIME type `image/jpeg`, filename `*.jpg`. This matters for the same reason as on iOS: phone photos are often HEIC and 3–12 MB, and the pipeline has a 10 MB cap.

### 7.8 Strings

- `core/text/Copy.kt` is a Kotlin `object Copy` holding **every** constant from `Core/Copy.swift`, with the same names (`Copy.genericFailure`, `Copy.WholesalerTab.home`, …), copied **verbatim** (same punctuation, ellipses, en-dashes, curly quotes). Nested Swift enums become nested `object`s. Functions stay functions.
- Strings that iOS writes inline inside views stay inline in the Kotlin composable, copied verbatim. Don't move them into `strings.xml`. There's no localisation requirement, and verbatim inline copy keeps the iOS ↔ Android diff mechanical. (`app_name` is the one entry in `strings.xml`, because the launcher needs it.)

### 7.9 Local storage keys (`Prefs`)

Port these keys exactly (names are shared with nothing else, but keeping them identical makes debugging across platforms easy):

| Key | Type | Owner (iOS file) |
|---|---|---|
| `jewel_view_mode.<userId>` | `"employee"` / `"retailer"` | `Core/AuthRouter.swift` (ViewModeStore) |
| `auth_identity` | String | `Core/SignupFlow.swift` |
| `referral_code` | String | SignupFlow |
| `referral_role` | String | SignupFlow |
| `signup_role` | String | SignupFlow |
| `referral_wholesaler` | String | SignupFlow |
| `otp_sent_at` | epoch millis (Long) | SignupFlow |
| `otp_remaining_resends` | Int | SignupFlow |
| `otp_locked_until` | epoch millis (Long) | SignupFlow |
| `employee_orders_last_checked.<seedId>` | epoch millis | `Networking/EmployeeAPI.swift` (`EmployeeSession.storageKey`), read/written in `Features/Employee/EmployeeStore.swift` |
| `jewel_store_theme` | String (theme id) | `Features/Retailer/StoreThemeView.swift` (`storageKey`) and `Networking/EmployeeAPI.swift` (`EmployeeTheme.localKey`), the same key shared by both |
| `jewel_employee_style` | String (`EmployeeStyle` raw value) | `Features/Employee/EmployeeAppearance.swift` (`preferenceKey`, an `@AppStorage` in `EmployeeShell` and `EmployeeAppearanceChrome`) |
| `jewel_employee_sarvam` | Boolean (legacy, read only) | `EmployeeAppearance.legacyPreferenceKey`. Read it as a fallback exactly as iOS does. |

If you find another `UserDefaults`/`@AppStorage` key while porting, add it here and to `Prefs`.

Ignore the `Jewel*` keys listed only in `Navigation/DebugScreenPeek.swift` and the employee fixture flags. They are DEBUG preview harness inputs.

### 7.10 Theming

- `JewelTheme { }` wraps `MaterialTheme` with a light `ColorScheme` built from `Palette` (`background = Palette.background`, `surface = Palette.light`, `onBackground/onSurface = Palette.foreground`, `primary = Palette.dark`, `onPrimary = Palette.light`, `outline = Palette.border`, `error = Palette.statusRejected`). This only gives sensible defaults to stock components. Screens style themselves explicitly from `Palette`/`Typography`, like the iOS views do.
- No dark mode (iOS forces Light). Set `android:forceDarkAllowed="false"` and use a light-only theme.
- Status bar: dark icons on light background (`enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(…))`). Pad content with `WindowInsets.safeDrawing` / `Modifier.windowInsetsPadding` the way iOS uses safe areas.
- Role themes: `RoleTheme.Wholesaler` (Cirka headings / Manrope body), `Retailer` (Satoshi Bold / Satoshi), `Employee` (Gilda Display / Manrope), exactly as `Design/Typography.swift`. Plus the employee personal styles in `Features/Employee/EmployeeAppearance.swift` (§16b). Provide the active role theme via a `LocalRoleTheme` CompositionLocal set by each shell.

---

# Part C — Backend contract

## §8 Every backend call the app makes

This is the complete surface, extracted from the iOS sources. The Android app calls **exactly these** with **exactly the same** parameters, and nothing else. The "iOS ref" column tells you where to read the precise request and response shape. Port the Swift DTO structs from that file field for field.

### 8.1 Supabase tables (PostgREST)

| Table | Operations | iOS ref |
|---|---|---|
| `profiles` | select `role` by `id` | `Core/AuthRouter.swift` |
| `wholesalers` | select (by `user_id`, by `email`), update `has_visited_dashboard`, upsert (onboarding) | `AuthRouter`, `Networking/WholesalerAPI.swift`, `Features/Onboarding/OnboardFlow.swift`, `Features/Onboarding/OnboardSubmittedView.swift`, `Features/Profile/ProfileViews.swift`, `Networking/JewelAPI.swift` |
| `retailers` | select `verification_status`, `id`, `selected_theme`; update `has_visited_dashboard`, `selected_theme`; upsert (onboarding) | `AuthRouter`, `Networking/RetailerAPI.swift`, `Networking/EmployeeAPI.swift`, `Features/RetailerOnboarding/RetailerOnboardFlow.swift`, `RetailerOnboardSubmittedView.swift` |
| `employees` | select `status` by `auth_user_id`; list the store's staff | `Core/SessionStore.swift`, `AuthRouter`, `Networking/EmployeeAPI.swift`, `Networking/StaffAccountsAPI.swift` |
| `products` | select (catalogue with count + range, by id, by ids, by wholesaler), update (flags, full edit, clear images), delete | `Networking/WholesalerAPI.swift`, `ChamakAPI.swift`, `EmployeeAPI.swift`, `WishlistAPI.swift` |
| `orders` | head count (pending), select (staff orders, latest update) | `WholesalerAPI.swift`, `OrdersChatAPI.swift`, `EmployeeAPI.swift` |
| `conversations` | select with `messages!inner(id)` join for unread counts | `WholesalerAPI.swift`, `EmployeeAPI.swift` |
| `messages` | select by `conversation_id` | `OrdersChatAPI.swift` (ChatAPI) |
| `referral_links` | select, insert (fallback code generation), update `is_active` | `JewelAPI.swift` |
| `retailer_designs` | select, insert, delete | `RetailerAPI.swift`, `EmployeeAPI.swift` |
| `retailer_selections` | select `product_id` | `EmployeeAPI.swift`, `ChamakAPI.swift` |
| `retailer_customers` | select, insert, delete | `WishlistAPI.swift` |
| `customer_boards` | select (with items), insert, delete | `WishlistAPI.swift` |
| `customer_board_items` | select, insert, delete | `WishlistAPI.swift`, `ChamakAPI.swift` |
| `product_likes` | select `product_id` | `LikesAPI.swift` |
| `chamak_generations` | select, insert, update | `ChamakAPI.swift` |
| `chamak_feedback` | insert | `ChamakAPI.swift` |
| `credit_prices` | select active, ordered by `sort_order` | `CreditsAPI.swift` |
| `credit_purchases` | select by `provider_ref` | `CreditsAPI.swift` |
| `entitlements` | select `entitlement_key, expires_at` | `CreditsAPI.swift` |
| `plans` | select `key, label, period_days, perks` | `CreditsAPI.swift` |
| `store_events` | insert (fire-and-forget) | `StoreActivity.swift` |

### 8.2 RPCs

| RPC | Params | Returns | iOS ref |
|---|---|---|---|
| `set_my_role` | `p_role` | void | `SessionStore.setUserRole`, `JewelAPI.setPassword` |
| `claim_staff_invite` | – | void | `SessionStore.claimStaffInvite` |
| `validate_referral_code` | `p_code` | answer object | `InviteAPI.swift` |
| `attach_referral_code` | `p_code` (trimmed, UPPERCASED) | void | `InviteAPI.swift` |
| `credits_wallet` | – | `CreditWallet` (`ok`, `error_code`, …) | `CreditsAPI.swift`, `TreasureChestModels.swift` |
| `credits_history` | `p_limit`, `p_before`, `p_before_id` | `{ok, data: [CreditLedgerEntry]}` | `CreditsAPI.swift` |
| `purchase_entitlement` | `p_key` | `EntitlementPurchase` | `CreditsAPI.swift` |
| `purchase_export` | `p_generation` | `EntitlementPurchase` | `CreditsAPI.swift` |
| `my_plan` | – | `PlanStatus` | `CreditsAPI.swift` |
| `subscribe_plan` | `p_plan` | `PlanPurchase` | `CreditsAPI.swift` |
| `set_plan_auto_renew` | `p_on` | void | `CreditsAPI.swift` |
| `wholesaler_orders` | – | `[WholesalerOrder]` | `OrdersChatAPI.swift` |
| `order_set_status` | `p_order`, `p_status`, `p_reason` | reply object | `OrdersChatAPI.swift` |
| `chat_threads` | – | `[ChatThread]` | `OrdersChatAPI.swift` |
| `chat_open` | `p_product` | conversation id | `OrdersChatAPI.swift` |
| `chat_send` | `p_conversation`, `p_content` | reply | `OrdersChatAPI.swift` |
| `chat_mark_read` | `p_conversation` | void | `OrdersChatAPI.swift` |
| `toggle_product_like` | `p_product` | `{ok, liked}` | `LikesAPI.swift` |
| `employee_store_profile` | – | store profile rows | `EmployeeAPI.swift` |
| `wholesaler_report` | period param (see file) | `WholesalerReport` | `Features/Wholesaler/Report/WholesalerReport.swift` |
| `register_device` | `p_device_id`, `p_model`, `p_app_version` | void (fire-and-forget) | `StoreActivity.swift` |

The chat **report** call in `ChatAPI.report(…)` and the RPC names it uses are in `OrdersChatAPI.swift` (around line 274). Port it from there.

Android specifics for `register_device`: `p_device_id` = a random UUID generated once and stored in `Prefs` (`device_id`). This is the equivalent of `identifierForVendor`, which also resets on reinstall. Don't use `ANDROID_ID`. `p_model` = `Build.MODEL`. `p_app_version` = `"${BuildConfig.VERSION_NAME} ${BuildConfig.VERSION_CODE}"`.

### 8.3 Storage buckets

| Bucket | Visibility | Use | iOS ref |
|---|---|---|---|
| `plant-images` | public | Product photos, edit-product custom/showcase images, Chamak source uploads, delete on product delete, HEAD-probe `products/temp/reve_<productId>.png` during reprocess | `WholesalerAPI.swift`, `AddProductForm.swift`, `EditProductView.swift`, `ChamakAPI.swift` |
| `retailer-designs` | public | Store's own designs | `RetailerAPI.swift` |
| `chamak-outputs` | private | Chamak results. Always use **signed URLs** (`ChamakAPI.getSignedURL(s)`). | `ChamakAPI.swift` |
| `aadhaar-documents`, `pan-documents`, `gst-documents`, `business-logos` | policy-gated | Onboarding documents, path `<uid>/<kind>-<stamp>` | `OnboardFlow.swift`, `RetailerOnboardFlow.swift` |

**Storage path case:** storage policies compare `auth.uid()::text` (lowercase) against the first path folder. iOS has to lowercase `uuidString`. On Android `user.id` from supabase-kt is already lowercase. Still, call `.lowercase()` on every uid you put in a storage path, so this can never regress.

### 8.4 Edge functions (POST `SUPABASE_URL/functions/v1/<name>`, see §7.5)

| Function | Body | Used for | iOS ref |
|---|---|---|---|
| `credits-topup` | `{"action":"options"}` / `{"action":"create","pack_key":…,"amount_inr":…}` | Not used by live iOS UI (purchases retired) → **don't port** | `CreditsAPI.swift` |
| `credits-topup` | `{"action":"onboarding_status"}` → `{amount_inr, required, payable, paid}` | Onboarding fee check | `Networking/OnboardingFeeAPI.swift` |
| `credits-topup` | `{"action":"onboarding_pay"}` → `{paid?, link_id?, url?}` | Razorpay payment link for the onboarding fee | `OnboardingFeeAPI.swift`, `Features/Onboarding/OnboardingFeeSheet.swift` |
| `credits-topup` | `{"action":"onboarding_confirm","link_id":…}` → `{paid}` | Confirm after the browser closes | same |
| `staff-accounts` | see `StaffAccountsAPI.Request` | Create staff login, invite Google staff, reset password, activate/deactivate, remove, suggest username | `Networking/StaffAccountsAPI.swift` |
| `delete-account` | empty | Account deletion (wholesaler and retailer menus) | `WholesalerAPI.swift` (`AccountAPI`) |
| `apple-iap` | – | **Not on Android** | – |

### 8.5 Site API (`SITE_URL`)

All JSON. "Auth" means `Authorization: Bearer <fresh token>`. iOS ref is `Networking/JewelAPI.swift` unless noted.

| Method + path | Auth | Body / query | Response | Notes |
|---|---|---|---|---|
| POST `/api/auth/check-user` | no | `{identity}` | `{exists, provider?, isEmail, isPhone}` | |
| POST `/api/auth/send-otp` | no | `{identity}` | `{success, remainingResends?, lastSentAt?}`; 429 carries `locked`, `lockedUntil`, `cooldown`, `waitSeconds` | Drives the OTP lockout UI. |
| POST `/api/auth/verify-otp` | no | `{identity, token, referralCode: string\|null}` | `{success, userId?, isNewUser, userRole?}` **+ `Set-Cookie` with the session** | Read the cookies with `SsrSessionBridge` → `auth.importAuthToken`. §12.4. |
| GET `/api/referral/manage?page=N` | yes | – | `InvitationSettings` | 404/405 → "service not updated" copy, then DB fallback (see Swift). |
| POST `/api/referral/manage` | yes | `{skip_guide}` | `InvitationSettings` | |
| DELETE `/api/referral/manage` | yes | `{id}` | – | Falls back to `referral_links.update(is_active=false)`. |
| POST `/api/referral/generate` | yes | `{source, gift_credits, idempotency_key}` | `RetailerInvitation` | **Android sends `source: "web"`, not `"android"`.** The route rejects anything except `ios`/`web` with a 400 (`Jewel-India-Frontend/app/api/referral/generate/route.js` line 12), and a 400 would silently drop into the DB-insert fallback. The fallback insert into `referral_links` also uses `source: "web"`. See §18 D9. Port the DB-insert fallback as written. |
| POST `/api/referral/claim` | yes | `{code}` | `{success, replayed?}` | After the retailer onboarding row exists. |
| GET `/api/retailer/marketplace` | yes | – | `{products, selected_product_ids}` | Legacy full catalogue. |
| GET `/api/retailer/marketplace?limit=&version=v2&category=&search=&cursor=` | yes | – | `{products, selected_product_ids, has_more, next_cursor, page_size}`, or legacy shape | Detect the legacy contract exactly as `PaginatedMarketplaceResponse.init(from:)`. Then filter client-side (`filteringLegacy`). |
| GET `/api/retailer/marketplace/categories` | yes | – | `{categories:[{id,name,count}]}` | |
| POST `/api/retailer/marketplace/hydrate` | yes | `{product_ids}` | `{products, selected_product_ids}` | 404 → fall back to full catalogue filter. |
| GET `/api/retailer/marketplace/<id>` | yes | – | `{product, is_selected}` | 404 → fallback. |
| POST `/api/retailer/your-taste` | yes | `{product_id, selected}` | `{success}` | |
| POST `/api/orders/create` | yes | `{items:[{product_id, quantity, customization_notes}]}` (max 20) | `{success, data:[Order], suppliers:{id: SupplierSummary}}` | |
| GET `/api/retailer/orders` | yes | – | `{orders, products, suppliers}` | |
| POST `/api/wishlist-shares` | yes | `{board_id, max_viewers, duration_minutes}` | `WishlistShare` | `Networking/WishlistAPI.swift` |
| GET `/api/wishlist-shares?board_id=` | yes | – | `{shares}` | WishlistAPI |
| POST `/api/wishlist-shares/<id>/revoke` | yes | `{}` | `{ok}` | WishlistAPI |

`/api/auth/set-password` and `/api/auth/toggle-view` are **not** called. iOS does those on-device (`JewelAPI.setPassword`, `ViewModeStore`). Do the same.

### 8.6 AI pipeline (`AI_PIPELINE_URL`)

| Method + path | Auth | Body | Notes / iOS ref |
|---|---|---|---|
| GET `/api/upload-usage?wholesaler_id=` | no | – | Failures fall back to "unknown/unlimited". `WholesalerAPI.fetchUploadUsage` |
| POST `/process` | yes + `Idempotency-Key` | multipart `title, jewellery_type, wholesaler_id, image_count, file` | 120 s timeout. 402 → insufficient credits (`detail.short_by`). `WholesalerAPI.processProduct` |
| POST `/process/<productId>` | no auth header in iOS | multipart `{wholesaler_id, file}` or JSON `{wholesaler_id}` | Reprocess. 429 → daily limit copy. `WholesalerAPI.reprocessProduct` |
| GET `/product/<id>` | no | – | Poll every 5 s, timeout 300 s, take first 4 `generated_image_urls`. `WholesalerAPI.reprocessPollTick` |
| HEAD `SUPABASE_URL/storage/v1/object/public/plant-images/products/temp/reve_<id>.png` | no | – | "Background removed" signal during reprocess |
| POST `/api/chamak/analyze` | yes | see `ChamakAPI.triggerStage1Analysis` | Stage 1 |
| POST `/api/chamak/generate-v2` | yes + idempotency | see `ChamakAPI.submitFormAndGenerate` | Combine / Fusion |
| POST `/api/set-creation/generate` | yes + idempotency | see `ChamakAPI.submitSetAndGenerate` | Set creation |
| POST `/api/retailer/image-search` | yes | multipart photo | `Features/Retailer/CatalogueImageSearch.swift`, `wholesaler ios/docs/retailer-image-search.md` |
| POST `/api/retailer/manufacturing-assets` | yes | multipart image | `Networking/ManufacturingAPI.swift` |
| POST `/api/retailer/manufacturing-requests` | yes + idempotency | `CreateManufacturingRequestParams` | ManufacturingAPI |
| GET `/api/retailer/manufacturing-requests?status=` | yes | – | ManufacturingAPI |
| GET `/api/retailer/manufacturing-requests/<id>` | yes | – | ManufacturingAPI |
| POST `/api/retailer/manufacturing-requests/<id>/cancel` | yes | – | ManufacturingAPI |
| POST `/api/retailer/manufacturing-requests/<id>/award` | yes | `{quote_id}` | ManufacturingAPI |
| GET `/api/wholesaler/manufacturing-offers?status=` | yes | – | ManufacturingAPI |
| GET `/api/wholesaler/manufacturing-offers/<id>` | yes | – | ManufacturingAPI |
| POST `/api/wholesaler/manufacturing-offers/<id>/accept` or `/quotes` (parallel mode) | yes + idempotency | `AcceptOfferQuoteParams` | ManufacturingAPI |
| POST `/api/wholesaler/manufacturing-offers/<id>/decline` | yes | `{reason?}` | ManufacturingAPI |
| POST `/api/devices/register` | yes | `{device_token, environment}` | **Not used in v1** (§18 D6) |

The `JEWEL_MFG_TEST_URL` loopback harness in `ManufacturingAPI.swift` is DEBUG-simulator-only. Don't port it.

---

# Part D — Build phases

Each phase lists **Goal → Files → How → Gotchas → Acceptance criteria**. Do them in order.

## §9 Phase 0 — Scaffold, build, toolchain

**Goal:** an empty Compose app called "Jewel India" that builds from the command line and shows a blank `#FEFEFE` screen with the text "Jewel India" in Cirka 32. This is the iOS `LaunchView`.

### 9.1 Toolchain commands

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="/Users/parashrautela/Library/Android/sdk"
cd "/Users/parashrautela/Documents/jewel india /jewel india android app "
```

No `gradle` is on the PATH. Bootstrap the wrapper with a Gradle distribution that is already in the local cache, then let the wrapper fetch 9.8.1:

```bash
GRADLE_BIN="$(find ~/.gradle/wrapper/dists/gradle-9.3.1-bin -path '*/bin/gradle' -type f | head -1)"
"$GRADLE_BIN" wrapper --gradle-version 9.8.1 --distribution-type bin
./gradlew --version
```

Run that `wrapper` command **after** writing `settings.gradle.kts` (an empty one is fine) so Gradle treats the folder as a build. If Gradle complains that the plugins in `build.gradle.kts` can't be resolved during wrapper generation, temporarily leave `build.gradle.kts` empty, generate the wrapper, then restore it.

### 9.2 Files to create

**`.gitignore`**
```
.gradle/
build/
app/build/
local.properties
secrets.properties
TEST_ACCOUNTS.local.md
*.keystore
*.jks
keystore.properties
.idea/
*.iml
.DS_Store
captures/
.kotlin/
```

**`secrets.example.properties`** (committed)
```
# Copy to secrets.properties and fill in. Values come from
# "wholesaler ios/Config.xcconfig" (replace "/$()/" with "//").
SUPABASE_URL=
SUPABASE_ANON_KEY=
AI_PIPELINE_URL=
SITE_URL=
# Optional. Empty = Google sign-in uses the browser flow.
GOOGLE_WEB_CLIENT_ID=
```

**`secrets.properties`** (NOT committed): the same keys with real values copied from `wholesaler ios/Config.xcconfig`. Don't echo the values anywhere.

**`local.properties`**: `sdk.dir=/Users/parashrautela/Library/Android/sdk`

**`settings.gradle.kts`**
```kotlin
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "JewelIndiaAndroid"   // explicit, so the folder name (with spaces) never matters
include(":app")
```

**`build.gradle.kts`** (root)
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
```

**`gradle.properties`**
```
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

**`app/build.gradle.kts`**
```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val secrets = Properties().apply {
    val f = rootProject.file("secrets.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String, required: Boolean = true): String {
    val v = secrets.getProperty(key)?.trim().orEmpty()
    if (required && v.isEmpty()) throw GradleException("secrets.properties is missing $key (see secrets.example.properties)")
    return v
}

android {
    namespace = "com.jewelindia.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jewelindia.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "SUPABASE_URL", "\"${secret("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${secret("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "AI_PIPELINE_URL", "\"${secret("AI_PIPELINE_URL")}\"")
        buildConfigField("String", "SITE_URL", "\"${secret("SITE_URL")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${secret("GOOGLE_WEB_CLIENT_ID", required = false)}\"")
        manifestPlaceholders["authScheme"] = "jewelindia"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // signingConfig: see §16d.4. Left unset until the founder provides a keystore.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.okhttp)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)

    implementation(libs.androidx.browser)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.googleid)
    implementation(libs.androidx.exifinterface)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

**`app/proguard-rules.pro`**: keep rules for kotlinx.serialization (`-keepattributes *Annotation*, InnerClasses`, keep `@Serializable` classes' `Companion`/`serializer()`), Ktor/OkHttp warnings (`-dontwarn org.slf4j.**`, `-dontwarn io.ktor.**` only if R8 asks), and supabase-kt. Add rules **only** in response to actual R8 warnings from a release build, each with a comment saying why.

**`app/src/main/AndroidManifest.xml`**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <queries>
        <!-- Custom Tabs provider discovery (API 30+ package visibility) -->
        <intent><action android:name="android.support.customtabs.action.CustomTabsService" /></intent>
    </queries>

    <application
        android:name=".JewelApplication"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:theme="@style/Theme.JewelIndia"
        android:allowBackup="true"
        android:fullBackupContent="@xml/backup_rules"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:screenOrientation="portrait"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <!-- Supabase auth callbacks: jewelindia://auth/callback (OAuth, password reset) -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="${authScheme}" android:host="auth" />
            </intent-filter>
            <!-- Retailer invitations: https://app.jewelindia.shop/join/<code> (iOS: applinks:app.jewelindia.shop) -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="https" android:host="app.jewelindia.shop" android:pathPrefix="/join/" />
            </intent-filter>
        </activity>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
```

Why portrait only: iOS declares portrait-only on iPhone. Tablet (iPad) orientations are out of scope for v1 (§18 D3).

**`res/xml/file_paths.xml`**: `<cache-path name="camera" path="camera/" />` and `<cache-path name="exports" path="exports/" />`.

**`res/xml/backup_rules.xml` / `data_extraction_rules.xml`**: exclude `sharedpref` files that hold the Supabase session and `jewel_prefs`. A restored session token on a new device must never auto-sign-in.

**`res/values/themes.xml`**: `Theme.JewelIndia` parent `android:Theme.Material.Light.NoActionBar`, `android:windowBackground` = `@color/launch_background` (`#FEFEFE`, from `Assets.xcassets/LaunchBackground.colorset`), status bar light. Optionally add the AndroidX SplashScreen with the same background and no icon animation.

**`JewelApplication.kt`**: creates `AppGraph`, installs the Coil singleton image loader (§7.7).

**`MainActivity.kt`** (Phase 0 version): `enableEdgeToEdge()`, `setContent { JewelTheme { LaunchScreen() } }`.

**`README.md`**: prerequisites, how to fill `secrets.properties`, build/run commands, where the plan and status files are.

### 9.3 Local git

```bash
git init
git add -A
git commit -m "Phase 0: scaffold Jewel India Android project"
```

Never add a remote.

### 9.4 Acceptance criteria

- `./gradlew :app:assembleDebug` succeeds. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew :app:testDebugUnitTest` runs (zero tests is fine).
- `secrets.properties`, `local.properties` and `.idea/` are not tracked by git (`git status --ignored` shows them ignored).
- If an emulator is available (`$ANDROID_HOME/emulator/emulator -list-avds`), install and launch: `adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.jewelindia.app/.MainActivity`, then take a screenshot with `adb exec-out screencap -p > /tmp/phase0.png` and look at it. If no AVD exists and a system image is installed under `$ANDROID_HOME/system-images`, create one with `avdmanager create avd -n jewel_test -k "<image>" -d pixel_7`. If nothing is installed, skip on-device checks and say so in `PORT_STATUS.md`.

## §10 Phase 1 — Design system and core utilities

**Goal:** every token, font, asset and pure-logic helper exists and is unit-tested, so later phases only compose.

### 10.1 Assets (`tools/copy_assets.sh`)

Write a script, run it, and commit both the script and its output. It must:

1. Copy the 24 fonts from `wholesaler ios/JewelIndia/Resources/Fonts/` to `app/src/main/res/font/`, renamed per §A2.1 (lowercase, underscores, no hyphens: an Android resource requirement).
2. For every `*.imageset` in `wholesaler ios/JewelIndia/Resources/Assets.xcassets/`:
   - If it contains a `.svg`, copy it to `app/src/main/assets/svg/<SetName>.svg`.
   - Otherwise copy the **largest** PNG/JPG (the `@3x` one if present; check `Contents.json`) to `app/src/main/res/drawable-nodpi/<snake_case_set_name>.<ext>`.
3. Generate launcher icons from `AppIcon.appiconset/AppIcon-1024.png` with `tools/make_launcher_icons.sh` using macOS `sips`:
   - Legacy: `mipmap-mdpi` 48, `hdpi` 72, `xhdpi` 96, `xxhdpi` 144, `xxxhdpi` 192 (`ic_launcher.png` and `ic_launcher_round.png`).
   - Adaptive (`mipmap-anydpi-v26/ic_launcher.xml`): background `@color/launch_background`; foreground = the 1024 icon scaled to 66% and centred on a transparent 108dp canvas (export `ic_launcher_foreground.png` at 432 px into `drawable-nodpi`). If padding a transparent canvas with `sips` isn't possible, use `sips --padToHeightWidth 1552 1552` on a copy of the 1024 PNG and then resize to 432. Check the result visually by reading the PNG.
4. Print a summary (counts per type) and exit non-zero if any expected file is missing.

### 10.2 Design tokens

- **`design/Palette.kt`** ← `Design/Palette.swift`. Exact hex values:
  `taupe #E6DFD3`, `cream #F5F2EB`, `dark #111111`, `light #FFFFFF`, `muted #8C857B`, `border #D9D0C5`, `background #FEFEFE`, `foreground = dark`, `catalogueSkeletonLight #F0F0F0`, `catalogueSkeletonMid #E0E0E0`, `shimmerHighlight = #F5F2EB @ 0.7 alpha`, `statusVerified #22C55E`, `statusPending #F59E0B`, `statusRejected #EF4444`.
  Also port the *other* colour enums that live in feature files (`AuthColor` in `AuthKit.swift`, `OnboardColor` in `OnboardKit.swift`, `ThemePalette` in `StoreThemeView.swift`, employee palettes in `EmployeeAppearance.swift`) into those features' Kotlin files. Same values.
- **`design/Layout.kt`** ← `Design/Layout.swift`. `Spacing.step(n) = n*4 dp`, `xs 4, sm 8, md 12, base 16, lg 20, xl 24, xxl 32, xxxl 48, huge 64, screenGutter 20, floatingNavClearance 72`. `Radius sm 4, md 6, lg 8, xl 12, xxl 16, pill 999` (use `CircleShape`/`RoundedCornerShape(50)` for pill). `Stroke hairline 1dp, emphasis 1.5dp`.
- **`design/Typography.kt`** ← `Design/Typography.swift`:
  - One `FontFamily` per family with every weight registered: Cirka (Light 300, Regular 400, Bold 700), Manrope (ExtraLight 200, Light 300, Regular 400, Medium 500, SemiBold 600, Bold 700, ExtraBold 800), Gilroy (Regular, Medium, SemiBold, Bold), Satoshi (Light, Regular, Medium, Bold, Black 900), GildaDisplay (Regular), EmployeeMatter (Regular, Medium, Bold), EmployeeSeasonMix (Regular).
  - A `JewelWeight` enum mirroring SwiftUI's `Font.Weight` (`UltraLight, Thin, Light, Regular, Medium, Semibold, Bold, Heavy, Black`) so call sites port literally.
  - Functions `cirka(size, weight)`, `manrope(size, weight)`, `gilroy(size, weight)`, `satoshi(size, weight)`, `gilda(size)`, `sfPro(size, weight)` returning `TextStyle`. **Port each `switch` exactly.** For example `manrope(.semibold)` → SemiBold face, `cirka(.semibold)` → Bold face, `satoshi(.semibold)` → Medium face. `sfPro` → `FontFamily.Default` (Roboto on Android, the system-face equivalent; §18 D10).
  - `RoleTheme { Wholesaler, Retailer, Employee }` with `headingFace` / `bodyFace` as in Swift.
  - Set `lineHeight` to `TextUnit.Unspecified` and `platformStyle = PlatformTextStyle(includeFontPadding = false)` everywhere, so text boxes match iOS metrics.
- **`design/Motion.kt`** ← `Design/Motion.swift`. The signature curve `cubic-bezier(0.16, 1, 0.3, 1)` → `CubicBezierEasing(0.16f, 1f, 0.3f, 1f)`. Port every named duration and curve. `ReducedMotionAware` → read `Settings.Global.ANIMATOR_DURATION_SCALE == 0f` and skip animations, as iOS does with Reduce Motion.
- **`design/JewelTheme.kt`**: §7.10.
- **`design/components/`**: generic controls used across roles, ported now so feature phases can use them:
  - From `Features/Wholesaler/FormComponents.swift`: `SectionHeader`, `LabelledField`, `JewelSelect`, `InputWithSuffix`, `JewelToggle`, `ErrorBanner`, `SpinnerRing`, `Shimmer` (Modifier), `FieldPair`.
  - `PressableButtonStyle` (`WholesalerHomeView.swift`) and `PressScaleStyle` (`EmployeeDetailViews.swift`) → a `Modifier.pressScale()` using `interactionSource.collectIsPressedAsState()`.
  - `SvgImage(name)`: `AsyncImage("file:///android_asset/svg/$name.svg")` with a fixed size.

### 10.3 Core pure logic (all unit-tested in this phase)

- `core/text/Credentials.kt` ← `Core/Credentials.swift`. Use the **exact** regexes. Kotlin `Regex` is Java syntax, which supports the same lookaheads. Note the email regex `^[^\s@]+@[^\s@]+\.[^\s@]+$` and the three Indian-mobile branches. `PasswordRule` labels verbatim (en-dashes).
- `core/text/Copy.kt` ← `Core/Copy.swift` (all 268 lines, verbatim).
- `core/text/StringExt.kt`: `val String.trimmed` (trim whitespace and newlines), `val String.nilIfEmpty: String?`.
- `core/time/JewelDates.kt`: ISO-8601 parse (with or without fractional seconds, microseconds), plus the formatters used by `ChatTime` (`ChatThreadsView.swift`), `OrderTime` (`WholesalerOrdersView.swift`), `EmployeeDates` (`EmployeeAPI.swift`) and `FormatterCache` (`TreasureChestModels.swift`). Use `Locale("en", "IN")` wherever iOS formats numbers/currency for India. Check each Swift formatter's locale and copy it.
- `core/model/*`: all DTOs from `Core/Models.swift` and `Features/Wholesaler/WholesalerModels.swift`, including `Product`'s computed helpers (`canPublish`, `displaySource`, `displayImageUrl(size)`, `hasDisplayImage`, `thumbnailUrls(size)`, `url(original, size)`), `UploadUsage` (`isUnlimited`, `display`, `remaining`), `CatalogueCategory`, `OrderStatus`.
- `feature/employee/WebShuffle.kt` ← `Features/Employee/Data/WebShuffle.swift`. **Critical:** it reproduces a JavaScript Double-precision quirk bit for bit. Port with `Double` arithmetic exactly as the Swift comment explains, and unit-test it with the values in the Swift file (or derive vectors from the web `app/dashboard/employee/page.jsx`).

### 10.4 Acceptance criteria

- Every font renders. Write one `@Preview` "FontSpecimen" showing every family/weight, and a unit test that the `R.font.*` ids exist (compile-time).
- `CredentialsTest`, `PasswordRuleTest`, `WebShuffleTest`, `ProductDisplayImageTest`, `JewelDatesTest` pass (§16d lists the cases).
- `copy_assets.sh` reports 24 fonts, 22 SVGs, all PNG/JPG sets, and launcher icons generated.

## §11 Phase 2 — Data layer

**Goal:** every backend call in §8 exists as a Kotlin function with the same semantics, compiles, and the pure parts (decoders, cookie bridge, error parsing, legacy-contract detection, quote maths) are unit-tested.

### 11.1 Files and what each must contain

| Kotlin file | Port from | Must include |
|---|---|---|
| `data/supabase/Supabase.kt` | `Networking/SupabaseManager.swift` | §7.4 client, `projectRef`. |
| `data/net/HttpClients.kt` | `NetworkResilience.swift` (`supabaseSession` config), `JewelAPI.session` | §7.5. The site client has an in-memory `CookieJar` that stores **all** cookies per host (like `HTTPCookieStorage.shared`), so verify-otp cookies are available. |
| `data/net/JewelNetwork.kt` | `Networking/NetworkResilience.swift` | `isTransient(e)`, `withRetry(attempts=3, initialDelay=400ms)`. |
| `data/net/SsrSessionBridge.kt` | `Networking/SSRSessionBridge.swift` | Parse `Set-Cookie` headers (OkHttp `Cookie.parseAll(url, headers)`), find `sb-<ref>-auth-token` or chunks `.0`, `.1`, … sorted **numerically**, concatenate, **percent-decode** (write your own `%XX` decoder that leaves `+` alone, because `URLDecoder` turns `+` into a space), strip `base64-` prefix, base64url decode with tolerant padding, then parse the JSON object (`access_token`/`refresh_token`) or the legacy positional array. Pure Kotlin (no `android.net.Uri`, no `android.util.Base64`; use `java.util.Base64.getUrlDecoder()`), so it is unit-testable. |
| `data/net/ApiError.kt` | `JewelAPI.APIError`, `throwIfError` | `class ApiError(status, message, locked, lockedUntil, cooldown, waitSeconds) : Exception(message)`, `fun parseApiError(code, body)`, `Throwable.userMessage()`. |
| `data/net/AuthToken.kt` | `auth.session` usage | §7.5. |
| `data/net/EdgeFunctions.kt` | `functions.invoke` usages | §7.5. |
| `data/net/Multipart.kt` | `MultipartForm` | Thin helper over `MultipartBody.Builder`. |
| `data/api/JewelApi.kt` | `Networking/JewelAPI.swift` | `checkUser`, `sendOtp`, `verifyOtp` (cookies → `importAuthToken`), `hasLocalSession`, `invitationSettings(page)` + DB fallback with the exact fallback numbers (`available 2000, minimum 1000, step 500, maximum 10000, reward 1000, giftCredits 1000`), `saveInvitationGuide`, `cancelInvitation` + fallback, `fetchLatestActiveInvitation`, `createRetailerInvitation` + DB fallback (code = initials + `-` + 6 random `[a-z0-9]`, 7-day expiry, `max_uses 1`, source `"web"`), `claimRetailerInvitation`, `fetchRetailerMarketplace`, `fetchPaginatedMarketplace` (+ legacy detection + `filteringLegacy` with chain aliases `chain, chains, neck chain, neck chains`), `fetchMarketplaceCategories`, `hydrateMarketplaceProducts` (+404 fallback), `fetchProductDetail` (+404 fallback), `setRetailerSelection`, `createRetailerOrder`, `createOrders` (max 20), `fetchRetailerOrders`, `setPassword(password, role)` (validate → `updateUser` → `set_my_role` → refresh user) with the exact error messages. |
| `data/api/WholesalerApi.kt` | `Networking/WholesalerAPI.swift` | Everything in that file: `fetchWholesaler` (by email then `user_id`), `markDashboardVisited`, three counts, `fetchCatalogue` (page size 20, `jewellery_type` filter with singular/plural normalisation and chain aliases, `count=exact`, newest first), `fetchProduct`, `updateProductFlags` (`in_stock` → `stock_available`), `ProductEdit` + `updateProduct`, `deleteProduct` (verify-after-delete, the RLS zero-row case with its error copy), `deleteProductImages` (parse `/storage/v1/object/public/plant-images/` paths), `updateOrderStatus` → `OrdersApi.setStatus`, `fetchUploadUsage`, `processProduct` (multipart, 120 s, `Idempotency-Key`, error mapping incl. 402 and the "Upload was cut off…" messages for connection-reset/size errors), `fetchPipelineProduct`, `pollInterval 5s` / `pollTimeout 300s`, `reprocessProduct` (file or JSON body), `clearProductImages` (explicit `JsonNull`, §7.6), `reprocessPollTick` (HEAD probe), `upload(bucket, path, bytes, contentType, upsert=true)` → public URL. Also `AccountApi.deleteMyAccount()` (edge function `delete-account`, error body `{error}`). |
| `data/api/ChamakApi.kt` | `Networking/ChamakAPI.swift` | All functions: picker product sources (wholesaler's products; or the store's designs = union of `retailer_selections` and `customer_board_items` product ids), `uploadSourceImage`, `createGeneration` (lower-cased paths), `triggerStage1Analysis`, `fetchGeneration`, `updateStatus`, `submitFormAndGenerate` (generate-v2), `submitSetAndGenerate`, `getSignedURL(s)` (bucket `chamak-outputs`; pass through already-signed/absolute URLs as the Swift does), `submitFeedback`, `fetchWholesalerGallery`, `InsufficientCreditsError`, `requireLiveSession`. |
| `data/api/CreditsApi.kt` + `CreditsModels.kt` | `Networking/CreditsAPI.swift`, `Features/Wholesaler/TreasureChest/TreasureChestModels.swift` | `fetchWallet` (+ `WalletError` codes `NOT_VERIFIED`, `NOT_AUTHENTICATED`, copy verbatim), `fetchRateCard`, `fetchLedger` (keyset `p_before`/`p_before_id`), `fetchEntitlementKeys` (+ `temporaryAccess` adds `theme.utsav`, `theme.neelam`), `purchaseEntitlement`, `purchaseExport`, `fetchPlans`, `fetchMyPlan`, `subscribe`, `setPlanAutoRenew`. Models: `CreditWallet` (tolerant decode!), `DailyCreditSchedule`, `CreditPrice`, `CreditLedgerEntry`, `EntitlementPurchase`, `Plan`, `PlanStatus` (microsecond dates), `PlanPurchase`. **Skip** `fetchTopUpOptions`, `createTopUpLink`, `purchase(forLink:)`, `TopUpOptions/Pack/Link/Quote`. They only serve the retired top-up UI (§18 D5). Port `TopUpQuote` anyway only if something live uses it (grep first). |
| `data/api/EmployeeApi.kt` | `Networking/EmployeeAPI.swift` | `resolveSession(userId, isRetailer)`, `storeProfile` (RPC first, own-row fallback), `fetchDesigns`, `fetchSelectedProducts`, `fetchProducts(ids)`, `hasUnreadQueries`, `latestOrderUpdate`; `EmployeeSession`, `EmployeeTheme` (+ `normalized`, `detailBackground(landscape)`, `localKey`), `CloudinaryArt.url(asset, width)`, `RetailerDesign` (flexible number decoding), `EmployeeDates`. |
| `data/api/OrdersApi.kt` | `Networking/OrdersChatAPI.swift` (OrdersAPI part) | `setStatus` (RPC `order_set_status`, reply handling), `fetchWholesalerOrders`, `fetchStaffOrders`; `StaffOrder`, `WholesalerOrder`. |
| `data/api/ChatApi.kt` | `OrdersChatAPI.swift` (ChatAPI part) | `fetchThreads`, `open(productId)`, `fetchMessages`, `send`, `report`, `markRead`; `ChatThread`. |
| `data/api/ManufacturingApi.kt` + `feature/manufacturing/ManufacturingModels.kt` | `Networking/ManufacturingAPI.swift`, `Features/Manufacturing/ManufacturingModels.swift` | All endpoints in §8.6 (manufacturing rows), idempotency keys, `detail` error parsing. Skip `registerDeviceToken` (§18 D6). |
| `data/api/WishlistApi.kt` | `Networking/WishlistAPI.swift` | Shares (site API), customers, boards, board items, `setDesign(productId, boardId, saved)`; `StoreCustomer`, `CustomerBoard`, `WishlistShare` (+ absolute URL resolution), `WishlistShareError`. |
| `data/api/StaffAccountsApi.kt` | `Networking/StaffAccountsAPI.swift` | `address(forUsername)` (exact mapping of a staff username to the synthetic sign-in address), `fetchStaff`, `suggestUsername`, `createLogin`, `inviteGoogle`, `resetPassword`, `setActive`, `remove`; `StaffMember`. |
| `data/api/RetailerApi.kt` | `Networking/RetailerAPI.swift` | `markDashboardVisited`, `fetchStoreId`, `fetchDesigns`, `NewDesign` + `addDesign` (bucket `retailer-designs`), `deleteDesign` + best-effort photo removal, `fetchSelectedTheme`, `setSelectedTheme`. |
| `data/api/LikeBook.kt` | `Networking/LikesAPI.swift` (`LikeBook`) | Session-wide set of liked product ids (**lowercased**), `load(force)`, optimistic `toggle` via `toggle_product_like`, `clear()`. Expose as `StateFlow<Set<String>>`. The `LikeButton` composable goes to `feature/retailer/LikeButton.kt`. |
| `data/api/InviteApi.kt` + `DbRefusal.kt` | `Networking/InviteAPI.swift` | `validate(raw)` → `Check`, `attach(raw)` (trim + uppercase); `DbRefusal.code(e)` / `message(e)` reading `PostgrestRestException` (`error`/`code`/`message`/`details`/`hint`). Port the code→copy table exactly. |
| `data/api/OnboardingFeeApi.kt` | `Networking/OnboardingFeeAPI.swift` | `status`, `createLink`, `confirm(linkId)`; `Status.mustPay = required && payable && !paid`; the exact fallback copy. |
| `data/api/StoreActivity.kt` | `Networking/StoreActivity.swift` | `log(event, productId?, customerId?)` fire-and-forget on an app-scoped `CoroutineScope(SupervisorJob() + Dispatchers.IO)`, the `Event` names (`design.viewed`, `design.shortlisted`, `wishlist.design_saved`, `customer.added`, `order.requested`, `chamak.exported`), `registerDevice()` (§8.2). |

### 11.2 Acceptance criteria

- Compiles. Every public function from every iOS `Networking/*.swift` file has a Kotlin counterpart, or is listed as intentionally skipped in `PORT_STATUS.md` with the reason.
- Unit tests pass: `SsrSessionBridgeTest` (unchunked, chunked out of order `.1,.0,.2`, `base64-` prefix, percent-encoded, legacy array, garbage → null), `ApiErrorTest` (send-otp 429 lock fields), `MarketplaceContractTest` (legacy vs v2 detection, legacy filtering, chain aliases), `CatalogueFilterTest` (`necklace` → `[necklace, necklaces]`, `chains` → 4 aliases, `all` → no filter), `CreditWalletDecodeTest` (short object decodes as zero wallet), `PlanStatusDecodeTest` (microsecond timestamps), `RetailerDesignDecodeTest` (numbers as strings).

## §12 Phase 3 — Auth, session and routing

**Goal:** a person can open the app, choose a door, sign in or sign up (phone/email + OTP + password, Google), reset a password, and land on the correct (still placeholder) destination for their role and status. Sign-out works. Process death and relaunch restore the session.

### 12.1 SessionStore (`core/session/SessionStore.kt` ← `Core/SessionStore.swift`)

- `sealed interface Phase { Loading; Unauthenticated(dest); Authenticated(dest) }`, exposed as `StateFlow<Phase>`. Also `user: StateFlow<UserInfo?>`.
- `start()`: collect `supabase.auth.sessionStatus` once (guard against double start) and translate it to the iOS events, then run the **same** `handle(event, session)` logic:

  | supabase-kt `SessionStatus` | iOS event |
  |---|---|
  | `Initializing` | (stay `Loading`) |
  | `Authenticated(session, source = SessionSource.Storage)` | `.initialSession` |
  | `Authenticated(source = SessionSource.Refresh)` | `.tokenRefreshed` |
  | `Authenticated(source = SessionSource.UserChanged / UserIdentitiesChanged)` | `.userUpdated` |
  | `Authenticated(any other source: SignIn, SignUp, External, Unknown…)` | `.signedIn` |
  | `NotAuthenticated(isSignOut = true)` | `.signedOut` |
  | `NotAuthenticated(isSignOut = false)` | `.initialSession` with no session → `Unauthenticated(Entry(null))` |
  | `RefreshFailure` | ignore (keep the current phase; the SDK retries) |

  Use the class names your supabase-kt version actually exposes; the mapping intent above is what matters.
- **Password recovery** (iOS `.passwordRecovery`): supabase-kt doesn't reliably surface a distinct recovery event for PKCE links. So `MainActivity` inspects the incoming URI first. If it is `jewelindia://auth/callback?...` and `next == "/update-password"`, call `sessionStore.beginPasswordRecovery()` *before* `supabase.handleDeeplinks(intent)`. That sets phase `Unauthenticated(UpdatePassword)`, and the same latch as iOS ("once the reset screen is up it owns the app until the password is changed") ignores the `signedIn` that follows.
- Keep every guard from `handle`: the `updatePassword` latch, `SignupFlow.isCompletingSignup` (an `AtomicBoolean`/`@Volatile` on the singleton `SignupFlow`), "token refresh doesn't re-route an authenticated user", and `isAuthFlow(destination)`.
- Actions, ported 1:1 with the same messages: `signIn(identity, password)` (email vs phone, the employee-status gate), `signOut()` (clear `ViewModeStore` for the user, Coil caches, `MarketplaceCatalogueStore` cache, `LikeBook`, then `auth.signOut()`), `refreshDestination()`, `returnToRoleChoice()`, `setUserRole(role)`, `claimStaffInvite(signOutOnFailure)`, `exitPasswordRecovery()`, `updatePassword(p)` (min 6 → `Copy.updateTooShort`), `requestPasswordReset(email)` (trim, lowercase, redirect URL §2.3).
- Run all of it on `Dispatchers.Main.immediate`, with network work inside the API classes on `Dispatchers.IO`.

### 12.2 AuthRouter + ViewModeStore + SignupFlow

- `AuthRouter` ← `Core/AuthRouter.swift`: the `Gate` sealed class (`Found(row) / Missing / Unreachable`) and `gate(table, select, where, equals)` (any exception → `Unreachable`). Also `destination(user)`, `resolveRole(user)`, `metadataRole(user)`, `wholesalerDestination`, `retailerDestination`, `employeeDestination`, with their sign-out side effects (banned → sign out → `Entry(Copy.bannedError)`; employee missing/inactive → sign out → `EmployeeLogin(Copy.employeeDeactivated)`).
- `ViewModeStore`: key `jewel_view_mode.<userId>`, default `.retailer`.
- `SignupFlow` ← `Core/SignupFlow.swift`: every property persisted to `Prefs` under the §7.9 keys (dates as epoch millis), `isCompletingSignup` latch, `signupRole`, `isLocked`, `otpSecondsLeft` (60 s window), `clearOTPState`, `clearIdentity`, `captureRetailerInvitation(uri)` (scheme https, host == `SITE_URL` host case-insensitive, path exactly `/join/<code>`), `clearReferral`. Drop a stale lockout at init.

### 12.3 Screens (`feature/auth/`)

Port each from its Swift file. Layout metrics come from `AuthKit.swift`. **The metrics differ per screen on purpose** (for example entry uses 52dp inputs with `#D9D0C5` borders, sign-in 48dp with `#E5E7EB`), so components take metrics as parameters, exactly like Swift.

| Kotlin | iOS | Key behaviour |
|---|---|---|
| `AuthKit.kt` | `AuthKit.swift` | `Breakpoint`, `AuthColor`, `AuthLayout` (incl. `AuthPanelImage` artwork, back button), `AuthFieldLabel`, `AuthTextField`, `AuthSecureField` (show/hide), `AuthErrorRow`, `AuthPrimaryButton` (loading state), `AuthLegalText` (Terms/Privacy links → Custom Tab), `AuthOrDivider`. |
| `RoleChoiceScreen.kt` | `RoleChoiceView.swift` | Three doors (Wholesaler / Retailer / Staff), "already have an account" → `Entry(SignIn)`. `signedIn = true` variant for no-role accounts (choosing calls `setUserRole`; staff calls `claimStaffInvite`; sign-out button). `InvitationBanner`. `UnreachableScreen` (retry → `refreshDestination`, sign out). Auto-forward when a captured invitation exists (line ~32). |
| `EntryScreen.kt` (+ `EntryMode`) | `EntryView.swift` | Identity field. Submit: trim → email passes, else `validateIndianMobile` (`Copy.invalidMobile`) → `checkUser` → exists+google → Google; exists → `SignIn(identity)`; new + signIn mode → `Copy.entryNoAccount`; new + signup → `sendOtp` → save `identity`, `otpSentAt`, `remainingResends` → `VerifyOtp`. Google button with the `isCompletingSignup` latch and `routeAfterSocialSignIn` (existing role kept; new account takes the door's role, or the role question in sign-in mode). **No Apple button** (§18 D4). The inline Google "G" is `GoogleG` (SVG asset). |
| `InviteCodeScreen.kt` | `InviteCodeView.swift` | Code field validated **as typed** through `InviteApi.validate` (debounce the way iOS does, see the `task(id:)`), states from `InviteCodeCheck`. On valid: store `referralCode`, `referralRole = retailer`, `invitedBy`, `chosenRole = retailer` → `Entry(Signup(Retailer))`. "No code" pops to the front door. |
| `EmployeeSignInScreen.kt` | `EmployeeSignInView.swift` | Username + password → `StaffAccountsApi.address(username)` → `session.signIn`. Google (staff invited by address) → `claimStaffInvite`. No forgot-password, by design. |
| `SignInScreen.kt` | `SignInView.swift` | Read-only identity chip with "Change" (clear identity, pop to root), password, "Forgot password" → `ForgotPassword`, `session.signIn`. |
| `VerifyOtpScreen.kt` | `VerifyOTPView.swift` | 8 single-character boxes with a literal `-` between the 4th and 5th (one hidden `BasicTextField` driving 8 visual boxes works best on Android, with paste of the full code and SMS autofill via `autofill`/`KeyboardType.NumberPassword`). 60 s validity countdown resumed from `otp_sent_at`, 30 s resend cooldown, 5 resends, 24 h lockout from the 429 body. Submit: set `isCompletingSignup` → `verifyOtp(identity, token, referralCode)` → new user → `SetPassword`; existing → release latch → `refreshDestination`. Heading and info note switch on `signupRole`. |
| `SetPasswordScreen.kt` | `SetPasswordView.swift` | Live checklist of the 4 `PasswordRule`s. Submit → `JewelApi.setPassword(p, flow.signupRole)` → `clearOTPState` → release latch → `refreshDestination`. |
| `PasswordResetScreens.kt` | `PasswordResetViews.swift` | `ForgotPasswordScreen` (confirmation card replaces the form on success), `UpdatePasswordScreen` (calls `updatePassword` then `exitPasswordRecovery`), `SuccessCheckCircle`. |
| `GoogleSignIn.kt` | `GoogleSignIn.swift` | §12.5. |

### 12.4 OTP session adoption

`JewelApi.verifyOtp` must:

1. POST with the site client (cookie jar on).
2. Throw `ApiError` for non-2xx (send-otp-style metadata included).
3. Decode `VerifyOTPResponse`.
4. `SsrSessionBridge.tokens(response.headers, url)` → if non-null, `auth.importAuthToken(access, refresh, retrieveUser = true, autoRefresh = true)` (swallow failure, like iOS `try?`).
5. Return the decoded body.

The screen then relies on `JewelApi.hasLocalSession` as iOS does.

### 12.5 Google sign-in on Android

```kotlin
sealed interface GoogleOutcome { data object SignedIn; data object Cancelled; data class Failed(val message: String) }

suspend fun signIn(activity: Activity): GoogleOutcome =
    if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()) signInNatively(activity) else signInInBrowser()
```

- **Native:** `rawNonce` = 32 random chars from the same charset as Swift (`SecureRandom`). `hashedNonce` = SHA-256 hex of `rawNonce`. Build a `GetGoogleIdOption` with `setServerClientId(GOOGLE_WEB_CLIENT_ID)`, `setFilterByAuthorizedAccounts(false)`, `setNonce(hashedNonce)`. Call `CredentialManager.create(activity).getCredential(activity, request)` → `GoogleIdTokenCredential.createFrom(result.credential.data).idToken` → `auth.signInWith(IDToken) { idToken = it; provider = Google; nonce = rawNonce }`. `GetCredentialCancellationException` → `Cancelled`. `NoCredentialException` (no Google account on the device) → fall back to the browser flow. Anything else → `Failed(message)`. **Google gets the hash and Supabase gets the raw value.** Swapping them fails exactly as the Swift comment describes.
- **Browser:** `auth.signInWith(Google, redirectUrl = "jewelindia://auth/callback") { queryParams["access_type"] = "offline"; queryParams["prompt"] = "consent" }`. This returns as soon as the Custom Tab opens. Then suspend until either `sessionStatus` becomes `Authenticated` (→ `SignedIn`), or the activity returns to `RESUMED` and no session arrives within 2 seconds (→ `Cancelled`, logged in debug with the same "add the redirect URL to the Supabase allow-list" hint as Swift). Time out after 5 minutes (→ `Cancelled`).
- The caller manages `SignupFlow.isCompletingSignup` exactly as `EntryView.startGoogle` / `EmployeeSignInView` do.

### 12.6 RootScreen and AuthNavHost

- `navigation/RootScreen.kt` ← `Navigation/RootView.swift`: `when (phase)`: `Loading → LaunchScreen` (Cirka 32 "Jewel India" on `Palette.background`, no spinner), `Unauthenticated(d) → AuthNavHost(d)`, `Authenticated(d) → scene(d)`. Scenes: `SelectRole → RoleChoiceScreen(signedIn = true)`, `Unreachable → UnreachableScreen`, `WholesalerOnboarding/Submitted/Dashboard`, `RetailerOnboarding/Submitted/Dashboard`, `EmployeeDashboard`. Until a phase builds a real screen, use `PhaseGatePlaceholder` (title, note, sign-out), ported from the same file.
- Deep links (`MainActivity`): `onCreate(intent)` and `onNewIntent(intent)` → `rootViewModel.handleIncomingUri(uri)`:
  1. If no user and `signupFlow.captureRetailerInvitation(uri)` → done.
  2. If `uri.scheme == "jewelindia"`: recovery check (§12.1), then `supabase.handleDeeplinks(intent)`.
- `AuthNavHost` ← `AuthFlowView.swift` (§7.2).

### 12.7 Acceptance criteria

- Unit tests: `AuthRouterTest` with a fake gate provider covering every row of §1.2 (including `Unreachable` and the sign-out side effects), `SignupFlowTest` (persistence, stale-lock drop, `otpSecondsLeft`, invitation capture accepting `https://<SITE_URL host>/join/ABC` and rejecting other hosts, extra segments and http), `SessionEventMappingTest` (status → event table).
- Manual (only with founder-provided test accounts, §0.2): sign in as each role and land on the right placeholder; kill the app and relaunch (session restored, same destination); sign out (back to the front door). Without test accounts: verify every screen with `@Preview`s and that `check-user` against a clearly fake identity (`not-a-user@example.invalid`) returns `exists: false` without side effects. **Don't** call `send-otp` for real numbers.

## §13 Phase 4 — Onboarding (wholesaler + retailer)

**Goal:** a signed-in wholesaler or retailer with no row completes the 3-step application with documents (and pays the onboarding fee when the server requires it), then sees the submitted/verification screen. That screen moves them into the dashboard once they are verified.

### 13.1 Shared onboarding kit (`feature/onboarding/OnboardKit.kt` ← `Features/Onboarding/OnboardKit.swift`)

`OnboardColor`, `OnboardLayout`, `OnboardNavbar`, `LeftPanel` (wide layouts only; phones hide it, per `Breakpoint`), `StepIndicator`, `OnboardTextField`, `PickedFile(bytes, filename, mimeType)`, `ImageUploadBox` (picker + camera + preview + remove + the `upload.<ext>` filename rule at line ~530 + the PDF/document option if the Swift offers one, so check), `OnboardPrimaryButton`, `OnboardLegalNote`. The retailer flow reuses all of these.

`ImageUploadBox` on Android: tapping opens a small chooser ("Take photo" / "Choose from library"), matching iOS's options. Library uses `PickVisualMedia(ImageOnly)`. Camera uses `TakePicture` into `cacheDir/camera/<uuid>.jpg` through `FileProvider`, after requesting `CAMERA`. If permission is denied, show the iOS-equivalent denial copy, or "Camera access is off. Enable it in Settings to take a photo." with a Settings deep link. Every result goes through `ImageNormalizer` (document mode, 1200 px).

### 13.2 Wholesaler

| Kotlin | iOS | Notes |
|---|---|---|
| `OnboardCoordinator.kt` | `OnboardCoordinator.swift` | Own `NavHost` (Step1 → Step2 → Step3). Owns `OnboardFlow` (ViewModel scoped to the coordinator, so leaving the flow discards it, as on iOS). Step 1 back → `session.returnToRoleChoice()`. After submit → `session.refreshDestination()`. |
| `OnboardFlow.kt` | `OnboardFlow.swift` | All fields, validation, `submit(user)`: verify a live session matches (`requireLiveSession` pattern), **lowercase uid**, upload the 5 documents **in parallel** (`async`) to `aadhaar-documents`, `pan-documents`, `gst-documents`, `business-logos` with `<uid>/<kind>-<stamp>` paths and the human labels used in error copy ("Aadhar Front", "Aadhar Back", "PAN Card", "GST Certificate", "Business Logo"), then upsert the `wholesalers` row with `withRetry`. Read lines ~145–285 carefully. The comments there explain three real failures (anon-session RLS, uppercase uid vs storage policy, retry). |
| `OnboardSteps.kt` | `OnboardSteps.swift` | Step 1 identity + Aadhaar, Step 2 business details, Step 3 documents. Step 3's `submitOrPay()` (line ~239): check `OnboardingFeeApi.status()`; `mustPay` → show `OnboardingFeeSheet`; otherwise `submit()`. |
| `OnboardingFeeSheet.kt` | `OnboardingFeeSheet.swift` | Shows the server amount (₹ formatting as iOS). "Pay" → `createLink()` (if `paid == true` already, continue) → open `url` in a **Custom Tab**. When the activity resumes (tab closed) → `confirm(linkId)` → `paid` → callback to submit; not paid → the iOS retry/"check again" state. Use `LifecycleResumeEffect` to detect the return. |
| `OnboardSubmittedScreen.kt` | `OnboardSubmittedView.swift` | `VerificationRow` select (`verification_status, notification_message, rejection_reason, rejected_documents`), `VerificationTimeline`, status-specific copy and colours (pending pulse amber, verified ripple green, rejected shake red; port the keyframes from `Motion`), rejected-document name mapping, pull to refresh, and the verified → "go to dashboard" action: `WholesalerApi.markDashboardVisited` → `refreshDestination` (line ~332). Sign-out available. |

### 13.3 Retailer

| Kotlin | iOS | Notes |
|---|---|---|
| `RetailerOnboardCoordinator.kt` | `RetailerOnboardCoordinator.swift` | Same shape as the wholesaler one. |
| `RetailerOnboardFlow.kt` | `RetailerOnboardFlow.swift` | `submit(user, referralCode)`: same 5 buckets ("Store Logo" label), `retailers` upsert with `withRetry`. Port the referral handling exactly as written. |
| `RetailerOnboardSteps.kt` | `RetailerOnboardSteps.swift` | Copy is byte-identical to the wholesaler flow except where the Swift differs (store-specific subheadings). |
| `RetailerOnboardSubmittedScreen.kt` | `RetailerOnboardSubmittedView.swift` | Reads **`retailers`**, not `wholesalers`. The file's doc comment explains the bug this avoids. Shares `VerificationRow`/`VerificationTimeline`. Attaches a pending invitation code via `InviteApi.attach` (line ~203). Verified → `RetailerApi.markDashboardVisited` → `refreshDestination`. |

### 13.4 Acceptance criteria

- `@Preview`s for every step (empty, filled, error), the fee sheet, and the submitted screen in each status (pending, verified, rejected with documents, on hold, resubmission required).
- Unit tests: field validation (port each rule), storage path builder (lowercase uid, stamp format), `OnboardingFeeApi.Status.mustPay` truth table.
- Live submission only with a founder test account. Otherwise verify the request bodies by logging them against a local fake (`MockWebServer` is fine to add as a `testImplementation`).

## §14 Phase 5 — Wholesaler app

**Goal:** the complete wholesaler experience except Chamak (Phase 6). Chamak's tab shows `PhasePlaceholder` until then.

### 14.1 Shell (`navigation/WholesalerShell.kt` ← `Navigation/WholesalerShell.swift`)

- Tabs, in this order: **Home** (`NavHome`), **Catalogue** (`NavCatalogue`), **Chamak** (sparkles icon → `Icons.Outlined.AutoAwesome`), **Orders** (`NavOrders`), **Chat** (`NavChat`). Labels from `Copy.WholesalerTab`.
- Shell-scoped state: `CreditStore`, `WholesalerOrdersStore`, selected tab, Home's back stack, `catalogueCategory` (Home's category tiles set it and switch to Catalogue).
- Home routes: `UploadHistory`, `TreasureChest`, `ManufacturingOffers`.
- Chat tab: `ChatThreadsScreen(emptyTitle = "No Active Queries", emptyMessage = "When stores ask about your designs, their questions will appear here.")`, titled "Queries & Chat".
- Top bar actions on **every** tab: `CreditBalancePill` (Home → push `TreasureChest`; other tabs → TreasureChest sheet with a "Done" button), then the `JewelLogo` 26dp rounded-6 image button opening a menu: **Profile** · **Invite Retailer** (`Copy.WholesalerTab.inviteRetailer`, `NavAddRetailer` icon) · **Manufacturing Offers** (hammer icon; Home → push, else sheet) · divider · **Log Out** (destructive → confirmation dialog with `Copy.logoutTitle`/`logoutBody`/`logoutConfirm`/`logoutCancel`).
- On first composition: `credits.refresh()` and `ordersStore.loadIfNeeded()` in parallel. On every resume: `credits.maintainDailyWallet()`.
- Sheets: Invite Retailer (content height ≈ 590dp, expandable), Profile (its "invite" action closes Profile, waits 450 ms, then opens Invite, so the two don't collide), TreasureChest, Manufacturing offers.

### 14.2 Screens

| Kotlin (feature/wholesaler/…) | iOS | What to get right |
|---|---|---|
| `WholesalerHomeScreen.kt` + `HomeViewModel.kt` + `HomeCards.kt` | `WholesalerHomeView.swift` (`HomeModel`, `StatCard`, `PingDot`, `UploadDesignCard`, `ChamakCard`, `CategoryCard`, `ViewAllCard`, `PressableButtonStyle`) | Composition in order: sticky "Home" title, hero upload banner (UploadDesign* artwork layers: hands, coin, wash, icon), "Insights" grid of 4 KPI cards (products count, pending orders, unread conversations, upload usage), Chamak promo (`ChamakStudioArtwork`, `ChamakStudioArrow`, `ChamakNecklace`), Invite Retailer card, Manufacturing offers card (`ManufacturingOffersCard`), report teaser (`WholesalerReportTeaser`), category grid (`Cat*` images) + "View All". Load: `fetchWholesaler(userId, email)` then counts in parallel; every count failure → 0 (iOS `try?`). The "Upload Now" CTA opens `AddProductSheet`. Pull to refresh. |
| `WholesalerCatalogueScreen.kt` + `CatalogueViewModel.kt` + `CatalogueProductCard.kt` + `CategoryChip.kt` | `WholesalerCatalogueView.swift` | Category chips (initial category from Home), search, grid of `CatalogueProductCard` (stock tags, published state), **wrap protected product images in `CaptureProtected`**, tap → `ProductDetailSheet`, context menu (edit/delete with confirm; delete also calls `deleteProductImages`), "+" → `AddProductSheet`, export folder (`OpenDocumentTree`, write each image), paging (20). |
| `AddProductScreen.kt` + `AddProductForm.kt` (ViewModel) + `AddProductSuccess.kt` | `AddProductView.swift`, `AddProductForm.swift` | Three numbered sections (image, essential details, specifications), daily upload-limit banner from `fetchUploadUsage`, full validation on submit, image-count choice and its credit cost (rate card `product.images_<n>`), a stable `submissionKey` per attempt, `processProduct`, the 402 path → `InsufficientCreditsSheet`, then the success view. Uploads go to `plant-images` where the Swift does (line ~207). **No polling here.** Results arrive later, by design (see the doc comment). |
| `EditProductScreen.kt` | `EditProductView.swift` | Edits `title`, **`category` = material dropdown** (not `jewellery_type`; the comment records the bug), `jewellery_type`, style, size, purity, weights, stock, make-to-order days, published, custom/showcase image upload to `plant-images` (line ~296). Save → `updateProduct`. |
| `ProductDetailSheet.kt` + `ReprocessState.kt` + `ReprocessOverlay.kt` | `ProductDetailSheet.swift` | Renders gallery (`thumbnailUrls`), publish toggle (`canPublish`), stock toggle (optimistic + `withRetry`), edit, delete, and the **Re-upload to AI** flow: confirm sheet (with/without a new photo) → `clearProductImages` → `reprocessProduct` → poll `reprocessPollTick` every 5 s up to 300 s, updating the status line ("background removed" once the HEAD probe hits), cancel on dismiss → done shows the new renders. |
| `UploadHistoryScreen.kt` | `UploadHistoryView.swift` | Today's uploads with AI status. |
| `WholesalerOrdersScreen.kt` + `WholesalerOrdersStore.kt` + `WholesalerOrderDetail.kt` + `RejectOrderSheet.kt` + `OrderTimeline.kt` + `OrderStatusBadge.kt` | `WholesalerOrdersView.swift` | Store pre-fetches with the shell. List cards with design thumbs, filters as in Swift, detail with timeline, status actions through `OrdersApi.setStatus` (accept, progress states, reject with reason sheet: `FlowLayout` of reason chips → `FlowRow`), `OrderTime` formatting, empty state with the Invite Retailer CTA. |
| `feature/chat/ChatThreadsScreen.kt` | `Features/Chat/ChatThreadsView.swift` | Shared by all roles. `chat_threads`, row (`ChatThreadRow`), unread state, `ChatTime`, empty title/message parameters. |
| `feature/chat/ChatThreadScreen.kt` + `ReportMessageSheet.kt` | `ChatThreadView.swift` | `side` (wholesaler/employee) decides bubble alignment. Messages, send (`chat_send`), mark read on open, report a message (reason + details), auto-scroll to the bottom, keyboard insets (`imePadding`). |
| `InviteRetailerSheet.kt` + `InvitationHistoryScreen.kt` + `InviteRetailerCard.kt` | `InviteRetailerSheet.swift` | Follow `wholesaler ios/README.md` §"Invitation flow" exactly: the "How it works" guide shows on first open even when settings fail to load; "Don't show me again" **starts selected**, can be toggled, and is saved per account only after the backend accepts it, with a local account-scoped cache (add a Prefs key `invite_skip_guide.<userId>` and record it in §7.9). Generation happens **only** on the explicit button and waits for real server settings, so no device-made codes or invented balances (the DB fallback is only for the server-404 case, as in Swift). Gift stepper (min/step/max from settings), copy code (clipboard), share link (`ACTION_SEND` text), history list with statuses and paging, cancel. |
| `feature/profile/ProfileScreens.kt` | `Features/Profile/ProfileViews.swift` | `WholesalerProfileScreen` (details from `wholesalers` via `WholesalerDetails`, wallet card, shortcuts, legal section, log out, **delete account** at line ~118), `StaffProfileScreen` (+ `StaffDetails`, `StaffProfileButton`, its own delete account at line ~294) used by the employee shell. |
| `treasurechest/CreditStore.kt` | `TreasureChest/CreditStore.swift` | Shell-scoped. `refresh()`, `refreshPlan()`, `maintainDailyWallet()`, `plan`, entitlements. **"An unavailable balance is never displayed as a fresh spendable amount."** Keep the stale/unavailable state distinct from zero. |
| `treasurechest/CreditBalancePill.kt` | `CreditBalancePill.swift` | |
| `treasurechest/TreasureChestScreen.kt`, `TreasureChestCard.kt`, `TransactionHistoryScreen.kt` | same names | Wallet breakdown, daily schedule (`DailyCreditSchedule`), rate card, ledger with keyset paging. |
| `treasurechest/InsufficientCreditsSheet.kt` | same | |
| `treasurechest/DailyCreditsSheet.kt` | `TopUpSheet.swift` **lines 1–190 only** (`TopUpSheet` + `DailyCreditAllowanceView`) | Info-only "Daily credits" sheet with "Done". Refresh on resume. **Don't port** `AppleCreditPurchases`, `AppleTopUpView`, `TopUpView`, `TopUpModel`, `SafariCheckoutView` (§18 D5). |
| `report/WholesalerReportScreen.kt` + `WholesalerReportTeaser.kt` | `Report/WholesalerReport.swift` | `wholesaler_report` for 7/30 days, each count vs the previous period, the cards. |
| `feature/manufacturing/WholesalerOffersListScreen.kt`, `WholesalerOfferDetailScreen.kt`, `AcceptQuoteSheet.kt`, `DeclineOfferSheet.kt`, `ManufacturingOffersCard.kt` | `Features/Manufacturing/*` (wholesaler side) | Status filters, offer detail, accept (with quote in parallel mode → `/quotes`, otherwise `/accept`) with idempotency, decline with optional reason. |

### 14.3 Acceptance criteria

- Every screen has `@Preview`s with sample data (reuse the sample builders from `DebugScreenPeek.swift` / `DebugPeekSamples` as fixtures in `src/debug` or `src/test`).
- Unit tests: `HomeViewModel` count fallbacks, catalogue filter (from Phase 2), `ProductEdit` encoding omits nulls, `clearProductImages` sends explicit nulls, reprocess poll state machine (fake API: pending → background removed → done; timeout at 300 s with a virtual clock).
- With a test wholesaler account (if provided): browse the catalogue, toggle stock (reverting it afterwards), open an order, send a chat message in a test conversation. Never create real products against production without explicit permission.

## §15 Phase 6 — Chamak studio

**Goal:** Chamak works for wholesalers (tab) and retailers (full-screen from the menu, with `catalogueSource = StoreDesigns`).

Read first: `Features/Wholesaler/Chamak/CHAMAK_PIPELINE_CHANGES.md`, `CHAMAK_2_OPENAI_SPEC.md`, `Networking/ChamakAPI.swift`, `ChamakModels.swift`, `ChamakViewModel.swift`. Production note: past Chamak outages were caused by the AI provider's balance running out, not by app code. A failed generation must show the server's failure message (see `wholesaler ios/tests/chamak-failure-message/`), not a generic one.

| Kotlin (feature/wholesaler/chamak/…) | iOS | Notes |
|---|---|---|
| `ChamakModels.kt` | `ChamakModels.swift` | `ChamakCatalogueSource`, `ChamakStatus`, `ChamakMode`, `SetBackdrop`, `SetStylingChip`, `ContentFlag` (and its user copy), `Stage1Analysis`, `ChamakAttribute`, `ChamakDesignItem`, `WeightedAttribute`, `WholesalerFormInput`, `SetCreationInput/Output`, `ChamakGeneration` (custom decoding: port it), `ChamakFeedback`, `ChamakQuote`, `ChamakSlot`, `JewelleryTypeCanonical` ("cross-platform agreement between iOS, Web, and AI pipeline": keep the exact raw values). |
| `ChamakViewModel.kt` (split: `ChamakViewModel.kt`, `ChamakGenerationRunner.kt`, `ChamakQuotes.kt`) | `ChamakViewModel.swift` (886 lines) | The state machine: picker → (combine) slider form or (set) styling → generating (poll `fetchGeneration` until terminal status) → result. Mode is set once and survives `resetToPicker()`. Credits checks via the rate card and wallet. Insufficient-credits path. Idempotency keys per submit. Stage-1 analysis trigger after source upload. |
| `ChamakHubScreen.kt` (+ `ChamakToolCard`, `ChamakSetCreationCard`, `ChamakLaunch`) | `ChamakHubView.swift` | Tools and the saved gallery. Launch a fresh run in a mode or open a saved result. |
| `ChamakFlowCoordinator.kt` | `ChamakFlowCoordinator.swift` | Nested NavHost for the flow; `openingGeneration` lands straight on a result. |
| `ChamakCatalogPickerScreen.kt` | `ChamakCatalogPickerView.swift` (751) | Pick designs from catalogue/store designs or upload a custom photo (normalise → `uploadSourceImage`), preview, slot rules, protected thumbnails. |
| `ChamakSliderFormScreen.kt` (+ `ArtisanChip`) | `ChamakSliderFormView.swift` | Weighted attribute sliders. The last iOS fix was "missing sliders and Try Again" (build 40). Check `git -C "../wholesaler ios" show b07a46f --stat` and read that diff to see what broke. |
| `ChamakSetStylingScreen.kt` | `ChamakSetStylingView.swift` | Backdrops, styling chips. |
| `ChamakGeneratingScreen.kt` (+ the four gem shapes) | `ChamakGeneratingView.swift` | Animated gems: port `SoftBlossomShape`, `SoftCabochonTrillionShape`, `SoftCushionGemShape`, `SoftTeardropGemShape` paths to `Path` in a `Canvas`. |
| `ChamakResultScreen.kt` | `ChamakResultView.swift` | Result with signed URLs, watermarked/protected view, feedback, export button, try again / new run. |
| `ChamakGalleryScreen.kt`, `ChamakGalleryCard.kt` | same | Signed thumbnails (the bucket is private, and the stored value is only a path), stacked source designs in the corner, unfinished-state display. |
| `ChamakImageViewer.kt` (+ `ZoomableProtectedImage`) | `ChamakImageViewer.swift` | Full-screen pager with pinch-zoom (`Modifier.transformable` / `detectTransformGestures`), protected. |
| `ChamakExportButton.kt` | `ChamakExportButton.swift` | Only shown when the rate card has an export price. `purchase_export(generationId)` → download the clean image to `cacheDir/exports/chamak-<id8>.png` → share sheet (`ACTION_SEND`, `image/png`, FileProvider) and "Save to Photos" (MediaStore). Log `StoreActivity` `chamak.exported`. |
| `ChamakFeedbackSheet.kt` | `ChamakFeedbackSheet.swift` | `chamak_feedback` insert. |

**Acceptance:** previews of every step. Unit tests for the quote maths, status transitions, `JewelleryTypeCanonical` mapping, the failure-message mapping (port `tests/chamak-failure-message`), and source-upload path building (port `tests/chamak-source-upload`). A live run only with a test account and the founder's OK, because it spends real AI credits.

## §16 Phase 7 — Retailer app

### 16.1 Shell (`navigation/RetailerShell.kt` ← `Navigation/RetailerShell.swift`)

- Tabs: **Dashboard** (`square.grid.2x2.fill` → `Icons.Filled.GridView`), **Catalogue** (`square.grid.2x2` → `Icons.Outlined.GridView`), **Employees** (`person.2` → `Icons.Outlined.People`), **Wishlists** (enum case `yourTaste`, label `Copy.RetailerTab.yourTaste`, `heart.text.square` → `Icons.Outlined.FavoriteBorder`) showing `CustomerWishlistScreen`, **Orders** (`bag` → `Icons.Outlined.ShoppingBag`).
- Top bar: `CreditBalancePill` (→ TreasureChest sheet) + avatar (`person.crop.circle` → `Icons.Outlined.AccountCircle`) menu, in this order: **Chamak Studio** (full screen, `catalogueSource = StoreDesigns`) · **Chats** (sheet: `ChatThreadsScreen` with empty title "No chats yet" and the exact message in the Swift file) · **Custom Enquiries** (sheet: `RetailerRequestsListScreen`) · divider · **Treasure Chest** · **Plans** · **Store Theme** · **Employee View** · divider · **Delete account** (destructive, confirm dialog "Delete your account?" / "Delete account permanently" / message verbatim → `AccountApi.deleteMyAccount()` → `session.signOut()`; failure → alert "Account deletion failed") · **Log Out**.
- Employee View: `ViewModeStore.set(Employee, userId)` → `session.refreshDestination()`.
- Startup: `StoreActivity.registerDevice()`, then `credits.refresh()`, `credits.refreshPlan()`, `ordersStore.loadIfNeeded()` in parallel. On resume: `maintainDailyWallet()`.
- Role theme: `RoleTheme.Retailer` (Satoshi).

### 16.2 Screens (`feature/retailer/…`)

| Kotlin | iOS | Notes |
|---|---|---|
| `RetailerDashboardScreen.kt` | `RetailerDashboardView.swift` | Store metrics, quick actions, Employee Portal URL card (copy), employee shortcuts (`onOpenAddEmployee`), `NewArrivalsStrip` (hidden when empty or failed). |
| `NewArrivalsStrip.kt` | `NewArrivalsStrip.swift` | |
| `RetailerCatalogueScreen.kt` (+ `DesignCard`, `StoreDesignDetail`) | `RetailerCatalogueView.swift` | The store's own designs. Add → `AddDesignSheet`. Delete. Protected images. |
| `AddDesignSheet.kt` | `AddDesignSheet.swift` | Photos sized on device (product mode, 2048 px) → `retailer-designs` → `retailer_designs` insert. No AI pipeline. |
| `EmployeesListScreen.kt` (+ `StaffRow`, `StatusChip`, `StaffCredentialsScreen`, `StaffCredentials`) | `EmployeesListView.swift` | Staff list via `employees`. Actions via `staff-accounts`: reset password (show credentials once with copy/share), activate/deactivate, remove (confirm). |
| `AddEmployeeSheet.kt` | `AddEmployeeSheet.swift` | Username+password login (with `suggestUsername`) or Google-address invite. |
| `MarketplaceCatalogueStore.kt` | `MarketplaceCatalogueStore.swift` | App-scoped, account-scoped repository: instant cached start-up (persist the last page set to `filesDir/MarketplaceCatalogueCache/<userId>.json`), background revalidation, keyset pagination (`next_cursor`, page 24), debounced search (port the delay), server-side category filter (with legacy fallback), optimistic selection updates with rollback, `clear()` on sign-out. |
| `YourTasteScreen.kt` (+ `MarketplaceProductCard`, `MarketplaceProductDetail`, `RetailerOrderRequestSheet`) | `YourTasteView.swift` (865) | Product-first marketplace. **No supplier identity on browse cards or detail.** Heart = store shortlist (`setRetailerSelection`). With a `board` argument the bookmark saves to that customer's board instead (`WishlistApi.setDesign`). Detail: gallery, specs, "Ask About this Design" → `ChatApi.open(productId)` → `ChatThreadScreen`, order request sheet (quantity, notes → `createRetailerOrder`, log `order.requested`). Image search entry. Log `design.viewed` / `design.shortlisted`. |
| `CatalogueImageSearch.kt` (+ model) | `CatalogueImageSearch.swift`, `docs/retailer-image-search.md` | Pick/capture photo → normalise → multipart POST `/api/retailer/image-search` → results hydrated via `hydrateMarketplaceProducts`. Port `tests/catalogue-image-search`. |
| `LikeButton.kt` | `LikesAPI.swift` (`LikeButton`) | |
| `RetailerOrdersScreen.kt` + `RetailerOrdersStore.kt` | `RetailerOrdersView.swift` | Shell-warm store, `fetchRetailerOrders` (orders + products + suppliers maps), pull to refresh, protected thumbs. |
| `StoreThemeScreen.kt` (+ `ThemeOption`, `ThemePalette`, `ThemeCard`, `LockedThemeSheet`, `UnlockThemeSheet`, `ClaimThemeSheet`, `ThemeSheetScaffold`) | `StoreThemeView.swift` (672) | Four themes, CLAIM / SELECTED / LOCKED badges, claim sequence, `retailers.selected_theme`, unlock with credits via `purchase_entitlement("theme.<id>")` (insufficient → sheet). Artwork is SVG served as PNG by Cloudinary (`ThemeOption.imageURL`); keep the same URLs. Local cache key `jewel_store_theme`. |
| `PlansScreen.kt` | `PlansView.swift` | Informational only. Plan sales/renewals are paused; text verbatim; "Daily credits" link → TreasureChest. |
| `wishlist/CustomerWishlistScreen.kt` (+ `CustomerRow`, `AddCustomerSheet`) | `Wishlist/CustomerWishlistView.swift` | Customers list, add (name, phone, note → log `customer.added`), delete, "Discover" entry at top → `YourTasteScreen`. |
| `wishlist/CustomerBoardsScreen.kt` | `Wishlist/CustomerBoardsView.swift` | Boards per customer, add/delete board, open a board → `YourTasteScreen(board)`. |
| `wishlist/WishlistShareScreen.kt` | `Wishlist/WishlistShareView.swift` | Create a share (viewers, minutes), list, revoke, share link. Port `tests/wishlist-sharing`. |
| `feature/manufacturing/RetailerRequestsListScreen.kt` (+ `RetailerRequestCard`), `RetailerRequestDetailScreen.kt`, `CreateManufacturingRequestSheet.kt` | `Features/Manufacturing/*` (retailer side) | Create request (photos → `/manufacturing-assets`, `MakingBudgetMode`, simplified requirements per migration `20261010_05`), list with status filter, detail with quotes, award a quote, cancel. |

**Acceptance:** previews, the marketplace store unit-tested with a fake API (cache-then-revalidate ordering, pagination end, search debounce with a virtual clock, optimistic selection rollback on failure), and the `tests/marketplace-contract` port.

## §16b Phase 8 — Staff / employee app

Reached by staff (role `employee`) and by verified retailers in Employee View.

| Kotlin (feature/employee/…) | iOS | Notes |
|---|---|---|
| `EmployeeStore.kt` (+ `EmployeeRoute`) | `EmployeeStore.swift` | Shell-scoped: who is looking (`EmployeeSession` via `EmployeeApi.resolveSession(userId, isRetailer)`), which store, theme, the long-press selection set (in memory only, cleared when the app is killed, like the web's `sessionStorage`), full-screen routes over the tabs, unread-queries and new-orders flags (`employee_orders_last_checked.<seed>`). Phases: loading / failed(message) / ready. |
| `EmployeeShell.kt` (+ `EmployeePillNav`, `EmployeePingDot`, `EmployeeViewBanner`) | `EmployeeShell.swift` | Tabs **Home / Catalogue / Queries / Orders** with the `EmpNav*` SVG glyphs in a custom floating pill nav (port its geometry exactly). Ping dots. A retailer owner in Employee View sees `EmployeeViewBanner` with a switch back (`ViewModeStore.set(Retailer)` → `refreshDestination`). Sheets: wishlists, credits, staff profile. Queries tab = `ChatThreadsScreen` (employee side). |
| `EmployeeAppearance.kt` | `EmployeeAppearance.swift` (566) | Personal styles (`EmployeeStyle`, saved in `jewel_employee_style`, legacy `jewel_employee_sarvam`), independent of the store theme. `EmployeeAppearance` colours/fonts (EmployeeMatter, EmployeeSeasonMix), `LocalEmployeeAppearance`, chrome modifiers, `EmployeeModeBar`, hero gradient, Sarvam button style, motifs (`EmployeeCourtyardArch` shape, `EmployeeJaliPattern`, `EmployeeCreativeBackdrop`, `EmployeeStyleEmblem`). This is a lot of custom drawing. Port the paths literally. |
| `EmployeeHomeScreen.kt` (+ `ViewModeCard`, `DesignerCollectionSection`) | `EmployeeHomeView.swift` | Full-height hero on the store theme artwork (no title bar), logo, name, view-mode card, then up to 6 store designs in the **same per-person order as the web** (`WebShuffle`). |
| `EmployeeCatalogueScreen.kt` (+ `CategoryBubble`, `FilterChip`, `PinCard`) | `EmployeeCatalogueView.swift` | Shortlisted pieces in a 2-column **staggered** grid with varied heights (port `PinShape` height rules), sideways category row, pinned filter chips, tap → review, **long-press 0.5 s → toggle selection** with haptic. |
| `EmployeeFilters.kt` | `EmployeeFilters.swift` | Category art (Cloudinary WebP, Haram local asset), dropdowns, filter state, `FlowRow`, `SmartHeaderState`, `EmployeePager`. |
| `EmployeeDesignsScreen.kt` | `EmployeeDesignsView.swift` | Store's own designs, read-only, category tiles, 4 filters, 10 per page. |
| `EmployeeInfiniteCanvas.kt` | `EmployeeInfiniteCanvas.swift` | Endless board, draggable in any direction, wrap-around tiles, slow auto-drift, fling momentum (`Animatable` + `splineBasedDecay`), tap opens, long-press selects, bottom tray with "Next". |
| `EmployeeReviewScreen.kt` (+ `SelectionCard`, `RequestSentScreen`) | `EmployeeReviewView.swift` (908) | Product page + selection list; "Send Request" → `JewelApi.createOrders(ids ≤ 20)` → `RequestSentScreen`. |
| `EmployeeDetailViews.kt` (split into `EmployeeDetailLayout.kt`, `EmployeeDesignDetail.kt`, `JewelFullImageViewer.kt`, `DetailSpec.kt`, `GlassButtons.kt`) | `EmployeeDetailViews.swift` (668) | CSS `clamp()` helper, glass buttons (translucent white + blur via `Modifier.blur` on API 31+, plain translucent below), themed detail background (landscape vs portrait art), spec rows with dashed rule. **Drop `EmployeeDetailRegionMarker`**: it is a UIKit debug geometry marker. |
| `EmployeeOrdersScreen.kt` | `EmployeeOrdersView.swift` | `fetchStaffOrders`. RLS decides scope. |
| `ComingSoonScreen.kt` | `ComingSoonView.swift` | |

**Acceptance:** previews for every style × theme on Home and detail. `WebShuffleTest` passes. Long-press selection works with TalkBack too (provide `onLongClickLabel`).

## §16c Phase 9 — Screen protection, deep links, account deletion, polish

### 16c.1 Screen protection (`core/security/`)

iOS protects (a) every photo of a design (wherever `ProtectedImageView`, `ZoomableProtectedImage` or `.captureProtected` is used) and (b) the app-switcher snapshot. Android:

- `ScreenSecurity` (held by `MainActivity`): a reference counter. `acquire()` increments; when it goes 0 → 1 it calls `window.addFlags(FLAG_SECURE)`. `release()` decrements; 1 → 0 clears the flag.
- `@Composable fun CaptureProtected(content)`: `DisposableEffect(Unit) { security.acquire(); onDispose { security.release() } }` and renders `content`. `ProtectedImage(url, …)` = `CaptureProtected { CachedImage(…) }`. Use these in **exactly** the files that use protection on iOS: `EmployeeCatalogueView`, `EmployeeDesignsView`, `EmployeeDetailViews`, `EmployeeHomeView`, `EmployeeInfiniteCanvas`, `EmployeeReviewView`, `NewArrivalsStrip`, `RetailerCatalogueView`, `RetailerOrdersView`, `YourTasteView`, `ChamakCatalogPickerView`, `ChamakGalleryCard`, `ChamakImageViewer`, `ChamakResultView`, `ChamakSetStylingView`, `ChamakSliderFormView`, `EditProductView`, `ProductDetailSheet`, `WholesalerCatalogueView`.
- App switcher: in `MainActivity.onCreate`, on API 33+ call `setRecentsScreenshotEnabled(false)`. On API 26–32 there's no equivalent, so set `FLAG_SECURE` permanently on those versions (acquire once at start-up and never release).
- `FLAG_SECURE` makes screenshots and screen recordings black, which covers iOS's "recording cover". iOS also shows a message while recording. Android needs no equivalent.
- `CaptureMonitor.screenshotCount` is never read anywhere in the iOS app (checked), so don't port the screenshot callback. `CaptureMonitor.kt` only needs to exist if something you port references it. Otherwise leave it out and note that in `PORT_STATUS.md`.

### 16c.2 Deep links

- `jewelindia://auth/callback…` → §12.6.
- `https://app.jewelindia.shop/join/<code>` → `SignupFlow.captureRetailerInvitation` (only when signed out) → the RoleChoice auto-forward picks it up. Parity note: iOS only captures when the link's host equals the `SITE_URL` host. Keep that rule (§18 D11 explains why it is kept even though it currently limits capture).
- App Links verification needs `/.well-known/assetlinks.json` on the domain (§17). Until then Android will offer to open these links in the browser, and the user can still type the code on the Invite Code screen.

### 16c.3 Polish checklist

- Back behaviour per §7.2. Every sheet/dialog closes on back.
- Keyboard: `imePadding()` on every form screen. Next/Done IME actions move focus like iOS `.submitLabel`.
- Pull to refresh wherever iOS has `.refreshable`/`refreshTask`.
- Empty, loading and error states for every list (iOS has them all; port the copy).
- Accessibility: `contentDescription` for every icon button (use the iOS `accessibilityLabel` strings, e.g. "More options", "Continue with Google"), minimum 48dp touch targets, TalkBack labels on custom controls (pill nav, OTP boxes, steppers).
- Font scaling: test at 1.3× font scale. Nothing may clip that doesn't clip on iOS at the equivalent Dynamic Type.
- Process death: `SavedStateHandle` for in-progress form fields that iOS persists (SignupFlow is already persisted). Onboarding discarding on process death is acceptable (parity with iOS "leaving the flow discards it").
- Offline: every network failure shows the iOS copy, and nothing crashes. Test with airplane mode on the emulator.
- Release logging: no `Log.d` of tokens, cookies, request bodies with personal data, or the anon key. Wrap debug logs in `if (BuildConfig.DEBUG)`.

## §16d Phase 10 — Testing, release build, Play Store readiness

### 16d.1 Unit test suite (JVM, `app/src/test/…`)

Minimum set. Each is a port of iOS logic or one of the iOS `tests/` harnesses:

| Test class | Covers | iOS source |
|---|---|---|
| `CredentialsTest` | email regex, Indian mobile branches (`9876543210`, `09876543210`, `919876543210`, `+91 98765 43210`, invalid `5876543210`, short), server password rule | `Core/Credentials.swift` |
| `PasswordRuleTest` | each rule + `allPass` | same |
| `SsrSessionBridgeTest` | §11.2 | `Networking/SSRSessionBridge.swift` |
| `ApiErrorTest` | error/lock parsing | `JewelAPI.throwIfError` |
| `MarketplaceContractTest` | legacy vs v2, filtering, chain aliases | `JewelAPI.PaginatedMarketplaceResponse`, `tests/marketplace-contract` |
| `CatalogueFilterTest` | jewellery-type normalisation | `WholesalerAPI.fetchCatalogue`, `tests/chains` |
| `ProductDisplayImageTest` | variant selection, fallbacks, `canPublish` | `WholesalerModels.swift` |
| `CreditWalletDecodeTest`, `PlanStatusDecodeTest` | tolerant decoding | `TreasureChestModels.swift`, `CreditsAPI.swift` |
| `DailyCreditScheduleTest` | schedule maths | `TreasureChestModels.swift`, `tests/daily-credit-schedule` |
| `WebShuffleTest` | JS-double-exact shuffle | `Features/Employee/Data/WebShuffle.swift` |
| `AuthRouterTest` | §1.2 table | `Core/AuthRouter.swift` |
| `SignupFlowTest` | §12.7 | `Core/SignupFlow.swift` |
| `ChamakFailureMessageTest` | server failure copy surfaces | `tests/chamak-failure-message` |
| `ChamakSourceUploadTest` | path building / lowercase | `tests/chamak-source-upload` |
| `ImageSearchTest` | request building, hydration | `tests/catalogue-image-search` |
| `WishlistShareTest` | share URL resolution, revoke | `tests/wishlist-sharing` |
| `AccountDeletionTest` | edge-function error mapping | `tests/account-deletion` |
| `JsonEncodingTest` | null omission vs explicit `JsonNull` | §7.6 |

Read each `wholesaler ios/tests/<name>/` folder. They are small Swift programs or shell scripts with concrete input/expected pairs. Reuse those exact vectors.

### 16d.2 Device verification

On an emulator (API 36 and API 26 if images are available):

1. Cold launch → front door, no crash, fonts correct.
2. Each role path with test accounts (if provided).
3. Rotate is locked to portrait.
4. Screenshots of protected screens come out black (`adb exec-out screencap`); unprotected screens don't (on API 33+).
5. Recents thumbnail blank (API 33+).
6. Airplane mode → error copy, no crash. Back online → pull to refresh recovers.
7. `adb shell am start -W -a android.intent.action.VIEW -d "jewelindia://auth/callback?code=test"` doesn't crash.
8. `adb shell am start -W -a android.intent.action.VIEW -d "https://app.jewelindia.shop/join/TEST-abc123"` (signed out) → invitation captured.

Record results in `PORT_STATUS.md` with screenshots saved under `docs/screens/` in the Android folder.

### 16d.3 Release build

- `./gradlew :app:assembleRelease` and `:app:bundleRelease` must succeed with R8 on. Fix R8 issues with targeted keep rules (§9.2).
- Signing: read `keystore.properties` (gitignored: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`) **if it exists**. Otherwise produce an unsigned release bundle and say so. **Don't generate the production upload key yourself.** That key is the founder's to create and keep (§17).
- Version: `versionName "1.0"`, `versionCode 1`. Bump `versionCode` on every Play upload.

### 16d.4 Play Store readiness pack (write into `docs/play-store/` in the Android folder)

- `listing.md`: title "Jewel India", short and full description adapted from `wholesaler ios/APP_REVIEW_NOTES_DRAFT.md`.
- `data-safety.md`: what the app collects (name, phone, email, business documents (Aadhaar, PAN, GST), photos, device id for store devices, chat messages, order data), why, and that it is sent over TLS to Supabase/Vercel/Railway. Account deletion is in-app in three places: the retailer menu (`RetailerShell.swift` ~131), the wholesaler profile (`ProfileViews.swift` ~118) and the staff profile (`ProfileViews.swift` ~294). It also needs the web URL the founder provides.
- `review-notes.md`: test account placeholders, how to reach each role, the onboarding fee note.
- `permissions.md`: CAMERA (photograph designs and documents), legacy storage write on API ≤ 28 (save Chamak exports).

---

# Part E — Outside the agent's reach

## §17 Founder-owned steps

The agent **can't and mustn't** do these. They are either outside the Android folder, outside this machine, or decisions with money or legal weight. The app is built to work without each of them, and the right column says how.

| # | Step | Who / where | What the app does until it's done |
|---|---|---|---|
| F1 | Create an **Android OAuth client** in the Google Cloud project that holds the iOS client: package `com.jewelindia.app`, SHA-1 of the debug key (`keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`) and later the release/Play signing key. Give the agent the project's **Web client ID** as `GOOGLE_WEB_CLIENT_ID`, and make sure that web client ID is in Supabase → Auth → Providers → Google → *Authorized Client IDs*. | Google Cloud console, Supabase dashboard | Google sign-in uses the browser flow (Custom Tab → `jewelindia://auth/callback`), exactly like iOS without a native client ID. |
| F2 | Confirm `jewelindia://auth/callback` is in Supabase → Auth → URL Configuration → Redirect URLs. iOS already needs it, so it is probably there. | Supabase dashboard | Browser OAuth and password-reset links won't return to the app. |
| F3 | Publish `https://app.jewelindia.shop/.well-known/assetlinks.json` with package `com.jewelindia.app` and the **release** SHA-256 fingerprint, and restore DNS for `app.jewelindia.shop` (the xcconfig notes it currently has no DNS record). | Web repo / DNS | `/join/<code>` links open in the browser. Retailers type the invite code on the Invite Code screen instead. |
| F4 | Create and keep the **upload keystore**, then enrol in Play App Signing. Put `keystore.properties` (gitignored) in the Android folder when you want signed builds. | Founder | Release builds are produced unsigned. |
| F5 | Play Console: create the app, store listing, data safety form, content rating, target audience, **account-deletion web URL** (Play requires one), privacy policy URL (`https://jewelindia.shop/privacy-policy`). | Play Console | Nothing is published. |
| F6 | Decide whether the **Razorpay onboarding fee** (collected in a browser tab, outside Google Play Billing) is acceptable under Play's payments policy for your case. It is a fee for business verification, not a digital good inside the app, which is usually allowed, but it is your call. | Founder | The fee flow is built exactly as on iOS. |
| F7 | (Optional) Allow `source: "android"` in `Jewel-India-Frontend/app/api/referral/generate/route.js` and the `referral_generate` RPC if you want Android invitations attributed separately. | Web repo | Android sends `source: "web"` (§18 D9). |
| F8 | (Optional) Provide test accounts in `jewel india android app /TEST_ACCOUNTS.local.md` (gitignored): one each of verified wholesaler, verified retailer, active staff, plus a fresh phone/email you are happy to receive OTPs on. | Founder | The agent verifies with previews, unit tests and harmless unauthenticated calls only. |
| F9 | (Later) Push notifications: Firebase project, `google-services.json`, and backend support for FCM tokens in `/api/devices/register`. | Firebase + backend | No push (iOS doesn't request permission either). |

## §18 Decisions already made (and why)

| ID | Decision | Reason |
|---|---|---|
| D1 | **Native Kotlin + Jetpack Compose port**, not a WebView/Capacitor wrapper and not Kotlin Multiplatform. | The founder asked for the iOS app (not the web app) on Android. Compose maps 1:1 onto the SwiftUI code. KMP would mean rewriting the iOS app too. |
| D2 | Application ID `com.jewelindia.app`. | Same as the iOS bundle ID. Simplest for Google OAuth, App Links and support. |
| D3 | `minSdk 26`, portrait-only phones, no tablet-specific layouts in v1. | Covers almost all active Indian Android devices. iOS is portrait-only on phones. |
| D4 | **No Sign in with Apple on Android.** | Not expected on Android. iOS uses Apple's native token flow, which doesn't exist on Android, and the web OAuth variant needs extra Apple/Supabase configuration that may not exist. Apple-only accounts are rare. If one appears, the founder can add it later. |
| D5 | **No in-app credit purchases.** Port only the "Daily credits" information sheet. | On iOS, purchases are retired ("Older callers open the allowance information sheet; purchases are retired"), and `AppleTopUpView`/`TopUpView` are unreachable from the live UI. Plans are informational too (`PlansView`). Credit-funded unlocks (`purchase_entitlement`, `purchase_export`) **are** ported, since they spend existing credits rather than money. |
| D6 | **No push notifications in v1.** | iOS registers an APNs token only if the system hands one over, and it never asks for notification permission, so push is effectively off. FCM needs backend work (F9). |
| D7 | Screen protection = reference-counted `FLAG_SECURE` on the same screens iOS protects + `setRecentsScreenshotEnabled(false)` (API 33+) / permanent `FLAG_SECURE` (API 26–32). | Android's native equivalent of iOS's secure-canvas trick, scoped the same way, so non-design screens stay screenshot-able as on iOS. |
| D8 | All copy in `Copy.kt`/inline Kotlin, verbatim, no `strings.xml` localisation. | Parity and mechanical diffs with iOS. No localisation requirement exists. |
| D9 | `source: "web"` for invitations created on Android. | The server only accepts `ios`/`web` and would 400 on `android` (F7). |
| D10 | "SF Pro" → Android system font (Roboto). | iOS maps `sfPro` to the system face too. Shipping Apple's font is not licensed. |
| D11 | Keep iOS's invitation-link host rule (host must equal the `SITE_URL` host). | Parity. While `SITE_URL` points at Vercel, links on `app.jewelindia.shop` won't auto-capture on either platform. The code path still works the moment the domain is restored, and the founder can widen the rule later on both platforms together. Log this in `OPEN_QUESTIONS.md`. |
| D12 | Not ported: `DebugScreenPeek.swift`, `CaptureSelfTest.swift`, `DebugBuild` tag (keep as a constant only), `FontAudit`, the `JEWEL_MFG_TEST_URL` harness, `EmployeeDetailRegionMarker`, `AppleAuth*`, StoreKit code. | Debug/iOS-only tooling. Compose previews and unit tests replace them. |
| D13 | `clearProductImages` sends explicit `null` for `processed_image_url`. | Matches the documented intent. The iOS encoder omits it (§7.6). |

---

# Appendix

## §A1 Full file-by-file mapping

Every one of the 140 Swift files in `wholesaler ios/JewelIndia/Sources/`, its line count, and its Kotlin destination (under `app/src/main/java/com/jewelindia/app/`). "Split" means one Swift file becomes several Kotlin files along its `struct` boundaries. "—" means not ported (reason in §18 D12).

### App + Core
| iOS | Lines | Kotlin |
|---|---:|---|
| `JewelIndiaApp.swift` | 61 | `JewelApplication.kt`, `MainActivity.kt` |
| `Core/AppConfig.swift` | 61 | `core/config/AppConfig.kt` |
| `Core/AuthRouter.swift` | 202 | `core/session/AuthRouter.kt`, `core/session/ViewModeStore.kt` |
| `Core/Copy.swift` | 268 | `core/text/Copy.kt` |
| `Core/Credentials.swift` | 102 | `core/text/Credentials.kt` |
| `Core/DebugBuild.swift` | 13 | `core/debug/DebugBuild.kt` (constant only) |
| `Core/ImageNormalizer.swift` | 88 | `core/images/ImageNormalizer.kt` |
| `Core/Images/CachedImage.swift` | 41 | `core/images/CachedImage.kt` |
| `Core/Images/ImageCache.swift` | 269 | `core/images/JewelImageLoader.kt` (Coil) |
| `Core/LegalLinks.swift` | 30 | `core/legal/LegalLinks.kt` |
| `Core/Models.swift` | 101 | `core/model/Models.kt` |
| `Core/RefreshTask.swift` | 9 | `core/ui/RefreshContainer.kt` |
| `Core/ScreenProtection/AppSwitcherCover.swift` | 27 | `core/security/ScreenSecurity.kt` |
| `Core/ScreenProtection/CaptureMonitor.swift` | 82 | `core/security/CaptureMonitor.kt` (only if referenced, §16c.1) |
| `Core/ScreenProtection/CaptureSelfTest.swift` | 55 | — |
| `Core/ScreenProtection/ProtectedImageView.swift` | 194 | `core/security/ProtectedImage.kt` |
| `Core/ScreenProtection/SecureLayerHost.swift` | 192 | `core/security/ScreenSecurity.kt` |
| `Core/ScreenProtection/View+CaptureProtected.swift` | 75 | `core/security/CaptureProtected.kt` |
| `Core/SessionStore.swift` | 261 | `core/session/SessionStore.kt` |
| `Core/SignupFlow.swift` | 157 | `core/session/SignupFlow.kt` |

### Design
| iOS | Lines | Kotlin |
|---|---:|---|
| `Design/Layout.swift` | 48 | `design/Layout.kt` |
| `Design/Motion.swift` | 131 | `design/Motion.kt` |
| `Design/Palette.swift` | 74 | `design/Palette.kt` |
| `Design/Typography.swift` | 166 | `design/Typography.kt` |

### Navigation
| iOS | Lines | Kotlin |
|---|---:|---|
| `Navigation/RootView.swift` | 135 | `navigation/RootScreen.kt` |
| `Navigation/AuthFlowView.swift` | 91 | `navigation/AuthNavHost.kt` |
| `Navigation/WholesalerShell.swift` | 235 | `navigation/WholesalerShell.kt`, `navigation/ShellChrome.kt` |
| `Navigation/RetailerShell.swift` | 298 | `navigation/RetailerShell.kt` (+ `PhasePlaceholder` → `navigation/RootScreen.kt`) |
| `Navigation/DebugScreenPeek.swift` | 901 | — (mine its sample data for `@Preview`s) |

### Networking
| iOS | Lines | Kotlin |
|---|---:|---|
| `Networking/SupabaseManager.swift` | 30 | `data/supabase/Supabase.kt` |
| `Networking/NetworkResilience.swift` | 88 | `data/net/JewelNetwork.kt`, `data/net/HttpClients.kt` |
| `Networking/SSRSessionBridge.swift` | 107 | `data/net/SsrSessionBridge.kt` |
| `Networking/JewelAPI.swift` | 780 | `data/api/JewelApi.kt`, `data/api/JewelApiModels.kt`, `data/net/ApiError.kt` |
| `Networking/WholesalerAPI.swift` | 578 | `data/api/WholesalerApi.kt`, `data/api/AccountApi.kt`, `data/net/Multipart.kt` |
| `Networking/ChamakAPI.swift` | 589 | `data/api/ChamakApi.kt` |
| `Networking/CreditsAPI.swift` | 444 | `data/api/CreditsApi.kt`, `data/api/CreditsModels.kt` |
| `Networking/EmployeeAPI.swift` | 382 | `data/api/EmployeeApi.kt`, `data/api/EmployeeModels.kt` |
| `Networking/OrdersChatAPI.swift` | 359 | `data/api/OrdersApi.kt`, `data/api/ChatApi.kt` |
| `Networking/ManufacturingAPI.swift` | 333 | `data/api/ManufacturingApi.kt` |
| `Networking/WishlistAPI.swift` | 270 | `data/api/WishlistApi.kt` |
| `Networking/StaffAccountsAPI.swift` | 207 | `data/api/StaffAccountsApi.kt` |
| `Networking/RetailerAPI.swift` | 168 | `data/api/RetailerApi.kt` |
| `Networking/LikesAPI.swift` | 94 | `data/api/LikeBook.kt`, `feature/retailer/LikeButton.kt` |
| `Networking/InviteAPI.swift` | 88 | `data/api/InviteApi.kt`, `data/api/DbRefusal.kt` |
| `Networking/OnboardingFeeAPI.swift` | 76 | `data/api/OnboardingFeeApi.kt` |
| `Networking/StoreActivity.swift` | 55 | `data/api/StoreActivity.kt` |

### Features/Auth → `feature/auth/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `AuthKit.swift` | 433 | `AuthKit.kt` (split: `AuthLayout.kt`, `AuthFields.kt`, `AuthButtons.kt`) |
| `EntryView.swift` | 343 | `EntryScreen.kt`, `EntryViewModel.kt` (Apple parts —) |
| `GoogleSignIn.swift` | 159 | `GoogleSignIn.kt` |
| `InviteCodeView.swift` | 149 | `InviteCodeScreen.kt` |
| `EmployeeSignInView.swift` | 149 | `EmployeeSignInScreen.kt` |
| `PasswordResetViews.swift` | 254 | `PasswordResetScreens.kt` |
| `RoleChoiceView.swift` | 281 | `RoleChoiceScreen.kt`, `UnreachableScreen.kt`, `InvitationBanner.kt` |
| `SetPasswordView.swift` | 156 | `SetPasswordScreen.kt` |
| `SignInView.swift` | 159 | `SignInScreen.kt` |
| `VerifyOTPView.swift` | 364 | `VerifyOtpScreen.kt`, `VerifyOtpViewModel.kt`, `OtpBoxes.kt` |

### Features/Chat → `feature/chat/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `ChatThreadView.swift` | 439 | `ChatThreadScreen.kt`, `ChatBubble.kt`, `ReportMessageSheet.kt` |
| `ChatThreadsView.swift` | 227 | `ChatThreadsScreen.kt`, `ChatTime.kt` |

### Features/Onboarding → `feature/onboarding/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `OnboardCoordinator.swift` | 38 | `OnboardCoordinator.kt` |
| `OnboardFlow.swift` | 286 | `OnboardFlow.kt` (ViewModel) |
| `OnboardKit.swift` | 612 | `OnboardKit.kt` (split: `OnboardLayout.kt`, `ImageUploadBox.kt`, `OnboardFields.kt`) |
| `OnboardSteps.swift` | 259 | `OnboardSteps.kt` |
| `OnboardSubmittedView.swift` | 460 | `OnboardSubmittedScreen.kt`, `VerificationTimeline.kt` |
| `OnboardingFeeSheet.swift` | 130 | `OnboardingFeeSheet.kt` |

### Features/RetailerOnboarding → `feature/retaileronboarding/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `RetailerOnboardCoordinator.swift` | 41 | `RetailerOnboardCoordinator.kt` |
| `RetailerOnboardFlow.swift` | 248 | `RetailerOnboardFlow.kt` |
| `RetailerOnboardSteps.swift` | 263 | `RetailerOnboardSteps.kt` |
| `RetailerOnboardSubmittedView.swift` | 364 | `RetailerOnboardSubmittedScreen.kt` |

### Features/Profile → `feature/profile/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `ProfileViews.swift` | 420 | `WholesalerProfileScreen.kt`, `StaffProfileScreen.kt`, `ProfileParts.kt` |

### Features/Wholesaler → `feature/wholesaler/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `AddProductForm.swift` | 388 | `AddProductForm.kt` (ViewModel) |
| `AddProductView.swift` | 668 | `AddProductScreen.kt`, `AddProductSections.kt`, `AddProductSuccess.kt` |
| `EditProductView.swift` | 305 | `EditProductScreen.kt` |
| `FormComponents.swift` | 315 | `design/components/FormComponents.kt` |
| `InviteRetailerSheet.swift` | 469 | `InviteRetailerSheet.kt`, `InvitationHistoryScreen.kt`, `InviteRetailerCard.kt` |
| `ProductDetailSheet.swift` | 540 | `ProductDetailSheet.kt`, `ReprocessFlow.kt` |
| `UploadHistoryView.swift` | 163 | `UploadHistoryScreen.kt` |
| `WholesalerCatalogueView.swift` | 576 | `WholesalerCatalogueScreen.kt`, `CatalogueViewModel.kt`, `CatalogueProductCard.kt`, `CatalogueExport.kt` |
| `WholesalerHomeView.swift` | 707 | `WholesalerHomeScreen.kt`, `HomeViewModel.kt`, `HomeCards.kt` |
| `WholesalerModels.swift` | 444 | `core/model/Product.kt`, `core/model/WholesalerModels.kt`, `core/text/StringExt.kt` |
| `WholesalerOrdersView.swift` | 835 | `WholesalerOrdersScreen.kt`, `WholesalerOrdersStore.kt`, `WholesalerOrderDetail.kt`, `RejectOrderSheet.kt`, `OrderParts.kt` (timeline, badge, thumb, `OrderTime`) |
| `Report/WholesalerReport.swift` | 514 | `report/WholesalerReport.kt` (model), `report/WholesalerReportScreen.kt`, `report/WholesalerReportTeaser.kt` |
| `TreasureChest/CreditBalancePill.swift` | 73 | `treasurechest/CreditBalancePill.kt` |
| `TreasureChest/CreditStore.swift` | 98 | `treasurechest/CreditStore.kt` |
| `TreasureChest/InsufficientCreditsSheet.swift` | 135 | `treasurechest/InsufficientCreditsSheet.kt` |
| `TreasureChest/TopUpSheet.swift` | 1020 | `treasurechest/DailyCreditsSheet.kt` (lines 1–190 only; rest —) |
| `TreasureChest/TransactionHistoryView.swift` | 308 | `treasurechest/TransactionHistoryScreen.kt` |
| `TreasureChest/TreasureChestCard.swift` | 106 | `treasurechest/TreasureChestCard.kt` |
| `TreasureChest/TreasureChestModels.swift` | 256 | `data/api/CreditsModels.kt` |
| `TreasureChest/TreasureChestView.swift` | 387 | `treasurechest/TreasureChestScreen.kt` |

### Features/Wholesaler/Chamak → `feature/wholesaler/chamak/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `ChamakCatalogPickerView.swift` | 750 | `ChamakCatalogPickerScreen.kt`, `ChamakPickerParts.kt` |
| `ChamakExportButton.swift` | 143 | `ChamakExportButton.kt` |
| `ChamakFeedbackSheet.swift` | 98 | `ChamakFeedbackSheet.kt` |
| `ChamakFlowCoordinator.swift` | 88 | `ChamakFlowCoordinator.kt` |
| `ChamakGalleryCard.swift` | 154 | `ChamakGalleryCard.kt` |
| `ChamakGalleryView.swift` | 156 | `ChamakGalleryScreen.kt` |
| `ChamakGeneratingView.swift` | 357 | `ChamakGeneratingScreen.kt`, `GemShapes.kt` |
| `ChamakHubView.swift` | 413 | `ChamakHubScreen.kt`, `ChamakToolCards.kt` |
| `ChamakImageViewer.swift` | 408 | `ChamakImageViewer.kt` |
| `ChamakModels.swift` | 572 | `ChamakModels.kt` (split: `ChamakEnums.kt`, `ChamakGeneration.kt`) |
| `ChamakResultView.swift` | 431 | `ChamakResultScreen.kt` |
| `ChamakSetStylingView.swift` | 362 | `ChamakSetStylingScreen.kt` |
| `ChamakSliderFormView.swift` | 624 | `ChamakSliderFormScreen.kt`, `ChamakSliders.kt` |
| `ChamakViewModel.swift` | 885 | `ChamakViewModel.kt`, `ChamakGenerationRunner.kt`, `ChamakQuotes.kt` |

### Features/Manufacturing → `feature/manufacturing/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `AcceptQuoteSheet.swift` | 206 | `AcceptQuoteSheet.kt` |
| `CreateManufacturingRequestSheet.swift` | 520 | `CreateManufacturingRequestSheet.kt`, `ManufacturingFormParts.kt` |
| `DeclineOfferSheet.swift` | 139 | `DeclineOfferSheet.kt` |
| `ManufacturingModels.swift` | 317 | `ManufacturingModels.kt` |
| `ManufacturingOffersCard.swift` | 81 | `ManufacturingOffersCard.kt` |
| `RetailerRequestDetailView.swift` | 449 | `RetailerRequestDetailScreen.kt` |
| `RetailerRequestsListView.swift` | 247 | `RetailerRequestsListScreen.kt` |
| `WholesalerOfferDetailView.swift` | 295 | `WholesalerOfferDetailScreen.kt` |
| `WholesalerOffersListView.swift` | 260 | `WholesalerOffersListScreen.kt` |

### Features/Retailer → `feature/retailer/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `AddDesignSheet.swift` | 232 | `AddDesignSheet.kt` |
| `AddEmployeeSheet.swift` | 240 | `AddEmployeeSheet.kt` |
| `CatalogueImageSearch.swift` | 201 | `CatalogueImageSearch.kt` |
| `EmployeesListView.swift` | 448 | `EmployeesListScreen.kt`, `StaffCredentialsScreen.kt` |
| `MarketplaceCatalogueStore.swift` | 374 | `MarketplaceCatalogueStore.kt` |
| `NewArrivalsStrip.swift` | 82 | `NewArrivalsStrip.kt` |
| `PlansView.swift` | 26 | `PlansScreen.kt` |
| `RetailerCatalogueView.swift` | 290 | `RetailerCatalogueScreen.kt` |
| `RetailerDashboardView.swift` | 138 | `RetailerDashboardScreen.kt` |
| `RetailerOrdersView.swift` | 271 | `RetailerOrdersScreen.kt`, `RetailerOrdersStore.kt` |
| `StoreThemeView.swift` | 671 | `StoreThemeScreen.kt`, `ThemeSheets.kt`, `ThemeOptions.kt` |
| `YourTasteView.swift` | 864 | `YourTasteScreen.kt`, `MarketplaceProductCard.kt`, `MarketplaceProductDetail.kt`, `RetailerOrderRequestSheet.kt` |
| `Wishlist/CustomerBoardsView.swift` | 266 | `wishlist/CustomerBoardsScreen.kt` |
| `Wishlist/CustomerWishlistView.swift` | 316 | `wishlist/CustomerWishlistScreen.kt`, `wishlist/AddCustomerSheet.kt` |
| `Wishlist/WishlistShareView.swift` | 115 | `wishlist/WishlistShareScreen.kt` |

### Features/Employee → `feature/employee/`
| iOS | Lines | Kotlin |
|---|---:|---|
| `ComingSoonView.swift` | 33 | `ComingSoonScreen.kt` |
| `Data/WebShuffle.swift` | 52 | `WebShuffle.kt` |
| `EmployeeAppearance.swift` | 565 | `EmployeeAppearance.kt`, `EmployeeMotifs.kt` |
| `EmployeeCatalogueView.swift` | 514 | `EmployeeCatalogueScreen.kt`, `PinCard.kt` |
| `EmployeeDesignsView.swift` | 291 | `EmployeeDesignsScreen.kt` |
| `EmployeeDetailViews.swift` | 667 | `EmployeeDetailLayout.kt`, `EmployeeDesignDetail.kt`, `JewelFullImageViewer.kt`, `DetailSpec.kt`, `GlassButtons.kt` |
| `EmployeeFilters.swift` | 356 | `EmployeeFilters.kt` |
| `EmployeeHomeView.swift` | 593 | `EmployeeHomeScreen.kt`, `DesignerCollectionSection.kt` |
| `EmployeeInfiniteCanvas.swift` | 373 | `EmployeeInfiniteCanvas.kt` |
| `EmployeeOrdersView.swift` | 153 | `EmployeeOrdersScreen.kt` |
| `EmployeeReviewView.swift` | 907 | `EmployeeReviewScreen.kt`, `SelectionCard.kt`, `RequestSentScreen.kt` |
| `EmployeeShell.swift` | 390 | `EmployeeShell.kt`, `EmployeePillNav.kt` |
| `EmployeeStore.swift` | 218 | `EmployeeStore.kt` |

## §A2 Asset and font mapping

### A2.1 Fonts → `app/src/main/res/font/`

| iOS file (`Resources/Fonts/`) | Android resource |
|---|---|
| `Cirka-Light.ttf` / `Cirka-Regular.ttf` / `Cirka-Bold.ttf` | `cirka_light.ttf` / `cirka_regular.ttf` / `cirka_bold.ttf` |
| `Manrope-ExtraLight.ttf` … `Manrope-ExtraBold.ttf` (7) | `manrope_extralight.ttf`, `manrope_light.ttf`, `manrope_regular.ttf`, `manrope_medium.ttf`, `manrope_semibold.ttf`, `manrope_bold.ttf`, `manrope_extrabold.ttf` |
| `Gilroy-Regular/Medium/SemiBold/Bold.ttf` | `gilroy_regular.ttf`, `gilroy_medium.ttf`, `gilroy_semibold.ttf`, `gilroy_bold.ttf` |
| `Satoshi-Light/Regular/Medium/Bold/Black.ttf` | `satoshi_light.ttf`, `satoshi_regular.ttf`, `satoshi_medium.ttf`, `satoshi_bold.ttf`, `satoshi_black.ttf` |
| `GildaDisplay-Regular.ttf` | `gilda_display_regular.ttf` |
| `EmployeeMatter-Regular/Medium/Bold.ttf` | `employee_matter_regular.ttf`, `employee_matter_medium.ttf`, `employee_matter_bold.ttf` |
| `EmployeeSeasonMix-Regular.ttf` | `employee_season_mix_regular.ttf` |

24 files in total. Font licences: these files already ship in the iOS app, so the same licence terms apply to Android. Note this in `README.md`.

### A2.2 Images (`Resources/Assets.xcassets/*.imageset`)

Name the Android asset after the **image set**, not the file inside it (several sets contain `card-art.png`, `checked.svg`, and so on).

- **SVG → `app/src/main/assets/svg/<SetName>.svg`** (22): `ChamakStudioArrow`, `EmpNavCatalogue`, `EmpNavHome`, `EmpNavOrders`, `EmpNavPerson`, `EmpNavQueries`, `InviteChecked`, `InviteConnector`, `InviteCopy`, `InviteMinus`, `InvitePlus`, `InviteStepDivider`, `InviteUserPlus`, `JewelLogo`, `NavAddRetailer`, `NavCatalogue`, `NavChat`, `NavHome`, `NavOrders`, `NavUpload`, `UploadDesignIcon`, `UploadDesignWash`.
- **PNG/JPG → `app/src/main/res/drawable-nodpi/<snake_case>.<ext>`** (22): `auth_panel_image.jpg`, `hero_frame.jpg`, `cat_bangles.png`, `cat_chains.png`, `cat_earrings.png`, `cat_haram.png`, `cat_mangalsutras.png`, `cat_necklace.png`, `cat_nosepins.png`, `cat_pendants.png`, `cat_rings.png`, `chamak_necklace.png`, `chamak_studio_artwork.png`, `google_g.png` (from `GoogleG@3x.png`), `invite_card_artwork.png` (`card-art.png`), `invite_guide_link.png` (`link.png`), `invite_guide_signup.png` (`signup.png`), `invite_guide_store.png` (`store.png`), `invite_reward_artwork.png` (`necklace.png`), `upload_design_coin.png`, `upload_design_left_hand.png`, `upload_design_right_hand.png`.
- **Template rendering (checked):** these 11 sets declare `template-rendering-intent` in `Contents.json` and must be drawn **tinted** with the current tab/menu colour (`AsyncImage(…, colorFilter = ColorFilter.tint(color))`): `NavHome`, `NavCatalogue`, `NavOrders`, `NavChat`, `NavUpload`, `NavAddRetailer`, `EmpNavHome`, `EmpNavCatalogue`, `EmpNavOrders`, `EmpNavQueries`, `EmpNavPerson`. Every other asset is drawn in its own colours.
- `AccentColor.colorset` and `LaunchBackground.colorset` (`#FEFEFE`) → `res/values/colors.xml`.
- `AppIcon.appiconset/AppIcon-1024.png` → launcher icons (§10.1 step 3).
- Remote artwork (Cloudinary category tiles, employee theme art, store theme art) is loaded by URL exactly as iOS builds it (`CloudinaryArt.url`, `ThemeOption.imageURL`, `EmployeeCategoryArt`). Don't bundle it.

### A2.3 SF Symbols used in shells and menus → Material icons

| SF Symbol | Material icon |
|---|---|
| `sparkles` | `Icons.Outlined.AutoAwesome` |
| `person.crop.circle` | `Icons.Outlined.AccountCircle` |
| `hammer` | `Icons.Outlined.Construction` |
| `rectangle.portrait.and.arrow.right` | `Icons.AutoMirrored.Outlined.Logout` |
| `square.grid.2x2.fill` / `square.grid.2x2` | `Icons.Filled.GridView` / `Icons.Outlined.GridView` |
| `person.2` | `Icons.Outlined.People` |
| `heart.text.square` | `Icons.Outlined.FavoriteBorder` |
| `bag` | `Icons.Outlined.ShoppingBag` |
| `bubble.left.and.bubble.right` | `Icons.Outlined.Forum` |
| `shippingbox` | `Icons.Outlined.Inventory2` |
| `crown` | `Icons.Outlined.WorkspacePremium` |
| `paintpalette` | `Icons.Outlined.Palette` |
| `person.crop.rectangle` | `Icons.Outlined.Badge` |
| `trash` | `Icons.Outlined.Delete` |

For any other symbol you meet in feature files, pick the closest Material Symbols icon and add it to this table in `PORT_STATUS.md`.

## §A3 `PORT_STATUS.md` template

```markdown
# Jewel India Android — Port Status

Last updated: <date> · Current phase: <n> · iOS reference build: 40 (feat/retailer-monetisation)

## Phase checklist
| Phase | Status | Build | Tests | Commit | Notes |
|---|---|---|---|---|---|
| 0 Scaffold | ✅/🟡/⬜ | ok | n/n | <sha> | |
| 1 Design + core | | | | | |
| 2 Data layer | | | | | |
| 3 Auth + routing | | | | | |
| 4 Onboarding | | | | | |
| 5 Wholesaler | | | | | |
| 6 Chamak | | | | | |
| 7 Retailer | | | | | |
| 8 Employee | | | | | |
| 9 Protection/links/polish | | | | | |
| 10 Tests/release | | | | | |

## File coverage
<copy §A1 tables; mark each row ✅ ported / 🟡 partial / ⛔ skipped (reason)>

## Deviations from iOS
- <what, where, why>

## Version changes from §4.2
- <library: planned → actual, why>

## Verification log
- <date> <what was checked, on which device/API level, result, screenshot path>
```

`OPEN_QUESTIONS.md` format: one entry per question with **Context**, **What I did instead**, and **What would settle it**.

---

*End of plan. Start with §0, then §9.*

