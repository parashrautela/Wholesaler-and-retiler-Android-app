# Android endpoint reference inventory

18 October 2026 · Static Android source references only.

**No endpoint was contacted, and no tests were run. Every live availability status is unknown.** This list extracts literal route references; SDK-generated authentication/storage/PostgREST URLs and runtime configuration values are not evaluated. Multiple HTTP methods can share a route.

Base URLs are configured by `SITE_URL`, `AI_PIPELINE_URL`, and `SUPABASE_URL`. Deployed values and secrets were not opened. See [the flow review](ANDROID_FLOW_REVIEW.md) for blockers and caller behavior.

## HTTP route patterns

| Reference | Android source location(s) |
| --- | --- |
| `/api/auth/check-user` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:66` |
| `/api/auth/send-otp` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:72` |
| `/api/auth/verify-otp` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:87` |
| `/api/catalogue-shares` | `app/src/main/java/com/jewelindia/app/data/api/CatalogueShareApi.kt:69`; `app/src/main/java/com/jewelindia/app/data/api/CatalogueShareApi.kt:80` |
| `/api/catalogue-shares/$trimmedId/revoke` | `app/src/main/java/com/jewelindia/app/data/api/CatalogueShareApi.kt:87` |
| `/api/chamak/analyze` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:185` |
| `/api/chamak/generate-v2` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:292` |
| `/api/devices/register` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:216` |
| `/api/orders/create` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:424`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:441` |
| `/api/referral/claim` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:334` |
| `/api/referral/generate` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:262` |
| `/api/referral/manage` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:191`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:201` |
| `/api/referral/manage?page=$page` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:121` |
| `/api/retailer/image-search` | `app/src/main/java/com/jewelindia/app/feature/retailer/CatalogueImageSearch.kt:164`; `app/src/main/java/com/jewelindia/app/feature/retailer/CatalogueImageSearch.kt:200` |
| `/api/retailer/manufacturing-assets` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:87` |
| `/api/retailer/manufacturing-requests` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:120`; `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:138` |
| `/api/retailer/manufacturing-requests/$id` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:149` |
| `/api/retailer/manufacturing-requests/$id/cancel` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:159` |
| `/api/retailer/manufacturing-requests/$requestId/award` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:164` |
| `/api/retailer/marketplace` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:339`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:349` |
| `/api/retailer/marketplace/$id` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:390` |
| `/api/retailer/marketplace/categories` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:364` |
| `/api/retailer/marketplace/hydrate` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:374` |
| `/api/retailer/orders` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:446` |
| `/api/retailer/your-taste` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:406` |
| `/api/set-creation/generate` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:362` |
| `/api/upload-usage?wholesaler_id=$wholesalerId` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:313` |
| `/api/wholesaler/manufacturing-offers` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:170` |
| `/api/wholesaler/manufacturing-offers/$id` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:181` |
| `/api/wholesaler/manufacturing-offers/$id/decline` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:210` |
| `/api/wholesaler/manufacturing-offers/{offerId}/accept` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:191` (parallel = false) |
| `/api/wholesaler/manufacturing-offers/{offerId}/quotes` | `app/src/main/java/com/jewelindia/app/data/api/ManufacturingApi.kt:191` (parallel = true) |
| `/api/wishlist-shares` | `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:38` |
| `/api/wishlist-shares/$id/revoke` | `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:53` |
| `/api/wishlist-shares?board_id=$boardId` | `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:46` |
| `/process` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:354` |
| `/process/$productId` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:421` |
| `/product/$id` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:400` |

## Supabase Edge Function names

| Reference | Android source location(s) |
| --- | --- |
| `credits-topup` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:155`; `app/src/main/java/com/jewelindia/app/data/api/OnboardingFeeApi.kt:57` |
| `delete-account` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:524` |
| `staff-accounts` | `app/src/main/java/com/jewelindia/app/data/api/StaffAccountsApi.kt:69` |

## Supabase database function names (RPC)

| Reference | Android source location(s) |
| --- | --- |
| `attach_referral_code` | `app/src/main/java/com/jewelindia/app/data/api/InviteApi.kt:54` |
| `chat_block` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:126` |
| `chat_block_state` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:119` |
| `chat_mark_read` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:150` |
| `chat_my_blocked_threads` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:144` |
| `chat_open` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:46` |
| `chat_report_message` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:98` |
| `chat_send` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:74` |
| `chat_threads` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:42` |
| `chat_unblock` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:135` |
| `claim_staff_invite` | `app/src/main/java/com/jewelindia/app/core/session/SessionStore.kt:289` |
| `credits_history` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:46` |
| `credits_wallet` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:20` |
| `employee_store_profile` | `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:70` |
| `my_plan` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:98` |
| `order_set_status` | `app/src/main/java/com/jewelindia/app/data/api/OrdersApi.kt:29` |
| `purchase_entitlement` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:78` |
| `purchase_export` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:85` |
| `register_device` | `app/src/main/java/com/jewelindia/app/data/api/StoreActivity.kt:64` |
| `set_my_role` | `app/src/main/java/com/jewelindia/app/core/session/SessionStore.kt:272`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:471` |
| `set_plan_auto_renew` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:109` |
| `subscribe_plan` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:102` |
| `toggle_product_like` | `app/src/main/java/com/jewelindia/app/data/api/LikeBook.kt:61` |
| `validate_referral_code` | `app/src/main/java/com/jewelindia/app/data/api/InviteApi.kt:38` |
| `wholesaler_orders` | `app/src/main/java/com/jewelindia/app/data/api/OrdersApi.kt:41` |
| `wholesaler_report` | `app/src/main/java/com/jewelindia/app/data/api/WholesalerReport.kt:52` |

## Database / storage resource names

| Reference | Android source location(s) |
| --- | --- |
| `chamak-outputs` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:414` |
| `chamak_feedback` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:441` |
| `chamak_generations` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:172`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:235`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:256`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:283`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:353`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:448` |
| `conversations` | `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:149`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:114` |
| `credit_prices` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:28` |
| `credit_purchases` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:143` |
| `customer_board_items` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:91`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:182`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:186` |
| `customer_boards` | `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:134`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:162`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:166` |
| `employees` | `app/src/main/java/com/jewelindia/app/core/session/SessionStore.kt:211`; `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:30`; `app/src/main/java/com/jewelindia/app/data/api/StaffAccountsApi.kt:36`; `app/src/main/java/com/jewelindia/app/feature/profile/StaffProfileScreen.kt:121` |
| `entitlements` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:60` |
| `messages` | `app/src/main/java/com/jewelindia/app/data/api/ChatApi.kt:64` |
| `orders` | `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:171`; `app/src/main/java/com/jewelindia/app/data/api/OrdersApi.kt:45`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:96` |
| `plans` | `app/src/main/java/com/jewelindia/app/data/api/CreditsApi.kt:92` |
| `plant-images` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:126`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:288` |
| `product_likes` | `app/src/main/java/com/jewelindia/app/data/api/LikeBook.kt:38` |
| `products` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:69`; `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:97`; `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:126`; `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:137`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:161`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:181`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:202`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:230`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:243`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:252`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:465`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:197` |
| `referral_links` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:150`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:236`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:307` |
| `retailer-designs` | `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:150`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:84`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:87` |
| `retailer_customers` | `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:107`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:111`; `app/src/main/java/com/jewelindia/app/data/api/WishlistApi.kt:97` |
| `retailer_designs` | `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:102`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:106`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:128`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:57` |
| `retailer_selections` | `app/src/main/java/com/jewelindia/app/data/api/ChamakApi.kt:87`; `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:117` |
| `retailers` | `app/src/main/java/com/jewelindia/app/data/api/EmployeeApi.kt:84`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:162`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:179`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:20`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:34`; `app/src/main/java/com/jewelindia/app/data/api/RetailerApi.kt:45`; `app/src/main/java/com/jewelindia/app/feature/retailer_onboarding/RetailerOnboardFlow.kt:241`; `app/src/main/java/com/jewelindia/app/feature/retailer_onboarding/RetailerOnboardSubmittedScreen.kt:111` |
| `store_events` | `app/src/main/java/com/jewelindia/app/data/api/StoreActivity.kt:42` |
| `wholesalers` | `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:130`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:221`; `app/src/main/java/com/jewelindia/app/data/api/JewelApi.kt:272`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:54`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:63`; `app/src/main/java/com/jewelindia/app/data/api/WholesalerApi.kt:78`; `app/src/main/java/com/jewelindia/app/feature/onboarding/OnboardFlow.kt:246`; `app/src/main/java/com/jewelindia/app/feature/onboarding/OnboardSubmittedScreen.kt:105` |

