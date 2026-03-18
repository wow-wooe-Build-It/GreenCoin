# GreenCoins Codebase Analysis

## 1. Project Overview
**GreenCoins** is an Android application designed to incentivize and reward users for environmental actions (such as planting trees or recycling). Users complete missions, upload proof, and earn "GreenCoins" (GC) which they can redeem in a virtual shop for real-world discounts and eco-friendly items.

Originally a React/TypeScript web app, it was ported natively into Android. The primary goal is maintaining the exact same UI flows, screens, and behaviors as the original MVP, now running as a native mobile client.

---

## 2. Tech Stack

- **Language:** Kotlin 1.9, SQL (PostgreSQL dialect for Supabase)
- **Framework:** Jetpack Compose (Material 3), Android SDK (minSdk 26, targetSdk 34)
- **Database:** Supabase (PostgreSQL)
- **Infrastructure:** Supabase Auth, Supabase Storage
- **Libraries:** Coil (for asynchronous image loading and fallback handling)

---

## 3. Repository Structure

The core application code is located in `app/src/main/java/com/greencoins/app/`.

- `/components` → Reusable Jetpack Compose UI elements (`GlassCard`, `Header`, `BottomNav`).
- `/screens` → The main view implementations (`AuthScreen`, `HomeScreen`, `ShopScreen`, `ProfileScreen`, `PlusFlow`).
- `/data` → Data structures (`UserProfile`, `Mission`, `Submission`, `Reward`) and dummy data repositories.
- `/theme` → Core design specifications (`AppColors`, typography, Theme wrappers).
- `/ui` → Icon sets and visual assets.
- `GreenCoinsApp.kt` → Root composable handling the app state and navigation.
- `supabase_schema.sql` (Root) → Provides the relational schema, tables, and Row-Level Security (RLS) policies for the backend.

---

## 4. System Architecture

**Pattern:** Monolithic Android Client interfacing with a Backend-as-a-Service (BaaS).
**Core UI Architecture:** Unidirectional Data Flow (UDF) typical of Jetpack Compose apps.

Instead of utilizing the standard Jetpack Navigation Component, the app employs a state-driven navigation architecture. A central top-level view (`GreenCoinsApp.kt`) holds the current visual state (`Screen` enum) and orchestrates transitions by swapping out underlying Composables based on user interactions. Business data and configurations rely heavily on Supabase for persistence. 

---

## 5. Execution Flow

1. **App Launch:** The OS instantiates `MainActivity.kt`.
2. **Context Setup:** The activity sets the content to the `GreenCoinsApp()` root composable.
3. **Authentication Check:** On launch, the `AuthRepository` dictates the initial state. If logged in, `Screen.Home` is rendered; otherwise, `Screen.Auth`.
4. **User Interaction:** The user taps a tab or button (e.g., initiating a mission in `HomeScreen`).
5. **State Mutation:** The event triggers a callback (e.g., `handleMissionSelect()`) which updates state variables in `GreenCoinsApp.kt` (`screen`, `plusStep`, `selectedMissionId`).
6. **Recomposition:** The UI reacts to the state changes dynamically fading out the old screen and fading in the new one via `AnimatedContent`.
7. **Data Persistence:** Submissions and transactions are pushed out to the Supabase endpoint.

---

## 6. Data Layer

- **Models:** Built using Koltin serialization (`UserProfile`, `Challenge`, `Mission`, `Submission`, `Reward`, `Transaction`).
- **Database Structure:** Represented by `supabase_schema.sql` which provisions tables for `users`, `missions`, `challenges`, `submissions`, `rewards`, and `transactions`.
- **Data Lifecycle:** User interactions on the frontend dispatch events that eventually translate to database operations. For example, signing up prompts a DB trigger (`handle_new_user()`) that instantly creates a matching profile record in the `users` table.

---

## 7. External Integrations

- **Supabase API:** Central infrastructure dealing with User Authentication, PostgreSQL storage queries, and blob storage (saving mission proof images and user avatars via public buckets).
- **Coil:** Third-party Android SDK used throughout the UI to fetch and display network images elegantly, handling potential loading fallbacks.

---

## 8. Key Business Logic

- **State Orchestration (`GreenCoinsApp.kt`):** Unlike a typical app with discrete ViewModels per fragment, a large bulk of the navigational flow and global UI state (like `coins` balance) is actively managed here.
- **Backend Policy Constraints (`supabase_schema.sql`):** Contains vital constraints like inserting standard profiles on auth events, and setting RLS constraints that ensure a given user can only read/manipulate their specific submissions and interactions. 

---

## 9. Developer Setup

For a new developer to get started:
1. **Clone the repository.**
2. **Install Dependencies:** Open the folder in Android Studio; allow Gradle (v8.7 wrapper) to sync automatically.
3. **Run the Project:** Select a suitable Android Emulator (running API 26 or newer) or connect a physical device, and hit **Run (`assembleDebug`)**.
4. **Environment Config:** Set up backend secrets (`SUPABASE_URL`, `SUPABASE_ANON_KEY`) in the environment if making live changes to the database structure.
5. **Testing Strategy:** The project currently relies on visual checkups via Android Studio's `@Preview` configurations on key screens. Automated testing frameworks (JUnit/Espresso) do not appear prominently configured yet.

---

## 10. Risks and Tech Debt

- **State-Based Navigation:** Directly mutating `Screen` enums works for an MVP but scales poorly. Transitioning to Android's Navigation component (`NavHost`) would resolve deep-linking and back-stack issues later.
- **Client-Determined Transactions:** The SQL schema highlights that `transactions` are currently instartable by the client directly (`Users can insert own transactions`). This is inherently insecure for production as users could easily fabricate an API request to grant themselves infinite GreenCoins.
- **Hardcoded Strings/Assets:** There is notable reliance on mock data strings and fallback placeholder images directly embedded into data classes rather than string resource XMLs.
- **"God" File Tendency:** `GreenCoinsApp.kt` holds a fairly vast amount of responsibility including shared global variables which creates tight coupling between view layers.

---

## 11. Clarifying Questions

- Will the application transition quickly toward Server-Side Validation (e.g. Supabase Edge Functions) for verifying missions and awarding coins to prevent client-side exploits?
- Where and how are the API keys/endpoints for the Supabase instance securely injected into the Android configuration (e.g., `local.properties` or `BuildConfig`)?
- Should we prioritize migrating the custom state-based router to a traditional `NavHost` layout, or maintain the React-styled global `Screen` state manager?
- What are the test coverage expectations before entering production?

---

## 12. Executive Summary

**GreenCoins** is a Kotlin-driven Android port of an eco-conscious web application that successfully replicates the original's visual and functional MVP flows. It leverages Jetpack Compose to orchestrate fluid, state-driven interfaces that incentivize users to perform, capture, and upload real-world sustainable tasks in exchange for digital tokens. Underpinned by a fast, robust Supabase architecture for authentication and data storage, the current build operates beautifully as a monolithic, tightly integrated frontend. Going forward, standardizing navigation paradigms and securely isolating transactional business logic to the backend will be critical steps in scaling the application for a broader commercial launch.
