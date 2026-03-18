# GreenCoins — Comprehensive Project Context (Android + Supabase)

This document is a **self-contained technical context** for the GreenCoins project, written so another AI agent can immediately understand the system and continue development **without access to the codebase**.

---

## 1) Project Overview

**GreenCoins** is a **gamified sustainability platform** built as a native **Android app**. It motivates users to perform real-world eco actions (missions), submit proof (photos + notes), and earn a digital currency (“GreenCoins”) that can be redeemed for rewards.

- **Core idea**: turn sustainability into a **habit loop** (missions → streaks/levels → coin rewards → redemption).
- **Problem it solves**: encourages consistent eco-friendly behavior by making it measurable, rewarding, and engaging.
- **Motivation**: a “premium eco-tech” experience (Cyber‑Eco design language) rather than a basic tracker.
- **Key features (high-level)**:
  - Authentication (email + Google OAuth)
  - Mission browsing + mission completion flow
  - Proof submission (before/after images, notes)
  - Verification lifecycle (pending → verified/rejected) *(verification pipeline is currently not implemented in repo)*
  - Coin balance + transactions
  - Rewards shop + redemption
  - Levels + streaks (primarily driven by backend triggers)
  - Challenges + joining challenges + leaderboard (partially implemented)

---

## 2) Tech Stack

### Frontend (Android)
- **Language**: Kotlin
- **UI**: **Jetpack Compose** (Material 3 styling patterns)
- **Navigation**: **state-driven router** (custom) — not Jetpack Navigation:
  - Root holds `screen` state (enum) and swaps Composables via `AnimatedContent`.
  - Back behavior is handled manually with `BackHandler`.

### Backend (Supabase)
- **Supabase Auth**
  - Email/password signup + login
  - Google OAuth login via deeplinks (`greencoins://login`)
  - Session persistence via encrypted storage on device
- **Supabase Postgres**
  - Tables: users, missions, submissions, rewards, transactions, challenges, levels, etc.
  - **RLS** enabled broadly with policies restricting user-owned data.
  - Triggers/functions for:
    - creating a `public.users` profile row on Auth signup
    - awarding coins, streak updates, level-ups when submissions become `verified`
    - weekly streak RPC returning boolean array
- **Supabase Storage**
  - Used for mission proof images and avatar images
  - Note: schema includes bucket/policies for `avatar`; client uploads mission proofs to `mission-proofs` (bucket/policies must exist in Supabase or uploads will fail)

### Other / planned
- **AI verification**: UI references “AI verifying,” but **no edge-function or AI pipeline exists in this repo**. Verification currently requires external/admin action to mark submissions verified.

---

## 3) System Architecture

### Conceptual architecture
- **Android app (Compose UI + repositories)**  
  ↔ **Supabase client SDK**  
  ↔ **Supabase Auth / PostgREST / Storage**  
  ↔ **Postgres tables + RLS + triggers/functions**

### Data flow pattern
- UI triggers repository actions (suspend functions).
- Repositories call Supabase (PostgREST queries, Storage uploads, RPC).
- Data is decoded into Kotlin models (`@Serializable`).
- Compose state updates → recomposition.

### Authentication flow
- Supabase session is persisted locally via an encrypted session manager.
- `AuthRepository` exposes:
  - `isLoggedIn: StateFlow<Boolean?>` (null while initializing)
  - `currentUser` (reads `auth.currentUserOrNull()`)
- Root UI (`GreenCoinsApp`) shows a loading spinner until login state is known.

### Storage flow (images)
- Upload mission proof images to a Storage bucket:
  - **Bucket**: `mission-proofs` (client expects this)
  - Path: `"$userId/${timestamp}_before.jpg"` / `"$userId/${timestamp}_after.jpg"`
  - Returns public URL and stores it in `public.submissions`.

---

## 4) Core Features (Functional + Implementation Details)

### 4.1 Mission System
**What it is**: a list of eco actions with coin rewards.

**Implementation**
- Missions are fetched from `public.missions`.
- Mission shape includes: title/description/icon type/reward.
- Mission “steps” exist in DB (`steps jsonb`), but UI may use placeholders depending on screen.

**Key files**
- `app/src/main/java/com/greencoins/app/data/MissionRepository.kt`
- `app/src/main/java/com/greencoins/app/screens/HomeScreen.kt`
- `app/src/main/java/com/greencoins/app/screens/PlusFlow.kt`

### 4.2 Submission System (Proof of Impact)
**What it is**: user submits proof for a mission.

**Current UI behavior**
- User chooses a mission → “Proof of Impact” form:
  - Upload **Before** image
  - Upload **After** image
  - Enter **Location** (manual text input)
  - Enter **Description** (optional)
  - Confirm checkbox: “I confirm that this action was completed by me.”
  - Submit for verification

**Important constraints**
- Submission is inserted with `status = 'pending'`.
- No automatic verification happens in repo.

**Key files**
- `app/src/main/java/com/greencoins/app/screens/PlusFlow.kt`
- `app/src/main/java/com/greencoins/app/data/MissionRepository.kt`

### 4.3 Verification System
**Expected**: pending → verified/rejected.

**Backend logic exists**, but **pipeline is missing**:
- Postgres trigger logic expects `submissions.status` to become `'verified'`.
- The app currently never sets it to verified; it only inserts pending.
- Therefore, streak/level/coin updates only happen if an external actor updates submission status (admin tool, edge function, manual SQL, etc.).

### 4.4 Rewards System (Shop + Redemption)
**What it is**: users spend coins on rewards.

**Implementation**
- Rewards are read from `public.rewards` and optionally categorized via `reward_categories`.
- Redemption currently:
  - inserts a row into `public.transactions` (spend)
  - updates user coin balance client-side *(non-atomic; should ideally be a DB function)*

**Key files**
- `app/src/main/java/com/greencoins/app/data/ShopRepository.kt`
- `app/src/main/java/com/greencoins/app/screens/ShopScreen.kt`
- `app/src/main/java/com/greencoins/app/screens/CategoryRewardsScreen.kt`

### 4.5 Level System
**DB-driven system** (implemented in schema):
- `public.levels` defines:
  - `level_number`
  - `missions_required`
  - `coin_reward`
  - `title`
- Trigger updates `users.level` when missions completed reach thresholds.

**UI**
- Home includes a streak/progress card that shows **level + title** and “missions progress to next level.”
- Some of this is placeholder UI state (can be wired to backend later).

### 4.6 Streak System
**DB-driven streak calculation** (implemented in schema):
- `users.streak_count` + `users.last_mission_date`
- Updated by trigger when submission becomes verified.

**Weekly tracker**
- RPC function `get_user_streak(userId)` returns a boolean array for the past week based on `submissions.completed_at` and `status='verified'`.
- UI can use this to render a 7-day streak row.

---

## 5) Database Design (Supabase Postgres)

Source of truth: `supabase_schema.sql`

### 5.1 `public.users`
**Purpose**: user profile + progression + economy.

**Key columns**
- `id uuid PK` → `auth.users.id`
- Profile: `email`, `full_name`, `avatar_url`, `phone`, `city`
- Progression: `level`, `missions_completed`, `streak_count`, `last_mission_date`
- Economy:
  - `coins` = current spendable balance
  - `total_gc` = lifetime earned (analytics/leaderboards)

**Relationships**
- 1 user → many submissions (`submissions.user_id`)
- 1 user → many transactions (`transactions.user_id`)
- many-to-many challenges via `user_challenges`

### 5.2 `public.missions`
**Purpose**: defines missions and their rewards.

**Key columns**
- `gc_reward int`
- `icon_type text`
- `steps jsonb` (mission steps/instructions)
- optional `challenge_id` → `challenges.id`

### 5.3 `public.submissions`
**Purpose**: proof submissions for missions.

**Key columns**
- `user_id` → users.id
- `mission_id` → missions.id
- proof: `before_image_url`, `after_image_url`, (legacy) `image_url`, `description`
- lifecycle: `status` (pending/verified/rejected), `rejected_reason`
- timestamps: `created_at`, plus fields used by streak logic:
  - `verified_at`
  - `completed_at`
- geo: `latitude`, `longitude`, `location_name`

**Notes**
- **`completed_at` matters**: weekly streak RPC uses it to compute the last 7 days of verified activity.

### 5.4 `public.rewards`
**Purpose**: shop items users can redeem.

**Key columns**
- `category`, `gc_cost`, `image_url`, `discount_label`, `stock`, `is_active`

### 5.5 `public.transactions`
**Purpose**: immutable ledger of coin changes.

**Key columns**
- `user_id`, `amount`, `type`, `created_at`
- schema shows `reference_id uuid` for linking to other entities

**Important mismatch risk**
- Some client logic expects `related_reward_id`; schema uses `reference_id`. If your live DB differs from the SQL file, align them.

### 5.6 `public.challenges` + `public.user_challenges`
**Purpose**: events/competitions with joining and scoring.

- `challenges`: title/description/cover/reward/is_active
- `user_challenges`: (user_id, challenge_id) unique join table + `challenge_score`

### 5.7 `public.levels`
**Purpose**: defines level thresholds and titles.

---

## 6) Backend Logic (Triggers, Functions, Consistency)

### 6.1 New user bootstrap
- Trigger on `auth.users` insert → inserts `public.users` record.

### 6.2 Submission verification awards (core gamification)
When a submission transitions to `status='verified'`:
- Update streak:
  - compare `last_mission_date` to today/yesterday
- Increment `missions_completed`
- Add mission reward:
  - increment `users.coins`
  - increment `users.total_gc`
- Insert a transaction row for reward
- Apply level-ups using `public.levels`, granting `coin_reward` per level gained

### 6.3 Weekly streak RPC
- `get_user_streak(user_id)` returns boolean[] for last 7 days based on verified submissions and `completed_at`.

### 6.4 Data consistency / duplicate prevention
- Intended to be consistent via trigger-driven awarding.
- Redemption is currently client-side and **not atomic**; production should use a DB function to ensure:
  - balance check
  - insert transaction
  - decrement coins
  all in one transaction.

---

## 7) Frontend Structure

### 7.1 App entry + navigation
- `MainActivity.kt` sets Compose content to `GreenCoinsApp()`.
- `GreenCoinsApp.kt`:
  - holds screen state, plus nested flow state
  - swaps screens using `AnimatedContent`
  - handles back navigation manually

### 7.2 Main screens
- **Auth**: `AuthScreen.kt`
- **Home**: `HomeScreen.kt`
  - shows streak/level card (`StreakProgressCard`)
  - shows daily missions and active challenges
- **PlusFlow**: `PlusFlow.kt`
  - Selection → Brief → Upload (Proof of Impact form) → Success
- **Shop**: `ShopScreen.kt` + `CategoryRewardsScreen.kt`
- **Challenges**: `ChallengesScreen.kt` + `ChallengeDetailScreen.kt`
- **Profile**: `ProfileScreen.kt` + sub-screens for personal info, impact stats, redemption history
  - duplicate “Help & Support” row on profile was removed; header “?” help remains global

### 7.3 Key UI components
- `components/Header.kt` displays coin balance + top-right help icon.
- `components/GlassCard.kt` provides the “glass” card style.
- `HomeScreen.kt` contains `StreakProgressCard()` (Cyber‑Eco motivational card).

### 7.4 How frontend consumes backend data
- Repositories in `data/` expose suspend functions:
  - PostgREST: `client.from("table")...`
  - Storage: `client.storage.from(bucket).upload(...)`
  - RPC: `client.postgrest.rpc(...)` (for weekly streak)
- Compose screens call repositories in `LaunchedEffect` and store results in `remember { mutableStateOf(...) }`.

---

## 8) UI/UX Design System (Cyber‑Eco)

Design language goals:
- **Dark, high-contrast** surfaces
- **Neon green** accent representing eco action/energy
- **Rounded** shapes, premium spacing
- **Glass-like cards**, subtle shadows/glow
- **Minimal typography**, modern feel
- Motivational, gamified dashboards (streaks, level progress)

Implementation patterns in Compose:
- Dark backgrounds from theme (`AppColors.bg`, `AppColors.border`, etc.)
- Accent color `AppColors.accent` used for highlights/progress/checks
- Rounded shapes (`RoundedCornerShape(16–32dp)`)
- Subtle `shadow(...)` + semi-transparent gradients for glow effect

---

## 9) Data Flow Examples

### Example 1: User completes a mission
1. User selects a mission (Home → PlusFlow Brief).
2. User opens Proof of Impact form:
   - uploads before + after images
   - enters location (manual)
   - adds optional description
   - checks confirmation box
3. App uploads images to Supabase Storage `mission-proofs`.
4. App inserts a `submissions` row:
   - `status='pending'`
   - URLs + description
5. *(Missing in repo)* A verifier (admin/AI/edge function) marks submission `verified`.
6. Trigger runs:
   - updates streak/level
   - adds coins/total_gc
   - logs transactions
7. UI reflects updated coins/streak/level after data refresh.

### Example 2: User opens Home screen
1. App renders Home.
2. Fetches missions and challenges.
3. (If wired) fetch user streak/level/mission count.
4. Renders `StreakProgressCard`, mission cards, and challenges.

---

## 10) Current State of the Project

### Implemented (working)
- Compose app shell + state-driven navigation
- Supabase session persistence + Auth flows
- Mission list fetch + mission proof submission (pending)
- Storage uploads for proofs (requires bucket/policies)
- Shop + rewards listing + redemption (non-atomic)
- Profile + avatar upload (uses `avatar` bucket)
- Backend schema includes streak/level/coin triggers and weekly streak RPC

### Partially implemented / inconsistent
- Challenge progress + some challenge stats (placeholders)
- Reward redemption linkage may not match schema (`related_reward_id` vs `reference_id`)
- Coin semantics sometimes mix `coins` vs `total_gc` depending on UI/leaderboards

### Planned / missing
- Verification pipeline (AI/manual/admin) to mark submissions verified
- Atomic redeem function (RPC) for safe production economics
- Full NavHost migration for scalable navigation/backstack
- Backend-driven UI values for streak/level card (currently placeholder-friendly)

---

## 11) Known Limitations / Risks

- **No verification pipeline in repo**: without status updates to `verified`, streak/level/coin awarding won’t occur.
- **Non-atomic redemption**: client-side coin updates can be exploited or race.
- **Schema ↔ client mismatches**: transaction reference fields and bucket naming must be aligned to live DB.
- **Mission-proofs storage policies not included** in the SQL provided (only avatar policies are shown).
- **Some hardcoded UI** remains in multiple screens (featured reward copy, challenge stats, refresh timer label, etc.).
- **Navigation scalability**: `GreenCoinsApp.kt` accumulates responsibilities and manual back logic.

---

## 12) Future Improvements

- Add **Edge Function / worker** to process new submissions:
  - AI-assisted image validation (before/after plausibility, metadata checks)
  - human review queue fallback
  - then update `submissions.status` + timestamps
- Implement **atomic redemption RPC**:
  - `redeem_reward(user_id, reward_id)` checks balance, deducts coins, inserts transaction, returns updated balance
- Expand social/community layer:
  - leaderboard per challenge
  - friends + community missions
  - NGO partnerships and donation rewards
- Analytics:
  - real-time dashboards
  - cohort retention and streak health
- Navigation refactor to `NavHost` for deep linking and proper backstack

---

## Key Files Index (for fast orientation)

- **Root navigation / app state**: `app/src/main/java/com/greencoins/app/GreenCoinsApp.kt`
- **Supabase init**: `app/src/main/java/com/greencoins/app/data/SupabaseManager.kt`
- **Auth**: `app/src/main/java/com/greencoins/app/data/AuthRepository.kt`
- **Missions + submissions**: `app/src/main/java/com/greencoins/app/data/MissionRepository.kt`, `app/src/main/java/com/greencoins/app/screens/PlusFlow.kt`
- **Home**: `app/src/main/java/com/greencoins/app/screens/HomeScreen.kt` (includes `StreakProgressCard`)
- **Shop**: `app/src/main/java/com/greencoins/app/data/ShopRepository.kt`, `ShopScreen.kt`, `CategoryRewardsScreen.kt`
- **Profile**: `app/src/main/java/com/greencoins/app/screens/ProfileScreen.kt`
- **Schema**: `supabase_schema.sql`

