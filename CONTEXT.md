# GreenCoins Project – Chat Context

Use this in a **new Cursor window** (e.g. paste into the chat or reference with `@CONTEXT.md`) to continue working as if we’ve been chatting here.

---

## What This Project Is

- **GreenCoins** – Android app that rewards environmental actions (missions, challenges, shop, profile).
- **Origin:** Converted from a **React/TypeScript + Vite** web app into **Kotlin + Jetpack Compose**.
- **Goal:** Same screens, flows, copy, and behavior as the original, implemented natively on Android.

---

## Project Location & Repo

- **GitHub repo:** **GreenCoin** (exact name)
  - URL: `https://github.com/YOUR_USERNAME/GreenCoin`
- You may be in the **original** folder (`...\new proj\kotlin-app`) or a **clean clone** (any path where you ran `git clone`). Both are the same codebase.
- **Initial commit:** “Initial commit: GreenCoins Android app (Kotlin + Compose)” – code is pushed and buildable.

---

## Tech Stack

- **Kotlin 1.9**, **Jetpack Compose** (Material3)
- **Coil** for image loading (with error fallback)
- **minSdk 26**, **targetSdk 34**, **compileSdk 34**
- **Gradle 8.7**, **Android Gradle Plugin 8.5.0**

---

## Project Structure (What’s in the Repo)

```
kotlin-app/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/greencoins/app/
│       │   ├── MainActivity.kt
│       │   ├── GreenCoinsApp.kt           # Root composable, navigation, state
│       │   ├── components/               # GlassCard, Header, BottomNav, ImageWithFallback
│       │   ├── data/Models.kt            # Screen, Mission, Reward, CHALLENGES, etc.
│       │   ├── screens/                  # Auth, Home, Shop, PlusFlow, Profile, Challenges, Help
│       │   ├── theme/                    # AppColors, GreenCoinsTheme
│       │   └── ui/Icons.kt               # MissionIcon → Material icons, NavIcons
│       └── res/values/                   # strings.xml, themes.xml
├── gradle/wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties                     # android.useAndroidX, org.gradle.jvmargs (2GB heap)
├── .gitignore                            # build/, .gradle/, local.properties, .idea, etc.
└── README.md
```

---

## What Was Done in This Chat

1. **Conversion:** Full React/TS app → Kotlin Compose; all screens and flows preserved.
2. **Fixes:** GlassCard `content` → trailing lambda; Header `Box` import + empty Box content; ProfileScreen `horizontalAlignment` → `horizontalArrangement`; ShopScreen Preview/GreenCoinsTheme imports.
3. **Gradle:** Wrapper set to **8.7**; **gradle.properties** created with `android.useAndroidX=true` and `org.gradle.jvmargs=-Xmx2048m ...` to fix “JVM thrashing” build failures.
4. **Previews:** `@Preview` added for **AuthScreen**, **HomeScreen**, **ShopScreen** so they can be viewed in Android Studio without a device.
5. **Git:** Repo initialized in `kotlin-app`, `.gitignore` added, initial commit created, user instructed to create **GreenCoin** on GitHub and push (`main` branch).

---

## Current State

- **Build:** Succeeds with the current Gradle/memory settings.
- **Run:** App runs on Pixel emulator (and can run on a physical device).
- **Git:** Code is committed and pushed to **GreenCoin**; repo contains all source and config needed to clone and build.

---

## Useful Conventions in This Codebase

- **Screens:** Auth, Home, Shop, Plus (selection → brief → upload → success), Challenges, Profile, Help.
- **State:** `Screen` enum, `PlusStep` sealed class, coins, `selectedMissionId`; handled in `GreenCoinsApp.kt`.
- **Theme:** `AppColors` (e.g. `#0D0D0D`, `#A2FF00`), `GreenCoinsTheme` wraps MaterialTheme.
- **Data:** `MissionsData`, `RewardsData`, `ChallengesData` in `data/Models.kt`; same content as original app.

---

## If You Need To…

- **Open in Android Studio:** File → Open → select `kotlin-app` folder.
- **See UI without device:** Open a screen file (e.g. `AuthScreen.kt`), switch to **Split** or **Design**, then **Build & Refresh** in the Preview pane.
- **Run on emulator:** Choose the **app** run configuration and a Pixel (or other) device, then Run (▶).
- **Push changes:** `git add -A`, `git commit -m "..."`, `git push origin main`.

Use this file as the context when continuing in a new Cursor window or in a clean clone.
