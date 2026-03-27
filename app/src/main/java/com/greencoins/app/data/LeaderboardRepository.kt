package com.greencoins.app.data

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ParticipationLeaderboardRow(
    @SerialName("user_id") val userId: String,
    @SerialName("coins_earned") val coinsEarned: Int,
)

@Serializable
private data class UserDisplayRow(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
)

object LeaderboardRepository {
    private const val TAG = "LeaderboardRepository"

    private val client = SupabaseManager.client

    /**
     * Top participants for this challenge by [challenge_participation.coins_earned] (not global user coins).
     */
    suspend fun getChallengeLeaderboard(
        challengeId: String,
        currentUserId: String?,
        limit: Int = 10,
    ): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        try {
            val rows = client.from("challenge_participation")
                .select(columns = Columns.list("user_id", "coins_earned")) {
                    filter { eq("challenge_id", challengeId) }
                    order(column = "coins_earned", order = Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ParticipationLeaderboardRow>()

            if (rows.isEmpty()) return@withContext emptyList()

            val userIds = rows.map { it.userId }.distinct()
            val users = client.from("users").select(columns = Columns.list("id", "full_name", "email")) {
                filter { isIn("id", userIds) }
            }.decodeList<UserDisplayRow>()
            val nameById = users.associateBy { it.id }

            rows.mapIndexed { index, row ->
                val u = nameById[row.userId]
                val displayName = u?.fullName?.takeIf { it.isNotBlank() }
                    ?: u?.email?.split("@")?.firstOrNull()?.replaceFirstChar { it.uppercaseChar() }
                    ?: "User"
                LeaderboardEntry(
                    rank = index + 1,
                    username = displayName,
                    coins = row.coinsEarned,
                    isCurrentUser = row.userId == currentUserId,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getChallengeLeaderboard failed", e)
            emptyList()
        }
    }
}
