# GreenCoins Database Migration Summary

## Overview

This migration makes the GreenCoins app **fully database-driven**. All dynamic content now comes from Supabase instead of hardcoded values.

---

## Step 1 — Hardcoded Data Identified (from Dynamic Readiness Report)

| Feature Area | Hardcoded Item | Now Sourced From |
|-------------|----------------|------------------|
| **Header** | Coins balance (8420) | `users.coins` via UserCoinsRepository |
| **Profile** | LVL 24, Total Earned 12,450, Global Rank #128 | `users` table (level, total_gc, global_rank) |
| **Profile** | Avatar URL | `users.avatar_url` |
| **Home** | Impact stats (12 Trees, 5kg Recycled, 140kg CO₂) | `users` (trees_planted, plastic_recycled_kg, co2_saved_kg) |
| **Shop** | Categories list | `reward_categories` table or distinct from `rewards` |
| **Challenge Detail** | Leaderboard (getMockLeaderboard) | `users` table via LeaderboardRepository |
| **Challenge Join** | joinedChallengeIds (in-memory) | `user_challenges` table |
| **Help** | FAQ list | `faq` table |
| **Plus Flow** | Mission brief steps | `missions.steps` (jsonb) |

---

## Step 2 — Database Schema Updates

### New/Updated Tables

- **users**: Added `trees_planted`, `plastic_recycled_kg`, `co2_saved_kg`, `level`, `global_rank`, `updated_at`
- **missions**: Added `image_url`, `category`, `is_active`, `steps` (jsonb), `updated_at`
- **rewards**: Added `stock`, `is_active`, `updated_at`
- **challenges**: Added `updated_at`
- **user_challenges** (NEW): Tracks which users joined which challenges
- **faq** (NEW): Help/FAQ content
- **reward_categories** (NEW): Shop category definitions

---

## Step 3 — Seed Data

The schema includes seed data for:
- Reward categories (Travel, Eco Store, Lifestyle, Direct Donate, Food & Beverage)
- Missions (6 missions with steps)
- Challenges (4 challenges)
- Rewards (9 rewards)
- FAQ (3 items)

---

## Step 4 & 5 — Repository Layer

### New Repositories
- **LeaderboardRepository** – Fetches leaderboard from `users` ordered by `total_gc`
- **FaqRepository** – Fetches FAQ items from `faq` table
- **UserChallengesRepository** – Tracks joined challenges, persists to `user_challenges`

### Updated Repositories
- **ShopRepository** – `getCategories()` fetches from `reward_categories` or derives from rewards
- **UserRepository** – `getProfile()` now includes coins, level, impact stats, global_rank

### Models Updated
- **UserProfile** – Added coins, treesPlanted, plasticRecycledKg, co2SavedKg, level, globalRank
- **Mission** – Added imageUrl, category, isActive, steps, instructionSteps
- **Reward** – Added stock, isActive

---

## Step 6 — UI Updates

| Screen | Changes |
|--------|---------|
| **GreenCoinsApp** | Coins from UserCoinsRepository; joinedChallengeIds from UserChallengesRepository; shopCategories from ShopViewModel StateFlow |
| **ShopScreen** | Categories from ShopViewModel (async from DB) |
| **ProfileScreen** | Level, Total Earned, Global Rank, Avatar from UserRepository.getProfile() |
| **HomeScreen** | Impact stats (Trees, Recycled, CO₂) from userProfile |
| **ChallengeDetailScreen** | Leaderboard from LeaderboardRepository; join persists to user_challenges |
| **HelpScreen** | FAQ from FaqRepository (fallback to default if empty) |
| **PlusFlow** | Mission steps from mission.instructionSteps (DB) |

---

## Removed Hardcoded Data

- `ShopRepository.categories` (listOf)
- `ChallengeDetailRepository.getMockLeaderboard()`
- `GreenCoinsApp` coins initial value (now from DB)
- `ProfileScreen` LVL 24, 12,450, #128
- `HomeScreen` 12 Trees, 5kg, 140kg
- `HelpScreen` FAQ listOf
- `PlusFlow` mission brief listOf

---

## Setup Steps to Run the Updated App

1. **Run the schema migration** in Supabase:
   - Open Supabase Dashboard → SQL Editor
   - Paste and run the contents of `supabase_schema.sql`
   - This creates/updates tables and seeds data

2. **Ensure Storage buckets exist**:
   - `mission-proofs` (for mission proof uploads)
   - `avatars` (for user avatars)

3. **For existing databases**: The schema uses `ALTER TABLE ADD COLUMN IF NOT EXISTS` for backward compatibility. New tables (`user_challenges`, `faq`, `reward_categories`) will be created. Seed data only inserts when tables are empty.

4. **Build and run** the Android app as usual. All dynamic content will load from Supabase.

---

## Validation Checklist

- [x] No mission/reward/challenge data hardcoded in codebase
- [x] All dynamic content fetched from Supabase
- [x] Coins from users.coins
- [x] Profile stats from users table
- [x] Leaderboard from users table
- [x] Categories from reward_categories or rewards
- [x] FAQ from faq table
- [x] Mission steps from missions.steps
- [x] Challenge join state persisted to user_challenges
