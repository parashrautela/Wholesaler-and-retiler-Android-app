# Jewel India Android — Port Status

Last updated: 2026-10-17 · Current phase: 0 (Complete) · iOS reference build: 40 (feat/retailer-monetisation)

## Phase checklist
| Phase | Status | Build | Tests | Commit | Notes |
|---|---|---|---|---|---|
| 0 Scaffold | ✅ | ok | 0/0 | pending | Empty Compose app running on Pixel 6 AVD, displaying LaunchScreen with Cirka 32 |
| 1 Design + core | ⬜ | | | | |
| 2 Data layer | ⬜ | | | | |
| 3 Auth + routing | ⬜ | | | | |
| 4 Onboarding | ⬜ | | | | |
| 5 Wholesaler | ⬜ | | | | |
| 6 Chamak | ⬜ | | | | |
| 7 Retailer | ⬜ | | | | |
| 8 Employee | ⬜ | | | | |
| 9 Protection/links/polish | ⬜ | | | | |
| 10 Tests/release | ⬜ | | | | |

## Deviations from iOS
- None.

## Version changes from §4.2
- `compileSdk`: updated from 36 to 37 (automatically downloaded platform 37.0 to satisfy Compose 1.12.1 / Core 1.19.1 dependencies).

## Verification log
- 2026-10-17: Phase 0 verified on Pixel_6 emulator (API 34/35). APK installed, launched, and visual inspection of `captures/phase0.png` confirmed centered "Jewel India" in Cirka font against `#FEFEFE` background.
