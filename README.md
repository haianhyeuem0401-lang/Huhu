# QBaoTweak X Vft — Dynamic Key UI

Android source project using Kotlin + Jetpack Compose.

## UI updates
- Animated neon key activation screen branded `QBAOTWEAK X VFT`.
- Dynamic control panel with `NEON AIMLOCK`, `SENSI OVERDRIVE`, `FRAME OPTIMIZER`, and `GAME PROFILE`.
- Admin information screen labeled `ADMIN CONTROL • PHẠM GIA BẢO`.
- Key validation remains connected to the configured backend endpoint.

## Important
The uploaded reference MP4 appears to show a person speaking and does not show a key/menu interface, so these changes follow the requested dynamic neon style rather than reproducing a UI from that clip. The admin screen is currently an informational UI, not a working key-management dashboard. Creating/extending/revoking keys requires authenticated admin endpoints on the backend. Build and test the Android app in Android Studio before installing.

## Build
Open this folder in Android Studio and sync Gradle. Configure `API_BASE_URL` in `app/build.gradle.kts` to your backend URL before building.
