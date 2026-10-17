# Jewel India — Android Application

Native Android app for Jewel India (Wholesaler, Retailer, and Staff) built with Kotlin and Jetpack Compose, ported from the native iOS codebase.

## Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK with Platforms 36, build-tools 36.x installed at `~/Library/Android/sdk`
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
