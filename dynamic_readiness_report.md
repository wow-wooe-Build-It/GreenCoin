# GreenCoins Database & Dynamic Readiness Report

## Step 1 — Database Schema Analysis

| Table | Purpose | Used by Frontend? |
| :--- | :--- | :--- |
| `users` | User profiles, eco_score, total_gc, and sign-up tracking. | Yes (Partially) |
| `challenges` | Available eco-challenges with goals and coin rewards. | Yes |
| `missions` | Available eco-missions with definitions and coin rewards. | Yes |
| `submissions` | Uploaded proof of impact (before/after images, GPS, descriptions). | Yes (Partially) |
| `rewards` | Shop items mapping to GC cost and discount labels. | Yes |
| `transactions` | Coin history tracking earnings and expenses. | No |

---

## Step 2 — Supabase Queries in the Codebase

| File | Table | Data Fetched / Mutated | UI Usage |
| :--- | :--- | :--- | :--- |
| [MissionRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/MissionRepository.kt) | `missions` | Select all missions, select mission by ID | [HomeScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt#57-342), [PlusFlow](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/PlusFlow.kt#66-114) |
| [MissionRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/MissionRepository.kt) | `submissions` | Insert new proof submission | [PlusFlow](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/PlusFlow.kt#66-114) |
| [UserRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/UserRepository.kt) | `users` | Select profile by ID (coins, name, avatar, eco_score) | [HomeScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt#57-342), [ProfileScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ProfileScreen.kt#44-183) |
| [UserRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/UserRepository.kt) | `users` | Update profile details (phone, full_name) | Not currently exposed |
| [ChallengeRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/ChallengeRepository.kt) | `challenges` | Select all active challenges, select challenge by ID | [HomeScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt#57-342) |
| [ShopRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/ShopRepository.kt) | `rewards` | Select all shop rewards | [ShopScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ShopScreen.kt#50-99) |
| [ShopRepository.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/ShopRepository.kt) | `transactions` | Insert new redemption transaction (auto-updates `users.total_gc` via Triggers theoretically) | [ShopScreen](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ShopScreen.kt#50-99) |

---

## Step 3 — Hardcoded Data Detection

| File | Hardcoded Data | Used In | Should Come From |
| :--- | :--- | :--- | :--- |
| [HomeScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt) | `userProfile?.ecoScore ?: 0` | Impact Dashboard Eco-Score Tracker | `users` table |
| [HomeScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt) | `12 Trees`, `5kg Recycled`, `140kg CO2` | Impact Dashboard Environmental Stats | `users` or aggregate from `submissions` |
| [HomeScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/HomeScreen.kt) | Daily Missions Refresh Time (`NEW REFRESH IN 4H`) | Missions Header | System Config / Time offset |
| [GreenCoinsApp.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/GreenCoinsApp.kt) | `var coins by remember { mutableStateOf(8420) }` | Global Header UI | `users.total_gc` via Auth flow |
| [ProfileScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ProfileScreen.kt) | `LVL 24`, `Total Earned: 12,450`, `Global Rank: #128` | Profile Screen User Stats | `users` and `transactions` aggregations |
| [ChallengeDetailScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ChallengeDetailScreen.kt) | [getMockLeaderboard(id)](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/Models.kt#111-118) | Challenge Top Agents | `users` table sorted by `eco_score` |
| [ChallengeDetailScreen.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/ChallengeDetailScreen.kt) | `Participants: 1.2k`, `Days Left: 12` | Challenge Statistics | Computed from `challenges.end_date` |
| [PlusFlow.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/PlusFlow.kt) | `GPS: 28.6139° N, 77.2090° E` | Mission Proof Upload Location | EXIF logic parsing local image |
| [PlusFlow.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/PlusFlow.kt) | Brief Instructions (3 Steps) | Mission Briefing view | A new `mission_steps` table or JSON array in `missions` |

---

## Step 4 — Screen-by-Screen Data Mapping

| Screen | Data Shown | Current Source | Ideal Source |
| :--- | :--- | :--- | :--- |
| **Home** | `missions` list | Supabase (`missions` table) | `missions` table |
| **Home** | `challenges` list | Supabase (`challenges` table) | `challenges` table |
| **Home** | Impact Stats (Trees, CO2, etc.) | Hardcoded | Computed `submissions` metrics |
| **Shop** | `rewards` catalog | Supabase (`rewards` table) | `rewards` table |
| **Profile** | Rank, Level, Total Earned | Hardcoded | `users` and `transactions` |
| **Profile** | User details (Email, Name) | Supabase Auth Metadata | [Auth](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/AuthScreen.kt#56-330) & `users` profile |
| **ChallengeDetail** | Challenge Info | Supabase (`challenges` table) | `challenges` table |
| **ChallengeDetail** | Leaderboard | Hardcoded [getMockLeaderboard()](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/Models.kt#111-118) | `users` table ranking |
| **PlusFlow** | Mission List | Supabase (`missions` table) | `missions` table |
| **PlusFlow** | Steps & GPS Location | Hardcoded Mock Map/Strings | EXIF parsing and DB step logic |

---

## Step 5 — Dynamic Readiness Summary

### **Already Dynamic (Production Ready)**
*   **Authentication Flow** is fully backed by Supabase (Google OAuth & Email).
*   **HomeScreen feeds** properly read `missions` and `challenges` from the backend.
*   **ShopScreen catalog** retrieves real `rewards` from the DB.
*   **Mission Submission ([PlusFlow](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/screens/PlusFlow.kt#66-114))** correctly uploads image proofs to Supabase Storage and inserts records into `submissions`.

### **Hardcoded but Should Be Dynamic**
*   **The global `coins` header:** Currently hardcoded to `8420` in [GreenCoinsApp.kt](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/GreenCoinsApp.kt) and `Header.kt`. Needs to subscribe to `UserRepository.getProfile(userId).totalGc`.
*   **Ecoscore and Impact Metrics:** Hardcoded on the HomeScreen and ProfileScreen.
*   **Leaderboards:** Challenge leaderboards use a locally seeded data class array ([getMockLeaderboard](file:///Users/kushagramehta/Documents/GreenCoin%20Last%20Stable/app/src/main/java/com/greencoins/app/data/Models.kt#111-118)). Lookups should fetch real users ordered by `eco_score`.
*   **GPS Exif Data:** Currently mocked physically to New Delhi coordinates.
*   **Mission Steps:** The Mission Brief hardcodes "Prepare, Perform, Proof".

### **Missing Backend Support (Architectural Gap)**
*   **Active Challenges Engagement:** There is no schema linking a specific user to a currently joined challenge. Adding a `user_challenges` lookup table would be required to track which challenges a user has explicitly joined and track their progress accurately.
*   **Detailed Impact Parameters:** The DB `users` table tracks global `eco_score` and `total_gc`, but there are no columns or lookup tables dedicated to tracking individual carbon metrics like `trees_planted`, `plastic_recycled_kg`, or `co2_saved_kg`—meaning the UI cannot fetch these without schema modifications or heavy JSON payload parsing.
