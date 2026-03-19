package com.greencoins.app.data

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Screen type - preserved exactly
enum class Screen {
    Auth, Home, Shop, Plus, Challenges, Profile, MissionBrief, MissionUpload, MissionSuccess, ChallengeDetail, Help, CommunityVerification
}

enum class MissionIcon { TreePine, Recycle, Leaf, Users, Trash2, Zap }

@Serializable
data class UserProfile(
    val id: String,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    val city: String? = null,
    @SerialName("eco_score") val ecoScore: Int = 0,
    @SerialName("total_gc") val totalGc: Int = 0,
    val coins: Int = 0,
    val level: Int = 1,
    @SerialName("streak_count") val streakCount: Int = 0,
    @SerialName("missions_completed") val missionsCompleted: Int = 0,
    @SerialName("last_mission_date") val lastMissionDate: String? = null,
    @SerialName("global_rank") val globalRank: Int? = null,
)

@Serializable
data class Challenge(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("reward_gc") val rewardGc: Int = 0,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class Mission(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerialName("icon_type") val iconType: String,
    @SerialName("gc_reward") val gcReward: Int = 0,
    @SerialName("challenge_id") val challengeId: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val category: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val steps: List<String>? = null,
) {
    val icon: MissionIcon get() = try { MissionIcon.valueOf(iconType) } catch(e: Exception) { MissionIcon.Leaf }
    val instructionSteps: List<String> get() = steps ?: listOf(
        "Prepare for the mission action",
        "Perform the eco-friendly task",
        "Take a photo as proof",
    )
}

@Serializable
data class Submission(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("mission_id") val missionId: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("before_image_url") val beforeImageUrl: String? = null,
    @SerialName("after_image_url") val afterImageUrl: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("location_name") val locationName: String? = null,
    val status: String = "pending",
    @SerialName("rejected_reason") val rejectedReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null
)

@Serializable
data class Reward(
    val id: String,
    val title: String,
    val description: String? = null,
    val category: String,
    @SerialName("gc_cost") val gcCost: Int = 0,
    @SerialName("cash_price_paise") val cashPricePaise: Long = 0L,
    @SerialName("rupee_cost") val rupeeCost: Int? = null,
    @SerialName("gc_price") val gcPrice: Int = 0,
    val currency: String = "INR",
    @SerialName("requires_payment") val requiresPayment: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("discount_label") val discountLabel: String? = null,
    val stock: Int = -1,
    @SerialName("is_active") val isActive: Boolean = true,
) {
    val actualCashPricePaise: Long
        get() = if (rupeeCost != null && rupeeCost > 0) (rupeeCost * 100L) else cashPricePaise
}

@Serializable
data class Transaction(
    val id: String,
    @SerialName("user_id") val userId: String,
    val amount: Int,
    val description: String?,
    val type: String,
    @SerialName("created_at") val createdAt: String? = null
)


/** Completed mission entry for Impact Statistics. */
data class CompletedMissionItem(
    val submissionId: String,
    val missionTitle: String,
    val dateCompleted: String?,
    val gcEarned: Int,
    val imageUrl: String?,
)

/** Completed challenge entry for Impact Statistics. */
data class CompletedChallengeItem(
    val challengeId: String,
    val challengeName: String,
    val joinedDate: String?,
    val gcReward: Int,
)

@Serializable
data class ImpactUserChallengeRow(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("joined_at") val joinedAt: String? = null,
)

/** Unified challenge data for the detail screen. */
data class ChallengeDetailData(
    val id: String,
    val title: String,
    val img: String,
    val instructions: List<String>,
)

/** Leaderboard entry for challenge detail. */
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val coins: Int,
    val isCurrentUser: Boolean,
)

object ChallengeDetailRepository {
    private const val PLACEHOLDER_IMG = "https://images.unsplash.com/photo-1647220576336-f2e94680f3b8?q=80&w=400"

    // Adapter for Supabase Challenge -> UI Detail Data
    fun toDetail(challenge: Challenge) = ChallengeDetailData(
        id = challenge.id,
        title = challenge.title,
        img = challenge.coverImageUrl ?: PLACEHOLDER_IMG,
        instructions = listOf(
            "Read the challenge description: ${challenge.description ?: "Make an impact!"}",
            "Perform the required eco-friendly actions.",
            "Upload your proof in the Missions tab to earn GC!"
        )
    )
}

@Serializable
data class CouponCampaign(
    val id: String,
    val title: String,
    @SerialName("brand_name") val brandName: String,
    val description: String? = null,
    val terms: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("accent_color") val accentColor: String = "#FF0000",
    @SerialName("reward_type") val rewardType: String = "scratch_coupon",
    @SerialName("expires_at") val expiresAt: String? = null
)

@Serializable
data class CouponInventory(
    val id: String,
    @SerialName("campaign_id") val campaignId: String,
    @SerialName("coupon_code") val couponCode: String,
    @SerialName("is_assigned") val isAssigned: Boolean = false
)

@Serializable
data class UserCoupon(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("campaign_id") val campaignId: String,
    @SerialName("inventory_id") val inventoryId: String,
    val status: String,
    @SerialName("assigned_at") val assignedAt: String,
    @SerialName("scratched_at") val scratchedAt: String? = null,
    val campaign: CouponCampaign? = null,
    val inventory: CouponInventory? = null
)
