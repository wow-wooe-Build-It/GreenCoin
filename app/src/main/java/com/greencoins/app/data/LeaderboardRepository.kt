package com.greencoins.app.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class LeaderboardUserRow(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val email: String? = null,
    @SerialName("total_gc") val totalGc: Int = 0,
)

object LeaderboardRepository {
    private val client = SupabaseManager.client

    /**
     * Fetch leaderboard: top users by total_gc (coins), from the database.
     */
    suspend fun getChallengeLeaderboard(
        challengeId: String,
        currentUserId: String?,
        limit: Int = 10,
    ): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        try {
            val users = client.from("users")
                .select(columns = Columns.list("id", "full_name", "email", "total_gc")) {
                    order(column = "total_gc", order = Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<LeaderboardUserRow>()

            users.mapIndexed { index, row ->
                val displayName = row.fullName?.takeIf { it.isNotBlank() }
                    ?: row.email?.split("@")?.firstOrNull()?.replaceFirstChar { it.uppercase() }
                    ?: "User"
                LeaderboardEntry(
                    rank = index + 1,
                    username = displayName,
                    coins = row.totalGc,
                    isCurrentUser = row.id == currentUserId,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
