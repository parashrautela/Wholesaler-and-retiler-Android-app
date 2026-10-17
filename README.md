# Jewel India — Android Application

Native Android app for Jewel India (Wholesaler, Retailer, and Staff) built with Kotlin and Jetpack Compose, ported from the native iOS codebase.

## Features & Modules
- **Design System & Aesthetics**: Cirka serif typography, curated jewel warm palette, dark/gold luxury themes, responsive edge-to-edge layouts, and custom jali geometric patterns.
- **Authentication & Onboarding**: Email/password, OTP, Google sign-in, multi-step role-specific onboarding flows for Wholesalers & Retailers with Razorpay fee integration via Custom Tabs, and verification status timeline.
- **Wholesaler Portal**: Complete catalogue management (add/edit/delete/in-stock toggles), bulk PDF catalogue exports, order workflow management, retail partner invites, and business performance reports.
- **Chamak Studio**: AI product background generation with style presets (Courtyard, Velvet, Royal), credit balance tracking, and image variant assignment.
- **Retailer Portal**: Discovery feed with WebShuffle deterministic sorting, image search, dynamic wishlist with shareable links, real-time enquiry chat, manufacturing custom request flow with wholesaler offer acceptance, staff management, and custom store themes.
- **Staff / Employee App**: Role-tailored personal styles (Classic, Sarvam, Modern), 2D infinite draggable canvas with wrap-around coordinates, 2-column staggered catalogue grid, long-press item selection tray, and batch order dispatch (up to 20 pieces).
- **Security & Protection**: Sensitive screenshot blocking with `FLAG_SECURE` and `setRecentsScreenshotEnabled(false)` (API 33+), secure deep-link routing, and encrypted local preferences.

## Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK with Platforms 36/37, build-tools installed at `~/Library/Android/sdk`
- JDK 21 (bundled JBR at `/Applications/Android Studio.app/Contents/jbr/Contents/Home`)

## Setup
1. Copy `secrets.example.properties` to `secrets.properties` and fill in:
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`
   - `AI_PIPELINE_URL`
   - `SITE_URL`
   - `GOOGLE_WEB_CLIENT_ID` (optional)
2. Verify `local.properties` points to your Android SDK (`sdk.dir=/Users/parashrautela/Library/Android/sdk`).
3. Assets: Run `./tools/copy_assets.sh` and `./tools/make_launcher_icons.sh`.

## Building & Testing
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export ANDROID_HOME="/Users/parashrautela/Library/Android/sdk"

# Build debug APK
./gradlew :app:assembleDebug

# Run unit tests
./gradlew :app:testDebugUnitTest

# Run lint
./gradlew :app:lintDebug
```

## Fonts & Licences
The bundled fonts in `app/src/main/res/font/` (Cirka, Manrope, Gilroy, Satoshi, Gilda Display, Employee Matter, Employee Season Mix) are the same assets licensed and bundled in the iOS app (`wholesaler ios/JewelIndia/Resources/Fonts/`).

