package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImpactRepository {
    private val client = SupabaseManager.client

    /** Fetch completed missions (submissions with approved/completed status) for the user. */
    suspend fun getCompletedMissions(userId: String): List<CompletedMissionItem> = withContext(Dispatchers.IO) {
        try {
            val submissions = client.from("submissions").select {
                filter { eq("user_id", userId) }
                order(column = "created_at", order = Order.DESCENDING)
            }.decodeList<Submission>()
            val allMissions = MissionRepository.getMissions()
            val missionMap = allMissions.associateBy { it.id }
            submissions
                .filter { it.status in listOf("approved", "completed") }
                .mapNotNull { sub ->
                    val mission = missionMap[sub.missionId] ?: return@mapNotNull null
                    CompletedMissionItem(
                        submissionId = sub.id,
                        missionTitle = mission.title,
                        dateCompleted = sub.createdAt,
                        gcEarned = mission.gcReward,
                        imageUrl = sub.imageUrl ?: mission.imageUrl,
                    )
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Fetch challenges the user has joined (participated in). */
    suspend fun getCompletedChallenges(userId: String): List<CompletedChallengeItem> = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("user_challenges").select {
                filter { eq("user_id", userId) }
                order(column = "joined_at", order = Order.DESCENDING)
            }.decodeList<ImpactUserChallengeRow>()
            if (rows.isEmpty()) return@withContext emptyList()
            val allChallenges = ChallengeRepository.getAllChallenges()
            val challengeMap = allChallenges.associateBy { it.id }
            rows.mapNotNull { row ->
                val c = challengeMap[row.challengeId] ?: return@mapNotNull null
                CompletedChallengeItem(
                    challengeId = c.id,
                    challengeName = c.title,
                    joinedDate = row.joinedAt,
                    gcReward = c.rewardGc,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
