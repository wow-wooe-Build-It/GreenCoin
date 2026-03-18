# GreenCoins – Kotlin (Android) Port

This is a **Kotlin** port of the GreenCoins React/TypeScript app. Every screen, flow, and UI element from the original is preserved and implemented in **Android** with **Jetpack Compose**.

## What’s preserved

- **Screens:** Auth, Home, Shop, Plus (mission selection → brief → upload → success), Challenges, Profile, Help  
- **Data:** Same missions, rewards, challenges (MISSIONS, REWARDS, CHALLENGES)  
- **Colors & theme:** `#0D0D0D`, `#A2FF00`, `#1F1F1F`, etc.  
- **Components:** GlassCard, Header, BottomNav, ImageWithFallback  
- **Copy:** All labels, titles, and FAQ text match the original  
- **Navigation & state:** Same flow (auth → home, mission selection → brief → upload → success, help, logout)

## How to run

1. Open the project in **Android Studio** (open the `kotlin-app` folder as the root).
2. Use a device or emulator with **API 26+**.
3. Run the **app** configuration (e.g. Run ▶️).

If the Gradle wrapper is missing, create it in Android Studio: **File → New → New Project**, then replace its contents with this project, or run in a terminal (from repository root):

```bash
cd kotlin-app
gradle wrapper
```

Then build from Android Studio or:

```bash
./gradlew assembleDebug
```

## Structure

- `app/src/main/java/com/greencoins/app/`
  - **theme/** – `AppColors`, `GreenCoinsTheme`
  - **data/** – `Screen`, `Mission`, `Reward`, challenges (same as original)
  - **components/** – `GlassCard`, `Header`, `BottomNav`, `ImageWithFallback`
  - **screens/** – Auth, Home, Shop, PlusFlow (all steps), Profile, Challenges, Help
  - **ui/** – `Icons` (MissionIcon → Material icons)
  - `GreenCoinsApp.kt` – Root composable and navigation state
  - `MainActivity.kt` – Activity entry point

## Tech stack

- **Kotlin 1.9**
- **Jetpack Compose** (Material3)
- **Coil** for image loading (with error fallback)
- **Android minSdk 26**, **targetSdk 34**
